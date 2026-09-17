"use client";

/**
 * 客户端岛：表单 / 站详情 / 换电 POST。
 * 站列表由 RSC 首屏注入；刷新列表走 router.refresh()。
 */

import { FormEvent, useCallback, useState } from "react";
import { useRouter } from "next/navigation";
import type { StationSummary } from "@/domains/swap/infrastructure/station-gateway";
import { resolveApiBase } from "@/domains/swap/infrastructure/station-gateway";
import styles from "./page.module.css";

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
  const [error, setError] = useState<string | null>(listError);
  const [busy, setBusy] = useState(false);

  const refreshList = useCallback(() => {
    setError(null);
    router.refresh();
  }, [router]);

  const loadStation = useCallback(async () => {
    setBusy(true);
    setError(null);
    try {
      const res = await fetch(
        `${apiBase}/stations/${encodeURIComponent(stationId)}`,
      );
      const body = await res.json().catch(() => ({}));
      if (!res.ok) {
        throw new Error(
          (body as { error?: string }).error ?? `HTTP ${res.status}`,
        );
      }
      setStation(body as StationView);
    } catch (e) {
      setError(triageFetchError(e));
    } finally {
      setBusy(false);
    }
  }, [apiBase, stationId]);

  async function onSwap(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setLastSwap(null);
    try {
      const res = await fetch(
        `${apiBase}/stations/${encodeURIComponent(stationId)}/swaps`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ incomingBatteryId }),
        },
      );
      const body = await res.json().catch(() => ({}));
      if (!res.ok) {
        throw new Error(
          (body as { error?: string }).error ?? `HTTP ${res.status}`,
        );
      }
      const swap = body as SwapResult;
      setLastSwap(swap);
      const stationRes = await fetch(
        `${apiBase}/stations/${encodeURIComponent(stationId)}`,
      );
      if (stationRes.ok) {
        setStation((await stationRes.json()) as StationView);
      }
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
            {stations.map((s) => (
              <li key={s.id}>
                <button
                  type="button"
                  className={
                    s.id === stationId
                      ? styles.stationPickActive
                      : styles.stationPick
                  }
                  onClick={() => setStationId(s.id)}
                >
                  {s.name}（{s.id}）— {s.canSwapOut ? "可换出" : "不可换出"} ·
                  电池 {s.batteryCount}
                </button>
              </li>
            ))}
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
