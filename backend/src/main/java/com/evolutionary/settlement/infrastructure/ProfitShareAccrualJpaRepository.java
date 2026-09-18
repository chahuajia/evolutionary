package com.evolutionary.settlement.infrastructure;


import com.evolutionary.settlement.domain.ProfitShareAccrual;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfitShareAccrualJpaRepository
        extends JpaRepository<ProfitShareAccrualJpaEntity, String> {

    List<ProfitShareAccrualJpaEntity> findByOrderId(String orderId);

    List<ProfitShareAccrualJpaEntity>
            findByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                    ProfitShareAccrual.Status status, Instant periodStart, Instant periodEnd);
}
