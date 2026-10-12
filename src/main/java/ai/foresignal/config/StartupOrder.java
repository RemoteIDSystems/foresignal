package ai.foresignal.config;

/**
 * The order in which background services start once the application is ready.
 *
 * <p>Schema bootstrap and partition creation happen before the {@code EntityManagerFactory}
 * ({@code SchemaBootstrap}); everything here starts on {@code ApplicationReadyEvent}, in ascending
 * order, by annotating the listener with {@code @Order(StartupOrder.X)}. The writer comes first so
 * that the trigger engine and the scheduler never submit work before anything can process it.
 * New background services register their phase here (Conventions, "Startup order").
 */
public final class StartupOrder {

    /**
     * Registering persisted custom sources and external providers (prompt 024). Runs before every
     * phase here: {@code CustomSourceRegistrar} registers them in
     * {@code afterSingletonsInstantiated}, before the web server and every
     * {@code ApplicationReadyEvent} listener (writer, trigger engine, scheduler) start, so no
     * consumer ever meets an unknown source id. The constant documents the position.
     */
    public static final int CUSTOM_SOURCES = 1;

    /** The trigger persist gate's first rule snapshot, before the writer reads it (prompt 017). */
    public static final int TRIGGER_GATE = 5;

    /** The single ingest writer (prompt 009). */
    public static final int WRITER = 10;

    /**
     * Loading the published retention state (prompt 019), before the SSE hub and the read API start
     * deciding {@code reset} and {@code 410} from it.
     */
    public static final int RETENTION_STATE = 12;

    /** The SSE hub's bus subscriptions (prompt 015). */
    public static final int SSE_HUB = 15;

    /** The notification router's alert bus subscription, before the engine fires alerts (prompt 018). */
    public static final int NOTIFY_ROUTER = 18;

    /** The trigger engine (prompt 017). */
    public static final int TRIGGER_ENGINE = 20;

    /** The ingest canary's change-bus subscription (prompt 019). */
    public static final int CANARY = 25;

    /** The collector scheduler (prompt 010). */
    public static final int SCHEDULER = 30;

    private StartupOrder() {}
}
