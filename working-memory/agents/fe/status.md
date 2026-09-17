# FE agent status（S36）

**分支**：`topic/credit-http-live`  
**日期**：2026-09-17

## 完成

- `/credit` 从全 CSR（`useEffect` fetch）改为 **async RSC** 首屏拉数
- 新增 `domains/credit/infrastructure/credit-gateway.ts`（自 `lib/credit/api` 迁出；api 薄壳再导出）
- RSC 服务端直连 `BACKEND_ORIGIN`；小客户端岛 `credit-refresh.tsx`（`router.refresh`）
- 读模型仍用 `cache: 'no-store'`（信用档案/账单）

## 阻塞

- 无。未改 backend / collaboration。

## 备注

- `domains/credit/application` / `domain` 分层未完整铺开（类型仍在 `lib/credit/types`）；本轮以 gateway + RSC 纠偏为主。
- 是否需 collaboration 新条：**否**（目标架构已有 WM 草案；RSC 直连约定与既有 `BACKEND_ORIGIN` rewrite 一致）。
