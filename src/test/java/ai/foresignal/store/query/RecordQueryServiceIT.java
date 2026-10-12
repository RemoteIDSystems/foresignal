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
import ai.foresignal.api.support.FieldSelector;
import ai.foresignal.api.support.NearFilter;
import ai.foresignal.api.support.TimeField;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.records.Queries;
import ai.foresignal.testsupport.records.RecordsTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link RecordQueryService} against a real PostGIS, seeded only through {@link TestRecords}
 * (Core-Data-Model §12, §17).
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RecordsTestSources.class)
class RecordQueryServiceIT {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");

    @Autowired RecordQueryService queries;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired JsonMapper json;

    @BeforeEach
    void setClock() {
        clock.setInstant(T);
    }

    /** Moves the clock forward to {@code at} (never back) and ingests, received at {@code at}. */
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

    private Map<String, Object> payload(RecordRow row) {
        return json.readValue(row.payload(), Map.class);
    }

    private static List<String> ids(List<RecordRow> rows) {
        return rows.stream().map(RecordRow::externalId).toList();
    }

    private void seedPlaces() {
        ingest(t(0), STANDARD,
                rec("alpha", STANDARD_KIND, t(0)).point(-97, 33).put("name", "Alpha 500").build(),
                rec("bravo", STANDARD_KIND, t(0)).point(10, 50).put("name", "Bravo 50%").build(),
                rec("east", STANDARD_KIND, t(0)).point(175, 0).put("name", "East_side").build(),
                rec("west", STANDARD_KIND, t(0)).point(-175, 0).put("name", "West side").build());
        ingest(t(1), TTL, rec("quake", TTL_KIND, t(1)).point(-97.1, 33.1).put("name", "Quake").build());
    }

    @Nested
    @DisplayName("current state")
    class Current {

        @Test
        @DisplayName("the payload merges head and live; a key dropped by a LIVE update is absent")
        void mergedPayloadAfterLive() {
            ingest(t(0), LATEST, rec("m", LATEST_KIND, t(0)).put("name", "a").put("x", 1).put("y", 1).build());
            ingest(t(10), LATEST, rec("m", LATEST_KIND, t(10)).put("name", "a").put("x", 2).build());

            RecordRow row = queries.list(Queries.current().sources(LATEST).build()).rows().getFirst();
            assertThat(payload(row)).isEqualTo(Map.of("name", "a", "x", 2));
            assertThat(row.live()).isTrue();
            assertThat(row.version()).isEqualTo(1);
            assertThat(row.observedAt()).isEqualTo(t(10));
            assertThat(row.persistedObservedAt()).isEqualTo(t(0));
        }

        @Test
        @DisplayName("each filter alone and combined")
        void filters() {
            seedPlaces();
            assertThat(ids(queries.list(Queries.current().sources(TTL).build()).rows())).containsExactly("quake");
            assertThat(ids(queries.list(Queries.current().kinds(STANDARD_KIND).build()).rows()))
                    .containsExactlyInAnyOrder("alpha", "bravo", "east", "west");
            assertThat(ids(queries.list(Queries.current().ids("bravo", "nope").build()).rows())).containsExactly("bravo");
            assertThat(ids(queries.list(Queries.current().bbox(BboxParser.parse("-98,32,-96,34")).build()).rows()))
                    .containsExactlyInAnyOrder("alpha", "quake");
            assertThat(ids(queries.list(Queries.current().near(new NearFilter(-97, 33, 20)).build()).rows()))
                    .containsExactlyInAnyOrder("alpha", "quake");
            assertThat(ids(queries.list(Queries.current().text("bravo").build()).rows())).containsExactly("bravo");
            assertThat(ids(queries.list(Queries.current()
                            .sources(STANDARD).bbox(BboxParser.parse("-98,32,-96,34")).text("alpha").build()).rows()))
                    .containsExactly("alpha");
            assertThat(queries.list(Queries.current().sources(STANDARD).kinds(TTL_KIND).build()).rows()).isEmpty();
            assertThat(ids(queries.list(Queries.current().excluded(STANDARD).build()).rows())).containsExactly("quake");
        }

