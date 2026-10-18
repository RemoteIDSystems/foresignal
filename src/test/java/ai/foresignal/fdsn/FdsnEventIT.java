package ai.foresignal.fdsn;

import static ai.foresignal.testsupport.formats.FormatsTestSources.QUAKES;
import static ai.foresignal.testsupport.formats.FormatsTestSources.QUAKES_PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.formats.FormatsTestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.security.TestTokens;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * {@code GET /fdsnws/event/1/query}, {@code /version} and {@code /application.wadl} over HTTP (prompt 141): QuakeML
 * validated against the official QuakeML 1.2 BED schema, the text format, filters and orderings, 204 (and
 * {@code nodata=404}) when empty, FDSN plain-text 400s, the private-source gate and {@code openReads}.
 */
@IntegrationTest
@Import(FormatsTestSources.class)
class FdsnEventIT {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");
    static final String QUERY = "/fdsnws/event/1/query";

    @Autowired MockMvc mvc;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired TokenService tokens;
    @Autowired AppSettingService settings;

    @BeforeEach
    void setUp() {
        clock.setInstant(T.plusSeconds(86_400));
    }

    @AfterEach
    void restore() {
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
    }

    private static NormalizedRecord quake(String id, Instant origin, double lon, double lat, double mag,
            String place) {
        return NormalizedRecord.builder(id, "earthquake").observedAt(origin.plusSeconds(120)).eventTime(origin)
                .point(lon, lat).put("mag", mag).put("magType", "mb").put("place", place).put("depth", 12.5)
                .put("net", "us").put("type", "earthquake").build();
    }

    private void seed() {
        IngestOutcome outcome = records.ingestAt(QUAKES, T.plusSeconds(3600),
                quake("q-small", T.plusSeconds(100), -120.5, 35.5, 3.1, "Central California"),
                quake("q-big", T.plusSeconds(200), 142.4, 38.3, 6.8, "Off the coast of Honshu"),
                quake("q-mid", T.plusSeconds(300), 20.1, -10.2, 4.9, "Somewhere | odd"));
        assertThat(outcome.rejections()).isEmpty();
        assertThat(records.ingestAt(QUAKES_PRIVATE, T.plusSeconds(3600),
                quake("q-secret", T.plusSeconds(150), 10, 10, 7.5, "Secret")).rejections()).isEmpty();
    }

