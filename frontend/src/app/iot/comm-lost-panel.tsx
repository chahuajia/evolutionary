"use client";

/**
 * IoT COMM_LOST 诊断客户端岛 — 默认 BAT-IOT-1；展示 stale / COMM_LOST / ticket。
 */

import { FormEvent, useState } from "react";
import { canDetectCommLost } from "@/domains/iot/domain/device-shadow-view";
import {
  DEFAULT_IOT_BATTERY,
  postDetectCommLost,
  type DetectCommLostResult,
} from "@/domains/iot/infrastructure/iot-gateway";
import styles from "./page.module.css";

export function CommLostPanel() {
  const [batteryId, setBatteryId] = useState(DEFAULT_IOT_BATTERY);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<DetectCommLostResult | null>(null);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const r = await postDetectCommLost(
        batteryId.trim() || DEFAULT_IOT_BATTERY,
      );
      setResult(r);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>通信丢失检测（HTTP）</h2>
      <p className={styles.note}>
        对齐 DetectCommLost：仅影子 stale 时抬 COMM_LOST；新鲜影子检测不会抬告警。
      </p>
      <form className={styles.form} onSubmit={onSubmit}>
        <label>
          batteryId
          <input
            value={batteryId}
            onChange={(e) => setBatteryId(e.target.value)}
          />
        </label>
        <button type="submit" disabled={busy}>
          {busy ? "检测中…" : "检测 COMM_LOST"}
        </button>
      </form>

      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}

      {result ? <DetectResultView result={result} /> : null}
    </section>
  );
}

function DetectResultView({ result }: { result: DetectCommLostResult }) {
  const alertLabel =
    result.alertType ?? (result.raised ? "COMM_LOST" : null);
  const detectUseful = canDetectCommLost(result.stale);

  return (
    <dl className={styles.dl}>
      <dt>电池</dt>
      <dd>{result.batteryId}</dd>

      <dt>stale</dt>
      <dd>
        <span
          className={`${styles.badge} ${
            result.stale ? styles.badgeStale : styles.badgeFresh
          }`}
        >
          {result.stale ? "stale" : "fresh"}
        </span>
        {detectUseful ? " · 适合检测" : " · 新鲜，不会抬告警"}
      </dd>

      <dt>告警</dt>
      <dd>
        {alertLabel ? (
          <span className={`${styles.badge} ${styles.badgeLost}`}>
            {alertLabel}
          </span>
        ) : (
          "未触发"
        )}
        {result.raised ? " · raised" : ""}
      </dd>

      <dt>ticket</dt>
      <dd>{result.ticketId ?? "无"}</dd>
    </dl>
  );
}
