/**
 * 产品壳 — 角色三分端侧栏 + 主区（演示级，无真登录）。
 */

"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import styles from "./app-shell.module.css";

export type ConsoleRole = "consumer" | "shop" | "operator";

const ROLE_STORAGE_KEY = "actto.console.role";

const ROLES: { id: ConsoleRole; label: string }[] = [
  { id: "consumer", label: "消费者" },
  { id: "shop", label: "店主" },
  { id: "operator", label: "运营商" },
];

type NavItem = { href: string; label: string; hint: string };

const NAV_BY_ROLE: Record<ConsoleRole, readonly NavItem[]> = {
  consumer: [
    { href: "/", label: "换电履约", hint: "站点 · 权益" },
    { href: "/credit", label: "信用", hint: "购 · 账 · 退" },
    { href: "/mall", label: "商城", hint: "券 · 下单" },
  ],
  shop: [
    { href: "/iot", label: "设备诊断", hint: "影子 · 工单" },
    { href: "/settlement", label: "分润结算", hint: "本店意向 · 批" },
  ],
  operator: [
    { href: "/operator", label: "运营配置", hint: "商家 · 套餐" },
    { href: "/settlement", label: "分润结算", hint: "全网意向 · 批" },
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
  shop: {
    brandSub: "店主端",
    home: "/iot",
    topbar: "店主端：设备诊断与本店分润结算。",
  },
  operator: {
    brandSub: "运营商端",
    home: "/operator",
    topbar: "运营商端：运营配置与分润结算。",
  },
};

function isValidRole(value: string | null): value is ConsoleRole {
  return value === "consumer" || value === "shop" || value === "operator";
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
