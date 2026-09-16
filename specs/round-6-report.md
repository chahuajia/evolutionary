# 第 6 轮暴露报告

**日期**：2026-09-16 ｜ **任务**：Spring Boot REST 边界 ｜ **结果**：`mvn -B test` → **34 / 0**
**性质**：真实压测；依赖三问写在 `round-6.md`，**先于** `pom.xml` 引入 Spring。

## 检索路径

1. 症状表「不确定要不要引入依赖」→ `dependency-decision`（三问已在规格）  
2. 症状表「领域层能不能碰框架」→ `domain-purity-is-structural`  
3. 按需：`parse-dont-validate`（Controller 把 JSON id 解析成 `Battery`）

## 暴露点验收

| # | 结果 | 证据 |
| :-- | :--- | :--- |
| **R1** | ✅ | `specs/round-6.md` 含完整三问；随后才改 `pom.xml` parent 为 Spring Boot 3.2.5 |
| **R2** | ✅ | `DomainFrameworkFreeTest` 扫 `domain/` + `application/`：0 处 `import org.springframework` |
| **R3** | ✅ | 见上；Spring 仅在 `interfaces/` · `infrastructure/` · boot 入口 |
| **R4** | ✅ | 规格先于 Controller / Config |
| **R5** | ✅ **1 条拦截** | `domain-purity-is-structural` → 用架构测试钉死，而不是靠自觉 |

## 三问结论（可证伪）

- **引入 Spring**：为了 HTTP 边界与压测「框架会不会脏领域」——有具体问题。  
- **位置**：interfaces / infrastructure；application 仍是纯 Java `PerformSwap`。  
- **pom**：现含 `spring-boot-starter-web`；领域测无需起容器仍绿。

## API（已实现）

`POST /stations/{stationId}/swaps` + `{ "incomingBatteryId" }`  
→ 200 / 409（无可用）/ 404（未知站）

## 诚实记录

- 归还电池在边界用 `Battery.create(id).swapOut()` 伪造 IN_USE —— **演示接线**，不是完整「电池从用户会话来」模型。  
- 站点仓储仍是内存；无 JPA。  
- 第 5 轮「不引 Spring」与本轮「引入」不矛盾：问题变了（第 5 无 HTTP 需求）。

## 对 collaboration

| 动作 | |
| :--- | :--- |
| interceptions +1 | `domain-purity-is-structural`（架构测试） |
| 不改 agreements | 遵守 |
