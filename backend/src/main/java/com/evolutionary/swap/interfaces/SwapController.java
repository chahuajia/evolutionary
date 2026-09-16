package com.evolutionary.swap.interfaces;

import com.evolutionary.battery.domain.Battery;
import com.evolutionary.station.domain.Station;
import com.evolutionary.station.domain.Station.NoAvailableBatteryException;
import com.evolutionary.swap.application.PerformSwap;
import com.evolutionary.swap.application.StationRepository;
import com.evolutionary.swap.domain.SwapSession;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP 适配器。边界负责把 JSON 解析成领域能吃的类型（parse-dont-validate）。
 */
@RestController
@RequestMapping("/stations")
public class SwapController {

    private final PerformSwap performSwap;
    private final StationRepository stations;

    public SwapController(PerformSwap performSwap, StationRepository stations) {
        this.performSwap = performSwap;
        this.stations = stations;
    }

    @GetMapping("/{stationId}")
    public StationView get(@PathVariable String stationId) {
        Station station = stations.get(stationId);
        List<BatteryView> batteries =
                station.batteries().stream()
                        .map(b -> new BatteryView(b.id(), b.status().name()))
                        .toList();
        return new StationView(station.id(), station.name(), batteries);
    }

    @PostMapping("/{stationId}/swaps")
    public SwapResponse swap(
            @PathVariable String stationId, @RequestBody SwapRequest body) {
        if (body == null || body.incomingBatteryId() == null || body.incomingBatteryId().isBlank()) {
            throw new IllegalArgumentException("incomingBatteryId required");
        }
        // 边界：外部只给 id；本轮简化为「用户持有一块 IN_USE 电池」
        Battery incoming = Battery.create(body.incomingBatteryId()).swapOut();
        SwapSession session = performSwap.execute(stationId, incoming);
        return SwapResponse.from(session);
    }

    @ExceptionHandler(NoAvailableBatteryException.class)
    public ResponseEntity<Map<String, String>> noBattery(NoAvailableBatteryException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException ex) {
        String msg = ex.getMessage() == null ? "bad request" : ex.getMessage();
        HttpStatus status = msg.startsWith("unknown station") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of("error", msg));
    }

    public record SwapRequest(String incomingBatteryId) {}

    public record SwapResponse(String stationId, String outgoingId, String incomingId) {
        static SwapResponse from(SwapSession session) {
            return new SwapResponse(
                    session.stationId(), session.outgoing().id(), session.incoming().id());
        }
    }

    /** 视图 DTO —— 不是领域对象的移植。 */
    public record StationView(String id, String name, List<BatteryView> batteries) {}

    public record BatteryView(String id, String status) {}
}
