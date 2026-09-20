# 交接：给下一个 Agent

**写于**：2026-09-20 17:24（v66；波次以 `loop.md` 为准）
**读者**：新开对话的 Agent（不是给用户的作业）
**取代**：v65（仍写 HEAD `d5dfe7b` —— 已过时）

**当前总状态**：**▶ 运行中（主树父写）**。HEAD `fed2f22`。前端 157/157 · lint 绿。
wave142 已落。常设规则：父自选推进，不等人。

> ⚠️ **本文件会腐烂。** 一切以后端代码 + `tasks/evo-collab-extreme/loop.md` 为准。
> 上次交接就因为 HEAD 停在 `303dc35` 而误导（实际早已前进）。

---

## 30 秒定位

你在压 **COLLABORATION 规范能否指导真实项目**，不是在做换电产品。代码是手段。

| 仓 | 绝对路径 | 角色 |
| :--- | :--- | :--- |
| **evolutionary** | `D:\actto\front\project\evolutionary_start\evolutionary` | **真相仓**：换电实现 + 项目 WM/规格 |
| **collab-cli** | `D:\actto\front\project\collab-cli\collab-cli` | CLI/MCP/validate；编排指针 |
| **collaboration** | `D:\actto\front\project\collaboration_aggregate\collaboration` | 长期 KB（126 条） |

用户语言：**简体中文**。本地 commit 可以；**AI 不 push**。
「继续」= 可派集群，**≠** merge 到 `version/v0`、≠ push。

---

## 当前基线（硬事实）

```bash
cd backend && mvn -o test     # →  218 tests, 0 failures, BUILD SUCCESS
```

**基线是绿的。** 这很重要，见硬规则 2。

| 域 | JPA 化 |
| :--- | :--- |
| commerce / credit / mall / settlement / swap / station / battery / iot / admin / **operator** | ✅ 全部清零 |

**全仓 InMemory 注入已清零**（wave43 `09f083f` MerchantProfile JPA）。

> ⚠️ **「还剩多少活」看「仍被 new 的数」，不是 `InMemory*.java` 文件数。**
> 文件留着是有意的（迁移后不再注入）。
> 曾按文件数误判成 "settlement 3 / credit 4 / mall 7"，全错。

---

## 波次

