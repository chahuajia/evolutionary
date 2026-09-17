# phase-0 切片 3 报告

**日期**：2026-09-17 ｜ **压力层**：L2 ｜ **集群**：explore 探路 + 主写切片 3 + 并行切片 4 agent

## 交付

| 范围 | 内容 |
| :--- | :--- |
| 领域 | `BatteryAsset`（idle/rented）、`UsageEvent`（STARTED→COMPLETED） |
| 用例 | `PerformEntitledSwap`：权益/过期门禁 → INV-3 → 闭环换电（INV-4） |
| 切片 2 | 过期校验在换电入口落地（`ENTITLEMENT_EXPIRED`） |

## 设计备注

- 与旧 `battery.domain.Battery`（AVAILABLE/IN_USE）**分离**，避免状态机混用
- phase-0：一次 `execute` = STARTED→COMPLETED + idle→rented→idle

## 测量

- collaboration HEAD：不变
- interceptions：0（候选：Battery vs BatteryAsset 双模型边界，暂不入库）
- `mvn test`：绿

## 下一 tick

切片 4 退款（集群 agent 并行中）→ 全切片收口 / W4
