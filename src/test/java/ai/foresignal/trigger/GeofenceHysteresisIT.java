package ai.foresignal.trigger;

import static ai.foresignal.testsupport.trigger.TriggerTestSources.SAMPLED;
import static ai.foresignal.testsupport.trigger.TriggerTestSources.SAMPLED_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.trigger.TriggerFixture;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Invariant TRG-2: geofence edges with {@code hysteresisMeters} alternate and never fire from jitter
 * inside the band (prompt 017). The fence spans longitudes 0…0.1 at the equator, where 500 m is about
 * 0.0045°, so positions within ±0.002° of the western edge stay inside the band.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(TriggerTestSources.class)
class GeofenceHysteresisIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final String FENCE = "{'type':'Polygon','coordinates':[[[0,-0.05],[0.1,-0.05],[0.1,0.05],[0,0.05],[0,-0.05]]]}";

    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired TriggerRuleService rules;
    @Autowired TriggerRuleRepository repository;
    @Autowired TriggerEngine engine;
    @Autowired AlertStore alerts;
    @Autowired AlertBus bus;
    @Autowired ChangeBus changeBus;
    @Autowired JdbcTemplate jdbc;

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

    void position(double lon) {
        now = now.plusSeconds(10);
        clock.setInstant(now);
        records.ingest(SAMPLED, NormalizedRecord.builder("boat", SAMPLED_KIND).observedAt(now).point(lon, 0)
                .put("name", "Boat").build());
    }

    long versions() {
        return jdbc.queryForObject("SELECT count(*) FROM record_version WHERE external_id = 'boat'", Long.class);
    }

    @Test
    @DisplayName("jitter along the boundary fires nothing; a real crossing fires one enter and one exit")
    void hysteresis() {
        UUID enter = fixture.rule("{'name':'In','sources':['" + SAMPLED + "'],'geofence':" + FENCE
                + ",'mode':'enter','hysteresisMeters':500,'autoResolve':true}");
        // autoResolve is off unless set (OpenAPI TriggerRuleInput.autoResolve: "false/null = off"); the
        // inverse-edge resolution asserted below needs it (prompt 022 added it).
        UUID exit = fixture.rule("{'name':'Out','sources':['" + SAMPLED + "'],'geofence':" + FENCE
                + ",'mode':'exit','hysteresisMeters':500}");
        assertThat(repository.findById(enter).orElseThrow().getGeofenceInner()).isNotNull();

        position(-0.05);
        for (int i = 0; i < 6; i++) {
            position(i % 2 == 0 ? -0.002 : 0.002);
        }
        assertThat(versions()).as("one edge into the grown fence, then nothing while jittering").isEqualTo(2);
        assertThat(fixture.alerts(enter)).isEmpty();
        assertThat(fixture.alerts(exit)).isEmpty();

        position(0.05);
        position(0.048);
        position(-0.05);
        List<AlertView> entered = fixture.alerts(enter);
        List<AlertView> exited = fixture.alerts(exit);
        assertThat(entered).singleElement().satisfies(a -> assertThat(a.resolvedBy()).isEqualTo("inverse_edge"));
        assertThat(exited).hasSize(1);
        assertThat(exited.getFirst().seq()).isGreaterThan(entered.getFirst().seq());

        for (int i = 0; i < 4; i++) {
            position(i % 2 == 0 ? 0.002 : -0.002);
        }
        assertThat(fixture.alerts(enter)).hasSize(1);
        assertThat(fixture.alerts(exit)).hasSize(1);
    }
}
