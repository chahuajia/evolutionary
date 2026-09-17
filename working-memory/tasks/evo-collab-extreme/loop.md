# evo-collab-extreme（双轴极端）

**更新**：2026-09-17 20:50  
**主轴**：下 tick **单主轴**（先 L3 或先 L2，勿强绑）  
**期望 HEAD**：按主轴声明；采纳 [[extreme-unattended-cluster]] **v4**  
**idle**：0 / 3  
**分支**：`topic/fe-ddd-rsc`  
**HEAD 锚**：evo `3226afa` 切片5✅

## 句柄

`AGENT_LOOP_WAKE_evo-collab-extreme`

## v4 吞吐（本任务强制）

1. **单主轴优先**：每 tick 只交 L2 *或* L3  
2. **加厚切片**：FE∥BE 一次交付 代码+目标测+RUNBOOK 一行+status  
3. **≤60s 父接管**；禁止干等 wake  
4. **目标测**（禁无谓全量 mvn test）  
5. **WM 真源**仅本文件；collab-cli 指针收口再改  

## 切片

| 步 | 轴 | 范围 | 状态 |
| :-- | :--- | :--- | :--- |
| 0–5 | L2/L3 | extreme→权益换电→逾期409 | ✅ |
| 6 | L3 *或* L2 | harvest Credit 翻译器 **或** 还款解冻 HTTP（加厚 FE∥BE） | 🔄 下一 tick |

## 下一 tick

- 若 L3：仅当有新墙才 harvest（忌重复 S34 空转）  
- 若 L2：还款解冻 → 解冻 E-1 → entitled-swaps 200（加厚 brief）  
- 无增量 idle+1  

## 停止

双轴连续 3 tick 无增量 → 停
