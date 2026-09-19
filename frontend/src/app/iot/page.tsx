/**
 * IoT 诊断工作台 — RSC 首屏读工单，操作为客户端岛。
 */

import { PageHeader } from "@/components/page-header";
import {
  DEFAULT_IOT_BATTERY,
  loadTickets,
} from "@/domains/iot/application/load-tickets";
import type { MaintenanceTicketView } from "@/domains/iot/domain/maintenance-ticket-view";
import { IotWorkspace } from "./iot-workspace";
import styles from "./page.module.css";

export default async function IotPage() {
  let tickets: readonly MaintenanceTicketView[] = [];
  let error: string | null = null;

  try {
    tickets = await loadTickets(DEFAULT_IOT_BATTERY);
  } catch (e) {
    error = e instanceof Error ? e.message : "工单拉取失败";
  }

  const openCount = tickets.filter((t) => t.needsAction).length;
  const preview = tickets.slice(0, 3);

  return (
    <>
      <PageHeader
        eyebrow="运营商 · 设备"
        title="设备诊断"
        description="影子新鲜度影响计量换电；通信丢失与 SOC 过时走诊断工单。"
      />
      {error ? (
        <p className={styles.alert} role="alert">
          {error}
        </p>
      ) : (
        <section className={styles.overview} aria-label="工单概览">
          <p className={styles.note}>
            {DEFAULT_IOT_BATTERY} · {tickets.length} 条工单 · 待处理 {openCount}
            （只读 · RSC）
          </p>
          {preview.length > 0 ? (
            <ul className={styles.preview}>
              {preview.map((t) => (
                <li key={t.ticketId}>
                  {t.ticketId} · {t.alertType} · {t.statusLabel}
                  {t.needsAction ? " · 需处理" : ""}
                </li>
              ))}
            </ul>
          ) : (
            <p className={styles.note}>暂无工单</p>
          )}
        </section>
      )}
      <IotWorkspace />
    </>
  );
}