| 波 | 内容 | 结果 |
| :--- | :--- | :--- |
| wave41 | iot 前半（DeviceShadow + TelemetryStore） | ✅ |
| **wave42** | **46a AlertStore ∥ 46b MaintenanceTicket** | ✅ |
| **wave43** | **47a MerchantProfile JPA ∥ 47b 前端 vitest** | ✅ `3d1e8d2` · InMemory 注入清零 |
| **wave44** | **48a operator page RSC ∥ 48b admin page RSC** | ✅ `806b748` · 全部 `page.tsx` 无 `"use client"` |
| **wave45** | **49a credit-profile-view 测 ∥ 49b mall-order-view** | ✅ `9c3aaac` · 前端 6/6 |
| **wave46** | **50a accrual-view ∥ 50b station-view** | ✅ `3b5d3b0` · 前端 10/10 |
| **wave47** | **51a mall-purchase 落地 ∥ 51b settlement-panel 落地** | ✅ `1a89702` |
| **wave48** | **52a mall-checkout-view ∥ 52b credit-journey 意向** | ✅ `6050f7c` · 前端 13/13 |
| **wave49** | **53a iot 工单 RSC ∥ 53b station 接线** | ✅ `6662402` · 前端 13/13 |
| **wave50** | **54a operator 有效价 RSC ∥ 54b home 换电日志 RSC** | ✅ `49fc611` |
| **wave51** | **55a 覆盖价 ¥ ∥ 55b 撤销价 ¥** | ✅ `04fd0f4` |
| **wave52** | **shared/money 分转元一处** | ✅ `4d0a308` |
| **wave53** | **可用额度不变量 + 不可换出站不可选** | ✅ `1a5def8` · 前端 18/18 |
| **wave54** | **canPurchaseOnCredit（仅 good）+ 档案状态入购/串联岛** | ✅ `19a5a8c` |
| **wave55** | **hasCreditHeadroom（可用≤0 不可购）** | ✅ `c33dc11` |
| **wave56** | **canOfferCreditRepay（good 且 used=0 不提供）** | ✅ `5b914f6` · 前端 25/25 |
| **wave57–58** | accrual 门 + settlement GET/RSC | ✅ `cc6bcb3` |
| **wave59** | PackageTemplate 三门 + 发布面板 | ✅ `3307845` |
| **wave60** | PackageOverride 激活/撤销门 | ✅ `1001c8d` |
| **wave61** | UserCoupon 结账可选门 | ✅ `f4bb91d` |
| **wave62** | MallOrder 仅 CREATED 可支付 + INV-16 | ✅ `5345f95` |
| **wave63** | checkout 复用订单契约 + hasDiscount | ✅ `09769fb` |
| **wave64** | wallet 可扣款门 | ✅ `71470bb` |
| **wave65** | MallSku 可购门 | ✅ `f4992bf` |
| **wave66** | Campaign 领券门 | ✅ `ed56382` |
| **wave67–68** | 入驻 SUBMITTED 可批 · 商家 ACTIVE 可交易 | ✅ `371d739` |
| **wave69** | 工单仅 OPEN 需处理 | ✅ `254ac6d` |
| **wave70** | 影子新鲜度：stale 禁计量换电 | ✅ `399486f` |
| **wave71** | 电池状态机：仅 AVAILABLE 可换出 | ✅ `dc18798` · 前端 110/110 |
| **wave72–73** | Entitlement ACTIVE 可换电 · Order PAID 可退 | ✅ `59aa36d` · 前端 122/122 |
| **wave74** | 影子 ShadowStatus/LockState 展示 | ✅ `e931420` · 前端 124/124 |
| **wave75** | SettlementBatch 仅 OPEN 可关账 | ✅ `f1b4121` · 前端 129/129 |
| **wave76** | GET SKU/Campaign + 面板真接线 | ✅ `d504785` · 前端 129/129 |
| **wave77** | GET MerchantProfile + 种子 M1 | ✅ `045fd1f` · 前端 129/129 |
| **wave78** | GET CouponTemplate · 领券面额真接线 | ✅ `3dc1f33` |
| **wave79** | PurchaseMallOrder 卡商家 isActive | ✅ `1608ee4` |
| **wave80** | Checkout 卡商家 isActive | ✅ `2becbc1` · 前端 129/129 |
| **wave81** | BillingStatement DUE/OVERDUE 可还 | ✅ `ec39819` |
| **wave82** | UsageEvent STARTED 可完结 | ✅ `a901fbe` |
| **wave83** | 还款岛 mark-overdue | ✅ `03df9e2` · 前端 140/140 |
| **wave84** | Organization isActive · 批下线结果 | ✅ `4393b0e` · 前端 146/146 |
| **wave85** | 发布/覆盖/撤销卡操作方 ACTIVE | ✅ `9af6599` |
| **wave86–87** | 购买/还款岛 canCoverCents | ✅ `b749430` |
| **wave88** | TelemetryFreshnessPort · 计量岛 GET shadow | ✅ `cf1d8fa` · 前端 146/146 · BE 相关 IT 6/6 |
| **wave89** | 通信丢失岛 canDetectCommLost | ✅ `f28bee6` |
| **wave90** | 结算岛 reverseAllowed 冲销 | ✅ `35a47f3` |
| **wave91** | 带券结账 SKU/商家/钱包门 | ✅ `8080461` |
| **wave92** | 计量换电岛 canCoverCents | ✅ `a193810` |
| **wave93** | selectDefaultEntitlement（AC-14） | ✅ `59906df` · 前端 152/152 |
| **wave94** | 跑批卡 settleAllowed | ✅ `1eacd73` · 前端 152/152 |
| **wave95** | 换电提交卡门 canSwapOutBattery | ✅ `0602abf` |
| **wave96** | POST resolve 工单 · resolveAllowed | ✅ `978fbf2` · 前端 152/152 · IoT IT 10/10 |
| **wave97** | 派生下一版本 HTTP · nextVersionAllowed | ✅ `414ee65` |
| **wave98** | GET 模板读口喂发布/派生门 | ✅ `e9ecc48` · 前端 **152/152** |
| **wave99** | GET 权益读口喂换电/计量岛 | ✅ `b4b6548` · 前端 **152/152** · CreditOverdueHttpIT |
| **wave100** | GET ACTIVE 目录喂默认选卡 | ✅ `d1949dd` · 前端 **152/152** · DefaultSelect* 5/5 |
| **wave101** | GET 组织读口喂操作方门 | ✅ `98d6d8b` · 前端 **152/152** · PublishPackageTemplateHttpIT |
| **wave102** | GET 入驻申请喂批下线/商家入驻 | ✅ `110e0d8` · 前端 **152/152** · ApproveOperatorDownlineHttpIT |
| **wave103** | merchant RSC GET 申请真态 | ✅ `d546fcf` · 前端 **152/152** |
| **wave104** | GET 订单读口喂退款/串联 | ✅ `45a2283` · 前端 **152/152** · RefundOrderHttpIT |
| **wave105** | GET 覆盖读口喂撤销/激活门 | ✅ `081cf05` · 前端 **152/152** · Override IT 4/4 |
| **wave106** | 覆盖激活门对齐模板 PUBLISHED | ✅ `24f737b` · 前端 **153/153** |
| **wave107** | GET 用户券喂带券结账门 | ✅ `7b1eb29` · 前端 **153/153** · ClaimCouponHttpIT 4/4 |
| **wave108** | 权益 GET 旁路计量费率 | ✅ `631e6c0` · 前端 **153/153** · MeteredEntitledSwapHttpIT 3/3 |
| **wave109** | 信用页缺档 fail-closed | ✅ `569f315` · 前端 **153/153** |
| **wave110** | 换电/派生/结算门 fail-closed | ✅ `7f75324` · 前端 **153/153** |
| **wave111** | POST 改基产品 · replaceAllowed | ✅ `747816f` · 前端 **153/153** · PublishPackageTemplateHttpIT 5/5 |
| **wave112** | 还款账单未命中 fail-closed | ✅ `0a9bd06` · 前端 **153/153** |
| **wave113** | GET 单账单读口喂还款门 | ✅ `155f3bc` · 前端 **153/153** · CreditRepayHttpIT |
| **wave114** | 站详情补 canSwapOut · 弃列表猜 | ✅ `a1fe7f0` · 前端 **153/153** · SwapControllerTest 9/9 |
| **wave115** | GET accruals?orderId= 喂冲销门 | ✅ `9545943` · 前端 **153/153** · SettlementHttpIT 5/5 |
| **wave116** | credit 契约迁 domains · loadCreditStatements | ✅ `3c38b28` · 前端 **153/153** |
| **wave117** | merchant RSC 经 loadOnboardingApplication · 删 lib/credit | ✅ `ad211b1` · 前端 **153/153** |
| **wave118** | home RSC 经 loadStationSummaries / loadSwapLogs | ✅ `dfda971` · 前端 **153/153** |
| **wave119** | operator/admin RSC 仅经 application 面 | ✅ `1c1a7e8` · 前端 **153/153** |
| **wave120** | mall RSC 经 loadCampaign 读活动门 | ✅ `5d71df5` · 前端 **153/153** |
| **wave121** | loadMallSku · RSC 双概览 · 下单岛经用例 | ✅ `cad4c22` · 前端 **153/153** |
| **wave122** | loadMerchantProfile · 领券/下单岛经用例 | ✅ `f6a6b1d` · 前端 **153/153** |
| **wave123** | loadUserCoupon · 结账岛经用例层 | ✅ `a24c2bc` · 前端 **153/153** |
| **wave124** | 入驻审批岛经 loadOnboardingApplication | ✅ `5499a8a` · 前端 **153/153** |
| **wave125** | loadPackageOverride · 撤销岛经用例 | ✅ `209e4d7` · 前端 **153/153** |
| **wave126** | loadOrganization · 激活覆盖岛经用例 | ✅ `d628a05` · 前端 **153/153** |
| **wave127** | loadPackageTemplate · 模板岛经用例 | ✅ `4707212` · 前端 **153/153** |
| **wave128** | loadCreditStatement · loadDeviceShadow · 岛经用例 | ✅ `54002ab` · 前端 **153/153** |
| **wave129** | loadEntitlement · 权益换电岛经用例 | ✅ `e405254` · 前端 **153/153** |
| **wave130** | loadActiveEntitlements · loadCommerceOrder | ✅ `469a0a2` · 前端 **153/153** |
| **wave131** | loadAccrualsByOrderId · 冲销岛经用例 | ✅ `27d1837` · 前端 **153/153** |
| **wave132** | couponTemplate/triage/monthlyBilling 经用例 | ✅ `d210b73` · 前端 **153/153** |
| **wave133** | telemetry/commLost/resolve 经用例 | ✅ `ddd46bf` · 前端 **153/153** |
| **wave134** | accrue/reverse/batch 经用例 | ✅ `248e84b` · 前端 **153/153** |
| **wave135** | claim/purchase/checkout 经用例 | ✅ `1986fe7` · 前端 **153/153** |
| **wave136** | purchase/refund/entitledSwap 经用例 | ✅ `ee9e946` · 前端 **153/153** |
| **wave137** | override/nextVersion/publish/onboarding 经用例 | ✅ `c59c022` · 前端 **153/153** |
| **wave138** | policy/repay/downline/revoke 经用例返 View | ✅ `c1a3817` · 前端 **153/153** |
| **wave139** | 站详情/换电经用例 · 岛内 infrastructure 清零 | ✅ `8727dc1` · 前端 **153/153** |
| **wave140** | eslint 禁 app→gateway · 同构岛轴收口 | ✅ `ecdd45b` · 前端 **153/153 · lint 绿** |
| **wave141** | swapAllowed 折入用尽门 | ✅ `d5dfe7b` · 前端 **156/156** |

