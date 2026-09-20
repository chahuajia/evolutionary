# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-20 10:08  
**模式**：主树父写（小切片）· **禁止**为 <5min 切片建 worktree/派 Task  
**状态**：**▶ 运行中**（常设规则：父自选推进）  
**波次**：wave103 ✅  
**HEAD**：`d546fcf`  
**idle**：— · **lanes**：0  
**测**：前端 **152/152** · 未 push

## 刚落地

| 波 | 内容 | HEAD | 测 |
| :-- | :--- | :--- | :--- |
| 102 | GET 入驻申请喂批下线/商家入驻 | `110e0d8` | 152/152 · ApproveOperatorDownlineHttpIT |
| **103** | **merchant RSC GET 申请真态** | `d546fcf` | **152/152** |

**选片依据**：103←商家页仍硬编码 SUBMITTED；复用 GET `/operator/onboarding/{id}`。

## 下一刀

| 候选 | 前提 |
| :--- | :--- |
| ReferralBinding / CreditLedgerDebt | 仍无读口 · 拒 |
| 扫余种子门 | 搜面板仍硬编码状态的岛 |

## 常设规则

> **父的默认动作是「推进」，不是「等指示」。** 小切片主树父写。
