"use client";

/**
 * 业务串联客户端岛 — 信用购 →（权益换电/分润意向说明）→ 退款冲销 → 可选结算。
 * 默认 U1 / P-CREDIT-1；不新造 BE，复用已有 gateway。
 */

import Link from "next/link";
import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { postRefundOrder } from "@/domains/commerce/infrastructure/order-refund-gateway";
import {
  DEFAULT_CREDIT_USER,
  postCreditPurchase,
  type CreditPurchaseResult,
} from "@/domains/credit/infrastructure/credit-gateway";
import { toAccrualView } from "@/domains/settlement/domain/accrual-view";
import {
  postAccrueSettlement,
  postRunSettlementBatch,
} from "@/domains/settlement/infrastructure/settlement-gateway";
import { formatYuan } from "@/lib/credit/types";
import styles from "./page.module.css";

const DEFAULT_PRODUCT_ID = "P-CREDIT-1";
const DEFAULT_ORG_ID = "ORG-L2";

function summarizePurchase(r: CreditPurchaseResult): string {
  const parts = [`订单 ${r.orderId}`, `权益 ${r.entitlementId}`];
  if (r.paidAmountCents != null) {
    parts.push(`金额 ¥${formatYuan(r.paidAmountCents)}`);
  }
  if (r.debtId) parts.push(`债务 ${r.debtId}`);
  return parts.join(" · ");
}

export function CreditJourneyPanel() {
  const router = useRouter();
  const [userId, setUserId] = useState(DEFAULT_CREDIT_USER);
  const [productId, setProductId] = useState(DEFAULT_PRODUCT_ID);
  const [orderId, setOrderId] = useState("");
  const [entitlementId, setEntitlementId] = useState("");
  const [paidAmountCents, setPaidAmountCents] = useState<number | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [log, setLog] = useState<string[]>([]);

  function append(line: string) {
    setLog((prev) => [...prev, line]);
  }

  async function onPurchase(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const r = await postCreditPurchase({ userId, productId });
      setOrderId(r.orderId);
      setEntitlementId(r.entitlementId);
      setPaidAmountCents(r.paidAmountCents ?? null);
      append(`① 信用购：${summarizePurchase(r)}`);
      append(
        "购后 BE 应已自动记分润意向（依赖 22a；未合入时可下方 Accrue 手调）",
      );
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
    try {
      const r = await postRefundOrder(orderId);
      const ent = r.entitlementId ?? (entitlementId || "(revoked)");
      append(
        `③ 退款冲销：${r.orderId} · ${r.status} · 权益 ${ent}` +
          (r.entitlementStatus ? `/${r.entitlementStatus}` : ""),
      );
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  async function onAccrue() {
    if (!orderId.trim()) {
      setError("请先信用购以获得 orderId");
      return;
    }
    setBusy(true);
    setError(null);
    try {
      const amount = paidAmountCents ?? 10000;
      const rows = await postAccrueSettlement({
        orderId,
        orgId: DEFAULT_ORG_ID,
        amountCents: amount,
        userId,
        completedAt: new Date().toISOString(),
      });
      const views = rows.map(toAccrualView);
      append(
        `④ Accrue：${views.length} 条 · ` +
          views.map((r) => `${r.orgId}=¥${r.amountYuan}/${r.status}`).join(" · "),
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  async function onBatch() {
    setBusy(true);
    setError(null);
    try {
      const now = new Date();
      const start = new Date(now);
      start.setUTCDate(start.getUTCDate() - 7);
      const r = await postRunSettlementBatch({
        periodStart: start.toISOString(),
        periodEnd: now.toISOString(),
      });
      append(`④ Run batch：${r.id} · ${r.status}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>业务串联</h2>
      <p className={styles.note}>
        演示：信用购 → 权益换电 / 分润意向 → 退款冲销 → 可选结算批。默认{" "}
        {DEFAULT_CREDIT_USER} / {DEFAULT_PRODUCT_ID}。
      </p>

      <form className={styles.repayForm} onSubmit={onPurchase}>
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
          {busy ? "提交中…" : "① 信用购"}
        </button>
      </form>

      {(orderId || entitlementId) && (
        <dl className={styles.dl}>
          <dt>orderId</dt>
          <dd>{orderId || "—"}</dd>
          <dt>entitlementId</dt>
          <dd>{entitlementId || "—"}</dd>
        </dl>
      )}

      <p className={styles.note}>
        ②{" "}
        <Link href="/">去权益换电（首页）</Link>
        {" · "}
        购后 BE 应已自动记分润意向（22a）
      </p>

      <form className={styles.repayForm} onSubmit={onRefund}>
        <label>
          orderId
          <input
            value={orderId}
            onChange={(e) => setOrderId(e.target.value)}
            placeholder="由信用购填入或粘贴"
            required
          />
        </label>
        <button type="submit" disabled={busy || !orderId.trim()}>
          {busy ? "提交中…" : "③ 退款冲销"}
        </button>
      </form>

      <div className={styles.journeyActions}>
        <button type="button" disabled={busy} onClick={onAccrue}>
          {busy ? "提交中…" : "④ Accrue（手调）"}
        </button>
        <button type="button" disabled={busy} onClick={onBatch}>
          {busy ? "提交中…" : "④ Run settlement batch"}
        </button>
      </div>

      {error ? (
        <p className={styles.note} role="alert">
          {error}
        </p>
      ) : null}
      {log.length > 0 ? (
        <ul className={styles.list}>
          {log.map((line, i) => (
            <li key={`${i}-${line.slice(0, 24)}`} className={styles.meta}>
              {line}
            </li>
          ))}
        </ul>
      ) : null}
    </section>
  );
}
