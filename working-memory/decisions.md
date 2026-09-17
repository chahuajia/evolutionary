# 决策日志 — evolutionary

| 日期 | 决策 | 理由 | 状态 |
| :--- | :--- | :--- | :--- |
| 2026-09-17 | **Monorepo 不拆仓**；全局工作区 = 本仓根。拆 FE/BE 仓的门槛：独立发布/权限/团队 | 当前一人+agent，拆仓增加契约同步成本 | 生效中 |
| 2026-09-17 | 分支命名 = **id + slug**：`version/v{MAJOR}`；`phase/p{N}-{slug}`（N 对齐规格 phase-N） | 可脚本对齐 + 人可读；见 `git-branching.md` | 生效中 |
| 2026-09-17 | **不默认第三层常驻分支**；切片用 commit/WM；并行冲突才短命 `phase/p{N}-wip-*` | 两层够用；「注入」是领域边界非分支族 | 生效中 |
| 2026-09-17 | 用户偏好进 collaboration `profiles/heiniao.yaml`（S11） | 中文 commit/注释、H2 开头回复跨仓生效 | 生效中 |
| 2026-09-17 | **契约三层**：IDL(TS 设计期) / 后端 Java / 前端 TS；`contracts/*.ts` 非运行时全局接口 | 避免把 WM 里的 TS 当成后端接口源 | 生效中 |
| 2026-09-17 | 本地 git 提交说明、代码注释、WM → **中文** | 用户语言 | 生效中 |
| 2026-09-17 | 压测层级 L1/L2/L3 见 collaboration `patterns/pressure-routing` | 防工具链压测冒充 KB 演化 | 生效中 |
