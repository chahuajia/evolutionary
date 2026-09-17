# 换电 phase-1 实现（L2）— 多 Product 规则

**更新**：2026-09-17 12:23 ｜ **压力层**：**L2**｜ **状态**：✅ 切片 1–3 收口 · **停 wake**

## 门禁（每 tick）

1. **期望 HEAD 变**：`evolutionary`；仅 interception 成立时改 collaboration
2. 读 `../battery-pressure/phase-1-spec.md` + `contracts/phase-1.ts` + `phase-1-acceptance.md`
3. 有增量 → 本地 commit + WM（不 push）

## 句柄

`AGENT_LOOP_WAKE_battery-phase-1` — **已停**

## 切片顺序

| 步 | 范围 | 状态 |
| :-- | :--- | :--- |
| 1 | FINITE 次卡 remainingSwaps（INV-6/7 · AC-10/11/12） | ✅ `slice-1-report.md` |
| 2 | 多 Entitlement 默认策略优先 FINITE（AC-14） | ✅ `slice-2-report.md` |
| 3 | METERED + PAY_AS_YOU_GO + METERED_CHARGE（INV-8 · AC-13） | ✅ `slice-3-report.md` |

## 停止

切片全绿 → **停 wake**；见 `retro.md`
