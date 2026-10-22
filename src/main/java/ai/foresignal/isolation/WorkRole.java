package ai.foresignal.isolation;

import java.util.Objects;

/**
 * What a thread is doing, which decides the database pool it draws from ({@link RoleRoutingDataSource}).
 *
 * <p>The roles keep the API answering while data is being gathered: a request thread draws only from the
 * {@link #API} pool, and every thread that gathers data (scheduled fetches, streams, derived sources, inbound
 * bindings, federation peers) draws only from the {@link #COLLECT} pool, so collection at any scale can never
 * take a connection a request needs. The ingest writer, the one thread that persists what was gathered, has its
 * own {@link #INGEST} pool so neither side can starve it. Every other thread (scheduled jobs, workers, startup)
 * is {@link #BACKGROUND}.
 *
 * <p>A thread's role is set explicitly: by {@link WorkThreads} for the threads it creates, and by
 * {@link ApiWorkRoleFilter} for the duration of a request. It is not inherited, so a helper thread started
 * from a request runs as {@link #BACKGROUND}, never in the API pool.
 */
public enum WorkRole {
    /** Serving an HTTP request. */
    API,
    /** Gathering data from a source. */
    COLLECT,
    /** Persisting gathered data: the ingest writer. */
    INGEST,
    /** Everything else. */
    BACKGROUND;

    private static final ThreadLocal<WorkRole> CURRENT = new ThreadLocal<>();

    /** The current thread's role; {@link #BACKGROUND} when none was set. */
    public static WorkRole current() {
        WorkRole role = CURRENT.get();
        return role == null ? BACKGROUND : role;
    }

    /**
     * Runs {@code body} with the current thread in {@code role}, then restores the role it had.
     *
     * @param role the role to run as
     * @param body what to run
     */
    public static void runAs(WorkRole role, Runnable body) {
        Objects.requireNonNull(body, "body");
        try (Scope ignored = enter(role)) {
            body.run();
        }
    }

    /**
     * Puts the current thread in {@code role} until the returned scope is closed, which restores the role it
     * had.
     *
     * @param role the role to enter
     * @return the scope to close
     */
    public static Scope enter(WorkRole role) {
        Objects.requireNonNull(role, "role");
        WorkRole previous = CURRENT.get();
        CURRENT.set(role);
        return () -> {
            if (previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(previous);
            }
        };
    }

    /** A role entered with {@link #enter}; closing it restores the previous role. */
    @FunctionalInterface
    public interface Scope extends AutoCloseable {
        @Override
        void close();
    }
}
