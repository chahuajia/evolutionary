/**
 * IoT 诊断工作台 — RSC 首屏读工单，操作为客户端岛。
 */

import { PageHeader } from "@/components/page-header";
import {
  DEFAULT_IOT_BATTERY,
  loadTickets,
} from "@/domains/iot/application/load-tickets";
import type { MaintenanceTicketItem } from "@/domains/iot/infrastructure/iot-gateway";
import { IotWorkspace } from "./iot-workspace";
import styles from "./page.module.css";

export default async function IotPage() {
  let tickets: readonly MaintenanceTicketItem[] = [];
  let error: string | null = null;

  try {
    tickets = await loadTickets(DEFAULT_IOT_BATTERY);
  } catch (e) {
    error = e instanceof Error ? e.message : "工单拉取失败";
  }

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
            {DEFAULT_IOT_BATTERY} · {tickets.length} 条工单（只读 · RSC）
          </p>
          {preview.length > 0 ? (
            <ul className={styles.preview}>
              {preview.map((t) => (
                <li key={t.ticketId}>
                  {t.ticketId} · {t.alertType} · {t.status}
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
