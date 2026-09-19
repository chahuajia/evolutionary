# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-19 17:0x
**模式**：主树父写（小切片）· **禁止**为 <5min 切片建 worktree/派 Task  
**状态**：**▶ 运行中**（Claude 接管指挥；常设规则见文末，**不再是"等点刀"**）
**波次**：wave54–56 ✅ · **wave57 ✅** · **wave58 ✅**  
**HEAD**：`cc6bcb3`  
**idle**：— · **lanes**：0  
**测**：后端 220/0 · 前端 35/35 · 未 push

## 集群修了什么

不是子代理在跑。停因三条：Task 创建超时、worktree 30–70s 负吞吐、未提交 WIP 冻住 HEAD。  
现规则见 `cluster-policy-pressure.md` 证据 6：**小切片主树父写**。

> **2026-09-19 15:00 那条「等 Claude 点刀」已作废** ——
> 它把父的职责（选片）外包给了用户。见文末「常设规则」。

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

## wave58 ✅（2026-09-19 16:5x · `cc6bcb3`）

**settlement 读路径**：`GET /settlement/accruals` + RSC 列表。

**前提先核实再选片**（上一刀的教训）：

| 候选 | 前提 |
|---|---|
| mall 五态 | ❌ 后端只有 1 个守卫 → 不变量会很薄 |
| **本刀** | ✅ 读侧端口已有；HTTP 层确无 GET |

后端 220/0 · 前端 35/35 · tsc 0。**顺带补完上一刀半接线的展示不变量**。

**代价（诚实记录）**：给端口加方法，**三个测试 stub 也要跟** ——
核实前提时只查了生产实现，漏了它们，编译当场报错。
**核实前提要包括测试替身。**


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
