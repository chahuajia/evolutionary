# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-18 11:12  
**模式**：extreme **v9**（≤2）  
**波次**：wave23 ✅ · wave24 · `dispatched=2 recovered=0`  
**合入**：27b 钱包读 · 27a apiBase+credit 三层（父接管 commit）  
**idle**：0 / 3  
**分支**：`topic/fe-ddd-rsc`

## MVP

| 端 | 状态 |
| :--- | :--- |
| 总后台批入驻 / 四端壳 | ✅ |
| apiBase + credit 试点 | ✅ |
| 钱包 HTTP | ✅ · FE 待 |
| 商家 B | 占位 → 本波加厚 |
| 运营商批下线 / 撤销 FE | 待 |

## 本波 wave24

| 路 | worktree | 目标 |
| :-- | :--- | :--- |
| 28a FE | `../evo-wt-28a-fe` | `/wallet` + gateway（`GET /commerce/users/{id}/wallet`）+ 消费者 NAV |
| 28b FE | `../evo-wt-28b-fe` | `/merchant` 加厚（入驻说明/平台审批文案；勿动 app-shell 若 28a 已改则只改 merchant/**） |

28a 独占 `app-shell`；28b 禁改 app-shell。

## 停止

idle≥3 或 MVP 全 ✅
