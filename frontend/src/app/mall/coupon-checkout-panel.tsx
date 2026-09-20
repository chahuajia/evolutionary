"use client";

/**
 * 带券结账客户端岛 — 先领券再 POST checkout-with-coupons（AC-42..44）。
 * GET SKU / Merchant / Wallet / UserCoupon 对齐可购、可交易、余额与 checkoutSelectable。
 */

import { FormEvent, useEffect, useMemo, useState } from "react";
import type { MallCheckoutView } from "@/domains/mall/domain/mall-checkout-view";
import {
  toUserCouponView,
  type UserCouponView,
} from "@/domains/mall/domain/user-coupon-view";
import type { MallSkuView } from "@/domains/mall/domain/mall-sku-view";
import type { MerchantProfileView } from "@/domains/mall/domain/merchant-profile-view";
import { loadMallSku } from "@/domains/mall/application/load-mall-sku";
import { loadMerchantProfile } from "@/domains/mall/application/load-merchant-profile";
import { loadUserCoupon } from "@/domains/mall/application/load-user-coupon";
import {
  DEFAULT_MALL_CAMPAIGN,
  DEFAULT_MALL_TEMPLATE,
  DEFAULT_MALL_USER,
  runClaimCoupon,
} from "@/domains/mall/application/run-claim-coupon";
import {
  DEFAULT_MALL_MERCHANT,
  DEFAULT_MALL_SKU,
  runCheckoutWithCoupons,
} from "@/domains/mall/application/run-checkout-with-coupons";
import {
  canCoverCents,
  formatCentsAsYuan,
  type WalletView,
} from "@/domains/wallet/domain/wallet-view";
import { loadWallet } from "@/domains/wallet/application/load-wallet";
import styles from "./page.module.css";

const QTY = 1;

