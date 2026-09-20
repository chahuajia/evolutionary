# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-20 14:36  
**模式**：主树父写（小切片）· **禁止**为 <5min 切片建 worktree/派 Task  
**状态**：**▶ 运行中**（常设规则：父自选推进）  
**波次**：wave123 ✅  
**HEAD**：`a24c2bc`  
**idle**：— · **lanes**：0  
**测**：前端 **153/153** · 未 push

## 刚落地

| 波 | 内容 | HEAD | 测 |
| :-- | :--- | :--- | :--- |
| 122 | loadMerchantProfile · 领券/下单岛经用例 | `f6a6b1d` | 153/153 |
| **123** | **loadUserCoupon · 结账岛经用例层** | `a24c2bc` | **153/153** |

**选片依据**：123←结账岛仍自组 UserCoupon/SKU/Merchant；mall 读路径 usecase 收齐。

## 下一刀

| 候选 | 前提 |
| :--- | :--- |
| 门禁轴 idle | 列表猜 / fail-open 已清 |
| ReferralBinding / CreditLedgerDebt | 仍无读口 · 拒 |
| 同构扩面 | 其它域岛仍自组 view（credit/iot/operator） |

## 常设规则

> **父的默认动作是「推进」，不是「等指示」。** 小切片主树父写。
