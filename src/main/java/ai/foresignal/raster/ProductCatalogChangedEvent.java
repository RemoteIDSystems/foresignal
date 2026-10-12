package ai.foresignal.raster;

/**
 * Published when the product catalog changes (prompt 042): a dynamic product registered, redefined
 * or removed, or a product disabled or re-enabled. Prompt 045 drops cached TileJSON and styles on it;
 * catalog ETags change with it.
 *
 * @param productId the product
 * @param sourceId its owning source
 * @param change what happened
 */
public record ProductCatalogChangedEvent(String productId, String sourceId, Change change) {

    /** What happened to the product. */
    public enum Change {
        /** A product joined the catalog. */
        REGISTERED,
        /** A dynamic product's spec changed in place. */
        UPDATED,
        /** A dynamic product left the catalog. */
        REMOVED,
        /** The product was disabled ({@code PRODUCT_UNAVAILABLE}). */
        DISABLED,
        /** The product was re-enabled. */
        ENABLED
    }
}
