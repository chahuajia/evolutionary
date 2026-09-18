"use client";

/**
 * IoT 运维工单列表客户端岛 — GET /iot/batteries/{id}/tickets。
 */

import { FormEvent, useState } from "react";
import {
  DEFAULT_IOT_BATTERY,
  fetchMaintenanceTickets,
  type MaintenanceTicketItem,
} from "@/domains/iot/infrastructure/iot-gateway";
import styles from "./page.module.css";

export function TicketsPanel() {
  const [batteryId, setBatteryId] = useState(DEFAULT_IOT_BATTERY);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [tickets, setTickets] = useState<MaintenanceTicketItem[] | null>(null);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setTickets(null);
    try {
      const list = await fetchMaintenanceTickets(
        batteryId.trim() || DEFAULT_IOT_BATTERY,
      );
      setTickets(list);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>运维工单列表（HTTP）</h2>
      <form className={styles.form} onSubmit={onSubmit}>
        <label>
          batteryId
          <input
            value={batteryId}
            onChange={(e) => setBatteryId(e.target.value)}
          />
        </label>
        <button type="submit" disabled={busy}>
          {busy ? "查询中…" : "查询工单"}
        </button>
      </form>
      {error ? (
        <p className={styles.error} role="alert">
          {error}
        </p>
      ) : null}
      {tickets ? (
        tickets.length === 0 ? (
          <p>暂无工单</p>
        ) : (
          <ul>
            {tickets.map((t) => (
              <li key={t.ticketId}>
                {t.ticketId} · {t.alertType} · {t.status}
              </li>
            ))}
          </ul>
        )
      ) : null}
    </section>
  );
}
