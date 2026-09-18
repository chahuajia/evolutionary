# evo-collab-extreme（双轴极端）

**更新**：2026-09-18 10:30  
**主轴**：L2 · extreme **v9**（≤2）  
**波次**：wave21 ✅ · `dispatched=2 recovered=0`  
**合入**：25a `df2781d` 撤销覆盖 · 25b `a0c1a07` 角色三分端壳  
**idle**：0 / 3  
**分支**：`topic/fe-ddd-rsc`

## 上波交付

- `POST /operator/overrides/{id}/revoke` → REVOKED，有效价回落
- AppShell：消费者 / 店主 / 运营商侧栏裁剪（`actto.console.role`）

## 下一波候选

FE 接通撤销覆盖按钮；或下一未 HTTP 化的 AC

## 停止

双轴连续 3 tick 无增量 → 停
