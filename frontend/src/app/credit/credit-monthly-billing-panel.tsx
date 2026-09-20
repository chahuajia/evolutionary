"use client";

/**
 * 月度出账客户端岛 — 默认 U1 / 2026-08-01~2026-08-31；成功展示新账单。
 */

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import {
  DEFAULT_CREDIT_USER,
  runMonthlyBilling,
} from "@/domains/credit/application/run-monthly-billing";
import styles from "./page.module.css";

const DEFAULT_PERIOD_START = "2026-08-01";
const DEFAULT_PERIOD_END = "2026-08-31";

export function CreditMonthlyBillingPanel() {
  const router = useRouter();
  const [userId, setUserId] = useState(DEFAULT_CREDIT_USER);
  const [periodStart, setPeriodStart] = useState(DEFAULT_PERIOD_START);
  const [periodEnd, setPeriodEnd] = useState(DEFAULT_PERIOD_END);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<string | null>(null);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const view = await runMonthlyBilling({
        userId: userId.trim() || DEFAULT_CREDIT_USER,
        periodStart,
        periodEnd,
      });
      setResult(
        [
          `账单 ${view.id}`,
          `账期 ${view.periodStart} ~ ${view.periodEnd}`,
          `应还 ¥${view.totalDueYuan}`,
          `${view.statusLabel}（${view.status}）`,
          `到期 ${view.dueDate}`,
          view.repayAllowed ? "可还款" : view.blockMessage ?? "",
        ]
          .filter(Boolean)
          .join(" · "),
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
      <h2>月度出账</h2>
      <form className={styles.repayForm} onSubmit={onSubmit}>
        <label>
          userId
          <input
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
          />
        </label>
        <label>
          periodStart
          <input
            value={periodStart}
            onChange={(e) => setPeriodStart(e.target.value)}
          />
        </label>
        <label>
          periodEnd
          <input
            value={periodEnd}
            onChange={(e) => setPeriodEnd(e.target.value)}
          />
        </label>
        <button type="submit" disabled={busy}>
          {busy ? "出账中…" : "月度出账"}
        </button>
      </form>
      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}
      {result ? <p className={styles.note}>{result}</p> : null}
    </section>
  );
}
