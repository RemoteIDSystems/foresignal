package ai.foresignal.store.query;

import static ai.foresignal.testsupport.ingest.TestIngestSources.LATEST;
import static ai.foresignal.testsupport.ingest.TestIngestSources.LATEST_KIND;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK_KIND;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TTL;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TTL_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.records.RecordsTestSources;
import ai.foresignal.testsupport.records.RecordsTestSources.StubRetentionState;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * Transaction-time asOf (Core-Data-Model §12; invariant HIST-3): the state as known at T, never a
 * version committed after T, with the horizon and the three 400 codes.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RecordsTestSources.class)
class AsOfQueryIT {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");

    @Autowired AsOfQueryService asOf;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired JsonMapper json;
    @Autowired StubRetentionState retention;

    @BeforeEach
    void setClock() {
        clock.setInstant(T);
        retention.reset();
    }

    @AfterEach
    void resetRetention() {
        retention.reset();
    }

    private void ingest(Instant at, String source, NormalizedRecord... batch) {
        if (clock.instant().isBefore(at)) {
            clock.setInstant(at);
        }
        records.ingestAt(source, at, batch);
    }

    private static Instant t(long seconds) {
        return T.plusSeconds(seconds);
    }

    private static NormalizedRecord.Builder rec(String id, String kind, Instant observedAt) {
        return NormalizedRecord.builder(id, kind).observedAt(observedAt);
    }

    private static AsOfQueryService.AsOfQuery query(Instant at, List<String> sources, List<String> kinds) {
        return new AsOfQueryService.AsOfQuery(at, sources, kinds, List.of(), null, null, null, 500, FieldSelection.ALL,
                GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON, false, Set.of());
    }

    private Map<String, Object> payload(RecordRow row) {
        return json.readValue(row.payload(), Map.class);
    }

    @Test
    @DisplayName("asOf before and after a revision returns the version known then")
    void revision() {
        ingest(t(0), STANDARD, rec("quake", STANDARD_KIND, t(0)).put("mag", 4.0).build());
        ingest(t(100), STANDARD, rec("quake", STANDARD_KIND, t(100)).put("mag", 4.5).build());
        clock.setInstant(t(200));

        AsOfQueryService.AsOfSlice before = asOf.list(query(t(50), List.of(STANDARD), List.of()));
        assertThat(before.rows()).hasSize(1);
        assertThat(payload(before.rows().getFirst())).containsEntry("mag", 4.0);
        assertThat(before.rows().getFirst().version()).isEqualTo(1);
        assertThat(before.rows().getFirst().ingestedAt()).isEqualTo(t(0));

        AsOfQueryService.AsOfSlice after = asOf.list(query(t(150), List.of(), List.of(STANDARD_KIND)));
        assertThat(payload(after.rows().getFirst())).containsEntry("mag", 4.5);
        assertThat(after.seq()).isEqualTo(after.rows().getFirst().seq());
        assertThat(after.asOf()).isEqualTo(t(150));
        assertThat(after.approximate()).isFalse();
        assertThat(after.horizon()).isEqualTo(t(200).minus(Duration.ofDays(7)).plus(Duration.ofDays(7).dividedBy(4))
                .plus(Duration.ofHours(1)));

        assertThat(asOf.list(query(t(0).minusMillis(1), List.of(STANDARD), List.of())).rows()).isEmpty();
    }

    @Test
    @DisplayName("a stationary keyframed aircraft (TRACK, SAMPLED) is present at a T between keyframes")
    void stationaryKeyframedAircraft() {
        for (long second = 0; second <= 480; second += 60) {
            ingest(t(second), TRACK, rec("ac", TRACK_KIND, t(second)).point(10, 50)
                    .put("callsign", "ABC123").put("squawk", "1200").put("alt", 0).put("gs", 0).put("track", 90).build());
        }
        clock.setInstant(t(600));

        AsOfQueryService.AsOfSlice between = asOf.list(query(t(400), List.of(TRACK), List.of()));
        assertThat(between.rows()).hasSize(1);
        RecordRow row = between.rows().getFirst();
        assertThat(row.ingestedAt()).isAfter(t(0)).isBeforeOrEqualTo(t(400));
        assertThat(between.approximate()).as("SAMPLED").isTrue();

        assertThat(asOf.list(query(t(30), List.of(TRACK), List.of())).rows()).hasSize(1);
        // Last observation at 480 s; the TTL is 5 min on the observed axis, so it has expired by 900 s.
        clock.setInstant(t(1000));
        assertThat(asOf.list(query(t(900), List.of(TRACK), List.of())).rows()).isEmpty();
    }

    @Test
    @DisplayName("a long-valid NWS-style alert (STANDARD, L = 0) stays valid until its explicit expiry")
    void longValidAlert() {
        Instant expires = T.plus(Duration.ofDays(3));
        ingest(t(0), STANDARD, rec("alert", STANDARD_KIND, t(0)).validUntil(expires).put("event", "Flood Watch").build());
        clock.setInstant(T.plus(Duration.ofDays(4)));

        assertThat(asOf.list(query(T.plus(Duration.ofDays(2)), List.of(STANDARD), List.of())).rows())
                .extracting(RecordRow::externalId).containsExactly("alert");
        assertThat(asOf.list(query(expires.plusSeconds(1), List.of(STANDARD), List.of())).rows()).isEmpty();
    }

    @Test
    @DisplayName("a TTL record is valid until ingested_at + ttl")
    void ttlValidity() {
        ingest(t(0), TTL, rec("q", TTL_KIND, t(0)).build());
        clock.setInstant(t(3600));
        assertThat(asOf.list(query(t(9 * 60), List.of(TTL), List.of())).rows()).hasSize(1);
        assertThat(asOf.list(query(t(11 * 60), List.of(TTL), List.of())).rows()).isEmpty();
    }

