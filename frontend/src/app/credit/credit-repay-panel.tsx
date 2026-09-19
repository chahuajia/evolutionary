"use client";

/**
 * 还款客户端岛 — 默认 U1 / STMT-2026-02 / 3000¢；成功后 router.refresh()。
 */

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import {
  DEFAULT_CREDIT_USER,
  postCreditRepay,
} from "@/domains/credit/infrastructure/credit-gateway";
import styles from "./page.module.css";

const DEFAULT_STATEMENT_ID = "STMT-2026-02";
const DEFAULT_AMOUNT_CENTS = 3000;

type CreditRepayPanelProps = {
  readonly repayAllowed?: boolean;
  readonly statusLabel?: string;
};

export function CreditRepayPanel({
  repayAllowed = true,
  statusLabel,
}: CreditRepayPanelProps) {
  const router = useRouter();
  const [statementId, setStatementId] = useState(DEFAULT_STATEMENT_ID);
  const [amountCents, setAmountCents] = useState(DEFAULT_AMOUNT_CENTS);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [status, setStatus] = useState<string | null>(null);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (!repayAllowed) {
      setError(
        statusLabel
          ? `档案${statusLabel}且无已用额度，无需还款`
          : "当前档案无需还款",
      );
      return;
    }
    setBusy(true);
    setError(null);
    setStatus(null);
    try {
      await postCreditRepay({
        userId: DEFAULT_CREDIT_USER,
        statementId,
        amountCents,
      });
      setStatus("还款成功，已刷新档案/账单");
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>还款解冻</h2>
      <form className={styles.repayForm} onSubmit={onSubmit}>
        <label>
          statementId
          <input
            value={statementId}
            onChange={(e) => setStatementId(e.target.value)}
          />
        </label>
        <label>
          amountCents
          <input
            type="number"
            value={amountCents}
            onChange={(e) => setAmountCents(Number(e.target.value))}
          />
        </label>
        <button type="submit" disabled={busy || !repayAllowed}>
          {busy ? "提交中…" : "还款解冻"}
        </button>
      </form>
      {!repayAllowed ? (
        <p className={styles.note} role="status">
          {statusLabel
            ? `档案${statusLabel}且无已用额度，无需还款`
            : "当前档案无需还款"}
        </p>
      ) : null}
      {error ? (
        <p className={styles.note} role="alert">
          {error}
        </p>
      ) : null}
      {status ? <p className={styles.note}>{status}</p> : null}
    </section>
  );
}
