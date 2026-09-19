"use client";

/**
 * 撤销套餐覆盖客户端岛 — POST /operator/overrides/{id}/revoke（AC-31 · 30a）。
 * 撤销后目录有效价回落至模板原价。
 */

import { FormEvent, useState } from "react";
import {
  parsePackageOverrideStatus,
  toPackageOverrideView,
  type PackageOverrideView,
} from "@/domains/operator/domain/package-override-view";
import {
  DEFAULT_OVERRIDE_ACTOR_ORG_ID,
  DEFAULT_OVERRIDE_ACTOR_USER_ID,
  DEFAULT_OVERRIDE_ID,
  revokePackageOverride,
} from "@/domains/operator/application/revoke-package-override";
import { formatCentsAsYuan } from "@/shared/money/format-cents";
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
  const [priceLabel, setPriceLabel] = useState<number | null>(null);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (view && !view.revokeAllowed) {
      setError(view.blockMessage ?? "当前状态不可撤销");
      return;
    }
    setBusy(true);
    setError(null);
    setView(null);
    setPriceLabel(null);
    try {
      const r = await revokePackageOverride({
        overrideId: overrideId.trim() || DEFAULT_OVERRIDE_ID,
        actorOrgId: actorOrgId.trim() || DEFAULT_OVERRIDE_ACTOR_ORG_ID,
        actorUserId: actorUserId.trim() || DEFAULT_OVERRIDE_ACTOR_USER_ID,
      });
      setView(
        toPackageOverrideView({
          overrideId: r.overrideId,
          orgId: r.orgId ?? "",
          templateId: r.templateId ?? "",
          templateVersion: 0,
          status: parsePackageOverrideStatus(r.status),
        }),
      );
      setPriceLabel(r.priceCents ?? null);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  const revokeBlocked = view != null && !view.revokeAllowed;

  return (
    <section className={styles.panel}>
      <h2>撤销套餐覆盖（HTTP · AC-31）</h2>
      <p className={styles.note}>
        L2 撤销已激活覆盖后，目录有效价回落至模板原价（不再应用
        patches）。默认 OV-1 / ORG-L2 / U-SZ。
      </p>
      <form className={styles.form} onSubmit={onSubmit}>
        <label>
          overrideId
          <input
            value={overrideId}
            onChange={(e) => {
              setOverrideId(e.target.value);
              setView(null);
            }}
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
      {revokeBlocked && view?.blockMessage ? (
        <p className={styles.note} role="status">
          {view.blockMessage}
        </p>
      ) : null}
      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}
      {view ? (
        <p>
          已撤销 {view.overrideId}
          {view.orgId ? ` · ${view.orgId}` : ""}
          {view.templateId ? ` · ${view.templateId}` : ""} · {view.status}
          {priceLabel != null
            ? ` · patches ¥${formatCentsAsYuan(priceLabel)}`
            : ""}
          {" · 有效价已回落模板原价"}
        </p>
      ) : null}
    </section>
  );
}
