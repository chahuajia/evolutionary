/**
 * 商城促销工作台 — RSC 首屏读活动 / SKU 门，操作为客户端岛。
 */

import { PageHeader } from "@/components/page-header";
import {
  DEFAULT_MALL_CAMPAIGN,
  loadCampaign,
} from "@/domains/mall/application/load-campaign";
import {
  DEFAULT_MALL_SKU,
  loadMallSku,
} from "@/domains/mall/application/load-mall-sku";
import type { CampaignView } from "@/domains/mall/domain/campaign-view";
import type { MallSkuView } from "@/domains/mall/domain/mall-sku-view";
import { formatCentsAsYuan } from "@/shared/money/format-cents";
import { MallWorkspace } from "./mall-workspace";
import styles from "./page.module.css";

export default async function MallPage() {
  let campaign: CampaignView | null = null;
  let sku: MallSkuView | null = null;
  let error: string | null = null;

  try {
    const [c, s] = await Promise.all([
      loadCampaign(DEFAULT_MALL_CAMPAIGN),
      loadMallSku(DEFAULT_MALL_SKU),
    ]);
    campaign = c;
    sku = s;
  } catch (e) {
    error = e instanceof Error ? e.message : "商城读模型拉取失败";
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
      ) : (
        <section className={styles.overview} aria-label="商城概览">
          {campaign ? (
            <p className={styles.note}>
              活动 {campaign.id} · {campaign.name} · {campaign.status} · 剩余{" "}
              {campaign.budgetRemainingCents} 分 ·{" "}
              {campaign.claimAllowed
                ? "可领券"
                : (campaign.blockMessage ?? "不可领券")}
            </p>
          ) : null}
          {sku ? (
            <p className={styles.note}>
              SKU {sku.id} · {sku.name} · ¥{formatCentsAsYuan(sku.priceCents)} ·
              库存 {sku.stock} ·{" "}
              {sku.purchaseAllowed
                ? "可购"
                : (sku.blockMessage ?? "不可购")}
            </p>
          ) : null}
        </section>
      )}
      <MallWorkspace />
    </>
  );
}
