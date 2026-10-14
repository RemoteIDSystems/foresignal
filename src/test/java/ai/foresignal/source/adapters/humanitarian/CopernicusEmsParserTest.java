package ai.foresignal.source.adapters.humanitarian;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code copernicus-ems} parsing (prompt 090): the WKT centroid and the offset-less timestamps. */
class CopernicusEmsParserTest {

    @Test
    @DisplayName("an activation maps to a Point from the WKT centroid and a non-null event time")
    void activation() {
        List<NormalizedRecord> records = new CopernicusEmsAdapter("")
                .parse(AdapterFixtures.bytes("copernicus-ems", "activations-info.json"));
        assertThat(records).isNotEmpty();
        NormalizedRecord first = records.get(0);
        assertThat(first.externalId()).isEqualTo("EMSR934");
        assertThat(first.kind()).isEqualTo("emergency-activation");
        assertThat(first.geometry().getGeometryType()).isEqualTo("Point");
        assertThat(first.eventTime()).isNotNull();
        assertThat(first.observedAt()).isNotNull();
        assertThat(first.payload()).containsKeys("name", "category", "countries", "nProducts");
    }

    @Test
    @DisplayName("WKT POINT parsing tolerates spaces and rejects junk")
    void wkt() {
        assertThat(CopernicusEmsAdapter.wktPoint("POINT (-80.67 8.00)")).containsExactly(-80.67, 8.00);
        assertThat(CopernicusEmsAdapter.wktPoint("POINT(1 2)")).containsExactly(1.0, 2.0);
        assertThat(CopernicusEmsAdapter.wktPoint("POLYGON ((0 0, 1 0, 1 1, 0 0))")).isNull();
        assertThat(CopernicusEmsAdapter.wktPoint(null)).isNull();
    }
}
