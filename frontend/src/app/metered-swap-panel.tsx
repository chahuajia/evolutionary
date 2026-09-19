"use client";

/**
 * 计量权益换电客户端岛 — 默认 U1 / E-M1 / CAB-1 · soc 80→60。
 */

import { FormEvent, useMemo, useState } from "react";
import {
  toEntitlementView,
  type EntitlementStatus,
} from "@/domains/commerce/domain/entitlement-view";
import { postEntitledSwap } from "@/domains/commerce/infrastructure/entitled-swap-gateway";
import { formatCentsAsYuan } from "@/shared/money/format-cents";
import styles from "./page.module.css";

function formatCents(cents: number): string {
  return `¥${formatCentsAsYuan(cents)}（${cents}¢）`;
}

/** 种子 E-M1 为 ACTIVE；无 GET 时默认按 ACTIVE，非种子 id 交后端判。 */
const SEED_METERED_ENTITLEMENT = "E-M1";
const SEED_STATUS = "ACTIVE" as EntitlementStatus;

export function MeteredSwapPanel() {
  const [userId, setUserId] = useState("U1");
  const [entitlementId, setEntitlementId] = useState(SEED_METERED_ENTITLEMENT);
  const [cabinetId, setCabinetId] = useState("CAB-1");
  const [socBefore, setSocBefore] = useState(80);
  const [socAfter, setSocAfter] = useState(60);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<string | null>(null);

  const gate = useMemo(() => {
    const id = entitlementId.trim() || SEED_METERED_ENTITLEMENT;
    if (id !== SEED_METERED_ENTITLEMENT) {
      return {
        swapAllowed: true,
        blockMessage: null as string | null,
        statusLabel: null as string | null,
      };
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
      const r = await postEntitledSwap({
        userId,
        entitlementId,
        cabinetId,
        socBefore,
        socAfter,
      });
      const charge =
        r.chargedAmountCents != null
          ? ` · 扣费 ${formatCents(r.chargedAmountCents)}`
          : "";
      setResult(
        `事件 ${r.usageEventId} · ${r.status} · 电池 ${r.batteryId}${charge}`,
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>计量权益换电（HTTP）</h2>
      <p className={styles.note}>
        前置：设备影子须新鲜（AssertShadowFreshForMetered）；stale 时后端
        TELEMETRY_STALE。权益须 ACTIVE
        {gate.statusLabel
          ? ` · 种子 ${SEED_METERED_ENTITLEMENT}=${gate.statusLabel}`
          : ""}
        。
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
        <label>
          socBefore
          <input
            type="number"
            value={socBefore}
            onChange={(e) => setSocBefore(Number(e.target.value))}
          />
        </label>
        <label>
          socAfter
          <input
            type="number"
            value={socAfter}
            onChange={(e) => setSocAfter(Number(e.target.value))}
          />
        </label>
        <button type="submit" disabled={busy || !gate.swapAllowed}>
          {busy ? "提交中…" : "计量换电"}
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
