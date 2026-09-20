"use client";

/**
 * 信用购客户端岛 — 默认 U1 / P-CREDIT-1；成功展示 order/entitlement。
 */

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import {
  DEFAULT_CREDIT_USER,
  runCreditPurchase,
} from "@/domains/credit/application/run-credit-purchase";
import {
  creditPurchaseBlockMessage,
  type CreditPurchaseBlock,
} from "@/domains/credit/domain/credit-profile-view";
import styles from "./page.module.css";

const DEFAULT_PRODUCT_ID = "P-CREDIT-1";

type CreditPurchasePanelProps = {
  readonly purchaseAllowed?: boolean;
  readonly purchaseBlock?: CreditPurchaseBlock;
  readonly statusLabel?: string;
  readonly availableYuan?: string;
};

export function CreditPurchasePanel({
  purchaseAllowed = false,
  purchaseBlock = "ok",
  statusLabel,
  availableYuan,
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
        creditPurchaseBlockMessage(purchaseBlock, statusLabel, availableYuan) ??
          "不可信用购",
      );
      return;
    }
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const r = await runCreditPurchase({ userId, productId });
      setResult(r.summary);
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
          {creditPurchaseBlockMessage(purchaseBlock, statusLabel, availableYuan)}
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
