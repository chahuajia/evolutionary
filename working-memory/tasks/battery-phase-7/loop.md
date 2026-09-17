# 换电 phase-7 实现（L2）— 多厂商 IoT

**更新**：2026-09-17 14:55 ｜ **压力层**：**L2**｜ **状态**：✅ 切片1–3 绿 · **停 wake** · 待合入
**分支**：`phase/p7-iot` ← 合回 `version/v0` 后删除
**语言**：中文（`profiles/heiniao.yaml`）
**调度**：已停（`AGENT_LOOP_WAKE_battery-phase-7`）

## 门禁

1. 只在本 phase / `wip/p7-*` commit；不 push
2. 读 `phase-7-spec.md` + `phase-7-acceptance.md`；IDL 仅对照
3. BE：`com.evolutionary.iot`；领域不吃 raw
4. FE：消费 IDL/mock，不阻塞 Spring

## 句柄

`AGENT_LOOP_WAKE_battery-phase-7` — **已停**

## 切片

| 步 | 范围 | 谁 | 状态 |
| :-- | :--- | :--- | :--- |
| 1 | Adapter 端口 + VendorA/B parse（AC-55） | BE | ✅ |
| 1b | 信用账单 UI 壳 `/credit` mock | FE | ✅ |
| 2 | DeviceShadow 只读更新 · stale（AC-56/57） | BE | ✅ |
| 3 | 命令幂等 + stale 拒计量（AC-58/59） | BE | ✅ |

## 停止

切片全绿 → **停 wake** → 等人确认合入 `version/v0`  
AC-60/61（COMM_LOST / 诊断）未纳入本轮切片，合入后可 `topic/iot-*` 补。
