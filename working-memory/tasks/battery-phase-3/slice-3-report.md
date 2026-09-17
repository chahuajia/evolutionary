# phase-3 切片3 报告

**日期**：2026-09-17 ｜ **压力层**：L2

## 交付

| 项 | 内容 |
| :--- | :--- |
| 包 | `com.evolutionary.operator.domain` + `application` |
| 聚合 | `PackageOverride`：draft→active→revoked；patches 白名单 |
| 视图 | `EffectiveProduct.resolve`：模板 base + active override（INV-13） |
| 用例 | `ActivatePackageOverride` · `RevokePackageOverride` · `ResolveEffectiveProduct` |
| 授权 | 仅模板 owner 的后代可覆盖（`OrgAuthorization.isDescendant`） |
| 测试 | `PackageOverrideEffectiveProductTest`：AC-26 / AC-27 / AC-31 |

## 测量

- collaboration：不变
- interceptions：0
- 叠在切片2 `df56e76` 之上
- `mvn test`：绿

## 备注

非法字段（如 `durationDays`）→ `FIELD_NOT_OVERRIDABLE`，不落库；撤销记 `OVERRIDE_REVOKE` 并回落原价。未覆盖 AC-28/29/30（非本切片范围）。
