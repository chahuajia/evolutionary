/**
 * 总后台 — 平台批准商家入驻。
 */

"use client";

import { PageHeader } from "@/components/page-header";
import {
  DEFAULT_ONBOARDING_APPLICATION_ID,
  DEFAULT_SHOP_NAME,
} from "@/domains/operator/infrastructure/operator-gateway";
import { OnboardingApprovePanel } from "./onboarding-approve-panel";
import styles from "./page.module.css";

export default function AdminPage() {
  return (
    <>
      <PageHeader
        eyebrow="总后台 · 审批"
        title="平台批准商家入驻"
        description={`商城商家入驻由总后台审批（${DEFAULT_ONBOARDING_APPLICATION_ID} / ${DEFAULT_SHOP_NAME}），非运营商职责。`}
      />
      <div className={styles.surface}>
        <OnboardingApprovePanel />
      </div>
    </>
  );
}
