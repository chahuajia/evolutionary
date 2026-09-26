import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.join(__dirname, "..", "src");

const NEST = {
  OnboardingApplication: "com.evolutionary.operator.domain.OnboardingApplication",
  ReferralBinding: "com.evolutionary.settlement.domain.ReferralBinding",
  SettlementBatch: "com.evolutionary.settlement.domain.SettlementBatch",
  ProfitShareAccrual: "com.evolutionary.settlement.domain.ProfitShareAccrual",
  UserCoupon: "com.evolutionary.mall.domain.UserCoupon",
  MerchantProfile: "com.evolutionary.mall.domain.MerchantProfile",
  MallSku: "com.evolutionary.mall.domain.MallSku",
  MallOrder: "com.evolutionary.mall.domain.MallOrder",
  Campaign: "com.evolutionary.mall.domain.Campaign",
  BillingStatement: "com.evolutionary.credit.domain.BillingStatement",
  CreditLedgerDebt: "com.evolutionary.credit.domain.CreditLedgerDebt",
  CreditProfile: "com.evolutionary.credit.domain.CreditProfile",
  PackageOverride: "com.evolutionary.operator.domain.PackageOverride",
  PackageTemplate: "com.evolutionary.operator.domain.PackageTemplate",
  Entitlement: "com.evolutionary.commerce.domain.Entitlement",
  UsageEvent: "com.evolutionary.commerce.domain.UsageEvent",
  BatteryAsset: "com.evolutionary.commerce.domain.BatteryAsset",
  Order: "com.evolutionary.commerce.domain.Order",
  Product: "com.evolutionary.commerce.domain.Product",
  Organization: "com.evolutionary.operator.domain.Organization",
};

function walk(dir, out = []) {
  if (!fs.existsSync(dir)) return out;
  for (const name of fs.readdirSync(dir)) {
    const p = path.join(dir, name);
    if (fs.statSync(p).isDirectory()) walk(p, out);
    else if (name.endsWith(".java")) out.push(p);
  }
  return out;
}

for (const file of [...walk(path.join(ROOT, "main")), ...walk(path.join(ROOT, "test"))]) {
  let text = fs.readFileSync(file, "utf8");
  let changed = false;
  for (const [agg, fqcn] of Object.entries(NEST)) {
    if (!text.includes(`${agg}.Status`)) continue;
    if (text.includes(`import ${fqcn};`)) continue;
    const pkgM = text.match(/^package ([\w.]+);/m);
    if (!pkgM) continue;
    const pkg = pkgM[1];
    if (pkg === fqcn.substring(0, fqcn.lastIndexOf("."))) continue; // same package
    text = text.replace(/^(package [\w.]+;\s*\n)/, `$1\nimport ${fqcn};\n`);
    changed = true;
  }
  if (changed) fs.writeFileSync(file, text);
}
console.log("imports fixed");
