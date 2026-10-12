package ai.foresignal.stats.metrics;

import static ai.foresignal.testsupport.stats.StatsTestSources.POINTS;
import static ai.foresignal.testsupport.stats.StatsTestSources.POINT_KIND;
import static ai.foresignal.testsupport.stats.StatsTestSources.SECRET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.notify.SystemEvent;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.stats.StatsItSupport;
import ai.foresignal.testsupport.stats.StatsTestSources;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import tools.jackson.databind.json.JsonMapper;

/**
 * Metric projections (prompt 038) against a real PostGIS, records seeded only through
 * {@code TestRecords}: seeding from current heads, live updates of a SAMPLED source, drops on
 * tombstone, expiry and purge, the coercion counter, the series caps and the settings validation.
 */
@IntegrationTest
@Import(StatsTestSources.class)
@RecordApplicationEvents
class ProjectionServiceIT extends StatsItSupport {

    @Autowired ProjectionService projections;
    @Autowired MetricSettingsService metricSettings;
    @Autowired MeterRegistry meters;
    @Autowired JsonMapper json;
    @Autowired ApplicationEvents events;

    private void project(String body) {
        metricSettings.update(json.readTree(body));
    }

    private static String projection(String name, String source, String path, String extra) {
        return "{\"projections\": [{\"name\": \"" + name + "\", \"source\": \"" + source + "\", \"path\": \"" + path
                + "\"" + extra + "}]}";
    }

    @Test
    @DisplayName("seeding reads current heads; a new version updates the value; a tombstone drops the series")
    void seedingAndChanges() {
        seed(POINTS, T0.plusSeconds(60), point("a", POINT_KIND, 10, 50, T0.plusSeconds(60), 4.5),
                point("b", POINT_KIND, 11, 50, T0.plusSeconds(60), 2));

        project(projection("value", POINTS, "value", ", \"kind\": \"" + POINT_KIND + "\""));

        assertThat(projections.value("value", List.of(POINTS, "a"))).isEqualTo(4.5);
        assertThat(projections.value("value", List.of(POINTS, "b"))).isEqualTo(2.0);
        assertThat(projections.seriesCount()).isEqualTo(2);

        seed(POINTS, T0.plusSeconds(120), point("a", POINT_KIND, 10, 50, T0.plusSeconds(120), 5.5));
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(projections.value("value", List.of(POINTS, "a"))).isEqualTo(5.5));

