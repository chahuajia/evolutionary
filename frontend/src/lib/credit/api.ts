/**
 * 兼容薄壳 — 实现已迁至 domains/credit/infrastructure/credit-gateway
 */

export {
  DEFAULT_CREDIT_USER,
  fetchCreditProfile,
  fetchCreditStatements,
  postCreditRepay,
} from "@/domains/credit/infrastructure/credit-gateway";
export type { CreditRepayRequest } from "@/domains/credit/infrastructure/credit-gateway";
