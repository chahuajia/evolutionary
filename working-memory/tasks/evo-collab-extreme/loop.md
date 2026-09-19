# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-19 21:48  
**模式**：主树父写（小切片）· **禁止**为 <5min 切片建 worktree/派 Task  
**状态**：**▶ 运行中**（常设规则：父自选推进）  
**波次**：wave78–79 ✅ · **wave80 ✅**  
**HEAD**：`2becbc1`  
**idle**：— · **lanes**：0  
**测**：前端 **129/129** · Purchase/Checkout* 绿 · 未 push

## 刚落地

| 波 | 内容 | HEAD | 测 |
| :-- | :--- | :--- | :--- |
| **78** | GET CouponTemplate · 领券面额真接线 | `3dc1f33` | 129/129 |
| **79** | PurchaseMallOrder 卡商家 isActive | `1608ee4` | |
| **80** | CheckoutMallOrderWithCoupons 卡 isActive | `2becbc1` | **129/129** |

**选片依据**：78←face 假种子；79–80←BE 购/结账与 FE 可交易门对齐。

## 下一刀

| 候选 | 前提 |
| :--- | :--- |
| ReferralBinding ACTIVE/EXPIRED | 无 FE 触点；可 Accrue 读路径展示 |
| CouponTemplate 展示 view 单测 | 可选薄切片 |
| Station/其它厚守卫 | 扫 BE 未对齐门 |

## 常设规则

> **父的默认动作是「推进」，不是「等指示」。** 小切片主树父写。
