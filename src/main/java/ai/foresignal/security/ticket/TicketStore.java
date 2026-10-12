package ai.foresignal.security.ticket;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.ApiTokenRevokedEvent;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.TokenHasher;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.Durations;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import tools.jackson.databind.JsonNode;

/**
 * Stream and tile tickets (prompt 023): for clients that cannot send an {@code Authorization}
 * header (browser {@code EventSource}, map tile loaders).
 *
 * <ul>
 *   <li>A ticket is {@code fst_} + base64url of 24 random bytes, held in memory by its SHA-256, so
 *       tickets are lost on restart and clients mint again after a {@code 401}.
 *   <li>It inherits the minting token's id, scope and restrictions, never more; every use re-checks
 *       that the parent token is still valid.
 *   <li>Stream tickets: first connect within {@link #FIRST_CONNECT_WINDOW}; then reconnects up to
 *       {@link #RECONNECT_WINDOW} after the last disconnect and {@link #STREAM_LIFETIME} in total;
 *       one concurrent stream.
 *   <li>Tile tickets: reusable until expiry ({@link #DEFAULT_TILE_TTL}, at most
 *       {@link #MAX_TILE_TTL}).
 *   <li>Revoking the parent token drops its tickets ({@link ApiTokenRevokedEvent}).
 * </ul>
 */
@Component
public class TicketStore {

    /** Every ticket value starts with this. */
    public static final String TICKET_PREFIX = "fst_";

    /** Random bytes behind a ticket. */
    static final int TICKET_BYTES = 24;

    /** A stream ticket must first connect within this of minting. */
    public static final Duration FIRST_CONNECT_WINDOW = Duration.ofSeconds(60);

    /** A stream ticket reconnects up to this after its last disconnect. */
    public static final Duration RECONNECT_WINDOW = Duration.ofMinutes(5);

    /** The longest a stream ticket lives. */
    public static final Duration STREAM_LIFETIME = Duration.ofHours(24);

    /** A tile ticket's lifetime when the request names none. */
    public static final Duration DEFAULT_TILE_TTL = Duration.ofMinutes(10);

    /** The longest tile-ticket lifetime. */
    public static final Duration MAX_TILE_TTL = Duration.ofHours(1);

    static final Set<String> REQUEST_MEMBERS = Set.of("purpose", "paths", "ttl");

    /**
     * A newly minted ticket, returned once.
     *
     * @param ticket the ticket value
     * @param purpose what it is for
     * @param expiresAt stream: the first-connect deadline; tiles: the end of its life
     * @param paths the narrower paths, or empty
     */
    public record MintedTicket(String ticket, TicketPurpose purpose, Instant expiresAt, List<String> paths) {}

    private final TokenHasher hasher;
    private final TokenService tokens;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();
    private final AntPathMatcher matcher = new AntPathMatcher();
    private final Map<String, Ticket> tickets = new ConcurrentHashMap<>();

