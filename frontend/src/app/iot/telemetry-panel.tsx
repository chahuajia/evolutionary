"use client";

/**
 * IoT 遥测入影客户端岛 — 默认 BAT-IOT-1 / vendorA；展示 soc / stale。
 */

import { FormEvent, useState } from "react";
import {
  DEFAULT_IOT_BATTERY,
  DEFAULT_TELEMETRY_SOC,
  DEFAULT_TELEMETRY_VENDOR,
  DEFAULT_TELEMETRY_VOLTAGE_MILLI,
  postTelemetry,
  type TelemetryResult,
} from "@/domains/iot/infrastructure/iot-gateway";
import styles from "./page.module.css";

export function TelemetryPanel() {
  const [batteryId, setBatteryId] = useState(DEFAULT_IOT_BATTERY);
  const [vendorId, setVendorId] = useState(DEFAULT_TELEMETRY_VENDOR);
  const [soc, setSoc] = useState(String(DEFAULT_TELEMETRY_SOC));
  const [voltageMilli, setVoltageMilli] = useState(
    String(DEFAULT_TELEMETRY_VOLTAGE_MILLI),
  );
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<TelemetryResult | null>(null);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const id = batteryId.trim() || DEFAULT_IOT_BATTERY;
      const r = await postTelemetry(id, {
        vendorId: vendorId.trim() || DEFAULT_TELEMETRY_VENDOR,
        soc: Number(soc) || DEFAULT_TELEMETRY_SOC,
        voltageMilli: Number(voltageMilli) || DEFAULT_TELEMETRY_VOLTAGE_MILLI,
      });
      setResult(r);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>遥测入影（HTTP）</h2>
      <form className={styles.form} onSubmit={onSubmit}>
        <label>
          batteryId
          <input
            value={batteryId}
            onChange={(e) => setBatteryId(e.target.value)}
          />
        </label>
        <label>
          vendorId
          <input
            value={vendorId}
            onChange={(e) => setVendorId(e.target.value)}
          />
        </label>
        <label>
          soc
          <input
            type="number"
            value={soc}
            onChange={(e) => setSoc(e.target.value)}
          />
        </label>
        <label>
          voltageMilli
          <input
            type="number"
            value={voltageMilli}
            onChange={(e) => setVoltageMilli(e.target.value)}
          />
        </label>
        <button type="submit" disabled={busy}>
          {busy ? "提交中…" : "提交遥测"}
        </button>
      </form>

      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}

      {result ? <TelemetryResultView result={result} /> : null}
    </section>
  );
}

function TelemetryResultView({ result }: { result: TelemetryResult }) {
  return (
    <dl className={styles.dl}>
      <dt>电池</dt>
      <dd>{result.batteryId}</dd>

      <dt>soc</dt>
      <dd>{result.soc}%</dd>

      <dt>voltageMilli</dt>
      <dd>{result.voltageMilli}</dd>

      <dt>stale</dt>
      <dd>
        <span
          className={`${styles.badge} ${
            result.stale ? styles.badgeStale : styles.badgeFresh
          }`}
        >
          {result.stale ? "stale" : "fresh"}
        </span>
      </dd>

      {result.lastSeenAt ? (
        <>
          <dt>lastSeenAt</dt>
          <dd>{result.lastSeenAt}</dd>
        </>
      ) : null}
    </dl>
  );
}