wave42 两路的分支仍在：`wave42/46a-alert-store-jpa`、`wave42/46b-maintenance-ticket-jpa`。
历史 worktree（35a–45b）约 20+ 棵残留 —— **用户未要求 prune，不要擅自 `worktree remove`**。

---

## 硬规则

1. **不 push**；不 `--force`；不跳过 hook。
2. **派工前置：基线必须绿。** 红基线上派功能切片 → 新红**不可归因**。
   红着只能派「修基线」这一路。（2026-09-19 补进 KB 集群策略，wave42 靠它才可验证）
3. **WM 双写**：改 git 后立刻写本仓 `loop.md` **和** collab-cli `AGENTS.md` 活跃表。
   **先 git 再登记**，勿空登记。
4. **collaboration 不写轮次日记**；拦截先记 `working-memory/interceptions-candidates.md`。
5. **domain/application 零 Spring/JPA**（`DomainFrameworkFreeTest` 会拦）。
   事务边界放 **interfaces**（见下表）。
6. 中文 commit / 注释 / WM。回答从 H2 开始，不客套。
7. 子代理通知 = **同轮 merge + 验绿**，父收口冲突。
8. **收口≠停派。** 基线绿 ∧ 仍有互不改同一方法体的双路 ∧ 用户未说暂停 → **同 tick 必须派**。「需判断」= 父当场选切片，不是问用户、不是停机。不造未接线 view 只过滤切片，不构成停机。

