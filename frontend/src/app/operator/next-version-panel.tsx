"use client";

/**
 * 派生下一版本草稿客户端岛 — POST /operator/templates/{id}/next-version（AC-25）。
 * 源须 PUBLISHED（nextVersionAllowed）；默认 T-PUB-1 → T-NEXT-1。
 */

import { FormEvent, useMemo, useState } from "react";
import { toOrganizationView } from "@/domains/operator/domain/organization-view";
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
  postCreateNextVersionDraft,
} from "@/domains/operator/infrastructure/operator-gateway";
import styles from "./page.module.css";

const SEED_ACTOR_STATUS = "ACTIVE" as const;
/** 无 GET 前：种子源模板视为已发布。 */
const SEED_SOURCE_STATUS = "PUBLISHED" as const;

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
  const [draftView, setDraftView] = useState<PackageTemplateView | null>(null);

  const actorGate = useMemo(() => {
    const id = actorOrgId.trim() || DEFAULT_PUBLISH_ACTOR_ORG_ID;
    if (id !== DEFAULT_PUBLISH_ACTOR_ORG_ID) {
      return { canAct: true, blockMessage: null as string | null };
    }
    const actor = toOrganizationView({
      id,
      name: "种子操作方",
      status: SEED_ACTOR_STATUS,
      operatorCapability: true,
    });
    return {
      canAct: actor.canActAsManager,
      blockMessage: actor.blockMessage,
      statusLabel: actor.statusLabel,
    };
  }, [actorOrgId]);

  const sourceGate = useMemo(() => {
    const id = sourceTemplateId.trim() || DEFAULT_NEXT_VERSION_SOURCE_ID;
    if (id !== DEFAULT_NEXT_VERSION_SOURCE_ID) {
      return {
        nextVersionAllowed: true,
        blockMessage: null as string | null,
      };
    }
    const view =
      sourceView ??
      toPackageTemplateView({
        id,
        ownerOrgId: DEFAULT_PUBLISH_ACTOR_ORG_ID,
        version: 1,
        status: parsePackageTemplateStatus(SEED_SOURCE_STATUS),
      });
    return {
      nextVersionAllowed: view.nextVersionAllowed,
      blockMessage: view.nextVersionAllowed
        ? null
        : (view.blockMessage ?? "仅已发布模板可派生下一版本"),
    };
  }, [sourceTemplateId, sourceView]);

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
      setSourceView(
        toPackageTemplateView({
          id: sourceTemplateId.trim() || DEFAULT_NEXT_VERSION_SOURCE_ID,
          ownerOrgId: DEFAULT_PUBLISH_ACTOR_ORG_ID,
          version: Math.max(1, r.version - 1),
          status: parsePackageTemplateStatus(SEED_SOURCE_STATUS),
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
      <h2>派生下一版本（HTTP · AC-25）</h2>
      <p className={styles.note}>
        仅 PUBLISHED 可派生（nextVersionAllowed）；操作方须 ACTIVE
        {"statusLabel" in actorGate && actorGate.statusLabel
          ? ` · 种子操作方=${actorGate.statusLabel}`
          : ""}
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
