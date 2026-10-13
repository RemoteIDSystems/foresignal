package ai.foresignal.source.kit.feed;

import ai.foresignal.source.spi.NormalizedRecord;

/**
 * Source-specific parsing of feed items (prompt 073), named by a feed spec's {@code itemMapper}: for a feed whose
 * items carry structured data in their text (prompt 075's InciWeb, for example).
 *
 * <p>Mappers are reviewed code registered in {@link FeedItemMappers}; a spec cannot name a class that is not there.
 * A mapper is pure, and treats item text as untrusted data.
 */
public interface FeedItemMapper {

    /** The name a spec uses. */
    String name();

    /**
     * Maps one item.
     *
     * @param item the parsed item
     * @param document the feed it came from
     * @param spec the source's spec
     * @param standard the record the kit would have produced
     * @return the record to keep (often {@code standard} with extra payload), or null to skip the item
     */
    NormalizedRecord map(FeedItem item, FeedDocument document, FeedSourceSpec spec, NormalizedRecord standard);
}
