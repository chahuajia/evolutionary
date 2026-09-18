package com.evolutionary.iot.infrastructure;

import com.evolutionary.iot.application.CommandDispatchLogRepository;
import com.evolutionary.iot.domain.CommandDispatchLog;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** 进程内命令下发审计（单测用；生产由 {@link JpaCommandDispatchLogRepository} 接管）。 */
public final class InMemoryCommandDispatchLogRepository implements CommandDispatchLogRepository {

    private final List<CommandDispatchLog> logs = new CopyOnWriteArrayList<>();

    @Override
    public void append(CommandDispatchLog log) {
        logs.add(log);
    }

    @Override
    public List<CommandDispatchLog> findByCommandId(String commandId) {
        List<CommandDispatchLog> matched = new ArrayList<>();
        for (CommandDispatchLog log : logs) {
            if (log.commandId().equals(commandId)) {
                matched.add(log);
            }
        }
        return List.copyOf(matched);
    }
}
