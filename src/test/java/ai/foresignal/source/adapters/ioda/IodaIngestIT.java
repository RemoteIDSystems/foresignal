package ai.foresignal.source.adapters.ioda;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.ingest.PersistReason;
import ai.foresignal.retention.RetentionResolver;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import ai.foresignal.trigger.TriggerEngine;
import ai.foresignal.trigger.TriggerRuleService;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code ioda-outages} end to end through the real fetch path, ingest writer and trigger engine (prompt 068): a
 * critical alert writes FIRST; a re-poll whose only change is the volatile {@code value} writes no version; a normal
 * alert writes SIGNIFICANT; a key that leaves the window expires 6 h after it was last received; a rule
 * {@code level == critical} fires on both the enter and the exit transition.
 */
class IodaIngestIT extends WeatherIngestSupport {

    static final String ID = IodaOutagesAdapter.ID;
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired private TriggerRuleService rules;
    @Autowired private TriggerEngine engine;
    @Autowired private RetentionResolver retention;

    @BeforeEach
    void setUp() {
        clock.setInstant(Instant.parse("2026-10-13T15:20:00Z"));
        configure(ID, false, Map.of());
    }

    /** One country alert on {@code bgp} for {@code code}. */
    private static String alert(String code, String level, long time, int value, int history) {
        return "{\"datasource\":\"bgp\",\"entity\":{\"code\":\"" + code + "\",\"name\":\"Country " + code
                + "\",\"subnames\":[],\"type\":\"country\",\"attrs\":{}},\"time\":" + time + ",\"level\":\"" + level
                + "\",\"condition\":\"" + ("normal".equals(level) ? "normal" : "< 0.99") + "\",\"value\":" + value
                + ",\"historyValue\":" + history + ",\"method\":\"median\"}";
    }

    private static void serve(String... alerts) {
        String body = "{\"type\":\"outages.alerts\",\"metadata\":{},\"requestParameters\":{},\"error\":null,"
                + "\"perf\":null,\"data\":[" + String.join(",", alerts) + "],\"copyright\":\"c\"}";
        UPSTREAM.stubFor(get(urlPathEqualTo(IodaOutagesAdapter.PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(body.getBytes(StandardCharsets.UTF_8))));
    }

    private List<Integer> reasons(String externalId) {
        return jdbc.queryForList("select persist_reason from record_version where source_id = ? and external_id = ?"
                + " order by seq", Integer.class, ID, externalId);
    }

    private String level(String externalId) {
        return jdbc.queryForObject("select payload->>'level' from record_head where source_id = ? and external_id = ?",
                String.class, ID, externalId);
    }

    private long epoch() {
        return clock.instant().getEpochSecond();
    }

    @Test
    @DisplayName("critical → FIRST; a changed value only → no version; normal → SIGNIFICANT")
    void transitions() {
        String key = "country:HT:bgp";
        int before = versions(ID, key);

        serve(alert("HT", "critical", epoch() - 600, 574, 658));
        assertThat(fetch(ID).success()).isTrue();
        assertThat(versions(ID, key) - before).isEqualTo(1);
        List<Integer> first = reasons(key);
        assertThat(first.get(first.size() - 1)).isEqualTo((int) PersistReason.FIRST.code());
        assertThat(level(key)).isEqualTo("critical");

        clock.advance(Duration.ofMinutes(5));
        serve(alert("HT", "critical", epoch() - 600, 571, 658), alert("HT", "critical", epoch() - 900, 574, 658));
        assertThat(fetch(ID).success()).isTrue();
        assertThat(versions(ID, key) - before).as("value, historyValue and ratio are volatile").isEqualTo(1);

        clock.advance(Duration.ofMinutes(5));
        serve(alert("HT", "critical", epoch() - 900, 571, 658), alert("HT", "normal", epoch() - 60, 658, 658));
        assertThat(fetch(ID).success()).isTrue();
        assertThat(versions(ID, key) - before).isEqualTo(2);
        List<Integer> reasons = reasons(key);
        assertThat(reasons.get(reasons.size() - 1)).isEqualTo((int) PersistReason.SIGNIFICANT.code());
        assertThat(level(key)).isEqualTo("normal");
        assertThat(head("event_time", ID, key)).as("a state has no eventTime").isNull();
    }

    @Test
    @DisplayName("a key that falls out of the window expires 6 h after it was last received")
    void expiry() {
        String key = "country:BI:bgp";
        serve(alert("BI", "critical", epoch() - 300, 149, 153));
        assertThat(fetch(ID).success()).isTrue();
        Instant received = live("last_received_at", ID, key);
        assertThat(received).isNotNull();
        assertThat(live("valid_until", ID, key)).isEqualTo(received.plus(Duration.ofHours(6)));

        clock.advance(Duration.ofHours(13));
        serve();
        assertThat(fetch(ID).success()).as("a quiet window is normal").isTrue();
        assertThat(live("valid_until", ID, key)).as("not re-observed, so not extended")
                .isEqualTo(received.plus(Duration.ofHours(6)));
        assertThat(live("valid_until", ID, key)).isBefore(clock.instant());
        assertThat(tombstoned(ID, key)).as("expiry, never a tombstone").isFalse();
    }

    @Test
    @DisplayName("a rule level == critical fires on the enter and on the exit transition")
    void enterExitRule() {
        UUID enter = rule("IODA DM critical (enter)", "enter");
        UUID exit = rule("IODA DM critical (exit)", "exit");

        serve(alert("DM", "critical", epoch() - 600, 35, 55));
        assertThat(fetch(ID).success()).isTrue();
        assertThat(alerts(enter)).isEqualTo(1);
        assertThat(alerts(exit)).isZero();

        clock.advance(Duration.ofMinutes(5));
        serve(alert("DM", "critical", epoch() - 900, 35, 55), alert("DM", "normal", epoch() - 60, 54, 55));
        assertThat(fetch(ID).success()).isTrue();
        assertThat(alerts(enter)).isEqualTo(1);
        assertThat(alerts(exit)).isEqualTo(1);
    }

    @Test
    @DisplayName("only the alerts endpoint is requested (no geocoding); outage retention is 1825 days")
    void noGeocodingAndRetention() {
        serve(alert("LI", "normal", epoch() - 600, 446, 446));
        assertThat(fetch(ID).success()).isTrue();
        UPSTREAM.verify(1, anyRequestedFor(anyUrl()));
        assertThat(jdbc.queryForObject("select payload->>'geometryApproximate' from record_head where source_id = ?"
                + " and external_id = ?", String.class, ID, "country:LI:bgp")).isEqualTo("true");
        assertThat(retention.window(ID, IodaOutagesAdapter.KIND)).isEqualTo(Duration.ofDays(1825));
    }

    private UUID rule(String name, String mode) {
        return rules.create(JSON.readTree("{\"name\":\"" + name + "\",\"sources\":[\"" + ID + "\"],\"kinds\":[\""
                + IodaOutagesAdapter.KIND + "\"],\"conditions\":[{\"path\":\"entityCode\",\"op\":\"==\",\"value\":\"DM\"},"
                + "{\"path\":\"level\",\"op\":\"==\",\"value\":\"critical\"}],\"mode\":\"" + mode + "\"}")).rule().getId();
    }

    private int alerts(UUID rule) {
        engine.catchUp();
        Integer count = jdbc.queryForObject("select count(*) from alert where rule_id = ?", Integer.class, rule);
        return count == null ? 0 : count;
    }
}
