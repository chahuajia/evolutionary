# phase-1 切片 1 报告

**日期**：2026-09-17 ｜ **压力层**：L2

## 交付

| 项 | 内容 |
| :--- | :--- |
| Product | `SwapLimit` unlimited/finite；旧 `create` 默认 unlimited |
| Entitlement | `remainingSwaps`；`consumeSwap`；`isExhausted` |
| 用例 | `PerformEntitledSwap` 门禁 EXHAUSTED + COMPLETED 后扣次 |
| 测试 | AC-10/11/12 |

## 测量

- collaboration：不变
- interceptions：0
- 切片2/3 候选（探路，暂不入库）：隐式随便扣卡；Order 子类；领域内解析 SOC；购买时扣 METERED
