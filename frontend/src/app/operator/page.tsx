/**
 * 运营商工作台 — 发布套餐 + 套餐覆盖。
 */

"use client";

import { PageHeader } from "@/components/page-header";
import { WorkflowTabs } from "@/components/workflow-tabs";
import {
  DEFAULT_OVERRIDE_TEMPLATE_ID,
  DEFAULT_PACKAGE_TEMPLATE_ID,
} from "@/domains/operator/infrastructure/operator-gateway";
import { PackageOverridePanel } from "./package-override-panel";
import { PublishPackageTemplatePanel } from "./publish-package-template-panel";

export default function OperatorPage() {
  return (
    <>
      <PageHeader
        eyebrow="运营商 · 配置"
        title="运营配置"
        description={`运营商发布套餐（${DEFAULT_PACKAGE_TEMPLATE_ID}）→ L2 套餐覆盖（${DEFAULT_OVERRIDE_TEMPLATE_ID}）。商家入驻由总后台审批。`}
      />
      <WorkflowTabs
        defaultId="publish"
        tabs={[
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
