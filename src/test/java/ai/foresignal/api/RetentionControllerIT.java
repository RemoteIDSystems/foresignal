package ai.foresignal.api;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK_KIND;
import static ai.foresignal.testsupport.retention.RetentionTestSources.KINDS;
import static ai.foresignal.testsupport.retention.RetentionTestSources.LONG_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.retention.RetentionResolver;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.store.SourceSettingsRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.retention.RetentionItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import java.time.Duration;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** {@code getRetention}, {@code updateRetention} and {@code runRetention} over HTTP (prompt 019). */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RetentionTestSources.class)
class RetentionControllerIT extends RetentionItSupport {

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired SourceSettingsRepository sourceRows;
    @Autowired RetentionResolver resolver;

    String token;

    @BeforeEach
    void rotate() {
        token = adminTokens.rotate();
    }

    ResultActions send(MockHttpServletRequestBuilder request, String body) throws Exception {
        return mvc.perform(request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body.replace('\'', '"')));
    }

    @Test
    @DisplayName("GET returns the defaults, admin only")
    void defaults() throws Exception {
        mvc.perform(get("/v1/settings/retention")).andExpect(status().isUnauthorized());
        mvc.perform(get("/v1/settings/retention").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.default").value("7d"))
                .andExpect(jsonPath("$.trackWindow").value("3d"))
                .andExpect(jsonPath("$.provenance").value("30d"))
                .andExpect(jsonPath("$.notifications").value("90d"))
                .andExpect(jsonPath("$.alerts").value("90d"))
                .andExpect(jsonPath("$.usage").value("30d"))
                .andExpect(jsonPath("$.briefings").value("180d"))
                .andExpect(jsonPath("$.ask").value("90d"));
    }

    @Test
    @DisplayName("PUT replaces the object; sources round-trip through source_settings")
    void sourcesRoundTrip() throws Exception {
        send(put("/v1/settings/retention"),
                "{'default':'10d','trackWindow':'2d','kinds':{'" + LONG_KIND + "':'400d'},'sources':{'" + STANDARD + "':'45d'}}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.default").value("10d"))
                .andExpect(jsonPath("$.trackWindow").value("2d"))
                .andExpect(jsonPath("$.alerts").value("90d"))
                .andExpect(jsonPath("$.kinds." + LONG_KIND).value("400d"))
                .andExpect(jsonPath("$.sources." + STANDARD).value("45d"));

        assertThat(sourceRows.findById(STANDARD).orElseThrow().getRetention()).isEqualTo("45d");
        assertThat(resolver.window(STANDARD, null)).isEqualTo(Duration.ofDays(45));
        assertThat(resolver.window(KINDS, LONG_KIND)).isEqualTo(Duration.ofDays(400));
        mvc.perform(get("/v1/sources/" + STANDARD)).andExpect(jsonPath("$.settings.effectiveRetention").value("45d"));

        send(put("/v1/settings/retention"), "{'default':'10d'}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sources").isEmpty());
        assertThat(sourceRows.findById(STANDARD).orElseThrow().getRetention()).isNull();
    }

    @Test
    @DisplayName("every error in one 400: durations under 1h, unknown sources and kinds, a non-member")
    void validation() throws Exception {
        send(put("/v1/settings/retention"),
                "{'default':'30m','alerts':'nope','sources':{'no-such-source':'7d','" + STANDARD + "':'10m'},"
                        + "'kinds':{'no-such-kind':'7d'},'bogus':1}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[*].field", hasItem("default")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("alerts")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("sources." + STANDARD)))
                .andExpect(jsonPath("$.errors[*].field", hasItem("sources")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("kinds")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("bogus")));

        send(put("/v1/settings/retention"), "{'alerts':'7d'}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItem("default")));
    }

    @Test
    @DisplayName("a TRACK source or a kind only TRACK sources emit is 400 RETENTION_TIER_FIXED")
    void trackTierFixed() throws Exception {
        send(put("/v1/settings/retention"), "{'default':'7d','sources':{'" + TRACK + "':'30d'}}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RETENTION_TIER_FIXED"))
                .andExpect(jsonPath("$.errors[0].message", containsString("trackWindow")));
        send(put("/v1/settings/retention"), "{'default':'7d','kinds':{'" + TRACK_KIND + "':'30d'},'alerts':'5m'}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RETENTION_TIER_FIXED"))
                .andExpect(jsonPath("$.errors[*].field", hasItem("alerts")));
    }

    @Test
    @DisplayName("POST /run runs the job and returns its counts")
    void runEndpoint() throws Exception {
        mvc.perform(post("/v1/settings/retention/run").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versionsDeleted").value(0))
                .andExpect(jsonPath("$.headsDeleted").value(0))
                .andExpect(jsonPath("$.partitionsDropped").isNumber())
                .andExpect(jsonPath("$.consumerClamp.active").isBoolean())
                .andExpect(jsonPath("$.skippedSources").isArray());
    }

    @Test
    @DisplayName("POST /run while a run is active is 409 ALREADY_RUNNING")
    void runConflict() throws Exception {
        blocking.arm();
        Thread runner = new Thread(() -> job.run());
        runner.start();
        assertThat(blocking.awaitStarted()).isTrue();
        try {
            mvc.perform(post("/v1/settings/retention/run").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("ALREADY_RUNNING"));
        } finally {
            blocking.release();
            runner.join(30_000);
        }
    }
}
