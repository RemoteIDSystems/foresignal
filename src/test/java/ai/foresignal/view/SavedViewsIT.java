package ai.foresignal.view;

import static ai.foresignal.testsupport.place.PlaceTestSources.EVENTS;
import static ai.foresignal.testsupport.place.PlaceTestSources.EVENT_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.ingest.PersistReason;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.Bbox;
import ai.foresignal.stream.ChangeSummary;
import ai.foresignal.stream.LiveUpdate;
import ai.foresignal.stream.sse.ChangeFilter;
import ai.foresignal.stream.sse.ChangesTopic;
import ai.foresignal.stream.sse.FifoEvent;
import ai.foresignal.stream.sse.FilterMatch;
import ai.foresignal.stream.sse.ReplayRequest;
import ai.foresignal.stream.sse.StreamPrincipal;
import ai.foresignal.stream.sse.TopicParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.place.PlaceTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.tiles.MvtDecoder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Saved views (prompt 036): records, tiles and the stream render the same set for one view;
 * {@code view=} is intersected with the other filters and with a restricted token; the {@code fields}
 * projection applies; the tile cache key (ETag and TileJSON template) carries the view version; a
 * view referencing a place needs a token.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(PlaceTestSources.class)
class SavedViewsIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();
    static final String BIG = "{\"slug\":\"big-quakes\",\"name\":\"Big quakes\",\"sources\":[\"" + EVENTS + "\"],"
            + "\"conditions\":[{\"path\":\"mag\",\"op\":\">=\",\"value\":5}],"
            // externalId is projected too: the view's records carry only the listed members, and ids() reads it
            // (checkpoint 041).
            + "\"fields\":[\"externalId\",\"title\",\"geometry\",\"payload.mag\"]}";

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired TokenService tokens;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;
    @Autowired TestRecords records;
    @Autowired ChangesTopic changes;

    String admin;
    Instant now = T;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        admin = "Bearer " + adminTokens.rotate();
        clock.setInstant(T);
    }

    ResultActions send(MockHttpServletRequestBuilder request, String token, String body) throws Exception {
        if (token != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, token);
        }
        if (body != null) {
            request = request.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return mvc.perform(request);
    }

    JsonNode json(ResultActions result) throws Exception {
        return JSON.readTree(result.andReturn().getResponse().getContentAsString());
    }

    void quake(String id, double mag, double lon, double lat) {
        now = now.plusSeconds(10);
        clock.setInstant(now);
        records.ingest(EVENTS, NormalizedRecord.builder(id, EVENT_KIND).observedAt(now).point(lon, lat)
                .put("mag", mag).put("name", "Quake " + id).put("depth", 10).build());
    }

    Set<String> ids(JsonNode page) {
        Set<String> out = new TreeSet<>();
        page.get("items").forEach(i -> out.add(i.get("externalId").asString()));
        return out;
    }

    void seed() {
        // Off (0,0): a source that does not allow Null Island drops that geometry (008), which hid q6 from every
        // spatial filter (checkpoint 041).
        quake("q6", 6, 0.01, 0.01);
        quake("q4", 4, 0.11, 0.11);
        quake("q7", 7, 0.21, 0.21);
    }

    @Test
    @DisplayName("CRUD: create 201 (version 1), slug 409, get by id or slug, unknown 404, replace bumps the version, delete")
    void crud() throws Exception {
        JsonNode created = json(send(post("/v1/views"), admin, BIG).andExpect(status().isCreated()));
        String id = created.get("id").asString();
        assertThat(created.get("version").asInt()).isEqualTo(1);
        send(post("/v1/views"), admin, BIG).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("VIEW_EXISTS"));
        send(get("/v1/views/" + id), null, null).andExpect(status().isOk()).andExpect(jsonPath("$.slug").value("big-quakes"));
        send(get("/v1/views/big-quakes"), null, null).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        send(get("/v1/views/nope"), null, null).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("UNKNOWN_VIEW"));
        assertThat(json(send(get("/v1/views"), null, null)).get("items")).hasSize(1);
        send(put("/v1/views/big-quakes"), admin, BIG.replace("\"value\":5", "\"value\":6"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.version").value(2));
        send(post("/v1/views"), admin, "{\"slug\":\"bad\",\"name\":\"x\",\"sources\":[\"no-such\"],"
                + "\"conditions\":[{\"path\":\"x\",\"op\":\"~\",\"value\":1}],\"fields\":[\"nonsense\"]}")
                .andExpect(status().isBadRequest());
        send(post("/v1/views"), null, BIG.replace("big-quakes", "other")).andExpect(status().isUnauthorized());
        send(delete("/v1/views/" + id), admin, null).andExpect(status().isNoContent());
        send(get("/v1/views/" + id), null, null).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("records, tiles and the stream (replay and live) render the same set for one view")
    void sameSet() throws Exception {
        String id = json(send(post("/v1/views"), admin, BIG).andExpect(status().isCreated())).get("id").asString();
        seed();

        Set<String> fromRecords = ids(json(send(get("/v1/views/big-quakes/records"), null, null).andExpect(status().isOk())));
        assertThat(fromRecords).containsExactly("q6", "q7");

        byte[] tile = send(get("/v1/views/" + id + "/tiles/0/0/0.mvt"), null, null).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        Set<String> fromTiles = new TreeSet<>();
        MvtDecoder.features(tile).forEach(f -> fromTiles.add((String) f.get("externalId")));
        assertThat(fromTiles).isEqualTo(fromRecords);

        StreamPrincipal principal = StreamPrincipal.anonymous("127.0.0.1");
        ChangeFilter filter = changes.parseQuery(new TopicParams("", Map.of("view", List.of("big-quakes"))), principal);
        List<FifoEvent> replayed = new ArrayList<>();
        changes.replay(new ReplayRequest(0, false), filter, replayed::add);
        Set<String> fromStream = new TreeSet<>();
        replayed.forEach(e -> fromStream.add((String) e.data().get("externalId")));
        assertThat(fromStream).isEqualTo(fromRecords);

        // Live: a version is judged on its payload; one that stops matching leaves with leftFilter.
        Bbox box = new Bbox(0, 0, 0, 0);
        assertThat(filter.matchChange(summary("q9", box, false), Map.of("mag", 8))).isEqualTo(FilterMatch.MATCH);
        assertThat(filter.matchChange(summary("q9", box, false), Map.of("mag", 3))).isEqualTo(FilterMatch.LEFT);
        assertThat(filter.matchChange(summary("q3", box, false), Map.of("mag", 3))).isEqualTo(FilterMatch.NONE);
        // Live updates follow the entities that match.
        assertThat(filter.matchLive(live("q6", box))).isEqualTo(FilterMatch.MATCH);
        assertThat(filter.matchLive(live("q4", box))).isEqualTo(FilterMatch.NONE);
    }

    @Test
    @DisplayName("view= is intersected with the other filters and with a restricted token")
    void intersection() throws Exception {
        send(post("/v1/views"), admin, BIG).andExpect(status().isCreated());
        seed();
        assertThat(ids(json(send(get("/v1/records?view=big-quakes"), null, null).andExpect(status().isOk()))))
                .containsExactly("q6", "q7");
        assertThat(ids(json(send(get("/v1/records?view=big-quakes&bbox=0.15,0.15,0.3,0.3"), null, null))))
                .containsExactly("q7");
        String restricted = TestTokens.bearer(TestTokens.create(tokens, "corner", Scope.READ,
                new TokenRestrictions(null, null, List.of(-0.05, -0.05, 0.05, 0.05), null), null));
        assertThat(ids(json(send(get("/v1/records?view=big-quakes"), restricted, null).andExpect(status().isOk()))))
                .containsExactly("q6");
        assertThat(ids(json(send(get("/v1/views/big-quakes/records"), restricted, null)))).containsExactly("q6");
        assertThat(ids(json(send(get("/v1/changes?view=big-quakes"), null, null)))).containsExactly("q6", "q7");
        send(get("/v1/records?view=nope"), null, null).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("the fields projection keeps only the listed members and payload paths")
    void projection() throws Exception {
        send(post("/v1/views"), admin, BIG).andExpect(status().isCreated());
        seed();
        JsonNode item = json(send(get("/v1/views/big-quakes/records"), null, null)).get("items").get(0);
        assertThat(item.has("title")).isTrue();
        assertThat(item.get("payload").has("mag")).isTrue();
        assertThat(item.get("payload").has("depth")).isFalse();
        String csv = send(get("/v1/views/big-quakes/records?format=csv"), null, null).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(csv.lines().count()).isEqualTo(3);
    }

    @Test
    @DisplayName("the tile ETag and the TileJSON template carry the view version")
    void versionKeyedTiles() throws Exception {
        String id = json(send(post("/v1/views"), admin, BIG).andExpect(status().isCreated())).get("id").asString();
        seed();
        MvcResult first = send(get("/v1/views/" + id + "/tiles/0/0/0.mvt"), null, null).andReturn();
        String etag1 = first.getResponse().getHeader(HttpHeaders.ETAG);
        assertThat(etag1).startsWith("\"v1-");
        send(get("/v1/views/" + id + "/tiles/0/0/0.mvt").header(HttpHeaders.IF_NONE_MATCH, etag1), null, null)
                .andExpect(status().isNotModified());
        JsonNode tileJson = json(send(get("/v1/views/" + id + "/tiles.json"), null, null).andExpect(status().isOk()));
        assertThat(tileJson.get("tiles").get(0).asString()).endsWith("/tiles/{z}/{x}/{y}.mvt?v=1");

        send(put("/v1/views/" + id), admin, BIG).andExpect(status().isOk());
        String etag2 = send(get("/v1/views/" + id + "/tiles/0/0/0.mvt"), null, null).andReturn().getResponse()
                .getHeader(HttpHeaders.ETAG);
        assertThat(etag2).startsWith("\"v2-").isNotEqualTo(etag1);
        assertThat(json(send(get("/v1/views/" + id + "/tiles.json"), null, null)).get("tiles").get(0).asString())
                .endsWith("?v=2");
    }

    @Test
    @DisplayName("a view that references a place needs a token to render; its definition stays readable")
    void placeViewsNeedAToken() throws Exception {
        send(post("/v1/places"), admin, "{\"id\":\"home\",\"name\":\"Home\",\"shape\":\"point\","
                + "\"geometry\":{\"type\":\"Point\",\"coordinates\":[0,0]},\"radiusKm\":5}").andExpect(status().isCreated());
        send(post("/v1/views"), admin, "{\"slug\":\"home\",\"name\":\"Home\",\"placeIds\":[\"home\"]}")
                .andExpect(status().isCreated());
        seed();
        send(get("/v1/views/home"), null, null).andExpect(status().isOk());
        send(get("/v1/views/home/records"), null, null).andExpect(status().isUnauthorized());
        send(get("/v1/records?view=home"), null, null).andExpect(status().isUnauthorized());
        assertThat(ids(json(send(get("/v1/views/home/records"), admin, null).andExpect(status().isOk()))))
                .containsExactly("q6");
    }

    static ChangeSummary summary(String id, Bbox box, boolean tombstone) {
        return new ChangeSummary(1_000_000L, null, HistoryTier.STANDARD, EVENTS, id, EVENT_KIND, 2,
                PersistReason.CHANGE, T, null, null, tombstone, 0, box, box, box);
    }

    static LiveUpdate live(String id, Bbox box) {
        return new LiveUpdate(EVENTS, id, EVENT_KIND, T, null, 1, null, box, box, Map.of(), null);
    }
}
