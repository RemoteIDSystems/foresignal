package ai.foresignal.ogc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.ogc.cql2.Cql2Queryables;
import ai.foresignal.ogc.cql2.Cql2SqlTranslator;
import ai.foresignal.ogc.cql2.Cql2TextParser;
import ai.foresignal.security.feed.FeedKeyService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.SqlParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.export.ExportItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * OGC API &ndash; Features end to end (prompt 112): landing, conformance, current and history collections, items
 * paging through {@code next}, {@code bbox}, every {@code datetime} form, CQL2 filters in text and JSON, the
 * queryables schema, private exclusion, the license policy, single features, parameter errors and a feed key.
 */
@IntegrationTest
@Import(RetentionTestSources.class)
class OgcFeaturesIT extends ExportItSupport {

    static final JsonMapper JSON = JsonMapper.builder().build();

    /** An open source with field specs. */
    static final String QUAKES = "test-ogc-quakes";

    /** A private source of the same kind. */
    static final String HIDDEN = "test-ogc-hidden";

    /** A source of the same kind whose license forbids redistribution. */
    static final String CLOSED = "test-ogc-closed";

    static final String KIND = "ogc-quake";

    @Autowired OgcFeatureService features;
    @Autowired FeedKeyService feedKeys;
    @Autowired NamedParameterJdbcTemplate named;

    @BeforeEach
    void seed() {
        unregister();
        registry.registerDynamic(source(QUAKES, "CC BY 4.0").titleField("place")
                .fieldSpec("mag", FieldSpec.create(FieldType.NUMBER, "1", "Magnitude", null, null, null, null))
                .fieldSpec("place", FieldSpec.create(FieldType.STRING, null, "Place", null, null, null, null))
                .fieldSpec("tsunami", FieldSpec.create(FieldType.BOOLEAN, null, null, null, null, null, null))
                .build());
        registry.registerDynamic(source(HIDDEN, "CC BY 4.0").privateSource(true).build());
        registry.registerDynamic(source(CLOSED, "OpenSky Network terms (non-commercial research)").build());
        for (int i = 0; i < 5; i++) {
            ingestAt(QUAKES, T0.plusSeconds(i * 60L), quake("q" + i, T0, -97.0 - i, 30.0 + i, 3.0 + i, "Place " + i));
        }
        ingestAt(QUAKES, T0.plusSeconds(600), quake("q0", T0.plusSeconds(600), -97.0, 30.0, 3.5, "Place 0 revised"));
        ingestAt(HIDDEN, T0.plusSeconds(610), quake("h", T0, -97.0, 30.0, 9.0, "Hidden"));
        ingestAt(CLOSED, T0.plusSeconds(620), quake("c", T0, -97.0, 30.0, 8.0, "Closed"));
        features.clearExtents();
    }

    @AfterEach
    void unregister() {
        for (String id : List.of(QUAKES, HIDDEN, CLOSED)) {
            if (registry.contains(id)) {
                registry.unregisterDynamic(id);
            }
        }
    }

    static SourceDescriptor.Builder source(String id, String license) {
        return SourceDescriptor.builder(id).name("OGC test " + id).kind(KIND).mode(Mode.PUSH)
                .historyTier(HistoryTier.STANDARD).defaultBudget(Budget.of(60, 100_000)).licence(license)
                .attribution("Data: " + id);
    }

    static NormalizedRecord quake(String id, java.time.Instant observedAt, double lon, double lat, double mag,
            String place) {
        return NormalizedRecord.builder(id, KIND).observedAt(observedAt).point(lon, lat).put("mag", mag).put("place", place)
                .put("tsunami", mag > 6).build();
    }

