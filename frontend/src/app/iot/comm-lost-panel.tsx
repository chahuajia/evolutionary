"use client";

/**
 * IoT COMM_LOST 诊断客户端岛 — 默认 BAT-IOT-1；展示 stale / COMM_LOST / ticket。
 */

import { FormEvent, useState } from "react";
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
      const r = await postDetectCommLost(batteryId.trim() || DEFAULT_IOT_BATTERY);
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
  const alertType = result.alert?.alertType ?? (result.raised ? "COMM_LOST" : "—");
  const ticket = result.ticket;

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
        {result.shadow.lastSeenAt
          ? ` · lastSeen ${result.shadow.lastSeenAt}`
          : ""}
      </dd>

      <dt>告警</dt>
      <dd>
        {result.raised || result.alert ? (
          <span className={`${styles.badge} ${styles.badgeLost}`}>
            {alertType}
          </span>
        ) : (
          "未触发"
        )}
        {result.alert?.severity ? ` · ${result.alert.severity}` : ""}
        {result.alert?.raisedAt ? ` · ${result.alert.raisedAt}` : ""}
      </dd>

      <dt>ticket</dt>
      <dd>
        {ticket ? (
          <>
            {ticket.id} · {ticket.status} · {ticket.alertType}
            {ticket.createdAt ? ` · ${ticket.createdAt}` : ""}
          </>
        ) : (
          "无"
        )}
      </dd>
    </dl>
  );
}
