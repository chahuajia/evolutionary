# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-19 20:44  
**模式**：主树父写（小切片）· **禁止**为 <5min 切片建 worktree/派 Task  
**状态**：**▶ 运行中**（常设规则：父自选推进）  
**波次**：wave67–69 ✅ · **wave70–71 ✅**  
**HEAD**：`dc18798`  
**idle**：— · **lanes**：0  
**测**：前端 **110/110** · 未 push

## 刚落地

| 波 | 内容 | HEAD | 测 |
| :-- | :--- | :--- | :--- |
| 69 | 工单仅 OPEN 需处理 | `254ac6d` | 98/98 |
| 70 | 影子新鲜度：stale 禁计量换电 | `399486f` | |
| 71 | 电池状态机：仅 AVAILABLE 可换出（站详情接线） | `dc18798` | **110/110** |

**选片依据**：70←`AssertShadowFreshForMetered` / DetectCommLost；71←`Battery.swapOut`（站 GET 真返回 status）。

**假设 5'**：本轮 2 个 `feat` → 推进真阳性。

## 下一刀

| 候选 | 前提 |
| :--- | :--- |
| GET SKU/Campaign | 让 65/66 库存与预算真接线（需后端加 GET） |
| commerce Order / Entitlement 门 | 先核守卫厚度 |
| ShadowStatus enum 展示 | DeviceShadow 还有 IDLE 等 |

## 常设规则

> **父的默认动作是「推进」，不是「等指示」。** 小切片主树父写。
