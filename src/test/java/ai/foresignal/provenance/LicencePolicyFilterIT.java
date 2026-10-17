package ai.foresignal.provenance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.api.RecordsController;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenCreatedVia;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
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

/**
 * Prompt 109, task 3: licence-filtered reads. Three runtime sources carry CC BY 4.0 (open), CC BY-NC 4.0
 * (non-commercial) and ODbL 1.0 (share-alike). Under {@code no-nc}, {@code no-sa} and {@code redistributable} the
 * records page, the GeoJSON collection, tiles and the SSE change stream leave the excluded records out, count them
 * ({@code licenceFiltered} on pages, {@code Foresignal-Licence-Filtered} on streams and tiles), aggregate
 * {@code attributions} with counts and send one {@code Link rel="license"} per license. A token's policy is a floor a
 * request cannot loosen, and {@code updateToken} replaces it.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(TestIngestSources.class)
class LicencePolicyFilterIT {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");
    static final String BY = "test-lic-by";
    static final String NC = "test-lic-nc";
    static final String SA = "test-lic-sa";
    static final String KIND = "test-licensed";

    @Autowired MockMvc mvc;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired SourceRegistry registry;
    @Autowired AppSettingService settings;
    @Autowired AdminTokenService adminTokens;
    @Autowired TokenService tokens;

    static SourceDescriptor source(String id, String licence) {
        return SourceDescriptor.builder(id).name("Licence test " + id).kind(KIND).mode(Mode.PUSH)
                .licence(licence).defaultBudget(Budget.of(60, 10_000)).build();
    }

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        clock.setInstant(T);
        for (String id : List.of(BY, NC, SA)) {
            if (registry.contains(id)) {
                registry.unregisterDynamic(id);
            }
        }
        registry.registerDynamic(source(BY, "CC BY 4.0"));
        registry.registerDynamic(source(NC, "CC BY-NC 4.0"));
        registry.registerDynamic(source(SA, "ODbL 1.0"));
        records.ingestAt(BY, T, record("b1", -79.38), record("b2", -79.37));
        records.ingestAt(NC, T, record("n1", -79.38));
        records.ingestAt(SA, T, record("s1", -79.38));
    }

    @AfterEach
    void tearDown() {
        for (String id : List.of(BY, NC, SA)) {
            if (registry.contains(id)) {
                registry.unregisterDynamic(id);
            }
        }
        settings.invalidateCache();
    }

    static NormalizedRecord record(String id, double lon) {
        return NormalizedRecord.builder(id, KIND).observedAt(T).point(lon, 43.65).put("value", 1).build();
    }

    @Test
    @DisplayName("no policy: every record, attributions per source with counts, one Link rel=license per licence")
    void unfiltered() throws Exception {
        MvcResult result = mvc.perform(get("/v1/records").param("kind", KIND).param("order", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(4)))
                .andExpect(jsonPath("$.licenceFiltered").doesNotExist())
                .andExpect(jsonPath("$.attributions", hasSize(3)))
                .andExpect(jsonPath("$.attributions[?(@.sourceId == '" + BY + "')].count").value(hasItem(2)))
                .andExpect(jsonPath("$.attributions[?(@.sourceId == '" + NC + "')].licence")
                        .value(hasItem("CC-BY-NC-4.0")))
                .andExpect(header().exists(RecordsController.ATTRIBUTION_HEADER))
                .andReturn();
        List<String> links = result.getResponse().getHeaders(HttpHeaders.LINK);
        assertThat(links).hasSize(3).allSatisfy(link -> assertThat(link).endsWith("; rel=\"license\""));
        assertThat(result.getResponse().getHeader(HttpHeaders.VARY)).contains("Foresignal-Licence-Policy");
    }

    @Test
    @DisplayName("no-nc, no-sa and redistributable each drop exactly the records they exclude, counted")
    void eachFlag() throws Exception {
        mvc.perform(get("/v1/records").param("kind", KIND).param("licencePolicy", "no-nc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)))
                .andExpect(jsonPath("$.licenceFiltered").value(1))
                .andExpect(jsonPath("$.items[?(@.source == '" + NC + "')]").isEmpty());
        mvc.perform(get("/v1/records").param("kind", KIND).header(RecordsController.LICENCE_POLICY_HEADER, "no-sa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)))
                .andExpect(jsonPath("$.licenceFiltered").value(1))
                .andExpect(jsonPath("$.items[?(@.source == '" + SA + "')]").isEmpty());
        mvc.perform(get("/v1/records").param("kind", KIND)
                        .header(RecordsController.LICENCE_POLICY_HEADER, "no-nc, no-sa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.licenceFiltered").value(2))
                .andExpect(jsonPath("$.attributions", hasSize(1)))
                .andExpect(jsonPath("$.attributions[0].count").value(2));
        // All three licenses allow redistribution, so the flag alone drops nothing.
        mvc.perform(get("/v1/records").param("kind", KIND).param("licencePolicy", "redistributable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(4)));
        mvc.perform(get("/v1/records").param("kind", KIND).param("licencePolicy", "no-such-flag"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GeoJSON: excluded features dropped, attributions and licenceFiltered as foreign members")
    void geoJson() throws Exception {
        mvc.perform(get("/v1/records").param("kind", KIND).param("licencePolicy", "no-nc")
                        .accept("application/geo+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("FeatureCollection"))
                .andExpect(jsonPath("$.features", hasSize(3)))
                .andExpect(jsonPath("$.licenceFiltered").value(1))
                .andExpect(jsonPath("$.attributions", hasSize(2)));
    }

    @Test
    @DisplayName("the ETag differs by licence policy header, so a cached unfiltered page is never revalidated for it")
    void eTagVariesByPolicy() throws Exception {
        String plain = mvc.perform(get("/v1/records").param("kind", KIND)).andReturn().getResponse()
                .getHeader(HttpHeaders.ETAG);
        String filtered = mvc.perform(get("/v1/records").param("kind", KIND)
                        .header(RecordsController.LICENCE_POLICY_HEADER, "no-nc"))
                .andReturn().getResponse().getHeader(HttpHeaders.ETAG);
        assertThat(plain).isNotNull().isNotEqualTo(filtered);
        mvc.perform(get("/v1/records").param("kind", KIND).header(RecordsController.LICENCE_POLICY_HEADER, "no-nc")
                        .header(HttpHeaders.IF_NONE_MATCH, plain))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("a token's policy is a floor: the request may add flags but never remove them; updateToken replaces it")
    void tokenFloor() throws Exception {
        TokenService.CreatedToken created = tokens.create(new TokenService.TokenSpec("floor", Scope.READ, null,
                TokenRestrictions.NONE, null, TokenCreatedVia.API, null, false, List.of("no-nc")));
        String bearer = "Bearer " + created.plaintext();
        mvc.perform(get("/v1/records").param("kind", KIND).header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(jsonPath("$.items", hasSize(3)))
                .andExpect(jsonPath("$.licenceFiltered").value(1));
        // A request cannot loosen the floor; asking for an unrelated flag only adds to it.
        mvc.perform(get("/v1/records").param("kind", KIND).param("licencePolicy", "redistributable")
                        .header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(jsonPath("$.items", hasSize(3)));
        mvc.perform(get("/v1/records").param("kind", KIND).header(RecordsController.LICENCE_POLICY_HEADER, "no-sa")
                        .header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.licenceFiltered").value(2));

        String admin = "Bearer " + adminTokens.rotate();
        mvc.perform(patch("/v1/tokens/" + created.entity().getId()).header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"licencePolicy\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licencePolicy").doesNotExist());
        mvc.perform(get("/v1/records").param("kind", KIND).header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(jsonPath("$.items", hasSize(4)));
        mvc.perform(patch("/v1/tokens/" + created.entity().getId()).header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"licencePolicy\":[\"no-nc\"],\"scope\":\"admin\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("tiles and the SSE change stream prune excluded sources and say so in Foresignal-Licence-Filtered")
    void tilesAndStreams() throws Exception {
        ai.foresignal.store.tiles.TileCoordinates tile = ai.foresignal.testsupport.tiles.TileMath.tileFor(-79.38, 43.65, 10);
        // The kind layer merges the three sources, so the policy prunes NC from it; a layer naming NC alone is
        // excluded whole (404, like any excluded source).
        String url = "/v1/tiles/" + KIND + "/" + tile.z() + "/" + tile.x() + "/" + tile.y() + ".mvt";
        mvc.perform(get(url).param("licencePolicy", "no-nc"))
                .andExpect(status().isOk())
                .andExpect(header().exists(LicencePolicyService.FILTERED_HEADER))
                .andExpect(header().string(HttpHeaders.VARY,
                        org.hamcrest.Matchers.containsString("Foresignal-Licence-Policy")));
        mvc.perform(get("/v1/changes").param("licencePolicy", "no-nc"))
                .andExpect(status().isOk())
                .andExpect(header().exists(LicencePolicyService.FILTERED_HEADER))
                .andExpect(jsonPath("$.items[?(@.source == '" + NC + "')]").isEmpty());
        mvc.perform(get("/v1/changes/stream").param("licencePolicy", "no-nc"))
                .andExpect(header().exists(LicencePolicyService.FILTERED_HEADER));
        mvc.perform(get("/v1/changes")).andExpect(header().doesNotExist(LicencePolicyService.FILTERED_HEADER));
    }
}
