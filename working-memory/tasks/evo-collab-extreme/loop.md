# evo-collab-extreme（双轴极端）

**更新**：2026-09-17 22:15  
**主轴**：**L2** 切片7 计量权益换电 HTTP  
**期望 HEAD**：`evolutionary`  
**idle**：0 / 3  
**分支**：`topic/fe-ddd-rsc`  
**门禁**：extreme **v4** 加厚 FE∥BE

## 句柄

`AGENT_LOOP_WAKE_evo-collab-extreme`

## 切片

| 步 | 轴 | 范围 | 状态 |
| :-- | :--- | :--- | :--- |
| 0–6 | — | 至还款解冻 | ✅ |
| 7 | L2 FE∥BE | 计量换电 POST + soc → METERED_CHARGE | 🔄 集群中 |

## 验收

1. `POST /entitled-swaps` 带 `socBefore`/`socAfter` + E-M1 → 200 COMPLETED + chargedAmount  
2. 余额不足 → 422 `INSUFFICIENT_BALANCE`  
3. 既有非计量 E-1 路径仍绿  
4. FE：计量换电岛（或扩展面板）  

## 停止

双轴连续 3 tick 无增量 → 停
