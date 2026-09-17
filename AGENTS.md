# AGENTS.md

> 本文件是 **AI 进入本项目的入口**（约定见 `A13-AI-入口文件规范`）。
> 原则：**入口指向，不复制内容**。
>
> ⚠️ **状态：刚建立，待补全。** 标 `TODO` 的地方只有你能填。

## 项目是什么

一个**换电站（battery swapping）**的 DDD 演练项目：前端 Next.js、后端 Spring Boot。

**它不是产品。** 它的产物是「**暴露报告**」—— 用来验证 `COLLABORATION` 的规范
能不能指导一个真实项目的真实决策。**代码是手段，不是目的。**

### 第一原则

**每一步都要能回答："这一轮暴露了什么？"**

跑一遍看看 = 无效。每一轮**先声明暴露点**，跑完**按暴露点验收**。

## 协作偏好（读 Profile）

进入本仓后读 knowledge 库 `profiles/heiniao.yaml`：
- 本地 commit / 注释 / WM → **中文**
- 回答从 **H2** 开始（S1）
- 长任务：无人值守 + 集群

分支策略见 `working-memory/git-branching.md`。

## 依赖的外部知识库

本项目的协作规范**不在本仓库内** —— 它在独立的 COLLABORATION 知识库：

```
D:\actto\front\project\collaboration_aggregate\collaboration
```

（相对本仓库：`..\..\collaboration_aggregate\collaboration`）

## 阅读顺序（不要全量读）

1. **知识库的 `AGENTS.md`（`<知识库>/AGENTS.md`）里的「症状 → 条目」表** ——
   **从这里开始**。它按"你要干什么"组织（如"AI 写了大量代码等我确认 → 读 A10"）。
   > 实测：这张手写表的命中率高于 `catalog.json`（2/4 vs 0/4）。
2. `catalog.json` —— 生成物，当**目录 + 关键词检索**用。
   ⚠️ **别指望它的 `trigger`**：只覆盖 11/108，而且恰好是"本来就会读的"那 11 条。
3. `ROOT.md` —— 结构、权限、演化机制
4. `meta/base-contract.md` —— **冻结的基座**（目录 / kind / id / frontmatter / 链接 / 生成物）
5. 按需读条目：
   - 边界、权限、不可谈判的规则 → `agreements/`（只有 8 条）
   - 执行剧本 → `workflows/`
   - 具体做法 → `skills/`
   - 概念与判据 → `patterns/`
   - 特定环境的操作手册 → `integrations/`

> **如果上面都找不到你要的** —— **明确报告"找不到"**，比猜一个规范出来有价值得多。

## 协作规则（摘要，只列标题）

- **先给规格再写实现**；review 对象优先级：规格 > 测试 > 类型 > 实现（`A10`）
- **AI 不 commit、不 push**；改动只留在工作区等人 review（`cli-agent-boundaries`）
- **命名先查 `meta/naming-conventions.md`**；新条目必须能回答"不看它，一个称职的通用模型会做错吗"
- **找不到就说找不到** —— 比猜一个规范出来更有价值

## 工具

```bash
# 校验知识库（工作区位置用 --dir 指定）
node <collab-cli>/dist/cli/index.js --dir <知识库> validate
```

（`collab` 也已作为 MCP server 注册，工具名 `collab_*`。）

## 工作记忆

跨对话进度在 **`working-memory/`**（见 `W10`）。业务压测规格：

`working-memory/tasks/battery-pressure/`（阶段 0–7 已收口，待 backend 实现）

## 本文件待补

- [x] `项目是什么`
- [x] 技术栈与运行方式（见下）
- [x] 本项目的**局部约定**（见下）
- [x] 工作记忆路径：`working-memory/README.md`

## 技术栈（用户已定）

| 层 | 选型 |
| :--- | :--- |
| 前端 | **Next.js（App Router）+ TypeScript** |
| 后端 | **Spring Boot + Java** |
| 契约 | 第一轮用 **REST**；GraphQL 待定 |

## 本项目的局部约定（只写"局部差异"，不重述全局）

1. **后端按业务模块组织包**，不按技术层：
   `station/` · `battery/` · `swap/`，每个模块内 `domain/ application/ infrastructure/ interfaces/`。
2. **`domain/` 零框架依赖** —— 不 import Spring，不 import JPA 注解。
3. **前端的状态与后端的领域不是一回事** —— 前端管"界面状态一致性"，
   不需要聚合根/仓储。（这条本身是**待验证**的判断，见 `specs/round-1.md` 暴露点）
4. **不引入依赖前先跑 `patterns/dependency-decision` 三问**。
