# BE agent status（S36）

**分支**：`topic/fe-ddd-rsc`  
**日期**：2026-09-17 · evo-collab-extreme 切片6 · RepayBillingStatement HTTP  
**HEAD**：`04ddd83`

## 完成

- InMemory：`AccountRepository` / `LedgerRepository` / `CreditLedgerDebtRepository` + Spring bean（CommerceConfig Account/Ledger；CreditConfig Debt）
- `InMemoryEntitlementRepository.findByUserIdAndStatus` 正确过滤 FROZEN/ACTIVE（还款解冻）
- `CreditConfig`：`@Bean RepayBillingStatement`（与同一 Entitlement/Account/Ledger bean）
- `CreditController`：`POST /credit/profiles/{userId}/repay` body `{"statementId","amountCents"}` → 200 statement PAID / Err→`CreditApiErrorTranslator`
- 种子：U1 余额 5000 + ORG `CREDIT-CLEARING` SETTLEMENT 0
- `CreditRepayHttpIT`：mark-overdue → 409 → repay PAID → entitled-swaps **200 COMPLETED**
- RUNBOOK：还款 curl 一行

## Tests run

```text
mvn -B "-Dtest=CreditRepayHttpIT,CreditOverdueHttpIT,CreditControllerTest,EntitledSwapControllerTest" test
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
```

## 阻塞

- 无

## 备注

- 未改 frontend；未 merge `version/v0`；未 push
- `CreditRepayHttpIT` / `CreditOverdueHttpIT` 带 `@DirtiesContext(AFTER_CLASS)`
