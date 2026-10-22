package ai.foresignal.intel;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.AnalyticFindingRepository;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastQuestionRepository;
import ai.foresignal.forecast.ForecastStatus;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Feeds resolved precursor outcomes into the intelligence calibration layer (prompt 151). A precursor-active finding
 * opens an {@code event-exists} forecast question ("will {consequent} occur in {region} between {from} and {to}?",
 * prompt 122); when that question resolves it has answered exactly "did the expected consequent occur in its window?".
 * This resolver reads those resolved questions, pairs the finding's stated probability with the binary outcome
 * ({@code yes → 1}, {@code no → 0}), and records it under the {@code precursor-active} scope. Recording is idempotent:
 * the outcome store is keyed by the question id, so a question is only ever counted once.
 *
 * <p>The scan is off when {@code foresignal.analytics.scheduled=false} (tests call {@link #scan()} directly).
 */
@Component
public class PrecursorOutcomeResolver {

    private static final Logger log = LoggerFactory.getLogger(PrecursorOutcomeResolver.class);

    private final ForecastQuestionRepository questions;
    private final AnalyticFindingRepository findings;
    private final IntelCalibrationService calibration;
    private final boolean scheduled;

    /** Creates the resolver. */
    public PrecursorOutcomeResolver(ForecastQuestionRepository questions, AnalyticFindingRepository findings,
            IntelCalibrationService calibration,
            @Value("${foresignal.analytics.scheduled:true}") boolean scheduled) {
        this.questions = questions;
        this.findings = findings;
        this.calibration = calibration;
        this.scheduled = scheduled;
    }

    /** Records outcomes from newly resolved precursor questions every 10 minutes. */
    @Scheduled(fixedDelayString = "10m", initialDelayString = "4m")
    public void scheduled() {
        if (!scheduled) {
            return;
        }
        try {
            scan();
        } catch (RuntimeException e) {
            log.warn("The precursor outcome scan failed: {}", e.toString());
        }
    }

    /**
     * Records the outcome of every resolved precursor question; returns how many outcomes were written or changed
     * (identical re-resolutions are not counted).
     */
    public int scan() {
        int written = 0;
        for (ForecastQuestionEntity q : questions.findByStatusOrderByIdAsc(ForecastStatus.RESOLVED)) {
            if (q.getPrecursorId() == null) {
                continue;
            }
            Double outcome = outcome(q);
            if (outcome == null) {
                continue;
            }
            double probability = probability(q);
            try {
                if (calibration.recordOutcome(IntelCalibrationService.SCOPE_PRECURSOR,
                        IntelOutcomeEntity.KIND_PRECURSOR, q.getId(), probability, outcome)) {
                    written++;
                }
            } catch (RuntimeException e) {
                log.warn("Recording the precursor outcome of question {} failed: {}", q.getId(), e.toString());
            }
        }
        return written;
    }

    /** The binary outcome of a resolved event-exists question ({@code yes → 1}, {@code no → 0}), or null. */
    private static Double outcome(ForecastQuestionEntity q) {
        Map<String, Object> resolution = q.getResolution();
        Object value = resolution == null ? null : resolution.get("outcome");
        if ("yes".equals(value)) {
            return 1.0;
        }
        if ("no".equals(value)) {
            return 0.0;
        }
        return null;
    }

    /**
     * The raw probability the precursor stated when the question opened, read from the finding, or 0.5 when absent.
     * The finding's {@code rawProbability} (the uncalibrated shrink-to-base-rate value, prompt 159) is used so the
     * calibration curve is always fitted on raw scores, never on a probability it already remapped; findings written
     * before 159 carry only {@code probability} (which was raw then), so that is the fallback.
     */
    private double probability(ForecastQuestionEntity q) {
        if (q.getFindingId() == null) {
            return 0.5;
        }
        AnalyticFindingEntity finding = findings.findById(q.getFindingId()).orElse(null);
        if (finding == null) {
            return 0.5;
        }
        Object value = finding.getStats().get("rawProbability");
        if (!(value instanceof Number)) {
            value = finding.getStats().get("probability");
        }
        return value instanceof Number n ? Math.max(0, Math.min(1, n.doubleValue())) : 0.5;
    }
}
