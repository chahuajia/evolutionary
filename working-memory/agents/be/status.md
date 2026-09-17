# BE agent status（S36）

**分支**：`topic/fe-ddd-rsc`  
**日期**：2026-09-18 · evo-collab-extreme 切片11a · IoT 遥测入影 HTTP  
**HEAD**：`cee4189`（feat `6f188bb`/`7ffac90`）

## 完成

- `InMemoryTelemetryStore` + `@Bean TelemetryStore` / `ApplyTelemetryToShadow`（IotConfig）
- `POST /iot/batteries/{batteryId}/telemetry`：body `{vendorId,soc,voltageMilli}` → 200 影子摘要；未知电池 → 404
- `reportedAt` 由服务端 `Instant.now()` 填充
- `TelemetryHttpIT`：入影后 GET shadow soc=75 stale=false + 404
- RUNBOOK：遥测入影 curl 段（已在 HEAD）
- 未改 frontend / mall / credit

## Tests run

```text
mvn -B "-Dtest=TelemetryHttpIT,CommLostHttpIT" test
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

## 阻塞

- 无

## 备注

- 未改 frontend；未 merge `version/v0`；未 push
