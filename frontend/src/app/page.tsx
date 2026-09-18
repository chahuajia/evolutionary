/**
 * 换电首页 — 履约工作台（站点 / 权益 / 计量分段）。
 */

import { PageHeader } from "@/components/page-header";
import {
  fetchStationSummaries,
  type StationSummary,
} from "@/domains/swap/infrastructure/station-gateway";
import { HomeWorkflows } from "./home-workflows";

export default async function Home() {
  let stations: readonly StationSummary[] = [];
  let listError: string | null = null;

  try {
    stations = await fetchStationSummaries();
  } catch (e) {
    listError =
      e instanceof Error
        ? e.message
        : "站列表拉取失败：请确认 Spring 已启动 :8080。";
  }

  const initialStationId = stations[0]?.id ?? "S1";

  return (
    <>
      <PageHeader
        eyebrow="消费者 · 履约"
        title="换电履约"
        description="先选站完成基础换电，再用权益 / 默认选卡 / 计量路径覆盖信用购后的履约场景。"
      />
      <HomeWorkflows
        stations={stations}
        initialStationId={initialStationId}
        listError={listError}
      />
    </>
  );
}
