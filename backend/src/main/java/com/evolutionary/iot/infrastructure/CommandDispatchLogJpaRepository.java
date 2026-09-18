package com.evolutionary.iot.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandDispatchLogJpaRepository
        extends JpaRepository<CommandDispatchLogJpaEntity, String> {

    List<CommandDispatchLogJpaEntity> findByCommandIdOrderByOccurredAtAsc(String commandId);
}
