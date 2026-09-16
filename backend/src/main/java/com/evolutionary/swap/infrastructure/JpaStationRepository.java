package com.evolutionary.swap.infrastructure;

import com.evolutionary.battery.domain.Battery;
import com.evolutionary.battery.domain.BatteryStatus;
import com.evolutionary.station.domain.Station;
import com.evolutionary.swap.application.StationRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 应用端口的 JPA 适配：Entity ↔ 领域 Station 映射发生在这里，不进 domain。
 */
@Component
public final class JpaStationRepository implements StationRepository {

    private final StationJpaRepository jpa;
    private final ObjectMapper json;

    public JpaStationRepository(StationJpaRepository jpa, ObjectMapper json) {
        this.jpa = jpa;
        this.json = json;
    }

    @Override
    public Station get(String stationId) {
        StationJpaEntity row =
                jpa.findById(stationId)
                        .orElseThrow(() -> new IllegalArgumentException("unknown station: " + stationId));
        return toDomain(row);
    }

    @Override
    public void save(Station station) {
        jpa.save(toRow(station));
    }

    @Override
    public List<Station> findAll() {
        List<Station> result = new ArrayList<>();
        for (StationJpaEntity row : jpa.findAll()) {
            result.add(toDomain(row));
        }
        return result;
    }

    public void seed(Station station) {
        save(station);
    }

    private Station toDomain(StationJpaEntity row) {
        try {
            List<BatterySnap> snaps =
                    json.readValue(row.getBatteriesJson(), new TypeReference<>() {});
            List<Battery> batteries = new ArrayList<>();
            for (BatterySnap snap : snaps) {
                batteries.add(Battery.rehydrate(snap.id(), BatteryStatus.valueOf(snap.status())));
            }
            return Station.create(row.getId(), row.getName(), batteries);
        } catch (Exception e) {
            throw new IllegalStateException("corrupt station row: " + row.getId(), e);
        }
    }

    private StationJpaEntity toRow(Station station) {
        try {
            List<BatterySnap> snaps = new ArrayList<>();
            for (Battery battery : station.batteries()) {
                snaps.add(new BatterySnap(battery.id(), battery.status().name()));
            }
            return new StationJpaEntity(
                    station.id(), station.name(), json.writeValueAsString(snaps));
        } catch (Exception e) {
            throw new IllegalStateException("cannot serialize station: " + station.id(), e);
        }
    }

    private record BatterySnap(String id, String status) {}
}