    JsonNode json(String url) throws Exception {
        MvcResult result = mvc.perform(get(url)).andExpect(status().isOk()).andReturn();
        return JSON.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    JsonNode items(String query) throws Exception {
        return json("/v1/ogc/collections/" + KIND + "/items" + query);
    }

    static List<String> ids(JsonNode collection) {
        List<String> out = new ArrayList<>();
        collection.get("features").forEach(f -> out.add(f.get("id").asString()));
        return out;
    }

    /** Percent-encodes a query value; a space is {@code %20}, since a servlet query string keeps {@code +}. */
    static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * A GET of an already-encoded URL. {@code MockMvcRequestBuilders.get(String)} treats its argument as a
     * URI template and encodes it again ({@code %3A} becomes {@code %253A}, {@code {} a variable), so the
     * URL is passed as a {@link URI}.
     */
    static MockHttpServletRequestBuilder get(String url) {
        return MockMvcRequestBuilders.get(URI.create(url));
    }

    @Test
    @DisplayName("landing links and the conformance classes of Features, Part 3 and CQL2, without oas30")
    void landingAndConformance() throws Exception {
        JsonNode landing = json("/v1/ogc");
        List<String> rels = new ArrayList<>();
        landing.get("links").forEach(l -> rels.add(l.get("rel").asString()));
        assertThat(rels).contains("self", "conformance", "data", "service-desc");
        JsonNode conformance = json("/v1/ogc/conformance");
        String classes = conformance.get("conformsTo").toString();
        assertThat(classes).contains("ogcapi-features-1/1.0/conf/core", "ogcapi-features-1/1.0/conf/geojson",
                "ogcapi-features-3/1.0/conf/filter", "ogcapi-features-3/1.0/conf/features-filter",
                "ogcapi-features-3/1.0/conf/queryables", "cql2/1.0/conf/basic-cql2", "cql2/1.0/conf/cql2-text",
                "cql2/1.0/conf/cql2-json", "cql2/1.0/conf/basic-spatial-functions").doesNotContain("oas30");
    }

    @Test
    @DisplayName("one current and one history collection per kind, with extents and attribution")
    void collections() throws Exception {
        JsonNode all = json("/v1/ogc/collections");
        List<String> idsSeen = new ArrayList<>();
        all.get("collections").forEach(c -> idsSeen.add(c.get("id").asString()));
        assertThat(idsSeen).contains(KIND, KIND + "-history");
        JsonNode current = json("/v1/ogc/collections/" + KIND);
        assertThat(current.get("itemType").asString()).isEqualTo("feature");
        assertThat(current.get("attribution").asString()).contains("Data: " + QUAKES);
        JsonNode bbox = current.get("extent").get("spatial").get("bbox").get(0);
        assertThat(bbox.get(0).asDouble()).isLessThanOrEqualTo(-101.0);
        assertThat(bbox.get(3).asDouble()).isGreaterThanOrEqualTo(34.0);
        assertThat(current.get("extent").get("temporal").get("interval").get(0).get(0).asString()).isNotNull();
        JsonNode history = json("/v1/ogc/collections/" + KIND + "-history");
        assertThat(history.get("itemType").asString()).isEqualTo("feature");
        mvc.perform(get("/v1/ogc/collections/no-such-kind")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("items page through next links with an opaque cursor; ids, properties and counts")
    void paging() throws Exception {
        JsonNode first = items("?limit=2&count=true&source=" + QUAKES);
        assertThat(first.get("type").asString()).isEqualTo("FeatureCollection");
        assertThat(first.get("numberReturned").asInt()).isEqualTo(2);
        assertThat(first.get("numberMatched").asInt()).isEqualTo(5);
        assertThat(first.get("timeStamp").asString()).isNotBlank();
        JsonNode feature = first.get("features").get(0);
        assertThat(feature.get("id").asString()).isEqualTo(QUAKES + ":q0");
        assertThat(feature.get("properties").get("title").asString()).isEqualTo("Place 0 revised");
        assertThat(feature.get("properties").get("sourceId").asString()).isEqualTo(QUAKES);
        assertThat(feature.get("properties").get("version").asInt()).isEqualTo(2);
        assertThat(feature.get("properties").get("mag").asDouble()).isEqualTo(3.5);
        assertThat(feature.get("geometry").get("type").asString()).isEqualTo("Point");
        List<String> seen = new ArrayList<>(ids(first));
        JsonNode page = first;
        int guard = 0;
        while (guard++ < 10) {
            String next = null;
            for (JsonNode link : page.get("links")) {
                if ("next".equals(link.get("rel").asString())) {
                    next = link.get("href").asString();
                }
            }
            if (next == null) {
                break;
            }
            page = json(next.substring(next.indexOf("/v1/")));
            seen.addAll(ids(page));
        }
        assertThat(seen).containsExactly(QUAKES + ":q0", QUAKES + ":q1", QUAKES + ":q2", QUAKES + ":q3", QUAKES + ":q4");
        mvc.perform(get("/v1/ogc/collections/" + KIND + "/items?cursor=garbage")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("history items are versions with {sourceId}:{externalId}:{version} ids")
    void history() throws Exception {
        JsonNode versions = json("/v1/ogc/collections/" + KIND + "-history/items?source=" + QUAKES
                + "&filter=" + enc("title = 'Place 0 revised'"));
        assertThat(ids(versions)).containsExactly(QUAKES + ":q0:2");
        JsonNode all = json("/v1/ogc/collections/" + KIND + "-history/items?source=" + QUAKES);
        assertThat(ids(all)).hasSize(6).contains(QUAKES + ":q0:1", QUAKES + ":q0:2");
        JsonNode one = json("/v1/ogc/collections/" + KIND + "-history/items/" + QUAKES + ":q0:1");
        assertThat(one.get("properties").get("title").asString()).isEqualTo("Place 0");
        mvc.perform(get("/v1/ogc/collections/" + KIND + "-history/items/" + QUAKES + ":q0:9"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("bbox (4 and 6 numbers) and the datetime forms")
    void bboxAndDatetime() throws Exception {
        assertThat(ids(items("?bbox=-98.5,29.5,-96.5,31.5&source=" + QUAKES)))
                .containsExactly(QUAKES + ":q0", QUAKES + ":q1");
        assertThat(ids(items("?bbox=-98.5,29.5,0,-96.5,31.5,1000&source=" + QUAKES)))
                .containsExactly(QUAKES + ":q0", QUAKES + ":q1");
        // effective time: q1..q4 were observed at T0, the revised q0 at T0 + 10 min
        assertThat(ids(items("?datetime=" + enc(T0.toString()) + "&source=" + QUAKES))).hasSize(4)
                .doesNotContain(QUAKES + ":q0");
        assertThat(ids(items("?datetime=" + enc("../" + T0.minusSeconds(1)) + "&source=" + QUAKES))).isEmpty();
        assertThat(ids(items("?datetime=" + enc(T0 + "/..") + "&source=" + QUAKES))).hasSize(5);
        assertThat(ids(items("?datetime=" + enc(T0.minusSeconds(60) + "/" + T0.plusSeconds(60)) + "&source=" + QUAKES)))
                .hasSize(4);
        assertThat(ids(items("?datetime=" + enc(T0.plusSeconds(300) + "/") + "&source=" + QUAKES)))
                .containsExactly(QUAKES + ":q0");
        // history matches observed time: the first q0 version was observed at T0
        assertThat(ids(json("/v1/ogc/collections/" + KIND + "-history/items?source=" + QUAKES + "&datetime="
                + enc("../" + T0)))).hasSize(5).contains(QUAKES + ":q0:1");
        mvc.perform(get("/v1/ogc/collections/" + KIND + "/items?bbox=1,2,3")).andExpect(status().isBadRequest());
        mvc.perform(get("/v1/ogc/collections/" + KIND + "/items?bbox-crs=EPSG:3857"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/v1/ogc/collections/" + KIND + "/items?datetime=tomorrow")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("CQL2 filters in text and JSON select the same features")
    void filters() throws Exception {
        String text = "mag > 5 AND S_INTERSECTS(geometry, BBOX(-102, 32, -99, 35))";
        JsonNode byText = items("?source=" + QUAKES + "&filter=" + enc(text));
        assertThat(ids(byText)).containsExactly(QUAKES + ":q3", QUAKES + ":q4");
        String json = "{\"op\":\"and\",\"args\":[{\"op\":\">\",\"args\":[{\"property\":\"mag\"},5]},"
                + "{\"op\":\"s_intersects\",\"args\":[{\"property\":\"geometry\"},{\"bbox\":[-102,32,-99,35]}]}]}";
        JsonNode byJson = items("?source=" + QUAKES + "&filter-lang=cql2-json&filter=" + enc(json));
        assertThat(ids(byJson)).isEqualTo(ids(byText));
        assertThat(ids(items("?source=" + QUAKES + "&filter=" + enc("tsunami = true")))).containsExactly(QUAKES + ":q4");
        assertThat(ids(items("?source=" + QUAKES + "&filter=" + enc("place IS NULL")))).isEmpty();
        assertThat(ids(items("?source=" + QUAKES + "&filter=" + enc("version >= 2")))).containsExactly(QUAKES + ":q0");
        assertThat(ids(items("?source=" + QUAKES + "&filter=" + enc("S_INTERSECTS(geometry, POINT(-97 30))"))))
                .containsExactly(QUAKES + ":q0");
    }

    @Test
    @DisplayName("an invalid filter or unknown property is 400 FILTER_INVALID with the position")
    void filterErrors() throws Exception {
        MvcResult bad = mvc.perform(get("/v1/ogc/collections/" + KIND + "/items?filter=" + enc("mag > 5 AND nope = 1")))
                .andExpect(status().isBadRequest()).andReturn();
        JsonNode problem = JSON.readTree(bad.getResponse().getContentAsString());
        assertThat(problem.get("code").asString()).isEqualTo("FILTER_INVALID");
        assertThat(problem.get("position").asString()).isEqualTo("12");
        mvc.perform(get("/v1/ogc/collections/" + KIND + "/items?filter=" + enc("mag >")))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/v1/ogc/collections/" + KIND + "/items?filter-lang=ecql&filter=x"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/v1/ogc/collections/" + KIND + "/items?unknownParam=1")).andExpect(status().isBadRequest());
        mvc.perform(get("/v1/ogc/collections/" + KIND + "/items?f=json")).andExpect(status().isOk());
        mvc.perform(get("/v1/ogc/collections/" + KIND + "/items?f=html")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("the queryables are a JSON Schema of the field specs plus the built-ins")
    void queryables() throws Exception {
        MvcResult result = mvc.perform(get("/v1/ogc/collections/" + KIND + "/queryables")).andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getContentType()).startsWith("application/schema+json");
        JsonNode schema = JSON.readTree(result.getResponse().getContentAsString());
        assertThat(schema.get("type").asString()).isEqualTo("object");
        JsonNode props = schema.get("properties");
        assertThat(props.get("mag").get("type").asString()).isEqualTo("number");
        assertThat(props.get("mag").get("title").asString()).isEqualTo("Magnitude");
        assertThat(props.get("place").get("type").asString()).isEqualTo("string");
        assertThat(props.get("observedAt").get("format").asString()).isEqualTo("date-time");
        assertThat(props.get("geometry").get("format").asString()).isEqualTo("geometry-any");
        assertThat(props.has("title") && props.has("sourceId") && props.has("eventTime") && props.has("version")).isTrue();
    }

    @Test
    @DisplayName("private sources are excluded for anonymous callers and shown to admin")
    void privateExclusion() throws Exception {
        assertThat(ids(items(""))).doesNotContain(HIDDEN + ":h");
        mvc.perform(get("/v1/ogc/collections/" + KIND + "/items/" + HIDDEN + ":h")).andExpect(status().isNotFound());
        MvcResult asAdmin = mvc.perform(get("/v1/ogc/collections/" + KIND + "/items")
                .header(HttpHeaders.AUTHORIZATION, admin)).andExpect(status().isOk()).andReturn();
        assertThat(ids(JSON.readTree(asAdmin.getResponse().getContentAsString()))).contains(HIDDEN + ":h");
    }

    @Test
    @DisplayName("licencePolicy=redistributable removes records that may not be redistributed; attribution headers")
    void licencePolicy() throws Exception {
        assertThat(ids(items(""))).contains(CLOSED + ":c");
        assertThat(ids(items("?licencePolicy=redistributable"))).doesNotContain(CLOSED + ":c").contains(QUAKES + ":q0");
        MvcResult result = mvc.perform(get("/v1/ogc/collections/" + KIND + "/items?source=" + QUAKES))
                .andExpect(status().isOk()).andReturn();
        assertThat(result.getResponse().getHeader("Foresignal-Attribution")).contains(QUAKES);
        JsonNode body = JSON.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("attributions").get(0).get("sourceId").asString()).isEqualTo(QUAKES);
        JsonNode feature = json("/v1/ogc/collections/" + KIND + "/items/" + QUAKES + ":q1");
        assertThat(feature.get("id").asString()).isEqualTo(QUAKES + ":q1");
        assertThat(feature.get("links").toString()).contains("\"rel\":\"collection\"");
    }

    @Test
    @DisplayName("with openReads off, a feed key reads features through its path; no credential is 401")
    void feedKey() throws Exception {
        settings.put(SettingKeys.SECURITY, new SecuritySettings(false));
        // a feed key must carry restrictions (prompt 035): this one is limited to the quake source
        FeedKeyService.CreatedKey key = feedKeys.create(JSON.readTree("{\"name\":\"qgis\",\"prefixes\":[\"/v1/ogc\"],"
                + "\"restrictions\":{\"sources\":[\"" + QUAKES + "\"]},"
                + "\"expiresAt\":\"" + T0.plus(Duration.ofDays(30)) + "\"}"));
        mvc.perform(get("/v1/ogc/collections/" + KIND + "/items")).andExpect(status().isUnauthorized());
        MvcResult result = mvc.perform(get("/v1/k/" + key.key() + "/ogc/collections/" + KIND + "/items?source=" + QUAKES))
                .andExpect(status().isOk()).andReturn();
        JsonNode body = JSON.readTree(result.getResponse().getContentAsString());
        assertThat(ids(body)).hasSize(5);
        assertThat(body.get("links").get(0).get("href").asString()).contains("/v1/k/" + key.key() + "/ogc/");
    }

    @Test
    @DisplayName("the CQL2 translation agrees with the 017 operator truth table on PostgreSQL")
    void truthTable() throws Exception {
        JsonNode root;
        try (InputStream in = getClass().getResourceAsStream("/trigger/operator-truth-table.json")) {
            root = JSON.readTree(in);
        }
        Set<String> overlapping = Set.of(">", ">=", "<", "<=", "==", "!=");
        int checked = 0;
        for (JsonNode row : root.get("rows")) {
            String op = row.get("op").asString();
            String path = row.get("path").asString();
            JsonNode value = row.get("value");
            boolean scalar = value.isNumber() || value.isString() || value.isBoolean();
            if (!overlapping.contains(op) || path.contains(".") || !scalar
                    || (!value.isNumber() && !op.equals("==") && !op.equals("!="))) {
                continue;
            }
            String cqlOp = op.equals("==") ? "=" : op.equals("!=") ? "<>" : op;
            String literal = value.isString() ? "'" + value.asString().replace("'", "''") + "'" : value.toString();
            SqlParams p = new SqlParams();
            String predicate = Cql2SqlTranslator.translate(Cql2TextParser.parse("\"" + path + "\" " + cqlOp + " " + literal),
                    new Cql2Queryables(Map.of(path, FieldType.STRING)), new Cql2SqlTranslator.Columns("t.source_id",
                            null, "t.observed_at", "t.event_time", "t.version", "t.payload", "t.geom"), p);
            p.source().addValue("payloadJson", row.get("payload").toString());
            Boolean result = named.queryForObject("SELECT " + predicate + " FROM (SELECT CAST(:payloadJson AS jsonb)"
                    + " AS payload, 'x'::text AS source_id, now() AS observed_at, now() AS event_time, 1 AS version,"
                    + " NULL::geometry AS geom) t", p.source(), Boolean.class);
            assertThat(result).as(row.toString()).isEqualTo(row.get("expected").asBoolean());
            checked++;
        }
        assertThat(checked).isGreaterThan(10);
    }
}