        seed(POINTS, T0.plusSeconds(180), NormalizedRecord.tombstone("b", POINT_KIND, T0.plusSeconds(180)));
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(projections.value("value", List.of(POINTS, "b"))).isNull());

        String text = projections.scrape(null, p -> true).body();
        assertThat(text).contains("foresignal_data_value{external_id=\"a\",source=\"" + POINTS + "\"} 5.5")
                .contains("foresignal_data_value_observed_timestamp_seconds{");
    }

    @Test
    @DisplayName("a SAMPLED source's live-only updates move the projected value")
    void liveUpdate() {
        String track = TestIngestSources.TRACK;
        seed(track, T0, NormalizedRecord.builder("ac1", TestIngestSources.TRACK_KIND).observedAt(T0).point(10, 50)
                .put("callsign", "ABC").put("alt", 1000).build());
        project(projection("altitude", track, "alt", ""));
        assertThat(projections.value("altitude", List.of(track, "ac1"))).isEqualTo(1000.0);

        seed(track, T0.plusSeconds(10), NormalizedRecord.builder("ac1", TestIngestSources.TRACK_KIND)
                .observedAt(T0.plusSeconds(10)).point(10, 50).put("callsign", "ABC").put("alt", 1100).build());
        assertThat(versions(track, "ac1")).as("below the threshold: a live-only update").isEqualTo(1);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(projections.value("altitude", List.of(track, "ac1"))).isEqualTo(1100.0));
    }

    @Test
    @DisplayName("series are dropped when their record expires or its head is purged")
    void expiryAndPurge() {
        seed(TestIngestSources.TTL, T0, NormalizedRecord.builder("t1", TestIngestSources.TTL_KIND).observedAt(T0)
                .put("v", 3).build());
        seed(POINTS, T0, point("p", POINT_KIND, 10, 50, T0, 1));
        project("{\"projections\": [{\"name\": \"ttl_v\", \"source\": \"" + TestIngestSources.TTL + "\", \"path\": \"v\"},"
                + " {\"name\": \"points_v\", \"source\": \"" + POINTS + "\", \"path\": \"value\"}]}");
        assertThat(projections.seriesCount()).isEqualTo(2);

        clock.setInstant(T0.plus(Duration.ofMinutes(11)));
        projections.sweep();
        assertThat(projections.value("ttl_v", List.of(TestIngestSources.TTL, "t1"))).as("expired").isNull();
        assertThat(projections.value("points_v", List.of(POINTS, "p"))).isEqualTo(1.0);

        jdbc.update("DELETE FROM record_head WHERE source_id = ?", POINTS);
        projections.sweep();
        assertThat(projections.value("points_v", List.of(POINTS, "p"))).as("purged").isNull();
        assertThat(projections.seriesCount()).isZero();
    }

    @Test
    @DisplayName("non-numeric values are skipped and counted; numeric strings are coerced")
    void coercion() {
        seed(POINTS, T0, point("n", POINT_KIND, 10, 50, T0, "abc"), point("s", POINT_KIND, 11, 50, T0, "7.25"));
        double before = meters.counter(ProjectionService.COERCION_METRIC, "projection", "coerce").count();

        project(projection("coerce", POINTS, "value", ""));

        assertThat(projections.value("coerce", List.of(POINTS, "n"))).isNull();
        assertThat(projections.value("coerce", List.of(POINTS, "s"))).isEqualTo(7.25);
        // At least once: the seeding batch may also reach the bus listener after the projection exists.
        assertThat(meters.counter(ProjectionService.COERCION_METRIC, "projection", "coerce").count())
                .isGreaterThanOrEqualTo(before + 1);
        assertThat(ProjectionService.coerce(true)).isNull();
        assertThat(ProjectionService.coerce(" 3 ")).isEqualTo(3.0);
        assertThat(ProjectionService.coerce("NaN")).isNull();
    }

    @Test
    @DisplayName("maxSeries bounds a projection; the global cap raises one system event and adds nothing more")
    void caps() {
        seed(POINTS, T0, point("a", POINT_KIND, 10, 50, T0, 1), point("b", POINT_KIND, 11, 50, T0, 2),
                point("c", POINT_KIND, 12, 50, T0, 3));
        project(projection("capped", POINTS, "value", ", \"maxSeries\": 2"));
        assertThat(projections.values("capped")).hasSize(2);

        try {
            projections.maxTotalSeries(1);
            project(projection("global", POINTS, "value", ""));
            assertThat(projections.seriesCount()).isEqualTo(1);
            assertThat(events.stream(SystemEvent.class).filter(e -> e.subtype().equals(ProjectionService.CAP_EVENT)))
                    .hasSize(1);
        } finally {
            projections.maxTotalSeries(ProjectionService.MAX_TOTAL_SERIES);
            projections.reload();
        }
    }

    @Test
    @DisplayName("labels replace source and external_id; entities with the same label values share a series")
    void labels() {
        seed(POINTS, T0, NormalizedRecord.builder("x", POINT_KIND).observedAt(T0).put("value", 1).put("zone", "north")
                .put("name", "X").build(), NormalizedRecord.builder("y", POINT_KIND).observedAt(T0).put("value", 2)
                .put("zone", "south").put("name", "Y").build());

        project(projection("by_zone", POINTS, "value", ", \"labels\": [{\"name\": \"zone\", \"path\": \"zone\"}]"));

        assertThat(projections.values("by_zone")).containsEntry(List.of("north"), 1.0).containsEntry(List.of("south"), 2.0);
        assertThat(projections.scrape("application/openmetrics-text", p -> true)).satisfies(e -> {
            assertThat(e.contentType()).startsWith("application/openmetrics-text");
            assertThat(e.body()).contains("foresignal_data_by_zone{zone=\"north\"} 1.0").endsWith("# EOF\n");
        });
    }

    @Test
    @DisplayName("validation: name pattern and uniqueness, paths, volatile fields, private sources, labels")
    void validation() {
        assertThatThrownBy(() -> project("{\"projections\": [{\"name\": \"Bad-Name\", \"source\": \"" + POINTS
                + "\", \"path\": \"value\"}, {\"name\": \"dup\", \"source\": \"" + POINTS + "\", \"path\": \"value\"},"
                + " {\"name\": \"dup\", \"source\": \"" + POINTS + "\", \"path\": \"updated\"},"
                + " {\"name\": \"secret\", \"source\": \"" + SECRET + "\", \"path\": \"value\"},"
                + " {\"name\": \"unknown\", \"source\": \"nope\", \"path\": \"a..b\"},"
                + " {\"name\": \"labels\", \"source\": \"" + POINTS + "\", \"path\": \"value\", \"kind\": \"zzz\","
                + " \"labels\": [{\"name\": \"__x\", \"path\": \"a\"}, {\"name\": \"b\", \"path\": \"b\"},"
                + " {\"name\": \"c\", \"path\": \"c\"}, {\"name\": \"d\", \"path\": \"d\"}, {\"name\": \"e\", \"path\": \"e\"}]}]}"))
                .isInstanceOfSatisfying(ValidationException.class, e -> assertThat(e.getErrors())
                        .extracting(FieldError::field)
                        .contains("projections[0].name", "projections[2].name", "projections[2].path",
                                "projections[3].source", "projections[4].source", "projections[4].path",
                                "projections[5].kind", "projections[5].labels"));
        assertThat(metricSettings.get().projections()).as("nothing stored").isEmpty();

        project(projection("secret_ok", SECRET, "value", ", \"allowPrivate\": true"));
        assertThat(metricSettings.get().projections()).singleElement()
                .satisfies(p -> assertThat(p.allowPrivate()).isTrue());
        assertThatThrownBy(() -> project("{\"projections\": [{\"name\": \"l\", \"source\": \"" + POINTS
                + "\", \"path\": \"value\", \"labels\": [{\"name\": \"__x\", \"path\": \"a\"}]}]}"))
                .isInstanceOfSatisfying(ValidationException.class, e -> assertThat(e.getErrors())
                        .extracting(FieldError::field).contains("projections[0].labels[0].name"));
    }
}
