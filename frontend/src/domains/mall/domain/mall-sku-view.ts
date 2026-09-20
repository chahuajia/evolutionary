/**
 * 商城 SKU 展示模型 — 视图边界。
 *
 * 对齐后端 `MallSku.deductStock`：
 * - qty 必须为正
 * - 仅 ON_SALE 可购
 * - stock >= qty
 */

export type MallSkuStatus = "ON_SALE" | "OFF_SALE";

const MALL_SKU_STATUSES = ["ON_SALE", "OFF_SALE"] as const;

export function parseMallSkuStatus(raw: unknown): MallSkuStatus {
  if (
    typeof raw === "string" &&
    (MALL_SKU_STATUSES as readonly string[]).includes(raw)
  ) {
    return raw as MallSkuStatus;
  }
  throw new Error(
    `未知 SKU 状态：${String(raw)}（契约：${MALL_SKU_STATUSES.join(" | ")}）`,
  );
}

export type MallSkuView = {
  readonly id: string;
  readonly merchantOrgId: string;
  readonly name: string;
  readonly priceCents: number;
  readonly stock: number;
  readonly status: MallSkuStatus;
  readonly statusLabel: string;
  readonly purchaseAllowed: boolean;
  readonly blockMessage: string | null;
};

export const MALL_SKU_STATUS_LABEL: Record<MallSkuStatus, string> = {
  ON_SALE: "上架",
  OFF_SALE: "下架",
};

/** qty 必须为正（对齐 deductStock）。 */
export function isValidPurchaseQty(qty: number): boolean {
  return Number.isFinite(qty) && qty > 0;
}

/**
 * 展示不变量：可购 = 上架 ∧ 库存够 ∧ qty 合法。
 */
export function canPurchaseSku(
  status: MallSkuStatus,
  stock: number,
  qty: number,
): boolean {
  if (!isValidPurchaseQty(qty)) return false;
  if (status !== "ON_SALE") return false;
  return stock >= qty;
}

export function skuPurchaseBlockMessage(
  status: MallSkuStatus,
  stock: number,
  qty: number,
): string | null {
  if (!isValidPurchaseQty(qty)) return "购买数量必须为正";
  if (status !== "ON_SALE") return "SKU 未上架，不可购买";
  if (stock < qty) return `库存不足（仅剩 ${stock}）`;
  return null;
}

export function toMallSkuView(
  dto: {
    id: string;
    merchantOrgId: string;
    name: string;
    priceCents: number;
    stock: number;
    status: MallSkuStatus;
  },
  qty: number = 1,
): MallSkuView {
  return {
    id: dto.id,
    merchantOrgId: dto.merchantOrgId,
    name: dto.name,
    priceCents: dto.priceCents,
    stock: dto.stock,
    status: dto.status,
    statusLabel: MALL_SKU_STATUS_LABEL[dto.status],
    purchaseAllowed: canPurchaseSku(dto.status, dto.stock, qty),
    blockMessage: skuPurchaseBlockMessage(dto.status, dto.stock, qty),
  };
}
