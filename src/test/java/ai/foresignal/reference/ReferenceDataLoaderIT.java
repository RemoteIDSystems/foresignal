package ai.foresignal.reference;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.reference.ReferenceManifest.DatasetSpec;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.reference.ReferenceFixtures;
import ai.foresignal.testsupport.reference.RegionTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * The reference data loader (prompt 040) against a WireMock upstream serving the small fixtures:
 * downloads are verified by SHA-256 (a mismatch is {@code REFERENCE_CHECKSUM_MISMATCH} and loads
 * nothing), the staging swap replaces a table whole and a failed load leaves it untouched, the offline
 * directory loads pre-placed files without a request, GeoNames TSV and zip files parse, and the status
 * and load endpoints answer per the spec.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RegionTestSources.class)
class ReferenceDataLoaderIT extends WireMockSupport {

    static final String ADMIN0 = "/ne/ne_10m_admin_0_countries.geojson";

    @Autowired ReferenceDataLoader loader;
    @Autowired ReferenceManifest manifest;
    @Autowired JdbcTemplate jdbc;
    @Autowired ApplicationContext context;
    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;

    String admin;
    String reader;

    @BeforeEach
    void setUp() {
        admin = TestTokens.bearer(TestTokens.create(tokens, "reference-admin", Scope.ADMIN));
        reader = TestTokens.bearer(TestTokens.create(tokens, "reference-reader", Scope.READ));
    }

