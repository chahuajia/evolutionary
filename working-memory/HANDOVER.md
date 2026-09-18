# 交接：给下一个 Agent

**写于**：2026-09-19  
**读者**：新开对话的 Agent（不是给用户的作业）  
**当前总状态**：**⏸ 暂停**。用户未说「继续」之前：**不要派子代理、不要 merge 下一波、不要 push。**

本文件与 `D:\actto\front\project\collab-cli\collab-cli\working-memory\HANDOVER.md` 同构。git 事实以本仓为准。

---

## 30 秒定位

你在压 **COLLABORATION 规范能否指导真实项目**，不是在做换电产品。代码是手段。

| 仓 | 绝对路径 | 角色 | 当前 |
| :--- | :--- | :--- | :--- |
| **collab-cli** | `D:\actto\front\project\collab-cli\collab-cli` | Cursor 工作区时常在此；CLI/MCP/validate；编排指针 | 分支 `collab-new` |
| **evolutionary** | `D:\actto\front\project\evolutionary_start\evolutionary` | 换电实现 + 项目 WM/规格 | `topic/fe-ddd-rsc` · HEAD **`303dc35`** · 与 origin 同步、工作区干净 |
| **collaboration** | `D:\actto\front\project\collaboration_aggregate\collaboration` | 长期 KB | 不要写项目日记；L3 harvest 须用户确认 |

用户语言：**简体中文**。本地 commit 可以；**AI 不 push**。「继续」≠ merge 到 `version/v0`、≠ push。

---

## 现在停在哪（evo-collab-extreme）

**任务**：InMemory → JPA 落库（无人值守双 worktree 集群）。  
**loop 真相**：`working-memory/tasks/evo-collab-extreme/loop.md`

| 项 | 值 |
| :--- | :--- |
| 状态 | ⏸ 暂停（用户 2026-09-18 19:17：跑完本轮后暂停、**不要再派集群**） |
| 已合入 | wave31–41（commerce/credit/mall/settlement 清零；iot 前半 DeviceShadow+Telemetry） |
| HEAD | `303dc35` `refactor(domain): 将独立的枚举类合并到对应的领域类内部`（用户 heiniao 提交并已与 origin 同步） |
| 上一 merge | `374d5fd` wave41 45b TelemetryStore |
| wave42 | **只建了 worktree，未派出 Task** |

wave42 已备、恢复时才用：

- `D:\actto\front\project\evolutionary_start\evo-wt-46a-be` · `wave42/46a-alert-store-jpa` · 基线 `374d5fd`（比当前 HEAD 旧，恢复时建议从 `303dc35` 重建或 rebase）
- `D:\actto\front\project\evolutionary_start\evo-wt-46b-be` · `wave42/46b-maintenance-ticket-jpa` · 同上

**恢复口令**：用户明确说「继续」。然后：

1. 刷新 46a/46b 到 `303dc35`（旧 worktree 不含 enum 内嵌，硬 merge 会痛）
2. 双路 Task：AlertStore ∥ MaintenanceTicket（`IotConfig` 会冲突，父收口）
3. iot 清零后再派 operator：Organization / PackageTemplate / PackageOverride / OnboardingApplication（AuditLog 已 JPA）

**父进程只做**：merge、冲突、验绿、WM。失败只接管一路。子代理 **composer-2.5-fast**、短 brief、**单消息双 Task**。本地 commit、不 push。

历史 worktree（35a–45b）大量残留。不要擅自 `worktree remove`。恢复前可问用户是否清旧树。

---

## 硬规则（违反会被用户纠正）

1. **不 push**；不 `--force`；不跳过 hook。  
2. **暂停期间 idle 允许 >0**，禁止 auto-dispatch。  
3. **WM 双写**：改 git 之后立刻写本仓 `loop.md` **和** collab-cli `working-memory/AGENTS.md` 活跃表 + 指针 loop。先 git 再登记 HEAD，勿空登记。  
4. **collaboration 不写轮次日记**；拦截先记 `working-memory/interceptions-candidates.md`。  
5. **domain 零 Spring/JPA**（`DomainFrameworkFreeTest`）。application 层用户已同意可适度放宽（如将来 `@Transactional`），但不要把 Bean Validation / `@Entity` 推进 domain。  
6. 中文 commit / 注释 / WM。回答从 H2 开始，不客套。  
7. 子代理完成通知 = **同轮 merge + 验绿**；暂停中 **到此为止**，不要续派。

