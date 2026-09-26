"use client";

/**
 * 月度出账客户端岛 — 默认 U1 / 2026-08-01~2026-08-31；成功展示新账单。
 * GET statements 预读近期账单（厚 GET）。
 */

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import {
  DEFAULT_CREDIT_USER,
  loadCreditStatements,
} from "@/domains/credit/application/load-credit-statements";
import { runMonthlyBilling } from "@/domains/credit/application/run-monthly-billing";
import type { BillingStatementView } from "@/domains/credit/domain/billing-statement-view";
import styles from "./page.module.css";

const DEFAULT_PERIOD_START = "2026-08-01";
const DEFAULT_PERIOD_END = "2026-08-31";

function summarizeStatement(view: BillingStatementView): string {
  return [
    `账单 ${view.id}`,
    `账期 ${view.periodStart} ~ ${view.periodEnd}`,
    `应还 ¥${view.totalDueYuan}`,
    `${view.statusLabel}`,
    `到期 ${view.dueDate}`,
    view.repayAllowed ? "可还款" : view.blockMessage ?? "",
  ]
    .filter(Boolean)
    .join(" · ");
}

export function CreditMonthlyBillingPanel() {
  const router = useRouter();
  const [userId, setUserId] = useState(DEFAULT_CREDIT_USER);
  const [periodStart, setPeriodStart] = useState(DEFAULT_PERIOD_START);
  const [periodEnd, setPeriodEnd] = useState(DEFAULT_PERIOD_END);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<string | null>(null);
  const [preload, setPreload] = useState<readonly BillingStatementView[]>([]);
  const [preloadError, setPreloadError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    const id = userId.trim() || DEFAULT_CREDIT_USER;
    setPreloadError(null);
    loadCreditStatements(id)
      .then((list) => {
        if (cancelled) return;
        setPreload(list);
      })
      .catch((err) => {
        if (cancelled) return;
        setPreload([]);
        setPreloadError(err instanceof Error ? err.message : String(err));
      });
    return () => {
      cancelled = true;
    };
  }, [userId]);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const uid = userId.trim() || DEFAULT_CREDIT_USER;
      const view = await runMonthlyBilling({
        userId: uid,
        periodStart,
        periodEnd,
      });
      setResult(summarizeStatement(view));
      setPreload(await loadCreditStatements(uid));
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  const latest = preload[0] ?? null;

  return (
    <section className={styles.panel}>
      <h2>月度出账</h2>
      <p className={styles.note}>
        GET statements 预读近期账单
        {latest
          ? ` · 最近 ${summarizeStatement(latest)}（共 ${preload.length} 条）`
          : preloadError
            ? ` · ${preloadError}`
            : " · 暂无账单 / 加载中…"}
      </p>
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
            placeholder="YYYY-MM-DD"
          />
        </label>
        <label>
          periodEnd
          <input
            value={periodEnd}
            onChange={(e) => setPeriodEnd(e.target.value)}
            placeholder="YYYY-MM-DD"
          />
        </label>
        <button type="submit" disabled={busy}>
          {busy ? "出账中…" : "月度出账"}
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
