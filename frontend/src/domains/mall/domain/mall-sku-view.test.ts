import { describe, expect, it } from "vitest";
import {
  canPurchaseSku,
  isValidPurchaseQty,
  parseMallSkuStatus,
  skuPurchaseBlockMessage,
  toMallSkuView,
} from "./mall-sku-view";

describe("parseMallSkuStatus", () => {
  it("accepts ON_SALE / OFF_SALE", () => {
    expect(parseMallSkuStatus("ON_SALE")).toBe("ON_SALE");
    expect(parseMallSkuStatus("OFF_SALE")).toBe("OFF_SALE");
  });

  it("throws on unknown", () => {
    expect(() => parseMallSkuStatus("DRAFT")).toThrow(/未知 SKU 状态/);
  });
});

describe("canPurchaseSku", () => {
  it("requires on-sale, stock, and positive qty", () => {
    expect(canPurchaseSku("ON_SALE", 5, 2)).toBe(true);
    expect(canPurchaseSku("ON_SALE", 1, 2)).toBe(false);
    expect(canPurchaseSku("OFF_SALE", 5, 1)).toBe(false);
    expect(canPurchaseSku("ON_SALE", 5, 0)).toBe(false);
  });
});

describe("isValidPurchaseQty", () => {
  it("rejects non-positive", () => {
    expect(isValidPurchaseQty(1)).toBe(true);
    expect(isValidPurchaseQty(0)).toBe(false);
  });
});

describe("toMallSkuView", () => {
  it("blocks off-sale", () => {
    const view = toMallSkuView({
      id: "S1",
      merchantOrgId: "M1",
      name: "pack",
      priceCents: 100,
      stock: 10,
      status: "OFF_SALE",
    });
    expect(view.purchaseAllowed).toBe(false);
    expect(skuPurchaseBlockMessage(view.status, view.stock, 1)).toContain(
      "未上架",
    );
  });
});
