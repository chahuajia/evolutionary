package com.evolutionary.settlement.interfaces;

import com.evolutionary.settlement.application.AccrueOnOrderCompleted;
import com.evolutionary.settlement.application.OrderCompletedFact;
import com.evolutionary.settlement.application.OrderRefundedFact;
import com.evolutionary.settlement.application.ReverseAccrualsOnRefund;
import com.evolutionary.settlement.application.RunSettlementBatch;
import com.evolutionary.settlement.domain.ProfitShareAccrual;
import com.evolutionary.settlement.domain.SettlementBatch;
import com.evolutionary.settlement.domain.SettlementException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 结算 HTTP（切片21a / phase-4 Accrue · Reverse · Batch）。
 *
 * <p>FE 契约：
 *
 * <ul>
 *   <li>{@code POST /settlement/accruals} body {@code {orderId, orgId, amountCents, completedAt?,
 *       userId?, currency?}} → PENDING Accrual 列表
 *   <li>{@code POST /settlement/batches} body {@code {periodStart, periodEnd}} → CLOSED Batch
 *   <li>{@code POST /settlement/orders/{orderId}/reverse-accruals} → REVERSED + reversal 行（AC-35）；已
 *       SETTLED → 422 {@code ORDER_NOT_REFUNDABLE_SETTLED}（AC-36）
 * </ul>
 *
 * <p>另：{@code POST /commerce/orders/{id}/refund} 成功后自动 {@link ReverseAccrualsOnRefund}（同 tick）。
 */
@RestController
@RequestMapping("/settlement")
public class SettlementController {

    private final AccrueOnOrderCompleted accrueOnOrderCompleted;
    private final ReverseAccrualsOnRefund reverseAccrualsOnRefund;
    private final RunSettlementBatch runSettlementBatch;
    private final Clock clock = Clock.systemUTC();

    public SettlementController(
            AccrueOnOrderCompleted accrueOnOrderCompleted,
            ReverseAccrualsOnRefund reverseAccrualsOnRefund,
            RunSettlementBatch runSettlementBatch) {
        this.accrueOnOrderCompleted = accrueOnOrderCompleted;
        this.reverseAccrualsOnRefund = reverseAccrualsOnRefund;
        this.runSettlementBatch = runSettlementBatch;
    }

    @PostMapping("/accruals")
    public ResponseEntity<List<AccrualView>> accrue(@RequestBody AccrueRequest body) {
        if (body == null) {
            throw new IllegalArgumentException("body required");
        }
        if (body.orderId() == null || body.orderId().isBlank()) {
            throw new IllegalArgumentException("orderId required");
        }
        if (body.orgId() == null || body.orgId().isBlank()) {
            throw new IllegalArgumentException("orgId required");
        }
        if (body.amountCents() == null) {
            throw new IllegalArgumentException("amountCents required");
        }
        String userId =
                body.userId() == null || body.userId().isBlank() ? "U1" : body.userId().trim();
        String currency =
                body.currency() == null || body.currency().isBlank()
                        ? "CNY"
                        : body.currency().trim();
        Instant completedAt =
                body.completedAt() == null ? clock.instant() : Instant.parse(body.completedAt());

        List<ProfitShareAccrual> created =
                accrueOnOrderCompleted.execute(
                        new OrderCompletedFact(
                                body.orderId().trim(),
                                userId,
                                body.orgId().trim(),
                                body.amountCents(),
                                currency,
                                completedAt));
        return ResponseEntity.ok(created.stream().map(SettlementController::toAccrual).toList());
    }

    @PostMapping("/batches")
    public ResponseEntity<BatchView> runBatch(@RequestBody BatchRequest body) {
        if (body == null) {
            throw new IllegalArgumentException("body required");
        }
        if (body.periodStart() == null || body.periodStart().isBlank()) {
            throw new IllegalArgumentException("periodStart required");
        }
        if (body.periodEnd() == null || body.periodEnd().isBlank()) {
            throw new IllegalArgumentException("periodEnd required");
        }
        Instant periodStart = Instant.parse(body.periodStart());
        Instant periodEnd = Instant.parse(body.periodEnd());
        SettlementBatch batch =
                runSettlementBatch.execute(periodStart, periodEnd, clock.instant());
        return ResponseEntity.ok(toBatch(batch));
    }

    /** AC-35 / AC-36：结算前冲销；已 SETTLED → 422。 */
    @PostMapping("/orders/{orderId}/reverse-accruals")
    public ResponseEntity<List<AccrualView>> reverse(@PathVariable String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId required");
        }
        List<ProfitShareAccrual> written =
                reverseAccrualsOnRefund.execute(
                        new OrderRefundedFact(orderId.trim(), clock.instant()));
        return ResponseEntity.ok(written.stream().map(SettlementController::toAccrual).toList());
    }

    @ExceptionHandler(SettlementException.class)
    public ResponseEntity<ApiError> handleSettlement(SettlementException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiError(ex.code().name(), ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex) {
        String msg = ex.getMessage() == null ? "bad request" : ex.getMessage();
        return ResponseEntity.badRequest().body(new ApiError(msg, null));
    }

    @ExceptionHandler(java.time.format.DateTimeParseException.class)
    public ResponseEntity<ApiError> handleParse(java.time.format.DateTimeParseException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiError("invalid instant: " + ex.getParsedString(), null));
    }

    private static AccrualView toAccrual(ProfitShareAccrual a) {
        return new AccrualView(
                a.id(),
                a.orderId(),
                a.orgId(),
                a.amountCents(),
                a.currency(),
                a.status().name(),
                a.batchId(),
                a.reversalOf(),
                a.createdAt().toString());
    }

    private static BatchView toBatch(SettlementBatch b) {
        return new BatchView(
                b.id(),
                b.periodStart().toString(),
                b.periodEnd().toString(),
                b.status().name(),
                b.createdAt().toString(),
                b.closedAt() == null ? null : b.closedAt().toString());
    }

    /** body：orderId / orgId(售卖方) / amountCents；completedAt · userId · currency 可选。 */
    public record AccrueRequest(
            String orderId,
            String orgId,
            Long amountCents,
            String completedAt,
            String userId,
            String currency) {}

    public record BatchRequest(String periodStart, String periodEnd) {}

    public record AccrualView(
            String id,
            String orderId,
            String orgId,
            long amountCents,
            String currency,
            String status,
            String batchId,
            String reversalOf,
            String createdAt) {}

    public record BatchView(
            String id,
            String periodStart,
            String periodEnd,
            String status,
            String createdAt,
            String closedAt) {}

    public record ApiError(String error, String suggestion) {}
}
