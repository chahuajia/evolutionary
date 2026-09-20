/**
 * 商家端 — 商城入驻进度（平台总后台审批，非运营商）。
 * GET /operator/onboarding/APP-M1 对齐 approveAllowed。
 */

import Link from "next/link";
import { PageHeader } from "@/components/page-header";
import {
  parseOnboardingStatus,
  toOnboardingApplicationView,
} from "@/domains/operator/domain/onboarding-application-view";
import {
  DEFAULT_ONBOARDING_APPLICATION_ID,
  fetchOnboardingApplication,
} from "@/domains/operator/infrastructure/operator-gateway";
import styles from "./page.module.css";

export default async function MerchantPage() {
  let app = null as ReturnType<typeof toOnboardingApplicationView> | null;
  let loadError: string | null = null;
  try {
    const dto = await fetchOnboardingApplication(
      DEFAULT_ONBOARDING_APPLICATION_ID,
    );
    app = toOnboardingApplicationView({
      id: dto.id,
      orgId: dto.orgId,
      capability: dto.capability,
      status: parseOnboardingStatus(dto.status),
    });
  } catch (err) {
    loadError = err instanceof Error ? err.message : String(err);
  }

  return (
    <>
      <PageHeader
        eyebrow="商家 · 商城入驻"
        title="入驻进度"
        description="商城商家入驻由总后台平台审批；电池运营商不批准商家入驻，只管理自己的下线运营商。"
      />
      <div className={styles.surface}>
        {app ? (
          <dl className={styles.meta}>
            <div>
              <dt>申请单</dt>
              <dd>{app.id}</dd>
            </div>
            <div>
              <dt>拟开组织</dt>
              <dd>{app.orgId}</dd>
            </div>
            <div>
              <dt>能力</dt>
              <dd>{app.capability}</dd>
            </div>
            <div>
              <dt>状态</dt>
              <dd>
                <span className={styles.badge}>{app.statusLabel}</span>
              </dd>
            </div>
            <div>
              <dt>可批准</dt>
              <dd>{app.approveAllowed ? "是（待总后台）" : "否"}</dd>
            </div>
          </dl>
        ) : (
          <p className={styles.note} role="status">
            {loadError ?? "正在加载入驻申请…"}
          </p>
        )}
        <p className={styles.note}>
          GET 申请对齐 DevSeed APP-M1。审批入口在总后台「平台审批」，不是运营商「运营配置」。
        </p>
        <p className={styles.hint}>
          切换角色 →{" "}
          <Link href="/admin" className={styles.link}>
            总后台 /admin
          </Link>
          （侧栏选「总后台」）。
        </p>
      </div>
    </>
  );
}
