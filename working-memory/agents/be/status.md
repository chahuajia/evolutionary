# BE agent status（S36）

**分支**：`topic/fe-ddd-rsc`  
**日期**：2026-09-18 · evo-collab-extreme 切片10a · 商城领券 HTTP  
**HEAD**：`f207055`

## 完成

- `InMemoryCampaignRepository` / `InMemoryCouponTemplateRepository` / `InMemoryUserCouponRepository`
- `MallConfig`：beans + 种子 CAMP-OK / CAMP-EMPTY + T-C1（预算 5000¢ / 0）
- `POST /mall/campaigns/{campaignId}/claims`：body `{userId,templateId}` → 200（id/userId/templateId/status）；预算耗尽 → 409 `CAMPAIGN_BUDGET_EXHAUSTED`
- `MallApiErrorTranslator`：S34 按码映射，不嗅探 message
- `ClaimCouponHttpIT`：200 + 409
- RUNBOOK：商城领券 curl 段
- 未改 frontend / iot / credit / commerce entitled-swaps

## Tests run

```text
mvn -B "-Dtest=ClaimCouponHttpIT,ClaimCouponFromCampaignTest" test
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

## 阻塞

- 无

## 备注

- 未改 frontend；未 merge `version/v0`；未 push
- FE 10b 可对接同路径 `/mall/campaigns/{campaignId}/claims`
