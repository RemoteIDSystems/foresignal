package ai.foresignal.source.adapters.hydro.usdm;

import static ai.foresignal.source.adapters.hydro.HydroTestSupport.fixture;
import static ai.foresignal.source.adapters.hydro.HydroTestSupport.node;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.adapters.env.EnvTestSupport;
import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link UsdmMapper} on the D3 and D4 features of the 2026-10-06 U.S. Drought Monitor map (released Thursday
 * 2026-10-08): the map-date computation from {@code Last-Modified}, the simplification flags (D3 has 20,458 vertices,
 * D4 598), tombstones for missing categories, and the descriptor of {@link UsdmAdapter} with its attribution.
 */
class UsdmMapperTest {

    static final LocalDate MAP_DATE = LocalDate.parse("2026-10-06");

    static List<NormalizedRecord> records() {
        return UsdmMapper.map(fixture("usdm", "usdm_current-d3d4.json"), MAP_DATE);
    }

    @Test
    @DisplayName("the map date is the latest Tuesday on or before Last-Modified")
    void mapDate() {
        Instant lastModified = UsdmMapper.httpDate("Thu, 08 Oct 2026 10:16:12 GMT");
        assertThat(lastModified).isEqualTo(Instant.parse("2026-10-08T10:16:12Z"));
        assertThat(UsdmMapper.mapDate(lastModified)).isEqualTo(MAP_DATE);
        assertThat(UsdmMapper.mapDate(Instant.parse("2026-10-06T23:59:00Z"))).isEqualTo(MAP_DATE);
        assertThat(UsdmMapper.mapDate(Instant.parse("2026-10-05T12:00:00Z"))).isEqualTo(LocalDate.parse("2026-09-29"));
        assertThat(UsdmMapper.httpDate("yesterday")).isNull();
        assertThat(UsdmMapper.httpDate(null)).isNull();
    }

    @Test
    @DisplayName("D3 is simplified below 9,500 vertices and flagged; D4 is kept whole")
    void simplification() {
        List<NormalizedRecord> records = records();
        assertThat(records).extracting(NormalizedRecord::externalId).containsExactly("D3", "D4");
        NormalizedRecord d3 = EnvTestSupport.byId(records, "D3");
        assertThat(d3.geometry().getNumPoints()).isLessThanOrEqualTo(FeatureGeometry.MAX_VERTICES);
        assertThat(d3.geometry().isValid()).isTrue();
        assertThat(d3.observedAt()).isEqualTo(Instant.parse("2026-10-06T00:00:00Z")).isEqualTo(d3.eventTime());
        assertThat(d3.payload()).containsEntry("category", "D3").containsEntry("label", "Extreme Drought")
                .containsEntry("mapDate", "2026-10-06").containsEntry("geometrySimplified", true)
                .containsEntry("simplifyToleranceDeg", 0.0005);
        NormalizedRecord d4 = EnvTestSupport.byId(records, "D4");
        assertThat(d4.geometry().getNumPoints()).isEqualTo(598);
        assertThat(d4.payload()).containsEntry("label", "Exceptional Drought")
                .containsEntry("geometrySimplified", false).doesNotContainKey("simplifyToleranceDeg");
        FieldSpecContract.assertPayloadKeysDeclared(new UsdmAdapter("").descriptor(), records);
    }

    @Test
    @DisplayName("every category missing from the map is tombstoned at the map date")
    void tombstones() {
        Instant at = Instant.parse("2026-10-06T00:00:00Z");
        List<NormalizedRecord> gone = UsdmMapper.tombstones(UsdmMapper.CATEGORIES, List.of("D0", "D1", "D2", "D3"), at);
        assertThat(gone).singleElement().satisfies(t -> {
            assertThat(t.externalId()).isEqualTo("D4");
            assertThat(t.tombstone()).isTrue();
            assertThat(t.kind()).isEqualTo("drought-area");
            assertThat(t.observedAt()).isEqualTo(at);
        });
        assertThat(UsdmMapper.tombstones(UsdmMapper.CATEGORIES, UsdmMapper.CATEGORIES, at)).isEmpty();
    }

    @Test
    @DisplayName("bad categories are skipped; no features array or no usable category is a format error")
    void formatErrors() {
        assertThatThrownBy(() -> UsdmMapper.map(node("{\"type\": \"FeatureCollection\"}"), MAP_DATE))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> UsdmMapper.map(node("""
                {"features": [{"properties": {"DM": 7}, "geometry": null}]}"""), MAP_DATE))
                .isInstanceOf(UpstreamFormatException.class);
        assertThat(UsdmMapper.map(node("{\"features\": []}"), MAP_DATE)).isEmpty();
    }

    @Test
    @DisplayName("descriptor: no TTL, the attribution, a 48 MB cap; the dated URL; snapshot contract across a week")
    void descriptorAndSnapshot() {
        UsdmAdapter adapter = new UsdmAdapter("");
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(adapter.descriptor().recordTtl()).isNull();
        assertThat(adapter.descriptor().attribution()).contains("National Drought Mitigation Center")
                .contains("Map courtesy of NDMC.");
        assertThat(adapter.descriptor().description()).contains("simplified");
        assertThat(adapter.descriptor().maxResponseBytes()).isEqualTo(UsdmAdapter.MAX_RESPONSE_BYTES);
        assertThat(adapter.datedUri(MAP_DATE))
                .hasToString("https://droughtmonitor.unl.edu/data/json/usdm_20261006.json");
        NormalizedRecord d4 = EnvTestSupport.byId(records(), "D4");
        SnapshotContract.assertStableStaticHash(adapter.descriptor(),
                List.of(d4, EnvTestSupport.later(d4, Duration.ofDays(7))));
    }
}
