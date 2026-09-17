# phase-7 切片3 报告

**分支**：`phase/p7-iot`  
**更新**：2026-09-17

## 交付

| AC | 实现 |
| :-- | :--- |
| AC-59 | `IdempotentCommandGateway`：24h 同 commandId 只物理下发一次 |
| AC-58 / INV-19 | `AssertShadowFreshForMetered` + `ShadowTelemetryFreshnessAdapter` → `PerformEntitledSwap` 计量前门禁；`DomainErrorCode.TELEMETRY_STALE` |

## 测试

- `CommandIdempotencyAndStaleGuardTest`
- `StaleTelemetryBlocksMeteredSwapTest`
- `mvn test` 全绿

## 未做（可后续）

- AC-60 COMM_LOST 告警 + MaintenanceTicket
- AC-61 诊断路径（运维手册级）
