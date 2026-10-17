package ai.foresignal.pol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.derive.DefaultDerivedContext;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.store.purge.EntityPurgeService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.records.RecordsTestSources;
import ai.foresignal.testsupport.weather.FakeDerived;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Pattern of life end to end (prompt 114): incremental stay assignment between nightly recomputes, places that
 * outlive the TRACK history they were learned from, {@code NEW_PLACE} after the warm-up, private inheritance with
 * the precision cap and admin-only visibility, and the 039 purge cascading to the subject and tombstoning its
 * derived records.
 *
 * <p>The subject's device sits at "home" 06:00–06:40 and at "work" (5 km east) 09:00–09:40 UTC every day, one fix
 * every 5 minutes. The server timezone is UTC.
 */
@IntegrationTest
@Import(RecordsTestSources.class)
class PolServiceIT {

    static final Instant D0 = Instant.parse("2026-09-01T00:00:00Z");
    static final String PUB = "test-pol-trk";
    static final String PRIV = "test-pol-private";
    static final String KIND = "test-device";
    static final double HOME_LON = -97.7431;
    static final double HOME_LAT = 30.2672;
    static final double WORK_LON = -97.6911;
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired SourceRegistry registry;
    @Autowired PolService pol;
    @Autowired PolEngine engine;
    @Autowired PolStayRepository stays;
    @Autowired PolPlaceRepository places;
    @Autowired PolSubjectRepository subjects;
    @Autowired EntityPurgeService purge;
    @Autowired JdbcTemplate jdbc;
    @Autowired @Qualifier("polGenerator") PolGenerator generator;
    @Autowired PolPrivateGenerator privateGenerator;

    final Recording out = new Recording();

    @BeforeEach
    void setUp() {
        cleanUp();
        clock.setInstant(D0);
        registry.registerDynamic(track(PUB).build());
        registry.registerDynamic(track(PRIV).privateSource(true).build());
    }

    @AfterEach
    void cleanUp() {
        for (String id : List.of(PUB, PRIV)) {
            if (registry.contains(id)) {
                registry.unregisterDynamic(id);
            }
        }
    }

    static SourceDescriptor.Builder track(String id) {
        return SourceDescriptor.builder(id)
                .name("Pattern-of-life test device " + id)
                .attribution("Test")
                .licence("CC BY 4.0")
                .kind(KIND)
                .mode(Mode.PUSH)
                .historyTier(HistoryTier.TRACK)
                .recordTtl(Duration.ofHours(1))
                .defaultIngestPolicy(IngestPolicy.all(Duration.ofMinutes(5)))
                .defaultBudget(Budget.of(600, 1_000_000));
    }

    /** Fixes every 5 minutes from {@code start} for {@code minutes}, jittered by about 10 m. */
    void stay(String source, String id, Instant start, int minutes, double lon, double lat) {
        for (int m = 0; m <= minutes; m += 5) {
            Instant t = start.plus(Duration.ofMinutes(m));
            clock.setInstant(t);
            double j = (m / 5 % 2 == 0 ? 1 : -1) * 0.0001;
            records.ingestAt(source, t, NormalizedRecord.builder(id, KIND).observedAt(t).point(lon + j, lat).build());
        }
    }

    void days(String source, String id, int from, int to) {
        for (int d = from; d < to; d++) {
            Instant day = D0.plus(Duration.ofDays(d));
            stay(source, id, day.plus(Duration.ofHours(6)), 40, HOME_LON, HOME_LAT);
            stay(source, id, day.plus(Duration.ofHours(9)), 40, WORK_LON, HOME_LAT);
        }
    }

    PolSubjectEntity subject(String ref, boolean admin) {
        return pol.create(JSON.readTree("{\"subjectType\":\"record\",\"ref\":\"" + ref + "\"}"), "test", admin);
    }

    void tick(Instant at, boolean privateClass) {
        clock.setInstant(at);
        engine.tick(privateClass, out, id -> HealthStatus.HEALTHY);
    }

