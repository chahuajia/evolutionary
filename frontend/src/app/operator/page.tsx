/**
<<<<<<< HEAD
 * 运营入驻工作台。
=======
 * 运维/运营 — 批准入驻 + 发布套餐模板客户端岛对接 Spring。
>>>>>>> wave19/23b-publish-pkg-fe
 */

import { PageHeader } from "@/components/page-header";
import {
  DEFAULT_ONBOARDING_APPLICATION_ID,
  DEFAULT_PACKAGE_TEMPLATE_ID,
  DEFAULT_PUBLISH_ACTOR_ORG_ID,
  DEFAULT_PUBLISH_ACTOR_USER_ID,
  DEFAULT_SHOP_NAME,
} from "@/domains/operator/infrastructure/operator-gateway";
import { OnboardingApprovePanel } from "./onboarding-approve-panel";
import { PublishPackageTemplatePanel } from "./publish-package-template-panel";
import styles from "./page.module.css";

export default function OperatorPage() {
  return (
<<<<<<< HEAD
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
=======
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
      <p className={styles.note}>
        发布套餐模板：
        <code>POST /operator/templates/{"{templateId}"}/publish</code>
        （默认 {DEFAULT_PACKAGE_TEMPLATE_ID} / {DEFAULT_PUBLISH_ACTOR_ORG_ID} /{" "}
        {DEFAULT_PUBLISH_ACTOR_USER_ID}；对齐 23a）。actor 可编辑。
      </p>

      <OnboardingApprovePanel />
      <PublishPackageTemplatePanel />
    </main>
>>>>>>> wave19/23b-publish-pkg-fe
  );
}
