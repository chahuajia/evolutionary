/**
 * 运维/运营 — 批准商家入驻客户端岛对接 Spring。
 */

import Link from "next/link";
import {
  DEFAULT_ONBOARDING_APPLICATION_ID,
  DEFAULT_SHOP_NAME,
} from "@/domains/operator/infrastructure/operator-gateway";
import { OnboardingApprovePanel } from "./onboarding-approve-panel";
import styles from "./page.module.css";

export default function OperatorPage() {
  return (
    <main className={styles.main}>
      <nav className={styles.nav}>
        <Link href="/">← 换电首页</Link>
      </nav>

      <h1 className={styles.title}>运维 / 运营</h1>
      <p className={styles.note}>
        批准入驻：
        <code>POST /operator/onboarding/{"{applicationId}"}/approve</code>
        （默认 {DEFAULT_ONBOARDING_APPLICATION_ID} / {DEFAULT_SHOP_NAME}；与 19a
        种子 APP-M1 对齐）。shopName 可编辑。
      </p>

      <OnboardingApprovePanel />
    </main>
  );
}
