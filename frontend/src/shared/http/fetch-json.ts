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
      const errMsg =
        typeof body === "object" &&
        body != null &&
        "error" in body &&
        typeof (body as { error: unknown }).error === "string"
          ? (body as { error: string }).error
          : `HTTP ${res.status}`;
      throw new Error(errMsg);
    }
    return body as T;
  } finally {
    if (timer != null) clearTimeout(timer);
  }
}
