package com.evolutionary.credit.interfaces;

import com.evolutionary.commerce.domain.Money;
import com.evolutionary.credit.application.ApplyCreditPolicyDowngrade;
import com.evolutionary.credit.application.BillingStatementRepository;
import com.evolutionary.credit.application.CreditProfileRepository;
import com.evolutionary.credit.application.CreditPurchaseResult;
import com.evolutionary.credit.application.MarkCreditOverdue;
import com.evolutionary.credit.application.PurchaseWithCredit;
import com.evolutionary.credit.application.RepayBillingStatement;
import com.evolutionary.credit.application.RunMonthlyBilling;
import com.evolutionary.credit.domain.BillingStatement;
import com.evolutionary.credit.domain.CreditOutcome;
import com.evolutionary.credit.domain.CreditProfile;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 信用 HTTP（档案/账单 + 信用购 + 逾期冻权益 + 还款解冻）。
 *
 * <p>JSON 字段对齐 phase-6 IDL / 前端 types（status 小写 good|overdue|frozen）。
 */
@RestController
@RequestMapping("/credit")
public class CreditController {

    private static final DateTimeFormatter DAY =
            DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC);

    private final CreditProfileRepository profiles;
    private final BillingStatementRepository statements;
    private final PurchaseWithCredit purchaseWithCredit;
    private final MarkCreditOverdue markCreditOverdue;
    private final RepayBillingStatement repayBillingStatement;
    private final RunMonthlyBilling runMonthlyBilling;
    private final ApplyCreditPolicyDowngrade applyCreditPolicyDowngrade;

    public CreditController(
            CreditProfileRepository profiles,
            BillingStatementRepository statements,
            PurchaseWithCredit purchaseWithCredit,
            MarkCreditOverdue markCreditOverdue,
            RepayBillingStatement repayBillingStatement,
            RunMonthlyBilling runMonthlyBilling,
            ApplyCreditPolicyDowngrade applyCreditPolicyDowngrade) {
        this.profiles = profiles;
        this.statements = statements;
        this.purchaseWithCredit = purchaseWithCredit;
        this.markCreditOverdue = markCreditOverdue;
        this.repayBillingStatement = repayBillingStatement;
        this.runMonthlyBilling = runMonthlyBilling;
        this.applyCreditPolicyDowngrade = applyCreditPolicyDowngrade;
    }

    @GetMapping("/profiles/{userId}")
    public ResponseEntity<CreditProfileView> profile(@PathVariable String userId) {
        return profiles
                .findByUserId(userId)
                .map(CreditController::toProfile)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/profiles/{userId}/statements")
    public ResponseEntity<List<BillingStatementView>> statements(@PathVariable String userId) {
        if (profiles.findByUserId(userId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<BillingStatementView> views =
                statements.findByUserId(userId).stream().map(CreditController::toStatement).toList();
        return ResponseEntity.ok(views);
    }

    @PostMapping("/purchases")
    public ResponseEntity<?> purchase(@RequestBody PurchaseRequest body) {
        if (body == null || body.userId() == null || body.userId().isBlank()) {
            throw new IllegalArgumentException("userId required");
        }
        if (body.productId() == null || body.productId().isBlank()) {
            throw new IllegalArgumentException("productId required");
        }
        CreditOutcome<CreditPurchaseResult> outcome =
                purchaseWithCredit.execute(body.userId().trim(), body.productId().trim());
        if (outcome instanceof CreditOutcome.Ok<CreditPurchaseResult> ok) {
            return ResponseEntity.ok(toPurchase(ok.value()));
        }
        CreditOutcome.Err<CreditPurchaseResult> err =
                (CreditOutcome.Err<CreditPurchaseResult>) outcome;
        CreditApiErrorTranslator.Translated translated = CreditApiErrorTranslator.translate(err);
        return ResponseEntity.status(translated.status()).body(translated.body());
    }

    @PostMapping("/profiles/{userId}/mark-overdue")
    public ResponseEntity<?> markOverdue(
            @PathVariable String userId, @RequestBody MarkOverdueRequest body) {
        if (body == null || body.statementId() == null || body.statementId().isBlank()) {
            throw new IllegalArgumentException("statementId required");
        }
        CreditOutcome<CreditProfile> outcome =
                markCreditOverdue.execute(userId, body.statementId().trim());
        if (outcome instanceof CreditOutcome.Ok<CreditProfile> ok) {
            return ResponseEntity.ok(toProfile(ok.value()));
        }
        CreditOutcome.Err<CreditProfile> err = (CreditOutcome.Err<CreditProfile>) outcome;
        CreditApiErrorTranslator.Translated translated = CreditApiErrorTranslator.translate(err);
        return ResponseEntity.status(translated.status()).body(translated.body());
    }

    @PostMapping("/profiles/{userId}/repay")
    public ResponseEntity<?> repay(@PathVariable String userId, @RequestBody RepayRequest body) {
        if (body == null || body.statementId() == null || body.statementId().isBlank()) {
            throw new IllegalArgumentException("statementId required");
        }
        if (body.amountCents() == null || body.amountCents() <= 0) {
            throw new IllegalArgumentException("amountCents required");
        }
        CreditOutcome<BillingStatement> outcome =
                repayBillingStatement.execute(
                        userId, body.statementId().trim(), Money.cny(body.amountCents()));
        if (outcome instanceof CreditOutcome.Ok<BillingStatement> ok) {
            return ResponseEntity.ok(toStatement(ok.value()));
        }
        CreditOutcome.Err<BillingStatement> err = (CreditOutcome.Err<BillingStatement>) outcome;
        CreditApiErrorTranslator.Translated translated = CreditApiErrorTranslator.translate(err);
        return ResponseEntity.status(translated.status()).body(translated.body());
    }

    @PostMapping("/profiles/{userId}/monthly-billing")
    public ResponseEntity<?> monthlyBilling(
            @PathVariable String userId, @RequestBody MonthlyBillingRequest body) {
        Instant periodStart = parseInstant(body == null ? null : body.periodStart(), "periodStart");
        Instant periodEnd = parseInstant(body == null ? null : body.periodEnd(), "periodEnd");
        CreditOutcome<BillingStatement> outcome =
                runMonthlyBilling.execute(userId, periodStart, periodEnd);
        if (outcome instanceof CreditOutcome.Ok<BillingStatement> ok) {
            return ResponseEntity.ok(toStatement(ok.value()));
        }
        CreditOutcome.Err<BillingStatement> err = (CreditOutcome.Err<BillingStatement>) outcome;
        CreditApiErrorTranslator.Translated translated = CreditApiErrorTranslator.translate(err);
        return ResponseEntity.status(translated.status()).body(translated.body());
    }

    /** 政策降额应用到档案（AC-54）；不清零 usedCredit。 */
    @PostMapping("/profiles/{userId}/apply-policy")
    public ResponseEntity<?> applyPolicy(
            @PathVariable String userId, @RequestBody ApplyPolicyRequest body) {
        if (body == null || body.policyVersion() == null) {
            throw new IllegalArgumentException("policyVersion required");
        }
        CreditOutcome<CreditProfile> outcome =
                applyCreditPolicyDowngrade.execute(userId, body.policyVersion());
        if (outcome instanceof CreditOutcome.Ok<CreditProfile> ok) {
            return ResponseEntity.ok(toProfile(ok.value()));
        }
        CreditOutcome.Err<CreditProfile> err = (CreditOutcome.Err<CreditProfile>) outcome;
        CreditApiErrorTranslator.Translated translated = CreditApiErrorTranslator.translate(err);
        return ResponseEntity.status(translated.status()).body(translated.body());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CreditApiErrorTranslator.ApiError> handleBadRequest(
            IllegalArgumentException ex) {
        String msg = ex.getMessage() == null ? "bad request" : ex.getMessage();
        return ResponseEntity.badRequest()
                .body(new CreditApiErrorTranslator.ApiError(msg, null));
    }

    private static CreditProfileView toProfile(CreditProfile p) {
        return new CreditProfileView(
                p.userId(),
                p.creditLimit().cents(),
                p.usedCredit().cents(),
                p.status().name().toLowerCase(),
                p.scoreTier().name(),
                p.policyVersion());
    }

    private static BillingStatementView toStatement(BillingStatement s) {
        return new BillingStatementView(
                s.id(),
                s.userId(),
                DAY.format(s.periodStart()),
                DAY.format(s.periodEnd()),
                s.totalDue().cents(),
                s.status().name(),
                DAY.format(s.dueDate()),
                s.createdAt().toString(),
                s.paidAt() == null ? null : s.paidAt().toString());
    }

    private static CreditPurchaseView toPurchase(CreditPurchaseResult r) {
        return new CreditPurchaseView(
                r.order().id(),
                r.entitlement().id(),
                r.order().productId(),
                r.order().userId(),
                r.order().paidAmount().cents(),
                r.debt().id(),
                r.profile().usedCredit().cents());
    }

    public record PurchaseRequest(String userId, String productId) {}

    public record CreditPurchaseView(
            String orderId,
            String entitlementId,
            String productId,
            String userId,
            long paidAmountCents,
            String debtId,
            long usedCredit) {}

    public record MarkOverdueRequest(String statementId) {}

    public record RepayRequest(String statementId, Long amountCents) {}

    public record MonthlyBillingRequest(String periodStart, String periodEnd) {}

    public record ApplyPolicyRequest(Integer policyVersion) {}

    private static Instant parseInstant(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(field + " required");
        }
        try {
            return Instant.parse(raw.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(field + " must be ISO-8601 Instant");
        }
    }

    public record CreditProfileView(
            String userId,
            long creditLimit,
            long usedCredit,
            String status,
            String scoreTier,
            int policyVersion) {}

    public record BillingStatementView(
            String id,
            String userId,
            String periodStart,
            String periodEnd,
            long totalDue,
            String status,
            String dueDate,
            String createdAt,
            String paidAt) {}
}
