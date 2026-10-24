package ai.foresignal.stats;

import static ai.foresignal.testsupport.stats.StatsTestSources.OTHER_KIND;
import static ai.foresignal.testsupport.stats.StatsTestSources.POINTS;
import static ai.foresignal.testsupport.stats.StatsTestSources.POINT_KIND;
import static ai.foresignal.testsupport.stats.StatsTestSources.SECRET;
import static ai.foresignal.testsupport.stats.StatsTestSources.SECRET_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.api.support.TimeField;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.store.timeline.Timeline;
import ai.foresignal.store.timeline.TimelineService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.stats.StatsItSupport;
import ai.foresignal.testsupport.stats.StatsTestSources;
import java.sql.Connection;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

/**
 * The stats engine (prompt 038) against a real PostGIS, records seeded only through
 * {@code TestRecords}: backend selection (rollup, raw, mixed), the bucket limit, the H3 and EPSG:3857
 * grids, top entities, the injected timeout, counts surviving raw deletion, the digest's period
 * totals and the rollup-backed timeline.
 */
@IntegrationTest
@Import(StatsTestSources.class)
class StatsServiceIT extends StatsItSupport {

    @Autowired DataSource dataSource;
    @Autowired TimelineService timelines;

    /** Two versions in hour 0 (two kinds), two in hour 1, one in hour 3 (left open). */
    private void seedHours() {
        seed(POINTS, T0.plusSeconds(600), point("a", POINT_KIND, 10, 50, T0.plusSeconds(600), 1),
                point("b", OTHER_KIND, 10.5, 50.5, T0.plusSeconds(600), 1));
        seed(POINTS, H1.plusSeconds(600), point("a", POINT_KIND, 10, 50, H1.plusSeconds(600), 2),
                point("c", POINT_KIND, 20, 55, H1.plusSeconds(600), 1));
        seed(POINTS, T0.plus(Duration.ofMinutes(185)), point("d", POINT_KIND, 10, 50,
                T0.plus(Duration.ofMinutes(185)), 1));
        rollUpAt(T0.plus(Duration.ofMinutes(195)));
    }

    private StatsSeries series(Instant from, Instant to, String groupBy, TimeField axis) {
        return stats.timeseries(List.of(POINTS), List.of(), null, from, to, "1h", StatsMeasure.VERSIONS, groupBy, axis,
                Set.of(), ReadRestriction.NONE);
    }

    private static List<Long> values(StatsSeries.Series s) {
        return s.points().stream().map(StatsSeries.Point::value).toList();
    }

    @Test
    @DisplayName("closed hours come from the rollup, the open hour from raw rows: rollup, then mixed")
    void backendSelection() {
        seedHours();

        StatsSeries closed = series(T0, T0.plus(Duration.ofHours(3)), "none", TimeField.INGESTED);
        assertThat(closed.backend()).isEqualTo("rollup");
        assertThat(values(closed.series().getFirst())).containsExactly(2L, 2L, 0L);
        assertThat(closed.bucket()).isEqualTo("1h");

        StatsSeries open = series(T0, T0.plus(Duration.ofHours(4)), "kind", TimeField.INGESTED);
        assertThat(open.backend()).isEqualTo("mixed");
        assertThat(values(open.series(POINT_KIND))).containsExactly(1L, 2L, 0L, 1L);
        assertThat(values(open.series(OTHER_KIND))).containsExactly(1L, 0L, 0L, 0L);
        assertThat(open.retainedFrom()).isEqualTo(clock.instant().minus(Duration.ofDays(7)));
    }

