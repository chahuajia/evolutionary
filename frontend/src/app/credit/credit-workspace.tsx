"use client";

import { WorkflowTabs } from "@/components/workflow-tabs";
import { CreditApplyPolicyPanel } from "./credit-apply-policy-panel";
import { CreditJourneyPanel } from "./credit-journey-panel";
import { CreditMonthlyBillingPanel } from "./credit-monthly-billing-panel";
import { CreditPurchasePanel } from "./credit-purchase-panel";
import { CreditRefundPanel } from "./credit-refund-panel";
import { CreditRepayPanel } from "./credit-repay-panel";

type CreditWorkspaceProps = {
  readonly purchaseAllowed?: boolean;
  readonly statusLabel?: string;
};

export function CreditWorkspace({
  purchaseAllowed = true,
  statusLabel,
}: CreditWorkspaceProps) {
  return (
    <WorkflowTabs
      defaultId="journey"
      tabs={[
        {
          id: "journey",
          label: "业务串联",
          description:
            "信用购 → 权益履约 → 退款冲销；购后后端自动记分润意向。",
          content: (
            <CreditJourneyPanel
              purchaseAllowed={purchaseAllowed}
              statusLabel={statusLabel}
            />
          ),
        },
        {
          id: "purchase",
          label: "信用购",
          description: "先用后付开权益（P-CREDIT-1）。",
          content: (
            <CreditPurchasePanel
              purchaseAllowed={purchaseAllowed}
              statusLabel={statusLabel}
            />
          ),
        },
        {
          id: "repay",
          label: "还款解冻",
          description: "逾期后全额还款，恢复换电。",
          content: <CreditRepayPanel />,
        },
        {
          id: "billing",
          label: "月度出账",
          description: "OPEN Debt → Statement DUE。",
          content: <CreditMonthlyBillingPanel />,
        },
        {
          id: "policy",
          label: "政策降额",
          description: "应用政策版本，不清零 usedCredit。",
          content: <CreditApplyPolicyPanel />,
        },
        {
          id: "refund",
          label: "订单退款",
          description: "撤销权益并冲销 PENDING 分润。",
          content: <CreditRefundPanel />,
        },
      ]}
    />
  );
}
