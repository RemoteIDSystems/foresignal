package ai.foresignal.forecast.ensemble;

/**
 * Counts the resolved linked questions of external (EDP) forecasters (prompt 138): questions the forecaster
 * forecast through a question link or a {@code questionRef} item that have since been scored. The ensemble admits an
 * external forecaster only from {@code externalMinResolved} (30) such questions. The binding package provides the
 * bean; without it the ensemble counts every scored question of the forecaster.
 */
public interface ExternalForecasterCounts {

    /** True when this counter answers for {@code forecasterId} ({@code edp:{sourceId}:{streamId}}). */
    boolean handles(String forecasterId);

    /** The resolved linked questions of {@code forecasterId}. */
    int resolvedLinked(String forecasterId);
}
