package ai.foresignal.store.track;

import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK_KIND;
import static ai.foresignal.testsupport.timetravel.TimeTravelTestSources.MOVER;
import static ai.foresignal.testsupport.timetravel.TimeTravelTestSources.MOVER_PRIVATE;
import static ai.foresignal.testsupport.timetravel.TimeTravelTestSources.VEHICLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.security.SourceVisibilityGate;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.timetravel.TimeTravelTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tracks (prompt 037) against a real PostGIS, seeded only through {@link TestRecords}: segments at
 * gaps and tombstones, skipped {@code POSITION_STALE} carry-forwards, the live tail under SAMPLED,
 * parallel {@code times} and {@code values}, {@code truncated}, the window limit, the private gate,
 * and the HTTP contract.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(TimeTravelTestSources.class)
class TrackServiceIT {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");

    @Autowired TrackService tracks;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired SourceRegistry registry;
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

    /** A zigzag position, so no point is collinear with its neighbors. */
    private static NormalizedRecord at(String id, int step, Instant observed) {
        return NormalizedRecord.builder(id, VEHICLE).observedAt(observed)
                .point(10 + step * 0.01, 50 + (step % 2) * 0.01)
                .put("callsign", "CS").put("alt", 1000 + step).put("gs", 200.5 + step).build();
    }

    private TrackQuery query(String source, String id, Instant from, Instant to, boolean live, List<String> fields) {
        return new TrackQuery(source, id, from, to, 0.0, 2000, null, live, fields, Set.of(), ReadRestriction.NONE);
    }

    @Test
    @DisplayName("a gap longer than max(ttl, 10 min) and a tombstone each start a new segment")
    void segments() {
        ingest(t(0), MOVER, at("m1", 0, t(0)));
        ingest(t(60), MOVER, at("m1", 1, t(60)));
        ingest(t(120), MOVER, at("m1", 2, t(120)));
        // 11 minutes later: a gap.
        ingest(t(780), MOVER, at("m1", 3, t(780)));
        ingest(t(840), MOVER, at("m1", 4, t(840)));
        ingest(t(900), MOVER, NormalizedRecord.tombstone("m1", VEHICLE, t(900)));
        ingest(t(960), MOVER, at("m1", 5, t(960)));
        ingest(t(1020), MOVER, at("m1", 6, t(1020)));
        clock.setInstant(t(2000));

        TrackResult track = tracks.track(query(MOVER, "m1", t(0), t(2000), false, List.of("alt", "gs")));
        assertThat(track.segments()).isEqualTo(3);
        assertThat(track.coordinates()).extracting(List::size).containsExactly(3, 2, 2);
        assertThat(track.times()).extracting(List::size).containsExactly(3, 2, 2);
        assertThat(track.times().getFirst()).containsExactly(t(0).toEpochMilli(), t(60).toEpochMilli(),
                t(120).toEpochMilli());
        assertThat(track.values()).containsOnlyKeys("alt", "gs");
        assertThat(track.values().get("alt")).extracting(List::size).containsExactly(3, 2, 2);
        assertThat(track.values().get("alt").getFirst()).containsExactly(1000.0, 1001.0, 1002.0);
        assertThat(track.values().get("gs").get(2)).containsExactly(205.5, 206.5);
        assertThat(track.kind()).isEqualTo(VEHICLE);
        assertThat(track.pointsScanned()).isEqualTo(8);
        assertThat(track.pointsReturned()).isEqualTo(7);
        assertThat(track.truncated()).isFalse();
        assertThat(track.live()).isFalse();
        assertThat(track.approximate()).isFalse();
        assertThat(track.watermarkSeq()).isPositive();
        assertThat(track.coordinates().getFirst().getFirst()).containsExactly(10.0, 50.0);
    }

    @Test
    @DisplayName("POSITION_STALE carry-forwards are skipped")
    void positionStaleSkipped() {
        ingest(t(0), MOVER, at("m1", 0, t(0)));
        // No position: the writer carries the last one forward and flags POSITION_STALE.
        ingest(t(60), MOVER, NormalizedRecord.builder("m1", VEHICLE).observedAt(t(60)).put("callsign", "CS")
                .put("alt", 5000).put("gs", 1.0).build());
        ingest(t(120), MOVER, at("m1", 1, t(120)));
        clock.setInstant(t(600));
        TrackResult track = tracks.track(query(MOVER, "m1", t(0), t(600), false, List.of("alt")));
        assertThat(track.pointsScanned()).isEqualTo(3);
        assertThat(track.pointsReturned()).isEqualTo(2);
        assertThat(track.values().get("alt").getFirst()).containsExactly(1000.0, 1001.0);
    }

