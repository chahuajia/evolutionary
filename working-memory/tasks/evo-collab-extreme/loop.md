# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-19 20:35  
**模式**：主树父写（小切片）· **禁止**为 <5min 切片建 worktree/派 Task  
**状态**：**▶ 运行中**（常设规则：父自选推进）  
**波次**：wave63–66 ✅ · **wave67–69 ✅**  
**HEAD**：`254ac6d`  
**idle**：— · **lanes**：0  
**测**：前端 **98/98** · 未 push

## 刚落地

| 波 | 内容 | HEAD | 测 |
| :-- | :--- | :--- | :--- |
| 66 | Campaign 领券门 | `ed56382` | 83/83 |
| 67–68 | 入驻 SUBMITTED 可批 · 商家 ACTIVE 可交易 | `371d739` | 93/93 |
| 69 | 工单仅 OPEN 需处理（RSC+面板） | `254ac6d` | **98/98** |

**选片依据**：67←`OnboardingApplication.approve`；68←`MerchantProfile.isActive`；69←`MaintenanceTicket.isOpen`（真有 GET，非种子假接线）。

**假设 5'**：本轮 ≥2 个 `feat` → 推进真阳性。

## 下一刀

| 候选 | 前提 |
| :--- | :--- |
| BatteryStatus 换电门 | 需 swap 面板消费点 |
| DeviceShadow 新鲜度 | 核对 stale 守卫 |
| GET SKU/Campaign | 让 65/66 库存与预算真接线 |

## 常设规则

> **父的默认动作是「推进」，不是「等指示」。** 小切片主树父写。
