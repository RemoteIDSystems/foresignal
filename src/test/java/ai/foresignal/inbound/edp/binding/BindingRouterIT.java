package ai.foresignal.inbound.edp.binding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.analytics.series.SeriesData;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.analytics.series.SeriesValue;
import ai.foresignal.chronicle.ChronicleLayerEntity;
import ai.foresignal.chronicle.ChronicleService;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.hypothesis.HypothesisVocabulary;
import ai.foresignal.hypothesis.IndicatorRepository;
import ai.foresignal.inbound.edp.ExternalEvent;
import ai.foresignal.inbound.edp.ExternalEventBus;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalKind;
import ai.foresignal.security.PrincipalRepository;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenCreatedVia;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.Ulids;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The binding router end to end (prompt 138, invariant EXT-2): a fixture provider with a stream of every item type,
 * each target applied from it (series, canonical family, social, news, evidence, indicator, forecast, place,
 * situation, chronicle), unbound streams creating nothing, idempotency per record version, backfill from stored
 * records at or below the router's cursor, the durable cursor across a restart in place, and a breaking manifest
 * update disabling the bindings of the changed stream with an event and a notification.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class BindingRouterIT {

    /** The receipt time of every push. */
    static final Instant T = Instant.parse("2026-10-18T06:00:00Z");

    private static final AtomicInteger PLACES = new AtomicInteger();

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired JsonMapper json;
    @Autowired MutableClock clock;
    @Autowired AppSettingService settings;
    @Autowired JdbcTemplate jdbc;
    @Autowired BindingRouter router;
    @Autowired SeriesReader series;
    @Autowired ChangeBus changeBus;
    @Autowired ExternalEventBus events;
    @Autowired ExternalCoverageRegistry coverage;
    @Autowired IndicatorRepository indicators;
    @Autowired ChronicleService chronicles;
    @Autowired ForecasterRegistry forecasters;
    @Autowired PrincipalRepository principals;

    String admin;
    String agent;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        settings.invalidateCache();
        forecasters.registerBuiltIns();
        admin = TestTokens.bearer(TestTokens.create(tokens, "bind-admin", Scope.ADMIN));
        agent = TestTokens.bearer(TestTokens.create(tokens, "bind-agent", Scope.AGENT));
    }

    // ------------------------------------------------------------------------- helpers

    MockHttpServletResponse call(String bearer, MockHttpServletRequestBuilder request) throws Exception {
        if (bearer != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, bearer);
        }
        return mvc.perform(request).andReturn().getResponse();
    }

    MockHttpServletResponse postJson(String bearer, String path, String body) throws Exception {
        return call(bearer, post(path).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    JsonNode body(MockHttpServletResponse response) throws Exception {
        return json.readTree(response.getContentAsString(StandardCharsets.UTF_8));
    }

    /**
     * A registered, approved provider.
     *
     * @param sourceId its source id ({@code ext-…})
     * @param key its ingest key
     * @param providerId its {@code source.id}
     */
    record Provider(String sourceId, String key, String providerId) {}

    Provider register(String levelKind) throws Exception {
        String id = BindingItFixtures.newProvider("harbor");
        MockHttpServletResponse response = postJson(admin, "/v1/external/sources",
                "{\"manifest\":" + BindingItFixtures.manifest(id, levelKind) + ",\"approve\":true}");
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(201);
        return new Provider("ext-" + id, body(response).get("ingestKey").asString(), id);
    }

    void push(Provider p, String batch) throws Exception {
        MockHttpServletResponse response = mvc.perform(post("/v1/external/sources/" + p.sourceId() + "/items")
                .header("X-Foresignal-Ingest-Key", p.key()).contentType(MediaType.APPLICATION_JSON).content(batch))
                .andReturn().getResponse();
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
        assertThat(body(response).get("rejected")).as(response.getContentAsString()).isEmpty();
    }

    JsonNode bind(Provider p, String input) throws Exception {
        MockHttpServletResponse response = postJson(admin, "/v1/external/sources/" + p.sourceId() + "/bindings", input);
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(201);
        return body(response);
    }

    JsonNode binding(String id) throws Exception {
        return body(call(admin, get("/v1/external/bindings/" + id)));
    }

    long appliedRows(String bindingId) {
        return jdbc.queryForObject("select count(*) from external_binding_applied where binding_id = ?", Long.class,
                bindingId);
    }

    long seqOf(Provider p, String externalId) {
        return jdbc.queryForObject("select max(seq) from record_version where source_id = ? and external_id = ?",
                Long.class, p.sourceId(), externalId);
    }

    long heads(Provider p, String kind) {
        return jdbc.queryForObject("select count(*) from record_head where source_id = ? and kind = ?", Long.class,
                p.sourceId(), kind);
    }

    /** A person principal with an agent token (chronicles are owned by principals). */
    String person(String handle) {
        PrincipalEntity principal = new PrincipalEntity(Ulids.next(clock.instant()), handle, handle.toUpperCase(),
                PrincipalKind.PERSON, clock.instant());
        principals.saveAndFlush(principal);
        TokenService.CreatedToken token = tokens.create(new TokenService.TokenSpec(handle + "-token", Scope.AGENT,
                null, TokenRestrictions.NONE, null, TokenCreatedVia.API, principal.getId()));
        return "Bearer " + token.plaintext();
    }

    // ------------------------------------------------------------------------- tests

    @Test
    @DisplayName("every target applies from the fixture provider; unbound streams create nothing (EXT-2)")
    void everyTarget() throws Exception {
        Provider p = register("harbor-level");
        push(p, BindingItFixtures.everything());
        router.catchUp();

        // EXT-2: nothing is derived from unbound streams.
        assertThat(heads(p, "social-post")).isZero();
        assertThat(heads(p, "news-article")).isZero();
        assertThat(jdbc.queryForObject("select count(*) from observation_link where source_id = ?", Long.class,
                p.sourceId())).isZero();
        assertThat(jdbc.queryForObject("select count(*) from external_binding_applied", Long.class)).isZero();

        // Destinations.
        JsonNode set = body(postJson(agent, "/v1/hypotheses/sets", BindingItFixtures.hypothesisSet()));
        String setId = set.get("id").asString();
        String fireIndicator = null;
        String craneIndicator = null;
        for (JsonNode i : set.get("indicators")) {
            if ("Harbor fire".equals(i.get("description").asString())) {
                fireIndicator = i.get("id").asString();
            } else {
                craneIndicator = i.get("id").asString();
            }
        }
        MockHttpServletResponse question = postJson(agent, "/v1/forecasts/questions", BindingItFixtures.binaryQuestion());
        assertThat(question.getStatus()).as(question.getContentAsString()).isEqualTo(201);
        String questionId = body(question).get("id").asString();
        MockHttpServletResponse link = postJson(agent, "/v1/external/sources/" + p.sourceId() + "/question-links",
                "{\"streamId\":\"odds\",\"externalQuestionId\":\"q-1\",\"questionId\":\"" + questionId + "\"}");
        assertThat(link.getStatus()).as(link.getContentAsString()).isEqualTo(201);
        String placeId = "harbor-p" + PLACES.incrementAndGet();
        MockHttpServletResponse place = postJson(admin, "/v1/places", "{\"id\":\"" + placeId + "\",\"name\":\"Harbor\","
                + "\"shape\":\"polygon\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[56,26],[57,26],[57,27],"
                + "[56,27],[56,26]]]}}");
        assertThat(place.getStatus()).as(place.getContentAsString()).isEqualTo(201);
        MockHttpServletResponse situation = postJson(agent, "/v1/situations", "{\"title\":\"Harbor fire\","
                + "\"theme\":\"mixed\",\"members\":[{\"memberType\":\"record\",\"ref\":\"" + p.sourceId()
                + "/cranes:c-1\",\"seq\":" + seqOf(p, "cranes:c-1") + "}]}");
        assertThat(situation.getStatus()).as(situation.getContentAsString()).isEqualTo(201);
        String situationId = body(situation).get("id").asString();
        MockHttpServletResponse chronicle = postJson(person("bind-owner-" + PLACES.incrementAndGet()),
                "/v1/chronicles", "{\"name\":\"Harbor\",\"layers\":[{\"name\":\"All\",\"selector\":{\"type\":\"records\","
                        + "\"sources\":[\"" + p.sourceId() + "\"]},\"capture\":{\"mode\":\"full\"}}]}");
        assertThat(chronicle.getStatus()).as(chronicle.getContentAsString()).isEqualTo(201);
        String chronicleId = body(chronicle).get("id").asString();

        // Bindings: each backfills the stored items (they are at or below the router's cursor).
        JsonNode seriesBinding = bind(p, "{\"streamId\":\"levels\",\"target\":\"series\",\"config\":{\"unit\":\"m\","
                + "\"transform\":\"log1p\"}}");
        JsonNode family = bind(p, "{\"streamId\":\"incidents\",\"target\":\"canonical-family\","
                + "\"config\":{\"family\":\"harbor-incident-test\"}}");
        JsonNode social = bind(p, "{\"streamId\":\"posts\",\"target\":\"social\"}");
        JsonNode news = bind(p, "{\"streamId\":\"articles\",\"target\":\"news\"}");
        JsonNode evidence = bind(p, "{\"streamId\":\"notes\",\"target\":\"evidence\",\"config\":{\"hypothesisSetId\":\""
                + setId + "\",\"sourceReliability\":\"C\",\"informationCredibility\":3}}");
        bind(p, "{\"streamId\":\"incidents\",\"target\":\"indicator\",\"config\":{\"indicatorId\":\"" + fireIndicator
                + "\"}}");
        bind(p, "{\"streamId\":\"notes\",\"target\":\"indicator\",\"config\":{\"indicatorId\":\"" + craneIndicator
                + "\"}}");
        JsonNode forecast = bind(p, "{\"streamId\":\"odds\",\"target\":\"forecast\"}");
        bind(p, "{\"streamId\":\"levels\",\"target\":\"place\",\"config\":{\"placeId\":\"" + placeId + "\"}}");
        bind(p, "{\"streamId\":\"incidents\",\"target\":\"situation\",\"config\":{\"situationId\":\"" + situationId
                + "\"}}");
        JsonNode chronicleBinding = bind(p, "{\"streamId\":\"cranes\",\"target\":\"chronicle\",\"config\":{"
                + "\"chronicleId\":\"" + chronicleId + "\",\"layerName\":\"Cranes\"}}");

        for (JsonNode b : List.of(seriesBinding, family, social, news, evidence, forecast)) {
            JsonNode now = binding(b.get("id").asString());
            assertThat(now.get("state").asString()).as(now.toString()).isEqualTo("active");
            assertThat(now.get("applied").asLong()).as(now.toString()).isEqualTo(1);
            assertThat(now.get("lastError").isNull()).as(now.toString()).isTrue();
        }

        // series
        SeriesData data = series.read(SeriesRef.parse("series:" + p.sourceId() + "/harbor-level/berth-1"),
                Instant.parse("2026-10-01T00:00:00Z"), Instant.parse("2026-10-18T00:00:00Z"), null);
        List<SeriesValue> points = data.points().stream().filter(v -> v.value() != null).toList();
        assertThat(points).hasSize(1);
        assertThat(points.get(0).value()).isEqualTo(4.2);
        assertThat(points.get(0).periodStart()).isEqualTo(Instant.parse("2026-10-16T00:00:00Z"));
        assertThat(points.get(0).knownAt()).as("knownAt is max(provider knownAt, receipt)").isEqualTo(T);
        assertThat(data.unit()).isEqualTo("m");
        assertThat(data.transform()).isEqualTo("log-level");
        assertThat(jdbc.queryForObject("select count(*) from analysis_series where ref = ?", Long.class,
                "series:" + p.sourceId() + "/harbor-level/berth-1")).isEqualTo(1);

        // canonical family
        assertThat(jdbc.queryForObject("select count(*) from observation_link l join canonical_event e on"
                + " e.id = l.canonical_event_id where l.source_id = ? and l.external_id = 'incidents:e-1'"
                + " and e.family = 'harbor-incident-test'", Long.class, p.sourceId())).isEqualTo(1);
        assertThat(coverage.kindsOf("harbor-incident-test")).contains("harbor-incident");
        assertThat(coverage.coverage(p.sourceId(), "harbor-incident-test").pDetect()).isEqualTo(0.6);

        // social and news, written under the provider
        assertThat(jdbc.queryForObject("select payload->>'collection' from record_head where source_id = ?"
                + " and external_id = 'telegram:p-1' and kind = 'social-post'", String.class, p.sourceId()))
                .isEqualTo("import");
        assertThat(heads(p, "news-article")).isEqualTo(1);

        // evidence and indicators
        assertThat(jdbc.queryForObject("select count(*) from ach_evidence where set_id = ? and source = 'record'"
                + " and ref like ?", Long.class, setId, "record:" + p.sourceId() + "/%")).isEqualTo(1);
        assertThat(indicators.findById(fireIndicator).orElseThrow().getState()).isEqualTo(HypothesisVocabulary.OBSERVED);
        assertThat(indicators.findById(craneIndicator).orElseThrow().getState())
                .isEqualTo(HypothesisVocabulary.REPORTED);

        // forecast: the external forecaster, made_at never before receipt (EXT-3)
        String forecaster = "edp:" + p.sourceId() + "/odds";
        assertThat(jdbc.queryForObject("select type from forecaster where id = ?", String.class, forecaster))
                .isEqualTo("external");
        assertThat(jdbc.queryForObject("select count(*) from forecast where forecaster_id = ? and question_id = ?",
                Long.class, forecaster, questionId)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select min(made_at) from forecast where forecaster_id = ?",
                java.sql.Timestamp.class, forecaster).toInstant()).isAfterOrEqualTo(T);
        // a move below 0.02 within a day is not recorded; a material move is
        push(p, BindingItFixtures.batch(BindingItFixtures.forecast("f-2", 0.31)));
        router.catchUp();
        assertThat(jdbc.queryForObject("select count(*) from forecast where forecaster_id = ?", Long.class, forecaster))
                .isEqualTo(1);
        push(p, BindingItFixtures.batch(BindingItFixtures.forecast("f-3", 0.45)));
        router.catchUp();
        assertThat(jdbc.queryForObject("select count(*) from forecast where forecaster_id = ?", Long.class, forecaster))
                .isEqualTo(2);

        // place
        JsonNode placeView = body(call(admin, get("/v1/places/" + placeId)));
        JsonNode external = placeView.get("external").get(p.sourceId() + "/levels");
        assertThat(external.get("value").asDouble()).isEqualTo(4.2);
        assertThat(external.get("seriesKey").asString()).isEqualTo("berth-1");

        // situation
        assertThat(jdbc.queryForObject("select count(*) from situation_member where situation_id = ? and ref = ?",
                Long.class, situationId, p.sourceId() + "/incidents:e-1")).isEqualTo(1);

        // chronicle
        List<ChronicleLayerEntity> layers = chronicles.liveLayers(chronicleId);
        assertThat(layers).anySatisfy(l -> {
            assertThat(l.getSelector().get("type")).isEqualTo("external-stream");
            assertThat(l.getSelector().get("streamId")).isEqualTo("cranes");
        });
        assertThat(binding(chronicleBinding.get("id").asString()).get("targetRef").asString())
                .startsWith("chronicle:" + chronicleId + "/layers/");

        // converted records carry no EDP provenance and are never routed back
        router.catchUp();
        assertThat(binding(social.get("id").asString()).get("applied").asLong()).isEqualTo(1);
    }

    @Test
    @DisplayName("idempotent per record version; backfill reads at or below the cursor; backfill:false waits for new items")
    void idempotencyAndBackfill() throws Exception {
        assertThat(BindingVocabulary.BACKFILL_CAP).isEqualTo(100_000);
        Provider p = register("harbor-level");
        push(p, BindingItFixtures.batch(BindingItFixtures.observation("berth-1:2026-10-15", "2026-10-15", 3.0)));
        router.catchUp();

        String a = bind(p, "{\"streamId\":\"levels\",\"target\":\"series\"}").get("id").asString();
        assertThat(binding(a).get("applied").asLong()).as("backfilled from the stored item").isEqualTo(1);
        String b = bind(p, "{\"streamId\":\"levels\",\"target\":\"series\",\"backfill\":false}").get("id").asString();
        assertThat(binding(b).get("state").asString()).isEqualTo("active");
        router.catchUp();
        assertThat(binding(b).get("applied").asLong()).as("nothing until new items arrive").isZero();

        push(p, BindingItFixtures.batch(BindingItFixtures.observation("berth-1:2026-10-16", "2026-10-16", 3.5)));
        router.catchUp();
        assertThat(binding(a).get("applied").asLong()).isEqualTo(2);
        assertThat(binding(b).get("applied").asLong()).isEqualTo(1);

        router.catchUp();
        router.rewind(0);
        router.catchUp();
        assertThat(binding(a).get("applied").asLong()).as("a re-read applies nothing twice").isEqualTo(2);
        assertThat(appliedRows(a)).isEqualTo(2);

        push(p, BindingItFixtures.batch(BindingItFixtures.observation("berth-1:2026-10-16", "2026-10-16", 3.9)));
        router.catchUp();
        assertThat(binding(a).get("applied").asLong()).as("a changed item is a new version").isEqualTo(3);
        assertThat(appliedRows(a)).isEqualTo(3);
    }

    @Test
    @DisplayName("the durable cursor external.binding.cursor survives a restart in place")
    void durableCursor() throws Exception {
        Provider p = register("harbor-level");
        router.catchUp();
        String a = bind(p, "{\"streamId\":\"levels\",\"target\":\"series\"}").get("id").asString();
        push(p, BindingItFixtures.batch(BindingItFixtures.observation("berth-1:2026-10-15", "2026-10-15", 3.0)));
        router.catchUp();
        assertThat(binding(a).get("applied").asLong()).isEqualTo(1);

        settings.invalidateCache();
        long stored = settings.get(SettingKeys.EXTERNAL_BINDING_CURSOR, Long.class).orElseThrow();
        assertThat(stored).isEqualTo(router.cursor());
        assertThat(stored).isEqualTo(changeBus.lastPublishedSeq());

        router.resetState();
        assertThat(router.cursor()).isEqualTo(stored);
        push(p, BindingItFixtures.batch(BindingItFixtures.observation("berth-1:2026-10-16", "2026-10-16", 3.2)));
        router.catchUp();
        assertThat(binding(a).get("applied").asLong()).isEqualTo(2);
        assertThat(appliedRows(a)).isEqualTo(2);
    }

    @Test
    @DisplayName("a breaking manifest update disables the bindings of the changed stream with an event and a notification")
    void breakingManifestUpdate() throws Exception {
        Provider p = register("harbor-level");
        String changed = bind(p, "{\"streamId\":\"levels\",\"target\":\"series\"}").get("id").asString();
        String unchanged = bind(p, "{\"streamId\":\"incidents\",\"target\":\"canonical-family\","
                + "\"config\":{\"family\":\"harbor-incident-test\"}}").get("id").asString();
        List<ExternalEvent> seen = new CopyOnWriteArrayList<>();
        AutoCloseable handle = events.subscribe(seen::add);
        try {
            MockHttpServletResponse response = mvc.perform(put("/v1/external/sources/" + p.sourceId() + "/manifest")
                    .header("X-Foresignal-Ingest-Key", p.key()).contentType(MediaType.APPLICATION_JSON)
                    .content(BindingItFixtures.manifest(p.providerId(), "harbor-level-v2"))).andReturn().getResponse();
            assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
            assertThat(body(response).get("breakingChanges")).isNotEmpty();
        } finally {
            handle.close();
        }

        JsonNode off = binding(changed);
        assertThat(off.get("enabled").asBoolean()).isFalse();
        assertThat(off.get("state").asString()).isEqualTo("disabled");
        assertThat(off.get("lastError").asString()).startsWith("breaking manifest update");
        JsonNode on = binding(unchanged);
        assertThat(on.get("enabled").asBoolean()).isTrue();
        assertThat(on.get("state").asString()).isIn("active", "backfilling");

        assertThat(seen).anySatisfy(e -> {
            assertThat(e.type()).isEqualTo(ExternalEvent.BINDING_DISABLED);
            assertThat(e.payload().get("bindingId")).isEqualTo(changed);
            assertThat(e.payload().get("sourceId")).isEqualTo(p.sourceId());
        });
        assertThat(seen).noneSatisfy(e -> assertThat(e.payload().get("bindingId")).isEqualTo(unchanged));
        assertThat(seen).anySatisfy(e -> {
            assertThat(e.type()).isEqualTo(ExternalEvent.SOURCE_STATE);
            assertThat(e.payload().get("change")).isEqualTo("manifest-changed");
        });
        assertThat(jdbc.queryForObject("select count(*) from notification where subtype = 'system.binding_disabled'",
                Long.class)).isPositive();

        // Re-enabling with PUT after review switches it back on.
        MockHttpServletResponse reenabled = call(admin, put("/v1/external/bindings/" + changed)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"streamId\":\"levels\",\"target\":\"series\",\"enabled\":true,\"backfill\":false}"));
        assertThat(reenabled.getStatus()).as(reenabled.getContentAsString()).isEqualTo(200);
        assertThat(body(reenabled).get("state").asString()).isEqualTo("active");
        assertThat(body(reenabled).get("lastError").isNull()).isTrue();
    }
}
