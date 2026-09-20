/**
 * 用例：加载商城 SKU 展示模型（编排 gateway → MallSkuView）。
 */

import {
  parseMallSkuStatus,
  toMallSkuView,
  type MallSkuView,
} from "@/domains/mall/domain/mall-sku-view";
import {
  DEFAULT_MALL_SKU,
  fetchMallSku,
} from "@/domains/mall/infrastructure/mall-gateway";

export async function loadMallSku(
  skuId: string = DEFAULT_MALL_SKU,
  qty: number = 1,
): Promise<MallSkuView> {
  const dto = await fetchMallSku(skuId);
  return toMallSkuView(
    {
      id: dto.id,
      merchantOrgId: dto.merchantOrgId,
      name: dto.name,
      priceCents: dto.priceCents,
      stock: dto.stock,
      status: parseMallSkuStatus(dto.status),
    },
    qty,
  );
}

export { DEFAULT_MALL_SKU };
