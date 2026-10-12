package ai.foresignal.api;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.records.RecordsTestSources;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code Records} operations through the real filter chain, controller, services and database,
 * checked against the field names of {@code docs/OpenAPI.json}.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RecordsTestSources.class)
class RecordsControllerIT {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");
    static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);

    @Autowired MockMvc mvc;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired JsonMapper json;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;

    String token;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        token = adminTokens.rotate();
        clock.setInstant(T);
    }

    @AfterEach
    void tearDown() {
        settings.invalidateCache();
    }

    private void ingest(Instant at, String source, NormalizedRecord... batch) {
        if (clock.instant().isBefore(at)) {
            clock.setInstant(at);
        }
        records.ingestAt(source, at, batch);
    }

    private static NormalizedRecord.Builder rec(String id, Instant observedAt) {
        return NormalizedRecord.builder(id, STANDARD_KIND).observedAt(observedAt);
    }

    private void seed() {
        ingest(T, STANDARD,
                rec("a", T).point(1.23456789, 2.5).eventTime(T.minusSeconds(60)).put("name", "Alpha").put("mag", 4.5)
                        .put("extra", "x").build(),
                rec("b", T).point(30, 40).put("name", "Bravo").put("mag", 5.5).build());
    }

    private JsonNode read(MvcResult result) throws Exception {
        return json.readTree(result.getResponse().getContentAsString());
    }

    @Nested
    @DisplayName("listRecords")
    class ListRecords {

        @Test
        @DisplayName("JSON uses the spec's field names and carries watermarkSeq")
        void json() throws Exception {
            seed();
            mvc.perform(get("/v1/records").param("order", "id"))
                    .andExpect(status().isOk())
                    .andExpect(header().exists(RecordsController.WATERMARK_HEADER))
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public, no-cache"))
                    .andExpect(header().string(HttpHeaders.VARY, "Accept, Accept-Encoding, Authorization"))
                    .andExpect(header().exists(HttpHeaders.ETAG))
                    .andExpect(jsonPath("$.items", hasSize(2)))
                    .andExpect(jsonPath("$.items[0].id").value(STANDARD + ":a"))
                    .andExpect(jsonPath("$.items[0].source").value(STANDARD))
                    .andExpect(jsonPath("$.items[0].externalId").value("a"))
                    .andExpect(jsonPath("$.items[0].kind").value(STANDARD_KIND))
                    .andExpect(jsonPath("$.items[0].title").value("Alpha"))
                    .andExpect(jsonPath("$.items[0].version").value(1))
                    .andExpect(jsonPath("$.items[0].seq").isNumber())
                    .andExpect(jsonPath("$.items[0].observedAt").value("2026-03-01T00:00:00Z"))
                    .andExpect(jsonPath("$.items[0].persistedObservedAt").value("2026-03-01T00:00:00Z"))
                    .andExpect(jsonPath("$.items[0].eventTime").value("2026-02-28T23:59:00Z"))
                    .andExpect(jsonPath("$.items[0].validUntil").value(nullValue()))
                    .andExpect(jsonPath("$.items[0].tombstone").value(false))
                    .andExpect(jsonPath("$.items[0].live").value(false))
                    .andExpect(jsonPath("$.items[0].qaFlags").value(0))
                    .andExpect(jsonPath("$.items[0].geometry.type").value("Point"))
                    .andExpect(jsonPath("$.items[0].payload.mag").value(4.5))
                    .andExpect(jsonPath("$.items[0].attribution").value(nullValue()))
                    .andExpect(jsonPath("$.items[0].ingestedAt").doesNotExist())
                    .andExpect(jsonPath("$.watermarkSeq").isNumber())
                    .andExpect(jsonPath("$.asOfSeq").isNumber())
                    .andExpect(jsonPath("$.nextCursor").value(nullValue()))
                    .andExpect(jsonPath("$.approximate").doesNotExist());
        }

        @Test
        @DisplayName("GeoJSON via ?format and via Accept; format wins")
        void geojson() throws Exception {
            seed();
            for (var request : List.of(get("/v1/records").param("format", "geojson"),
                    get("/v1/records").header(HttpHeaders.ACCEPT, "application/geo+json"))) {
                mvc.perform(request.param("order", "id"))
                        .andExpect(status().isOk())
                        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, startsWith("application/geo+json")))
                        .andExpect(jsonPath("$.type").value("FeatureCollection"))
                        .andExpect(jsonPath("$.features[0].type").value("Feature"))
                        .andExpect(jsonPath("$.features[0].id").value(STANDARD + ":a"))
                        .andExpect(jsonPath("$.features[0].geometry.type").value("Point"))
                        .andExpect(jsonPath("$.features[0].properties.title").value("Alpha"))
                        .andExpect(jsonPath("$.features[0].properties.live").value(false))
                        .andExpect(jsonPath("$.features[0].properties.payload.mag").value(4.5))
                        .andExpect(jsonPath("$.watermarkSeq").isNumber());
            }
            mvc.perform(get("/v1/records").param("format", "json").header(HttpHeaders.ACCEPT, "application/geo+json"))
                    .andExpect(jsonPath("$.items").isArray());
        }

        @Test
        @DisplayName("fields, precision and simplify shape the output")
        void projectionPrecisionSimplify() throws Exception {
            seed();
            Coordinate[] line = new Coordinate[21];
            for (int i = 0; i <= 20; i++) {
                line[i] = new Coordinate(10 + i * 0.001, 10 + (i % 2) * 0.00001);
            }
            ingest(T, STANDARD, rec("line", T).geometry(GEOMETRY.createLineString(line)).build());

            JsonNode projected = read(mvc.perform(get("/v1/records")
                            .param("externalId", "a").param("fields", "externalId,payload.mag"))
                    .andExpect(status().isOk()).andReturn());
            JsonNode item = projected.get("items").get(0);
            assertThat(item.propertyNames()).containsExactlyInAnyOrder("externalId", "payload");
            assertThat(item.get("payload").propertyNames()).containsExactly("mag");

            JsonNode rounded = read(mvc.perform(get("/v1/records").param("externalId", "a").param("precision", "2"))
                    .andReturn());
            assertThat(rounded.get("items").get(0).get("geometry").get("coordinates").get(0).doubleValue()).isEqualTo(1.23);

            JsonNode simplified = read(mvc.perform(get("/v1/records").param("externalId", "line,a")
                    .param("simplify", "1000").param("order", "id")).andReturn());
            JsonNode a = simplified.get("items").get(0);
            JsonNode lineItem = simplified.get("items").get(1);
            assertThat(a.get("geometry").get("type").asString()).isEqualTo("Point");
            assertThat(lineItem.get("geometry").get("coordinates").size()).isLessThan(21).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("ETag and 304 on lists; the tag changes after a write")
        void listETag() throws Exception {
            seed();
            String etag = mvc.perform(get("/v1/records")).andReturn().getResponse().getHeader(HttpHeaders.ETAG);
            mvc.perform(get("/v1/records").header(HttpHeaders.IF_NONE_MATCH, etag))
                    .andExpect(status().isNotModified())
                    .andExpect(header().string(HttpHeaders.ETAG, etag));
            ingest(T, STANDARD, rec("c", T).build());
            mvc.perform(get("/v1/records").header(HttpHeaders.IF_NONE_MATCH, etag)).andExpect(status().isOk());
        }

        @Test
        @DisplayName("pages with nextCursor")
        void paging() throws Exception {
            seed();
            JsonNode first = read(mvc.perform(get("/v1/records").param("order", "id").param("limit", "1")).andReturn());
            assertThat(first.get("items").get(0).get("externalId").asString()).isEqualTo("a");
            String cursor = first.get("nextCursor").asString();
            mvc.perform(get("/v1/records").param("order", "id").param("limit", "1").param("cursor", cursor))
                    .andExpect(jsonPath("$.items[0].externalId").value("b"))
                    .andExpect(jsonPath("$.nextCursor").value(nullValue()));
        }

        @Test
        @DisplayName("sort=distance returns distanceKm")
        void distance() throws Exception {
            seed();
            mvc.perform(get("/v1/records").param("near", "1.2,2.5").param("radiusKm", "100").param("sort", "distance"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.items[0].distanceKm").isNumber());
        }

        @Test
        @DisplayName("asOf through the API echoes asOf, horizon and approximate")
        void asOf() throws Exception {
            seed();
            clock.setInstant(T.plusSeconds(60));
            mvc.perform(get("/v1/records").param("asOf", "2026-03-01T00:00:30Z").param("source", STANDARD))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(2)))
                    .andExpect(jsonPath("$.items[0].ingestedAt").value("2026-03-01T00:00:00Z"))
                    .andExpect(jsonPath("$.asOf").value("2026-03-01T00:00:30Z"))
                    .andExpect(jsonPath("$.approximate").value(false))
                    .andExpect(jsonPath("$.horizon").isString());
            mvc.perform(get("/v1/records").param("asOf", "2026-03-01T00:00:30Z"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("ASOF_FILTER_REQUIRED"));
            mvc.perform(get("/v1/records").param("asOf", "2026-03-01T00:00:30Z").param("kind", STANDARD_KIND)
                            .param("since", "2026-03-01T00:00:00Z"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("ASOF_INVALID"));
            mvc.perform(get("/v1/records").param("asOf", "2025-01-01T00:00:00Z").param("source", STANDARD))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("ASOF_BEYOND_RETENTION"))
                    .andExpect(jsonPath("$.horizon").isString());
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "bbox=1,2,3", "bbox=0,10,10,5", "near=1,2", "radiusKm=5", "near=1,2&radiusKm=0", "limit=0", "limit=5001",
            "sort=distance", "sort=distance&order=id&near=1,2&radiusKm=5", "order=sideways", "timeField=valid",
            "fields=bogus", "precision=8", "simplify=-1", "include=secrets", "format=xml", "since=yesterday",
            "since=2026-03-02T00:00:00Z&until=2026-03-01T00:00:00Z", "asOfSeq=-1", "includeExpired=maybe"
        })
        @DisplayName("400 for malformed parameters")
        void badRequests(String query) throws Exception {
            mvc.perform(get("/v1/records?" + query))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        }

        @Test
        @DisplayName("400 INVALID_CURSOR for a bad cursor")
        void badCursor() throws Exception {
            mvc.perform(get("/v1/records").param("cursor", "c1.garbage.0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_CURSOR"));
        }

        @Test
        @DisplayName("404 for an unknown source filter")
        void unknownSource() throws Exception {
            mvc.perform(get("/v1/records").param("source", "no-such-source")).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("private sources are hidden from anonymous callers and visible with a token")
        void privateSources() throws Exception {
            ingest(T, RecordsTestSources.PRIVATE, NormalizedRecord.builder("s1", RecordsTestSources.PRIVATE_KIND)
                    .observedAt(T).build());
            mvc.perform(get("/v1/records")).andExpect(jsonPath("$.items", hasSize(0)));
            mvc.perform(get("/v1/records").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(jsonPath("$.items", hasSize(1)));
            mvc.perform(get("/v1/records/" + RecordsTestSources.PRIVATE + "/s1")).andExpect(status().isNotFound());
            mvc.perform(get("/v1/kinds")).andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("getRecord")
    class GetRecord {

        @Test
        @DisplayName("returns the record with a strong ETag; If-None-Match gives 304")
        void getWithETag() throws Exception {
            seed();
            MvcResult result = mvc.perform(get("/v1/records/" + STANDARD + "/a"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(STANDARD + ":a"))
                    .andExpect(jsonPath("$.title").value("Alpha"))
                    .andExpect(header().string(HttpHeaders.ETAG, "\"1." + T.toEpochMilli() + "\""))
                    .andReturn();
            String etag = result.getResponse().getHeader(HttpHeaders.ETAG);
            mvc.perform(get("/v1/records/" + STANDARD + "/a").header(HttpHeaders.IF_NONE_MATCH, etag))
                    .andExpect(status().isNotModified());
            mvc.perform(get("/v1/records/" + STANDARD + "/a").param("format", "geojson"))
                    .andExpect(jsonPath("$.type").value("Feature"))
                    .andExpect(jsonPath("$.properties.externalId").value("a"));
            mvc.perform(get("/v1/records/" + STANDARD + "/a").param("fields", "title").param("precision", "1"))
                    .andExpect(jsonPath("$.title").value("Alpha"))
                    .andExpect(jsonPath("$.id").doesNotExist());
        }

        @Test
        @DisplayName("404 for an unknown source or record")
        void notFound() throws Exception {
            mvc.perform(get("/v1/records/no-such-source/a")).andExpect(status().isNotFound());
            mvc.perform(get("/v1/records/" + STANDARD + "/missing"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("NOT_FOUND"));
            mvc.perform(get("/v1/records/" + STANDARD + "/a").param("format", "csv")).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("observers: omitted for anonymous callers, returned to admin tokens or with exposeObserver")
        void observerGating() throws Exception {
            ingest(T, TRACK, NormalizedRecord.builder("ac", TRACK_KIND).observedAt(T).point(10, 50).put("callsign", "X")
                    .observers(List.of(Observer.of("rx-1"))).build());
            String url = "/v1/records/" + TRACK + "/ac?include=observer";
            mvc.perform(get(url)).andExpect(status().isOk()).andExpect(jsonPath("$.observer").doesNotExist());
            mvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(jsonPath("$.observer[0].receiverId").value("rx-1"));
            mvc.perform(get("/v1/records/" + TRACK + "/ac").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(jsonPath("$.observer").doesNotExist());

            settings.put(SettingKeys.SECURITY, new SecuritySettings(true, true));
            settings.invalidateCache();
            mvc.perform(get(url)).andExpect(jsonPath("$.observer[0].receiverId").value("rx-1"));
            mvc.perform(get("/v1/records/" + TRACK + "/ac/versions?include=observer"))
                    .andExpect(jsonPath("$.items[0].observer[0].receiverId").value("rx-1"));
        }
    }

    @Nested
    @DisplayName("listRecordVersions and queryHistory")
    class VersionsAndHistory {

        @Test
        @DisplayName("versions carry the RecordVersion fields and page by seq")
        void versions() throws Exception {
            seed();
            ingest(T.plusSeconds(10), STANDARD, rec("a", T.plusSeconds(10)).put("name", "Alpha").put("mag", 4.6).build());
            mvc.perform(get("/v1/records/" + STANDARD + "/a/versions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(2)))
                    .andExpect(jsonPath("$.items[0].version").value(1))
                    .andExpect(jsonPath("$.items[0].persistReason").value("FIRST"))
                    .andExpect(jsonPath("$.items[0].ingestedAt").value("2026-03-01T00:00:00Z"))
                    .andExpect(jsonPath("$.items[0].receivedAt").value("2026-03-01T00:00:00Z"))
                    .andExpect(jsonPath("$.items[0].historyTier").value("STANDARD"))
                    .andExpect(jsonPath("$.items[0].downsampled").value(false))
                    .andExpect(jsonPath("$.items[0].title").value("Alpha"))
                    .andExpect(jsonPath("$.items[1].prevSeq").isNumber())
                    .andExpect(jsonPath("$.nextCursor").value(nullValue()));
            mvc.perform(get("/v1/records/" + STANDARD + "/a/versions").param("limit", "1"))
                    .andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.nextCursor").isString());
            mvc.perform(get("/v1/records/" + STANDARD + "/a/versions").param("since", "2026-03-01T00:00:05Z"))
                    .andExpect(jsonPath("$.items", hasSize(1)));
            mvc.perform(get("/v1/records/" + STANDARD + "/missing/versions")).andExpect(status().isNotFound());
            mvc.perform(get("/v1/records/" + STANDARD + "/a/versions").param("limit", "5001"))
                    .andExpect(status().isBadRequest());
            mvc.perform(get("/v1/records/" + STANDARD + "/a/versions").param("cursor", "nope"))
                    .andExpect(jsonPath("$.code").value("INVALID_CURSOR"));
        }

        @Test
        @DisplayName("history needs since and serves JSON and GeoJSON")
        void history() throws Exception {
            seed();
            mvc.perform(get("/v1/history")).andExpect(status().isBadRequest());
            mvc.perform(get("/v1/history").param("since", "2026-02-28T00:00:00Z").param("format", "czml"))
                    .andExpect(status().isBadRequest());
            mvc.perform(get("/v1/history").param("since", "2026-02-28T00:00:00Z"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(2)))
                    .andExpect(jsonPath("$.items[0].persistReason").value("FIRST"));
            mvc.perform(get("/v1/history").param("since", "2026-02-28T00:00:00Z").param("format", "geojson"))
                    .andExpect(jsonPath("$.type").value("FeatureCollection"))
                    .andExpect(jsonPath("$.features", hasSize(2)));
            mvc.perform(get("/v1/history").param("since", "2026-02-28T00:00:00Z").param("limit", "50001"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("listKinds")
    class Kinds {

        @Test
        @DisplayName("counts current records per kind, with display, ETag and 304")
        void kinds() throws Exception {
            seed();
            ingest(T, RecordsTestSources.SCHEMA_A, NormalizedRecord.builder("q1", RecordsTestSources.SCHEMA_KIND)
                    .observedAt(T).build());
            MvcResult result = mvc.perform(get("/v1/kinds"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[?(@.kind == '" + STANDARD_KIND + "')].currentCount").value(org.hamcrest.Matchers.hasItem(2)))
                    .andExpect(jsonPath("$[?(@.kind == '" + RecordsTestSources.SCHEMA_KIND + "')].display.icon")
                            .value(org.hamcrest.Matchers.hasItem("earthquake")))
                    .andExpect(jsonPath("$[?(@.kind == '" + RecordsTestSources.SCHEMA_KIND + "')].display.colour")
                            .value(org.hamcrest.Matchers.hasItem("#D9480F")))
                    .andExpect(jsonPath("$[?(@.kind == '" + STANDARD_KIND + "')].sources[0]")
                            .value(org.hamcrest.Matchers.hasItem(STANDARD)))
                    .andReturn();
            String etag = result.getResponse().getHeader(HttpHeaders.ETAG);
            mvc.perform(get("/v1/kinds").header(HttpHeaders.IF_NONE_MATCH, etag)).andExpect(status().isNotModified());
        }
    }
}
