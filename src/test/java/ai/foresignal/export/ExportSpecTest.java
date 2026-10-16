package ai.foresignal.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The export and pack request parsers (prompt 111). */
class ExportSpecTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    private static JsonNode json(String text) {
        return JSON.readTree(text);
    }

    @Test
    @DisplayName("modes and time fields match case-insensitively and are stored in their canonical spelling")
    void canonicalModes() {
        ExportSpec asOf = ExportSpec.parse(json("{\"mode\":\"ASOF\",\"asOf\":\"2026-03-01T00:00:00Z\",\"sources\":[\"s\"],"
                + "\"timeField\":\"Observed\"}"));
        assertThat(asOf.mode()).isEqualTo("asOf");
        assertThat(asOf.timeField()).isEqualTo("observed");
        assertThat(ExportSpec.parse(json("{\"mode\":\"atseq\",\"atSeq\":42}")).mode()).isEqualTo("atSeq");
        assertThat(ExportSpec.parse(json("{\"mode\":\"History\",\"format\":\"GPX\"}")).format())
                .isEqualTo(ExportFormat.GPX);
        ExportSpec defaults = ExportSpec.parse(json("{}"));
        assertThat(defaults.mode()).isEqualTo("current");
        assertThat(defaults.format()).isEqualTo(ExportFormat.GEOPARQUET);
        assertThat(defaults.timeField()).isEqualTo("effective");
    }

    @Test
    @DisplayName("invalid specs are 400 EXPORT_SPEC_INVALID with every field error")
    void invalid() {
        assertThatThrownBy(() -> ExportSpec.parse(json("{\"mode\":\"sometime\",\"format\":\"shp\",\"bbox\":[1,2,3],"
                + "\"from\":\"yesterday\",\"sources\":\"s\"}")))
                .isInstanceOfSatisfying(ValidationException.class, e -> {
                    assertThat(e.getCode()).isEqualTo(ErrorCodes.EXPORT_SPEC_INVALID);
                    assertThat(e.getErrors()).extracting(f -> f.field())
                            .contains("mode", "format", "bbox", "from", "sources");
                });
        assertThatThrownBy(() -> ExportSpec.parse(json("{\"format\":\"gpx\"}"))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ExportSpec.parse(json("{\"mode\":\"asOf\"}"))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ExportSpec.parse(json("{\"mode\":\"atSeq\",\"atSeq\":\"x\"}")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ExportSpec.parse(json("{\"bbox\":[10,0,5,1]}"))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ExportSpec.parse(json("{\"from\":\"2026-03-02T00:00:00Z\",\"to\":\"2026-03-01T00:00:00Z\"}")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ExportSpec.parse(json("[]"))).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("the stored map round-trips through fromMap")
    void mapRoundTrip() {
        ExportSpec spec = ExportSpec.parse(json("{\"name\":\"n\",\"format\":\"geopackage\",\"sources\":[\"a\",\"b\"],"
                + "\"kinds\":[\"k\"],\"bbox\":[-1,-2,3,4],\"from\":\"2026-03-01T00:00:00Z\",\"timeField\":\"ingested\","
                + "\"mode\":\"history\",\"fields\":[\"x\"],\"flatten\":true,\"includePrivate\":true,\"personalUse\":true}"));
        ExportSpec back = ExportSpec.fromMap(spec.toMap());
        assertThat(back.toMap()).isEqualTo(spec.toMap());
        assertThat(back.bbox()).containsExactly(-1, -2, 3, 4);
        assertThat(back.from()).isEqualTo(Instant.parse("2026-03-01T00:00:00Z"));
        assertThat(spec.withBbox(new double[] {0, 0, 1, 1}).bbox()).containsExactly(0, 0, 1, 1);
    }

    @Test
    @DisplayName("formats parse by wire value and name their extension and media type")
    void formats() {
        assertThat(ExportFormat.parse(null)).isEqualTo(ExportFormat.GEOPARQUET);
        assertThat(ExportFormat.GEOPARQUET.extension()).isEqualTo("parquet");
        assertThat(ExportFormat.GEOPACKAGE.extension()).isEqualTo("gpkg");
        assertThat(ExportFormat.FLATGEOBUF.extension()).isEqualTo("flatgeobuf");
        assertThat(ExportFormat.GPX.mediaType()).isEqualTo("application/gpx+xml");
        assertThatThrownBy(() -> ExportFormat.parse("csv")).isInstanceOf(ValidationException.class);
        assertThat(ExportWriterFactory.create(ExportFormat.FLATGEOBUF)).isInstanceOf(FlatGeobufWriter.class);
        assertThat(ExportWriterFactory.create(ExportFormat.GPX)).isInstanceOf(GpxWriter.class);
    }

    @Test
    @DisplayName("pack specs: zoom bounds, a vector or raster part, the stored map round trip")
    void packSpec() {
        PackSpec spec = PackSpec.parse(json("{\"name\":\"p\",\"bbox\":[-98,30,-97,31],\"minZoom\":2,\"maxZoom\":9,"
                + "\"vector\":{\"sources\":[\"s\"],\"asOf\":\"2026-03-01T00:00:00Z\"},\"raster\":{\"productId\":\"mrms\"}}"));
        assertThat(spec.raster().frame()).isEqualTo("latest");
        PackSpec back = PackSpec.fromMap(spec.toMap());
        assertThat(back.toMap()).isEqualTo(spec.toMap());
        assertThat(back.vector().asOf()).isEqualTo(Instant.parse("2026-03-01T00:00:00Z"));
        assertThatThrownBy(() -> PackSpec.parse(json("{\"name\":\"p\",\"minZoom\":3,\"maxZoom\":15,\"vector\":{}}")))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.getErrors()).extracting(f -> f.field()).contains("maxZoom"));
        assertThatThrownBy(() -> PackSpec.parse(json("{\"name\":\"p\",\"minZoom\":5,\"maxZoom\":4}")))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.getErrors()).extracting(f -> f.field()).contains("maxZoom", "vector"));
        assertThatThrownBy(() -> PackSpec.parse(json("{\"minZoom\":1,\"maxZoom\":2,\"raster\":{}}")))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.getErrors()).extracting(f -> f.field()).contains("name", "raster.productId"));
        PackSpec minimal = PackSpec.fromMap(Map.of("name", "m", "minZoom", 0, "maxZoom", 1,
                "vector", Map.of("sources", List.of(), "kinds", List.of())));
        assertThat(minimal.raster()).isNull();
        assertThat(minimal.bbox()).isNull();
    }
}
