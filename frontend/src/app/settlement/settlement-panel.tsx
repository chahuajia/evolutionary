"use client";

/**
 * 结算批客户端岛 — accrue + runBatch（对齐 ORG-L2 种子规则）。
 */

import { FormEvent, useState } from "react";
import {
  postAccrueSettlement,
  postRunSettlementBatch,
} from "@/domains/settlement/infrastructure/settlement-gateway";
import styles from "../credit/page.module.css";

export function SettlementPanel() {
  const [orderId, setOrderId] = useState("O-STL-UI");
  const [userId, setUserId] = useState("U1");
  const [orgId, setOrgId] = useState("ORG-L2");
  const [amountCents, setAmountCents] = useState(10000);
  const [periodStart, setPeriodStart] = useState("2026-09-17T00:00:00Z");
  const [periodEnd, setPeriodEnd] = useState("2026-09-20T00:00:00Z");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<string | null>(null);

  async function onAccrue(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const rows = await postAccrueSettlement({
        orderId,
        orgId,
        amountCents,
        userId,
        completedAt: new Date().toISOString(),
      });
      setResult(
        `已记意向 ${rows.length} 条：` +
          rows.map((r) => `${r.orgId}=${r.amountCents}¢/${r.status}`).join(" · "),
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  async function onBatch(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const r = await postRunSettlementBatch({ periodStart, periodEnd });
      setResult(`批 ${r.id} · ${r.status}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <>
      <section className={styles.panel}>
        <h2>记分润意向</h2>
        <p className={styles.note}>
          <code>POST /settlement/accruals</code>
        </p>
        <form className={styles.repayForm} onSubmit={onAccrue}>
          <label>
            orderId
            <input value={orderId} onChange={(e) => setOrderId(e.target.value)} />
          </label>
          <label>
            userId
            <input value={userId} onChange={(e) => setUserId(e.target.value)} />
          </label>
          <label>
            orgId（售卖方）
            <input value={orgId} onChange={(e) => setOrgId(e.target.value)} />
          </label>
          <label>
            amountCents
            <input
              type="number"
              value={amountCents}
              onChange={(e) => setAmountCents(Number(e.target.value))}
            />
          </label>
          <button type="submit" disabled={busy}>
            {busy ? "提交中…" : "Accrue"}
          </button>
        </form>
      </section>
      <section className={styles.panel}>
        <h2>跑结算批</h2>
        <p className={styles.note}>
          <code>POST /settlement/batches</code>
        </p>
        <form className={styles.repayForm} onSubmit={onBatch}>
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
            {busy ? "提交中…" : "Run batch"}
          </button>
        </form>
      </section>
      {error ? (
        <p className={styles.note} role="alert">
          {error}
        </p>
      ) : null}
      {result ? <p className={styles.note}>{result}</p> : null}
    </>
  );
}
