package ai.foresignal.source.adapters.awc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code awc-metars} end to end (prompt 047), with the synthetic {@code preamble.csv} and a stations
 * allowlist: a new report is a version, the same report a touch, and the 3-hour TTL anchored on the
 * observation retires an old report.
 */
class MetarIngestIT extends WeatherIngestSupport {

    static final String ID = AwcMetarsAdapter.ID;

    @BeforeEach
    void setUp() {
        clock.setInstant(Instant.parse("2026-10-13T01:00:00Z"));
        configure(ID, false, Map.of(AwcMetarsAdapter.STATIONS_PARAM, List.of("KTST", "KVRB")));
    }

    static void serve(String csv) {
        UPSTREAM.resetAll();
        UPSTREAM.stubFor(get(urlPathEqualTo(AwcMetarsAdapter.PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/octet-stream").withBody(csv.getBytes(StandardCharsets.UTF_8))));
    }

    static String file() {
        return AdapterFixtures.text("awc", "preamble.csv");
    }

    @Test
    @DisplayName("new report -> version; same report -> touch; validUntil = observation + 3 h")
    void versionsTouchesAndTtl() {
        serve(file());
        FetchOutcome first = fetch(ID);
        assertThat(first.success()).isTrue();
        assertThat(versions(ID, PersistReason.FIRST)).isEqualTo(2);
        assertThat(live("observed_at", ID, "KTST")).isEqualTo(Instant.parse("2026-10-13T00:53:00Z"));
        assertThat(live("valid_until", ID, "KTST")).isEqualTo(Instant.parse("2026-10-13T03:53:00Z"));

        clock.advance(Duration.ofMinutes(5));
        FetchOutcome same = fetch(ID);
        assertThat(same.persisted()).isZero();
        assertThat(same.touched() + same.touchesThrottled()).isEqualTo(2);

        clock.advance(Duration.ofMinutes(55));
        serve(file().replace("\"SPECI KTST 130053Z 18015G25KT 10SM OVC003 20/19 A2990\",KTST,2026-10-13T00:53:00.000Z",
                "\"METAR KTST 130153Z 18012KT 10SM OVC005 19/18 A2991\",KTST,2026-10-13T01:53:00.000Z"));
        FetchOutcome next = fetch(ID);
        assertThat(next.persisted()).isEqualTo(1);
        assertThat(versions(ID, "KTST")).isEqualTo(2);
        assertThat(live("valid_until", ID, "KTST")).isEqualTo(Instant.parse("2026-10-13T04:53:00Z"));

        // KVRB stopped changing: three hours after its observation it is no longer current.
        clock.setInstant(Instant.parse("2026-10-13T03:00:01Z"));
        assertThat(live("valid_until", ID, "KVRB")).isBefore(clock.instant());
    }
}
