package ai.foresignal.trip;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.records.RecordsTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * Trip corridor watch (prompt 114): the corridor buffered from a client-supplied LineString, hazards ordered along the
 * route in their sections, a forecast hazard inside the window through {@code validFrom}, a dry-run check, a
 * recurring occurrence across a DST change, the ephemeral rule created and deleted with {@code activeFrom}/
 * {@code activeUntil}, and route validation ({@code TRIP_ROUTE_INVALID}).
 *
 * <p>The route runs 0.2° (about 22 km) east along the equator with a 2 km buffer.
 */
@IntegrationTest
@Import(RecordsTestSources.class)
class TripCorridorIT {

    static final Instant T = Instant.parse("2026-10-16T12:00:00Z");
    static final Instant DEPART = T.plus(Duration.ofMinutes(30));
    static final String SRC = "test-trip-hazards";
    static final String HAZARD = "test-hazard";
    static final String FORECAST = "test-forecast";
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired SourceRegistry registry;
    @Autowired TripService trips;
    @Autowired TripWatch watch;
    @Autowired TripRunRepository runs;
    @Autowired TripRepository repository;
    @Autowired TriggerRuleRepository rules;

    @BeforeEach
    void setUp() {
        cleanUp();
        clock.setInstant(T);
        registry.registerDynamic(SourceDescriptor.builder(SRC)
                .name("Trip test hazards")
                .attribution("Test")
                .licence("CC BY 4.0")
                .kinds(HAZARD, FORECAST)
                .mode(Mode.PUSH)
                .historyTier(HistoryTier.STANDARD)
                .defaultBudget(Budget.of(60, 100_000))
                .build());
    }

    @AfterEach
    void cleanUp() {
        if (registry.contains(SRC)) {
            registry.unregisterDynamic(SRC);
        }
    }

    static String tripJson(String schedule) {
        return "{\"name\":\"Test commute\",\"route\":{\"type\":\"LineString\",\"coordinates\":[[0,0],[0.1,0],[0.2,0]]},"
                + "\"bufferMeters\":2000,\"schedule\":" + schedule + ",\"expectedDuration\":\"40m\",\"leadTime\":\"60m\","
                + "\"hazardKinds\":[\"" + HAZARD + "\",\"" + FORECAST + "\"]}";
    }

    TripEntity once() {
        return trips.create(JSON.readTree(tripJson("{\"type\":\"once\",\"departAt\":\"" + DEPART + "\"}")), "test");
    }

    NormalizedRecord.Builder hazard(String id, String kind, double lon, double lat) {
        return NormalizedRecord.builder(id, kind).observedAt(T).point(lon, lat).put("title", "Hazard " + id)
                .put("severity", "Moderate");
    }

    @Test
    @DisplayName("the corridor is the route buffered on the ellipsoid")
    void corridor() {
        TripEntity trip = once();
        GeometryFactory f = new GeometryFactory();
        assertThat(trip.getCorridor().contains(f.createPoint(new Coordinate(0.1, 0.01)))).as("1.1 km off").isTrue();
        assertThat(trip.getCorridor().contains(f.createPoint(new Coordinate(0.1, 0.03)))).as("3.3 km off").isFalse();
        assertThat(trip.getCorridor().contains(f.createPoint(new Coordinate(0.2, 0)))).isTrue();
        // Roughly 22 km × 4 km plus the round caps, in square degrees at the equator.
        assertThat(trip.getCorridor().getArea()).isBetween(0.0078, 0.0087);
        Map<String, Object> view = trips.view(trip);
        assertThat(view.get("nextOccurrence")).isEqualTo(DEPART);
        assertThat(view.get("expectedDuration")).isEqualTo("40m");
    }

    @Test
    @DisplayName("hazards are ordered along the route in their sections; a forecast inside the window is timing")
    void hazardsOrderedAndDryRun() {
        records.ingestAt(SRC, T,
                hazard("far-end", HAZARD, 0.18, 0.005).build(),
                hazard("start", HAZARD, 0.02, -0.003).build(),
                hazard("near", HAZARD, 0.1, 0.03).build(),
                hazard("away", HAZARD, 0.1, 0.2).build(),
                hazard("forecast-in", FORECAST, 0.05, 0).validFrom(DEPART.plus(Duration.ofMinutes(10))).build(),
                hazard("forecast-late", FORECAST, 0.06, 0).validFrom(DEPART.plus(Duration.ofHours(5))).build());
        TripEntity trip = once();
        TripRunEntity run = watch.check(trip.getId(), true, null);

        assertThat(run.getStatus()).isEqualTo("dry-run");
        assertThat(run.getDeliveredAt()).isNull();
        assertThat(run.isClear()).isFalse();
        assertThat(run.getOccurrence()).isEqualTo(DEPART);
        List<Map<String, Object>> hazards = run.getHazards();
        assertThat(hazards).extracting(h -> h.get("externalId"))
                .containsExactly("start", "forecast-in", "near", "far-end");
        assertThat(hazards).extracting(h -> h.get("section"))
                .containsExactly("on-route", "timing", "near-route", "on-route");
        assertThat(hazards.getFirst().get("severity")).isEqualTo("warning");
        assertThat(((Number) hazards.get(2).get("distanceFromRouteM")).doubleValue()).isBetween(3_000.0, 3_600.0);
        assertThat(run.getBriefing()).contains("# Trip briefing: Test commute", "## On route", "## Near route",
                "## Timing", "Hazard start");
        assertThat(trips.runs(trip.getId(), null, 10).items()).singleElement()
                .satisfies(r -> assertThat(r.get("status")).isEqualTo("dry-run"));
    }

