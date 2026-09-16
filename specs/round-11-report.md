# 第 11 轮暴露报告

**日期**：2026-09-17 ｜ **任务**：多站概览 + REST 批量读 ｜ **结果**：`mvn -B test` **37 / 0**；`npm run build` 绿  
**性质**：选择性压测 collaboration（压 round-10 GraphQL 证伪条件的一半）。

## 检索路径

1. 症状表「要设计新结构」→ `design-decision`（batch vs 前端 N+1）  
2. 症状表「要不要引依赖」→ `dependency-decision`（GraphQL **仍否**）  
3. 列表失败 → `W1-blank-page-triage`（Network 层提示）  
4. 查「REST 列表 / API N+1 怎么读」→ **症状表无专条**；`catalog` 关键词仅命中 `domains/graphql` 索引（GraphQL 向），**无 REST 批量读判据** → 记 **known-gaps**

## 暴露点验收

| # | 结果 | 证据 |
| :-- | :--- | :--- |
| **L1** | ✅ | `round-11.md` 书面裁决：加 `GET /stations`，禁止 UI N+1 |
| **L2** | ✅ | MockMvc `listStations`：3 站，S3 `canSwapOut=false` |
| **L3** | ✅ | `loadStationList` 仅 `fetch(\`${API_BASE}/stations\`)` 一次 |
| **L4** | ✅ | `triageFetchError`：Failed to fetch → W1 式提示 |
| **L5** | ✅ **缺口** | 见上；追加 known-gaps 一行 |
| **L6** | ✅ **0 条拦截** | 规格先行否决 N+1；不编 |

## 三问结论

- **批量 REST 端点**：3 站概览若前端循环 GET = 可测 N+1；单次 list 足够，**仍不需要 GraphQL**。  
- round-10 证伪条件「>3 异构聚合」：本轮 3 站**同构** summary，**未完全触发** GraphQL 重开；N+1 半条已用 REST 批量关闭。

## 对 collaboration

| 动作 | |
| :--- | :--- |
| known-gaps | +1：REST 列表/N+1 读法无 symptom 路由 |
| interceptions | +0（仍为 4） |
| evolution-log | v4.7.10 |
