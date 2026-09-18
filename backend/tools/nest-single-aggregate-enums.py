#!/usr/bin/env python3
"""Nest single-aggregate *Status enums into their owning aggregate as inner enum Status."""

from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "src"

NEST = {
    "OnboardingStatus": ("OnboardingApplication", "operator/domain/OnboardingApplication.java"),
    "ReferralStatus": ("ReferralBinding", "settlement/domain/ReferralBinding.java"),
    "BatchStatus": ("SettlementBatch", "settlement/domain/SettlementBatch.java"),
    "AccrualStatus": ("ProfitShareAccrual", "settlement/domain/ProfitShareAccrual.java"),
    "UserCouponStatus": ("UserCoupon", "mall/domain/UserCoupon.java"),
    "MerchantStatus": ("MerchantProfile", "mall/domain/MerchantProfile.java"),
    "MallSkuStatus": ("MallSku", "mall/domain/MallSku.java"),
    "MallOrderStatus": ("MallOrder", "mall/domain/MallOrder.java"),
    "CampaignStatus": ("Campaign", "mall/domain/Campaign.java"),
    "StatementStatus": ("BillingStatement", "credit/domain/BillingStatement.java"),
    "DebtStatus": ("CreditLedgerDebt", "credit/domain/CreditLedgerDebt.java"),
    "CreditStatus": ("CreditProfile", "credit/domain/CreditProfile.java"),
    "OverrideStatus": ("PackageOverride", "operator/domain/PackageOverride.java"),
    "TemplateStatus": ("PackageTemplate", "operator/domain/PackageTemplate.java"),
    "EntitlementStatus": ("Entitlement", "commerce/domain/Entitlement.java"),
    "UsageEventStatus": ("UsageEvent", "commerce/domain/UsageEvent.java"),
    "BatteryAssetStatus": ("BatteryAsset", "commerce/domain/BatteryAsset.java"),
    "OrderStatus": ("Order", "commerce/domain/Order.java"),
    "ProductStatus": ("Product", "commerce/domain/Product.java"),
    "OrgStatus": ("Organization", "operator/domain/Organization.java"),
}


def java_files() -> list[Path]:
    files: list[Path] = []
    for base in (ROOT / "main", ROOT / "test"):
        if base.exists():
            files.extend(base.rglob("*.java"))
    return files


def parse_enum(path: Path, enum_name: str) -> tuple[str, str]:
    text = path.read_text(encoding="utf-8")
    doc = ""
    doc_m = re.search(r"/\*\*(.*?)\*/\s*public enum", text, re.DOTALL)
    if doc_m:
        inner = doc_m.group(1).strip()
        lines = [(" " + ln).rstrip() for ln in inner.splitlines()]
        doc = "    /**\n" + "\n".join(f"     *{ln}" for ln in lines) + "\n     */\n"
    body_m = re.search(r"public enum " + re.escape(enum_name) + r"\s*\{([^}]*)\}", text, re.DOTALL)
    if not body_m:
        raise RuntimeError(f"Cannot parse enum body: {path}")
    return doc, body_m.group(1).strip()


def nest_into_aggregate(aggregate_rel: str, doc: str, body: str) -> None:
    path = Path(str(ROOT / "main" / "java" / "com" / "evolutionary") / Path(aggregate_rel))
    text = path.read_text(encoding="utf-8")
    if re.search(r"public enum Status\s*\{", text):
        return
    simple = path.stem
    marker = f"public final class {simple} {{"
    if marker not in text:
        raise RuntimeError(f"Marker not found in {path}")
    inner_body = "\n".join("        " + ln.rstrip() for ln in body.splitlines() if ln.strip())
    inner = f"{marker}\n\n{doc}    public enum Status {{\n{inner_body}\n    }}\n"
    path.write_text(text.replace(marker, inner, 1), encoding="utf-8")


def replace_refs(old: str, aggregate: str) -> None:
    new = f"{aggregate}.Status"
    import_re = re.compile(
        rf"^import com\.evolutionary\.[\w.]+\.{re.escape(old)};\s*\n", re.MULTILINE
    )
    for path in java_files():
        text = path.read_text(encoding="utf-8")
        updated = import_re.sub("", text)
        updated = updated.replace(old, new)
        if updated != text:
            path.write_text(updated, encoding="utf-8")


def find_enum_file(enum_name: str) -> Path:
    for path in java_files():
        if path.name == f"{enum_name}.java" and "domain" in path.as_posix():
            return path
    raise FileNotFoundError(enum_name)


def main() -> None:
    for old, (aggregate, agg_rel) in NEST.items():
        enum_path = find_enum_file(old)
        doc, body = parse_enum(enum_path, old)
        nest_into_aggregate(agg_rel, doc, body)
        replace_refs(old, aggregate)
        enum_path.unlink()
        print(f"nested {old} -> {aggregate}.Status")


if __name__ == "__main__":
    main()
