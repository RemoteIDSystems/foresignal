package ai.foresignal.access;

import java.time.Duration;
import java.util.List;

/**
 * The fixed rules of tile keys (prompt 050; Weather-Imagery-Design §6.7): the plaintext prefix, the
 * longest lifetime and the canonical path prefixes a tile key may be limited to.
 *
 * <p>A tile key is read-only (scope {@code read}), carried in the path ({@code /v1/k/{key}/…}),
 * expires within {@link #MAX_TTL}, and never reaches private sources.
 */
public final class TileKeyPolicy {

    /** Every tile key starts with this (Conventions "Key-in-path URLs"). */
    public static final String KEY_PREFIX = "fsk_";

    /** The longest a tile key may live. */
    public static final Duration MAX_TTL = Duration.ofDays(30);

    /**
     * The canonical prefixes a tile key may name; anything else is {@code 400 VALIDATION_FAILED} at
     * creation. {@code /v1/maps/static} (prompt 055) and {@code /v1/sample} (prompt 052) are accepted
     * before their endpoints exist and answer {@code 404} until then.
     */
    public static final List<String> ALLOWED_PREFIXES = List.of(
            "/v1/products/",
            "/v1/styles",
            "/v1/media/",
            "/v1/tiles/",
            "/v1/maps/static",
            "/v1/sample",
            "/v1/ground-stations/",
            "/v1/changes/stream",
            "/v1/events/stream");

    private TileKeyPolicy() {}

    /** True when {@code prefix} is one of {@link #ALLOWED_PREFIXES}, exactly. */
    public static boolean allowed(String prefix) {
        return prefix != null && ALLOWED_PREFIXES.contains(prefix);
    }
}
