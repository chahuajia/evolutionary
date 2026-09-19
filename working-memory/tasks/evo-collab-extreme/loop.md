# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-19 21:30  
**模式**：主树父写（小切片）· **禁止**为 <5min 切片建 worktree/派 Task  
**状态**：**▶ 运行中**（常设规则：父自选推进）  
**波次**：wave76 ✅ · **wave77 ✅**  
**HEAD**：`045fd1f`  
**idle**：— · **lanes**：0  
**测**：前端 **129/129** · PurchaseMallOrderHttpIT 绿 · 未 push

## 刚落地

| 波 | 内容 | HEAD | 测 |
| :-- | :--- | :--- | :--- |
| 76 | GET SKU/Campaign + 面板真接线 | `d504785` | 129/129 |
| **77** | **GET MerchantProfile + 种子 M1 · 购面板可交易门** | `045fd1f` | **129/129** |

**选片依据**：77←购面板商家门此前假种子 ACTIVE；补 GET + MallConfig 种子。

## 下一刀

| 候选 | 前提 |
| :--- | :--- |
| CouponTemplate GET（面额） | 领券 face 仍种子 500¢ |
| PurchaseMallOrder 注入 isActive | BE 购用例尚未卡商家 ACTIVE |
| ReferralBinding | 无 FE 触点 |

## 常设规则

> **父的默认动作是「推进」，不是「等指示」。** 小切片主树父写。
