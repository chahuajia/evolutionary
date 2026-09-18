# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-18 15:50  
**模式**：extreme **v9+反坍缩**（默认 ≥2；父禁吞两路）  
**波次**：wave30 ✅（34a `audit_logs`）· wave31 🔄 `dispatched=2 recovered=0`  
**HEAD**：见 `git log -1`  
**idle**：0 / 3  

## 吞吐反坍缩（本任务强制）

1. **有可并行缺口 → 必须 ≥2 Task**；父只做 merge/会计/拒收，**禁止**「沉默后独自写完两路」。
2. **沉默 ≤45s → 同 brief 重派该路**（最多 1 次），仍沉默才父接管**这一路**；另一路继续跑。
3. **切片加厚**：每路一次交付 feat+目标测；禁半截 stub。
4. **默认并行面**：commerce InMemory→JPA 按聚合拆（Entitlement∥Account；Product∥Ledger…）。

## 本波 wave31

| 路 | worktree | 目标 |
| :-- | :--- | :--- |
| 35a BE | `../evo-wt-35a-be` | `Entitlement` → JPA 表 `entitlements` |
| 35b BE | `../evo-wt-35b-be` | `Account` → JPA 表 `accounts` |

路径：`commerce/entitlement*` vs `commerce/account*` + 各改 `CommerceConfig` 时 **只删自己的 @Bean**，勿改对方。

## L3（协作仓 · 未 commit）

`multi-portal-capability-gate` + known-gaps 关闭 + AGENTS 行 + `_index` —— 待用户确认后 commit。
