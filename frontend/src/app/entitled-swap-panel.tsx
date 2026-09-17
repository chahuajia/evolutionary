"use client";

/**
 * 客户端岛：权益换电（与站级 SwapPanel 并行，自包含）。
 */

import { FormEvent, useState } from "react";
import {
  postEntitledSwap,
  type EntitledSwapResult,
} from "@/domains/commerce/infrastructure/entitled-swap-gateway";
import styles from "./page.module.css";

export function EntitledSwapPanel() {
  const [userId, setUserId] = useState("U1");
  const [entitlementId, setEntitlementId] = useState("E-1");
  const [cabinetId, setCabinetId] = useState("CAB-1");
  const [result, setResult] = useState<EntitledSwapResult | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const body = await postEntitledSwap({
        userId,
        entitlementId,
        cabinetId,
      });
      setResult(body);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>权益换电</h2>
      <form className={styles.form} onSubmit={onSubmit}>
        <label>
          用户 ID
          <input value={userId} onChange={(e) => setUserId(e.target.value)} />
        </label>
        <label>
          权益 ID
          <input
            value={entitlementId}
            onChange={(e) => setEntitlementId(e.target.value)}
          />
        </label>
        <label>
          柜机 ID
          <input
            value={cabinetId}
            onChange={(e) => setCabinetId(e.target.value)}
          />
        </label>
        <div className={styles.actions}>
          <button type="submit" disabled={busy}>
            权益换电
          </button>
        </div>
      </form>

      {error && <p className={styles.error}>{error}</p>}

      {result && (
        <p>
          用量 {result.id}：用户 {result.userId} · 权益 {result.entitlementId} ·
          柜 {result.cabinetId} · 电池 {result.batteryId} · {result.status}
        </p>
      )}
    </section>
  );
}
