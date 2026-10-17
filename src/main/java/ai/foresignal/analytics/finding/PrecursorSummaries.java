package ai.foresignal.analytics.finding;

import java.util.Map;

/**
 * Resolves the summary of a precursor for the {@code precursor.*} events of the {@code analytics} topic (prompt 120),
 * so that {@link FindingService} can replay them from {@code analytic_finding_log} without depending on the precursor
 * package.
 */
public interface PrecursorSummaries {

    /**
     * The precursor summary of the {@code PrecursorEvent} schema.
     *
     * @param precursorId the precursor id
     * @return the summary, or null when the precursor no longer exists
     */
    Map<String, Object> summary(String precursorId);
}
