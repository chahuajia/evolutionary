/**
 * 钱包 HTTP gateway — GET /commerce/users/{userId}/wallet
 */

import { apiBase } from "@/shared/http/api-base";
import { fetchJson } from "@/shared/http/fetch-json";

export const DEFAULT_WALLET_USER = "U1";

export type WalletDto = {
  userId: string;
  balanceCents: number;
  pointsCents: number;
  currency: string;
};

export async function fetchUserWallet(userId: string): Promise<WalletDto> {
  const base = apiBase();
  return fetchJson<WalletDto>(
    `${base}/commerce/users/${encodeURIComponent(userId)}/wallet`,
    { timeoutMs: 8000 },
  );
}