---

## 本会话新立的决定（代码已落，不是待办）

| 题 | 结论 | 证据 |
| :--- | :--- | :--- |
| **事务边界** | `interfaces/TransactionalPerformSwap` 包住 `PerformSwap`，Controller 注入包装类。**不在用例上加 `@Transactional`** | `26b4aba` · `PerformSwapAtomicityTest` |
| **测试隔离** | 每个 Spring context **独立 H2**（`${random.uuid}`）。此前全 JVM 共用一个库 + 种子按 context 跑一次 → 清表后复用缓存 context 报 404 | `3ceda84` |
| **组织种子时机** | 组织须在 **bean 构造时**同步入仓（`OrgAuthorization` 构造时建索引；`ApplicationRunner` 太晚）。`JpaOrganizationRepository` **刻意不加 `@Component`** | `56861f1` |
| enum 内嵌 | 单聚合生命周期 → 内嵌；跨聚合共享词汇独立（`BatteryStatus`/`OrgCapability` 故意不内嵌） | `303dc35` |

---

## 下一刀

见 `tasks/evo-collab-extreme/loop.md`。前端 **156/156** · lint 绿 · HEAD `d5dfe7b`。父自选推进。
门禁/同构岛 idle。下刀 refundBlockMessage（CREATED 文案仍岛内猜）。拒 ReferralBinding/CreditLedgerDebt。
禁止再派 Task/worktree 给 <5min 切片。红基线上仍只许「修基线」。

