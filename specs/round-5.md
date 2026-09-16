# 第 5 轮：应用层薄用例 + 状态机缺口裁决

**日期**：2026-09-16 ｜ **性质**：真实压测（非对照）
**前置**：第 4 轮领域层已绿；`known-gaps` 路由半截已关，**状态机专条内容半截仍开**

## 任务（业务）

1. 在 `swap/application/`（或等价包）增加**薄用例**：给定站 + 归还电池 → 调用领域换电 → 返回新站 + 会话  
2. **仍不引入 Spring**（除非本轮 dependency-decision 三问推翻）  
3. `mvn -B test` 全绿；不 commit / 不 push  

## 暴露点（先声明，跑完不许改）

| # | 暴露点 | 怎么验收 |
| :-- | :--- | :--- |
| **Q1** | 「要设计新结构」是否命中症状表 → `design-decision` | 报告写出检索路径 |
| **Q2** | 「要不要引依赖」是否命中 → `dependency-decision`（本轮焦点：Spring） | 三问书面回答；结论可观测（pom 有/无 spring） |
| **Q3** | `layer-vs-context`：application 与 domain 是同上下文的层，还是新上下文？ | 报告用「共享统一语言吗」判据写结论 |
| **Q4** | A10：本规格先于应用层代码 | 时间序 / 路径 |
| **Q5** | **状态机专条**：建 / 不建？ | 必须给可证伪裁决 + 关闭或改写 `known-gaps` 对应行；**禁止**「再观察一下」 |
| **Q6** | 真实拦截？ | 有则记 interceptions；无则写 0 |

## 本轮明确不做

- 不开 A/B；不引 REST/JPA；不批量填 trigger；不加 agreement

## 交付

| 文件 | 内容 |
| :--- | :--- |
| `specs/round-5.md` | 本文件 |
| `specs/round-5-report.md` | 验收 + 状态机裁决 |
| application 用例 + 测试 | 工作区 |
