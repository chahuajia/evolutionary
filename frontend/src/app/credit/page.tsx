/**
 * 信用账单 — 经 `/api/credit` 对接 Spring（DevSeed U1）
 */

"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import {
  DEFAULT_CREDIT_USER,
  fetchCreditProfile,
  fetchCreditStatements,
} from "@/lib/credit/api";
import {
  CREDIT_STATUS_LABEL,
  STATEMENT_STATUS_LABEL,
  formatYuan,
  type BillingStatement,
  type CreditProfile,
  type StatementStatus,
} from "@/lib/credit/types";
import styles from "./page.module.css";

function statusBadgeClass(status: StatementStatus): string {
  if (status === "DUE") return `${styles.badge} ${styles.badgeDue}`;
  if (status === "PAID") return `${styles.badge} ${styles.badgePaid}`;
  if (status === "OVERDUE") return `${styles.badge} ${styles.badgeOverdue}`;
  return styles.badge;
}

type LoadState =
  | { kind: "loading" }
  | { kind: "error"; message: string }
  | {
      kind: "ok";
      profile: CreditProfile;
      statements: readonly BillingStatement[];
    };

export default function CreditPage() {
  const [state, setState] = useState<LoadState>({ kind: "loading" });

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const [profile, statements] = await Promise.all([
          fetchCreditProfile(DEFAULT_CREDIT_USER),
          fetchCreditStatements(DEFAULT_CREDIT_USER),
        ]);
        if (!cancelled) {
          setState({ kind: "ok", profile, statements });
        }
      } catch (e) {
        if (!cancelled) {
          setState({
            kind: "error",
            message:
              e instanceof Error
                ? e.message
                : "信用数据拉取失败：请确认 Spring :8080 已启动且 Next rewrite /api 生效。",
          });
        }
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <main className={styles.main}>
      <nav className={styles.nav}>
        <Link href="/">← 换电首页</Link>
      </nav>

      <h1 className={styles.title}>信用账单</h1>
      <p className={styles.note}>
        对接 Spring <code>GET /credit/profiles/{DEFAULT_CREDIT_USER}</code>
        （经 Next <code>/api</code> rewrite）。
      </p>

      {state.kind === "loading" && <p className={styles.note}>加载中…</p>}

      {state.kind === "error" && (
        <p className={styles.note} role="alert">
          {state.message}
        </p>
      )}

      {state.kind === "ok" && (
        <>
          <section className={styles.panel}>
            <h2>信用档案</h2>
            <dl className={styles.dl}>
              <dt>用户</dt>
              <dd>{state.profile.userId}</dd>
              <dt>信用额度</dt>
              <dd>¥{formatYuan(state.profile.creditLimit)}</dd>
              <dt>已用额度</dt>
              <dd>¥{formatYuan(state.profile.usedCredit)}</dd>
              <dt>可用额度</dt>
              <dd>
                ¥
                {formatYuan(
                  state.profile.creditLimit - state.profile.usedCredit,
                )}
              </dd>
              <dt>状态</dt>
              <dd>
                {CREDIT_STATUS_LABEL[state.profile.status]}（
                {state.profile.status}）
              </dd>
              <dt>评分档</dt>
              <dd>{state.profile.scoreTier}</dd>
              <dt>政策版本</dt>
              <dd>v{state.profile.policyVersion}</dd>
            </dl>
          </section>

          <section className={styles.panel}>
            <h2>账单列表</h2>
            <ul className={styles.list}>
              {state.statements.map((s) => (
                <li key={s.id} className={styles.item}>
                  <div className={styles.itemHead}>
                    <span>{s.id}</span>
                    <span className={statusBadgeClass(s.status)}>
                      {STATEMENT_STATUS_LABEL[s.status]}
                    </span>
                  </div>
                  <div className={styles.meta}>
                    账期 {s.periodStart} ~ {s.periodEnd} · 应还 ¥
                    {formatYuan(s.totalDue)} · 到期 {s.dueDate}
                    {s.paidAt ? ` · 已还于 ${s.paidAt.slice(0, 10)}` : ""}
                  </div>
                </li>
              ))}
            </ul>
          </section>
        </>
      )}
    </main>
  );
}
