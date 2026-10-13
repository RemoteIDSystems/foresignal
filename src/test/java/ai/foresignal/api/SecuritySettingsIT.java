package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code GET/PUT /v1/settings/security} (prompt 023). */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class SecuritySettingsIT {

    @Autowired private MockMvc mvc;
    @Autowired private AdminTokenService adminTokens;
    @Autowired private AppSettingService settings;
    @Autowired private JsonMapper json;

    private String admin;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        admin = "Bearer " + adminTokens.rotate();
    }

    /** Other tests in this cached context expect the shipped openReads. */
    @AfterEach
    void restore() {
        settings.put(ai.foresignal.settings.SettingKeys.SECURITY, ai.foresignal.settings.SecuritySettings.defaults());
    }

    private MockHttpServletResponse putSecurity(String body) throws Exception {
        return mvc.perform(put("/v1/settings/security").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn().getResponse();
    }

    @Test
    @DisplayName("defaults: openReads on, exposeObserver off")
    void defaults() throws Exception {
        JsonNode body = json.readTree(mvc.perform(get("/v1/settings/security").header(HttpHeaders.AUTHORIZATION, admin))
                .andReturn().getResponse().getContentAsString());
        assertThat(body.get("openReads").asBoolean()).isTrue();
        assertThat(body.get("exposeObserver").asBoolean()).isFalse();
    }

    @Test
    @DisplayName("turning openReads off makes data reads need a token at once")
    void openReadsOff() throws Exception {
        assertThat(mvc.perform(get("/v1/records")).andReturn().getResponse().getStatus()).isEqualTo(200);

        MockHttpServletResponse response = putSecurity("{\"openReads\":false,\"exposeObserver\":true}");

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(json.readTree(response.getContentAsString()).get("exposeObserver").asBoolean()).isTrue();
        assertThat(mvc.perform(get("/v1/records")).andReturn().getResponse().getStatus()).isEqualTo(401);
        putSecurity("{\"openReads\":true}");
        assertThat(mvc.perform(get("/v1/records")).andReturn().getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("invalid bodies are 400; 035 members and 069's exposeMlat are accepted")
    void validation() throws Exception {
        assertThat(putSecurity("{}").getStatus()).isEqualTo(400);
        assertThat(putSecurity("{\"openReads\":true,\"exposeMlat\":\"yes\"}").getStatus()).isEqualTo(400);
        assertThat(putSecurity("{\"openReads\":true,\"exposeMlat\":true}").getStatus()).isEqualTo(200);
        assertThat(putSecurity("{\"openReads\":true,\"anonymous\":{\"sources\":[\"no-such-source\"]}}").getStatus())
                .as("the anonymous profile's sources must exist").isEqualTo(400);
        assertThat(putSecurity("{\"openReads\":true,\"metricsOpen\":false}").getStatus()).isEqualTo(200);
        assertThat(putSecurity("{\"openReads\":true}").getStatus()).isEqualTo(200);
    }
}
