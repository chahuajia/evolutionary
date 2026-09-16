# 第 8 轮暴露报告

**日期**：2026-09-16 ｜ **任务**：Next.js → REST 换电 UI ｜ **结果**：`mvn -B test` **36 / 0**；`npm run build` **成功**
**性质**：真实压测；依赖三问写在 `round-8.md`，**先于** `create-next-app`。

## 检索路径

1. 症状表「不确定要不要引入依赖」→ `dependency-decision`  
2. 项目 `AGENTS.md` 局部约定 #3：前端状态 ≠ 领域  
3. 未引入状态管理库 / UI kit

## 暴露点验收

| # | 结果 | 证据 |
| :-- | :--- | :--- |
| **F1** | ✅ | `specs/round-8.md` 先于 `frontend/package.json` |
| **F2** | ✅ | `page.tsx` 仅有 `UiState` / API DTO；无 Repository、无领域状态机移植 |
| **F3** | ✅ | 表单 `POST /stations/{id}/swaps`；成功后刷新 `GET` |
| **F4** | ✅ **暴露契约缺口** | 浏览器跨域需要 CORS；UI 需要 `GET /stations/{id}` + 启动种子 —— 本轮最小补丁：`CorsConfig` · `get` · `DevSeedConfig` |
| **F5** | ✅ **0 条拦截** | 按规格预先避坑，未出现「差点做错再被条目拦住」；不编 |

## 三问结论（可证伪）

- **引入 Next.js**：为了浏览器可调 API 与压测「界面状态 vs 聚合」——有具体问题。  
- **位置**：`frontend/`；后端契约仍 REST。  
- **未加**：Redux / React Query / Tailwind（三问答不上来）。

## 诚实记录

- Next 从脚手架 15.1.0 升到 **15.5.7**（脚手架版本有已知安全告警）。  
- 前端未做 E2E 浏览器自动化；验收靠 `npm run build` + 后端 MockMvc（含 GET）。  
- 收口提交（第 4–7 轮）已在本轮之前完成：`evolutionary` `944c67f` · `collaboration` `0225e23` · `collab-cli` `ea35e84`。本轮改动未再 commit。

## 对 collaboration

| 动作 | |
| :--- | :--- |
| interceptions | **+0**（仍为 4） |
| evolution-log | v4.7.7 记本轮 |
| 不改 agreements | 遵守 |
