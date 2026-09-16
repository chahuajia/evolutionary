# 第 8 轮：Next.js 前端（依赖三问先行）

**日期**：2026-09-16 ｜ **性质**：真实压测  
**前置**：第 4–7 轮已收口 commit（`944c67f`）；REST `POST /stations/{id}/swaps` 可用。

## dependency-decision：要不要 Next.js？

| 问 | 答 |
| :--- | :--- |
| **没有它，具体问题？** | 项目已定栈含前端；仅有后端无法压测「前端状态 ≠ 领域」局部约定，也无法从浏览器打换电 API |
| **收益？** | 可观察的 UI→REST 路径；第一次检验「界面状态 vs 聚合根」边界 |
| **成本？** | Node 工具链、CORS、双进程开发 |

→ **引入** Next.js（App Router）+ TypeScript（与 `AGENTS.md` 技术栈一致）。  
**不**为「以后可能用」加状态管理库 / UI kit / GraphQL。

### 位置三问

| 问 | 答 |
| :--- | :--- |
| 哪一层？ | **独立前端应用**（`frontend/`）；不进后端 `domain/` |
| 泄漏？ | 前端**不**复制 `Station`/`Battery` 聚合根模型；只用 DTO / 视图状态 |
| 替换？ | 换前端框架只改 `frontend/`；后端契约保持 REST |

## 暴露点（先声明）

| # | 暴露点 | 验收 |
| :-- | :--- | :--- |
| **F1** | 三问先于 `package.json` / `create-next-app` | 本文件落盘时间序 |
| **F2** | 前端无「聚合根 / 仓储」移植 | 代码审查：无 `Repository`、无领域状态机移植 |
| **F3** | 浏览器可发起换电并展示结果 | UI → `POST /stations/{id}/swaps` |
| **F4** | CORS / 契约缺口是否暴露 | 有则诚实记入报告（必要时最小后端补丁：CORS、GET、种子） |
| **F5** | 真实拦截？ | 有则记 interceptions；无则写 0 |

## 本轮明确不做

- 不引入 Redux/Zustand/React Query（除非三问推翻）  
- 不做登录、多租户、SSR 数据层过度设计  
- 不 push；不改 agreements

## 交付

`specs/round-8.md` · `round-8-report.md` · `frontend/` 最小可运行页
