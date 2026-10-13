package ai.foresignal.source.adapters.cctv;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * What a {@link CctvPackParser} is given besides the body (prompt 072): the pack, the bodies of the pack's further
 * catalog URLs (Estonia's images document), and a place to count skipped entries and report a next page.
 *
 * <p>Not thread-safe: one context per fetch.
 */
public final class PackContext {

    /** Skip reason: no usable coordinates. */
    public static final String NO_COORDINATES = "no-coordinates";

    /** Skip reason: coordinates outside the pack's bounding box. */
    public static final String OUT_OF_BOUNDS = "out-of-bounds";

    /** Skip reason: an id already seen in this fetch. */
    public static final String DUPLICATE = "duplicate";

    /** Skip reason: an entry without a usable id or shape. */
    public static final String MALFORMED = "malformed";

    /** Skip reason: an entry the upstream marks as not a live camera (planned, removed, unpublished). */
    public static final String NOT_LISTED = "not-listed";

    private final CctvPack pack;
    private final List<byte[]> extraBodies;
    private final Map<String, Integer> skipped = new TreeMap<>();
    private URI nextPage;

    /**
     * Creates a context.
     *
     * @param pack the pack being parsed
     * @param extraBodies the bodies of the pack's catalog URLs after the first, in order; empty for most packs
     */
    public PackContext(CctvPack pack, List<byte[]> extraBodies) {
        this.pack = pack;
        this.extraBodies = extraBodies == null ? List.of() : List.copyOf(extraBodies);
    }

    /** A context with no further bodies. */
    public static PackContext of(CctvPack pack) {
        return new PackContext(pack, List.of());
    }

    /** The pack. */
    public CctvPack pack() {
        return pack;
    }

    /**
     * The body of the pack's catalog URL number {@code index + 1} (0 is the second URL).
     *
     * @throws ai.foresignal.source.spi.UpstreamFormatException when it was not fetched
     */
    public byte[] extraBody(int index) {
        if (index < 0 || index >= extraBodies.size()) {
            throw new ai.foresignal.source.spi.UpstreamFormatException(
                    pack.id() + ": catalog document " + (index + 2) + " is missing", "");
        }
        return extraBodies.get(index);
    }

    /** Counts one skipped entry under {@code reason}. */
    public void skip(String reason) {
        skipped.merge(reason, 1, Integer::sum);
    }

    /** Skipped entries by reason, sorted by reason. */
    public Map<String, Integer> skipped() {
        return Map.copyOf(skipped);
    }

    /** Skipped entries of one reason. */
    public int skipped(String reason) {
        return skipped.getOrDefault(reason, 0);
    }

    /** All skipped entries. */
    public int skippedTotal() {
        return skipped.values().stream().mapToInt(Integer::intValue).sum();
    }

    /** Reports the next page of a paginated catalog (OGC API {@code rel=next}); null for none. */
    public void nextPage(URI next) {
        this.nextPage = next;
    }

    /** The next page reported by the last parse, or null. */
    public URI nextPage() {
        return nextPage;
    }
}
