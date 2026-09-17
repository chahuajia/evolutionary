# 换电 phase-5 实现（L2）— 商城与促销

**更新**：2026-09-17 14:00 ｜ **压力层**：**L2**｜ **状态**：✅ 已合入 `version/v0` · **停 wake**
**分支**：`version/v0`（`phase/p5-mall` 已删）
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

切片全绿 → merge 到 `version/v0` → 删本分支 → W4
