"use client";

/**
 * IoT 运维工单列表客户端岛 — GET tickets + POST resolve（resolveAllowed）。
 */

import { FormEvent, useState } from "react";
import type { MaintenanceTicketView } from "@/domains/iot/domain/maintenance-ticket-view";
import {
  DEFAULT_IOT_BATTERY,
  loadTickets,
} from "@/domains/iot/application/load-tickets";
import { runResolveMaintenanceTicket } from "@/domains/iot/application/run-resolve-maintenance-ticket";
import styles from "./page.module.css";

export function TicketsPanel() {
  const [batteryId, setBatteryId] = useState(DEFAULT_IOT_BATTERY);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [tickets, setTickets] = useState<MaintenanceTicketView[] | null>(null);
  const [status, setStatus] = useState<string | null>(null);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setStatus(null);
    setTickets(null);
    try {
      setTickets([
        ...(await loadTickets(batteryId.trim() || DEFAULT_IOT_BATTERY)),
      ]);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  async function onResolve(ticket: MaintenanceTicketView) {
    if (!ticket.resolveAllowed) {
      setError(ticket.blockMessage ?? "工单不可解决");
      return;
    }
    setBusy(true);
    setError(null);
    setStatus(null);
    try {
      const view = await runResolveMaintenanceTicket(ticket.ticketId);
      setTickets((prev) =>
        (prev ?? []).map((t) => (t.ticketId === view.ticketId ? view : t)),
      );
      setStatus(`已解决 ${view.ticketId} · ${view.statusLabel}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className={styles.panel}>
      <h2>运维工单列表（HTTP）</h2>
      <p className={styles.note}>
        GET tickets · POST /iot/tickets/{"{id}"}/resolve（仅 OPEN ·
        resolveAllowed）
      </p>
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
      {status ? <p className={styles.note}>{status}</p> : null}
      {tickets ? (
        tickets.length === 0 ? (
          <p>暂无工单</p>
        ) : (
          <ul>
            {tickets.map((t) => (
              <li key={t.ticketId}>
                {t.ticketId} · {t.alertType} · {t.statusLabel}
                {t.needsAction ? " · 可解决" : ` · ${t.blockMessage}`}
                {" "}
                <button
                  type="button"
                  disabled={busy || !t.resolveAllowed}
                  onClick={() => onResolve(t)}
                >
                  解决
                </button>
              </li>
            ))}
          </ul>
        )
      ) : null}
    </section>
  );
}
