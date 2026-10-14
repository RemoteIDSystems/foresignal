package ai.foresignal.source.adapters.nps;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link NpsBoundaryParser} contract (prompt 094). */
class NpsBoundaryParserTest {

    private final SourceDescriptor descriptor = new NpsBoundariesAdapter("").descriptor();

    @Test
    @DisplayName("parses polygons and multipolygons with the declared keys")
    void parses() {
        List<NormalizedRecord> records =
                NpsBoundaryParser.parse(AdapterFixtures.bytes(NpsBoundariesAdapter.ID, "boundaries.geojson"));
        assertThat(records).extracting(NormalizedRecord::externalId).containsExactly("YOSE", "GRCA");
        FieldSpecContract.assertPayloadKeysDeclared(descriptor, records);

        NormalizedRecord yose = records.get(0);
        assertThat(yose.payload()).containsEntry("unitName", "Yosemite National Park")
                .containsEntry("unitType", "National Park")
                .containsEntry("state", "CA");
        assertThat(yose.geometry().getGeometryType()).isEqualTo("Polygon");
        assertThat(records.get(1).geometry().getGeometryType()).isEqualTo("MultiPolygon");
    }

    @Test
    @DisplayName("a geometry over ten thousand vertices is simplified and flagged")
    void simplifies() {
        byte[] body = largePolygon(12_000).getBytes(StandardCharsets.UTF_8);
        List<NormalizedRecord> records = NpsBoundaryParser.parse(body);
        assertThat(records).hasSize(1);
        NormalizedRecord record = records.get(0);
        assertThat(record.geometry().getNumPoints()).isLessThanOrEqualTo(10_000);
        assertThat(record.quality().extra()).containsEntry("geom.simplified", true);
        FieldSpecContract.assertPayloadKeysDeclared(descriptor, records);
    }

    /** A FeatureCollection with one unit whose polygon ring has {@code vertices} points. */
    private static String largePolygon(int vertices) {
        StringBuilder ring = new StringBuilder();
        for (int i = 0; i < vertices; i++) {
            double angle = 2 * Math.PI * i / vertices;
            double lon = -119.0 + 0.5 * Math.cos(angle);
            double lat = 37.7 + 0.5 * Math.sin(angle);
            if (i > 0) {
                ring.append(',');
            }
            ring.append('[').append(lon).append(',').append(lat).append(']');
        }
        ring.append(",[").append(-119.0 + 0.5).append(',').append(37.7).append(']');
        return "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{"
                + "\"UNIT_CODE\":\"BIG\",\"UNIT_NAME\":\"Big Park\"},\"geometry\":{\"type\":\"Polygon\","
                + "\"coordinates\":[[" + ring + "]]}}]}";
    }
}
