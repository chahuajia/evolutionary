# 第 9 轮：同域 BFF 代理（rewrite）vs CORS

**日期**：2026-09-17 ｜ **性质**：真实压测  
**前置**：第 8 轮 Next.js 直连 `localhost:8080` + 后端 `CorsConfig`；第 8 轮改动仍未 commit。

## 任务

浏览器换电请求改为**同域**（经 Next.js rewrite 代理到 Spring），压测：跨域补丁是不是默认最优解。

## dependency-decision：要不要保留「浏览器直连 + CORS」？

| 问 | 答 |
| :--- | :--- |
| **没有 CORS 直连，具体问题？** | 浏览器同源策略挡跨端口；第 8 轮因此加了 `CorsConfig` |
| **改用 Next rewrite 的收益？** | UI 只打同源 `/api/*`；少暴露 CORS 面；前端环境变量可默认空 |
| **成本？** | 开发需双进程；rewrite 只覆盖本 UI，其它客户端仍可能要 CORS |

→ **本轮 UI 改走 rewrite（BFF 薄代理）**。  
`CorsConfig` **保留**（不删）：其它工具 / 将来原生客户端仍可能直连；删它没有具体收益。

### 位置三问

| 问 | 答 |
| :--- | :--- |
| 哪一层？ | rewrite 在 `frontend/next.config`；业务仍在 Spring |
| 泄漏？ | 不把领域逻辑放进 Next Route Handler（本轮只用 rewrite，不加自定义 BFF 业务） |
| 替换？ | 换掉 Next 时改回直连 + CORS 即可 |

## 暴露点（先声明）

| # | 暴露点 | 验收 |
| :-- | :--- | :--- |
| **B1** | 三问先于改 `next.config` / `page.tsx` API_BASE | 本文件 |
| **B2** | UI 默认同源 `/api`，不再依赖浏览器 CORS | 代码可观察 |
| **B3** | **未**把换电业务搬进 Next Route Handler | 无 `app/api/**/route.ts` 业务实现（或仅空代理则用 rewrite） |
| **B4** | 进程级验收：后端活着时 `curl` 换电成功 | 命令输出入报告 |
| **B5** | 真实拦截？ | 有则记；无则 0 |

## 不做

- 不加 GraphQL / React Query  
- 不删 CorsConfig（无收益）  
- 不 commit / 不 push（除非用户另说）

## 交付

`specs/round-9.md` · `round-9-report.md` · rewrite + UI 改动
