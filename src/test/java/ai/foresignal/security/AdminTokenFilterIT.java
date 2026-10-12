package ai.foresignal.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.IntegrationTest;
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
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"foresignal\""))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.type").value("https://foresignal.ai/problems/unauthorized"));
    }

    @Test
    @DisplayName("an admin GET without a token is 401")
    void adminGetWithoutTokenIsRejected() throws Exception {
        mvc.perform(get("/v1/settings/retention")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("an invalid token is 401 and the response never echoes it")
    void invalidTokenIsRejectedWithoutEchoing() throws Exception {
        mvc.perform(get("/v1/settings/retention").header(HttpHeaders.AUTHORIZATION, "Bearer fsr_wrong-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(content().string(Matchers.not(Matchers.containsString("fsr_wrong-token"))));
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
        // The real SourcesController (013) answers now; an empty body changes nothing.
        mvc.perform(put("/v1/sources/usgs-earthquakes")
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descriptor.id").value("usgs-earthquakes"));
    }

    @Test
    @DisplayName("an admin GET with the token succeeds")
    void adminGetWithTokenSucceeds() throws Exception {
        mvc.perform(get("/v1/settings/retention").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.default").isString());
    }

    @Test
    @DisplayName("agent-scope writes accept the bootstrap admin token")
    void agentWriteAcceptsAdminToken() throws Exception {
        // The real createTrigger (017) answers an empty body with 400: the token passed the filter.
        mvc.perform(post("/v1/triggers").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isBadRequest());
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
        mvc.perform(get("/v1/records")).andExpect(status().isOk()).andExpect(jsonPath("$.items").isArray());
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
        mvc.perform(get("/v1/records").header(HttpHeaders.AUTHORIZATION, "Bearer fsr_wrong"))
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
    @DisplayName("ingest accepts an admin token: the request reaches the real controller (404 for an unknown source)")
    void ingestAcceptsAdminToken() throws Exception {
        // 024's IngestController serves this path, so the stub is gone (checkpoint 041): past the filter, an
        // unknown source is the controller's 404, not the filter's 401 or 403.
        mvc.perform(post("/v1/ingest/custom-sdr").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isNotFound());
    }

    // ---------- deny by default ----------

    @Test
    @DisplayName("an endpoint with no policy rule requires a token")
    void unknownV1PathsAreClosed() throws Exception {
        mvc.perform(delete("/v1/records/usgs-earthquakes/abc")).andExpect(status().isUnauthorized());
    }

    /**
     * Stub endpoints on policy paths no real controller serves yet. Prompt 022 removed the stubs for
     * {@code /v1/records}, {@code /v1/kinds}, {@code /v1/settings/retention}, {@code POST /v1/triggers}
     * and {@code /v1/system}: their real controllers (014, 017, 019) made the mappings ambiguous. Checkpoint 041
     * removed the {@code POST /v1/ingest/{sourceId}} stub for the same reason (024's IngestController).
     */
    @TestConfiguration
    @RestController
    static class StubEndpoints {

        @DeleteMapping("/v1/records/{sourceId}/{externalId}")
        String deleteRecord() {
            return "deleted";
        }
    }
}
