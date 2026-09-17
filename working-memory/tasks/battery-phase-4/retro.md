# battery-phase-4 复盘（W4 简版）

**日期**：2026-09-17 ｜ **压力层**：L2 ｜ **状态**：✅ 切片 1–3 收口，**停 wake**

## 交付链

ReferralBinding + ProfitSharingRule → ProfitShareAccrual（PENDING，不写账）→ SettlementBatch / REVERSED

## 三问

| 问 | 答 |
| :--- | :--- |
| 做得好的 | Accrual/结算分离落地；commerce 仅增量 refType；INV-15 用 PENDING 查询自然排除 |
| 卡住的 | 无 |
| 下次改 | Order 缺 COMPLETED 仍靠 Fact 旁路；合回 v0 后再考虑定时 T+7 |

## Interceptions

0

## 下一步（待人确认）

- **待人确认后** merge 到 `version/v0`，再删本 phase 分支（**勿自动 merge/删分支**）
- 可选：commerce Order 补 COMPLETED，或 REST 接线
