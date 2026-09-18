package com.evolutionary.swap.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.battery.domain.Battery;
import com.evolutionary.battery.domain.BatteryStatus;
import com.evolutionary.station.domain.Station;
import com.evolutionary.station.domain.Station.NoAvailableBatteryException;
import com.evolutionary.swap.domain.SwapLog;
import com.evolutionary.swap.domain.SwapSession;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PerformSwapTest {

    private static final Instant T0 = Instant.parse("2026-09-18T04:00:00Z");
    private static final Clock FIXED = Clock.fixed(T0, ZoneOffset.UTC);

    private static final class InMemoryStations implements StationRepository {
        private final Map<String, Station> store = new HashMap<>();

        @Override
        public Station get(String stationId) {
            Station station = store.get(stationId);
            if (station == null) {
                throw new UnknownStationException(stationId);
            }
            return station;
        }

        @Override
        public void save(Station station) {
            store.put(station.id(), station);
        }

        @Override
        public List<Station> findAll() {
            return List.copyOf(store.values());
        }
    }

    private static final class InMemorySwapLogs implements SwapLogRepository {
        private final List<SwapLog> store = new ArrayList<>();

        @Override
        public void append(SwapLog log) {
            store.add(log);
        }

        @Override
        public List<SwapLog> findByStationId(String stationId) {
            return store.stream().filter(l -> l.stationId().equals(stationId)).toList();
        }
    }

    @Test
    @DisplayName("用例：换电后会话正确，站已存，且追加 1 条换电日志")
    void executesAndPersistsWithLog() {
        InMemoryStations repo = new InMemoryStations();
        InMemorySwapLogs logs = new InMemorySwapLogs();
        repo.save(Station.create("S1", "东门站", List.of(Battery.create("B-out"))));

        PerformSwap useCase = new PerformSwap(repo, logs, FIXED);
        SwapSession session = useCase.execute("S1", Battery.create("B-in").swapOut());

        assertEquals("S1", session.stationId());
        assertEquals("B-out", session.outgoing().id());
        assertEquals(BatteryStatus.IN_USE, session.outgoing().status());
        assertEquals("B-in", session.incoming().id());
        assertEquals(BatteryStatus.CHARGING, session.incoming().status());

        Station saved = repo.get("S1");
        assertEquals(1, saved.batteries().size());
        assertEquals("B-in", saved.batteries().get(0).id());
        assertEquals(BatteryStatus.CHARGING, saved.batteries().get(0).status());

        List<SwapLog> byStation = logs.findByStationId("S1");
        assertEquals(1, byStation.size());
        assertEquals("B-out", byStation.get(0).outgoingBatteryId());
        assertEquals("B-in", byStation.get(0).incomingBatteryId());
        assertEquals(T0, byStation.get(0).occurredAt());
    }

    @Test
    @DisplayName("空站：领域异常穿透，不写日志")
    void propagatesDomainFailureNoLog() {
        InMemoryStations repo = new InMemoryStations();
        InMemorySwapLogs logs = new InMemorySwapLogs();
        repo.save(Station.create("S1", "东门站"));
        PerformSwap useCase = new PerformSwap(repo, logs, FIXED);

        assertThrows(
                NoAvailableBatteryException.class,
                () -> useCase.execute("S1", Battery.create("B-in").swapOut()));
        assertTrue(repo.get("S1").batteries().isEmpty());
        assertTrue(logs.findByStationId("S1").isEmpty());
    }

    @Test
    @DisplayName("未知站点：仓库报错，未写日志")
    void unknownStation() {
        InMemorySwapLogs logs = new InMemorySwapLogs();
        PerformSwap useCase = new PerformSwap(new InMemoryStations(), logs, FIXED);
        assertThrows(
                UnknownStationException.class,
                () -> useCase.execute("missing", Battery.create("B-in").swapOut()));
        assertTrue(logs.findByStationId("missing").isEmpty());
    }
}
