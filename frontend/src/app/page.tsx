"use client";

/**
 * 界面状态：表单 + 列表 DTO + 上次响应。不是领域 Station 的移植。
 * 第 11 轮：站列表单次 GET /stations，禁止 per-id N+1。
 */

import { FormEvent, useCallback, useEffect, useState } from "react";
import styles from "./page.module.css";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? "/api";

type BatteryView = { id: string; status: string };
type StationView = { id: string; name: string; batteries: BatteryView[] };
type StationSummary = {
  id: string;
  name: string;
  canSwapOut: boolean;
  batteryCount: number;
};
type SwapResult = { stationId: string; outgoingId: string; incomingId: string };

type UiState = {
  stationId: string;
  incomingBatteryId: string;
  stationList: StationSummary[] | null;
  station: StationView | null;
  lastSwap: SwapResult | null;
  error: string | null;
  busy: boolean;
};

function triageFetchError(e: unknown): string {
  const msg = e instanceof Error ? e.message : String(e);
  if (msg === "Failed to fetch" || msg.includes("NetworkError")) {
    return "站列表拉取失败（W1）：请确认 Spring 已启动 :8080，且 Next rewrite /api 生效。";
  }
  return msg;
}

export default function Home() {
  const [ui, setUi] = useState<UiState>({
    stationId: "S1",
    incomingBatteryId: "B-user-1",
    stationList: null,
    station: null,
    lastSwap: null,
    error: null,
    busy: false,
  });

  const loadStationList = useCallback(async () => {
    setUi((s) => ({ ...s, busy: true, error: null }));
    try {
      const res = await fetch(`${API_BASE}/stations`);
      const body = await res.json().catch(() => ({}));
      if (!res.ok) {
        throw new Error(body.error ?? `HTTP ${res.status}`);
      }
      setUi((s) => ({
        ...s,
        stationList: body as StationSummary[],
        busy: false,
      }));
    } catch (e) {
      setUi((s) => ({
        ...s,
        busy: false,
        error: triageFetchError(e),
      }));
    }
  }, []);

  useEffect(() => {
    void loadStationList();
  }, [loadStationList]);

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
        error: triageFetchError(e),
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
      const stationRes = await fetch(
        `${API_BASE}/stations/${encodeURIComponent(ui.stationId)}`,
      );
      if (stationRes.ok) {
        const station = (await stationRes.json()) as StationView;
        setUi((s) => ({ ...s, station }));
      }
      await loadStationList();
    } catch (err) {
      setUi((s) => ({
        ...s,
        busy: false,
        error: triageFetchError(err),
      }));
    }
  }

  return (
    <main className={styles.main}>
      <h1 className={styles.title}>换电（压测 UI）</h1>
      <p className={styles.note}>
        站列表单次 REST 读取；详情/换电仍按站 ID。不移植领域聚合。
      </p>

      <section className={styles.panel}>
        <div className={styles.panelHead}>
          <h2>站点概览</h2>
          <button type="button" onClick={loadStationList} disabled={ui.busy}>
            刷新列表
          </button>
        </div>
        {ui.stationList && (
          <ul className={styles.stationList}>
            {ui.stationList.map((s) => (
              <li key={s.id}>
                <button
                  type="button"
                  className={
                    s.id === ui.stationId ? styles.stationPickActive : styles.stationPick
                  }
                  onClick={() => setUi((prev) => ({ ...prev, stationId: s.id }))}
                >
                  {s.name}（{s.id}）— {s.canSwapOut ? "可换出" : "不可换出"} · 电池{" "}
                  {s.batteryCount}
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
