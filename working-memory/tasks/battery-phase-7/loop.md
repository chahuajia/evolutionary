# 换电 phase-7 实现（L2）— 多厂商 IoT

**更新**：2026-09-17 14:45 ｜ **压力层**：**L2**｜ **状态**：🔄 启动 · BE∥FE
**分支**：`phase/p7-iot` ← 合回 `version/v0` 后删除
**语言**：中文（`profiles/heiniao.yaml`）
**调度**：800ms one-shot（`AGENT_LOOP_WAKE_battery-phase-7`）

## 门禁

1. 只在本 phase / `wip/p7-*` commit；不 push
2. 读 `phase-7-spec.md` + `phase-7-acceptance.md`；IDL 仅对照
3. BE：`com.evolutionary.iot`（适配器 + DeviceShadow）；领域不吃 raw
4. FE：可并行，消费 IDL/mock，不阻塞 Spring 控制器

## 句柄

`AGENT_LOOP_WAKE_battery-phase-7`

## 切片

| 步 | 范围 | 谁 | 状态 |
| :-- | :--- | :--- | :--- |
| 1 | Adapter 端口 + VendorA/B parse → 统一事件（AC-55） | BE | ✅ |
| 1b | 信用账单 UI 壳（IDL mock，对齐 phase-6） | FE | 🔄 集群 |
| 2 | DeviceShadow 只读更新 · stale（AC-56/57） | BE | 待 |
| 3 | 命令幂等 + stale 拒计量（AC-58+） | BE | 待 |

## 停止

切片全绿 → 停 wake → 等人确认合入 `version/v0`
