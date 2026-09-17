# phase-4 切片1 报告

**分支**：`phase/4-profit-sharing`  
**交付**：`ReferralBinding`（72h 窗口）、`ProfitSharingRule`（百分比≤100，余量 PLATFORM）  
**包**：`com.evolutionary.settlement`

## 探路对齐

- 包名选择与 [探路](51420f87-46f8-440f-a210-0da4ec88b620) 一致
- **缺口**：`OrderStatus` 尚无 `COMPLETED`——切片2 Accrual 须先补触发点，禁止在 PAID 分润
