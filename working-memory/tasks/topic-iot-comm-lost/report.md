# topic/iot-comm-lost 报告

**分支**：`topic/iot-comm-lost`  
**日期**：2026-09-17

| AC | 实现 |
| :-- | :--- |
| AC-60 | `DetectCommLost`：stale → `BatteryAlertRaised COMM_LOST` + `MaintenanceTicket` OPEN（不重复开单） |
| AC-61 | `TriageOutdatedSoc`：先 shadow.stale/lastSeenAt，再 CHECK_ADAPTER |

测试：`CommLostAndTriageTest`（5）
