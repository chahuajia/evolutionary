# Git 与分支协作（evolutionary）

**更新**：2026-09-17 ｜ **状态**：生效中

## 1. 要不要拆前后端成两个 Git 仓库？

**现阶段：不拆。继续 monorepo（一个仓库 = 全局工作区）。**

| 方案 | 何时用 |
| :--- | :--- |
| **Monorepo**（现状） | FE/BE 同版本演进、共享 WM/契约、一人+多 agent 协作 |
| **拆仓** | 发布节奏独立、权限/CI 独立、≥2 团队分别拥有 |

拆仓后「全局工作区」通常是：
- **聚合根目录**（本身可无业务代码，或用 git submodule / 多 root workspace），或
- Cursor / IDE **multi-root workspace** 同时打开两个仓 —— **不必**再套一层 Git，除非要统一版本标签。

跨仓共享协议仍遵守：≥2 消费者才上提（见 collab-cli `decisions.md` 仓库纯度）。

## 2. 版本分支 × 阶段分支（推荐，已采纳）

```text
main（或 release 线）
  └── version/v0          ← 产品版本线（可长期活）
        ├── phase/4-profit-sharing   ← 阶段任务（短命）
        └── phase/5-mall             ← 另一阶段
```

### 规则

1. **版本分支** `version/<名>`：该产品大版本的稳定线；阶段确认后才合并进来。
2. **阶段分支** `phase/<N>-<slug>`：从对应 version 检出；只做本阶段增量。
3. 阶段 **验收绿 + W4** → merge 回 version → **删除** phase 分支。
4. 事后修 bug：从 version（或旧 phase tag）再 `phase/<N>-fix-<简述>`，修完合回 version。
5. Agent：**只在当前 phase 分支上 commit**；不 push 除非人要求。
6. 允许大胆重构/回滚 —— 烂在 phase 分支里，不污染 version。

### 当前线

| 分支 | 用途 |
| :--- | :--- |
| `version/v0` | phase-0～3 已收口基线 |
| `phase/4-profit-sharing` | phase-4 分润结算（进行中） |

## 3. 与 User Profile

协作偏好见 collaboration `profiles/heiniao.yaml`（中文 commit、H2 开头等）。
