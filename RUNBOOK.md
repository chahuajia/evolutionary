# 本地联调 RUNBOOK

**更新**：2026-09-17 · 分支 `topic/fe-ddd-rsc`

## 正式种子（FE RSC stations + credit 共用）

| 端点 | 种子 | 来源 |
| :--- | :--- | :--- |
| `GET /stations` | S1 东门站 / S2 西门站 / S3 南站 | `DevSeedConfig` |
| `GET /credit/profiles/U1` | U1 limit=10000 used=3000（分）| `CreditConfig` |
| `POST /credit/purchases` | P-CREDIT-1 FIXED 3000¢（非计量）+ U1 good | `CreditConfig` |
| `POST /credit/profiles/U1/monthly-billing` | OPEN Debt → Statement DUE（需先 purchases） | `RunMonthlyBilling` |
| `POST /entitled-swaps` | E-1 ACTIVE（U1）+ BAT-1 idle；计量 E-M1 / P-M1 / BAT-M1；默认选卡 E-FINITE | `CommerceConfig` |
| `POST /iot/.../detect-comm-lost` | BAT-IOT-1 shadow lastSeen 过期（>5min） | `IotConfig` |
| `POST /mall/campaigns/CAMP-OK/claims` | CAMP-OK 预算 5000¢ + T-C1 | `MallConfig` |
| `POST /mall/orders` | SKU S1（M1 · 1000¢ · stock20）+ ACC-M1-SETTLE | `MallConfig` |
| `POST /mall/orders/checkout-with-coupons` | claim CAMP-OK + T-C1 FIXED_OFF 500（minSpend 3000¢ → qty≥3） | `MallConfig` |

## 已接通（正式可跑）

| 场景 | FE | BE |
| :--- | :--- | :--- |
| 站列表 / 详情 / 换电 | `/` → `/api/stations…` | `SwapController` :8080 |
| 信用档案 + 账单 | `/credit` → `/api/credit/profiles/U1…` | `CreditController` + CreditConfig U1 |
| 信用购 | `/credit` → `/api/credit/purchases` | `CreditController` + PurchaseWithCredit |
| 月度出账 | `/credit` → `/api/credit/profiles/U1/monthly-billing` | `CreditController` + RunMonthlyBilling |
| 权益换电（非计量 / 计量 / 默认选卡） | `/` 岛 → `/api/entitled-swaps` | `EntitledSwapController` |
| IoT 影子 / COMM_LOST / 遥测 / triage / 工单 | `/iot` → `/api/iot/...` | `IotController` |
| 商城领券 / 下单 / 带券结账 | `/mall` → `/api/mall/...` | `MallController` |

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

- 登录与多用户

## 已接通（wave12–13）

- 默认选卡 HTTP / FE（省略 entitlementId → E-FINITE）
- IoT 遥测入影 UI · SOC 过时诊断 · 工单列表
- 商城下单 HTTP / FE（POST /mall/orders · S1）

## 新接通（权益换电）

```bash
curl -s -X POST http://localhost:8080/entitled-swaps \
  -H "Content-Type: application/json" \
  -d '{"userId":"U1","entitlementId":"E-1","cabinetId":"CAB-1"}'
# 省略 entitlementId → 默认选卡优先 E-FINITE（AC-14）
# curl ... -d '{"userId":"U1","cabinetId":"CAB-1"}'
```

## 新接通（计量权益换电）
```bash
# E-M1 PAY_AS_YOU_GO；soc 80→60 × 50¢ = 1000¢ → chargedAmountCents=1000
curl -s -X POST http://localhost:8080/entitled-swaps \
  -H "Content-Type: application/json" \
  -d '{"userId":"U1","entitlementId":"E-M1","cabinetId":"CAB-1","socBefore":80,"socAfter":60}'
```
## 新接通（逾期冻权益）

```bash
# 逾期冻 E-1；随后 POST /entitled-swaps → 409 CREDIT_OVERDUE_BLOCKED
curl -s -X POST http://localhost:8080/credit/profiles/U1/mark-overdue \
  -H "Content-Type: application/json" \
  -d '{"statementId":"STMT-2026-02"}'
```

## 新接通（还款解冻）

