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
 * 切片23a：发布套餐模板 HTTP（AC-24 · T-DRAFT-1 / ORG-L1）。
 *
 * <p>{@link DirtiesContext}：发布写 PUBLISHED + AuditLog，按方法刷新上下文。
 */
@SpringBootTest
@AutoConfigureMockMvc
class PublishPackageTemplateHttpIT {

    private static final String L1_BODY =
            "{\"actorUserId\":\"U-ADMIN\",\"actorOrgId\":\"ORG-L1\"}";
    private static final String MERCHANT_BODY =
            "{\"actorUserId\":\"U-M\",\"actorOrgId\":\"ORG-NEW\"}";

    @Autowired private MockMvc mvc;

    @Test
    @DisplayName("GET /operator/orgs/ORG-L1 → ACTIVE；未知 404")
    void getOrganization() throws Exception {
        mvc.perform(get("/operator/orgs/ORG-L1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ORG-L1"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.operatorCapability").value(true));

        mvc.perform(get("/operator/orgs/NO-SUCH")).andExpect(status().isNotFound());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("POST /operator/templates/T-DRAFT-1/publish → 200 PUBLISHED v1")
    void publishDraftOk() throws Exception {
        mvc.perform(
                        post("/operator/templates/T-DRAFT-1/publish")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(L1_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("T-DRAFT-1"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.publishedAt").isNotEmpty());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("ORG-NEW（无 OPERATOR）发布 T-DRAFT-M → 403 CAPABILITY_DENIED")
    void merchantOrgForbidden() throws Exception {
        mvc.perform(
                        post("/operator/templates/T-DRAFT-M/publish")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(MERCHANT_BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("CAPABILITY_DENIED"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("POST T-DRAFT-1/base-product → 200 DRAFT（replaceAllowed）")
    void mutateDraftBaseProductOk() throws Exception {
        mvc.perform(
                        post("/operator/templates/T-DRAFT-1/base-product")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"actorOrgId\":\"ORG-L1\",\"displayName\":\"草稿改价\",\"priceCents\":1999,\"durationDays\":30}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("T-DRAFT-1"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("POST T-PUB-1/base-product → 422 TEMPLATE_IMMUTABLE")
    void mutatePublishedRejected() throws Exception {
        mvc.perform(
                        post("/operator/templates/T-PUB-1/base-product")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"actorOrgId\":\"ORG-L1\",\"displayName\":\"不可改\",\"priceCents\":1,\"durationDays\":1}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("TEMPLATE_IMMUTABLE"))
                .andExpect(jsonPath("$.suggestion").exists());
    }
}