集群坍缩根因：follow-up 只汇报不 merge；用户插问后忘续派；WM 不刷新。**插问不暂停 pipeline——除非用户说暂停。**

---

## Collaboration：怎么用（不要全量读）

KB 入口：`collaboration/AGENTS.md` **症状表**（不要先读 catalog 当路由）。

| 场景 | 做 |
| :--- | :--- |
| 设计墙（不变量、依赖、边界、错误码） | 症状表 1 行 → 读 1–2 条 → 决策 |
| 机械 JPA 切片 | **不读 KB**；对标上一切片 + 目标测绿 |
| 跨对话进度 | 读本文件 + `working-memory/`，不读 KB 全文 |
| 入库 / 新条目 | **人触发**；说不出「不看它称职模型会做错」就别建 |

D 实验：有规格的实现任务，读库改变 SUMMARY、不改变代码结构。拦截账本「差点」多为自述。项目特异决定留在本仓 `decisions.md`，不要当通用 pattern 往 KB 灌。

---

## 本会话已拍板、未全部落代码的架构

| 题 | 结论 | 代码状态 |
| :--- | :--- | :--- |
| AOP | domain 绝对纯净；application 可白名单；事务宜放 interfaces/infrastructure | 几乎无 `@Transactional`；`PerformSwap` 注释「同事务」与实现不一致 |
| 事件 | Fact record + Controller 同步调用；不必上总线 | 保持 |
| 命令模式 / CQRS | `execute()` 即写侧；读写分离先拆端口 | 保持 |
| enum | 单聚合 → `Aggregate.Status` | **已 commit** `303dc35`。`BatteryStatus`、`OrgCapability` **故意不内嵌** |
| Config 种子 | 程序化种子，不换 data.sql | 有意 |
| `OrgCapability` | BC 级共享词汇，不要塞进 Organization | 保持独立文件 |

---

## 代码地图

包按 BC：`swap/` `station/` `battery/` `commerce/` `credit/` `mall/` `settlement/` `iot/` `operator/` `admin/`，每模块 `domain / application / infrastructure / interfaces`。

JPA 对标最近 `Jpa*Repository`：`rehydrate` + Entity + Spring Data + `@Component` 适配；Config 去 InMemory bean；InMemory **文件保留**。测试 `@SpringBootTest`，PowerShell 须 `"-Dtest=A,B"`。双路改同一 `*Config` → 父收口，禁止残留冲突标记。

---

## 恢复后的默认下一刀

1. iot：AlertStore ∥ MaintenanceTicket → iot JPA 清零  
2. operator 四仓储（Organization 种子必须在 **Bean 构造时**写入，供 `OrgAuthorization`）  
3. 可选：`PerformSwap` 真事务；application 测试放宽 Spring、仍禁 JPA  

不要在 JPA 集群里夹带大改前端（RSC 目标仍标待实施），除非用户点名。

---

## 不要做

- 项目日记进 `collaboration/meta/evolution-log`  
- 为「有产出」新建 KB 条目  
- 未说「继续」就派 wave42  
- 以本仓 `AGENTS.md`「刚建立 / battery-pressure 待实现」为准——**以代码 + HANDOVER + loop.md 为准**  
- 发明第三套 commit 规则：本任务惯例是 **L2 本地 commit、不 push**

---

## 读序

1. 本文件  
2. `tasks/evo-collab-extreme/loop.md`  
3. collab-cli `working-memory/AGENTS.md`（若在该工作区）  
4. 设计决策：collaboration 症状表 → 1–2 条  
5. 跑代码：对标最近 `Jpa*Repository`