    @Test
    @DisplayName("under SAMPLED the live position is appended when newer than the last version")
    void liveTail() {
        NormalizedRecord first = NormalizedRecord.builder("ac", TRACK_KIND).observedAt(t(0)).point(10, 50)
                .put("callsign", "ABC").put("squawk", "1200").put("alt", 1000).put("gs", 100).put("track", 90).build();
        ingest(t(0), TRACK, first);
        // 100 m east, 10 s later: below the SAMPLED thresholds, so live only.
        ingest(t(10), TRACK, NormalizedRecord.builder("ac", TRACK_KIND).observedAt(t(10)).point(10.0014, 50)
                .put("callsign", "ABC").put("squawk", "1200").put("alt", 1000).put("gs", 100).put("track", 90).build());
        clock.setInstant(t(20));

        TrackResult track = tracks.track(query(TRACK, "ac", t(0), t(20), true, List.of("gs")));
        assertThat(track.pointsScanned()).isEqualTo(1);
        assertThat(track.live()).isTrue();
        assertThat(track.pointsReturned()).isEqualTo(2);
        assertThat(track.times().getFirst()).containsExactly(t(0).toEpochMilli(), t(10).toEpochMilli());
        assertThat(track.approximate()).as("SAMPLED").isTrue();
        assertThat(track.immutable()).as("a live tail is never immutable").isFalse();

        TrackResult withoutLive = tracks.track(query(TRACK, "ac", t(0), t(20), false, List.of()));
        assertThat(withoutLive.live()).isFalse();
        assertThat(withoutLive.coordinates()).hasSize(1);
        assertThat(withoutLive.coordinates().getFirst()).as("a lone point is repeated").hasSize(2);
        assertThat(withoutLive.pointsReturned()).isEqualTo(1);
    }

    @Test
    @DisplayName("truncated when more versions exist than the scan cap")
    void truncated() {
        for (int i = 0; i < 5; i++) {
            ingest(t(i * 60L), MOVER, at("m1", i, t(i * 60L)));
        }
        clock.setInstant(t(600));
        TrackResult track = tracks.track(query(MOVER, "m1", t(0), t(600), false, List.of()), 3);
        assertThat(track.truncated()).isTrue();
        assertThat(track.pointsScanned()).isEqualTo(3);
        assertThat(tracks.track(query(MOVER, "m1", t(0), t(600), false, List.of()), 5).truncated()).isFalse();
    }

    @Test
    @DisplayName("400 TRACK_WINDOW_TOO_LARGE beyond the retention window, and other 400s and 404s")
    void validation() {
        ingest(t(0), MOVER, at("m1", 0, t(0)));
        clock.setInstant(t(600));
        assertThatThrownBy(() -> tracks.track(query(MOVER, "m1", t(600).minus(Duration.ofDays(4)), t(600), false,
                List.of())))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getCode())
                        .isEqualTo(ErrorCodes.TRACK_WINDOW_TOO_LARGE));
        assertThatThrownBy(() -> tracks.track(query(MOVER, "m1", t(0), t(700), false, List.of())))
                .as("to in the future").isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> tracks.track(query(MOVER, "m1", t(500), t(100), false, List.of())))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> tracks.track(query(MOVER, "m1", t(0), t(600), false, List.of("a", "b", "c", "d", "e"))))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> tracks.track(query(MOVER, "m1", t(0), t(600), false, List.of("bad-name"))))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> tracks.track(query(MOVER, "nobody", t(0), t(600), false, List.of())))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> tracks.track(query("no-such-source", "m1", t(0), t(600), false, List.of())))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("a private source's track is 404 for callers that fail the private gate")
    void privateGate() {
        ingest(t(0), MOVER_PRIVATE, at("p1", 0, t(0)));
        clock.setInstant(t(600));
        Set<String> anonymous = SourceVisibilityGate.excludedSources(registry, null, TokenRestrictions.NONE);
        assertThatThrownBy(() -> tracks.track(new TrackQuery(MOVER_PRIVATE, "p1", t(0), t(600), null, 2000, null,
                false, List.of(), anonymous, ReadRestriction.NONE))).isInstanceOf(NotFoundException.class);
        assertThat(tracks.track(query(MOVER_PRIVATE, "p1", t(0), t(600), false, List.of())).publicAllowed()).isFalse();
    }

    @Test
    @DisplayName("over HTTP: a GeoJSON Feature with MultiLineString, immutable only for a closed window without live")
    void http() throws Exception {
        ingest(t(0), MOVER, at("m1", 0, t(0)));
        ingest(t(60), MOVER, at("m1", 1, t(60)));
        ingest(t(120), MOVER, at("m1", 2, t(120)));
        clock.setInstant(t(3600));
        String url = "/v1/records/" + MOVER + "/m1/track";
        mvc.perform(get(url).param("from", t(0).toString()).param("to", t(600).toString())
                        .param("includeLive", "false").param("tolerance", "0").param("fields", "alt,gs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/geo+json"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable"))
                .andExpect(header().exists(HttpHeaders.ETAG))
                .andExpect(jsonPath("$.type").value("Feature"))
                .andExpect(jsonPath("$.geometry.type").value("MultiLineString"))
                .andExpect(jsonPath("$.geometry.coordinates[0].length()").value(3))
                .andExpect(jsonPath("$.properties.times[0].length()").value(3))
                .andExpect(jsonPath("$.properties.values.alt[0].length()").value(3))
                .andExpect(jsonPath("$.properties.segments").value(1))
                .andExpect(jsonPath("$.properties.sourceId").value(MOVER));
        mvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public, max-age=300"));
        mvc.perform(get(url).param("from", t(0).toString()).param("to", t(600).toString()).param("format", "czml"))
                .andExpect(status().isBadRequest());
        mvc.perform(get(url).param("maxPoints", "1")).andExpect(status().isBadRequest());
        mvc.perform(get(url).param("from", t(3600).minus(Duration.ofDays(5)).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TRACK_WINDOW_TOO_LARGE"));
        mvc.perform(get("/v1/records/" + MOVER_PRIVATE + "/p1/track")).andExpect(status().isNotFound());
    }
}
