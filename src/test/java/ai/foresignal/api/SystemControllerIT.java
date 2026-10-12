package ai.foresignal.api;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.system.HistoryStats;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.retention.RetentionItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

/** {@code GET /v1/system}: the anonymous subset, the full document, history and warnings (prompt 019). */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RetentionTestSources.class)
class SystemControllerIT extends RetentionItSupport {

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired HistoryStats history;
    @Autowired ai.foresignal.system.OperatorGauges gauges;
    @Autowired io.micrometer.core.instrument.MeterRegistry meters;

    String token;

    @BeforeEach
    void rotate() {
        token = adminTokens.rotate();
        history.invalidate();
    }

    @Test
    @DisplayName("anonymous callers get only version, apiVersion, startedAt, uptimeSeconds, status, features and limits")
    void anonymousSubset() throws Exception {
        mvc.perform(get("/v1/system"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").isString())
                .andExpect(jsonPath("$.apiVersion").value(not("unknown")))
                .andExpect(jsonPath("$.startedAt").isString())
                .andExpect(jsonPath("$.uptimeSeconds").isNumber())
                .andExpect(jsonPath("$.status", in(List.of("ok", "degraded", "down"))))
                .andExpect(jsonPath("$.features.notifications").value(true))
                .andExpect(jsonPath("$.features.retention").value(true))
                .andExpect(jsonPath("$.limits.pageMax").value(5000))
                .andExpect(jsonPath("$.limits.sseClientBuffer").isNumber())
                .andExpect(jsonPath("$.limits.liveMaxEntities").isNumber())
                .andExpect(jsonPath("$.limits.ingestMaxBytes").value(5 * 1024 * 1024))
                .andExpect(jsonPath("$.limits.ingestMaxRecords").value(10000))
                .andExpect(jsonPath("$.limits.tileMaxFeatures").isNumber())
                .andExpect(jsonPath("$.database").doesNotExist())
                .andExpect(jsonPath("$.history").doesNotExist())
                .andExpect(jsonPath("$.warnings").doesNotExist());
    }

    @Test
    @DisplayName("an invalid token is treated as anonymous, not rejected")
    void invalidTokenIsAnonymous() throws Exception {
        mvc.perform(get("/v1/system").header(HttpHeaders.AUTHORIZATION, "Bearer fsr_not-a-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.database").doesNotExist());
    }

    @Test
    @DisplayName("a valid token gets the full document with history per tier")
    void fullDocument() throws Exception {
        mvc.perform(get("/v1/system").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.database.status").value("up"))
                .andExpect(jsonPath("$.database.postgisVersion").isString())
                .andExpect(jsonPath("$.database.sizeBytes").isNumber())
                .andExpect(jsonPath("$.records.current").isNumber())
                .andExpect(jsonPath("$.records.lastSeq").isNumber())
                .andExpect(jsonPath("$.sources.total").isNumber())
                .andExpect(jsonPath("$.scheduler.ingestQueueDepth").isNumber())
                .andExpect(jsonPath("$.ingestWriter.state").value("RUNNING"))
                .andExpect(jsonPath("$.streams.clients").isNumber())
                .andExpect(jsonPath("$.notifications.channels").isNumber())
                .andExpect(jsonPath("$.history.tiers", hasSize(2)))
                .andExpect(jsonPath("$.history.tiers[*].tier", hasItem("TRACK")))
                .andExpect(jsonPath("$.history.tiers[*].tier", hasItem("STANDARD")))
                .andExpect(jsonPath("$.history.tiers[0].partitions").isNumber())
                .andExpect(jsonPath("$.history.tiers[0].windowDays").isNumber())
                .andExpect(jsonPath("$.history.versionsPerMinute").isNumber())
                .andExpect(jsonPath("$.catchUp.catchingUp").isBoolean())
                .andExpect(jsonPath("$.warnings").isArray());
    }

    @Test
    @DisplayName("the operator gauges are registered and refreshed from cached queries")
    void gauges() {
        gauges.refresh();
        org.assertj.core.api.Assertions.assertThat(meters.find("foresignal.records.current").gauge()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(meters.find("foresignal.alerts.active").tag("severity", "critical").gauge())
                .isNotNull();
        org.assertj.core.api.Assertions.assertThat(meters.find("foresignal.sse.clients").tag("stream", "changes").gauge())
                .isNotNull();
        org.assertj.core.api.Assertions.assertThat(meters.find("foresignal.history.rows").tag("tier", "track").gauge())
                .isNotNull();
        org.assertj.core.api.Assertions.assertThat(
                meters.find("foresignal.source.data_age_seconds").tag("source", "test-standard").gauge()).isNotNull();
    }

    @Test
    @DisplayName("OPEN_READS_EXPOSED appears with open reads and a public base URL, and goes with openReads off")
    void openReadsExposed() throws Exception {
        settings.put(SettingKeys.SERVER, new ServerSettings("https://foresignal.example.com"));
        mvc.perform(get("/v1/system").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(jsonPath("$.warnings[*].code", hasItem("OPEN_READS_EXPOSED")))
                .andExpect(jsonPath("$.status").value("degraded"));

        settings.put(SettingKeys.SECURITY, new SecuritySettings(false));
        mvc.perform(get("/v1/system").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(jsonPath("$.warnings[*].code", not(hasItem("OPEN_READS_EXPOSED"))));
    }
}
