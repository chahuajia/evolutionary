# BE agent status（S36）

**分支**：`topic/fe-ddd-rsc`  
**日期**：2026-09-17 · evo-collab-extreme 切片5 · MarkCreditOverdue HTTP  
**HEAD**：`984841d`

## 完成

- `CreditConfig`：`@Bean MarkCreditOverdue`（statements/profiles + **同一** `EntitlementRepository` + `Clock.systemUTC()`）
- `CreditController`：`POST /credit/profiles/{userId}/mark-overdue` body `{"statementId"}` → 200 profile / Err→`CreditApiErrorTranslator`（S34 按码）
- `CreditOverdueHttpIT`：U1/STMT-2026-02 → overdue；再 `POST /entitled-swaps` U1/E-1/CAB-1 → **409** `CREDIT_OVERDUE_BLOCKED` + suggestion
- RUNBOOK：逾期冻权益 curl 一行
- 测绿：`mvn -B "-Dtest=CreditOverdueHttpIT,CreditControllerTest,EntitledSwapControllerTest,FormalLiveContractTest" test` → Tests run: 8, Failures: 0

## 阻塞

- 无

## 备注

- 未改 frontend；未 merge `version/v0`；未 push
- `CreditOverdueHttpIT` 带 `@DirtiesContext(AFTER_CLASS)`，避免冻 E-1 污染同上下文换电/档案测
