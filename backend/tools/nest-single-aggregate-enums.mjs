import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.join(__dirname, "..", "src");

const NEST = {
  OnboardingStatus: ["OnboardingApplication", "operator/domain/OnboardingApplication.java"],
  ReferralStatus: ["ReferralBinding", "settlement/domain/ReferralBinding.java"],
  BatchStatus: ["SettlementBatch", "settlement/domain/SettlementBatch.java"],
  AccrualStatus: ["ProfitShareAccrual", "settlement/domain/ProfitShareAccrual.java"],
  UserCouponStatus: ["UserCoupon", "mall/domain/UserCoupon.java"],
  MerchantStatus: ["MerchantProfile", "mall/domain/MerchantProfile.java"],
  MallSkuStatus: ["MallSku", "mall/domain/MallSku.java"],
  MallOrderStatus: ["MallOrder", "mall/domain/MallOrder.java"],
  CampaignStatus: ["Campaign", "mall/domain/Campaign.java"],
  StatementStatus: ["BillingStatement", "credit/domain/BillingStatement.java"],
  DebtStatus: ["CreditLedgerDebt", "credit/domain/CreditLedgerDebt.java"],
  CreditStatus: ["CreditProfile", "credit/domain/CreditProfile.java"],
  OverrideStatus: ["PackageOverride", "operator/domain/PackageOverride.java"],
  TemplateStatus: ["PackageTemplate", "operator/domain/PackageTemplate.java"],
  EntitlementStatus: ["Entitlement", "commerce/domain/Entitlement.java"],
  UsageEventStatus: ["UsageEvent", "commerce/domain/UsageEvent.java"],
  BatteryAssetStatus: ["BatteryAsset", "commerce/domain/BatteryAsset.java"],
  OrderStatus: ["Order", "commerce/domain/Order.java"],
  ProductStatus: ["Product", "commerce/domain/Product.java"],
  OrgStatus: ["Organization", "operator/domain/Organization.java"],
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

function javaFiles() {
  return [...walk(path.join(ROOT, "main")), ...walk(path.join(ROOT, "test"))];
}

function parseEnum(file, enumName) {
  const text = fs.readFileSync(file, "utf8");
  const docM = text.match(/\/\*\*([\s\S]*?)\*\/\s*public enum/);
  let doc = "";
  if (docM) {
    const lines = docM[1].trim().split("\n").map((l) => l.trim());
    doc = "    /**\n" + lines.map((l) => "     * " + l).join("\n") + "\n     */\n";
  }
  const bodyM = text.match(new RegExp(`public enum ${enumName}\\s*\\{([^}]*)\\}`, "s"));
  if (!bodyM) throw new Error(`parse fail ${file}`);
  return { doc, body: bodyM[1].trim() };
}

function nest(aggRel, doc, body) {
  const file = path.join(ROOT, "main", "java", "com", "evolutionary", ...aggRel.split("/"));
  let text = fs.readFileSync(file, "utf8");
  if (/public enum Status\s*\{/.test(text)) return;
  const simple = path.basename(file, ".java");
  const marker = `public final class ${simple} {`;
  if (!text.includes(marker)) throw new Error(`no marker ${file}`);
  const innerBody = body
    .split("\n")
    .filter((l) => l.trim())
    .map((l) => "        " + l.trim())
    .join("\n");
  const inner = `${marker}\n\n${doc}    public enum Status {\n${innerBody}\n    }\n`;
  fs.writeFileSync(file, text.replace(marker, inner));
}

function replaceRefs(old, aggregate) {
  const neu = `${aggregate}.Status`;
  const importRe = new RegExp(`^import com\\.evolutionary\\.[\\w.]+\\.${old};\\s*\\n`, "gm");
  for (const file of javaFiles()) {
    let text = fs.readFileSync(file, "utf8");
    const updated = text.replace(importRe, "").replaceAll(old, neu);
    if (updated !== text) fs.writeFileSync(file, updated);
  }
}

function findEnum(enumName) {
  return javaFiles().find(
    (f) => path.basename(f) === `${enumName}.java` && f.includes(`${path.sep}domain${path.sep}`)
  );
}

for (const [old, [aggregate, aggRel]] of Object.entries(NEST)) {
  const enumFile = findEnum(old);
  if (!enumFile) throw new Error(`missing ${old}`);
  const { doc, body } = parseEnum(enumFile, old);
  nest(aggRel, doc, body);
  replaceRefs(old, aggregate);
  fs.unlinkSync(enumFile);
  console.log(`nested ${old} -> ${aggregate}.Status`);
}
