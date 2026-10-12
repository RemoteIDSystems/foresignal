package ai.foresignal.retention;

/**
 * One ordered step of a {@link RetentionJob} run (prompt 019).
 *
 * <p>Steps run in ascending {@link #order()}. The built-in steps use 100–700 (TRACK partitions,
 * STANDARD deletes, STANDARD leaf drops, head purges, {@code fetch_log}, {@code seq_clock}, other
 * tables); later prompts add steps by declaring beans (027 embeddings, 028 Ask threads, 029
 * briefings, 023 audit). A step that throws is logged and the run continues with the next one.
 * Steps must not touch the intelligence block's typed tables unless their prompt says so.
 */
public interface RetentionStep {

    /** A short name for logs and the {@code foresignal.retention.deleted{table}} metric. */
    String name();

    /** The position in the run; lower runs first. */
    int order();

    /** Performs the step, recording its counts on {@code run}. */
    void run(RetentionRun run);
}
