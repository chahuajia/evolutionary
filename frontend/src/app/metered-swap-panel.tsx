"use client";

/**
 * 计量权益换电客户端岛 — 默认 U1 / E-M1 / CAB-1 · soc 80→60。
 * GET shadow + wallet + entitlement：对齐影子新鲜度、canCoverCents、swapAllowed（含 FROZEN）与计量费率。
 */

import { FormEvent, useEffect, useMemo, useState } from "react";
import type { EntitlementView } from "@/domains/commerce/domain/entitlement-view";
import { isExhaustedEntitlement } from "@/domains/commerce/domain/select-entitlement";
import { loadEntitlement } from "@/domains/commerce/application/load-entitlement";
import { runEntitledSwap } from "@/domains/commerce/application/run-entitled-swap";
import type { DeviceShadowView } from "@/domains/iot/domain/device-shadow-view";
import { loadDeviceShadow } from "@/domains/iot/application/load-device-shadow";
import { loadWallet } from "@/domains/wallet/application/load-wallet";
import {
  canCoverCents,
  formatCentsAsYuan,
  type WalletView,
} from "@/domains/wallet/domain/wallet-view";
import styles from "./page.module.css";

function formatCents(cents: number): string {
  return `¥${formatCentsAsYuan(cents)}（${cents}¢）`;
}

/** 种子 E-M1；状态与费率以 GET /entitled-swaps/{id} 为准。 */
const SEED_METERED_ENTITLEMENT = "E-M1";
/** 计量种子电池；findAnyIdle 也可能拿到 BAT-1（同种子新鲜影子）。 */
const DEFAULT_METERED_BATTERY = "BAT-M1";

