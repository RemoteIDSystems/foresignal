package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.retention.RetentionItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

/** {@code rotateAdminToken} (prompt 019). */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RetentionTestSources.class)
class AdminTokenRotationIT extends RetentionItSupport {

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;

    @Test
    @DisplayName("returns a new token once; the old one stops working immediately")
    void rotates() throws Exception {
        String old = adminTokens.rotate();

        String body = mvc.perform(post("/v1/system/admin-token/rotate").header(HttpHeaders.AUTHORIZATION, "Bearer " + old))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn().getResponse().getContentAsString();
        String fresh = JsonMapper.builder().build().readTree(body).get("token").asString();

        assertThat(fresh).startsWith("fsr_").isNotEqualTo(old);
        mvc.perform(get("/v1/settings/cors").header(HttpHeaders.AUTHORIZATION, "Bearer " + old))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/v1/settings/cors").header(HttpHeaders.AUTHORIZATION, "Bearer " + fresh))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("requires the admin token")
    void requiresAdmin() throws Exception {
        mvc.perform(post("/v1/system/admin-token/rotate")).andExpect(status().isUnauthorized());
    }
}