---

## 常设规则（**替代「等 Claude 点刀」**）

> **父的默认动作是「推进」，不是「等指示」。**

**2026-09-19 15:00 那条「听 Claude 点刀，本执行面不自选」已作废** ——
它把父的职责（选片）外包给了用户，与证据 4「把需判断当停机」是**同一个 bug 的两面**：
把未决的判断当成停止条件。

| 情况 | 动作 |
| :--- | :--- |
| 后端有守卫可对齐 ∧ 能先写红测试 | **自选并推进**（选片是父的职责） |
| 小切片（父 <5min） | **主树父写**，不派（v11 派工成本门槛） |
| 大切片（单路 >15min） | worktree + Task |
| 产品语义无法从代码判定 / 跨仓契约 / 推翻既有决定 | **才问用户**，且给出建议（不抛选择题） |
| `RUNBOOK` 缺口表空了 | 停 wake，W4 |

**判断 ≠ 停机。等指示 ≠ 停下。** 三处可观察：新 `feat` commit / loop 新波 / KB 新 diff。

---

## Collaboration：怎么用

KB 入口：`collaboration/AGENTS.md` 的**症状表**（不要拿 catalog 当第一路由）。
**完整用法见 `collaboration/integrations/usage-guide.md`**（2026-09-19 新增）。

| 场景 | 做 |
| :--- | :--- |
| 设计墙（不变量、边界、错误码、种子时机） | 症状表 1 行 → 读 1–2 条 → 决策 |
| 机械 JPA 切片 | **不读 KB**；对标上一切片 + 目标测绿 |
| 跨对话进度 | 读本文件 + `working-memory/`，不读 KB 全文 |
| 查不到 | **报告"找不到"**，记 `meta/known-gaps`；**不要发明规范** |

---

## 不要做

- 把项目报告写进 `collaboration/meta/evolution-log`
- 为「有产出」新建 KB 条目（入库要 `falsifier`）
- 在**红基线**上派功能切片
- 假设本文件永远准 —— 以后端代码 + `loop.md` 为准
- 把「禁止 commit」与「feat commit 当凭证」当同一条 ——
  本任务惯例是 **L2 切片本地 commit、不 push**

---

## 待验证的假设（**不要当成结论用**）

`borrowing-from-model-arch.md`

