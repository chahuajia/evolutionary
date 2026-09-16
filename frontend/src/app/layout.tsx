import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "换电压测 UI",
  description: "evolutionary round-8 — Next.js → Spring REST",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="zh-CN">
      <body>{children}</body>
    </html>
  );
}