    @Test
    @DisplayName("a clear route is reported as clear")
    void clear() {
        TripRunEntity run = watch.check(once().getId(), true, null);
        assertThat(run.isClear()).isTrue();
        assertThat(run.getHazards()).isEmpty();
        assertThat(run.getBriefing()).contains("## Route clear");
    }

    @Test
    @DisplayName("a recurring commute keeps its local departure time across the end of daylight saving")
    void recurringAcrossDst() {
        TripEntity trip = trips.create(JSON.readTree(tripJson("{\"type\":\"recurring\",\"days\":[\"SA\",\"SU\",\"MO\"],"
                + "\"departTime\":\"07:45\",\"timezone\":\"America/Chicago\"}")), "test");
        TripSchedule schedule = trips.schedule(trip);
        List<Instant> occurrences = schedule.occurrences(Instant.parse("2026-10-31T00:00:00Z"),
                Instant.parse("2026-11-03T00:00:00Z"));
        assertThat(occurrences).containsExactly(Instant.parse("2026-10-31T12:45:00Z"),
                Instant.parse("2026-11-01T13:45:00Z"), Instant.parse("2026-11-02T13:45:00Z"));
        assertThat(schedule.next(Instant.parse("2026-10-31T13:00:00Z"))).isEqualTo(Instant.parse("2026-11-01T13:45:00Z"));
        assertThat(schedule.toMap()).containsEntry("departTime", "07:45").containsEntry("timezone", "America/Chicago");
    }

    @Test
    @DisplayName("the pre-trip check fires at departAt − leadTime once, and the ephemeral rule watches the window")
    void ephemeralRule() {
        records.ingestAt(SRC, T, hazard("start", HAZARD, 0.02, -0.003).build());
        TripEntity trip = once();
        watch.tick();
        watch.tick();
        List<TripRunEntity> done = runs.findByTripIdOrderByCreatedAtDescIdDesc(trip.getId());
        assertThat(done).singleElement().satisfies(r -> {
            assertThat(r.getStatus()).isEqualTo("delivered");
            assertThat(r.getDeliveredAt()).isEqualTo(T);
        });
        TripEntity watching = repository.findById(trip.getId()).orElseThrow();
        assertThat(watching.getActiveRuleId()).isNotNull();
        TriggerRuleEntity rule = rules.findById(watching.getActiveRuleId()).orElseThrow();
        assertThat(rule.getActiveFrom()).isEqualTo(DEPART);
        assertThat(rule.getActiveUntil()).isEqualTo(DEPART.plus(Duration.ofMinutes(40)));
        assertThat(rule.getTags()).containsExactly(TripWatch.tag(trip.getId()));
        assertThat(rule.getKinds()).containsExactlyInAnyOrder(HAZARD, FORECAST);
        assertThat(rule.getGeofence()).isNotNull();

        clock.setInstant(DEPART.plus(Duration.ofMinutes(41)));
        watch.tick();
        assertThat(rules.findById(watching.getActiveRuleId())).isEmpty();
        assertThat(repository.findById(trip.getId()).orElseThrow().getActiveRuleId()).isNull();
    }

    @Test
    @DisplayName("deleting a trip deletes its runs and its active rule")
    void delete() {
        TripEntity trip = once();
        watch.tick();
        java.util.UUID ruleId = repository.findById(trip.getId()).orElseThrow().getActiveRuleId();
        assertThat(ruleId).isNotNull();
        trips.delete(trip.getId());
        assertThat(rules.findById(ruleId)).isEmpty();
        assertThat(runs.findByTripIdOrderByCreatedAtDescIdDesc(trip.getId())).isEmpty();
    }

    @Test
    @DisplayName("an invalid route is 400 TRIP_ROUTE_INVALID")
    void invalidRoutes() {
        for (String route : List.of("{\"type\":\"LineString\",\"coordinates\":[[0,0]]}",
                "{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[1,0],[1,1],[0,0]]]}",
                "{\"type\":\"LineString\",\"coordinates\":[[1,1],[1,1]]}")) {
            String body = tripJson("{\"type\":\"once\",\"departAt\":\"" + DEPART + "\"}")
                    .replace("{\"type\":\"LineString\",\"coordinates\":[[0,0],[0.1,0],[0.2,0]]}", route);
            assertThatThrownBy(() -> trips.create(JSON.readTree(body), "test")).isInstanceOf(ValidationException.class)
                    .satisfies(e -> assertThat(((ValidationException) e).getCode()).isEqualTo("TRIP_ROUTE_INVALID"));
        }
        String both = tripJson("{\"type\":\"once\",\"departAt\":\"" + DEPART + "\"}").replace("\"route\":",
                "\"routePlaceId\":\"home\",\"route\":");
        assertThatThrownBy(() -> trips.create(JSON.readTree(both), "test"))
                .satisfies(e -> assertThat(((ValidationException) e).getCode()).isEqualTo("TRIP_ROUTE_INVALID"));
        String noDuration = tripJson("{\"type\":\"once\",\"departAt\":\"" + DEPART + "\"}")
                .replace("\"expectedDuration\":\"40m\",", "");
        assertThatThrownBy(() -> trips.create(JSON.readTree(noDuration), "test")).isInstanceOf(ValidationException.class);
    }
}