从 DeepSeek V4.1-Flash 架构 + 推测解码 + MoE 文献借来的四条，**全部零本地验证**：

| # | 假设 | 怎么验 | 成本 |
| :-- | :--- | :--- | :--- |
| 1 | `acceptance rate` 是派工账本缺的数（现在只记 `dispatched=N recovered=M`，那只是"有回音"，不是"经受住审查"） | 每波多记 3 个数：accepted / reworked / rejected | 低 |
| 2 | 症状表是"廉价查找面"，**不该往里面塞推理** | 10 个真实症状走两条路径，比命中率 | 低 |
| 3 | KB 参与该有**三档**（Full / Reindex / **Reuse**），现在只有两档 | 只观察：标"本会话已确立却重查了"的次数 | 极低 |
| 4 | 条目的"**饿死**"（有链接但零路由）和"孤岛"是两回事 | 已算：96/121 从不走症状表 —— **但结论不是"清理它们"** | 已完成 |

**假设 4 的意外产出（这条最硬，本地证据）**：

> `pruning-policy` 有「引用计数 = 0 → 候选」，而**全库没有"引用计数"这个仪器**。
> 我们建的 `retire --candidates` 是**图结构**的（孤岛），**不是读取行为**的。

⇒ 该做的不是清理 96 条，是**补一把尺**。没有尺之前"该不该清"不可判定。

**并且这次去找了反例，找到一个，修正了结论**：

`check-freshness.mjs` 写于 09-16 22:27，接进 pre-push 是 09-18 22:35 ——
**中间两天仪器存在但从未运行**，一跑就抓出 2/3 仓已烂。

⇒ **"有仪器" ≠ "被执行"。仪器必须接线才算存在。**
（这正是本会话另一处观察：机制只管它被安装的地方。）

**留给下一个人**：找"已接线但从未触发"与"无仪器却执行良好"两类反例。
两轮找不到 → 够格入库。

## 读序（新对话）

1. 本文件
2. `working-memory/README.md` → `tasks/evo-collab-extreme/loop.md`（波次真相）
3. 设计决策：`collaboration/AGENTS.md` 症状表 → 1–2 条正文
4. 跑代码：`mvn -o test`（确认基线）→ `backend/` 对标最近的 `Jpa*Repository`
5. 想理解判断依据：
   - `next-direction.md`（**项目级**：方向与方针，JPA 矿挖完了）
   - `borrowing-from-model-arch.md`（**项目级**：四条待验假设 + 仪器结论）
   - `tasks/evo-collab-extreme/cluster-policy-pressure.md`（任务级：wave42 的三条证据）

> **项目级 vs 任务级的判据**：讲"这个项目往哪走"→ `working-memory/` 根；
> 讲"某一次任务"→ `tasks/<任务>/`。
> 理由：**容器与内容匹配** —— 项目级结论住在任务目录里会被当成"那个任务的东西"，
> 下次换任务就没人翻了。

### ⚠️ 另一个更容易撞的坑：worktree 看不到分岔之后的提交

**症状**：某个 worktree 里 `ls working-memory/tasks/.../` 只有旧文件，
但 `git log` / `git show <分支>:<路径>` 明明看得到 —— 于是怀疑"文件丢了"。

**根因**：那个 worktree checkout 的分支**从更早的 commit 岔出**，
之后的提交不在它的树里。**与文件放在哪个目录无关。**

实测（2026-09-19）：`evo-wt-46a-be` 停在 `3ceda84`，而目标文件提交在 `0f70a19` ——
把文件从任务目录搬到 `working-memory/` 根，**46a 依然看不到**。

**修法只有一个**：用它之前**重新基线到当前 HEAD**。
```bash
git -C evo-wt-XX-be reset --hard $(git -C evolutionary rev-parse HEAD)
```
wave42 派工前就是这么做的（旧基线 `374d5fd` 不含 enum 内嵌）。
**这不是可选项，是派工前置的一部分。**
