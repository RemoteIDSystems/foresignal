package ai.foresignal.mcp;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

/**
 * Binds each MCP session to the caller that created it (prompt 025): a session id presented later
 * with a different token, or without one, is refused.
 *
 * <p>The SDK creates a session synchronously on the request thread that carries {@code initialize}
 * (streamable HTTP) or opens the event stream (legacy SSE), so {@link McpCallerFilter} runs the
 * request inside {@link #open}, and the session factories call {@link #bind} with that caller. A
 * session created outside such a request is bound to nobody and every later request is refused,
 * which fails closed.
 *
 * <p>Also counts the open sessions for {@code /v1/system.mcp.sessions}. Streamable sessions end
 * when the client sends {@code DELETE /mcp}; legacy sessions when their event stream closes.
 */
@Component
public class McpSessions {

    /** The streamable HTTP transport ({@code /mcp}). */
    public static final String STREAMABLE = "streamable";

    /** The legacy HTTP+SSE transport ({@code /mcp/sse}, {@code /mcp/message}). */
    public static final String LEGACY_SSE = "legacySse";

    /** The binding of a session created without a caller; it matches no caller. */
    static final String UNBOUND = "unbound";

    private static final ThreadLocal<McpCaller> PENDING = new ThreadLocal<>();

    private final Map<String, Binding> bindings = new ConcurrentHashMap<>();
    private final Clock clock;

    /** Creates the registry. */
    public McpSessions(Clock clock) {
        this.clock = clock;
    }

    /**
     * One session's owner.
     *
     * @param callerKey {@link McpCaller#key()} of the creator
     * @param transport the transport
     * @param createdAt when it was created
     */
    public record Binding(String callerKey, String transport, Instant createdAt) {}

    /**
     * Makes {@code caller} the creator of any session opened on this thread until the returned
     * scope is closed. Scopes nest.
     */
    public CallerScope open(McpCaller caller) {
        McpCaller previous = PENDING.get();
        PENDING.set(caller);
        return () -> {
            if (previous == null) {
                PENDING.remove();
            } else {
                PENDING.set(previous);
            }
        };
    }

    /** {@code work} run with {@code caller} as the creator of any session it opens. */
    public <T> T callAs(McpCaller caller, Supplier<T> work) {
        try (CallerScope ignored = open(caller)) {
            return work.get();
        }
    }

    /** An open {@link #open} scope; closing it restores the previous caller. */
    @FunctionalInterface
    public interface CallerScope extends AutoCloseable {

        /** Restores the previous caller. */
        @Override
        void close();
    }

    /** Binds a new session to the caller of the current request (or to nobody outside one). */
    public void bind(String sessionId, String transport) {
        McpCaller caller = PENDING.get();
        bindings.put(sessionId, new Binding(caller == null ? UNBOUND : caller.key(), transport, clock.instant()));
    }

    /** The binding of {@code sessionId}, if the session is open. */
    public Optional<Binding> binding(String sessionId) {
        return sessionId == null ? Optional.empty() : Optional.ofNullable(bindings.get(sessionId));
    }

    /** True when {@code sessionId} is open and was created by {@code caller}. */
    public boolean matches(String sessionId, McpCaller caller) {
        Binding binding = sessionId == null ? null : bindings.get(sessionId);
        return binding != null && caller != null && binding.callerKey().equals(caller.key());
    }

    /** Forgets a closed session. */
    public void remove(String sessionId) {
        if (sessionId != null) {
            bindings.remove(sessionId);
        }
    }

    /** Open sessions on every transport. */
    public int count() {
        return bindings.size();
    }

    /** Open sessions on {@code transport}. */
    public int count(String transport) {
        return (int) bindings.values().stream().filter(b -> b.transport().equals(transport)).count();
    }

    /** Forgets every session; for tests. */
    public void clear() {
        bindings.clear();
    }
}
