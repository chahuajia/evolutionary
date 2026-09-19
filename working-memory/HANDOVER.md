# 交接：给下一个 Agent

**写于**：2026-09-19 09:00（v2）
**读者**：新开对话的 Agent（不是给用户的作业）
**取代**：v1（`303dc35` 那版）与 `collab-cli/working-memory/HANDOVER.md` 的对应段落

**当前总状态**：**▶ 集群可运行** —— 用户 2026-09-19 已说「继续」，wave42 已收口。
但**下一刀派不派要单独判断** —— 见「下一刀」。

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

**全仓只剩 1 处 InMemory 注入**：

```
operator/interfaces/OperatorConfig.java:89
    return new InMemoryMerchantProfileRepository();   // ← mall 域的仓储
```

> ⚠️ **「还剩多少活」看「仍被 new 的数」，不是 `InMemory*.java` 文件数。**
> 文件剩 32 个是有意保留（迁移后文件留着、只是不再注入）。
> 曾按文件数误判成 "settlement 3 / credit 4 / mall 7"，全错。

---

## 波次

| 波 | 内容 | 结果 |
| :--- | :--- | :--- |
| wave41 | iot 前半（DeviceShadow + TelemetryStore） | ✅ |
| **wave42** | **46a AlertStore ∥ 46b MaintenanceTicket**（双 worktree） | ✅ 收口于 `2a10645` |
| wave43 | 未派 | — |

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

**JPA 化这条线基本走完了。** 只剩 1 处跨域注入，且**不该盲目 JPA 化** ——
`InMemoryMerchantProfileRepository` 是 mall 域仓储接在 operator 的 config 里，
先判断 merchant profile 是不是共享内核（KB 有 `shared-kernel-across-bc` 一行）。

真正的大块是**前端**：

- `topic/fe-ddd-rsc` 决策表仍标「待实施」：App Router 默认 RSC、服务端读模型、
  客户端仅交互岛、目录按 BC 视图模型
- **但前端零测试**（`frontend/` 下无任何 `*.test.*`）⇒ **前端没有基线**
- 按硬规则 2，直接开大改会重演"红着基线派双路"。**先给前端建一条能跑的验收**
  （哪怕只是 `next build` + 一条冒烟），才谈得上改造

⇒ **合理的第一步是主进程单干（给前端建验收），不是派双路。**

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
> 这不是洁癖：`tasks/` 下的内容会**随 worktree 复制多份**（本机曾有 25 份
> `evo-collab-extreme/`），而项目级结论住在那里，换个 worktree 就找不到了。
