package com.evolutionary.swap.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SwapLogJpaRepository extends JpaRepository<SwapLogJpaEntity, String> {

    List<SwapLogJpaEntity> findByStationIdOrderByOccurredAtAsc(String stationId);
}
