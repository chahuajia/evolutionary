package com.evolutionary.swap.interfaces;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.evolutionary.battery.domain.Battery;
import com.evolutionary.station.domain.Station;
import com.evolutionary.swap.application.StationRepository;
import com.evolutionary.swap.infrastructure.JpaStationRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SwapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaStationRepository stations;

    @Autowired
    private StationRepository stationPort;

    @BeforeEach
    void seed() {
        stations.seed(Station.create("S1", "东门站", List.of(Battery.create("B-out"))));
    }

    @Test
    @DisplayName("缺 incomingBatteryId → 400（parse 边界）")
    void badRequestWhenMissingIncoming() throws Exception {
        mockMvc.perform(
                        post("/stations/S1/swaps")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("incomingBatteryId required"));
    }

    @Test
    @DisplayName("POST 换电 → 200 + 会话字段")
    void swapOk() throws Exception {
        mockMvc.perform(
                        post("/stations/S1/swaps")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"incomingBatteryId\":\"B-in\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stationId").value("S1"))
                .andExpect(jsonPath("$.outgoingId").value("B-out"))
                .andExpect(jsonPath("$.incomingId").value("B-in"));
    }

    @Test
    @DisplayName("无可用电池 → 409 + suggestion（S34）")
    void conflictWhenEmpty() throws Exception {
        stations.seed(Station.create("S-empty", "空站"));
        mockMvc.perform(
                        post("/stations/S-empty/swaps")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"incomingBatteryId\":\"B-in\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("station S-empty has no available battery"))
                .andExpect(jsonPath("$.suggestion").exists());
    }

    @Test
    @DisplayName("未知站 → 404")
    void notFound() throws Exception {
        mockMvc.perform(
                        post("/stations/missing/swaps")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"incomingBatteryId\":\"B-in\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /stations → 多站概览（第 11 轮）")
    void listStations() throws Exception {
        stations.seed(Station.create("S2", "西门站", List.of(Battery.create("B2"))));
        stations.seed(Station.create("S3", "南站"));

        mockMvc.perform(get("/stations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[?(@.id=='S1')].canSwapOut").value(true))
                .andExpect(jsonPath("$[?(@.id=='S3')].canSwapOut").value(false));
    }

    @Test
    @DisplayName("GET 站点 → 200 + 视图字段（第 8 轮）")
    void getStation() throws Exception {
        mockMvc.perform(get("/stations/S1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("S1"))
                .andExpect(jsonPath("$.name").value("东门站"))
                .andExpect(jsonPath("$.batteries[0].id").value("B-out"));
    }

    @Test
    @DisplayName("换电后通过仓储再读：状态已持久化（J3）")
    void persistsAcrossReads() throws Exception {
        mockMvc.perform(
                        post("/stations/S1/swaps")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"incomingBatteryId\":\"B-in\"}"))
                .andExpect(status().isOk());

        Station reloaded = stationPort.get("S1");
        org.junit.jupiter.api.Assertions.assertEquals(1, reloaded.batteries().size());
        org.junit.jupiter.api.Assertions.assertEquals("B-in", reloaded.batteries().get(0).id());
        org.junit.jupiter.api.Assertions.assertEquals(
                com.evolutionary.battery.domain.BatteryStatus.CHARGING,
                reloaded.batteries().get(0).status());
    }
}
