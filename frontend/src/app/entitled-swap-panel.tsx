"use client";

/**
 * 权益换电客户端岛 — 默认 U1 / E-1 / CAB-1（与 CommerceConfig 种子对齐）。
 */

import { FormEvent, useState } from "react";
import { postEntitledSwap } from "@/domains/commerce/infrastructure/entitled-swap-gateway";
import styles from "./page.module.css";

export function EntitledSwapPanel() {
  const [userId, setUserId] = useState("U1");
  const [entitlementId, setEntitlementId] = useState("E-1");
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
      const r = await postEntitledSwap({ userId, entitlementId, cabinetId });
      setResult(
        `事件 ${r.usageEventId} · ${r.status} · 电池 ${r.batteryId}`,
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>权益换电（HTTP）</h2>
      <form className={styles.form} onSubmit={onSubmit}>
        <label>
          userId
          <input
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
          />
        </label>
        <label>
          entitlementId
          <input
            value={entitlementId}
            onChange={(e) => setEntitlementId(e.target.value)}
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
          权益换电
        </button>
      </form>
      {error && <p className={styles.error}>{error}</p>}
      {result && <p>{result}</p>}
    </section>
  );
}
