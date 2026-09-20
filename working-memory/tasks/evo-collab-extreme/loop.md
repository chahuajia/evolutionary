# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-20 10:06  
**模式**：主树父写（小切片）· **禁止**为 <5min 切片建 worktree/派 Task  
**状态**：**▶ 运行中**（常设规则：父自选推进）  
**波次**：wave102 ✅  
**HEAD**：`110e0d8`  
**idle**：— · **lanes**：0  
**测**：前端 **152/152** · ApproveOperatorDownlineHttpIT 3/3 · 未 push

## 刚落地

| 波 | 内容 | HEAD | 测 |
| :-- | :--- | :--- | :--- |
| 101 | GET 组织读口喂操作方门 | `98d6d8b` | 152/152 · PublishPackageTemplateHttpIT |
| **102** | **GET 入驻申请喂批下线/商家入驻** | `110e0d8` | **152/152** · ApproveOperatorDownlineHttpIT |

**选片依据**：102←批下线/商家入驻仍种子 SUBMITTED；GET `/operator/onboarding/{id}` 对齐 approveAllowed。

## 下一刀

| 候选 | 前提 |
| :--- | :--- |
| merchant RSC 种子申请 | 展示页仍硬编码 SUBMITTED（可改 GET） |
| ReferralBinding / CreditLedgerDebt | 仍无读口 · 拒 |

## 常设规则

> **父的默认动作是「推进」，不是「等指示」。** 小切片主树父写。
