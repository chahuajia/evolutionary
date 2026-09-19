"use client";

/**
 * 计量权益换电客户端岛 — 默认 U1 / E-M1 / CAB-1 · soc 80→60。
 */

import { FormEvent, useState } from "react";
import { postEntitledSwap } from "@/domains/commerce/infrastructure/entitled-swap-gateway";
import { formatCentsAsYuan } from "@/shared/money/format-cents";
import styles from "./page.module.css";

function formatCents(cents: number): string {
  return `¥${formatCentsAsYuan(cents)}（${cents}¢）`;
}

export function MeteredSwapPanel() {
  const [userId, setUserId] = useState("U1");
  const [entitlementId, setEntitlementId] = useState("E-M1");
  const [cabinetId, setCabinetId] = useState("CAB-1");
  const [socBefore, setSocBefore] = useState(80);
  const [socAfter, setSocAfter] = useState(60);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<string | null>(null);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
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
        <button type="submit" disabled={busy}>
          {busy ? "提交中…" : "计量换电"}
        </button>
      </form>
      {error && <p className={styles.error}>{error}</p>}
      {result && <p>{result}</p>}
    </section>
  );
}
