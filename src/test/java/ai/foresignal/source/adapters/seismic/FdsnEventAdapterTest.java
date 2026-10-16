package ai.foresignal.source.adapters.seismic;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link FdsnEventAdapter} against a stubbed FDSN event service (prompt 089, post-review fixes): HTTP 204 is an empty
 * successful run, a full page is followed by the next {@code offset} until a short page, the cursor moves only when a
 * pass completes, and a budget refusal after the first page stores the pass for the next run.
 */
@IntegrationTest
class FdsnEventAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T12:00:00Z");
    static final String PATH = "/fdsnws/event/1/query";
    static final String HEADER = "#EventID|Time|Latitude|Longitude|Depth/km|Author|Catalog|Contributor|ContributorID"
            + "|MagType|Magnitude|MagAuthor|EventLocationName|EventType\n";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    private FdsnEventAdapter adapter(String id) {
        return new FdsnEventAdapter(new FdsnService(id, "Test " + id, URI.create(baseUrl() + PATH), true,
                "CC BY 4.0", null, "Test", null, null));
    }

    private AdapterHttpHarness harness(FdsnEventAdapter adapter, Budget budget) {
        AdapterHttpHarness harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install,
                new MutableClock(NOW), budget);
        harness.reset(adapter.id(), FdsnEventAdapter.CURSOR_KEY, FdsnEventAdapter.PASS_KEY);
        return harness;
    }

    /** {@code count} distinct rows starting at {@code first}. */
    private static String rows(int first, int count) {
        StringBuilder sb = new StringBuilder(HEADER);
        for (int i = first; i < first + count; i++) {
            sb.append("ev").append(i).append("|2026-10-14T10:00:00.00|1.0|2.0|10.0|||T|ev").append(i)
                    .append("|Mw|4.5||Test Region|earthquake\n");
        }
        return sb.toString();
    }

    private static void serve(int offset, int status, String body) {
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).withQueryParam("offset", equalTo(String.valueOf(offset)))
                .willReturn(aResponse().withStatus(status).withHeader("Content-Type", "text/plain")
                        .withBody(body == null ? "" : body)));
    }

    @Test
    @DisplayName("HTTP 204 (FDSN nodata) is an empty, successful run, and the cursor moves")
    void noContent() throws Exception {
        FdsnEventAdapter adapter = adapter("t204");
        AdapterHttpHarness harness = harness(adapter, Budget.of(1000, 100_000));
        serve(1, 204, null);
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(result.records()).isEmpty();
        assertThat(harness.client.stateStore(adapter.id()).get(FdsnEventAdapter.CURSOR_KEY))
                .contains("2026-10-14T11:55:00Z");
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(PATH)).withQueryParam("updatedafter",
                equalTo("2026-10-14T10:00:00Z")).withQueryParam("limit", equalTo("500")));
    }

    @Test
    @DisplayName("a full page is followed by the next offset until a short page; the real fixture parses")
    void paging() throws Exception {
        FdsnEventAdapter adapter = adapter("tpage");
        AdapterHttpHarness harness = harness(adapter, Budget.of(1000, 100_000));
        serve(1, 200, rows(0, FdsnEventAdapter.LIMIT));
        serve(1 + FdsnEventAdapter.LIMIT, 200, new String(AdapterFixtures.bytes("fdsn-geofon", "events.txt")));
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(result.records().size()).isGreaterThan(FdsnEventAdapter.LIMIT);
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(PATH)));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(PATH)).withQueryParam("offset", equalTo("501")));
        AdapterStateStore state = harness.client.stateStore(adapter.id());
        assertThat(state.get(FdsnEventAdapter.CURSOR_KEY)).contains("2026-10-14T11:55:00Z");
        assertThat(state.get(FdsnEventAdapter.PASS_KEY)).isEmpty();
    }

    @Test
    @DisplayName("a budget refusal after the first page keeps the cursor and stores the pass; the next run continues")
    void budgetStopsCleanly() throws Exception {
        FdsnEventAdapter adapter = adapter("tbudget");
        AdapterHttpHarness harness = harness(adapter, Budget.of(1, 100));
        serve(1, 200, rows(0, FdsnEventAdapter.LIMIT));
        serve(1 + FdsnEventAdapter.LIMIT, 200, rows(FdsnEventAdapter.LIMIT, 3));
        FetchResult first = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(first.records()).hasSize(FdsnEventAdapter.LIMIT);
        AdapterStateStore state = harness.client.stateStore(adapter.id());
        assertThat(state.get(FdsnEventAdapter.CURSOR_KEY)).as("the pass did not complete").isEmpty();
        assertThat(state.get(FdsnEventAdapter.PASS_KEY)).contains("2026-10-14T12:00:00Z|501");

        harness.clock.advance(java.time.Duration.ofMinutes(5));
        FetchResult second = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(second.records()).hasSize(3);
        assertThat(state.get(FdsnEventAdapter.PASS_KEY)).isEmpty();
        assertThat(state.get(FdsnEventAdapter.CURSOR_KEY)).as("the start of the pass, not of the last run")
                .contains("2026-10-14T11:55:00Z");
    }

    @Test
    @DisplayName("any other non-200 status fails the run without moving the cursor")
    void otherStatusFails() {
        FdsnEventAdapter adapter = adapter("tstatus");
        AdapterHttpHarness harness = harness(adapter, Budget.of(1000, 100_000));
        serve(1, 202, "");
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of())))
                .isInstanceOf(AdapterException.class);
        assertThat(harness.client.stateStore(adapter.id()).get(FdsnEventAdapter.CURSOR_KEY)).isEmpty();
    }

    @Test
    @DisplayName("rows counts data lines, usable or not, and ignores the header and blank lines")
    void rowCount() {
        assertThat(FdsnTextParser.rows((HEADER + "a|b\n\nc|d\n").getBytes())).isEqualTo(2);
        assertThat(FdsnTextParser.rows(null)).isZero();
    }
}
