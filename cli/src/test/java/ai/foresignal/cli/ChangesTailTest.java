package ai.foresignal.cli;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code foresignal changes tail} against a WireMock SSE server: resume with {@code Last-Event-ID}, {@code reset}
 * (snapshot and reopen at the watermark, or resume from {@code resumeAfter}), {@code resync}, the
 * {@code observedAt} guard, the heartbeat watchdog, {@code Retry-After}, and giving up.
 */
@Timeout(30)
class ChangesTailTest {

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort()).build();

    private static final String STREAM = "/v1/changes/stream";

    @TempDir
    Path tmp;

    private final JsonMapper json = JsonMapper.builder().build();

    private static String change(long seq, String externalId, String observedAt) {
        return "id: " + seq + "\nevent: change\ndata: {\"seq\":" + seq + ",\"source\":\"usgs\",\"externalId\":\""
                + externalId + "\",\"kind\":\"earthquake\",\"version\":1,\"observedAt\":\"" + observedAt
                + "\",\"tombstone\":false}\n\n";
    }

    private static String event(String name, String data) {
        return "event: " + name + "\ndata: " + data + "\n\n";
    }

    private static ResponseDefinitionBuilder sse(String body) {
        return aResponse().withStatus(200).withHeader("Content-Type", "text/event-stream").withBody(body);
    }

    private CliHarness cli() {
        return CliHarness.against(tmp, wm.baseUrl(), "fsr_t");
    }

    private List<JsonNode> lines(CliHarness cli) {
        return cli.outLines().stream().map(json::readTree).toList();
    }

    @Test
    void resumesWithLastEventIdAfterADisconnect() {
        wm.stubFor(get(urlPathEqualTo(STREAM)).withHeader("Last-Event-ID", absent())
                .willReturn(sse(": hello\n\n" + change(5, "a", "2026-10-18T10:00:00Z"))));
        wm.stubFor(get(urlPathEqualTo(STREAM)).withHeader("Last-Event-ID", equalTo("5"))
                .willReturn(sse(change(6, "b", "2026-10-18T10:00:01Z"))));
        CliHarness cli = cli();

        int code = cli.run("changes", "tail", "--source", "usgs", "--max-events", "2", "-o", "ndjson");

        assertEquals(ExitCodes.OK, code, cli.err());
        List<JsonNode> lines = lines(cli);
        assertEquals(2, lines.size());
        assertEquals("change", lines.get(0).path("event").asString());
        assertEquals("5", lines.get(0).path("id").asString());
        assertEquals("6", lines.get(1).path("id").asString());
        assertEquals("b", lines.get(1).path("data").path("externalId").asString());
        wm.verify(getRequestedFor(urlPathEqualTo(STREAM)).withHeader("Last-Event-ID", equalTo("5"))
                .withQueryParam("after", equalTo("5")).withQueryParam("source", equalTo("usgs"))
                .withQueryParam("live", equalTo("false")));
        assertEquals(1, cli.sleeps().size(), "one backoff after the dropped connection");
        Duration first = cli.sleeps().get(0);
        assertTrue(first.toMillis() >= 800 && first.toMillis() <= 1200, "1 s +-20%: " + first);
        assertTrue(cli.err().contains("reconnecting"), cli.err());
    }

    @Test
    void resetWithoutResumeAfterPagesTheSnapshotAndReopensAtTheWatermark() {
        wm.stubFor(get(urlPathEqualTo(STREAM)).withHeader("Last-Event-ID", absent())
                .willReturn(sse(event("reset", "{\"reason\":\"resync_required\",\"resumeAfter\":null,"
                        + "\"watermarkSeq\":90}"))));
        wm.stubFor(get(urlPathEqualTo("/v1/records")).withQueryParam("order", equalTo("id"))
                .withQueryParam("cursor", absent())
                .willReturn(okJson("{\"items\":[{\"source\":\"usgs\",\"externalId\":\"r1\",\"observedAt\":"
                        + "\"2026-10-18T09:00:00Z\"},{\"source\":\"usgs\",\"externalId\":\"r2\",\"observedAt\":"
                        + "\"2026-10-18T09:00:01Z\"}],\"nextCursor\":\"c2\",\"watermarkSeq\":100}")));
        wm.stubFor(get(urlPathEqualTo("/v1/records")).withQueryParam("cursor", equalTo("c2"))
                .willReturn(okJson("{\"items\":[{\"source\":\"usgs\",\"externalId\":\"r3\",\"observedAt\":"
                        + "\"2026-10-18T09:00:02Z\"}],\"nextCursor\":null,\"watermarkSeq\":100}")));
        wm.stubFor(get(urlPathEqualTo(STREAM)).withHeader("Last-Event-ID", equalTo("100"))
                .willReturn(sse(change(101, "r4", "2026-10-18T10:00:00Z"))));
        CliHarness cli = cli();

        int code = cli.run("changes", "tail", "--source", "usgs", "--kind", "earthquake", "--max-events", "4",
                "-o", "json");

        assertEquals(ExitCodes.OK, code, cli.err());
        List<JsonNode> lines = lines(cli);
        assertEquals(4, lines.size(), cli.out());
        assertEquals("snapshot", lines.get(0).path("event").asString());
        assertEquals("r1", lines.get(0).path("data").path("externalId").asString());
        assertEquals("r3", lines.get(2).path("data").path("externalId").asString());
        assertEquals("change", lines.get(3).path("event").asString());
        assertEquals("101", lines.get(3).path("id").asString());
        wm.verify(getRequestedFor(urlPathEqualTo("/v1/records")).withQueryParam("order", equalTo("id"))
                .withQueryParam("source", equalTo("usgs")).withQueryParam("kind", equalTo("earthquake")));
        wm.verify(getRequestedFor(urlPathEqualTo(STREAM)).withHeader("Last-Event-ID", equalTo("100"))
                .withQueryParam("after", equalTo("100")));
        assertTrue(cli.sleeps().isEmpty(), "a reset reconnects at once: " + cli.sleeps());
        assertTrue(cli.err().contains("reset (resync_required)"), cli.err());
        assertTrue(cli.err().contains("watermarkSeq 100"), cli.err());
    }

    @Test
    void resetWithResumeAfterResumesWithoutASnapshot() {
        wm.stubFor(get(urlPathEqualTo(STREAM)).withHeader("Last-Event-ID", absent())
                .willReturn(sse(change(40, "a", "2026-10-18T10:00:00Z")
                        + event("reset", "{\"reason\":\"buffer_overflow\",\"resumeAfter\":42,\"watermarkSeq\":50}"))));
        wm.stubFor(get(urlPathEqualTo(STREAM)).withHeader("Last-Event-ID", equalTo("42"))
                .willReturn(sse(change(43, "b", "2026-10-18T10:00:01Z"))));
        CliHarness cli = cli();

        assertEquals(ExitCodes.OK, cli.run("changes", "tail", "--max-events", "2", "-o", "ndjson"), cli.err());

        List<JsonNode> lines = lines(cli);
        assertEquals("40", lines.get(0).path("id").asString());
        assertEquals("43", lines.get(1).path("id").asString());
        wm.verify(0, getRequestedFor(urlPathEqualTo("/v1/records")));
        assertTrue(cli.sleeps().isEmpty());
        assertTrue(cli.err().contains("resuming after 42"), cli.err());
    }

    @Test
    void resyncRefetchesTheCurrentStateAndKeepsTheStreamOpen() {
        wm.stubFor(get(urlPathEqualTo(STREAM)).willReturn(sse(change(7, "a", "2026-10-18T10:00:00Z")
                + event("resync", "{\"reason\":\"gap\",\"watermarkSeq\":7}")
                + change(8, "b", "2026-10-18T10:00:01Z"))));
        wm.stubFor(get(urlPathEqualTo("/v1/records")).willReturn(okJson(
                "{\"items\":[{\"source\":\"usgs\",\"externalId\":\"a\",\"observedAt\":\"2026-10-18T10:00:00Z\"}],"
                        + "\"nextCursor\":null,\"watermarkSeq\":7}")));
        CliHarness cli = cli();

        assertEquals(ExitCodes.OK, cli.run("changes", "tail", "--live", "--max-events", "3", "-o", "ndjson"),
                cli.err());

        List<JsonNode> lines = lines(cli);
        assertEquals(List.of("change", "snapshot", "change"),
                lines.stream().map(l -> l.path("event").asString()).toList());
        wm.verify(1, getRequestedFor(urlPathEqualTo(STREAM)));
        assertTrue(cli.err().contains("resync (gap)"), cli.err());
    }

    @Test
    void liveEventsOlderThanTheStoredObservedAtAreNotApplied() {
        wm.stubFor(get(urlPathEqualTo(STREAM)).willReturn(sse(change(1, "ship-1", "2026-10-18T10:00:05Z")
                + event("live", "{\"sourceId\":\"usgs\",\"externalId\":\"ship-1\",\"kind\":\"vessel\","
                        + "\"observedAt\":\"2026-10-18T10:00:01Z\",\"version\":1}")
                + event("heartbeat", "{}")
                + event("live", "{\"sourceId\":\"usgs\",\"externalId\":\"ship-1\",\"kind\":\"vessel\","
                        + "\"observedAt\":\"2026-10-18T10:00:09Z\",\"version\":1}"))));
        CliHarness cli = cli();

        assertEquals(ExitCodes.OK, cli.run("changes", "tail", "--live", "--max-events", "2", "-o", "ndjson"),
                cli.err());

        List<JsonNode> lines = lines(cli);
        assertEquals(2, lines.size());
        assertEquals("live", lines.get(1).path("event").asString());
        assertEquals("2026-10-18T10:00:09Z", lines.get(1).path("data").path("observedAt").asString());
        assertTrue(lines.get(1).path("id").isMissingNode(), "live events carry no id");
        wm.verify(getRequestedFor(urlPathEqualTo(STREAM)).withQueryParam("live", equalTo("true")));
    }

    @Test
    void theWatchdogReconnectsASilentStream() {
        StringBuilder padding = new StringBuilder(event("heartbeat", "{}"));
        for (int i = 0; i < 200; i++) {
            padding.append(": keep-alive padding\n");
        }
        wm.stubFor(get(urlPathEqualTo(STREAM)).inScenario("silent").whenScenarioStateIs(Scenario.STARTED)
                .willReturn(sse(padding.toString()).withChunkedDribbleDelay(2, 6000))
                .willSetStateTo("second"));
        wm.stubFor(get(urlPathEqualTo(STREAM)).inScenario("silent").whenScenarioStateIs("second")
                .willReturn(sse(change(9, "a", "2026-10-18T10:00:00Z"))));
        CliHarness cli = cli();

        int code = cli.run("changes", "tail", "--heartbeat-seconds", "0.2", "--max-events", "1", "-o", "ndjson");

        assertEquals(ExitCodes.OK, code, cli.err());
        assertTrue(cli.err().contains("no event or heartbeat within 500 ms"), cli.err());
        assertEquals(1, cli.sleeps().size());
        assertEquals("9", lines(cli).get(0).path("id").asString());
    }

    @Test
    void retryAfterIsHonoredAsTheReconnectFloor() {
        wm.stubFor(get(urlPathEqualTo(STREAM)).inScenario("limited").whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(429).withHeader("Retry-After", "7")
                        .withHeader("Content-Type", "application/problem+json")
                        .withBody("{\"title\":\"Too many streams\",\"status\":429,\"code\":\"STREAM_LIMIT\"}"))
                .willSetStateTo("ok"));
        wm.stubFor(get(urlPathEqualTo(STREAM)).inScenario("limited").whenScenarioStateIs("ok")
                .willReturn(sse(change(3, "a", "2026-10-18T10:00:00Z"))));
        CliHarness cli = cli();

        assertEquals(ExitCodes.OK, cli.run("changes", "tail", "--max-events", "1"), cli.err());

        assertEquals(Duration.ofSeconds(7), cli.sleeps().get(0));
        assertTrue(cli.out().startsWith("change  3  usgs/a  earthquake  v1"), cli.out());
    }

    @Test
    void theServerRetryFieldIsAFloorToo() {
        wm.stubFor(get(urlPathEqualTo(STREAM)).withHeader("Last-Event-ID", absent())
                .willReturn(sse("retry: 15000\n\n" + change(1, "a", "2026-10-18T10:00:00Z"))));
        wm.stubFor(get(urlPathEqualTo(STREAM)).withHeader("Last-Event-ID", equalTo("1"))
                .willReturn(sse(change(2, "b", "2026-10-18T10:00:00Z"))));
        CliHarness cli = cli();

        assertEquals(ExitCodes.OK, cli.run("changes", "tail", "--max-events", "2"), cli.err());

        assertEquals(Duration.ofSeconds(15), cli.sleeps().get(0));
    }

    @Test
    void aRejectedStreamIsAnApiError() {
        wm.stubFor(get(urlPathEqualTo(STREAM)).willReturn(aResponse().withStatus(400)
                .withHeader("Content-Type", "application/problem+json")
                .withBody("{\"title\":\"Bad Request\",\"status\":400,\"code\":\"INVALID_BBOX\"}")));
        CliHarness cli = cli();

        assertEquals(ExitCodes.API_ERROR, cli.run("changes", "tail", "--bbox", "1,2,3"));

        assertTrue(cli.err().contains("INVALID_BBOX"), cli.err());
        assertTrue(cli.sleeps().isEmpty());
    }

    @Test
    void anUnreachableServerGivesUpAfterMaxRetries() throws IOException {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        CliHarness cli = CliHarness.against(tmp, "http://127.0.0.1:" + port, null);

        assertEquals(ExitCodes.NETWORK, cli.run("changes", "tail", "--max-retries", "2"));

        assertEquals(2, cli.sleeps().size());
        assertTrue(cli.sleeps().get(1).compareTo(cli.sleeps().get(0)) > 0, "exponential: " + cli.sleeps());
    }

    @Test
    void snapshotFirstOpensTheStreamAtTheSnapshotWatermark() {
        wm.stubFor(get(urlPathEqualTo("/v1/records")).willReturn(okJson(
                "{\"items\":[{\"source\":\"usgs\",\"externalId\":\"r1\",\"observedAt\":\"2026-10-18T09:00:00Z\"}],"
                        + "\"nextCursor\":null,\"watermarkSeq\":500}")));
        wm.stubFor(get(urlPathEqualTo(STREAM)).withHeader("Last-Event-ID", equalTo("500"))
                .willReturn(sse(change(501, "r1", "2026-10-18T10:00:00Z"))));
        CliHarness cli = cli();

        assertEquals(ExitCodes.OK, cli.run("changes", "tail", "--snapshot", "--max-events", "2", "-o", "ndjson"),
                cli.err());

        List<JsonNode> lines = lines(cli);
        assertEquals("snapshot", lines.get(0).path("event").asString());
        assertEquals("501", lines.get(1).path("id").asString());
    }

    @Test
    void afterStartsFromTheGivenSeq() {
        wm.stubFor(get(urlPathEqualTo(STREAM)).withHeader("Last-Event-ID", equalTo("77"))
                .willReturn(sse(change(78, "a", "2026-10-18T10:00:00Z"))));
        CliHarness cli = cli();

        assertEquals(ExitCodes.OK, cli.run("changes", "tail", "--after", "77", "--max-events", "1"), cli.err());

        wm.verify(getRequestedFor(urlPathEqualTo(STREAM)).withQueryParam("after", equalTo("77")));
    }
}
