package ai.foresignal.api;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.retention.RetentionItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** {@code getCors} and {@code updateCors} (prompt 019). */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RetentionTestSources.class)
class CorsControllerIT extends RetentionItSupport {

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;

    String token;

    @BeforeEach
    void rotate() {
        token = adminTokens.rotate();
    }

    ResultActions putCors(String body) throws Exception {
        return mvc.perform(put("/v1/settings/cors").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body.replace('\'', '"')));
    }

    @Test
    @DisplayName("defaults: reads open to every origin, writes closed")
    void defaults() throws Exception {
        mvc.perform(get("/v1/settings/cors").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readOrigins[0]").value("*"))
                .andExpect(jsonPath("$.writeOrigins").isEmpty());
        mvc.perform(get("/v1/settings/cors")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT stores valid origins and returns them")
    void update() throws Exception {
        putCors("{'readOrigins':['http://localhost:5173'],'writeOrigins':['http://localhost:5173']}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readOrigins[0]").value("http://localhost:5173"))
                .andExpect(jsonPath("$.writeOrigins[0]").value("http://localhost:5173"));
        mvc.perform(get("/v1/settings/cors").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(jsonPath("$.writeOrigins[0]").value("http://localhost:5173"));
    }

    @Test
    @DisplayName("a wildcard write origin and an origin with a path are 400 with both errors")
    void validation() throws Exception {
        putCors("{'readOrigins':['http://localhost:5173/app'],'writeOrigins':['*']}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[*].field", hasItem("readOrigins")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("writeOrigins")));
    }
}
