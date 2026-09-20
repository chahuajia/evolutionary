/**
 * 商城促销工作台 — RSC 首屏读活动门，操作为客户端岛。
 */

import { PageHeader } from "@/components/page-header";
import {
  DEFAULT_MALL_CAMPAIGN,
  loadCampaign,
} from "@/domains/mall/application/load-campaign";
import type { CampaignView } from "@/domains/mall/domain/campaign-view";
import { MallWorkspace } from "./mall-workspace";
import styles from "./page.module.css";

export default async function MallPage() {
  let campaign: CampaignView | null = null;
  let error: string | null = null;

  try {
    campaign = await loadCampaign(DEFAULT_MALL_CAMPAIGN);
  } catch (e) {
    error = e instanceof Error ? e.message : "活动拉取失败";
  }

  return (
    <>
      <PageHeader
        eyebrow="消费者 · 商城"
        title="商城促销"
        description="领券 → 下单 / 带券结账。与换电权益账本分离（独立 MallOrder）。"
      />
      {error ? (
        <p className={styles.alert} role="alert">
          {error}
        </p>
      ) : campaign ? (
        <section className={styles.overview} aria-label="活动概览">
          <p className={styles.note}>
            {campaign.id} · {campaign.name} · 只读 · RSC
          </p>
          <p className={styles.note}>
            {campaign.status} · 剩余预算 {campaign.budgetRemainingCents} 分 ·{" "}
            {campaign.claimAllowed ? "可领券" : (campaign.blockMessage ?? "不可领券")}
          </p>
        </section>
      ) : null}
      <MallWorkspace />
    </>
  );
}
