package ai.foresignal.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.feed.FeedKeyService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.adapters.celestrak.CelestrakGpAdapter;
import ai.foresignal.space.OmmFormats;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The CelesTrak-compatible output (prompt 064) over heads ingested through the real {@code celestrak-gp} fetch:
 * every FORMAT, selectors, TLE columns and checksums against CelesTrak's own TLE, the empty result, the 400s,
 * caching headers, feed-key access as {@code ?key=} and refusal without {@code read}. Never calls CelesTrak.
 */
class CelestrakCompatControllerIT extends WeatherIngestSupport {

    private static final String ID = CelestrakGpAdapter.ID;
    private static final Instant NOW = Instant.parse("2026-10-13T13:00:00Z");

    @Autowired private MockMvc mvc;
    @Autowired private AppSettingService settings;
    @Autowired private AdminTokenService adminTokens;
    @Autowired private FeedKeyService feedKeys;
    @Autowired private JsonMapper json;

    private String admin;

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW);
        configure(ID, false, Map.of(CelestrakGpAdapter.MEMBERS_PARAM, List.of()));
        UPSTREAM.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(
                urlEqualTo("/NORAD/elements/gp.php?GROUP=active&FORMAT=csv")).willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "text/plain").withBody(AdapterFixtures.bytes(ID, "stations.csv"))));
        assertThat(fetch(ID).success()).isTrue();
        UPSTREAM.resetAll();
        settings.invalidateCache();
        settings.put(SettingKeys.SECURITY, new SecuritySettings(false));
        admin = adminTokens.rotate();
    }

    @AfterEach
    void tearDown() {
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
    }

    private MockHttpServletResponse call(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request).andReturn().getResponse();
    }

    private MockHttpServletResponse gp(String query) throws Exception {
        return call(get("/compat/celestrak/gp.php?" + query).header("Authorization", "Bearer " + admin));
    }

    @Test
    @DisplayName("FORMAT=TLE and the NORAD alias: CelesTrak's columns and checksums, six-digit objects left out")
    void tle() throws Exception {
        MockHttpServletResponse r = gp("CATNR=25544&FORMAT=tle");
        assertThat(r.getStatus()).isEqualTo(200);
        assertThat(r.getContentType()).startsWith("text/plain");
        assertThat(r.getContentAsString()).isEqualTo(AdapterFixtures.text(ID, "iss-tle.txt"));
        MockHttpServletResponse alias = call(get("/compat/celestrak/NORAD/elements/gp.php?CATNR=25544&FORMAT=3LE")
                .header("Authorization", "Bearer " + admin));
        assertThat(alias.getContentAsString()).isEqualTo(r.getContentAsString());

        String stations = gp("GROUP=active&FORMAT=tle").getContentAsString();
        assertThat(stations).contains("ISS (ZARYA)").doesNotContain("SOYUZ-MS 29").doesNotContain("CREW DRAGON 13");
        for (String line : stations.split("\r\n")) {
            if (line.startsWith("1 ") || line.startsWith("2 ")) {
                assertThat(line).hasSize(69);
                assertThat(OmmFormats.checksum(line)).isEqualTo(line.charAt(68) - '0');
                new org.orekit.propagation.analytical.tle.TLE(line.startsWith("1 ") ? line : firstLineBefore(stations, line),
                        line.startsWith("2 ") ? line : secondLineAfter(stations, line),
                        ai.foresignal.orbit.OrekitData.context().getTimeScales().getUTC());
            }
        }
        assertThat(gp("CATNR=25544&FORMAT=2LE").getContentAsString()).startsWith("1 25544U");
    }

    private static String firstLineBefore(String text, String line2) {
        String[] lines = text.split("\r\n");
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].equals(line2)) {
                return lines[i - 1];
            }
        }
        throw new AssertionError("no line 1 for " + line2);
    }

    private static String secondLineAfter(String text, String line1) {
        String[] lines = text.split("\r\n");
        for (int i = 0; i + 1 < lines.length; i++) {
            if (lines[i].equals(line1)) {
                return lines[i + 1];
            }
        }
        throw new AssertionError("no line 2 for " + line1);
    }

    @Test
    @DisplayName("JSON, JSON-PRETTY, CSV, XML and KVN have the right media types; unknown and absent FORMAT are CSV")
    void formats() throws Exception {
        MockHttpServletResponse j = gp("NAME=zarya&FORMAT=json");
        assertThat(j.getContentType()).startsWith("application/json");
        JsonNode arr = json.readTree(j.getContentAsString());
        assertThat(arr).hasSize(1);
        assertThat(arr.get(0).get("NORAD_CAT_ID").asLong()).isEqualTo(25544);
        assertThat(gp("NAME=zarya&FORMAT=JSON-PRETTY").getContentAsString()).contains("\n");
        MockHttpServletResponse csv = gp("GROUP=active&FORMAT=csv");
        assertThat(csv.getContentType()).startsWith("text/csv");
        assertThat(csv.getContentAsString().split("\r\n")).hasSize(22);
        assertThat(gp("GROUP=active&FORMAT=omm").getContentAsString()).isEqualTo(csv.getContentAsString());
        assertThat(gp("GROUP=active").getContentAsString()).isEqualTo(csv.getContentAsString());
        MockHttpServletResponse xml = gp("CATNR=100057&FORMAT=xml");
        assertThat(xml.getContentType()).startsWith("application/xml");
        assertThat(xml.getContentAsString()).contains("<NORAD_CAT_ID>100057</NORAD_CAT_ID>");
        assertThat(gp("CATNR=100057&FORMAT=kvn").getContentAsString()).contains("NORAD_CAT_ID        = 100057");
    }

    @Test
    @DisplayName("selectors: INTDES is a prefix, NAME a case-insensitive substring; an empty result is 'No GP data found'")
    void selectors() throws Exception {
        assertThat(gp("INTDES=1998-067&FORMAT=csv").getContentAsString().split("\r\n").length - 1).isEqualTo(8);
        assertThat(gp("NAME=css&FORMAT=csv").getContentAsString().split("\r\n").length - 1).isEqualTo(3);
        MockHttpServletResponse none = gp("GROUP=weather&FORMAT=tle");
        assertThat(none.getStatus()).isEqualTo(200);
        assertThat(none.getContentType()).startsWith("text/plain");
        assertThat(none.getContentAsString()).isEqualTo("No GP data found");
        assertThat(gp("CATNR=100057&FORMAT=tle").getContentAsString()).isEqualTo("No GP data found");
    }

    @Test
    @DisplayName("400 as plain text: no selector, two selectors, SPECIAL, a bad CATNR")
    void badRequests() throws Exception {
        for (String q : List.of("FORMAT=tle", "GROUP=active&CATNR=25544", "SPECIAL=gpz", "CATNR=abc")) {
            MockHttpServletResponse r = gp(q);
            assertThat(r.getStatus()).as(q).isEqualTo(400);
            assertThat(r.getContentType()).as(q).startsWith("text/plain");
        }
        assertThat(gp("SPECIAL=gpz").getContentAsString()).contains("not supported");
    }

    @Test
    @DisplayName("ETag, max-age=3600 and the elements age; If-None-Match answers 304")
    void caching() throws Exception {
        MockHttpServletResponse r = gp("GROUP=active&FORMAT=json");
        String etag = r.getHeader("ETag");
        assertThat(etag).startsWith("\"");
        assertThat(r.getHeader("Cache-Control")).isEqualTo("max-age=3600");
        assertThat(r.getHeader("X-Foresignal-Elements-Age")).isEqualTo("0");
        assertThat(r.getHeader("Warning")).isNull();
        assertThat(gp("GROUP=active&FORMAT=csv").getHeader("ETag")).isNotEqualTo(etag);
        MockHttpServletResponse cached = call(get("/compat/celestrak/gp.php?GROUP=active&FORMAT=json")
                .header("Authorization", "Bearer " + admin).header("If-None-Match", etag));
        assertThat(cached.getStatus()).isEqualTo(304);
        clock.setInstant(NOW.plus(Duration.ofHours(80)));
        assertThat(gp("GROUP=active&FORMAT=json").getHeader("Warning")).as("past the 3-day TTL nothing is current")
                .isNull();
    }

    @Test
    @DisplayName("without read (openReads off, no credential) it is 401; never open by accident")
    void deniedWithoutRead() throws Exception {
        assertThat(call(get("/compat/celestrak/gp.php?GROUP=active")).getStatus()).isEqualTo(401);
        assertThat(call(post("/compat/celestrak/gp.php?GROUP=active").header("Authorization", "Bearer " + admin))
                .getStatus()).isIn(403, 405);
    }

    @Test
    @DisplayName("a feed key passed as ?key= reads it (private cache); a bad key is 401; a key for other paths 403")
    void feedKey() throws Exception {
        String body = "{\"name\":\"gpredict\",\"prefixes\":[\"/compat/celestrak/\"],\"restrictions\":{\"sources\":[\""
                + ID + "\"]},\"expiresAt\":\"" + NOW.plus(Duration.ofDays(30)) + "\"}";
        String key = feedKeys.create(json.readTree(body)).key();
        MockHttpServletResponse ok = call(get("/compat/celestrak/gp.php?GROUP=active&FORMAT=tle&key=" + key));
        assertThat(ok.getStatus()).isEqualTo(200);
        assertThat(ok.getContentAsString()).contains("ISS (ZARYA)");
        assertThat(ok.getHeader("Cache-Control")).isEqualTo("private, max-age=3600");
        assertThat(ok.getHeader("Referrer-Policy")).isEqualTo("no-referrer");
        assertThat(ok.getContentAsString()).doesNotContain(key);

        assertThat(call(get("/compat/celestrak/gp.php?GROUP=active&key=fsf_nope")).getStatus()).isEqualTo(401);

        String other = "{\"name\":\"records\",\"prefixes\":[\"/v1/records\"],\"restrictions\":{\"sources\":[\"" + ID
                + "\"]},\"expiresAt\":\"" + NOW.plus(Duration.ofDays(30)) + "\"}";
        String otherKey = feedKeys.create(json.readTree(other)).key();
        assertThat(call(get("/compat/celestrak/gp.php?GROUP=active&key=" + otherKey)).getStatus()).isEqualTo(403);

        String elsewhere = "{\"name\":\"quakes\",\"prefixes\":[\"/compat/celestrak/\"],\"restrictions\":{\"sources\":"
                + "[\"usgs-earthquakes\"]},\"expiresAt\":\"" + NOW.plus(Duration.ofDays(30)) + "\"}";
        String quakeKey = feedKeys.create(json.readTree(elsewhere)).key();
        assertThat(call(get("/compat/celestrak/gp.php?GROUP=active&key=" + quakeKey)).getContentAsString())
                .as("a key restricted to another source sees no element sets").isEqualTo("No GP data found");
    }
}
