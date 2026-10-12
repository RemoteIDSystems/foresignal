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

    /**
     * History downsampling's startup recovery (prompt 044): an interrupted rewrite is rolled back or
     * finished, and the thinned ranges are reloaded, after the retention state and before the read
     * paths and the trigger engine depend on them.
     */
    public static final int HISTORY_DOWNSAMPLE = 13;

    /** The SSE hub's bus subscriptions (prompt 015). */
    public static final int SSE_HUB = 15;

    /**
     * Push setup (prompt 031): the VAPID key pair and the built-in Push channel, before the
     * notification router routes the first alert.
     */
    public static final int PUSH = 17;

    /** The notification router's alert bus subscription, before the engine fires alerts (prompt 018). */
    public static final int NOTIFY_ROUTER = 18;

    /**
     * The enrichment worker's alert bus subscription (prompt 030), after the notification router and
     * before the trigger engine fires alerts.
     */
    public static final int ENRICHMENT = 19;

    /** The trigger engine (prompt 017). */
    public static final int TRIGGER_ENGINE = 20;

    /**
     * The embedding worker's change-bus subscription and a resumed reindex (prompt 027), after the
     * trigger engine and before the scheduler (Conventions, "Startup order").
     */
    public static final int EMBEDDING = 22;

    /**
     * The metric projections' seeding and bus subscriptions (prompt 038), after the embedding worker and
     * before the canary and the scheduler.
     */
    public static final int METRIC_PROJECTIONS = 23;

    /**
     * The region tag worker's change-bus and live-bus subscriptions (prompt 040), after the metric
     * projections and before the canary and the scheduler.
     */
    public static final int REGION_TAGGING = 24;

    /** The ingest canary's change-bus subscription (prompt 019). */
    public static final int CANARY = 25;

    /**
     * Mirroring the product catalog into {@code raster_product} (prompt 042), before the scheduler
     * starts the first product fetch.
     */
    public static final int RASTER_PRODUCTS = 27;

    /** The collector scheduler (prompt 010). */
    public static final int SCHEDULER = 30;

    /** The briefing scheduler (prompt 029), after the collector scheduler. */
    public static final int BRIEFINGS = 35;

    /**
     * The reference data's first-start load (prompt 040), last: it is queued on the loader's own thread
     * after a delay and never blocks startup or readiness.
     */
    public static final int REFERENCE = 40;

    private StartupOrder() {}
}
