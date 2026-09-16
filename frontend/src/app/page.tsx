"use client";

/**
 * 界面状态：表单 + 上次响应。不是领域 Station / Battery 的移植。
 * 见项目 AGENTS.md 局部约定 #3；暴露点 F2。
 */

import { FormEvent, useCallback, useState } from "react";
import styles from "./page.module.css";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? "/api";

type BatteryView = { id: string; status: string };
type StationView = { id: string; name: string; batteries: BatteryView[] };
type SwapResult = { stationId: string; outgoingId: string; incomingId: string };

type UiState = {
  stationId: string;
  incomingBatteryId: string;
  station: StationView | null;
  lastSwap: SwapResult | null;
  error: string | null;
  busy: boolean;
};

export default function Home() {
  const [ui, setUi] = useState<UiState>({
    stationId: "S1",
    incomingBatteryId: "B-user-1",
    station: null,
    lastSwap: null,
    error: null,
    busy: false,
  });

  const loadStation = useCallback(async () => {
    setUi((s) => ({ ...s, busy: true, error: null }));
    try {
      const res = await fetch(`${API_BASE}/stations/${encodeURIComponent(ui.stationId)}`);
      const body = await res.json().catch(() => ({}));
      if (!res.ok) {
        throw new Error(body.error ?? `HTTP ${res.status}`);
      }
      setUi((s) => ({ ...s, station: body as StationView, busy: false }));
    } catch (e) {
      setUi((s) => ({
        ...s,
        busy: false,
        error: e instanceof Error ? e.message : String(e),
      }));
    }
  }, [ui.stationId]);

  async function onSwap(e: FormEvent) {
    e.preventDefault();
    setUi((s) => ({ ...s, busy: true, error: null, lastSwap: null }));
    try {
      const res = await fetch(
        `${API_BASE}/stations/${encodeURIComponent(ui.stationId)}/swaps`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ incomingBatteryId: ui.incomingBatteryId }),
        },
      );
      const body = await res.json().catch(() => ({}));
      if (!res.ok) {
        throw new Error(body.error ?? `HTTP ${res.status}`);
      }
      const swap = body as SwapResult;
      setUi((s) => ({ ...s, lastSwap: swap, busy: false }));
      // 刷新视图状态（仍是 DTO，不是聚合）
      const stationRes = await fetch(
        `${API_BASE}/stations/${encodeURIComponent(ui.stationId)}`,
      );
      if (stationRes.ok) {
        const station = (await stationRes.json()) as StationView;
        setUi((s) => ({ ...s, station }));
      }
    } catch (err) {
      setUi((s) => ({
        ...s,
        busy: false,
        error: err instanceof Error ? err.message : String(err),
      }));
    }
  }

  return (
    <main className={styles.main}>
      <h1 className={styles.title}>换电（压测 UI）</h1>
      <p className={styles.note}>
        调用后端 REST；本页只持有表单与展示状态，不移植领域聚合。
      </p>

      <form className={styles.form} onSubmit={onSwap}>
        <label>
          站点 ID
          <input
            value={ui.stationId}
            onChange={(e) => setUi((s) => ({ ...s, stationId: e.target.value }))}
          />
        </label>
        <label>
          归还电池 ID
          <input
            value={ui.incomingBatteryId}
            onChange={(e) =>
              setUi((s) => ({ ...s, incomingBatteryId: e.target.value }))
            }
          />
        </label>
        <div className={styles.actions}>
          <button type="button" onClick={loadStation} disabled={ui.busy}>
            刷新站点
          </button>
          <button type="submit" disabled={ui.busy}>
            换电
          </button>
        </div>
      </form>

      {ui.error && <p className={styles.error}>{ui.error}</p>}

      {ui.lastSwap && (
        <section className={styles.panel}>
          <h2>上次换电</h2>
          <p>
            站 {ui.lastSwap.stationId}：取出 {ui.lastSwap.outgoingId}，放入{" "}
            {ui.lastSwap.incomingId}
          </p>
        </section>
      )}

      {ui.station && (
        <section className={styles.panel}>
          <h2>
            {ui.station.name}（{ui.station.id}）
          </h2>
          <ul>
            {ui.station.batteries.map((b) => (
              <li key={b.id}>
                {b.id} — {b.status}
              </li>
            ))}
          </ul>
        </section>
      )}
    </main>
  );
}
