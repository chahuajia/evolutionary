"use client";

import { WorkflowTabs } from "@/components/workflow-tabs";
import { DefaultSelectPanel } from "./default-select-panel";
import { EntitledSwapPanel } from "./entitled-swap-panel";
import { MeteredSwapPanel } from "./metered-swap-panel";
import { SwapPanel } from "./swap-panel";
import type { StationView } from "@/domains/swap/domain/station-view";

export function HomeWorkflows({
  stations,
  initialStationId,
  listError,
}: {
  stations: readonly StationView[];
  initialStationId: string;
  listError: string | null;
}) {
  return (
    <WorkflowTabs
      defaultId="station"
      tabs={[
        {
          id: "station",
          label: "站点换电",
          description: "选站 → 详情 → 发起换电（压测主路径）。",
          content: (
            <SwapPanel
              stations={stations}
              initialStationId={initialStationId}
              listError={listError}
            />
          ),
        },
        {
          id: "entitled",
          label: "权益换电",
          description: "持卡用户指定权益履约。",
          content: <EntitledSwapPanel />,
        },
        {
          id: "default",
          label: "默认选卡",
          description: "省略 entitlementId，后端优先 FINITE。",
          content: <DefaultSelectPanel />,
        },
        {
          id: "metered",
          label: "计量换电",
          description: "PAY_AS_YOU_GO · SOC 差值计费。",
          content: <MeteredSwapPanel />,
        },
      ]}
    />
  );
}
