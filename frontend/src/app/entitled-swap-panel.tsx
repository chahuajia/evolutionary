"use client";

/**
 * 权益换电客户端岛 — 默认 U1 / E-1 / CAB-1（与 CommerceConfig 种子对齐）。
 * GET /entitled-swaps/{id} 对齐 swapAllowed（含 FROZEN / 用尽）。
 */

import { FormEvent, useEffect, useMemo, useState } from "react";
import type { EntitlementView } from "@/domains/commerce/domain/entitlement-view";
import { isExhaustedEntitlement } from "@/domains/commerce/domain/select-entitlement";
import { toUsageEventView } from "@/domains/commerce/domain/usage-event-view";
import { loadEntitlement } from "@/domains/commerce/application/load-entitlement";
import { postEntitledSwap } from "@/domains/commerce/infrastructure/entitled-swap-gateway";
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
  const [remainingSwaps, setRemainingSwaps] = useState<number | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    const id = entitlementId.trim() || SEED_ENTITLEMENT_ID;
    setLoadError(null);
    loadEntitlement(id)
      .then(({ view: next, remainingSwaps: remaining }) => {
        if (cancelled) return;
        setRemainingSwaps(remaining);
        setView(next);
      })
      .catch((err) => {
        if (cancelled) return;
        setView(null);
        setRemainingSwaps(null);
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
    if (!view.swapAllowed) {
      return {
        swapAllowed: false,
        blockMessage: view.blockMessage,
        statusLabel: view.statusLabel,
      };
    }
    if (isExhaustedEntitlement(remainingSwaps)) {
      return {
        swapAllowed: false,
        blockMessage: "权益次数已用尽，不可换电",
        statusLabel: view.statusLabel,
      };
    }
    return {
      swapAllowed: true,
      blockMessage: null as string | null,
      statusLabel: view.statusLabel,
    };
  }, [view, loadError, remainingSwaps]);

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
      const r = await postEntitledSwap({ userId, entitlementId, cabinetId });
      const ue = toUsageEventView({
        id: r.usageEventId,
        status: r.status,
      });
      setResult(
        `事件 ${ue.id} · ${ue.statusLabel}` +
          (ue.blockMessage ? ` · ${ue.blockMessage}` : "") +
          ` · 电池 ${r.batteryId}`,
      );
      const loaded = await loadEntitlement(
        entitlementId.trim() || SEED_ENTITLEMENT_ID,
      );
      setRemainingSwaps(loaded.remainingSwaps);
      setView(loaded.view);
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
        GET 权益对齐仅 ACTIVE 且未用尽可履约
        {gate.statusLabel ? ` · ${entitlementId}=${gate.statusLabel}` : ""}
        {remainingSwaps != null ? ` · 余 ${remainingSwaps} 次` : ""}
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
