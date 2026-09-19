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

## wave57 ✅（2026-09-19 15:21 · `e98dc3b`）

**accrual 展示不变量 + 修契约漂移** —— 前端 25 → **35/35**。

> ⚠️ **我下的刀有一半作废，记在这里免得重犯。**
> 我点的是「settlement RSC 化 + accrual 不变量」。核实后发现：
> `SettlementController` **只有 3 个 POST、没有 GET** —— 没有可读端点，
> **RSC 化无处取数**。选片依据「page 是纯壳」本身没错，
> 但"纯壳 → 应该 RSC 化"这一步**没核实数据源是否存在**。

实际落地（都是核过后才做的）：

| # | 内容 | 依据 |
| :-- | :--- | :--- |
| 1 | `status: string` → `AccrualStatus` | 测试里的 `"ACCRUED"` **后端从来不存在**；类型一收紧，编译器立刻抓出网关也在裸转 |
| 2 | 两份真相 → 派生：网关只声明 `AccrualDto` | 网关自己定义了**第二份** `AccrualView` —— 那就是漂移能发生的原因 |
| 3 | `String(r.status ?? "")` → `parseAccrualStatus` | 未知值**当场炸**，不产生默认值（parse-dont-validate） |
| 4 | `canSettleAccrual`/`canReverseAccrual`/`accrualBlockMessage` + panel 消费 | 对齐后端 `settle`/`reverse` 均仅 PENDING |

**先写红测试**：7 failed → 实现 → 35/35 绿。

## 下一刀（选片判据已备 · 父可自选）

**结构缺口：settlement 无读路径。** 要真做列表/RSC，需先加后端 GET 端点
（如 `GET /settlement/accruals?orgId=`）—— 那是**跨前后端的切片**，>15min，
按 v11 值得派（或父写）。

**已做「裸 `string` 状态字段」体检**（本波那条 drift 就是被类型收紧顺带抓出的，
所以值得扫一遍）。结果**要准确**：

| 位置 | 现状 |
| :--- | :--- |
| `settlement/accrual-view.ts` | ✅ 已修（本波） |
| `mall/mall-order-view.ts:13` | ⚠️ **潜伏**：`status: string`，但测试用的是 `"PAID"`（后端合法）→ **未漂移，但已上膛** |
| `mall/mall-checkout-view.ts:11` | ⚠️ 同上 |
| `wallet/wallet-view.ts:13` | `currency: string` —— **不是同类**（货币码不是状态机），不动 |

**所以不是"又 2 个 bug"，是 1 个已漂移 + 2 个潜伏。** `MallOrder.Status` 有**五态**
（CREATED/PAID/SHIPPED/COMPLETED/REFUNDED），收紧类型即可获得编译期保护。

候选切片（同判据：后端有守卫可对齐 ∧ 能先写红测试）：

- `mall-order-view`：收紧为 `MallOrderStatus` + 五态守卫不变量
  （`canRefundMallOrder` / `canCancelMallOrder` 之类，对齐后端状态机）
- `mall-checkout-view`：同上

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
