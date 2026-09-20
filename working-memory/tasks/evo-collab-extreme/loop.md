# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-20 14:23  
**模式**：主树父写（小切片）· **禁止**为 <5min 切片建 worktree/派 Task  
**状态**：**▶ 运行中**（常设规则：父自选推进）  
**波次**：wave121 ✅  
**HEAD**：`cad4c22`  
**idle**：— · **lanes**：0  
**测**：前端 **153/153** · 未 push

## 刚落地

| 波 | 内容 | HEAD | 测 |
| :-- | :--- | :--- | :--- |
| 120 | mall RSC 经 loadCampaign 读活动门 | `5d71df5` | 153/153 |
| **121** | **loadMallSku · RSC 双概览 · 下单岛经用例** | `cad4c22` | **153/153** |

**选片依据**：121←SKU 读模型仍散落；对齐 loadWallet 岛经 usecase。

## 下一刀

| 候选 | 前提 |
| :--- | :--- |
| 门禁轴 idle | 列表猜 / fail-open 已清 |
| ReferralBinding / CreditLedgerDebt | 仍无读口 · 拒 |
| 同构扩面 | loadMerchantProfile / loadCampaign 进领券岛 |

## 常设规则

> **父的默认动作是「推进」，不是「等指示」。** 小切片主树父写。
