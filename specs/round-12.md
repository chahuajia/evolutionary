# 第 12 轮：边界 Parse（swap 请求）

**日期**：2026-09-17 ｜ **性质**：选择性压测  
**前置**：第 11 轮已 commit `6fb2537`。

## 任务

换电 `POST` 的 JSON → 领域 `Battery` **在 interfaces 解析**，不散落 if/validate。

## 检索路径

- 症状表「不变量放哪」旁路 → `parse-dont-validate` + `S13`（形状在边界，业务在领域）  
- `PerformSwap` 只收已类型化的 `Battery`

## 暴露点

| # | 暴露点 | 验收 |
| :-- | :--- | :--- |
| **P1** | 规格先于 Parser 类 | 本文件 |
| **P2** | Controller 不内联 blank 校验；委托 `IncomingSwapRequest.parse` | 代码 |
| **P3** | 缺/空 `incomingBatteryId` → 400 | MockMvc |
| **P4** | 拦截？ | 有则记 |

## 不做

GraphQL · commit/push 由本轮末 commit 本地 only
