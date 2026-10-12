package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.audit.AuditRecord;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.stats.StatsItSupport;
import ai.foresignal.testsupport.stats.StatsTestSources;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The operator's {@code private} tightening and private-read auditing (prompt 039): {@code updateSource}
 * with {@code private: true} hides a source from callers without the grant, stops its rollup and deletes
 * its embeddings; a descriptor-private source cannot be made public; {@code embed: true} is refused for a
 * private source; and every private-data read by a non-admin principal holding the grant is audited once
 * per request (admin reads and callers without the grant are not).
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(StatsTestSources.class)
class SourcePrivacyIT extends StatsItSupport {

    private static final String POINTS = StatsTestSources.POINTS;
    private static final String SECRET = StatsTestSources.SECRET;

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired SourceVisibility visibility;
    @Autowired AuditService audit;

    String admin;
    String reader;
    String granted;

    @BeforeEach
    void setUp() {
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
        admin = TestTokens.bearer(TestTokens.create(tokens, "privacy-admin", Scope.ADMIN));
        reader = TestTokens.bearer(TestTokens.create(tokens, "privacy-reader", Scope.READ));
        granted = TestTokens.bearer(TestTokens.create(tokens, "privacy-granted", Scope.READ, true));
    }

    @AfterEach
    void restore() {
        visibility.setOperatorPrivate(POINTS, false);
        visibility.refresh();
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
    }

    private void update(String sourceId, String body, int expected) throws Exception {
        mvc.perform(put("/v1/sources/" + sourceId).header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is(expected));
    }

    @Test
    @DisplayName("private: true hides the source from anonymous lists, shows private in settings, stops the rollup")
    void tighten() throws Exception {
        assertThat(mvc.perform(get("/v1/sources")).andReturn().getResponse().getContentAsString()).contains(POINTS);
        assertThat(statsSettings.rolledUpSources()).contains(POINTS);

        update(POINTS, "{\"private\": true}", 200);

        assertThat(visibility.isPrivate(POINTS)).isTrue();
        assertThat(mvc.perform(get("/v1/sources")).andReturn().getResponse().getContentAsString())
                .doesNotContain(POINTS);
        mvc.perform(get("/v1/sources/" + POINTS)).andExpect(status().isNotFound());
        mvc.perform(get("/v1/sources/" + POINTS).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settings.private").value(true))
                .andExpect(jsonPath("$.settings.embed").value(false));
        assertThat(statsSettings.rolledUpSources()).doesNotContain(POINTS);

        update(POINTS, "{\"private\": false}", 200);
        assertThat(visibility.isPrivate(POINTS)).isFalse();
        assertThat(mvc.perform(get("/v1/sources")).andReturn().getResponse().getContentAsString()).contains(POINTS);
    }

    @Test
    @DisplayName("a descriptor-private source cannot be made public; embed: true is refused for a private source")
    void refusals() throws Exception {
        update(SECRET, "{\"private\": false}", 400);
        update(SECRET, "{\"embed\": true}", 400);
        update(POINTS, "{\"private\": true, \"embed\": true}", 400);
        assertThat(visibility.isPrivate(POINTS)).as("nothing applied on a 400").isFalse();
        mvc.perform(get("/v1/sources/" + SECRET).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(jsonPath("$.settings.private").value(true));
    }

    @Test
    @DisplayName("a source that becomes private loses its embedding rows")
    void embeddingsDeleted() throws Exception {
        Instant now = clock.instant();
        jdbc.update("INSERT INTO embedding_document (doc_type, doc_id, source_id, text, text_hash, updated_at)"
                + " VALUES ('record', ?, ?, 'Name e1', 1, ?)", POINTS + ":e1", POINTS, java.sql.Timestamp.from(now));
        jdbc.update("INSERT INTO embedding_document (doc_type, doc_id, source_id, text, text_hash, updated_at)"
                + " VALUES ('alert', '991', ?, 'Alert text', 2, ?)", POINTS, java.sql.Timestamp.from(now));

        update(POINTS, "{\"private\": true}", 200);

        assertThat(jdbc.queryForObject("SELECT count(*) FROM embedding_document WHERE source_id = ?", Long.class,
                POINTS)).isZero();
    }

    private long privateReads(String tokenName) {
        audit.flush();
        return jdbc.queryForObject("SELECT count(*) FROM audit_event WHERE target_type = ? AND token_name = ?",
                Long.class, AuditRecord.TARGET_PRIVATE_READ, tokenName);
    }

    @Test
    @DisplayName("a read by a non-admin token with the grant is audited once per request; admin and plain reads are not")
    void privateReadAudit() throws Exception {
        seed(SECRET, T0.plusSeconds(60), point("s1", StatsTestSources.SECRET_KIND, 10, 50, T0.plusSeconds(60), 1));
        long before = privateReads("privacy-granted");

        mvc.perform(get("/v1/records").header(HttpHeaders.AUTHORIZATION, granted)).andExpect(status().isOk());
        assertThat(privateReads("privacy-granted")).isEqualTo(before + 1);
        mvc.perform(get("/v1/records").header(HttpHeaders.AUTHORIZATION, granted)).andExpect(status().isOk());
        assertThat(privateReads("privacy-granted")).isEqualTo(before + 2);

        mvc.perform(get("/v1/records").header(HttpHeaders.AUTHORIZATION, admin)).andExpect(status().isOk());
        mvc.perform(get("/v1/records").header(HttpHeaders.AUTHORIZATION, reader)).andExpect(status().isOk());
        assertThat(privateReads("privacy-admin")).isZero();
        assertThat(privateReads("privacy-reader")).isZero();
    }
}
