package ai.foresignal.source.adapters.faa;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.kit.feature.ArcGisQueryClient;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code faa-uasfm} against a stubbed ArcGIS layer through prompt 073's {@link ArcGisQueryClient} (prompt 087): offset
 * paging, the box required and sent as an envelope, incremental {@code LAST_EDIT IN (…)} reads, the 56-day full
 * reload with tombstones for missing cells, and the layer-unchanged short-circuit.
 */
@IntegrationTest
class UasfmAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T05:00:00Z");
    static final String LAYER = "/services6.arcgis.com/ssFJjBXIUyZDrSYZ/ArcGIS/rest/services/FAA_UAS_FacilityMap_Data"
            + "/FeatureServer/0";
    static final Envelope AUSTIN = new Envelope(-97.80, -97.70, 30.20, 30.30);
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    FaaUasfmAdapter adapter;
    ArrayNode page;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1000, 100_000));
        adapter = new FaaUasfmAdapter(baseUrl());
        harness.reset(adapter.id(), FaaUasfmAdapter.LAST_MAX_KEY, FaaUasfmAdapter.LAST_RUN_KEY,
                FaaUasfmAdapter.FULL_AT_KEY, FaaUasfmAdapter.BOX_KEY, FaaUasfmAdapter.IDS_KEY, ArcGisQueryClient.META_KEY,
                ArcGisQueryClient.LAST_EDIT_KEY);
        page = (ArrayNode) JSON.readTree(AdapterFixtures.bytes("faa-uasfm", "page-austin.json")).get("features");
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
    }

    private static void serveLayer(long lastEditDate) {
        ObjectNode layer = (ObjectNode) JSON.readTree(AdapterFixtures.bytes("faa-uasfm", "layer.json"));
        ObjectNode editing = (ObjectNode) layer.get("editingInfo");
        editing.put("lastEditDate", lastEditDate);
        editing.put("dataLastEditDate", lastEditDate);
        WIREMOCK.stubFor(get(urlPathEqualTo(LAYER)).withQueryParam("f", equalTo("json")).willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "application/json").withBody(layer.toString())));
    }

    private static String collection(List<?> features, boolean exceeded) {
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        root.put("type", "FeatureCollection");
        ArrayNode array = root.putArray("features");
        for (Object f : features) {
            array.add((tools.jackson.databind.JsonNode) f);
        }
        if (exceeded) {
            root.putObject("properties").put("exceededTransferLimit", true);
        }
        return root.toString();
    }

    private List<tools.jackson.databind.JsonNode> slice(int from, int to) {
        List<tools.jackson.databind.JsonNode> out = new java.util.ArrayList<>();
        for (int i = from; i < to; i++) {
            out.add(page.get(i));
        }
        return out;
    }

    /** The full read as two pages of 40 and 37 cells, the second at offset 40. */
    private void serveFull(List<tools.jackson.databind.JsonNode> features) {
        List<tools.jackson.databind.JsonNode> first = features.subList(0, Math.min(40, features.size()));
        List<tools.jackson.databind.JsonNode> rest = features.subList(first.size(), features.size());
        WIREMOCK.stubFor(get(urlPathEqualTo(LAYER + "/query")).withQueryParam("where", equalTo("1=1"))
                .withQueryParam("resultOffset", equalTo("0")).willReturn(aResponse().withStatus(200)
                        .withBody(collection(first, true))));
        WIREMOCK.stubFor(get(urlPathEqualTo(LAYER + "/query")).withQueryParam("where", equalTo("1=1"))
                .withQueryParam("resultOffset", equalTo(String.valueOf(first.size()))).willReturn(aResponse()
                        .withStatus(200).withBody(collection(rest, false))));
    }

    private FetchResult fetch(Envelope box) throws Exception {
        return adapter.fetch(harness.context(adapter, Map.of(FaaUasfmAdapter.BBOX_PARAM, box)));
    }

    @Test
    @DisplayName("a full read pages through the client with the box as an envelope; records carry the grid fields")
    void paging() throws Exception {
        serveLayer(1791942657524L);
        serveFull(slice(0, 77));

        FetchResult result = fetch(AUSTIN);
        assertThat(result.complete()).isTrue();
        assertThat(result.records()).hasSize(77).noneMatch(NormalizedRecord::tombstone);
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(LAYER + "/query")).withQueryParam("geometry",
                equalTo("-97.8,30.2,-97.7,30.3")).withQueryParam("geometryType", equalTo("esriGeometryEnvelope"))
                .withQueryParam("inSR", equalTo("4326")).withQueryParam("outSR", equalTo("4326"))
                .withQueryParam("f", equalTo("geojson")).withQueryParam("resultRecordCount", equalTo("2000")));
        NormalizedRecord first = result.records().get(0);
        assertThat(first.externalId()).isEqualTo("8dc9d8d1-9bfa-458b-bdba-8295f0bccd28");
        assertThat(first.kind()).isEqualTo(FaaUasfmAdapter.KIND);
        assertThat(first.observedAt()).isEqualTo(Instant.parse("2017-05-08T00:00:00Z"));
        assertThat(first.geometry()).isInstanceOf(Polygon.class);
        assertThat(first.payload()).containsEntry("ceilingFt", 400).containsEntry("airports", List.of("AUS"))
                .containsEntry("laanc", true).containsEntry("mapEffective", "2026-10-01");
        FieldSpecContract.assertPayloadKeysDeclared(adapter.descriptor(), result.records());
    }

    @Test
    @DisplayName("the box is required: refused at validation and at fetch, before any request")
    void boxRequired() {
        assertThat(adapter.checkParams(ValidatedParams.empty())).containsKey(FaaUasfmAdapter.BBOX_PARAM);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(FaaUasfmAdapter.BBOX_PARAM, AUSTIN,
                FaaUasfmAdapter.MAX_FEATURES_PARAM, 60_000)))).containsOnlyKeys(FaaUasfmAdapter.MAX_FEATURES_PARAM);
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of()))).isInstanceOf(AdapterException.class)
                .hasMessageContaining(FaaUasfmAdapter.BBOX_PARAM);
        WIREMOCK.verify(0, anyRequestedFor(anyUrl()));
        assertThat(adapter.descriptor().param(FaaUasfmAdapter.BBOX_PARAM).required()).isTrue();
        assertThat(adapter.descriptor().param(FaaUasfmAdapter.BBOX_PARAM).defaultValue()).isNull();
    }

    @Test
    @DisplayName("an unedited layer short-circuits: only the layer document is read")
    void unchangedLayer() throws Exception {
        serveLayer(1791942657524L);
        serveFull(slice(0, 77));
        fetch(AUSTIN);
        harness.clock.advance(Duration.ofDays(7));
        WIREMOCK.resetRequests();

        assertThat(fetch(AUSTIN).notModified()).isTrue();
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(LAYER + "/query")));
    }

    @Test
    @DisplayName("after a full read, LAST_EDIT IN (every day from a week before the last read to today); no tombstones")
    void incremental() throws Exception {
        serveLayer(1791942657524L);
        serveFull(slice(0, 77));
        fetch(AUSTIN);

        harness.clock.advance(Duration.ofDays(7));
        WIREMOCK.resetRequests();
        serveLayer(1792547457524L);
        ObjectNode edited = ((ObjectNode) page.get(0)).deepCopy();
        ((ObjectNode) edited.get("properties")).put("LAST_EDIT", "10/20/2026").put("CEILING", 200);
        ObjectNode added = ((ObjectNode) page.get(1)).deepCopy();
        ((ObjectNode) added.get("properties")).put("LAST_EDIT", "10/20/2026")
                .put("GLOBALID", "00000000-0000-0000-0000-000000000087");
        WIREMOCK.stubFor(get(urlPathEqualTo(LAYER + "/query")).withQueryParam("where",
                matching("^LAST_EDIT IN \\('10/7/2026',.*'10/21/2026'\\)$")).willReturn(aResponse().withStatus(200)
                .withBody(collection(List.of(edited, added), false))));

        FetchResult result = fetch(AUSTIN);
        assertThat(result.complete()).as("an incremental read is never the whole set").isFalse();
        assertThat(result.records()).hasSize(2).noneMatch(NormalizedRecord::tombstone);
        assertThat(result.records().get(0).payload()).containsEntry("ceilingFt", 200);
        assertThat(FaaUasfmAdapter.where(LocalDate.parse("2026-10-07"), LocalDate.parse("2026-10-09")))
                .isEqualTo("LAST_EDIT IN ('10/7/2026','10/8/2026','10/9/2026')");
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(LAYER + "/query")).withQueryParam("where", equalTo("1=1")));
    }

    @Test
    @DisplayName("every 56 days the whole box is read again and cells missing from it are tombstoned")
    void fullReload() throws Exception {
        serveLayer(1791942657524L);
        serveFull(slice(0, 77));
        fetch(AUSTIN);

        harness.clock.advance(Duration.ofDays(57));
        WIREMOCK.resetAll();
        serveLayer(1791942657524L);
        serveFull(slice(2, 77));
        FetchResult result = fetch(AUSTIN);

        assertThat(result.complete()).isTrue();
        assertThat(result.records()).filteredOn(NormalizedRecord::tombstone).extracting(NormalizedRecord::externalId)
                .containsExactlyInAnyOrder(page.get(0).get("properties").get("GLOBALID").asString(),
                        page.get(1).get("properties").get("GLOBALID").asString());
        assertThat(result.records()).filteredOn(r -> !r.tombstone()).hasSize(75);
    }

    @Test
    @DisplayName("a new box forces a full read even when the layer is unedited")
    void newBox() throws Exception {
        serveLayer(1791942657524L);
        serveFull(slice(0, 77));
        fetch(AUSTIN);
        WIREMOCK.resetRequests();

        FetchResult wider = fetch(new Envelope(-98.0, -97.5, 30.0, 30.5));
        assertThat(wider.complete()).isTrue();
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(LAYER + "/query")).withQueryParam("where", equalTo("1=1"))
                .withQueryParam("geometry", equalTo("-98,30,-97.5,30.5")));
    }

    @Test
    @DisplayName("the descriptor: valid, weekly, 20/min and 400/day, reference retention")
    void descriptor() {
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(adapter.descriptor().defaultInterval()).isEqualTo(Duration.ofDays(7));
        assertThat(adapter.descriptor().defaultBudget()).isEqualTo(Budget.of(20, 400));
        assertThat(adapter.descriptor().defaultRetention()).isEqualTo(Duration.ofDays(90));
    }
}
