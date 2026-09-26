/**
 * 消费者钱包 — 余额 / 积分读模型。
 */

import { PageHeader } from "@/components/page-header";
import {
  DEFAULT_WALLET_USER,
  loadWallet,
} from "@/domains/wallet/application/load-wallet";
import type { WalletView } from "@/domains/wallet/domain/wallet-view";
import styles from "./page.module.css";

export default async function WalletPage() {
  let wallet: WalletView | null = null;
  let error: string | null = null;

  try {
    wallet = await loadWallet(DEFAULT_WALLET_USER);
  } catch (e) {
    error = e instanceof Error ? e.message : "钱包拉取失败";
  }

  return (
    <>
      <PageHeader
        eyebrow="消费者 · 钱包"
        title="余额与积分"
        description={`默认用户 ${DEFAULT_WALLET_USER}：余额购商城 / 积分抵扣；与信用额度分账。`}
      />
      {error ? (
        <p className={styles.alert} role="alert">
          {error}
        </p>
      ) : wallet ? (
        <div className={styles.overview}>
          <div className={styles.metric}>
            <span className={styles.metricLabel}>余额</span>
            <span className={styles.metricValue}>¥{wallet.balanceYuan}</span>
            <span className={styles.metricMeta}>
              {wallet.balanceCents} 分 · {wallet.currency}
              {wallet.hasSpendableBalance ? " · 可扣款" : " · 余额不足扣款"}
            </span>
          </div>
          <div className={styles.metric}>
            <span className={styles.metricLabel}>积分</span>
            <span className={styles.metricValue}>¥{wallet.pointsYuan}</span>
            <span className={styles.metricMeta}>
              {wallet.pointsCents} 分等价 · {wallet.userId}
              {wallet.hasSpendablePoints ? " · 可抵扣" : " · 无可用积分"}
            </span>
          </div>
        </div>
      ) : null}
    </>
  );
}
