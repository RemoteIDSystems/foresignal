package ai.foresignal.source.adapters.stac;

/**
 * One reviewed Earth Search feed: a collection, whether a cloud filter applies, and its licence.
 *
 * <p>The list is code-reviewed ({@link StacSources#REVIEWED}); a caller can never supply a collection or a
 * URL (the family rule of {@code SourceAdapterFamily}).
 *
 * @param id the source id, {@code stac-*}
 * @param name a human-readable name
 * @param collection the STAC collection searched
 * @param cloudFiltered true when {@code eo:cloud_cover} applies (false for SAR)
 * @param licence the data licence
 * @param attribution the credit line the upstream asks for
 * @param termsUrl the upstream's terms
 * @param homepage where a human can read about the collection
 * @param enabledByDefault the prompt's intent (all built-in sources start disabled until the operator
 *     enables them; Sentinel-1 is additionally marked off by default)
 */
public record StacSource(
        String id,
        String name,
        String collection,
        boolean cloudFiltered,
        String licence,
        String attribution,
        String termsUrl,
        String homepage,
        boolean enabledByDefault) {
}
