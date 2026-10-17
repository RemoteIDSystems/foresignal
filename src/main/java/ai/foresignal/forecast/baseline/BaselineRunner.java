package ai.foresignal.forecast.baseline;

import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastInputs.ForecastValue;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastQuestionRepository;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecastStatus;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Runs the baseline forecasters (prompt 122 §4) on open questions: on every new question, and again at most daily
 * when their inputs changed. A forecaster that cannot forecast a question it supports stores why in the question's
 * {@code baselineNotes}; a re-forecast with the same value and inputs writes nothing.
 */
@Service
public class BaselineRunner {

    private static final Logger log = LoggerFactory.getLogger(BaselineRunner.class);

    /** The shortest interval between two forecasts of one baseline on one question. */
    public static final Duration MIN_INTERVAL = Duration.ofDays(1);

    private final List<BaselineForecaster> baselines;
    private final ForecasterRegistry registry;
    private final ForecastService ledger;
    private final ForecastQuestionRepository questions;
    private final TransactionTemplate transactions;
    private final Clock clock;
    private final ReentrantLock lock = new ReentrantLock();

    /** Creates the runner. */
    public BaselineRunner(List<BaselineForecaster> baselines, ForecasterRegistry registry, ForecastService ledger,
            ForecastQuestionRepository questions, TransactionTemplate transactions, Clock clock) {
        this.baselines = List.copyOf(baselines);
        this.registry = registry;
        this.ledger = ledger;
        this.questions = questions;
        this.transactions = transactions;
        this.clock = clock;
    }

    /** The baseline forecasters. */
    public List<BaselineForecaster> baselines() {
        return baselines;
    }

    /** The baseline with {@code id}, or null. */
    public BaselineForecaster baseline(String id) {
        return baselines.stream().filter(b -> b.id().equals(id)).findFirst().orElse(null);
    }

    /** Runs every baseline on one question; returns how many forecasts were written. */
    public int runFor(String questionId) {
        lock.lock();
        try {
            int written = 0;
            for (BaselineForecaster b : baselines) {
                try {
                    Boolean wrote = transactions.execute(status -> runOne(questionId, b));
                    if (Boolean.TRUE.equals(wrote)) {
                        written++;
                    }
                } catch (RuntimeException e) {
                    log.warn("Baseline {} on question {} failed: {}", b.id(), questionId, e.toString());
                    transactions.executeWithoutResult(status -> ledger.noteBaseline(questionId, b.id(),
                            "failed: " + e.getMessage()));
                }
            }
            return written;
        } finally {
            lock.unlock();
        }
    }

    /** Runs every baseline on every open question; returns how many forecasts were written. */
    public int runAll() {
        int written = 0;
        for (ForecastQuestionEntity q : questions.findByStatusInOrderByResolveByAsc(List.of(ForecastStatus.OPEN))) {
            written += runFor(q.getId());
        }
        return written;
    }

    private boolean runOne(String questionId, BaselineForecaster b) {
        ForecastQuestionEntity q = questions.findById(questionId).orElse(null);
        Instant now = clock.instant();
        if (q == null || !q.acceptsForecasts(now) || !b.supports(q)) {
            return false;
        }
        ForecasterEntity forecaster = registry.find(b.id()).orElse(null);
        if (forecaster == null || !forecaster.isActive()) {
            return false;
        }
        ForecastEntity latest = ledger.latestByForecaster(q.getId()).get(b.id());
        if (latest != null && latest.getMadeAt().isAfter(now.minus(MIN_INTERVAL))) {
            return false;
        }
        BaselineForecaster.Outcome outcome = b.forecast(q, now, false);
        if (outcome.skipped()) {
            ledger.noteBaseline(q.getId(), b.id(), outcome.skipReason());
            return false;
        }
        ledger.noteBaseline(q.getId(), b.id(), null);
        if (latest != null && same(latest, outcome.value()) && sameInputs(latest.getInputs(), outcome.inputs())) {
            return false;
        }
        ledger.record(q, forecaster, outcome.value(), outcome.rationale(), List.of(), List.of(), outcome.inputs(),
                null, false);
        return true;
    }

    /** True when a stored forecast has the same value. */
    static boolean same(ForecastEntity f, ForecastValue v) {
        return Objects.equals(f.getProbability(), v.probability())
                && Objects.equals(f.getDistribution(), v.distribution())
                && Objects.equals(f.getQuantiles(), v.quantiles());
    }

    /** True when two input maps agree apart from their {@code asOf}, {@code asOfSeq} and {@code manifestId}. */
    static boolean sameInputs(Map<String, Object> a, Map<String, Object> b) {
        Map<String, Object> x = new java.util.LinkedHashMap<>(a == null ? Map.of() : a);
        Map<String, Object> y = new java.util.LinkedHashMap<>(b == null ? Map.of() : b);
        for (String volatileKey : List.of("asOf", "asOfSeq", "manifestId")) {
            x.remove(volatileKey);
            y.remove(volatileKey);
        }
        return String.valueOf(x).equals(String.valueOf(y));
    }
}
