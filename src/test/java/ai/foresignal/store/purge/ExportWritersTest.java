package ai.foresignal.store.purge;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** The ndjson and GPX export writers (prompt 039): shapes, ordering and the manifest hash. */
class ExportWritersTest {

    private final JsonMapper json = JsonMapper.builder().build();

    private static ExportItem item(String id, double lon, double lat, String at, Double ele) {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("id", "src:" + id);
        record.put("observedAt", at);
        return new ExportItem(record, "{\"type\":\"Point\",\"coordinates\":[" + lon + "," + lat + "]}",
                Instant.parse(at), false, ele);
    }

    private static String sha256(String text) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("ndjson writes one typed line per item and the manifest last with the hash of the lines before")
    void ndjson() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NdjsonExportWriter writer = new NdjsonExportWriter(out, json);
        writer.start("src", "a", "tracker");
        writer.current(item("a", 1, 2, "2026-01-01T00:02:00Z", null));
        writer.version(item("a", 1, 2, "2026-01-01T00:00:00Z", null));
        writer.alert(Map.of("id", 7));
        Map<String, Object> manifest = new LinkedHashMap<>();
        String sha = writer.finishWithHash(manifest, () -> { });
        String body = out.toString(StandardCharsets.UTF_8);
        String[] lines = body.split("\n");
        assertThat(lines).hasSize(4);
        assertThat(lines[0]).startsWith("{\"type\":\"current\"");
        assertThat(lines[2]).startsWith("{\"type\":\"alert\"");
        assertThat(lines[3]).startsWith(NdjsonExportWriter.MARKER).contains(sha);
        assertThat(sha).isEqualTo(sha256(body.substring(0, body.lastIndexOf(NdjsonExportWriter.MARKER))));
        assertThat(manifest).containsEntry("sha256", sha);
    }

    @Test
    @DisplayName("GPX for a tracker writes a trk with trkpts in order and appends a newer current state")
    void gpxTrack() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GpxExportWriter writer = new GpxExportWriter(out, json);
        writer.start("src", "phone & co", "tracker");
        writer.current(item("a", 3, 4, "2026-01-01T00:05:00Z", null));
        writer.version(item("a", 1, 2, "2026-01-01T00:00:00Z", 12.5));
        writer.version(new ExportItem(Map.of("id", "src:a"), null, Instant.parse("2026-01-01T00:01:00Z"), true, null));
        writer.version(item("a", 2, 3, "2026-01-01T00:02:00Z", null));
        String sha = writer.finishWithHash(new LinkedHashMap<>(), () -> { });
        String body = out.toString(StandardCharsets.UTF_8);
        assertThat(body).contains("<name>src:phone &amp; co</name>").contains("<trk>").contains("<trkseg>")
                .contains("<trkpt lat=\"2.0\" lon=\"1.0\"><ele>12.5</ele><time>2026-01-01T00:00:00Z</time></trkpt>");
        assertThat(body.indexOf("lon=\"1.0\"")).isLessThan(body.indexOf("lon=\"2.0\""));
        assertThat(body.indexOf("lon=\"2.0\"")).isLessThan(body.indexOf("lon=\"3.0\""));
        assertThat(writer.points()).isEqualTo(3);
        assertThat(body).contains("</trkseg></trk>\n<extensions><foresignal:manifest");
        assertThat(sha).isEqualTo(sha256(body.substring(0, body.lastIndexOf(GpxExportWriter.MARKER))));
    }

    @Test
    @DisplayName("GPX skips an older current state, non-point geometries, and writes waypoints for other kinds")
    void gpxWaypoints() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GpxExportWriter writer = new GpxExportWriter(out, json);
        writer.start("src", "b", "station");
        writer.current(item("b", 9, 9, "2026-01-01T00:00:00Z", null));
        writer.version(item("b", 1, 1, "2026-01-01T00:01:00Z", null));
        writer.version(new ExportItem(Map.of(), "{\"type\":\"LineString\",\"coordinates\":[[0,0],[1,1]]}",
                Instant.parse("2026-01-01T00:02:00Z"), false, null));
        writer.finish(new LinkedHashMap<>());
        String body = out.toString(StandardCharsets.UTF_8);
        assertThat(body).contains("<wpt lat=\"1.0\" lon=\"1.0\">").doesNotContain("<trk>").doesNotContain("lon=\"9.0\"");
        assertThat(writer.points()).isEqualTo(1);
    }

    @Test
    @DisplayName("GeoJSON with no alerts still closes the alerts array before the manifest")
    void geojsonEmptyAlerts() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GeoJsonExportWriter writer = new GeoJsonExportWriter(out, json);
        writer.start("src", "a", "x");
        writer.current(new ExportItem(new LinkedHashMap<>(Map.of("id", "src:a")), null, null, false, null));
        writer.finish(new LinkedHashMap<>());
        String body = out.toString(StandardCharsets.UTF_8);
        assertThat(json.readTree(body).path("alerts").isArray()).isTrue();
        assertThat(json.readTree(body).path("manifest").path("sha256").asString())
                .isEqualTo(sha256(body.substring(0, body.lastIndexOf(GeoJsonExportWriter.MARKER))));
    }

    @Test
    @DisplayName("export formats parse case-insensitively and default to geojson")
    void formats() {
        assertThat(ExportFormat.parse(null)).isEqualTo(ExportFormat.GEOJSON);
        assertThat(ExportFormat.parse(" GPX ")).isEqualTo(ExportFormat.GPX);
        assertThat(ExportFormat.NDJSON.mediaType()).isEqualTo("application/x-ndjson");
    }
}
