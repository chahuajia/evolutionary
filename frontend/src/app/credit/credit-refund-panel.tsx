"use client";

/**
 * 订单退款客户端岛 — 粘贴 orderId，或先信用购拿 orderId 再退。
 * GET /commerce/orders/{id} 对齐 refundAllowed（仅 PAID）。
 */

import { FormEvent, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import type { CommerceOrderView } from "@/domains/commerce/domain/commerce-order-view";
import { loadCommerceOrder } from "@/domains/commerce/application/load-commerce-order";
import { runRefundOrder } from "@/domains/commerce/application/run-refund-order";
import {
  DEFAULT_CREDIT_USER,
  runCreditPurchase,
} from "@/domains/credit/application/run-credit-purchase";
import styles from "./page.module.css";

const DEFAULT_PRODUCT_ID = "P-CREDIT-1";

export function CreditRefundPanel() {
  const router = useRouter();
  const [userId, setUserId] = useState(DEFAULT_CREDIT_USER);
  const [productId, setProductId] = useState(DEFAULT_PRODUCT_ID);
  const [orderId, setOrderId] = useState("");
  const [orderView, setOrderView] = useState<CommerceOrderView | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<string | null>(null);

  useEffect(() => {
    const id = orderId.trim();
    if (!id) {
      setOrderView(null);
      setLoadError(null);
      return;
    }
    let cancelled = false;
    setLoadError(null);
    loadCommerceOrder(id)
      .then((view) => {
        if (cancelled) return;
        setOrderView(view);
      })
      .catch((err) => {
        if (cancelled) return;
        setOrderView(null);
        setLoadError(err instanceof Error ? err.message : String(err));
      });
    return () => {
      cancelled = true;
    };
  }, [orderId]);

  const refundGate = useMemo(() => {
    const id = orderId.trim();
    if (!id) {
      return { refundAllowed: false, blockMessage: null as string | null };
    }
    if (!orderView) {
      return {
        refundAllowed: false,
        blockMessage: loadError ?? "正在加载订单…",
      };
    }
    if (!orderView.refundAllowed) {
      return {
        refundAllowed: false,
        blockMessage: orderView.refundBlockMessage ?? "当前状态不可退款",
        statusLabel: orderView.statusLabel,
      };
    }
    return {
      refundAllowed: true,
      blockMessage: null as string | null,
      statusLabel: orderView.statusLabel,
    };
  }, [orderId, orderView, loadError]);

  async function onBuy(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const r = await runCreditPurchase({ userId, productId });
      setOrderId(r.orderId);
      setResult(`已购订单 ${r.orderId} · 权益 ${r.entitlementId}`);
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  async function onRefund(e: FormEvent) {
    e.preventDefault();
    if (!refundGate.refundAllowed) {
      setError(refundGate.blockMessage ?? "当前订单不可退款");
      return;
    }
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const r = await runRefundOrder(orderId);
      setOrderView(r.order);
      setResult(
        `已退 ${r.order.orderId} · ${r.order.statusLabel} · 权益 ${r.entitlementLabel}`,
      );
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>订单退款</h2>
      <p className={styles.note}>
        GET 订单对齐仅 PAID 可退
        {"statusLabel" in refundGate && refundGate.statusLabel
          ? ` · 当前=${refundGate.statusLabel}`
          : ""}
      </p>
      <form className={styles.repayForm} onSubmit={onBuy}>
        <label>
          userId
          <input value={userId} onChange={(e) => setUserId(e.target.value)} />
        </label>
        <label>
          productId
          <input
            value={productId}
            onChange={(e) => setProductId(e.target.value)}
          />
        </label>
        <button type="submit" disabled={busy}>
          {busy ? "提交中…" : "先信用购"}
        </button>
      </form>
      <form className={styles.repayForm} onSubmit={onRefund}>
        <label>
          orderId
          <input
            value={orderId}
            onChange={(e) => setOrderId(e.target.value)}
            placeholder="粘贴或由信用购填入"
            required
          />
        </label>
        <button
          type="submit"
          disabled={busy || !orderId.trim() || !refundGate.refundAllowed}
        >
          {busy ? "提交中…" : "退款"}
        </button>
      </form>
      {!refundGate.refundAllowed && refundGate.blockMessage ? (
        <p className={styles.note} role="status">
          {refundGate.blockMessage}
        </p>
      ) : null}
      {error ? (
        <p className={styles.note} role="alert">
          {error}
        </p>
      ) : null}
      {result ? <p className={styles.note}>{result}</p> : null}
    </section>
  );
}
