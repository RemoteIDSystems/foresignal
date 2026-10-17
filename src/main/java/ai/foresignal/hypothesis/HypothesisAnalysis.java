package ai.foresignal.hypothesis;

import ai.foresignal.collect.HealthService;
import ai.foresignal.forecast.JsonValues;
import ai.foresignal.observability.AbsenceCheck;
import ai.foresignal.observability.ObservabilityDeclarations;
import ai.foresignal.observability.ObservabilityService;
import ai.foresignal.observability.Place;
import ai.foresignal.security.Ulids;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The audit and planning views of a hypothesis set (prompt 125 §3 and §6a): the ACH matrix, expected information gain
 * per pending indicator with its 116 detection probability, collection gaps with recommendations, and the observability
 * judgments the indicators need ({@link #detect}). Recommendations never change configuration.
 */
@Service
public class HypothesisAnalysis {

    private static final Logger log = LoggerFactory.getLogger(HypothesisAnalysis.class);

    /** A gap needs a detection probability below this. */
    public static final double GAP_P_DETECT = 0.5;

    /** A gap's potential EIG ranks within this many of its set. */
    public static final int GAP_TOP = 3;

    /** The audit note of the matrix. */
    public static final String MATRIX_NOTE = "Audit view: probabilities come from the updater and are scored in the"
            + " ledger";

    private final HypothesisService service;
    private final CollectionGapRepository gaps;
    private final ObjectProvider<ObservabilityService> observability;
    private final ObjectProvider<ObservabilityDeclarations> declarations;
    private final ObjectProvider<SourceRegistry> sources;
    private final ObjectProvider<HealthService> health;
    private final Clock clock;

    /** Creates the component. */
    public HypothesisAnalysis(HypothesisService service, CollectionGapRepository gaps,
            ObjectProvider<ObservabilityService> observability, ObjectProvider<ObservabilityDeclarations> declarations,
            ObjectProvider<SourceRegistry> sources, ObjectProvider<HealthService> health, Clock clock) {
        this.service = service;
        this.gaps = gaps;
        this.observability = observability;
        this.declarations = declarations;
        this.sources = sources;
        this.health = health;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- matrix

    /** The {@code AchMatrix} of a set the caller may see. */
    public Map<String, Object> matrix(String setId, boolean seesPrivate) {
        HypothesisSetEntity set = service.get(setId, seesPrivate);
        List<HypothesisEntity> hyps = service.hypothesesOf(set.getId());
        List<String> ids = new ArrayList<>();
        for (HypothesisEntity h : hyps) {
            ids.add(h.getId());
        }
        List<AchEvidenceEntity> live = service.liveEvidence(set.getId());
        List<AchMatrix.Row> rows = new ArrayList<>();
        for (AchEvidenceEntity e : live) {
            Map<String, String> ratings = new LinkedHashMap<>();
            e.getRatings().forEach((k, v) -> ratings.put(k, v == null ? "NA" : v.toString()));
            rows.add(new AchMatrix.Row(e.getId(), ratings, e.getCredibility(), e.getRelevance()));
        }
        AchMatrix.Result result = AchMatrix.compute(ids, rows);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("setId", set.getId());
        out.put("computedAt", clock.instant().toString());
        List<Map<String, Object>> columns = new ArrayList<>();
        for (HypothesisEntity h : hyps) {
            Map<String, Object> column = new LinkedHashMap<>();
            column.put("id", h.getId());
            column.put("label", h.getLabel());
            column.put("probability", h.getProbability());
            column.put("estimativeTerm", h.getEstimativeTerm());
            column.put("inconsistencyScore", result.scores().get(h.getId()));
            columns.add(column);
        }
        out.put("hypotheses", columns);
        List<Map<String, Object>> rowViews = new ArrayList<>();
        for (AchEvidenceEntity e : live) {
            rowViews.add(HypothesisViews.evidence(e, result.nonDiagnostic().contains(e.getId())));
        }
        out.put("rows", rowViews);
        out.put("scores", result.scores());
        out.put("leastInconsistentHypothesisId", result.leastInconsistent());
        out.put("nonDiagnosticEvidenceIds", result.nonDiagnostic());
        out.put("note", MATRIX_NOTE);
        return out;
    }

    // ------------------------------------------------------------------------- observability

    /**
     * An observability judgment of an indicator.
     *
     * @param family the 116 observability family used
     * @param pDetect the detection probability, or null when it cannot be judged
     * @param sourcesAvailable false when no enabled, healthy source observes the family
     * @param absence the absence verdict over the window, or null when no window was asked
     */
    public record Detect(String family, Double pDetect, boolean sourcesAvailable, AbsenceCheck absence) {}

    /**
     * Judges an indicator's observability: the 116 family is that of its watch rule's first kind (else its signal
     * family), the place is the set's first region (else global), and the sources are the rule's (else every source
     * observing the family).
     *
     * @param indicator the indicator
     * @param set its set
     * @param from the start of the absence window, or null for detectability only
     * @param to the end of the window (the time judged)
     */
    public Detect detect(IndicatorEntity indicator, HypothesisSetEntity set, Instant from, Instant to) {
        ObservabilityService obs = observability.getIfAvailable();
        Map<String, Object> spec;
        try {
            spec = IndicatorWatchService.ruleSpec(indicator.getId(), indicator.getDescription(),
                    indicator.getSignal());
        } catch (RuntimeException e) {
            spec = null;
        }
        List<String> kinds = spec == null ? List.of() : JsonValues.strings(spec, "kinds");
        List<String> ruleSources = spec == null ? List.of() : JsonValues.strings(spec, "sources");
        ObservabilityDeclarations decl = declarations.getIfAvailable();
        String family = !kinds.isEmpty() && decl != null ? decl.familyOfKind(kinds.get(0)) : indicator.getSignalFamily();
        Place place = set.getRegionCodes().isEmpty() ? Place.GLOBAL : Place.region(set.getRegionCodes().get(0));
        if (obs == null) {
            return new Detect(family, null, true, from == null ? null : new AbsenceCheck(false, null,
                    "observability is not available"));
        }
        // Derived sources (foresignal-intel, foresignal-analytics) observe nothing themselves: judge the family.
        boolean derived = ruleSources.isEmpty() || ruleSources.stream().allMatch(s -> s.startsWith("foresignal-"));
        try {
            ObservabilityService.Detectability d = derived ? obs.pDetect(family, place, to, null)
                    : obs.pDetect(ruleSources, family, place, to, null);
            AbsenceCheck absence = null;
            if (from != null) {
                absence = derived ? obs.canSeeAbsence(family, place, from, to, null)
                        : obs.canSeeAbsence(ruleSources, family, place, from, to, null);
            }
            return new Detect(family, d.p(), !d.sources().isEmpty(), absence);
        } catch (RuntimeException e) {
            log.debug("Detectability of indicator {} could not be judged: {}", indicator.getId(), e.toString());
            return new Detect(family, null, false, from == null ? null : new AbsenceCheck(false, null,
                    "detectability could not be judged: " + e.getMessage()));
        }
    }

    // ------------------------------------------------------------------------- information value

    /** One ranked indicator. */
    private record Ranked(IndicatorEntity indicator, InformationValue.Gain gain, Detect detect) {}

    private List<Ranked> rank(HypothesisSetEntity set, List<HypothesisEntity> hyps) {
        List<String> ids = new ArrayList<>();
        Map<String, Double> probabilities = new LinkedHashMap<>();
        for (HypothesisEntity h : hyps) {
            ids.add(h.getId());
            probabilities.put(h.getId(), h.getProbability());
        }
        Instant now = clock.instant();
        List<Ranked> out = new ArrayList<>();
        for (IndicatorEntity i : service.indicatorsOf(set.getId())) {
            if (!i.waiting()) {
                continue;
            }
            Detect detect = detect(i, set, null, now);
            Map<String, Double> lr = HypothesisService.observedLr(i, hyps);
            double s = JsonValues.number(i.getSignal(), "baseRate") != null
                    ? JsonValues.number(i.getSignal(), "baseRate") : InformationValue.DEFAULT_SIGNAL_BASE_RATE;
            Double pDetect = detect.pDetect() == null ? (detect.sourcesAvailable() ? 1.0 : 0.0) : detect.pDetect();
            out.add(new Ranked(i, InformationValue.gain(set.exclusive(), ids, probabilities, lr, s, pDetect),
                    new Detect(detect.family(), pDetect, detect.sourcesAvailable(), null)));
        }
        out.sort(Comparator.comparingDouble((Ranked r) -> -r.gain().eig()).thenComparing(r -> r.indicator().getId()));
        return out;
    }

    private static Set<String> gapIds(List<Ranked> ranked) {
        List<Ranked> byPotential = new ArrayList<>(ranked);
        byPotential.sort(Comparator.comparingDouble((Ranked r) -> -r.gain().potentialEig())
                .thenComparing(r -> r.indicator().getId()));
        Set<String> out = new LinkedHashSet<>();
        for (int k = 0; k < Math.min(GAP_TOP, byPotential.size()); k++) {
            Ranked r = byPotential.get(k);
            boolean unobservable = !r.detect().sourcesAvailable() || r.detect().pDetect() == null
                    || r.detect().pDetect() < GAP_P_DETECT;
            if (unobservable && r.gain().potentialEig() > 1e-9) {
                out.add(r.indicator().getId());
            }
        }
        return out;
    }

    /** The {@code InformationValue} of a set the caller may see. */
    public Map<String, Object> informationValue(String setId, boolean seesPrivate) {
        HypothesisSetEntity set = service.get(setId, seesPrivate);
        List<HypothesisEntity> hyps = service.hypothesesOf(set.getId());
        List<Ranked> ranked = rank(set, hyps);
        Set<String> gapIds = gapIds(ranked);
        double[] p = new double[hyps.size()];
        for (int i = 0; i < hyps.size(); i++) {
            p[i] = hyps.get(i).getProbability();
        }
        double entropy = 0;
        if (set.exclusive()) {
            entropy = InformationValue.entropy(p);
        } else {
            for (double v : p) {
                entropy += InformationValue.binaryEntropy(v);
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("setId", set.getId());
        out.put("computedAt", clock.instant().toString());
        out.put("entropy", entropy);
        out.put("version", InformationValue.LABEL);
        List<Map<String, Object>> items = new ArrayList<>();
        int rank = 1;
        for (Ranked r : ranked) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("indicatorId", r.indicator().getId());
            item.put("description", r.indicator().getDescription());
            item.put("signalFamily", r.indicator().getSignalFamily());
            item.put("state", r.indicator().getState());
            item.put("rank", rank++);
            item.put("eig", r.gain().eig());
            item.put("potentialEig", r.gain().potentialEig());
            item.put("pDetect", r.detect().pDetect());
            item.put("pObserved", r.gain().pObserved());
            item.put("collectionGap", gapIds.contains(r.indicator().getId()));
            items.add(item);
        }
        out.put("indicators", items);
        return out;
    }

    // ------------------------------------------------------------------------- collection gaps

    /**
     * Recomputes a set's collection gaps: adds or refreshes a row for each qualifying indicator and deletes the rows
     * of indicators that no longer qualify.
     *
     * @return the number of gaps
     */
    @Transactional
    public int refreshGaps(String setId) {
        HypothesisSetEntity set = service.find(setId).orElse(null);
        if (set == null) {
            return 0;
        }
        Instant now = clock.instant();
        List<Ranked> ranked = set.active() ? rank(set, service.hypothesesOf(setId)) : List.of();
        Set<String> qualifying = gapIds(ranked);
        Map<String, Ranked> byId = new HashMap<>();
        for (Ranked r : ranked) {
            byId.put(r.indicator().getId(), r);
        }
        for (CollectionGapEntity gap : gaps.findBySetIdOrderByIdAsc(setId)) {
            if (!qualifying.contains(gap.getIndicatorId())) {
                gaps.delete(gap);
            }
        }
        for (String id : qualifying) {
            Ranked r = byId.get(id);
            CollectionGapEntity gap = gaps.findByIndicatorId(id).orElseGet(() -> new CollectionGapEntity(
                    Ulids.next(now), setId, id, now));
            gap.setPotentialEig(r.gain().potentialEig());
            gap.setActualEig(r.gain().eig());
            gap.setPDetect(r.detect().pDetect());
            gap.setRecommendations(recommendations(r.detect().family()));
            gap.setUpdatedAt(now);
            gaps.save(gap);
        }
        return qualifying.size();
    }

    /**
     * The recommendations for an unobservable family: cataloged sources observing it that are not healthy (enable
     * them), a local receiver or sensor family when one fits (069 receivers, 078 environment sensors, 086 local RF
     * and seismic decoders), or "no cataloged source observes this".
     */
    List<Map<String, Object>> recommendations(String family) {
        List<Map<String, Object>> out = new ArrayList<>();
        SourceRegistry registry = sources.getIfAvailable();
        ObservabilityDeclarations decl = declarations.getIfAvailable();
        HealthService healthService = health.getIfAvailable();
        if (registry != null && decl != null && family != null) {
            Set<String> kinds = decl.kindsOf(family);
            for (SourceDescriptor descriptor : registry.descriptors()) {
                if (out.size() >= 5 || descriptor.kinds().stream().noneMatch(kinds::contains)) {
                    continue;
                }
                HealthStatus status;
                try {
                    status = healthService == null ? HealthStatus.IDLE : healthService.health(descriptor.id())
                            .status();
                } catch (RuntimeException e) {
                    status = HealthStatus.IDLE;
                }
                if (status != HealthStatus.HEALTHY) {
                    out.add(recommendation("enable-source", descriptor.id(), null, "Enable or repair the cataloged"
                            + " source " + descriptor.id() + ", which observes " + family + " (an operator action)."));
                }
            }
        }
        String local = localFamily(family);
        if (local != null) {
            String[] parts = local.split("\\|");
            out.add(recommendation(parts[0], null, parts[1], parts[2]));
        }
        if (out.isEmpty()) {
            out.add(recommendation("none", null, null, "No cataloged source observes this."));
        }
        return out;
    }

    private static String localFamily(String family) {
        if (family == null) {
            return null;
        }
        String f = family.toLowerCase(java.util.Locale.ROOT);
        if (f.contains("aircraft") || f.contains("aviation") || f.contains("vessel") || f.contains("ais")) {
            return "add-receiver|069|Add a local ADS-B or AIS receiver (prompt 069 local receivers).";
        }
        if (f.contains("air") || f.contains("radiation") || f.contains("environment") || f.contains("pollut")) {
            return "add-sensor|078|Add a local environment sensor (prompt 078 environment sensors).";
        }
        if (f.contains("seismic") || f.contains("gnss") || f.contains("radio") || f.contains("rf")) {
            return "add-sensor|086|Add a local decoder or sensor (prompt 086 local RF and seismic decoders).";
        }
        return null;
    }

    private static Map<String, Object> recommendation(String type, String sourceId, String sensorFamily, String text) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", type);
        out.put("sourceId", sourceId);
        out.put("sensorFamily", sensorFamily);
        out.put("text", text);
        return out;
    }

    /** Collection gaps newest first, filtered by set; gaps of private sets need private access. */
    public HypothesisService.Page<CollectionGapEntity> listGaps(String setId, String cursor, int limit,
            boolean seesPrivate) {
        if (setId != null) {
            service.get(setId, seesPrivate);
        }
        String after = HypothesisService.decodeCursor(cursor);
        List<CollectionGapEntity> all = setId != null ? gaps.findBySetIdOrderByIdAsc(setId)
                : gaps.findAllByOrderByIdAsc();
        List<CollectionGapEntity> visible = new ArrayList<>();
        Map<String, Boolean> privateBySet = new HashMap<>();
        for (int k = all.size() - 1; k >= 0; k--) {
            CollectionGapEntity gap = all.get(k);
            if (after != null && gap.getId().compareTo(after) >= 0) {
                continue;
            }
            boolean hidden = privateBySet.computeIfAbsent(gap.getSetId(), id -> service.find(id)
                    .map(HypothesisSetEntity::isPrivateSet).orElse(true));
            if (hidden && !seesPrivate) {
                continue;
            }
            visible.add(gap);
            if (visible.size() > limit) {
                break;
            }
        }
        String next = null;
        if (visible.size() > limit) {
            visible = visible.subList(0, limit);
            next = HypothesisService.encodeCursor(visible.get(visible.size() - 1).getId());
        }
        return new HypothesisService.Page<>(visible, next);
    }
}
