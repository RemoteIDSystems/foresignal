package ai.foresignal.retention;

import static ai.foresignal.testsupport.retention.RetentionTestSources.KINDS;
import static ai.foresignal.testsupport.retention.RetentionTestSources.LONG_KIND;
import static ai.foresignal.testsupport.retention.RetentionTestSources.SHORT_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.retention.RetentionItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code previewRetention} and the cap-history day cap end to end (prompt R2-015): the preview counts exactly what the
 * next run deletes, deletes nothing itself, and a {@code maxDays} cap shortens a longer per-kind window.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RetentionTestSources.class)
class RetentionPreviewIT extends RetentionItSupport {

    @Autowired RetentionPreviewService preview;
    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired JsonMapper json;

    /** Puts the stored settings back to the defaults. */
    @AfterEach
    void clearSettings() {
        settings.remove(SettingKeys.RETENTION);
        settings.remove(SettingKeys.RETENTION_DB_CAP);
        settings.invalidateCache();
    }

    /** The defaults with the cap switched on. */
    static RetentionSettings capped(Integer maxDays, Double maxDbGb, Boolean overwriteOldest) {
        RetentionSettings d = RetentionSettings.defaults();
        return new RetentionSettings(d.defaultRetention(), d.alerts(), d.usage(), d.sources(), d.trackWindow(),
                d.provenance(), d.notifications(), d.briefings(), d.ask(), d.kinds(), d.audit(), d.inbox(), d.stats(),
                d.trackRawWindow(), d.trackDownsampleInterval(), d.entityLinks(), true, maxDays, maxDbGb,
                overwriteOldest);
    }

    private long kindVersions(String kind) {
        return jdbc.queryForObject("SELECT count(*) FROM record_version WHERE kind = ?", Long.class, kind);
    }

    /** The preview at {@code now}, after the trigger engine has caught up (so it clamps as the run will). */
    private RetentionPreview previewAt(Instant now) {
        awaitTriggerCursor();
        clock.setInstant(now);
        return preview.preview();
    }

    private static long records(RetentionPreview p, String kind) {
        return p.items().stream().filter(i -> "records".equals(i.subsystem()) && kind.equals(i.kind()))
                .mapToLong(RetentionPreview.Item::affectedCount).sum();
    }

    @Test
    @DisplayName("the preview counts what the next run deletes, and deletes nothing itself")
    void previewMatchesTheRun() {
        ingestAt(KINDS, T0, record("ps1", SHORT_KIND, T0, 1), record("pl1", LONG_KIND, T0, 1));
        Instant marker = T0.plus(Duration.ofMinutes(1));
        ingestAt(KINDS, marker, record("ps2", SHORT_KIND, marker, 1), record("pl2", LONG_KIND, marker, 1));
        Instant now = T0.plus(Duration.ofDays(8));

        long shortBefore = kindVersions(SHORT_KIND);
        long longBefore = kindVersions(LONG_KIND);
        RetentionPreview p = previewAt(now);

        assertThat(kindVersions(SHORT_KIND)).isEqualTo(shortBefore);
        assertThat(versions(KINDS, "ps1")).isEqualTo(1);
        assertThat(records(p, SHORT_KIND)).isPositive();
        assertThat(records(p, LONG_KIND)).isZero();
        assertThat(p.totalAffected()).isEqualTo(p.items().stream().mapToLong(RetentionPreview.Item::affectedCount)
                .sum());
        assertThat(p.dbCap()).isNull();
        assertThat(p.generatedAt()).isEqualTo(now);

        runAt(now);

        assertThat(shortBefore - kindVersions(SHORT_KIND)).isEqualTo(records(p, SHORT_KIND));
        assertThat(kindVersions(LONG_KIND)).isEqualTo(longBefore);
        assertThat(versions(KINDS, "ps1")).isZero();
        assertThat(records(previewAt(now), SHORT_KIND)).isZero();
    }

    @Test
    @DisplayName("capHistory with maxDays shortens a longer per-kind window, in the preview and in the run")
    void maxDaysCapsALongerWindow() {
        ingestAt(KINDS, T0, record("cl1", LONG_KIND, T0, 1));
        Instant marker = T0.plus(Duration.ofMinutes(1));
        ingestAt(KINDS, marker, record("cl2", LONG_KIND, marker, 1));
        Instant now = T0.plus(Duration.ofDays(40));

        assertThat(records(previewAt(now), LONG_KIND)).isZero();

        settings.put(SettingKeys.RETENTION, capped(30, null, true));
        settings.invalidateCache();
        RetentionPreview p = previewAt(now);
        assertThat(records(p, LONG_KIND)).isPositive();
        assertThat(p.items()).filteredOn(i -> LONG_KIND.equals(i.kind()))
                .allSatisfy(i -> assertThat(i.cutoff()).isEqualTo(now.minus(Duration.ofDays(30))));

        long before = kindVersions(LONG_KIND);
        RetentionRunResult result = runAt(now);

        assertThat(before - kindVersions(LONG_KIND)).isEqualTo(records(p, LONG_KIND));
        assertThat(versions(KINDS, "cl1")).isZero();
        assertThat(result.capWindowDays()).isEqualTo(30);
        assertThat(result.dbCap()).isNull();
    }

    @Test
    @DisplayName("GET /v1/settings/retention/preview returns the preview to an admin token")
    void endpoint() throws Exception {
        String admin = TestTokens.create(tokens, "retention-preview-it", Scope.ADMIN).plaintext();
        awaitTriggerCursor();
        MockHttpServletResponse r = mvc.perform(get("/v1/settings/retention/preview")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)).andReturn().getResponse();

        assertThat(r.getStatus()).isEqualTo(200);
        JsonNode body = json.readTree(r.getContentAsString());
        assertThat(body.get("items").isArray()).isTrue();
        assertThat(body.get("totalAffected").isNumber()).isTrue();
        assertThat(body.get("generatedAt").isString()).isTrue();
    }
}
