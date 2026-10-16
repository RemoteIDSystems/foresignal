package ai.foresignal.source.adapters.wikistream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/** {@code wiki-edit-surge} against a stubbed SSE stream (prompt 095). */
@IntegrationTest
class WikiStreamAdapterTest extends WireMockSupport {

    private static final Instant NOON = Instant.parse("2026-10-13T12:00:00Z");
    private static final String PATH = "/v2/stream/recentchange";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private AdapterHttpHarness harness;
    private WikiStreamAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOON),
                ai.foresignal.source.spi.Budget.of(100, 10_000));
        harness.reset(WikiStreamAdapter.ID, WikiStreamAdapter.LAST_EVENT_ID_KEY);
        adapter = new WikiStreamAdapter(baseUrl() + PATH);
    }

    private WikiTestContext context() {
        return new WikiTestContext(adapter.descriptor(),
                harness.client.forSource(WikiStreamAdapter.ID, adapter.descriptor()), new MutableClock(NOON),
                ValidatedParams.empty());
    }

    @Test
    @DisplayName("fifteen edits by fifteen editors emit one rising surge record")
    void surgeEmitsRecord() throws Exception {
        serve(sseBody(15, "http://localhost/wiki/Example", "enwiki"));
        List<NormalizedRecord> received = Collections.synchronizedList(new ArrayList<>());
        RecordSink sink = received::add;

        StreamHandle handle = adapter.start(context(), sink);
        try {
            await(() -> !received.isEmpty());
        } finally {
            handle.close();
        }
        assertThat(received).isNotEmpty();
        NormalizedRecord record = received.get(0);
        assertThat(record.kind()).isEqualTo(WikiStreamAdapter.KIND);
        assertThat(record.externalId()).isEqualTo("enwiki:Example");
        assertThat(record.payload().get("phase")).isEqualTo(SurgeDetector.RISING);
        assertThat(record.payload().get("edits15m")).isEqualTo(15);
        assertThat(record.payload()).doesNotContainKeys("user", "comment", "parsedcomment");
    }

    @Test
    @DisplayName("Last-Event-ID is sent on reconnect")
    void lastEventIdOnReconnect() throws Exception {
        String id = "[{\"topic\":\"eqiad.mediawiki.recentchange\",\"partition\":0,\"offset\":7}]";
        serve("id: " + id + "\ndata: " + event("2026-09-01T12:00:00Z", "enwiki", "http://localhost/wiki/X") + "\n\n");
        StreamHandle handle = adapter.start(context(), record -> {});
        try {
            await(() -> WIREMOCK.getAllServeEvents().size() >= 2);
        } finally {
            handle.close();
        }
        assertThat(WIREMOCK.getAllServeEvents()).anySatisfy(event ->
                assertThat(event.getRequest().getHeader("Last-Event-ID")).isEqualTo(id));
    }

    @Test
    @DisplayName("the last event id is persisted when a connection ends, not once per event")
    void eventIdPersistedPeriodically() throws Exception {
        serve(sseBody(15, "http://localhost/wiki/Example", "enwiki"));
        WikiTestContext ctx = context();
        String lastId = "[{\"topic\":\"t\",\"partition\":0,\"offset\":14}]";

        StreamHandle handle = adapter.start(ctx, record -> {});
        try {
            await(() -> lastId.equals(ctx.stateValue(WikiStreamAdapter.LAST_EVENT_ID_KEY)));
        } finally {
            handle.close();
        }

        assertThat(ctx.stateValue(WikiStreamAdapter.LAST_EVENT_ID_KEY)).isEqualTo(lastId);
        assertThat(ctx.statePuts()).isLessThan(15);
    }

    @Test
    @DisplayName("a 429 waits thirty seconds, and the ladder is 1 s, 5 s, 30 s, 120 s")
    void throttleAndLadder() {
        assertThat(adapter.delayFor(0, new UpstreamHttpException(429, "slow down")))
                .isEqualTo(Duration.ofSeconds(30));
        assertThat(adapter.delayFor(3, new RuntimeException("boom")).toMillis()).isBetween(90_000L, 150_000L);
        long first = adapter.delayFor(0, new RuntimeException("boom")).toMillis();
        assertThat(first).isBetween(700L, 1_300L);
    }

    @Test
    @DisplayName("the geotag query is restricted to *.wikipedia.org")
    void geotagHostAllowlist() {
        assertThat(WikiStreamAdapter.geotagUri("https://en.wikipedia.org", "Example").toString())
                .contains("/w/api.php").contains("prop=coordinates");
        assertThat(WikiStreamAdapter.geotagUri("https://evil.example", "Example")).isNull();
        assertThat(WikiStreamAdapter.geotagUri("https://en.wikipedia.org.evil.com", "Example")).isNull();
    }

    private static void serve(String body) {
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/event-stream").withBody(body)));
    }

    private static String sseBody(int edits, String titleUrl, String wiki) {
        StringBuilder sb = new StringBuilder();
        Instant base = Instant.parse("2026-09-01T12:00:00Z");
        for (int i = 0; i < edits; i++) {
            sb.append("id: [{\"topic\":\"t\",\"partition\":0,\"offset\":").append(i).append("}]\n")
                    .append("data: ").append(event(base.plusSeconds(i * 30L).toString(), wiki, titleUrl)
                            .replace("\"user\":\"editor0\"", "\"user\":\"editor" + i + "\""))
                    .append("\n\n");
        }
        return sb.toString();
    }

    private static String event(String dt, String wiki, String titleUrl) {
        return "{\"meta\":{\"dt\":\"" + dt + "\",\"domain\":\"" + wiki.replace("wiki", ".wikipedia.org")
                + "\",\"id\":\"x\"},\"type\":\"edit\",\"namespace\":0,\"title\":\"Example\",\"title_url\":\""
                + titleUrl + "\",\"user\":\"editor0\",\"bot\":false,\"wiki\":\"" + wiki + "\"}";
    }

    private static void await(java.util.function.BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + Duration.ofSeconds(4).toNanos();
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(25);
        }
    }
}
