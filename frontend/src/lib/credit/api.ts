/**
 * 兼容薄壳 — 实现已迁至 domains/credit/infrastructure/credit-gateway
 */

export {
  DEFAULT_CREDIT_USER,
  fetchCreditProfile,
  fetchCreditStatements,
  postCreditPurchase,
  postCreditRepay,
} from "@/domains/credit/infrastructure/credit-gateway";
export type {
  CreditPurchaseRequest,
  CreditPurchaseResult,
  CreditRepayRequest,
} from "@/domains/credit/infrastructure/credit-gateway";
