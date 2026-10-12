package ai.foresignal.security.ticket;

import ai.foresignal.security.AuthenticatedToken;
import java.time.Instant;
import java.util.List;

/**
 * One minted ticket, held in memory by the hash of its value (prompt 023).
 *
 * <p>Stream tickets: the first connect must come within 60 s of minting; after that the same ticket
 * keeps working for reconnects up to 5 min after the last disconnect and 24 h in total, with at most
 * one concurrent stream. Tile tickets are reusable for any number of requests until
 * {@link #expiresAt}. State changes are synchronized on the ticket.
 */
public final class Ticket {

    private final String hash;
    private final TicketPurpose purpose;
    private final AuthenticatedToken parent;
    private final List<String> paths;
    private final Instant mintedAt;
    private final Instant firstConnectBy;
    private final Instant expiresAt;
    private boolean connected;
    private int active;
    private Instant lastDisconnectAt;

    Ticket(String hash, TicketPurpose purpose, AuthenticatedToken parent, List<String> paths, Instant mintedAt,
            Instant firstConnectBy, Instant expiresAt) {
        this.hash = hash;
        this.purpose = purpose;
        this.parent = parent;
        this.paths = paths == null ? List.of() : List.copyOf(paths);
        this.mintedAt = mintedAt;
        this.firstConnectBy = firstConnectBy;
        this.expiresAt = expiresAt;
    }

    /** The SHA-256 of the ticket value. */
    public String hash() {
        return hash;
    }

    /** What the ticket is for. */
    public TicketPurpose purpose() {
        return purpose;
    }

    /** The token that minted it; the ticket never carries more than this token. */
    public AuthenticatedToken parent() {
        return parent;
    }

    /** The narrower paths it may be used on; empty for every path of its purpose. */
    public List<String> paths() {
        return paths;
    }

    /** When it was minted. */
    public Instant mintedAt() {
        return mintedAt;
    }

    /** Stream tickets: the first-connect deadline; null for tile tickets. */
    public Instant firstConnectBy() {
        return firstConnectBy;
    }

    /** The hard end of its life: 24 h after minting for streams, the ttl for tiles. */
    public Instant expiresAt() {
        return expiresAt;
    }

    /** True while a stream holds the ticket. */
    synchronized boolean inUse() {
        return active > 0;
    }

    /**
     * Whether the ticket can be used at {@code now}, ignoring concurrency: before its hard end and,
     * for a stream ticket, within the first-connect window or the reconnect window.
     */
    synchronized boolean usableAt(Instant now) {
        if (!now.isBefore(expiresAt)) {
            return false;
        }
        if (purpose != TicketPurpose.STREAM || active > 0) {
            return true;
        }
        if (!connected) {
            return !now.isAfter(firstConnectBy);
        }
        return lastDisconnectAt == null || !now.isAfter(lastDisconnectAt.plus(TicketStore.RECONNECT_WINDOW));
    }

    /** Claims the ticket for a stream; false when a stream already holds it or it is no longer usable. */
    synchronized boolean beginStream(Instant now) {
        if (active > 0 || !usableAt(now)) {
            return false;
        }
        active = 1;
        connected = true;
        return true;
    }

    /** Releases the stream's claim and starts the reconnect window. */
    synchronized void endStream(Instant now) {
        if (active > 0) {
            active = 0;
            lastDisconnectAt = now;
        }
    }
}
