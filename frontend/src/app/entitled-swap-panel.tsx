"use client";

/**
 * 权益换电客户端岛 — 默认 U1 / E-1 / CAB-1（与 CommerceConfig 种子对齐）。
 */

import { FormEvent, useMemo, useState } from "react";
import {
  toEntitlementView,
  type EntitlementStatus,
} from "@/domains/commerce/domain/entitlement-view";
import { toUsageEventView } from "@/domains/commerce/domain/usage-event-view";
import { postEntitledSwap } from "@/domains/commerce/infrastructure/entitled-swap-gateway";
import styles from "./page.module.css";

/** 种子 E-1 为 ACTIVE；无 GET 时默认按 ACTIVE，非种子 id 交后端判。 */
const SEED_ENTITLEMENT_ID = "E-1";
const SEED_STATUS = "ACTIVE" as EntitlementStatus;

export function EntitledSwapPanel() {
  const [userId, setUserId] = useState("U1");
  const [entitlementId, setEntitlementId] = useState(SEED_ENTITLEMENT_ID);
  const [cabinetId, setCabinetId] = useState("CAB-1");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<string | null>(null);

  const gate = useMemo(() => {
    const id = entitlementId.trim() || SEED_ENTITLEMENT_ID;
    if (id !== SEED_ENTITLEMENT_ID) {
      return { swapAllowed: true, blockMessage: null as string | null };
    }
    const view = toEntitlementView({ id, status: SEED_STATUS });
    return {
      swapAllowed: view.swapAllowed,
      blockMessage: view.blockMessage,
      statusLabel: view.statusLabel,
    };
  }, [entitlementId]);

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
        仅 ACTIVE 权益可履约
        {"statusLabel" in gate && gate.statusLabel
          ? ` · 种子 ${SEED_ENTITLEMENT_ID}=${gate.statusLabel}`
          : ""}
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
          权益换电
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
