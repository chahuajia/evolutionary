package com.evolutionary.swap.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.battery.domain.Battery;
import com.evolutionary.station.domain.Station;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ListStationsTest {

    private static final class InMemoryStations implements StationRepository {
        private final Map<String, Station> store = new HashMap<>();

        @Override
        public Station get(String stationId) {
            return store.get(stationId);
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
    @DisplayName("返回全部站且按 id 排序")
    void listsSorted() {
        InMemoryStations repo = new InMemoryStations();
        repo.save(Station.create("S2", "西"));
        repo.save(Station.create("S1", "东", List.of(Battery.create("B1"))));

        List<Station> result = new ListStations(repo).execute();

        assertEquals(2, result.size());
        assertEquals("S1", result.get(0).id());
        assertEquals("S2", result.get(1).id());
        assertTrue(result.get(0).canSwapOut());
    }
}
