/**
 * 商家端占位 — 入驻由总后台审批，非运营商。
 */

import { PageHeader } from "@/components/page-header";
import styles from "./page.module.css";

export default function MerchantPage() {
  return (
    <>
      <PageHeader
        eyebrow="商家 · 入驻"
        title="商城商家：入驻进度（平台审批）"
        description="入驻申请由总后台平台审批，运营商不负责批准入驻。本页为进度占位，后续接通申请状态读模型。"
      />
      <div className={styles.surface}>
        <p className={styles.note}>
          当前状态：待总后台审批。请切换至「总后台」角色完成平台批准商家入驻。
        </p>
      </div>
    </>
  );
}
