# 第 11 轮：多站概览 + REST 批量读（压 round-10 证伪条件）

**日期**：2026-09-17 ｜ **性质**：选择性压测 collaboration  
**前置**：第 10 轮否决 GraphQL；证伪条件含「>3 异构聚合 + 可测 N+1」。

## 任务

1. 种子 **3 个站**（S1/S2/S3）  
2. 后端 **`GET /stations`** 一次返回概览（id / name / 可换出）  
3. 前端首页加载站列表；**禁止**客户端对每站 `GET /stations/{id}` 循环（N+1）  
4. 列表拉取失败时按 **W1** 给可行动提示（非空白页）

## design-decision：批量端点 vs 前端 N+1？

| 问 | 答 |
| :--- | :--- |
| 没有批量端点？ | 3 站概览 = 3 次 HTTP；延迟与契约噪音；**可测 N+1** |
| 收益？ | 单次读、UI 一次渲染；**仍不需要 GraphQL** |
| 成本？ | 多一个 list 方法与 summary DTO |

→ **加 `GET /stations`**；前端只调一次。

## dependency-decision：GraphQL 仍否？

→ **仍否**（与第 10 轮一致）；本场景用 REST 批量即可关闭「N+1 证伪」的一半条件。

## 暴露点（先声明）

| # | 暴露点 | 验收 |
| :-- | :--- | :--- |
| **L1** | 症状表 → `design-decision` 书面裁决 batch vs N+1 | 本文件 + 报告 |
| **L2** | `GET /stations` 返回 ≥3 站 summary | MockMvc |
| **L3** | 前端 **1 次** fetch 列表（代码可观察） | `page.tsx` |
| **L4** | 列表失败 → W1 式提示（非空错误区） | UI 文案 |
| **L5** | 症状表/catalog 能否路由「API N+1 / 列表读法」？ | 检索路径 → 找不到则记 **known-gaps** |
| **L6** | 真实拦截？ | 有则记；无则 0 |

## 不做

- GraphQL · Playwright · commit/push

## 交付

`specs/round-11.md` · `round-11-report.md` · 后端 list + 前端列表
