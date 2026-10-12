package ai.foresignal.trigger;

import static ai.foresignal.testsupport.place.PlaceTestSources.AREAS;
import static ai.foresignal.testsupport.place.PlaceTestSources.AREA_KIND;
import static ai.foresignal.testsupport.place.PlaceTestSources.EVENTS;
import static ai.foresignal.testsupport.place.PlaceTestSources.EVENT_KIND;
import static ai.foresignal.testsupport.place.PlaceTestSources.PHONE;
import static ai.foresignal.testsupport.place.PlaceTestSources.PHONE_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.place.PlaceTestSources;
import ai.foresignal.testsupport.trigger.TriggerFixture;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Moving-anchor ("near me") rules on a {@link MutableClock} (prompt 036, task 5): a target that
 * enters while the anchor is still fires on the target side; an anchor that moves toward a still
 * target fires a reverse enter, and away from it a reverse exit; a move below {@code minMoveMeters}
 * evaluates nothing; a stale anchor suspends the rule with exactly one system event and it resumes on
 * a fresh observation; radius 0 means "the anchor is inside the target's polygon".
 */
@IntegrationTest
@Import(PlaceTestSources.class)
class MovingAnchorIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();
    static final GeometryFactory WGS84 = new GeometryFactory(new PrecisionModel(), 4326);

    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired TriggerRuleService rules;
    @Autowired TriggerRuleRepository repository;
    @Autowired TriggerEngine engine;
    @Autowired AlertStore alerts;
    @Autowired AlertBus bus;
    @Autowired ChangeBus changeBus;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlaceTestSources.SystemEvents systemEvents;

    TriggerFixture fixture;
    Instant now = T;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        systemEvents.clear();
        fixture = new TriggerFixture(rules, engine, changeBus, alerts, bus);
    }

    @AfterEach
    void tearDown() {
        fixture.close();
    }

    void tick() {
        now = now.plusSeconds(10);
        clock.setInstant(now);
    }

    void phone(double lon, double lat) {
        tick();
        records.ingest(PHONE, NormalizedRecord.builder("pixel", PHONE_KIND).observedAt(now).point(lon, lat)
                .put("name", "Pixel").build());
        settle();
    }

    void quake(String id, double lon, double lat) {
        tick();
        records.ingest(EVENTS, NormalizedRecord.builder(id, EVENT_KIND).observedAt(now).point(lon, lat)
                .put("name", id).put("mag", 6).build());
        settle();
    }

    /** Waits for the engine and its anchor pass (both synchronized on the engine). */
    void settle() {
        fixture.settle();
        engine.runAnchors();
    }

    List<AlertView> fired(UUID rule) {
        settle();
        return fixture.alerts(rule);
    }

    UUID rule(String mode, double radiusKm, String extra) {
        return fixture.rule("{'name':'Near me " + mode + "','sources':['" + EVENTS + "'],'mode':'" + mode + "',"
                + "'near':{'anchor':{'source':'" + PHONE + "','externalId':'pixel'},'radiusKm':" + radiusKm + extra + "},"
                + "'messageTemplate':'M{{payload.mag}} {{anchor.distanceKm}} km from you'}");
    }

    double evalX(UUID rule) {
        return jdbc.queryForObject("SELECT ST_X(eval_geom) FROM trigger_anchor_state WHERE rule_id = ?", Double.class, rule);
    }

    @Test
    @DisplayName("a target entering near a still anchor fires on the target side, with the anchor distance and no coordinates")
    void targetSide() throws Exception {
        phone(0, 0);
        UUID rule = rule("enter", 50, "");
        settle();
        quake("near", 0.1, 0);
        quake("far", 2, 0);
        List<AlertView> fired = fired(rule);
        assertThat(fired).extracting(AlertView::externalId).containsExactly("near");
        AlertView alert = fired.getFirst();
        JsonNode context = JSON.readTree(alert.context()).get("anchor");
        assertThat(context.get("source").asString()).isEqualTo(PHONE);
        assertThat(context.get("distanceKm").asDouble()).isCloseTo(11.1, org.assertj.core.data.Offset.offset(0.3));
        assertThat(alert.message()).isEqualTo("M6 11.1 km from you");
        assertThat(alert.context()).doesNotContain("coordinates");
    }

    @Test
    @DisplayName("the anchor moving toward a still target fires a reverse enter; moving away fires a reverse exit")
    void reverse() {
        phone(0, 0);
        UUID enter = rule("enter", 50, "");
        UUID exit = rule("exit", 50, "");
        settle();
        quake("far", 2, 0);
        assertThat(fired(enter)).isEmpty();

        phone(1.8, 0);
        List<AlertView> entered = fired(enter);
        assertThat(entered).extracting(AlertView::externalId).containsExactly("far");
        JsonNode anchor = JSON.readTree(entered.getFirst().context()).get("anchor");
        assertThat(anchor.get("distanceKm").asDouble()).isCloseTo(22.2, org.assertj.core.data.Offset.offset(0.5));
        assertThat(anchor.get("seq").asLong()).isPositive();
        assertThat(fired(exit)).isEmpty();
        Long versionsOfFar = jdbc.queryForObject("SELECT count(*) FROM record_version WHERE external_id = 'far'",
                Long.class);
        assertThat(versionsOfFar).as("reverse alerts write no versions").isEqualTo(1);

        phone(0, 0);
        assertThat(fired(exit)).extracting(AlertView::externalId).containsExactly("far");
        assertThat(fired(enter)).hasSize(1);
    }

    @Test
    @DisplayName("a move shorter than minMoveMeters evaluates nothing")
    void smallMove() {
        phone(0, 0);
        UUID enter = rule("enter", 50, ",'minMoveMeters':500");
        settle();
        quake("edge", 0.45, 0);
        double before = evalX(enter);
        phone(0.003, 0);
        assertThat(evalX(enter)).as("the evaluated position stays").isEqualTo(before);
        assertThat(fired(enter)).isEmpty();
        phone(0.01, 0);
        assertThat(evalX(enter)).isCloseTo(0.01, org.assertj.core.data.Offset.offset(1e-9));
    }

    @Test
    @DisplayName("a stale anchor suspends the rule with one system event, and a fresh observation resumes it")
    void staleness() {
        phone(0, 0);
        UUID enter = rule("enter", 50, ",'maxAnchorAge':'PT5M'");
        settle();

        now = now.plus(Duration.ofMinutes(10));
        clock.setInstant(now);
        engine.runAnchors();
        engine.runAnchors();
        assertThat(systemEvents.of(AnchorEvaluator.STALE_SUBTYPE)).hasSize(1);
        assertThat(systemEvents.of(AnchorEvaluator.STALE_SUBTYPE).getFirst().data())
                .containsEntry("ruleId", enter.toString()).containsEntry("code", "ANCHOR_STALE");
        assertThat(repository.findById(enter).orElseThrow().getLastError()).startsWith("ANCHOR_STALE");
        assertThat(jdbc.queryForObject("SELECT status FROM trigger_anchor_state WHERE rule_id = ?", String.class, enter))
                .isEqualTo("stale");

        quake("while-stale", 0.1, 0);
        assertThat(fired(enter)).as("suspended: no target-side alert").isEmpty();

        phone(0, 0.001);
        assertThat(repository.findById(enter).orElseThrow().getLastError()).isNull();
        quake("after", 0.05, 0);
        assertThat(fired(enter)).extracting(AlertView::externalId).containsExactly("after");
        assertThat(systemEvents.of(AnchorEvaluator.STALE_SUBTYPE)).hasSize(1);
    }

    @Test
    @DisplayName("radius 0: the anchor point inside a warning polygon, from either side's movement")
    void insidePolygon() {
        phone(5, 5);
        UUID inside = fixture.rule("{'name':'Warning over me','sources':['" + AREAS + "'],'mode':'enter',"
                + "'near':{'anchor':{'source':'" + PHONE + "','externalId':'pixel'},'radiusKm':0}}");
        settle();
        tick();
        records.ingest(AREAS, NormalizedRecord.builder("warning-1", AREA_KIND).observedAt(now)
                .geometry(WGS84.createPolygon(new Coordinate[] {new Coordinate(-1, -1), new Coordinate(1, -1),
                    new Coordinate(1, 1), new Coordinate(-1, 1), new Coordinate(-1, -1)}))
                .put("name", "Tornado warning").build());
        settle();
        assertThat(fired(inside)).as("the phone is outside the polygon").isEmpty();

        phone(0.2, 0.2);
        assertThat(fired(inside)).extracting(AlertView::externalId).containsExactly("warning-1");

        tick();
        records.ingest(AREAS, NormalizedRecord.builder("warning-2", AREA_KIND).observedAt(now)
                .geometry(WGS84.createPolygon(new Coordinate[] {new Coordinate(0, 0), new Coordinate(0.5, 0),
                    new Coordinate(0.5, 0.5), new Coordinate(0, 0.5), new Coordinate(0, 0)}))
                .put("name", "Flood warning").build());
        assertThat(fired(inside)).extracting(AlertView::externalId).containsExactly("warning-1", "warning-2");
        assertThat(Map.of("distance", JSON.readTree(fired(inside).getLast().context()).get("anchor")
                .get("distanceKm").asDouble())).containsEntry("distance", 0.0);
    }
}
