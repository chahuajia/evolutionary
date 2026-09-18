# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-18 13:00  
**模式**：extreme **v9**（≤2）  
**波次**：wave26 ✅ · wave27 · `dispatched=2 recovered=0`  
**合入**：30a 撤销覆盖 FE · 30b ONBOARDING_APPROVE 审计  
**idle**：0 / 3  

## MVP

| 项 | 状态 |
| :--- | :--- |
| 四端 / 总后台 / 钱包 / 商家 / 批下线 / 换电日志 | ✅ |
| FE 撤销覆盖 | ✅ |
| 入驻 AuditLog | ✅ |
| UsageEvent → JPA | 🔄 本波 |
| IoT 命令审计 | 待 |

## 本波 wave27

| 路 | worktree | 目标 |
| :-- | :--- | :--- |
| 31a BE | `../evo-wt-31a-be` | `UsageEvent` InMemory → JPA 表 |
| 31b BE | `../evo-wt-31b-be` | IoT 命令下发审计仓（append-only） |

路径不冲突：commerce vs iot。

## 停止

idle≥3 或轨迹 P0/P1 清完
