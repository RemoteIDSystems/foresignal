package ai.foresignal.impact;

import ai.foresignal.analytics.finding.AnalyticFindingRepository;
import ai.foresignal.analytics.finding.AnalyticsEventSummaries;
import ai.foresignal.analytics.finding.FindingEvent;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The payloads of the prompt 127 events of the {@code analytics} SSE topic: {@code impact.projected} and
 * {@code impact.superseded} ({@code ImpactProjectionEvent}, from the {@code projected-impact} finding). A private
 * finding has no payload, so no event is sent for it.
 */
@Component
public class ImpactEventSummaries implements AnalyticsEventSummaries {

    private final AnalyticFindingRepository findings;

    /** Creates the resolver. */
    public ImpactEventSummaries(AnalyticFindingRepository findings) {
        this.findings = findings;
    }

    @Override
    public boolean handles(String event) {
        return FindingEvent.IMPACT_PROJECTED.equals(event) || FindingEvent.IMPACT_SUPERSEDED.equals(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> summary(String event, String objectId) {
        // A projection from a private hazard source sends no stream event (flat events are never private).
        return findings.findById(objectId).filter(f -> ImpactProjectionService.FINDING_TYPE.equals(f.getFindingType())
                && !f.isPrivateFinding()).map(f -> ImpactViews.event(f, f.getUpdatedAt())).orElse(null);
    }
}
