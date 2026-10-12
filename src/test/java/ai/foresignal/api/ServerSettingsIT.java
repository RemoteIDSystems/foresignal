package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.SecureRequestMonitor;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.collect.CollectorTestSources;
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

/** {@code GET/PUT /v1/settings/server}, its warnings and the settings bundle (prompt 023). */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class ServerSettingsIT {

    @Autowired private MockMvc mvc;
    @Autowired private AdminTokenService adminTokens;
    @Autowired private AppSettingService settings;
    @Autowired private SecureRequestMonitor secureRequests;
    @Autowired private JsonMapper json;

    private String admin;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        secureRequests.reset();
        admin = "Bearer " + adminTokens.rotate();
    }

    private MockHttpServletResponse putServer(String body) throws Exception {
        return mvc.perform(put("/v1/settings/server").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn().getResponse();
    }

    private JsonNode getJson(String path) throws Exception {
        return json.readTree(mvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, admin)).andReturn().getResponse()
                .getContentAsString());
    }

    @Test
    @DisplayName("defaults: no publicBaseUrl, UTC, foresignal:// client links, no warnings")
    void defaults() throws Exception {
        JsonNode body = getJson("/v1/settings/server");
        assertThat(body.get("publicBaseUrl").isNull()).isTrue();
        assertThat(body.get("timezone").asString()).isEqualTo("UTC");
        assertThat(body.get("clientLinks").get("alert").asString()).isEqualTo("foresignal://alerts/{id}");
        assertThat(body.get("warnings")).isEmpty();
    }

    @Test
    @DisplayName("clientLinks and timezone round-trip; unset templates keep their defaults")
    void roundTrip() throws Exception {
        MockHttpServletResponse response = putServer("""
                {"publicBaseUrl":"https://fs.example","timezone":"America/Chicago",
                 "clientLinks":{"alert":"https://fs.example/app/alerts/{id}"}}
                """);

        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
        JsonNode body = getJson("/v1/settings/server");
        assertThat(body.get("publicBaseUrl").asString()).isEqualTo("https://fs.example");
        assertThat(body.get("timezone").asString()).isEqualTo("America/Chicago");
        assertThat(body.get("clientLinks").get("alert").asString()).isEqualTo("https://fs.example/app/alerts/{id}");
        assertThat(body.get("clientLinks").get("pair").asString()).isEqualTo("foresignal://pair?u={publicBaseUrl}&c={code}");
    }

    @Test
    @DisplayName("http:// on a public host warns PUBLIC_BASE_URL_INSECURE, in the response and in /v1/system")
    void insecureWarning() throws Exception {
        JsonNode body = json.readTree(putServer("{\"publicBaseUrl\":\"http://foresignal.example.com\"}").getContentAsString());

        assertThat(body.get("warnings").findValuesAsString("code")).containsExactly("PUBLIC_BASE_URL_INSECURE");
        assertThat(getJson("/v1/system").get("warnings").findValuesAsString("code")).contains("PUBLIC_BASE_URL_INSECURE");
        assertThat(json.readTree(putServer("{\"publicBaseUrl\":\"http://foresignal.local:8080\"}").getContentAsString())
                .get("warnings")).isEmpty();
    }

    @Test
    @DisplayName("requests over https with an http:// publicBaseUrl warn PUBLIC_BASE_URL_MISMATCH")
    void mismatchWarning() throws Exception {
        putServer("{\"publicBaseUrl\":\"http://192.168.1.20:8080\"}");
        mvc.perform(get("/v1/settings/server").secure(true).header(HttpHeaders.AUTHORIZATION, admin));

        assertThat(getJson("/v1/settings/server").get("warnings").findValuesAsString("code"))
                .containsExactly("PUBLIC_BASE_URL_MISMATCH");
    }

    @Test
    @DisplayName("invalid bodies are 400")
    void validation() throws Exception {
        assertThat(putServer("{}").getStatus()).isEqualTo(400);
        assertThat(putServer("{\"publicBaseUrl\":\"ftp://x\"}").getStatus()).isEqualTo(400);
        assertThat(putServer("{\"publicBaseUrl\":null,\"clientLinks\":{\"alert\":\"http://x/{id}\"}}").getStatus())
                .isEqualTo(400);
    }

    @Test
    @DisplayName("export carries security and server and never a token; import applies them")
    void exportImport() throws Exception {
        putServer("{\"publicBaseUrl\":\"https://fs.example\",\"timezone\":\"Europe/Berlin\"}");
        mvc.perform(put("/v1/settings/security").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"openReads\":false}"));

        String exported = mvc.perform(get("/v1/settings/export").header(HttpHeaders.AUTHORIZATION, admin))
                .andReturn().getResponse().getContentAsString();
        JsonNode bundle = json.readTree(exported);
        assertThat(bundle.get("security").get("openReads").asBoolean()).isFalse();
        assertThat(bundle.get("server").get("timezone").asString()).isEqualTo("Europe/Berlin");
        assertThat(exported).doesNotContain(admin.substring(7)).doesNotContain("fsr_");

        putServer("{\"publicBaseUrl\":null}");
        mvc.perform(put("/v1/settings/security").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"openReads\":true}"));
        MockHttpServletResponse imported = mvc.perform(put("/v1/settings/import").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content(exported)).andReturn().getResponse();
        assertThat(imported.getStatus()).as(imported.getContentAsString()).isEqualTo(200);

        assertThat(getJson("/v1/settings/server").get("publicBaseUrl").asString()).isEqualTo("https://fs.example");
        assertThat(getJson("/v1/settings/security").get("openReads").asBoolean()).isFalse();
        mvc.perform(put("/v1/settings/security").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"openReads\":true}"));
    }
}
