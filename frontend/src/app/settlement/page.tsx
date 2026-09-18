/**
 * 分润结算工作台。
 */

import { PageHeader } from "@/components/page-header";
import { SettlementPanel } from "./settlement-panel";
import styles from "./page.module.css";

export default function SettlementPage() {
  return (
    <>
      <PageHeader
        eyebrow="运营商 · 分润"
        title="分润结算"
        description="信用购成功后自动记 PENDING 意向；运营商查看全网意向并跑批关账，或手工记意向 / 冲销验证。"
      />
      <div className={styles.surface}>
        <SettlementPanel />
      </div>
    </>
  );
}
