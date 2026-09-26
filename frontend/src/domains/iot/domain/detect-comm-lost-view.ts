/**
 * 通信丢失检测展示模型 — 视图边界。
 *
 * 对齐后端 `DetectCommLost` / `AlertType`：
 * - 仅 stale 时抬 COMM_LOST 有意义
 * - alertType 有则 parse；无则 null（勿岛内猜 COMM_LOST）
 */

import {
  ALERT_TYPE_LABEL,
  parseAlertType,
  type AlertType,
} from "./alert-type";
import { canDetectCommLost } from "./device-shadow-view";

export type { AlertType };
export { parseAlertType, ALERT_TYPE_LABEL };

export type DetectCommLostView = {
  readonly batteryId: string;
  readonly stale: boolean;
  readonly staleLabel: string;
  readonly raised: boolean;
  readonly raisedLabel: string;
  readonly alertType: AlertType | null;
  readonly alertTypeLabel: string | null;
  readonly ticketId: string | null;
  /** 本次结果上的 stale 是否仍值得检测（对齐 canDetectCommLost）。 */
  readonly detectUseful: boolean;
};

export function toDetectCommLostView(dto: {
  batteryId: string;
  stale: boolean;
  raised: boolean;
  alertType: string | null;
  ticketId: string | null;
}): DetectCommLostView {
  const alertType =
    dto.alertType != null && String(dto.alertType).length > 0
      ? parseAlertType(dto.alertType)
      : null;
  return {
    batteryId: dto.batteryId,
    stale: dto.stale,
    staleLabel: dto.stale ? "过期" : "新鲜",
    raised: dto.raised,
    raisedLabel: dto.raised ? "已抬告警" : "未抬告警",
    alertType,
    alertTypeLabel: alertType ? ALERT_TYPE_LABEL[alertType] : null,
    ticketId: dto.ticketId,
    detectUseful: canDetectCommLost(dto.stale),
  };
}
