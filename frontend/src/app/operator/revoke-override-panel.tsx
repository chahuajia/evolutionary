"use client";

/**
 * 撤销套餐覆盖客户端岛 — POST /operator/overrides/{id}/revoke（AC-31 · 30a）。
 * GET 覆盖对齐 revokeAllowed（仅 ACTIVE）；撤销后目录有效价回落模板原价。
 */

import { FormEvent, useEffect, useState } from "react";
import type { PackageOverrideView } from "@/domains/operator/domain/package-override-view";
import { loadPackageOverride } from "@/domains/operator/application/load-package-override";
import {
  DEFAULT_OVERRIDE_ACTOR_ORG_ID,
  DEFAULT_OVERRIDE_ACTOR_USER_ID,
  DEFAULT_OVERRIDE_ID,
  revokePackageOverride,
} from "@/domains/operator/application/revoke-package-override";
import { formatCentsAsYuan } from "@/shared/money/format-cents";
import { useActorOrganization } from "./use-actor-organization";
import styles from "./page.module.css";

export function RevokeOverridePanel() {
  const [overrideId, setOverrideId] = useState(DEFAULT_OVERRIDE_ID);
  const [actorOrgId, setActorOrgId] = useState(DEFAULT_OVERRIDE_ACTOR_ORG_ID);
  const [actorUserId, setActorUserId] = useState(
    DEFAULT_OVERRIDE_ACTOR_USER_ID,
  );
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [view, setView] = useState<PackageOverrideView | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [priceLabel, setPriceLabel] = useState<number | null>(null);
  const actorGate = useActorOrganization(
    actorOrgId,
    DEFAULT_OVERRIDE_ACTOR_ORG_ID,
  );

  useEffect(() => {
    let cancelled = false;
    const id = overrideId.trim() || DEFAULT_OVERRIDE_ID;
    setLoadError(null);
    setPriceLabel(null);
    loadPackageOverride(id)
      .then(({ view: next, priceCents }) => {
        if (cancelled) return;
        setView(next);
        setPriceLabel(priceCents);
      })
      .catch((err) => {
        if (cancelled) return;
        setView(null);
        setLoadError(err instanceof Error ? err.message : String(err));
      });
    return () => {
      cancelled = true;
    };
  }, [overrideId]);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (!actorGate.canAct) {
      setError(actorGate.blockMessage ?? "操作方组织不可撤销覆盖");
      return;
    }
    if (!view || !view.revokeAllowed) {
      setError(
        view?.blockMessage ?? loadError ?? "当前覆盖不可撤销（须已激活）",
      );
      return;
    }
    setBusy(true);
    setError(null);
    try {
      const id = overrideId.trim() || DEFAULT_OVERRIDE_ID;
      const r = await revokePackageOverride({
        overrideId: id,
        actorOrgId: actorOrgId.trim() || DEFAULT_OVERRIDE_ACTOR_ORG_ID,
        actorUserId: actorUserId.trim() || DEFAULT_OVERRIDE_ACTOR_USER_ID,
        templateVersion: view.templateVersion,
      });
      setView(r.view);
      setPriceLabel(r.priceCents);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  const revokeBlocked =
    !actorGate.canAct || view == null || !view.revokeAllowed;
  const blockMessage = !actorGate.canAct
    ? actorGate.blockMessage
    : view == null
      ? (loadError ?? "正在加载覆盖…")
      : !view.revokeAllowed
        ? view.blockMessage
        : null;

  return (
    <section className={styles.panel}>
      <h2>撤销套餐覆盖（HTTP · AC-31）</h2>
      <p className={styles.note}>
        GET 覆盖对齐仅已激活可撤销；GET 组织对齐 canActAsManager
        {actorGate.statusLabel
          ? ` · ${actorOrgId}=${actorGate.statusLabel}`
          : ""}
        {view ? ` · ${view.overrideId}=${view.statusLabel}` : ""}
      </p>
      <form className={styles.form} onSubmit={onSubmit}>
        <label>
          overrideId
          <input
            value={overrideId}
            onChange={(e) => setOverrideId(e.target.value)}
          />
        </label>
        <label>
          actorOrgId
          <input
            value={actorOrgId}
            onChange={(e) => setActorOrgId(e.target.value)}
          />
        </label>
        <label>
          actorUserId
          <input
            value={actorUserId}
            onChange={(e) => setActorUserId(e.target.value)}
          />
        </label>
        <button type="submit" disabled={busy || revokeBlocked}>
          {busy ? "撤销中…" : "撤销覆盖"}
        </button>
      </form>
      {revokeBlocked && blockMessage ? (
        <p className={styles.note} role="status">
          {blockMessage}
        </p>
      ) : null}
      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}
      {view?.revoked ? (
        <p>
          已撤销 {view.overrideId}
          {view.orgId ? ` · ${view.orgId}` : ""}
          {view.templateId ? ` · ${view.templateId}` : ""} · {view.statusLabel}
          {priceLabel != null
            ? ` · patches ¥${formatCentsAsYuan(priceLabel)}`
            : ""}
          {" · 有效价已回落模板原价"}
        </p>
      ) : null}
    </section>
  );
}
