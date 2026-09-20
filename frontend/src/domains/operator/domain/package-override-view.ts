/**
 * 套餐覆盖展示模型 —— 视图边界。
 *
 * 对齐后端 `PackageOverride`：
 * - `activate` 仅 DRAFT
 * - `revoke` 仅 ACTIVE
 */

export type PackageOverrideStatus = "DRAFT" | "ACTIVE" | "REVOKED";

const PACKAGE_OVERRIDE_STATUSES = ["DRAFT", "ACTIVE", "REVOKED"] as const;

/** 边界解析：未知值当场炸。 */
export function parsePackageOverrideStatus(
  raw: unknown,
): PackageOverrideStatus {
  if (
    typeof raw === "string" &&
    (PACKAGE_OVERRIDE_STATUSES as readonly string[]).includes(raw)
  ) {
    return raw as PackageOverrideStatus;
  }
  throw new Error(
    `未知套餐覆盖状态：${String(raw)}（契约：${PACKAGE_OVERRIDE_STATUSES.join(" | ")}）`,
  );
}

export type PackageOverrideView = {
  readonly overrideId: string;
  readonly orgId: string;
  readonly templateId: string;
  readonly templateVersion: number;
  readonly status: PackageOverrideStatus;
  readonly statusLabel: string;
  /** 终态：已撤销（展示结果区用，勿再 status===）。 */
  readonly revoked: boolean;
  /** 仅 DRAFT 可激活（对齐 `PackageOverride.activate`）。 */
  readonly activateAllowed: boolean;
  /** 仅 ACTIVE 可撤销（对齐 `PackageOverride.revoke`）。 */
  readonly revokeAllowed: boolean;
  readonly blockMessage: string | null;
};

export const PACKAGE_OVERRIDE_STATUS_LABEL: Record<
  PackageOverrideStatus,
  string
> = {
  DRAFT: "草稿",
  ACTIVE: "已激活",
  REVOKED: "已撤销",
};

/** 展示不变量：仅草稿可激活。 */
export function canActivateOverride(status: PackageOverrideStatus): boolean {
  return status === "DRAFT";
}

/** 展示不变量：仅激活态可撤销。 */
export function canRevokeOverride(status: PackageOverrideStatus): boolean {
  return status === "ACTIVE";
}

export function isOverrideRevoked(status: PackageOverrideStatus): boolean {
  return status === "REVOKED";
}

/**
 * 不可动原因。ACTIVE 指向撤销路径；REVOKED 是终态。
 */
export function overrideBlockMessage(
  status: PackageOverrideStatus,
): string | null {
  if (status === "DRAFT") return null;
  if (status === "ACTIVE") {
    return "覆盖已激活 —— 变更请先撤销后再新建";
  }
  return "覆盖已撤销，不可再激活或撤销";
}

export function toPackageOverrideView(dto: {
  overrideId: string;
  orgId: string;
  templateId: string;
  templateVersion: number;
  status: PackageOverrideStatus;
}): PackageOverrideView {
  return {
    overrideId: dto.overrideId,
    orgId: dto.orgId,
    templateId: dto.templateId,
    templateVersion: dto.templateVersion,
    status: dto.status,
    statusLabel: PACKAGE_OVERRIDE_STATUS_LABEL[dto.status],
    revoked: isOverrideRevoked(dto.status),
    activateAllowed: canActivateOverride(dto.status),
    revokeAllowed: canRevokeOverride(dto.status),
    blockMessage: overrideBlockMessage(dto.status),
  };
}
