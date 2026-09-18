package com.evolutionary.iot.application;

import com.evolutionary.iot.domain.CommandDispatchLog;
import java.util.List;

/** 命令下发审计端口（append-only）。 */
public interface CommandDispatchLogRepository {

    void append(CommandDispatchLog log);

    List<CommandDispatchLog> findByCommandId(String commandId);
}
