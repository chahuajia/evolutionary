"use client";

/**
 * 发布套餐模板客户端岛 — POST publish（AC-24）+ POST base-product（仅 DRAFT · replaceAllowed）。
 */

import { FormEvent, useEffect, useState } from "react";
import type { PackageTemplateView } from "@/domains/operator/domain/package-template-view";
import { loadPackageTemplate } from "@/domains/operator/application/load-package-template";
import {
  DEFAULT_PACKAGE_TEMPLATE_ID,
  DEFAULT_PUBLISH_ACTOR_ORG_ID,
  runMutatePackageTemplateBaseProduct,
} from "@/domains/operator/application/run-mutate-package-template-base-product";
import {
  DEFAULT_PUBLISH_ACTOR_USER_ID,
  runPublishPackageTemplate,
} from "@/domains/operator/application/run-publish-package-template";
import { useActorOrganization } from "./use-actor-organization";
import styles from "./page.module.css";

const DEFAULT_DISPLAY_NAME = "草稿改价";
const DEFAULT_PRICE_CENTS = 1999;
const DEFAULT_DURATION_DAYS = 30;

export function PublishPackageTemplatePanel() {
  const [templateId, setTemplateId] = useState(DEFAULT_PACKAGE_TEMPLATE_ID);
  const [actorOrgId, setActorOrgId] = useState(DEFAULT_PUBLISH_ACTOR_ORG_ID);
  const [actorUserId, setActorUserId] = useState(
    DEFAULT_PUBLISH_ACTOR_USER_ID,
  );
  const [displayName, setDisplayName] = useState(DEFAULT_DISPLAY_NAME);
  const [priceCents, setPriceCents] = useState(DEFAULT_PRICE_CENTS);
  const [durationDays, setDurationDays] = useState(DEFAULT_DURATION_DAYS);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [view, setView] = useState<PackageTemplateView | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const actorGate = useActorOrganization(
    actorOrgId,
    DEFAULT_PUBLISH_ACTOR_ORG_ID,
  );

  useEffect(() => {
    let cancelled = false;
    const id = templateId.trim() || DEFAULT_PACKAGE_TEMPLATE_ID;
    setLoadError(null);
    loadPackageTemplate(id)
      .then((next) => {
        if (cancelled) return;
        setView(next);
        if (next.displayName) setDisplayName(next.displayName);
        if (next.priceCents != null) setPriceCents(next.priceCents);
        if (next.durationDays != null) setDurationDays(next.durationDays);
      })
      .catch((err) => {
        if (cancelled) return;
        setView(null);
        setLoadError(err instanceof Error ? err.message : String(err));
      });
    return () => {
      cancelled = true;
    };
  }, [templateId]);

  async function onPublish(e: FormEvent) {
    e.preventDefault();
    if (!actorGate.canAct) {
      setError(actorGate.blockMessage ?? "操作方组织不可发布");
      return;
    }
    if (view && !view.publishAllowed) {
      setError(view.blockMessage ?? "当前状态不可发布");
      return;
    }
    setBusy(true);
    setError(null);
    try {
      setView(
        await runPublishPackageTemplate({
          templateId: templateId.trim() || DEFAULT_PACKAGE_TEMPLATE_ID,
          actorOrgId: actorOrgId.trim() || DEFAULT_PUBLISH_ACTOR_ORG_ID,
          actorUserId: actorUserId.trim() || DEFAULT_PUBLISH_ACTOR_USER_ID,
        }),
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  async function onReplace(e: FormEvent) {
    e.preventDefault();
    if (!actorGate.canAct) {
      setError(actorGate.blockMessage ?? "操作方组织不可改基产品");
      return;
    }
    if (view && !view.replaceAllowed) {
      setError(view.blockMessage ?? "当前状态不可原地改基产品");
      return;
    }
    setBusy(true);
    setError(null);
    try {
      setView(
        await runMutatePackageTemplateBaseProduct({
          templateId: templateId.trim() || DEFAULT_PACKAGE_TEMPLATE_ID,
          actorOrgId: actorOrgId.trim() || DEFAULT_PUBLISH_ACTOR_ORG_ID,
          displayName: displayName.trim() || DEFAULT_DISPLAY_NAME,
          priceCents: Number.isFinite(priceCents)
            ? priceCents
            : DEFAULT_PRICE_CENTS,
          durationDays: Number.isFinite(durationDays)
            ? durationDays
            : DEFAULT_DURATION_DAYS,
        }),
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  const publishBlocked =
    !actorGate.canAct || view == null || !view.publishAllowed;
  const replaceBlocked =
    !actorGate.canAct || view == null || !view.replaceAllowed;
  const publishBlockMessage = !actorGate.canAct
    ? actorGate.blockMessage
    : view == null
      ? (loadError ?? "正在加载模板…")
      : !view.publishAllowed
        ? view.blockMessage
        : null;
  const replaceBlockMessage = !actorGate.canAct
    ? actorGate.blockMessage
    : view == null
      ? (loadError ?? "正在加载模板…")
      : !view.replaceAllowed
        ? view.blockMessage
        : null;

  return (
    <section className={styles.panel}>
      <h2>发布 / 改基产品（HTTP · AC-24/25）</h2>
      <p className={styles.note}>
        GET 组织对齐 canActAsManager；GET 模板对齐 publishAllowed /
        replaceAllowed
        {actorGate.statusLabel
          ? ` · ${actorOrgId}=${actorGate.statusLabel}`
          : ""}
        {view ? ` · ${view.id}=${view.statusLabel}` : ""}
      </p>
      <form className={styles.form} onSubmit={onPublish}>
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
      {publishBlocked && publishBlockMessage ? (
        <p className={styles.note} role="status">
          {publishBlockMessage}
        </p>
      ) : null}
      <form className={styles.form} onSubmit={onReplace}>
        <label>
          displayName
          <input
            value={displayName}
            onChange={(e) => setDisplayName(e.target.value)}
          />
        </label>
        <label>
          priceCents
          <input
            type="number"
            value={priceCents}
            onChange={(e) => setPriceCents(Number(e.target.value))}
          />
        </label>
        <label>
          durationDays
          <input
            type="number"
            value={durationDays}
            onChange={(e) => setDurationDays(Number(e.target.value))}
          />
        </label>
        <button type="submit" disabled={busy || replaceBlocked}>
          {busy ? "改价中…" : "原地改基产品（仅草稿）"}
        </button>
      </form>
      {replaceBlocked && replaceBlockMessage ? (
        <p className={styles.note} role="status">
          {replaceBlockMessage}
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
          {view.statusLabel}
          {view.publishAllowed ? " · 可发布" : " · 已不可再发布"}
          {view.replaceAllowed ? " · 可改基产品" : ""}
          {view.nextVersionAllowed ? " · 可派生下一版本" : ""}
        </p>
      ) : null}
    </section>
  );
}
