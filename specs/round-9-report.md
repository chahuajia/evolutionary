# 第 9 轮暴露报告

**日期**：2026-09-17 ｜ **任务**：Next rewrite（同域 `/api`）vs CORS 直连 ｜ **结果**：`npm run build` 绿；活后端 curl 换电 **200** / 再换 **409**
**性质**：真实压测；三问在 `round-9.md`，先于改 `next.config` / `API_BASE`。

## 检索路径

1. 症状表「不确定要不要引入依赖 / 放哪」→ `dependency-decision`  
2. 第 8 轮 F4：CORS 是补丁，未必是终点  
3. **未**新增 Route Handler 业务层

## 暴露点验收

| # | 结果 | 证据 |
| :-- | :--- | :--- |
| **B1** | ✅ | `specs/round-9.md` 先于 config/UI 改动 |
| **B2** | ✅ | `page.tsx` 默认 `API_BASE="/api"`；`next.config.ts` rewrite → `localhost:8080` |
| **B3** | ✅ | `frontend/src` **无** `route.ts`；代理只用 rewrite |
| **B4** | ✅ | 活进程：`GET /stations/S1` → 200；`POST .../swaps` → `B-out`/`B-live-1`；再 POST → **409** `no available battery` |
| **B5** | ✅ **0 条拦截** | 规格已排除「把换电搬进 Next Handler」；未出现差点做错再被条目拦住 |

## 三问结论（可证伪）

- **UI 改走 rewrite**：减少浏览器对 CORS 的依赖；有具体问题（第 8 轮跨域）。  
- **CorsConfig 保留**：删它无收益；直连客户端仍可能需要。  
- **不加自定义 BFF 业务**：换电仍在 Spring；Next 只转发路径。

## 诚实记录

- 第 8+9 轮改动仍未 commit（用户未再要求提交）。  
- 活验收用 PowerShell `Invoke-RestMethod`（curl.exe 在本机对 JSON body 转义失败）。  
- 控制台中文站名乱码属终端编码，JSON 字段本身正常。

## 对 collaboration

| 动作 | |
| :--- | :--- |
| interceptions | **+0**（仍为 4） |
| evolution-log | v4.7.8 |
| 不改 agreements | 遵守 |
