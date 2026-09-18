# operator 域 JPA 化（切片 1）

**日期**：2026-09-19 ｜ **分支**：`topic/fe-ddd-rsc` ｜ **基线**：`380db15`

## 做了什么

operator 域 4 个仓储 InMemory → JPA（AuditLog 此前已完成）：

| 仓储 | 表 |
| :--- | :--- |
| `OrganizationRepository` | `organizations` + `organization_capabilities` + `organization_regions` |
| `PackageTemplateRepository` | `package_templates` + `package_template_override_fields` |
| `PackageOverrideRepository` | `package_overrides` + `package_override_fields` |
| `OnboardingApplicationRepository` | `onboarding_applications` |

`mvn -o test -Dtest=JpaOperatorRepositoryTest` → **6/6 绿**。

## 结果：operator 域 JPA 清零

`InMemory*` 文件全部保留（不注入），与既有惯例一致。

## 过程中的两个决定（都不是机械照搬）

### 1. `JpaOrganizationRepository` 刻意**不加** `@Component`

本仓其他 JPA 适配都是 `@Component`，这个是唯一例外：

组织种子**必须在 bean 构造时写入**（`OrgAuthorization` 是另一个 bean，
在**构造时**一次性读全表建祖先链索引）。所以它由 `OperatorConfig` 的 `@Bean`
方法构造并播种。

**踩过的坑**：第一次同时留了 `@Component` 和 `@Bean` → 容器里两个
`OrganizationRepository` → `AdminConfig` 注入时
`Parameter 1 of method approveMerchantOnboarding required a single bean, but 2 were found`，
应用起不来。**删掉 `@Component`** 才对。

### 2. InMemory → JPA 把"内存可见性"换成了"事务可见性"

种子约束没变，但**失效方式变了**：InMemory 里 `save` 之后 `findAll` 立刻可见；
JPA 里若不 flush，同一事务内的 `findAll` **看不到**。
所以 `JpaOrganizationRepository.save` 用 `saveAndFlush`。
这条值得记：**换持久化实现时，"顺序约束"往往还在，但它的失败模式换了名字。**

## 测试策略的一个例外（有意）

`JpaOperatorRepositoryTest` **不写** `@BeforeEach deleteAllInBatch` ——
与本仓其他 JPA 测试不同。原因：组织种子只在 bean 构造时播种一次，
清表会让 `OrgAuthorization` 的索引与实际库**永久不一致**（种子不会再跑）。
所以用独立测试 ID，不断言"表里只有我这一条"。

## 预存在的失败（**不是本次引入**）

全量 `mvn -o test`：**211 tests, 2 failures**。

```
FormalLiveContractTest.creditProfileU1Ok:42  Status expected:<200> but was:<404>
CreditControllerTest.profileAndStatementsForU1:23  Status expected:<200> but was:<404>
```

**验证方式**：`git stash -u` 后在干净树上跑全量 → **205 tests, 2 failures，同一批**。
本次改动只是 +6 绿测试（205→211），**零新增失败**。

疑似根因（未修，留给下一刀）：`JpaCreditProfileRepositoryTest` 会
`deleteAllInBatch()` 清 `credit_profiles`，而 credit 种子是
`ApplicationRunner`（每个 Spring context **只跑一次**）。全量跑时测试类共享
缓存 context，清表后种子不会重播 → 后续需要 U1 的测试 404。
**单跑不复现**（context 不同），只在全量顺序下出现。

> 这与 operator 的组织种子是**同一类问题**：**"种子"与"读种子的人"谁先谁后**。
> operator 侧我已经用 bean 构造时播种解决了；credit 侧还是 ApplicationRunner。
> 修法：给 credit 的种子加 `@DirtiesContext` 语义，或把清表改成"只删自己造的"。

## 未做

- credit 侧的种子/清表顺序问题（上面那条）
- `PerformSwap.execute` 的真事务（HANDOVER 列的债）
- 前端 `topic/fe-ddd-rsc` 的 RSC 改造（HANDOVER 明确说不要在 JPA 簇里夹带）
