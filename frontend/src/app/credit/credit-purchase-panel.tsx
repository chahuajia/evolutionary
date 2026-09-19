"use client";

/**
 * 信用购客户端岛 — 默认 U1 / P-CREDIT-1；成功展示 order/entitlement。
 */

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import {
  DEFAULT_CREDIT_USER,
  postCreditPurchase,
  type CreditPurchaseResult,
} from "@/domains/credit/infrastructure/credit-gateway";
import { formatYuan } from "@/lib/credit/types";
import styles from "./page.module.css";

const DEFAULT_PRODUCT_ID = "P-CREDIT-1";

type CreditPurchasePanelProps = {
  readonly purchaseAllowed?: boolean;
  readonly statusLabel?: string;
};

function summarizePurchase(r: CreditPurchaseResult): string {
  const parts = [
    `订单 ${r.orderId}`,
    `权益 ${r.entitlementId}`,
  ];
  if (r.productId) parts.push(`商品 ${r.productId}`);
  if (r.paidAmountCents != null) {
    parts.push(`金额 ¥${formatYuan(r.paidAmountCents)}`);
  }
  if (r.debtId) parts.push(`债务 ${r.debtId}`);
  return parts.join(" · ");
}

export function CreditPurchasePanel({
  purchaseAllowed = true,
  statusLabel,
}: CreditPurchasePanelProps) {
  const router = useRouter();
  const [userId, setUserId] = useState(DEFAULT_CREDIT_USER);
  const [productId, setProductId] = useState(DEFAULT_PRODUCT_ID);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<string | null>(null);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (!purchaseAllowed) {
      setError(
        statusLabel
          ? `档案${statusLabel}，不可信用购`
          : "档案状态不允许信用购",
      );
      return;
    }
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const r = await postCreditPurchase({ userId, productId });
      setResult(summarizePurchase(r));
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>信用购</h2>
      <form className={styles.repayForm} onSubmit={onSubmit}>
        <label>
          userId
          <input
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
          />
        </label>
        <label>
          productId
          <input
            value={productId}
            onChange={(e) => setProductId(e.target.value)}
          />
        </label>
        <button type="submit" disabled={busy || !purchaseAllowed}>
          {busy ? "提交中…" : "信用购"}
        </button>
      </form>
      {!purchaseAllowed ? (
        <p className={styles.note} role="status">
          {statusLabel
            ? `档案${statusLabel}，不可信用购`
            : "档案状态不允许信用购"}
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
