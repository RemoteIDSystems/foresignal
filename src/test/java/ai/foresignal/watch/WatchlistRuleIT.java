package ai.foresignal.watch;

import static ai.foresignal.testsupport.place.PlaceTestSources.AIRCRAFT;
import static ai.foresignal.testsupport.place.PlaceTestSources.AIR_A;
import static ai.foresignal.testsupport.place.PlaceTestSources.AIR_B;
import static ai.foresignal.testsupport.place.PlaceTestSources.AIR_C;
import static ai.foresignal.testsupport.place.PlaceTestSources.AIR_NONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.place.PlaceTestSources;
import ai.foresignal.testsupport.trigger.TriggerFixture;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertQuery;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.TriggerEngine;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * One watchlist rule across several sources (prompt 036): {@code inWatchlist} without a path reads
 * each source's declared {@code icao24} field, so one rule fires for a listed tail on every source
 * that declares the identity and never on one that does not; adding an item takes effect on the
 * entity's next observation, never retroactively; the alerts are sensitive and hidden from
 * anonymous callers and feed keys.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(PlaceTestSources.class)
class WatchlistRuleIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;
    @Autowired TestRecords records;
    @Autowired WatchlistService watchlists;
    @Autowired WatchlistSnapshot snapshot;
    @Autowired TriggerRuleService rules;
    @Autowired TriggerEngine engine;
    @Autowired ChangeBus changeBus;
    @Autowired AlertStore alerts;
    @Autowired AlertBus bus;

    String admin;
    TriggerFixture fixture;
    Instant now = T;
    UUID list;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        admin = "Bearer " + adminTokens.rotate();
        clock.setInstant(T);
        fixture = new TriggerFixture(rules, engine, changeBus, alerts, bus);
        list = watchlists.create(JSON.readTree("{\"name\":\"Tails\",\"identity\":\"icao24\"}")).getId();
        watchlists.add(list, JSON.readTree("{\"items\":[{\"value\":\"a1b2c3\",\"label\":\"N123AB\"}]}"));
    }

    @AfterEach
    void tearDown() {
        fixture.close();
    }

    void observe(String source, String id, String field, String value, double lon) {
        now = now.plusSeconds(10);
        clock.setInstant(now);
        records.ingest(source, NormalizedRecord.builder(id, AIRCRAFT).observedAt(now).point(lon, 10)
                .put(field, value).put("name", id).build());
    }

    UUID rule() {
        return fixture.rule("{'name':'Tails','sources':['" + AIR_A + "','" + AIR_B + "','" + AIR_C + "','" + AIR_NONE
                + "'],'conditions':[{'op':'inWatchlist','value':'" + list + "'}],'severity':'info'}");
    }

    @Test
    @DisplayName("one rule fires for the listed tail on each source that declares icao24, whatever its field, and never elsewhere")
    void acrossSources() {
        UUID rule = rule();
        observe(AIR_A, "a-1", "hex", "a1b2c3", 0);
        observe(AIR_B, "b-1", "icao", "A1B2C3", 0);
        observe(AIR_C, "c-1", "addr", " A1B2C3 ", 0);
        observe(AIR_NONE, "n-1", "hex", "A1B2C3", 0);
        observe(AIR_A, "a-2", "hex", "ffffff", 0);
        List<AlertView> fired = fixture.alerts(rule);
        assertThat(fired).extracting(AlertView::sourceId).containsExactlyInAnyOrder(AIR_A, AIR_B, AIR_C);
        assertThat(fired).allSatisfy(a -> assertThat(a.sensitive()).isTrue());
    }

    @Test
    @DisplayName("adding an item takes effect on the entity's next observation; nothing fires retroactively")
    void nextObservation() {
        UUID rule = rule();
        observe(AIR_A, "late", "hex", "b22222", 0);
        assertThat(fixture.alerts(rule)).isEmpty();

        watchlists.add(list, JSON.readTree("{\"items\":[{\"value\":\"B22222\"}]}"));
        assertThat(snapshot.contains(list, "B22222")).isTrue();
        fixture.settle();
        assertThat(fixture.alerts(rule)).as("no retroactive alert").isEmpty();

        observe(AIR_A, "late", "hex", "b22222", 0.5);
        assertThat(fixture.alerts(rule)).hasSize(1);

        watchlists.remove(list, List.of("b22222"));
        observe(AIR_A, "late", "hex", "b22222", 1.0);
        assertThat(fixture.alerts(rule)).as("removed from the list: no more alerts").hasSize(1);
    }

    @Test
    @DisplayName("sensitive alerts: anonymous readers and feed-key restrictions never see them; a token does")
    void sensitive() throws Exception {
        UUID rule = rule();
        observe(AIR_A, "a-1", "hex", "a1b2c3", 0);
        assertThat(fixture.alerts(rule)).hasSize(1);

        JsonNode anonymous = JSON.readTree(mvc.perform(get("/v1/alerts")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        assertThat(anonymous.get("items")).isEmpty();
        JsonNode withToken = JSON.readTree(mvc.perform(get("/v1/alerts").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(withToken.get("items")).hasSize(1);
        assertThat(withToken.get("items").get(0).get("sensitive").asBoolean()).isTrue();
        long id = withToken.get("items").get(0).get("id").asLong();
        mvc.perform(get("/v1/alerts/" + id)).andExpect(status().isNotFound());

        // A feed key's restriction excludes sensitive alerts the same way (prompt 035's clamp).
        ReadRestriction feedKey = new ReadRestriction(null, List.of(AIRCRAFT), null, null, null, true, null)
                .withExcludeSensitive(true);
        assertThat(alerts.list(new AlertQuery(Set.of(rule), null, null, null, null, null, null, null, null, true,
                Set.of(), feedKey), null, 100)).isEmpty();
        assertThat(alerts.list(new AlertQuery(Set.of(rule), null, null, null, null, null, null, null, null, true,
                Set.of(), feedKey.withExcludeSensitive(false)), null, 100)).hasSize(1);
    }

    @Test
    @DisplayName("inWatchlist= on /v1/records needs a token and keeps only listed entities")
    void recordsFilter() throws Exception {
        observe(AIR_A, "a-1", "hex", "a1b2c3", 0);
        observe(AIR_B, "b-2", "icao", "eeeeee", 0);
        mvc.perform(get("/v1/records?inWatchlist=" + list)).andExpect(status().isUnauthorized());
        JsonNode page = JSON.readTree(mvc.perform(get("/v1/records?kind=" + AIRCRAFT + "&inWatchlist=" + list)
                .header(HttpHeaders.AUTHORIZATION, admin)).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString());
        assertThat(page.get("items")).hasSize(1);
        assertThat(page.get("items").get(0).get("externalId").asString()).isEqualTo("a-1");
        mvc.perform(get("/v1/records?inWatchlist=" + UUID.randomUUID()).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNotFound());
    }
}
