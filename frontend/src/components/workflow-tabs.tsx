"use client";

/**
 * 工作流分段切换 — 避免一页堆满表单。
 */

import { useState } from "react";
import styles from "./workflow-tabs.module.css";

export type WorkflowTab = {
  id: string;
  label: string;
  description?: string;
  content: React.ReactNode;
};

export function WorkflowTabs({
  tabs,
  defaultId,
}: {
  tabs: WorkflowTab[];
  defaultId?: string;
}) {
  const [active, setActive] = useState(defaultId ?? tabs[0]?.id);
  const current = tabs.find((t) => t.id === active) ?? tabs[0];

  return (
    <div className={styles.root}>
      <div className={styles.tablist} role="tablist" aria-label="工作流">
        {tabs.map((tab) => {
          const selected = tab.id === current?.id;
          return (
            <button
              key={tab.id}
              type="button"
              role="tab"
              aria-selected={selected}
              className={selected ? styles.tabActive : styles.tab}
              onClick={() => setActive(tab.id)}
            >
              {tab.label}
            </button>
          );
        })}
      </div>
      {current ? (
        <section
          className={styles.panel}
          role="tabpanel"
          aria-label={current.label}
        >
          {current.description ? (
            <p className={styles.panelDesc}>{current.description}</p>
          ) : null}
          {current.content}
        </section>
      ) : null}
    </div>
  );
}
