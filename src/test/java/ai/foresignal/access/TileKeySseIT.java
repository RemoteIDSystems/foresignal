package ai.foresignal.access;

import static ai.foresignal.testsupport.records.RecordsTestSources.SCHEMA_A;
import static ai.foresignal.testsupport.records.RecordsTestSources.SCHEMA_B;
import static ai.foresignal.testsupport.records.RecordsTestSources.SCHEMA_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.sse.SseClient;
import ai.foresignal.stream.sse.SseHub;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.stream.SseTestClient;
import ai.foresignal.testsupport.stream.StreamTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * A browser {@code EventSource} on {@code /v1/k/{key}/changes/stream} over a real socket (prompt 050):
 * with {@code openReads} off, the tile key alone opens the stream, events stay inside the key's
 * restriction, a reconnect with {@code Last-Event-ID} replays exactly the missed events, and revoking
 * the key resets the stream. Every read has a timeout.
 */
@WebIntegrationTest
@Import(StreamTestSources.class)
class TileKeySseIT {

    private static final Instant T0 = Instant.parse("2026-10-12T12:00:00Z");
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @LocalServerPort int port;
    @Autowired MutableClock clock;
    @Autowired AppSettingService settings;
    @Autowired TestRecords records;
    @Autowired SseHub hub;
    @Autowired TileKeyService tileKeys;

    private final List<SseTestClient> streams = new ArrayList<>();

    @BeforeEach
    void setUp() {
        clock.setInstant(T0);
        settings.invalidateCache();
        settings.put(SettingKeys.SECURITY, new SecuritySettings(false));
    }

    @AfterEach
    void tearDown() {
        streams.forEach(SseTestClient::close);
        hub.clients().forEach(SseClient::close);
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
    }

    private String base() {
        return "http://localhost:" + port;
    }

    private SseTestClient open(String key, Map<String, String> headers) {
        SseTestClient client = SseTestClient.connect(base(), "/v1/k/" + key + "/changes/stream", headers);
        streams.add(client);
        return client;
    }

    private void ingest(String source, String id) {
        records.ingestAt(source, T0, NormalizedRecord.builder(id, SCHEMA_KIND).observedAt(T0).point(1, 1).build());
    }

    private String key() {
        return tileKeys.create(JSON.readTree("{\"name\":\"wall-map\",\"prefixes\":[\"/v1/changes/stream\"],"
                + "\"ttl\":\"1d\",\"restrictions\":{\"sources\":[\"" + SCHEMA_A + "\"]}}")).key();
    }

    @Test
    @DisplayName("an EventSource-style GET streams changes inside the key's restriction and resumes with Last-Event-ID")
    void streamsAndResumes() {
        String key = key();
        SseTestClient first = open(key, Map.of());
        assertThat(first.status()).isEqualTo(200);
        assertThat(first.header("Referrer-Policy")).isEqualTo("no-referrer");
        ingest(SCHEMA_B, "b1");
        ingest(SCHEMA_A, "a1");
        ingest(SCHEMA_A, "a2");
        ingest(SCHEMA_A, "a3");
        List<SseTestClient.Event> all = first.collect("change", Duration.ofSeconds(2));
        assertThat(all).hasSize(3);
        assertThat(all).noneSatisfy(e -> assertThat(e.data()).contains("b1"));
        assertThat(hub.clients()).anySatisfy(c -> assertThat(c.principal().id()).startsWith("tilekey:"));
        first.close();

        SseTestClient resumed = open(key, Map.of("Last-Event-ID", all.getFirst().id()));
        assertThat(resumed.status()).isEqualTo(200);
        List<SseTestClient.Event> replayed = resumed.collect("change", Duration.ofSeconds(2));
        assertThat(replayed).extracting(SseTestClient.Event::id).containsExactly(all.get(1).id(), all.get(2).id());
        ingest(SCHEMA_B, "b2");
        ingest(SCHEMA_A, "a4");
        SseTestClient.Event live = resumed.next("change", TIMEOUT);
        assertThat(live).isNotNull();
        assertThat(live.data()).contains("a4").doesNotContain("b2");
    }

    @Test
    @DisplayName("revoking the tile key resets its open stream")
    void revokeResetsTheStream() {
        String key = key();
        SseTestClient client = open(key, Map.of());
        assertThat(client.status()).isEqualTo(200);
        ingest(SCHEMA_A, "a1");
        assertThat(client.next("change", TIMEOUT)).isNotNull();
        java.util.UUID id = tileKeys.list().getFirst().getId();
        tileKeys.revoke(id);
        SseTestClient.Event reset = client.next("reset", TIMEOUT);
        assertThat(reset).isNotNull();
        assertThat(reset.data()).contains("revoked");
        assertThat(client.awaitEnd(TIMEOUT)).isTrue();
        SseTestClient again = open(key, Map.of());
        assertThat(again.status()).isEqualTo(401);
    }
}
