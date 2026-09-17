# 本地联调 RUNBOOK

**更新**：2026-09-17 · 分支 `topic/fe-ddd-rsc`

## 正式种子（FE RSC stations + credit 共用）

| 端点 | 种子 | 来源 |
| :--- | :--- | :--- |
| `GET /stations` | S1 东门站 / S2 西门站 / S3 南站 | `DevSeedConfig` |
| `GET /credit/profiles/U1` | U1 limit=10000 used=3000（分）| `CreditConfig` |

## 已接通（正式可跑）

| 场景 | FE | BE |
| :--- | :--- | :--- |
| 站列表 / 详情 / 换电 | `/` → `/api/stations…` | `SwapController` :8080 |
| 信用档案 + 账单 | `/credit` → `/api/credit/profiles/U1…` | `CreditController` + CreditConfig U1 |

## 启动

```bash
# 终端 1
cd backend && mvn spring-boot:run

# 终端 2
cd frontend && npm run dev
```

- UI：http://localhost:3000  
- 信用：http://localhost:3000/credit  
- 直连 API：http://localhost:8080/credit/profiles/U1  

可选：`BACKEND_ORIGIN=http://localhost:8080`（Next rewrite 默认已是此值）。

## 尚未接通（非正式完整场景）

- 信用购 / 还款 HTTP、逾期冻权益再换电
- 权益换电 `PerformEntitledSwap`（现网仍是站级 `PerformSwap`）
- IoT 遥测入影 / 影子查询 / COMM_LOST 的 HTTP 与 UI
- 登录与多用户

## 正式验收（两枪）

- 站列表：`curl -s http://localhost:8080/stations` → JSON 数组长度 ≥1（含 S1/S2/S3）
- 信用档案：`curl -s http://localhost:8080/credit/profiles/U1` → HTTP 200（userId=U1）

契约测试：`FormalLiveContractTest`（MockMvc）。

## 验收速查

```bash
curl -s http://localhost:8080/stations | head
curl -s http://localhost:8080/credit/profiles/U1
curl -s http://localhost:8080/credit/profiles/U1/statements
```
