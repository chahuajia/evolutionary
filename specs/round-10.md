# 第 10 轮：GraphQL 三问裁决（不默认引入）

**日期**：2026-09-17 ｜ **性质**：真实压测（**规格 + 书面裁决**，默认不引 GraphQL）  
**前置**：第 8–9 轮 REST + Next 已绿；`AGENTS.md` 写「GraphQL 待定」。  
**取消项**：Playwright E2E（用户取消 —— 成本过高，非本轮暴露点）。

## dependency-decision：要不要 GraphQL？

| 问 | 答 |
| :--- | :--- |
| **没有它，具体问题？** | **没有。** 换电只有 1 个写操作 + 1 个读站点；REST 已覆盖；前端经 rewrite 同域调用 |
| **收益？** | 多资源一次查询 —— 当前 UI 只展示单站 + 单次换电，**收益≈0** |
| **成本？** | Spring GraphQL 依赖、schema 维护、与 hexagonal 边界新一层、测试面扩大 |

→ **不引入 GraphQL。** 继续 REST；若未来出现「多站聚合屏 / N+1 读」再重跑三问。

### 位置三问（若将来引入）

| 问 | 答 |
| :--- | :--- |
| 哪一层？ | `interfaces/` 新 adapter，**不进** domain/application |
| 泄漏？ | 禁止 domain import graphql 注解 |
| 替换？ | 与 REST 并列 adapter，共享 `PerformSwap` |

## 暴露点（先声明）

| # | 暴露点 | 验收 |
| :-- | :--- | :--- |
| **G1** | 三问先于任何 graphql 依赖 | 本文件 |
| **G2** | 书面「不引入」可证伪（触发条件写清） | 报告 |
| **G3** | 症状表 `dependency-decision` 被用于**否决**依赖 | 检索路径 |
| **G4** | 真实拦截？ | 有则记；无则 0 |

## 证伪条件（何时重开）

- UI 需一次拉取 **>3** 个异构聚合且 REST 导致可测 N+1  
- 或外部客户端明确要求 GraphQL 且 REST 无法满足

## 不做

- 不加 `spring-graphql` · 不改 pom · 不 commit/push（除非用户另说）

## 交付

`specs/round-10.md` · `round-10-report.md`
