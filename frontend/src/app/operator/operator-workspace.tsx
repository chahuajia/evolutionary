"use client";

import { WorkflowTabs } from "@/components/workflow-tabs";
import {
  DEFAULT_DOWNLINE_APPLICATION_ID,
  DEFAULT_OVERRIDE_ID,
  DEFAULT_OVERRIDE_TEMPLATE_ID,
  DEFAULT_PACKAGE_TEMPLATE_ID,
} from "@/domains/operator/infrastructure/operator-gateway";
import { ApproveDownlinePanel } from "./approve-downline-panel";
import { PackageOverridePanel } from "./package-override-panel";
import { PublishPackageTemplatePanel } from "./publish-package-template-panel";
import { RevokeOverridePanel } from "./revoke-override-panel";

export function OperatorWorkspace() {
  return (
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
          id: "revoke",
          label: "撤销覆盖",
          description:
            "撤销后有效价回落模板原价；默认 OV-1 / ORG-L2 / U-SZ。",
          content: <RevokeOverridePanel />,
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
  );
}
