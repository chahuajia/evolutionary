/**
 * 商城订单展示模型 — 视图边界。
 */

export type MallOrderView = {
  readonly orderId: string;
  readonly userId: string;
  readonly merchantOrgId: string;
  readonly status: string;
  readonly paidAmountCents: number;
  readonly paidAmountYuan: string;
  readonly skuId: string;
  readonly qty: number;
};

export function formatCentsAsYuan(cents: number): string {
  return (cents / 100).toFixed(2);
}

export function toMallOrderView(dto: {
  orderId: string;
  userId: string;
  merchantOrgId: string;
  status: string;
  paidAmountCents: number;
  skuId: string;
  qty: number;
}): MallOrderView {
  return {
    orderId: dto.orderId,
    userId: dto.userId,
    merchantOrgId: dto.merchantOrgId,
    status: dto.status,
    paidAmountCents: dto.paidAmountCents,
    paidAmountYuan: formatCentsAsYuan(dto.paidAmountCents),
    skuId: dto.skuId,
    qty: dto.qty,
  };
}
