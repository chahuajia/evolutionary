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
 * 切片25a：套餐覆盖撤销 HTTP（AC-31 · ORG-L2 / T-PUB-1 / OV-1）。
 *
 * <p>{@link DirtiesContext}：激活+撤销写 PackageOverride + AuditLog，按方法刷新上下文。
 */
@SpringBootTest
@AutoConfigureMockMvc
class RevokePackageOverrideHttpIT {

    private static final String L2_ACTIVATE_BODY =
            """
            {"actorUserId":"U-SZ","actorOrgId":"ORG-L2","overrideId":"OV-1","patches":{"price":2800}}
            """;
    private static final String L2_REVOKE_BODY =
            """
            {"actorUserId":"U-SZ","actorOrgId":"ORG-L2"}
            """;
    private static final String NON_OWNER_REVOKE_BODY =
            """
            {"actorUserId":"U-M","actorOrgId":"ORG-NEW"}
            """;

    @Autowired private MockMvc mvc;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("激活 OV-1 → revoke 200 REVOKED → GET effective 回落模板原价 3000")
    void activateThenRevokeRestoresTemplatePrice() throws Exception {
        mvc.perform(
                        post("/operator/templates/T-PUB-1/overrides")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(L2_ACTIVATE_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("OV-1"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.priceCents").value(2800));

        mvc.perform(
                        post("/operator/overrides/OV-1/revoke")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(L2_REVOKE_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("OV-1"))
                .andExpect(jsonPath("$.status").value("REVOKED"));

        mvc.perform(get("/operator/orgs/ORG-L2/templates/T-PUB-1/effective-product"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateId").value("T-PUB-1"))
                .andExpect(jsonPath("$.priceCents").value(3000));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("非所属 org 撤销 → 4xx ORG_NOT_OWNER")
    void nonOwnerForbidden() throws Exception {
        mvc.perform(
                        post("/operator/templates/T-PUB-1/overrides")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(L2_ACTIVATE_BODY))
                .andExpect(status().isOk());

        mvc.perform(
                        post("/operator/overrides/OV-1/revoke")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(NON_OWNER_REVOKE_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("ORG_NOT_OWNER"));
    }
}
