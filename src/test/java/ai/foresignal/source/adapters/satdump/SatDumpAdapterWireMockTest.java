package ai.foresignal.source.adapters.satdump;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.collect.BudgetGovernor;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.collect.UsageMeter;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.WireMockSupport;
import java.net.InetAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * A SatDump station against a stubbed legacy web server (prompt 049): {@code /api} polled through the
 * real {@link UpstreamClient} at a pinned loopback address (the host name is never resolved),
 * {@code /polarplot.jpeg} fetched only in autotrack during a pass, {@code /fft.jpeg} and
 * {@code /schedule.jpeg} never requested, the budget charged once per inbox scan, and the failing
 * health after three unreachable polls.
 */
@IntegrationTest
class SatDumpAdapterWireMockTest extends WireMockSupport {

    static final Instant T = Instant.parse("2026-10-12T18:01:40Z");
    /** A name no resolver knows: only the pinned address makes the request reach the stub. */
    static final String HOST = "satdump-station.invalid";
    static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16, 'J', 'F', 'I', 'F', 0};

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    @TempDir Path temp;

    SatDumpTestContext ctx;
    UsageMeter usage;
    BudgetGovernor budgets;

    @BeforeEach
    void setUp() throws Exception {
        Path root = Files.createDirectories(temp.resolve("inbox"));
        ctx = new SatDumpTestContext(root, T);
        URI api = URI.create("http://" + HOST + ":" + port() + "/");
        ctx.params.put(SatDumpDescriptor.API_URL, api);
        ctx.pinned.put(SatDumpDescriptor.API_URL, InetAddress.getByName("127.0.0.1"));
        SourceService sources = mock(SourceService.class);
        when(sources.effectiveBudget(anyString())).thenReturn(Budget.of(60, 50_000));
        when(sources.effectiveParams(anyString())).thenReturn(new ValidatedParams(ctx.params, ctx.pinned));
        CredentialsAccess credentials = mock(CredentialsAccess.class);
        when(credentials.forSource(anyString())).thenReturn(Map.of());
        usage = new UsageMeter(jdbc, ctx.clock);
        budgets = new BudgetGovernor(sources, usage, ctx.clock);
        UpstreamClient client = new UpstreamClient(budgets, new BackoffRegistry(jdbc, ctx.clock), usage, install,
                credentials, sources, jdbc, jsonMapper, properties, ctx.clock);
        SourceHttp http = client.forSource(ctx.sourceId(), ctx.descriptor);
        ctx.http = http;
        WIREMOCK.stubFor(get(urlPathEqualTo("/polarplot.jpeg"))
                .willReturn(aResponse().withHeader("Content-Type", "image/jpeg").withBody(JPEG)));
    }

    void api(String fixture) {
        WIREMOCK.stubFor(get(urlPathEqualTo("/api")).willReturn(aResponse().withHeader("Content-Type", "application/json")
                .withBody(SatDumpFixtures.text(fixture))));
    }

    void poll(SatDumpStation s, int times) {
        for (int i = 0; i < times; i++) {
            s.pollApiOnce();
            ctx.clock.advance(Duration.ofSeconds(5));
        }
    }

    @Test
    @DisplayName("/api through the pinned address with the original Host; polar plot only during an autotrack pass")
    void autotrackPass() throws Exception {
        SatDumpStation s = ctx.station();
        api("api/autotrack-idle.json");
        poll(s, 2);
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo("/api")).withHeader("Host", equalTo(HOST + ":" + port())));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/polarplot.jpeg")));

        api("api/autotrack.json");
        poll(s, 7);
        // 35 s of pass: a plot at the first poll, then every 15 s.
        WIREMOCK.verify(3, getRequestedFor(urlPathEqualTo("/polarplot.jpeg")));
        assertThat(ctx.held).startsWith("hold " + JPEG.length);

        api("api/autotrack-idle.json");
        poll(s, 2);
        assertThat(ctx.held).last().isEqualTo("clear");
        WIREMOCK.verify(3, getRequestedFor(urlPathEqualTo("/polarplot.jpeg")));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/fft.jpeg")));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/schedule.jpeg")));

        List<NormalizedRecord> records = ctx.drain();
        assertThat(records).filteredOn(r -> r.kind().equals(PassBook.KIND))
                .extracting(r -> r.payload().get("status")).containsExactly("receiving", "recorded");
        assertThat(records).filteredOn(r -> r.kind().equals(GroundStationRecords.KIND))
                .extracting(r -> r.payload().get("state")).containsExactly("idle", "locked", "idle");
        assertThat(ctx.state.get(StationStore.SESSIONS)).isPresent();
        assertThat(ctx.state.get(StationStore.ACTIVE)).isEmpty();
    }

    @Test
    @DisplayName("live mode: no polar plot, ever; the session ends after two idle polls")
    void liveNoPlot() throws Exception {
        SatDumpStation s = ctx.station();
        api("api/live.json");
        poll(s, 6);
        WIREMOCK.stubFor(get(urlPathEqualTo("/api")).willReturn(aResponse().withBody("{}")));
        poll(s, 2);
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/polarplot.jpeg")));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/fft.jpeg")));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/schedule.jpeg")));
        assertThat(ctx.drain()).filteredOn(r -> r.kind().equals(PassBook.KIND))
                .extracting(r -> r.payload().get("status")).containsExactly("receiving", "recorded");
    }

    @Test
    @DisplayName("three unreachable polls make the source failing; a good poll clears it")
    void unreachable() throws Exception {
        SatDumpStation s = ctx.station();
        WIREMOCK.stubFor(get(urlPathEqualTo("/api")).willReturn(aResponse().withStatus(500)));
        poll(s, 2);
        assertThat(ctx.state.get("health.failing")).isEmpty();
        poll(s, 1);
        assertThat(ctx.state.get("health.failing")).get().asString().startsWith("SatDump /api unreachable for 3");

        api("api/autotrack-idle.json");
        poll(s, 1);
        assertThat(ctx.state.get("health.failing")).isEmpty();
    }

    @Test
    @DisplayName("a drifted /api degrades with the version-drift reason")
    void drift() throws Exception {
        SatDumpStation s = ctx.station();
        WIREMOCK.stubFor(get(urlPathEqualTo("/api")).willReturn(aResponse().withBody("{\"live_pipeline\":{}}")));
        poll(s, 1);
        assertThat(ctx.state.get("health.degraded")).contains(SatDumpApiParser.DRIFT_REASON);
        api("api/autotrack-idle.json");
        poll(s, 1);
        assertThat(ctx.state.get("health.degraded")).isEmpty();
    }

    @Test
    @DisplayName("every inbox scan consumes one unit of the real budget")
    void budgetPerScan() throws Exception {
        Path station = Files.createDirectories(temp.resolve("inbox/satdump/home"));
        ctx.params.put(SatDumpDescriptor.INBOX_PATH, station.toRealPath());
        SatDumpTestContext.Recording rasters = ctx.rasters;
        assertThat(rasters.commits).isEmpty();
        SatDumpStation s = new SatDumpAdapter("home", () -> null, ctx::holder).newStation(new ChargingContext(ctx),
                ctx.sink());
        long before = budgets.status(ctx.sourceId()).minuteRemaining();
        s.scanInboxOnce();
        s.scanInboxOnce();
        assertThat(budgets.status(ctx.sourceId()).minuteRemaining()).isEqualTo(before - 2);
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/api")));
    }

    /** The test context, but charging through the real client's {@code chargeRequest}. */
    static final class ChargingContext extends DelegatingContext {

        ChargingContext(SatDumpTestContext delegate) {
            super(delegate);
        }

        @Override
        public void chargeRequest(String label) throws ai.foresignal.source.spi.AdapterException {
            http().chargeRequest(label);
        }
    }
}
