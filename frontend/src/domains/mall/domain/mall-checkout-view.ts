/**
 * 带券结账展示模型 — 视图边界（切片 52a）。
 */

export type MallCheckoutView = {
  readonly orderId: string;
  readonly userId: string;
  readonly merchantOrgId: string;
  readonly status: string;
  readonly paidAmountCents: number;
  readonly paidAmountYuan: string;
  readonly skuId: string;
  readonly qty: number;
  readonly discountCents: number;
  readonly discountYuan: string;
};

function formatCentsAsYuan(cents: number): string {
  return (cents / 100).toFixed(2);
}

export function toCheckoutView(dto: {
  orderId: string;
  userId: string;
  merchantOrgId: string;
  status: string;
  paidAmountCents: number;
  skuId: string;
  qty: number;
  discountCents: number;
}): MallCheckoutView {
  return {
    orderId: dto.orderId,
    userId: dto.userId,
    merchantOrgId: dto.merchantOrgId,
    status: dto.status,
    paidAmountCents: dto.paidAmountCents,
    paidAmountYuan: formatCentsAsYuan(dto.paidAmountCents),
    skuId: dto.skuId,
    qty: dto.qty,
    discountCents: dto.discountCents,
    discountYuan: formatCentsAsYuan(dto.discountCents),
  };
}
