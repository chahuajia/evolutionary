package com.evolutionary.iot.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.iot.domain.BatteryCommand;
import com.evolutionary.iot.domain.BatteryCommandAcked;
import com.evolutionary.iot.domain.CommandDispatchLog;
import com.evolutionary.iot.domain.DeviceShadow;
import com.evolutionary.iot.domain.IotErrorCode;
import com.evolutionary.iot.domain.IotOutcome;
import com.evolutionary.iot.infrastructure.InMemoryCommandDispatchLogRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** AC-58 / AC-59；切片31b 命令下发审计。 */
class CommandIdempotencyAndStaleGuardTest {

    private static final Instant T0 = Instant.parse("2026-09-17T10:00:00Z");

    @Test
    @DisplayName("AC-59：24h 内同 commandId 只物理下发一次，回执相同")
    void duplicateCommandIdDeduped() {
        AtomicInteger physical = new AtomicInteger();
        Clock clock = Clock.fixed(T0, ZoneOffset.UTC);
        InMemoryCommandDispatchLogRepository logs = new InMemoryCommandDispatchLogRepository();
        IdempotentCommandGateway gateway =
                new IdempotentCommandGateway(
                        cmd -> {
                            physical.incrementAndGet();
                            return BatteryCommandAcked.of(
                                    cmd.commandId(),
                                    cmd.batteryId(),
                                    cmd.action(),
                                    true,
                                    clock.instant());
                        },
                        logs,
                        clock);

        BatteryCommand cmd =
                BatteryCommand.of("CMD-X", "B1", BatteryCommand.Action.LOCK, "U1", T0);
        BatteryCommandAcked first = gateway.send(cmd);
        BatteryCommandAcked second = gateway.send(cmd);

        assertEquals(1, physical.get());
        assertEquals(1, gateway.physicalSendCount());
        assertEquals(first.commandId(), second.commandId());
        assertTrue(first.success() && second.success());
        assertEquals(1, logs.findByCommandId("CMD-X").size(), "幂等命中不再追加审计");
    }

    @Test
    @DisplayName("切片31b：物理下发成功 → CommandDispatchLog ack=true")
    void physicalSendAppendsSuccessAudit() {
        Clock clock = Clock.fixed(T0, ZoneOffset.UTC);
        InMemoryCommandDispatchLogRepository logs = new InMemoryCommandDispatchLogRepository();
        IdempotentCommandGateway gateway =
                new IdempotentCommandGateway(
                        cmd ->
                                BatteryCommandAcked.of(
                                        cmd.commandId(),
                                        cmd.batteryId(),
                                        cmd.action(),
                                        true,
                                        clock.instant()),
                        logs,
                        clock);

        gateway.send(BatteryCommand.of("CMD-OK", "B1", BatteryCommand.Action.UNLOCK, "U1", T0));

        List<CommandDispatchLog> rows = logs.findByCommandId("CMD-OK");
        assertEquals(1, rows.size());
        assertTrue(rows.get(0).ack());
        assertEquals("B1", rows.get(0).batteryId());
        assertEquals(BatteryCommand.Action.UNLOCK, rows.get(0).action());
    }

    @Test
    @DisplayName("切片31b：物理下发失败 ack → CommandDispatchLog ack=false")
    void physicalSendAppendsFailureAckAudit() {
        Clock clock = Clock.fixed(T0, ZoneOffset.UTC);
        InMemoryCommandDispatchLogRepository logs = new InMemoryCommandDispatchLogRepository();
        IdempotentCommandGateway gateway =
                new IdempotentCommandGateway(
                        cmd ->
                                BatteryCommandAcked.of(
                                        cmd.commandId(),
                                        cmd.batteryId(),
                                        cmd.action(),
                                        false,
                                        clock.instant()),
                        logs,
                        clock);

        BatteryCommandAcked ack =
                gateway.send(BatteryCommand.of("CMD-FAIL", "B2", BatteryCommand.Action.RESET, "U1", T0));

        assertFalse(ack.success());
        List<CommandDispatchLog> rows = logs.findByCommandId("CMD-FAIL");
        assertEquals(1, rows.size());
        assertFalse(rows.get(0).ack());
    }

    @Test
    @DisplayName("切片31b：物理下发抛错 → 仍 append ack=false 审计")
    void physicalSendExceptionStillAudited() {
        Clock clock = Clock.fixed(T0, ZoneOffset.UTC);
        InMemoryCommandDispatchLogRepository logs = new InMemoryCommandDispatchLogRepository();
        IdempotentCommandGateway gateway =
                new IdempotentCommandGateway(
                        cmd -> {
                            throw new IllegalStateException("vendor down");
                        },
                        logs,
                        clock);

        assertThrows(
                IllegalStateException.class,
                () ->
                        gateway.send(
                                BatteryCommand.of(
                                        "CMD-EX", "B3", BatteryCommand.Action.LOCK, "U1", T0)));

        List<CommandDispatchLog> rows = logs.findByCommandId("CMD-EX");
        assertEquals(1, rows.size());
        assertFalse(rows.get(0).ack());
        assertEquals("B3", rows.get(0).batteryId());
    }

    @Test
    @DisplayName("AC-58：shadow stale → TELEMETRY_STALE，禁止计量")
    void staleBlocksMetered() {
        Instant old = T0.minus(DeviceShadow.STALE_AFTER).minusSeconds(10);
        InMemoryShadows shadows = new InMemoryShadows();
        shadows.save(DeviceShadow.seed("B1", "vendorA", "ext-1", 80, 4200, old));

        AssertShadowFreshForMetered guard =
                new AssertShadowFreshForMetered(
                        shadows, Clock.fixed(T0, ZoneOffset.UTC));
        IotOutcome<DeviceShadow> outcome = guard.execute("B1");
        assertInstanceOf(IotOutcome.Err.class, outcome);
        assertEquals(
                IotErrorCode.TELEMETRY_STALE,
                ((IotOutcome.Err<DeviceShadow>) outcome).code());
    }

    @Test
    @DisplayName("新鲜影子允许计量读取")
    void freshShadowAllowed() {
        InMemoryShadows shadows = new InMemoryShadows();
        shadows.save(DeviceShadow.seed("B1", "vendorA", "ext-1", 80, 4200, T0));
        AssertShadowFreshForMetered guard =
                new AssertShadowFreshForMetered(
                        shadows, Clock.fixed(T0, ZoneOffset.UTC));
        IotOutcome<DeviceShadow> outcome = guard.execute("B1");
        assertInstanceOf(IotOutcome.Ok.class, outcome);
        assertEquals(80, ((IotOutcome.Ok<DeviceShadow>) outcome).value().soc());
    }

    private static final class InMemoryShadows implements DeviceShadowRepository {
        private final Map<String, DeviceShadow> byId = new HashMap<>();

        @Override
        public void save(DeviceShadow shadow) {
            byId.put(shadow.batteryId(), shadow);
        }

        @Override
        public Optional<DeviceShadow> findByBatteryId(String batteryId) {
            return Optional.ofNullable(byId.get(batteryId));
        }
    }
}
