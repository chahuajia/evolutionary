/**
 * 运营商工作台 — RSC 首屏读有效价，操作为客户端岛。
 */

import { PageHeader } from "@/components/page-header";
import {
  DEFAULT_DOWNLINE_APPLICATION_ID,
  DEFAULT_OVERRIDE_ID,
  DEFAULT_PACKAGE_TEMPLATE_ID,
} from "@/domains/operator/infrastructure/operator-gateway";
import {
  DEFAULT_OVERRIDE_ACTOR_ORG_ID,
  DEFAULT_OVERRIDE_TEMPLATE_ID,
  loadEffectiveProduct,
} from "@/domains/operator/application/load-effective-product";
import type { EffectiveProductResult } from "@/domains/operator/infrastructure/operator-gateway";
import { OperatorWorkspace } from "./operator-workspace";
import styles from "./page.module.css";

export default async function OperatorPage() {
  let product: EffectiveProductResult | null = null;
  let error: string | null = null;

  try {
    product = await loadEffectiveProduct(
      DEFAULT_OVERRIDE_ACTOR_ORG_ID,
      DEFAULT_OVERRIDE_TEMPLATE_ID,
    );
  } catch (e) {
    error = e instanceof Error ? e.message : "有效价拉取失败";
  }

  return (
    <>
      <PageHeader
        eyebrow="运营商 · 配置"
        title="运营配置"
        description={`运营商发布套餐（${DEFAULT_PACKAGE_TEMPLATE_ID}）→ L2 套餐覆盖（${DEFAULT_OVERRIDE_TEMPLATE_ID}）→ 撤销覆盖（${DEFAULT_OVERRIDE_ID}）→ 批运营商下线（${DEFAULT_DOWNLINE_APPLICATION_ID}）。商城商家入驻由总后台审批。`}
      />
      {error ? (
        <p className={styles.alert} role="alert">
          {error}
        </p>
      ) : product ? (
        <section className={styles.overview} aria-label="有效价概览">
          <p className={styles.note}>
            {DEFAULT_OVERRIDE_ACTOR_ORG_ID} / {product.templateId} · 只读 · RSC
          </p>
          <p className={styles.note}>
            {product.displayName} · ¥{(product.priceCents / 100).toFixed(2)} ·
            覆盖 {product.overrideId ?? "无覆盖"}
          </p>
        </section>
      ) : null}
      <OperatorWorkspace />
    </>
  );
}
