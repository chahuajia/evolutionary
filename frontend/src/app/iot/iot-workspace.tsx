"use client";

import { WorkflowTabs } from "@/components/workflow-tabs";
import { CommLostPanel } from "./comm-lost-panel";
import { TelemetryPanel } from "./telemetry-panel";
import { TicketsPanel } from "./tickets-panel";
import { TriagePanel } from "./triage-panel";

export function IotWorkspace() {
  return (
    <WorkflowTabs
      defaultId="telemetry"
      tabs={[
        {
          id: "telemetry",
          label: "遥测入影",
          description: "上报 SOC/电压写入 DeviceShadow。",
          content: <TelemetryPanel />,
        },
        {
          id: "comm",
          label: "通信丢失",
          description: "检测通信丢失。",
          content: <CommLostPanel />,
        },
        {
          id: "triage",
          label: "SOC 过时",
          description: "诊断过时影子并开建议。",
          content: <TriagePanel />,
        },
        {
          id: "tickets",
          label: "工单",
          description: "电池相关维护工单列表。",
          content: <TicketsPanel />,
        },
      ]}
    />
  );
}
