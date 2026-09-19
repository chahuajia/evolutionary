import { describe, expect, it } from "vitest";
import {
  canCreateNextVersion,
  canPublishTemplate,
  canReplaceBaseProduct,
  parsePackageTemplateStatus,
  templateBlockMessage,
  toPackageTemplateView,
  type PackageTemplateStatus,
} from "./package-template-view";

// ── 展示不变量（对齐后端 `PackageTemplate` 的三个守卫）──────────
// `publish`              仅 DRAFT
// `replaceBaseProduct`   仅 DRAFT（否则 TEMPLATE_IMMUTABLE「已发布模板不可原地修改」）
// `createNextVersionDraft` 仅 PUBLISHED

describe("parsePackageTemplateStatus", () => {
  it("accepts contract values", () => {
    expect(parsePackageTemplateStatus("DRAFT")).toBe("DRAFT");
    expect(parsePackageTemplateStatus("PUBLISHED")).toBe("PUBLISHED");
    expect(parsePackageTemplateStatus("DEPRECATED")).toBe("DEPRECATED");
  });

  it("throws on unknown", () => {
    expect(() => parsePackageTemplateStatus("ACTIVE")).toThrow(/未知套餐模板状态/);
  });
});

describe("canPublishTemplate", () => {
  it("only DRAFT can be published", () => {
    expect(canPublishTemplate("DRAFT")).toBe(true);
    expect(canPublishTemplate("PUBLISHED")).toBe(false);
    expect(canPublishTemplate("DEPRECATED")).toBe(false);
  });
});

describe("canReplaceBaseProduct", () => {
  it("only DRAFT can be edited in place", () => {
    // 已发布模板**不可原地修改** —— 这是 TEMPLATE_IMMUTABLE 的展示面
    expect(canReplaceBaseProduct("DRAFT")).toBe(true);
    expect(canReplaceBaseProduct("PUBLISHED")).toBe(false);
    expect(canReplaceBaseProduct("DEPRECATED")).toBe(false);
  });
});

describe("canCreateNextVersion", () => {
  it("only PUBLISHED can spawn the next version", () => {
    expect(canCreateNextVersion("PUBLISHED")).toBe(true);
    expect(canCreateNextVersion("DRAFT")).toBe(false);
    expect(canCreateNextVersion("DEPRECATED")).toBe(false);
  });
});

describe("templateBlockMessage", () => {
  it("is null for DRAFT (everything is still possible)", () => {
    expect(templateBlockMessage("DRAFT")).toBeNull();
  });

  it("explains PUBLISHED as immutable-in-place, and names the alternative", () => {
    const msg = templateBlockMessage("PUBLISHED");
    expect(msg).not.toBeNull();
    expect(msg).toContain("不可原地修改");
    // 光说"不行"没用 —— 要给出**能怎么办**
    expect(msg).toContain("下一版本");
  });

  it("distinguishes DEPRECATED from PUBLISHED", () => {
    expect(templateBlockMessage("DEPRECATED")).not.toBe(
      templateBlockMessage("PUBLISHED"),
    );
  });
});

describe("toPackageTemplateView", () => {
  const base = {
    id: "T-1",
    ownerOrgId: "ORG-L1",
    version: 2,
    status: "PUBLISHED" as PackageTemplateStatus,
  };

  it("carries the gates so the panel never has to re-derive them", () => {
    const view = toPackageTemplateView(base);
    expect(view.publishAllowed).toBe(false);
    expect(view.replaceAllowed).toBe(false);
    expect(view.nextVersionAllowed).toBe(true);
    expect(view.blockMessage).toContain("不可原地修改");
  });

  it("DRAFT: publish/replace yes, next-version no", () => {
    const view = toPackageTemplateView({ ...base, status: "DRAFT" });
    expect(view.publishAllowed).toBe(true);
    expect(view.replaceAllowed).toBe(true);
    expect(view.nextVersionAllowed).toBe(false);
    expect(view.blockMessage).toBeNull();
  });
});
