package com.evolutionary.operator.interfaces;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 切片24a：套餐覆盖激活 + 有效价 HTTP（AC-26 · ORG-L2 / T-PUB-1）。
 *
 * <p>{@link DirtiesContext}：激活写 PackageOverride + AuditLog，按方法刷新上下文。
 */
@SpringBootTest
@AutoConfigureMockMvc
class ActivatePackageOverrideHttpIT {

    private static final String L2_ACTIVATE_BODY =
            """
            {"actorUserId":"U-SZ","actorOrgId":"ORG-L2","overrideId":"OV-1","patches":{"price":2800}}
            """;
    private static final String NON_DESCENDANT_BODY =
            """
            {"actorUserId":"U-M","actorOrgId":"ORG-NEW","overrideId":"OV-BAD","patches":{"price":2800}}
            """;

    @Autowired private MockMvc mvc;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("ORG-L2 对 T-PUB-1 激活 price=2800 → 200；GET effective → 2800")
    void activateThenEffectivePrice() throws Exception {
        mvc.perform(
                        post("/operator/templates/T-PUB-1/overrides")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(L2_ACTIVATE_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("OV-1"))
                .andExpect(jsonPath("$.orgId").value("ORG-L2"))
                .andExpect(jsonPath("$.templateId").value("T-PUB-1"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.priceCents").value(2800));

        mvc.perform(get("/operator/orgs/ORG-L2/templates/T-PUB-1/effective-product"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateId").value("T-PUB-1"))
                .andExpect(jsonPath("$.priceCents").value(2800))
                .andExpect(jsonPath("$.overrideId").value("OV-1"))
                .andExpect(jsonPath("$.durationDays").value(30));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("非后代 ORG-NEW 激活 → 422 ORG_NOT_DESCENDANT")
    void nonDescendantForbidden() throws Exception {
        mvc.perform(
                        post("/operator/templates/T-PUB-1/overrides")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(NON_DESCENDANT_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("ORG_NOT_DESCENDANT"));
    }
}
