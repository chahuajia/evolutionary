# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-19 02:45
**模式**：extreme **v9+反坍缩 v3**
**状态**：**▶ 已恢复**（用户 02:xx 说「继续」）；wave42 双路已收回
**波次**：wave41 ✅ · **wave42 ✅** · wave43 待派
**HEAD**：`2a10645`（wave42 双 merge 已合入）
**idle**：2 / 3 · **lanes**：0（子代理均已收回）
**交接**：`../../HANDOVER.md`（完整版亦在 collab-cli `working-memory/HANDOVER.md`）

## wave42（双路集群 · 已收口）

| 路 | worktree | 分支 | 结果 |
| :-- | :--- | :--- | :--- |
| 46a | `evo-wt-46a-be` | `wave42/46a-alert-store-jpa` | ✅ `f260fe5` AlertStore → 表 `battery_alerts` |
| 46b | `evo-wt-46b-be` | `wave42/46b-maintenance-ticket-jpa` | ✅ `a8c5c01` MaintenanceTicket → 表 `maintenance_tickets` |

- 两路 worktree 派工前**重新基线到 `3ceda84`**（旧基线 `374d5fd` 不含 enum 内嵌）
- **`IotConfig` 冲突如预告发生**，父收口：两行 import 都删（两个 Bean 都已 JPA 化），
  两处注释都留，`DetectCommLost` 签名未动。**无 `<<<<<<<` 残留**
- 合并后全量：**217 tests / 0 failures / BUILD SUCCESS**
  （211 基线 + 3 + 3 —— 算术对得上，两路的测试都在合并树上跑了）
- **iot 域 JPA 清零**（5 个 InMemory 文件保留，全部不再注入）

## 前序切片

**接手（主进程单干）**：operator 域 4 仓储 InMemory → JPA ✅ `56861f1`
- 报告：`working-memory/tasks/operator-jpa/slice-1-report.md`
- **实测 KB**：设计墙（种子时机/初始化顺序）→ 症状表**无行**；catalog 命中
  `self-bootstrapping-requires-fixed-core`（**假阳性**）。记 KB `known-gaps`（`ef4effd`），**不开条目**（项目决定）。

**修基线（派集群的前置）**：`3ceda84` 每个 Spring context 独立 H2。
修前全量 205/2 红；根因是「种子按 context 跑一次 + H2 全 JVM 共用」的顺序依赖。
**不清掉它，wave42 的新红无法归因** —— 这是先修后派的理由。

**下一刀（wave43）**：**全仓只剩 1 处 InMemory 注入** —— 而且它跨域：

```
operator/interfaces/OperatorConfig.java:89
    return new InMemoryMerchantProfileRepository();   // ← mall 域的仓储
```

`InMemory*` **文件**还剩 32 个，但那是**有意保留**的（本仓惯例：迁移后文件留着、
只是不再注入）。**判断"还有多少活要干"要看"仍被 new 的数"，不是文件数** ——
我第一版这里就是按文件数写的，全错。

派工前先确认各域仓储是否与别的域**共享 Config 文件** —— 那是 wave42 冲突的唯一来源。

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
