package com.evolutionary.commerce.interfaces;

import com.evolutionary.commerce.application.EntitlementRepository;
import com.evolutionary.commerce.application.PerformEntitledSwap;
import com.evolutionary.commerce.domain.DomainOutcome;
import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.commerce.domain.UsageEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/entitled-swaps")
public class EntitledSwapController {

    private final PerformEntitledSwap performEntitledSwap;
    private final EntitlementRepository entitlements;

    public EntitledSwapController(
            PerformEntitledSwap performEntitledSwap, EntitlementRepository entitlements) {
        this.performEntitledSwap = performEntitledSwap;
        this.entitlements = entitlements;
    }

    /** 只读：供换电岛对齐 swapAllowed（含 FROZEN）。 */
    @GetMapping("/{entitlementId}")
    public ResponseEntity<EntitlementView> entitlement(@PathVariable String entitlementId) {
        return entitlements
                .findById(entitlementId.trim())
                .map(EntitledSwapController::toView)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> swap(@RequestBody EntitledSwapRequest body) {
        if (body == null) {
            throw new IllegalArgumentException("body required");
        }
        IncomingEntitledSwapRequest parsed =
                IncomingEntitledSwapRequest.parse(
                        body.userId(),
                        body.entitlementId(),
                        body.cabinetId(),
                        body.socBefore(),
                        body.socAfter());
        DomainOutcome<UsageEvent> outcome;
        if (parsed.useDefaultSelect()) {
            // AC-14：省略 entitlementId → 默认优先 FINITE（非计量路径）
            outcome = performEntitledSwap.executeWithoutId(parsed.userId(), parsed.cabinetId());
        } else if (parsed.isMetered()) {
            outcome =
                    performEntitledSwap.execute(
                            parsed.userId(),
                            parsed.entitlementId(),
                            parsed.cabinetId(),
                            parsed.socBefore(),
                            parsed.socAfter());
        } else {
            outcome =
                    performEntitledSwap.execute(
                            parsed.userId(), parsed.entitlementId(), parsed.cabinetId());
        }
        if (outcome instanceof DomainOutcome.Ok<UsageEvent> ok) {
            UsageEvent event = ok.value();
            Money charged = event.chargedAmount();
            return ResponseEntity.ok(
                    new EntitledSwapResponse(
                            event.id(),
                            event.status().name(),
                            event.batteryId(),
                            event.cabinetId(),
                            event.entitlementId(),
                            charged == null ? null : charged.cents()));
        }
        DomainOutcome.Err<UsageEvent> err = (DomainOutcome.Err<UsageEvent>) outcome;
        EntitledSwapApiErrorTranslator.Translated translated =
                EntitledSwapApiErrorTranslator.translate(err);
        return ResponseEntity.status(translated.status()).body(translated.body());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<EntitledSwapApiErrorTranslator.ApiError> handleBadRequest(
            IllegalArgumentException ex) {
        String msg = ex.getMessage() == null ? "bad request" : ex.getMessage();
        return ResponseEntity.badRequest()
                .body(new EntitledSwapApiErrorTranslator.ApiError(msg, null));
    }

    public record EntitledSwapRequest(
            String userId,
            String entitlementId,
            String cabinetId,
            Integer socBefore,
            Integer socAfter) {}

    public record EntitledSwapResponse(
            String usageEventId,
            String status,
            String batteryId,
            String cabinetId,
            String entitlementId,
            Long chargedAmountCents) {}

    public record EntitlementView(
            String id, String userId, String status, Integer remainingSwaps) {}

    private static EntitlementView toView(Entitlement entitlement) {
        return new EntitlementView(
                entitlement.id(),
                entitlement.userId(),
                entitlement.status().name(),
                entitlement.remainingSwaps());
    }
}
