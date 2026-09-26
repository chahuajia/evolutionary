/**
 * 套餐模板展示模型 —— 视图边界。
 *
 * @remarks
 * operator 是**最后一个没有视图模型层的域**（此前只有 application / infrastructure，
 * 面板全靠手输 + 显示服务端返回的裸字符串）。本文件把后端的三个状态守卫
 * 变成**带名字的展示不变量**。
 */

import { formatCentsAsYuan } from "@/shared/money/format-cents";

/**
 * 后端 `PackageTemplate.Status` 的契约。
 *
 * 裸 `string` 会让契约从类型上丢失 —— settlement 那条漂移
 * （测试里用了后端从不存在的 `"ACCRUED"`）就是这么发生的（[[S33-测试数据的契约一致性]]）。
 */
export type PackageTemplateStatus = "DRAFT" | "PUBLISHED" | "DEPRECATED";

const PACKAGE_TEMPLATE_STATUSES = [
  "DRAFT",
  "PUBLISHED",
  "DEPRECATED",
] as const;

/**
 * 边界解析：未知值当场炸，不产生默认值（parse-dont-validate）。
 */
export function parsePackageTemplateStatus(
  raw: unknown,
): PackageTemplateStatus {
  if (
    typeof raw === "string" &&
    (PACKAGE_TEMPLATE_STATUSES as readonly string[]).includes(raw)
  ) {
    return raw as PackageTemplateStatus;
  }
  throw new Error(
    `未知套餐模板状态：${String(raw)}（契约：${PACKAGE_TEMPLATE_STATUSES.join(" | ")}）`,
  );
}

export const PACKAGE_TEMPLATE_STATUS_LABEL: Record<
  PackageTemplateStatus,
  string
> = {
  DRAFT: "草稿",
  PUBLISHED: "已发布",
  DEPRECATED: "已废弃",
};

export type PackageTemplateView = {
  readonly id: string;
  readonly ownerOrgId: string;
  readonly version: number;
  readonly status: PackageTemplateStatus;
  readonly statusLabel: string;
  readonly displayName: string | null;
  readonly priceCents: number | null;
  readonly priceYuan: string | null;
  readonly durationDays: number | null;
  readonly inheritedFrom: string | null;
  /** 仅 DRAFT 可发布（对齐 `PackageTemplate.publish` 守卫）。 */
  readonly publishAllowed: boolean;
  /** 仅 DRAFT 可原地改基产品（对齐 `replaceBaseProduct`：否则 TEMPLATE_IMMUTABLE）。 */
  readonly replaceAllowed: boolean;
  /** 仅 PUBLISHED 可派生下一版本（对齐 `createNextVersionDraft` 守卫）。 */
  readonly nextVersionAllowed: boolean;
  /** 仅 PUBLISHED 可激活覆盖（对齐 `ActivatePackageOverride` / TEMPLATE_NOT_PUBLISHED）。 */
  readonly overrideActivateAllowed: boolean;
  /** 不可动时的原因；可动为 null。 */
  readonly blockMessage: string | null;
};

/** 展示不变量：仅 DRAFT 可发布。对齐 `PackageTemplate.publish`。 */
export function canPublishTemplate(status: PackageTemplateStatus): boolean {
  return status === "DRAFT";
}

/**
 * 展示不变量：仅 DRAFT 可原地改。
 *
 * 对齐 `replaceBaseProduct` —— 已发布模板**不可原地修改**，
 * 合法变更是"派生下一版本草稿"。这是 `TEMPLATE_IMMUTABLE` 的展示面。
 */
export function canReplaceBaseProduct(status: PackageTemplateStatus): boolean {
  return status === "DRAFT";
}

/** 展示不变量：仅 PUBLISHED 可派生下一版本。对齐 `createNextVersionDraft`。 */
export function canCreateNextVersion(status: PackageTemplateStatus): boolean {
  return status === "PUBLISHED";
}

/** 展示不变量：仅 PUBLISHED 可激活覆盖。对齐 `ActivatePackageOverride`。 */
export function canActivateOverrideOnTemplate(
  status: PackageTemplateStatus,
): boolean {
  return status === "PUBLISHED";
}

/**
 * 不可动的原因（供 UI 说明，不让用户对着灰按钮猜）。
 *
 * @remarks
 * **光说"不行"没用，要给出"能怎么办"。**
 * PUBLISHED 的说明必须指向**派生下一版本**这条合法路径 ——
 * 否则用户面对一个改不动的已发布模板，唯一能想到的是"那我复制一份"，
 * 而正确做法是 `createNextVersionDraft`（保留 `inheritedFrom` 血缘）。
 */
export function templateBlockMessage(
  status: PackageTemplateStatus,
): string | null {
  if (status === "DRAFT") return null;
  if (status === "PUBLISHED") {
    return "已发布模板不可原地修改 —— 变更请派生下一版本草稿";
  }
  return "该模板已废弃，不可发布或修改";
}

export function toPackageTemplateView(dto: {
  id: string;
  ownerOrgId: string;
  version: number;
  status: PackageTemplateStatus;
  displayName?: string | null;
  priceCents?: number | null;
  durationDays?: number | null;
  inheritedFrom?: string | null;
}): PackageTemplateView {
  const priceCents =
    dto.priceCents != null && Number.isFinite(dto.priceCents)
      ? dto.priceCents
      : null;
  const durationDays =
    dto.durationDays != null && Number.isFinite(dto.durationDays)
      ? dto.durationDays
      : null;
  const displayName =
    dto.displayName != null && dto.displayName.trim() !== ""
      ? dto.displayName
      : null;
  return {
    id: dto.id,
    ownerOrgId: dto.ownerOrgId,
    version: dto.version,
    status: dto.status,
    statusLabel: PACKAGE_TEMPLATE_STATUS_LABEL[dto.status],
    displayName,
    priceCents,
    priceYuan: priceCents != null ? formatCentsAsYuan(priceCents) : null,
    durationDays,
    inheritedFrom: dto.inheritedFrom ?? null,
    publishAllowed: canPublishTemplate(dto.status),
    replaceAllowed: canReplaceBaseProduct(dto.status),
    nextVersionAllowed: canCreateNextVersion(dto.status),
    overrideActivateAllowed: canActivateOverrideOnTemplate(dto.status),
    blockMessage: templateBlockMessage(dto.status),
  };
}
