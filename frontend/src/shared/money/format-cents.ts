/**
 * 分→元展示 — 共享内核（无单边业务所有权）。
 * 各 BC 视图只组合字段，不各自再写一份 toFixed。
 */

export function formatCentsAsYuan(cents: number): string {
  return (cents / 100).toFixed(2);
}
