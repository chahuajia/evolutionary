# BE agent status（S36）

**分支**：`topic/fe-ddd-rsc`  
**日期**：2026-09-17 · evo-collab-extreme 切片4 · POST /entitled-swaps  
**HEAD**：`90b4e89`

## 完成

- `commerce/infrastructure`：InMemory Entitlement / BatteryAsset / UsageEvent（非计量最小面）
- `CommerceConfig`：仓储 + `PerformEntitledSwap(Clock.systemUTC())`；种子 E-1 ACTIVE（U1）+ BAT-1 idle
- `EntitledSwapController`：`POST /entitled-swaps`；边界 parse→400；`DomainOutcome`→200 / 409|422（S34，`EntitledSwapApiErrorTranslator`）
- `EntitledSwapControllerTest`：200 / 400 / 422 绿；`FormalLiveContractTest` 仍绿
- RUNBOOK：权益换电 curl（U1 / E-1 / CAB-1）
- 未改 frontend / collaboration（FE 岛由并行 agent 负责）

## 阻塞

- 无

## 备注

- 种子用户为 **U1**（对齐信用 FormalLive）；单元测 `PerformEntitledSwapTest` 仍用 U-1，互不影响
