"use client";

/**
 * IoT COMM_LOST 诊断客户端岛 — 默认 BAT-IOT-1；展示 stale / COMM_LOST / ticket。
 * GET shadow 对齐 commLostDetectUseful：仅 stale 时检测有意义。
 */

import { FormEvent, useEffect, useMemo, useState } from "react";
import type { DeviceShadowView } from "@/domains/iot/domain/device-shadow-view";
import {
  DEFAULT_IOT_BATTERY,
  loadDeviceShadow,
} from "@/domains/iot/application/load-device-shadow";
import {
  runDetectCommLost,
  type DetectCommLostView,
} from "@/domains/iot/application/run-detect-comm-lost";
import styles from "./page.module.css";

export function CommLostPanel() {
  const [batteryId, setBatteryId] = useState(DEFAULT_IOT_BATTERY);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<DetectCommLostView | null>(null);
  const [shadowView, setShadowView] = useState<DeviceShadowView | null>(null);
  const [shadowLoadError, setShadowLoadError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    const id = batteryId.trim() || DEFAULT_IOT_BATTERY;
    setShadowLoadError(null);
    setResult(null);
    loadDeviceShadow(id)
      .then((view) => {
        if (cancelled) return;
        setShadowView(view);
      })
      .catch((err) => {
        if (cancelled) return;
        setShadowView(null);
        setShadowLoadError(err instanceof Error ? err.message : String(err));
      });
    return () => {
      cancelled = true;
    };
  }, [batteryId]);

  const gate = useMemo(() => {
    if (!shadowView) {
      return {
        detectAllowed: false,
        blockMessage: shadowLoadError ?? "正在加载设备影子…",
      };
    }
    return {
      detectAllowed: shadowView.commLostDetectUseful,
      blockMessage: shadowView.commLostDetectBlockMessage,
    };
  }, [shadowView, shadowLoadError]);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (!gate.detectAllowed) {
      setError(gate.blockMessage ?? "当前不可检测");
      return;
    }
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const id = batteryId.trim() || DEFAULT_IOT_BATTERY;
      setResult(await runDetectCommLost(id));
      setShadowView(await loadDeviceShadow(id));
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
        GET shadow 对齐 DetectCommLost：仅 stale 时检测有意义
        {shadowView
          ? ` · ${shadowView.batteryId}=${shadowView.fresh ? "fresh" : "stale"}`
          : ""}
      </p>
      <form className={styles.form} onSubmit={onSubmit}>
        <label>
          batteryId
          <input
            value={batteryId}
            onChange={(e) => setBatteryId(e.target.value)}
          />
        </label>
        <button type="submit" disabled={busy || !gate.detectAllowed}>
          {busy ? "检测中…" : "检测 COMM_LOST"}
        </button>
      </form>

      {!gate.detectAllowed && gate.blockMessage ? (
        <p className={styles.note} role="status">
          {gate.blockMessage}
        </p>
      ) : null}

      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}

      {result ? <DetectResultView result={result} /> : null}
    </section>
  );
}

function DetectResultView({ result }: { result: DetectCommLostView }) {
  const alertLabel =
    result.alertType ?? (result.raised ? "COMM_LOST" : null);

  return (
    <dl className={styles.dl}>
      <dt>电池</dt>
      <dd>{result.batteryId}</dd>

      <dt>stale</dt>
      <dd>
        <span
          className={result.stale ? styles.badgeStale : styles.badgeFresh}
        >
          {result.stale ? "stale" : "fresh"}
        </span>
        {result.detectUseful ? " · 值得检测" : " · 检测无意义"}
      </dd>

      <dt>raised</dt>
      <dd>{result.raised ? "是" : "否"}</dd>

      {alertLabel ? (
        <>
          <dt>alertType</dt>
          <dd>{alertLabel}</dd>
        </>
      ) : null}

      {result.ticketId ? (
        <>
          <dt>ticketId</dt>
          <dd>{result.ticketId}</dd>
        </>
      ) : null}
    </dl>
  );
}
