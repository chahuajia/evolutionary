/**
 * 运营入驻工作台。
 */

import { PageHeader } from "@/components/page-header";
import {
  DEFAULT_ONBOARDING_APPLICATION_ID,
  DEFAULT_SHOP_NAME,
} from "@/domains/operator/infrastructure/operator-gateway";
import { OnboardingApprovePanel } from "./onboarding-approve-panel";
import styles from "./page.module.css";

export default function OperatorPage() {
  return (
    <>
      <PageHeader
        eyebrow="Operator"
        title="运营入驻"
        description={`批准商家入驻（默认 ${DEFAULT_ONBOARDING_APPLICATION_ID} / ${DEFAULT_SHOP_NAME}）。发布套餐模板为后续能力。`}
      />
      <div className={styles.surface}>
        <OnboardingApprovePanel />
      </div>
    </>
  );
}
