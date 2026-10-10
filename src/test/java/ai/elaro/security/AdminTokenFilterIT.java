package ai.elaro.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.elaro.settings.AppSettingService;
import ai.elaro.settings.SecuritySettings;
import ai.elaro.settings.SettingKeys;
import ai.elaro.test.IntegrationTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The token filter against real endpoints, through the real filter chain.
 *
 * <p>The stub controllers sit on the actual {@code /v1} paths the policy knows about, so this
 * exercises the wiring (filter order, problem rendering, header handling) rather than re-testing the
 * policy table, which {@link AccessPolicyTest} covers exhaustively.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(AdminTokenFilterIT.StubEndpoints.class)
class AdminTokenFilterIT {

    @Autowired private MockMvc mvc;
    @Autowired private AdminTokenService adminTokens;
    @Autowired private AppSettingService settings;

    private String token;

    @BeforeEach
    void mintAToken() {
        settings.invalidateCache();
        // The table was truncated, so re-establish a token and keep the plaintext.
        token = adminTokens.rotate();
    }

    private String bearer() {
        return "Bearer " + token;
    }

    // ---------- rejection ----------

    @Test
    @DisplayName("a write without a token is 401 problem+json with code UNAUTHORIZED")
    void writeWithoutTokenIsRejected() throws Exception {
        mvc.perform(put("/v1/sources/usgs-earthquakes"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"elaro\""))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.type").value("https://elaro.ai/problems/unauthorized"));
    }

