# phase-1 切片 2 报告

**日期**：2026-09-17 ｜ **压力层**：L2

## 交付

| 项 | 内容 |
| :--- | :--- |
| Repo | `EntitlementRepository.findActiveByUser` |
| 选择 | `SelectEntitlement.selectDefault`：优先 FINITE，否则 UNLIMITED |
| 用例 | `PerformEntitledSwap.executeWithoutId`；无可用卡 → `ENTITLEMENT_INACTIVE` |
| 测试 | AC-14（P1+P2 ACTIVE，默认扣 P2） |

## 测量

- collaboration：不变
- interceptions：0
- 叠在切片 1 `remainingSwaps` / `consumeSwap` 之上
