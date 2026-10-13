package ai.foresignal.source.kit.feed;

import ai.foresignal.source.spi.NormalizedRecord;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Feed items as records (prompt 073), shared by the polling adapter and the WebSub push path. Pure.
 *
 * <ul>
 *   <li><strong>Filter and cap:</strong> the spec's category filter, then the first {@code maxItems} items in
 *       document order;
 *   <li><strong>Identity:</strong> {@code externalId} is the item id (an id longer than 256 characters becomes
 *       {@code sha256:} plus its first 32 hex characters); a repeated id keeps its first item;
 *   <li><strong>Times:</strong> {@code eventTime} is {@code published}; {@code observedAt} follows the spec
 *       ({@code updated}, {@code published} or receipt); an item without the named date is skipped
 *       ({@value #SKIP_MISSING_TIME}), never given the other date;
 *   <li><strong>Payload:</strong> {@code title, summary, link, published, updated, categories, authors, enclosures,
 *       feedTitle}, nulls and empty lists left out; geometry from the item (often none);
 *   <li><strong>Item mapper:</strong> the spec's reviewed {@link FeedItemMapper}, when named, sees each record last.
 * </ul>
 * Feeds are windows, so nothing here tombstones; items expire by {@code recordTtl}.
 */
public final class FeedRecords {

    /** The skip reason of an item without the date {@code observedAt} names. */
    public static final String SKIP_MISSING_TIME = "missing-observed-at";

    /** The skip reason of a filtered item. */
    public static final String SKIP_FILTERED = "category-filter";

    /** The skip reason of an item past {@code maxItems}. */
    public static final String SKIP_OVER_CAP = "over-max-items";

    /** The skip reason of a repeated id. */
    public static final String SKIP_DUPLICATE = "duplicate-id";

    /** The skip reason of an item its mapper dropped. */
    public static final String SKIP_MAPPER = "item-mapper";

    /** The longest external id. */
    static final int MAX_ID = 256;

    /**
     * The records of a document.
     *
     * @param records the records
     * @param skipped skip reason to count
     */
    public record Mapped(List<NormalizedRecord> records, Map<String, Integer> skipped) {}

    private FeedRecords() {}

    /**
     * Maps a document under its spec.
     *
     * @param spec the source's spec
     * @param document the parsed feed
     */
    public static Mapped map(FeedSourceSpec spec, FeedDocument document) {
        List<NormalizedRecord> out = new ArrayList<>();
        Map<String, Integer> skipped = new TreeMap<>();
        Set<String> ids = new HashSet<>();
        FeedItemMapper mapper = FeedItemMappers.of(spec.itemMapper());
        int kept = 0;
        for (FeedItem item : document.items()) {
            if (!spec.categories().keeps(item.categories())) {
                skipped.merge(SKIP_FILTERED, 1, Integer::sum);
                continue;
            }
            if (kept >= spec.effectiveMaxItems()) {
                skipped.merge(SKIP_OVER_CAP, 1, Integer::sum);
                continue;
            }
            kept++;
            Instant observedAt = switch (spec.effectiveObservedAt()) {
                case FeedSourceSpec.UPDATED -> item.updated();
                case FeedSourceSpec.PUBLISHED -> item.published();
                default -> null;
            };
            if (observedAt == null && !FeedSourceSpec.RECEIVED.equals(spec.effectiveObservedAt())) {
                skipped.merge(SKIP_MISSING_TIME, 1, Integer::sum);
                continue;
            }
            String id = externalId(item.id());
            if (!ids.add(id)) {
                skipped.merge(SKIP_DUPLICATE, 1, Integer::sum);
                continue;
            }
            NormalizedRecord record = NormalizedRecord.builder(id, spec.effectiveKind())
                    .observedAt(observedAt)
                    .eventTime(item.published())
                    .geometry(item.geometry())
                    .putAll(payload(item, document))
                    .build();
            if (mapper != null) {
                record = mapper.map(item, document, spec, record);
                if (record == null) {
                    skipped.merge(SKIP_MAPPER, 1, Integer::sum);
                    continue;
                }
            }
            out.add(record);
        }
        return new Mapped(out, skipped);
    }

    /** The payload of an item: the allowlisted keys, nulls and empty lists left out. */
    static Map<String, Object> payload(FeedItem item, FeedDocument document) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("title", item.title());
        p.put("summary", item.summary());
        p.put("link", item.link());
        p.put("published", item.published() == null ? null : item.published().toString());
        p.put("updated", item.updated() == null ? null : item.updated().toString());
        p.put("categories", item.categories().isEmpty() ? null : item.categories());
        p.put("authors", item.authors().isEmpty() ? null : item.authors());
        if (!item.enclosures().isEmpty()) {
            List<Map<String, Object>> enclosures = new ArrayList<>();
            for (FeedItem.Enclosure e : item.enclosures()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("url", e.url());
                if (e.type() != null) {
                    m.put("type", e.type());
                }
                if (e.length() != null) {
                    m.put("length", e.length());
                }
                enclosures.add(m);
            }
            p.put("enclosures", enclosures);
        }
        p.put("feedTitle", document.title());
        p.values().removeIf(v -> v == null);
        return p;
    }

    /** {@code id}, or a digest of it when it is longer than {@value #MAX_ID} characters. */
    static String externalId(String id) {
        if (id.length() <= MAX_ID) {
            return id;
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(id.getBytes(StandardCharsets.UTF_8));
            return "sha256:" + HexFormat.of().formatHex(digest).substring(0, 32);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
