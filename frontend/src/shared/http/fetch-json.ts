/**
 * 轻量 fetch JSON 助手（可选超时）。
 */

export type FetchJsonOptions = RequestInit & {
  /** 超时毫秒；默认不启用 */
  timeoutMs?: number;
};

export async function fetchJson<T = unknown>(
  url: string,
  options: FetchJsonOptions = {},
): Promise<T> {
  const { timeoutMs, signal: outerSignal, ...init } = options;
  const controller = timeoutMs != null ? new AbortController() : null;
  const timer =
    controller && timeoutMs != null
      ? setTimeout(() => controller.abort(), timeoutMs)
      : null;

  try {
    const res = await fetch(url, {
      ...init,
      signal: controller?.signal ?? outerSignal,
    });
    const body = await res.json().catch(() => ({}));
    if (!res.ok) {
      const record =
        typeof body === "object" && body != null
          ? (body as Record<string, unknown>)
          : null;
      const errorCode =
        record && typeof record.error === "string" ? record.error : null;
      const suggestion =
        record && typeof record.suggestion === "string"
          ? record.suggestion
          : null;
      const base = errorCode ?? `HTTP ${res.status}`;
      const errMsg =
        suggestion != null && suggestion.length > 0
          ? `${base}: ${suggestion}`
          : base;
      throw new Error(errMsg);
    }
    return body as T;
  } finally {
    if (timer != null) clearTimeout(timer);
  }
}
