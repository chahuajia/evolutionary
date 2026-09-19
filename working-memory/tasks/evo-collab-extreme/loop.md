# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-19 21:23  
**模式**：主树父写（小切片）· **禁止**为 <5min 切片建 worktree/派 Task  
**状态**：**▶ 运行中**（常设规则：父自选推进）  
**波次**：wave75 ✅ · **wave76 ✅**  
**HEAD**：`d504785`  
**idle**：— · **lanes**：0  
**测**：前端 **129/129** · BE PurchaseMallOrder* 绿 · 未 push

## 刚落地

| 波 | 内容 | HEAD | 测 |
| :-- | :--- | :--- | :--- |
| 75 | SettlementBatch 仅 OPEN 可关账 | `f1b4121` | 129/129 |
| **76** | **GET /mall/skus · /campaigns + 面板真接线** | `d504785` | **129/129** |

**选片依据**：76←65/66 展示门此前靠种子假库存；BE 补只读后 FE GET 对齐 stock/预算。

**假设 5'**：本轮 1 个 `feat`（含 BE GET）→ 推进真阳性。

## 下一刀

| 候选 | 前提 |
| :--- | :--- |
| GET MerchantProfile | 购面板商家门仍种子 ACTIVE |
| ReferralBinding ACTIVE/EXPIRED | 无 FE 触点 |
| CouponTemplate GET（面额） | 领券 face 仍种子 500¢ |

## 常设规则

> **父的默认动作是「推进」，不是「等指示」。** 小切片主树父写。
