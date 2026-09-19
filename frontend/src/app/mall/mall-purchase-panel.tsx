"use client";

/**
 * 商城下单客户端岛 — POST /mall/orders（AC-41 · S1 / M1）。
 * GET SKU + Merchant 对齐库存/上架/可交易。
 */

import { FormEvent, useEffect, useMemo, useState } from "react";
import {
  toMallOrderView,
  type MallOrderView,
} from "@/domains/mall/domain/mall-order-view";
import {
  parseMallSkuStatus,
  toMallSkuView,
  type MallSkuView,
} from "@/domains/mall/domain/mall-sku-view";
import {
  parseMerchantProfileStatus,
  toMerchantProfileView,
  type MerchantProfileView,
} from "@/domains/mall/domain/merchant-profile-view";
import {
  DEFAULT_MALL_MERCHANT,
  DEFAULT_MALL_SKU,
  DEFAULT_MALL_USER,
  fetchMallSku,
  fetchMerchantProfile,
  postPurchaseMallOrder,
} from "@/domains/mall/infrastructure/mall-gateway";
import styles from "./page.module.css";

export function MallPurchasePanel() {
  const [userId, setUserId] = useState(DEFAULT_MALL_USER);
  const [merchantOrgId, setMerchantOrgId] = useState(DEFAULT_MALL_MERCHANT);
  const [skuId, setSkuId] = useState(DEFAULT_MALL_SKU);
  const [qty, setQty] = useState(1);
  const [skuView, setSkuView] = useState<MallSkuView | null>(null);
  const [merchantView, setMerchantView] =
    useState<MerchantProfileView | null>(null);
  const [skuLoadError, setSkuLoadError] = useState<string | null>(null);
  const [merchantLoadError, setMerchantLoadError] = useState<string | null>(
    null,
  );
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<MallOrderView | null>(null);

  useEffect(() => {
    let cancelled = false;
    const id = skuId.trim() || DEFAULT_MALL_SKU;
    setSkuLoadError(null);
    fetchMallSku(id)
      .then((dto) => {
        if (cancelled) return;
        setSkuView(
          toMallSkuView(
            {
              id: dto.id,
              merchantOrgId: dto.merchantOrgId,
              name: dto.name,
              priceCents: dto.priceCents,
              stock: dto.stock,
              status: parseMallSkuStatus(dto.status),
            },
            qty,
          ),
        );
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
    fetchMerchantProfile(id)
      .then((dto) => {
        if (cancelled) return;
        setMerchantView(
          toMerchantProfileView({
            orgId: dto.orgId,
            shopName: dto.shopName,
            status: parseMerchantProfileStatus(dto.status),
          }),
        );
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
    return { purchaseAllowed: true, blockMessage: null as string | null };
  }, [skuView, merchantView, skuLoadError, merchantLoadError]);

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
      const r = await postPurchaseMallOrder({
        userId: userId.trim() || DEFAULT_MALL_USER,
        merchantOrgId: merchantOrgId.trim() || DEFAULT_MALL_MERCHANT,
        skuId: skuId.trim() || DEFAULT_MALL_SKU,
        qty,
      });
      setResult(toMallOrderView(r));
      const dto = await fetchMallSku(skuId.trim() || DEFAULT_MALL_SKU);
      setSkuView(
        toMallSkuView(
          {
            id: dto.id,
            merchantOrgId: dto.merchantOrgId,
            name: dto.name,
            priceCents: dto.priceCents,
            stock: dto.stock,
            status: parseMallSkuStatus(dto.status),
          },
          qty,
        ),
      );
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
        GET SKU / Merchant 对齐库存与可交易
        {skuView
          ? ` · ${skuView.name} · 库存 ${skuView.stock} · ${skuView.status}`
          : ""}
        {merchantView
          ? ` · ${merchantView.shopName}(${merchantView.statusLabel})`
          : ""}
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
          订单 {result.orderId} · {result.status} · ¥{result.paidAmountYuan} ·{" "}
          {result.skuId}×{result.qty}
          {result.entitlementForbidden ? " · 不可开换电权益" : ""}
          {result.blockMessage ? ` · ${result.blockMessage}` : ""}
        </p>
      ) : null}
    </section>
  );
}
