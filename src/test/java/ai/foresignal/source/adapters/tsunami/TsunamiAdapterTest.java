package ai.foresignal.source.adapters.tsunami;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.formats.cap.CapAlert;
import ai.foresignal.source.formats.cap.CapParser;
import ai.foresignal.source.formats.cap.CapThreadResolver;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.weather.WeatherAdapterHarness;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code tsunami-warnings} against a stubbed {@code www.tsunami.gov} through the real upstream client (prompt 067): two
 * CAP fetches, the Atom fallback and its host and path allowlist, the cancellation thread as three versions of one
 * record ending {@code cancelled}, and an unrelated next event that does not close the first.
 */
@IntegrationTest
class TsunamiAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-13T21:05:00Z");
    static final String ID = TsunamiWarningsAdapter.ID;
    static final String PAAQ_CAP = "/events/xml/PAAQCAP.xml";
    static final String PHEB_CAP = "/events/xml/PHEBCAP.xml";
    static final String PAAQ_ATOM = "/events/xml/PAAQAtom.xml";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    MutableClock clock;
    WeatherAdapterHarness.Upstream upstream;
    WeatherAdapterHarness.MapState state;
    TsunamiWarningsAdapter adapter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        upstream = WeatherAdapterHarness.upstream(jdbc, jsonMapper, properties, install, clock, Budget.of(100, 1_000), null);
        state = new WeatherAdapterHarness.MapState();
        adapter = new TsunamiWarningsAdapter(baseUrl());
    }

    @AfterEach
    void tearDown() {
        upstream.backoff().onSuccess(ID);
    }

    private WeatherAdapterHarness.Context context() {
        return new WeatherAdapterHarness.Context(adapter.descriptor(),
                upstream.client().forSource(ID, adapter.descriptor()), ValidatedParams.empty(), state, clock);
    }

    private static void serve(String path, byte[] body, String contentType) {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(path)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", contentType).withHeader("Last-Modified", "Tue, 13 Oct 2026 12:36:35 GMT")
                .withBody(body)));
    }

    private static void serveFixture(String path, String fixture) {
        serve(path, AdapterFixtures.bytes(ID, fixture), "text/xml");
    }

    private static void notModified(String path) {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(path)).willReturn(aResponse().withStatus(304)));
    }

    /** The Atom fixture with its links moved to the stub server. */
    private static byte[] atom(String fixture) {
        return AdapterFixtures.text(ID, fixture).replace("https://www.tsunami.gov", baseUrl())
                .getBytes(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("both CAP URLs are fetched in turn; the captured messages become one record per incident")
    void twoCapFetches() throws Exception {
        serveFixture(PAAQ_CAP, "PAAQCAP.xml");
        serveFixture(PHEB_CAP, "PHEBCAP.xml");

        FetchResult result = adapter.fetch(context());

        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(PAAQ_CAP)));
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(PHEB_CAP)));
        WIREMOCK.verify(2, anyRequestedFor(anyUrl()));
        assertThat(result.complete()).isFalse();
        assertThat(result.records()).extracting(NormalizedRecord::externalId).containsExactly("PAAQ:tmuiel", "PHEB:26286001");
        NormalizedRecord paaq = result.records().get(0);
        assertThat(paaq.kind()).isEqualTo("tsunami-warning");
        assertThat(paaq.observedAt()).isEqualTo(Instant.parse("2026-10-13T12:36:06Z"));
        assertThat(paaq.validUntil()).isEqualTo(Instant.parse("2026-10-13T13:36:06Z"));
        assertThat(paaq.validFrom()).as("no onset and no effective").isNull();
        assertThat(paaq.geometry()).isInstanceOf(Point.class);
        assertThat(paaq.payload())
                .containsEntry("center", "PAAQ")
                .containsEntry("messageType", "Information Statement")
                .containsEntry("state", "active")
                .containsEntry("epicenter", Map.of("lat", -0.2, "lon", -19.2))
                .containsEntry("parameters", Map.of(
                        "EventPreliminaryMagnitude", List.of("6.8"),
                        "EventOriginTime", List.of("2026-10-13T12:23:11-00:00"),
                        "EventLatLon", List.of("-0.200,-19.200 0.000")));
        NormalizedRecord pheb = result.records().get(1);
        assertThat(pheb.validFrom()).isEqualTo(Instant.parse("2026-10-13T12:34:39Z"));
        assertThat(pheb.eventTime()).isEqualTo(Instant.parse("2026-10-13T12:34:39Z"));
        assertThat(pheb.payload()).doesNotContainKey("headline");
        assertThat(((Map<?, ?>) pheb.payload().get("parameters")).containsKey("NWSUGC")).isTrue();
        FieldSpecContract.assertPayloadKeysDeclared(adapter.descriptor(), result.records());
    }

    @Test
    @DisplayName("304 from both centers is not modified")
    void bothNotModified() throws Exception {
        notModified(PAAQ_CAP);
        notModified(PHEB_CAP);

        FetchResult result = adapter.fetch(context());

        assertThat(result.notModified()).isTrue();
        assertThat(result.records()).isEmpty();
    }

    @Test
    @DisplayName("a CAP URL that is not a single alert falls back to the Atom feed and follows the allowed CAP link")
    void atomFallback() throws Exception {
        serve(PAAQ_CAP, atom("PAAQAtom.xml"), "text/xml");
        serve(PAAQ_ATOM, atom("PAAQAtom.xml"), "application/atom+xml");
        serveFixture("/events/PAAQ/2026/10/13/tmuiel/1/WEXX32/PAAQCAP.xml", "PAAQCAP.xml");
        notModified(PHEB_CAP);

        FetchResult result = adapter.fetch(context());

        assertThat(result.records()).extracting(NormalizedRecord::externalId).containsExactly("PAAQ:tmuiel");
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(PAAQ_ATOM)));
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo("/events/PAAQ/2026/10/13/tmuiel/1/WEXX32/PAAQCAP.xml")));
    }

    @Test
    @DisplayName("an Atom CAP link to another host or path is never followed")
    void atomAllowlist() {
        // The captured links name https://www.tsunami.gov, not the stub server's base URL, so they are refused.
        serve(PAAQ_CAP, atom("PAAQAtom.xml"), "text/xml");
        serve(PAAQ_ATOM, AdapterFixtures.bytes(ID, "PAAQAtom.xml"), "application/atom+xml");

        assertThatThrownBy(() -> adapter.fetch(context())).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("outside the allowed");
        WIREMOCK.verify(2, anyRequestedFor(anyUrl()));
        assertThat(state.get(SourceHttp.LAST_MODIFIED_STATE_PREFIX + TsunamiWarningsAdapter.CAP_STATE_PREFIX + "PAAQ"))
                .as("validators forgotten").isEmpty();

        String base = baseUrl();
        assertThat(adapter.allowedCapLink("PAAQ", base + "/events/PAAQ/2026/10/13/tmuiel/1/WEXX32/PAAQCAP.xml")).isTrue();
        assertThat(adapter.allowedCapLink("PAAQ", base + "/events/PHEB/2026/10/13/x/1/W/PHEBCAP.xml")).isFalse();
        assertThat(adapter.allowedCapLink("PAAQ", base + "/events/PAAQ/../../etc/PAAQCAP.xml")).isFalse();
        assertThat(adapter.allowedCapLink("PAAQ", base + "/events/PAAQ/2026/x.jpg")).isFalse();
        assertThat(adapter.allowedCapLink("PAAQ", base + "/events/PAAQ/2026/PAAQCAP.xml?x=1")).isFalse();
        assertThat(adapter.allowedCapLink("PAAQ", "https://evil.example/events/PAAQ/2026/PAAQCAP.xml")).isFalse();
        TsunamiWarningsAdapter production = new TsunamiWarningsAdapter("");
        assertThat(production.allowedCapLink("PHEB",
                "https://www.tsunami.gov/events/PHEB/2026/10/13/26286001/1/WECA42/PHEBCAP.xml")).isTrue();
        assertThat(production.allowedCapLink("PHEB",
                "http://www.tsunami.gov/events/PHEB/2026/10/13/26286001/1/WECA42/PHEBCAP.xml")).isFalse();
    }

    @Test
    @DisplayName("the cancellation thread: three versions of one record, the last canceled and expiring at its expires")
    void cancellationThread() throws Exception {
        notModified(PHEB_CAP);
        List<NormalizedRecord> versions = new ArrayList<>();
        for (String fixture : List.of("thread-1-warning.xml", "thread-2-update.xml", "thread-3-cancellation.xml")) {
            serveFixture(PAAQ_CAP, fixture);
            versions.addAll(adapter.fetch(context()).records());
            clock.advance(Duration.ofMinutes(30));
        }

        assertThat(versions).hasSize(3).extracting(NormalizedRecord::externalId).containsOnly("PAAQ:tsthr01");
        assertThat(versions).noneMatch(NormalizedRecord::tombstone);
        assertThat(versions).extracting(r -> r.payload().get("state")).containsExactly("active", "updated", "cancelled");
        assertThat(versions).extracting(r -> r.payload().get("messageType")).containsExactly("Warning", "Warning", null);
        NormalizedRecord canceled = versions.get(2);
        assertThat(canceled.validUntil()).isEqualTo(Instant.parse("2026-10-13T22:00:00Z"));
        assertThat(canceled.payload()).containsEntry("event", "Tsunami Cancellation").containsEntry("references", 2);
        assertThat(state.get(CapThreadResolver.STATE_KEY)).isPresent();
    }

    @Test
    @DisplayName("an unrelated next event is a new record; the first thread is neither closed nor tombstoned")
    void unrelatedNextEvent() throws Exception {
        notModified(PHEB_CAP);
        serveFixture(PAAQ_CAP, "thread-1-warning.xml");
        NormalizedRecord warning = adapter.fetch(context()).records().get(0);
        serveFixture(PAAQ_CAP, "unrelated-next-event.xml");

        FetchResult next = adapter.fetch(context());

        assertThat(next.records()).singleElement().satisfies(r -> {
            assertThat(r.externalId()).isEqualTo("PAAQ:tsnext1");
            assertThat(r.tombstone()).isFalse();
        });
        assertThat(warning.validUntil()).as("the warning expires on its own").isEqualTo(Instant.parse("2026-10-14T08:00:00Z"));
    }

    @Test
    @DisplayName("Test messages are skipped; a malformed document is a format error that forgets the validators")
    void skippedAndMalformed() throws Exception {
        String test = AdapterFixtures.text(ID, "thread-1-warning.xml").replace("<status>Actual</status>", "<status>Test</status>");
        serve(PAAQ_CAP, test.getBytes(StandardCharsets.UTF_8), "text/xml");
        notModified(PHEB_CAP);
        FetchResult skipped = adapter.fetch(context());
        assertThat(skipped.records()).isEmpty();
        assertThat(skipped.notModified()).isFalse();

        serve(PAAQ_CAP, "<alert xmlns=\"urn:oasis:names:tc:emergency:cap:1.2\"><identifier>x</identifier></alert>"
                .getBytes(StandardCharsets.UTF_8), "text/xml");
        assertThatThrownBy(() -> adapter.fetch(context())).isInstanceOf(UpstreamFormatException.class);
        assertThat(state.get(SourceHttp.LAST_MODIFIED_STATE_PREFIX + TsunamiWarningsAdapter.CAP_STATE_PREFIX + "PAAQ"))
                .isEmpty();
    }

    @Test
    @DisplayName("thread keys, the cancellation rule, message types and the epicenter")
    void mappingRules() {
        CapAlert paaq = CapParser.parse(AdapterFixtures.bytes(ID, "PAAQCAP.xml"));
        assertThat(TsunamiWarningsAdapter.threadKey("PAAQ", paaq)).isEqualTo("PAAQ:tmuiel");
        CapAlert noIncidents = new CapAlert("1.2", "PAAQ-4-evt9", "s", paaq.sent(), "Actual", "Alert", null, "Public",
                null, null, List.of(), null, List.of(), null, List.of());
        assertThat(TsunamiWarningsAdapter.threadKey("PAAQ", noIncidents)).isEqualTo("PAAQ:evt9");
        CapAlert neither = new CapAlert("1.2", "other-id", "s", paaq.sent(), "Actual", "Alert", null, "Public",
                null, null, List.of(), null, List.of(), null, List.of());
        assertThat(TsunamiWarningsAdapter.threadKey("PAAQ", neither)).isNull();

        CapAlert cancel = CapParser.parse(AdapterFixtures.bytes(ID, "thread-3-cancellation.xml"));
        assertThat(TsunamiWarningsAdapter.cancels(cancel, cancel.firstInfo())).isTrue();
        CapAlert update = CapParser.parse(AdapterFixtures.bytes(ID, "thread-2-update.xml"));
        assertThat(TsunamiWarningsAdapter.cancels(update, update.firstInfo())).isFalse();
        String vtecOnly = AdapterFixtures.text(ID, "thread-2-update.xml").replace("/O.CON.", "/O.CAN.");
        CapAlert byVtec = CapParser.parse(vtecOnly.getBytes(StandardCharsets.UTF_8));
        assertThat(TsunamiWarningsAdapter.cancels(byVtec, byVtec.firstInfo())).isTrue();
        assertThat(TsunamiWarningsAdapter.cancels(noIncidents, null)).isFalse();

        assertThat(TsunamiWarningsAdapter.messageType("Tsunami Warning")).isEqualTo("Warning");
        assertThat(TsunamiWarningsAdapter.messageType("Tsunami Advisory")).isEqualTo("Advisory");
        assertThat(TsunamiWarningsAdapter.messageType("Tsunami Watch")).isEqualTo("Watch");
        assertThat(TsunamiWarningsAdapter.messageType("Tsunami Information")).isEqualTo("Information Statement");
        assertThat(TsunamiWarningsAdapter.messageType("Tsunami Threat Message")).isEqualTo("Threat");
        assertThat(TsunamiWarningsAdapter.messageType("Tsunami Cancellation")).isNull();
        assertThat(TsunamiWarningsAdapter.epicenter(paaq.firstInfo())).containsExactly(-0.2, -19.2);
    }

    @Test
    @DisplayName("the descriptor is valid and carries the prompt's values")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.id()).isEqualTo("tsunami-warnings");
        assertThat(d.kinds()).containsExactly("tsunami-warning");
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofMinutes(2));
        assertThat(d.minInterval()).isEqualTo(Duration.ofMinutes(1));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(4, 3_000));
        assertThat(d.recordTtl()).isNull();
        assertThat(d.zeroResultsNormal()).isTrue();
        assertThat(d.embedFields()).containsExactly("headline", "description", "instruction");
        assertThat(d.licence()).isEqualTo("US-PD");
    }
}
