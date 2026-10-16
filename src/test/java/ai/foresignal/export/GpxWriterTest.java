package ai.foresignal.export;

import static ai.foresignal.export.ExportRowsFixture.line;
import static ai.foresignal.export.ExportRowsFixture.point;
import static ai.foresignal.export.ExportRowsFixture.row;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The GPX 1.1 writer (prompt 111), validated against TopoGrafix's GPX 1.1 schema
 * ({@code src/test/resources/export/gpx-1.1.xsd}): waypoints before tracks, metadata in schema order,
 * elevation in meters, longitudes in {@code [-180, 180)}, escaping, and records without a point left out.
 */
class GpxWriterTest {

    @TempDir Path dir;

    private static Schema schema() throws Exception {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        try (InputStream xsd = GpxWriterTest.class.getResourceAsStream("/export/gpx-1.1.xsd")) {
            return factory.newSchema(new StreamSource(xsd));
        }
    }

    private Path write(List<Map<String, Object>> rows, Map<String, String> metadata) throws Exception {
        Path file = dir.resolve("out.gpx");
        try (GpxWriter writer = new GpxWriter()) {
            writer.open(file, ExportSchema.standard(List.of(), true), metadata);
            for (Map<String, Object> row : rows) {
                writer.write(row);
            }
        }
        return file;
    }

    private static Map<String, Object> track(String id, long seq, double lon, double lat) {
        Map<String, Object> row = row("trk-src", id, "aircraft", seq, point(lon, lat));
        row.put(ExportWriter.TRACK, true);
        row.put(ExportWriter.ELEVATION, 1234.5);
        return row;
    }

    @Test
    @DisplayName("tracks and waypoints interleaved in the input come out schema-valid, waypoints first")
    void validAgainstXsd() throws Exception {
        Path file = write(List.of(
                        track("a", 1, 10, 50), track("a", 2, 10.1, 50.1),
                        row("pts", "w1", "place", 3, point(180, 45)),
                        track("b", 4, -20, 30),
                        row("pts", "w2", "place", 5, line(1, 2)),
                        row("pts", "w3", "place", 6, null)),
                Map.of("name", "Q&A export", "attribution", "Data © \"Someone\" <x>",
                        "licenseUrl", "https://creativecommons.org/licenses/by/4.0/", "licenseName", "CC BY 4.0"));
        schema().newValidator().validate(new StreamSource(file.toFile()));
        String xml = Files.readString(file);
        assertThat(xml.indexOf("<wpt")).isLessThan(xml.indexOf("<trk>"));
        assertThat(xml.lastIndexOf("<wpt")).isLessThan(xml.indexOf("<trk>"));
        assertThat(xml).contains("<name>Q&amp;A export</name>")
                .contains("author=\"Data © &quot;Someone&quot; &lt;x&gt;\"")
                .contains("<license>https://creativecommons.org/licenses/by/4.0/</license>")
                .contains("<link href=\"https://creativecommons.org/licenses/by/4.0/\"><text>CC BY 4.0</text></link>")
                .contains("<trkpt lat=\"50\" lon=\"10\"><ele>1234.5</ele><time>2026-03-01T12:00:01.123Z</time></trkpt>")
                .contains("<wpt lat=\"45\" lon=\"-180\">")
                .contains("<name>trk-src/a</name>")
                .contains("<bounds ");
        assertThat(xml.split("<trk>", -1)).hasSize(3);
        assertThat(xml).doesNotContain("w2").doesNotContain("w3");
    }

    @Test
    @DisplayName("without metadata or rows the file is still schema-valid")
    void emptyIsValid() throws Exception {
        Path file = write(List.of(), null);
        schema().newValidator().validate(new StreamSource(file.toFile()));
        assertThat(Files.readString(file)).contains("<copyright author=\"Foresignal\">").doesNotContain("<bounds");
        try (var listing = Files.list(dir)) {
            assertThat(listing.map(p -> p.getFileName().toString())).containsExactly("out.gpx");
        }
    }

    @Test
    @DisplayName("skipped counts records without a point; helpers format decimals and escape control characters")
    void helpers() throws Exception {
        GpxWriter writer = new GpxWriter();
        writer.open(dir.resolve("x.gpx"), ExportSchema.standard(List.of(), true), Map.of());
        writer.write(row("s", "a", "k", 1, line(1, 1)));
        writer.write(row("s", "b", "k", 2, point(1, 1)));
        assertThat(writer.skipped()).isEqualTo(1);
        assertThat(writer.rows()).isEqualTo(1);
        writer.close();
        writer.close();
        assertThat(GpxWriter.decimal(1e-7)).isEqualTo("0.0000001");
        assertThat(GpxWriter.decimal(120.0)).isEqualTo("120");
        assertThat(GpxWriter.escape("a\u0001'b")).isEqualTo("a &apos;b");
        assertThat(GpxWriter.latLon(null)).isNull();
        assertThat(GpxWriter.latLon(ExportRowsFixture.wkb(point(190, 10)))).containsExactly(10, -170);
    }
}
