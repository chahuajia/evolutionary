# 阶段性复盘：L2 实现 + collaboration 选择性压力（0→7）

**日期**：2026-09-17  
**范围**：battery-pressure（L1 规格）→ phase-0..7（L2 实现）· 对照 collaboration / collab-cli / evolutionary 三仓  
**目的**：增强**下次**选择性压力（选层、落点、命名、工作区），不是再写一轮进度账。

---

## 1. 这次复杂实现里，什么真正产生了选择压力

| 有效 | 说明 |
| :--- | :--- |
| **规格先于代码（A10）** | phase-N spec/AC/契约已存在 → L2 实现是「撞墙验证」而非边写边发明 |
| **INV / DomainErrorCode 可测** | FROZEN、TELEMETRY_STALE、CREDIT_OVERDUE 把 KB 判断变成 surefire 绿/红 |
| **pressure-routing（事后才清晰）** | L1 collab-pressure ≠ L3；本段 L2 才该逼出设计；harvest 才进 collaboration |
| **version × phase 分支** | 合入确认 + 删 phase，强制「可发布线」与「进行中线」分离 |
| **FE∥BE 切片并行** | 信用 UI 壳不堵 IoT BE；代价是 commit 偶发揉在一起 |

| 无效 / 昂贵 | 说明 |
| :--- | :--- |
| **wake 空转风险** | one-shot 800ms 在「有增量」时像进化；无明确停条件会烧上下文 |
| **集群 agent 无独立工作区** | FE agent 与父会话共用 monorepo，成功靠约定而非隔离 |
| **L3 harvest 偏少** | 本段大量拦截是「执行已有条目」，collaboration HEAD 几乎不因 L2 而变——**这其实正常**，但容易误判「KB 没用」 |
| **账本与证据混写** | evolution-log / interceptions 行级证据绑 evolutionary，读起来像「collaboration 被项目淹没」 |

**对下次选择性压力的直接改法**

1. 每个长任务 `loop.md` 首段强制：压力层 · 期望变 HEAD 的仓 · 停 wake 条件（已有 pattern，**要执行**）。
2. L2 撞墙时：**先**写业务仓 `interceptions-candidates.md`，达标再 W5；禁止把轮次报告整页贴进 `meta/evolution-log`。
3. 集群：开任务前声明工作区边界（见 §6）。

---

## 2. collaboration 还值不值得推广？Agent 要不要用？

### 结论（可执行）

| 问题 | 答 |
| :--- | :--- |
| 值得推广吗？ | **值得，但是「判断层 + 代谢层」，不是「第二个 git 日记」**。推广对象应是：跨仓可复用的硬约束与路由；不是项目进度。 |
| Agent 要不要用？ | **要读、少写**。读：`AGENTS.md` 症状表 → 2–3 条相关条目。写：仅 interception 成立或 W4 提炼后走 W5；日常进度只写各仓 WM。 |
| 好处 | 跨对话可复现的设计墙；新人/新会话不从零发明；L1/L2/L3 落点可审计；修剪与代谢有理论（即使执行滞后）。 |
| 坏处 | 检索税（找错层、读太多）；仪式化（每 tick validate 冒充演化）；**账本被项目证据淹没**；命名双轨增加链接税；不读 profile 时语言/偏好漂移。 |

### 对比：用 vs 不用 collaboration（本轮实证）

| 维度 | **用**（本轮实际） | **不用**（反事实） |
| :--- | :--- | :--- |
| Order/Entitlement/Mall 边界 | design-decision + A10 直接拒「揉进 Order」 | 极易在 phase-1~5 长出上帝 Order，后期撕裂成本高 |
| IoT raw / 时序库 | parse-dont-validate、A16、dependency-decision | 适配器逻辑进 swap；或过早引入 Influx |
| 逾期冻权益 | 信用子域 + FROZEN 已在规格 | 可能用「余额扣负」糊过去，AC-52 难测 |
| 进度与合入 | WM + version/phase | 全靠聊天记忆 → 合入边界糊 |
| 空转 | collab-pressure 曾把 L1 当 L3（坏例） | 不用 KB 也会空转，只是空转在「乱写代码」 |
| 速度 | 前期读条目慢 5–15 分钟；撞墙后少返工 | 前期快，phase-5+ 返工概率高 |

