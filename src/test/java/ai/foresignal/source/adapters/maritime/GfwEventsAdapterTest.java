package ai.foresignal.source.adapters.maritime;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.DefaultFetchContext;
import ai.foresignal.collect.FetchTrace;
import ai.foresignal.collect.RecordedExchange;
import ai.foresignal.collect.RequestRecorder;
import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code gfw-events} against a stubbed GFW gateway through the real upstream client (prompt 088): the Bearer token
 * (never in a URL or a recorded exchange), the dataset and date parameters, paging by {@code nextOffset} until
 * {@code total}, the per-run page cap and its resume, the POST form with a box, a 401 as rejected credentials, and a
 * 429 backing the source off. The fixtures are GFW's documented example responses (synthetic; see their README).
 */
@IntegrationTest
class GfwEventsAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T06:00:00Z");
    static final String PATH = "/gateway.api.globalfishingwatch.org/v3/events";
    static final String TOKEN = "gfw-test-token-0123456789abcdef";
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    GfwEventsAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1000, 100_000));
        adapter = new GfwEventsAdapter(baseUrl());
        harness.reset(adapter.id(), GfwEventsAdapter.CURSOR_KEY);
        harness.credentials.clear();
        harness.credentials.put(GfwEventsAdapter.TOKEN, TOKEN);
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
        harness.credentials.clear();
    }

    /** A page of {@code count} copies of the fixture's entry with distinct ids. */
    private static String page(String fixture, int offset, int count, Integer next, int total) {
        ObjectNode root = (ObjectNode) JSON.readTree(AdapterFixtures.bytes(GfwEventsAdapter.ID, fixture));
        ObjectNode template = (ObjectNode) root.get("entries").get(0);
        ArrayNode entries = root.putArray("entries");
        for (int i = 0; i < count; i++) {
            ObjectNode e = template.deepCopy();
            e.put("id", template.get("id").asString() + "-" + (offset + i));
            entries.add(e);
        }
        root.put("offset", offset);
        root.put("total", total);
        if (next == null) {
            root.putNull("nextOffset");
        } else {
            root.put("nextOffset", next);
        }
        return root.toString();
    }

    private static void serveGet(String dataset, int offset, String body) {
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).withQueryParam("datasets[0]", equalTo(dataset))
                .withQueryParam("offset", equalTo(String.valueOf(offset))).willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json").withBody(body)));
    }

    private FetchResult fetch(Map<String, Object> params) throws Exception {
        return adapter.fetch(harness.context(adapter, params));
    }

    @Test
    @DisplayName("GET form: Bearer token, dataset and window parameters, paging until total")
    void pagingToTotal() throws Exception {
        String ds = GfwEventsAdapter.FISHING_DATASET;
        serveGet(ds, 0, page("fishing-events.json", 0, 500, 500, 1200));
        serveGet(ds, 500, page("fishing-events.json", 500, 500, 1000, 1200));
        serveGet(ds, 1000, page("fishing-events.json", 1000, 200, 1200, 1200));

        FetchResult result = fetch(Map.of());
        assertThat(result.records()).hasSize(1200).allMatch(r -> r.kind().equals(GfwEventsAdapter.FISHING_KIND));
        WIREMOCK.verify(3, getRequestedFor(urlPathEqualTo(PATH))
                .withHeader("Authorization", equalTo("Bearer " + TOKEN))
                .withQueryParam("start-date", equalTo("2026-10-04"))
                .withQueryParam("end-date", equalTo("2026-10-14"))
                .withQueryParam("limit", equalTo("500")));
        assertThat(WIREMOCK.getAllServeEvents()).noneMatch(e -> e.getRequest().getUrl().contains(TOKEN));
        assertThat(harness.client.stateStore(adapter.id()).get(GfwEventsAdapter.CURSOR_KEY)).isEmpty();
    }

    @Test
    @DisplayName("records: kind by event type, times, point, vessel fields, encounter partner, port name, license")
    void records() throws Exception {
        serveGet(GfwEventsAdapter.FISHING_DATASET, 0, new String(AdapterFixtures.bytes(GfwEventsAdapter.ID,
                "fishing-events.json")).replace("\"nextOffset\": 1", "\"nextOffset\": null"));
        serveGet(GfwEventsAdapter.ENCOUNTERS_DATASET, 0, page("encounters-events.json", 0, 1, null, 1));
        serveGet(GfwEventsAdapter.LOITERING_DATASET, 0, page("loitering-events.json", 0, 1, null, 1));
        serveGet(GfwEventsAdapter.PORT_VISITS_DATASET, 0, page("port-visits-events.json", 0, 1, null, 1));
        FetchResult result = fetch(Map.of(GfwEventsAdapter.DATASETS_PARAM, GfwEventsAdapter.ALLOWED_DATASETS));
        assertThat(result.records()).extracting(NormalizedRecord::kind).containsExactly(GfwEventsAdapter.FISHING_KIND,
                GfwEventsAdapter.ENCOUNTER_KIND, GfwEventsAdapter.LOITERING_KIND, GfwEventsAdapter.PORT_VISIT_KIND);
        NormalizedRecord fishing = result.records().get(0);
        assertThat(fishing.externalId()).isEqualTo("718a4d93c16f78d9a54e9bc56d20c7e0");
        assertThat(fishing.eventTime()).isEqualTo(Instant.parse("2017-01-18T23:56:16Z"));
        assertThat(fishing.observedAt()).isEqualTo(Instant.parse("2017-01-19T03:42:27Z"));
        assertThat(fishing.geometry().getCoordinate().y).isEqualTo(-46.1538);
        assertThat(fishing.payload()).containsEntry("mmsi", "701000948").containsEntry("vesselName", "CLAUDINA")
                .containsEntry("vesselId", "9b3e9019d-d67f-005a-9593-b66b997559e5").containsEntry("distanceKm", 2.14)
                .containsEntry("durationH", 3.77).containsEntry("start", "2017-01-18T23:56:16Z");
        assertThat(result.records().get(1).payload()).containsEntry("encounterMmsi", "273355780");
        assertThat(result.records().get(2).payload()).containsEntry("distanceKm", 0.68);
        assertThat(result.records().get(3).payload()).containsEntry("portName", "VIGO");
        FieldSpecContract.assertPayloadKeysDeclared(adapter.descriptor(), result.records());
        assertThat(adapter.descriptor().licence()).isEqualTo("CC BY-NC 4.0");
    }

    @Test
    @DisplayName("the page cap: a long window stops after 10 pages and the next run resumes the same window")
    void pageCapAndResume() throws Exception {
        String ds = GfwEventsAdapter.FISHING_DATASET;
        int total = 12 * 500;
        for (int i = 0; i < 12; i++) {
            int offset = i * 500;
            serveGet(ds, offset, page("fishing-events.json", offset, 500, offset + 500, total));
        }
        FetchResult first = fetch(Map.of());
        assertThat(first.records()).hasSize(10 * 500);
        assertThat(harness.client.stateStore(adapter.id()).get(GfwEventsAdapter.CURSOR_KEY)).isPresent();

        harness.clock.advance(Duration.ofDays(1));
        WIREMOCK.resetRequests();
        FetchResult second = fetch(Map.of());
        assertThat(second.records()).hasSize(2 * 500);
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(PATH)).withQueryParam("start-date", equalTo("2026-10-04")));
        assertThat(harness.client.stateStore(adapter.id()).get(GfwEventsAdapter.CURSOR_KEY)).isEmpty();
    }

    @Test
    @DisplayName("a saved cursor whose window ended before the configured window starts is dropped: a fresh window")
    void staleCursorStartsFresh() throws Exception {
        String ds = GfwEventsAdapter.FISHING_DATASET;
        int total = 12 * 500;
        for (int i = 0; i < 12; i++) {
            int offset = i * 500;
            serveGet(ds, offset, page("fishing-events.json", offset, 500, offset + 500, total));
        }
        fetch(Map.of());
        assertThat(harness.client.stateStore(adapter.id()).get(GfwEventsAdapter.CURSOR_KEY)).isPresent();

        harness.clock.advance(Duration.ofDays(30));
        WIREMOCK.resetRequests();
        fetch(Map.of());
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(PATH)).withQueryParam("start-date", equalTo("2026-10-04")));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(PATH)).withQueryParam("start-date", equalTo("2026-11-03"))
                .withQueryParam("offset", equalTo("0")));
    }

    @Test
    @DisplayName("POST form with a box: the documented body with a GeoJSON polygon, paging in the query string")
    void postWithBox() throws Exception {
        WIREMOCK.stubFor(post(urlPathEqualTo(PATH)).withQueryParam("offset", equalTo("0"))
                .withQueryParam("limit", equalTo("500"))
                .withHeader("Authorization", equalTo("Bearer " + TOKEN))
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(equalToJson("{\"datasets\":[\"public-global-fishing-events:latest\"],"
                        + "\"startDate\":\"2026-10-04\",\"endDate\":\"2026-10-14\",\"geometry\":{\"type\":\"Polygon\","
                        + "\"coordinates\":[[[120.0,26.5],[122.5,26.5],[122.5,28.5],[120.0,28.5],[120.0,26.5]]]}}"))
                .willReturn(aResponse().withStatus(200).withBody(page("fishing-events-region.json", 0, 3, null, 3))));
        FetchResult result = fetch(Map.of(GfwEventsAdapter.BBOX_PARAM, new Envelope(120.0, 122.5, 26.5, 28.5)));
        assertThat(result.records()).hasSize(3);
        WIREMOCK.verify(1, postRequestedFor(urlPathEqualTo(PATH)));
    }

    @Test
    @DisplayName("a missing token and a 401 are rejected credentials; the token never reaches a recorded exchange")
    void credentials() {
        harness.credentials.clear();
        assertThatThrownBy(() -> fetch(Map.of())).isInstanceOf(CredentialsRejectedException.class);
        WIREMOCK.verify(0, anyRequestedFor(anyUrl()));

        harness.credentials.put(GfwEventsAdapter.TOKEN, TOKEN);
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(401)
                .withBody("{\"error\":\"invalid token\"}")));
        RequestRecorder recorder = new RequestRecorder();
        DefaultFetchContext ctx = harness.context(adapter, Map.of(), recorder);
        assertThatThrownBy(() -> adapter.fetch(ctx)).isInstanceOf(CredentialsRejectedException.class);
        assertThat(ctx.traces()).as("the 401 exchange was traced").isNotEmpty();
        assertThat(ctx.traces()).extracting(FetchTrace::redactedUrl).allSatisfy(u -> assertThat(u).doesNotContain(TOKEN));
        assertThat(recorder.exchanges()).as("the 401 exchange was recorded, so the loop below checks something")
                .isNotEmpty();
        for (RecordedExchange exchange : recorder.exchanges()) {
            assertThat(exchange.toString()).doesNotContain(TOKEN);
        }
    }

    @Test
    @DisplayName("a 429 puts the source in backoff (Retry-After) and nothing is sent while it lasts")
    void tooManyRequests() {
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(429)
                .withHeader("Retry-After", "600").withBody("Too Many Requests")));
        assertThatThrownBy(() -> fetch(Map.of())).isInstanceOfSatisfying(UpstreamHttpException.class,
                e -> assertThat(e.getStatus()).isEqualTo(429));
        assertThat(harness.backoff.isInBackoff(adapter.id())).isTrue();
        WIREMOCK.resetRequests();
        assertThatThrownBy(() -> fetch(Map.of())).isInstanceOf(AdapterException.class);
        WIREMOCK.verify(0, anyRequestedFor(anyUrl()));
    }

    @Test
    @DisplayName("parameters: only the event datasets, a window of 1 to 30 days; unknown event types are skipped")
    void paramsAndSkips() {
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(GfwEventsAdapter.DATASETS_PARAM,
                List.of("public-global-presence:latest"))))).containsKey("datasets");
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(GfwEventsAdapter.WINDOW_PARAM, 31))))
                .containsKey("windowDays");
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(GfwEventsAdapter.DATASETS_PARAM,
                List.of(GfwEventsAdapter.PORT_VISITS_DATASET), GfwEventsAdapter.WINDOW_PARAM, 5)))).isEmpty();
        assertThat(GfwEventsAdapter.record(JSON.readTree("{\"id\":\"x\",\"type\":\"gap\",\"start\":"
                + "\"2026-10-01T00:00:00Z\"}"))).isNull();
        assertThat(GfwEventsAdapter.record(JSON.readTree("{\"id\":\"x\",\"type\":\"fishing\"}"))).isNull();
        NormalizedRecord noEnd = GfwEventsAdapter.record(JSON.readTree("{\"id\":\"x\",\"type\":\"loitering\","
                + "\"start\":\"2026-10-01T00:00:00Z\"}"));
        assertThat(noEnd.observedAt()).isEqualTo(Instant.parse("2026-10-01T00:00:00Z"));
        assertThat(noEnd.geometry()).isNull();
    }

    @Test
    @DisplayName("the descriptor: required token, 1 h (min 30 min), 10/min and 300/day, interval fields, event retention")
    void descriptor() {
        var d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.requiredCredentialFields()).anyMatch(c -> c.name().equals(GfwEventsAdapter.TOKEN));
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofHours(1));
        assertThat(d.minInterval()).isEqualTo(Duration.ofMinutes(30));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(10, 300));
        assertThat(d.startField()).isEqualTo("start");
        assertThat(d.endField()).isEqualTo("end");
        assertThat(d.defaultRetention()).isEqualTo(Duration.ofDays(1825));
        List<String> kinds = new ArrayList<>(d.kinds());
        assertThat(kinds).containsExactlyInAnyOrder("fishing-event", "vessel-encounter", "loitering-event",
                "port-visit");
    }
}
