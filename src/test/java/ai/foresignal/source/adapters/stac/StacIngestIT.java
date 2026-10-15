package ai.foresignal.source.adapters.stac;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import ai.foresignal.trigger.CooldownScope;
import ai.foresignal.trigger.MatchRow;
import ai.foresignal.trigger.OperatorRegistry;
import ai.foresignal.trigger.RuleCondition;
import ai.foresignal.trigger.RuleSpec;
import ai.foresignal.trigger.RuleSqlBuilder;
import ai.foresignal.trigger.Severity;
import ai.foresignal.store.TriggerMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * {@code stac-sentinel2} end to end through the real fetch path and ingest writer (prompt 096): a first
 * run ingests the scenes over the AOI, an identical re-fetch writes no version, a reprocessed item with a
 * different cloud cover is a SIGNIFICANT version, and an {@code only_new} rule on the AOI coverage matches
 * only first versions.
 */
class StacIngestIT extends WeatherIngestSupport {

    private static final String ID = StacSources.SENTINEL2;
    private static final String PATH = "/search";
    private static final String FIRST = "sentinel-2-c1-l2a:S2C_T14RPU_20261010T171726_L2A";

    /** A small AOI inside both Sentinel-2 footprints, so the coverage is 100 %. */
    private static final Map<String, Object> PARAMS = Map.of(
            EarthSearchAdapter.AOI_PARAM, "-97.7,30.2,-97.5,30.4", EarthSearchAdapter.LOOKBACK_DAYS_PARAM, 10);

    @Autowired private NamedParameterJdbcTemplate named;

    @AfterEach
    void disable() {
        // stac-sentinel2's AOI parameter is required, so clearing it would be rejected; just disable.
        sources.update(ID, SourceSettingsUpdate.enabled(false));
    }

    private static void serve(String body) {
        UPSTREAM.stubFor(post(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/geo+json").withBody(body)));
    }

    /** The {@code only_new} rule on {@code aoiCoverPct >= 80}, evaluated against every version written. */
    private List<MatchRow> cloudFreeOnlyNew(Instant evalTime) {
        RuleCondition condition = new RuleCondition("aoiCoverPct",
                OperatorRegistry.builtIn().find(">=").orElseThrow(), 80);
        RuleSpec rule = new RuleSpec(UUID.fromString("00000000-0000-0000-0000-000000000096"),
                "cloud-free scene over home", true, Set.of(ID), Set.of(EarthSearchAdapter.KIND), List.of(),
                List.of(condition), null, null, null, 0, TriggerMode.MATCH, true, false, Duration.ZERO,
                CooldownScope.RECORD, Severity.WARNING, null, null, false, null, null, null, null, null, null, false);
        Long to = jdbc.queryForObject("select max(seq) from record_version", Long.class);
        RuleSqlBuilder.RuleSql sql = RuleSqlBuilder.matches(rule, 0, to == null ? 0 : to, evalTime, false);
        return named.query(sql.sql(), sql.params(), MatchRow.MAPPER);
    }

    @Test
    @DisplayName("first run ingests the scenes; an identical re-fetch writes none; a reprocess is version 2")
    void ingest() {
        clock.setInstant(Instant.parse("2026-10-13T12:00:00Z"));
        String fixture = AdapterFixtures.text("stac-sentinel2", "search.json");
        serve(fixture);
        configure(ID, true, PARAMS);

        FetchOutcome first = fetch(ID);
        assertThat(first.success()).isTrue();
        assertThat(versions(ID, (PersistReason) null)).isEqualTo(2);
        assertThat(versions(ID, FIRST)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select persist_reason from record_version where source_id = ? and"
                + " external_id = ? order by seq desc limit 1", Integer.class, ID, FIRST))
                .isEqualTo(PersistReason.FIRST.code());
        assertThat(jdbc.queryForObject("select payload->>'aoiCoverPct' from record_head where source_id = ? and"
                + " external_id = ?", String.class, ID, FIRST)).isEqualTo("100");

        assertThat(cloudFreeOnlyNew(clock.instant())).as("only_new fires once per scene").hasSize(2);

        FetchOutcome same = fetch(ID);
        assertThat(same.success()).isTrue();
        assertThat(same.persisted()).isZero();
        assertThat(versions(ID, (PersistReason) null)).isEqualTo(2);

        serve(fixture.replace("0.00105", "12.5"));
        FetchOutcome reprocessed = fetch(ID);
        assertThat(reprocessed.success()).isTrue();
        assertThat(versions(ID, FIRST)).isEqualTo(2);
        assertThat(jdbc.queryForObject("select persist_reason from record_version where source_id = ? and"
                + " external_id = ? order by seq desc limit 1", Integer.class, ID, FIRST))
                .isEqualTo(PersistReason.SIGNIFICANT.code());

        List<MatchRow> afterReprocess = cloudFreeOnlyNew(clock.instant());
        assertThat(afterReprocess).as("only_new matches first versions only").hasSize(2);
        assertThat(afterReprocess).allSatisfy(row -> assertThat(row.version()).isEqualTo(1));
    }
}
