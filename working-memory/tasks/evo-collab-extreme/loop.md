# evo-collab-extreme（双轴极端 · 无人值守完成）

**更新**：2026-09-18 11:00  
**模式**：extreme **v9**（≤2）  
**波次**：wave22 ✅ · wave23 · `dispatched=2 recovered=0`  
**合入**：26a `b266e4a` 总后台入驻 · 26b `459b03f` 四端壳  
**idle**：0 / 3  
**分支**：`topic/fe-ddd-rsc`

## 完成判据（MVP）

| 端 | 最小能力 | 状态 |
| :--- | :--- | :--- |
| 消费者 | 换电·信用·钱包·商城C | 钱包待 |
| 商家 | 入驻进度占位 | ✅ 占位 |
| 运营商 | 套餐/设备/分润（无商家审批） | ✅ |
| 总后台 | 平台批商家入驻 | ✅ |

## Backlog

| # | 切片 | 状态 |
| :--- | :--- | :--- |
| W22 | 总后台+四端 | ✅ |
| W23 | 27a `apiBase`去重+credit 三层试点 · 27b 钱包读 HTTP | 🔄 |
| W24 | 商家 B 加厚 | 待 |
| W25 | 运营商批下线 | 待 |
| W26 | FE 撤销覆盖 | 待 |

## 本波 wave23

| 路 | worktree | 目标 |
| :-- | :--- | :--- |
| 27a FE | `../evo-wt-27a-fe` | `shared/http/api-base` + 全 gateway；credit `domain`+`application` 薄试点 |
| 27b BE | `../evo-wt-27b-be` | `GET` 用户余额/积分读模型 |

## 停止

idle≥3 或 MVP 全 ✅
