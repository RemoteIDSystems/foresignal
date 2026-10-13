package ai.foresignal.source.adapters.gdelt;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.retention.RetentionResolver;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * {@code gdelt-events} end to end through the real fetch path and ingest writer (prompt 066): the first file writes
 * FIRST versions with {@code eventTime} at the start of {@code Day}; the same file again requests only
 * {@code lastupdate.txt} and writes nothing; records expire 24 h after receipt; retention is 90 days.
 */
class GdeltIngestIT extends WeatherIngestSupport {

    static final String ID = GdeltEventsAdapter.ID;
    static final String FILE = "20261013143000.export.CSV.zip";
    static final String LASTUPDATE_PATH = GdeltEventsAdapter.PATH + "lastupdate.txt";
    static final String ZIP_PATH = GdeltEventsAdapter.PATH + FILE;

    @Autowired private UpstreamClient client;
    @Autowired private RetentionResolver retention;

    @BeforeEach
    void setUp() {
        clock.setInstant(Instant.parse("2026-10-13T14:35:00Z"));
        AdapterStateStore state = client.stateStore(ID);
        state.remove(GdeltEventsAdapter.LAST_FILE);
        state.remove(GdeltEventsAdapter.PENDING_FILE);
        SourceHttp.forgetValidators(state, GdeltEventsAdapter.LASTUPDATE_STATE_KEY);
        configure(ID, false, Map.of());
    }

    private static void serve() {
        UPSTREAM.stubFor(get(urlEqualTo(LASTUPDATE_PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/plain").withBody(AdapterFixtures.text(ID, "lastupdate.txt"))));
        UPSTREAM.stubFor(get(urlEqualTo(ZIP_PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/zip").withBody(AdapterFixtures.bytes(ID, FILE))));
    }

    @Test
    @DisplayName("first file: FIRST versions, eventTime = start of Day; the same file again: no ZIP, no versions")
    void lifecycle() {
        serve();
        int before = versions(ID, (PersistReason) null);
        int firstBefore = versions(ID, PersistReason.FIRST);

        FetchOutcome first = fetch(ID);

        assertThat(first.success()).isTrue();
        assertThat(versions(ID, PersistReason.FIRST) - firstBefore).isEqualTo(183);
        String id = "1327721728";
        assertThat(versions(ID, id)).isEqualTo(1);
        assertThat(head("event_time", ID, id)).isEqualTo(Instant.parse("2025-10-13T00:00:00Z"));
        assertThat(live("observed_at", ID, id)).isEqualTo(Instant.parse("2026-10-13T14:30:00Z"));
        Instant received = live("last_received_at", ID, id);
        assertThat(received).isNotNull();
        assertThat(live("valid_until", ID, id)).as("records expire 24 h after receipt")
                .isEqualTo(received.plus(Duration.ofHours(24)));

        clock.advance(Duration.ofMinutes(15));
        FetchOutcome second = fetch(ID);

        assertThat(second.success()).isTrue();
        assertThat(versions(ID, (PersistReason) null) - before).isEqualTo(183);
        UPSTREAM.verify(1, getRequestedFor(urlEqualTo(ZIP_PATH)));
        UPSTREAM.verify(2, getRequestedFor(urlEqualTo(LASTUPDATE_PATH)));
        UPSTREAM.verify(3, anyRequestedFor(anyUrl()));
        assertThat(client.stateStore(ID).get(GdeltEventsAdapter.LAST_FILE)).contains(FILE);
    }

    @Test
    @DisplayName("conflict-event retention of gdelt-events resolves to 90 days")
    void retention() {
        assertThat(retention.window(ID, GdeltEventsAdapter.KIND)).isEqualTo(Duration.ofDays(90));
    }
}
