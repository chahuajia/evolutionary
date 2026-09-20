"use client";

/**
 * 客户端岛：表单 / 站详情 / 换电 POST / 换电日志。
 * 站列表由 RSC 首屏注入；刷新列表走 router.refresh()。
 * 换电提交卡门：GET 站详情 canSwapOut ∧ 站内至少一块 swapOutAllowed。
 */

import { FormEvent, useCallback, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import type { StationView } from "@/domains/swap/domain/station-view";
import { stationNoSwapOutBatteryMessage } from "@/domains/swap/domain/station-view";
import type { BatteryView } from "@/domains/battery/domain/battery-view";
import {
  loadStationDetail,
  type StationDetailView,
} from "@/domains/swap/application/load-station-detail";
import {
  loadSwapLogs,
  type SwapLog,
} from "@/domains/swap/application/load-swap-logs";
import {
  runStationSwap,
  type StationSwapResult,
} from "@/domains/swap/application/run-station-swap";
import styles from "./page.module.css";

type Props = {
  stations: readonly StationView[];
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
  const [stationId, setStationId] = useState(initialStationId);
  const [incomingBatteryId, setIncomingBatteryId] = useState("B-user-1");
  const [detail, setDetail] = useState<StationDetailView | null>(null);
  const [lastSwap, setLastSwap] = useState<StationSwapResult | null>(null);
  const [swapLogs, setSwapLogs] = useState<readonly SwapLog[]>([]);
  const [error, setError] = useState<string | null>(listError);
  const [busy, setBusy] = useState(false);

  const batteryViews: readonly BatteryView[] | null = detail?.batteries ?? null;
  const stationDetailView = detail?.station ?? null;

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
        blockMessage: stationDetailView.blockMessage,
      };
    }
    const anyOut = batteryViews.some((b) => b.swapOutAllowed);
    if (!anyOut) {
      return {
        swapAllowed: false,
        blockMessage: stationNoSwapOutBatteryMessage(batteryViews),
      };
    }
    return { swapAllowed: true, blockMessage: null as string | null };
  }, [stationDetailView, batteryViews]);

  const refreshList = useCallback(() => {
    setError(null);
    router.refresh();
  }, [router]);

  const refreshLogs = useCallback(async (id: string) => {
    try {
      setSwapLogs(await loadSwapLogs(id));
    } catch {
      /* 日志失败不阻断主流程 */
    }
  }, []);

  const loadStation = useCallback(async () => {
    setBusy(true);
    setError(null);
    try {
      setDetail(await loadStationDetail(stationId));
      await refreshLogs(stationId);
    } catch (e) {
      setError(triageFetchError(e));
    } finally {
      setBusy(false);
    }
  }, [stationId, refreshLogs]);

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
      const swap = await runStationSwap({
        stationId,
        incomingBatteryId,
      });
      setLastSwap(swap);
      try {
        setDetail(await loadStationDetail(stationId));
      } catch {
        /* 换电已成功；详情刷新失败不阻断，列表仍 refresh */
      }
      await refreshLogs(stationId);
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
            {stations.map((view) => (
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
                    setDetail(null);
                  }}
                >
                  {view.name}（{view.id}）— {view.availabilityLabel} · 电池{" "}
                  {view.batteryCount}
                </button>
              </li>
            ))}
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
          stationId
          <input
            value={stationId}
            onChange={(e) => {
              setStationId(e.target.value);
              setDetail(null);
            }}
          />
        </label>
        <label>
          incomingBatteryId
          <input
            value={incomingBatteryId}
            onChange={(e) => setIncomingBatteryId(e.target.value)}
          />
        </label>
        <div className={styles.formActions}>
          <button type="button" onClick={loadStation} disabled={busy}>
            {busy ? "加载中…" : "刷新站点"}
          </button>
          <button type="submit" disabled={busy || !swapGate.swapAllowed}>
            {busy ? "换电中…" : "换电"}
          </button>
        </div>
      </form>

      {!swapGate.swapAllowed && swapGate.blockMessage ? (
        <p className={styles.note} role="status">
          {swapGate.blockMessage}
        </p>
      ) : null}

      {error ? (
        <p className={styles.note} role="alert">
          {error}
        </p>
      ) : null}

      {lastSwap ? (
        <section className={styles.panel}>
          <h2>最近换电</h2>
          <dl className={styles.dl}>
            <dt>站</dt>
            <dd>{lastSwap.stationId}</dd>
            <dt>换出</dt>
            <dd>{lastSwap.outgoingId}</dd>
            <dt>换入</dt>
            <dd>{lastSwap.incomingId}</dd>
          </dl>
        </section>
      ) : null}

      {batteryViews ? (
        <section className={styles.panel}>
          <h2>站内电池</h2>
          <ul className={styles.list}>
            {batteryViews.map((b) => (
              <li key={b.id}>
                {b.id} · {b.statusLabel}
                {b.swapOutAllowed ? " · 可换出" : b.blockMessage ? ` · ${b.blockMessage}` : ""}
              </li>
            ))}
          </ul>
        </section>
      ) : null}

      {swapLogs.length > 0 ? (
        <section className={styles.panel}>
          <h2>换电日志</h2>
          <ul className={styles.list}>
            {swapLogs.map((log) => (
              <li key={log.id}>
                {log.occurredAt} · 出 {log.outgoingBatteryId} / 入{" "}
                {log.incomingBatteryId}
              </li>
            ))}
          </ul>
        </section>
      ) : null}
    </>
  );
}
