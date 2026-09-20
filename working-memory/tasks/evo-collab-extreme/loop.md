# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-20 13:58  
**模式**：主树父写（小切片）· **禁止**为 <5min 切片建 worktree/派 Task  
**状态**：**▶ 运行中**（常设规则：父自选推进）  
**波次**：wave117 ✅  
**HEAD**：`ad211b1`  
**idle**：— · **lanes**：0  
**测**：前端 **153/153** · 未 push

## 刚落地

| 波 | 内容 | HEAD | 测 |
| :-- | :--- | :--- | :--- |
| 116 | credit 契约迁 domains · loadCreditStatements | `3c38b28` | 153/153 |
| **117** | **merchant RSC 经 loadOnboardingApplication · 删 lib/credit** | `ad211b1` | **153/153** |

**选片依据**：117←`@/lib/credit` 零引用死壳；merchant 仍直调 gateway，对齐 settlement/credit 用例层。

## 下一刀

| 候选 | 前提 |
| :--- | :--- |
| 门禁轴 idle | 列表猜 / fail-open 已清 |
| ReferralBinding / CreditLedgerDebt | 仍无读口 · 拒 |
| 同构扩面 | home/admin/operator RSC 仍直调 gateway |

## 常设规则

> **父的默认动作是「推进」，不是「等指示」。** 小切片主树父写。
