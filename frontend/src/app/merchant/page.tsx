/**
 * 商家端 — 商城入驻进度（平台总后台审批，非运营商）。
 */

import Link from "next/link";
import { PageHeader } from "@/components/page-header";
import { toOnboardingApplicationView } from "@/domains/operator/domain/onboarding-application-view";
import styles from "./page.module.css";

const SEED = toOnboardingApplicationView({
  id: "APP-M1",
  orgId: "ORG-NEW",
  capability: "MERCHANT",
  status: "SUBMITTED",
});

export default function MerchantPage() {
  return (
    <>
      <PageHeader
        eyebrow="商家 · 商城入驻"
        title="入驻进度"
        description="商城商家入驻由总后台平台审批；电池运营商不批准商家入驻，只管理自己的下线运营商。"
      />
      <div className={styles.surface}>
        <dl className={styles.meta}>
          <div>
            <dt>申请单</dt>
            <dd>{SEED.id}</dd>
          </div>
          <div>
            <dt>拟开组织</dt>
            <dd>{SEED.orgId}</dd>
          </div>
          <div>
            <dt>能力</dt>
            <dd>{SEED.capability}</dd>
          </div>
          <div>
            <dt>状态</dt>
            <dd>
              <span className={styles.badge}>{SEED.statusLabel}</span>
            </dd>
          </div>
          <div>
            <dt>可批准</dt>
            <dd>{SEED.approveAllowed ? "是（待总后台）" : "否"}</dd>
          </div>
        </dl>
        <p className={styles.note}>
          演示种子与后端 DevSeed 对齐。审批入口在总后台「平台审批」，不是运营商「运营配置」。
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
