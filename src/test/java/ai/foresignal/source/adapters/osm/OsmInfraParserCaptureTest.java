package ai.foresignal.source.adapters.osm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;

/**
 * The production {@link OsmInfraParser} over the Monaco infrastructure extract made 2026-10-17 from the
 * Geofabrik {@code monaco-261016.osm.pbf} (checkpoint 124;
 * {@code src/test/resources/fixtures/osm-infrastructure/README.md}): RFC 8142 records, counts by kind at
 * the default and a lowered voltage threshold, and one sampled hospital.
 */
class OsmInfraParserCaptureTest {

    static final String FIXTURE = "fixtures/osm-infrastructure/monaco-261016-infra.geojsonseq";
    static final Instant NOW = Instant.parse("2026-10-17T12:00:00Z");

    static OsmInfraParser.Parsed parse(double minVoltageKv) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(DecoderFixtures.bytes(FIXTURE)), StandardCharsets.UTF_8))) {
            return OsmInfraParser.parse(reader, minVoltageKv, NOW, OsmInfrastructureAdapter.MAX_FEATURES);
        }
    }

    static Map<String, Long> byKind(OsmInfraParser.Parsed parsed) {
        return parsed.records().stream().collect(Collectors.groupingBy(NormalizedRecord::kind, TreeMap::new,
                Collectors.counting()));
    }

    @Test
    @DisplayName("at 110 kV: 40 features, no parse errors, 38 records (29 generators, 7 hospitals, 2 substations)")
    void defaultThreshold() throws Exception {
        OsmInfraParser.Parsed parsed = parse(110);
        assertThat(parsed.features()).isEqualTo(40);
        assertThat(parsed.parseErrors()).isZero();
        assertThat(parsed.capExceeded()).isFalse();
        assertThat(byKind(parsed)).containsExactlyEntriesOf(new TreeMap<>(Map.of(
                "hospital", 7L, "power-plant-osm", 29L, "substation", 2L)));
    }

    @Test
    @DisplayName("at 60 kV the two 63 kV RTE cables are kept as power lines")
    void lowerThreshold() throws Exception {
        OsmInfraParser.Parsed parsed = parse(60);
        assertThat(byKind(parsed)).containsEntry("power-line", 2L).hasSize(4);
        NormalizedRecord cable = parsed.records().stream().filter(r -> r.externalId().equals("w1172678029"))
                .findFirst().orElseThrow();
        assertThat(cable.kind()).isEqualTo("power-line");
        assertThat(cable.payload()).containsEntry("voltageKv", 63.0).containsEntry("operator", "RTE")
                .containsEntry("osmType", "way");
        assertThat(cable.geometry().getGeometryType()).isEqualTo("LineString");
    }

    @Test
    @DisplayName("sample: w49209406, Centre hospitalier Princesse Grace, a 20-vertex polygon in Monaco")
    void sampledHospital() throws Exception {
        NormalizedRecord chpg = parse(110).records().stream().filter(r -> r.externalId().equals("w49209406"))
                .findFirst().orElseThrow();
        assertThat(chpg.kind()).isEqualTo("hospital");
        assertThat(chpg.payload()).containsEntry("name", "Centre hospitalier Princesse Grace (CHPG)")
                .containsEntry("osmType", "way").doesNotContainKey("website");
        Geometry geometry = chpg.geometry();
        assertThat(geometry.getGeometryType()).isEqualTo("Polygon");
        assertThat(geometry.getNumPoints()).isEqualTo(20);
        assertThat(geometry.getCentroid().getX()).isCloseTo(7.410, within(0.002));
        assertThat(geometry.getCentroid().getY()).isCloseTo(43.730, within(0.002));
        NormalizedRecord substation = parse(110).records().stream()
                .filter(r -> r.externalId().equals("n6459866604")).findFirst().orElseThrow();
        assertThat(substation.kind()).isEqualTo("substation");
        assertThat(substation.payload()).containsEntry("ref", "P208").containsEntry("osmType", "node");
    }
}
