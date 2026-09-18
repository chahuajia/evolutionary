# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-18 11:42  
**模式**：extreme **v9**（≤2）  
**波次**：wave25 ✅  
**合入**：29a `8993fba` 批下线 · 29b `ea6f95e` FE tab  
**idle**：0 / 3  

## MVP

| 项 | 状态 |
| :--- | :--- |
| 四端壳 / 总后台批商家 | ✅ |
| apiBase + credit 三层 | ✅ |
| 钱包 HTTP + FE | ✅ |
| 商家入驻进度 | ✅ |
| 运营商批下线 | ✅ |
| FE 撤销套餐覆盖 | 待 W26 |
| 换电日志落库 | 计划待确认（非 DB 侧已有） |

## 下一波

W26：FE 接通 `POST /operator/overrides/{id}/revoke`（BE 已有）

## 停止

idle≥3 或 MVP 余项清完
