# phase-0 切片 1 报告

**日期**：2026-09-17 ｜ **压力层**：L2 ｜ **测试**：41 → **49/0**（+8 commerce）

## 交付

| 范围 | 内容 |
| :--- | :--- |
| 领域 | `commerce.domain`：Product、Order、Entitlement、Account、LedgerEntry、LedgerInvariant |
| 用例 | `PurchaseProduct`：发布校验 → 余额校验 → 分录 → INV-1 → PAID + ACTIVE 权益 |
| 不变量 | INV-1 分录平衡；INV-2 权益仅来自 PAID 订单 |

## 测量

- collaboration HEAD：**不变**（符合 L2）
- interceptions：**0**
- `mvn test`：**49/0**

## 下一 tick

切片 3：UsageEvent + BatteryAsset 状态机（INV-3/4）—— 切片 2 过期校验在换电入口一并落地
