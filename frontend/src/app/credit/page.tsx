/**
 * 信用账户 — 概览 + 工作流分段（不再纵向堆表单）。
 */

import { PageHeader } from "@/components/page-header";
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
import { CreditRefreshButton } from "./credit-refresh";
import { CreditWorkspace } from "./credit-workspace";
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
    <>
      <PageHeader
        eyebrow="消费者 · 信用"
        title="信用账户"
        description={`用户 ${DEFAULT_CREDIT_USER} 的额度、账单与购退闭环。服务端 RSC 读档案；操作为客户端岛。`}
        action={<CreditRefreshButton />}
      />

      {error ? (
        <p className={styles.alert} role="alert">
          {error}
        </p>
      ) : null}

      {profile ? (
        <section className={styles.overview} aria-label="信用概览">
          <div className={styles.metric}>
            <span className={styles.metricLabel}>可用额度</span>
            <strong className={styles.metricValue}>
              ¥{formatYuan(profile.creditLimit - profile.usedCredit)}
            </strong>
          </div>
          <div className={styles.metric}>
            <span className={styles.metricLabel}>已用 / 总额度</span>
            <strong className={styles.metricValue}>
              ¥{formatYuan(profile.usedCredit)}
              <span className={styles.metricSuffix}>
                / ¥{formatYuan(profile.creditLimit)}
              </span>
            </strong>
          </div>
          <div className={styles.metric}>
            <span className={styles.metricLabel}>状态</span>
            <strong className={styles.metricValue}>
              {CREDIT_STATUS_LABEL[profile.status]}
            </strong>
            <span className={styles.metricMeta}>
              {profile.scoreTier} · 政策 v{profile.policyVersion}
            </span>
          </div>
        </section>
      ) : null}

      {statements.length > 0 ? (
        <section className={styles.statements}>
          <div className={styles.statementsHead}>
            <h2>近期账单</h2>
            <span className={styles.statementsHint}>只读 · RSC</span>
          </div>
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
      ) : null}

      <div className={styles.workspace}>
        <CreditWorkspace />
      </div>
    </>
  );
}
