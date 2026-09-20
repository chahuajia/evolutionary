"use client";

/**
 * 批准运营商下线客户端岛 — POST /operator/onboarding/{id}/approve-downline（29a/29b）。
 * 批的是运营商下线，不是商城商家（商家走 /admin）。
 * GET 申请对齐 approveAllowed（仅 SUBMITTED）。
 */

import { FormEvent, useEffect, useState } from "react";
import {
  approveOperatorDownline,
  DEFAULT_DOWNLINE_ACTOR_ORG_ID,
  DEFAULT_DOWNLINE_ACTOR_USER_ID,
  DEFAULT_DOWNLINE_APPLICATION_ID,
} from "@/domains/operator/application/approve-operator-downline";
import { loadOnboardingApplication } from "@/domains/operator/application/load-onboarding-application";
import type { OnboardingApplicationView } from "@/domains/operator/domain/onboarding-application-view";
import {
  toOrganizationView,
  type OrganizationView,
} from "@/domains/operator/domain/organization-view";
import { useActorOrganization } from "./use-actor-organization";
import styles from "./page.module.css";

export function ApproveDownlinePanel() {
  const [applicationId, setApplicationId] = useState(
    DEFAULT_DOWNLINE_APPLICATION_ID,
  );
  const [actorOrgId, setActorOrgId] = useState(DEFAULT_DOWNLINE_ACTOR_ORG_ID);
  const [actorUserId, setActorUserId] = useState(
    DEFAULT_DOWNLINE_ACTOR_USER_ID,
  );
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [appView, setAppView] = useState<OnboardingApplicationView | null>(
    null,
  );
  const [appLoadError, setAppLoadError] = useState<string | null>(null);
  const [orgView, setOrgView] = useState<OrganizationView | null>(null);
  const actorGate = useActorOrganization(
    actorOrgId,
    DEFAULT_DOWNLINE_ACTOR_ORG_ID,
  );

  useEffect(() => {
    let cancelled = false;
    const id = applicationId.trim() || DEFAULT_DOWNLINE_APPLICATION_ID;
    setAppLoadError(null);
    loadOnboardingApplication(id)
      .then((view) => {
        if (cancelled) return;
        setAppView(view);
      })
      .catch((err) => {
        if (cancelled) return;
        setAppView(null);
        setAppLoadError(err instanceof Error ? err.message : String(err));
      });
    return () => {
      cancelled = true;
    };
  }, [applicationId]);

  const approveAllowed =
    appView != null && appView.approveAllowed && actorGate.canAct;
  const blockMessage =
    appView == null
      ? (appLoadError ?? "正在加载入驻申请…")
      : !appView.approveAllowed
        ? appView.blockMessage
        : !actorGate.canAct
          ? actorGate.blockMessage
          : null;

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (!approveAllowed) {
      setError(blockMessage ?? "当前状态不可批准");
      return;
    }
    setBusy(true);
    setError(null);
    setOrgView(null);
    try {
      const id = applicationId.trim() || DEFAULT_DOWNLINE_APPLICATION_ID;
      const r = await approveOperatorDownline({
        applicationId: id,
        actorOrgId: actorOrgId.trim() || DEFAULT_DOWNLINE_ACTOR_ORG_ID,
        actorUserId: actorUserId.trim() || DEFAULT_DOWNLINE_ACTOR_USER_ID,
      });
      setOrgView(
        toOrganizationView({
          id: r.orgId,
          name: r.name,
          parentId: r.parentOrgId,
          status: r.status || "ACTIVE",
          operatorCapability: r.operatorCapability,
        }),
      );
      setAppView(await loadOnboardingApplication(id));
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>批运营商下线（HTTP · 29a）</h2>
      <p className={styles.note}>
        GET 申请对齐仅 SUBMITTED 可批；商家入驻走 /admin
        {appView
          ? ` · ${appView.id}=${appView.statusLabel}`
          : ""}
        {appView?.approveAllowed ? " · 可批准" : ""}
        {actorGate.statusLabel
          ? ` · ${actorOrgId}=${actorGate.statusLabel}`
          : ""}
      </p>
      <form className={styles.form} onSubmit={onSubmit}>
        <label>
          applicationId
          <input
            value={applicationId}
            onChange={(e) => setApplicationId(e.target.value)}
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
        <button type="submit" disabled={busy || !approveAllowed}>
          {busy ? "批准中…" : "批下线"}
        </button>
      </form>
      {!approveAllowed && blockMessage ? (
        <p className={styles.note} role="status">
          {blockMessage}
        </p>
      ) : null}
      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}
      {orgView ? (
        <p>
          下线组织 {orgView.id} · {orgView.name} · {orgView.statusLabel}
          {orgView.parentId ? ` · parent ${orgView.parentId}` : ""}
          {orgView.operatorCapability ? " · OPERATOR" : ""}
          {orgView.active ? " · 可参与授权" : ""}
          {orgView.blockMessage ? ` · ${orgView.blockMessage}` : ""}
        </p>
      ) : null}
    </section>
  );
}
