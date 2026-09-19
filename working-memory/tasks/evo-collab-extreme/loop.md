# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-19 20:03  
**模式**：主树父写（小切片）· **禁止**为 <5min 切片建 worktree/派 Task  
**状态**：**▶ 运行中**（常设规则：父自选推进）  
**波次**：wave57–58 ✅ · **wave59–62 ✅**  
**HEAD**：`5345f95`  
**idle**：— · **lanes**：0  
**测**：前端 **66/66** · 未 push

## 集群修了什么

小切片主树父写（证据 6）。「等点刀」已作废 —— 见文末常设规则。

## 刚落地

| 波 | 内容 | HEAD | 测 |
| :-- | :--- | :--- | :--- |
| 57 | accrual 展示门 + 契约收紧 | `e98dc3b` | 35/35 |
| 58 | GET /settlement/accruals + RSC | `cc6bcb3` | 35/35 |
| 59 | PackageTemplate 三门 + 发布面板 | `3307845` | 45/45 |
| 60 | PackageOverride 激活/撤销门 + 两面板 | `1001c8d` | 54/54 |
| 61 | UserCoupon 结账可选门 + 领/结账面板 | `f4bb91d` | 61/61 |
| 62 | MallOrder 仅 CREATED 可支付 + INV-16 | `5345f95` | **66/66** |

**选片依据（均已核实后端守卫）**：

| 波 | 后端守卫 | 为何不是空映射 |
| :-- | :--- | :--- |
| 59 | publish/replace 仅 DRAFT；nextVersion 仅 PUBLISHED | 红测先写、实现缺失 = 修基线 |
| 60 | activate 仅 DRAFT；revoke 仅 ACTIVE | 与撤销岛同契约 |
| 61 | lock 仅 AVAILABLE；结账选用 = AVAILABLE | 对齐 UserCoupon |
| 62 | pay 仅 CREATED；INV-16 PAID 后禁权益 | **不造** ship/complete 五态门 |

**假设 5'（顺手）**：本轮自起点有 4 个非 docs `feat` → 「有推进」真阳性。未开 worktree，假设 6/7 不适用。

## 下一刀（父自选，不等人）

| 候选 | 前提 |
| :--- | :--- |
| mall-checkout `status` 契约收紧 | 与 MallOrder 同枚举；核实 checkout 返回态 |
| MallSku ON_SALE | 需面板消费点 |
| wallet 余额购门槛 | 先核钱包域是否有 debit 守卫 |

## 常设规则

> **父的默认动作是「推进」，不是「等指示」。**

| 情况 | 动作 |
| :--- | :--- |
| 后端有守卫可对齐 ∧ 能先写红测试 | **自选并推进** |
| 小切片（父 <5min） | **主树父写**，不派 |
| 大切片（单路 >15min） | worktree + Task |
| 产品语义无法从代码判定 | **才问用户**（给建议） |
| `RUNBOOK` 缺口表空了 | 停 wake，W4 |
