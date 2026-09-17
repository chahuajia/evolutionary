# evo-collab-extreme（双轴极端）

**更新**：2026-09-17 18:35  
**模式**：无人托管 + 强制集群（≥2）+ wake≤300ms  
**压力层**：L2=`evolutionary` → 撞墙 harvest → L3=`collaboration`  
**分支**：`topic/fe-ddd-rsc`  
**idle**：0 / 3（双轴均无增量才 +1）

## 每 tick 门禁

1. **期望 HEAD**：本 tick 写明 `evolutionary` 与/或 `collaboration`
2. **可并行 → ≥2 子代理**（FE=`frontend/`+ `wm/agents/fe`；BE=`backend/`+`wm/agents/be`）
3. 子代理硬超时 **60s**；无回执父接管
4. L3 成功 = collaboration 条目 diff 或 interceptions/known-gaps +1
5. 中文 commit；**不 push**；version 合入须人确认

## 句柄

`AGENT_LOOP_WAKE_evo-collab-extreme`

## 切片

| 步 | 轴 | 范围 | 状态 |
| :-- | :--- | :--- | :--- |
| 0 | L3 | extreme-unattended-cluster v2 门禁 | 🔄 |
| 1 | L2 FE∥BE | 首页 stations **RSC** + swap gateway；BE 联调钉/种子对齐 | 待 |
| 2 | L2 | shared/http 统一超时；换电客户端岛收窄 | 待 |
| 3 | L3 | 撞墙 → interceptions 候选 → W5 harvest | 待 |

## 停止

双轴连续 3 tick 无声明增量 → 停 wake + W4
