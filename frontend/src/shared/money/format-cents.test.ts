import { describe, expect, it } from "vitest";
import { formatCentsAsYuan } from "./format-cents";

describe("formatCentsAsYuan", () => {
  it("converts cents to yuan string with two decimals", () => {
    expect(formatCentsAsYuan(250)).toBe("2.50");
    expect(formatCentsAsYuan(199)).toBe("1.99");
    expect(formatCentsAsYuan(10000)).toBe("100.00");
  });
});
