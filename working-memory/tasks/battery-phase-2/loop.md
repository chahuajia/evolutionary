# 换电 phase-2 实现（L2）— 混合支付

**更新**：2026-09-17 12:35 ｜ **压力层**：**L2**｜ **状态**：✅ 切片 1–3 收口 · **停 wake**
**语言**：本地 git 提交说明、WM、Java 注释 → **中文**

## 门禁（每 tick）

1. 期望 HEAD 变：`evolutionary`；仅 interception 成立时改 collaboration
2. 读 `../battery-pressure/phase-2-spec.md` + `phase-2-acceptance.md`
3. 有增量 → 本地 commit（中文 message）+ WM（不 push）

## 句柄

`AGENT_LOOP_WAKE_battery-phase-2` — **已停**

## 切片顺序

| 步 | 范围 | 状态 |
| :-- | :--- | :--- |
| 1 | POINTS 账户 + 混合购买（AC-17/18/19/23） | ✅ `slice-1-report.md` |
| 2 | 混合退款拆账 INV-9（AC-20） | ✅ `slice-2-report.md` |
| 3 | 积分过期 POINTS_EXPIRED（AC-21）+ refType 不混用（AC-22） | ✅ `slice-3-report.md` |

## 停止

见 `retro.md` — **不再 arm wake**
