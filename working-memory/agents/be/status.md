# BE agent status（wave16）

**日期**：2026-09-18 · evo-collab-extreme wave16 / extreme v9  
**20a**：订单退款 HTTP（AC-20 / INV-9）· `POST /commerce/orders/{orderId}/refund` · `RefundOrderHttpIT` · SUCCESS  

FE 契约：`POST /commerce/orders/{orderId}/refund` → `{ orderId, status, revokedEntitlementId }`；错误 `ORDER_NOT_REFUNDABLE` (422) / `REFUND_BLOCKED_IN_PROGRESS_SWAP` (409)。
