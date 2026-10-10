package ai.elaro.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.elaro.settings.AppSettingService;
import ai.elaro.settings.CorsSettings;
import ai.elaro.settings.CorsSettingsService;
import ai.elaro.test.IntegrationTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@IntegrationTest
@AutoConfigureMockMvc
@Import(CorsFilterIT.CorsStubEndpoints.class)
class CorsFilterIT {

    private static final String ORIGIN = "http://localhost:5173";
    private static final String OTHER_ORIGIN = "https://somewhere-else.example";

    @Autowired private MockMvc mvc;
    @Autowired private AppSettingService settings;
    @Autowired private CorsSettingsService corsSettings;

    @BeforeEach
    void resetSettings() {
        settings.invalidateCache();
    }

    // ---------- reads ----------

    @Test
    @DisplayName("a read preflight is allowed from any origin by default")
    void readPreflightAllowedByWildcard() throws Exception {
        mvc.perform(
                        options("/v1/records")
                                .header(HttpHeaders.ORIGIN, ORIGIN)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "600"));
    }

    @Test
    @DisplayName("the preflight advertises the headers Elaro's clients need")
    void preflightAdvertisesHeaders() throws Exception {
        mvc.perform(
                        options("/v1/records")
                                .header(HttpHeaders.ORIGIN, ORIGIN)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(
                        header().string(
                                        HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                                        "Authorization, Content-Type, Accept, Last-Event-ID"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Location"));
    }

    @Test
    @DisplayName("an actual read carries the allow-origin header")
    void actualReadIsAllowed() throws Exception {
        mvc.perform(get("/v1/records").header(HttpHeaders.ORIGIN, ORIGIN))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN));
    }

    @Test
    @DisplayName("the response echoes the origin and varies on it, rather than returning *")
    void echoesOriginAndVaries() throws Exception {
        mvc.perform(get("/v1/records").header(HttpHeaders.ORIGIN, ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN))
                .andExpect(header().stringValues(HttpHeaders.VARY, org.hamcrest.Matchers.hasItem("Origin")));
    }

    @Test
    @DisplayName("a same-origin request gets no CORS headers at all")
    void noOriginNoCorsHeaders() throws Exception {
        mvc.perform(get("/v1/records"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    @DisplayName("restricting read origins turns away everyone else")
    void restrictedReadOriginsExcludeOthers() throws Exception {
        corsSettings.update(new CorsSettings(List.of(ORIGIN), List.of()));

        mvc.perform(get("/v1/records").header(HttpHeaders.ORIGIN, ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN));
        mvc.perform(get("/v1/records").header(HttpHeaders.ORIGIN, OTHER_ORIGIN))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    // ---------- writes ----------

    @Test
    @DisplayName("a write preflight is refused by default, because writeOrigins is empty")
    void writePreflightRefusedByDefault() throws Exception {
        mvc.perform(
                        options("/v1/records")
                                .header(HttpHeaders.ORIGIN, ORIGIN)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    @DisplayName("a write preflight is allowed once the origin is added to writeOrigins")
    void writePreflightAllowedAfterUpdate() throws Exception {
        corsSettings.update(new CorsSettings(List.of("*"), List.of(ORIGIN)));

        mvc.perform(
                        options("/v1/records")
                                .header(HttpHeaders.ORIGIN, ORIGIN)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN));
    }

    @Test
    @DisplayName("a wildcard read list does not permit cross-origin writes")
    void wildcardReadsDoNotGrantWrites() throws Exception {
        corsSettings.update(new CorsSettings(List.of("*"), List.of()));

        mvc.perform(
                        options("/v1/records")
                                .header(HttpHeaders.ORIGIN, OTHER_ORIGIN)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "DELETE"))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    @DisplayName("an origin allowed for writes is not allowed for a different one")
    void writeOriginsAreExact() throws Exception {
        corsSettings.update(new CorsSettings(List.of("*"), List.of(ORIGIN)));

        mvc.perform(
                        options("/v1/records")
                                .header(HttpHeaders.ORIGIN, OTHER_ORIGIN)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    @DisplayName("the preflight is judged by the method the browser intends to use")
    void preflightUsesRequestedMethodNotOptions() throws Exception {
        corsSettings.update(new CorsSettings(List.of("*"), List.of()));

        // An OPTIONS preflight for a POST must be judged against writeOrigins, not readOrigins.
        mvc.perform(
                        options("/v1/records")
                                .header(HttpHeaders.ORIGIN, ORIGIN)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        mvc.perform(
                        options("/v1/records")
                                .header(HttpHeaders.ORIGIN, ORIGIN)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN));
    }

    // ---------- interaction with the token filter ----------

    @Test
    @DisplayName("a preflight is answered without a token, so the browser can see the real reply")
    void preflightDoesNotNeedAToken() throws Exception {
        corsSettings.update(new CorsSettings(List.of("*"), List.of(ORIGIN)));

        mvc.perform(
                        options("/v1/settings/retention")
                                .header(HttpHeaders.ORIGIN, ORIGIN)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PUT"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN));
    }

    @Test
    @DisplayName("a rejected cross-origin request still carries CORS headers, so the 401 is visible")
    void rejectedRequestStillGetsCorsHeaders() throws Exception {
        mvc.perform(get("/v1/settings/retention").header(HttpHeaders.ORIGIN, ORIGIN))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN));
    }

    // ---------- live updates ----------

    @Test
    @DisplayName("a settings change takes effect without a restart")
    void settingsChangeIsPickedUp() throws Exception {
        mvc.perform(
                        options("/v1/records")
                                .header(HttpHeaders.ORIGIN, ORIGIN)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));

        corsSettings.update(new CorsSettings(List.of("*"), List.of(ORIGIN)));

        mvc.perform(
                        options("/v1/records")
                                .header(HttpHeaders.ORIGIN, ORIGIN)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN));
    }

    @TestConfiguration
    @RestController
    static class CorsStubEndpoints {

        @GetMapping("/v1/records")
        String records() {
            return "records";
        }

        @PostMapping("/v1/records")
        String createRecord() {
            return "created";
        }

        @GetMapping("/v1/settings/retention")
        String retention() {
            return "retention";
        }
    }
}
