import { dirname } from "path";
import { fileURLToPath } from "url";
import { FlatCompat } from "@eslint/eslintrc";

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const compat = new FlatCompat({
  baseDirectory: __dirname,
});

const noAppInfrastructure = {
  files: ["src/app/**/*.{ts,tsx}", "src/components/**/*.{ts,tsx}"],
  rules: {
    "no-restricted-imports": [
      "error",
      {
        patterns: [
          {
            group: ["**/domains/*/infrastructure", "@/domains/*/infrastructure"],
            message:
              "app/components 禁止直连 gateway；经 domains/*/application 用例编排。",
          },
          {
            group: ["@/shared/http/fetch-json", "**/shared/http/fetch-json"],
            message:
              "app/components 禁止直连 fetchJson；经 application → gateway。",
          },
        ],
      },
    ],
  },
};

const eslintConfig = [
  ...compat.extends("next/core-web-vitals", "next/typescript"),
  noAppInfrastructure,
];

export default eslintConfig;
