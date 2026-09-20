# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-20 10:17  
**模式**：主树父写（小切片）· **禁止**为 <5min 切片建 worktree/派 Task  
**状态**：**▶ 运行中**（常设规则：父自选推进）  
**波次**：wave104 ✅  
**HEAD**：`45a2283`  
**idle**：— · **lanes**：0  
**测**：前端 **152/152** · RefundOrderHttpIT 绿 · 未 push

## 刚落地

| 波 | 内容 | HEAD | 测 |
| :-- | :--- | :--- | :--- |
| 103 | merchant RSC GET 申请真态 | `d546fcf` | 152/152 |
| **104** | **GET 订单读口喂退款/串联** | `45a2283` | **152/152** · RefundOrderHttpIT |

**选片依据**：104←退款岛粘贴 orderId 默认放行；GET `/commerce/orders/{id}` 对齐 refundAllowed。

## 下一刀

| 候选 | 前提 |
| :--- | :--- |
| ReferralBinding / CreditLedgerDebt | 仍无读口 · 拒 |
| GET 覆盖（revoke 前真态） | 激活是 create+activate 一体；撤销侧可真读 ACTIVE |

## 常设规则

> **父的默认动作是「推进」，不是「等指示」。** 小切片主树父写。
