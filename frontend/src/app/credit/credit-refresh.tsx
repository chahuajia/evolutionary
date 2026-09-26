"use client";

/**
 * 客户端岛：仅负责 router.refresh()，读模型仍由 RSC 拉取。
 */

import { useRouter } from "next/navigation";
import { useState, useTransition } from "react";
import styles from "./credit-refresh.module.css";

export function CreditRefreshButton({ className }: { className?: string }) {
  const router = useRouter();
  const [pending, startTransition] = useTransition();
  const [hint, setHint] = useState<string | null>(null);

  return (
    <span className={`${styles.wrap} ${className ?? ""}`}>
      <button
        type="button"
        className={styles.btn}
        disabled={pending}
        onClick={() => {
          setHint(null);
          startTransition(() => {
            router.refresh();
            setHint("已请求刷新");
          });
        }}
      >
        {pending ? "刷新中…" : "刷新档案"}
      </button>
      {hint ? <span className={styles.hint}>{hint}</span> : null}
    </span>
  );
}
