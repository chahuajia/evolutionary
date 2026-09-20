package com.evolutionary.swap.interfaces;

import com.evolutionary.station.domain.Station;
import com.evolutionary.swap.application.GetStation;
import com.evolutionary.swap.application.ListStations;
import com.evolutionary.swap.application.ListSwapLogs;
import com.evolutionary.swap.application.PerformSwap;
import com.evolutionary.swap.domain.SwapLog;
import com.evolutionary.swap.domain.SwapSession;
import java.time.Instant;
import java.util.List;
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

    /**
     * 事务边界包装，不是裸的 {@code PerformSwap} —— 站库存与换电日志必须同生共死，
     * 而用例本身要保持零框架依赖（见 {@link TransactionalPerformSwap}）。
     */
    private final TransactionalPerformSwap performSwap;

    private final ListStations listStations;
    private final GetStation getStation;
    private final ListSwapLogs listSwapLogs;

    public SwapController(
            TransactionalPerformSwap performSwap,
            ListStations listStations,
            GetStation getStation,
            ListSwapLogs listSwapLogs) {
        this.performSwap = performSwap;
        this.listStations = listStations;
        this.getStation = getStation;
        this.listSwapLogs = listSwapLogs;
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
        return new StationView(
                station.id(), station.name(), station.canSwapOut(), batteries);
    }

    @GetMapping("/{stationId}/swap-logs")
    public List<SwapLogView> swapLogs(@PathVariable String stationId) {
        return listSwapLogs.execute(stationId).stream().map(SwapController::toLogView).toList();
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

    private static SwapLogView toLogView(SwapLog log) {
        return new SwapLogView(
                log.id(),
                log.stationId(),
                log.outgoingBatteryId(),
                log.incomingBatteryId(),
                log.occurredAt());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<SwapApiErrorTranslator.ApiError> handleRuntime(RuntimeException ex) {
        SwapApiErrorTranslator.Translated translated = SwapApiErrorTranslator.translate(ex);
        return ResponseEntity.status(translated.status()).body(translated.body());
    }

    public record SwapRequest(String incomingBatteryId) {}

    public record SwapResponse(String stationId, String outgoingId, String incomingId) {
        static SwapResponse from(SwapSession session) {
            return new SwapResponse(
                    session.stationId(), session.outgoing().id(), session.incoming().id());
        }
    }

    public record StationView(
            String id, String name, boolean canSwapOut, List<BatteryView> batteries) {}

    public record StationSummaryView(
            String id, String name, boolean canSwapOut, int batteryCount) {}

    public record BatteryView(String id, String status) {}

    public record SwapLogView(
            String id,
            String stationId,
            String outgoingBatteryId,
            String incomingBatteryId,
            Instant occurredAt) {}
}
