package ai.foresignal.cli;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Every command group maps to the documented operations, paths, parameters and bodies. */
class CommandsTest {

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort()).build();

    @TempDir
    Path tmp;

    private final JsonMapper json = JsonMapper.builder().build();
    private CliHarness cli;

    @BeforeEach
    void setUp() {
        cli = CliHarness.against(tmp.resolve("cfg"), wm.baseUrl(), "fsr_test");
    }

    private void ok(String... args) {
        int code = cli.run(args);
        assertEquals(ExitCodes.OK, code, String.join(" ", args) + "\n" + cli.err());
    }

    // --- sources -------------------------------------------------------------------------------------------

    @Test
    void sourcesShowEnableDisableHealthFetchProbe() {
        wm.stubFor(get(urlPathEqualTo("/v1/sources/usgs")).willReturn(okJson("{\"id\":\"usgs\",\"enabled\":true}")));
        wm.stubFor(put(urlPathEqualTo("/v1/sources/usgs")).willReturn(okJson("{\"id\":\"usgs\",\"enabled\":false}")));
        wm.stubFor(get(urlPathEqualTo("/v1/sources/health")).willReturn(okJson(
                "[{\"sourceId\":\"usgs\",\"status\":\"failing\",\"consecutiveFailures\":3,\"lastError\":\"503\"}]")));
        wm.stubFor(get(urlPathEqualTo("/v1/sources/usgs/health")).willReturn(okJson(
                "{\"sourceId\":\"usgs\",\"status\":\"healthy\",\"consecutiveFailures\":0}")));
        wm.stubFor(post(urlPathEqualTo("/v1/sources/usgs/fetch")).willReturn(okJson(
                "{\"sourceId\":\"usgs\",\"success\":true,\"recordsSeen\":10,\"recordsChanged\":2}")));
        wm.stubFor(post(urlPathEqualTo("/v1/sources/usgs/probe")).willReturn(okJson(
                "{\"sourceId\":\"usgs\",\"requests\":[],\"records\":[]}")));

        ok("sources", "show", "usgs");
        ok("sources", "disable", "usgs");
        assertTrue(cli.out().contains("usgs: enabled=false"), cli.out());
        ok("sources", "enable", "usgs");
        ok("sources", "health", "--status", "failing,degraded");
        assertTrue(cli.out().contains("FAILURES"), cli.out());
        ok("sources", "health", "usgs");
        ok("sources", "fetch", "usgs", "--allow-shrink");
        ok("sources", "probe", "usgs", "--max-bytes", "4096", "--seconds", "5");

        wm.verify(putRequestedFor(urlPathEqualTo("/v1/sources/usgs")).withRequestBody(equalToJson("{\"enabled\":false}")));
        wm.verify(putRequestedFor(urlPathEqualTo("/v1/sources/usgs")).withRequestBody(equalToJson("{\"enabled\":true}")));
        wm.verify(getRequestedFor(urlPathEqualTo("/v1/sources/health")).withQueryParam("status", equalTo("degraded")));
        wm.verify(postRequestedFor(urlPathEqualTo("/v1/sources/usgs/fetch"))
                .withQueryParam("allowShrink", equalTo("true")));
        wm.verify(postRequestedFor(urlPathEqualTo("/v1/sources/usgs/probe")).withQueryParam("maxBytes", equalTo("4096"))
                .withQueryParam("seconds", equalTo("5")));
        wm.verify(getRequestedFor(urlPathEqualTo("/v1/sources/usgs"))
                .withHeader("Authorization", equalTo("Bearer fsr_test")));
    }

    // --- records -------------------------------------------------------------------------------------------

    @Test
    void recordsGetHistoryAndTrackEncodeTheExternalId() {
        wm.stubFor(get(urlPathMatching("/v1/records/usgs/us(%20| )7000")).willReturn(okJson(
                "{\"source\":\"usgs\",\"externalId\":\"us 7000\"}")));
        wm.stubFor(get(urlPathMatching("/v1/records/usgs/us(%20| )7000/versions")).willReturn(okJson(
                "{\"items\":[{\"seq\":1,\"version\":1,\"persistReason\":\"FIRST\"}],\"nextCursor\":null}")));
        wm.stubFor(get(urlPathEqualTo("/v1/records/ais/123/track")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/geo+json")
                .withBody("{\"type\":\"Feature\",\"geometry\":null,\"properties\":{\"points\":2}}")));

        ok("records", "get", "usgs", "us 7000");
        ok("records", "history", "usgs", "us 7000", "--since", "2026-10-01T00:00:00Z");
        assertTrue(cli.out().contains("FIRST"), cli.out());
        ok("records", "track", "ais", "123", "--from", "2026-10-18T00:00:00Z", "--max-points", "100", "--no-live",
                "-o", "json");

        wm.verify(getRequestedFor(urlPathMatching("/v1/records/usgs/us(%20| )7000/versions"))
                .withQueryParam("since", equalTo("2026-10-01T00:00:00Z")));
        wm.verify(getRequestedFor(urlPathEqualTo("/v1/records/ais/123/track"))
                .withQueryParam("maxPoints", equalTo("100")).withQueryParam("includeLive", equalTo("false")));
    }

    @Test
    void recordsQueryPassesEveryFilter() {
        wm.stubFor(get(urlPathEqualTo("/v1/records")).willReturn(okJson("{\"items\":[],\"nextCursor\":null}")));

        ok("records", "query", "--source", "a,b", "--kind", "earthquake", "--bbox", "-10,40,10,60", "-q", "red sea",
                "--near", "1,2", "--radius-km", "50", "--order", "id", "--limit", "10", "--include-expired");

        wm.verify(getRequestedFor(urlPathEqualTo("/v1/records")).withQueryParam("source", equalTo("a"))
                .withQueryParam("source", equalTo("b")).withQueryParam("kind", equalTo("earthquake"))
                .withQueryParam("bbox", equalTo("-10,40,10,60")).withQueryParam("q", equalTo("red sea"))
                .withQueryParam("near", equalTo("1,2")).withQueryParam("radiusKm", equalTo("50.0"))
                .withQueryParam("order", equalTo("id")).withQueryParam("limit", equalTo("10"))
                .withQueryParam("includeExpired", equalTo("true")));
        assertTrue(cli.out().contains("(none)"), cli.out());
    }

    // --- alerts --------------------------------------------------------------------------------------------

    @Test
    void alertsListAckResolve() {
        wm.stubFor(get(urlPathEqualTo("/v1/alerts")).willReturn(okJson(
                "{\"items\":[{\"id\":7,\"severity\":\"critical\",\"state\":\"firing\",\"message\":\"M6\"}],"
                        + "\"nextCursor\":null}")));
        wm.stubFor(post(urlPathEqualTo("/v1/alerts/7/ack")).willReturn(okJson("{\"id\":7,\"state\":\"acknowledged\"}")));
        wm.stubFor(post(urlPathEqualTo("/v1/alerts/7/resolve")).willReturn(okJson("{\"id\":7,\"state\":\"resolved\"}")));

        ok("alerts", "list", "--state", "firing", "--min-severity", "warning");
        assertTrue(cli.out().contains("critical"), cli.out());
        ok("alerts", "ack", "7", "--note", "on it", "--feedback", "useful");
        ok("alerts", "resolve", "7");

        wm.verify(getRequestedFor(urlPathEqualTo("/v1/alerts")).withQueryParam("state", equalTo("firing"))
                .withQueryParam("minSeverity", equalTo("warning")));
        wm.verify(postRequestedFor(urlPathEqualTo("/v1/alerts/7/ack"))
                .withRequestBody(equalToJson("{\"note\":\"on it\",\"feedback\":\"useful\"}")));
        wm.verify(postRequestedFor(urlPathEqualTo("/v1/alerts/7/resolve")).withRequestBody(equalToJson("{}")));
    }

    @Test
    void alertsTailResumesWithTheLastAlertRevision() {
        wm.stubFor(get(urlPathEqualTo("/v1/alerts/stream")).withHeader("Last-Event-ID", absent())
                .willReturn(aResponse().withHeader("Content-Type", "text/event-stream").withBody(
                        "id: 11\nevent: alert\ndata: {\"type\":\"fired\",\"alert\":{\"id\":7,\"severity\":"
                                + "\"critical\",\"state\":\"firing\",\"message\":\"M6\"}}\n\n")));
        wm.stubFor(get(urlPathEqualTo("/v1/alerts/stream")).withHeader("Last-Event-ID", equalTo("11"))
                .willReturn(aResponse().withHeader("Content-Type", "text/event-stream").withBody(
                        "event: heartbeat\ndata: {}\n\nid: 12\nevent: alert.updated\ndata: {\"type\":\"acknowledged\","
                                + "\"alert\":{\"id\":7,\"severity\":\"critical\",\"state\":\"acknowledged\"}}\n\n")));

        ok("alerts", "tail", "--min-severity", "warning", "--max-events", "2");

        List<String> lines = cli.outLines();
        assertEquals("alert  7  critical  firing  M6", lines.get(0));
        assertTrue(lines.get(1).startsWith("alert.updated  7  critical  acknowledged"), lines.get(1));
        assertEquals(1, cli.sleeps().size());
    }

    // --- triggers ------------------------------------------------------------------------------------------

    @Test
    void triggersListCreateTestQualityAndTune() throws Exception {
        String rule = "{\"name\":\"Big quakes\",\"source\":\"usgs\",\"conditions\":[]}";
        Path file = Files.writeString(tmp.resolve("rule.json"), rule);
        wm.stubFor(get(urlPathEqualTo("/v1/triggers")).willReturn(okJson("[{\"id\":\"r1\",\"name\":\"Big quakes\"}]")));
        wm.stubFor(post(urlPathEqualTo("/v1/triggers")).willReturn(aResponse().withStatus(201)
                .withHeader("Content-Type", "application/json").withBody("{\"id\":\"r1\",\"name\":\"Big quakes\"}")));
        wm.stubFor(post(urlPathEqualTo("/v1/triggers/r1/test")).willReturn(okJson(
                "{\"ruleId\":\"r1\",\"matches\":[{\"seq\":3,\"source\":\"usgs\",\"externalId\":\"q\"}],"
                        + "\"matchCount\":1}")));
        wm.stubFor(get(urlPathEqualTo("/v1/triggers/r1/quality")).willReturn(okJson(
                "{\"ruleId\":\"r1\",\"alerts\":4,\"precision\":0.75}")));
        wm.stubFor(get(urlPathEqualTo("/v1/triggers/r1/suggestions")).willReturn(okJson(
                "{\"ruleId\":\"r1\",\"since\":\"x\",\"suggesterVersion\":\"1\",\"suggestions\":"
                        + "[{\"id\":\"s1\",\"kind\":\"raise-threshold\",\"rationale\":\"noisy\"}]}")));
        wm.stubFor(post(urlPathEqualTo("/v1/triggers/r1/suggestions/s1/apply")).willReturn(okJson("{\"id\":\"log1\"}")));
        wm.stubFor(post(urlPathEqualTo("/v1/triggers/r1/suggestions/s1/dismiss")).willReturn(okJson("{\"id\":\"s1\"}")));

        ok("triggers", "list", "--enabled", "true");
        ok("triggers", "create", "-f", file.toString());
        ok("triggers", "test", "r1", "--since", "2026-10-01T00:00:00Z", "--simulate");
        assertTrue(cli.out().contains("Rule r1: 1 matches"), cli.out());
        ok("triggers", "quality", "r1");
        ok("triggers", "tune", "r1", "--simulate");
        assertTrue(cli.out().contains("raise-threshold"), cli.out());
        ok("triggers", "tune", "r1", "--apply", "s1", "--note", "ok");
        ok("triggers", "tune", "r1", "--dismiss", "s1", "--reason", "no");
        assertEquals(ExitCodes.USAGE, cli.run("triggers", "tune", "r1", "--apply", "s1", "--dismiss", "s1"));

        wm.verify(getRequestedFor(urlPathEqualTo("/v1/triggers")).withQueryParam("enabled", equalTo("true")));
        wm.verify(postRequestedFor(urlPathEqualTo("/v1/triggers")).withRequestBody(equalToJson(rule)));
        wm.verify(postRequestedFor(urlPathEqualTo("/v1/triggers/r1/test")).withQueryParam("simulate", equalTo("true")));
        wm.verify(postRequestedFor(urlPathEqualTo("/v1/triggers/r1/suggestions/s1/apply"))
                .withRequestBody(equalToJson("{\"note\":\"ok\"}")));
        wm.verify(postRequestedFor(urlPathEqualTo("/v1/triggers/r1/suggestions/s1/dismiss"))
                .withRequestBody(equalToJson("{\"reason\":\"no\"}")));
    }

    @Test
    void triggersCreateRejectsANonObject() throws Exception {
        Path file = Files.writeString(tmp.resolve("rule.json"), "[1,2]");
        assertEquals(ExitCodes.USAGE, cli.run("triggers", "create", "-f", file.toString()));
        assertEquals(0, wm.getAllServeEvents().size());
    }

    // --- ask -----------------------------------------------------------------------------------------------

    @Test
    void askStreamsDeltasThenPrintsTheVerifiedAnswer() {
        wm.stubFor(post(urlPathEqualTo("/v1/ask/stream")).willReturn(aResponse()
                .withHeader("Content-Type", "text/event-stream").withBody(
                        "event: progress\ndata: {\"tool\":\"search_records\",\"status\":\"calling\"}\n\n"
                                + "event: delta\ndata: {\"text\":\"Two \"}\n\n"
                                + "event: delta\ndata: {\"text\":\"quakes.\"}\n\n"
                                + "event: warning\ndata: {\"message\":\"uncited claim dropped\"}\n\n"
                                + "event: done\ndata: {\"answer\":\"Two quakes [1].\",\"citations\":"
                                + "[{\"id\":\"usgs/q1\",\"title\":\"M6.1 off Japan\"}]}\n\n")));

        ok("ask", "How", "many", "quakes?", "--source", "usgs", "--since", "2026-10-17T00:00:00Z");

        assertTrue(cli.out().startsWith("Two quakes."), cli.out());
        assertTrue(cli.out().contains("Verified answer:\nTwo quakes [1]."), cli.out());
        assertTrue(cli.out().contains("[1] M6.1 off Japan (usgs/q1)"), cli.out());
        assertTrue(cli.err().contains("[search_records calling]"), cli.err());
        assertTrue(cli.err().contains("warning: uncited claim dropped"), cli.err());
        wm.verify(postRequestedFor(urlPathEqualTo("/v1/ask/stream")).withHeader("Accept",
                containing("text/event-stream"))
                .withRequestBody(equalToJson("{\"question\":\"How many quakes?\",\"scope\":{\"sources\":[\"usgs\"],"
                        + "\"since\":\"2026-10-17T00:00:00Z\"}}")));
    }

    @Test
    void askErrorEventsAreApiErrors() {
        wm.stubFor(post(urlPathEqualTo("/v1/ask/stream")).willReturn(aResponse()
                .withHeader("Content-Type", "text/event-stream").withBody(
                        "event: error\ndata: {\"title\":\"Budget\",\"status\":429,\"code\":\"BUDGET_EXCEEDED\"}\n\n")));

        assertEquals(ExitCodes.API_ERROR, cli.run("ask", "anything"));
        assertTrue(cli.err().contains("BUDGET_EXCEEDED"), cli.err());
    }

    @Test
    void askWithoutStreamingAndJsonOutput() {
        wm.stubFor(post(urlPathEqualTo("/v1/ask")).willReturn(okJson("{\"answer\":\"Yes.\",\"citations\":[]}")));
        ok("ask", "--no-stream", "-o", "json", "Is it raining?");
        assertEquals("Yes.", json.readTree(cli.out()).path("answer").asString());
    }

    @Test
    void anAskStreamThatEndsEarlyIsANetworkError() {
        wm.stubFor(post(urlPathEqualTo("/v1/ask/stream")).willReturn(aResponse()
                .withHeader("Content-Type", "text/event-stream").withBody("event: delta\ndata: {\"text\":\"x\"}\n\n")));
        assertEquals(ExitCodes.NETWORK, cli.run("ask", "q"));
    }

    // --- briefings -----------------------------------------------------------------------------------------

    @Test
    void briefingsListAndRun() {
        wm.stubFor(get(urlPathEqualTo("/v1/briefings")).willReturn(okJson(
                "[{\"id\":\"b1\",\"name\":\"Morning\",\"schedule\":\"0 7 * * *\"}]")));
        wm.stubFor(post(urlPathEqualTo("/v1/briefings/b1/run")).willReturn(okJson(
                "{\"id\":\"run1\",\"status\":\"succeeded\",\"contentMarkdown\":\"# Morning\\n\\nQuiet night.\"}")));

        ok("briefings", "list");
        assertTrue(cli.out().contains("Morning"), cli.out());
        ok("briefings", "run", "b1");
        assertTrue(cli.out().contains("# Morning"), cli.out());
        assertTrue(cli.err().contains("run run1: succeeded"), cli.err());
    }

    // --- exports -------------------------------------------------------------------------------------------

    @Test
    void exportsCreateWaitsUntilDone() {
        wm.stubFor(post(urlPathEqualTo("/v1/exports")).willReturn(aResponse().withStatus(202)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\":\"E1\",\"status\":\"QUEUED\",\"progress\":0,\"format\":\"geoparquet\"}")));
        wm.stubFor(get(urlPathEqualTo("/v1/exports/E1")).inScenario("export").whenScenarioStateIs(Scenario.STARTED)
                .willReturn(okJson("{\"id\":\"E1\",\"status\":\"RUNNING\",\"progress\":50,\"format\":\"geoparquet\"}"))
                .willSetStateTo("done"));
        wm.stubFor(get(urlPathEqualTo("/v1/exports/E1")).inScenario("export").whenScenarioStateIs("done")
                .willReturn(okJson("{\"id\":\"E1\",\"status\":\"DONE\",\"progress\":100,\"format\":\"geoparquet\","
                        + "\"rowCount\":12,\"files\":[{\"name\":\"records.parquet\",\"url\":\"/x\",\"bytes\":10,"
                        + "\"sha256\":\"ab\",\"expiresAt\":\"2026-10-25T00:00:00Z\"}]}")));

        ok("exports", "create", "--format", "geoparquet", "--source", "usgs", "--bbox", "-10,40,10,60",
                "--mode", "history", "--from", "2026-10-01T00:00:00Z", "--wait", "--poll-interval", "1");

        wm.verify(postRequestedFor(urlPathEqualTo("/v1/exports")).withRequestBody(equalToJson(
                "{\"format\":\"geoparquet\",\"mode\":\"history\",\"from\":\"2026-10-01T00:00:00Z\","
                        + "\"sources\":[\"usgs\"],\"bbox\":[-10.0,40.0,10.0,60.0]}")));
        assertEquals(List.of(Duration.ofSeconds(1), Duration.ofSeconds(1)), cli.sleeps());
        assertTrue(cli.out().contains("Export E1: DONE 100% (geoparquet), 12 rows"), cli.out());
        assertTrue(cli.out().contains("records.parquet"), cli.out());
    }

    @Test
    void exportsCreateNeedsAFormatAndAValidBbox() {
        assertEquals(ExitCodes.USAGE, cli.run("exports", "create", "--source", "usgs"));
        assertEquals(ExitCodes.USAGE, cli.run("exports", "create", "--format", "gpx", "--bbox", "1,2,3"));
        assertEquals(0, wm.getAllServeEvents().size());
    }

    @Test
    void exportsStatusAndDownloadIntoADirectory() throws Exception {
        wm.stubFor(get(urlPathEqualTo("/v1/exports/E2")).willReturn(okJson(
                "{\"id\":\"E2\",\"status\":\"DONE\",\"progress\":100,\"format\":\"gpx\"}")));
        wm.stubFor(get(urlPathEqualTo("/v1/exports/E2/download")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/gpx+xml")
                .withHeader("Content-Disposition", "attachment; filename=\"../tracks.gpx\"")
                .withBody("<gpx/>")));
        Path dir = Files.createDirectories(tmp.resolve("downloads"));

        ok("exports", "status", "E2");
        assertTrue(cli.out().contains("Export E2: DONE"), cli.out());
        ok("exports", "download", "E2", "--dest", dir.toString());

        assertEquals("<gpx/>", Files.readString(dir.resolve("tracks.gpx")));
        try (var files = Files.list(dir)) {
            assertEquals(1, files.count(), "no partial file is left behind");
        }
    }

    @Test
    void exportsDownloadToAFileWithZip() throws Exception {
        wm.stubFor(get(urlPathEqualTo("/v1/exports/E3/download")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/zip").withBody("PK")));
        Path target = tmp.resolve("out").resolve("e3.zip");
        Files.createDirectories(target.getParent());

        ok("exports", "download", "E3", "--zip", "--dest", target.toString(), "-o", "json");

        assertEquals("PK", Files.readString(target));
        assertEquals(2, json.readTree(cli.out()).path("bytes").asLong());
        wm.verify(getRequestedFor(urlPathEqualTo("/v1/exports/E3/download")).withQueryParam("bundle", equalTo("zip")));
    }

    @Test
    void aFailedDownloadIsAnApiErrorAndLeavesNoFile() throws Exception {
        wm.stubFor(get(urlPathEqualTo("/v1/exports/E4/download")).willReturn(aResponse().withStatus(410)
                .withHeader("Content-Type", "application/problem+json")
                .withBody("{\"title\":\"Gone\",\"status\":410,\"code\":\"EXPORT_EXPIRED\"}")));
        Path dir = Files.createDirectories(tmp.resolve("d"));

        assertEquals(ExitCodes.API_ERROR, cli.run("exports", "download", "E4", "--dest", dir.toString()));

        assertTrue(cli.err().contains("EXPORT_EXPIRED"), cli.err());
        try (var files = Files.list(dir)) {
            assertEquals(0, files.count());
        }
    }

    // --- tokens --------------------------------------------------------------------------------------------

    @Test
    void tokensListCreateRevoke() {
        wm.stubFor(get(urlPathEqualTo("/v1/tokens")).willReturn(okJson(
                "[{\"id\":\"t1\",\"name\":\"ci\",\"scope\":\"read\",\"tokenPrefix\":\"fsr_ab\",\"createdAt\":\"x\"}]")));
        wm.stubFor(post(urlPathEqualTo("/v1/tokens")).willReturn(aResponse().withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\":\"t2\",\"name\":\"bot\",\"scope\":\"agent\",\"token\":\"fsr_newsecret\"}")));
        wm.stubFor(delete(urlPathEqualTo("/v1/tokens/t2")).willReturn(aResponse().withStatus(204)));

        ok("tokens", "list");
        assertTrue(cli.out().contains("fsr_ab"), cli.out());
        ok("tokens", "create", "--name", "bot", "--scope", "agent", "--expires-at", "2027-01-01T00:00:00Z");
        assertTrue(cli.out().contains("fsr_newsecret"), "a new token is shown once");
        ok("tokens", "revoke", "t2");
        assertTrue(cli.out().contains("Revoked token t2"));

        wm.verify(postRequestedFor(urlPathEqualTo("/v1/tokens")).withRequestBody(equalToJson(
                "{\"name\":\"bot\",\"scope\":\"agent\",\"expiresAt\":\"2027-01-01T00:00:00Z\"}")));
        wm.verify(deleteRequestedFor(urlPathEqualTo("/v1/tokens/t2")));
        assertTrue(!cli.out().contains("fsr_test"), "the configured token is never printed");
    }

    // --- edp -----------------------------------------------------------------------------------------------

    @Test
    void edpSourcesAndRegistrationTokens() {
        wm.stubFor(get(urlPathEqualTo("/v1/external/sources")).willReturn(okJson(
                "{\"items\":[{\"sourceId\":\"ext-a\",\"state\":\"pending\"}],\"nextCursor\":null}")));
        wm.stubFor(get(urlPathEqualTo("/v1/external/sources/ext-a")).willReturn(okJson(
                "{\"sourceId\":\"ext-a\",\"state\":\"pending\"}")));
        wm.stubFor(post(urlPathEqualTo("/v1/external/sources/ext-a/approve")).willReturn(okJson(
                "{\"sourceId\":\"ext-a\",\"state\":\"active\"}")));
        wm.stubFor(post(urlPathEqualTo("/v1/external/sources/ext-a/suspend")).willReturn(okJson(
                "{\"sourceId\":\"ext-a\",\"state\":\"suspended\"}")));
        wm.stubFor(post(urlPathEqualTo("/v1/external/registration-tokens")).willReturn(aResponse().withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\":\"rt1\",\"name\":\"lab\",\"prefix\":\"fsx_ab\",\"maxUses\":3,\"uses\":0,"
                        + "\"createdAt\":\"x\",\"token\":\"fsx_secret\"}")));

        ok("edp", "sources", "list", "--state", "pending");
        assertTrue(cli.out().contains("ext-a"), cli.out());
        ok("edp", "sources", "show", "ext-a");
        ok("edp", "sources", "approve", "ext-a");
        assertTrue(cli.out().contains("ext-a: active"), cli.out());
        ok("edp", "sources", "suspend", "ext-a");
        assertTrue(cli.out().contains("ext-a: suspended"), cli.out());
        ok("edp", "tokens", "create", "--name", "lab", "--max-uses", "3", "--source-id-prefix", "lab-");
        assertTrue(cli.out().contains("fsx_secret"), cli.out());

        wm.verify(getRequestedFor(urlPathEqualTo("/v1/external/sources")).withQueryParam("state", equalTo("pending")));
        wm.verify(postRequestedFor(urlPathEqualTo("/v1/external/registration-tokens")).withRequestBody(equalToJson(
                "{\"name\":\"lab\",\"maxUses\":3,\"sourceIdPrefix\":\"lab-\"}")));
    }

    @Test
    void ndjsonOfACommandThatReturnsAnObjectIsOneLine() {
        wm.stubFor(get(urlPathEqualTo("/v1/system")).willReturn(okJson("{\"version\":\"1\",\"status\":\"ok\"}")));
        ok("system", "-o", "ndjson");
        assertEquals(1, cli.outLines().size());
        JsonNode node = json.readTree(cli.outLines().get(0));
        assertEquals("ok", node.path("status").asString());
    }
}
