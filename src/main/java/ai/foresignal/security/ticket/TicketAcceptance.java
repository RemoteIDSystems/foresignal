package ai.foresignal.security.ticket;

/**
 * The outcome of presenting a ticket (prompt 023).
 */
public sealed interface TicketAcceptance {

    /**
     * The ticket is valid for this request. A stream ticket is now claimed and must be released with
     * {@link TicketStore#release(Ticket)} when the stream ends.
     *
     * @param ticket the ticket
     */
    record Accepted(Ticket ticket) implements TicketAcceptance {}

    /**
     * Unknown, expired, outside its windows, used on a path or method its purpose does not cover,
     * or its parent token is no longer valid: answered {@code 401}, and the client mints again.
     *
     * @param reason why, for the problem detail (never the ticket value)
     */
    record Rejected(String reason) implements TicketAcceptance {}

    /** A stream already holds this stream ticket: answered {@code 429 STREAM_LIMIT}. */
    record Busy() implements TicketAcceptance {}
}