    private String body(MvcResult result) throws Exception {
        return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("format=xml: QuakeML whose eventParameters validates against the BED XSD, newest first, publicIDs")
    void quakeMl() throws Exception {
        seed();
        MvcResult result = mvc.perform(get(QUERY))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/xml"))
                .andReturn();
        Document doc = FdsnFormatsTest.validate(body(result));
        NodeList events = doc.getElementsByTagNameNS(QuakeMlWriter.BED_NS, "event");
        assertThat(events.getLength()).as("the private source is hidden anonymously").isEqualTo(3);
        assertThat(((Element) events.item(0)).getAttribute("publicID")).isEqualTo("smi:foresignal/" + QUAKES + "/q-mid");
        assertThat(((Element) events.item(2)).getAttribute("publicID"))
                .isEqualTo("smi:foresignal/" + QUAKES + "/q-small");
        assertThat(body(result)).contains("<depth><value>12500</value></depth>");
    }

    @Test
    @DisplayName("format=text: header plus one line per event; orderby, limit, magnitude and area filters")
    void textAndFilters() throws Exception {
        seed();
        String text = body(mvc.perform(get(QUERY).param("format", "text").param("orderby", "magnitude"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/plain"))
                .andReturn());
        String[] lines = text.split("\n");
        assertThat(lines[0]).isEqualTo(FdsnTextWriter.HEADER);
        assertThat(lines).hasSize(4);
        assertThat(lines[1]).startsWith("q-big|2026-03-01T00:03:20.000|38.3|142.4|12.5|us|us|" + QUAKES + "|q-big|mb|6.8|");
        assertThat(lines[3]).startsWith("q-small|");
        assertThat(text).contains("Somewhere   odd");

        String limited = body(mvc.perform(get(QUERY).param("format", "text").param("orderby", "time-asc")
                .param("limit", "1")).andExpect(status().isOk()).andReturn());
        assertThat(limited.split("\n")).hasSize(2);
        assertThat(limited.split("\n")[1]).startsWith("q-small|");

        String strong = body(mvc.perform(get(QUERY).param("format", "text").param("minmag", "4.5")
                .param("maxmagnitude", "6")).andExpect(status().isOk()).andReturn());
        assertThat(strong.split("\n")).hasSize(2);
        assertThat(strong).contains("q-mid|");

        String boxed = body(mvc.perform(get(QUERY).param("format", "text").param("minlatitude", "30")
                .param("maxlatitude", "40").param("minlongitude", "-130").param("maxlongitude", "-110"))
                .andExpect(status().isOk()).andReturn());
        assertThat(boxed.split("\n")).hasSize(2);
        assertThat(boxed).contains("q-small|");

        String circle = body(mvc.perform(get(QUERY).param("format", "text").param("latitude", "38")
                .param("longitude", "142").param("maxradius", "2")).andExpect(status().isOk()).andReturn());
        assertThat(circle.split("\n")).hasSize(2);
        assertThat(circle).contains("q-big|");

        String window = body(mvc.perform(get(QUERY).param("format", "text")
                .param("starttime", "2026-03-01T00:02:00").param("endtime", "2026-03-01T00:04:00"))
                .andExpect(status().isOk()).andReturn());
        assertThat(window.split("\n")).hasSize(2);
        assertThat(window).contains("q-big|");
    }

    @Test
    @DisplayName("no match is 204 with an empty body; nodata=404 is a 404 with the FDSN text body")
    void empty() throws Exception {
        seed();
        MvcResult none = mvc.perform(get(QUERY).param("minmagnitude", "9.5")).andExpect(status().isNoContent())
                .andReturn();
        assertThat(none.getResponse().getContentAsByteArray()).isEmpty();
        String notFound = body(mvc.perform(get(QUERY).param("minmagnitude", "9.5").param("nodata", "404"))
                .andExpect(status().isNotFound()).andReturn());
        assertThat(notFound).startsWith("Error 404: Not Found");
    }

    @Test
    @DisplayName("an empty database is 204 too")
    void emptyDatabase() throws Exception {
        mvc.perform(get(QUERY).param("format", "text")).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("a bad or unsupported parameter is a 400 with the FDSN plain-text error body")
    void badRequest() throws Exception {
        String body = body(mvc.perform(get(QUERY).param("eventid", "us123"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/plain"))
                .andReturn());
        assertThat(body).startsWith("Error 400: Bad Request\n\nUnsupported parameter 'eventid'")
                .contains("Usage details are available from").contains("/fdsnws/event/1/application.wadl")
                .contains("Request:\n").contains("Request Submitted:\n").contains("Service version:\n1.2.0");
        mvc.perform(get(QUERY).param("minlatitude", "100")).andExpect(status().isBadRequest());
        mvc.perform(get(QUERY).param("format", "json")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("a token with the private grant sees the private source; without openReads anonymous is 401")
    void privateGateAndOpenReads() throws Exception {
        seed();
        String privateReader = TestTokens.bearer(TestTokens.create(tokens, "fdsn-private", Scope.READ, true));
        String text = body(mvc.perform(get(QUERY).param("format", "text")
                .header(HttpHeaders.AUTHORIZATION, privateReader)).andExpect(status().isOk()).andReturn());
        assertThat(text).contains("q-secret|");

        settings.put(SettingKeys.SECURITY, new SecuritySettings(false, null, null, null));
        mvc.perform(get(QUERY)).andExpect(status().isUnauthorized());
        String reader = TestTokens.bearer(TestTokens.create(tokens, "fdsn-reader", Scope.READ));
        String withToken = body(mvc.perform(get(QUERY).param("format", "text")
                .header(HttpHeaders.AUTHORIZATION, reader)).andExpect(status().isOk()).andReturn());
        assertThat(withToken).contains("q-big|").doesNotContain("q-secret|");
    }

    @Test
    @DisplayName("version is 1.2.0 as text; application.wadl lists the resources and the query parameters")
    void versionAndWadl() throws Exception {
        mvc.perform(get("/fdsnws/event/1/version"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/plain"))
                .andExpect(content().string("1.2.0"));
        String wadl = body(mvc.perform(get("/fdsnws/event/1/application.wadl"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/xml"))
                .andReturn());
        assertThat(wadl).contains("<application xmlns=\"http://wadl.dev.java.net/2009/02\"")
                .contains("<resource path=\"query\">").contains("<resource path=\"version\">")
                .contains("name=\"starttime\"").contains("name=\"maxradius\"").contains("<option value=\"time-asc\"/>")
                .contains("/fdsnws/event/1/\"");
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        assertThat(dbf.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(
                wadl.getBytes(StandardCharsets.UTF_8))).getDocumentElement().getLocalName()).isEqualTo("application");
    }
}
