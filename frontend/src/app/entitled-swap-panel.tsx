"use client";

/**
 * 权益换电客户端岛 — 默认 U1 / E-1 / CAB-1（与 CommerceConfig 种子对齐）。
 * GET /entitled-swaps/{id} 对齐 swapAllowed（含 FROZEN / 用尽）。
 */

import { FormEvent, useEffect, useMemo, useState } from "react";
import type { EntitlementView } from "@/domains/commerce/domain/entitlement-view";
import { loadEntitlement } from "@/domains/commerce/application/load-entitlement";
import { runEntitledSwap } from "@/domains/commerce/application/run-entitled-swap";
import styles from "./page.module.css";

const SEED_ENTITLEMENT_ID = "E-1";

export function EntitledSwapPanel() {
  const [userId, setUserId] = useState("U1");
  const [entitlementId, setEntitlementId] = useState(SEED_ENTITLEMENT_ID);
  const [cabinetId, setCabinetId] = useState("CAB-1");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<string | null>(null);
  const [view, setView] = useState<EntitlementView | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    const id = entitlementId.trim() || SEED_ENTITLEMENT_ID;
    setLoadError(null);
    loadEntitlement(id)
      .then((next) => {
        if (cancelled) return;
        setView(next);
      })
      .catch((err) => {
        if (cancelled) return;
        setView(null);
        setLoadError(err instanceof Error ? err.message : String(err));
      });
    return () => {
      cancelled = true;
    };
  }, [entitlementId]);

  const gate = useMemo(() => {
    if (!view) {
      return {
        swapAllowed: false,
        blockMessage: loadError ?? "正在加载权益…",
        statusLabel: null as string | null,
      };
    }
    return {
      swapAllowed: view.swapAllowed,
      blockMessage: view.blockMessage,
      statusLabel: view.statusLabel,
    };
  }, [view, loadError]);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (!gate.swapAllowed) {
      setError(gate.blockMessage ?? "权益不可换电");
      return;
    }
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const r = await runEntitledSwap({ userId, entitlementId, cabinetId });
      const ue = r.usageEvent;
      setResult(
        `事件 ${ue.id} · ${ue.statusLabel}` +
          (ue.blockMessage ? ` · ${ue.blockMessage}` : "") +
          ` · 电池 ${r.batteryId}`,
      );
      const loaded = await loadEntitlement(
        entitlementId.trim() || SEED_ENTITLEMENT_ID,
      );
      setView(loaded);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>权益换电（HTTP）</h2>
      <p className={styles.note}>
        GET 权益对齐仅有效且未用尽可履约
        {gate.statusLabel ? ` · ${entitlementId}=${gate.statusLabel}` : ""}
        {view?.remainingSwaps != null ? ` · 余 ${view.remainingSwaps} 次` : ""}
      </p>
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
        <button type="submit" disabled={busy || !gate.swapAllowed}>
          {busy ? "换电中…" : "权益换电"}
        </button>
      </form>
      {!gate.swapAllowed && gate.blockMessage ? (
        <p className={styles.note} role="status">
          {gate.blockMessage}
        </p>
      ) : null}
      {error && <p className={styles.error}>{error}</p>}
      {result && <p>{result}</p>}
    </section>
  );
}
