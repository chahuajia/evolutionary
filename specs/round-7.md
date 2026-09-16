# 第 7 轮：JPA 持久化（依赖三问先行 + 领域零 JPA）

**日期**：2026-09-16 ｜ **性质**：真实压测  
**前置**：第 6 轮 REST + 内存仓库；架构测试只禁 `org.springframework`。

## dependency-decision：要不要 JPA / H2？

| 问 | 答 |
| :--- | :--- |
| **没有它，具体问题？** | 进程一重启站点种子丢失；无法压测「持久化适配会不会把 `@Entity` 涂进领域」 |
| **收益？** | 换电结果可跨请求存活（同 JVM 内 H2）；第一次检验 **JPA 边界** |
| **成本？** | 依赖面、映射样板、测试变慢 |

→ **引入** `spring-boot-starter-data-jpa` + H2（test/runtime 内存）。

### 位置三问

| 问 | 答 |
| :--- | :--- |
| 哪一层？ | **infrastructure** 放 `@Entity` / `JpaRepository`；**禁止** domain 出现 `jakarta.persistence` |
| 泄漏？ | 扩展 `DomainFrameworkFreeTest`：禁 `jakarta.persistence` / `org.hibernate` |
| 替换？ | 换仓储实现不改 `PerformSwap` / `Station` |

## 暴露点（先声明）

| # | 暴露点 | 验收 |
| :-- | :--- | :--- |
| **J1** | 三问先于 pom 出现 jpa | 本文件 |
| **J2** | domain/application 零 JPA import | 架构测试扩展后仍绿 |
| **J3** | REST 换电后新请求仍能读到站状态（同库） | 集成测试 |
| **J4** | 真实拦截？ | 有则记 interceptions |

## 设计约束

- `Station` / `Battery` **保持纯 Java**；持久化用 `StationJpaEntity` + 映射  
- 电池集合序列化：本轮可用 JSON 列或简化表结构（诚实记录取舍）

## 不做

- 前端 · 多租户 · commit/push · 加 agreement

## 交付

`specs/round-7.md` · `round-7-report.md` · JPA 适配 + 测试
