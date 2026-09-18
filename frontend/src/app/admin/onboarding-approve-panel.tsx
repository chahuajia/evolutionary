"use client";

/**
 * 批准商家入驻客户端岛 — POST /admin/onboarding/{id}/approve（AC-40 · 26a）。
 */

import { FormEvent, useState } from "react";
import {
  DEFAULT_ONBOARDING_APPLICATION_ID,
  DEFAULT_SHOP_NAME,
  postApproveOnboarding,
  type ApproveOnboardingResult,
} from "@/domains/operator/infrastructure/operator-gateway";
import styles from "./page.module.css";

export function OnboardingApprovePanel() {
  const [applicationId, setApplicationId] = useState(
    DEFAULT_ONBOARDING_APPLICATION_ID,
  );
  const [shopName, setShopName] = useState(DEFAULT_SHOP_NAME);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<ApproveOnboardingResult | null>(null);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const r = await postApproveOnboarding({
        applicationId:
          applicationId.trim() || DEFAULT_ONBOARDING_APPLICATION_ID,
        shopName: shopName.trim() || DEFAULT_SHOP_NAME,
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
      <h2>平台批准商家入驻（HTTP · AC-40）</h2>
      <form className={styles.form} onSubmit={onSubmit}>
        <label>
          applicationId
          <input
            value={applicationId}
            onChange={(e) => setApplicationId(e.target.value)}
          />
        </label>
        <label>
          shopName
          <input
            value={shopName}
            onChange={(e) => setShopName(e.target.value)}
          />
        </label>
        <button type="submit" disabled={busy}>
          {busy ? "批准中…" : "批准入驻"}
        </button>
      </form>
      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}
      {result ? (
        <p>
          商家 {result.orgId} · {result.shopName} · {result.status}
        </p>
      ) : null}
    </section>
  );
}
