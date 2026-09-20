"use client";

/**
 * IoT 遥测入影客户端岛 — 默认 BAT-IOT-1 / vendorA；展示 soc / stale。
 */

import { FormEvent, useState } from "react";
import type { DeviceShadowView } from "@/domains/iot/domain/device-shadow-view";
import {
  DEFAULT_IOT_BATTERY,
  DEFAULT_TELEMETRY_SOC,
  DEFAULT_TELEMETRY_VENDOR,
  DEFAULT_TELEMETRY_VOLTAGE_MILLI,
  runPostTelemetry,
} from "@/domains/iot/application/run-post-telemetry";
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
  const [view, setView] = useState<DeviceShadowView | null>(null);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setView(null);
    try {
      setView(
        await runPostTelemetry(batteryId.trim() || DEFAULT_IOT_BATTERY, {
          vendorId: vendorId.trim() || DEFAULT_TELEMETRY_VENDOR,
          soc: Number(soc) || DEFAULT_TELEMETRY_SOC,
          voltageMilli: Number(voltageMilli) || DEFAULT_TELEMETRY_VOLTAGE_MILLI,
        }),
      );
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

      {view ? <ShadowResultView view={view} /> : null}
    </section>
  );
}

function ShadowResultView({ view }: { view: DeviceShadowView }) {
  return (
    <dl className={styles.dl}>
      <dt>电池</dt>
      <dd>{view.batteryId}</dd>

      <dt>soc</dt>
      <dd>{view.soc}%</dd>

      <dt>voltageMilli</dt>
      <dd>{view.voltageMilli}</dd>

      <dt>影子</dt>
      <dd>
        <span
          className={`${styles.badge} ${
            view.stale ? styles.badgeStale : styles.badgeFresh
          }`}
        >
          {view.freshnessLabel}
        </span>
      </dd>

      {view.statusLabel ? (
        <>
          <dt>业务态</dt>
          <dd>
            {view.statusLabel}
            {view.lockStateLabel ? ` · ${view.lockStateLabel}` : ""}
          </dd>
        </>
      ) : null}

      <dt>计量换电</dt>
      <dd>{view.meteredSwapAllowed ? "允许" : "禁止"}</dd>

      {view.blockMessage ? (
        <>
          <dt>说明</dt>
          <dd>{view.blockMessage}</dd>
        </>
      ) : null}

      {view.lastSeenAt ? (
        <>
          <dt>lastSeenAt</dt>
          <dd>{view.lastSeenAt}</dd>
        </>
      ) : null}
    </dl>
  );
}
