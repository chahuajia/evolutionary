"use client";

/**
 * 结算批客户端岛 — accrue + reverse + runBatch（对齐 ORG-L2 种子规则）。
 */

import { FormEvent, useMemo, useState } from "react";
import {
  toAccrualView,
  type AccrualView,
} from "@/domains/settlement/domain/accrual-view";
import {
  toSettlementBatchView,
  type SettlementBatchView,
} from "@/domains/settlement/domain/settlement-batch-view";
import {
  postAccrueSettlement,
  postReverseAccruals,
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
  const [batchView, setBatchView] = useState<SettlementBatchView | null>(null);
  const [localAccruals, setLocalAccruals] = useState<AccrualView[] | null>(
    null,
  );

  const list = localAccruals ?? accruals;

  const reverseGate = useMemo(() => {
    const forOrder = list.filter((a) => a.orderId === orderId.trim());
    if (forOrder.length === 0) {
      return {
        reverseAllowed: true,
        blockMessage: null as string | null,
        hint: "列表无该订单 · 交后端判",
      };
    }
    const blocked = forOrder.find((a) => !a.reverseAllowed);
    if (blocked) {
      return {
        reverseAllowed: false,
        blockMessage: blocked.blockMessage,
        hint: null as string | null,
      };
    }
    return {
      reverseAllowed: true,
      blockMessage: null as string | null,
      hint: null as string | null,
    };
  }, [list, orderId]);

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
      setLocalAccruals(views);
      setResult(
        `已记意向 ${views.length} 条：` +
          views
            .map(
              (r) =>
                `${r.orgId}=¥${r.amountYuan}/${r.status}` +
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

  async function onReverse(e: FormEvent) {
    e.preventDefault();
    if (!reverseGate.reverseAllowed) {
      setError(reverseGate.blockMessage ?? "不可冲销");
      return;
    }
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const rows = await postReverseAccruals(orderId.trim() || "O-STL-UI");
      const views = rows.map(toAccrualView);
      setLocalAccruals((prev) => {
        const byId = new Map((prev ?? list).map((a) => [a.id, a]));
        for (const v of views) byId.set(v.id, v);
        return Array.from(byId.values());
      });
      setResult(
        `已冲销 ${views.length} 条：` +
          views
            .map(
              (r) =>
                `${r.orderId}=¥${r.amountYuan}/${r.status}` +
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
    setBatchView(null);
    try {
      const r = await postRunSettlementBatch({ periodStart, periodEnd });
      const view = toSettlementBatchView(r);
      setBatchView(view);
      setResult(
        `批 ${view.id} · ${view.statusLabel}` +
          (view.blockMessage ? ` · ${view.blockMessage}` : ""),
      );
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
            <input
              value={orderId}
              onChange={(e) => setOrderId(e.target.value)}
            />
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
        <h2>冲销意向（HTTP · AC-35）</h2>
        <p className={styles.note}>
          <code>POST /settlement/orders/{"{orderId}"}/reverse-accruals</code>
          {" · "}
          仅 PENDING 可冲销（reverseAllowed）
          {reverseGate.hint ? ` · ${reverseGate.hint}` : ""}
        </p>
        <form className={styles.repayForm} onSubmit={onReverse}>
          <button type="submit" disabled={busy || !reverseGate.reverseAllowed}>
            {busy ? "冲销中…" : "冲销该订单意向"}
          </button>
        </form>
        {!reverseGate.reverseAllowed && reverseGate.blockMessage ? (
          <p className={styles.note} role="status">
            {reverseGate.blockMessage}
          </p>
        ) : null}
      </section>
      <section className={styles.panel}>
        <h2>跑结算批</h2>
        <p className={styles.note}>
          <code>POST /settlement/batches</code>
          {" · "}
          一次跑完即关账（仅 OPEN 域上可再关；HTTP 返回通常已是 CLOSED）
          {batchView ? ` · 上次=${batchView.statusLabel}` : ""}
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

      <section className={styles.panel}>
        <h2>意向列表（RSC · 只读）</h2>
        {list.length === 0 ? (
          <p className={styles.note}>该组织暂无分润意向。</p>
        ) : (
          <ul className={styles.list}>
            {list.map((a) => (
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
