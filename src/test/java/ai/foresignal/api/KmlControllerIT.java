package ai.foresignal.api;

import static ai.foresignal.testsupport.tiles.TileTestSources.QUAKE;
import static ai.foresignal.testsupport.tiles.TileTestSources.QUAKE_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.kml.KmlWriter;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.tiles.TileTestSources;
import ai.foresignal.trigger.TriggerRuleService;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
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
import tools.jackson.databind.json.JsonMapper;

/** {@code getKmlNetworkLink}, {@code getKmlRecords}, {@code getKmlTracks} and {@code getTriggerKml} (prompt 058). */
@IntegrationTest
@Import(TileTestSources.class)
class KmlControllerIT {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");
    static final String KML = KmlWriter.KML;

    @Autowired MockMvc mvc;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired AppSettingService settings;
    @Autowired TriggerRuleService rules;
    @Autowired JsonMapper json;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        clock.setInstant(T);
    }

    @AfterEach
    void tearDown() {
        settings.invalidateCache();
    }

    private void quake(String id, double lon, double lat, double mag, Instant at) {
        clock.setInstant(at);
        records.ingestAt(QUAKE, at, NormalizedRecord.builder(id, QUAKE_KIND).observedAt(at).point(lon, lat)
                .put("mag", mag).put("place", "Place " + id).build());
    }

    static Document parse(byte[] xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
    }

    static List<String> names(Document doc) {
        List<String> out = new ArrayList<>();
        NodeList placemarks = doc.getElementsByTagNameNS(KML, "Placemark");
        for (int i = 0; i < placemarks.getLength(); i++) {
            out.add(((Element) placemarks.item(i)).getElementsByTagNameNS(KML, "name").item(0).getTextContent());
        }
        return out;
    }

    static String first(Document doc, String name) {
        NodeList list = doc.getElementsByTagNameNS(KML, name);
        return list.getLength() == 0 ? null : list.item(0).getTextContent();
    }

    @Test
    @DisplayName("the NetworkLink: records href per layer, onInterval refresh, onStop after 2 s, bbox viewFormat, KML media type")
    void networkLink() throws Exception {
        MvcResult result = mvc.perform(get("/v1/kml/networklink.kml").param("layers", QUAKE).param("refresh", "45"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, KmlWriter.KML_MEDIA_TYPE))
                .andReturn();
        Document doc = parse(result.getResponse().getContentAsByteArray());
        assertThat(first(doc, "href")).isEqualTo("http://localhost/v1/kml/records.kml?layer=" + QUAKE);
        assertThat(first(doc, "refreshMode")).isEqualTo("onInterval");
        assertThat(first(doc, "refreshInterval")).isEqualTo("45");
        assertThat(first(doc, "viewRefreshMode")).isEqualTo("onStop");
        assertThat(first(doc, "viewRefreshTime")).isEqualTo("2");
        assertThat(first(doc, "viewFormat")).isEqualTo(KmlWriter.VIEW_FORMAT);
        mvc.perform(get("/v1/kml/networklink.kml").param("refresh", "10")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("records: newest first with title and time; a bbox across the antimeridian finds both sides only")
    void antimeridian() throws Exception {
        quake("east", 179.5, 0, 4.0, T);
        quake("west", -179.5, 0, 4.5, T.plusSeconds(60));
        quake("middle", 0, 0, 5.0, T.plusSeconds(120));
        Document all = parse(mvc.perform(get("/v1/kml/records.kml").param("layer", QUAKE))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, KmlWriter.KML_MEDIA_TYPE))
                .andReturn().getResponse().getContentAsByteArray());
        assertThat(names(all)).containsExactly("Place middle", "Place west", "Place east");
        assertThat(first(all, "description")).contains("Attribution of " + QUAKE);
        Document split = parse(mvc.perform(get("/v1/kml/records.kml").param("source", QUAKE)
                        .param("bbox", "179,-10,-179,10"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray());
        assertThat(names(split)).containsExactlyInAnyOrder("Place west", "Place east");
        mvc.perform(get("/v1/kml/records.kml")).andExpect(status().isBadRequest());
        mvc.perform(get("/v1/kml/records.kml").param("layer", "no-such-layer")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("asOf passes through: the document shows what was known then; a future asOf is 400")
    void asOf() throws Exception {
        quake("q1", 10, 10, 3.0, T);
        quake("q1", 10, 10, 6.0, T.plusSeconds(3600));
        String then = T.plusSeconds(600).toString();
        String xml = mvc.perform(get("/v1/kml/records.kml").param("source", QUAKE).param("asOf", then))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(xml).contains("<value>3.0</value>").doesNotContain("<value>6.0</value>");
        String now = mvc.perform(get("/v1/kml/records.kml").param("source", QUAKE))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(now).contains("<value>6.0</value>");
        mvc.perform(get("/v1/kml/records.kml").param("source", QUAKE).param("asOf", T.plusSeconds(99_999).toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("format=kmz is a zip of doc.kml and the icons used, with the KMZ media type")
    void kmz() throws Exception {
        quake("q1", 10, 10, 3.0, T);
        byte[] zip = mvc.perform(get("/v1/kml/records.kml").param("source", QUAKE).param("format", "kmz"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, KmlWriter.KMZ_MEDIA_TYPE))
                .andReturn().getResponse().getContentAsByteArray();
        List<String> entries = new ArrayList<>();
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
            for (var e = in.getNextEntry(); e != null; e = in.getNextEntry()) {
                entries.add(e.getName());
            }
        }
        assertThat(entries).startsWith("doc.kml");
        mvc.perform(get("/v1/kml/records.kml").param("source", QUAKE).param("format", "kml2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("tracks: a gx:Track for one entity; a missing externalId or kind is 400")
    void tracks() throws Exception {
        quake("q1", 10, 10, 3.0, T);
        quake("q1", 10.5, 10.5, 3.5, T.plusSeconds(60));
        clock.setInstant(T.plusSeconds(120));
        String xml = mvc.perform(get("/v1/kml/tracks.kml").param("sourceId", QUAKE).param("externalId", "q1")
                        .param("from", T.minusSeconds(60).toString()).param("to", T.plusSeconds(120).toString()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, KmlWriter.KML_MEDIA_TYPE))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        Document doc = parse(xml.getBytes(StandardCharsets.UTF_8));
        assertThat(doc.getElementsByTagNameNS(KmlWriter.GX, "Track").getLength()).isEqualTo(1);
        assertThat(doc.getElementsByTagNameNS(KmlWriter.GX, "coord").getLength()).isGreaterThanOrEqualTo(2);
        assertThat(first(doc, "altitudeMode")).isEqualTo("clampToGround");
        mvc.perform(get("/v1/kml/tracks.kml").param("sourceId", QUAKE)).andExpect(status().isBadRequest());
        mvc.perform(get("/v1/kml/tracks.kml")).andExpect(status().isBadRequest());
        mvc.perform(get("/v1/kml/tracks.kml").param("kind", QUAKE_KIND)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("a trigger's geofence exports as a Polygon; a rule without one, or an unknown id, is 404")
    void triggerGeofence() throws Exception {
        UUID fenced = rules.create(json.readTree(("{'name':'Fence <1>','sources':['" + QUAKE + "'],'geofence':"
                + "{'type':'Polygon','coordinates':[[[20,49.5],[21,49.5],[21,50.5],[20,50.5],[20,49.5]]]},'mode':'enter'}")
                .replace('\'', '"'))).rule().getId();
        UUID plain = rules.create(json.readTree(("{'name':'Plain','sources':['" + QUAKE + "'],"
                + "'conditions':[{'path':'mag','op':'>=','value':5}]}").replace('\'', '"'))).rule().getId();
        try {
            Document doc = parse(mvc.perform(get("/v1/triggers/" + fenced + ".kml"))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, KmlWriter.KML_MEDIA_TYPE))
                    .andReturn().getResponse().getContentAsByteArray());
            assertThat(doc.getElementsByTagNameNS(KML, "Polygon").getLength()).isGreaterThanOrEqualTo(1);
            assertThat(first(doc, "name")).isEqualTo("Fence &lt;1&gt;");
            mvc.perform(get("/v1/triggers/" + plain + ".kml")).andExpect(status().isNotFound());
            mvc.perform(get("/v1/triggers/" + UUID.randomUUID() + ".kml")).andExpect(status().isNotFound());
            mvc.perform(get("/v1/triggers/not-a-uuid.kml")).andExpect(status().isNotFound());
        } finally {
            rules.delete(fenced);
            rules.delete(plain);
        }
    }
}