    PolPlaceEntity home(String subjectId) {
        return places.findBySubjectIdOrderByIdAsc(subjectId).stream()
                .filter(p -> Math.abs(p.getCentroid().getX() - HOME_LON) < 0.001).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("stays are assigned incrementally between nightly recomputes, which recluster from the stay store")
    void incrementalAndNightly() {
        days(PUB, "phone", 0, 3);
        PolSubjectEntity s = subject(PUB + "/phone", false);
        tick(D0.plus(Duration.ofDays(3)).plus(Duration.ofHours(5)), false);

        assertThat(stays.countBySubjectId(s.getId())).isEqualTo(6);
        List<PolPlaceEntity> learned = places.findBySubjectIdOrderByIdAsc(s.getId());
        assertThat(learned).hasSize(2).allSatisfy(p -> assertThat(p.getVisits()).isEqualTo(3));
        PolPlaceEntity home = home(s.getId());
        assertThat(home.getRadiusM()).isLessThan(50);
        assertThat(home.getHourOfWeek()[PolStats.bucket(D0.plus(Duration.ofHours(6)), java.time.ZoneOffset.UTC)])
                .isEqualTo(40);
        assertThat(out.places).extracting(PolPlaceEntity::getId).contains(home.getId());
        PolSubjectEntity computed = subjects.findById(s.getId()).orElseThrow();
        assertThat(computed.getLastComputedAt()).isNotNull();
        assertThat(computed.getLastSeq()).isPositive();

        // A fourth home stay the same morning: not due for the nightly run, so it joins the place incrementally.
        Instant day3 = D0.plus(Duration.ofDays(3));
        stay(PUB, "phone", day3.plus(Duration.ofHours(6)), 40, HOME_LON, HOME_LAT);
        tick(day3.plus(Duration.ofHours(7)).plus(Duration.ofMinutes(30)), false);
        PolPlaceEntity after = places.findById(home.getId()).orElseThrow();
        assertThat(after.getVisits()).isEqualTo(4);
        assertThat(subjects.findById(s.getId()).orElseThrow().getLastComputedAt())
                .isEqualTo(computed.getLastComputedAt());
        assertThat(out.deviations).noneMatch(d -> "NEW_PLACE".equals(d.get("deviationType")));

        // After 03:00 the next night the recompute runs over the stored stays and keeps the place's id.
        pol.label(home.getId(), JSON.readTree("{\"label\":\"home\"}"), false);
        tick(D0.plus(Duration.ofDays(4)).plus(Duration.ofMinutes(210)), false);
        PolPlaceEntity nightly = places.findById(home.getId()).orElseThrow();
        assertThat(nightly.getVisits()).isEqualTo(4);
        assertThat(nightly.getLabel()).isEqualTo("home");
        assertThat(subjects.findById(s.getId()).orElseThrow().getLastComputedAt())
                .isAfter(computed.getLastComputedAt());
        assertThat(pol.stays(s.getId(), day3, null, null, 100, false).items()).hasSize(1);
        assertThat(pol.places(s.getId(), null, 1, false).nextCursor()).isNotNull();
    }

    @Test
    @DisplayName("places outlive the TRACK history they were learned from")
    void placesOutliveTrackHistory() {
        days(PUB, "car", 0, 3);
        PolSubjectEntity s = subject(PUB + "/car", false);
        tick(D0.plus(Duration.ofDays(3)).plus(Duration.ofHours(5)), false);
        assertThat(places.countBySubjectId(s.getId())).isEqualTo(2);

        // The TRACK partitions holding the positions are dropped (simulated by deleting the versions).
        jdbc.update("DELETE FROM record_version WHERE source_id = ?", PUB);
        pol.recompute(s.getId(), false);
        tick(D0.plus(Duration.ofDays(3)).plus(Duration.ofHours(6)), false);

        assertThat(places.findBySubjectIdOrderByIdAsc(s.getId())).hasSize(2)
                .allSatisfy(p -> assertThat(p.getVisits()).isEqualTo(3));
        assertThat(stays.countBySubjectId(s.getId())).isEqualTo(6);
    }

    @Test
    @DisplayName("NEW_PLACE is reported for a stay outside every place once the subject has 14 days of history")
    void newPlaceAfterWarmUp() {
        days(PUB, "van", 20, 23);
        PolSubjectEntity s = subject(PUB + "/van", false);
        tick(D0.plus(Duration.ofDays(23)).plus(Duration.ofHours(5)), false);
        assertThat(out.deviations).noneMatch(d -> "NEW_PLACE".equals(d.get("deviationType")));

        // An old stay gives the subject more than 14 days of history.
        PolStayEntity old = new PolStayEntity(ai.foresignal.security.Ulids.next(D0));
        old.setSubjectId(s.getId());
        old.setArrive(D0);
        old.setDepart(D0.plus(Duration.ofMinutes(30)));
        old.setCentroid((Point) engine.point(s, HOME_LON, HOME_LAT));
        old.setFixes(7);
        stays.save(old);
        PolSubjectEntity stored = subjects.findById(s.getId()).orElseThrow();
        Map<String, Object> state = new java.util.LinkedHashMap<>(stored.getState());
        state.remove("historyStart");
        stored.setState(state);
        subjects.save(stored);

        Instant day23 = D0.plus(Duration.ofDays(23));
        stay(PUB, "van", day23.plus(Duration.ofHours(12)), 30, -97.60, 30.40);
        tick(day23.plus(Duration.ofHours(14)), false);
        assertThat(out.deviations).anySatisfy(d -> {
            assertThat(d.get("deviationType")).isEqualTo("NEW_PLACE");
            assertThat(d.get("subjectId")).isEqualTo(s.getId());
            assertThat(d.get("placeId")).isNull();
        });
    }

    @Test
    @DisplayName("a subject on a private source needs admin, is private, is precision-capped and hidden from others")
    void privateInheritance() {
        days(PRIV, "tracker", 0, 3);
        assertThatThrownBy(() -> subject(PRIV + "/tracker", false)).isInstanceOf(ForesignalException.class)
                .satisfies(e -> assertThat(((ForesignalException) e).getStatus().value()).isEqualTo(403));
        PolSubjectEntity s = subject(PRIV + "/tracker", true);
        assertThat(s.isPrivateSubject()).isTrue();
        assertThat(s.getPrecisionDecimals()).isEqualTo(SourceDescriptor.DEFAULT_PRIVATE_PRECISION_DECIMALS);

        tick(D0.plus(Duration.ofDays(3)).plus(Duration.ofHours(5)), false);
        assertThat(stays.countBySubjectId(s.getId())).as("the public generator never processes it").isZero();
        tick(D0.plus(Duration.ofDays(3)).plus(Duration.ofHours(5)), true);
        assertThat(stays.findBySubjectIdOrderByArriveAscIdAsc(s.getId())).isNotEmpty().allSatisfy(stay -> {
            assertThat(capped(stay.getCentroid().getX())).isTrue();
            assertThat(capped(stay.getCentroid().getY())).isTrue();
        });
        assertThat(places.findBySubjectIdOrderByIdAsc(s.getId())).isNotEmpty()
                .allSatisfy(p -> assertThat(capped(p.getCentroid().getX())).isTrue());

        assertThat(pol.list(null, 100, false).items()).noneMatch(i -> s.getId().equals(i.get("id")));
        assertThat(pol.list(null, 100, true).items()).anyMatch(i -> s.getId().equals(i.get("id"))
                && Boolean.TRUE.equals(i.get("private")));
        assertThatThrownBy(() -> pol.get(s.getId(), false)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> pol.places(s.getId(), null, 10, false)).isInstanceOf(NotFoundException.class);
        assertThat(privateGenerator.descriptor().privateSource()).isTrue();
        assertThat(generator.descriptor().privateSource()).isFalse();
    }

    static boolean capped(double value) {
        double scaled = value * 10_000;
        return Math.abs(scaled - Math.rint(scaled)) < 1e-6;
    }

    @Test
    @DisplayName("purging the subject's record deletes the subject, its stays and places, and tombstones its records")
    void purgeCascade() {
        days(PUB, "watch", 0, 3);
        PolSubjectEntity s = subject(PUB + "/watch", false);
        Instant now = D0.plus(Duration.ofDays(3)).plus(Duration.ofHours(5));
        clock.setInstant(now);
        FakeDerived world = new FakeDerived(now);
        DefaultDerivedContext first = world.context(generator.descriptor(), ValidatedParams.empty());
        generator.reconcile(List.of(), first);
        generator.onTick(now, first);
        List<NormalizedRecord> emitted = world.collect(first);
        List<String> placeIds = emitted.stream().filter(r -> PolGenerator.KIND_PLACE.equals(r.kind()))
                .map(NormalizedRecord::externalId).distinct().toList();
        assertThat(placeIds).hasSize(2).allSatisfy(id -> assertThat(id).startsWith(s.getId() + ":place:"));
        assertThat(generator.published(placeIds.getFirst())).isTrue();

        List<HeadSnapshot> heads = new ArrayList<>();
        for (String id : placeIds) {
            heads.add(new HeadSnapshot(PolGenerator.ID, id, PolGenerator.KIND_PLACE, 1, 1, now, false, Map.of(), null));
        }
        world.seed(heads);
        generator.reconcile(heads, world.context(generator.descriptor(), ValidatedParams.empty()));

        purge.purge(PUB, "watch", false);
        assertThat(subjects.findById(s.getId())).isEmpty();
        assertThat(stays.countBySubjectId(s.getId())).isZero();
        assertThat(places.countBySubjectId(s.getId())).isZero();

        DefaultDerivedContext next = world.context(generator.descriptor(), ValidatedParams.empty());
        generator.onTick(now, next);
        List<NormalizedRecord> tombstones = world.collect(next).stream().filter(NormalizedRecord::tombstone).toList();
        assertThat(tombstones).extracting(NormalizedRecord::externalId).containsExactlyInAnyOrderElementsOf(placeIds);
    }

    @Test
    @DisplayName("validation: unknown types, refs, parameters and duplicates are refused")
    void validation() {
        assertThatThrownBy(() -> pol.create(JSON.readTree("{\"subjectType\":\"person\",\"ref\":\"x\"}"), "t", true))
                .isInstanceOf(ForesignalException.class);
        assertThatThrownBy(() -> subject(PUB + "/nobody", true)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> pol.create(JSON.readTree("{\"subjectType\":\"record\",\"ref\":\"" + PUB
                + "/x\",\"params\":{\"eps\":0}}"), "t", true)).isInstanceOf(ForesignalException.class);
        days(PUB, "dup", 0, 1);
        subject(PUB + "/dup", false);
        assertThatThrownBy(() -> subject(PUB + "/dup", false)).isInstanceOf(ForesignalException.class);
        assertThatThrownBy(() -> PolService.parseLabel(JSON.readTree("{\"label\":5}")))
                .isInstanceOf(ForesignalException.class);
    }

    /** Collects what the engine publishes. */
    static final class Recording implements PolEngine.Output {

        final List<PolPlaceEntity> places = new ArrayList<>();
        final List<String> removed = new ArrayList<>();
        final List<Map<String, Object>> deviations = new ArrayList<>();

        @Override
        public void place(PolSubjectEntity subject, PolPlaceEntity place) {
            places.add(place);
        }

        @Override
        public void placeRemoved(String subjectId, String placeId) {
            removed.add(placeId);
        }

        @Override
        public void deviation(PolSubjectEntity subject, String key, Map<String, Object> payload, Point point,
                Instant observedAt) {
            deviations.add(payload);
        }
    }
}