        @Test
        @DisplayName("a bbox crossing the antimeridian matches both sides")
        void antimeridianBbox() {
            seedPlaces();
            assertThat(ids(queries.list(Queries.current().bbox(BboxParser.parse("170,-10,-170,10")).build()).rows()))
                    .containsExactlyInAnyOrder("east", "west");
        }

        @Test
        @DisplayName("near + radius is measured on the geography, across the antimeridian too")
        void nearOnGeography() {
            seedPlaces();
            // From (179, 0): east (175, 0) is 4 degrees (about 445 km) away, west (-175, 0) 6 degrees (about 668 km).
            assertThat(ids(queries.list(Queries.current().near(new NearFilter(179, 0, 500)).build()).rows()))
                    .containsExactly("east");
            assertThat(ids(queries.list(Queries.current().near(new NearFilter(180, 0, 600)).build()).rows()))
                    .containsExactlyInAnyOrder("east", "west");
            assertThat(queries.list(Queries.current().near(new NearFilter(180, 0, 500)).build()).rows()).isEmpty();
        }

        @Test
        @DisplayName("sort=distance pages nearest first with no duplicates or gaps")
        void distancePaging() {
            List<NormalizedRecord> batch = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                batch.add(rec("d" + i, STANDARD_KIND, t(0)).point(0.1 * i, 0).build());
            }
            ingest(t(0), STANDARD, batch.toArray(NormalizedRecord[]::new));

            List<RecordRow> all = new ArrayList<>();
            String cursor = null;
            do {
                Slices.RecordSlice page = queries.list(Queries.current().near(new NearFilter(0, 0, 100))
                        .order(ListOrder.DISTANCE).limit(2).cursor(cursor).build());
                all.addAll(page.rows());
                cursor = page.nextCursor();
            } while (cursor != null);
            assertThat(ids(all)).containsExactly("d1", "d2", "d3", "d4", "d5");
            assertThat(all.getFirst().distanceMeters()).isBetween(11_000.0, 11_200.0);
            for (int i = 1; i < all.size(); i++) {
                assertThat(all.get(i).distanceMeters()).isGreaterThan(all.get(i - 1).distanceMeters());
            }
        }

        @Test
        @DisplayName("expired records need includeExpired; tombstones never appear")
        void expiryAndTombstones() {
            ingest(t(0), TTL, rec("q1", TTL_KIND, t(0)).put("name", "x").build());
            ingest(t(0), STANDARD, rec("gone", STANDARD_KIND, t(0)).put("name", "y").build());
            ingest(t(5), STANDARD, NormalizedRecord.tombstone("gone", STANDARD_KIND, t(5)));

            assertThat(ids(queries.list(Queries.current().build()).rows())).containsExactly("q1");
            clock.setInstant(t(0).plus(Duration.ofMinutes(11)));
            assertThat(queries.list(Queries.current().build()).rows()).isEmpty();
            assertThat(ids(queries.list(Queries.current().includeExpired(true).build()).rows())).containsExactly("q1");
        }

        @Test
        @DisplayName("% and _ in q are literal")
        void textEscaping() {
            seedPlaces();
            assertThat(ids(queries.list(Queries.current().text("50%").build()).rows())).containsExactly("bravo");
            assertThat(ids(queries.list(Queries.current().text("_").build()).rows())).containsExactly("east");
            assertThat(ids(queries.list(Queries.current().text("ALPHA").build()).rows())).containsExactly("alpha");
        }