export function CouponCheckoutPanel() {
  const [userId, setUserId] = useState(DEFAULT_MALL_USER);
  const [couponId, setCouponId] = useState("");
  const [couponView, setCouponView] = useState<UserCouponView | null>(null);
  const [couponLoadError, setCouponLoadError] = useState<string | null>(null);
  const [skuView, setSkuView] = useState<MallSkuView | null>(null);
  const [merchantView, setMerchantView] =
    useState<MerchantProfileView | null>(null);
  const [walletView, setWalletView] = useState<WalletView | null>(null);
  const [skuLoadError, setSkuLoadError] = useState<string | null>(null);
  const [merchantLoadError, setMerchantLoadError] = useState<string | null>(
    null,
  );
  const [walletLoadError, setWalletLoadError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<MallCheckoutView | null>(null);

  useEffect(() => {
    let cancelled = false;
    setSkuLoadError(null);
    loadMallSku(DEFAULT_MALL_SKU, QTY)
      .then((view) => {
        if (cancelled) return;
        setSkuView(view);
      })
      .catch((err) => {
        if (cancelled) return;
        setSkuView(null);
        setSkuLoadError(err instanceof Error ? err.message : String(err));
      });
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    let cancelled = false;
    setMerchantLoadError(null);
    loadMerchantProfile(DEFAULT_MALL_MERCHANT)
      .then((view) => {
        if (cancelled) return;
        setMerchantView(view);
      })
      .catch((err) => {
        if (cancelled) return;
        setMerchantView(null);
        setMerchantLoadError(err instanceof Error ? err.message : String(err));
      });
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    let cancelled = false;
    const id = userId.trim() || DEFAULT_MALL_USER;
    setWalletLoadError(null);
    loadWallet(id)
      .then((view) => {
        if (cancelled) return;
        setWalletView(view);
      })
      .catch((err) => {
        if (cancelled) return;
        setWalletView(null);
        setWalletLoadError(err instanceof Error ? err.message : String(err));
      });
    return () => {
      cancelled = true;
    };
  }, [userId]);

  useEffect(() => {
    let cancelled = false;
    const id = couponId.trim();
    if (!id) {
      setCouponView(null);
      setCouponLoadError(null);
      return;
    }
    setCouponLoadError(null);
    loadUserCoupon(id)
      .then((view) => {
        if (cancelled) return;
        setCouponView(view);
      })
      .catch((err) => {
        if (cancelled) return;
        setCouponView(null);
        setCouponLoadError(err instanceof Error ? err.message : String(err));
      });
    return () => {
      cancelled = true;
    };
  }, [couponId]);

  const gate = useMemo(() => {
    if (!skuView) {
      return {
        checkoutAllowed: false,
        blockMessage: skuLoadError ?? "正在加载 SKU…",
      };
    }
    if (!merchantView) {
      return {
        checkoutAllowed: false,
        blockMessage: merchantLoadError ?? "正在加载商家…",
      };
    }
    if (!walletView) {
      return {
        checkoutAllowed: false,
        blockMessage: walletLoadError ?? "正在加载钱包…",
      };
    }
    if (!skuView.purchaseAllowed) {
      return {
        checkoutAllowed: false,
        blockMessage: skuView.blockMessage,
      };
    }
    if (!merchantView.tradeAllowed) {
      return {
        checkoutAllowed: false,
        blockMessage: merchantView.blockMessage,
      };
    }
    const pasted = couponId.trim();
    if (pasted) {
      if (!couponView || couponView.id !== pasted) {
        return {
          checkoutAllowed: false,
          blockMessage: couponLoadError ?? "正在加载用户券…",
        };
      }
      if (!couponView.checkoutSelectable) {
        return {
          checkoutAllowed: false,
          blockMessage: couponView.blockMessage,
        };
      }
    }
    const listCents = skuView.priceCents * QTY;
    if (!canCoverCents(walletView.balanceCents, listCents)) {
      return {
        checkoutAllowed: false,
        blockMessage: `余额不足覆盖标价（¥${walletView.balanceYuan} < ¥${formatCentsAsYuan(listCents)}；券后可能更低）`,
      };
    }
    return { checkoutAllowed: true, blockMessage: null as string | null };
  }, [
    skuView,
    merchantView,
    walletView,
    couponView,
    couponId,
    couponLoadError,
    skuLoadError,
    merchantLoadError,
    walletLoadError,
  ]);

  async function claimFirst() {
    setBusy(true);
    setError(null);
    try {
      const c = await runClaimCoupon({
        campaignId: DEFAULT_MALL_CAMPAIGN,
        userId: userId.trim() || DEFAULT_MALL_USER,
        templateId: DEFAULT_MALL_TEMPLATE,
      });
      setCouponId(c.id);
      // couponId effect 会 GET 真态；先本地写入避免按钮闪灰
      setCouponView(c);
      setCouponLoadError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (!gate.checkoutAllowed) {
      setError(gate.blockMessage ?? "不可结账");
      return;
    }
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const uid = userId.trim() || DEFAULT_MALL_USER;
      const ids = couponId.trim() ? [couponId.trim()] : [];
      setResult(
        await runCheckoutWithCoupons({
          userId: uid,
          merchantOrgId: DEFAULT_MALL_MERCHANT,
          skuId: DEFAULT_MALL_SKU,
          qty: QTY,
          userCouponIds: ids,
        }),
      );
      if (ids[0]) {
        try {
          setCouponView(await loadUserCoupon(ids[0]));
        } catch {
          if (couponView && ids[0] === couponView.id) {
            setCouponView(
              toUserCouponView({
                id: couponView.id,
                userId: couponView.userId,
                templateId: couponView.templateId,
                status: "USED",
              }),
            );
          }
        }
      }
      setWalletView(await loadWallet(uid));
      setSkuView(await loadMallSku(DEFAULT_MALL_SKU, QTY));
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>带券结账（HTTP · AC-42..44）</h2>
      <p className={styles.note}>
        GET SKU / Merchant / Wallet / UserCoupon 对齐可购、可交易、余额与
        checkoutSelectable
        {skuView ? ` · ${skuView.name}` : ""}
        {merchantView
          ? ` · ${merchantView.shopName}(${merchantView.statusLabel})`
          : ""}
        {walletView ? ` · 余额 ¥${walletView.balanceYuan}` : ""}
        {couponView
          ? ` · 券 ${couponView.id}=${couponView.status}`
          : ""}
      </p>
      <form className={styles.form} onSubmit={onSubmit}>
        <label>
          userId
          <input value={userId} onChange={(e) => setUserId(e.target.value)} />
        </label>
        <label>
          userCouponId
          <input
            value={couponId}
            onChange={(e) => setCouponId(e.target.value)}
            placeholder="先点领券或粘贴券 id"
          />
        </label>
        <div style={{ display: "flex", gap: "0.5rem" }}>
          <button type="button" disabled={busy} onClick={claimFirst}>
            先领券
          </button>
          <button type="submit" disabled={busy || !gate.checkoutAllowed}>
            {busy ? "结账中…" : "带券结账"}
          </button>
        </div>
      </form>
      {!gate.checkoutAllowed && gate.blockMessage ? (
        <p className={styles.note} role="status">
          {gate.blockMessage}
        </p>
      ) : null}
      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}
      {result ? (
        <p>
          {result.orderId} · {result.status} · 实付 ¥{result.paidAmountYuan}
          {result.hasDiscount ? ` · 优惠 ¥${result.discountYuan}` : ""}
          {result.entitlementForbidden ? " · 不可开换电权益" : ""}
        </p>
      ) : null}
    </section>
  );
}