    /** Creates the store. */
    public TicketStore(TokenHasher hasher, TokenService tokens, Clock clock) {
        this.hasher = hasher;
        this.tokens = tokens;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- mint

    /**
     * Mints a ticket for {@code parent} from a {@code StreamTicketRequest} body.
     *
     * @throws ValidationException listing every problem
     */
    public MintedTicket mint(AuthenticatedToken parent, JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a StreamTicketRequest object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String member : body.propertyNames()) {
            if (!REQUEST_MEMBERS.contains(member)) {
                errors.add(new FieldError(member, "is not a StreamTicketRequest member"));
            }
        }
        JsonNode purposeNode = body.get("purpose");
        TicketPurpose purpose = purposeNode != null && purposeNode.isString()
                ? TicketPurpose.fromWireValue(purposeNode.asString())
                : null;
        if (purpose == null) {
            errors.add(new FieldError("purpose", "must be stream or tiles"));
        }
        List<String> paths = paths(body.get("paths"), purpose, errors);
        Duration ttl = ttl(body.get("ttl"), purpose, errors);
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "The ticket request is not valid.", errors);
        }
        prune();
        String value = newValue();
        Instant now = clock.instant();
        Ticket ticket = purpose == TicketPurpose.STREAM
                ? new Ticket(hasher.hash(value), purpose, parent, paths, now, now.plus(FIRST_CONNECT_WINDOW),
                        now.plus(STREAM_LIFETIME))
                : new Ticket(hasher.hash(value), purpose, parent, paths, now, null, now.plus(ttl));
        tickets.put(ticket.hash(), ticket);
        Instant shown = purpose == TicketPurpose.STREAM ? ticket.firstConnectBy() : ticket.expiresAt();
        return new MintedTicket(value, purpose, shown, ticket.paths());
    }

    // ------------------------------------------------------------------------- accept

    /** True when {@code method} on {@code path} is a path some ticket purpose covers. */
    public boolean coversPath(HttpMethod method, String path) {
        return purposeFor(method, path).isPresent();
    }

    /**
     * Presents {@code value} for {@code method} on {@code path}. A stream ticket that is accepted is
     * claimed until {@link #release(Ticket)}.
     */
    public TicketAcceptance accept(String value, HttpMethod method, String path) {
        if (value == null || !value.startsWith(TICKET_PREFIX)) {
            return new TicketAcceptance.Rejected("The ticket is not valid; mint a new one.");
        }
        Ticket ticket = tickets.get(hasher.hash(value));
        Instant now = clock.instant();
        if (ticket == null) {
            return new TicketAcceptance.Rejected("The ticket is unknown or has expired; mint a new one.");
        }
        Optional<TicketPurpose> purpose = purposeFor(method, path);
        if (purpose.isEmpty() || purpose.get() != ticket.purpose()) {
            return new TicketAcceptance.Rejected("This ticket is for " + ticket.purpose().wireValue()
                    + " requests and is not accepted here.");
        }
        String normalized = normalize(path);
        if (!ticket.paths().isEmpty() && ticket.paths().stream().noneMatch(p -> matcher.match(p, normalized))) {
            return new TicketAcceptance.Rejected("This ticket is limited to other paths.");
        }
        if (!ticket.usableAt(now)) {
            if (!ticket.inUse()) {
                tickets.remove(ticket.hash(), ticket);
            }
            return new TicketAcceptance.Rejected("The ticket has expired; mint a new one.");
        }
        Optional<AuthenticatedToken> parent = tokens.current(ticket.parent().id());
        if (parent.isEmpty()) {
            tickets.remove(ticket.hash(), ticket);
            return new TicketAcceptance.Rejected("The token that minted this ticket is no longer valid.");
        }
        if (ticket.purpose() == TicketPurpose.STREAM && !ticket.beginStream(now)) {
            return ticket.inUse()
                    ? new TicketAcceptance.Busy()
                    : new TicketAcceptance.Rejected("The ticket has expired; mint a new one.");
        }
        return new TicketAcceptance.Accepted(ticket);
    }

    /** Ends a stream's claim on {@code ticket}; the reconnect window starts now. */
    public void release(Ticket ticket) {
        if (ticket != null && ticket.purpose() == TicketPurpose.STREAM) {
            ticket.endStream(clock.instant());
        }
    }

    // ------------------------------------------------------------------------- revocation

    /** Drops every ticket of a revoked token. Its open streams are closed by the SSE hub. */
    @EventListener
    public void onTokenRevoked(ApiTokenRevokedEvent event) {
        tickets.values().removeIf(t -> t.parent().id().equals(event.tokenId()));
    }

    /** True while a stream holds the ticket {@code value} (diagnostics and tests). */
    public boolean inUse(String value) {
        Ticket ticket = value == null ? null : tickets.get(hasher.hash(value));
        return ticket != null && ticket.inUse();
    }

    /** Number of tickets held, for tests and diagnostics. */
    public int size() {
        return tickets.size();
    }

    /** Forgets every ticket, as a restart does. */
    public void clear() {
        tickets.clear();
    }

    // ------------------------------------------------------------------------- helpers

    private Optional<TicketPurpose> purposeFor(HttpMethod method, String path) {
        if (!HttpMethod.GET.equals(method) && !HttpMethod.HEAD.equals(method)) {
            return Optional.empty();
        }
        String normalized = normalize(path);
        for (TicketPurpose purpose : TicketPurpose.values()) {
            if (purpose.patterns().stream().anyMatch(p -> matcher.match(p, normalized))) {
                return Optional.of(purpose);
            }
        }
        return Optional.empty();
    }

    private List<String> paths(JsonNode node, TicketPurpose purpose, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return List.of();
        }
        if (!node.isArray()) {
            errors.add(new FieldError("paths", "must be an array of paths"));
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (JsonNode item : node) {
            if (!item.isString() || !item.asString().startsWith("/")) {
                errors.add(new FieldError("paths", "must contain only paths starting with /"));
                return List.of();
            }
            String path = normalize(item.asString().strip());
            if (purpose != null && purpose.patterns().stream().noneMatch(p -> matcher.match(p, path))) {
                errors.add(new FieldError("paths", "'" + path + "' is outside the " + purpose.wireValue()
                        + " paths " + purpose.patterns()));
                continue;
            }
            out.add(path);
        }
        return out;
    }

    private static Duration ttl(JsonNode node, TicketPurpose purpose, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return DEFAULT_TILE_TTL;
        }
        if (purpose == TicketPurpose.STREAM) {
            errors.add(new FieldError("ttl", "applies to tile tickets only; stream tickets follow their windows"));
            return DEFAULT_TILE_TTL;
        }
        if (!node.isString()) {
            errors.add(new FieldError("ttl", "must be a duration such as 10m or PT10M"));
            return DEFAULT_TILE_TTL;
        }
        try {
            Duration ttl = Durations.parse(node.asString());
            if (ttl.isZero() || ttl.isNegative() || ttl.compareTo(MAX_TILE_TTL) > 0) {
                errors.add(new FieldError("ttl", "must be positive and at most 1h"));
                return DEFAULT_TILE_TTL;
            }
            return ttl;
        } catch (InvalidInputException e) {
            errors.add(new FieldError("ttl", e.getMessage()));
            return DEFAULT_TILE_TTL;
        }
    }

    private void prune() {
        Instant now = clock.instant();
        tickets.values().removeIf(t -> !t.inUse() && !t.usableAt(now));
    }

    private String newValue() {
        byte[] bytes = new byte[TICKET_BYTES];
        random.nextBytes(bytes);
        return TICKET_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String normalize(String path) {
        String value = path == null ? "/" : path;
        int query = value.indexOf('?');
        if (query >= 0) {
            value = value.substring(0, query);
        }
        if (value.length() > 1 && value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
