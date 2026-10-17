package ai.foresignal.trigger;

import static ai.foresignal.testsupport.trigger.TriggerTestSources.SAMPLED;
import static ai.foresignal.testsupport.trigger.TriggerTestSources.SAMPLED_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.api.mapper.TriggerMapper;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.trigger.TriggerFixture;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The trigger condition {@code approach} (prompt 114): it fires {@code enter} once as a vessel starts closing on a
 * target and honors the cooldown; the persist gate persists the approach flip ({@code TRIGGER_EDGE}) of a SAMPLED
 * source that would otherwise persist nothing; an ambiguous (unknown) target is conservative: every move flips and
 * nothing fires; and the rule reads back in its wire form.
 *
 * <p>The target point is at longitude 0.5 on the equator. A vessel at 10 knots (5.14 m/s) covers about 6.2 km in
 * 20 minutes, so it approaches within the window from longitude 0.46 (about 4.4 km away), never from 0.
 */
@IntegrationTest
@Import(TriggerTestSources.class)
class ApproachConditionIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final String TARGET = "{'type':'Point','coordinates':[0.5,0]}";

    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired TriggerRuleService rules;
    @Autowired TriggerEngine engine;
    @Autowired AlertStore alerts;
    @Autowired AlertBus bus;
    @Autowired ChangeBus changeBus;
    @Autowired JdbcTemplate jdbc;
    @Autowired TriggerMapper mapper;

    TriggerFixture fixture;
    Instant now = T;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        fixture = new TriggerFixture(rules, engine, changeBus, alerts, bus);
    }

    @AfterEach
    void tearDown() {
        fixture.close();
    }

    void position(String id, double lon, double cog) {
        now = now.plusSeconds(10);
        clock.setInstant(now);
        records.ingest(SAMPLED, NormalizedRecord.builder(id, SAMPLED_KIND).observedAt(now).point(lon, 0)
                .put("name", "Boat").put("sog", 10.0).put("cog", cog).build());
    }

    long versions(String id) {
        return jdbc.queryForObject("SELECT count(*) FROM record_version WHERE source_id = ? AND external_id = ?",
                Long.class, SAMPLED, id);
    }

    long edges(String id) {
        return jdbc.queryForObject("SELECT count(*) FROM record_version WHERE source_id = ? AND external_id = ?"
                + " AND persist_reason = ?", Long.class, SAMPLED, id, PersistReason.TRIGGER_EDGE.code());
    }

    UUID approachRule(String target, String extra) {
        return fixture.rule("{'name':'Closing','sources':['" + SAMPLED + "'],'conditions':[{'type':'approach','target':"
                + target + ",'withinMeters':1000,'withinMinutes':20}],'mode':'enter'" + extra + "}");
    }

    @Test
    @DisplayName("fires enter once when the vessel starts closing, and the cooldown holds a second approach")
    void firesOnceAndRespectsCooldown() {
        UUID rule = approachRule("{'point':" + TARGET + "}", ",'cooldown':'1h'");
        position("boat", 0.0, 90);
        position("boat", 0.01, 90);
        assertThat(fixture.routed(rule)).as("55 km away is not within 20 minutes").isEmpty();

        position("boat", 0.46, 90);
        position("boat", 0.465, 90);
        List<AlertView> fired = fixture.routed(rule);
        assertThat(fired).singleElement().satisfies(a -> assertThat(a.externalId()).isEqualTo("boat"));

        position("boat", 0.47, 270);
        position("boat", 0.47, 90);
        assertThat(fixture.routed(rule)).as("the second approach is inside the cooldown").hasSize(1);
    }

    @Test
    @DisplayName("the persist gate persists the approach flip as TRIGGER_EDGE")
    void gateFlip() {
        approachRule("{'point':" + TARGET + "}", "");
        // Not 0.0: (0,0) is the null island, whose geometry the normalizer drops (QA flag), so the next position
        // would add a geometry and persist SIGNIFICANT (checkpoint 124).
        position("ship", 0.001, 90);
        position("ship", 0.005, 90);
        position("ship", 0.01, 90);
        assertThat(versions("ship")).as("a SAMPLED source persists only the first version while nothing flips")
                .isEqualTo(1);
        position("ship", 0.46, 90);
        assertThat(edges("ship")).as("the approach flip is persisted").isEqualTo(1);
        position("ship", 0.462, 90);
        assertThat(edges("ship")).as("still approaching: no flip").isEqualTo(1);
        position("ship", 0.462, 270);
        assertThat(edges("ship")).as("turning away flips back").isEqualTo(2);
    }

    @Test
    @DisplayName("an unknown target is ambiguous: every move is persisted conservatively and nothing fires")
    void staleTargetConservative() {
        UUID rule = approachRule("{'record':'" + SAMPLED + "/ghost'}", "");
        position("tug", 0.40, 90);
        position("tug", 0.41, 90);
        position("tug", 0.42, 90);
        position("tug", 0.42, 90);
        assertThat(versions("tug")).as("three moves flip conservatively; the repeat changes nothing").isEqualTo(3);
        assertThat(fixture.routed(rule)).isEmpty();
    }

    @Test
    @DisplayName("an approach condition reads back in its wire form and validates its target")
    void wireForm() {
        UUID rule = approachRule("{'point':" + TARGET + "}", "");
        Map<String, Object> view = mapper.rule(rules.get(rule));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> conditions = (List<Map<String, Object>>) view.get("conditions");
        assertThat(conditions).singleElement().satisfies(c -> {
            assertThat(c).containsEntry("type", "approach").containsEntry("withinMeters", 1000.0)
                    .containsEntry("withinMinutes", 20.0);
            assertThat(c.get("target")).isInstanceOf(Map.class);
        });
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> fixture.rule("{'name':'Bad','sources':['" + SAMPLED
                + "'],'conditions':[{'type':'approach','target':{},'withinMeters':1,'withinMinutes':1}],'mode':'enter'}"))
                .isInstanceOf(ai.foresignal.api.error.ValidationException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> fixture.rule("{'name':'Bad','sources':['" + SAMPLED
                + "'],'conditions':[{'type':'approach','target':{'point':" + TARGET + "},'withinMeters':1,"
                + "'withinMinutes':1}],'mode':'enter','geofence':{'type':'Polygon','coordinates':[[[0,-1],[1,-1],[1,1],"
                + "[0,1],[0,-1]]]},'hysteresisMeters':100}"))
                .isInstanceOf(ai.foresignal.api.error.ValidationException.class);
    }
}
