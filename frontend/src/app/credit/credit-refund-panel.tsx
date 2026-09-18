"use client";

/**
 * 订单退款客户端岛 — 可粘贴 orderId，或先余额购拿 orderId 再退。
 */

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import {
  postBalancePurchase,
  postRefundOrder,
} from "@/domains/commerce/infrastructure/order-gateway";
import { DEFAULT_CREDIT_USER } from "@/domains/credit/infrastructure/credit-gateway";
import styles from "./page.module.css";

const DEFAULT_PRODUCT_ID = "P-CREDIT-1";

export function CreditRefundPanel() {
  const router = useRouter();
  const [userId, setUserId] = useState(DEFAULT_CREDIT_USER);
  const [productId, setProductId] = useState(DEFAULT_PRODUCT_ID);
  const [orderId, setOrderId] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<string | null>(null);

  async function onBuy(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const r = await postBalancePurchase({ userId, productId });
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
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const r = await postRefundOrder(orderId.trim());
      setResult(
        `已退 ${r.orderId} · ${r.status} · 撤销权益 ${r.revokedEntitlementId}`,
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
        契约 <code>POST /commerce/orders/{"{orderId}"}/refund</code>
        （余额购路径；信用购无支付分录不适用）
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
          {busy ? "提交中…" : "先余额购"}
        </button>
      </form>
      <form className={styles.repayForm} onSubmit={onRefund}>
        <label>
          orderId
          <input
            value={orderId}
            onChange={(e) => setOrderId(e.target.value)}
            placeholder="粘贴或由余额购填入"
            required
          />
        </label>
        <button type="submit" disabled={busy || !orderId.trim()}>
          {busy ? "提交中…" : "退款"}
        </button>
      </form>
      {error ? (
        <p className={styles.note} role="alert">
          {error}
        </p>
      ) : null}
      {result ? <p className={styles.note}>{result}</p> : null}
    </section>
  );
}
