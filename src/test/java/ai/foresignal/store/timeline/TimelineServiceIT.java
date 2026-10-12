package ai.foresignal.store.timeline;

import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK_KIND;
import static ai.foresignal.testsupport.timetravel.TimeTravelTestSources.MOVER;
import static ai.foresignal.testsupport.timetravel.TimeTravelTestSources.VEHICLE;
import static ai.foresignal.testsupport.timetravel.TimeTravelTestSources.ZONE;
import static ai.foresignal.testsupport.timetravel.TimeTravelTestSources.ZONE_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.api.support.TimeField;
import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.AlertEntity;
import ai.foresignal.store.AlertRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.timetravel.TimeTravelTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The timeline (prompt 037) against a real PostGIS, with records seeded only through
 * {@link TestRecords} (alerts, which are not records, through their repository): buckets,
 * {@code groupBy}, {@code measure=entities}, keyframes excluded, the alert overlay, the ingested axis
 * through {@code seq_clock}, and the bucket limit.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(TimeTravelTestSources.class)
class TimelineServiceIT {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");
    static final Duration MINUTE = Duration.ofMinutes(1);

    @Autowired TimelineService timelines;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired AlertRepository alerts;
    @Autowired MockMvc mvc;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
    }

    private void ingest(Instant at, String source, NormalizedRecord... batch) {
        if (clock.instant().isBefore(at)) {
            clock.setInstant(at);
        }
        IngestOutcome outcome = records.ingestAt(source, at, batch);
        assertThat(outcome.rejections()).isEmpty();
    }

    private static Instant t(long seconds) {
        return T.plusSeconds(seconds);
    }

    private static NormalizedRecord mover(String id, double lon, Instant observed) {
        return NormalizedRecord.builder(id, VEHICLE).observedAt(observed).point(lon, 50).put("callsign", id).build();
    }

    private Timeline timeline(List<String> sources, TimeField field, String measure, String groupBy,
            boolean keyframes, boolean alerts) {
        return timelines.timeline(sources, List.of(), null, t(0), t(180), MINUTE, field, measure, groupBy, keyframes,
                alerts, Set.of(), ReadRestriction.NONE);
    }

    private void seed() {
        ingest(t(0), MOVER, mover("m1", 10.0, t(0)));
        ingest(t(30), MOVER, mover("m1", 10.1, t(30)), mover("m2", 10.2, t(30)));
        ingest(t(70), MOVER, mover("m1", 10.2, t(70)));
        ingest(t(130), ZONE, NormalizedRecord.builder("z1", ZONE_KIND).observedAt(t(130)).point(10, 50)
                .put("name", "Z").put("level", 1).build());
        clock.setInstant(t(600));
    }

    @Test
    @DisplayName("versions per bucket on the observed axis, every bucket returned")
    void buckets() {
        seed();
        Timeline timeline = timeline(List.of(MOVER, ZONE), TimeField.OBSERVED, "versions", "none", false, false);
        assertThat(timeline.buckets()).extracting(Timeline.Bucket::t).containsExactly(t(0), t(60), t(120));
        assertThat(timeline.buckets()).extracting(Timeline.Bucket::total).containsExactly(3L, 1L, 1L);
        assertThat(timeline.maxBucket()).isEqualTo(3L);
        assertThat(timeline.alerts()).isNull();
        assertThat(timeline.backend()).isEqualTo("raw");
        assertThat(timeline.retainedFrom()).as("the track window is the shortest").isEqualTo(t(600).minus(Duration.ofDays(3)));
        assertThat(timeline.buckets().getFirst().counts()).isEmpty();
    }

    @Test
    @DisplayName("groupBy kind and source, and measure=entities")
    void groupingAndEntities() {
        seed();
        Timeline byKind = timeline(List.of(MOVER, ZONE), TimeField.OBSERVED, "versions", "kind", false, false);
        assertThat(byKind.buckets().getFirst().counts()).containsExactly(java.util.Map.entry(VEHICLE, 3L));
        assertThat(byKind.buckets().get(2).counts()).containsExactly(java.util.Map.entry(ZONE_KIND, 1L));
        Timeline bySource = timeline(List.of(), TimeField.OBSERVED, "versions", "source", false, false);
        assertThat(bySource.buckets().get(2).counts()).containsEntry(ZONE, 1L);

        Timeline entities = timeline(List.of(MOVER), TimeField.OBSERVED, "entities", "kind", false, false);
        assertThat(entities.buckets()).extracting(Timeline.Bucket::total).containsExactly(2L, 1L, 0L);
        assertThat(entities.buckets().getFirst().counts()).containsEntry(VEHICLE, 2L);
    }

    @Test
    @DisplayName("HEARTBEAT and KEYFRAME versions are excluded unless includeKeyframes")
    void keyframesExcluded() {
        for (long second = 0; second <= 480; second += 60) {
            ingest(t(second), TRACK, NormalizedRecord.builder("ac", TRACK_KIND).observedAt(t(second)).point(10, 50)
                    .put("callsign", "ABC").put("squawk", "1200").put("alt", 0).put("gs", 0).put("track", 90).build());
        }
        clock.setInstant(t(1000));
        Timeline without = timelines.timeline(List.of(TRACK), List.of(), null, t(0), t(600), Duration.ofMinutes(10),
                TimeField.OBSERVED, "versions", "none", false, false, Set.of(), ReadRestriction.NONE);
        Timeline with = timelines.timeline(List.of(TRACK), List.of(), null, t(0), t(600), Duration.ofMinutes(10),
                TimeField.OBSERVED, "versions", "none", true, false, Set.of(), ReadRestriction.NONE);
        assertThat(without.buckets().getFirst().total()).as("only the FIRST version").isEqualTo(1L);
        assertThat(with.buckets().getFirst().total()).isGreaterThan(1L);
        assertThat(without.approximate()).as("SAMPLED").isTrue();
    }

    @Test
    @DisplayName("the alert overlay counts alerts per severity per bucket")
    void alertOverlay() {
        seed();
        UUID rule = UUID.randomUUID();
        AlertEntity warning = new AlertEntity(rule, 1L, MOVER, "m1", t(10));
        warning.setKind(VEHICLE);
        AlertEntity critical = new AlertEntity(rule, 2L, MOVER, "m1", t(70));
        critical.setKind(VEHICLE);
        critical.setSeverity("critical");
        AlertEntity other = new AlertEntity(rule, 3L, "test-standard", "x", t(70));
        alerts.saveAndFlush(warning);
        alerts.saveAndFlush(critical);
        alerts.saveAndFlush(other);

        Timeline timeline = timeline(List.of(MOVER), TimeField.OBSERVED, "versions", "none", false, true);
        assertThat(timeline.alerts()).hasSize(3);
        assertThat(timeline.alerts().get(0)).isEqualTo(new Timeline.AlertBucket(t(0), 0, 1, 0));
        assertThat(timeline.alerts().get(1)).isEqualTo(new Timeline.AlertBucket(t(60), 0, 0, 1));
        assertThat(timeline.alerts().get(2)).isEqualTo(new Timeline.AlertBucket(t(120), 0, 0, 0));
    }

    @Test
    @DisplayName("timeField=ingested bins by ingest time through seq_clock")
    void ingestedAxis() {
        // Observed at t(0) but received and ingested at t(130).
        ingest(t(130), MOVER, mover("late", 10, t(0)));
        clock.setInstant(t(600));
        Timeline observed = timeline(List.of(MOVER), TimeField.OBSERVED, "versions", "none", false, false);
        Timeline ingested = timeline(List.of(MOVER), TimeField.INGESTED, "versions", "none", false, false);
        assertThat(observed.buckets()).extracting(Timeline.Bucket::total).containsExactly(1L, 0L, 0L);
        assertThat(ingested.buckets()).extracting(Timeline.Bucket::total).containsExactly(0L, 0L, 1L);
        assertThat(ingested.timeField()).isEqualTo("ingested");
    }

    @Test
    @DisplayName("bbox narrows, auto buckets, and more than 1,000 buckets is TIMELINE_TOO_LARGE")
    void bboxAutoAndLimit() {
        seed();
        Timeline boxed = timelines.timeline(List.of(MOVER), List.of(), BboxParser.parse("9.95,49.9,10.05,50.1"), t(0),
                t(180), MINUTE, TimeField.OBSERVED, "versions", "none", false, false, Set.of(), ReadRestriction.NONE);
        assertThat(boxed.buckets()).extracting(Timeline.Bucket::total).containsExactly(1L, 0L, 0L);

        assertThat(TimelineService.auto(t(0), t(0).plus(Duration.ofHours(2)))).isEqualTo(MINUTE);
        assertThat(TimelineService.auto(t(0), t(0).plus(Duration.ofDays(1)))).isEqualTo(Duration.ofMinutes(10));
        assertThat(TimelineService.auto(t(0), t(0).plus(Duration.ofDays(36_500)))).isEqualTo(Duration.ofDays(30));
        Timeline auto = timelines.timeline(List.of(MOVER), List.of(), null, t(0), t(180), null, TimeField.OBSERVED,
                "versions", "none", false, false, Set.of(), ReadRestriction.NONE);
        assertThat(auto.bucket()).isEqualTo(MINUTE);

        assertThatThrownBy(() -> timelines.timeline(List.of(MOVER), List.of(), null, t(0), t(7200),
                Duration.ofSeconds(1), TimeField.OBSERVED, "versions", "none", false, false, Set.of(),
                ReadRestriction.NONE))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getCode()).isEqualTo(ErrorCodes.TIMELINE_TOO_LARGE));
        assertThatThrownBy(() -> timelines.timeline(List.of(MOVER), List.of(), null, t(10), t(0), MINUTE,
                TimeField.OBSERVED, "versions", "none", false, false, Set.of(), ReadRestriction.NONE))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("over HTTP: the Timeline document, 400s and the private source 404")
    void http() throws Exception {
        seed();
        mvc.perform(get("/v1/timeline").param("source", MOVER).param("from", t(0).toString())
                        .param("to", t(180).toString()).param("bucket", "1m").param("groupBy", "kind")
                        .param("includeAlerts", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bucket").value("1m"))
                .andExpect(jsonPath("$.timeField").value("observed"))
                .andExpect(jsonPath("$.buckets.length()").value(3))
                .andExpect(jsonPath("$.buckets[0].total").value(3))
                .andExpect(jsonPath("$.buckets[0].counts['" + VEHICLE + "']").value(3))
                .andExpect(jsonPath("$.alerts.length()").value(3))
                .andExpect(jsonPath("$.maxBucket").value(3))
                .andExpect(jsonPath("$.backend").value("raw"));
        mvc.perform(get("/v1/timeline").param("from", t(0).toString()).param("to", t(7200).toString())
                        .param("bucket", "1s"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TIMELINE_TOO_LARGE"));
        mvc.perform(get("/v1/timeline").param("from", t(0).toString())).andExpect(status().isBadRequest());
        mvc.perform(get("/v1/timeline").param("source", "test-private").param("from", t(0).toString())
                        .param("to", t(180).toString()))
                .andExpect(status().isNotFound());
    }
}