    static String sha(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    DatasetSpec admin0(String path, String sha256) {
        return manifest.find("ne-admin0").orElseThrow().with(url(path), sha256);
    }

    List<String> countries() {
        return jdbc.queryForList("SELECT iso2 FROM ref_country ORDER BY iso2", String.class);
    }

    @Test
    @DisplayName("a checksum mismatch is REFERENCE_CHECKSUM_MISMATCH: nothing is loaded and the file is deleted")
    void checksumMismatch() throws Exception {
        byte[] body = ReferenceFixtures.bytes("ne_10m_admin_0_countries.geojson");
        WIREMOCK.stubFor(get(urlEqualTo(ADMIN0)).willReturn(aResponse().withStatus(200).withBody(body)));

        Map<String, String> result = loader.loadNow(List.of(admin0(ADMIN0, "0".repeat(64))));

        assertThat(result).containsEntry("ne-admin0", ReferenceDataLoader.FAILED);
        assertThat(countries()).isEmpty();
        assertThat(jdbc.queryForObject("SELECT error FROM ref_dataset WHERE name = 'ne-admin0'", String.class))
                .isEqualTo(ai.foresignal.api.error.ErrorCodes.REFERENCE_CHECKSUM_MISMATCH);
        assertThat(datasetState("ne-admin0")).isEqualTo(ReferenceDataLoader.FAILED);
        WIREMOCK.verify(getRequestedFor(urlEqualTo(ADMIN0)));
        assertThat(Files.exists(dataDir().resolve("ne_10m_admin_0_countries.geojson"))).isFalse();
    }

    @Test
    @DisplayName("a verified download is staged and swapped in: -99 features skipped, geom_simple set, row recorded")
    void download() throws Exception {
        byte[] body = ReferenceFixtures.bytes("ne_10m_admin_0_countries.geojson");
        WIREMOCK.stubFor(get(urlEqualTo(ADMIN0)).willReturn(aResponse().withStatus(200).withBody(body)));

        assertThat(loader.loadNow(List.of(admin0(ADMIN0, sha(body))))).containsEntry("ne-admin0",
                ReferenceDataLoader.LOADED);

        assertThat(countries()).containsExactly("MX", "US");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM ref_country WHERE geom_simple IS NOT NULL", Long.class))
                .isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT name FROM ref_country WHERE iso2 = 'US'", String.class))
                .isEqualTo("United States of America");
        Map<String, Object> row = jdbc.queryForMap("SELECT sha256, rows, state, loaded_at FROM ref_dataset"
                + " WHERE name = 'ne-admin0'");
        assertThat(row).containsEntry("sha256", sha(body)).containsEntry("rows", 2L)
                .containsEntry("state", ReferenceDataLoader.LOADED);
        assertThat(row.get("loaded_at")).isNotNull();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM ref_stage_country", Long.class)).as("staging emptied")
                .isZero();
    }

    @Test
    @DisplayName("the staging swap is atomic: a new edition replaces the table whole, a broken file leaves it alone")
    void atomicSwap() throws Exception {
        byte[] v1 = ReferenceFixtures.bytes("ne_10m_admin_0_countries.geojson");
        WIREMOCK.stubFor(get(urlEqualTo(ADMIN0)).willReturn(aResponse().withStatus(200).withBody(v1)));
        loader.loadNow(List.of(admin0(ADMIN0, sha(v1))));
        assertThat(countries()).containsExactly("MX", "US");

        byte[] v2 = ("{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":"
                + "{\"ISO_A2_EH\":\"CA\",\"ISO_A3_EH\":\"CAN\",\"NAME\":\"Canada\"},\"geometry\":{\"type\":\"Polygon\","
                + "\"coordinates\":[[[-120,50],[-100,50],[-100,60],[-120,60],[-120,50]]]}}]}")
                .getBytes(StandardCharsets.UTF_8);
        WIREMOCK.stubFor(get(urlEqualTo("/v2" + ADMIN0)).willReturn(aResponse().withStatus(200).withBody(v2)));
        assertThat(loader.loadNow(List.of(admin0("/v2" + ADMIN0, sha(v2))))).containsEntry("ne-admin0",
                ReferenceDataLoader.LOADED);
        assertThat(countries()).as("v2 replaced v1 whole").containsExactly("CA");

        byte[] broken = "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{\"ISO_A2_EH\""
                .getBytes(StandardCharsets.UTF_8);
        WIREMOCK.stubFor(get(urlEqualTo("/v3" + ADMIN0)).willReturn(aResponse().withStatus(200).withBody(broken)));
        assertThat(loader.loadNow(List.of(admin0("/v3" + ADMIN0, sha(broken))))).containsEntry("ne-admin0",
                ReferenceDataLoader.FAILED);
        assertThat(countries()).as("the failed load changed nothing").containsExactly("CA");
        assertThat(datasetState("ne-admin0")).as("still loaded from v2").isEqualTo(ReferenceDataLoader.LOADED);
        assertThat(jdbc.queryForObject("SELECT error FROM ref_dataset WHERE name = 'ne-admin0'", String.class))
                .isEqualTo(ReferenceDataException.LOAD_FAILED);
    }

    @Test
    @DisplayName("an upstream failure is REFERENCE_DOWNLOAD_FAILED and loads nothing")
    void downloadFailure() {
        WIREMOCK.stubFor(get(urlEqualTo(ADMIN0)).willReturn(aResponse().withStatus(404)));
        assertThat(loader.loadNow(List.of(admin0(ADMIN0, "0".repeat(64))))).containsEntry("ne-admin0",
                ReferenceDataLoader.FAILED);
        assertThat(jdbc.queryForObject("SELECT error FROM ref_dataset WHERE name = 'ne-admin0'", String.class))
                .isEqualTo(ReferenceDataException.DOWNLOAD_FAILED);
        assertThat(countries()).isEmpty();
    }

    @Test
    @DisplayName("the offline directory loads pre-placed files with no request; a missing file is REFERENCE_FILE_MISSING")
    void offlineDirectory() throws Exception {
        Map<String, String> result = ReferenceFixtures.loadAll(context);

        assertThat(result.values()).containsOnly(ReferenceDataLoader.LOADED);
        assertThat(countries()).containsExactly("MX", "US");
        assertThat(jdbc.queryForList("SELECT code FROM ref_admin1 ORDER BY code", String.class))
                .containsExactly("MX-NLE", "US-TX");
        assertThat(jdbc.queryForObject("SELECT gn_code FROM ref_admin1 WHERE code = 'US-TX'", String.class))
                .isEqualTo("US.TX");
        assertThat(jdbc.queryForList("SELECT fips5 || ' ' || state_code FROM ref_admin2 ORDER BY fips5", String.class))
                .containsExactly("48201 US-TX", "48453 US-TX");
        assertThat(jdbc.queryForList("SELECT name FROM ref_sea", String.class)).containsExactly("Gulf of Mexico");
        assertThat(jdbc.queryForList("SELECT name FROM gazetteer_place ORDER BY name", String.class))
                .containsExactly("Austin", "Houston", "Monterrey");
        assertThat(jdbc.queryForMap("SELECT iso2, admin1_code, population, timezone FROM gazetteer_place"
                        + " WHERE geonameid = 4671654"))
                .containsEntry("iso2", "US").containsEntry("admin1_code", "TX").containsEntry("population", 931830L)
                .containsEntry("timezone", "America/Chicago");
        assertThat(jdbc.queryForObject("SELECT name FROM gazetteer_admin1 WHERE code = 'MX.19'", String.class))
                .isEqualTo("Nuevo León");
        assertThat(jdbc.queryForList("SELECT iso2 FROM gazetteer_country ORDER BY iso2", String.class))
                .containsExactly("MX", "US");

        Path empty = Files.createTempDirectory("foresignal-reference-empty");
        ReferenceDataLoader offline = ReferenceFixtures.loader(context, empty);
        assertThat(offline.loadNow(List.of(manifest.find("ne-marine").orElseThrow()))).containsEntry("ne-marine",
                ReferenceDataLoader.FAILED);
        assertThat(jdbc.queryForObject("SELECT error FROM ref_dataset WHERE name = 'ne-marine'", String.class))
                .isEqualTo(ReferenceDataException.FILE_MISSING);
        assertThat(jdbc.queryForList("SELECT name FROM ref_sea", String.class)).as("the earlier load stays")
                .containsExactly("Gulf of Mexico");
    }

    @Test
    @DisplayName("GET /v1/reference lists datasets, editions, rows, attributions and the boundary note")
    void statusEndpoint() throws Exception {
        ReferenceFixtures.load(context, List.of("ne-admin0", "geonames-cities15000"));
        mvc.perform(MockMvcRequestBuilders.get("/v1/reference").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loading").value(false))
                .andExpect(jsonPath("$.datasets[?(@.name == 'ne-admin0')].state").value("loaded"))
                .andExpect(jsonPath("$.datasets[?(@.name == 'ne-admin0')].rows").value(2))
                .andExpect(jsonPath("$.datasets[?(@.name == 'ne-admin0')].licence").value("Public domain"))
                .andExpect(jsonPath("$.datasets[?(@.name == 'geonames-cities15000')].rows").value(3))
                .andExpect(jsonPath("$.datasets[?(@.name == 'geonames-cities15000')].licence").value("CC BY 4.0"))
                .andExpect(jsonPath("$.datasets[?(@.name == 'ne-marine')].state").value("absent"))
                .andExpect(jsonPath("$.datasets[?(@.name == 'geonames-cities500')].optional").value(true))
                .andExpect(jsonPath("$.attributions[1]").value(RegionAttribution.GEONAMES))
                .andExpect(jsonPath("$.boundaryNote").isString());
    }

    @Test
    @DisplayName("POST /v1/reference/load is admin; unknown datasets and malformed bodies are 400")
    void loadEndpoint() throws Exception {
        mvc.perform(post("/v1/reference/load").header(HttpHeaders.AUTHORIZATION, reader)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"datasets\":[\"ne-admin1\"]}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/v1/reference/load").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"datasets\":[\"osm-planet\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        mvc.perform(post("/v1/reference/load").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"datasets\":[]}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/v1/reference/load").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"urls\":[\"http://example.org/x\"]}"))
                .andExpect(status().isBadRequest());
        assertThat(loader.loading()).as("nothing was queued").isFalse();
    }

    @Test
    @DisplayName("the manifest names every default dataset with a pinned SHA-256; marine sea areas are Natural Earth")
    void manifest() {
        assertThat(manifest.defaults()).containsExactly("ne-admin0", "ne-admin1", "ne-admin2-us", "ne-marine",
                "geonames-cities15000", "geonames-admin1", "geonames-countryinfo");
        assertThat(manifest.datasets()).allSatisfy(d -> {
            assertThat(d.sha256()).matches("^[0-9a-f]{64}$");
            assertThat(d.url()).startsWith("https://");
            assertThat(d.licence()).isNotBlank();
        });
        assertThat(manifest.datasets()).filteredOn(d -> d.url().contains("geonames.org"))
                .allSatisfy(d -> assertThat(d.rolling()).isTrue());
        assertThat(manifest.find("ne-admin0").orElseThrow().variants()).containsKey("ind");
        assertThat(manifest.datasets()).noneMatch(d -> d.url().contains("marineregions"));
    }

    String datasetState(String name) {
        return jdbc.queryForObject("SELECT state FROM ref_dataset WHERE name = ?", String.class, name);
    }

    Path dataDir() {
        return context.getBean(ai.foresignal.config.ForesignalProperties.class).dataDir().resolve("reference");
    }
}
