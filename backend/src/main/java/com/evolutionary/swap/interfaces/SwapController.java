package com.evolutionary.swap.interfaces;

import com.evolutionary.station.domain.Station;
import com.evolutionary.station.domain.Station.NoAvailableBatteryException;
import com.evolutionary.swap.application.GetStation;
import com.evolutionary.swap.application.ListStations;
import com.evolutionary.swap.application.PerformSwap;
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
 * 读写均经 application 用例 —— 见 specs/round-13.md。
 */
@RestController
@RequestMapping("/stations")
public class SwapController {

    private final PerformSwap performSwap;
    private final ListStations listStations;
    private final GetStation getStation;

    public SwapController(PerformSwap performSwap, ListStations listStations, GetStation getStation) {
        this.performSwap = performSwap;
        this.listStations = listStations;
        this.getStation = getStation;
    }

    @GetMapping
    public List<StationSummaryView> list() {
        return listStations.execute().stream().map(SwapController::toSummary).toList();
    }

    @GetMapping("/{stationId}")
    public StationView get(@PathVariable String stationId) {
        Station station = getStation.execute(stationId);
        List<BatteryView> batteries =
                station.batteries().stream()
                        .map(b -> new BatteryView(b.id(), b.status().name()))
                        .toList();
        return new StationView(station.id(), station.name(), batteries);
    }

    @PostMapping("/{stationId}/swaps")
    public SwapResponse swap(
            @PathVariable String stationId, @RequestBody SwapRequest body) {
        IncomingSwapRequest parsed =
                IncomingSwapRequest.parse(body == null ? null : body.incomingBatteryId());
        SwapSession session = performSwap.execute(stationId, parsed.incomingBattery());
        return SwapResponse.from(session);
    }

    private static StationSummaryView toSummary(Station station) {
        return new StationSummaryView(
                station.id(), station.name(), station.canSwapOut(), station.batteries().size());
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

    public record StationView(String id, String name, List<BatteryView> batteries) {}

    public record StationSummaryView(
            String id, String name, boolean canSwapOut, int batteryCount) {}

    public record BatteryView(String id, String status) {}
}
