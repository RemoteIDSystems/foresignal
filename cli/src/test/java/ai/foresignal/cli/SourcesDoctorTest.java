package ai.foresignal.cli;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code foresignal sources doctor}: {@code DoctorOptions}, polling the {@code Location} of a {@code 202} until it
 * answers {@code 200}, and printing findings and the patch with its "proposed, not applied" label.
 */
class SourcesDoctorTest {

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort()).build();

    private static final String REPORT_ID = "01JABCDEFGHJKMNPQRSTVWXYZ0";

    private static final String ACCEPTED = "{\"reportId\":\"" + REPORT_ID + "\",\"status\":\"running\","
            + "\"location\":\"/v1/doctor/reports/" + REPORT_ID + "\"}";

    private static final String REPORT = """
            {"id":"01JABCDEFGHJKMNPQRSTVWXYZ0","sourceId":"usgs-earthquakes","createdAt":"2026-10-18T10:00:00Z",
             "trigger":"MANUAL","status":"BROKEN",
             "findings":[{"code":"FIELD_RENAMED","severity":"error","path":"$.features[*].properties.mag",
                          "evidence":["fixture has mag","probe has magnitude"],
                          "suggestedAction":"Map 'magnitude' to 'mag'","heuristic":false}],
             "probe":{"skipped":false,"status":"ok","httpStatus":200,"records":12},
             "llm":{"status":"ok","model":"test-model","summary":"The upstream renamed a field.",
                    "likelyCause":"API v2 rollout","suggestedActions":["Update the parser"],
                    "patch":{"file":"UsgsAdapter.java","unifiedDiff":"--- a/UsgsAdapter.java\\n+++ b/UsgsAdapter.java",
                             "patchApplies":true,"label":"proposed, not applied"}}}
            """;

    @TempDir
    Path tmp;

    @Test
    void pollsTheLocationUntilTheReportIsReady() {
        wm.stubFor(post(urlPathEqualTo("/v1/sources/usgs-earthquakes/doctor")).willReturn(aResponse()
                .withStatus(202).withHeader("Location", "/v1/doctor/reports/" + REPORT_ID)
                .withHeader("Content-Type", "application/json").withBody(ACCEPTED)));
        wm.stubFor(get(urlPathEqualTo("/v1/doctor/reports/" + REPORT_ID)).inScenario("doctor")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(202).withHeader("Content-Type", "application/json")
                        .withBody(ACCEPTED))
                .willSetStateTo("done"));
        wm.stubFor(get(urlPathEqualTo("/v1/doctor/reports/" + REPORT_ID)).inScenario("doctor")
                .whenScenarioStateIs("done").willReturn(okJson(REPORT)));
        CliHarness cli = CliHarness.against(tmp, wm.baseUrl(), "fsr_admin");

        int code = cli.run("sources", "doctor", "usgs-earthquakes", "--llm", "on", "--detail", "full");

        assertEquals(ExitCodes.OK, code, cli.err());
        wm.verify(postRequestedFor(urlPathEqualTo("/v1/sources/usgs-earthquakes/doctor"))
                .withRequestBody(equalToJson("{\"probe\":true,\"llm\":\"on\",\"detail\":\"full\"}")));
        wm.verify(2, getRequestedFor(urlPathEqualTo("/v1/doctor/reports/" + REPORT_ID)));
        assertEquals(List.of(Duration.ofSeconds(5), Duration.ofSeconds(5)), cli.sleeps());
        String out = cli.out();
        assertTrue(out.contains("Doctor report " + REPORT_ID + " for usgs-earthquakes: BROKEN"), out);
        assertTrue(out.contains("[error] FIELD_RENAMED at $.features[*].properties.mag"), out);
        assertTrue(out.contains("evidence: probe has magnitude"), out);
        assertTrue(out.contains("action: Map 'magnitude' to 'mag'"), out);
        assertTrue(out.contains("likely cause: API v2 rollout"), out);
        assertTrue(out.contains("Patch (proposed, not applied): UsgsAdapter.java"), out);
        assertTrue(out.contains("+++ b/UsgsAdapter.java"), out);
        assertTrue(cli.err().contains("in progress"), cli.err());
    }

    @Test
    void aSynchronousReportIsPrintedAsJson() {
        wm.stubFor(post(urlPathEqualTo("/v1/sources/usgs-earthquakes/doctor")).willReturn(okJson(REPORT)));
        CliHarness cli = CliHarness.against(tmp, wm.baseUrl(), "fsr_admin");

        assertEquals(ExitCodes.OK, cli.run("sources", "doctor", "usgs-earthquakes", "--no-probe", "-o", "json"),
                cli.err());

        JsonMapper json = JsonMapper.builder().build();
        assertEquals(json.readTree(REPORT), json.readTree(cli.out()));
        wm.verify(postRequestedFor(urlPathEqualTo("/v1/sources/usgs-earthquakes/doctor"))
                .withRequestBody(equalToJson("{\"probe\":false}")));
        assertTrue(cli.sleeps().isEmpty());
    }

    @Test
    void thePatchLabelDefaultsWhenTheServerOmitsIt() {
        wm.stubFor(post(urlPathEqualTo("/v1/sources/x/doctor")).willReturn(okJson(
                "{\"id\":\"R\",\"sourceId\":\"x\",\"status\":\"DEGRADED_INFO\",\"findings\":[],"
                        + "\"llm\":{\"status\":\"ok\",\"patch\":{\"file\":\"X.java\",\"unifiedDiff\":\"d\","
                        + "\"patchApplies\":false,\"error\":\"hunk 1 failed\"}}}")));
        CliHarness cli = CliHarness.against(tmp, wm.baseUrl(), "fsr_admin");

        assertEquals(ExitCodes.OK, cli.run("sources", "doctor", "x"), cli.err());

        assertTrue(cli.out().contains("Findings: none"), cli.out());
        assertTrue(cli.out().contains("Patch (proposed, not applied): X.java [does not apply: hunk 1 failed]"),
                cli.out());
    }

    @Test
    void maxPollsStopsWaitingAndPrintsTheRun() {
        wm.stubFor(post(urlPathEqualTo("/v1/sources/x/doctor")).willReturn(aResponse().withStatus(202)
                .withHeader("Location", wm.baseUrl() + "/v1/doctor/reports/" + REPORT_ID)
                .withHeader("Content-Type", "application/json").withBody(ACCEPTED)));
        wm.stubFor(get(urlPathEqualTo("/v1/doctor/reports/" + REPORT_ID)).willReturn(aResponse().withStatus(202)
                .withHeader("Content-Type", "application/json").withBody(ACCEPTED)));
        CliHarness cli = CliHarness.against(tmp, wm.baseUrl(), "fsr_admin");

        assertEquals(ExitCodes.OK, cli.run("sources", "doctor", "x", "--max-polls", "1", "--poll-interval", "2"),
                cli.err());

        assertEquals(List.of(Duration.ofSeconds(2)), cli.sleeps());
        assertTrue(cli.err().contains("still running after 1 polls"), cli.err());
        assertTrue(cli.out().contains(REPORT_ID), cli.out());
    }

    @Test
    void aDoctorErrorIsAnApiError() {
        wm.stubFor(post(urlPathEqualTo("/v1/sources/x/doctor")).willReturn(aResponse().withStatus(409)
                .withHeader("Content-Type", "application/problem+json")
                .withBody("{\"title\":\"Busy\",\"status\":409,\"code\":\"DOCTOR_BUSY\"}")));
        CliHarness cli = CliHarness.against(tmp, wm.baseUrl(), "fsr_admin");

        assertEquals(ExitCodes.API_ERROR, cli.run("sources", "doctor", "x"));
        assertTrue(cli.err().contains("DOCTOR_BUSY"), cli.err());
    }
}
