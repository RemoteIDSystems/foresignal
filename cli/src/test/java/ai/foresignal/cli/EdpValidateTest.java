package ai.foresignal.cli;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.net.URISyntaxException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code foresignal edp validate}: local validation against the bundled EDP 1.0 schemas, and {@code --remote}. */
class EdpValidateTest {

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort()).build();

    @TempDir
    Path tmp;

    private final JsonMapper json = JsonMapper.builder().build();

    private static String fixture(String name) {
        try {
            return Path.of(EdpValidateTest.class.getResource("/edp/" + name).toURI()).toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void aValidManifestAndItemsPassLocallyWithoutAServer() {
        CliHarness cli = new CliHarness(tmp);

        int code = cli.run("edp", "validate", fixture("manifest.json"), fixture("items.ndjson"));

        assertEquals(ExitCodes.OK, code, cli.out() + cli.err());
        assertTrue(cli.out().contains("manifest: valid"), cli.out());
        assertTrue(cli.out().contains("items: 2 valid, 0 rejected"), cli.out());
    }

    @Test
    void anInvalidManifestFailsWithItsPaths() {
        CliHarness cli = new CliHarness(tmp);

        assertEquals(ExitCodes.USAGE, cli.run("edp", "validate", fixture("bad-manifest.json")));

        assertTrue(cli.out().contains("manifest: invalid"), cli.out());
        assertTrue(cli.out().contains("protocol"), cli.out());
        assertTrue(cli.out().contains("/streams"), cli.out());
    }

    @Test
    void aRejectedItemIsReportedWithItsLine() {
        CliHarness cli = new CliHarness(tmp);

        assertEquals(ExitCodes.USAGE, cli.run("edp", "validate", fixture("manifest.json"), fixture("bad-items.ndjson"),
                "-o", "json"));

        JsonNode report = json.readTree(cli.out());
        assertFalse(report.path("valid").asBoolean());
        assertTrue(report.path("manifestValid").asBoolean());
        assertEquals(1, report.path("itemsValid").asInt());
        assertEquals(1, report.path("itemsRejected").asInt());
        assertEquals("bad-items.ndjson:2", report.path("problems").get(0).path("location").asString());
    }

    @Test
    void aProviderKitExampleIsUnwrappedWithItsBatch() {
        CliHarness cli = new CliHarness(tmp);

        assertEquals(ExitCodes.OK, cli.run("edp", "validate", fixture("observation.provider.json")),
                cli.out() + cli.err());

        assertTrue(cli.out().contains("items: 2 valid, 0 rejected"), cli.out());
    }

    @Test
    void anItemsLineThatIsNotJsonIsAUsageError() {
        CliHarness cli = new CliHarness(tmp);

        assertEquals(ExitCodes.USAGE, cli.run("edp", "validate", fixture("manifest.json"), fixture("broken.ndjson")));

        assertTrue(cli.err().contains("broken.ndjson:1 is not valid JSON"), cli.err());
    }

    @Test
    void remoteSendsTheManifestAndItemsToTheServer() {
        wm.stubFor(post(urlPathEqualTo("/v1/external/validate")).willReturn(okJson(
                "{\"manifestValid\":true,\"manifestErrors\":[],\"breakingChanges\":[],\"itemsValid\":2,"
                        + "\"rejected\":[]}")));
        CliHarness cli = CliHarness.against(tmp, wm.baseUrl(), "fsr_agent");

        assertEquals(ExitCodes.OK, cli.run("edp", "validate", fixture("manifest.json"), fixture("items.ndjson"),
                "--remote", "--source-id", "ext-river-gauges-example"), cli.err());

        wm.verify(postRequestedFor(urlPathEqualTo("/v1/external/validate"))
                .withRequestBody(matchingJsonPath("$.manifest.protocol", equalTo("foresignal-edp/1.0")))
                .withRequestBody(matchingJsonPath("$.items[1].id", equalTo("rhine-kaub:2026-10-16")))
                .withRequestBody(matchingJsonPath("$.sourceId", equalTo("ext-river-gauges-example"))));
        assertTrue(cli.out().contains("manifest: valid"), cli.out());
        assertTrue(cli.out().contains("items: 2 valid, 0 rejected"), cli.out());
    }

    @Test
    void remoteRejectionsFailTheCommand() {
        wm.stubFor(post(urlPathEqualTo("/v1/external/validate")).willReturn(okJson(
                "{\"manifestValid\":false,\"manifestErrors\":[{\"path\":\"/streams/0/kind\",\"message\":\"taken\"}],"
                        + "\"breakingChanges\":[\"stream levels removed\"],\"itemsValid\":0,"
                        + "\"rejected\":[{\"index\":0,\"reason\":\"SCHEMA_INVALID\"}]}")));
        CliHarness cli = CliHarness.against(tmp, wm.baseUrl(), "fsr_agent");

        assertEquals(ExitCodes.USAGE, cli.run("edp", "validate", fixture("manifest.json"), "--remote"));

        assertTrue(cli.out().contains("/streams/0/kind: taken"), cli.out());
        assertTrue(cli.out().contains("breaking change: stream levels removed"), cli.out());
        assertTrue(cli.out().contains("SCHEMA_INVALID"), cli.out());
    }
}