    @Test
    @DisplayName("an admin GET without a token is 401")
    void adminGetWithoutTokenIsRejected() throws Exception {
        mvc.perform(get("/v1/settings/retention")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("an invalid token is 401 and the response never echoes it")
    void invalidTokenIsRejectedWithoutEchoing() throws Exception {
        mvc.perform(get("/v1/settings/retention").header(HttpHeaders.AUTHORIZATION, "Bearer elr_wrong-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(content().string(Matchers.not(Matchers.containsString("elr_wrong-token"))));
    }

    @Test
    @DisplayName("a malformed Authorization header is treated as absent")
    void malformedAuthorizationHeaderIsRejected() throws Exception {
        mvc.perform(get("/v1/settings/retention").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/v1/settings/retention").header(HttpHeaders.AUTHORIZATION, "Basic " + token))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/v1/settings/retention").header(HttpHeaders.AUTHORIZATION, "Bearer "))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("the correct token on the wrong scheme is still rejected")
    void rejectsCorrectTokenWithWrongScheme() throws Exception {
        mvc.perform(get("/v1/settings/retention").header(HttpHeaders.AUTHORIZATION, "bearer-" + token))
                .andExpect(status().isUnauthorized());
    }

    // ---------- acceptance ----------

    @Test
    @DisplayName("a write with the token succeeds")
    void writeWithTokenSucceeds() throws Exception {
        mvc.perform(put("/v1/sources/usgs-earthquakes").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk())
                .andExpect(content().string("updated"));
    }

    @Test
    @DisplayName("an admin GET with the token succeeds")
    void adminGetWithTokenSucceeds() throws Exception {
        mvc.perform(get("/v1/settings/retention").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk())
                .andExpect(content().string("retention"));
    }

    @Test
    @DisplayName("agent-scope writes accept the bootstrap admin token")
    void agentWriteAcceptsAdminToken() throws Exception {
        mvc.perform(post("/v1/triggers").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("a rotated token works immediately and the previous one stops")
    void rotationTakesEffectImmediately() throws Exception {
        String previous = token;
        String rotated = adminTokens.rotate();

        mvc.perform(get("/v1/settings/retention").header(HttpHeaders.AUTHORIZATION, "Bearer " + rotated))
                .andExpect(status().isOk());
        mvc.perform(get("/v1/settings/retention").header(HttpHeaders.AUTHORIZATION, "Bearer " + previous))
                .andExpect(status().isUnauthorized());
    }

    // ---------- open endpoints ----------

    @Test
    @DisplayName("data GETs are open while openReads is on")
    void dataGetsAreOpenByDefault() throws Exception {
        mvc.perform(get("/v1/records")).andExpect(status().isOk()).andExpect(content().string("records"));
        mvc.perform(get("/v1/kinds")).andExpect(status().isOk());
        mvc.perform(get("/v1/sources")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /v1/system is open")
    void systemIsOpen() throws Exception {
        mvc.perform(get("/v1/system")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("the authored spec and the actuator health endpoint are open")
    void specAndHealthAreOpen() throws Exception {
        mvc.perform(get("/v1/openapi.json")).andExpect(status().isOk());
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("prometheus is open, deliberately, for LAN scraping")
    void prometheusIsOpen() throws Exception {
        mvc.perform(get("/actuator/prometheus")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("a data GET still works when a valid token is supplied")
    void dataGetAcceptsATokenToo() throws Exception {
        mvc.perform(get("/v1/records").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("an invalid token on an open-read endpoint is rejected rather than ignored")
    void invalidTokenOnOpenReadIsRejected() throws Exception {
        // Silently ignoring a bad token would hide a client's misconfiguration.
        mvc.perform(get("/v1/records").header(HttpHeaders.AUTHORIZATION, "Bearer elr_wrong"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- openReads = false ----------

    @Test
    @DisplayName("turning openReads off closes data GETs")
    void closedReadsRequireAToken() throws Exception {
        settings.put(SettingKeys.SECURITY, new SecuritySettings(false));

        mvc.perform(get("/v1/records")).andExpect(status().isUnauthorized());
        mvc.perform(get("/v1/records").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("openReads off does not close the always-open endpoints")
    void closedReadsLeaveOpenEndpointsAlone() throws Exception {
        settings.put(SettingKeys.SECURITY, new SecuritySettings(false));

        mvc.perform(get("/v1/system")).andExpect(status().isOk());
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("turning openReads back on reopens data GETs without a restart")
    void reopeningReadsTakesEffect() throws Exception {
        settings.put(SettingKeys.SECURITY, new SecuritySettings(false));
        mvc.perform(get("/v1/records")).andExpect(status().isUnauthorized());

        settings.put(SettingKeys.SECURITY, new SecuritySettings(true));

        mvc.perform(get("/v1/records")).andExpect(status().isOk());
    }

    // ---------- ingest ----------

    @Test
    @DisplayName("ingest without a credential is 401 until ingest keys arrive")
    void ingestRequiresACredential() throws Exception {
        mvc.perform(post("/v1/ingest/custom-sdr"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value(Matchers.containsString("ingest key")));
    }

    @Test
    @DisplayName("ingest accepts an admin token")
    void ingestAcceptsAdminToken() throws Exception {
        mvc.perform(post("/v1/ingest/custom-sdr").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk());
    }

    // ---------- deny by default ----------

    @Test
    @DisplayName("an endpoint with no policy rule requires a token")
    void unknownV1PathsAreClosed() throws Exception {
        mvc.perform(delete("/v1/records/usgs-earthquakes/abc")).andExpect(status().isUnauthorized());
    }

    /** Stub endpoints on the real policy paths. */
    @TestConfiguration
    @RestController
    static class StubEndpoints {

        @GetMapping("/v1/records")
        String records() {
            return "records";
        }

        @GetMapping("/v1/kinds")
        String kinds() {
            return "kinds";
        }

        @GetMapping("/v1/sources")
        String sources() {
            return "sources";
        }

        @PutMapping("/v1/sources/{sourceId}")
        String updateSource() {
            return "updated";
        }

        @GetMapping("/v1/settings/retention")
        String retention() {
            return "retention";
        }

        @PostMapping("/v1/triggers")
        String createTrigger() {
            return "created";
        }

        @GetMapping("/v1/system")
        String system() {
            return "system";
        }

        @PostMapping("/v1/ingest/{sourceId}")
        String ingest() {
            return "ingested";
        }

        @DeleteMapping("/v1/records/{sourceId}/{externalId}")
        String deleteRecord() {
            return "deleted";
        }
    }
}
