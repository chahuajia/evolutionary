"use client";

/**
 * 商城下单客户端岛 — POST /mall/orders（AC-41 · S1 / M1）。
 * GET SKU + Merchant + Wallet 对齐库存/上架/可交易/余额可覆盖。
 */

import { FormEvent, useEffect, useMemo, useState } from "react";
import type { MallOrderView } from "@/domains/mall/domain/mall-order-view";
import type { MallSkuView } from "@/domains/mall/domain/mall-sku-view";
import type { MerchantProfileView } from "@/domains/mall/domain/merchant-profile-view";
import {
  DEFAULT_MALL_MERCHANT,
  DEFAULT_MALL_SKU,
  DEFAULT_MALL_USER,
  runPurchaseMallOrder,
} from "@/domains/mall/application/run-purchase-mall-order";
import {
  walletCoverGate,
  type WalletView,
} from "@/domains/wallet/domain/wallet-view";
import { loadWallet } from "@/domains/wallet/application/load-wallet";
import { loadMallSku } from "@/domains/mall/application/load-mall-sku";
import { loadMerchantProfile } from "@/domains/mall/application/load-merchant-profile";
import styles from "./page.module.css";

export function MallPurchasePanel() {
  const [userId, setUserId] = useState(DEFAULT_MALL_USER);
  const [merchantOrgId, setMerchantOrgId] = useState(DEFAULT_MALL_MERCHANT);
  const [skuId, setSkuId] = useState(DEFAULT_MALL_SKU);
  const [qty, setQty] = useState(1);
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
  const [result, setResult] = useState<MallOrderView | null>(null);

  useEffect(() => {
    let cancelled = false;
    const id = skuId.trim() || DEFAULT_MALL_SKU;
    setSkuLoadError(null);
    loadMallSku(id, qty)
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
  }, [skuId, qty]);

  useEffect(() => {
    let cancelled = false;
    const id = merchantOrgId.trim() || DEFAULT_MALL_MERCHANT;
    setMerchantLoadError(null);
    loadMerchantProfile(id)
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
  }, [merchantOrgId]);

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

  const gate = useMemo(() => {
    if (!skuView) {
      return {
        purchaseAllowed: false,
        blockMessage: skuLoadError ?? "正在加载 SKU…",
      };
    }
    if (!merchantView) {
      return {
        purchaseAllowed: false,
        blockMessage: merchantLoadError ?? "正在加载商家…",
      };
    }
    if (!walletView) {
      return {
        purchaseAllowed: false,
        blockMessage: walletLoadError ?? "正在加载钱包…",
      };
    }
    if (!skuView.purchaseAllowed) {
      return {
        purchaseAllowed: false,
        blockMessage: skuView.blockMessage,
      };
    }
    if (!merchantView.tradeAllowed) {
      return {
        purchaseAllowed: false,
        blockMessage: merchantView.blockMessage,
      };
    }
    const dueCents = skuView.priceCents * qty;
    const cover = walletCoverGate(walletView, dueCents);
    if (!cover.coverAllowed) {
      return {
        purchaseAllowed: false,
        blockMessage: cover.blockMessage,
      };
    }
    return { purchaseAllowed: true, blockMessage: null as string | null };
  }, [
    skuView,
    merchantView,
    walletView,
    qty,
    skuLoadError,
    merchantLoadError,
    walletLoadError,
  ]);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (!gate.purchaseAllowed) {
      setError(gate.blockMessage ?? "不可购买");
      return;
    }
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const uid = userId.trim() || DEFAULT_MALL_USER;
      setResult(
        await runPurchaseMallOrder({
          userId: uid,
          merchantOrgId: merchantOrgId.trim() || DEFAULT_MALL_MERCHANT,
          skuId: skuId.trim() || DEFAULT_MALL_SKU,
          qty,
        }),
      );
      setSkuView(await loadMallSku(skuId.trim() || DEFAULT_MALL_SKU, qty));
      setWalletView(await loadWallet(uid));
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>商城下单（HTTP · AC-41）</h2>
      <p className={styles.note}>
        GET SKU / Merchant / Wallet 对齐库存、可交易与余额（canCoverCents）
        {skuView
          ? ` · ${skuView.name} · 库存 ${skuView.stock} · ${skuView.statusLabel}`
          : ""}
        {merchantView
          ? ` · ${merchantView.shopName}(${merchantView.statusLabel})`
          : ""}
        {walletView ? ` · 余额 ¥${walletView.balanceYuan}` : ""}
      </p>
      <form className={styles.form} onSubmit={onSubmit}>
        <label>
          userId
          <input value={userId} onChange={(e) => setUserId(e.target.value)} />
        </label>
        <label>
          merchantOrgId
          <input
            value={merchantOrgId}
            onChange={(e) => setMerchantOrgId(e.target.value)}
          />
        </label>
        <label>
          skuId
          <input value={skuId} onChange={(e) => setSkuId(e.target.value)} />
        </label>
        <label>
          qty
          <input
            type="number"
            min={1}
            value={qty}
            onChange={(e) => setQty(Number(e.target.value) || 0)}
          />
        </label>
        <button type="submit" disabled={busy || !gate.purchaseAllowed}>
          {busy ? "下单中…" : "余额购买"}
        </button>
      </form>
      {!gate.purchaseAllowed && gate.blockMessage ? (
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
          订单 {result.orderId} · {result.statusLabel} · ¥{result.paidAmountYuan} ·{" "}
          {result.skuId}×{result.qty}
          {result.entitlementForbidden ? " · 不可开换电权益" : ""}
          {result.blockMessage ? ` · ${result.blockMessage}` : ""}
        </p>
      ) : null}
    </section>
  );
}
