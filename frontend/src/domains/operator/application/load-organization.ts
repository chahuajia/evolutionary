/**
 * 用例：加载组织展示模型（编排 gateway → OrganizationView）。
 */

import {
  toOrganizationView,
  type OrganizationView,
} from "@/domains/operator/domain/organization-view";
import { fetchOrganization } from "@/domains/operator/infrastructure/operator-gateway";

export async function loadOrganization(
  orgId: string,
): Promise<OrganizationView> {
  const dto = await fetchOrganization(orgId);
  return toOrganizationView({
    id: dto.id,
    name: dto.name,
    parentId: dto.parentId,
    status: dto.status,
    operatorCapability: dto.operatorCapability,
  });
}
