# BE agent status（S36）

**分支**：`topic/fe-ddd-rsc`  
**日期**：2026-09-17 · evo-collab-extreme 切片8a · IoT COMM_LOST HTTP  
**HEAD**：`08a6445`

## 完成

- InMemory：`DeviceShadowRepository` / `AlertStore` / `MaintenanceTicketRepository`
- `IotConfig`：beans + 种子 `BAT-IOT-1`（lastSeen > STALE_AFTER）
- `POST /iot/batteries/{id}/detect-comm-lost` → stale/raised/alertType/ticketId
- `GET /iot/batteries/{id}/shadow` → 200 / 404
- `IotApiErrorTranslator`（S34 按码，不嗅探 message）
- `CommLostHttpIT`：detect → COMM_LOST；再 detect 同 ticketId
- RUNBOOK：IoT curl 段

## Tests run

```text
mvn -B "-Dtest=CommLostHttpIT" test
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
```

## 阻塞

- 无

## 备注

- 未改 frontend / commerce entitled-swaps / credit；未 push
