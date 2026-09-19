"use client";

/**
 * 客户端岛：表单 / 站详情 / 换电 POST / 换电日志。
 * 站列表由 RSC 首屏注入；刷新列表走 router.refresh()。
 */

import { FormEvent, useCallback, useState } from "react";
import { useRouter } from "next/navigation";
import type {
  StationSummary,
  SwapLog,
} from "@/domains/swap/infrastructure/station-gateway";
import { toStationView } from "@/domains/swap/domain/station-view";
import {
  fetchSwapLogs,
  resolveApiBase,
} from "@/domains/swap/infrastructure/station-gateway";
import { fetchJson } from "@/shared/http/fetch-json";
import styles from "./page.module.css";

const TIMEOUT_MS = 8000;

type BatteryView = { id: string; status: string };
type StationView = { id: string; name: string; batteries: BatteryView[] };
type SwapResult = { stationId: string; outgoingId: string; incomingId: string };

type Props = {
  stations: readonly StationSummary[];
  initialStationId: string;
  listError: string | null;
};

function triageFetchError(e: unknown): string {
  const msg = e instanceof Error ? e.message : String(e);
  if (msg === "Failed to fetch" || msg.includes("NetworkError")) {
    return "请求失败（W1）：请确认 Spring 已启动 :8080，且 Next rewrite /api 生效。";
  }
  return msg;
}

export function SwapPanel({ stations, initialStationId, listError }: Props) {
  const router = useRouter();
  const apiBase = resolveApiBase();
  const [stationId, setStationId] = useState(initialStationId);
  const [incomingBatteryId, setIncomingBatteryId] = useState("B-user-1");
  const [station, setStation] = useState<StationView | null>(null);
  const [lastSwap, setLastSwap] = useState<SwapResult | null>(null);
  const [swapLogs, setSwapLogs] = useState<readonly SwapLog[]>([]);
  const [error, setError] = useState<string | null>(listError);
  const [busy, setBusy] = useState(false);

  const refreshList = useCallback(() => {
    setError(null);
    router.refresh();
  }, [router]);

  const loadSwapLogs = useCallback(
    async (id: string) => {
      try {
        setSwapLogs(await fetchSwapLogs(id));
      } catch {
        /* 日志失败不阻断主流程 */
      }
    },
    [],
  );

  const loadStation = useCallback(async () => {
    setBusy(true);
    setError(null);
    try {
      const body = await fetchJson<StationView>(
        `${apiBase}/stations/${encodeURIComponent(stationId)}`,
        { timeoutMs: TIMEOUT_MS },
      );
      setStation(body);
      await loadSwapLogs(stationId);
    } catch (e) {
      setError(triageFetchError(e));
    } finally {
      setBusy(false);
    }
  }, [apiBase, stationId, loadSwapLogs]);

  async function onSwap(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setLastSwap(null);
    try {
      const swap = await fetchJson<SwapResult>(
        `${apiBase}/stations/${encodeURIComponent(stationId)}/swaps`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ incomingBatteryId }),
          timeoutMs: TIMEOUT_MS,
        },
      );
      setLastSwap(swap);
      try {
        const next = await fetchJson<StationView>(
          `${apiBase}/stations/${encodeURIComponent(stationId)}`,
          { timeoutMs: TIMEOUT_MS },
        );
        setStation(next);
      } catch {
        /* 换电已成功；详情刷新失败不阻断，列表仍 refresh */
      }
      await loadSwapLogs(stationId);
      router.refresh();
    } catch (err) {
      setError(triageFetchError(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <>
      <section className={styles.panel}>
        <div className={styles.panelHead}>
          <h2>站点概览</h2>
          <button type="button" onClick={refreshList} disabled={busy}>
            刷新列表
          </button>
        </div>
        {stations.length > 0 && (
          <ul className={styles.stationList}>
            {stations.map((s) => {
              const view = toStationView(s);
              return (
                <li key={view.id}>
                  <button
                    type="button"
                    className={
                      view.id === stationId
                        ? styles.stationPickActive
                        : styles.stationPick
                    }
                    onClick={() => setStationId(view.id)}
                  >
                    {view.name}（{view.id}）— {view.availabilityLabel} · 电池{" "}
                    {view.batteryCount}
                  </button>
                </li>
              );
            })}
          </ul>
        )}
      </section>

      <form className={styles.form} onSubmit={onSwap}>
        <label>
          站点 ID
          <input
            value={stationId}
            onChange={(e) => setStationId(e.target.value)}
          />
        </label>
        <label>
          归还电池 ID
          <input
            value={incomingBatteryId}
            onChange={(e) => setIncomingBatteryId(e.target.value)}
          />
        </label>
        <div className={styles.actions}>
          <button type="button" onClick={loadStation} disabled={busy}>
            刷新站点
          </button>
          <button type="submit" disabled={busy}>
            换电
          </button>
        </div>
      </form>

      {error && <p className={styles.error}>{error}</p>}

      {lastSwap && (
        <section className={styles.panel}>
          <h2>上次换电</h2>
          <p>
            站 {lastSwap.stationId}：取出 {lastSwap.outgoingId}，放入{" "}
            {lastSwap.incomingId}
          </p>
        </section>
      )}

      {swapLogs.length > 0 && (
        <section className={styles.panel}>
          <h2>换电日志</h2>
          <ul>
            {swapLogs.map((log) => (
              <li key={log.id}>
                {log.occurredAt}：出 {log.outgoingBatteryId} → 入{" "}
                {log.incomingBatteryId}
              </li>
            ))}
          </ul>
        </section>
      )}

      {station && (
        <section className={styles.panel}>
          <h2>
            {station.name}（{station.id}）
          </h2>
          <ul>
            {station.batteries.map((b) => (
              <li key={b.id}>
                {b.id} — {b.status}
              </li>
            ))}
          </ul>
        </section>
      )}
    </>
  );
}
