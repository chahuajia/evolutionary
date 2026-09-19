"use client";

/**
 * 结算批客户端岛 — accrue + runBatch（对齐 ORG-L2 种子规则）。
 */

import { FormEvent, useState } from "react";
import {
  toAccrualView,
  type AccrualView,
} from "@/domains/settlement/domain/accrual-view";
import {
  postAccrueSettlement,
  postRunSettlementBatch,
} from "@/domains/settlement/infrastructure/settlement-gateway";
import styles from "../credit/page.module.css";

type SettlementPanelProps = {
  /**
   * 服务端读来的意向列表（RSC）。
   *
   * @remarks
   * **只读** —— 写操作仍由本岛发 POST，成功后 `router.refresh()`
   * 让服务端重取（本仓既有写法）。
   */
  readonly accruals?: readonly AccrualView[];
};

export function SettlementPanel({ accruals = [] }: SettlementPanelProps) {
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
      const views = rows.map(toAccrualView);
      setResult(
        `已记意向 ${views.length} 条：` +
          views
            .map(
              (r) =>
                `${r.orgId}=¥${r.amountYuan}/${r.status}` +
                // 不可结算/冲销时把原因一并显示，不让用户对着灰按钮猜。
                // 新建的必定是 PENDING，这里当前恒空 —— 但状态一旦不是 PENDING
                // （读路径接上后），说明会立刻生效。
                (r.blockMessage ? `（${r.blockMessage}）` : ""),
            )
            .join(" · "),
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

      {/*
        服务端读来的列表。**这里才是展示不变量的用武之地** ——
        列表里有已结算/已冲销的行，界面必须说明"为什么它不能动"，
        否则灰按钮让人猜。
      */}
      <section className={styles.panel}>
        <h2>意向列表（RSC · 只读）</h2>
        {accruals.length === 0 ? (
          <p className={styles.note}>该组织暂无分润意向。</p>
        ) : (
          <ul className={styles.list}>
            {accruals.map((a) => (
              <li key={a.id} className={styles.item}>
                <div className={styles.itemHead}>
                  <span>{a.orderId}</span>
                  <span>{a.status}</span>
                </div>
                <div className={styles.meta}>
                  ¥{a.amountYuan} · 可结算 {a.settleAllowed ? "是" : "否"} ·
                  可冲销 {a.reverseAllowed ? "是" : "否"}
                  {a.blockMessage ? ` · ${a.blockMessage}` : ""}
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </>
  );
}
