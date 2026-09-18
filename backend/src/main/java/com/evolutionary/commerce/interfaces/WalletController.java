package com.evolutionary.commerce.interfaces;

import com.evolutionary.commerce.application.ResolveUserWallet;
import com.evolutionary.commerce.application.UserWallet;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消费者钱包读 HTTP（切片27b）。
 *
 * <p>契约：{@code GET /commerce/users/{userId}/wallet} → userId / balanceCents / pointsCents /
 * currency；未知用户（无余额或积分账户）→ 404。
 */
@RestController
@RequestMapping("/commerce/users")
public class WalletController {

    private final ResolveUserWallet resolveUserWallet;

    public WalletController(ResolveUserWallet resolveUserWallet) {
        this.resolveUserWallet = resolveUserWallet;
    }

    @GetMapping("/{userId}/wallet")
    public ResponseEntity<WalletResponse> wallet(@PathVariable String userId) {
        return resolveUserWallet
                .execute(userId)
                .map(WalletController::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private static WalletResponse toResponse(UserWallet w) {
        return new WalletResponse(
                w.userId(), w.balanceCents(), w.pointsCents(), w.currency().name());
    }

    public record WalletResponse(
            String userId, long balanceCents, long pointsCents, String currency) {}
}
