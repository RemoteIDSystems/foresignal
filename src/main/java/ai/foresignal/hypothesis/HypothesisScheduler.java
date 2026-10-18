package ai.foresignal.hypothesis;

import ai.foresignal.observability.AbsenceCheck;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The schedule of rolling hypotheses (prompt 125), UTC: every 15 minutes the indicators whose {@code expected_by}
 * passed are judged ({@code absent} with coverage, else {@code unknown-coverage}) and waiting band-change
 * notifications whose rate limit passed are sent; hourly the collection gaps are refreshed; daily at 02:50 the base
 * rates behind priors are re-read. Reviews themselves belong to prompt 128's cycles. {@code
 * foresignal.analytics.scheduled=false} turns the schedule off (tests call the methods directly).
 */
@Component
public class HypothesisScheduler {

    private static final Logger log = LoggerFactory.getLogger(HypothesisScheduler.class);

    /** Indicators judged per pass. */
    static final int BATCH = 200;

    private final HypothesisService service;
    private final HypothesisAnalysis analysis;
    private final RevisionWriter writer;
    private final IndicatorRepository indicators;
    private final HypothesisSetRepository sets;
    private final Clock clock;
    private final boolean scheduled;

    /** Creates the scheduler. */
    public HypothesisScheduler(HypothesisService service, HypothesisAnalysis analysis, RevisionWriter writer,
            IndicatorRepository indicators, HypothesisSetRepository sets, Clock clock,
            @Value("${foresignal.analytics.scheduled:true}") boolean scheduled) {
        this.service = service;
        this.analysis = analysis;
        this.writer = writer;
        this.indicators = indicators;
        this.sets = sets;
        this.clock = clock;
        this.scheduled = scheduled;
    }

    /** The 15-minute pass. */
    @Scheduled(fixedDelayString = "15m", initialDelayString = "4m")
    public void quarterHour() {
        if (!scheduled) {
            return;
        }
        run("expected-by check", this::checkExpected);
        run("band-change flush", this::flushBandChanges);
    }

    /** The hourly pass. */
    @Scheduled(cron = "0 25 * * * *", zone = "UTC")
    public void hourly() {
        if (scheduled) {
            run("collection gaps", this::refreshGaps);
        }
    }

    /** The daily pass. */
    @Scheduled(cron = "0 50 2 * * *", zone = "UTC")
    public void daily() {
        if (scheduled) {
            run("base-rate refresh", this::refreshBaseRates);
        }
    }

    /**
     * Judges the waiting indicators whose {@code expected_by} passed: with 116 {@code canSeeAbsence} over the window
     * from the indicator's creation to {@code expected_by} the indicator is {@code absent} (the updater applies its
     * absence LRs scaled by {@code p_detect}); without it {@code unknown-coverage} and no update.
     *
     * @return how many indicators were judged
     */
    public int checkExpected() {
        Instant now = clock.instant();
        List<IndicatorEntity> due = indicators.findByStateInAndExpectedByLessThanEqual(
                HypothesisVocabulary.WAITING_STATES, now, Limit.of(BATCH));
        int n = 0;
        for (IndicatorEntity i : due) {
            HypothesisSetEntity set = service.find(i.getSetId()).orElse(null);
            if (set == null || !set.active()) {
                continue;
            }
            Instant from = i.getCreatedAt().isBefore(i.getExpectedBy()) ? i.getCreatedAt()
                    : i.getExpectedBy().minusSeconds(3600);
            HypothesisAnalysis.Detect detect = analysis.detect(i, set, from, i.getExpectedBy());
            AbsenceCheck absence = detect.absence();
            try {
                if (absence != null && absence.ok()) {
                    double p = absence.p() != null ? absence.p() : detect.pDetect() != null ? detect.pDetect() : 1.0;
                    service.absent(i.getId(), p, null);
                } else {
                    service.unknownCoverage(i.getId(), absence == null ? detect.pDetect() : absence.p(),
                            absence == null ? "observability is not available" : absence.reason());
                }
                n++;
            } catch (RuntimeException e) {
                log.warn("Indicator {} could not be judged at expected_by: {}", i.getId(), e.toString());
            }
        }
        return n;
    }

    /** Sends the waiting band-change notifications whose rate limit passed; returns how many were sent. */
    public int flushBandChanges() {
        Instant now = clock.instant();
        int n = 0;
        for (HypothesisSetEntity set : sets.findByPendingBandChangeIsNotNull(Limit.of(500))) {
            try {
                if (writer.flushPendingBandChange(set, now)) {
                    n++;
                }
            } catch (RuntimeException e) {
                log.warn("Band change of set {} could not be sent: {}", set.getId(), e.toString());
            }
        }
        return n;
    }

    /** Refreshes the collection gaps of every active set; returns the number of gaps. */
    public int refreshGaps() {
        int n = 0;
        for (HypothesisSetEntity set : sets.findByStatusOrderByIdAsc(HypothesisVocabulary.SET_ACTIVE,
                Limit.of(5000))) {
            try {
                n += analysis.refreshGaps(set.getId());
            } catch (RuntimeException e) {
                log.warn("Collection gaps of set {} failed: {}", set.getId(), e.toString());
            }
        }
        return n;
    }

    /** Re-reads the base rates behind the priors of every active set; returns how many sets were revised. */
    public int refreshBaseRates() {
        int n = 0;
        for (HypothesisSetEntity set : sets.findByStatusOrderByIdAsc(HypothesisVocabulary.SET_ACTIVE,
                Limit.of(5000))) {
            try {
                if (service.refreshBaseRates(set.getId())) {
                    n++;
                }
            } catch (RuntimeException e) {
                log.warn("Base-rate refresh of set {} failed: {}", set.getId(), e.toString());
            }
        }
        return n;
    }

    private static void run(String what, java.util.function.IntSupplier task) {
        try {
            int n = task.getAsInt();
            if (n > 0) {
                log.info("Hypotheses {}: {}", what, n);
            }
        } catch (RuntimeException e) {
            log.warn("Hypotheses {} failed: {}", what, e.toString());
        }
    }
}
