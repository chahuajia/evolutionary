# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-18 11:18  
**模式**：extreme **v9**（≤2）  
**波次**：wave24 ✅（父接管）· wave25 · `dispatched=2 recovered=0`  
**idle**：0 / 3  

## MVP

| 项 | 状态 |
| :--- | :--- |
| 四端壳 / 总后台批商家 | ✅ |
| apiBase + credit 三层 | ✅ |
| 钱包 HTTP + FE | ✅ |
| 商家入驻进度页 | ✅ |
| 运营商批下线 | 🔄 本波 |
| FE 撤销覆盖 | 待 |

## 本波 wave25

| 路 | worktree | 目标 |
| :-- | :--- | :--- |
| 29a BE | `../evo-wt-29a-be` | 运营商批准下线 OPERATOR 入驻（非 MERCHANT）；与 `/admin` 商家审批分 API |
| 29b FE | `../evo-wt-29b-fe` | 运营商页「批下线」tab；契约钉 29a Notes |

## 停止

idle≥3 或 MVP 全 ✅
