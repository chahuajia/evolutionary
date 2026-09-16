# 第 10 轮暴露报告

**日期**：2026-09-17 ｜ **任务**：GraphQL 三问裁决 ｜ **结果**：**不引入** GraphQL  
**性质**：真实压测；Playwright E2E **已取消**（用户指令）。

## 检索路径

1. 症状表「不确定要不要引入依赖」→ `dependency-decision`  
2. 项目 `AGENTS.md` 技术栈：GraphQL 待定 → 本轮给**明确结论**  
3. 未读 catalog trigger；未新增 agreement

## 暴露点验收

| # | 结果 | 证据 |
| :-- | :--- | :--- |
| **G1** | ✅ | 本规格先于任何 graphql 依赖 / pom 改动 |
| **G2** | ✅ | 书面「不引入」+ 证伪条件见 `round-10.md` |
| **G3** | ✅ | 三问用于**否决**：无具体问题 / 收益≈0 / 成本明确 |
| **G4** | ✅ **0 条拦截** | 裁决型轮次，无「差点做错」；不编 |

## 三问结论（可证伪）

- **不引入 GraphQL** —— REST + rewrite 已满足当前 UI。  
- **重开条件**：多聚合 N+1 或外部强制 GraphQL（见规格）。

## 取消说明

- 原方案 Playwright 浏览器 E2E：Chrome 下载 + 双进程超时，用户 **取消**。  
- 不记为暴露失败 —— 属成本裁断，非规范失效。

## 对 collaboration

| 动作 | |
| :--- | :--- |
| interceptions | **+0**（仍为 4） |
| evolution-log | v4.7.9 |
| 不改 agreements | 遵守 |