export function MeteredSwapPanel() {
  const [userId, setUserId] = useState("U1");
  const [entitlementId, setEntitlementId] = useState(SEED_METERED_ENTITLEMENT);
  const [cabinetId, setCabinetId] = useState("CAB-1");
  const [batteryId, setBatteryId] = useState(DEFAULT_METERED_BATTERY);
  const [socBefore, setSocBefore] = useState(80);
  const [socAfter, setSocAfter] = useState(60);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<string | null>(null);
  const [shadowView, setShadowView] = useState<DeviceShadowView | null>(null);
  const [shadowLoadError, setShadowLoadError] = useState<string | null>(null);
  const [walletView, setWalletView] = useState<WalletView | null>(null);
  const [walletLoadError, setWalletLoadError] = useState<string | null>(null);
  const [entitlementView, setEntitlementView] = useState<EntitlementView | null>(
    null,
  );
  const [entitlementLoadError, setEntitlementLoadError] = useState<
    string | null
  >(null);
  const [remainingSwaps, setRemainingSwaps] = useState<number | null>(null);
  const [meteredRateCents, setMeteredRateCents] = useState<number | null>(null);

  useEffect(() => {
    let cancelled = false;
    const id = batteryId.trim() || DEFAULT_METERED_BATTERY;
    setShadowLoadError(null);
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

  useEffect(() => {
    let cancelled = false;
    const id = userId.trim() || "U1";
    setWalletLoadError(null);
    loadWallet(id)
      .then((view) => {
        if (cancelled) return;
        setWalletView(view);
      })
      .catch((err) => {
        if (cancelled) return;
        setWalletView(null);
        setWalletLoadError(err instanceof Error ? err.message : String(err));
      });
    return () => {
      cancelled = true;
    };
  }, [userId]);

  useEffect(() => {
    let cancelled = false;
    const id = entitlementId.trim() || SEED_METERED_ENTITLEMENT;
    setEntitlementLoadError(null);
    loadEntitlement(id)
      .then(({ view, remainingSwaps: remaining, meteredRateCents: rate }) => {
        if (cancelled) return;
        setRemainingSwaps(remaining);
        setMeteredRateCents(rate);
        setEntitlementView(view);
      })
      .catch((err) => {
        if (cancelled) return;
        setEntitlementView(null);
        setRemainingSwaps(null);
        setMeteredRateCents(null);
        setEntitlementLoadError(
          err instanceof Error ? err.message : String(err),
        );
      });
    return () => {
      cancelled = true;
    };
  }, [entitlementId]);

  const estimatedChargeCents = useMemo(() => {
    if (meteredRateCents == null || !Number.isFinite(meteredRateCents)) {
      return null;
    }
    const delta = socBefore - socAfter;
    if (!Number.isFinite(delta) || delta < 0) return null;
    return delta * meteredRateCents;
  }, [socBefore, socAfter, meteredRateCents]);

  const gate = useMemo(() => {
    const entitlementOk = !entitlementView
      ? {
          swapAllowed: false,
          blockMessage: entitlementLoadError ?? "正在加载权益…",
          statusLabel: null as string | null,
        }
      : !entitlementView.swapAllowed
        ? {
            swapAllowed: false,
            blockMessage: entitlementView.blockMessage,
            statusLabel: entitlementView.statusLabel,
          }
        : isExhaustedEntitlement(remainingSwaps)
          ? {
              swapAllowed: false,
              blockMessage: "权益次数已用尽，不可换电",
              statusLabel: entitlementView.statusLabel,
            }
          : meteredRateCents == null
            ? {
                swapAllowed: false,
                blockMessage: "权益未挂计量费率，不可估费换电",
                statusLabel: entitlementView.statusLabel,
              }
          : {
              swapAllowed: true,
              blockMessage: null as string | null,
              statusLabel: entitlementView.statusLabel,
            };
    if (!entitlementOk.swapAllowed) {
      return entitlementOk;
    }
    if (!shadowView) {
      return {
        swapAllowed: false,
        blockMessage: shadowLoadError ?? "正在加载设备影子…",
        statusLabel: entitlementOk.statusLabel,
      };
    }
    if (!shadowView.meteredSwapAllowed) {
      return {
        swapAllowed: false,
        blockMessage: shadowView.blockMessage,
        statusLabel: entitlementOk.statusLabel,
      };
    }
    if (estimatedChargeCents == null) {
      return {
        swapAllowed: false,
        blockMessage: "socAfter 不得超过 socBefore",
        statusLabel: entitlementOk.statusLabel,
      };
    }
    if (!walletView) {
      return {
        swapAllowed: false,
        blockMessage: walletLoadError ?? "正在加载钱包…",
        statusLabel: entitlementOk.statusLabel,
      };
    }
    if (!canCoverCents(walletView.balanceCents, estimatedChargeCents)) {
      return {
        swapAllowed: false,
        blockMessage: `余额不足（¥${walletView.balanceYuan} < ¥${formatCentsAsYuan(estimatedChargeCents)}）`,
        statusLabel: entitlementOk.statusLabel,
      };
    }
    return {
      swapAllowed: true,
      blockMessage: null as string | null,
      statusLabel: entitlementOk.statusLabel,
    };
  }, [
    entitlementView,
    entitlementLoadError,
    remainingSwaps,
    meteredRateCents,
    shadowView,
    shadowLoadError,
    walletView,
    walletLoadError,
    estimatedChargeCents,
  ]);

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
      const uid = userId.trim() || "U1";
      const r = await runEntitledSwap({
        userId: uid,
        entitlementId,
        cabinetId,
        socBefore,
        socAfter,
      });
      const charge =
        r.chargedAmountCents != null
          ? ` · 扣费 ${formatCents(r.chargedAmountCents)}`
          : "";
      const ue = r.usageEvent;
      setResult(
        `事件 ${ue.id} · ${ue.statusLabel}` +
          (ue.blockMessage ? ` · ${ue.blockMessage}` : "") +
          ` · 电池 ${r.batteryId}${charge}`,
      );
      setShadowView(
        await loadDeviceShadow(batteryId.trim() || DEFAULT_METERED_BATTERY),
      );
      setWalletView(await loadWallet(uid));
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
        GET 权益（含 meteredRateCents）/ shadow / wallet 对齐 swapAllowed、新鲜度与
        canCoverCents
        {gate.statusLabel
          ? ` · ${entitlementId}=${gate.statusLabel}`
          : ""}
        {meteredRateCents != null ? ` · 费率 ${meteredRateCents}¢/SOC` : ""}
        {shadowView
          ? ` · ${shadowView.batteryId}=${shadowView.fresh ? "fresh" : "stale"}`
          : ""}
        {estimatedChargeCents != null
          ? ` · 预估 ${formatCents(estimatedChargeCents)}`
          : ""}
        {walletView ? ` · 余额 ¥${walletView.balanceYuan}` : ""}
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
          batteryId（影子门）
          <input
            value={batteryId}
            onChange={(e) => setBatteryId(e.target.value)}
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
