# phase-3 切片1 报告

**日期**：2026-09-17 ｜ **压力层**：L2

## 交付

| 项 | 内容 |
| :--- | :--- |
| 包 | `com.evolutionary.operator.domain`（与 commerce 交易分离） |
| 类型 | Organization、RoleBinding、OrgAuthorization |
| 规则 | canManage = 自身或祖先 + regionScope；**不用 level** |

## 契约对照

IDL `contracts/phase-3.ts` 仅对照字段名；实现以 Java 为准（见 `contracts/README.md`）。
