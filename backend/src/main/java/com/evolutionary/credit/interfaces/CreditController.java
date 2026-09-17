package com.evolutionary.credit.interfaces;

import com.evolutionary.credit.application.BillingStatementRepository;
import com.evolutionary.credit.application.CreditProfileRepository;
import com.evolutionary.credit.domain.BillingStatement;
import com.evolutionary.credit.domain.CreditProfile;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 信用只读 HTTP（正式联调第一步）。
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

    public CreditController(
            CreditProfileRepository profiles, BillingStatementRepository statements) {
        this.profiles = profiles;
        this.statements = statements;
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
