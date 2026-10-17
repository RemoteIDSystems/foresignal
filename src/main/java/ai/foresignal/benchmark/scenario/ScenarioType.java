package ai.foresignal.benchmark.scenario;

import java.util.List;

/** The synthetic scenarios of {@code bench-scenarios} 1.0.0 (prompt 123), plus {@code historical} for corpus cases. */
public enum ScenarioType {

    /** Seasonal counts or levels with noise and no signal. */
    NULL_SEASONAL("null-seasonal"),
    /** A level shift of known size at a known time. */
    STEP_CHANGE("step-change"),
    /** A short burst. */
    SPIKE("spike"),
    /** True events during a source outage or low detectability. */
    COVERAGE_OUTAGE("coverage-outage"),
    /** One originator's report re-published ten times by aggregators. */
    DUPLICATE_ECHO("duplicate-echo"),
    /** Values or event times revised after first publication. */
    LATE_REVISION("late-revision"),
    /** Two series sharing a seasonal driver and trend but no lead–lag. */
    COMMON_DRIVER("common-driver"),
    /** X precedes Y with a known lag and hit rate. */
    TRUE_PRECURSOR("true-precursor"),
    /** The event rate changes halfway. */
    BASE_RATE_DRIFT("base-rate-drift"),
    /** An abnormal market move before planted events, plus placebo events. */
    PRE_EVENT_MOVE("pre-event-move"),
    /** A historical corpus case. */
    HISTORICAL("historical");

    /** The generator's name and version as stored with every run. */
    public static final String SUITE_VERSION = "bench-scenarios 1.0.0";

    /** The synthetic scenarios (every value but {@link #HISTORICAL}). */
    public static final List<ScenarioType> SYNTHETIC = List.of(NULL_SEASONAL, STEP_CHANGE, SPIKE, COVERAGE_OUTAGE,
            DUPLICATE_ECHO, LATE_REVISION, COMMON_DRIVER, TRUE_PRECURSOR, BASE_RATE_DRIFT, PRE_EVENT_MOVE);

    private final String wire;

    ScenarioType(String wire) {
        this.wire = wire;
    }

    /** The wire value. */
    public String wire() {
        return wire;
    }
}