    @Test
    @DisplayName("a day still open is served from its closed hours and only the open hour raw; entities stay raw")
    void openDayFromHours() {
        seedHours();
        Instant day = T0.plus(Duration.ofDays(1));

        StatsSeries versions = stats.timeseries(List.of(POINTS), List.of(), null, T0, day, "1d", StatsMeasure.VERSIONS,
                "none", TimeField.INGESTED, Set.of(), ReadRestriction.NONE);
        assertThat(versions.backend()).isEqualTo("mixed");
        assertThat(versions.bucket()).isEqualTo("1d");
        assertThat(values(versions.series().getFirst())).containsExactly(5L);

        StatsQuery q = new StatsQuery(List.of(POINTS), List.of(), null, ReadRestriction.NONE, T0, day,
                RollupResolution.DAY, StatsMeasure.VERSIONS, "none", TimeField.INGESTED);
        StatsService.Plan plan = stats.withClosedHours(q, stats.plan(q, RollupResolution.DAY, T0, day, clock.instant()),
                clock.instant());
        assertThat(plan.hours()).containsExactly(new StatsService.HourPart(List.of(POINTS), T0,
                T0.plus(Duration.ofHours(3))));
        assertThat(plan.raw()).containsExactly(new StatsService.RawPart(List.of(POINTS), T0.plus(Duration.ofHours(3)),
                day));

        StatsSeries entities = stats.timeseries(List.of(POINTS), List.of(), null, T0, day, "1d", StatsMeasure.ENTITIES,
                "none", TimeField.INGESTED, Set.of(), ReadRestriction.NONE);
        assertThat(entities.backend()).isEqualTo("raw");
        assertThat(values(entities.series().getFirst())).containsExactly(4L);
    }

    @Test
    @DisplayName("other time axes read raw history and need from ≥ retainedFrom")
    void rawAxis() {
        seedHours();

        StatsSeries observed = series(T0, T0.plus(Duration.ofHours(4)), "none", TimeField.OBSERVED);
        assertThat(observed.backend()).isEqualTo("raw");
        assertThat(values(observed.series().getFirst())).containsExactly(2L, 2L, 0L, 1L);
        assertThatThrownBy(() -> series(T0.minus(Duration.ofDays(30)), T0, "none", TimeField.OBSERVED))
                .isInstanceOfSatisfying(ValidationException.class, e -> assertThat(e.getErrors())
                        .extracting(ai.foresignal.api.error.FieldError::field).containsExactly("from"));
    }

    @Test
    @DisplayName("a private source the rollup skips is read raw; entities are exact through the totals rows")
    void rawForSkippedSourcesAndEntities() {
        seedHours();
        seed(SECRET, T0.plus(Duration.ofMinutes(190)), point("s", SECRET_KIND, 10, 50,
                T0.plus(Duration.ofMinutes(190)), 1));
        StatsSeries secret = stats.timeseries(List.of(SECRET), List.of(), null, T0, T0.plus(Duration.ofHours(4)), "1h",
                StatsMeasure.VERSIONS, "none", TimeField.INGESTED, Set.of(), ReadRestriction.NONE);
        assertThat(secret.backend()).isEqualTo("raw");
        assertThat(secret.series().getFirst().total()).isEqualTo(1);

        StatsSeries entities = stats.timeseries(List.of(POINTS), List.of(), null, T0, T0.plus(Duration.ofHours(3)),
                "1h", StatsMeasure.ENTITIES, "none", TimeField.INGESTED, Set.of(), ReadRestriction.NONE);
        assertThat(values(entities.series().getFirst())).containsExactly(2L, 2L, 0L);
        StatsSeries alerts = stats.timeseries(List.of(POINTS), List.of(), null, T0, T0.plus(Duration.ofHours(3)), "1h",
                StatsMeasure.ALERTS, "none", TimeField.INGESTED, Set.of(), ReadRestriction.NONE);
        assertThat(alerts.backend()).isEqualTo("raw");
        assertThat(alerts.series().getFirst().total()).isZero();
    }

