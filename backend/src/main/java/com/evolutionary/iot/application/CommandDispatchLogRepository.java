package com.evolutionary.iot.application;

import com.evolutionary.iot.domain.CommandDispatchLog;
import java.util.List;

/** 命令下发审计端口（append-only；IoT 现为 InMemory 半成品）。 */
public interface CommandDispatchLogRepository {

    void append(CommandDispatchLog log);

    List<CommandDispatchLog> findByCommandId(String commandId);
}
