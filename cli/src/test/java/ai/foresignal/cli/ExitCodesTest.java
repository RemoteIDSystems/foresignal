package ai.foresignal.cli;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

/** Exit codes: 0 success, 1 usage, 2 API error (with the ProblemDetail code), 3 network. */
class ExitCodesTest {

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort()).build();

    @TempDir
    Path tmp;

    @Test
    void usageErrorsExitWithOne() {
        CliHarness cli = CliHarness.against(tmp, wm.baseUrl(), null);
        assertEquals(ExitCodes.USAGE, cli.run());
        assertTrue(cli.err().contains("Missing required subcommand"), cli.err());
        assertEquals(ExitCodes.USAGE, cli.run("sources"));
        assertEquals(ExitCodes.USAGE, cli.run("bogus"));
        assertEquals(ExitCodes.USAGE, cli.run("sources", "show"));
        assertEquals(ExitCodes.USAGE, cli.run("sources", "list", "--nope"));
        assertEquals(ExitCodes.USAGE, cli.run("alerts", "ack", "not-a-number"));
        assertEquals(0, wm.getAllServeEvents().size(), "no request on a usage error");
    }

    @Test
    void helpAndVersionExitWithZero() {
        CliHarness cli = new CliHarness(tmp);
        assertEquals(ExitCodes.OK, cli.run("--help"));
        assertTrue(cli.out().contains("Exit codes:"));
        assertEquals(ExitCodes.OK, cli.run("--version"));
        assertTrue(cli.out().contains("foresignal " + ForesignalCli.VERSION));
    }

    @Test
    void noConfiguredUrlIsAUsageError() {
        CliHarness cli = new CliHarness(tmp.resolve("empty"));
        assertEquals(ExitCodes.USAGE, cli.run("system"));
        assertTrue(cli.err().contains("foresignal login"), cli.err());
    }

    @Test
    void anUnreadableInputFileIsAUsageError() {
        CliHarness cli = CliHarness.against(tmp, wm.baseUrl(), "fsr_t");
        assertEquals(ExitCodes.USAGE, cli.run("triggers", "create", "-f", tmp.resolve("missing.json").toString()));
        assertTrue(cli.err().contains("Cannot read"), cli.err());
    }

    @Test
    void anApiErrorExitsWithTwoAndPrintsTheProblemCode() {
        wm.stubFor(get(urlPathEqualTo("/v1/sources/nope")).willReturn(aResponse().withStatus(404)
                .withHeader("Content-Type", "application/problem+json")
                .withBody("{\"title\":\"Not Found\",\"status\":404,\"code\":\"NOT_FOUND\","
                        + "\"detail\":\"Unknown source 'nope'\"}")));
        CliHarness cli = CliHarness.against(tmp, wm.baseUrl(), "fsr_t");

        assertEquals(ExitCodes.API_ERROR, cli.run("sources", "show", "nope"));

        assertTrue(cli.err().contains("error: NOT_FOUND (HTTP 404): Not Found"), cli.err());
        assertTrue(cli.err().contains("Unknown source 'nope'"), cli.err());
        assertEquals("", cli.out());
    }

    @Test
    void validationErrorsListTheirFields() {
        wm.stubFor(put(urlPathEqualTo("/v1/sources/usgs")).willReturn(aResponse().withStatus(403)
                .withHeader("Content-Type", "application/problem+json")
                .withBody("{\"title\":\"Forbidden\",\"status\":403,\"code\":\"INSUFFICIENT_SCOPE\","
                        + "\"requiredScope\":\"admin\",\"errors\":[{\"field\":\"enabled\",\"message\":\"no\"}]}")));
        CliHarness cli = CliHarness.against(tmp, wm.baseUrl(), "fsr_t");

        assertEquals(ExitCodes.API_ERROR, cli.run("sources", "enable", "usgs"));

        assertTrue(cli.err().contains("INSUFFICIENT_SCOPE"), cli.err());
        assertTrue(cli.err().contains("- enabled: no"), cli.err());
    }

    @Test
    void anErrorWithoutAProblemBodyUsesTheStatus() {
        wm.stubFor(get(urlPathEqualTo("/v1/system")).willReturn(aResponse().withStatus(502)
                .withHeader("Content-Type", "text/html").withBody("<html>bad gateway</html>")));
        CliHarness cli = CliHarness.against(tmp, wm.baseUrl(), null);

        assertEquals(ExitCodes.API_ERROR, cli.run("system"));

        assertTrue(cli.err().contains("HTTP_502"), cli.err());
    }

    @Test
    void anUnreachableServerExitsWithThree() throws IOException {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        CliHarness cli = CliHarness.against(tmp, "http://127.0.0.1:" + port, "fsr_t");

        assertEquals(ExitCodes.NETWORK, cli.run("system"));

        assertTrue(cli.err().contains("error: network: Cannot reach http://127.0.0.1:" + port), cli.err());
        assertTrue(!cli.err().contains("fsr_t"), "the token is never printed");
    }
}
