# 换电 phase-4 实现（L2）— 分润与结算

**更新**：2026-09-17 13:25 ｜ **压力层**：**L2**｜ **状态**：✅ 切片1–3 收口 · **停 wake**
**分支**：`phase/4-profit-sharing` ← **待人确认后**合回 `version/v0` 再删
**语言**：中文（`profiles/heiniao.yaml`）

## 门禁

1. 只在本 phase 分支 commit；不 push
2. 读 `phase-4-spec.md` + `phase-4-acceptance.md`；IDL 仅对照
3. 实现落 Java `com.evolutionary.settlement`（或 operator 下 settlement）

## 句柄

`AGENT_LOOP_WAKE_battery-phase-4` — **已停**

## 切片

| 步 | 范围 | 状态 |
| :-- | :--- | :--- |
| 1 | ReferralBinding + ProfitSharingRule（百分比约束） | ✅ `slice-1-report.md` |
| 2 | ProfitShareAccrual 意向（ORDER_COMPLETED） | ✅ `slice-2-report.md` |
| 3 | SettlementBatch + REVERSED（退款未结算） | ✅ `slice-3-report.md` |

## 停止

切片全绿 → **待人确认后** merge 到 `version/v0` → 删本分支 → W4  
（本 tick **不**自动 merge / 删分支；见 `retro.md`）
