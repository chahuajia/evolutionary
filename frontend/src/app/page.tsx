/**
 * 换电首页 — 履约工作台（站点 / 权益 / 计量分段）。
 */

import { PageHeader } from "@/components/page-header";
import { loadStationSummaries } from "@/domains/swap/application/load-station-summaries";
import {
  loadSwapLogs,
  type SwapLog,
} from "@/domains/swap/application/load-swap-logs";
import type { StationView } from "@/domains/swap/domain/station-view";
import { HomeWorkflows } from "./home-workflows";
import styles from "./page.module.css";

export default async function Home() {
  let stations: readonly StationView[] = [];
  let listError: string | null = null;

  try {
    stations = await loadStationSummaries();
  } catch (e) {
    listError =
      e instanceof Error
        ? e.message
        : "站列表拉取失败：请确认 Spring 已启动 :8080。";
  }

  const initialStationId = stations[0]?.id ?? "S1";

  let swapLogs: readonly SwapLog[] = [];
  let logsError: string | null = null;

  try {
    swapLogs = await loadSwapLogs(initialStationId);
  } catch (e) {
    logsError =
      e instanceof Error
        ? e.message
        : "换电日志拉取失败：请确认 Spring 已启动 :8080。";
  }

  const recentLogs = swapLogs.slice(-3);

  return (
    <>
      <PageHeader
        eyebrow="消费者 · 履约"
        title="换电履约"
        description="先选站完成基础换电，再用权益 / 默认选卡 / 计量路径覆盖信用购后的履约场景。"
      />
      {logsError ? (
        <p className={styles.logsAlert} role="alert">
          {logsError}
        </p>
      ) : (
        <section className={styles.logsOverview} aria-label="换电日志概览">
          <p className={styles.note}>
            站 {initialStationId} · {swapLogs.length} 条换电日志（只读 · RSC）
          </p>
          {recentLogs.length > 0 ? (
            <ul className={styles.logsPreview}>
              {recentLogs.map((log) => (
                <li key={log.id}>
                  {log.id} · {log.outgoingBatteryId} → {log.incomingBatteryId}{" "}
                  · {log.occurredAt}
                </li>
              ))}
            </ul>
          ) : (
            <p className={styles.note}>暂无换电日志</p>
          )}
        </section>
      )}
      <HomeWorkflows
        stations={stations}
        initialStationId={initialStationId}
        listError={listError}
      />
    </>
  );
}
