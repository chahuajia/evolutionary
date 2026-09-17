/**
 * 商城领券 — 客户端岛对接 Spring POST /mall/campaigns/{campaignId}/claims。
 */

import Link from "next/link";
import {
  DEFAULT_MALL_CAMPAIGN,
  DEFAULT_MALL_TEMPLATE,
  DEFAULT_MALL_USER,
} from "@/domains/mall/infrastructure/mall-gateway";
import { CouponClaimPanel } from "./coupon-claim-panel";
import styles from "./page.module.css";

export default function MallPage() {
  return (
    <main className={styles.main}>
      <nav className={styles.nav}>
        <Link href="/">← 换电首页</Link>
      </nav>

      <h1 className={styles.title}>商城领券</h1>
      <p className={styles.note}>
        客户端岛调用{" "}
        <code>POST /mall/campaigns/{"{campaignId}"}/claims</code>
        ；默认活动 <code>{DEFAULT_MALL_CAMPAIGN}</code>、用户{" "}
        <code>{DEFAULT_MALL_USER}</code>、模板{" "}
        <code>{DEFAULT_MALL_TEMPLATE}</code>。成功展示券 id/status；错误经
        fetchJson suggestion。
      </p>

      <CouponClaimPanel />
    </main>
  );
}
