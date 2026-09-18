/**
 * 商城 — 领券 + 无券下单 + 带券结账客户端岛对接 Spring。
 */

import Link from "next/link";
import {
  DEFAULT_MALL_CAMPAIGN,
  DEFAULT_MALL_MERCHANT,
  DEFAULT_MALL_SKU,
  DEFAULT_MALL_TEMPLATE,
  DEFAULT_MALL_USER,
} from "@/domains/mall/infrastructure/mall-gateway";
import { CouponClaimPanel } from "./coupon-claim-panel";
import { CouponCheckoutPanel } from "./coupon-checkout-panel";
import { MallPurchasePanel } from "./mall-purchase-panel";
import styles from "./page.module.css";

export default function MallPage() {
  return (
    <main className={styles.main}>
      <nav className={styles.nav}>
        <Link href="/">← 换电首页</Link>
      </nav>

      <h1 className={styles.title}>商城</h1>
      <p className={styles.note}>
        领券：
        <code>POST /mall/campaigns/{"{campaignId}"}/claims</code>
        （默认 {DEFAULT_MALL_CAMPAIGN}/{DEFAULT_MALL_USER}/{DEFAULT_MALL_TEMPLATE}
        ）。下单：
        <code>POST /mall/orders</code>
        （默认 {DEFAULT_MALL_MERCHANT}/{DEFAULT_MALL_SKU}）。带券结账：
        <code>POST /mall/orders/checkout-with-coupons</code>
        （默认 {DEFAULT_MALL_USER}/{DEFAULT_MALL_MERCHANT}/{DEFAULT_MALL_SKU}）。
      </p>

      <CouponClaimPanel />
      <MallPurchasePanel />
      <CouponCheckoutPanel />
    </main>
  );
}
