package ai.foresignal.forecast;

import java.util.List;

/** The statuses of a forecast question (prompt 122): open, closed (no more forecasts), resolved or annulled. */
public final class ForecastStatus {

    /** Accepting forecasts. */
    public static final String OPEN = "open";

    /** Past {@code closes_at}, awaiting resolution. */
    public static final String CLOSED = "closed";

    /** Resolved and scored. */
    public static final String RESOLVED = "resolved";

    /** Annulled; never scored (LED-1). */
    public static final String ANNULLED = "annulled";

    /** Every status, in lifecycle order. */
    public static final List<String> ALL = List.of(OPEN, CLOSED, RESOLVED, ANNULLED);

    private ForecastStatus() {}
}
