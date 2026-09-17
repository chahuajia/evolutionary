# FE agent status（extreme pressure · slice 8b）

**分支**：`topic/fe-ddd-rsc`  
**日期**：2026-09-17  
**心跳**：slice 8b IoT COMM_LOST 诊断客户端岛  
**HEAD**：`0e67c02`

## 完成

### Slice 8b（IoT FE · 加厚）

- `domains/iot/infrastructure/iot-gateway.ts`：`postDetectCommLost` → `POST /iot/batteries/{id}/detect-comm-lost`；默认 `BAT-IOT-1`；对齐 BE 扁平 DTO `{ batteryId, stale, raised, alertType, ticketId }`；错误经 `fetchJson` suggestion
- `app/iot/`：路由 `/iot` + `CommLostPanel` 客户端岛（展示 stale / COMM_LOST / ticketId）
- 首页链到 `/iot`
- 初版 `86c2bf2`；DTO 对齐 `0e67c02`
- `npx tsc --noEmit` 通过
- 未改 backend；未 push

### 前序切片摘要

- Slice 7：计量权益换电岛
- Slice 6：信用还款解冻岛
- Slice 4–5：权益换电岛 + `fetchJson` suggestion
- Slice 1–2：RSC 站列表 + gateway

## 阻塞

- 无（BE 8a 已交付扁平 DetectCommLostView）。

## 调用示例

```http
POST /iot/batteries/BAT-IOT-1/detect-comm-lost
```
