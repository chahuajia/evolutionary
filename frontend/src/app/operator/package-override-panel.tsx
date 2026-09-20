"use client";

/**
 * 套餐覆盖客户端岛 — POST overrides + GET effective-product（AC-26）。
 */

import { FormEvent, useEffect, useState } from "react";
import type { PackageOverrideView } from "@/domains/operator/domain/package-override-view";
import type { PackageTemplateView } from "@/domains/operator/domain/package-template-view";
import { loadPackageOverride } from "@/domains/operator/application/load-package-override";
import {
  loadEffectiveProduct,
  type EffectiveProductResult,
} from "@/domains/operator/application/load-effective-product";
import { loadPackageTemplate } from "@/domains/operator/application/load-package-template";
import {
  DEFAULT_OVERRIDE_ACTOR_ORG_ID,
  DEFAULT_OVERRIDE_ACTOR_USER_ID,
  DEFAULT_OVERRIDE_ID,
  DEFAULT_OVERRIDE_PRICE_CENTS,
  DEFAULT_OVERRIDE_TEMPLATE_ID,
  runActivatePackageOverride,
} from "@/domains/operator/application/run-activate-package-override";
import { formatCentsAsYuan } from "@/shared/money/format-cents";
import { useActorOrganization } from "./use-actor-organization";
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
  const [view, setView] = useState<PackageOverrideView | null>(null);
  const [overrideMissing, setOverrideMissing] = useState(false);
  const [templateView, setTemplateView] = useState<PackageTemplateView | null>(
    null,
  );
  const [templateLoadError, setTemplateLoadError] = useState<string | null>(
    null,
  );
  const [priceLabel, setPriceLabel] = useState<number | null>(null);
  const [effective, setEffective] = useState<EffectiveProductResult | null>(
    null,
  );
  const actorGate = useActorOrganization(
    actorOrgId,
    DEFAULT_OVERRIDE_ACTOR_ORG_ID,
  );

  useEffect(() => {
    let cancelled = false;
    const id = templateId.trim() || DEFAULT_OVERRIDE_TEMPLATE_ID;
    setTemplateLoadError(null);
    loadPackageTemplate(id)
      .then((view) => {
        if (cancelled) return;
        setTemplateView(view);
      })
      .catch((err) => {
        if (cancelled) return;
        setTemplateView(null);
        setTemplateLoadError(
          err instanceof Error ? err.message : String(err),
        );
      });
    return () => {
      cancelled = true;
    };
  }, [templateId]);

  useEffect(() => {
    let cancelled = false;
    const id = overrideId.trim() || DEFAULT_OVERRIDE_ID;
    setOverrideMissing(false);
    loadPackageOverride(id)
      .then(({ view: next, priceCents: cents }) => {
        if (cancelled) return;
        setOverrideMissing(false);
        setView(next);
        setPriceLabel(cents);
      })
      .catch(() => {
        if (cancelled) return;
        // 404：尚无覆盖，可新建激活
        setView(null);
        setPriceLabel(null);
        setOverrideMissing(true);
      });
    return () => {
      cancelled = true;
    };
  }, [overrideId]);

  async function onActivate(e: FormEvent) {
    e.preventDefault();
    if (!actorGate.canAct) {
      setError(actorGate.blockMessage ?? "操作方组织不可激活覆盖");
      return;
    }
    if (!templateView?.overrideActivateAllowed) {
      setError(
        templateView == null
          ? (templateLoadError ?? "正在加载模板…")
          : (templateView.blockMessage ?? "当前模板不可激活覆盖"),
      );
      return;
    }
    if (view && !view.activateAllowed) {
      setError(view.blockMessage ?? "当前状态不可激活");
      return;
    }
    setBusy(true);
    setError(null);
    try {
      const r = await runActivatePackageOverride({
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
      setOverrideMissing(false);
      setView(r.view);
      setPriceLabel(r.priceCents);
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
      const r = await loadEffectiveProduct(
        actorOrgId.trim() || DEFAULT_OVERRIDE_ACTOR_ORG_ID,
        templateId.trim() || DEFAULT_OVERRIDE_TEMPLATE_ID,
      );
      setEffective(r);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  const activateBlocked =
    !actorGate.canAct ||
    templateView == null ||
    !templateView.overrideActivateAllowed ||
    (view != null && !view.activateAllowed);
  const blockMessage = !actorGate.canAct
    ? actorGate.blockMessage
    : templateView == null
      ? (templateLoadError ?? "正在加载模板…")
      : !templateView.overrideActivateAllowed
        ? (templateView.blockMessage ?? "当前模板不可激活覆盖")
        : view != null && !view.activateAllowed
          ? view.blockMessage
          : null;

  return (
    <section className={styles.panel}>
      <h2>套餐覆盖与有效价（HTTP · AC-26）</h2>
      <p className={styles.note}>
        GET 模板须已发布；GET 覆盖对齐 activateAllowed（缺省则可新建）
        {actorGate.statusLabel
          ? ` · ${actorOrgId}=${actorGate.statusLabel}`
          : ""}
        {templateView ? ` · ${templateView.id}=${templateView.statusLabel}` : ""}
        {view
          ? ` · ${view.overrideId}=${view.statusLabel}`
          : overrideMissing
            ? " · 覆盖尚不存在"
            : ""}
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
              setPriceCents(
                Number(e.target.value) || DEFAULT_OVERRIDE_PRICE_CENTS,
              )
            }
          />
        </label>
        <button type="submit" disabled={busy || activateBlocked}>
          {busy ? "提交中…" : "激活覆盖"}
        </button>
      </form>
      <form className={styles.form} onSubmit={onQueryEffective}>
        <button type="submit" disabled={busy}>
          {busy ? "查询中…" : "查询有效价"}
        </button>
      </form>
      {activateBlocked && blockMessage ? (
        <p className={styles.note} role="status">
          {blockMessage}
          {view?.revokeAllowed ? "（可去「撤销覆盖」页）" : ""}
        </p>
      ) : null}
      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}
      {view ? (
        <p>
          覆盖 {view.overrideId} · {view.orgId || "—"} · {view.templateId} v
          {view.templateVersion} · {view.statusLabel}
          {view.revokeAllowed ? " · 可撤销" : ""}
          {priceLabel != null ? ` · ¥${formatCentsAsYuan(priceLabel)}` : ""}
        </p>
      ) : null}
      {effective ? (
        <p>
          有效价 ¥{formatCentsAsYuan(effective.priceCents)} ·{" "}
          {effective.displayName || "—"} · {effective.durationDays}天 ·{" "}
          {effective.templateId} v{effective.templateVersion}
          {effective.overrideId
            ? ` · override ${effective.overrideId}`
            : " · 无覆盖"}
        </p>
      ) : null}
    </section>
  );
}
