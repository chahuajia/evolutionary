"use client";

/**
 * 客户端岛：表单 / 站详情 / 换电 POST / 换电日志。
 * 站列表由 RSC 首屏注入；刷新列表走 router.refresh()。
 * 换电提交卡门：GET 站详情 canSwapOut ∧ 站内至少一块 swapOutAllowed。
 */

import { FormEvent, useCallback, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import type {
  StationSummary,
  SwapLog,
} from "@/domains/swap/infrastructure/station-gateway";
import { toStationView } from "@/domains/swap/domain/station-view";
import {
  parseBatteryStatus,
  toBatteryView,
  type BatteryView,
} from "@/domains/battery/domain/battery-view";
import {
  fetchSwapLogs,
  resolveApiBase,
} from "@/domains/swap/infrastructure/station-gateway";
import { fetchJson } from "@/shared/http/fetch-json";
import styles from "./page.module.css";

const TIMEOUT_MS = 8000;

type StationBatteryDto = { id: string; status: string };
type StationDetailDto = {
  id: string;
  name: string;
  canSwapOut: boolean;
  batteries: StationBatteryDto[];
};
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
  const [station, setStation] = useState<StationDetailDto | null>(null);
  const [lastSwap, setLastSwap] = useState<SwapResult | null>(null);
  const [swapLogs, setSwapLogs] = useState<readonly SwapLog[]>([]);
  const [error, setError] = useState<string | null>(listError);
  const [busy, setBusy] = useState(false);

  const batteryViews: BatteryView[] | null = useMemo(() => {
    if (!station) return null;
    return station.batteries.map((b) =>
      toBatteryView({
        id: b.id,
        status: parseBatteryStatus(b.status),
      }),
    );
  }, [station]);

  const stationDetailView = useMemo(() => {
    if (!station) return null;
    return toStationView({
      id: station.id,
      name: station.name,
      canSwapOut: Boolean(station.canSwapOut),
      batteryCount: station.batteries.length,
    });
  }, [station]);

  const swapGate = useMemo(() => {
    if (!stationDetailView || !batteryViews) {
      return {
        swapAllowed: false,
        blockMessage: "请先加载站详情",
      };
    }
    if (!stationDetailView.selectable) {
      return {
        swapAllowed: false,
        blockMessage: `${stationDetailView.name} 不可换出，请另选站点`,
      };
    }
    const anyOut = batteryViews.some((b) => b.swapOutAllowed);
    if (!anyOut) {
      return {
        swapAllowed: false,
        blockMessage:
          batteryViews.find((b) => b.blockMessage)?.blockMessage ??
          "站内无 AVAILABLE 电池可换出",
      };
    }
    return { swapAllowed: true, blockMessage: null as string | null };
  }, [stationDetailView, batteryViews]);

  const refreshList = useCallback(() => {
    setError(null);
    router.refresh();
  }, [router]);

  const loadSwapLogs = useCallback(async (id: string) => {
    try {
      setSwapLogs(await fetchSwapLogs(id));
    } catch {
      /* 日志失败不阻断主流程 */
    }
  }, []);

  const loadStation = useCallback(async () => {
    setBusy(true);
    setError(null);
    try {
      const body = await fetchJson<StationDetailDto>(
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
    if (!swapGate.swapAllowed) {
      setError(swapGate.blockMessage ?? "不可换电");
      return;
    }
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
        const next = await fetchJson<StationDetailDto>(
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
                    disabled={!view.selectable}
                    className={
                      view.id === stationId
                        ? styles.stationPickActive
                        : styles.stationPick
                    }
                    onClick={() => {
                      setStationId(view.id);
                      setStation(null);
                    }}
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
        <p className={styles.note}>
          GET 站详情对齐 canSwapOut；站内电池对齐 swapOutAllowed
          {stationDetailView
            ? ` · ${stationDetailView.id}=${stationDetailView.availabilityLabel}`
            : " · 先「刷新站点」拉详情门"}
          {batteryViews
            ? ` · 可换出 ${batteryViews.filter((b) => b.swapOutAllowed).length}/${batteryViews.length}`
            : ""}
        </p>
        <label>
          站点 ID
          <input
            value={stationId}
            onChange={(e) => {
              setStationId(e.target.value);
              setStation(null);
            }}
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
          <button type="submit" disabled={busy || !swapGate.swapAllowed}>
            换电
          </button>
        </div>
      </form>

      {!swapGate.swapAllowed && swapGate.blockMessage ? (
        <p className={styles.note} role="status">
          {swapGate.blockMessage}
        </p>
      ) : null}

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

      {station && batteryViews && (
        <section className={styles.panel}>
          <h2>
            {station.name}（{station.id}）
          </h2>
          <ul>
            {batteryViews.map((bat) => (
              <li key={bat.id}>
                {bat.id} — {bat.statusLabel}
                {bat.swapOutAllowed ? " · 可换出" : ""}
                {bat.blockMessage ? ` · ${bat.blockMessage}` : ""}
              </li>
            ))}
          </ul>
        </section>
      )}
    </>
  );
}
