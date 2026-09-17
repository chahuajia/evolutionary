"use client";

/**
 * 默认选卡换电客户端岛 — 省略 entitlementId，展示 BE AC-14 选中的权益。
 */

import { FormEvent, useState } from "react";
import { postEntitledSwap } from "@/domains/commerce/infrastructure/entitled-swap-gateway";
import styles from "./page.module.css";

export function DefaultSelectPanel() {
  const [userId, setUserId] = useState("U1");
  const [cabinetId, setCabinetId] = useState("CAB-1");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<string | null>(null);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const r = await postEntitledSwap({ userId, cabinetId });
      setResult(
        `默认选中 ${r.entitlementId} · 事件 ${r.usageEventId} · ${r.status} · 电池 ${r.batteryId}`,
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>默认选卡换电（HTTP · AC-14）</h2>
      <form className={styles.form} onSubmit={onSubmit}>
        <label>
          userId
          <input
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
          />
        </label>
        <label>
          cabinetId
          <input
            value={cabinetId}
            onChange={(e) => setCabinetId(e.target.value)}
          />
        </label>
        <button type="submit" disabled={busy}>
          {busy ? "换电中…" : "默认选卡换电"}
        </button>
      </form>
      {error ? <p className={styles.error}>{error}</p> : null}
      {result ? <p>{result}</p> : null}
    </section>
  );
}
