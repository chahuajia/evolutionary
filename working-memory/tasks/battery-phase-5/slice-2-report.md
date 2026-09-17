# phase-5 切片2 报告

**分支**：`phase/p5-mall`  
**交付**：`MallSku` · `MallOrder` · `PurchaseMallOrder`（PAID 无 Entitlement/UsageEvent）  
**验收**：AC-41 / INV-16  
**包**：`com.evolutionary.mall`

## 要点

- 仅依赖 `merchantOrgId` 字符串，可与切片1 Merchant 并行
- `PurchaseMallOrder` **故意不注入** Entitlement/UsageEvent 仓储
- domain/application 无 Spring/JPA
