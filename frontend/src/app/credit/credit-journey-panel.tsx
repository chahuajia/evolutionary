"use client";

/**
 * 业务串联客户端岛 — 信用购 →（权益换电/分润意向说明）→ 退款冲销 → 可选结算。
 * 默认 U1 / P-CREDIT-1；不新造 BE，复用已有 gateway。
 */

import Link from "next/link";
import { FormEvent, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import type { CommerceOrderView } from "@/domains/commerce/domain/commerce-order-view";
import { loadCommerceOrder } from "@/domains/commerce/application/load-commerce-order";
import { runRefundOrder } from "@/domains/commerce/application/run-refund-order";
import {
  DEFAULT_CREDIT_USER,
  runCreditPurchase,
} from "@/domains/credit/application/run-credit-purchase";
import { runAccrueSettlement } from "@/domains/settlement/application/run-accrue-settlement";
import { runSettlementBatch } from "@/domains/settlement/application/run-settlement-batch";
import {
  creditPurchaseBlockMessage,
  type CreditPurchaseBlock,
} from "@/domains/credit/domain/credit-profile-view";
import styles from "./page.module.css";

const DEFAULT_PRODUCT_ID = "P-CREDIT-1";
const DEFAULT_ORG_ID = "ORG-L2";

type CreditJourneyPanelProps = {
  readonly purchaseAllowed?: boolean;
  readonly purchaseBlock?: CreditPurchaseBlock;
  readonly statusLabel?: string;
  readonly availableYuan?: string;
};

export function CreditJourneyPanel({
  purchaseAllowed = false,
  purchaseBlock = "ok",
  statusLabel,
  availableYuan,
}: CreditJourneyPanelProps) {
  const router = useRouter();
  const [userId, setUserId] = useState(DEFAULT_CREDIT_USER);
  const [productId, setProductId] = useState(DEFAULT_PRODUCT_ID);
  const [orderId, setOrderId] = useState("");
  const [orderView, setOrderView] = useState<CommerceOrderView | null>(null);
  const [orderLoadError, setOrderLoadError] = useState<string | null>(null);
  const [entitlementId, setEntitlementId] = useState("");
  const [paidAmountCents, setPaidAmountCents] = useState<number | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [log, setLog] = useState<string[]>([]);

  useEffect(() => {
    const id = orderId.trim();
    if (!id) {
      setOrderView(null);
      setOrderLoadError(null);
      return;
    }
    let cancelled = false;
    setOrderLoadError(null);
    loadCommerceOrder(id)
      .then((view) => {
        if (cancelled) return;
        setOrderView(view);
      })
      .catch((err) => {
        if (cancelled) return;
        setOrderView(null);
        setOrderLoadError(err instanceof Error ? err.message : String(err));
      });
    return () => {
      cancelled = true;
    };
  }, [orderId]);

  const refundGate = useMemo(() => {
    const id = orderId.trim();
    if (!id) {
      return { refundAllowed: false, blockMessage: null as string | null };
    }
    if (!orderView) {
      return {
        refundAllowed: false,
        blockMessage: orderLoadError ?? "正在加载订单…",
      };
    }
    if (!orderView.refundAllowed) {
      return {
        refundAllowed: false,
        blockMessage: orderView.refundBlockMessage ?? "当前状态不可退款",
        statusLabel: orderView.statusLabel,
      };
    }
    return {
      refundAllowed: true,
      blockMessage: null as string | null,
      statusLabel: orderView.statusLabel,
    };
  }, [orderId, orderView, orderLoadError]);

  function append(line: string) {
    setLog((prev) => [...prev, line]);
  }

  async function onPurchase(e: FormEvent) {
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
    try {
      const r = await runCreditPurchase({ userId, productId });
      setOrderId(r.orderId);
      setEntitlementId(r.entitlementId);
      setPaidAmountCents(r.paidAmountCents);
      append(`① 信用购：${r.summary}`);
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
    if (!refundGate.refundAllowed) {
      setError(refundGate.blockMessage ?? "当前订单不可退款");
      return;
    }
    setBusy(true);
    setError(null);
    try {
      const r = await runRefundOrder(orderId);
      setOrderView(r.order);
      append(
        `③ 退款冲销：${r.order.orderId} · ${r.order.statusLabel} · 权益 ${r.entitlementLabel}`,
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
      const views = await runAccrueSettlement({
        orderId,
        orgId: DEFAULT_ORG_ID,
        amountCents: amount,
        userId,
        completedAt: new Date().toISOString(),
      });
      append(
        `④ Accrue：${views.length} 条 · ` +
          views.map((r) => `${r.orgId}=¥${r.amountYuan}/${r.statusLabel}`).join(" · "),
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
      const view = await runSettlementBatch({
        periodStart: start.toISOString(),
        periodEnd: now.toISOString(),
      });
      append(
        `④ Run batch：${view.id} · ${view.statusLabel}` +
          (view.blockMessage ? ` · ${view.blockMessage}` : ""),
      );
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
        演示：信用购 → 权益换电 / 分润意向 → 退款冲销 → 可选结算批。GET 订单对齐
        仅已支付可退 · 默认 {DEFAULT_CREDIT_USER} / {DEFAULT_PRODUCT_ID}
        {"statusLabel" in refundGate && refundGate.statusLabel
          ? ` · 当前订单=${refundGate.statusLabel}`
          : ""}
        。
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
        <button type="submit" disabled={busy || !purchaseAllowed}>
          {busy ? "提交中…" : "① 信用购"}
        </button>
      </form>
      {!purchaseAllowed ? (
        <p className={styles.note} role="status">
          {creditPurchaseBlockMessage(purchaseBlock, statusLabel, availableYuan)}
        </p>
      ) : null}

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
        <button
          type="submit"
          disabled={busy || !orderId.trim() || !refundGate.refundAllowed}
        >
          {busy ? "提交中…" : "③ 退款冲销"}
        </button>
      </form>
      {!refundGate.refundAllowed && refundGate.blockMessage ? (
        <p className={styles.note} role="status">
          {refundGate.blockMessage}
        </p>
      ) : null}

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
