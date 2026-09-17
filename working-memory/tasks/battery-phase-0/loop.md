# 换电 phase-0 实现（L2）— 垂直切片

**更新**：2026-09-17 12:10 ｜ **压力层**：**L2**｜ **状态**：✅ 切片 1–4 完成

## 门禁（每 tick）

1. **期望 HEAD 变**：`evolutionary` 实现/测试；**仅** interception 成立时 `collaboration` 条目 diff
2. 读 `../battery-pressure/phase-0-spec.md` + `contracts/phase-0.ts`
3. 有可验证增量 → 本地 commit + 刷新 WM（不 push）

## 句柄

`AGENT_LOOP_WAKE_battery-phase-0` — 按需 arm，**禁止空转**

## 切片顺序

| 步 | 范围 | 状态 |
| :-- | :--- | :--- |
| 1 | Product + Order 领域 + 支付分录（INV-1/2） | ✅ `slice-1-report.md` |
| 2 | Entitlement 生成 + 过期校验（INV-2） | ✅ 生成+换电入口过期校验 |
| 3 | UsageEvent + BatteryAsset 状态机（INV-3/4） | ✅ `slice-3-report.md` |
| 4 | 退款链 + REFUND_BLOCKED（INV-5） | ✅ `slice-4-report.md` |

## 停止

规格切片全绿且连续 3 tick 无 interception 候选 → 停 wake，W4 复盘
