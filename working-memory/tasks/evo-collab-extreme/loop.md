# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-20 12:20  
**模式**：主树父写（小切片）· **禁止**为 <5min 切片建 worktree/派 Task  
**状态**：**▶ 运行中**（常设规则：父自选推进）  
**波次**：wave115 ✅  
**HEAD**：`f9b94d3`  
**idle**：— · **lanes**：0  
**测**：前端 **153/153** · SwapControllerTest 9/9 · SettlementHttpIT 5/5 · 未 push

## 刚落地

| 波 | 内容 | HEAD | 测 |
| :-- | :--- | :--- | :--- |
| 114 | 站详情补 canSwapOut · 弃列表猜 | `a1fe7f0` | 153/153 · Swap 9/9 |
| **115** | **GET accruals?orderId= 喂冲销门** | `9545943` | **153/153** · Settlement 5/5 |

**选片依据**：114←详情无 canSwapOut 时用列表猜 selectable；115←冲销扫 org 列表猜 order（同构 wave113）。

## 下一刀

| 候选 | 前提 |
| :--- | :--- |
| 面板门禁轴可 idle | 列表猜门基本清完 |
| ReferralBinding / CreditLedgerDebt | 仍无读口 · 拒 |
| 改扫其他轴 | 如 `@/lib/credit` 迁 domains |

## 常设规则

> **父的默认动作是「推进」，不是「等指示」。** 小切片主树父写。
