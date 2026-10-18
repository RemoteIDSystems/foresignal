package ai.foresignal.doctor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link StructureExtractor} (prompt 140, task 2): JSON path trees with nested arrays and presence ratios, CSV/TSV
 * column inference, XML with XXE attempts refused, binary sniffing and HTML detection.
 */
class StructureExtractorTest {

    final StructureExtractor extractor = new StructureExtractor();

    @Test
    @DisplayName("JSON: nested arrays get [] paths, presence is counted per parent instance, examples are capped")
    void jsonNestedArrays() {
        String body = """
                {"type":"FeatureCollection","metadata":{"count":3},
                 "features":[
                   {"id":"a","properties":{"mag":2.5,"place":"10 km N of Somewhere","tags":["x","y"]}},
                   {"id":"b","properties":{"mag":4,"tags":[]}},
                   {"id":"c","properties":{"mag":null,"place":"%s"}}]}
                """.formatted("L".repeat(200));
        Structure s = extractor.extract(body, "application/json");

        assertThat(s.format()).isEqualTo(StructureFormat.JSON);
        assertThat(s.paths()).containsKeys("type", "metadata.count", "features", "features[]", "features[].id",
                "features[].properties.mag", "features[].properties.place", "features[].properties.tags[]");
        assertThat(s.paths().get("features[].id").presence()).isEqualTo(1.0);
        assertThat(s.paths().get("features[].properties.place").presence()).isEqualTo(2 / 3.0, within());
        assertThat(s.paths().get("features[].properties.tags").presence()).isEqualTo(2 / 3.0, within());
        assertThat(s.paths().get("features[].properties.mag").types()).containsExactly("null", "number");
        assertThat(s.paths().get("features[].properties.mag").nonNullTypes()).containsExactly("number");
        assertThat(s.paths().get("features[].properties.mag").min()).isEqualTo(2.5);
        assertThat(s.paths().get("features[].properties.mag").max()).isEqualTo(4.0);
        assertThat(s.paths().get("features[].properties.place").examples())
                .allSatisfy(e -> assertThat(e.length()).isLessThanOrEqualTo(StructureExtractor.MAX_EXAMPLE_LENGTH));
        assertThat(s.arrays().get("features").maxLength()).isEqualTo(3);
        assertThat(s.arrays().get("features[].properties.tags").minLength()).isZero();
        assertThat(s.arrays().get("features[].properties.tags").instances()).isEqualTo(2);
    }

    @Test
    @DisplayName("JSON: a top-level array, at most 3 examples per path, numbers kept as written")
    void jsonRootArray() {
        Structure s = extractor.extract("[{\"v\":1},{\"v\":2},{\"v\":3},{\"v\":4}]", null);

        assertThat(s.paths()).containsKeys("[]", "[].v");
        assertThat(s.paths().get("[].v").examples()).containsExactly("1", "2", "3");
        assertThat(s.arrays().get("[]").maxLength()).isEqualTo(4);
    }

    @Test
    @DisplayName("JSON: a syntax error is reported, not thrown")
    void jsonBroken() {
        Structure s = extractor.extract("{\"a\": [1, 2", "application/json");

        assertThat(s.format()).isEqualTo(StructureFormat.JSON);
        assertThat(s.error()).startsWith("not valid JSON");
        assertThat(s.paths()).isEmpty();
    }

    @Test
    @DisplayName("CSV: header, delimiter, per-column type and null ratio")
    void csvInference() {
        String body = "time,latitude,mag,place,reviewed\n"
                + "2026-10-11T22:00:00Z,35.1,2.5,Somewhere,true\n"
                + "2026-10-11T22:05:00Z,36.2,,\"Elsewhere, CA\",false\n"
                + "2026-10-11T22:10:00Z,37,3,Nowhere,true\n";
        Structure s = extractor.extract(body, "text/csv");

        assertThat(s.format()).isEqualTo(StructureFormat.CSV);
        assertThat(s.header()).isTrue();
        assertThat(s.delimiter()).isEqualTo(",");
        assertThat(s.columns()).containsExactly("time", "latitude", "mag", "place", "reviewed");
        assertThat(s.paths().get("time").types()).containsExactly("date");
        assertThat(s.paths().get("latitude").types()).containsExactly("number");
        assertThat(s.paths().get("mag").nullRatio()).isEqualTo(1 / 3.0, within());
        assertThat(s.paths().get("place").examples()).contains("Elsewhere, CA");
        assertThat(s.paths().get("reviewed").types()).containsExactly("boolean");
        assertThat(s.arrays().get("rows").maxLength()).isEqualTo(3);
    }

    @Test
    @DisplayName("TSV without a header: tab delimiter and colN names")
    void tsvWithoutHeader() {
        Structure s = extractor.extract("1\t2.5\tx\n2\t3.5\ty\n", null);

        assertThat(s.format()).isEqualTo(StructureFormat.TSV);
        assertThat(s.header()).isFalse();
        assertThat(s.columns()).containsExactly("col1", "col2", "col3");
        assertThat(s.paths().get("col1").types()).containsExactly("integer");
    }

