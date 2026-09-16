# 第 13 轮暴露报告

**日期**：2026-09-17 ｜ **任务**：读用例进 application ｜ **结果**：`mvn -B test` **39 / 0**  
**性质**：选择性压测 `layer-vs-context` / `design-decision` 对称性。

## 检索路径

1. 第 5 轮：`PerformSwap` 已证明写路径在同上下文 application  
2. 第 11 轮：`list()` 曾 **Controller → Repository**（不对称）  
3. 症状表「要设计新结构」→ `design-decision` 三问 → 补 `ListStations` / `GetStation`

## 暴露点验收

| # | 结果 | 证据 |
| :-- | :--- | :--- |
| **A1** | ✅ | `round-13.md` 先于用例类 |
| **A2** | ✅ | `SwapController` 无 `StationRepository` |
| **A3** | ✅ | `ListStationsTest` + 原 MockMvc 全绿 |
| **A4** | ✅ **0 条拦截** | 规格主动对齐对称性；不编 |

## 结论

读写路径均经 application；interfaces 只依赖用例 + DTO 映射。第 11 轮的「读捷径」被本轮回正。

## 对 collaboration

| 动作 | |
| :--- | :--- |
| interceptions | +0 |
| evolution-log | v4.7.12 |
