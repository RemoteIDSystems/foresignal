package ai.foresignal.source.adapters.weather;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.collect.HealthService;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.adapters.mrms.ProbSevereAdapter;
import ai.foresignal.source.adapters.nws.NwsAlertsAdapter;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.raster.ProductAdapterHarness;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import ai.foresignal.trigger.TriggerEngine;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.json.JsonMapper;

/**
 * Unknown is not clear (prompt 047; Weather-Imagery-Design §7.1): when the NWS or ProbSevere feed
 * fails, nothing is tombstoned, no exit alert fires, and the source shows {@code degraded}.
 */
class StaleNeverClearsIT extends WeatherIngestSupport {

    @Autowired TriggerRuleService rules;
    @Autowired TriggerEngine engine;
    @Autowired HealthService health;

    private UUID exitRule(String kind) {
        return rules.create(JsonMapper.builder().build().readTree(
                "{\"name\":\"" + kind + " cleared\",\"kinds\":[\"" + kind + "\"],\"conditions\":[{\"path\":\"automated\","
                        + "\"op\":\"exists\",\"value\":true}],\"mode\":\"exit\"}")).rule().getId();
    }

    private UUID exitRuleOnEvent() {
        return rules.create(JsonMapper.builder().build().readTree(
                "{\"name\":\"alert cleared\",\"kinds\":[\"nws-alert\"],\"conditions\":[{\"path\":\"event\","
                        + "\"op\":\"exists\",\"value\":true}],\"mode\":\"exit\"}")).rule().getId();
    }

    private int alerts(UUID rule) {
        engine.catchUp();
        Integer count = jdbc.queryForObject("select count(*) from alert where rule_id = ?", Integer.class, rule);
        return count == null ? 0 : count;
    }

    @Test
    @DisplayName("NWS answers 500: no tombstones, no exit alerts, health degraded")
    void nwsDown() {
        clock.setInstant(Instant.parse("2026-10-13T00:01:00Z"));
        configure(NwsAlertsAdapter.ID, true, Map.of());
        UUID rule = exitRuleOnEvent();
        UPSTREAM.stubFor(get(urlPathEqualTo("/alerts/active")).willReturn(aResponse().withStatus(200)
                .withBody(AdapterFixtures.bytes("nws", "edge-cases.json"))));
        assertThat(fetch(NwsAlertsAdapter.ID).success()).isTrue();
        int versions = versions(NwsAlertsAdapter.ID, (PersistReason) null);

        clock.advance(Duration.ofMinutes(1));
        UPSTREAM.resetAll();
        UPSTREAM.stubFor(get(urlPathEqualTo("/alerts/active")).willReturn(aResponse().withStatus(500)));
        FetchOutcome failed = fetch(NwsAlertsAdapter.ID);

        assertThat(failed.success()).isFalse();
        assertThat(versions(NwsAlertsAdapter.ID, PersistReason.TOMBSTONE)).isZero();
        assertThat(versions(NwsAlertsAdapter.ID, (PersistReason) null)).isEqualTo(versions);
        assertThat(alerts(rule)).isZero();
        assertThat(health.health(NwsAlertsAdapter.ID).status()).isEqualTo(HealthStatus.DEGRADED);

        // A body cut short is no better: still nothing tombstoned.
        UPSTREAM.resetAll();
        UPSTREAM.stubFor(get(urlPathEqualTo("/alerts/active")).willReturn(aResponse().withStatus(200)
                .withBody("{\"type\":\"FeatureCollection\",\"features\":[{\"type\":")));
        assertThat(fetch(NwsAlertsAdapter.ID).success()).isFalse();
        assertThat(versions(NwsAlertsAdapter.ID, PersistReason.TOMBSTONE)).isZero();
        assertThat(alerts(rule)).isZero();
        configure(NwsAlertsAdapter.ID, false, Map.of());
    }

    @Test
    @DisplayName("ProbSevere download fails: no tombstones, no exit alerts, health degraded")
    void probSevereDown() {
        clock.setInstant(Instant.parse("2026-10-12T23:59:50Z"));
        configure(ProbSevereAdapter.ID, true, Map.of());
        UUID rule = exitRule("probsevere-object");
        String bucket = ProbSevereAdapter.BUCKET;
        String prefix = "ProbSevere/20261012/";
        String key = prefix + "MRMS_PROBSEVERE_20261012_235841.json";
        UPSTREAM.stubFor(get(urlPathEqualTo("/" + bucket + "/")).withQueryParam("start-after", absent())
                .willReturn(aResponse().withHeader("Content-Type", "application/xml")
                        .withBody(ProductAdapterHarness.listing(bucket, prefix, Map.of(key, 264_916L), false))));
        UPSTREAM.stubFor(get(urlPathEqualTo("/" + bucket + "/" + key))
                .willReturn(aResponse().withBody(AdapterFixtures.bytes("probsevere", "MRMS_PROBSEVERE_20261012_235841.json"))));
        assertThat(fetch(ProbSevereAdapter.ID).success()).isTrue();
        assertThat(versions(ProbSevereAdapter.ID, PersistReason.FIRST)).isEqualTo(10);

        String next = prefix + "MRMS_PROBSEVERE_20261012_235950.json";
        UPSTREAM.stubFor(get(urlPathEqualTo("/" + bucket + "/")).withQueryParam("start-after", equalTo(key))
                .willReturn(aResponse().withHeader("Content-Type", "application/xml")
                        .withBody(ProductAdapterHarness.listing(bucket, prefix, Map.of(next, 264_916L), false))));
        UPSTREAM.stubFor(get(urlPathEqualTo("/" + bucket + "/" + next)).willReturn(aResponse().withStatus(500)));
        clock.advance(Duration.ofSeconds(5));
        FetchOutcome failed = fetch(ProbSevereAdapter.ID);

        assertThat(failed.success()).isFalse();
        assertThat(versions(ProbSevereAdapter.ID, PersistReason.TOMBSTONE)).isZero();
        assertThat(alerts(rule)).isZero();
        assertThat(health.health(ProbSevereAdapter.ID).status()).isEqualTo(HealthStatus.DEGRADED);
        configure(ProbSevereAdapter.ID, false, Map.of());
    }
}
