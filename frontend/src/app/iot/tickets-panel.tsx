"use client";

/**
 * IoT 运维工单列表客户端岛 — GET tickets + POST resolve（resolveAllowed）。
 */

import { FormEvent, useState } from "react";
import {
  toMaintenanceTicketView,
  type MaintenanceTicketView,
} from "@/domains/iot/domain/maintenance-ticket-view";
import {
  DEFAULT_IOT_BATTERY,
  fetchMaintenanceTickets,
  postResolveMaintenanceTicket,
} from "@/domains/iot/infrastructure/iot-gateway";
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
      const list = await fetchMaintenanceTickets(
        batteryId.trim() || DEFAULT_IOT_BATTERY,
      );
      setTickets(list.map(toMaintenanceTicketView));
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
      const dto = await postResolveMaintenanceTicket(ticket.ticketId);
      const view = toMaintenanceTicketView(dto);
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
