# 第 13 轮：读用例进 application（层对称）

**日期**：2026-09-17 ｜ **性质**：选择性压测  
**前置**：第 11 轮 `list()` 在 Controller 直接调 `StationRepository` —— 与第 5 轮 `PerformSwap` **不对称**。

## 任务

1. 新增 `ListStations` · `GetStation` 应用用例  
2. `SwapController` **不再**注入 `StationRepository`  
3. 测试保持绿

## 检索路径

- 第 5 轮已用 `layer-vs-context`：PerformSwap 是同上下文外层  
- 症状表「要设计新结构」→ `design-decision`（读路径是否也要用例）

## design-decision：读路径要不要用例？

| 问 | 答 |
| :--- | :--- |
| 没有它会遇到什么问题？ | Controller 直连仓储 = 写有用例、读无；换读规则要改 HTTP 层 |
| 收益？ | 与 PerformSwap 对称；interfaces 只依赖 application |
| 成本？ | 两个薄类 |

→ **提取读用例**。

## 暴露点

| # | 暴露点 | 验收 |
| :-- | :--- | :--- |
| **A1** | 规格先于 Java | 本文件 |
| **A2** | Controller 零 `StationRepository` import | 代码 |
| **A3** | `ListStations` / `GetStation` 单元或集成测 | 测试 |
| **A4** | 拦截？ | 有则记 |

## 不做

新依赖 · push
