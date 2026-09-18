/**
 * 运营商工作台 — 发布套餐 + 套餐覆盖 + 批下线。
 */

"use client";

import { PageHeader } from "@/components/page-header";
import { WorkflowTabs } from "@/components/workflow-tabs";
import {
  DEFAULT_DOWNLINE_APPLICATION_ID,
  DEFAULT_OVERRIDE_TEMPLATE_ID,
  DEFAULT_PACKAGE_TEMPLATE_ID,
} from "@/domains/operator/infrastructure/operator-gateway";
import { ApproveDownlinePanel } from "./approve-downline-panel";
import { PackageOverridePanel } from "./package-override-panel";
import { PublishPackageTemplatePanel } from "./publish-package-template-panel";

export default function OperatorPage() {
  return (
    <>
      <PageHeader
        eyebrow="运营商 · 配置"
        title="运营配置"
        description={`运营商发布套餐（${DEFAULT_PACKAGE_TEMPLATE_ID}）→ L2 套餐覆盖（${DEFAULT_OVERRIDE_TEMPLATE_ID}）→ 批运营商下线（${DEFAULT_DOWNLINE_APPLICATION_ID}）。商城商家入驻由总后台审批。`}
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
          {
            id: "downline",
            label: "批下线",
            description:
              "批运营商下线入驻（非商城商家）；默认 APP-DL1 / ORG-L1。",
            content: <ApproveDownlinePanel />,
          },
        ]}
      />
    </>
  );
}
