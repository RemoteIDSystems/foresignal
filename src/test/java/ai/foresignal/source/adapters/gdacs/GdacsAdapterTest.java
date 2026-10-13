package ai.foresignal.source.adapters.gdacs;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
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
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherAdapterHarness;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code gdacs} against a stubbed {@code www.gdacs.org} through the real upstream client (prompt 067): one request per
 * configured type, sequentially and in order; a 400 surfaced; Green kept; one {@code externalId} per event across
 * episodes.
 */
@IntegrationTest
class GdacsAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-13T15:00:00Z");

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    MutableClock clock;
    WeatherAdapterHarness.Upstream upstream;
    GdacsAdapter adapter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        upstream = WeatherAdapterHarness.upstream(jdbc, jsonMapper, properties, install, clock, Budget.of(100, 1_000), null);
        adapter = new GdacsAdapter(baseUrl());
    }

    @AfterEach
    void tearDown() {
        upstream.backoff().onSuccess(GdacsAdapter.ID);
    }

    private WeatherAdapterHarness.Context context(Map<String, Object> params) {
        return new WeatherAdapterHarness.Context(adapter.descriptor(),
                upstream.client().forSource(GdacsAdapter.ID, adapter.descriptor()), ValidatedParams.of(params),
                new WeatherAdapterHarness.MapState(), clock);
    }

    private static void serve(String type, String fixture) {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(GdacsAdapter.PATH + "?eventtype=" + type)).willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "application/json; charset=utf-8")
                .withBody(AdapterFixtures.bytes(GdacsAdapter.ID, fixture))));
    }

    private static void serveEmpty(String type) {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(GdacsAdapter.PATH + "?eventtype=" + type)).willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "application/json")
                .withBody("{\"type\":\"FeatureCollection\",\"features\":[]}")));
    }

    /** The request URLs in the order the stub received them. */
    private static List<String> requestOrder() {
        return WIREMOCK.getAllServeEvents().stream()
                .sorted(Comparator.comparing(e -> e.getRequest().getLoggedDate()))
                .map(ServeEvent::getRequest).map(r -> r.getUrl()).toList();
    }

    @Test
    @DisplayName("one request per configured type, in the configured order, each with a single eventtype")
    void oneRequestPerType() throws Exception {
        serve("TC", "MAP-TC.json");
        serve("EQ", "MAP-EQ.json");

        FetchResult result = adapter.fetch(context(Map.of(GdacsAdapter.EVENT_TYPES_PARAM, List.of("TC", "EQ"))));

        assertThat(requestOrder()).containsExactly(GdacsAdapter.PATH + "?eventtype=TC", GdacsAdapter.PATH + "?eventtype=EQ");
        assertThat(result.records()).hasSize(12);
        assertThat(result.records().get(0).externalId()).startsWith("TC:");
        assertThat(result.records().get(11).externalId()).startsWith("EQ:");
    }

    @Test
    @DisplayName("without the parameter all six types are requested")
    void allTypesByDefault() throws Exception {
        serve("EQ", "MAP-EQ.json");
        serve("TC", "MAP-TC.json");
        for (String type : List.of("FL", "VO", "DR", "WF")) {
            serveEmpty(type);
        }

        FetchResult result = adapter.fetch(context(Map.of()));

        assertThat(requestOrder()).extracting(u -> u.substring(u.indexOf('=') + 1))
                .containsExactly("EQ", "TC", "FL", "VO", "DR", "WF");
        assertThat(result.records()).hasSize(12);
        WIREMOCK.verify(6, anyRequestedFor(anyUrl()));
    }

    @Test
    @DisplayName("a 400 is surfaced and stops the run")
    void badRequest() {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(GdacsAdapter.PATH + "?eventtype=EQ")).willReturn(aResponse()
                .withStatus(400).withHeader("Content-Type", "application/json; charset=utf-8")
                .withBody(AdapterFixtures.bytes(GdacsAdapter.ID, "400-no-eventtype.json"))));

        assertThatThrownBy(() -> adapter.fetch(context(Map.of(GdacsAdapter.EVENT_TYPES_PARAM, List.of("EQ", "TC")))))
                .isInstanceOf(UpstreamHttpException.class)
                .satisfies(e -> assertThat(((UpstreamHttpException) e).getStatus()).isEqualTo(400));
        WIREMOCK.verify(1, anyRequestedFor(anyUrl()));
    }

    @Test
    @DisplayName("Green alerts are kept")
    void greenKept() throws Exception {
        serve("EQ", "MAP-EQ.json");

        FetchResult result = adapter.fetch(context(Map.of(GdacsAdapter.EVENT_TYPES_PARAM, List.of("EQ"))));

        assertThat(result.records()).extracting(r -> r.payload().get("alertLevel"))
                .containsOnly("Green", "Orange").contains("Green");
    }

    @Test
    @DisplayName("a new episode keeps the event's externalId and updates the record")
    void episodes() throws Exception {
        serve("EQ", "MAP-EQ.json");
        NormalizedRecord before = adapter.fetch(context(Map.of(GdacsAdapter.EVENT_TYPES_PARAM, List.of("EQ")))).records()
                .stream().filter(r -> r.externalId().equals("EQ:1570346")).findFirst().orElseThrow();
        serve("EQ", "MAP-EQ-next-episode.json");
        NormalizedRecord after = adapter.fetch(context(Map.of(GdacsAdapter.EVENT_TYPES_PARAM, List.of("EQ")))).records()
                .stream().filter(r -> r.externalId().equals("EQ:1570346")).findFirst().orElseThrow();

        assertThat(after.externalId()).isEqualTo(before.externalId());
        assertThat(before.payload()).containsEntry("episodeId", 1738688L).containsEntry("alertLevel", "Green");
        assertThat(after.payload()).containsEntry("episodeId", 1739999L).containsEntry("alertLevel", "Orange");
        assertThat(after.observedAt()).isAfter(before.observedAt());
    }

    @Test
    @DisplayName("an unknown type is refused; an explicitly empty list fails checkParams")
    void params() {
        assertThatThrownBy(() -> GdacsAdapter.eventTypes(ValidatedParams.of(Map.of(GdacsAdapter.EVENT_TYPES_PARAM,
                List.of("XX"))))).isInstanceOf(ai.foresignal.source.spi.AdapterException.class);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(GdacsAdapter.EVENT_TYPES_PARAM, List.of()))))
                .containsKey(GdacsAdapter.EVENT_TYPES_PARAM);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of()))).isEmpty();
    }

    @Test
    @DisplayName("the descriptor is valid and carries the prompt's values")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.id()).isEqualTo("gdacs");
        assertThat(d.kinds()).containsExactly("disaster");
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofMinutes(15));
        assertThat(d.minInterval()).isEqualTo(Duration.ofMinutes(10));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(6, 700));
        assertThat(d.recordTtl()).isEqualTo(Duration.ofHours(48));
        assertThat(d.ttlAnchor()).isEqualTo(TtlAnchor.RECEIVED);
        assertThat(d.embedFields()).containsExactly("name", "description");
        assertThat(d.tileProperties()).containsExactly("alertLevel", "eventType", "alertScore");
        assertThat(d.rankField()).isEqualTo("alertScore");
        assertThat(d.licence()).isEqualTo("GDACS terms of use (disclaimer; no explicit licence)");
        assertThat(d.termsUrl()).isEqualTo(GdacsAdapter.TERMS_URL);
        assertThat(d.description()).contains("not reviewed").contains("no substitute for official warnings");
    }
}
