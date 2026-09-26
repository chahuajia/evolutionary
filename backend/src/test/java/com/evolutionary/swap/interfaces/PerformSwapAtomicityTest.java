package com.evolutionary.swap.interfaces;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.evolutionary.station.domain.Station;
import com.evolutionary.swap.application.SwapLogRepository;
import com.evolutionary.swap.infrastructure.JpaStationRepository;
import com.evolutionary.swap.infrastructure.SwapLogJpaRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import jakarta.servlet.ServletException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 换电的**原子性**：站库存与换电日志必须同生共死。
 *
 * <p>{@link com.evolutionary.swap.application.PerformSwap} 的契约写着"同事务双写"，
 * 而全仓此前**没有一处 {@code @Transactional}** —— 也就是说两次写在各自的事务里，
 * 第二次失败时第一次**已经提交**，日志丢了而站已经改了。
 *
 * <p>本测试把第二次写（append 日志）**弄失败**，断言第一次写（站库存）**被回滚**。
 * 没有事务边界时它会红 —— 这正是它存在的意义。
 */
@SpringBootTest
@AutoConfigureMockMvc
class PerformSwapAtomicityTest {

    private static final String STATION_ID = "S1";
    private static final String OUTGOING = "B-out";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaStationRepository stations;

    @Autowired
    private SwapLogJpaRepository swapLogJpa;

    /** 让日志追加失败，模拟"第二次写挂了"。 */
    @MockBean
    private SwapLogRepository swapLogs;

    @BeforeEach
    void seed() {
        swapLogJpa.deleteAllInBatch();
        stations.seed(
                Station.create(
                        STATION_ID,
                        "东门站",
                        List.of(com.evolutionary.battery.domain.Battery.create(OUTGOING))));
        doThrow(new IllegalStateException("模拟：日志追加失败"))
                .when(swapLogs)
                .append(any());
    }

    @Test
    @DisplayName("日志追加失败 → 站库存必须回滚（B-out 仍在站内）")
    void stationRolledBackWhenLogAppendFails() throws Exception {
        // `SwapApiErrorTranslator` 对未知 RuntimeException 是**原样 rethrow**（见其末行），
        // 所以这里不会有 5xx 响应，而是把异常抛穿 MockMvc —— 无妨，
        // 本测试要断言的不是响应码，是**站库存有没有回滚**。
        try {
            mockMvc.perform(
                    post("/stations/" + STATION_ID + "/swaps")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"incomingBatteryId\":\"B-in\"}"));
        } catch (ServletException | IllegalStateException expected) {
            // 预期：模拟的日志追加失败被抛穿
        }

        // 关键断言：换电没成功，站必须**原样不动**
        Station after = stations.get(STATION_ID);
        List<String> batteryIds = after.batteries().stream().map(b -> b.id()).toList();
        assertTrue(
                batteryIds.contains(OUTGOING),
                "日志写失败后站库存应回滚，但 B-out 已离站 —— 说明两次写不在同一事务。实际: " + batteryIds);
    }
}
