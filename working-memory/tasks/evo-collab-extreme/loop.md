# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-20 11:20  
**模式**：主树父写（小切片）· **禁止**为 <5min 切片建 worktree/派 Task  
**状态**：**▶ 运行中**（常设规则：父自选推进）  
**波次**：wave107 ✅  
**HEAD**：`7b1eb29`  
**idle**：— · **lanes**：0  
**测**：前端 **153/153** · ClaimCouponHttpIT 4/4 · 未 push

## 刚落地

| 波 | 内容 | HEAD | 测 |
| :-- | :--- | :--- | :--- |
| 106 | 覆盖激活门对齐模板 PUBLISHED | `24f737b` | 153/153 |
| **107** | **GET 用户券喂带券结账 checkoutSelectable** | `7b1eb29` | **153/153** · Claim 4/4 |

**选片依据**：107←粘贴券 id 曾清门跳过券态；GET `/mall/user-coupons/{id}` 对齐 AVAILABLE。

## 下一刀

| 候选 | 前提 |
| :--- | :--- |
| 计量岛 SEED 费率 → 权益/产品 GET 旁路 | 估费与 canCoverCents |
| ReferralBinding / CreditLedgerDebt | 仍无读口 · 拒 |

## 常设规则

> **父的默认动作是「推进」，不是「等指示」。** 小切片主树父写。
