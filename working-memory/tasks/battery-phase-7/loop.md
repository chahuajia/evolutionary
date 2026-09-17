# 换电 phase-7 实现（L2）— 多厂商 IoT

**更新**：2026-09-17 15:00 ｜ **压力层**：**L2**｜ **状态**：✅ 已合入 `version/v0` · **停 wake**
**分支**：`version/v0`（`phase/p7-iot` 已删）· merge `b39b72d`
**语言**：中文（`profiles/heiniao.yaml`）

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

已合入 `version/v0` → 删 phase → 见 `retro.md` · 阶段性复盘见 `../_archive/2026-09-17-selective-pressure-retro.md`
