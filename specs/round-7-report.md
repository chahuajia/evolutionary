# 第 7 轮暴露报告

**日期**：2026-09-16 ｜ **任务**：JPA + H2（领域零 JPA）｜ **结果**：`mvn -B test` → **35 / 0**
**性质**：真实压测；依赖三问写在 `round-7.md`，**先于** `pom.xml` 增加 jpa/h2。

## 检索路径

1. 症状表「不确定要不要引入依赖」→ `dependency-decision`（三问已在规格）  
2. 症状表「领域层能不能碰框架」→ `domain-purity-is-structural`  
3. 未把 JPA 涂进 domain

## 暴露点验收

| # | 结果 | 证据 |
| :-- | :--- | :--- |
| **J1** | ✅ | `specs/round-7.md` 含完整三问；随后才改 pom |
| **J2** | ✅ | `DomainFrameworkFreeTest` 扩展禁 `jakarta.persistence` / `org.hibernate`；domain/application 0 违规 |
| **J3** | ✅ | `persistsAcrossReads`：POST 换电后再 `stationPort.get` 读到归还电池 `CHARGING` |
| **J4** | ✅ **1 条拦截** | 差点把 `@Entity` 直接标在 `Station` 上 → `StationJpaEntity` 映射 + 架构测试钉死 |

## 三问结论（可证伪）

- **引入 JPA/H2**：为了跨请求存活与压测「持久化会不会脏领域」——有具体问题。  
- **位置**：infrastructure 的 `StationJpaEntity` / `JpaStationRepository`；`PerformSwap` / `Station` 不变。  
- **pom**：现含 `spring-boot-starter-data-jpa` + H2；领域测仍无需起容器。

## 设计取舍（诚实）

- 电池列表落在 **JSON 列**，不是正规 3NF 表 —— 本轮只压「适配边界」，不压数据模型完备性。  
- 内存仓储已删除；Spring 装配只有 `JpaStationRepository`。

## 对 collaboration

| 动作 | |
| :--- | :--- |
| interceptions +1 | `domain-purity-is-structural`（JPA `@Entity` 场景；与第 6 轮 Spring import 同条、不同错误形态）→ 账本 **4** |
| 不改 agreements | 遵守 |
