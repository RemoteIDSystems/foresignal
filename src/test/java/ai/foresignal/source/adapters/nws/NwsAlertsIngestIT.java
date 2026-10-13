package ai.foresignal.source.adapters.nws;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code nws-alerts} end to end (prompt 047): the synthetic edge-case response through the real fetch
 * path and ingest writer: the time base and expiry, an absent alert tombstoned, an identical re-fetch
 * that only touches.
 */
class NwsAlertsIngestIT extends WeatherIngestSupport {

    static final String ID = NwsAlertsAdapter.ID;
    static final Instant NOW = Instant.parse("2026-10-13T00:01:00Z");

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW);
        configure(ID, false, Map.of());
    }

    static void serve(byte[] body) {
        UPSTREAM.stubFor(get(urlPathEqualTo("/alerts/active")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/geo+json").withBody(body)));
    }

    static byte[] edgeCases() {
        return AdapterFixtures.bytes("nws", "edge-cases.json");
    }

    static byte[] without(String id) {
        ObjectNode root = (ObjectNode) JsonMapper.builder().build().readTree(edgeCases());
        ArrayNode features = (ArrayNode) root.get("features");
        for (int i = features.size() - 1; i >= 0; i--) {
            if (id.equals(features.get(i).path("properties").path("id").asString())) {
                features.remove(i);
            }
        }
        return root.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("the first fetch writes every alert with eventTime = onset, validFrom = onset and validUntil = expires/ends")
    void timeBaseAndExpiry() {
        serve(edgeCases());

        FetchOutcome outcome = fetch(ID);

        assertThat(outcome.success()).isTrue();
        assertThat(outcome.rejected()).isZero();
        assertThat(versions(ID, PersistReason.FIRST)).isEqualTo(9);
        assertThat(head("event_time", ID, "urn:oid:test.polygon.1")).isEqualTo(Instant.parse("2026-10-12T15:35:00Z"));
        assertThat(live("observed_at", ID, "urn:oid:test.polygon.1")).isEqualTo(Instant.parse("2026-10-12T15:35:00Z"));
        assertThat(live("valid_until", ID, "urn:oid:test.polygon.1")).isEqualTo(Instant.parse("2026-10-13T06:00:00Z"));
        assertThat(live("valid_until", ID, "urn:oid:test.ends.1")).isEqualTo(Instant.parse("2026-10-13T22:00:00Z"));
        Timestamp validFrom = jdbc.queryForObject(
                "select valid_from from record_version where source_id = ? and external_id = ?", Timestamp.class, ID,
                "urn:oid:test.polygon.1");
        assertThat(validFrom.toInstant()).isEqualTo(Instant.parse("2026-10-12T15:35:00Z"));
        // An alert whose expiry has passed is stored but already expired: validUntil, not a tombstone, retires it.
        assertThat(live("valid_until", ID, "urn:oid:test.inverted.1")).isBefore(NOW);
        assertThat(tombstoned(ID, "urn:oid:test.inverted.1")).isFalse();
        String title = jdbc.queryForObject("select title from record_head where source_id = ? and external_id = ?",
                String.class, ID, "urn:oid:test.polygon.1");
        assertThat(title).startsWith("Flood Warning issued October 12");
    }

    @Test
    @DisplayName("an alert absent from the next complete response becomes a TOMBSTONE version")
    void absentTombstoned() {
        serve(edgeCases());
        fetch(ID);

        clock.advance(Duration.ofMinutes(1));
        UPSTREAM.resetAll();
        serve(without("urn:oid:test.polygon.1"));
        FetchOutcome outcome = fetch(ID);

        assertThat(outcome.success()).isTrue();
        assertThat(outcome.byReason().get(PersistReason.TOMBSTONE)).isEqualTo(1);
        assertThat(tombstoned(ID, "urn:oid:test.polygon.1")).isTrue();
        assertThat(tombstoned(ID, "urn:oid:test.severe.1")).isFalse();
    }

    @Test
    @DisplayName("an identical re-fetch writes no versions, only touches")
    void refetchTouches() {
        serve(edgeCases());
        fetch(ID);
        int before = versions(ID, (PersistReason) null);

        clock.advance(Duration.ofMinutes(1));
        FetchOutcome outcome = fetch(ID);

        assertThat(outcome.persisted()).isZero();
        assertThat(outcome.touched() + outcome.touchesThrottled()).isEqualTo(9);
        assertThat(versions(ID, (PersistReason) null)).isEqualTo(before);
    }
}
