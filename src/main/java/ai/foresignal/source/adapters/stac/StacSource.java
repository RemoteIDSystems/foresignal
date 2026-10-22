package ai.foresignal.source.adapters.stac;

/**
 * One reviewed Earth Search feed: a collection, whether a cloud filter applies, and its license.
 *
 * <p>The list is code-reviewed ({@link StacSources#REVIEWED}); a caller can never supply a collection or a
 * URL (the family rule of {@code SourceAdapterFamily}).
 *
 * @param id the source id, {@code stac-*}
 * @param name a human-readable name
 * @param collection the STAC collection searched
 * @param cloudFiltered true when {@code eo:cloud_cover} applies (false for SAR)
 * @param licence the data license
 * @param attribution the credit line the upstream asks for
 * @param termsUrl the upstream's terms
 * @param homepage where a human can read about the collection
 * @param enabledByDefault prompt 096's intent, recorded only: seeding follows the source rule of prompts 145 and 146
 *     (enabled unless the source needs a credential or a parameter); every STAC source needs its {@code aoi}, and
 *     Sentinel-1 is additionally marked off
 * @param observedAtField the item property {@code observedAt} is read from for this collection,
 *     {@value #UPDATED} or {@value #CREATED}: fixed per collection and decided by the captured fixture, never per
 *     item (prompt 096); an item without it is skipped
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
        boolean enabledByDefault,
        String observedAtField) {

    /** {@code properties.updated}. */
    public static final String UPDATED = "updated";

    /** {@code properties.created}. */
    public static final String CREATED = "created";

    /** Accepts {@value #UPDATED} or {@value #CREATED} only. */
    public StacSource {
        if (!UPDATED.equals(observedAtField) && !CREATED.equals(observedAtField)) {
            throw new IllegalArgumentException(
                    "observedAtField must be " + UPDATED + " or " + CREATED + ", not " + observedAtField);
        }
    }

    /** A feed whose {@code observedAt} is {@code properties.updated}. */
    public StacSource(String id, String name, String collection, boolean cloudFiltered, String licence,
            String attribution, String termsUrl, String homepage, boolean enabledByDefault) {
        this(id, name, collection, cloudFiltered, licence, attribution, termsUrl, homepage, enabledByDefault, UPDATED);
    }
}
