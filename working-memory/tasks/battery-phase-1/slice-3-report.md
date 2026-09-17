# phase-1 切片 3 报告

**日期**：2026-09-17 ｜ **压力层**：L2

## 交付

| 项 | 内容 |
| :--- | :--- |
| Product | `createMetered` + `meteredRate`；旧 FIXED_PRICE factory 保留 |
| Entitlement | `meteringMode=PAY_AS_YOU_GO`；`validUntil=null` 时 `isActiveAt` 不误杀 |
| UsageEvent | `MeterReading`（int SOC）+ `chargedAmount` |
| Ledger | `METERED_CHARGE`；refId=usageEventId（INV-8） |
| 用例 | `PerformEntitledSwap` 注入 Product/Account/Ledger；COMPLETED 后结算 |
| 测试 | AC-13 / AC-16 |

## 测量

- collaboration：不变
- interceptions：0
- `mvn test`：74 绿
- 计费口径：`(socBefore - socAfter) × rate`（AC-13 文案「10 units」与 80→60 不符，实现取公式）

## 叠在

切片 2 `ad42439`（经 `1e22d79` WM 标记）之上
