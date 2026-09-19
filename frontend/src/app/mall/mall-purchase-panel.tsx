"use client";

/**
 * 商城下单客户端岛 — POST /mall/orders（AC-41 · S1 / M1）。
 */

import { FormEvent, useState } from "react";
import {
  toMallOrderView,
  type MallOrderView,
} from "@/domains/mall/domain/mall-order-view";
import {
  canPurchaseSku,
  skuPurchaseBlockMessage,
} from "@/domains/mall/domain/mall-sku-view";
import {
  DEFAULT_MALL_MERCHANT,
  DEFAULT_MALL_SKU,
  DEFAULT_MALL_USER,
  postPurchaseMallOrder,
} from "@/domains/mall/infrastructure/mall-gateway";
import styles from "./page.module.css";

/** 无 GET SKU 前：种子默认上架；库存未知时不在前端卡库存。 */
const SEED_SKU_STATUS = "ON_SALE" as const;
const SEED_STOCK_UNKNOWN = Number.MAX_SAFE_INTEGER;

export function MallPurchasePanel() {
  const [userId, setUserId] = useState(DEFAULT_MALL_USER);
  const [merchantOrgId, setMerchantOrgId] = useState(DEFAULT_MALL_MERCHANT);
  const [skuId, setSkuId] = useState(DEFAULT_MALL_SKU);
  const [qty, setQty] = useState(1);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<MallOrderView | null>(null);

  const purchaseAllowed = canPurchaseSku(
    SEED_SKU_STATUS,
    SEED_STOCK_UNKNOWN,
    qty,
  );
  const blockMessage = skuPurchaseBlockMessage(
    SEED_SKU_STATUS,
    SEED_STOCK_UNKNOWN,
    qty,
  );

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (!purchaseAllowed) {
      setError(blockMessage ?? "不可购买");
      return;
    }
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const r = await postPurchaseMallOrder({
        userId: userId.trim() || DEFAULT_MALL_USER,
        merchantOrgId: merchantOrgId.trim() || DEFAULT_MALL_MERCHANT,
        skuId: skuId.trim() || DEFAULT_MALL_SKU,
        qty,
      });
      setResult(toMallOrderView(r));
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>商城下单（HTTP · AC-41）</h2>
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
        <button type="submit" disabled={busy || !purchaseAllowed}>
          {busy ? "下单中…" : "余额购买"}
        </button>
      </form>
      {!purchaseAllowed && blockMessage ? (
        <p className={styles.note} role="status">
          {blockMessage}
        </p>
      ) : null}
      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}
      {result ? (
        <p>
          订单 {result.orderId} · {result.status} · ¥{result.paidAmountYuan} ·{" "}
          {result.skuId}×{result.qty}
          {result.entitlementForbidden ? " · 不可开换电权益" : ""}
          {result.blockMessage ? ` · ${result.blockMessage}` : ""}
        </p>
      ) : null}
    </section>
  );
}