    @Test
    @DisplayName("more than 2,000 buckets is 400 STATS_TOO_LARGE; auto picks hours up to 7 days, then days")
    void bucketLimit() {
        assertThatThrownBy(() -> series(T0, T0.plus(Duration.ofDays(100)), "none", TimeField.INGESTED))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCodes.STATS_TOO_LARGE));
        assertThat(StatsService.bucket("auto", T0, T0.plus(Duration.ofDays(7)))).isEqualTo(RollupResolution.HOUR);
        assertThat(StatsService.bucket(null, T0, T0.plus(Duration.ofDays(8)))).isEqualTo(RollupResolution.DAY);
        assertThatThrownBy(() -> StatsService.bucket("5m", T0, H1)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("the H3 grid aggregates cells to the requested resolution and returns their polygons")
    void h3Grid() {
        seedHours();

        StatsGrid grid = stats.grid(List.of(POINTS), List.of(), BboxParser.parse("5,45,25,60"), T0,
                T0.plus(Duration.ofHours(3)), StatsMeasure.VERSIONS, 2, "rollup", null, Set.of(), ReadRestriction.NONE);

        assertThat(grid.cellSystem()).isEqualTo("h3");
        assertThat(grid.res()).isEqualTo(2);
        assertThat(grid.backend()).isEqualTo("rollup");
        Map<String, Long> expected = new java.util.TreeMap<>();
        expected.merge(cells.address(cells.parent(cells.cell(10, 50, 4), 2)), 2L, Long::sum);
        expected.merge(cells.address(cells.parent(cells.cell(10.5, 50.5, 4), 2)), 1L, Long::sum);
        expected.merge(cells.address(cells.parent(cells.cell(20, 55, 4), 2)), 1L, Long::sum);
        Map<String, Long> actual = new java.util.TreeMap<>();
        grid.cells().forEach(c -> actual.put(c.id(), c.value()));
        assertThat(actual).as("versions per res-2 parent: a twice, b and c once").isEqualTo(expected);
        assertThat(grid.cells()).allSatisfy(c -> {
            assertThat(cells.resolution(cells.parse(c.id()))).isEqualTo(2);
            assertThat(c.geometry()).containsEntry("type", "Polygon");
        });
        assertThat(grid.toMap()).containsEntry("type", "FeatureCollection");
        assertThatThrownBy(() -> stats.grid(List.of(POINTS), List.of(), BboxParser.parse("5,45,25,60"), T0, H1,
                StatsMeasure.VERSIONS, 9, "rollup", null, Set.of(), ReadRestriction.NONE))
                .isInstanceOf(ValidationException.class)
                // The field error carries the reason; the exception message is the generic detail (checkpoint 041).
                .satisfies(e -> assertThat(((ValidationException) e).getErrors()).singleElement()
                        .satisfies(err -> assertThat(err.message()).contains("cellRes")));
        assertThatThrownBy(() -> stats.grid(List.of(POINTS), List.of(), null, T0, H1, StatsMeasure.VERSIONS, null,
                "rollup", null, Set.of(), ReadRestriction.NONE)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("the raw grid uses EPSG:3857 hexagons, regular on the map at 60°N, and refuses long ranges")
    void hexGrid() {
        seed(POINTS, T0.plusSeconds(600), point("n1", POINT_KIND, 25.2, 60.2, T0.plusSeconds(600), 1),
                point("n2", POINT_KIND, 25.2, 60.2, T0.plusSeconds(600), 1));

        StatsGrid grid = stats.grid(List.of(POINTS), List.of(), BboxParser.parse("24,59,26,61"), T0, H1,
                StatsMeasure.VERSIONS, null, "raw", 20.0, Set.of(), ReadRestriction.NONE);

        assertThat(grid.cellSystem()).isEqualTo("hex3857");
        assertThat(grid.sizeKm()).isEqualTo(20.0);
        assertThat(grid.cells()).hasSize(1);
        assertThat(grid.cells().getFirst().value()).isEqualTo(2);
        @SuppressWarnings("unchecked")
        List<List<List<Number>>> rings = (List<List<List<Number>>>) grid.cells().getFirst().geometry().get("coordinates");
        List<List<Number>> ring = rings.getFirst();
        assertThat(ring).hasSize(7);
        double expected = 20_000 / Math.cos(Math.toRadians(60));
        for (int i = 0; i < 6; i++) {
            double[] a = mercator(ring.get(i));
            double[] b = mercator(ring.get(i + 1));
            assertThat(Math.hypot(b[0] - a[0], b[1] - a[1])).as("side %d in EPSG:3857 meters", i)
                    .isCloseTo(expected, within(expected * 0.01));
        }
        assertThatThrownBy(() -> stats.grid(List.of(POINTS), List.of(), BboxParser.parse("24,59,26,61"), T0,
                T0.plus(Duration.ofDays(8)), StatsMeasure.VERSIONS, null, "raw", 20.0, Set.of(), ReadRestriction.NONE))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> stats.grid(List.of(POINTS), List.of(), BboxParser.parse("-180,-80,180,80"), T0, H1,
                StatsMeasure.VERSIONS, null, "raw", 0.01, Set.of(), ReadRestriction.NONE))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCodes.STATS_TOO_LARGE));
    }

    private static double[] mercator(List<Number> lonLat) {
        double r = 6378137.0;
        return new double[] {Math.toRadians(lonLat.get(0).doubleValue()) * r,
                r * Math.log(Math.tan(Math.PI / 4 + Math.toRadians(lonLat.get(1).doubleValue()) / 2))};
    }

    @Test
    @DisplayName("top entities read raw history and carry titles; top sources and kinds combine rollup and raw")
    void top() {
        seedHours();

        StatsTop entities = stats.top("entity", StatsMeasure.VERSIONS, T0, T0.plus(Duration.ofHours(4)), 2,
                List.of(POINTS), List.of(), Set.of(), ReadRestriction.NONE);
        assertThat(entities.backend()).isEqualTo("raw");
        assertThat(entities.items()).hasSize(2);
        assertThat(entities.items().getFirst()).satisfies(i -> {
            assertThat(i.externalId()).isEqualTo("a");
            assertThat(i.count()).isEqualTo(2);
            assertThat(i.title()).isEqualTo("Name a");
            assertThat(i.key()).isEqualTo(POINTS + ":a");
        });

        StatsTop kinds = stats.top("kind", StatsMeasure.VERSIONS, T0, T0.plus(Duration.ofHours(4)), 10,
                List.of(POINTS), List.of(), Set.of(), ReadRestriction.NONE);
        assertThat(kinds.backend()).isEqualTo("mixed");
        assertThat(kinds.items()).extracting(StatsTop.Item::kind).containsExactly(POINT_KIND, OTHER_KIND);
        assertThat(kinds.items()).extracting(StatsTop.Item::count).containsExactly(4L, 1L);

        StatsTop topCells = stats.top("cell", StatsMeasure.VERSIONS, T0, T0.plus(Duration.ofHours(4)), 10,
                List.of(POINTS), List.of(), Set.of(), ReadRestriction.NONE);
        assertThat(topCells.items().getFirst().cell()).isEqualTo(cells.address(cells.cell(10, 50, 4)));
        assertThat(topCells.items().getFirst().count()).isEqualTo(3);
        assertThatThrownBy(() -> stats.top("entity", StatsMeasure.VERSIONS, T0.minus(Duration.ofDays(30)), T0, 10,
                List.of(POINTS), List.of(), Set.of(), ReadRestriction.NONE)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("a query past the injected statement timeout is 503 QUERY_TIMEOUT")
    void timeout() throws Exception {
        seedHours();
        StatsQuery q = new StatsQuery(List.of(POINTS), List.of(), null, ReadRestriction.NONE, T0,
                T0.plus(Duration.ofHours(3)), RollupResolution.HOUR, StatsMeasure.VERSIONS, "none", TimeField.INGESTED);
        try (Connection blocker = dataSource.getConnection()) {
            blocker.setAutoCommit(false);
            blocker.createStatement().execute("LOCK TABLE activity_rollup IN ACCESS EXCLUSIVE MODE");
            assertThatThrownBy(() -> stats.series(q, Duration.ofSeconds(1)))
                    .isInstanceOfSatisfying(ForesignalException.class, e -> {
                        assertThat(e.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                        assertThat(e.getCode()).isEqualTo(ErrorCodes.QUERY_TIMEOUT);
                    });
            blocker.rollback();
        }
    }

    @Test
    @DisplayName("counts of a rolled-up hour are unchanged after its raw versions are deleted")
    void countsSurviveRawDeletion() {
        seedHours();
        StatsSeries before = series(T0, T0.plus(Duration.ofHours(3)), "none", TimeField.INGESTED);

        jdbc.update("DELETE FROM record_version WHERE source_id = ? AND ingested_at < ?", POINTS,
                java.sql.Timestamp.from(T0.plus(Duration.ofHours(3))));
        StatsSeries after = series(T0, T0.plus(Duration.ofHours(3)), "none", TimeField.INGESTED);

        assertThat(after.backend()).isEqualTo("rollup");
        assertThat(values(after.series().getFirst())).isEqualTo(values(before.series().getFirst()))
                .containsExactly(2L, 2L, 0L);
    }

    @Test
    @DisplayName("period totals for the digest: whole hours from the rollup, edges from raw, grouped by source and kind")
    void totals() {
        seedHours();
        jdbc.update("DELETE FROM record_version WHERE source_id = ? AND ingested_at < ?", POINTS,
                java.sql.Timestamp.from(T0.plus(Duration.ofHours(3))));

        StatsService.Totals totals = stats.totals(List.of(POINTS), List.of(), null, ReadRestriction.NONE, T0,
                T0.plus(Duration.ofMinutes(200)), StatsService.GROUP_PAIR,
                List.of(StatsMeasure.FIRSTS, StatsMeasure.CHANGES, StatsMeasure.TOMBSTONES));

        assertThat(totals.backend()).isEqualTo("mixed");
        assertThat(totals.complete()).isTrue();
        Map<StatsMeasure, Long> point = totals.values().get(POINTS + StatsService.PAIR_SEPARATOR + POINT_KIND);
        assertThat(point).containsEntry(StatsMeasure.FIRSTS, 3L).containsEntry(StatsMeasure.CHANGES, 1L)
                .containsEntry(StatsMeasure.TOMBSTONES, 0L);
        assertThat(totals.values().get(POINTS + StatsService.PAIR_SEPARATOR + OTHER_KIND))
                .containsEntry(StatsMeasure.FIRSTS, 1L);
    }

    @Test
    @DisplayName("the timeline serves ingested buckets older than retention from the rollup (backend mixed)")
    void rollupTimeline() {
        seed(POINTS, T0.plusSeconds(600), point("t1", POINT_KIND, 10, 50, T0.plusSeconds(600), 1));
        seed(POINTS, H1.plusSeconds(600), point("t2", POINT_KIND, 10, 50, H1.plusSeconds(600), 1));
        Instant now = T0.plus(Duration.ofDays(7)).plus(Duration.ofMinutes(90));
        rollUpAt(now);
        jdbc.update("DELETE FROM record_version WHERE source_id = ?", POINTS);

        Timeline timeline = timelines.timeline(List.of(POINTS), List.of(), null, T0, T0.plus(Duration.ofHours(3)),
                Duration.ofHours(1), TimeField.INGESTED, "versions", "none", false, false, Set.of(),
                ReadRestriction.NONE);

        assertThat(timeline.backend()).isEqualTo("mixed");
        assertThat(timeline.retainedFrom()).isEqualTo(now.minus(Duration.ofDays(7)));
        assertThat(timeline.buckets()).extracting(Timeline.Bucket::total).containsExactly(1L, 1L, 0L);
        assertThat(RollupTimelineBackend.resolution(new ai.foresignal.store.timeline.TimelineQuery(List.of(POINTS),
                List.of(), null, T0, H1, Duration.ofMinutes(30), TimeField.INGESTED, "versions", "none", false, false,
                null))).as("a half-hour bucket cannot come from hourly rows").isNull();
    }
}