    @Test
    @DisplayName("tombstoned entities are absent after the tombstone")
    void tombstone() {
        ingest(t(0), STANDARD, rec("x", STANDARD_KIND, t(0)).build());
        ingest(t(60), STANDARD, NormalizedRecord.tombstone("x", STANDARD_KIND, t(60)));
        clock.setInstant(t(120));
        assertThat(asOf.list(query(t(30), List.of(STANDARD), List.of())).rows()).hasSize(1);
        assertThat(asOf.list(query(t(90), List.of(STANDARD), List.of())).rows()).isEmpty();
    }

    @Test
    @DisplayName("bbox and kind apply to the chosen version, and keyset pages merge both tiers")
    void filtersAndPaging() {
        ingest(t(0), STANDARD, rec("a", STANDARD_KIND, t(0)).point(1, 1).build(),
                rec("b", STANDARD_KIND, t(0)).point(1, 1).build());
        ingest(t(0), TRACK, rec("ac", TRACK_KIND, t(0)).point(1, 1).put("callsign", "Z").build());
        ingest(t(30), STANDARD, rec("b", STANDARD_KIND, t(30)).point(50, 50).build());
        clock.setInstant(t(60));

        AsOfQueryService.AsOfQuery inBox = new AsOfQueryService.AsOfQuery(t(40), List.of(STANDARD, TRACK), List.of(),
                List.of(), BboxParser.parse("0,0,2,2"), null, null, 500, FieldSelection.ALL, GeometryOptions.DEFAULT,
                GeometryEncoding.GEOJSON, false, Set.of());
        assertThat(asOf.list(inBox).rows()).extracting(RecordRow::externalId).containsExactly("a", "ac");

        AsOfQueryService.AsOfQuery paged = new AsOfQueryService.AsOfQuery(t(40), List.of(STANDARD, TRACK), List.of(),
                List.of(), null, null, null, 2, FieldSelection.ALL, GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON,
                false, Set.of());
        AsOfQueryService.AsOfSlice first = asOf.list(paged);
        // Keyset order is (source_id, external_id): test-standard before test-track.
        assertThat(first.rows()).extracting(RecordRow::externalId).containsExactly("a", "b");
        AsOfQueryService.AsOfSlice second = asOf.list(new AsOfQueryService.AsOfQuery(t(40), List.of(STANDARD, TRACK),
                List.of(), List.of(), null, null, first.nextCursor(), 2, FieldSelection.ALL, GeometryOptions.DEFAULT,
                GeometryEncoding.GEOJSON, false, Set.of()));
        assertThat(second.rows()).extracting(RecordRow::externalId).containsExactly("ac");
        assertThat(second.nextCursor()).isNull();

        assertThat(asOf.list(query(t(40), List.of(), List.of(TRACK_KIND))).rows())
                .extracting(RecordRow::externalId).containsExactly("ac");
    }

    @Test
    @DisplayName("approximate is true for a SAMPLED or LATEST source")
    void approximate() {
        ingest(t(0), LATEST, rec("l", LATEST_KIND, t(0)).put("name", "n").build());
        clock.setInstant(t(60));
        AsOfQueryService.AsOfSlice slice = asOf.list(query(t(30), List.of(LATEST), List.of()));
        assertThat(slice.approximate()).isTrue();
        assertThat(slice.rows()).hasSize(1);
    }

    @Test
    @DisplayName("400 ASOF_BEYOND_RETENTION before the horizon, with the horizon")
    void beyondHorizon() {
        clock.setInstant(T.plus(Duration.ofDays(30)));
        assertThatThrownBy(() -> asOf.list(query(T, List.of(STANDARD), List.of())))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> {
                    ValidationException v = (ValidationException) e;
                    assertThat(v.getCode()).isEqualTo(ErrorCodes.ASOF_BEYOND_RETENTION);
                    assertThat(v.getProperties()).containsKey("horizon");
                });

        retention.setWindow(STANDARD, Duration.ofDays(60));
        assertThat(asOf.list(query(T, List.of(STANDARD), List.of())).rows()).isEmpty();
    }

    @Test
    @DisplayName("400 ASOF_FILTER_REQUIRED without a source or kind")
    void filterRequired() {
        assertThatThrownBy(() -> asOf.list(query(T, List.of(), List.of())))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getCode()).isEqualTo(ErrorCodes.ASOF_FILTER_REQUIRED));
    }

    @Test
    @DisplayName("400 ASOF_INVALID in the future or combined with a forbidden parameter")
    void invalid() {
        assertThatThrownBy(() -> asOf.list(query(T.plusSeconds(1), List.of(STANDARD), List.of())))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getCode()).isEqualTo(ErrorCodes.ASOF_INVALID));
        for (boolean[] flags : new boolean[][] {
            {true, false, false, false, false}, {false, true, false, false, false}, {false, false, true, false, false},
            {false, false, false, true, false}, {false, false, false, false, true}}) {
            assertThatThrownBy(() -> AsOfQueryService.checkCombination(flags[0], flags[1], flags[2], flags[3], flags[4]))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(e -> assertThat(((ValidationException) e).getCode()).isEqualTo(ErrorCodes.ASOF_INVALID));
        }
        AsOfQueryService.checkCombination(false, false, false, false, false);
    }

    @Test
    @DisplayName("an unknown source in the filter is 404")
    void unknownSource() {
        assertThatThrownBy(() -> asOf.list(query(T, List.of("no-such-source"), List.of())))
                .isInstanceOf(ai.foresignal.api.error.NotFoundException.class);
    }
}
