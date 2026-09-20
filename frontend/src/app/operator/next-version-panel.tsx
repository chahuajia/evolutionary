"use client";

/**
 * 派生下一版本草稿客户端岛 — POST /operator/templates/{id}/next-version（AC-25）。
 * 源须 PUBLISHED（nextVersionAllowed）；默认 T-PUB-1 → T-NEXT-1。
 */

import { FormEvent, useEffect, useMemo, useState } from "react";
import {
  parsePackageTemplateStatus,
  toPackageTemplateView,
  type PackageTemplateView,
} from "@/domains/operator/domain/package-template-view";
import {
  DEFAULT_NEXT_VERSION_DISPLAY_NAME,
  DEFAULT_NEXT_VERSION_DURATION_DAYS,
  DEFAULT_NEXT_VERSION_NEW_ID,
  DEFAULT_NEXT_VERSION_PRICE_CENTS,
  DEFAULT_NEXT_VERSION_SOURCE_ID,
  DEFAULT_PUBLISH_ACTOR_ORG_ID,
  DEFAULT_PUBLISH_ACTOR_USER_ID,
  fetchPackageTemplate,
  postCreateNextVersionDraft,
} from "@/domains/operator/infrastructure/operator-gateway";
import { useActorOrganization } from "./use-actor-organization";
import styles from "./page.module.css";

