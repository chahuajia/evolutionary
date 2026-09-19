# 拦截候选（业务仓）

> L2 撞墙先记这里；W4 通过后 harvest **一行**进 collaboration `meta/interceptions.md`。  
> 见 [[patterns/project-evidence-vs-kb-ledger]]。

| 日期 | 条目（候选） | 拦住了什么 | 证据路径 | 状态 |
| :--- | :--- | :--- | :--- | :--- |
| 2026-09-17 | [[patterns/frontend-ddd-rsc]] | 差点继续首页全 use client+useEffect 拉站列表 | topic/fe-ddd-rsc b20f899 | harvested |
| 2026-09-19 | [[patterns/tests-encode-assumptions]] | 差点把「同事务双写」当成已实现——注释这么写，全仓却无一处 `@Transactional`。先写红测试证明（日志失败后站库存**未**回滚），再修 | swap `26b4aba` · `PerformSwapAtomicityTest` | harvested 09-19 |
| 2026-09-19 | [[patterns/domain-purity-is-structural]] | 差点把 `@Transactional` 直接打到 `PerformSwap` 上——`DomainFrameworkFreeTest` 会拦，且会把事务范围错划成"一次 HTTP 请求" | swap `26b4aba` · `TransactionalPerformSwap` | harvested 09-19 |
| 2026-09-19 | [[patterns/derivation-over-copy]] | 差点留着**两份** `AccrualView`（网关一份、领域一份）——那正是契约能漂移的原因 | settlement `e98dc3b` | harvested 09-19 |
| 2026-09-19 | [[S33-测试数据的契约一致性]] | 前端测试里的 `status: "ACCRUED"` **后端从来不存在**，因为 `status` 是裸 `string` —— 契约从类型上丢了 | settlement `e98dc3b`（类型收紧后编译器当场抓出） | harvested 09-19 |
| 2026-09-19 | [[patterns/parse-dont-validate]] | 网关写 `String(r.status ?? "")`：未知状态**静默变空串**往下传，不炸 | settlement `e98dc3b` · `parseAccrualStatus` | harvested 09-19 |
