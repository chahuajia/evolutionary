/**
 * 换电首页 — RSC 首屏拉站列表；表单/详情/换电为客户端岛。
 * 权益换电岛自包含，RSC 无需传参。
 */

import Link from "next/link";
import {
  fetchStationSummaries,
  type StationSummary,
} from "@/domains/swap/infrastructure/station-gateway";
import { EntitledSwapPanel } from "./entitled-swap-panel";
import { MeteredSwapPanel } from "./metered-swap-panel";
import { SwapPanel } from "./swap-panel";
import styles from "./page.module.css";

export default async function Home() {
  let stations: readonly StationSummary[] = [];
  let listError: string | null = null;

  try {
    stations = await fetchStationSummaries();
  } catch (e) {
    listError =
      e instanceof Error
        ? e.message
        : "站列表拉取失败（W1）：请确认 Spring 已启动 :8080。";
  }

  const initialStationId = stations[0]?.id ?? "S1";

  return (
    <main className={styles.main}>
      <h1 className={styles.title}>换电（压测 UI）</h1>
      <p className={styles.note}>
        站列表 RSC 单次 REST 读取；详情/换电仍按站 ID（客户端岛）。不移植领域聚合。{" "}
        <Link href="/credit">信用账单</Link>
      </p>

      <SwapPanel
        stations={stations}
        initialStationId={initialStationId}
        listError={listError}
      />

      <EntitledSwapPanel />
      <MeteredSwapPanel />
    </main>
  );
}
