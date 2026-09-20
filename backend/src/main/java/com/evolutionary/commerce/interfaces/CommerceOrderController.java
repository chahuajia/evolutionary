package com.evolutionary.commerce.interfaces;

import com.evolutionary.commerce.application.OrderRepository;
import com.evolutionary.commerce.application.RefundOrder;
import com.evolutionary.commerce.application.RefundResult;
import com.evolutionary.commerce.domain.DomainOutcome;
import com.evolutionary.commerce.domain.Order;
import com.evolutionary.credit.application.CreditLedgerDebtRepository;
import com.evolutionary.credit.application.CreditProfileRepository;
import com.evolutionary.credit.domain.CreditLedgerDebt;
import com.evolutionary.credit.domain.CreditProfile;
import com.evolutionary.settlement.application.OrderRefundedFact;
import com.evolutionary.settlement.application.ReverseAccrualsOnRefund;
import com.evolutionary.settlement.domain.SettlementException;
import java.time.Clock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单退款 HTTP（切片20a / AC-20 / INV-9）。
 *
 * <p>契约：{@code POST /commerce/orders/{orderId}/refund} → orderId / status /
 * revokedEntitlementId。信用购订单额外核销 OPEN 负债并归还 usedCredit。
 *
 * <p>切片21a：退款成功后同 tick 调用 {@link ReverseAccrualsOnRefund}（AC-35）；已 SETTLED → 422
 * {@code ORDER_NOT_REFUNDABLE_SETTLED}（AC-36）。
 */
@RestController
@RequestMapping("/commerce/orders")
public class CommerceOrderController {

    private final OrderRepository orders;
    private final RefundOrder refundOrder;
    private final CreditLedgerDebtRepository debts;
    private final CreditProfileRepository profiles;
    private final ReverseAccrualsOnRefund reverseAccrualsOnRefund;
    private final Clock clock = Clock.systemUTC();

    public CommerceOrderController(
            OrderRepository orders,
            RefundOrder refundOrder,
            CreditLedgerDebtRepository debts,
            CreditProfileRepository profiles,
            ReverseAccrualsOnRefund reverseAccrualsOnRefund) {
        this.orders = orders;
        this.refundOrder = refundOrder;
        this.debts = debts;
        this.profiles = profiles;
        this.reverseAccrualsOnRefund = reverseAccrualsOnRefund;
    }

    /** 只读：供退款岛对齐 refundAllowed（仅 PAID）。 */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderView> order(@PathVariable String orderId) {
        return orders
                .findById(orderId.trim())
                .map(CommerceOrderController::toView)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{orderId}/refund")
    public ResponseEntity<?> refund(@PathVariable String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId required");
        }
        DomainOutcome<RefundResult> outcome = refundOrder.execute(orderId.trim());
        if (outcome instanceof DomainOutcome.Ok<RefundResult> ok) {
            RefundResult result = ok.value();
            reverseCreditIfPresent(result.order().id(), result.order().userId());
            reverseAccrualsOnRefund.execute(
                    new OrderRefundedFact(result.order().id(), clock.instant()));
            return ResponseEntity.ok(
                    new RefundResponse(
                            result.order().id(),
                            result.order().status().name(),
                            result.entitlement().id()));
        }
        DomainOutcome.Err<RefundResult> err = (DomainOutcome.Err<RefundResult>) outcome;
        CommerceApiErrorTranslator.Translated translated =
                CommerceApiErrorTranslator.translate(err);
        return ResponseEntity.status(translated.status()).body(translated.body());
    }

    /** 信用购接线：OPEN debt → WRITTEN_OFF，并 repay usedCredit。 */
    private void reverseCreditIfPresent(String orderId, String userId) {
        for (CreditLedgerDebt debt : debts.findByOrderId(orderId)) {
            if (debt.status() != CreditLedgerDebt.Status.OPEN) {
                continue;
            }
            debts.save(debt.writeOff());
            CreditProfile profile = profiles.get(userId);
            profiles.save(profile.repay(debt.amount()));
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommerceApiErrorTranslator.ApiError> handleBadRequest(
            IllegalArgumentException ex) {
        String msg = ex.getMessage() == null ? "bad request" : ex.getMessage();
        return ResponseEntity.badRequest()
                .body(new CommerceApiErrorTranslator.ApiError(msg, null));
    }

    @ExceptionHandler(SettlementException.class)
    public ResponseEntity<CommerceApiErrorTranslator.ApiError> handleSettlement(
            SettlementException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new CommerceApiErrorTranslator.ApiError(ex.code().name(), ex.getMessage()));
    }

    public record RefundResponse(String orderId, String status, String revokedEntitlementId) {}

    public record OrderView(String orderId, String userId, String status) {}

    private static OrderView toView(Order order) {
        return new OrderView(order.id(), order.userId(), order.status().name());
    }
}
