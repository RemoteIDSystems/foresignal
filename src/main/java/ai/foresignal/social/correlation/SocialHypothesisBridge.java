package ai.foresignal.social.correlation;

import ai.foresignal.hypothesis.HypothesisService;
import ai.foresignal.hypothesis.HypothesisSetEntity;
import ai.foresignal.hypothesis.HypothesisSetRepository;
import ai.foresignal.hypothesis.HypothesisVocabulary;
import ai.foresignal.hypothesis.IndicatorEntity;
import ai.foresignal.hypothesis.IndicatorRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

/**
 * How comparisons update 125 indicators (prompt 137). An indicator is concerned by a claim when its set is active and
 * linked to one of the narrative's situations or shares the claim's region (or its country), and its signal family is
 * the claim's.
 *
 * <ul>
 *   <li>A compared claim marks waiting indicators {@code reported}: no probability moves (a report is not an
 *       observation).</li>
 *   <li>{@code corroborated}: the indicators reported by this comparison are {@code observed} from the matching
 *       physical evidence, never from the claim.</li>
 *   <li>{@code unobserved} under adequate coverage: the indicators reported by this comparison are {@code absent},
 *       weighted by detectability ({@code pDetect}), so hypotheses that require the claimed event lose support.</li>
 * </ul>
 */
@Component
public class SocialHypothesisBridge {

    private static final Logger log = LoggerFactory.getLogger(SocialHypothesisBridge.class);

    /** The most active sets read. */
    static final int SETS = 500;

    private final ObjectProvider<HypothesisService> hypotheses;
    private final ObjectProvider<HypothesisSetRepository> sets;
    private final ObjectProvider<IndicatorRepository> indicators;

    /** Creates the bridge. */
    public SocialHypothesisBridge(ObjectProvider<HypothesisService> hypotheses,
            ObjectProvider<HypothesisSetRepository> sets, ObjectProvider<IndicatorRepository> indicators) {
        this.hypotheses = hypotheses;
        this.sets = sets;
        this.indicators = indicators;
    }

    /** True when a set concerns a claim of {@code regionCode} in a narrative linked to {@code situationIds}. */
    static boolean concerns(HypothesisSetEntity set, List<String> situationIds, String regionCode) {
        if (set.getSituationId() != null && situationIds.contains(set.getSituationId())) {
            return true;
        }
        if (regionCode == null) {
            return false;
        }
        for (String r : set.getRegionCodes()) {
            if (r.equals(regionCode) || (regionCode.length() > 2 && r.equals(regionCode.substring(0, 2)))) {
                return true;
            }
        }
        return false;
    }

    /** The indicators a comparison concerns. */
    List<IndicatorEntity> concerned(ComparisonStore.Comparison c, List<String> situationIds) {
        HypothesisSetRepository setRepo = sets.getIfAvailable();
        IndicatorRepository indicatorRepo = indicators.getIfAvailable();
        if (setRepo == null || indicatorRepo == null) {
            return List.of();
        }
        List<IndicatorEntity> out = new ArrayList<>();
        for (HypothesisSetEntity set : setRepo.findByStatusOrderByIdAsc(HypothesisVocabulary.SET_ACTIVE,
                Limit.of(SETS))) {
            if (!concerns(set, situationIds, c.regionCode())) {
                continue;
            }
            for (IndicatorEntity i : indicatorRepo.findBySetIdOrderByIdAsc(set.getId())) {
                if (Objects.equals(i.getSignalFamily(), c.signalFamily())) {
                    out.add(i);
                }
            }
        }
        return out;
    }

    /** True when an indicator was reported by this comparison. */
    static boolean reportedBy(IndicatorEntity i, String comparisonId) {
        return HypothesisVocabulary.REPORTED.equals(i.getState()) && i.getObservedEvidence() != null
                && comparisonId.equals(String.valueOf(i.getObservedEvidence().get("comparisonId")));
    }

    /**
     * Applies a comparison to the indicators it concerns.
     *
     * @param c the comparison (after evaluation)
     * @param situationIds the narrative's situations
     * @param evidence the matching physical evidence
     * @param detectability detectability over the window
     * @return the indicators changed
     */
    public int apply(ComparisonStore.Comparison c, List<String> situationIds,
            List<PhysicalEvidenceSource.Evidence> evidence, SocialPhysicalComparator.Detectability detectability) {
        HypothesisService service = hypotheses.getIfAvailable();
        if (service == null) {
            return 0;
        }
        int changed = 0;
        for (IndicatorEntity i : concerned(c, situationIds)) {
            try {
                if (HypothesisVocabulary.PENDING.equals(i.getState())
                        || HypothesisVocabulary.EMERGING.equals(i.getState())) {
                    Map<String, Object> claim = new LinkedHashMap<>();
                    claim.put("source", "social-claim");
                    claim.put("ref", "claim:" + c.claimId());
                    claim.put("comparisonId", c.id());
                    claim.put("narrativeId", c.narrativeId());
                    if (service.reported(i.getId(), claim)) {
                        changed++;
                        i.setState(HypothesisVocabulary.REPORTED, c.evaluatedAt());
                        i.setObservedEvidence(claim);
                    }
                }
                if (!reportedBy(i, c.id())) {
                    continue;
                }
                if (CorrelationVocabulary.CORROBORATED.equals(c.status())) {
                    PhysicalEvidenceSource.Evidence first = evidence.stream().filter(e -> !e.negates() && !e.social())
                            .findFirst().orElse(null);
                    if (first != null) {
                        Map<String, Object> physical = new LinkedHashMap<>();
                        physical.put("source", first.ref().startsWith("finding:") ? "finding" : "record");
                        physical.put("ref", first.ref());
                        physical.put("originator", first.originator());
                        physical.put("at", first.firstObservedAt() == null ? null : first.firstObservedAt().toString());
                        physical.put("comparisonId", c.id());
                        service.observe(i.getId(), physical, HypothesisVocabulary.OBSERVED);
                        changed++;
                    }
                } else if (CorrelationVocabulary.UNOBSERVED.equals(c.status()) && detectability != null
                        && detectability.ok()) {
                    service.absent(i.getId(), detectability.p(), "social-comparison:" + c.id());
                    changed++;
                }
            } catch (RuntimeException e) {
                log.warn("Indicator {} could not take comparison {}: {}", i.getId(), c.id(), e.toString());
            }
        }
        return changed;
    }
}
