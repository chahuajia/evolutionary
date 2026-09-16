# 第 6 轮：REST / Spring（依赖三问先行）

**日期**：2026-09-16 ｜ **性质**：真实压测  
**前置**：第 5 轮薄应用层已绿、明确不引 Spring；本轮**再跑** `dependency-decision`，允许推翻。

## 任务

为换电提供 **HTTP API**（至少 `POST` 换电），技术栈按项目 `AGENTS.md`：Spring Boot + REST。  
领域 / 应用层**继续零 Spring import**；框架只出现在 `interfaces/`（或 `infrastructure/`）。

## dependency-decision（本轮必须先写，再写代码）

### 引入三问：要不要 Spring Boot？

| 问 | 答 |
| :--- | :--- |
| **没有它，具体问题是什么？** | 项目已定栈是 Spring Boot + REST；第 5 轮只有内存用例，**无法被 HTTP 客户端调用**，也压不到「框架边界会不会污染领域」 |
| **收益？** | 得到可调用的 REST 边界；第一次真实检验 `domain-purity-is-structural`（领域 jar 是否仍无 Spring） |
| **成本？** | 依赖体积、测试需 `@SpringBootTest` 或 MockMvc、升级面；首次引入配置噪音 |

→ **引入。** 理由是**本轮暴露点需要边界**，不是「以后可能用」。

### 位置三问

| 问 | 答 |
| :--- | :--- |
| 属于哪一层？ | **interfaces**（适配 HTTP）；DI 组装可在 boot 入口 |
| 会泄漏吗？ | 禁止 `domain/` / `application/` import `org.springframework.*`（可用架构测试钉死） |
| 替换成本？ | 换 Web 框架只改编配器；`PerformSwap` / `Station` 不动 |

## 暴露点（先声明，跑完不许改）

| # | 暴露点 | 验收 |
| :-- | :--- | :--- |
| **R1** | 三问是否**先于** `pom` 出现 spring 依赖 | `round-6.md` 时间序 / 本文件已含三问 |
| **R2** | 领域+应用是否仍零 Spring | 架构测试扫 import，或 `jdeps`/`rg` 可复现 |
| **R3** | `domain-purity-is-structural` 是否被用到 | 报告写检索路径 |
| **R4** | A10：规格先于 Controller | 本文件先于接口代码 |
| **R5** | 真实拦截？ | 有则记 interceptions |

## API 草案（规格级，可在实现微调路径但不可无规格开写）

```
POST /stations/{stationId}/swaps
Body: { "incomingBatteryId": "B-in" }   // 简化：测试里用已构造的 IN_USE 电池；或内联状态
→ 200 { stationId, outgoingId, incomingId }
→ 409 无可用电池
→ 404 未知站
```

> 诚实：完整「电池从哪来」需要仓储；本轮允许 **测试用的内存装配**，REST 只验证边界接线。

## 不做

- JPA / 真实 DB  
- 前端  
- 往 agreements 加条  
- commit / push  

## 交付

`specs/round-6.md` · `specs/round-6-report.md` · Spring 边界代码 + 测试
