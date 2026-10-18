package ai.foresignal.czml;

import static ai.foresignal.testsupport.formats.FormatsTestSources.QUAKES;
import static ai.foresignal.testsupport.timetravel.TimeTravelTestSources.MOVER;
import static ai.foresignal.testsupport.timetravel.TimeTravelTestSources.MOVER_PRIVATE;
import static ai.foresignal.testsupport.timetravel.TimeTravelTestSources.VEHICLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.api.RecordsController;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.formats.FormatsTestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code format=czml} on {@code GET /v1/history} and {@code GET /v1/records/{s}/{id}/track} (prompt 141): the
 * document packet with the requested clock and the attribution, one packet per entity with sampled positions,
 * availability, the title as label and the kindDisplay color, the private gate, and the track's ETag.
 */
@IntegrationTest
@Import(FormatsTestSources.class)
class CzmlEndpointsIT {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
    }

    private void ingest(Instant at, String source, NormalizedRecord... batch) {
        if (clock.instant().isBefore(at)) {
            clock.setInstant(at);
        }
        assertThat(records.ingestAt(source, at, batch).rejections()).isEmpty();
    }

    private static NormalizedRecord mover(String id, int step, Instant at) {
        return NormalizedRecord.builder(id, VEHICLE).observedAt(at).point(10 + step * 0.01, 50 + (step % 2) * 0.01)
                .put("callsign", "CS-" + id).put("alt", 1000).put("gs", 200.0).build();
    }

    private void seed() {
        for (int i = 0; i < 5; i++) {
            ingest(T.plusSeconds(i * 60L), MOVER, mover("m1", i, T.plusSeconds(i * 60L)));
        }
        ingest(T.plusSeconds(30), MOVER, mover("m2", 0, T.plusSeconds(30)));
        ingest(T.plusSeconds(30), MOVER_PRIVATE, mover("secret", 0, T.plusSeconds(30)));
        ingest(T.plusSeconds(90), QUAKES, NormalizedRecord.builder("q1", "earthquake").observedAt(T.plusSeconds(90))
                .eventTime(T.plusSeconds(80)).point(-120.5, 35.5).put("mag", 4.2).put("place", "Central California")
                .build());
        clock.setInstant(T.plusSeconds(3600));
    }

    private static JsonNode json(MvcResult result) throws Exception {
        return JSON.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private static JsonNode packet(JsonNode document, String id) {
        for (JsonNode packet : document) {
            if (id.equals(packet.get("id").asString())) {
                return packet;
            }
        }
        return null;
    }

    @Test
    @DisplayName("history: document packet over since..until with attribution, entities with samples, label and color")
    void history() throws Exception {
        seed();
        MvcResult result = mvc.perform(get("/v1/history").param("since", T.toString())
                        .param("until", T.plusSeconds(600).toString()).param("format", "czml"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, startsWith("application/czml+json")))
                .andExpect(header().doesNotExist(RecordsController.CZML_TRUNCATED_HEADER))
                .andReturn();
        JsonNode doc = json(result);
        JsonNode first = doc.get(0);
        assertThat(first.get("id").asString()).isEqualTo("document");
        assertThat(first.get("version").asString()).isEqualTo("1.0");
        assertThat(first.get("clock").get("interval").asString())
                .isEqualTo("2026-03-01T00:00:00Z/2026-03-01T00:10:00Z");
        assertThat(first.get("description").asString()).startsWith("Data: ");

        JsonNode m1 = packet(doc, MOVER + ":m1");
        assertThat(m1).isNotNull();
        assertThat(m1.get("label").get("text").asString()).isEqualTo("CS-m1");
        assertThat(m1.get("availability").asString()).isEqualTo("2026-03-01T00:00:00Z/2026-03-01T00:04:00Z");
        assertThat(m1.get("position").get("epoch").asString()).isEqualTo(T.toString());
        assertThat(m1.get("position").get("cartographicDegrees")).hasSize(5 * 4);
        assertThat(m1.get("position").get("cartographicDegrees").get(4).asDouble()).isEqualTo(60.0);

        JsonNode m2 = packet(doc, MOVER + ":m2");
        assertThat(m2.get("position").get("cartographicDegrees")).as("one sample: constant position").hasSize(3);

        JsonNode quake = packet(doc, QUAKES + ":q1");
        assertThat(quake.get("point").get("color").get("rgba").toString()).isEqualTo("[204,51,0,255]");
        assertThat(quake.get("label").get("text").asString()).isEqualTo("Central California");

        assertThat(packet(doc, MOVER_PRIVATE + ":secret")).as("private source hidden anonymously").isNull();

        JsonNode onlyQuakes = json(mvc.perform(get("/v1/history").param("since", T.toString())
                .param("kind", "earthquake").param("format", "czml")).andExpect(status().isOk()).andReturn());
        assertThat(onlyQuakes).hasSize(2);
        assertThat(onlyQuakes.get(0).get("description").asString()).startsWith("Data: ")
                .isNotEqualTo("Data: Foresignal");
    }

    @Test
    @DisplayName("history czml with no data is just the document packet; count=true still wins")
    void emptyHistory() throws Exception {
        JsonNode doc = json(mvc.perform(get("/v1/history").param("since", T.minusSeconds(60).toString())
                .param("until", T.toString()).param("format", "czml")).andExpect(status().isOk()).andReturn());
        assertThat(doc).hasSize(1);
        assertThat(doc.get(0).get("description").asString()).isEqualTo("Data: Foresignal");
        mvc.perform(get("/v1/history").param("since", T.minusSeconds(60).toString()).param("format", "czml")
                        .param("count", "true"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, startsWith("application/json")));
    }

    @Test
    @DisplayName("track: CZML over [from, to], one entity with the current title, a strong ETag and 304")
    void track() throws Exception {
        seed();
        String url = "/v1/records/" + MOVER + "/m1/track";
        MvcResult result = mvc.perform(get(url).param("from", T.toString())
                        .param("to", T.plusSeconds(600).toString()).param("format", "czml"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, startsWith("application/czml+json")))
                .andReturn();
        JsonNode doc = json(result);
        assertThat(doc).hasSize(2);
        assertThat(doc.get(0).get("clock").get("interval").asString())
                .isEqualTo("2026-03-01T00:00:00Z/2026-03-01T00:10:00Z");
        assertThat(doc.get(0).get("description").asString()).isEqualTo("Data: Attribution of " + MOVER);
        assertThat(doc.get(1).get("id").asString()).isEqualTo(MOVER + ":m1");
        assertThat(doc.get(1).get("label").get("text").asString()).isEqualTo("CS-m1");
        assertThat(doc.get(1).get("properties").get("kind").asString()).isEqualTo(VEHICLE);
        String etag = result.getResponse().getHeader(HttpHeaders.ETAG);
        assertThat(etag).isNotBlank();
        mvc.perform(get(url).param("from", T.toString()).param("to", T.plusSeconds(600).toString())
                        .param("format", "czml").header(HttpHeaders.IF_NONE_MATCH, etag))
                .andExpect(status().isNotModified());
        mvc.perform(get(url).param("format", "kml")).andExpect(status().isBadRequest());
        mvc.perform(get("/v1/records/" + MOVER_PRIVATE + "/secret/track").param("format", "czml"))
                .andExpect(status().isNotFound());
    }
}
