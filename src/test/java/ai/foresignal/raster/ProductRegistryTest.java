package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.adapters.FakePollingAdapter;
import ai.foresignal.testsupport.raster.SyntheticRasterAdapter;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The product catalog's validation and dynamic registration (prompt 042), over a real
 * {@link SourceRegistry} of fake adapters and a mocked repository.
 */
class ProductRegistryTest {

    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-10T00:00:00Z"), ZoneOffset.UTC);

    final List<Object> events = new ArrayList<>();
    RasterProductRepository repository;

    @BeforeEach
    void setUp() {
        repository = mock(RasterProductRepository.class);
        when(repository.findById(any())).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    static SourceAdapter source(String id, boolean rasterKind, ProductSpec... products) {
        SourceDescriptor.Builder builder = FakePollingAdapter.defaultDescriptor(id);
        if (rasterKind) {
            builder.kind(ProductSpec.RECORD_KIND);
        }
        for (ProductSpec product : products) {
            builder.product(product);
        }
        return new FakePollingAdapter(builder.build());
    }

    ProductRegistry registry(SourceAdapter... adapters) {
        SourceRegistry sources = new SourceRegistry(List.of(adapters), List.of(), events::add);
        return new ProductRegistry(sources, repository, events::add, CLOCK);
    }

    static ProductSpec spec(String id) {
        return ProductSpec.builder(id)
                .category("radar")
                .dtype(ai.foresignal.source.spi.RasterDtype.U8)
                .temporal(ProductSpec.Temporal.observation(Duration.ofMinutes(2), Duration.ofMinutes(6)))
                .spatial(new ProductSpec.Spatial("CONUS", 8, 0))
                .attribution(new ProductSpec.Attribution("Test", null, null, "US-PD", null, false))
                .build();
    }

    @Test
    @DisplayName("valid specs from every descriptor join the catalog and are mirrored on sync")
    void registersDeclaredProducts() {
        ProductRegistry registry = registry(new SyntheticRasterAdapter());
        assertThat(registry.all()).extracting(ProductRegistry.Registered::id)
                .containsExactly(SyntheticRasterAdapter.FCST, SyntheticRasterAdapter.OBS);
        assertThat(registry.forSource(SyntheticRasterAdapter.SOURCE)).hasSize(2);
        assertThat(registry.hasProducts(SyntheticRasterAdapter.SOURCE)).isTrue();
        assertThat(registry.require(SyntheticRasterAdapter.OBS).dynamic()).isFalse();
        registry.sync();
        verify(repository, org.mockito.Mockito.times(2)).save(any());
    }

    @Test
    @DisplayName("an unknown product is 404 UNKNOWN_PRODUCT")
    void unknownProduct() {
        ProductRegistry registry = registry(new SyntheticRasterAdapter());
        assertThatThrownBy(() -> registry.require("nope")).isInstanceOf(ForesignalException.class)
                .extracting(e -> ((ForesignalException) e).getCode()).isEqualTo("UNKNOWN_PRODUCT");
    }

    @Test
    @DisplayName("a product id declared by two sources stops startup")
    void duplicateId() {
        assertThatThrownBy(() -> registry(source("src-a", true, spec("dup-product")),
                source("src-b", true, spec("dup-product"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate product id 'dup-product'");
    }

    @Test
    @DisplayName("a source that does not list raster-frame cannot own products")
    void missingRasterFrameKind() {
        assertThatThrownBy(() -> registry(source("src-c", false, spec("orphan-product"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must list 'raster-frame'");
    }

    @Test
    @DisplayName("ladder rungs must ascend, keepEvery must not shrink, and maxAge must lie beyond them")
    void badLadder() {
        ProductSpec descending = ProductSpec.builder("bad-ladder").category("radar")
                .dtype(ai.foresignal.source.spi.RasterDtype.U8)
                .temporal(ProductSpec.Temporal.observation(Duration.ofMinutes(2), null))
                .spatial(new ProductSpec.Spatial("CONUS", 8, 0))
                .attribution(new ProductSpec.Attribution("T", null, null, "US-PD", null, false))
                .retention(new ProductSpec.RetentionLadder(List.of(
                        new ProductSpec.RetentionLadder.Rung(Duration.ofHours(24), Duration.ofMinutes(30)),
                        new ProductSpec.RetentionLadder.Rung(Duration.ofHours(6), Duration.ofMinutes(10))),
                        Duration.ofHours(12), null))
                .build();
        List<String> problems = ProductRegistry.problems(descending, "src", SyntheticRasterAdapter.buildDescriptor());
        assertThat(problems).anyMatch(p -> p.contains("ascend by olderThan"))
                .anyMatch(p -> p.contains("must not shrink keepEvery"))
                .anyMatch(p -> p.contains("maxAge must lie beyond"));
    }

    @Test
    @DisplayName("maxGap below the cadence, a bad id, a reserved id and missing facts are each reported")
    void otherRules() {
        SourceDescriptor owner = SyntheticRasterAdapter.buildDescriptor();
        ProductSpec gap = ProductSpec.builder("gap-product").category("radar")
                .dtype(ai.foresignal.source.spi.RasterDtype.U8)
                .temporal(ProductSpec.Temporal.observation(Duration.ofMinutes(5), Duration.ofMinutes(2)))
                .spatial(new ProductSpec.Spatial("CONUS", 8, 0))
                .attribution(new ProductSpec.Attribution("T", null, null, "US-PD", null, false)).build();
        assertThat(ProductRegistry.problems(gap, "src", owner)).anyMatch(p -> p.contains("maxGap must be at least"));
        assertThat(ProductRegistry.problems(spec("Bad_Id"), "src", owner)).anyMatch(p -> p.contains("must match"));
        assertThat(ProductRegistry.problems(spec("timeline"), "src", owner)).anyMatch(p -> p.contains("reserved"));
        // 057: /v1/products/loop is the loop endpoint, so "loop" is reserved too (Weather-Imagery-Design §6.3).
        assertThat(ProductRegistry.problems(spec("loop"), "src", owner)).anyMatch(p -> p.contains("reserved"));
        assertThat(ProductRegistry.problems(new ProductSpec("bare", "Bare", "raster-frame"), "src", owner))
                .anyMatch(p -> p.contains("kind must be one of"))
                .anyMatch(p -> p.contains("category"))
                .anyMatch(p -> p.contains("dtype"))
                .anyMatch(p -> p.contains("temporal facts"))
                .anyMatch(p -> p.contains("spatial facts"))
                .anyMatch(p -> p.contains("attribution"));
        assertThat(ProductRegistry.problems(spec("fine-product"), "src", owner)).isEmpty();
        assertThat(ProductRegistry.problems(spec("fine-product"), "src", null)).anyMatch(p -> p.contains("not registered"));
    }

    @Test
    @DisplayName("dynamic registration publishes REGISTERED then UPDATED, refuses another owner, and can be removed")
    void dynamicRegistration() {
        ProductRegistry registry = registry(new SyntheticRasterAdapter());
        events.clear();
        ProductRegistry.Registered added = registry.registerDynamic(SyntheticRasterAdapter.SOURCE,
                spec("satdump-home-goes-19-fd-c13"));
        assertThat(added.dynamic()).isTrue();
        assertThat(events).containsExactly(new ProductCatalogChangedEvent("satdump-home-goes-19-fd-c13",
                SyntheticRasterAdapter.SOURCE, ProductCatalogChangedEvent.Change.REGISTERED));
        registry.registerDynamic(SyntheticRasterAdapter.SOURCE, spec("satdump-home-goes-19-fd-c13"));
        assertThat(events).last().isEqualTo(new ProductCatalogChangedEvent("satdump-home-goes-19-fd-c13",
                SyntheticRasterAdapter.SOURCE, ProductCatalogChangedEvent.Change.UPDATED));
        assertThatThrownBy(() -> registry.registerDynamic(SyntheticRasterAdapter.SOURCE, spec(SyntheticRasterAdapter.OBS)))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> registry.registerDynamic(SyntheticRasterAdapter.SOURCE, spec("Bad_Id")))
                .isInstanceOf(ValidationException.class);
        assertThat(registry.removeDynamic("satdump-home-goes-19-fd-c13")).isTrue();
        assertThat(events).last().extracting(e -> ((ProductCatalogChangedEvent) e).change())
                .isEqualTo(ProductCatalogChangedEvent.Change.REMOVED);
        assertThat(registry.removeDynamic(SyntheticRasterAdapter.OBS)).isFalse();
        assertThat(registry.find("satdump-home-goes-19-fd-c13")).isEmpty();
    }

    @Test
    @DisplayName("a source with products defaults to zeroResultsNormal, volatile expiresAt and raster-frame 7d")
    void productSourceDefaults() {
        SourceDescriptor descriptor = SyntheticRasterAdapter.buildDescriptor();
        assertThat(descriptor.zeroResultsNormal()).isTrue();
        assertThat(descriptor.volatileFields()).contains("expiresAt");
        assertThat(descriptor.kindRetention()).containsEntry(ProductSpec.RECORD_KIND, Duration.ofDays(7));
        SourceDescriptor explicit = FakePollingAdapter.defaultDescriptor("explicit-src").kind(ProductSpec.RECORD_KIND)
                .product(spec("explicit-product")).zeroResultsNormal(false).build();
        assertThat(explicit.zeroResultsNormal()).isFalse();
        assertThat(FakePollingAdapter.defaultDescriptor("plain-src").build().zeroResultsNormal()).isFalse();
    }

    @Test
    @DisplayName("the ladder summary reads as the design's tables")
    void ladderSummary() {
        ProductSpec.RetentionLadder mrms = new ProductSpec.RetentionLadder(
                List.of(new ProductSpec.RetentionLadder.Rung(Duration.ofHours(6), Duration.ofMinutes(10))),
                Duration.ofHours(48), null);
        assertThat(mrms.summary()).isEqualTo("every frame for 6h, then 10m frames to 48h");
        assertThat(ProductSpec.RetentionLadder.runs(2).summary()).isEqualTo("newest 2 complete runs");
        assertThat(ProductSpec.RetentionLadder.maxAge(Duration.ofHours(72)).summary()).isEqualTo("every frame for 72h");
    }
}
