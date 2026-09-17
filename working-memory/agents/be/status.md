# BE agent status（S36）

**分支**：`topic/fe-ddd-rsc`  
**日期**：2026-09-17 · evo-collab-extreme 切片9a · 信用购 HTTP  
**HEAD**：（本提交后见 git log）

## 完成

- `InMemoryOrderRepository` + `@Bean OrderRepository`（CreditConfig）
- `PurchaseWithCredit` Spring bean
- `POST /credit/purchases`：body `{userId,productId}` → 200（orderId/entitlementId/debtId/…）；额度不足 → 409 `CREDIT_LIMIT_EXCEEDED`
- 种子：`P-CREDIT-1` FIXED 3000¢（非计量）写入 ProductRepository；U1 good 可用 7000¢
- `CreditPurchaseHttpIT`：200 + 409
- RUNBOOK：信用购 curl 段
- 未改 `EntitledSwapController` / 计量换电种子

## Tests run

```text
mvn -B "-Dtest=CreditPurchaseHttpIT,CreditControllerTest,PurchaseWithCreditTest,CreditRepayHttpIT" test
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
```

## 阻塞

- 无

## 备注

- 未改 frontend；未 merge `version/v0`；未 push
- FE 9b 已对接同路径 `/credit/purchases`
