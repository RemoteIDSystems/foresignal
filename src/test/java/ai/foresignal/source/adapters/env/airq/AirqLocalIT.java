package ai.foresignal.source.adapters.env.airq;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * {@code airq-local-1} end to end through the real fetch path and ingest writer (prompt 078), against the shared
 * stub server reached as {@code 127.0.0.1}: a loopback {@code LAN_URL} is accepted and a public one refused at
 * PUT; under the SAMPLED policy a 2 µg/m³ PM2.5 change creates no version and a 6 µg/m³ change a SAMPLE version;
 * the source is private and {@code LOCAL_ONLY}. Also: every prompt 078 source registers disabled.
 */
class AirqLocalIT extends WeatherIngestSupport {

    static final String SLOT = "airq-local-1";
    static final String ENTITY = "airgradient:it0000000001";

    @Autowired private EgressPolicy egress;

    @BeforeEach
    void setUp() {
        clock.setInstant(Instant.parse("2026-10-13T22:00:00Z"));
        backoff.onSuccess(SLOT);
    }

    private String loopbackUrl() {
        return "http://127.0.0.1:" + UPSTREAM.port();
    }

    private void configureSlot(String url) {
        configure(SLOT, false, Map.of(AirqLocalAdapter.DEVICE_TYPE_PARAM, AirqLocalAdapter.AIRGRADIENT,
                AirqLocalAdapter.URL_PARAM, url, AirqLocalAdapter.LAT_PARAM, 40.0, AirqLocalAdapter.LON_PARAM, -105.0));
    }

    private void serve(double pm25) {
        String body = "{\"serialno\": \"it0000000001\", \"model\": \"I-9PSL\", \"firmware\": \"3.1.3\", \"pm01\": 2,"
                + " \"pm02\": " + pm25 + ", \"pm10\": 12, \"rco2\": 450, \"atmp\": 21.5, \"rhum\": 40, \"tvocIndex\": 100,"
                + " \"noxIndex\": 1}";
        UPSTREAM.stubFor(get(urlPathEqualTo("/measures/current")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(body.getBytes(StandardCharsets.UTF_8))));
    }

    private List<Integer> reasons() {
        return jdbc.queryForList("select persist_reason from record_version where source_id = ? and external_id = ?"
                + " order by seq", Integer.class, SLOT, ENTITY);
    }

    @Test
    @DisplayName("every 078 source is registered and disabled")
    void registered() {
        List<String> ids = List.of("openaq", "safecast-radiation", "epa-radnet", "ndbc-buoys", "sensor-community",
                "airnow-hourly", "airq-local-1", "airq-local-2");
        assertThat(sources.list()).filteredOn(s -> ids.contains(s.id())).hasSize(ids.size())
                .allSatisfy(s -> assertThat(s.enabled()).as(s.id()).isFalse());
    }

    @Test
    @DisplayName("LAN_URL: a loopback address is accepted, a public address refused")
    void lanUrl() {
        configureSlot(loopbackUrl());
        assertThatThrownBy(() -> configureSlot("http://8.8.8.8")).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("SAMPLED: +2 ug/m3 after 61 s is no version; +6 against the last version after 122 s is a SAMPLE")
    void sampling() {
        configureSlot(loopbackUrl());
        int before = reasons().size();
        serve(10.0);
        assertThat(fetch(SLOT).success()).isTrue();
        assertThat(reasons()).hasSize(before + 1);

        clock.advance(Duration.ofSeconds(61));
        serve(12.0);
        assertThat(fetch(SLOT).success()).isTrue();
        assertThat(reasons()).as("a 2 ug/m3 change is under the 5 ug/m3 threshold").hasSize(before + 1);

        clock.advance(Duration.ofSeconds(61));
        serve(16.0);
        assertThat(fetch(SLOT).success()).isTrue();
        List<Integer> reasons = reasons();
        assertThat(reasons).hasSize(before + 2);
        assertThat(reasons.getLast()).isEqualTo((int) PersistReason.SAMPLE.code());
        assertThat(jdbc.queryForObject("select kind from record_head where source_id = ? and external_id = ?",
                String.class, SLOT, ENTITY)).isEqualTo("indoor-air-quality");
    }

    @Test
    @DisplayName("the slot is private (hidden from anonymous readers, never embedded or federated) and LOCAL_ONLY")
    void privateAndLocal() {
        assertThat(egress.isPrivate(SLOT)).isTrue();
        assertThat(egress.hiddenSources(Viewer.anonymous())).contains(SLOT);
        assertThat(egress.visible(SLOT, Viewer.anonymous())).isFalse();
        assertThat(egress.allowsEmbedding(SLOT)).isFalse();
        assertThat(egress.allows(EgressPath.FEDERATION, SLOT, Viewer.admin())).isFalse();
        assertThat(egress.isLocalOnly(SLOT)).isTrue();
        assertThat(egress.allows(SLOT, EgressPath.SINK)).isFalse();
    }
}
