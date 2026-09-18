/**
 * IoT 诊断页 — 遥测入影 + COMM_LOST + SOC 过时诊断客户端岛（对接 Spring HTTP）。
 */

import Link from "next/link";
import {
  DEFAULT_IOT_BATTERY,
  DEFAULT_TELEMETRY_SOC,
  DEFAULT_TELEMETRY_VENDOR,
  DEFAULT_TELEMETRY_VOLTAGE_MILLI,
} from "@/domains/iot/infrastructure/iot-gateway";
import { CommLostPanel } from "./comm-lost-panel";
import { TelemetryPanel } from "./telemetry-panel";
import { TicketsPanel } from "./tickets-panel";
import { TriagePanel } from "./triage-panel";
import styles from "./page.module.css";

export default function IotPage() {
  return (
    <main className={styles.main}>
      <nav className={styles.nav}>
        <Link href="/">← 换电首页</Link>
      </nav>

      <h1 className={styles.title}>IoT 诊断</h1>
      <p className={styles.note}>
        客户端岛调用{" "}
        <code>
          POST /iot/batteries/{"{batteryId}"}/telemetry
        </code>
        、{" "}
        <code>
          POST /iot/batteries/{"{batteryId}"}/detect-comm-lost
        </code>{" "}
        与{" "}
        <code>
          POST /iot/batteries/{"{batteryId}"}/triage-outdated-soc
        </code>
        ；默认电池 <code>{DEFAULT_IOT_BATTERY}</code>。遥测默认 body{" "}
        <code>
          {`{"vendorId":"${DEFAULT_TELEMETRY_VENDOR}","soc":${DEFAULT_TELEMETRY_SOC},"voltageMilli":${DEFAULT_TELEMETRY_VOLTAGE_MILLI}}`}
        </code>
        ；成功展示 soc / stale / nextStep；错误经 fetchJson suggestion。
      </p>

      <TelemetryPanel />
      <CommLostPanel />
      <TriagePanel />
      <TicketsPanel />
    </main>
  );
}
