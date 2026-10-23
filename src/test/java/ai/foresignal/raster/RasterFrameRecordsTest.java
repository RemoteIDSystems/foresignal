package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.testsupport.raster.SyntheticRasterAdapter;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** The {@code raster-frame} records (prompt 042, OpenAPI {@code RasterFrameEvent}). */
class RasterFrameRecordsTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant T = Instant.parse("2026-10-10T14:32:40Z");
    static final List<Double> BOUNDS = List.of(-100.0, 28.0, -94.0, 34.0);

    static RasterFrameRow row(Map<String, Object> stats, Instant run, Integer step) {
        return new RasterFrameRow("p", run == null ? FrameKeys.observationFrameId(T) : FrameKeys.forecastFrameId(run, step),
                T, run, step, null, null, "g", "p", 1, "1a2b3c4d" + "0".repeat(56), "u8", 1, stats, null,
                RasterFrameRow.COMPLETE, false, false, T, T.plusSeconds(3600));
    }

    @Test
    @DisplayName("an observation record: externalId = product, observedAt = eventTime = valid time, the documented payload")
    void observation() {
        ProductSpec spec = SyntheticRasterAdapter.observationSpec();
        NormalizedRecord record = RasterFrameRecords.observation(spec, row(Map.of("coveragePct", 50.0), null, null),
                BOUNDS, "", JSON);
        assertThat(record.externalId()).isEqualTo(spec.id());
        assertThat(record.kind()).isEqualTo("raster-frame");
        assertThat(record.observedAt()).isEqualTo(T);
        assertThat(record.eventTime()).isEqualTo(T);
        assertThat(record.geometry().getEnvelopeInternal().getMinX()).isEqualTo(-100.0);
        assertThat(record.payload()).containsKeys("productId", "frameKey", "time", "timeKey", "tiles", "bounds", "stats",
                "expiresAt");
        assertThat(record.payload().get("frameKey")).isEqualTo("20261010T143240Z~1a2b3c4d");
        assertThat((String) record.payload().get("tiles")).startsWith("/v1/products/" + spec.id() + "/tiles/");
    }

    @Test
    @DisplayName("statistics that would push the payload over 1 KB lose their code counts, then go")
    void payloadBudget() {
        Map<String, Object> counts = new LinkedHashMap<>();
        for (int i = 0; i < 120; i++) {
            counts.put(Integer.toString(i), 123456L);
        }
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("coveragePct", 50.0);
        stats.put("codeCounts", counts);
        NormalizedRecord record = RasterFrameRecords.observation(SyntheticRasterAdapter.observationSpec(),
                row(stats, null, null), BOUNDS, "", JSON);
        assertThat(RasterFrameRecords.size(new LinkedHashMap<>(record.payload()), JSON))
                .isLessThanOrEqualTo(RasterFrameRecords.MAX_PAYLOAD_BYTES);
        @SuppressWarnings("unchecked")
        Map<String, Object> kept = (Map<String, Object>) record.payload().get("stats");
        assertThat(kept).containsKey("coveragePct").doesNotContainKey("codeCounts");
    }

    @Test
    @DisplayName("a forecast run record and the run-in-progress record and its tombstone")
    void forecast() {
        ProductSpec spec = SyntheticRasterAdapter.forecastSpec();
        Instant run = Instant.parse("2026-10-10T12:00:00Z");
        NormalizedRecord full = RasterFrameRecords.forecastRun(spec, run, List.of(row(null, run, 0), row(null, run, 1)),
                BOUNDS, "https://fs.example");
        assertThat(full.externalId()).isEqualTo(spec.id());
        assertThat(full.observedAt()).isEqualTo(run);
        assertThat(full.payload().get("steps")).isEqualTo(List.of(0, 1));
        assertThat((String) full.payload().get("tilesTemplate"))
                .isEqualTo("https://fs.example/v1/products/" + spec.id() + "/tiles/{timeKey}/{z}/{x}/{y}.png"
                        + "?style=wind-speed@1&run=2026-10-10T12:00:00Z");
        NormalizedRecord partial = RasterFrameRecords.partialRun(spec, run, List.of(row(null, run, 0)), BOUNDS, "");
        assertThat(partial.externalId()).isEqualTo(spec.id() + ":partial");
        assertThat(partial.payload()).containsEntry("partial", true);
        NormalizedRecord tombstone = RasterFrameRecords.partialTombstone(spec, run);
        assertThat(tombstone.tombstone()).isTrue();
        assertThat(tombstone.externalId()).isEqualTo(spec.id() + ":partial");
    }

    @Test
    @DisplayName("a world box is densified: same envelope, no edge longer than 10 degrees")
    void worldBoxDensified() {
        org.locationtech.jts.geom.Polygon box = RasterFrameRecords.box(List.of(-180.0, -90.0, 180.0, 90.0));
        org.locationtech.jts.geom.Envelope env = box.getEnvelopeInternal();
        assertThat(List.of(env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY()))
                .containsExactly(-180.0, -90.0, 180.0, 90.0);
        org.locationtech.jts.geom.Coordinate[] ring = box.getExteriorRing().getCoordinates();
        for (int i = 1; i < ring.length; i++) {
            assertThat(ring[i].distance(ring[i - 1])).isLessThanOrEqualTo(RasterFrameRecords.MAX_EDGE_DEGREES + 1e-9);
        }
    }
}
