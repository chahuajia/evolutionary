# Git 与分支协作（evolutionary）

**更新**：2026-09-17 ｜ **状态**：生效中

## 1. 要不要拆前后端成两个 Git 仓库？

**现阶段：不拆。继续 monorepo（一个仓库 = 全局工作区）。**

| 方案 | 何时用 |
| :--- | :--- |
| **Monorepo**（现状） | FE/BE 同版本演进、共享 WM/契约、一人+多 agent 协作 |
| **拆仓** | 发布节奏独立、权限/CI 独立、≥2 团队分别拥有 |

拆仓后「全局工作区」通常是 IDE multi-root 或聚合目录，**不必**再套一层 Git。

## 2. 命名：要 id，也要语义

分支名 = **稳定 id** + **可读 slug**。id 方便脚本/agent 对齐规格；slug 方便人扫一眼。

### 版本分支

```text
version/v{MAJOR}              # 例：version/v0、version/v1
version/v{MAJOR}.{MINOR}      # 例：version/v0.1（同大版本内里程碑，可选）
```

| 部分 | 含义 |
| :--- | :--- |
| `v{MAJOR}` | **版本 id**（语义化主号；破坏性产品线升级才 +1） |
| `.{MINOR}` | 可选；同主线下的里程碑，不是每次 phase 都涨 |

合入后可打 tag：`v0.1.0`（完整 semver 留给「可发布快照」；日常分支不必三段）。

### 阶段分支

```text
phase/p{N}-{slug}             # 例：phase/p4-profit-sharing
phase/p{N}-fix-{slug}        # 例：phase/p4-fix-batch-reversal
```

| 部分 | 含义 |
| :--- | :--- |
| `p{N}` | **阶段 id**，与 `battery-pressure/phase-N-*` **同一数字**，永不复用错位 |
| `{slug}` | **语义化**英文短横线（领域词，非日期/人名） |
| `fix` | 从已合入 version 再叉出修该阶段 |

**旧名兼容**：现有 `phase/4-profit-sharing`、`version/v0` 视为合法；**新开分支**起用 `phase/p{N}-*` 前缀（显式 `p`，避免与纯数字路径混淆）。

### 反面

- 不要用 `phase/张三-临时`、`phase/2026-09-17`
- 不要阶段号与规格 phase-N 错位（规格是 4，分支却叫 p5）
- 不要在 version 分支上直接堆未验收大改

## 3. 粒度：只要 version × phase 够不够？

**结论：日常两层够用；不默认增加第三层常驻分支（如 `feat/` /「注入」线）。**

| 层 | 职责 | 寿命 |
| :--- | :--- | :--- |
| `version/v*` | 已验收产品线 | 长 |
| `phase/p{N}-*` | 对齐规格 phase-N 的一整段交付 | 短（合入即删） |
| **切片** | 阶段内步进（AC 批） | **commit / WM 表**，不是分支 |

更细粒度优先落在：

1. **phase 内顺序 commit**（切片1→2→3）+ `loop.md` 状态表  
2. **同 phase 前后端/多 agent 并行**：`wip/p{N}-be-{slug}` · `wip/p{N}-fe-{slug}` → 合回 **phase** → 再合 version  
3. **已合入模块上并行增量**：`topic/{module}-{slug}` 从 `version/v*` 开（不新占 phase 号）  
4. 已合入后的修补：`phase/p{N}-fix-{slug}`

合入 version 后 phase 指针：**删除**（默认）或改名 `archive/p{N}-{slug}`（若需本地扫档）；历史以 merge commit 为准。

**不要**为「功能注入」单独开常驻分支族——那是领域/包边界。

反面：三层常驻长期滞留 → agent 对齐成本上升。

## 4. 版本 × 阶段流程

```text
main（可选镜像）
  └── version/v0
        ├── phase/p6-credit
        │     ├── wip/p6-be-overdue
        │     └── wip/p6-fe-credit-ui
        └── topic/mall-coupon-ui
```

1. 从 `version/v*` 检出 `phase/p{N}-{slug}`
2. 阶段验收绿 + W4 → **merge 回 version** → **删除**（或 archive）phase 分支
3. 事后修：`phase/p{N}-fix-{slug}` → 再合回 version
4. Agent 只在当前 phase/wip/topic 分支 commit；不 push 除非人要求
5. **前后端默认可并行**：同 phase 上 BE∥FE 双集群，契约冲突面用 wip 隔离

### 当前线

| 分支 | 状态 |
| :--- | :--- |
| `version/v0` | 稳定线（含已合入阶段） |
| `phase/p6-credit` | 合入后删除 |

## 5. 与 User Profile

协作偏好见 collaboration `profiles/heiniao.yaml`。
