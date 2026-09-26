"use client";

/**
 * 应用信用政策客户端岛 — 默认 U1 / policyVersion=2；成功展示档案读模型。
 * GET profile 预读当前档案（厚 GET）。
 */

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import {
  DEFAULT_CREDIT_USER,
  loadCreditProfile,
} from "@/domains/credit/application/load-credit-profile";
import { runApplyCreditPolicy } from "@/domains/credit/application/run-apply-credit-policy";
import type { CreditProfileView } from "@/domains/credit/domain/credit-profile-view";
import styles from "./page.module.css";

const DEFAULT_POLICY_VERSION = 2;

function summarizeProfile(p: CreditProfileView): string {
  return [
    `用户 ${p.userId}`,
    `额度 ¥${p.limitYuan}`,
    `已用 ¥${p.usedYuan}`,
    `${p.statusLabel}`,
    `档 ${p.scoreTier}`,
    `政策 v${p.policyVersion}`,
  ].join(" · ");
}

export function CreditApplyPolicyPanel() {
  const router = useRouter();
  const [userId, setUserId] = useState(DEFAULT_CREDIT_USER);
  const [policyVersion, setPolicyVersion] = useState(DEFAULT_POLICY_VERSION);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<string | null>(null);
  const [preload, setPreload] = useState<CreditProfileView | null>(null);
  const [preloadError, setPreloadError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    const id = userId.trim() || DEFAULT_CREDIT_USER;
    setPreloadError(null);
    loadCreditProfile(id)
      .then((p) => {
        if (cancelled) return;
        setPreload(p);
      })
      .catch((err) => {
        if (cancelled) return;
        setPreload(null);
        setPreloadError(err instanceof Error ? err.message : String(err));
      });
    return () => {
      cancelled = true;
    };
  }, [userId]);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setResult(null);
    try {
      const profile = await runApplyCreditPolicy({
        userId: userId.trim() || DEFAULT_CREDIT_USER,
        policyVersion,
      });
      setResult(summarizeProfile(profile));
      setPreload(profile);
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>应用信用政策</h2>
      <p className={styles.note}>
        GET profile 预读当前档案
        {preload
          ? ` · ${summarizeProfile(preload)}`
          : preloadError
            ? ` · ${preloadError}`
            : " · 加载中…"}
      </p>
      <form className={styles.repayForm} onSubmit={onSubmit}>
        <label>
          userId
          <input
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
          />
        </label>
        <label>
          policyVersion
          <input
            type="number"
            value={policyVersion}
            onChange={(e) => setPolicyVersion(Number(e.target.value))}
          />
        </label>
        <button type="submit" disabled={busy}>
          {busy ? "提交中…" : "应用政策"}
        </button>
      </form>
      {error ? (
        <p className={styles.note} role="alert">
          {error}
        </p>
      ) : null}
      {result ? <p className={styles.note}>{result}</p> : null}
    </section>
  );
}
