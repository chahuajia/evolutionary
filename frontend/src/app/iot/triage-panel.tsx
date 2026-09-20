"use client";

/**
 * IoT SOC 过时诊断客户端岛 — 默认 BAT-IOT-1；展示 nextStep / orderedChecks / shadow。
 * GET shadow 预读当前新鲜度（厚 GET）；POST triage 出下一步。
 */

import { FormEvent, useEffect, useState } from "react";
import type { DeviceShadowView } from "@/domains/iot/domain/device-shadow-view";
import {
  DEFAULT_IOT_BATTERY,
  loadDeviceShadow,
} from "@/domains/iot/application/load-device-shadow";
import {
  runTriageOutdatedSoc,
  type TriageOutdatedSocView,
} from "@/domains/iot/application/run-triage-outdated-soc";
import styles from "./page.module.css";

export function TriagePanel() {
  const [batteryId, setBatteryId] = useState(DEFAULT_IOT_BATTERY);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [view, setView] = useState<TriageOutdatedSocView | null>(null);
  const [preload, setPreload] = useState<DeviceShadowView | null>(null);
  const [preloadError, setPreloadError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    const id = batteryId.trim() || DEFAULT_IOT_BATTERY;
    setPreloadError(null);
    setView(null);
    loadDeviceShadow(id)
      .then((next) => {
        if (cancelled) return;
        setPreload(next);
      })
      .catch((err) => {
        if (cancelled) return;
        setPreload(null);
        setPreloadError(err instanceof Error ? err.message : String(err));
      });
    return () => {
      cancelled = true;
    };
  }, [batteryId]);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setView(null);
    try {
      const id = batteryId.trim() || DEFAULT_IOT_BATTERY;
      const next = await runTriageOutdatedSoc(id);
      setView(next);
      setPreload(next.shadow);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>SOC 过时诊断（HTTP）</h2>
      <p className={styles.note}>
        GET shadow 预读；stale 时下一步多为「影子已过期」
        {preload
          ? ` · ${preload.batteryId}=${preload.freshnessLabel}`
          : preloadError
            ? ` · ${preloadError}`
            : " · 加载中…"}
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
          {shadow.freshnessLabel}
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
