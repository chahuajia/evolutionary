# 换电 phase-5 实现（L2）— 商城与促销

**更新**：2026-09-17 14:10 ｜ **压力层**：**L2**｜ **状态**：✅ 切片全绿 · **停 wake** · 待合入
**分支**：`phase/p5-mall` ← 合回 `version/v0` 后删除
**语言**：中文（`profiles/heiniao.yaml`）

## 门禁

1. 只在本 phase 分支 commit；不 push
2. 读 `phase-5-spec.md` + `phase-5-acceptance.md`；IDL 仅对照
3. 实现落 `com.evolutionary.mall`（与 commerce/operator/settlement 分离）

## 句柄

`AGENT_LOOP_WAKE_battery-phase-5` — **已停**

## 切片

| 步 | 范围 | 状态 |
| :-- | :--- | :--- |
| 1 | MERCHANT 入驻 + 禁发 PackageTemplate（AC-40） | ✅ |
| 2 | MallSku + MallOrder（INV-16，无 Entitlement）AC-41 | ✅ |
| 3 | Coupon 核销互斥（AC-42..47） | ✅ |

## 停止

切片全绿 → **停 wake** → 等人确认合入 `version/v0`（本 tick **不**自动 merge）
