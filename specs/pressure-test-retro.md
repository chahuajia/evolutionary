# Evolutionary 压测阶段复盘（第 4–14 轮）

**日期**：2026-09-17 ｜ **工作流**：W4 → W5（evolution-log v4.7.14）  
**范围**：D 实验停止后的 **11 轮**真实压测（非 A/B）

## 一、轮次一览

| 轮 | 主题 | 主要条目 | 拦截 |
| :-- | :--- | :--- | :-- |
| 4 | Station/Swap 领域 | S13 · domain-purity | **+1** S13 |
| 5 | PerformSwap | dependency-decision · design-decision | **+1** dep |
| 6 | Spring REST | domain-purity · dependency-decision | **+1** purity |
| 7 | JPA/H2 | domain-purity · dependency-decision | **+1** purity |
| 8 | Next.js UI | dependency-decision | 0 |
| 9 | rewrite `/api` | dependency-decision | 0 |
| 10 | GraphQL 否决 | dependency-decision | 0 |
| 11 | 多站 list | design-decision · W1 | 0 · **gap +1** |
| 12 | boundary parse | parse-dont-validate | 0 |
| 13 | 读用例对称 | layer-vs-context · design-decision | 0 |
| 14 | API 错误翻译 | S34 | **+1** S34 |

**测试**：11 → **41**（后端）· 前端 build 绿  
**账本**：interceptions **5** · known-gaps **3**（含 REST N+1 路由缺口）

---

## 二、W4 复盘三问

### 1. 哪个条目/模式最有效？

**症状表手写路由 + 工程三件套**：

| 条目 | 命中次数 | 典型作用 |
| :--- | :--- | :--- |
| **dependency-decision** | 每轮引/拒依赖 | 第 5 拒 Spring · 第 6–7 引 · 第 10 拒 GraphQL |
| **domain-purity-is-structural** | 6–7 + 架构测试 | 两次不同形态拦截（Spring import · `@Entity`） |
| **S13 / parse-dont-validate** | 4 · 12 | 不变量层 · 边界 Parse |
| **design-decision** | 5 · 11 · 13 | 读用例、batch vs N+1 |
| **S34** | 14（按需读，无 symptom 行） | 去掉 startsWith 判 HTTP |

**结论**：**dependency-decision + domain-purity** 承重最高；**症状表工程行**在修入口后确实被独立命中（第 4 轮验证）。

### 2. 工作流哪一步卡住了？

| 卡点 | 现象 | 处理 |
| :--- | :--- | :--- |
| **Playwright E2E** | 下载/双进程过重，用户取消 | 改 GraphQL 三问裁决（第 10 轮） |
| **第 11 轮读捷径** | Controller 直连 Repository | 第 13 轮回填对称 |
| **S34 无 symptom 行** | 只能 catalog/关联链找到 | 第 14 轮仍有效，但入口弱 |
| **REST N+1 无专条** | design-decision 自裁决 | known-gaps 记账，未发明条目 |

**未卡住**：A10 规格先行、interceptions 记账、evolution-log 逐轮同步。

### 3. 约定是否需要补充？

**不需要新 agreement**（配额满；且本轮是**模式/技能**层收益，不是协作规则缺口）。

**需要入口层动作**（见行动项）：known-gaps 已有 2 条仍开（trigger · REST N+1）。

---

## 三、行动项（W4 必填）

| # | 行动 | 负责人 | 关闭条件 |
| :-- | :--- | :--- | :--- |
| **A1** | 症状表增工程行：「多资源 REST 读法 / 客户端 N+1」→ design-decision 或专 pattern | 人 + RFC | known-gaps 第 4 行关闭 |
| **A2** | 症状表或 integrations 增指针：「HTTP/CLI 错误翻译」→ S34 | 人 | 第 14 轮式「按需读」不再依赖关联链碰运气 |
| **A3** | **不**批量填 catalog `trigger` | 已裁决 | 维持 AGENTS 声明 |
| **A4** | evolutionary 压测 **阶段暂停**；下一压力源：CLI 或 collab validate 真 CI | 人拍板 | working-memory 切任务 |
| **A5** | interceptions ≥5：domain-purity 可考虑写硬/写靠前（pruning-policy 提升阈值） | 人 | 非本轮 AI 擅自改 |

---

## 四、对 collaboration 的测量结论（可证伪）

1. **规范有收益**：5 条拦截均有代码证据，非自述。  
2. **入口 > 条目数**：修症状表后命中率明显高于 catalog trigger。  
3. **缺口侧同样重要**：REST N+1 证明「找不到要有去处」— known-gaps 有效。  
4. **否决也值钱**：dependency-decision 记录「不引」与「引」同样多（第 5 · 10）。  
5. **证伪条件**：若再开 5 轮 **零拦截且零 gap**，则怀疑「对已有栈重复劳动」→ 应换项目或换症状。

---

## 五、关联

`specs/round-4-report.md` … `round-14-report.md` · `collaboration/meta/interceptions.md` · `known-gaps.md` · evolution-log v4.7.3–v4.7.13
