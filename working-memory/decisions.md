# 决策日志 — evolutionary

| 日期 | 决策 | 理由 | 状态 |
| :--- | :--- | :--- | :--- |
| 2026-09-17 | **契约三层**：IDL(TS 设计期) / 后端 Java / 前端 TS；`contracts/*.ts` 非运行时全局接口 | 避免把 WM 里的 TS 当成后端接口源；跨端 JSON 形状用 TS 作 IDL 最省；领域行为只在 Java | 生效中 |
| 2026-09-17 | 本地 git 提交说明、代码注释、WM → **中文** | 用户语言 | 生效中 |
| 2026-09-17 | 压测层级 L1/L2/L3 见 collaboration `patterns/pressure-routing` | 防工具链压测冒充 KB 演化 | 生效中 |
