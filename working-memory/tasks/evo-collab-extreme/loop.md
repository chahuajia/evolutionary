# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-19 23:58  
**模式**：主树父写（小切片）· **禁止**为 <5min 切片建 worktree/派 Task  
**状态**：**▶ 运行中**（常设规则：父自选推进）  
**波次**：wave95–**96 ✅**  
**HEAD**：`978fbf2`  
**idle**：— · **lanes**：0  
**测**：前端 **152/152** · IoT 相关 IT 10/10 · 未 push

## 刚落地

| 波 | 内容 | HEAD | 测 |
| :-- | :--- | :--- | :--- |
| 95 | 换电提交卡门 canSwapOutBattery | `0602abf` | 152/152 |
| **96** | **POST resolve 工单 · resolveAllowed** | `978fbf2` | **152/152 · BE 10/10** |

**选片依据**：95←列表已标可换出但按钮未卡门；96←域 resolve 就绪缺 HTTP。

## 下一刀

| 候选 | 前提 |
| :--- | :--- |
| 模板派生下一版 createNextVersion | 缺 HTTP |
| 权益 FROZEN 真读 | 缺 GET entitlement |
| ReferralBinding / CreditLedgerDebt | 仍无读口 · 拒 |

## 常设规则

> **父的默认动作是「推进」，不是「等指示」。** 小切片主树父写。
