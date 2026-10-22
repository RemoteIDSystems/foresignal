package ai.foresignal.infra;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The infrastructure graph builder (prompt 104): a rebuild nodes the mapped sources, records its
 * inputs and counts, is deterministic, and replaces the previous build instead of adding a copy.
 */
@WebIntegrationTest
@Import(TestIngestSources.class)
class InfraGraphBuilderIT {

    @Autowired InfraGraphBuilder builder;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;

    /** A plant (WRI) with an OSM substation 2 km away and an OSM hospital 3 km beyond it. */
    static void seed(TestRecords records, MutableClock clock) {
        clock.setInstant(Instant.parse("2026-03-01T12:00:00Z"));
        Instant now = clock.instant();
        records.ingestAt("wri-power-plants", now,
                NormalizedRecord.builder("pp1", "power-plant").observedAt(now).point(-95, 32)
                        .put("name", "Plant").put("capacityMw", 500).build());
        records.ingestAt("osm-infrastructure", now,
                NormalizedRecord.builder("n10", "substation").observedAt(now).point(-94.98, 32)
                        .put("name", "Oak substation").build(),
                NormalizedRecord.builder("n11", "hospital").observedAt(now).point(-94.95, 32)
                        .put("name", "County hospital").build());
    }

    /** The edges of the current build, keyed by their endpoints' records, with params and evidence. */
    private List<String> edgeSignature() {
        return jdbc.queryForList("SELECT f.source_id || '/' || f.external_id || ' -> ' || t.source_id || '/'"
                + " || t.external_id || ' ' || e.relation || ' ' || e.method || ' ' || e.label || ' ' || e.rule_id"
                + " || ' ' || e.params::text || ' ' || e.evidence::text || ' ' || e.licence FROM infra_edge e"
                + " JOIN infra_node f ON f.id = e.from_node JOIN infra_node t ON t.id = e.to_node ORDER BY 1",
                String.class);
    }

    @Test
    @DisplayName("a rebuild nodes the mapped sources, records inputs and counts, and edges carry their license")
    void rebuild() {
        seed(records, clock);
        InfraBuildEntity build = builder.rebuild(Map.of());
        assertThat(build.status()).isEqualTo("done");
        assertThat(build.getNodes()).isEqualTo(3);
        assertThat(build.getInputs()).containsKeys("wri-power-plants", "osm-infrastructure");
        assertThat(build.getEdgesByMethod()).containsEntry("proximity", 2);
        assertThat(build.getEdgesByLabel()).containsEntry("possible", 2);
        assertThat(build.getRulesVersion()).isEqualTo(2);
        assertThat(edgeSignature()).anyMatch(e -> e.startsWith("wri-power-plants/pp1 -> osm-infrastructure/n10"
                + " supplies_power proximity possible plant-substation") && e.endsWith("ODbL-1.0"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM infra_edge WHERE params::text = '{}'", Long.class))
                .isZero();
    }

    @Test
    @DisplayName("a second rebuild is identical and replaces the first instead of adding a copy")
    void deterministicAndReplacing() {
        seed(records, clock);
        InfraBuildEntity first = builder.rebuild(Map.of());
        List<String> before = edgeSignature();
        InfraBuildEntity second = builder.rebuild(Map.of());
        assertThat(second.getId()).isGreaterThan(first.getId());
        assertThat(edgeSignature()).isEqualTo(before);
        assertThat(jdbc.queryForObject("SELECT count(DISTINCT build_id) FROM infra_node", Long.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM infra_node", Long.class)).isEqualTo(3);
        assertThat(builder.current()).get().extracting(InfraBuildEntity::getId).isEqualTo(second.getId());
    }

    @Test
    @DisplayName("an override disables a rule and the build says so")
    void overrides() {
        seed(records, clock);
        InfraBuildEntity build = builder.rebuild(Map.of("rules", List.of(Map.of("id", "substation-critical-load",
                "enabled", false))));
        assertThat(build.getEdgesByMethod()).containsEntry("proximity", 1);
        assertThat(build.getWarnings()).contains("rule substation-critical-load is disabled");
    }
}
