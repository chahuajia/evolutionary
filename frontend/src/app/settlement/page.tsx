/**
 * 分润结算工作台。
 *
 * @remarks
 * **RSC 首屏**：意向列表在服务端取，客户端岛只做写操作（记意向 / 跑批 / 冲销）。
 * 对齐 `/credit` 与 `/operator` —— 本仓其余路由根都已是 RSC，
 * settlement 此前是唯一还是纯壳的那个。
 */

import { PageHeader } from "@/components/page-header";
import {
  DEFAULT_SETTLEMENT_ORG,
  loadAccruals,
} from "@/domains/settlement/application/load-accruals";
import type { AccrualView } from "@/domains/settlement/domain/accrual-view";
import { SettlementPanel } from "./settlement-panel";
import styles from "./page.module.css";

export default async function SettlementPage() {
  let accruals: readonly AccrualView[] = [];
  let error: string | null = null;

  try {
    accruals = await loadAccruals(DEFAULT_SETTLEMENT_ORG);
  } catch (e) {
    error = e instanceof Error ? e.message : "结算数据拉取失败：请确认 Spring :8080 已启动。";
  }

  return (
    <>
      <PageHeader
        eyebrow="运营商 · 分润"
        title="分润结算"
        description={`组织 ${DEFAULT_SETTLEMENT_ORG} 的分润意向。服务端 RSC 读列表；记意向 / 跑批 / 冲销为客户端岛。`}
      />
      {error ? (
        <p className={styles.alert} role="alert">
          {error}
        </p>
      ) : null}
      <div className={styles.surface}>
        <SettlementPanel accruals={accruals} />
      </div>
    </>
  );
}
