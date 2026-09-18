/**
 * 产品壳 — 侧栏导航 + 主区（参考 Stripe/Linear 控制台信息架构，非堆表单）。
 */

"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import styles from "./app-shell.module.css";

const NAV = [
  { href: "/", label: "换电履约", hint: "站点 · 权益" },
  { href: "/credit", label: "信用账户", hint: "购 · 账 · 退" },
  { href: "/mall", label: "商城促销", hint: "券 · 下单" },
  { href: "/iot", label: "设备诊断", hint: "影子 · 工单" },
  { href: "/settlement", label: "分润结算", hint: "意向 · 批" },
  { href: "/operator", label: "运营入驻", hint: "商家 · 套餐" },
] as const;

export function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname() || "/";

  return (
    <div className={styles.shell}>
      <aside className={styles.aside} aria-label="主导航">
        <Link href="/" className={styles.brand}>
          <span className={styles.brandMark}>ACTTO</span>
          <span className={styles.brandSub}>换电控制台</span>
        </Link>
        <nav className={styles.nav}>
          {NAV.map((item) => {
            const active =
              item.href === "/"
                ? pathname === "/"
                : pathname === item.href || pathname.startsWith(`${item.href}/`);
            return (
              <Link
                key={item.href}
                href={item.href}
                className={active ? styles.navItemActive : styles.navItem}
                aria-current={active ? "page" : undefined}
              >
                <span className={styles.navLabel}>{item.label}</span>
                <span className={styles.navHint}>{item.hint}</span>
              </Link>
            );
          })}
        </nav>
        <p className={styles.asideFoot}>本地联调 · Spring :8080</p>
      </aside>
      <div className={styles.mainColumn}>
        <header className={styles.topbar}>
          <p className={styles.topbarNote}>
            运营面按业务链分区：履约 → 信用 → 结算；商城 / IoT / 入驻为平行能力。
          </p>
        </header>
        <div className={styles.content}>{children}</div>
      </div>
    </div>
  );
}
