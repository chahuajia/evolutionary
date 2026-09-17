/**
 * 信用账单 — RSC SSR 读模型；刷新为客户端岛。
 */

import Link from "next/link";
import {
  DEFAULT_CREDIT_USER,
  fetchCreditProfile,
  fetchCreditStatements,
} from "@/domains/credit/infrastructure/credit-gateway";
import {
  CREDIT_STATUS_LABEL,
  STATEMENT_STATUS_LABEL,
  formatYuan,
  type BillingStatement,
  type CreditProfile,
  type StatementStatus,
} from "@/lib/credit/types";
import { CreditPurchasePanel } from "./credit-purchase-panel";
import { CreditRefreshButton } from "./credit-refresh";
import { CreditRepayPanel } from "./credit-repay-panel";
import styles from "./page.module.css";

function statusBadgeClass(status: StatementStatus): string {
  if (status === "DUE") return `${styles.badge} ${styles.badgeDue}`;
  if (status === "PAID") return `${styles.badge} ${styles.badgePaid}`;
  if (status === "OVERDUE") return `${styles.badge} ${styles.badgeOverdue}`;
  return styles.badge;
}

export default async function CreditPage() {
  let profile: CreditProfile | null = null;
  let statements: readonly BillingStatement[] = [];
  let error: string | null = null;

  try {
    const [p, s] = await Promise.all([
      fetchCreditProfile(DEFAULT_CREDIT_USER),
      fetchCreditStatements(DEFAULT_CREDIT_USER),
    ]);
    profile = p;
    statements = s;
  } catch (e) {
    error =
      e instanceof Error
        ? e.message
        : "信用数据拉取失败：请确认 Spring :8080 已启动。";
  }

  return (
    <main className={styles.main}>
      <nav className={styles.nav}>
        <Link href="/">← 换电首页</Link>
      </nav>

      <h1 className={styles.title}>信用账单</h1>
      <p className={styles.note}>
        RSC 对接 Spring <code>GET /credit/profiles/{DEFAULT_CREDIT_USER}</code>
        （服务端直连；浏览器刷新走客户端岛）。{" "}
        <CreditRefreshButton />
      </p>

      {error && (
        <p className={styles.note} role="alert">
          {error}
        </p>
      )}

      {profile && (
        <>
          <section className={styles.panel}>
            <h2>信用档案</h2>
            <dl className={styles.dl}>
              <dt>用户</dt>
              <dd>{profile.userId}</dd>
              <dt>信用额度</dt>
              <dd>¥{formatYuan(profile.creditLimit)}</dd>
              <dt>已用额度</dt>
              <dd>¥{formatYuan(profile.usedCredit)}</dd>
              <dt>可用额度</dt>
              <dd>
                ¥{formatYuan(profile.creditLimit - profile.usedCredit)}
              </dd>
              <dt>状态</dt>
              <dd>
                {CREDIT_STATUS_LABEL[profile.status]}（{profile.status}）
              </dd>
              <dt>评分档</dt>
              <dd>{profile.scoreTier}</dd>
              <dt>政策版本</dt>
              <dd>v{profile.policyVersion}</dd>
            </dl>
          </section>

          <section className={styles.panel}>
            <h2>账单列表</h2>
            <ul className={styles.list}>
              {statements.map((s) => (
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

      <CreditPurchasePanel />
      <CreditRepayPanel />
    </main>
  );
}
