"use client";

/**
 * 发布套餐模板客户端岛 — POST /operator/templates/{id}/publish（AC-24）。
 */

import { FormEvent, useState } from "react";
import {
  parsePackageTemplateStatus,
  toPackageTemplateView,
  type PackageTemplateView,
} from "@/domains/operator/domain/package-template-view";
import {
  DEFAULT_PACKAGE_TEMPLATE_ID,
  DEFAULT_PUBLISH_ACTOR_ORG_ID,
  DEFAULT_PUBLISH_ACTOR_USER_ID,
  postPublishPackageTemplate,
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
  const [view, setView] = useState<PackageTemplateView | null>(null);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (view && !view.publishAllowed) {
      setError(view.blockMessage ?? "当前状态不可发布");
      return;
    }
    setBusy(true);
    setError(null);
    setView(null);
    try {
      const r = await postPublishPackageTemplate({
        templateId: templateId.trim() || DEFAULT_PACKAGE_TEMPLATE_ID,
        actorOrgId: actorOrgId.trim() || DEFAULT_PUBLISH_ACTOR_ORG_ID,
        actorUserId: actorUserId.trim() || DEFAULT_PUBLISH_ACTOR_USER_ID,
      });
      setView(
        toPackageTemplateView({
          id: r.templateId,
          ownerOrgId: r.ownerOrgId,
          version: r.version,
          status: parsePackageTemplateStatus(r.status),
        }),
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  const publishBlocked = view != null && !view.publishAllowed;

  return (
    <section className={styles.panel}>
      <h2>发布套餐模板（HTTP · AC-24）</h2>
      <form className={styles.form} onSubmit={onSubmit}>
        <label>
          templateId
          <input
            value={templateId}
            onChange={(e) => {
              setTemplateId(e.target.value);
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
        <button type="submit" disabled={busy || publishBlocked}>
          {busy ? "发布中…" : "发布模板"}
        </button>
      </form>
      {publishBlocked && view?.blockMessage ? (
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
          模板 {view.id} · {view.ownerOrgId || "—"} · v{view.version} ·{" "}
          {view.status}
          {view.publishAllowed ? "" : " · 已不可再发布"}
          {view.nextVersionAllowed ? " · 可派生下一版本" : ""}
        </p>
      ) : null}
    </section>
  );
}
