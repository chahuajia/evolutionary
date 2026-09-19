/**
 * 运营商工作台 — 发布套餐 + 套餐覆盖 + 撤销覆盖 + 批下线。
 */

import { PageHeader } from "@/components/page-header";
import {
  DEFAULT_DOWNLINE_APPLICATION_ID,
  DEFAULT_OVERRIDE_ID,
  DEFAULT_OVERRIDE_TEMPLATE_ID,
  DEFAULT_PACKAGE_TEMPLATE_ID,
} from "@/domains/operator/infrastructure/operator-gateway";
import { OperatorWorkspace } from "./operator-workspace";

export default function OperatorPage() {
  return (
    <>
      <PageHeader
        eyebrow="运营商 · 配置"
        title="运营配置"
        description={`运营商发布套餐（${DEFAULT_PACKAGE_TEMPLATE_ID}）→ L2 套餐覆盖（${DEFAULT_OVERRIDE_TEMPLATE_ID}）→ 撤销覆盖（${DEFAULT_OVERRIDE_ID}）→ 批运营商下线（${DEFAULT_DOWNLINE_APPLICATION_ID}）。商城商家入驻由总后台审批。`}
      />
      <OperatorWorkspace />
    </>
  );
}
