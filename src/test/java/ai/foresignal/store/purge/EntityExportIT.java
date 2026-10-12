package ai.foresignal.store.purge;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.AlertEntity;
import ai.foresignal.store.AlertRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.retention.RetentionItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Entity export (prompt 039): GeoJSON, ndjson and GPX, each with a manifest whose SHA-256 the test
 * recomputes over the bytes before the manifest; the REST endpoint's headers, 404 and admin scope.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RetentionTestSources.class)
class EntityExportIT extends RetentionItSupport {

    @Autowired EntityExportService exports;
    @Autowired AlertRepository alerts;
    @Autowired JsonMapper json;
    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;

    private void seed(String id, int count) {
        for (int i = 0; i < count; i++) {
            Instant at = T0.plus(Duration.ofMinutes(i));
            ingestAt(STANDARD, at, NormalizedRecord.builder(id, STANDARD_KIND).observedAt(at)
                    .point(10.0 + i, 50.0).put("v", i).put("altitudeM", 100 + i).build());
        }
        AlertEntity a = new AlertEntity(UUID.randomUUID(), 1L, STANDARD, id, T0);
        a.setSeverity("info");
        a.setMessage("Alert for " + id);
        alerts.saveAndFlush(a);
    }

    private String export(String id, ExportFormat format) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exports.write(STANDARD, id, format, out);
        return out.toString(StandardCharsets.UTF_8);
    }

    static String sha256(String text) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
    }

    /** The hash over every byte before the last occurrence of {@code marker}. */
    static String prefixHash(String body, String marker) throws Exception {
        return sha256(body.substring(0, body.lastIndexOf(marker)));
    }

    @Test
    @DisplayName("GeoJSON: current plus every version, alerts, and a manifest hashing the bytes before it")
    void geojson() throws Exception {
        seed("x-geo", 3);
        String body = export("x-geo", ExportFormat.GEOJSON);
        JsonNode doc = json.readTree(body);
        assertThat(doc.path("type").asString()).isEqualTo("FeatureCollection");
        JsonNode features = doc.path("features");
        assertThat(features.size()).isEqualTo(4);
        assertThat(features.get(0).path("properties").path("exportPart").asString()).isEqualTo("current");
        assertThat(features.get(1).path("properties").path("exportPart").asString()).isEqualTo("version");
        assertThat(features.get(1).path("geometry").path("type").asString()).isEqualTo("Point");
        assertThat(doc.path("alerts").size()).isEqualTo(1);
        JsonNode manifest = doc.path("manifest");
        assertThat(manifest.path("counts").path("versions").asInt()).isEqualTo(3);
        assertThat(manifest.path("counts").path("alerts").asInt()).isEqualTo(1);
        assertThat(manifest.path("sha256").asString()).isEqualTo(prefixHash(body, GeoJsonExportWriter.MARKER));
    }

    @Test
    @DisplayName("ndjson: typed lines in order, the manifest last with the hash of the lines before it")
    void ndjson() throws Exception {
        seed("x-nd", 2);
        String body = export("x-nd", ExportFormat.NDJSON);
        String[] lines = body.split("\n");
        List<String> types = new ArrayList<>();
        for (String line : lines) {
            types.add(json.readTree(line).path("type").asString());
        }
        assertThat(types).containsExactly("current", "version", "version", "alert", "manifest");
        JsonNode manifest = json.readTree(lines[lines.length - 1]).path("manifest");
        assertThat(manifest.path("sha256").asString()).isEqualTo(prefixHash(body, NdjsonExportWriter.MARKER));
        StringBuilder before = new StringBuilder();
        for (int i = 0; i < lines.length - 1; i++) {
            before.append(lines[i]).append('\n');
        }
        assertThat(manifest.path("sha256").asString()).isEqualTo(sha256(before.toString()));
    }

    @Test
    @DisplayName("GPX: non-tracker kinds become waypoints with ele and time; the manifest hash verifies")
    void gpx() throws Exception {
        seed("x-gpx", 2);
        String body = export("x-gpx", ExportFormat.GPX);
        assertThat(body).startsWith("<?xml").contains("<gpx version=\"1.1\"").contains("<wpt lat=\"50.0\" lon=\"10.0\">")
                .contains("<ele>100.0</ele>").doesNotContain("<trk>").endsWith("</gpx>\n");
        String manifestJson = body.substring(body.indexOf("\">", body.lastIndexOf("<foresignal:manifest")) + 2,
                body.lastIndexOf("</foresignal:manifest>")).replace("&quot;", "\"").replace("&amp;", "&")
                .replace("&lt;", "<").replace("&gt;", ">").replace("&apos;", "'");
        JsonNode manifest = json.readTree(manifestJson);
        assertThat(manifest.path("sha256").asString()).isEqualTo(prefixHash(body, GpxExportWriter.MARKER));
        assertThat(manifest.path("alertIds").size()).isEqualTo(1);
    }

    @Test
    @DisplayName("an unknown entity is 404 and an unknown format 400")
    void unknown() {
        assertThatThrownBy(() -> exports.require(STANDARD, "nobody")).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> ExportFormat.parse("kml")).isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("REST: admin gets an attachment with the format's media type; read tokens get 403")
    void rest() throws Exception {
        seed("x-rest", 1);
        String admin = TestTokens.bearer(TestTokens.create(tokens, "export-admin", Scope.ADMIN));
        String read = TestTokens.bearer(TestTokens.create(tokens, "export-read", Scope.READ));
        mvc.perform(get("/v1/records/{s}/{e}/export", STANDARD, "x-rest").param("format", "ndjson")
                        .header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE,
                        org.hamcrest.Matchers.startsWith("application/x-ndjson")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        org.hamcrest.Matchers.containsString("attachment")));
        mvc.perform(get("/v1/records/{s}/{e}/export", STANDARD, "nobody").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNotFound());
        mvc.perform(get("/v1/records/{s}/{e}/export", STANDARD, "x-rest").header(HttpHeaders.AUTHORIZATION, read))
                .andExpect(status().isForbidden());
        mvc.perform(post("/v1/records/{s}/{e}/purge", STANDARD, "x-rest").header(HttpHeaders.AUTHORIZATION, read))
                .andExpect(status().isForbidden());
        mvc.perform(post("/v1/records/{s}/{e}/purge", STANDARD, "x-rest").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType("application/json").content("{\"purgeAlerts\":\"yes\"}"))
                .andExpect(status().isBadRequest());
        clock.setInstant(T0.plus(Duration.ofHours(1)));
        String report = mvc.perform(post("/v1/records/{s}/{e}/purge", STANDARD, "x-rest")
                        .header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode parsed = json.readTree(report);
        assertThat(parsed.has("tombstoneSeq") && parsed.has("versionsDeleted") && parsed.has("alertsRedacted")
                && parsed.has("citingDocuments")).isTrue();
    }
}
