/**
 * 运营入驻工作台 — 批准入驻 + 发布套餐 + 套餐覆盖。
 */

"use client";

import { PageHeader } from "@/components/page-header";
import { WorkflowTabs } from "@/components/workflow-tabs";
import {
  DEFAULT_ONBOARDING_APPLICATION_ID,
  DEFAULT_OVERRIDE_TEMPLATE_ID,
  DEFAULT_PACKAGE_TEMPLATE_ID,
  DEFAULT_SHOP_NAME,
} from "@/domains/operator/infrastructure/operator-gateway";
import { OnboardingApprovePanel } from "./onboarding-approve-panel";
import { PackageOverridePanel } from "./package-override-panel";
import { PublishPackageTemplatePanel } from "./publish-package-template-panel";

export default function OperatorPage() {
  return (
    <>
      <PageHeader
        eyebrow="Operator"
        title="运营入驻"
        description={`入驻批准（${DEFAULT_ONBOARDING_APPLICATION_ID} / ${DEFAULT_SHOP_NAME}）→ 运营商发布套餐（${DEFAULT_PACKAGE_TEMPLATE_ID}）→ L2 套餐覆盖（${DEFAULT_OVERRIDE_TEMPLATE_ID}）。`}
      />
      <WorkflowTabs
        defaultId="onboard"
        tabs={[
          {
            id: "onboard",
            label: "批准入驻",
            description: "MERCHANT 申请 → MerchantProfile.active。",
            content: <OnboardingApprovePanel />,
          },
          {
            id: "publish",
            label: "发布套餐",
            description: "OPERATOR 发布草稿模板（AC-24）。",
            content: <PublishPackageTemplatePanel />,
          },
          {
            id: "override",
            label: "套餐覆盖",
            description: "L2 激活 patches + 查询有效价（AC-26）。",
            content: <PackageOverridePanel />,
          },
        ]}
      />
    </>
  );
}