export function NextVersionPanel() {
  const [sourceTemplateId, setSourceTemplateId] = useState(
    DEFAULT_NEXT_VERSION_SOURCE_ID,
  );
  const [newTemplateId, setNewTemplateId] = useState(
    DEFAULT_NEXT_VERSION_NEW_ID,
  );
  const [actorOrgId, setActorOrgId] = useState(DEFAULT_PUBLISH_ACTOR_ORG_ID);
  const [actorUserId, setActorUserId] = useState(
    DEFAULT_PUBLISH_ACTOR_USER_ID,
  );
  const [displayName, setDisplayName] = useState(
    DEFAULT_NEXT_VERSION_DISPLAY_NAME,
  );
  const [priceCents, setPriceCents] = useState(DEFAULT_NEXT_VERSION_PRICE_CENTS);
  const [durationDays, setDurationDays] = useState(
    DEFAULT_NEXT_VERSION_DURATION_DAYS,
  );
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [sourceView, setSourceView] = useState<PackageTemplateView | null>(
    null,
  );
  const [sourceLoadError, setSourceLoadError] = useState<string | null>(null);
  const [draftView, setDraftView] = useState<PackageTemplateView | null>(null);
  const actorGate = useActorOrganization(
    actorOrgId,
    DEFAULT_PUBLISH_ACTOR_ORG_ID,
  );

  useEffect(() => {
    let cancelled = false;
    const id = sourceTemplateId.trim() || DEFAULT_NEXT_VERSION_SOURCE_ID;
    setSourceLoadError(null);
    fetchPackageTemplate(id)
      .then((dto) => {
        if (cancelled) return;
        setSourceView(
          toPackageTemplateView({
            id: dto.templateId,
            ownerOrgId: dto.ownerOrgId,
            version: dto.version,
            status: parsePackageTemplateStatus(dto.status),
          }),
        );
      })
      .catch((err) => {
        if (cancelled) return;
        setSourceView(null);
        setSourceLoadError(err instanceof Error ? err.message : String(err));
      });
    return () => {
      cancelled = true;
    };
  }, [sourceTemplateId]);

  const sourceGate = useMemo(() => {
    if (!sourceView) {
      return {
        nextVersionAllowed: false,
        blockMessage: sourceLoadError ?? "正在加载源模板…",
      };
    }
    return {
      nextVersionAllowed: sourceView.nextVersionAllowed,
      blockMessage: sourceView.nextVersionAllowed
        ? null
        : (sourceView.blockMessage ?? "仅已发布模板可派生下一版本"),
    };
  }, [sourceView, sourceLoadError]);

  const deriveBlocked = !actorGate.canAct || !sourceGate.nextVersionAllowed;
  const blockMessage = !actorGate.canAct
    ? actorGate.blockMessage
    : sourceGate.blockMessage;

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (deriveBlocked) {
      setError(blockMessage ?? "不可派生下一版本");
      return;
    }
    setBusy(true);
    setError(null);
    setDraftView(null);
    try {
      const r = await postCreateNextVersionDraft({
        sourceTemplateId:
          sourceTemplateId.trim() || DEFAULT_NEXT_VERSION_SOURCE_ID,
        newTemplateId: newTemplateId.trim() || DEFAULT_NEXT_VERSION_NEW_ID,
        actorOrgId: actorOrgId.trim() || DEFAULT_PUBLISH_ACTOR_ORG_ID,
        actorUserId: actorUserId.trim() || DEFAULT_PUBLISH_ACTOR_USER_ID,
        displayName: displayName.trim() || DEFAULT_NEXT_VERSION_DISPLAY_NAME,
        priceCents: Number.isFinite(priceCents)
          ? priceCents
          : DEFAULT_NEXT_VERSION_PRICE_CENTS,
        durationDays: Number.isFinite(durationDays)
          ? durationDays
          : DEFAULT_NEXT_VERSION_DURATION_DAYS,
      });
      setDraftView(
        toPackageTemplateView({
          id: r.templateId,
          ownerOrgId: r.ownerOrgId,
          version: r.version,
          status: parsePackageTemplateStatus(r.status),
        }),
      );
      const srcId = sourceTemplateId.trim() || DEFAULT_NEXT_VERSION_SOURCE_ID;
      try {
        const src = await fetchPackageTemplate(srcId);
        setSourceView(
          toPackageTemplateView({
            id: src.templateId,
            ownerOrgId: src.ownerOrgId,
            version: src.version,
            status: parsePackageTemplateStatus(src.status),
          }),
        );
      } catch {
        /* 源模板再读失败不阻断派生结果展示 */
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>派生下一版本（HTTP · AC-25）</h2>
      <p className={styles.note}>
        仅 PUBLISHED 可派生（GET 模板 · nextVersionAllowed）；GET 组织对齐
        canActAsManager
        {actorGate.statusLabel
          ? ` · ${actorOrgId}=${actorGate.statusLabel}`
          : ""}
        {sourceView ? ` · 源 ${sourceView.id}=${sourceView.status}` : ""}
      </p>
      <form className={styles.form} onSubmit={onSubmit}>
        <label>
          sourceTemplateId
          <input
            value={sourceTemplateId}
            onChange={(e) => {
              setSourceTemplateId(e.target.value);
              setSourceView(null);
              setDraftView(null);
            }}
          />
        </label>
        <label>
          newTemplateId
          <input
            value={newTemplateId}
            onChange={(e) => setNewTemplateId(e.target.value)}
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
            min={0}
            value={priceCents}
            onChange={(e) =>
              setPriceCents(
                Number(e.target.value) || DEFAULT_NEXT_VERSION_PRICE_CENTS,
              )
            }
          />
        </label>
        <label>
          durationDays
          <input
            type="number"
            min={1}
            value={durationDays}
            onChange={(e) =>
              setDurationDays(
                Number(e.target.value) || DEFAULT_NEXT_VERSION_DURATION_DAYS,
              )
            }
          />
        </label>
        <button type="submit" disabled={busy || deriveBlocked}>
          {busy ? "派生中…" : "派生下一版本草稿"}
        </button>
      </form>
      {deriveBlocked && blockMessage ? (
        <p className={styles.note} role="status">
          {blockMessage}
        </p>
      ) : null}
      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}
      {draftView ? (
        <p>
          新草稿 {draftView.id} · {draftView.ownerOrgId || "—"} · v
          {draftView.version} · {draftView.status}
          {draftView.publishAllowed ? " · 可发布" : ""}
        </p>
      ) : null}
    </section>
  );
}
