/**
 * 组织展示模型 — 视图边界。
 *
 * 对齐后端：
 * - `Organization.isActive`：仅 ACTIVE
 * - `OrgAuthorization.canManage`：actor 与 owner 均须 ACTIVE（另卡祖先链/区域）
 */

export type OrganizationStatus = "PENDING" | "ACTIVE" | "SUSPENDED";

const ORGANIZATION_STATUSES = ["PENDING", "ACTIVE", "SUSPENDED"] as const;

export function parseOrganizationStatus(raw: unknown): OrganizationStatus {
  if (
    typeof raw === "string" &&
    (ORGANIZATION_STATUSES as readonly string[]).includes(raw)
  ) {
    return raw as OrganizationStatus;
  }
  throw new Error(
    `未知组织状态：${String(raw)}（契约：${ORGANIZATION_STATUSES.join(" | ")}）`,
  );
}

export const ORGANIZATION_STATUS_LABEL: Record<OrganizationStatus, string> = {
  PENDING: "待激活",
  ACTIVE: "有效",
  SUSPENDED: "已停用",
};

export type OrganizationView = {
  readonly id: string;
  readonly name: string;
  readonly parentId: string | null;
  readonly status: OrganizationStatus;
  readonly statusLabel: string;
  readonly operatorCapability: boolean;
  /** 仅 ACTIVE（对齐 isActive）。 */
  readonly active: boolean;
  /** 作为操作方时可否参与 canManage（仅看本组织 ACTIVE）。 */
  readonly canActAsManager: boolean;
  readonly blockMessage: string | null;
};

/** 展示不变量：仅 ACTIVE 为有效组织。 */
export function isOrganizationActive(status: OrganizationStatus): boolean {
  return status === "ACTIVE";
}

/**
 * 展示不变量：canManage 的组织态前置 —— actor 与 owner 均须 ACTIVE。
 * 祖先链/区域由后端再判。
 */
export function canManageOrganizations(
  actorStatus: OrganizationStatus,
  ownerStatus: OrganizationStatus,
): boolean {
  return (
    isOrganizationActive(actorStatus) && isOrganizationActive(ownerStatus)
  );
}

export function organizationBlockMessage(
  status: OrganizationStatus,
): string | null {
  if (status === "ACTIVE") return null;
  if (status === "PENDING") return "组织待激活，不可参与授权管理";
  return "组织已停用，不可参与授权管理";
}

export function toOrganizationView(dto: {
  id: string;
  name: string;
  parentId?: string | null;
  status: string;
  operatorCapability?: boolean;
}): OrganizationView {
  const status = parseOrganizationStatus(dto.status);
  return {
    id: dto.id,
    name: dto.name,
    parentId: dto.parentId ?? null,
    status,
    statusLabel: ORGANIZATION_STATUS_LABEL[status],
    operatorCapability: dto.operatorCapability ?? false,
    active: isOrganizationActive(status),
    canActAsManager: isOrganizationActive(status),
    blockMessage: organizationBlockMessage(status),
  };
}
