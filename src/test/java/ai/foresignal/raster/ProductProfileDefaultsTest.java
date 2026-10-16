package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.ValidatedParams;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Per-product enablement and the profile defaults (prompt 106). */
class ProductProfileDefaultsTest {

    private static final List<String> ALL = List.of("a", "b", "c");

    @Test
    @DisplayName("the small profile takes the light defaults")
    void smallDefaults() {
        assertThat(ProductEnablement.enabled(null, "small", ALL, List.of("a"), List.of("a", "b", "c")))
                .containsExactly("a");
    }

    @Test
    @DisplayName("the large profile takes the large defaults")
    void largeDefaults() {
        assertThat(ProductEnablement.enabled(null, "large", ALL, List.of("a"), List.of("a", "b", "c")))
                .containsExactly("a", "b", "c");
    }

    @Test
    @DisplayName("an explicit products list wins, minus unknown ids")
    void explicitWins() {
        ValidatedParams params = ValidatedParams.of(Map.of("products", List.of("b", "zzz")));
        assertThat(ProductEnablement.enabled(params, "small", ALL, List.of("a"), List.of("a", "b", "c")))
                .containsExactly("b");
    }

    @Test
    @DisplayName("AIFS is off on the small profile and on by default on the large one")
    void aifsProfile() {
        Set<String> small = ProductEnablement.enabled(null, "small",
                ai.foresignal.source.adapters.ecmwf.AifsAdapter.PRODUCTS, List.of(),
                ai.foresignal.source.adapters.ecmwf.AifsAdapter.PRODUCTS);
        assertThat(small).isEmpty();
        Set<String> large = ProductEnablement.enabled(null, "large",
                ai.foresignal.source.adapters.ecmwf.AifsAdapter.PRODUCTS, List.of(),
                ai.foresignal.source.adapters.ecmwf.AifsAdapter.PRODUCTS);
        assertThat(large).contains("aifs");
    }

    @Test
    @DisplayName("the on() helper tests membership")
    void on() {
        assertThat(ProductEnablement.on(Set.of("a"), "a")).isTrue();
        assertThat(ProductEnablement.on(Set.of("a"), "b")).isFalse();
    }
}
