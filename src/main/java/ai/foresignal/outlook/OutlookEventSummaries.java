package ai.foresignal.outlook;

import ai.foresignal.analytics.finding.AnalyticsEventSummaries;
import ai.foresignal.analytics.finding.FindingEvent;
import ai.foresignal.outlook.index.RegionalIndexDefEntity;
import ai.foresignal.outlook.index.RegionalIndexDefRepository;
import ai.foresignal.outlook.index.RegionalIndexFitEntity;
import ai.foresignal.outlook.index.RegionalIndexFitRepository;
import ai.foresignal.outlook.index.RegionalIndexValueEntity;
import ai.foresignal.outlook.index.RegionalIndexValueRepository;
import ai.foresignal.outlook.regional.OutlookViews;
import ai.foresignal.outlook.regional.RegionalOutlookRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The payloads of the prompt 126 events of the {@code analytics} SSE topic: {@code outlook.published}
 * ({@code OutlookPublishedEvent}, from the outlook), {@code index.changed} ({@code IndexChangedEvent}, from the index
 * value and the previous day's) and {@code index.weights_proposed} ({@code IndexWeightsProposedEvent}, from the fit).
 */
@Component
public class OutlookEventSummaries implements AnalyticsEventSummaries {

    private final RegionalOutlookRepository outlooks;
    private final RegionalIndexValueRepository values;
    private final RegionalIndexDefRepository defs;
    private final RegionalIndexFitRepository fits;

    /** Creates the resolver. */
    public OutlookEventSummaries(RegionalOutlookRepository outlooks, RegionalIndexValueRepository values,
            RegionalIndexDefRepository defs, RegionalIndexFitRepository fits) {
        this.outlooks = outlooks;
        this.values = values;
        this.defs = defs;
        this.fits = fits;
    }

    @Override
    public boolean handles(String event) {
        return FindingEvent.flat(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> summary(String event, String objectId) {
        return switch (event) {
            case FindingEvent.OUTLOOK_PUBLISHED -> outlooks.findById(objectId).map(OutlookViews::published)
                    .orElse(null);
            case FindingEvent.INDEX_CHANGED -> values.findById(objectId).map(this::changed).orElse(null);
            case FindingEvent.INDEX_WEIGHTS_PROPOSED -> fits.findById(objectId).map(OutlookEventSummaries::proposed).orElse(null);
            default -> null;
        };
    }

    /** The {@code IndexChangedEvent} payload of a value. */
    Map<String, Object> changed(RegionalIndexValueEntity v) {
        Double previous = values.findByDefIdAndRegionAndDay(v.getDefId(), v.getRegion(), v.getDay().minusDays(1))
                .map(RegionalIndexValueEntity::getValue).orElse(null);
        return changed(v, previous, defs.findById(v.getDefId()).map(RegionalIndexDefEntity::getWeightsOrigin)
                .orElse("default"));
    }

    /** The {@code IndexChangedEvent} payload of a value given the previous day's value and the weights origin. */
    public static Map<String, Object> changed(RegionalIndexValueEntity v, Double previous, String weightsOrigin) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("indexId", v.getDefId());
        out.put("region", v.getRegion());
        out.put("day", v.getDay().toString());
        out.put("value", v.getValue());
        out.put("previousValue", previous);
        out.put("delta24h", v.getDelta24h());
        out.put("delta7d", v.getDelta7d());
        out.put("weightsOrigin", weightsOrigin);
        return out;
    }

    /** The {@code IndexWeightsProposedEvent} payload of a fit, or null without proposed weights. */
    public static Map<String, Object> proposed(RegionalIndexFitEntity f) {
        if (f.getProposedWeights() == null || f.getWeightsVersion() == null) {
            return null;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("indexId", f.getIndexId());
        out.put("fitId", f.getId());
        out.put("weightsVersion", f.getWeightsVersion());
        out.put("proposedWeights", f.getProposedWeights());
        out.put("skill", f.getSkill());
        out.put("autoApplied", f.isApplied());
        return out;
    }
}
