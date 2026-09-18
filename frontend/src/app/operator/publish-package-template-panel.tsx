"use client";

/**
 * 发布套餐模板客户端岛 — POST /operator/templates/{id}/publish（AC-24）。
 */

import { FormEvent, useState } from "react";
import {
  DEFAULT_PACKAGE_TEMPLATE_ID,
  DEFAULT_PUBLISH_ACTOR_ORG_ID,
  DEFAULT_PUBLISH_ACTOR_USER_ID,
  postPublishPackageTemplate,
  type PublishPackageTemplateResult,
} from "@/domains/operator/infrastructure/operator-gateway";
import styles from "./page.module.css";

export function PublishPackageTemplatePanel() {
  const [templateId, setTemplateId] = useState(DEFAULT_PACKAGE_TEMPLATE_ID);
  const [actorOrgId, setActorOrgId] = useState(DEFAULT_PUBLISH_ACTOR_ORG_ID);
  const [actorUserId, setActorUserId] = useState(
    DEFAULT_PUBLISH_ACTOR_USER_ID,
  );
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<PublishPackageTemplateResult | null>(
    null,
  );

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const r = await postPublishPackageTemplate({
        templateId: templateId.trim() || DEFAULT_PACKAGE_TEMPLATE_ID,
        actorOrgId: actorOrgId.trim() || DEFAULT_PUBLISH_ACTOR_ORG_ID,
        actorUserId: actorUserId.trim() || DEFAULT_PUBLISH_ACTOR_USER_ID,
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
      <h2>发布套餐模板（HTTP · AC-24）</h2>
      <form className={styles.form} onSubmit={onSubmit}>
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
        <button type="submit" disabled={busy}>
          {busy ? "发布中…" : "发布模板"}
        </button>
      </form>
      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}
      {result ? (
        <p>
          模板 {result.templateId} · {result.ownerOrgId || "—"} · v
          {result.version} · {result.status}
          {result.publishedAt ? ` · ${result.publishedAt}` : ""}
        </p>
      ) : null}
    </section>
  );
}
