package com.evolutionary.swap.interfaces;

import com.evolutionary.battery.domain.Battery;
import com.evolutionary.swap.application.PerformSwap;
import com.evolutionary.swap.domain.SwapSession;
import java.util.Objects;
import org.springframework.transaction.annotation.Transactional;

/**
 * 给 {@link PerformSwap} 包一层**事务边界**。
 *
 * <h2>为什么需要它</h2>
 *
 * {@code PerformSwap.execute} 要写两处：**站库存**（{@code stations.save}）与
 * **换电日志**（{@code swapLogs.append}）。它的 javadoc 一直写着"同事务双写"，
 * 但在这之前**全仓没有一处 {@code @Transactional}** —— 两次写各自跑在自己的事务里。
 *
 * <p>后果不是"性能差"，是**数据错**：第二次写失败时第一次**已经提交**，
 * 于是站里的电池已被取走、而换电日志没有记录。且它**静默** ——
 * 没有异常逃到用户面前，只有账对不上。
 * 见 {@code PerformSwapAtomicityTest}：日志追加失败后，站库存必须回滚。
 *
 * <h2>为什么放在 interfaces 层，而不是 PerformSwap 上</h2>
 *
 * 三条理由，按重要性排：
 *
 * <ol>
 *   <li><b>领域/应用层零框架</b> —— {@code DomainFrameworkFreeTest} 会拦下
 *       {@code import org.springframework}。这是结构性判据
 *       （[[patterns/domain-purity-is-structural]]），不为一个注解开口子。
 *   <li><b>边界属于"驱动方"，不属于用例</b> —— 事务范围是"一次换电操作"，
 *       而"一次操作"由调用方界定。放这里，任何驱动方（HTTP / CLI / 消息）
 *       经由本类都自动获得原子性；若把 {@code @Transactional} 打在 Controller
 *       的方法上，事务范围就变成了"一次 HTTP 请求" —— 换个驱动方就静默失去原子性。
 *   <li>与既定决策一致（`HANDOVER.md`：<i>事务宜放 interfaces/infrastructure</i>）。
 * </ol>
 *
 * <h2>它不是多余的一层</h2>
 *
 * 它就是那个"把跨两次写的操作变原子"的地方。删掉它，注释里的"同事务双写"
 * 又变回一句空话。
 */
public class TransactionalPerformSwap {

    private final PerformSwap delegate;

    public TransactionalPerformSwap(PerformSwap delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    /** 事务范围 = 一次换电：站库存与换电日志同生共死。 */
    @Transactional
    public SwapSession execute(String stationId, Battery incoming) {
        return delegate.execute(stationId, incoming);
    }
}
