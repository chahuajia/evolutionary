# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-19 15:01  
**模式**：主树父写（小切片）· **禁止**为 <5min 切片建 worktree/派 Task  
**状态**：**⏸ 听 Claude 点刀**（执行面停派，不自动续 wave57+）  
**波次**：wave54 ✅ · wave55 ✅ · wave56 ✅  
**HEAD**：`5b914f6`  
**idle**：— · **lanes**：0  
**前端测**：25/25 · 未 push

## 集群修了什么

不是子代理在跑。停因三条：Task 创建超时、worktree 30–70s 负吞吐、未提交 WIP 冻住 HEAD。  
现规则见 `cluster-policy-pressure.md` 证据 6：**小切片主树父写**。

2026-09-19 15:00 用户改指挥：**等 Claude 点刀/管理，本执行面不自选下一刀。**

## 刚落地

| 波 | 内容 | HEAD | 测 |
| :-- | :--- | :--- | :--- |
| 52 | `shared/money/format-cents` 一处 | `4d0a308` | |
| 53 | `availableCreditCents` + `canChooseStationForSwap` | `1a5def8` | 18/18 |
| 54 | `canPurchaseOnCredit`：仅 good；档案状态传入购/串联岛 | `19a5a8c` | 20/20 |
| 55 | `hasCreditHeadroom`：可用≤0 不可购；状态优先于额度 | `c33dc11` | 23/23 |
| 56 | `canOfferCreditRepay`：good 且 used=0 不提供还款 | `5b914f6` | **25/25** |

## 下一刀（Claude 已点 · 2026-09-19 15:2x）

**wave57：`settlement/page.tsx` RSC 化 + accrual 展示不变量**

选片依据（全部已核实，不是猜的）：

| 事实 | 证据 |
| :--- | :--- |
| `settlement/page.tsx` **0 个 `await`** —— 纯壳，唯一还没 RSC 化的页 | 对比 iot(2) / operator(2) / credit(2) 均已 RSC |
| 后端有真守卫可对齐 | `ProfitShareAccrual.settle` **仅 PENDING 可结算**；`ReverseAccrualsOnRefund` **已结算不可冲销**（`anySettled → throw`） |
| accrual-view 是**纯 DTO 映射** | 只有一个 `toAccrualView`，无不变量 |

**交付**（加厚切片，一次做完）：
1. `settlement/page.tsx` 服务端取 accruals（对齐 credit 的 `loadCreditProfile` 写法）
2. `accrual-view.ts` 加**带名字的不变量**：`canSettleAccrual` / `canReverseAccrual` + `accrualBlockMessage`
3. `settlement-panel.tsx` 消费它（禁用 + 说明理由）
4. **先写红测试**再加实现（`apps/*.test.ts` 已有 7 个文件可对标）

**不要**：为 <5min 的切片建 worktree / 派 Task（v11 派工成本门槛）。

---

## 常设规则（**替代「等 Claude 点刀」**）

> **父的默认动作是「推进」，不是「等指示」。**

两头都错过：证据 4 是「把需判断当停机」，今天这几小时是「等指挥当停机」——
**同一个 bug 的两面**：把未决的判断当成停止条件。

| 情况 | 动作 |
| :--- | :--- |
| 后端有守卫可对齐 ∧ 能先写红测试 | **自选并推进**（选片是父的职责，不是用户的） |
| 小切片（父 <5min） | **主树父写**，不派（v11） |
| 大切片（单路 >15min） | worktree + Task |
| 产品语义无法从代码判定 / 跨仓契约 / 推翻既有决定 | **才问用户**（且给出你的建议，不是抛选择题） |
| 连 `RUNBOOK` 缺口表都空了 | 停 wake，W4 |

**判断 ≠ 停机。等指示 ≠ 停下。** 三件事都可观察：
`git log` 有无新 `feat`、`loop.md` 有无新波、KB 有无新 diff。
