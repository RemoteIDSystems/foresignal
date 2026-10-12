package ai.foresignal.security.ticket;

import java.util.List;
import java.util.Locale;

/**
 * What a ticket may be used for (OpenAPI {@code StreamTicketRequest.purpose}, prompt 023), and on
 * which paths. Tickets are accepted only as {@code ?ticket=} on {@code GET} requests to these
 * paths, never in an {@code Authorization} header.
 */
public enum TicketPurpose {

    /** The SSE streams; reconnect-aware, one concurrent stream per ticket. */
    STREAM(List.of("/v1/changes/stream", "/v1/alerts/stream", "/v1/events/stream")),

    /** Vector tiles and TileJSON; reusable until expiry. */
    TILES(List.of("/v1/tiles/**", "/v1/tiles.json"));

    private final List<String> patterns;

    TicketPurpose(List<String> patterns) {
        this.patterns = patterns;
    }

    /** The Ant patterns of the paths this purpose covers. */
    public List<String> patterns() {
        return patterns;
    }

    /** The wire value. */
    public String wireValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** Parses a wire value; null when unknown. */
    public static TicketPurpose fromWireValue(String value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case "stream" -> STREAM;
            case "tiles" -> TILES;
            default -> null;
        };
    }
}
