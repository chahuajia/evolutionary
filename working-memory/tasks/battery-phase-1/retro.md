# battery-phase-1 复盘（W4 简版）

**日期**：2026-09-17 ｜ **压力层**：L2 ｜ **测试**：74/0 ｜ **状态**：✅ 切片 1–3 收口，**停 wake**

## 交付链

FINITE 次卡（INV-6/7）→ 默认选卡（AC-14）→ METERED 后付（INV-8 / AC-13/16）

## 三问

| 问 | 答 |
| :--- | :--- |
| 做得好的 | 探路约束钉死「COMPLETED 后写分录 / 不改 FIXED_PRICE 预付」；旧 Product factory 兼容 |
| 卡住的 | AC-13 文案「10 units」与 `(80-60)×rate` 不一致——取公式 |
| 下次改 | 验收数字与公式同表写出，避免口头换算漂移 |

## Interceptions

0

## 下一步（非本 tick）

- phase-2 规格（积分/拆分 LedgerRef）或 commerce→REST
- AC-15 显式 entitlementId（阶段 0 已有入口，可补验收测）
