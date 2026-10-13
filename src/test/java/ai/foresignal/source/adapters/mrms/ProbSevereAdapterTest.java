package ai.foresignal.source.adapters.mrms;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.raster.ProductAdapterHarness;
import ai.foresignal.testsupport.weather.WeatherAdapterHarness;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code mrms-probsevere} against a stubbed S3 (prompt 047): the listing with {@code start-after},
 * newest-only download, AOI filtering and re-filtering, tombstones from the newest processed file, and
 * none from a failed download or an older file.
 */
@IntegrationTest
class ProbSevereAdapterTest extends WireMockSupport {

    static final String BUCKET = ProbSevereAdapter.BUCKET;
    static final Instant NOW = Instant.parse("2026-10-12T23:59:30Z");
    static final String PREFIX = "ProbSevere/20261012/";
    static final String K1 = PREFIX + "MRMS_PROBSEVERE_20261012_235437.json";
    static final String K2 = PREFIX + "MRMS_PROBSEVERE_20261012_235638.json";
    static final String K3 = PREFIX + "MRMS_PROBSEVERE_20261012_235841.json";
    static final double[] TEXAS = {-106.65, 25.84, -93.51, 36.50};

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    MutableClock clock;
    WeatherAdapterHarness.Upstream upstream;
    WeatherAdapterHarness.MapState state;
    WeatherAdapterHarness.TestAoi aoi;
    ProbSevereAdapter adapter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        upstream = WeatherAdapterHarness.upstream(jdbc, jsonMapper, properties, install, clock, Budget.of(100, 10_000, 300),
                baseUrl());
        state = new WeatherAdapterHarness.MapState();
        aoi = new WeatherAdapterHarness.TestAoi(TEXAS);
        adapter = new ProbSevereAdapter(aoi, baseUrl());
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/")).atPriority(10)
                .willReturn(aResponse().withHeader("Content-Type", "application/xml")
                        .withBody(ProductAdapterHarness.listing(BUCKET, "", Map.of(), false))));
    }

    private WeatherAdapterHarness.Context context() {
        SourceDescriptor d = adapter.descriptor();
        return new WeatherAdapterHarness.Context(d, upstream.client().forSource(ProbSevereAdapter.ID, d),
                ValidatedParams.empty(), state, clock);
    }

    private static void stubListing(String prefix, String startAfter, String... keys) {
        Map<String, Long> objects = new LinkedHashMap<>();
        for (String key : keys) {
            objects.put(key, 264_916L);
        }
        MappingBuilder mapping = get(urlPathEqualTo("/" + BUCKET + "/"))
                .withQueryParam("list-type", equalTo("2"))
                .withQueryParam("prefix", equalTo(prefix))
                .withQueryParam("start-after", startAfter == null ? absent() : equalTo(startAfter));
        WIREMOCK.stubFor(mapping.willReturn(aResponse().withHeader("Content-Type", "application/xml")
                .withBody(ProductAdapterHarness.listing(BUCKET, prefix, objects, false))));
    }

    private static void stubObject(String key, byte[] body) {
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + key)).willReturn(aResponse().withBody(body)));
    }

    static byte[] captured() {
        return AdapterFixtures.bytes("probsevere", ProbSevereParserTest.FILE);
    }

    /** The captured file with another valid time and without the object {@code dropId}. */
    static byte[] variant(String validTime, String dropId) {
        ObjectNode root = (ObjectNode) JsonMapper.builder().build().readTree(captured());
        root.put("validTime", validTime);
        ArrayNode features = (ArrayNode) root.get("features");
        for (int i = features.size() - 1; i >= 0; i--) {
            if (dropId != null && dropId.equals(features.get(i).path("properties").path("ID").asString())) {
                features.remove(i);
            }
        }
        return root.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static List<String> tombstones(FetchResult result) {
        return result.records().stream().filter(NormalizedRecord::tombstone).map(NormalizedRecord::externalId).toList();
    }

    @Test
    @DisplayName("a cold start lists the day and downloads only the newest file; the next run lists after it")
    void newestOnly() throws Exception {
        stubListing(PREFIX, null, K1, K2, K3);
        stubObject(K3, captured());

        FetchResult result = adapter.fetch(context());

        assertThat(result.complete()).isTrue();
        assertThat(result.records()).hasSize(10).noneMatch(NormalizedRecord::tombstone);
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/" + BUCKET + "/" + K3)));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/" + BUCKET + "/" + K1)));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/" + BUCKET + "/" + K2)));
        assertThat(state.get(ProbSevereAdapter.LAST_KEY)).contains(K3);
        assertThat(aoi.consumers()).contains(ProbSevereAdapter.ID);

        // Nothing newer: the run lists with start-after and is unchanged.
        stubListing(PREFIX, K3);
        FetchResult again = adapter.fetch(context());
        assertThat(again.notModified()).isTrue();
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/" + BUCKET + "/")).withQueryParam("start-after", equalTo(K3)));
    }

    @Test
    @DisplayName("an object missing from the next file is tombstoned at that file's valid time")
    void absentTombstoned() throws Exception {
        stubListing(PREFIX, null, K3);
        stubObject(K3, captured());
        FetchResult first = adapter.fetch(context());
        String gone = first.records().get(0).externalId();

        clock.advance(Duration.ofMinutes(2));
        String k4 = "ProbSevere/20261013/MRMS_PROBSEVERE_20261013_000041.json";
        stubListing("ProbSevere/20261013/", null, k4);
        stubObject(k4, variant("20261013_000041 UTC", gone));

        FetchResult second = adapter.fetch(context());

        assertThat(tombstones(second)).containsExactly(gone);
        assertThat(second.records().stream().filter(NormalizedRecord::tombstone).findFirst().orElseThrow().observedAt())
                .isEqualTo(Instant.parse("2026-10-13T00:00:41Z"));
    }

    @Test
    @DisplayName("a failed download fails the run, tombstones nothing and keeps the cursor")
    void failedDownload() throws Exception {
        stubListing(PREFIX, null, K3);
        stubObject(K3, captured());
        adapter.fetch(context());
        Map<String, String> before = state.asMap();

        String k4 = PREFIX + "MRMS_PROBSEVERE_20261012_235950.json";
        stubListing(PREFIX, K3, k4);
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + k4)).willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> adapter.fetch(context())).isInstanceOf(UpstreamHttpException.class);
        assertThat(state.asMap()).isEqualTo(before);
    }

    @Test
    @DisplayName("a file older than the last processed one is ignored without tombstones")
    void olderFile() throws Exception {
        stubListing(PREFIX, null, K3);
        stubObject(K3, captured());
        adapter.fetch(context());
        Map<String, String> before = state.asMap();

        String k4 = PREFIX + "MRMS_PROBSEVERE_20261012_235950.json";
        stubListing(PREFIX, K3, k4);
        stubObject(k4, variant("20261012_230000 UTC", null));

        FetchResult result = adapter.fetch(context());
        assertThat(result.notModified()).isTrue();
        assertThat(state.asMap()).isEqualTo(before);
    }

    @Test
    @DisplayName("when the AOI moves, the last file is processed again and objects now outside are tombstoned")
    void aoiMoved() throws Exception {
        stubListing(PREFIX, null, K3);
        stubObject(K3, captured());
        FetchResult first = adapter.fetch(context());
        assertThat(first.records()).hasSize(10);

        aoi.set(10, 10, 11, 11);
        stubListing(PREFIX, K3);
        FetchResult second = adapter.fetch(context());

        assertThat(tombstones(second)).hasSize(10);
        assertThat(second.records()).allMatch(NormalizedRecord::tombstone);
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo("/" + BUCKET + "/" + K3)));
    }

    @Test
    @DisplayName("just after UTC midnight an empty new day falls back to yesterday's listing")
    void dayRollover() throws Exception {
        clock.setInstant(Instant.parse("2026-10-13T00:00:20Z"));
        stubListing("ProbSevere/20261013/", null);
        stubListing(PREFIX, null, K2, K3);
        stubObject(K3, captured());

        assertThat(adapter.fetch(context()).records()).hasSize(10);
        assertThat(state.get(ProbSevereAdapter.LAST_KEY)).contains(K3);
    }

    @Test
    @DisplayName("the descriptor has the prompt's values and validates")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofMinutes(2));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(4, 1_600, 300));
        assertThat(d.kindRetention()).containsEntry("probsevere-object", Duration.ofDays(3));
        assertThat(d.titleField()).isEqualTo("label");
        assertThat(d.volatileFields()).containsExactly("muCape", "mlCape", "ebShearKt", "srh01km", "flashRate");
        assertThat(d.recordTtl()).isNull();
        assertThat(d.licence()).isEqualTo("US-PD");
        assertThat(d.zeroResultsNormal()).isTrue();
    }
}
