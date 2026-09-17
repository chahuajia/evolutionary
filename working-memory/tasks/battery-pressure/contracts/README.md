# 契约（contracts）怎么放、用什么语言

> 决策：2026-09-17（回应「TS 契约是不是全局接口形状」）

## 结论（先看这里）

当前 `phase-*.ts` **不是**运行时全局接口，也 **不是** 前端源码。
它们是 A10 第 3 层的 **设计期 IDL**（用 TypeScript 语法写「形状」），供 review / 对照实现。

| 层 | 放哪 | 语言 | 是否进运行时 |
| :--- | :--- | :--- | :--- |
| **A. 跨端共享形状**（钱、状态枚举、API JSON） | `working-memory/.../contracts/idl/`（现文件暂留本目录） | **TypeScript 作 IDL**（或日后 OpenAPI） | 否（默认不打包） |
| **B. 仅后端**（仓储端口、领域行为） | `backend/src/...` 的 Java 类型/接口 | **Java** | 是 |
| **C. 仅前端**（页面 ViewModel、组件 props） | `frontend/` | **TypeScript** | 是 |

**不要**把 B/C 塞进本目录的 `.ts`；**也不要**让 Java 领域类去「实现」这些 `.ts` 文件。

## 为什么跨端 IDL 先用 TypeScript

1. 前后端边界上的交换物本质是 **JSON 形状**；TS 写联合类型/品牌类型比散文规格更可 review。
2. 本仓前端已是 TS；IDL 与 FE 同族，阅读成本低。
3. 后端是 Java —— 真源在 **Java 领域模型**；IDL 只约束「对外可观察的字段与枚举名」，不替代 Java。
4. 若日后多语言消费者变多，再抽 **OpenAPI / JSON Schema** 生成两端；现在抽生成器 = 过早。

## 反面（已踩过的坑）

- 把 `contracts/*.ts` 当成「全局唯一接口」→ 后端开始抄 TS、前端 import WM 路径 → 仓库纯度坏掉。
- 为每个 Product 规则在 Java 里再写一份「契约类」却从不跑 → 文档腐烂。
- 用 `org.level` 这种展示字段当授权依据 —— 那是规格问题，不是契约语言问题。

## 实现对照规则

| 改动 | 先改 | 再改 |
| :--- | :--- | :--- |
| 对外 JSON / 状态枚举名 | IDL（本目录）+ 规格 | Java DTO / FE 类型 |
| 领域行为 / 不变量守卫 | 规格 + 验收 | **只**改 Java domain/application |
| 纯 UI 状态 | — | **只**改 frontend |

## 迁移说明

- 现有 `phase-0.ts` … `phase-7.ts` **暂不搬迁**（避免空转 rename）。
- 新阶段（phase-3+ 实现）注释写明：`contracts/*.ts` = IDL；实现落点 = `backend/...`。
- 目录正式改名为 `idl/` 留待单独 tick（有增量再动）。