        @Test
        @DisplayName("order=observed and order=id page equal timestamps without duplicates or gaps")
        void pagingWithEqualTimestamps() {
            List<NormalizedRecord> batch = new ArrayList<>();
            for (String id : List.of("e", "a", "d", "b", "c")) {
                batch.add(rec(id, STANDARD_KIND, t(0)).build());
            }
            ingest(t(0), STANDARD, batch.toArray(NormalizedRecord[]::new));
            ingest(t(1), LATEST, rec("z", LATEST_KIND, t(1)).put("name", "n").build());

            assertThat(ids(pageAll(ListOrder.OBSERVED))).containsExactly("z", "e", "d", "c", "b", "a");
            assertThat(ids(pageAll(ListOrder.ID))).containsExactly("z", "a", "b", "c", "d", "e");
        }

        private List<RecordRow> pageAll(ListOrder order) {
            List<RecordRow> all = new ArrayList<>();
            String cursor = null;
            int pages = 0;
            do {
                Slices.RecordSlice page = queries.list(Queries.current().order(order).limit(2).cursor(cursor).build());
                all.addAll(page.rows());
                cursor = page.nextCursor();
                pages++;
            } while (cursor != null);
            assertThat(pages).isEqualTo(3);
            return all;
        }

        @Test
        @DisplayName("snapshot paging is stable under mutation; changes after w deliver the update")
        void snapshotStableUnderMutation() {
            ingest(t(0), STANDARD,
                    rec("a", STANDARD_KIND, t(0)).put("v", 1).build(),
                    rec("b", STANDARD_KIND, t(0)).put("v", 1).build(),
                    rec("c", STANDARD_KIND, t(0)).put("v", 1).build(),
                    rec("d", STANDARD_KIND, t(0)).put("v", 1).build());

            Slices.RecordSlice first = queries.list(Queries.current().order(ListOrder.ID).limit(2).build());
            long w = first.asOfSeq();
            assertThat(w).isEqualTo(first.watermarkSeq());
            assertThat(ids(first.rows())).containsExactly("a", "b");

            ingest(t(10), STANDARD, rec("c", STANDARD_KIND, t(10)).put("v", 2).build());

            Slices.RecordSlice second = queries.list(
                    Queries.current().order(ListOrder.ID).limit(2).cursor(first.nextCursor()).build());
            assertThat(ids(second.rows())).containsExactly("d");
            assertThat(second.asOfSeq()).isEqualTo(w);
            assertThat(second.nextCursor()).isNull();

            Slices.ChangeSlice changes = queries.changes(Queries.changes(w).build());
            assertThat(ids(changes.rows())).containsExactly("c");
            assertThat(changes.rows().getFirst().version()).isEqualTo(2);
        }

        @Test
        @DisplayName("asOfSeq pins every page, page 1 included")
        void asOfSeqPins() {
            ingest(t(0), STANDARD, rec("a", STANDARD_KIND, t(0)).build());
            long w = queries.list(Queries.current().build()).watermarkSeq();
            ingest(t(10), STANDARD, rec("b", STANDARD_KIND, t(10)).build());

            Slices.RecordSlice pinned = queries.list(Queries.current().asOfSeq(w).build());
            assertThat(ids(pinned.rows())).containsExactly("a");
            assertThat(pinned.asOfSeq()).isEqualTo(w);
            assertThat(queries.list(Queries.current().build()).asOfSeq()).isNull();
        }

