package com.evolutionary.station.domain;

import com.evolutionary.battery.domain.Battery;
import com.evolutionary.battery.domain.BatteryStatus;
import com.evolutionary.swap.domain.SwapSession;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 换电站聚合根 —— 持有在站电池。
 *
 * <p><b>操作不变量</b>（不是构造不变量）：至少 1 块 {@code AVAILABLE} 才能换出。
 * 依据症状表「不变量放哪一层」→ [[S13]]：对象自守；跨对象由聚合操作守。
 * 「有没有可用电池」是站点库存规则，放在 {@link #swap}，不放在构造器
 * （否则空站 / 全在充电的站无法存在）。
 *
 * <p><b>零框架</b>：症状表「领域层能不能碰框架」→ [[domain-purity-is-structural]]。
 */
public final class Station {

    private final String id;
    private final String name;
    private final Map<String, Battery> batteries;

    private Station(String id, String name, Map<String, Battery> batteries) {
        this.id = id;
        this.name = name;
        this.batteries = Map.copyOf(batteries);
    }

    public static Station create(String id, String name) {
        return create(id, name, List.of());
    }

    public static Station create(String id, String name, Collection<Battery> initial) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("station id must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("station name must not be blank");
        }
        Objects.requireNonNull(initial, "batteries");
        Map<String, Battery> byId = new LinkedHashMap<>();
        for (Battery battery : initial) {
            putOnRack(byId, battery);
        }
        return new Station(id, name, byId);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public List<Battery> batteries() {
        return List.copyOf(batteries.values());
    }

    public boolean canSwapOut() {
        return batteries.values().stream().anyMatch(b -> b.status() == BatteryStatus.AVAILABLE);
    }

    /** 放入一块在站电池；原对象不变。 */
    public Station stock(Battery battery) {
        Map<String, Battery> next = new LinkedHashMap<>(batteries);
        putOnRack(next, battery);
        return new Station(id, name, next);
    }

    /**
     * 一次换电：换出第一块 AVAILABLE（离站），换进用户归还的 IN_USE（入站变 CHARGING）。
     *
     * @throws NoAvailableBatteryException 无可用电池
     */
    public Swap swap(Battery incoming) {
        Objects.requireNonNull(incoming, "incoming");
        Battery available = batteries.values().stream()
                .filter(b -> b.status() == BatteryStatus.AVAILABLE)
                .findFirst()
                .orElseThrow(() -> new NoAvailableBatteryException(id));
        if (incoming.id().equals(available.id())) {
            throw new IllegalArgumentException("incoming and outgoing must differ");
        }
        if (batteries.containsKey(incoming.id())) {
            throw new IllegalArgumentException("incoming already at station");
        }

        Battery outgoing = available.swapOut();
        Battery returned = incoming.returnForCharging();

        Map<String, Battery> next = new LinkedHashMap<>(batteries);
        next.remove(available.id());
        next.put(returned.id(), returned);
        return new Swap(new Station(id, name, next), SwapSession.record(id, outgoing, returned));
    }

    private static void putOnRack(Map<String, Battery> rack, Battery battery) {
        Objects.requireNonNull(battery, "battery");
        if (battery.status() == BatteryStatus.IN_USE) {
            throw new IllegalArgumentException("cannot stock an in-use battery");
        }
        if (rack.containsKey(battery.id())) {
            throw new IllegalArgumentException("duplicate battery id: " + battery.id());
        }
        rack.put(battery.id(), battery);
    }

    public static final class Swap {
        private final Station station;
        private final SwapSession session;

        private Swap(Station station, SwapSession session) {
            this.station = station;
            this.session = session;
        }

        public Station station() {
            return station;
        }

        public SwapSession session() {
            return session;
        }
    }

    public static final class NoAvailableBatteryException extends RuntimeException {
        NoAvailableBatteryException(String stationId) {
            super("station " + stationId + " has no available battery");
        }
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Station s && id.equals(s.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
