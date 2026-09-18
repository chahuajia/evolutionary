# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-19 02:05
**模式**：extreme **v9+反坍缩 v3+暂停**  
**状态**：**⏸ 集群仍暂停**；**主进程已接手做主轴的活**（见下）
**波次**：wave41 ✅ · wave42 ⏸ 仅 worktree  
**HEAD**：`56861f1`（operator JPA 切片1 已 commit）
**idle**：2 / 3 · **lanes**：0  
**交接**：`../../HANDOVER.md`（完整版亦在 collab-cli `working-memory/HANDOVER.md`）

## 接手切片（主进程单干，**未派集群**）

**2026-09-19**：用户指示「接手 evolutionary 来实测 collaboration」。
遵守暂停约定**未派任何子代理**，主进程直接做：

- operator 域 4 仓储 InMemory → JPA ✅ `56861f1`（operator JPA 清零）
- 报告：`working-memory/tasks/operator-jpa/slice-1-report.md`
- **实测 KB 的结果**：设计墙（种子时机/初始化顺序）→ 症状表**无行**；
  catalog 关键词命中 `self-bootstrapping-requires-fixed-core`（**假阳性**）。
  结论记入 KB `meta/known-gaps.md`（`ef4effd`）：**不开条目**，属项目决定。

**下一刀（恢复时）**：operator 已清零 → 按 HANDOVER 顺序应是 iot 的
AlertStore ∥ MaintenanceTicket（**需双路 Task，即需用户说「继续」**）；
或主进程继续单干 credit 的种子顺序问题（预存在 2 红，见切片报告）。

## 反坍缩 v3+暂停

| # | 规则 |
| :-- | :--- |
| 1–7 | 见 v3（merge 同轮续派、插问不挡 pipeline…） |
| **8** | **用户「暂停」→ 标记 ⏸；不 auto-dispatch；idle 允许 >0** |
| **9** | **WM 双写**：evolutionary 真相 + collab-cli `AGENTS.md` 活跃任务表 |

### 本轮单 agent 根因（wave41→42）

1. wave41 merge 后 **只建了** evo-wt-46a/46b worktree，**未双路 Task**（turn 被 Briefly inform / 用户插问截断）
2. 用户明确要求 **暂不续派** → 集群 idle=0 无法维持
3. WM 停在 19:05 wave41🔄，**未随 merge 刷新** → 工作区 tasks 看起来「没更新」

## wave41（iot 前半 ✅）

| 路 | 结果 |
| :-- | :--- |
| 45a | `device_shadows` · `98ca4e2` → merge |
| 45b | `telemetry_records` · `d17ddf4` → merge `374d5fd` |

验绿：`JpaDeviceShadowRepositoryTest` + `JpaTelemetryStoreTest` 2/2。

## wave42（⏸ 未派出）

worktree 已备：`evo-wt-46a-be`（AlertStore）· `evo-wt-46b-be`（MaintenanceTicket）  
**恢复时**：双路 dispatch → merge `IotConfig` → iot 域 JPA 清零

## 本地 refactor

- **enum 内聚**：已合入 `303dc35`（20 个单聚合 `*Status` → `Aggregate.Status`）
- **保留独立**：`BatteryStatus`、`OrgCapability`
- 工具：`backend/tools/nest-single-aggregate-enums.mjs`（一次性脚本，不必再跑）

## 下一域（暂停后）

iot 剩 2 → operator 4