        @Test
        @DisplayName("a cursor from another ordering or query is INVALID_CURSOR")
        void mismatchedCursor() {
            ingest(t(0), STANDARD, rec("a", STANDARD_KIND, t(0)).build(), rec("b", STANDARD_KIND, t(0)).build());
            String cursor = queries.list(Queries.current().limit(1).build()).nextCursor();
            assertThatThrownBy(() -> queries.list(Queries.current().order(ListOrder.ID).limit(1).cursor(cursor).build()))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(e -> assertThat(((ValidationException) e).getCode()).isEqualTo(ErrorCodes.INVALID_CURSOR));
            assertThatThrownBy(() -> queries.list(Queries.current().kinds("x").limit(1).cursor(cursor).build()))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("timeField effective vs observed for a revised quake")
        void effectiveVersusObserved() {
            Instant origin = t(0);
            ingest(t(7200), STANDARD, rec("quake", STANDARD_KIND, t(7200)).eventTime(origin).put("mag", 4.5).build());

            Queries.Current window = Queries.current().since(origin.minusSeconds(3600)).until(origin.plusSeconds(3600));
            assertThat(ids(queries.list(window.timeField(TimeField.EFFECTIVE).build()).rows())).containsExactly("quake");
            assertThat(queries.list(window.timeField(TimeField.OBSERVED).build()).rows()).isEmpty();
            assertThat(ids(queries.list(window.timeField(TimeField.EVENT).build()).rows())).containsExactly("quake");
            assertThat(ids(queries.list(Queries.current().since(t(7000)).timeField(TimeField.OBSERVED).build()).rows()))
                    .containsExactly("quake");
        }

        @Test
        @DisplayName("timeField=ingested becomes seq bounds through seq_clock")
        void ingestedAxis() {
            ingest(t(0), STANDARD, rec("early", STANDARD_KIND, t(0)).build());
            ingest(t(300), STANDARD, rec("late", STANDARD_KIND, t(0)).build());

            assertThat(ids(queries.list(Queries.current().timeField(TimeField.INGESTED).since(t(60)).build()).rows()))
                    .containsExactly("late");
            assertThat(ids(queries.list(Queries.current().timeField(TimeField.INGESTED).until(t(60)).build()).rows()))
                    .containsExactly("early");
            assertThat(ids(queries.list(Queries.current().timeField(TimeField.INGESTED).since(t(0)).until(t(301)).build())
                    .rows())).containsExactlyInAnyOrder("early", "late");
        }

        @Test
        @DisplayName("include=observer reads the head's version")
        void observers() {
            ingest(t(0), TRACK, rec("ac", TRACK_KIND, t(0)).point(10, 50).put("callsign", "X")
                    .observers(List.of(Observer.of("rx-1"))).build());
            RecordRow with = queries.list(Queries.current().sources(TRACK).includeObserver(true).build()).rows().getFirst();
            assertThat(with.observer()).contains("rx-1");
            RecordRow without = queries.list(Queries.current().sources(TRACK).build()).rows().getFirst();
            assertThat(without.observer()).isNull();
            assertThat(queries.get(TRACK, "ac", true, ai.foresignal.api.support.FieldSelection.ALL,
                    ai.foresignal.api.support.GeometryOptions.DEFAULT, Set.of()).orElseThrow().observer()).contains("rx-1");
        }

        @Test
        @DisplayName("private sources are left out for anonymous callers (excludedSources)")
        void privateSourceHidden() {
            ingest(t(0), RecordsTestSources.PRIVATE, rec("s1", RecordsTestSources.PRIVATE_KIND, t(0)).build());
            assertThat(queries.list(Queries.current().excluded(RecordsTestSources.PRIVATE).build()).rows()).isEmpty();
            assertThat(ids(queries.list(Queries.current().build()).rows())).containsExactly("s1");
            assertThat(queries.get(RecordsTestSources.PRIVATE, "s1", false, ai.foresignal.api.support.FieldSelection.ALL,
                    ai.foresignal.api.support.GeometryOptions.DEFAULT, Set.of(RecordsTestSources.PRIVATE))).isEmpty();
            assertThat(queries.kinds(Set.of(RecordsTestSources.PRIVATE))).isEmpty();
        }

        @Test
        @DisplayName("fields= projects payload paths in SQL and can drop the geometry")
        void projection() {
            ingest(t(0), STANDARD, rec("p", STANDARD_KIND, t(0)).point(1, 2)
                    .put("mag", 4.5).put("nested", Map.of("a", 1, "b", 2)).put("other", "x").build());
            RecordRow row = queries.list(Queries.current().fields(FieldSelector.parse("externalId,payload.mag,payload.nested.a"))
                    .build()).rows().getFirst();
            assertThat(payload(row)).isEqualTo(Map.of("mag", 4.5, "nested.a", 1));
            assertThat(row.geometry()).isNull();
        }

        @Test
        @DisplayName("kinds counts current records per kind")
        void kinds() {
            seedPlaces();
            List<Slices.KindRow> kinds = queries.kinds(Set.of());
            // Sorted by kind: "test-event" (TTL) before "test-record" (STANDARD).
            assertThat(kinds).extracting(Slices.KindRow::kind).containsExactly(TTL_KIND, STANDARD_KIND);
            assertThat(kinds.get(1).count()).isEqualTo(4);
            assertThat(kinds.get(1).sources()).containsExactly(STANDARD);
        }
    }

