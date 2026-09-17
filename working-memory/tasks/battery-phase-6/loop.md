# 换电 phase-6 实现（L2）— 信用与先用后付

**更新**：2026-09-17 14:20 ｜ **压力层**：**L2**｜ **状态**：🔄 切片1–2 绿 · 切片3
**分支**：`phase/p6-credit` ← 合回 `version/v0` 后删除
**语言**：中文（`profiles/heiniao.yaml`）
**调度**：800ms one-shot（`AGENT_LOOP_WAKE_battery-phase-6`）

## 门禁

1. 只在本 phase 分支 commit；不 push
2. 读 `phase-6-spec.md` + `phase-6-acceptance.md`；IDL 仅对照
3. 实现落 `com.evolutionary.credit`（独立子域，不并入 PaymentIntent）
4. 仅换电 Order；不碰 MallOrder 信用

## 句柄

`AGENT_LOOP_WAKE_battery-phase-6`

## 切片

| 步 | 范围 | 状态 |
| :-- | :--- | :--- |
| 1 | CreditProfile + PurchaseWithCredit（AC-48/49 · INV-17） | ✅ |
| 2 | BillingStatement + Repay（AC-50/51） | ✅ |
| 3 | 逾期冻结 + 政策版本（AC-52..54） | **本 tick** |

## 停止

切片全绿 → 停 wake → 等人确认合入 `version/v0`
