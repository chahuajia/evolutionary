package com.evolutionary.iot.application;

import com.evolutionary.iot.domain.BatteryCommand;
import com.evolutionary.iot.domain.BatteryCommandAcked;
import com.evolutionary.iot.domain.CommandDispatchLog;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * 命令幂等网关（AC-59）：24h 内同 commandId 只物理下发一次；每次物理下发 append 审计。
 */
public final class IdempotentCommandGateway {

    public static final Duration DEDUPE_WINDOW = Duration.ofHours(24);

    private final Function<BatteryCommand, BatteryCommandAcked> physicalSend;
    private final CommandDispatchLogRepository dispatchLogs;
    private final Clock clock;
    private final Map<String, CachedAck> cache = new ConcurrentHashMap<>();
    private final AtomicInteger physicalSendCount = new AtomicInteger();

    public IdempotentCommandGateway(
            Function<BatteryCommand, BatteryCommandAcked> physicalSend,
            CommandDispatchLogRepository dispatchLogs,
            Clock clock) {
        this.physicalSend = Objects.requireNonNull(physicalSend, "physicalSend");
        this.dispatchLogs = Objects.requireNonNull(dispatchLogs, "dispatchLogs");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public BatteryCommandAcked send(BatteryCommand command) {
        Objects.requireNonNull(command, "command");
        Instant now = clock.instant();
        prune(now);

        CachedAck cached = cache.get(command.commandId());
        if (cached != null && Duration.between(cached.storedAt, now).compareTo(DEDUPE_WINDOW) <= 0) {
            return cached.ack;
        }

        physicalSendCount.incrementAndGet();
        try {
            BatteryCommandAcked ack = physicalSend.apply(command);
            dispatchLogs.append(CommandDispatchLog.fromAck(ack));
            cache.put(command.commandId(), new CachedAck(ack, now));
            return ack;
        } catch (RuntimeException ex) {
            dispatchLogs.append(
                    CommandDispatchLog.of(
                            command.commandId(),
                            command.batteryId(),
                            command.action(),
                            false,
                            now));
            throw ex;
        }
    }

    public int physicalSendCount() {
        return physicalSendCount.get();
    }

    private void prune(Instant now) {
        cache.entrySet()
                .removeIf(
                        e -> Duration.between(e.getValue().storedAt, now).compareTo(DEDUPE_WINDOW) > 0);
    }

    private record CachedAck(BatteryCommandAcked ack, Instant storedAt) {}
}
