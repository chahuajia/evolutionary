# 前端目标架构（纠偏草案）

**日期**：2026-09-17｜**状态**：待 `topic/fe-ddd-rsc` 实施

## 现状（偏移）

| 项 | 现状 | 问题 |
| :--- | :--- | :--- |
| 渲染 | `/credit` 全 `"use client"` + `useEffect` fetch | 未用 RSC；首屏无服务端数据 |
| ISR/SSG/SSR | 未声明 | 信用/站列表本可 SSR 或 ISR |
| 状态 | 组件 local `useState` | 无服务端缓存语义；与服务真相两套 |
| 请求 | 裸 `fetch` | 无弱网/重试/去重/失效策略 |
| DDD | `app/` + `lib/credit` 薄壳 | 无 BC 边界、无应用用例层、无防腐 |

## 目标分层（前端 DDD）

```text
app/                          # 路由 + RSC 组合根（无业务规则）
domains/
  swap/                       # 换电 BC 视图
    application/              # 用例：listStations、performSwap（调 gateway）
    domain/                   # 前端只读模型 / 展示不变量（非后端拷贝）
    infrastructure/           # HTTP gateway、DTO→模型
  credit/
    ...
shared/
  http/                       # 统一 fetch：timeout、重试、错误码翻译
  ui/                         # 哑组件
```

## Next 渲染约定

| 数据类型 | 默认策略 |
| :--- | :--- |
| 站列表、信用档案（读多） | **RSC SSR**（`fetch` + `cache: 'no-store'` 或 `revalidate`） |
| 静态营销壳 | SSG |
| 换电按钮、表单 | **客户端岛**（`"use client"` 仅此） |
| 账单列表可短缓存 | ISR `revalidate: 30`（有后端时） |

## 状态分工

| 层 | 管什么 |
| :--- | :--- |
| 服务端（RSC + Next fetch cache） | 服务真相的读模型 |
| 客户端 | 仅 UI 瞬态（展开、输入中、乐观 UI） |
| 禁止 | 用客户端 store 再镜像一整份服务端列表当「源」 |

## 非目标（本轮不做）

- 上 Redux/Zustand 全局店（无足够复杂度前）
- 复制后端聚合进前端 domain
