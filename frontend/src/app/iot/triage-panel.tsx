"use client";

/**
 * IoT SOC 过时诊断客户端岛 — 默认 BAT-IOT-1；展示 nextStep / orderedChecks / shadow。
 */

import { FormEvent, useState } from "react";
import {
  DEFAULT_IOT_BATTERY,
  runTriageOutdatedSoc,
  type TriageOutdatedSocView,
} from "@/domains/iot/application/run-triage-outdated-soc";
import styles from "./page.module.css";

export function TriagePanel() {
  const [batteryId, setBatteryId] = useState(DEFAULT_IOT_BATTERY);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [view, setView] = useState<TriageOutdatedSocView | null>(null);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setView(null);
    try {
      setView(
        await runTriageOutdatedSoc(batteryId.trim() || DEFAULT_IOT_BATTERY),
      );
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

      {view ? <TriageResultView view={view} /> : null}
    </section>
  );
}

function TriageResultView({ view }: { view: TriageOutdatedSocView }) {
  const { shadow } = view;
  const nextStepClass =
    view.nextStepBadgeTone === "stale"
      ? styles.badgeStale
      : styles.badgeFresh;

  return (
    <dl className={styles.dl}>
      <dt>电池</dt>
      <dd>{view.batteryId}</dd>

      <dt>下一步</dt>
      <dd>
        <span className={`${styles.badge} ${nextStepClass}`}>
          {view.nextStepLabel}
        </span>
      </dd>

      <dt>orderedChecks</dt>
      <dd>
        {view.orderedChecks.length > 0 ? (
          <ol>
            {view.orderedChecks.map((check, i) => (
              <li key={`${i}-${check}`}>{check}</li>
            ))}
          </ol>
        ) : (
          "无"
        )}
      </dd>

      <dt>shadow.soc</dt>
      <dd>{shadow.soc}%</dd>

      <dt>shadow</dt>
      <dd>
        <span
          className={`${styles.badge} ${
            shadow.stale ? styles.badgeStale : styles.badgeFresh
          }`}
        >
          {shadow.fresh ? "fresh" : "stale"}
        </span>
        {shadow.meteredSwapAllowed ? " · 可计量换电" : " · 禁计量换电"}
        {shadow.statusLabel ? ` · ${shadow.statusLabel}` : ""}
        {shadow.lockStateLabel ? ` · ${shadow.lockStateLabel}` : ""}
      </dd>

      {shadow.blockMessage ? (
        <>
          <dt>说明</dt>
          <dd>{shadow.blockMessage}</dd>
        </>
      ) : null}

      {shadow.lastSeenAt ? (
        <>
          <dt>shadow.lastSeenAt</dt>
          <dd>{shadow.lastSeenAt}</dd>
        </>
      ) : null}
    </dl>
  );
}
