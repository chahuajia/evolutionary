/**
 * 兼容薄壳 — 类型已迁至 domains/credit/domain/credit-contracts
 */

export {
  CREDIT_STATUS_LABEL,
  STATEMENT_STATUS_LABEL,
  CreditStatusValues,
  StatementStatusValues,
  formatYuan,
  isCreditStatus,
  isStatementStatus,
} from "@/domains/credit/domain/credit-contracts";
export type {
  BillingStatement,
  CreditProfile,
  CreditStatus,
  MoneyCents,
  ScoreTier,
  StatementId,
  StatementStatus,
  UserId,
} from "@/domains/credit/domain/credit-contracts";