**一句话**：collaboration 的 ROI 在「阻止错误结构」，不在「加速打字」。Agent **必须用路由读它**；**禁止**把它当 scratchpad。

---

## 3. collaboration 方向、漂移、改进

### 方向（应坚持）

1. **选择 > 增殖**：ROOT 选择压力、interceptions、代谢配额（3 增 1 处理）。
2. **四实体分离**：WM / 笔记 / COLLABORATION / 主体（A14）；冲突时 collaboration 优先——前提是 collaboration **保持稀薄**。
3. **压测分层**：pressure-routing L1/L2/L3。
4. **id 不可变**（ADR-0009）+ 修剪分代。

### 已出现的漂移

| 漂移 | 表现 | 纠偏 |
| :--- | :--- | :--- |
| **账本项目化** | evolution-log / interceptions 行几乎全是 evolutionary 证据 | 见下「分层账本」 |
| **成功判据工具化** | 测试绿 / 轮次完成 ≈ 进化 | 强制「本 tick 哪个仓 HEAD 变」 |
| **domains 空心** | 几乎只有 `_index` | 域条目补「症状→条目」或并回 AGENTS |
| **命名双轨** | A/W/S 编号+中英混；pattern 纯语义 | 统一「id + 语义 slug」；pattern 补稳定短 id（如 `P-`）或强制 aliases |
| **README 过时计数** | W1–W10、patterns 40… | 由 catalog 生成，禁手改数字 |
| **WM 实体叙事** | 文档仍写「默认 WM 在 collab-cli」；业务进度已在 evolutionary | 改为「每主体仓一份 WM；collab-cli 只登记工具链任务」 |

### 改进 backlog（建议进 collab-cli `candidate-queue`，再 W5）

1. **分层账本**：`meta/evolution-log` 只记 **KB 本体版本**；项目运行日志 → `evolutionary/.../evolution-log.md`（或 `specs/`）；interceptions 主表保留跨项目摘要行，证据链用链接。
2. **interceptions / known-gaps 分片**：主文件 = 索引；详情 `meta/interceptions/<project>/YYYY.md` 或业务仓候选表 + 定期 harvest。
3. **profiles 归属**：维持在 collaboration（跨仓）；项目可覆盖 `working-memory/profile-overrides.yaml`；**不要**放进 CLI 包（CLI 是工具，不是身份源）。
4. **命名统一 RFC**：pattern 增加 `P<n>-` 或 `id: P12` 与文件名语义并存；领域目录与 skill 域标签对齐。
5. **agent 工作区协议**：见 §6。

---

## 4. 对你观察的逐条回应（先判断，再落点）

### 4.1 evolution-log 在 collaboration——应该吗？巨量日志怎么办？

**应该——但只应该放「KB 自身的演化」**（schema、目录契约、路由大改、代谢事件）。  
**不应该**把每轮 evolutionary / collab-pressure 的过程日志当作主日志正文。

当前状态：**机制对、内容过载**——行级证据绑项目，读起来像项目日记进了公共库。

**解法（推荐）**

| 层 | 放哪 | 写什么 |
| :--- | :--- | :--- |
| L0 耐久 | `collaboration/meta/evolution-log.md` | 仅 KB 版本与结构变更（短） |
| L2 项目 | `evolutionary/working-memory/` 或 `specs/` | 轮次/phase 报告（可巨量） |
| 桥 | collaboration 行内 | 一行摘要 + 链接到项目证据路径 |

超过阈值（如主 log > N 行）→ 强制归档旧段到 `meta/archive/evolution-log-*.md` 或砍到「只保留版本里程碑」。

### 4.2 interceptions / known-gaps 同类问题

**同解**：主表 = **跨项目可检索的判决索引**（条目 id · 一句话 · 链接）。  
项目撞墙细节 → 业务仓 `interceptions-candidates.md`；W4 通过后 **一行** harvest 进 collaboration。  
known-gaps：工具债（catalog trigger）留 collaboration；业务 gap 候选留业务仓，≥3 触发代谢配额时再入库。

