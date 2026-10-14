package ai.foresignal.source.adapters.faa;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.MultiPolygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code faa-tfr} against a stubbed tfr.faa.gov through the real upstream client (prompt 087): details fetched only
 * for new or changed list rows, the 40-fetch cap, a tombstone when a row leaves the list, the underscore form of the
 * detail URL, the daily refresh, a 404 detail retried later, and the records' geometry and validity.
 */
@IntegrationTest
class FaaTfrAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T04:30:00Z");
    static final String LIST = "/tfr.faa.gov/tfrapi/exportTfrList";
    static final String DETAIL = "/tfr.faa.gov/download/detail_";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    FaaTfrAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1000, 100_000));
        adapter = new FaaTfrAdapter(baseUrl());
        harness.reset(adapter.id(), FaaTfrAdapter.ROWS_KEY);
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
    }

    /** The captured list rows of {@code ids}, in the captured order. */
    private static ArrayNode rows(String... ids) {
        Set<String> wanted = Set.of(ids);
        ArrayNode out = JsonNodeFactory.instance.arrayNode();
        for (JsonNode row : jsonMapper().readTree(AdapterFixtures.bytes("faa-tfr", "exportTfrList.json"))) {
            if (wanted.contains(row.get("notam_id").asString())) {
                out.add(row);
            }
        }
        return out;
    }

    private static JsonMapper jsonMapper() {
        return JsonMapper.builder().build();
    }

    private static void serveList(ArrayNode rows) {
        WIREMOCK.stubFor(get(urlPathEqualTo(LIST)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withHeader("Cache-Control", "max-age=0, no-cache")
                .withBody(rows.toString().getBytes(StandardCharsets.UTF_8))));
    }

    private static void serveDetails(String... ids) {
        for (String id : ids) {
            String file = "detail_" + id.replace('/', '_') + ".xml";
            WIREMOCK.stubFor(get(urlPathEqualTo(DETAIL + id.replace('/', '_') + ".xml")).willReturn(aResponse()
                    .withStatus(200).withHeader("Content-Type", "text/xml").withBody(AdapterFixtures.bytes("faa-tfr",
                            file))));
        }
    }

    private FetchResult fetch() throws Exception {
        return adapter.fetch(harness.context(adapter, Map.of()));
    }

    @Test
    @DisplayName("details are fetched for new rows, then only for a changed row; an unchanged list emits nothing")
    void onlyNewOrChanged() throws Exception {
        serveList(rows("6/9375", "4/3634", "6/8928"));
        serveDetails("6/9375", "4/3634", "6/8928");

        FetchResult first = fetch();
        assertThat(first.complete()).isTrue();
        assertThat(first.records()).extracting(NormalizedRecord::externalId)
                .containsExactlyInAnyOrder("6/9375", "4/3634", "6/8928");
        WIREMOCK.verify(3, getRequestedFor(urlPathMatching(DETAIL + ".*")));

        WIREMOCK.resetRequests();
        FetchResult second = fetch();
        assertThat(second.notModified()).isTrue();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(LIST)));
        WIREMOCK.verify(0, getRequestedFor(urlPathMatching(DETAIL + ".*")));

        ArrayNode changed = rows("6/9375", "4/3634", "6/8928");
        ((ObjectNode) changed.get(0)).put("description", "11NM SE SENECA, OR, amended");
        serveList(changed);
        WIREMOCK.resetRequests();
        FetchResult third = fetch();
        assertThat(third.records()).extracting(NormalizedRecord::externalId).containsExactly("6/9375");
        WIREMOCK.verify(1, getRequestedFor(urlPathMatching(DETAIL + ".*")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(DETAIL + "6_9375.xml")));
    }

    @Test
    @DisplayName("at most 40 detail fetches per run; the rest follow on the next run")
    void fortyCap() throws Exception {
        ArrayNode many = JsonNodeFactory.instance.arrayNode();
        for (int i = 1; i <= 45; i++) {
            ObjectNode row = many.addObject();
            row.put("notam_id", String.format("9/%04d", i));
            row.put("type", "SECURITY");
            row.put("facility", "ZHU");
            row.put("state", "TX");
            row.put("description", "SYNTHETIC " + i);
            row.put("creation_date", "10/13/2026");
        }
        serveList(many);
        WIREMOCK.stubFor(get(urlPathMatching(DETAIL + "9_\\d{4}\\.xml")).willReturn(aResponse().withStatus(200)
                .withBody(AdapterFixtures.bytes("faa-tfr", "detail_9_0001.xml"))));

        FetchResult first = fetch();
        assertThat(first.records()).hasSize(FaaTfrAdapter.MAX_DETAILS);
        assertThat(first.complete()).as("the cap leaves rows for later").isFalse();
        WIREMOCK.verify(FaaTfrAdapter.MAX_DETAILS, getRequestedFor(urlPathMatching(DETAIL + ".*")));

        WIREMOCK.resetRequests();
        FetchResult second = fetch();
        assertThat(second.records()).hasSize(5);
        assertThat(second.complete()).isTrue();
        WIREMOCK.verify(5, getRequestedFor(urlPathMatching(DETAIL + ".*")));
    }

    @Test
    @DisplayName("a row that leaves the list is tombstoned at once, and only once")
    void tombstoneOnRemoval() throws Exception {
        serveList(rows("6/9375", "4/3634", "6/8928"));
        serveDetails("6/9375", "4/3634", "6/8928");
        fetch();

        serveList(rows("6/9375", "6/8928"));
        FetchResult after = fetch();
        assertThat(after.records()).singleElement().satisfies(r -> {
            assertThat(r.externalId()).isEqualTo("4/3634");
            assertThat(r.tombstone()).isTrue();
            assertThat(r.kind()).isEqualTo(FaaTfrAdapter.KIND);
        });
        assertThat(fetch().notModified()).as("no second tombstone").isTrue();
    }

    @Test
    @DisplayName("the detail URL replaces the slash with an underscore")
    void detailUrl() throws Exception {
        assertThat(FaaTfrAdapter.detailUrl("6/8784")).isEqualTo("https://tfr.faa.gov/download/detail_6_8784.xml");
        serveList(rows("6/9375"));
        serveDetails("6/9375");
        fetch();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/tfr.faa.gov/download/detail_6_9375.xml")));
    }

    @Test
    @DisplayName("a detail older than 24 hours is fetched again although its row is unchanged")
    void dailyRefresh() throws Exception {
        serveList(rows("6/9375"));
        serveDetails("6/9375");
        fetch();
        harness.clock.advance(Duration.ofHours(25));
        WIREMOCK.resetRequests();
        assertThat(fetch().records()).extracting(NormalizedRecord::externalId).containsExactly("6/9375");
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(DETAIL + "6_9375.xml")));
    }

    @Test
    @DisplayName("a 404 detail is skipped and retried on the next run; the run is partial")
    void missingDetail() throws Exception {
        serveList(rows("6/9375", "4/3634"));
        serveDetails("6/9375");
        WIREMOCK.stubFor(get(urlPathEqualTo(DETAIL + "4_3634.xml")).willReturn(aResponse().withStatus(404)));

        FetchResult first = fetch();
        assertThat(first.complete()).isFalse();
        assertThat(first.records()).extracting(NormalizedRecord::externalId).containsExactly("6/9375");

        serveDetails("4/3634");
        assertThat(fetch().records()).extracting(NormalizedRecord::externalId).containsExactly("4/3634");
    }

    @Test
    @DisplayName("records: XNOTAM geometry, validity, vertical limits and the list's type, facility and state")
    void records() throws Exception {
        serveList(rows("6/9375", "6/8928", "4/3634"));
        serveDetails("6/9375", "6/8928", "4/3634");
        List<NormalizedRecord> records = new ArrayList<>(fetch().records());

        NormalizedRecord seneca = records.stream().filter(r -> r.externalId().equals("6/9375")).findFirst().orElseThrow();
        assertThat(seneca.observedAt()).isEqualTo(Instant.parse("2026-10-14T01:12:41Z"));
        assertThat(seneca.validFrom()).isEqualTo(Instant.parse("2026-10-14T15:00:00Z"));
        assertThat(seneca.validUntil()).isEqualTo(Instant.parse("2026-10-19T03:00:00Z"));
        assertThat(seneca.payload()).containsEntry("notamId", "6/9375").containsEntry("tfrType", "HAZARDS")
                .containsEntry("facility", "ZLC").containsEntry("state", "OR").containsEntry("city", "11NM SE SENECA")
                .containsEntry("regulation", "91.137(a)(1)").containsEntry("lowerFt", 0).containsEntry("upperFt", 6100)
                .containsEntry("lowerRef", "ALT").containsEntry("upperRef", "ALT");
        assertThat((String) seneca.payload().get("description")).startsWith("!FDC 6/9375");

        NormalizedRecord atlanta = records.stream().filter(r -> r.externalId().equals("6/8928")).findFirst()
                .orElseThrow();
        assertThat(atlanta.geometry()).isInstanceOf(MultiPolygon.class);
        assertThat(atlanta.payload()).as("the id comes from the list, not txtLocalName")
                .containsEntry("notamId", "6/8928");

        NormalizedRecord disney = records.stream().filter(r -> r.externalId().equals("4/3634")).findFirst()
                .orElseThrow();
        assertThat(disney.validUntil()).as("until further notice").isNull();
        assertThat(disney.validFrom()).isNull();
        FieldSpecContract.assertPayloadKeysDeclared(adapter.descriptor(), records);
    }

    @Test
    @DisplayName("the descriptor: valid, 15-minute poll, 45/min and 2,000/day, event retention, embedded text")
    void descriptor() {
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(adapter.descriptor().defaultInterval()).isEqualTo(Duration.ofMinutes(15));
        assertThat(adapter.descriptor().defaultBudget()).isEqualTo(Budget.of(45, 2000));
        assertThat(adapter.descriptor().defaultRetention()).isEqualTo(Duration.ofDays(1825));
        assertThat(adapter.descriptor().titleField()).isEqualTo("description");
        assertThat(adapter.descriptor().embedFields()).containsExactly("description");
        assertThat(adapter.descriptor().tileProperties()).containsExactly("tfrType", "lowerFt", "upperFt");
    }
}
