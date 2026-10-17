package ai.foresignal.raster.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.place.PlaceService;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.RasterFrameQueries;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.render.LegendService;
import ai.foresignal.raster.render.RasterRenderService;
import ai.foresignal.raster.render.WebMercatorQuad;
import ai.foresignal.raster.render.WorldCrs84Quad;
import ai.foresignal.raster.sample.ForecastSeriesService;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** Export format rules, attribution embedding and place-pack product visibility (prompt 108, post-review fixes). */
class RasterExportRulesTest {

    private static final Instant T = Instant.parse("2026-10-16T12:00:00Z");

    private static ProductSpec spec(String id, String text, String licence) {
        return ProductSpec.builder(id)
                .title(id)
                .category("model")
                .units("K")
                .dtype(RasterDtype.U8)
                .scaleOffset(1.0, 0.0)
                .temporal(ProductSpec.Temporal.observation(Duration.ofMinutes(10), Duration.ofMinutes(30)))
                .spatial(new ProductSpec.Spatial("global", 4, 0))
                .attribution(new ProductSpec.Attribution(text, null, null, licence, "https://example.org/l", false))
                .build();
    }

    @Test
    @DisplayName("MBTiles and PMTiles refuse WorldCRS84Quad (they are XYZ WebMercator archives); COG accepts both")
    void tmsPerFormat() {
        assertThatThrownBy(() -> RasterExportService.requireSupportedTms("mbtiles", WorldCrs84Quad.INSTANCE))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> RasterExportService.requireSupportedTms("pmtiles", WorldCrs84Quad.INSTANCE))
                .isInstanceOf(ValidationException.class);
        RasterExportService.requireSupportedTms("mbtiles", WebMercatorQuad.INSTANCE);
        RasterExportService.requireSupportedTms("cog", WorldCrs84Quad.INSTANCE);
    }

    @Test
    @DisplayName("a COG follows the tile matrix set: WebMercatorQuad writes EPSG:3857, WorldCRS84Quad EPSG:4326")
    void cogCrs() {
        assertThat(RasterExportService.cogEpsg(WebMercatorQuad.INSTANCE)).isEqualTo(3857);
        assertThat(RasterExportService.cogEpsg(WorldCrs84Quad.INSTANCE)).isEqualTo(4326);
    }

    @Test
    @DisplayName("every export carries attribution and license; ECMWF and CC BY add the changes-made note")
    void attribution() {
        ProductSpec ecmwf = spec("ifs", "ECMWF open data", "CC-BY-4.0");
        assertThat(ExportAttribution.changesMade(ecmwf)).isTrue();
        assertThat(ExportAttribution.line(ecmwf)).contains("ECMWF open data").contains("License: CC-BY-4.0")
                .contains("https://example.org/l").endsWith(ExportAttribution.CHANGES_MADE);
        assertThat(ExportAttribution.structured(ecmwf)).containsEntry("licence", "CC-BY-4.0")
                .containsEntry("changesMade", ExportAttribution.CHANGES_MADE);
        ProductSpec noaa = spec("mrms", "NOAA MRMS\nline", "US-PD");
        assertThat(ExportAttribution.changesMade(noaa)).isFalse();
        assertThat(ExportAttribution.line(noaa)).doesNotContain("\n").contains("License: US-PD");
        RasterFrameRow frame = new RasterFrameRow("ifs", "20261016T120000Z", T, null, null, null, null, "g", "p", 1,
                "0".repeat(64), "u8", 1, Map.of(), null, RasterFrameRow.COMPLETE, false, false, T, null);
        String xml = RasterExportService.gdalMetadata(ecmwf, frame);
        assertThat(xml).startsWith("<GDALMetadata>").contains("<Item name=\"LICENSE\">CC-BY-4.0</Item>")
                .contains("<Item name=\"FRAME_KEY\">20261016T120000Z~00000000</Item>").contains("ATTRIBUTION");
    }

    @Test
    @DisplayName("a place pack drops products of excluded (private or restricted) sources and external products")
    void packVisibility() {
        ProductRegistry registry = mock(ProductRegistry.class);
        when(registry.find("radar")).thenReturn(Optional.of(new ProductRegistry.Registered(spec("radar", "R", "US-PD"),
                "open-src", false)));
        when(registry.find("secret")).thenReturn(Optional.of(new ProductRegistry.Registered(
                spec("secret", "S", "US-PD"), "private-src", false)));
        when(registry.find("gone")).thenReturn(Optional.empty());
        PlacePackService packs = new PlacePackService(mock(PlaceService.class), mock(ForecastSeriesService.class),
                mock(RasterRenderService.class), mock(LegendService.class), mock(RasterFrameQueries.class), registry,
                mock(ExportPaths.class), JsonMapper.builder().build(), Clock.systemUTC());
        assertThat(packs.visibleProducts(List.of("radar", "secret", "gone", "radar"), Set.of("private-src")))
                .containsExactly("radar");
        assertThat(packs.visibleProducts(List.of("radar", "secret"), Set.of())).containsExactly("radar", "secret");
    }

    @Test
    @DisplayName("the pack spec stores products only when named and keeps the caller's exclusions sorted")
    void packSpec() {
        PlacePackService.Request none = new PlacePackService.Request("home", List.of(), 12, 150, 8,
                List.of("b", "a"));
        assertThat(none.toMap()).doesNotContainKey("products").containsEntry("excludedSources", List.of("a", "b"));
        PlacePackService.Request some = new PlacePackService.Request("home", List.of("radar"), 12, 150, 8);
        assertThat(some.toMap()).containsEntry("products", List.of("radar"));
    }
}
