package ai.foresignal.cli;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code --output table|json|ndjson} over real commands. */
class OutputFormatTest {

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort()).build();

    private static final String SOURCES = """
            [{"id":"usgs-earthquakes","name":"USGS earthquakes","kinds":["earthquake"],"mode":"polling",
              "enabled":true,"status":"healthy","lastSuccessAt":"2026-10-18T10:00:00Z"},
             {"id":"nws-alerts","name":"NWS alerts","kinds":["weather-alert"],"mode":"polling",
              "enabled":false,"status":"disabled","lastSuccessAt":null}]
            """;

    @TempDir
    Path tmp;

    private final JsonMapper json = JsonMapper.builder().build();
    private CliHarness cli;

    @BeforeEach
    void setUp() {
        wm.stubFor(get(urlPathEqualTo("/v1/sources")).willReturn(okJson(SOURCES)));
        cli = CliHarness.against(tmp, wm.baseUrl(), "fsr_t");
    }

    @Test
    void tableIsTheDefaultWithAlignedColumns() {
        assertEquals(ExitCodes.OK, cli.run("sources", "list"), cli.err());
        List<String> lines = cli.outLines();
        assertEquals(3, lines.size(), cli.out());
        assertTrue(lines.get(0).startsWith("ID"), lines.get(0));
        assertTrue(lines.get(0).contains("NAME") && lines.get(0).contains("LAST SUCCESS"));
        assertTrue(lines.get(1).startsWith("usgs-earthquakes  USGS earthquakes"), lines.get(1));
        assertTrue(lines.get(2).contains("disabled"));
        assertTrue(lines.get(2).endsWith("-"), "null shows as -: " + lines.get(2));
        int nameColumn = lines.get(0).indexOf("NAME");
        assertEquals(nameColumn, lines.get(1).indexOf("USGS"));
        assertEquals(nameColumn, lines.get(2).indexOf("NWS"));
    }

    @Test
    void jsonPrintsTheDocumentAsReturned() {
        assertEquals(ExitCodes.OK, cli.run("sources", "list", "-o", "json"), cli.err());
        assertEquals(json.readTree(SOURCES), json.readTree(cli.out()));
        assertTrue(cli.out().contains("\n  "), "pretty-printed");
    }

    @Test
    void ndjsonPrintsOneLinePerItem() {
        assertEquals(ExitCodes.OK, cli.run("sources", "list", "--output", "ndjson"), cli.err());
        List<String> lines = cli.outLines();
        assertEquals(2, lines.size());
        assertEquals("usgs-earthquakes", json.readTree(lines.get(0)).path("id").asString());
        assertEquals("nws-alerts", json.readTree(lines.get(1)).path("id").asString());
    }

    @Test
    void theFormatIsCaseInsensitiveAndCanPrecedeTheSubcommand() {
        assertEquals(ExitCodes.OK, cli.run("-o", "JSON", "sources", "list"), cli.err());
        assertTrue(json.readTree(cli.out()).isArray());
    }

    @Test
    void anObjectInTableFormatIsAFieldListing() {
        wm.stubFor(get(urlPathEqualTo("/v1/system")).willReturn(okJson(
                "{\"version\":\"0.9.1\",\"status\":\"ok\",\"records\":{\"current\":12,\"lastSeq\":99}}")));
        assertEquals(ExitCodes.OK, cli.run("system"), cli.err());
        List<String> lines = cli.outLines();
        assertEquals("version  0.9.1", lines.get(0));
        assertEquals("status   ok", lines.get(1));
        assertEquals("records  {\"current\":12,\"lastSeq\":99}", lines.get(2));
    }

    @Test
    void anUnknownFormatIsAUsageError() {
        assertEquals(ExitCodes.USAGE, cli.run("sources", "list", "-o", "xml"));
    }

    @Test
    void allFollowsCursorsAcrossPages() {
        wm.stubFor(get(urlPathEqualTo("/v1/records")).withQueryParam("cursor", absent()).willReturn(okJson(
                "{\"items\":[{\"source\":\"s\",\"externalId\":\"1\",\"kind\":\"k\",\"observedAt\":\"t1\"},"
                        + "{\"source\":\"s\",\"externalId\":\"2\",\"kind\":\"k\",\"observedAt\":\"t2\"}],"
                        + "\"nextCursor\":\"p2\",\"watermarkSeq\":5}")));
        wm.stubFor(get(urlPathEqualTo("/v1/records")).withQueryParam("cursor", equalTo("p2")).willReturn(okJson(
                "{\"items\":[{\"source\":\"s\",\"externalId\":\"3\",\"kind\":\"k\",\"observedAt\":\"t3\"}],"
                        + "\"nextCursor\":null}")));

        assertEquals(ExitCodes.OK, cli.run("records", "query", "--source", "s", "--all", "-o", "ndjson"), cli.err());

        List<String> lines = cli.outLines();
        assertEquals(3, lines.size());
        assertEquals("3", json.readTree(lines.get(2)).path("externalId").asString());
    }

    @Test
    void withoutAllTheNextCursorIsNamedOnStandardError() {
        wm.stubFor(get(urlPathEqualTo("/v1/records")).willReturn(okJson(
                "{\"items\":[{\"source\":\"s\",\"externalId\":\"1\"}],\"nextCursor\":\"p2\"}")));
        assertEquals(ExitCodes.OK, cli.run("records", "query"), cli.err());
        assertTrue(cli.err().contains("--cursor p2"), cli.err());
        assertEquals(2, cli.outLines().size(), cli.out());
    }

    @Test
    void jsonWithoutAllPrintsThePageWithItsCursor() {
        wm.stubFor(get(urlPathEqualTo("/v1/records")).willReturn(okJson(
                "{\"items\":[{\"source\":\"s\",\"externalId\":\"1\"}],\"nextCursor\":\"p2\"}")));
        assertEquals(ExitCodes.OK, cli.run("records", "query", "-o", "json"), cli.err());
        JsonNode page = json.readTree(cli.out());
        assertEquals("p2", page.path("nextCursor").asString());
    }
}
