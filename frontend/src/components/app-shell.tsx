/**
 * 产品壳 — 四端角色侧栏 + 主区（演示级，无真登录）。
 */

"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import styles from "./app-shell.module.css";

export type ConsoleRole = "consumer" | "merchant" | "operator" | "admin";

const ROLE_STORAGE_KEY = "actto.console.role";

const ROLES: { id: ConsoleRole; label: string }[] = [
  { id: "consumer", label: "消费者" },
  { id: "merchant", label: "商家" },
  { id: "operator", label: "运营商" },
  { id: "admin", label: "总后台" },
];

type NavItem = { href: string; label: string; hint: string };

const NAV_BY_ROLE: Record<ConsoleRole, readonly NavItem[]> = {
  consumer: [
    { href: "/", label: "换电履约", hint: "站点 · 权益" },
    { href: "/credit", label: "信用", hint: "购 · 账 · 退" },
    { href: "/mall", label: "商城", hint: "券 · 下单" },
  ],
  merchant: [
    { href: "/merchant", label: "入驻进度", hint: "平台审批中" },
  ],
  operator: [
    { href: "/operator", label: "运营配置", hint: "套餐 · 覆盖" },
    { href: "/iot", label: "设备诊断", hint: "影子 · 工单" },
    { href: "/settlement", label: "分润结算", hint: "全网意向 · 批" },
  ],
  admin: [
    { href: "/admin", label: "平台审批", hint: "商家入驻" },
  ],
};

const ROLE_UI: Record<
  ConsoleRole,
  { brandSub: string; home: string; topbar: string }
> = {
  consumer: {
    brandSub: "消费者端",
    home: "/",
    topbar: "消费者端：换电履约、信用账户与商城促销。",
  },
  merchant: {
    brandSub: "商家端",
    home: "/merchant",
    topbar: "商家端：商城商家入驻进度（由总后台平台审批）。",
  },
  operator: {
    brandSub: "运营商端",
    home: "/operator",
    topbar: "运营商端：套餐发布/覆盖、设备诊断与分润结算。",
  },
  admin: {
    brandSub: "总后台",
    home: "/admin",
    topbar: "总后台：平台批准商家入驻。",
  },
};

function isValidRole(value: string | null): value is ConsoleRole {
  return (
    value === "consumer" ||
    value === "merchant" ||
    value === "operator" ||
    value === "admin"
  );
}

function pathInNav(pathname: string, nav: readonly NavItem[]): boolean {
  return nav.some((item) =>
    item.href === "/"
      ? pathname === "/"
      : pathname === item.href || pathname.startsWith(`${item.href}/`),
  );
}

export function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname() || "/";
  const router = useRouter();
  const [role, setRole] = useState<ConsoleRole>("consumer");
  const [hydrated, setHydrated] = useState(false);

  useEffect(() => {
    const stored = localStorage.getItem(ROLE_STORAGE_KEY);
    if (isValidRole(stored)) {
      setRole(stored);
    }
    setHydrated(true);
  }, []);

  useEffect(() => {
    if (!hydrated) return;
    const nav = NAV_BY_ROLE[role];
    if (!pathInNav(pathname, nav)) {
      router.push(ROLE_UI[role].home);
    }
  }, [hydrated, role, pathname, router]);

  const nav = NAV_BY_ROLE[role];
  const ui = ROLE_UI[role];

  function selectRole(next: ConsoleRole) {
    setRole(next);
    localStorage.setItem(ROLE_STORAGE_KEY, next);
  }

  return (
    <div className={styles.shell}>
      <aside className={styles.aside} aria-label="主导航">
        <Link href={ui.home} className={styles.brand}>
          <span className={styles.brandMark}>ACTTO</span>
          <span className={styles.brandSub}>{ui.brandSub}</span>
        </Link>

        <div className={styles.roleSwitch} role="group" aria-label="角色切换">
          {ROLES.map((item) => (
            <button
              key={item.id}
              type="button"
              className={
                role === item.id ? styles.roleChipActive : styles.roleChip
              }
              aria-pressed={role === item.id}
              onClick={() => selectRole(item.id)}
            >
              {item.label}
            </button>
          ))}
        </div>

        <nav className={styles.nav}>
          {nav.map((item) => {
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
          <p className={styles.topbarNote}>{ui.topbar}</p>
        </header>
        <div className={styles.content}>{children}</div>
      </div>
    </div>
  );
}
