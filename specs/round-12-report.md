# 第 12 轮暴露报告

**日期**：2026-09-17 ｜ **任务**：swap 边界 Parse ｜ **结果**：`mvn -B test` **38 / 0**  
**性质**：选择性压测 `parse-dont-validate`。

## 检索路径

症状表「不变量放哪」→ `S13` / `parse-dont-validate`：形状校验在边界，业务在领域。

## 暴露点验收

| # | 结果 | 证据 |
| :-- | :--- | :--- |
| **P1** | ✅ | `round-12.md` 先于 `IncomingSwapRequest.java` |
| **P2** | ✅ | Controller 委托 `IncomingSwapRequest.parse(...)` |
| **P3** | ✅ | `{}` body → 400 `incomingBatteryId required` |
| **P4** | ✅ **0 条拦截** | 提取 Parser 为规格驱动重构，非「差点做错」 |

## 对 collaboration

| 动作 | |
| :--- | :--- |
| interceptions | +0 |
| evolution-log | v4.7.11 |
