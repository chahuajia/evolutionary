/**
 * 用例：加载消费者钱包展示模型。
 */

import {
  DEFAULT_WALLET_USER,
  fetchUserWallet,
} from "@/domains/wallet/infrastructure/wallet-gateway";
import {
  toWalletView,
  type WalletView,
} from "@/domains/wallet/domain/wallet-view";

export async function loadWallet(
  userId: string = DEFAULT_WALLET_USER,
): Promise<WalletView> {
  const dto = await fetchUserWallet(userId);
  return toWalletView(dto);
}

export { DEFAULT_WALLET_USER };
