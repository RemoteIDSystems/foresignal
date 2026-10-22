package ai.foresignal.isolation;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Creates the threads that do one kind of work: dedicated platform threads, each running in its {@link WorkRole}.
 *
 * <p>Data gathering runs on these rather than on virtual threads. Virtual threads share a small set of carrier
 * threads with the HTTP request threads, so a few hundred sources blocking on upstreams and the database could
 * occupy every carrier and stop the API answering (2026-10-21: with the free sources enabled, fetches pinned
 * all 16 carriers waiting for pool connections). A platform thread blocks only itself; a slow or stuck source
 * holds its own thread and nothing else.
 *
 * <p>Every thread is a daemon, so none of them keeps the JVM alive at shutdown; the owners stop them.
 */
public final class WorkThreads {

    private WorkThreads() {}

    /**
     * An unstarted platform thread that runs {@code body} in {@code role}.
     *
     * @param role the thread's role
     * @param name the thread's name, as thread dumps show it
     * @param body what it runs
     * @return the thread, not yet started
     */
    public static Thread unstarted(WorkRole role, String name, Runnable body) {
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(body, "body");
        Thread thread = Thread.ofPlatform().name(name).daemon(true).unstarted(() -> WorkRole.runAs(role, body));
        return thread;
    }

    /**
     * Starts a platform thread that runs {@code body} in {@code role}.
     *
     * @param role the thread's role
     * @param name the thread's name, as thread dumps show it
     * @param body what it runs
     * @return the started thread
     */
    public static Thread start(WorkRole role, String name, Runnable body) {
        Thread thread = unstarted(role, name, body);
        thread.start();
        return thread;
    }

    /**
     * A factory of platform threads in {@code role}, named {@code prefix} followed by a counter from 0.
     *
     * @param role the threads' role
     * @param prefix the start of every thread's name
     * @return the factory
     */
    public static ThreadFactory factory(WorkRole role, String prefix) {
        Objects.requireNonNull(prefix, "prefix");
        AtomicLong next = new AtomicLong();
        return body -> unstarted(role, prefix + next.getAndIncrement(), body);
    }

    /**
     * A factory of platform threads in {@code role} that all carry the same {@code name}: for an executor that
     * only ever runs one thread, such as a source's own.
     *
     * @param role the threads' role
     * @param name every thread's name
     * @return the factory
     */
    public static ThreadFactory named(WorkRole role, String name) {
        Objects.requireNonNull(name, "name");
        return body -> unstarted(role, name, body);
    }
}
