package ai.foresignal.forecast;

/**
 * The {@code forecasts} SSE topic filter (prompt 122): whether the caller may see private questions (EGR-1).
 *
 * @param privateAccess whether the caller may see private questions
 */
public record ForecastFilter(boolean privateAccess) {

    /** True when an event of a question with {@code privateQuestion} passes this filter. */
    public boolean matches(boolean privateQuestion) {
        return !privateQuestion || privateAccess;
    }
}
