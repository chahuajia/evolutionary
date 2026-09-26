"use client";

/**
 * 默认选卡换电客户端岛 — 省略 entitlementId，展示 BE AC-14 选中的权益。
 * GET /entitled-swaps?userId= 对齐 ACTIVE 目录，再跑 selectDefaultEntitlement。
 */

import { FormEvent, useEffect, useMemo, useState } from "react";
import {
  selectDefaultEntitlement,
  defaultSelectBlockMessage,
  type SelectableEntitlement,
} from "@/domains/commerce/domain/select-entitlement";
import { loadActiveEntitlements } from "@/domains/commerce/application/load-active-entitlements";
import { runEntitledSwap } from "@/domains/commerce/application/run-entitled-swap";
import styles from "./page.module.css";

export function DefaultSelectPanel() {
  const [userId, setUserId] = useState("U1");
  const [cabinetId, setCabinetId] = useState("CAB-1");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<string | null>(null);
  const [catalog, setCatalog] = useState<readonly SelectableEntitlement[] | null>(
    null,
  );
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    const id = userId.trim() || "U1";
    setLoadError(null);
    loadActiveEntitlements(id)
      .then((rows) => {
        if (cancelled) return;
        setCatalog(rows);
      })
      .catch((err) => {
        if (cancelled) return;
        setCatalog(null);
        setLoadError(err instanceof Error ? err.message : String(err));
      });
    return () => {
      cancelled = true;
    };
  }, [userId]);

  const preview = useMemo(
    () => (catalog == null ? null : selectDefaultEntitlement(catalog)),
    [catalog],
  );

  const gate = useMemo(() => {
    if (catalog == null) {
      return {
        swapAllowed: false,
        blockMessage: loadError ?? "正在加载权益目录…",
      };
    }
    if (!preview) {
      return {
        swapAllowed: false,
        blockMessage: defaultSelectBlockMessage(catalog),
      };
    }
    return {
      swapAllowed: true,
      blockMessage: null as string | null,
    };
  }, [catalog, preview, loadError]);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (!gate.swapAllowed) {
      setError(gate.blockMessage ?? "不可默认选卡换电");
      return;
    }
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const r = await runEntitledSwap({ userId, cabinetId });
      const ue = r.usageEvent;
      setResult(
        `默认选中 ${r.entitlementId}` +
          (preview && r.entitlementId === preview.id
            ? "（对齐 FE 预览）"
            : preview
              ? `（FE 预览 ${preview.id}）`
              : "") +
          ` · 事件 ${ue.id} · ${ue.statusLabel}` +
          (ue.blockMessage ? ` · ${ue.blockMessage}` : "") +
          ` · 电池 ${r.batteryId}`,
      );
      setCatalog(await loadActiveEntitlements(userId.trim() || "U1"));
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>默认选卡换电（HTTP · AC-14）</h2>
      <p className={styles.note}>
        GET 有效权益目录 · FE 预览 selectDefaultEntitlement（次卡优先）
        {catalog != null ? ` · 目录 ${catalog.length} 张` : ""}
        {preview
          ? ` · 预览选中 ${preview.id}${
              preview.remainingSwaps != null
                ? `（余 ${preview.remainingSwaps} 次）`
                : "（无限）"
            }`
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
          cabinetId
          <input
            value={cabinetId}
            onChange={(e) => setCabinetId(e.target.value)}
          />
        </label>
        <button type="submit" disabled={busy || !gate.swapAllowed}>
          {busy ? "换电中…" : "默认选卡换电"}
        </button>
      </form>
      {!gate.swapAllowed && gate.blockMessage ? (
        <p className={styles.note} role="status">
          {gate.blockMessage}
        </p>
      ) : null}
      {error ? <p className={styles.error}>{error}</p> : null}
      {result ? <p>{result}</p> : null}
    </section>
  );
}
