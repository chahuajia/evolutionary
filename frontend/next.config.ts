import type { NextConfig } from "next";

/**
 * 同域代理：浏览器 → Next `/api/*` → Spring `http://localhost:8080/*`
 * 见 specs/round-9.md（BFF rewrite vs CORS）。
 */
const nextConfig: NextConfig = {
  async rewrites() {
    const backend = process.env.BACKEND_ORIGIN ?? "http://localhost:8080";
    return [
      {
        source: "/api/:path*",
        destination: `${backend}/:path*`,
      },
    ];
  },
};

export default nextConfig;
