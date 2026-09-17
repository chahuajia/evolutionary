package com.evolutionary.credit.interfaces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CreditControllerTest {

    @Autowired private MockMvc mvc;

    @Test
    void profileAndStatementsForU1() throws Exception {
        mvc.perform(get("/credit/profiles/U1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("U1"))
                .andExpect(jsonPath("$.creditLimit").value(10_000))
                .andExpect(jsonPath("$.usedCredit").value(3_000))
                .andExpect(jsonPath("$.status").value("good"));

        String body =
                mvc.perform(get("/credit/profiles/U1/statements"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(2))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        assertEquals(true, body.contains("STMT-2026-02"));
        assertEquals(true, body.contains("DUE") || body.contains("PAID"));
    }

    @Test
    void unknownUser404() throws Exception {
        mvc.perform(get("/credit/profiles/NOPE")).andExpect(status().isNotFound());
    }
}
