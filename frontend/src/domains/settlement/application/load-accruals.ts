/**
 * 用例：加载某组织的应计展示模型（编排 gateway，不写 fetch 细节）。
 *
 * @remarks
 * 对齐 `credit/application/load-credit-profile.ts` —— 同一种"服务端读模型"。
 * 这是本仓前端 DDD 的写法：**页面不直接调 gateway**，中间隔一层用例，
 * 这样视图模型的构造规则只有一处（[[patterns/domain-purity-is-structural]]）。
 */

import {
  toAccrualView,
  type AccrualView,
} from "@/domains/settlement/domain/accrual-view";
import {
  fetchAccruals,
  fetchAccrualsByOrderId,
} from "@/domains/settlement/infrastructure/settlement-gateway";

/** 工作台默认看的组织 —— 与后端种子规则对齐（ORG-L2）。 */
export const DEFAULT_SETTLEMENT_ORG = "ORG-L2";

export async function loadAccruals(
  orgId: string = DEFAULT_SETTLEMENT_ORG,
): Promise<AccrualView[]> {
  const dtos = await fetchAccruals(orgId);
  // 视图模型在这里统一构造 —— 含展示不变量（settleAllowed / reverseAllowed /
  // blockMessage）。面板只消费，不自己判断。
  return dtos.map(toAccrualView);
}

/** GET ?orderId= — 冲销门按订单真态。 */
export async function loadAccrualsByOrderId(
  orderId: string,
): Promise<AccrualView[]> {
  const dtos = await fetchAccrualsByOrderId(orderId);
  return dtos.map(toAccrualView);
}
