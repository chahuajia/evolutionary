"use client";

/**
 * 业务串联客户端岛 — 信用购 →（权益换电/分润意向说明）→ 退款冲销 → 可选结算。
 * 默认 U1 / P-CREDIT-1；不新造 BE，复用已有 gateway。
 */

import Link from "next/link";
import { FormEvent, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import {
  parseCommerceOrderStatus,
  toCommerceOrderView,
  type CommerceOrderView,
} from "@/domains/commerce/domain/commerce-order-view";
import {
  parseEntitlementStatus,
  toEntitlementView,
} from "@/domains/commerce/domain/entitlement-view";
import {
  fetchCommerceOrder,
  postRefundOrder,
} from "@/domains/commerce/infrastructure/order-refund-gateway";
import {
  DEFAULT_CREDIT_USER,
  postCreditPurchase,
  type CreditPurchaseResult,
} from "@/domains/credit/infrastructure/credit-gateway";
import { toAccrualView } from "@/domains/settlement/domain/accrual-view";
import { toSettlementBatchView } from "@/domains/settlement/domain/settlement-batch-view";
import {
  postAccrueSettlement,
  postRunSettlementBatch,
} from "@/domains/settlement/infrastructure/settlement-gateway";
import {
  creditPurchaseBlockMessage,
  type CreditPurchaseBlock,
} from "@/domains/credit/domain/credit-profile-view";
import { formatCentsAsYuan } from "@/shared/money/format-cents";
import styles from "./page.module.css";

const DEFAULT_PRODUCT_ID = "P-CREDIT-1";
const DEFAULT_ORG_ID = "ORG-L2";

type CreditJourneyPanelProps = {
  readonly purchaseAllowed?: boolean;
  readonly purchaseBlock?: CreditPurchaseBlock;
  readonly statusLabel?: string;
  readonly availableYuan?: string;
};

function summarizePurchase(r: CreditPurchaseResult): string {
  const parts = [`订单 ${r.orderId}`, `权益 ${r.entitlementId}`];
  if (r.paidAmountCents != null) {
    parts.push(`金额 ¥${formatCentsAsYuan(r.paidAmountCents)}`);
  }
  if (r.debtId) parts.push(`债务 ${r.debtId}`);
  return parts.join(" · ");
}

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
    fetchCommerceOrder(id)
      .then((dto) => {
        if (cancelled) return;
        setOrderView(
          toCommerceOrderView({
            orderId: dto.orderId,
            status: parseCommerceOrderStatus(dto.status),
          }),
        );
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
        blockMessage:
          orderView.blockMessage ??
          (orderView.status === "CREATED"
            ? "订单未支付，不可退款"
            : "当前状态不可退款"),
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
    if (!refundGate.refundAllowed) {
      setError(refundGate.blockMessage ?? "当前订单不可退款");
      return;
    }
    setBusy(true);
    setError(null);
    try {
      const r = await postRefundOrder(orderId);
      const status = parseCommerceOrderStatus(r.status);
      const view = toCommerceOrderView({ orderId: r.orderId, status });
      setOrderView(view);
      let ent = r.entitlementId ?? (entitlementId || "(revoked)");
      if (r.entitlementStatus) {
        try {
          const ev = toEntitlementView({
            id: r.entitlementId ?? (entitlementId || "?"),
            status: parseEntitlementStatus(r.entitlementStatus),
          });
          ent = `${ev.id}/${ev.statusLabel}`;
        } catch {
          ent = `${ent}/${r.entitlementStatus}`;
        }
      }
      append(`③ 退款冲销：${view.orderId} · ${view.statusLabel} · 权益 ${ent}`);
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
      const view = toSettlementBatchView(r);
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
        仅 PAID 可退 · 默认 {DEFAULT_CREDIT_USER} / {DEFAULT_PRODUCT_ID}
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