### 4.3 用户肖像（profile）放哪？

**应继续放在 collaboration `profiles/`**（已决策，跨仓生效：中文 commit、h2-first、unattended）。  
- CLI：只负责 `collab sync`（未实现）**分发**，不拥有真相。  
- 项目层：仅放 **覆盖项**（例如本仓临时英文注释），默认继承 collaboration profile。  
「Persona 角色卡」（产品经理/审查者）若要做，与 profile 分开：profile = 人的偏好；persona = 任务角色，可放 skills 或项目 WM。

### 4.4 模式层命名未统一成 id+语义；领域等是否统一？

**是问题，值得统一**，但 pattern 无 `A/W/S` 前缀是历史设计（思想层 vs 可执行层）。  
建议：**不改已有文件名暴力重编**；改为：

- 每个 pattern frontmatter 增加稳定短 id（`P12`）并写入 aliases；
- 新条目一律 `P<n>-<kebab>.md` 或 `A/W/S` 同构；
- domains：要么充实为「域路由卡」，要么降级为 AGENTS 表驱动，避免空目录装门面。

### 4.5 battery-pressure 开头内容要不要合并到对应目录？

**要整理，不必物理拆散已稳定的 phase 文件。**  
建议目录语义：

```text
working-memory/tasks/battery-pressure/   # 规格压测产物（只读归档气质）
  contracts/ phase-*-spec|acceptance|report
working-memory/tasks/battery-phase-N/    # L2 实现进度（loop/retro/slice）
```

「合并」= README 索引把「规格真相」指到 battery-pressure，「实现真相」指到 phase-N；  
把仍散落在仓根 `specs/round-*` 的旧报告链进 pressure 的 `retro` / `_index`，避免双真相。  
**不要**把 battery-pressure 搬回 collab-cli（已迁出正确）。

### 4.6 前后端 agent 集群了，为何目录下没有工作空间？如何划分？

根因：**决策是 Monorepo**（`decisions.md`）——没有 `frontend.git` / `backend.git`，故无「仓级工作空间」；  
集群 agent 实际是 **同仓不同任务切片**，隔离靠分支/`wip`，不靠目录根。

这导致：子代理与父会话抢同一工作树，commit 揉并、路径约定靠口头。

**推荐实施（由轻到重）**

| 级别 | 做法 | 何时 |
| :--- | :--- | :--- |
| L0 约定 | loop 写明「FE 只碰 `frontend/` · BE 只碰 `backend/`+约定包」 | 立即 |
| L1 WM 沙箱 | `working-memory/agents/fe/` · `be/` 放该 agent 的 loop/笔记（**不**放业务源码） | 下个并行 phase |
| L2 git | `wip/pN-fe-*` / `wip/pN-be-*`，父会话 merge | 已有约定，要强制 |
| L3 工具 | Cursor multi-root 或 worktree 每 agent 一棵树 | 冲突频繁时再上 |

「工作空间」= **权限边界 + 进度文件 + 可选 worktree**，不是再拆一个 git 远程。

---

## 5. 下次选择性压力清单（可贴进新 loop）

- [ ] 声明 L1/L2/L3 与期望 HEAD 仓  
- [ ] 停条件：切片全绿 / 无 interception 候选 / 人确认合入  
- [ ] 撞墙 → 业务仓候选表，不写爆 `meta/evolution-log`  
- [ ] 并行 agent → L0 路径边界 + L1 agents WM + L2 wip 分支  
- [ ] 合入 version 只等人确认；AI 不 push  
- [ ] 复盘后最多 harvest **1–3** 条到 collaboration（代谢配额）

---

## 6. 本轮可 harvest 到 collaboration 的候选（未执行，等人点头）

1. 补强 `pressure-routing`：项目证据不得写入 `evolution-log` 正文（只链出）。  
2. 新 pattern 草案：`project-evidence-vs-kb-ledger`（账本分层）。  
3. 新 skill 草案：`agent-workspace-boundaries`（FE/BE 路径与 WM 沙箱）。  

（执行须走 W5 + 代谢配额，本复盘**只记账不落库**。）
