"use client";

import { WorkflowTabs } from "@/components/workflow-tabs";
import { CouponClaimPanel } from "./coupon-claim-panel";
import { CouponCheckoutPanel } from "./coupon-checkout-panel";
import { MallPurchasePanel } from "./mall-purchase-panel";

export function MallWorkspace() {
  return (
    <WorkflowTabs
      defaultId="claim"
      tabs={[
        {
          id: "claim",
          label: "领券",
          description: "活动预算内领取用户券。",
          content: <CouponClaimPanel />,
        },
        {
          id: "purchase",
          label: "无券下单",
          description: "商城 SKU 直购。",
          content: <MallPurchasePanel />,
        },
        {
          id: "checkout",
          label: "带券结账",
          description: "核销满减 / 折扣券。",
          content: <CouponCheckoutPanel />,
        },
      ]}
    />
  );
}
