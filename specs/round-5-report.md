# 第 5 轮暴露报告

**日期**：2026-09-16 ｜ **任务**：薄应用层 `PerformSwap` ｜ **结果**：`mvn -B test` → **30 / 0**
**性质**：真实压测（非对照）

## 检索路径

1. 项目 `AGENTS.md` → KB  
2. KB 症状表：
   - 「要设计一个新结构或抽象」→ `design-decision`
   - 「不确定要不要引入依赖 / 放哪」→ `dependency-decision`
3. 按需读：`layer-vs-context`（层 vs 上下文判据）  
4. **未**用 catalog `trigger` 做本轮路由

## 暴露点验收

| # | 结果 | 证据 |
| :-- | :--- | :--- |
| **Q1** | ✅ | 症状表 → `design-decision`；用例形态（取站→换电→存站）过三问 |
| **Q2** | ✅ | **不引 Spring**（见下三问）；`pom.xml` 仍只有 JUnit |
| **Q3** | ✅ | Station/Battery/SwapSession **共享统一语言** → application 是同上下文外层，不是新上下文；端口 `StationRepository` 放 application，domain 零 IO |
| **Q4** | ✅ | `specs/round-5.md` 先于 `PerformSwap*.java` |
| **Q5** | ✅ **裁决：不建状态机专条** | 见下节（可证伪） |
| **Q6** | ✅ **1 条拦截** | `dependency-decision` 拦住「应用层一出现就上 Spring」 |

## dependency-decision：本轮要不要 Spring？

| 问 | 答 |
| :--- | :--- |
| 没有它会遇到什么具体问题？ | **没有。** 用例只需内存仓库接口；没有 HTTP、没有 DI 容器需求 |
| 收益？ | 自动配置、以后 REST 方便 —— **都是未来假设，本轮零收益** |
| 成本？ | 领域被间接拖进框架风险、测试变重、攻击面/升级面 |

→ **不加。** `pom` 可证伪。

## design-decision：为何要有 `PerformSwap` 而不是只调 `Station.swap`？

| 问 | 答 |
| :--- | :--- |
| 没有它 | 调用方自己 get/save，编排散落，易漏 `save` |
| 收益 | 换电的「取→改→存」一处完成；领域保持纯 |
| 成本 | 多一个类型；本轮仓库是内存假实现 |

## Q5 裁决：领域状态机专条 —— **不建**

**裁决**：不在 collaboration 新增「领域状态机怎么建模」专条。症状表「状态机」行保持「无专条 → known-gaps」改为**已关闭：明确不建**。

**可证伪证据**（满足 known-gaps 关闭条件「明确永久不建、只复用既有实体状态机，并有一次独立使用」）：

| 轮次 | 事实 |
| :--- | :--- |
| 第 1 轮 | `Battery` 状态机无专条指导，11 测全绿；E1 记「内容层无条目」 |
| D 六跑 | M5 全同：模型自己把不变量放聚合根 |
| 第 4 轮 | 复用 `Battery` 流转，未发明站级状态机；按表记缺口 |
| **第 5 轮** | 应用层仍零新状态机；独立使用「复用实体状态机」策略 |

**为何不建（pruning / A4）**：说不出「不看它，称职模型会做错」—— 多轮证据是模型**已经会**。建条 = 给废话修路由的镜像错误（给已会的东西占索引）。

**若证伪**：未来出现「模型把领域状态机建成贫血服务 / 框架注解状态」且专条能拦住 → 再开缺口，那时才写条。

## 本轮对 collaboration 的动作

| 动作 | 状态 |
| :--- | :--- |
| 关闭 known-gaps「工程症状」整行（路由已关 + 状态机**明确不建**） | 本轮执行 |
| interceptions +1：`dependency-decision` | 本轮执行 |
| 不改 agreements；不填 trigger；不开 A/B | 遵守 |

## 产物

- `swap/application/PerformSwap.java`
- `swap/application/StationRepository.java`
- `PerformSwapTest`（3）
