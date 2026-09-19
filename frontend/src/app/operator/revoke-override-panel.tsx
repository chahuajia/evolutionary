"use client";

/**
 * 撤销套餐覆盖客户端岛 — POST /operator/overrides/{id}/revoke（AC-31 · 30a）。
 * 撤销后目录有效价回落至模板原价。
 */

import { FormEvent, useState } from "react";
import {
  DEFAULT_OVERRIDE_ACTOR_ORG_ID,
  DEFAULT_OVERRIDE_ACTOR_USER_ID,
  DEFAULT_OVERRIDE_ID,
  revokePackageOverride,
} from "@/domains/operator/application/revoke-package-override";
import type { RevokePackageOverrideResult } from "@/domains/operator/infrastructure/operator-gateway";
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
  const [result, setResult] = useState<RevokePackageOverrideResult | null>(
    null,
  );

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const r = await revokePackageOverride({
        overrideId: overrideId.trim() || DEFAULT_OVERRIDE_ID,
        actorOrgId: actorOrgId.trim() || DEFAULT_OVERRIDE_ACTOR_ORG_ID,
        actorUserId: actorUserId.trim() || DEFAULT_OVERRIDE_ACTOR_USER_ID,
      });
      setResult(r);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

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
        <button type="submit" disabled={busy}>
          {busy ? "撤销中…" : "撤销覆盖"}
        </button>
      </form>
      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}
      {result ? (
        <p>
          已撤销 {result.overrideId}
          {result.orgId ? ` · ${result.orgId}` : ""}
          {result.templateId ? ` · ${result.templateId}` : ""} ·{" "}
          {result.status}
          {result.priceCents != null
            ? ` · patches ¥${formatCentsAsYuan(result.priceCents)}`
            : ""}
          {" · 有效价已回落模板原价"}
        </p>
      ) : null}
    </section>
  );
}
