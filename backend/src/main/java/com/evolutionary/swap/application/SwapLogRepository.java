package com.evolutionary.swap.application;

import com.evolutionary.swap.domain.SwapLog;
import java.util.List;

/**
 * 换电日志端口：仅应由 {@link PerformSwap} 在换电成功后 append。
 *
 * <p>与站库存的**同事务**保证不在用例层，而在
 * {@code interfaces/TransactionalPerformSwap} —— 用例保持零框架依赖
 * （{@code DomainFrameworkFreeTest} 会拦 {@code import org.springframework}），
 * 事务边界由驱动方界定。删掉那个包装类，"同事务"就只是一句注释。
 */
public interface SwapLogRepository {

    void append(SwapLog log);

    List<SwapLog> findByStationId(String stationId);
}
