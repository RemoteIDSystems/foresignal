package ai.foresignal.source.adapters.hms;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code smoke-polygon} from {@code hms-smoke} end to end (prompt 048): 67 polygons with Start/End as
 * eventTime/observedAt and a 24-hour TTL anchored on the End, a 304 re-fetch, and a polygon the analysts
 * removed tombstoned from the next complete file.
 */
class SmokePolygonIngestIT extends WeatherIngestSupport {

    static final String ID = HmsSmokeAdapter.ID;
    static final String PATH = "/pub/FIRE/web/HMS/Smoke_Polygons/KML/2026/10/hms_smoke20261012.kml";

    static void stub(String etag, byte[] body) {
        UPSTREAM.stubFor(get(urlPathEqualTo(PATH)).withHeader("If-None-Match", absent()).willReturn(aResponse()
                .withHeader("ETag", etag).withBody(body)));
        UPSTREAM.stubFor(get(urlPathEqualTo(PATH)).withHeader("If-None-Match", equalTo(etag)).atPriority(1)
                .willReturn(aResponse().withStatus(304).withHeader("ETag", etag)));
    }

    @BeforeEach
    void setUp() {
        clock.setInstant(Instant.parse("2026-10-12T18:00:00Z"));
        configure(ID, false, Map.of());
    }

    @Test
    @DisplayName("67 polygons with the HMS time base; a 304 changes nothing; an edited file tombstones the removed one")
    void lifecycle() {
        byte[] kml = AdapterFixtures.bytes("hms", "hms_smoke20261012.kml");
        stub("\"v1\"", kml);
        FetchOutcome first = fetch(ID);
        assertThat(first.success()).isTrue();
        assertThat(versions(ID, PersistReason.FIRST)).isEqualTo(67);

        HmsKmlParser.Result parsed = HmsKmlParser.parse(kml, LocalDate.of(2026, 10, 12));
        String id = parsed.ids().get(0);
        assertThat(head("event_time", ID, id)).isEqualTo(Instant.parse("2026-10-12T12:00:00Z"));
        assertThat(live("observed_at", ID, id)).isEqualTo(Instant.parse("2026-10-12T15:00:00Z"));
        assertThat(live("valid_until", ID, id)).isEqualTo(Instant.parse("2026-10-12T15:00:00Z").plus(Duration.ofHours(24)));

        clock.advance(Duration.ofMinutes(30));
        FetchOutcome second = fetch(ID);
        assertThat(second.status()).isEqualTo(FetchStatus.NOT_MODIFIED);
        assertThat(versions(ID, (PersistReason) null)).isEqualTo(67);

        String text = new String(kml, StandardCharsets.UTF_8);
        int start = text.indexOf("<Placemark>");
        int end = text.indexOf("</Placemark>", start) + "</Placemark>".length();
        UPSTREAM.resetAll();
        stub("\"v2\"", (text.substring(0, start) + text.substring(end)).getBytes(StandardCharsets.UTF_8));
        clock.advance(Duration.ofMinutes(30));
        FetchOutcome third = fetch(ID);

        assertThat(third.success()).isTrue();
        assertThat(versions(ID, PersistReason.TOMBSTONE)).isEqualTo(1);
        assertThat(tombstoned(ID, id)).isTrue();
        assertThat(tombstoned(ID, parsed.ids().get(1))).isFalse();
    }
}