    @Test
    @DisplayName("XML: element and attribute paths with presence")
    void xml() {
        String body = "<?xml version=\"1.0\"?><feed><entry id=\"1\"><title>A</title><mag>2.5</mag></entry>"
                + "<entry id=\"2\"><title>B</title></entry></feed>";
        Structure s = extractor.extract(body, "application/xml");

        assertThat(s.format()).isEqualTo(StructureFormat.XML);
        assertThat(s.error()).isNull();
        assertThat(s.paths()).containsKeys("feed", "feed.entry", "feed.entry@id", "feed.entry.title", "feed.entry.mag");
        assertThat(s.paths().get("feed.entry.mag").presence()).isEqualTo(0.5, within());
        assertThat(s.paths().get("feed.entry.mag").types()).contains("element", "number");
        assertThat(s.paths().get("feed.entry@id").types()).contains("attribute", "integer");
    }

    @Test
    @DisplayName("XML: an XXE attempt is refused (DOCTYPE disallowed) and nothing is resolved")
    void xxeRejected() {
        String body = "<?xml version=\"1.0\"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>"
                + "<foo>&xxe;</foo>";
        Structure s = extractor.extract(body, "application/xml");

        assertThat(s.format()).isEqualTo(StructureFormat.XML);
        assertThat(s.error()).contains("DTDs and external entities are refused");
        assertThat(s.paths()).isEmpty();
        assertThat(s.toString()).doesNotContain("root:");
    }

    @Test
    @DisplayName("XML: a billion-laughs style internal entity is refused too")
    void internalEntityRejected() {
        String body = "<?xml version=\"1.0\"?><!DOCTYPE lol [<!ENTITY lol \"lol\"><!ENTITY lol2 \"&lol;&lol;\">]>"
                + "<lolz>&lol2;</lolz>";
        assertThat(extractor.extract(body, null).error()).isNotNull();
    }

    @Test
    @DisplayName("binary: magic bytes for GRIB2, PNG, NetCDF and gzip; ZIP entry names")
    void binarySniffing() throws IOException {
        assertThat(extractor.extract("GRIB\u0000\u0000\u0000\u0002".getBytes(StandardCharsets.ISO_8859_1), null)
                .binaryKind()).isEqualTo("grib2");
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10};
        Structure p = extractor.extract(png, "image/png");
        assertThat(p.format()).isEqualTo(StructureFormat.BINARY);
        assertThat(p.binaryKind()).isEqualTo("png");
        assertThat(p.magic()).isEqualTo("89504e470d0a1a0a");
        assertThat(extractor.extract(new byte[] {'C', 'D', 'F', 1, 0, 0}, null).binaryKind()).isEqualTo("netcdf");
        assertThat(extractor.extract(new byte[] {0x1f, (byte) 0x8b, 8, 0}, null).binaryKind()).isEqualTo("gzip");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry("data/a.csv"));
            zip.write("x,y\n1,2\n".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("README.txt"));
            zip.closeEntry();
        }
        Structure z = extractor.extract(bytes.toByteArray(), "application/zip");
        assertThat(z.binaryKind()).isEqualTo("zip");
        assertThat(z.zipEntries()).containsExactly("data/a.csv", "README.txt");
        assertThat(z.bytes()).isEqualTo(bytes.size());
    }

    @Test
    @DisplayName("HTML: detected by doctype or content type, title captured and collapsed")
    void htmlDetection() {
        Structure s = extractor.extract("<!DOCTYPE html><html><head><title>\n  Scheduled   Maintenance </title></head>"
                + "<body>Back soon</body></html>", "text/html; charset=utf-8");

        assertThat(s.format()).isEqualTo(StructureFormat.HTML);
        assertThat(s.htmlTitle()).isEqualTo("Scheduled Maintenance");
        assertThat(s.mediaType()).isEqualTo("text/html");
        assertThat(extractor.extract("<html><body>no title</body></html>", null).format())
                .isEqualTo(StructureFormat.HTML);
        assertThat(extractor.extract("<html><body>no title</body></html>", null).htmlTitle()).isNull();
    }

    @Test
    @DisplayName("plain text without a delimiter is TEXT; an empty body is TEXT with zero bytes")
    void text() {
        assertThat(extractor.extract("just a sentence\nand another", "text/plain").format())
                .isEqualTo(StructureFormat.TEXT);
        Structure empty = extractor.extract(new byte[0], null);
        assertThat(empty.format()).isEqualTo(StructureFormat.TEXT);
        assertThat(empty.bytes()).isZero();
    }

    private static org.assertj.core.data.Offset<Double> within() {
        return org.assertj.core.data.Offset.offset(1e-9);
    }
}
