/**
 * IoT 诊断工作台。
 */

import { PageHeader } from "@/components/page-header";
import { IotWorkspace } from "./iot-workspace";

export default function IotPage() {
  return (
    <>
      <PageHeader
        eyebrow="运营商 · 设备"
        title="设备诊断"
        description="影子新鲜度影响计量换电；通信丢失与 SOC 过时走诊断工单。"
      />
      <IotWorkspace />
    </>
  );
}
