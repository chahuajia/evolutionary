"use client";

/**
 * IoT SOC 过时诊断客户端岛 — 默认 BAT-IOT-1；展示 nextStep / orderedChecks / shadow。
 */

import { FormEvent, useState } from "react";
import {
  DEFAULT_IOT_BATTERY,
  postTriageOutdatedSoc,
  type TriageOutdatedSocResult,
} from "@/domains/iot/infrastructure/iot-gateway";
import styles from "./page.module.css";

export function TriagePanel() {
  const [batteryId, setBatteryId] = useState(DEFAULT_IOT_BATTERY);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<TriageOutdatedSocResult | null>(null);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const r = await postTriageOutdatedSoc(
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
      <h2>SOC 过时诊断（HTTP）</h2>
      <form className={styles.form} onSubmit={onSubmit}>
        <label>
          batteryId
          <input
            value={batteryId}
            onChange={(e) => setBatteryId(e.target.value)}
          />
        </label>
        <button type="submit" disabled={busy}>
          {busy ? "诊断中…" : "诊断 SOC 过时"}
        </button>
      </form>

      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}

      {result ? <TriageResultView result={result} /> : null}
    </section>
  );
}

function TriageResultView({ result }: { result: TriageOutdatedSocResult }) {
  const { shadow } = result;
  const nextStepClass =
    result.nextStep === "SHADOW_STALE"
      ? styles.badgeStale
      : result.nextStep === "CHECK_ADAPTER"
        ? styles.badgeFresh
        : styles.badgeLost;

  return (
    <dl className={styles.dl}>
      <dt>电池</dt>
      <dd>{result.batteryId}</dd>

      <dt>nextStep</dt>
      <dd>
        <span className={`${styles.badge} ${nextStepClass}`}>
          {result.nextStep || "—"}
        </span>
      </dd>

      <dt>orderedChecks</dt>
      <dd>
        {result.orderedChecks.length > 0 ? (
          <ol>
            {result.orderedChecks.map((check, i) => (
              <li key={`${i}-${check}`}>{check}</li>
            ))}
          </ol>
        ) : (
          "无"
        )}
      </dd>

      <dt>shadow.soc</dt>
      <dd>{shadow.soc}%</dd>

      <dt>shadow.stale</dt>
      <dd>
        <span
          className={`${styles.badge} ${
            shadow.stale ? styles.badgeStale : styles.badgeFresh
          }`}
        >
          {shadow.stale ? "stale" : "fresh"}
        </span>
      </dd>

      {shadow.lastSeenAt ? (
        <>
          <dt>shadow.lastSeenAt</dt>
          <dd>{shadow.lastSeenAt}</dd>
        </>
      ) : null}
    </dl>
  );
}
