# phase-4 切片3 报告

**分支**：`phase/4-profit-sharing`  
**交付**：`SettlementBatch` + `RunSettlementBatch`（显式 runBatch）+ `ReverseAccrualsOnRefund`（REVERSED）  
**账本增量**：`LedgerRefType.PROFIT_SHARING_SETTLEMENT`（commerce）  
**验收**：AC-34 / AC-35 / AC-36 · INV-14 / INV-15  
**包**：`com.evolutionary.settlement`

## 要点

- Accrual 创建仍不写账；入账仅在 Batch
- REVERSED 不进 Batch（`findPending` + 状态过滤）
- 已 SETTLED → `ORDER_NOT_REFUNDABLE_SETTLED`
