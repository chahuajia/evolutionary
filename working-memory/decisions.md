# 决策日志 — evolutionary

| 日期 | 决策 | 理由 | 状态 |
| :--- | :--- | :--- | :--- |
| 2026-09-17 | **Monorepo 不拆仓**；全局工作区 = 本仓根。拆 FE/BE 仓的门槛：独立发布/权限/团队 | 当前一人+agent，拆仓增加契约同步成本 | 生效中 |
| 2026-09-17 | **版本分支 × 阶段分支**：`version/*` 稳线，`phase/*` 短命；合回后删 phase；可再叉出修 | 支持大胆重构/回滚且不污染版本线；见 `git-branching.md` | 生效中 |
| 2026-09-17 | 用户偏好进 collaboration `profiles/heiniao.yaml`（S11） | 中文 commit/注释、H2 开头回复跨仓生效 | 生效中 |
| 2026-09-17 | **契约三层**：IDL(TS 设计期) / 后端 Java / 前端 TS；`contracts/*.ts` 非运行时全局接口 | 避免把 WM 里的 TS 当成后端接口源 | 生效中 |
| 2026-09-17 | 本地 git 提交说明、代码注释、WM → **中文** | 用户语言 | 生效中 |
| 2026-09-17 | 压测层级 L1/L2/L3 见 collaboration `patterns/pressure-routing` | 防工具链压测冒充 KB 演化 | 生效中 |
