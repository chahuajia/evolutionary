# FE agent status（extreme pressure · slice 4）

**分支**：`topic/fe-ddd-rsc`  
**日期**：2026-09-17  
**心跳**：slice 4 entitled-swap island landed

## 完成

### Slice 1
- 首页去掉全页 `"use client"` + `useEffect` 拉站列表
- 新增 `domains/swap/infrastructure/station-gateway.ts`（`resolveApiBase`、`fetchStationSummaries`、`cache:'no-store'`、DTO parse、W1 错误文案）
- 可选 `shared/http/fetch-json.ts`（timeout 助手）
- `app/page.tsx` → async RSC 首屏拉列表并传入 `initialStationId`
- 客户端岛 `app/swap-panel.tsx`：列表选择 / 详情 GET / 换电 POST；列表刷新 `router.refresh()`
- 样式沿用 `page.module.css`；链到 `/credit`

### Slice 2
- `credit-gateway.ts` / `station-gateway.ts` 统一走 `fetchJson`（`timeoutMs: 8000`、`cache:'no-store'`）；保留 W1/业务错误文案与 DTO parse
- `swap-panel.tsx` 客户端 GET/POST 改用 `fetchJson`；列表仍 `router.refresh()`；无新全局 store

### Slice 4（权益换电 · 与 BE HTTP 并行）
- `domains/commerce/infrastructure/entitled-swap-gateway.ts`：`postEntitledSwap({userId, entitlementId, cabinetId})` → `POST /entitled-swaps`（`fetchJson` 8s；`resolveApiBase` 同站 gateway）
- 客户端岛 `app/entitled-swap-panel.tsx`：默认 U1 / E-1 / CAB-1；按钮「权益换电」；展示结果/错误；自包含
- `app/page.tsx` 挂载 `EntitledSwapPanel`（RSC 无传参）；站列表 RSC 路径未改
- BE 未就绪时 404 可接受（压测）

## 阻塞

- 无。未改 backend / collaboration。未 push。

## 备注

- 列表 UI 仍在岛内渲染，但数据由 RSC 注入（无挂载时客户端 list fetch）
- `tsc`/build 未在本切片强制跑通时以类型自检为准
