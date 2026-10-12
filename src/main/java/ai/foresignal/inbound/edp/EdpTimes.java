package ai.foresignal.inbound.edp;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.OffsetDateTime;
import tools.jackson.databind.JsonNode;

/**
 * Time rules of EDP items (docs/External-Data-Protocol.md §3.5).
 *
 * <p>Provider times are stored as claims; Foresignal's receipt is the transaction-time boundary, so
 * availability ({@code knownAt}, {@code publishedAt}, {@code madeAt}) is
 * {@code max(provider time, receipt)} and a provider can never make information appear available
 * before Foresignal had it (invariant EXT-4).
 */
public final class EdpTimes {

    private EdpTimes() {}

    /**
     * The effective availability time: the later of {@code provider} and {@code receivedAt}.
     *
     * @param provider the provider's claim, or null
     * @param receivedAt when Foresignal received the item
     * @return never earlier than {@code receivedAt}
     */
    public static Instant effective(Instant provider, Instant receivedAt) {
        if (provider == null || provider.isBefore(receivedAt)) {
            return receivedAt;
        }
        return provider;
    }

    /**
     * Parses an RFC 3339 date-time with an offset.
     *
     * @return the instant, or null when {@code text} is null or not a valid date-time
     */
    public static Instant parse(String text) {
        if (text == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(text).toInstant();
        } catch (DateTimeException e) {
            return null;
        }
    }

    /** The date-time member {@code name} of {@code node}, or null when absent, null or invalid. */
    public static Instant parse(JsonNode node, String name) {
        JsonNode value = node == null ? null : node.get(name);
        return value == null || !value.isString() ? null : parse(value.asString());
    }
}
