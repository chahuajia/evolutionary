/**
 * 结算 — 跑结算批 + 可选记分润意向客户端岛对接 Spring。
 */

import Link from "next/link";
import { SettlementPanel } from "./settlement-panel";
import styles from "./page.module.css";

export default function SettlementPage() {
  return (
    <main className={styles.main}>
      <nav className={styles.nav}>
        <Link href="/">← 换电首页</Link>
      </nav>

      <h1 className={styles.title}>结算批</h1>
      <p className={styles.note}>
        批：
        <code>POST /settlement/batches</code>
        （periodStart / periodEnd ISO Instant）。意向：
        <code>POST /settlement/accruals</code>
        （orderId / orgId / amountCents）。
      </p>

      <SettlementPanel />
    </main>
  );
}
