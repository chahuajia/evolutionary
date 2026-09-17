# evo-collab-extreme（双轴极端）

**更新**：2026-09-17 23:55  
**主轴**：L2 · extreme **v7 收口≠停派**  
**期望 HEAD**：切片10 mall∥11 IoT 遥测（四路）  
**idle**：0 / 3  
**分支**：`topic/fe-ddd-rsc`

## 句柄

`AGENT_LOOP_WAKE_evo-collab-extreme`

## v7 本 tick

- 违规：上波 8–9 ✅ 后父单会话「收口/答问」未派下一波 → 补 v7 门禁并立刻全派  
- 并行：`mall/**` ∥ `iot/**`（路径写锁）  
- 父：60s 查 transcript 工具调用；无则接管  

## 切片

| 步 | 轴 | 范围 | 状态 |
| :-- | :--- | :--- | :--- |
| 0–9 | — | 至信用购/IoT COMM_LOST | ✅ |
| 10a | L2 商城 BE | ClaimCouponFromCampaign HTTP | 🔄 |
| 10b | L2 商城 FE | 领券客户端岛 | 🔄 |
| 11a | L2 IoT BE | ApplyTelemetryToShadow HTTP | 🔄 |
| 11b | L2 IoT FE | 遥测入影岛 | 🔄 |

## 停止

双轴连续 3 tick 无增量 → 停
