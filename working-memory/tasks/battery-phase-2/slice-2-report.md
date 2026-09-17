# phase-2 切片2 报告

**日期**：2026-09-17 ｜ **压力层**：L2

## 交付

| 项 | 内容 |
| :--- | :--- |
| 退款用例 | `RefundOrder`：按 `orderId` 查支付分录 → 先 `ORDER_REFUND_BALANCE` 再 `ORDER_REFUND_POINTS` → 权益 `REVOKED` |
| 不变量 | INV-9：退款金额与支付分录逐类型相等；AC-20 逆序 |
| 仓储 | `LedgerRepository.findByOrderId`；`findByRefId`（默认委托） |
| 测试 | 纯余额退款回归 + AC-20 混合退款（20 积分 + 10 余额） |

## 测量

- collaboration：不变
- interceptions：0
- 叠在切片1混合购买之上（支付侧已写 `ORDER_PAYMENT_*`）
- `mvn test`：绿

## 备注

`RefundOrder` 拆账实现在切片1提交窗口内已提前合入；本切片补齐 AC-20 断言与 `findByRefId`，并收口 WM。
