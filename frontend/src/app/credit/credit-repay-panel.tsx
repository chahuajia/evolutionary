"use client";

/**
 * 还款客户端岛 — 默认 U1 / STMT-2026-02 / 3000¢；成功后 router.refresh()。
 * GET /credit/statements/{id} 对齐 repayAllowed；钱包门 canCoverCents。
 */

import { FormEvent, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import {
  toBillingStatementView,
  type BillingStatementView,
} from "@/domains/credit/domain/billing-statement-view";
import {
  DEFAULT_CREDIT_USER,
  fetchCreditStatement,
  postCreditRepay,
  postMarkCreditOverdue,
} from "@/domains/credit/infrastructure/credit-gateway";
import {
  canCoverCents,
  formatCentsAsYuan,
  type WalletView,
} from "@/domains/wallet/domain/wallet-view";
import { loadWallet } from "@/domains/wallet/application/load-wallet";
import styles from "./page.module.css";

const DEFAULT_STATEMENT_ID = "STMT-2026-02";
const DEFAULT_AMOUNT_CENTS = 3000;

type CreditRepayPanelProps = {
  /** 档案层：good 且 used=0 时不提供还款入口。 */
  readonly repayAllowed?: boolean;
  readonly statusLabel?: string;
};

export function CreditRepayPanel({
  repayAllowed = false,
  statusLabel,
}: CreditRepayPanelProps) {
  const router = useRouter();
  const [statementId, setStatementId] = useState(DEFAULT_STATEMENT_ID);
  const [amountCents, setAmountCents] = useState(DEFAULT_AMOUNT_CENTS);
  const [statementView, setStatementView] =
    useState<BillingStatementView | null>(null);
  const [walletView, setWalletView] = useState<WalletView | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [walletLoadError, setWalletLoadError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [status, setStatus] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    const id = statementId.trim() || DEFAULT_STATEMENT_ID;
    setLoadError(null);
    fetchCreditStatement(id)
      .then((hit) => {
        if (cancelled) return;
        setStatementView(
          toBillingStatementView({
            id: hit.id,
            userId: hit.userId,
            status: hit.status,
            totalDue: hit.totalDue,
            periodStart: hit.periodStart,
            periodEnd: hit.periodEnd,
            dueDate: hit.dueDate,
            paidAt: hit.paidAt,
          }),
        );
      })
      .catch((err) => {
        if (cancelled) return;
        setStatementView(null);
        setLoadError(err instanceof Error ? err.message : String(err));
      });
    return () => {
      cancelled = true;
    };
  }, [statementId]);

  useEffect(() => {
    let cancelled = false;
    setWalletLoadError(null);
    loadWallet(DEFAULT_CREDIT_USER)
      .then((view) => {
        if (cancelled) return;
        setWalletView(view);
      })
      .catch((err) => {
        if (cancelled) return;
        setWalletView(null);
        setWalletLoadError(err instanceof Error ? err.message : String(err));
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const gate = useMemo(() => {
    if (!repayAllowed) {
      return {
        allowed: false,
        message: statusLabel
          ? `档案${statusLabel}且无已用额度，无需还款`
          : "当前档案无需还款",
      };
    }
    if (!statementView) {
      return {
        allowed: false,
        message: loadError ?? "正在加载账单…",
      };
    }
    if (!statementView.repayAllowed) {
      return {
        allowed: false,
        message: statementView.blockMessage,
      };
    }
    if (!walletView) {
      return {
        allowed: false,
        message: walletLoadError ?? "正在加载钱包…",
      };
    }
    if (!canCoverCents(walletView.balanceCents, amountCents)) {
      return {
        allowed: false,
        message: `余额不足（¥${walletView.balanceYuan} < ¥${formatCentsAsYuan(amountCents)}）`,
      };
    }
    return { allowed: true, message: null as string | null };
  }, [
    repayAllowed,
    statusLabel,
    statementView,
    loadError,
    walletView,
    walletLoadError,
    amountCents,
  ]);

  async function onMarkOverdue() {
    if (!statementView?.markOverdueAllowed) {
      setError(statementView?.blockMessage ?? "当前账单不可标逾期");
      return;
    }
    setBusy(true);
    setError(null);
    setStatus(null);
    try {
      const profile = await postMarkCreditOverdue({
        userId: DEFAULT_CREDIT_USER,
        statementId: statementId.trim() || DEFAULT_STATEMENT_ID,
      });
      setStatus(`已标逾期 · 档案 ${profile.status}`);
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (!gate.allowed) {
      setError(gate.message ?? "不可还款");
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
      setWalletView(await loadWallet(DEFAULT_CREDIT_USER));
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
      <p className={styles.note}>
        仅待还/逾期账单可还；余额须覆盖金额（canCoverCents）
        {statementView
          ? ` · 当前 ${statementView.id}=${statementView.statusLabel}`
          : ""}
        {walletView ? ` · 余额 ¥${walletView.balanceYuan}` : ""}
      </p>
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
        <button type="submit" disabled={busy || !gate.allowed}>
          {busy ? "提交中…" : "还款解冻"}
        </button>
        <button
          type="button"
          disabled={busy || !statementView?.markOverdueAllowed}
          onClick={onMarkOverdue}
        >
          标逾期
        </button>
      </form>
      {!gate.allowed && gate.message ? (
        <p className={styles.note} role="status">
          {gate.message}
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
