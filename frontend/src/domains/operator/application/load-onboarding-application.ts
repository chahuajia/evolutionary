/**
 * 用例：加载入驻申请展示模型（编排 gateway，不写 fetch 细节）。
 */

import {
  parseOnboardingStatus,
  toOnboardingApplicationView,
  type OnboardingApplicationView,
} from "@/domains/operator/domain/onboarding-application-view";
import {
  DEFAULT_ONBOARDING_APPLICATION_ID,
  fetchOnboardingApplication,
} from "@/domains/operator/infrastructure/operator-gateway";

export async function loadOnboardingApplication(
  applicationId: string = DEFAULT_ONBOARDING_APPLICATION_ID,
): Promise<OnboardingApplicationView> {
  const dto = await fetchOnboardingApplication(applicationId);
  return toOnboardingApplicationView({
    id: dto.id,
    orgId: dto.orgId,
    capability: dto.capability,
    status: parseOnboardingStatus(dto.status),
  });
}

export { DEFAULT_ONBOARDING_APPLICATION_ID };
