# 决策日志 — evolutionary

| 日期 | 决策 | 理由 | 状态 |
| :--- | :--- | :--- | :--- |
| 2026-09-19 | **事务边界放 interfaces，用例保持零框架**：`TransactionalPerformSwap` 包住 `PerformSwap`，Controller 注入包装类。跨两次写的操作（站库存 + 换电日志）由此原子化 | 三条：① `DomainFrameworkFreeTest` 拦 application 层的 `import org.springframework`，不为一个注解开口子；② **边界属驱动方不属用例** —— `@Transactional` 打在 Controller 方法上会让事务范围变成"一次 HTTP 请求"，换驱动方（CLI/消息）就静默失去原子性；③ 与 HANDOVER 既定决策一致 | 生效中（`26b4aba`） |
| 2026-09-17 | **前端目标架构（纠偏）**：App Router 默认 **RSC**；服务端读模型用 `fetch`+cache；客户端仅交互岛；请求层独立（弱网/错误/重试）；目录按 **BC 视图模型**（`domains/{swap,credit}/`）而非纯 page 堆叠 | L2 压测阶段用全 CSR 壳换速度，已偏移 | 待实施（`topic/fe-ddd-rsc`） |
| 2026-09-17 | **mall→commerce**：允许应用层依赖 commerce 的 **共享内核**（`Money`/`Account`/`Ledger*`）；禁止 mall 域依赖 commerce **换电聚合**（Order/Entitlement/Usage）——已由 `PurchaseMallOrder` 注释钉死 | 支付账本跨 BC；换电权益不进商城 | 生效中 |
| 2026-09-17 | **Monorepo 不拆仓**；全局工作区 = 本仓根。拆 FE/BE 仓的门槛：独立发布/权限/团队 | 当前一人+agent，拆仓增加契约同步成本 | 生效中 |
| 2026-09-17 | collaboration 账本分层：`evolution-log`/`interceptions` 主表只留跨项目摘要；项目证据留本仓 WM | 防项目日志淹没 KB | **已 W5**（`project-evidence-vs-kb-ledger`） |
| 2026-09-17 | 并行 agent 工作区 = **路径约定 + `working-memory/agents/{fe,be}/` + `wip/` 分支**；不为此拆 git 远程 | 集群已用、目录沙箱未建 → 复盘补齐 | **已 W5**（S36 + agents/） |
| 2026-09-17 | 分支命名 = **id + slug**：`version/v{MAJOR}`；`phase/p{N}-{slug}`（N 对齐规格 phase-N） | 可脚本对齐 + 人可读；见 `git-branching.md` | 生效中 |
| 2026-09-17 | **不默认第三层常驻分支**；切片用 commit/WM；并行用 `wip/pN-*` 与 `topic/*` | 两层够用；合入后删或 archive phase | 生效中 |
| 2026-09-17 | 用户偏好进 collaboration `profiles/heiniao.yaml`（S11） | 中文 commit/注释、H2 开头回复跨仓生效 | 生效中 |
| 2026-09-17 | **契约三层**：IDL(TS 设计期) / 后端 Java / 前端 TS；`contracts/*.ts` 非运行时全局接口 | 避免把 WM 里的 TS 当成后端接口源 | 生效中 |
| 2026-09-17 | 本地 git 提交说明、代码注释、WM → **中文** | 用户语言 | 生效中 |
| 2026-09-17 | 压测层级 L1/L2/L3 见 collaboration `patterns/pressure-routing` | 防工具链压测冒充 KB 演化 | 生效中 |
