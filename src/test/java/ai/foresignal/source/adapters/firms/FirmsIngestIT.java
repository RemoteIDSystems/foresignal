package ai.foresignal.source.adapters.firms;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.retention.RetentionResolver;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * {@code nasa-firms} end to end through the real fetch path and ingest writer (prompt 065): FIRST versions, the
 * overlap re-emit writing no versions, the 24 h expiry anchored on the acquisition, the 7 d retention, and no
 * enable without a MAP_KEY.
 */
class FirmsIngestIT extends WeatherIngestSupport {

    static final String ID = FirmsAdapter.ID;
    static final String SOURCE = "VIIRS_NOAA20_NRT";
    static final String KEY = "a1b2c3d4e5f60718293a4b5c6d7e8f90";

    @Autowired private RetentionResolver retention;

    @BeforeEach
    void setUp() {
        clock.setInstant(Instant.parse("2026-10-13T12:00:00Z"));
        sources.setCredentials(ID, Map.of(FirmsAdapter.MAP_KEY, KEY));
        configure(ID, false, Map.of(FirmsAdapter.SOURCES_PARAM, List.of(SOURCE)));
    }

    @AfterEach
    void clearCredentials() {
        sources.deleteCredentials(ID);
    }

    private static void serveViirs() {
        UPSTREAM.stubFor(get(urlPathMatching("/api/area/csv/" + KEY + "/" + SOURCE + "/world/1"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/plain;charset=UTF-8")
                        .withBody(AdapterFixtures.text(ID, "viirs.csv"))));
    }

    @Test
    @DisplayName("first fetch: FIRST versions; the overlap re-emit: no versions; valid_until = observed_at + 24 h")
    void lifecycle() {
        serveViirs();

        FetchOutcome first = fetch(ID);

        assertThat(first.success()).isTrue();
        assertThat(versions(ID, PersistReason.FIRST)).isEqualTo(50);
        String id = FirmsCsvParser.externalId(SOURCE, "2026-10-13", "5", 34.95386, -107.60109);
        Instant acquired = Instant.parse("2026-10-13T00:05:00Z");
        assertThat(live("observed_at", ID, id)).isEqualTo(acquired);
        assertThat(head("event_time", ID, id)).isEqualTo(acquired);
        assertThat(live("valid_until", ID, id)).isEqualTo(acquired.plus(Duration.ofHours(24)));

        clock.advance(Duration.ofMinutes(30));
        FetchOutcome second = fetch(ID);

        assertThat(second.success()).isTrue();
        assertThat(versions(ID, (PersistReason) null)).isEqualTo(50);
        String late = FirmsCsvParser.externalId(SOURCE, "2026-10-13", "1131", 26.04268, -120.60826);
        assertThat(versions(ID, late)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select adapter_state::text from source_health where source_id = ?",
                String.class, ID)).contains("hw:" + SOURCE).doesNotContain(KEY);
    }

    @Test
    @DisplayName("fire-detection retention resolves to 7 days")
    void retention() {
        assertThat(retention.window(ID, FirmsAdapter.KIND)).isEqualTo(Duration.ofDays(7));
    }

    @Test
    @DisplayName("the source cannot be enabled without a MAP_KEY, and nothing is requested")
    void enableRefused() {
        sources.deleteCredentials(ID);
        assertThatThrownBy(() -> sources.update(ID, SourceSettingsUpdate.enabled(true)))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertThat(((ConflictException) e).getCode())
                        .isEqualTo(ConflictException.CREDENTIALS_REQUIRED))
                .hasMessageContaining(FirmsAdapter.MAP_KEY);
        assertThat(fetch(ID).success()).isFalse();
        UPSTREAM.verify(0, anyRequestedFor(anyUrl()));
    }
}
