package com.evolutionary.settlement.infrastructure;

import com.evolutionary.settlement.domain.AccrualStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfitShareAccrualJpaRepository
        extends JpaRepository<ProfitShareAccrualJpaEntity, String> {

    List<ProfitShareAccrualJpaEntity> findByOrderId(String orderId);

    List<ProfitShareAccrualJpaEntity>
            findByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                    AccrualStatus status, Instant periodStart, Instant periodEnd);
}
