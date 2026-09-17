# phase-0 切片 4 报告

**日期**：2026-09-17 ｜ **压力层**：L2 ｜ **测试**：68/0

## 交付

| 范围 | 内容 |
| :--- | :--- |
| 领域 | `Entitlement.revoke()`；`LedgerEntry.orderRefund(...)` |
| 用例 | `RefundOrder`：PAID 校验 → STARTED 拦截 → 反向分录 → REVOKED + REFUNDED |
| 仓储扩展 | `OrderRepository.get`；`EntitlementRepository.findByOrderId`；`UsageEventRepository.findStartedByEntitlement` |
| 不变量 | INV-5（退款撤销权益）；Q1 `REFUND_BLOCKED_IN_PROGRESS_SWAP` |

## 测量

- collaboration HEAD：**不变**（符合 L2）
- interceptions：**0**
- `mvn test`：**68/0**

## 下一 tick

规格切片 1–4 全绿 → 连续 tick 无 interception 候选则停 wake / W4 复盘
