# phase-6 切片2 报告

**分支**：`phase/p6-credit`  
**交付**：`BillingStatement` · `RunMonthlyBilling` · `RepayBillingStatement`  
**验收**：AC-50 / AC-51  
**包**：`com.evolutionary.credit`

## 要点

- 出账：用户 OPEN Debt → Statement `DUE`（宽限 periodEnd+7d）→ Debt `BILLED`
- 还款：仅全额；借 BALANCE / 贷 `CREDIT-CLEARING`；Debt `PAID`；`usedCredit` 下降；status → `good`
- 账本：`LedgerRefType.CREDIT_STATEMENT_REPAYMENT`（refId = statementId）

## 依赖

- 叠在切片1 `CreditProfile` / `CreditLedgerDebt` / `PurchaseWithCredit` 之上