```bash
# 全额还 STMT-2026-02（需先 mark-overdue）；随后 POST /entitled-swaps → 200 COMPLETED
curl -s -X POST http://localhost:8080/credit/profiles/U1/repay \
  -H "Content-Type: application/json" \
  -d '{"statementId":"STMT-2026-02","amountCents":3000}'
```

## 新接通（信用购）

```bash
# P-CREDIT-1 FIXED 3000¢；U1 可用额度 7000 → 200 orderId/entitlementId/debtId
curl -s -X POST http://localhost:8080/credit/purchases \
  -H "Content-Type: application/json" \
  -d '{"userId":"U1","productId":"P-CREDIT-1"}'
```

## 新接通（月度出账 AC-50）

```bash
# 先 credit/purchases 造 OPEN debt；再出账 → 200 stmt-UUID + totalDue=3000 status=DUE
curl -s -X POST http://localhost:8080/credit/profiles/U1/monthly-billing \
  -H "Content-Type: application/json" \
  -d '{"periodStart":"2026-02-01T00:00:00Z","periodEnd":"2026-02-28T23:59:59Z"}'
```

## 新接通（IoT COMM_LOST）

```bash
# 影子只读；BAT-IOT-1 lastSeen 过期
curl -s http://localhost:8080/iot/batteries/BAT-IOT-1/shadow

# stale → COMM_LOST + OPEN ticket；再跑同 ticketId（不重复开单）
curl -s -X POST http://localhost:8080/iot/batteries/BAT-IOT-1/detect-comm-lost
```

## 新接通（IoT 遥测入影）

```bash
# 入影刷新 soc/lastSeenAt，清除 stale；200 返回影子摘要
curl -s -X POST http://localhost:8080/iot/batteries/BAT-IOT-1/telemetry \
  -H "Content-Type: application/json" \
  -d '{"vendorId":"vendorA","soc":75,"voltageMilli":4150}'

# 验证：GET shadow soc=75 stale=false
curl -s http://localhost:8080/iot/batteries/BAT-IOT-1/shadow

# SOC 过时诊断：先 shadow.stale → SHADOW_STALE；遥测刷新后 → CHECK_ADAPTER
curl -s -X POST http://localhost:8080/iot/batteries/BAT-IOT-1/triage-outdated-soc
```

## 新接通（商城下单 · wave13）

```bash
# S1 1000¢；U1 余额种子 5000¢ → 200 PAID
curl -s -X POST http://localhost:8080/mall/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":"U1","merchantOrgId":"M1","skuId":"S1","qty":1}'
```

## 新接通（带券结账 · wave14 / 16a）

```bash
# 1) 领券 → 记下 id
curl -s -X POST http://localhost:8080/mall/campaigns/CAMP-OK/claims \
  -H "Content-Type: application/json" \
  -d '{"userId":"U1","templateId":"T-C1"}'
# 2) 带券下单（T-C1 minSpend 3000¢ → qty=3；paid=2500 discount=500）
curl -s -X POST http://localhost:8080/mall/orders/checkout-with-coupons \
  -H "Content-Type: application/json" \
  -d '{"userId":"U1","merchantOrgId":"M1","skuId":"S1","qty":3,"userCouponIds":["<couponId>"]}'
```

## 新接通（运维工单列表 · wave13）

```bash
# 先 detect-comm-lost 开单，再查列表
curl -s -X POST http://localhost:8080/iot/batteries/BAT-IOT-1/detect-comm-lost
curl -s http://localhost:8080/iot/batteries/BAT-IOT-1/tickets
```

## 正式验收（两枪）

- 站列表：`curl -s http://localhost:8080/stations` → JSON 数组长度 ≥1（含 S1/S2/S3）
- 信用档案：`curl -s http://localhost:8080/credit/profiles/U1` → HTTP 200（userId=U1）

契约测试：`FormalLiveContractTest` 钉死 S1/S2/S3 + U1（MockMvc）。

## 验收速查

```bash
curl -s http://localhost:8080/stations | head
curl -s http://localhost:8080/credit/profiles/U1
curl -s http://localhost:8080/credit/profiles/U1/statements
curl -s -X POST http://localhost:8080/entitled-swaps -H "Content-Type: application/json" -d '{"userId":"U1","entitlementId":"E-1","cabinetId":"CAB-1"}'
```
