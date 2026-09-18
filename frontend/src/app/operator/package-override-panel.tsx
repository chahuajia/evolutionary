"use client";

/**
 * 套餐覆盖客户端岛 — POST overrides + GET effective-product（AC-26）。
 */

import { FormEvent, useState } from "react";
import {
  DEFAULT_OVERRIDE_ACTOR_ORG_ID,
  DEFAULT_OVERRIDE_ACTOR_USER_ID,
  DEFAULT_OVERRIDE_ID,
  DEFAULT_OVERRIDE_PRICE_CENTS,
  DEFAULT_OVERRIDE_TEMPLATE_ID,
  getEffectiveProduct,
  postActivatePackageOverride,
  type ActivatePackageOverrideResult,
  type EffectiveProductResult,
} from "@/domains/operator/infrastructure/operator-gateway";
import styles from "./page.module.css";

export function PackageOverridePanel() {
  const [templateId, setTemplateId] = useState(DEFAULT_OVERRIDE_TEMPLATE_ID);
  const [actorOrgId, setActorOrgId] = useState(DEFAULT_OVERRIDE_ACTOR_ORG_ID);
  const [actorUserId, setActorUserId] = useState(
    DEFAULT_OVERRIDE_ACTOR_USER_ID,
  );
  const [overrideId, setOverrideId] = useState(DEFAULT_OVERRIDE_ID);
  const [priceCents, setPriceCents] = useState(DEFAULT_OVERRIDE_PRICE_CENTS);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activateResult, setActivateResult] =
    useState<ActivatePackageOverrideResult | null>(null);
  const [effective, setEffective] = useState<EffectiveProductResult | null>(
    null,
  );

  async function onActivate(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setActivateResult(null);
    try {
      const r = await postActivatePackageOverride({
        templateId: templateId.trim() || DEFAULT_OVERRIDE_TEMPLATE_ID,
        actorOrgId: actorOrgId.trim() || DEFAULT_OVERRIDE_ACTOR_ORG_ID,
        actorUserId: actorUserId.trim() || DEFAULT_OVERRIDE_ACTOR_USER_ID,
        overrideId: overrideId.trim() || DEFAULT_OVERRIDE_ID,
        patches: {
          price: Number.isFinite(priceCents)
            ? priceCents
            : DEFAULT_OVERRIDE_PRICE_CENTS,
        },
      });
      setActivateResult(r);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  async function onQueryEffective(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setEffective(null);
    try {
      const r = await getEffectiveProduct({
        orgId: actorOrgId.trim() || DEFAULT_OVERRIDE_ACTOR_ORG_ID,
        templateId: templateId.trim() || DEFAULT_OVERRIDE_TEMPLATE_ID,
      });
      setEffective(r);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>套餐覆盖与有效价（HTTP · AC-26）</h2>
      <p className={styles.note}>
        L2 对已发布模板激活 patches；目录侧 GET effective-product 合成价。
      </p>
      <form className={styles.form} onSubmit={onActivate}>
        <label>
          templateId
          <input
            value={templateId}
            onChange={(e) => setTemplateId(e.target.value)}
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
        <label>
          overrideId
          <input
            value={overrideId}
            onChange={(e) => setOverrideId(e.target.value)}
          />
        </label>
        <label>
          patches.price（分）
          <input
            type="number"
            min={0}
            value={priceCents}
            onChange={(e) =>
              setPriceCents(Number(e.target.value) || DEFAULT_OVERRIDE_PRICE_CENTS)
            }
          />
        </label>
        <button type="submit" disabled={busy}>
          {busy ? "提交中…" : "激活覆盖"}
        </button>
      </form>
      <form className={styles.form} onSubmit={onQueryEffective}>
        <button type="submit" disabled={busy}>
          {busy ? "查询中…" : "查询有效价"}
        </button>
      </form>
      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}
      {activateResult ? (
        <p>
          覆盖 {activateResult.overrideId} · {activateResult.orgId || "—"} ·{" "}
          {activateResult.templateId} v{activateResult.templateVersion} ·{" "}
          {activateResult.status}
          {activateResult.priceCents != null
            ? ` · ${activateResult.priceCents}¢`
            : ""}
        </p>
      ) : null}
      {effective ? (
        <p>
          有效价 {effective.priceCents}¢ · {effective.displayName || "—"} ·{" "}
          {effective.durationDays}天 · {effective.templateId} v
          {effective.templateVersion}
          {effective.overrideId
            ? ` · override ${effective.overrideId}`
            : " · 无覆盖"}
        </p>
      ) : null}
    </section>
  );
}
