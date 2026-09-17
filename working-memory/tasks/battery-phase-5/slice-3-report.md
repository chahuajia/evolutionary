# phase-5 切片3 报告

**分支**：`phase/p5-mall`  
**交付**：CouponTemplate · UserCoupon · CouponRedemption · Campaign · CouponRules · CheckoutMallOrderWithCoupons · ClaimCouponFromCampaign  
**验收**：AC-42..47  
**包**：`com.evolutionary.mall`

## 要点

- 轻量内置规则（互斥 / 叠加≤2 / scope），无外部引擎
- MERCHANT 券禁换电；OPERATOR 券默认不进商城（除非 SKU_LIST）
- 结账用例独立于 `PurchaseMallOrder`，INV-16 仍成立
