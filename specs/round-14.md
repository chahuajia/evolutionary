# 第 14 轮：S34 边界错误翻译（去字符串判 HTTP）

**日期**：2026-09-17 ｜ **性质**：选择性压测  
**前置**：`SwapController` 用 `msg.startsWith("unknown station")` 判 404 —— **重新解析领域消息**，S34 反面。

## 任务

1. 仓储「未知站」→ 类型化 `UnknownStationException`（消息仍来自一处）  
2. `SwapApiErrorTranslator`：异常类型 → HTTP + 透传 `message` + 可选 `suggestion`  
3. 删除 Controller 内字符串前缀分支  
4. 测试绿

## 检索路径

- 症状表无专条；按需读 **S34**（边界只翻译，不重复校验、不重组消息）  
- 关联 W1：HTTP 4xx 仍由 Network/测试验收

## 暴露点

| # | 暴露点 | 验收 |
| :-- | :--- | :--- |
| **T1** | 规格先于 Translator | 本文件 |
| **T2** | 404 由 `UnknownStationException` 类型决定，非 startsWith | 代码 + 测 |
| **T3** | 409 响应含领域 message + suggestion | MockMvc jsonPath |
| **T4** | 拦截？ | 有则记 S34 |

## 不做

新 agreement · push
