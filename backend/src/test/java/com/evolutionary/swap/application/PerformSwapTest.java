package com.evolutionary.swap.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.battery.domain.Battery;
import com.evolutionary.battery.domain.BatteryStatus;
import com.evolutionary.station.domain.Station;
import com.evolutionary.station.domain.Station.NoAvailableBatteryException;
import com.evolutionary.swap.domain.SwapSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PerformSwapTest {

    private static final class InMemoryStations implements StationRepository {
        private final Map<String, Station> store = new HashMap<>();

        @Override
        public Station get(String stationId) {
            Station station = store.get(stationId);
            if (station == null) {
                throw new IllegalArgumentException("unknown station: " + stationId);
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

    @Test
    @DisplayName("用例：换电后会话正确，且仓库里是换电后的站")
    void executesAndPersists() {
        InMemoryStations repo = new InMemoryStations();
        repo.save(Station.create("S1", "东门站", List.of(Battery.create("B-out"))));

        PerformSwap useCase = new PerformSwap(repo);
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
    }

    @Test
    @DisplayName("空站：领域异常穿透用例（应用层不吞）")
    void propagatesDomainFailure() {
        InMemoryStations repo = new InMemoryStations();
        repo.save(Station.create("S1", "东门站"));
        PerformSwap useCase = new PerformSwap(repo);

        assertThrows(
                NoAvailableBatteryException.class,
                () -> useCase.execute("S1", Battery.create("B-in").swapOut()));
        assertTrue(repo.get("S1").batteries().isEmpty());
    }

    @Test
    @DisplayName("未知站点：仓库报错，未调用领域")
    void unknownStation() {
        PerformSwap useCase = new PerformSwap(new InMemoryStations());
        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute("missing", Battery.create("B-in").swapOut()));
    }
}
