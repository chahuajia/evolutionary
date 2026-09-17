/**
 * 信用账单只读壳 — mock/IDL，不接 Spring（phase-7 切片 1b）
 * 文案级对照 AC-48..54：档案 limit/used/status + 账单 DUE/PAID
 */

import Link from "next/link";
import { MOCK_CREDIT_PROFILE, MOCK_STATEMENTS } from "@/lib/credit/mock";
import {
  CREDIT_STATUS_LABEL,
  STATEMENT_STATUS_LABEL,
  formatYuan,
  type StatementStatus,
} from "@/lib/credit/types";
import styles from "./page.module.css";

function statusBadgeClass(status: StatementStatus): string {
  if (status === "DUE") return `${styles.badge} ${styles.badgeDue}`;
  if (status === "PAID") return `${styles.badge} ${styles.badgePaid}`;
  if (status === "OVERDUE") return `${styles.badge} ${styles.badgeOverdue}`;
  return styles.badge;
}

export default function CreditPage() {
  const profile = MOCK_CREDIT_PROFILE;
  const available = profile.creditLimit - profile.usedCredit;

  return (
    <main className={styles.main}>
      <nav className={styles.nav}>
        <Link href="/">← 换电首页</Link>
      </nav>

      <h1 className={styles.title}>信用账单</h1>
      <p className={styles.note}>
        只读 mock（IDL 字段对齐 phase-6）。不接 Spring；对照 AC-48..54 文案级。
      </p>

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
          <dd>¥{formatYuan(available)}</dd>
          <dt>状态</dt>
          <dd>{CREDIT_STATUS_LABEL[profile.status]}（{profile.status}）</dd>
          <dt>评分档</dt>
          <dd>{profile.scoreTier}</dd>
          <dt>政策版本</dt>
          <dd>v{profile.policyVersion}</dd>
        </dl>
      </section>

      <section className={styles.panel}>
        <h2>账单列表</h2>
        <ul className={styles.list}>
          {MOCK_STATEMENTS.map((s) => (
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
    </main>
  );
}
