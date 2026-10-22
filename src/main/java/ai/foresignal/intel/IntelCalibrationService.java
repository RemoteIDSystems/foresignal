package ai.foresignal.intel;

import ai.foresignal.forecast.score.Calibration;
import ai.foresignal.security.Ulids;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The intelligence calibration layer (prompt 151): the outcome → calibration machinery that turns the uncalibrated
 * {@link EventScorer} confidence into a calibrated probability. Resolved real-world outcomes are recorded per scope
 * ({@link IntelOutcomeEntity}); on each new outcome the scope is refitted with the shared
 * {@link ai.foresignal.forecast.score.Calibration} PAV/CORP machinery, producing a recalibration curve, a 10-bin
 * reliability table and the Brier score ({@link IntelCalibrationEntity}). A score is mapped to a calibrated
 * probability by interpolating the fitted curve, but only once a scope has at least
 * {@link Calibration#CORP_MIN} resolved outcomes; below that the raw score stands and {@code calibrated} is false.
 */
@Service
public class IntelCalibrationService {

    /** The calibration scope of intelligence events. */
    public static final String SCOPE_EVENT = "intel-event";

    /** The calibration scope of precursor-active findings. */
    public static final String SCOPE_PRECURSOR = "precursor-active";

    private final IntelOutcomeRepository outcomes;
    private final IntelCalibrationRepository calibrations;
    private final Clock clock;

    /** Creates the service. */
    public IntelCalibrationService(IntelOutcomeRepository outcomes, IntelCalibrationRepository calibrations,
            Clock clock) {
        this.outcomes = outcomes;
        this.calibrations = calibrations;
        this.clock = clock;
    }

    /**
     * Records one resolved outcome and refits its scope. Re-resolving the same subject updates its row in place (the
     * {@code (subjectKind, subjectId)} pair is unique), so a subject never double-counts.
     *
     * @param scope the calibration scope ({@link #SCOPE_EVENT}, {@link #SCOPE_PRECURSOR})
     * @param subjectKind {@link IntelOutcomeEntity#KIND_EVENT} or {@link IntelOutcomeEntity#KIND_PRECURSOR}
     * @param subjectId the event id, or the resolved question id
     * @param rawScore the uncalibrated score at the time the subject was scored
     * @param outcome 1 when the real-world outcome happened, 0 when it did not
     * @return true when a row was written or changed (false for an identical re-resolution)
     */
    @Transactional
    public boolean recordOutcome(String scope, String subjectKind, String subjectId, double rawScore, double outcome) {
        double binary = outcome >= 0.5 ? 1 : 0;
        Instant now = clock.instant();
        Optional<IntelOutcomeEntity> existing = outcomes.findBySubjectKindAndSubjectId(subjectKind, subjectId);
        if (existing.isPresent()) {
            IntelOutcomeEntity row = existing.get();
            if (row.getOutcome() == binary && row.getRawScore() == rawScore && scope.equals(row.getScope())) {
                return false;
            }
            row.setScope(scope);
            row.setRawScore(rawScore);
            row.setOutcome(binary);
            row.setResolvedAt(now);
            outcomes.save(row);
        } else {
            outcomes.save(new IntelOutcomeEntity(Ulids.next(now), scope, subjectKind, subjectId, rawScore, binary,
                    now));
        }
        refit(scope);
        return true;
    }

    /** Refits one scope from its resolved outcomes. */
    @Transactional
    public void refit(String scope) {
        List<IntelOutcomeEntity> rows = outcomes.findByScopeOrderByResolvedAtAsc(scope);
        Instant now = clock.instant();
        IntelCalibrationEntity row = calibrations.findById(scope)
                .orElseGet(() -> new IntelCalibrationEntity(scope, now));
        int n = rows.size();
        if (n == 0) {
            row.setFit(List.of(), List.of(), null, 0, false, now);
            calibrations.save(row);
            return;
        }
        double[] p = new double[n];
        double[] o = new double[n];
        for (int i = 0; i < n; i++) {
            p[i] = clamp01(rows.get(i).getRawScore());
            o[i] = rows.get(i).getOutcome();
        }
        Calibration.Corp corp = Calibration.corp(p, o);
        Calibration.Murphy murphy = Calibration.murphy(p, o);
        List<Map<String, Object>> curve = new ArrayList<>();
        if (corp != null) {
            for (Calibration.Point point : corp.curve()) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("forecast", point.forecast());
                entry.put("calibrated", point.calibrated());
                curve.add(entry);
            }
        }
        List<Map<String, Object>> bins = new ArrayList<>();
        for (Calibration.Bin bin : Calibration.bins(p, o)) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("lower", bin.lower());
            entry.put("upper", bin.upper());
            entry.put("n", bin.n());
            entry.put("meanForecast", bin.meanForecast());
            entry.put("observedFrequency", bin.observedFrequency());
            bins.add(entry);
        }
        boolean calibrated = n >= Calibration.CORP_MIN && !curve.isEmpty();
        row.setFit(curve, bins, murphy == null ? null : murphy.brier(), n, calibrated, now);
        calibrations.save(row);
    }

    /**
     * Maps a raw score to a calibrated probability by interpolating the scope's fitted curve, or empty when the scope
     * is not yet calibrated.
     *
     * @param scope the calibration scope
     * @param rawScore the uncalibrated score
     * @return the calibrated probability, or empty
     */
    @Transactional(readOnly = true)
    public Optional<Double> calibrate(String scope, double rawScore) {
        IntelCalibrationEntity row = calibrations.findById(scope).orElse(null);
        if (row == null || !row.isCalibrated() || row.getCurve().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(interpolate(row.getCurve(), clamp01(rawScore)));
    }

    /**
     * The calibration state of a scope for the admin app: whether it is calibrated, the sample size, the Brier score,
     * the reliability table and the recalibration curve.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> reliability(String scope) {
        IntelCalibrationEntity row = calibrations.findById(scope).orElse(null);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("scope", scope);
        out.put("minSamples", Calibration.CORP_MIN);
        if (row == null) {
            out.put("calibrated", false);
            out.put("sampleN", 0);
            out.put("brier", null);
            out.put("bins", List.of());
            out.put("curve", List.of());
            out.put("fittedAt", null);
            out.put("updatedAt", null);
            return out;
        }
        out.put("calibrated", row.isCalibrated());
        out.put("sampleN", row.getSampleN());
        out.put("brier", row.getBrier());
        out.put("bins", row.getBins());
        out.put("curve", row.getCurve());
        out.put("fittedAt", row.getFittedAt());
        out.put("updatedAt", row.getUpdatedAt());
        return out;
    }

    /** The calibration state of every scope. */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> all() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (IntelCalibrationEntity row : calibrations.findAll()) {
            out.add(reliability(row.getScope()));
        }
        return out;
    }

    /**
     * Linear interpolation of {@code x} over a curve of {@code {forecast, calibrated}} points in forecast order;
     * clamped to the end points outside the curve's range.
     */
    static double interpolate(List<Map<String, Object>> curve, double x) {
        double prevF = Double.NaN;
        double prevC = Double.NaN;
        for (Map<String, Object> point : curve) {
            double f = number(point.get("forecast"));
            double c = number(point.get("calibrated"));
            if (Double.isNaN(prevF)) {
                if (x <= f) {
                    return clamp01(c);
                }
            } else if (x <= f) {
                double span = f - prevF;
                double t = span <= 0 ? 0 : (x - prevF) / span;
                return clamp01(prevC + t * (c - prevC));
            }
            prevF = f;
            prevC = c;
        }
        return clamp01(prevC);
    }

    private static double number(Object value) {
        return value instanceof Number n ? n.doubleValue() : 0;
    }

    private static double clamp01(double value) {
        return Math.max(0, Math.min(1, value));
    }
}
