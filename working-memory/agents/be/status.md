# BE agent status（S36）

**分支**：`topic/fe-ddd-rsc`  
**日期**：2026-09-17 · evo-collab-extreme 切片7 · 计量权益换电 HTTP  
**HEAD**：（本提交后见 git log）

## 完成

- `InMemoryProductRepository` + `@Bean ProductRepository`
- `PerformEntitledSwap` bean 改 7 参（products/accounts/ledger）
- `POST /entitled-swaps`：可选 `socBefore`/`socAfter`（都有→计量；都无→非计量；只给一个→400）
- 响应可含 `chargedAmountCents`（可 null）
- `EntitledSwapApiErrorTranslator` 补 `INSUFFICIENT_BALANCE` suggestion
- 种子：P-M1 / E-M1 PAY_AS_YOU_GO / BAT-M1 + ORG-1 SETTLEMENT（保留 E-1/BAT-1）
- `MeteredEntitledSwapHttpIT`：200 charged=1000；422 INSUFFICIENT_BALANCE
- RUNBOOK：计量 curl 一行

## Tests run

```text
mvn -B "-Dtest=MeteredEntitledSwapHttpIT,EntitledSwapControllerTest,CreditRepayHttpIT" test
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
```

## 阻塞

- 无

## 备注

- 未改 frontend；未 merge `version/v0`；未 push
- U1 余额仍由 CreditConfig `ACC-U1-BAL` 5000；计量结算户 `ACC-ORG1-SETTLE`
