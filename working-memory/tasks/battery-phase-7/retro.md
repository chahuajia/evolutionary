# phase-7 复盘

**日期**：2026-09-17  
**分支**：已合入 `version/v0`（原 `phase/p7-iot` 已删）· merge `b39b72d`  
**补丁**：AC-60/61 经 `topic/iot-comm-lost` 合入（`07bdfab`）

## 交付

| 切片 | AC | 要点 |
| :-- | :--- | :--- |
| 1 | AC-55 | VendorA/B Adapter · 归一 `BatteryTelemetryReported` |
| 1b | FE | `/credit` mock 壳（与 BE 并行） |
| 2 | AC-56/57 | DeviceShadow · stale / TelemetryRecord |
| 3 | AC-58/59 | 命令幂等 · stale → `TELEMETRY_STALE` 拒计量 |
| topic | AC-60/61 | COMM_LOST 工单 · W1 诊断路径 |

## 合入

人确认后 merge → 删 phase/topic。未 push。

## 拦截（本 phase）

| 条目 | 拦住 |
| :-- | :--- |
| parse-dont-validate / INV-18 | raw MQTT/HTTP 不进领域 |
| INV-19 / AC-58 | stale 禁止 P3 |
| domain-purity | commerce 经 `TelemetryFreshnessPort`，不依赖厂商 |
| A16 / dependency-decision | TelemetryStore 端口，不绑时序库 |
| W1 | SOC 过时先查 shadow，再适配器 |
