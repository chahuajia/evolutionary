# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-20 14:27  
**模式**：主树父写（小切片）· **禁止**为 <5min 切片建 worktree/派 Task  
**状态**：**▶ 运行中**（常设规则：父自选推进）  
**波次**：wave122 ✅  
**HEAD**：`f6a6b1d`  
**idle**：— · **lanes**：0  
**测**：前端 **153/153** · 未 push

## 刚落地

| 波 | 内容 | HEAD | 测 |
| :-- | :--- | :--- | :--- |
| 121 | loadMallSku · RSC 双概览 · 下单岛经用例 | `cad4c22` | 153/153 |
| **122** | **loadMerchantProfile · 领券/下单岛经用例** | `f6a6b1d` | **153/153** |

**选片依据**：122←领券岛仍自组 CampaignView；商家档案双真源并入 usecase。

## 下一刀

| 候选 | 前提 |
| :--- | :--- |
| 门禁轴 idle | 列表猜 / fail-open 已清 |
| ReferralBinding / CreditLedgerDebt | 仍无读口 · 拒 |
| 同构扩面 | loadUserCoupon / checkout 岛；其它域岛仍自组 view |

## 常设规则

> **父的默认动作是「推进」，不是「等指示」。** 小切片主树父写。
