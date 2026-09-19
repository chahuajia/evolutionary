"use client";

/**
 * 计量权益换电客户端岛 — 默认 U1 / E-M1 / CAB-1 · soc 80→60。
 * GET shadow(BAT-M1) 对齐 AssertShadowFreshForMetered。
 */

import { FormEvent, useEffect, useMemo, useState } from "react";
import {
  toEntitlementView,
  type EntitlementStatus,
} from "@/domains/commerce/domain/entitlement-view";
import { toUsageEventView } from "@/domains/commerce/domain/usage-event-view";
import { postEntitledSwap } from "@/domains/commerce/infrastructure/entitled-swap-gateway";
import {
  toDeviceShadowView,
  type DeviceShadowView,
} from "@/domains/iot/domain/device-shadow-view";
import { fetchDeviceShadow } from "@/domains/iot/infrastructure/iot-gateway";
import { formatCentsAsYuan } from "@/shared/money/format-cents";
import styles from "./page.module.css";

function formatCents(cents: number): string {
  return `¥${formatCentsAsYuan(cents)}（${cents}¢）`;
}

/** 种子 E-M1 为 ACTIVE；无 GET 时默认按 ACTIVE，非种子 id 交后端判。 */
const SEED_METERED_ENTITLEMENT = "E-M1";
const SEED_STATUS = "ACTIVE" as EntitlementStatus;
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

  useEffect(() => {
    let cancelled = false;
    const id = batteryId.trim() || DEFAULT_METERED_BATTERY;
    setShadowLoadError(null);
    fetchDeviceShadow(id)
      .then((dto) => {
        if (cancelled) return;
        setShadowView(
          toDeviceShadowView({
            batteryId: dto.batteryId,
            soc: dto.soc,
            voltageMilli: dto.voltageMilli,
            stale: dto.stale,
            lastSeenAt: dto.lastSeenAt,
            status: dto.status,
            lockState: dto.lockState,
          }),
        );
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

  const gate = useMemo(() => {
    const id = entitlementId.trim() || SEED_METERED_ENTITLEMENT;
    const entitlementOk =
      id !== SEED_METERED_ENTITLEMENT
        ? {
            swapAllowed: true,
            blockMessage: null as string | null,
            statusLabel: null as string | null,
          }
        : (() => {
            const view = toEntitlementView({ id, status: SEED_STATUS });
            return {
              swapAllowed: view.swapAllowed,
              blockMessage: view.blockMessage,
              statusLabel: view.statusLabel,
            };
          })();
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
    return {
      swapAllowed: true,
      blockMessage: null as string | null,
      statusLabel: entitlementOk.statusLabel,
    };
  }, [entitlementId, shadowView, shadowLoadError]);

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
      const ue = toUsageEventView({
        id: r.usageEventId,
        status: r.status,
      });
      setResult(
        `事件 ${ue.id} · ${ue.statusLabel}` +
          (ue.blockMessage ? ` · ${ue.blockMessage}` : "") +
          ` · 电池 ${r.batteryId}${charge}`,
      );
      const dto = await fetchDeviceShadow(
        batteryId.trim() || DEFAULT_METERED_BATTERY,
      );
      setShadowView(
        toDeviceShadowView({
          batteryId: dto.batteryId,
          soc: dto.soc,
          voltageMilli: dto.voltageMilli,
          stale: dto.stale,
          lastSeenAt: dto.lastSeenAt,
          status: dto.status,
          lockState: dto.lockState,
        }),
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
        GET shadow 对齐 AssertShadowFreshForMetered；权益须 ACTIVE
        {gate.statusLabel
          ? ` · 种子 ${SEED_METERED_ENTITLEMENT}=${gate.statusLabel}`
          : ""}
        {shadowView
          ? ` · ${shadowView.batteryId}=${shadowView.fresh ? "fresh" : "stale"}`
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
