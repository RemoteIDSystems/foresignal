package ai.foresignal.source.adapters.stac;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.IngestPolicyValidator;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PlacePoints;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code stac-sentinel2}, {@code stac-landsat} and {@code stac-sentinel1} descriptors (prompt 096). */
class StacDescriptorsTest {

    private static final Supplier<PlacePoints> NO_PLACES = () -> null;

    private static List<SourceAdapter> adapters() {
        return new EarthSearchFamily("", NO_PLACES).adapters();
    }

    private static SourceAdapter adapter(String id) {
        return adapters().stream().filter(a -> a.id().equals(id)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("the family registers the three reviewed feeds and validates the list")
    void registers() {
        assertThat(StacSources.problems(StacSources.REVIEWED)).isEmpty();
        assertThat(adapters()).extracting(SourceAdapter::id)
                .containsExactly("stac-sentinel2", "stac-landsat", "stac-sentinel1");
    }

    @Test
    @DisplayName("Sentinel-1 is off by default; Sentinel-2 and Landsat are on")
    void defaultEnablement() {
        assertThat(StacSources.reviewed("stac-sentinel1").enabledByDefault()).isFalse();
        assertThat(StacSources.reviewed("stac-sentinel2").enabledByDefault()).isTrue();
        assertThat(StacSources.reviewed("stac-landsat").enabledByDefault()).isTrue();
    }

    @Test
    @DisplayName("every adapter validates, its default policy passes and the AOI is required")
    void descriptors() {
        for (SourceAdapter adapter : adapters()) {
            SourceDescriptor d = adapter.descriptor();
            assertThat(DescriptorValidator.problems(d, adapter)).as(d.id()).isEmpty();
            assertThat(IngestPolicyValidator.problems(d.defaultIngestPolicy(), d, "defaultIngestPolicy"))
                    .as(d.id()).isEmpty();
            assertThat(d.param(EarthSearchAdapter.AOI_PARAM).required()).as(d.id()).isTrue();
            assertThat(adapter.checkParams(ValidatedParams.empty()))
                    .as(d.id())
                    .containsKey(EarthSearchAdapter.AOI_PARAM);
            assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.UPSTREAM);
            assertThat(d.recordTtl()).isNull();
            assertThat(d.zeroResultsNormal()).isTrue();
            assertThat(d.defaultRetention()).isEqualTo(Duration.ofDays(90));
            assertThat(d.minInterval()).isEqualTo(Duration.ofMinutes(15));
            assertThat(d.defaultInterval()).isEqualTo(Duration.ofHours(1));
            assertThat(d.defaultBudget()).isEqualTo(Budget.of(4, 300));
            assertThat(d.maxResponseBytes()).isEqualTo(8 * 1024 * 1024);
            assertThat(d.kinds()).containsExactly("satellite-scene");
            assertThat(d.searchFields()).containsExactly("platform", "mgrsTile", "gridCode");
            assertThat(d.tileProperties()).containsExactly("platform", "cloudCover", "aoiCoverPct", "eventTime");
            assertThat(d.fieldSpecs().get("cloudCover").unit()).isEqualTo("%");
            assertThat(d.fieldSpecs().get("sunElevation").unit()).isEqualTo("deg");
        }
    }

    @Test
    @DisplayName("licences are per collection")
    void licences() {
        assertThat(adapter("stac-sentinel2").descriptor().licence())
                .isEqualTo("Copernicus Sentinel data terms (free, full and open)");
        assertThat(adapter("stac-sentinel1").descriptor().licence())
                .isEqualTo("Copernicus Sentinel data terms (free, full and open)");
        assertThat(adapter("stac-landsat").descriptor().licence()).isEqualTo("US-PD (USGS Landsat)");
        assertThat(adapter("stac-landsat").descriptor().attribution())
                .contains("Landsat imagery courtesy of the U.S. Geological Survey");
        assertThat(adapter("stac-sentinel2").descriptor().attribution())
                .contains("Contains modified Copernicus Sentinel data");
    }

    @Test
    @DisplayName("a bad AOI is refused by checkParams")
    void badAoi() {
        Map<String, String> problems =
                adapter("stac-sentinel2").checkParams(ValidatedParams.of(Map.of(EarthSearchAdapter.AOI_PARAM, "nope")));
        assertThat(problems).containsKey(EarthSearchAdapter.AOI_PARAM);
        assertThat(adapter("stac-sentinel2").checkParams(
                ValidatedParams.of(Map.of(EarthSearchAdapter.AOI_PARAM, "30,-98,31,-97")))).isEmpty();
    }
}
