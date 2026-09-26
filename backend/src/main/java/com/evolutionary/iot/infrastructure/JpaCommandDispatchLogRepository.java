package com.evolutionary.iot.infrastructure;

import com.evolutionary.iot.application.CommandDispatchLogRepository;
import com.evolutionary.iot.domain.CommandDispatchLog;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** 命令下发审计 JPA 适配（append-only；表 command_dispatch_logs）。 */
@Component
public final class JpaCommandDispatchLogRepository implements CommandDispatchLogRepository {

    private final CommandDispatchLogJpaRepository jpa;

    public JpaCommandDispatchLogRepository(CommandDispatchLogJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void append(CommandDispatchLog log) {
        jpa.save(
                new CommandDispatchLogJpaEntity(
                        log.id(),
                        log.commandId(),
                        log.batteryId(),
                        log.action(),
                        log.ack(),
                        log.occurredAt()));
    }

    @Override
    public List<CommandDispatchLog> findByCommandId(String commandId) {
        List<CommandDispatchLog> result = new ArrayList<>();
        for (CommandDispatchLogJpaEntity row :
                jpa.findByCommandIdOrderByOccurredAtAsc(commandId)) {
            result.add(
                    CommandDispatchLog.rehydrate(
                            row.getId(),
                            row.getCommandId(),
                            row.getBatteryId(),
                            row.getAction(),
                            row.isAck(),
                            row.getOccurredAt()));
        }
        return result;
    }
}
