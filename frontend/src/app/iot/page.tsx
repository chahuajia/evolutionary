/**
 * IoT 诊断页 — COMM_LOST 检测为客户端岛（对接即将存在的 Spring HTTP）。
 */

import Link from "next/link";
import { DEFAULT_IOT_BATTERY } from "@/domains/iot/infrastructure/iot-gateway";
import { CommLostPanel } from "./comm-lost-panel";
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
          POST /iot/batteries/{"{batteryId}"}/detect-comm-lost
        </code>
        ；默认电池 <code>{DEFAULT_IOT_BATTERY}</code>。展示 stale / COMM_LOST /
        ticket；错误经 fetchJson suggestion。
      </p>

      <CommLostPanel />
    </main>
  );
}
