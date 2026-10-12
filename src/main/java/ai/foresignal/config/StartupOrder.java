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

    /** The trigger persist gate's first rule snapshot, before the writer reads it (prompt 017). */
    public static final int TRIGGER_GATE = 5;

    /** The single ingest writer (prompt 009). */
    public static final int WRITER = 10;

    /** The SSE hub's bus subscriptions (prompt 015). */
    public static final int SSE_HUB = 15;

    /** The trigger engine (prompt 017). */
    public static final int TRIGGER_ENGINE = 20;

    /** The collector scheduler (prompt 010). */
    public static final int SCHEDULER = 30;

    private StartupOrder() {}
}