    @Nested
    @DisplayName("versions and history")
    class VersionsAndHistory {

        @Test
        @DisplayName("versions are ordered by seq and paged with a seq cursor")
        void versions() {
            for (int i = 0; i < 5; i++) {
                ingest(t(i * 10L), STANDARD, rec("v", STANDARD_KIND, t(i * 10L)).put("n", i).build());
            }
            Slices.VersionSlice first = queries.versions(STANDARD, "v", null, null, null, 3, false, Set.of()).orElseThrow();
            assertThat(first.rows()).extracting(RecordRow::version).containsExactly(1, 2, 3);
            Slices.VersionSlice second =
                    queries.versions(STANDARD, "v", null, null, first.nextCursor(), 3, false, Set.of()).orElseThrow();
            assertThat(second.rows()).extracting(RecordRow::version).containsExactly(4, 5);
            assertThat(second.nextCursor()).isNull();
            assertThat(second.rows().getFirst().persistReason()).isNotNull();
            assertThat(second.rows().getFirst().ingestedAt()).isNotNull();

            Slices.VersionSlice ranged = queries.versions(STANDARD, "v", t(10), t(30), null, 10, false, Set.of()).orElseThrow();
            assertThat(ranged.rows()).extracting(RecordRow::version).containsExactly(2, 3);
            assertThat(queries.versions(STANDARD, "missing", null, null, null, 10, false, Set.of())).isEmpty();
            assertThat(queries.versions(STANDARD, "v", null, null, null, 10, false, Set.of(STANDARD))).isEmpty();
        }

        @Test
        @DisplayName("history pages on (observedAt, seq) and filters by axis, kind and bbox")
        void history() {
            for (int i = 0; i < 4; i++) {
                ingest(t(i * 10L), STANDARD, rec("h" + (i % 2), STANDARD_KIND, t(i * 10L)).point(i, 0).put("n", i).build());
            }
            List<RecordRow> all = new ArrayList<>();
            String cursor = null;
            do {
                Slices.VersionSlice page = queries.history(Queries.history(T).limit(3).cursor(cursor).build());
                all.addAll(page.rows());
                cursor = page.nextCursor();
            } while (cursor != null);
            assertThat(all).extracting(RecordRow::observedAt).containsExactly(t(0), t(10), t(20), t(30));
            assertThat(new HashSet<>(all.stream().map(RecordRow::seq).toList())).hasSize(4);

            assertThat(queries.history(Queries.history(t(15)).build()).rows()).hasSize(2);
            assertThat(queries.history(Queries.history(T).until(t(15)).build()).rows()).hasSize(2);
            assertThat(queries.history(Queries.history(T).bbox(BboxParser.parse("1.5,-1,3.5,1")).build()).rows()).hasSize(2);
            assertThat(queries.history(Queries.history(T).ids("h0").build()).rows()).hasSize(2);
            assertThat(queries.history(Queries.history(T).timeField(TimeField.INGESTED).build()).rows()).hasSize(4);
            assertThat(queries.history(Queries.history(t(25)).timeField(TimeField.INGESTED).build()).rows()).hasSize(1);
            assertThat(queries.history(Queries.history(T).kinds("other").build()).rows()).isEmpty();
        }
    }
}
