package ai.foresignal.source.spi;

/**
 * A raster or field product a source publishes (Weather-Imagery-Design §2.2).
 *
 * <p>Minimal on purpose: the raster-products prompt extends this record. The slot exists now so
 * that adding products later does not mean changing the descriptor's shape.
 *
 * @param id stable kebab-case identifier
 * @param title shown to operators
 * @param kind the record kind the product's frames are published as
 */
public record ProductSpec(String id, String title, String kind) {

    public ProductSpec {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ProductSpec id is required");
        }
    }
}
