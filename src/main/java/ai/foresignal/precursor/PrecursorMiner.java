package ai.foresignal.precursor;

import ai.foresignal.analytics.AnalyticsSupport;
import ai.foresignal.analytics.finding.FindingCaveats;
import ai.foresignal.analytics.finding.FindingEvent;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.analytics.math.Seeds;
import ai.foresignal.analytics.registry.AnalysisSeriesEntity;
import ai.foresignal.analytics.registry.AnalysisSeriesRepository;
import ai.foresignal.analytics.series.SeriesData;
import ai.foresignal.analytics.series.SeriesFrequency;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.backtest.BacktestCoverage;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.backtest.PrecursorTester;
import ai.foresignal.manifest.ManifestRequest;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.observability.AbsenceCheck;
import ai.foresignal.observability.ObservabilityDeclarations;
import ai.foresignal.observability.ObservabilityService;
import ai.foresignal.observability.Place;
import ai.foresignal.security.Ulids;
import ai.foresignal.store.query.SeqClockQuery;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Precursor mining and the monthly re-test (prompt 120 §2–§4, FDR-1).
 *
 * <p><b>Mining</b> ({@link #run} with mode {@code mine}):
 * <ol>
 *   <li>The global range is the last {@code analysis.precursors.lookbackDays} days up to today (UTC midnight). Event
 *       families are counted over its discovery share (the oldest {@code discoveryFraction}, two thirds by default).
 *   <li>{@link CandidateGenerator} plans the batch: operator pairs, top-K event-family pairs per region and related
 *       analysis-series pairs, capped by {@code analysis.precursors.maxTestsPerRun}.
 *   <li>Each candidate's own range is the global range clipped to where both sides have data; the oldest share is its
 *       discovery window and the newest its validation window. Ranges under {@value #MIN_RANGE_DAYS} days, or with
 *       coverage gaps over 30%, are {@code insufficient}.
 *   <li>Event pairs run the 103 tester (circular-shift baseline, lift, permutation p with
 *       {@value PrecursorRules#MINING_SHIFTS} shifts, reverse test); an antecedent without a consequent counts as a
 *       miss only when 116 {@code canSeeAbsence} holds for the consequent's family in its region and lag window,
 *       otherwise it is excluded and counted under {@code LOW_DETECTABILITY}. Series pairs run {@link LeadLagCcf} on
 *       pre-whitened residuals.
 *   <li>Benjamini–Hochberg at q = 0.10 across the batch (Benjamini–Yekutieli when series pairs overlap); survivors are
 *       re-tested on the validation window, which discovery never saw, with the same definition, and become
 *       {@code validated} or stay {@code candidate} with the reason.
 * </ol>
 *
 * <p><b>Re-test</b> (mode {@code retest}): validated and stale precursors are tested on the newest third of the range;
 * {@link PrecursorRules#afterRetest} decides the transition. Every run is a {@code precursor_batch} row with its sizes.
 * At most one run at a time (an in-process lock; the service also refuses a second queued job).
 */
@Service
public class PrecursorMiner {

    private static final Logger log = LoggerFactory.getLogger(PrecursorMiner.class);

    /** The miner name. */
    public static final String NAME = "precursor-mining";

    /** The miner version. */
    public static final String VERSION = "1.0.0";

    /** The shortest candidate range. */
    public static final int MIN_RANGE_DAYS = 60;

    /** The coverage-gap share above which a candidate is insufficient. */
    public static final double GAP_LIMIT = PrecursorTester.COVERAGE_GAP_LIMIT;

    private final PrecursorOccurrences occurrences;
    private final PrecursorRepository precursors;
    private final PrecursorBatchRepository batches;
    private final PrecursorCandidateRepository candidates;
    private final AnalysisSeriesRepository registry;
    private final SeriesReader reader;
    private final AnalyticsSupport support;
    private final BacktestCoverage coverage;
    private final PrecursorSettings settings;
    private final FindingService findings;
    private final ObjectProvider<ObservabilityService> observability;
    private final ObjectProvider<ObservabilityDeclarations> declarations;
    private final ObjectProvider<ManifestService> manifests;
    private final NamedParameterJdbcTemplate jdbc;
    private final SeqClockQuery seqClock;
    private final Clock clock;
    private final ReentrantLock lock = new ReentrantLock();

    /** Creates the miner. */
    public PrecursorMiner(PrecursorOccurrences occurrences, PrecursorRepository precursors,
            PrecursorBatchRepository batches, PrecursorCandidateRepository candidates,
            AnalysisSeriesRepository registry, SeriesReader reader, AnalyticsSupport support,
            BacktestCoverage coverage, PrecursorSettings settings, FindingService findings,
            ObjectProvider<ObservabilityService> observability,
            ObjectProvider<ObservabilityDeclarations> declarations, ObjectProvider<ManifestService> manifests,
            NamedParameterJdbcTemplate jdbc, SeqClockQuery seqClock, Clock clock) {
        this.occurrences = occurrences;
        this.precursors = precursors;
        this.batches = batches;
        this.candidates = candidates;
        this.registry = registry;
        this.reader = reader;
        this.support = support;
        this.coverage = coverage;
        this.settings = settings;
        this.findings = findings;
        this.observability = observability;
        this.declarations = declarations;
        this.manifests = manifests;
        this.jdbc = jdbc;
        this.seqClock = seqClock;
        this.clock = clock;
    }

    /**
     * The global range of a run.
     *
     * @param start the oldest instant
     * @param split where discovery ends and validation (or the re-test window) begins
     * @param end the newest instant (exclusive)
     * @param fraction the discovery share
     */
    public record Windows(Instant start, Instant split, Instant end, double fraction) {

        /** The windows as stored on a batch. */
        Map<String, Object> view() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("start", start.toString());
            out.put("split", split.toString());
            out.put("end", end.toString());
            out.put("discoveryFraction", fraction);
            return out;
        }
    }

    /** True while a run holds the lock. */
    public boolean running() {
        return lock.isLocked();
    }

    /**
     * Runs a batch: mining or the re-test.
     *
     * @param mode {@code mine} or {@code retest}
     * @param trigger {@code schedule}, {@code manual} or {@code cycle}
     * @param jobId the 103 job, or null for a direct call
     * @param monitor cancellation and progress
     * @return the finished batch
     * @throws IllegalStateException when another run holds the lock
     */
    public PrecursorBatchEntity run(String mode, String trigger, String jobId, JobMonitor monitor) {
        if (!lock.tryLock()) {
            throw new IllegalStateException("A precursor mining or re-test run is already running.");
        }
        Instant now = clock.instant();
        PrecursorBatchEntity batch = new PrecursorBatchEntity(Ulids.next(now), "retest".equals(mode) ? "retest" : "mine",
                trigger == null ? "manual" : trigger, now);
        batch.setJobId(jobId);
        batch.setQ(Fdr.DEFAULT_Q);
        batch = batches.save(batch);
        try {
            if ("retest".equals(batch.getMode())) {
                retest(batch, now, monitor);
            } else {
                mine(batch, now, monitor);
            }
            batch.setStatus("done");
        } catch (JobMonitor.Canceled e) {
            batch.setStatus("cancelled");
            batch.setFinishedAt(clock.instant());
            batches.save(batch);
            throw e;
        } catch (RuntimeException e) {
            batch.setStatus("failed");
            batch.setError(String.valueOf(e.getMessage() == null ? e : e.getMessage()));
            batch.setFinishedAt(clock.instant());
            batches.save(batch);
            throw e;
        } finally {
            lock.unlock();
        }
        batch.setFinishedAt(clock.instant());
        return batches.save(batch);
    }

    /** The global windows at {@code now}: the lookback up to today's UTC midnight, split by the discovery share. */
    public Windows windows(Instant now) {
        Instant end = now.truncatedTo(ChronoUnit.DAYS);
        Instant start = end.minus(Duration.ofDays(settings.lookbackDays()));
        double fraction = settings.discoveryFraction();
        return new Windows(start, split(start, end, fraction), end, fraction);
    }

    /** The instant {@code fraction} of the way from {@code start} to {@code end}, to the second. */
    static Instant split(Instant start, Instant end, double fraction) {
        long span = Duration.between(start, end).toSeconds();
        return start.plusSeconds(Math.round(span * fraction));
    }

    // ------------------------------------------------------------------------- mining

    void mine(PrecursorBatchEntity batch, Instant now, JobMonitor monitor) {
        Windows w = windows(now);
        batch.setWindows(w.view());
        monitor.checkpoint(0.0);
        List<CandidateGenerator.FamilyCount> families = occurrences.familyCounts(w.start(), w.split());
        monitor.checkpoint(0.05);
        Map<String, SeriesData> data = new HashMap<>();
        List<CandidateGenerator.SeriesInfo> series = seriesInfos(w, data);
        monitor.checkpoint(0.1);
        Set<String> excluded = new LinkedHashSet<>();
        for (PrecursorEntity p : precursors.findByStatusInOrderByIdAsc(List.of(PrecursorRules.VALIDATED,
                PrecursorRules.STALE, PrecursorRules.RETIRED))) {
            excluded.add(p.getCandidateKey());
        }
        List<Candidate> operator = new ArrayList<>();
        for (PrecursorCandidateEntity c : candidates.findAllByOrderByIdAsc()) {
            operator.add(PrecursorViews.candidate(c, frequencyOf(c, data, w)));
        }
        CandidateGenerator.Plan plan = CandidateGenerator.plan(operator, families, series,
                (a, b) -> overlap(data.get(a.ref()), data.get(b.ref())), excluded,
                new CandidateGenerator.Settings(CandidateGenerator.DEFAULT_TOP_K, CandidateGenerator.DEFAULT_LAG_GRID,
                        settings.maxTestsPerRun()));
        batch.setCandidatesGenerated(plan.generated());
        batch.setCapped(plan.capped());
        long asOfSeq = seqClock.seqAt(jdbc, now);
        List<Tested> tested = new ArrayList<>();
        int insufficient = 0;
        int i = 0;
        for (Candidate c : plan.selected()) {
            monitor.checkpoint(0.1 + 0.6 * i++ / Math.max(1, plan.selected().size()));
            Tested t;
            try {
                t = c.event() ? discoverEvent(c, w, now) : discoverSeries(c, w, data, now);
            } catch (RuntimeException e) {
                log.warn("Precursor candidate {} could not be tested: {}", c.key(), e.toString());
                t = null;
            }
            if (t == null || t.insufficient()) {
                insufficient++;
                continue;
            }
            tested.add(t);
        }
        Set<String> seen = new LinkedHashSet<>();
        boolean overlapping = false;
        for (Tested t : tested) {
            if (!t.candidate().event()) {
                for (String ref : List.of(Candidate.seriesRef(t.candidate().antecedent()),
                        Candidate.seriesRef(t.candidate().consequent()))) {
                    overlapping |= !seen.add(ref);
                }
            }
        }
        double[] p = new double[tested.size()];
        for (int k = 0; k < p.length; k++) {
            p[k] = tested.get(k).p();
        }
        Fdr.Adjusted adjusted = Fdr.adjust(p, overlapping, Fdr.DEFAULT_Q);
        batch.setTested(tested.size());
        batch.setInsufficient(insufficient);
        batch.setMethod(adjusted.method());
        batch.setSurvivors(adjusted.survivors());
        int validated = 0;
        for (int k = 0; k < tested.size(); k++) {
            monitor.checkpoint(0.7 + 0.3 * k / Math.max(1, tested.size()));
            if (!adjusted.rejected()[k]) {
                continue;
            }
            Tested t = tested.get(k);
            t.discovery().put("adjustedP", AnalyticsSupport.round(adjusted.adjusted()[k]));
            try {
                if (validate(t, batch, adjusted, data, now, asOfSeq)) {
                    validated++;
                }
            } catch (RuntimeException e) {
                log.warn("Precursor candidate {} could not be validated: {}", t.candidate().key(), e.toString());
            }
        }
        batch.setValidated(validated);
        batch.setManifestId(manifest(NAME, Map.of("mode", "mine", "maxTestsPerRun", settings.maxTestsPerRun(),
                "topK", CandidateGenerator.DEFAULT_TOP_K, "q", Fdr.DEFAULT_Q, "method", adjusted.method(),
                "discoveryFraction", w.fraction(), "lookbackDays", settings.lookbackDays()), asOfSeq,
                Map.of("windows", w.view(), "tested", tested.size(), "capped", plan.capped())));
        log.info("Precursor mining {}: generated {}, tested {}, capped {}, insufficient {}, survivors {}, validated {}",
                batch.getId(), plan.generated(), tested.size(), plan.capped(), insufficient, adjusted.survivors(),
                validated);
    }

    /**
     * One tested candidate.
     *
     * @param candidate the candidate
     * @param range the candidate's own range {@code [start, split, end]}
     * @param discovery the discovery-window statistics (mutable: the adjusted p is added)
     * @param p the raw discovery p-value
     * @param insufficient true when it could not be tested
     * @param event the event result, or null
     * @param seriesResult the series result, or null
     * @param caveats the caveats so far
     */
    record Tested(Candidate candidate, Instant[] range, Map<String, Object> discovery, double p, boolean insufficient,
            PrecursorTester.Result event, LeadLagCcf.Result seriesResult, List<Map<String, Object>> caveats) {}

    /** The discovery test of an event candidate, or an insufficient result. */
    Tested discoverEvent(Candidate c, Windows w, Instant now) {
        Map<String, Object> antSelector = Candidate.selector(c.antecedent());
        Map<String, Object> conSelector = Candidate.selector(c.consequent());
        if (antSelector == null || conSelector == null) {
            return insufficientResult(c, null, "missing selector");
        }
        List<PrecursorOccurrences.Occurrence> ant = occurrences.resolve(antSelector, w.start(), w.end());
        List<PrecursorOccurrences.Occurrence> con = occurrences.resolve(conSelector, w.start(), w.end());
        if (ant.isEmpty() || con.isEmpty()) {
            return insufficientResult(c, null, "no occurrences");
        }
        Instant start = max(w.start(), max(ant.get(0).at(), con.get(0).at()).truncatedTo(ChronoUnit.DAYS));
        Instant end = w.end();
        if (Duration.between(start, end).toDays() < MIN_RANGE_DAYS) {
            return insufficientResult(c, new Instant[] {start, end, end}, "range shorter than " + MIN_RANGE_DAYS
                    + " days");
        }
        Instant split = split(start, end, w.fraction());
        Set<String> sources = new LinkedHashSet<>(occurrences.sources(antSelector, ant));
        sources.addAll(occurrences.sources(conSelector, con));
        EventRun run = eventRun(c, ant, con, start, split, sources, c.key() + ":discovery");
        Map<String, Object> stats = eventStats(run, start, split, null);
        List<Map<String, Object>> caveats = new ArrayList<>();
        if (run.gapFraction() > 0) {
            caveats.add(FindingCaveats.of(FindingCaveats.COVERAGE_GAPS, "Coverage gaps touched "
                    + PrecursorText.pct(run.gapFraction()) + " of the discovery window."));
        }
        boolean insufficient = run.result().insufficient();
        return new Tested(c, new Instant[] {start, split, end}, stats, run.result().pValue(), insufficient,
                run.result(), null, caveats);
    }

    /**
     * One run of the event tester over a window.
     *
     * @param result the tester result
     * @param lowDetectability the antecedents excluded under {@code LOW_DETECTABILITY}
     * @param gapFraction the coverage-gap share
     */
    record EventRun(PrecursorTester.Result result, int lowDetectability, double gapFraction) {}

    /** Runs the 103 tester over {@code [from, to)} with the detectability exclusion. */
    EventRun eventRun(Candidate c, List<PrecursorOccurrences.Occurrence> ant, List<PrecursorOccurrences.Occurrence> con,
            Instant from, Instant to, Set<String> sources, String seedText) {
        PrecursorTester.Spatial spatial = spatial(c.spatial());
        List<PrecursorTester.Occurrence> consequents = new ArrayList<>();
        for (PrecursorOccurrences.Occurrence o : con) {
            if (!o.at().isBefore(from) && o.at().isBefore(to)) {
                consequents.add(o.tester());
            }
        }
        String family = occurrences.signalFamily(Candidate.selector(c.consequent()));
        boolean checkAbsence = family != null && declarations.getIfAvailable() != null
                && declarations.getIfAvailable().eventFamilies().contains(family)
                && observability.getIfAvailable() != null;
        Map<String, Boolean> absenceCache = new HashMap<>();
        List<PrecursorTester.Occurrence> antecedents = new ArrayList<>();
        int excluded = 0;
        for (PrecursorOccurrences.Occurrence o : ant) {
            if (o.at().isBefore(from) || !o.at().isBefore(to)) {
                continue;
            }
            PrecursorTester.Occurrence a = o.tester();
            if (checkAbsence && !hit(a, consequents, c.lagMin(), c.lagMax(), spatial, to)
                    && !canSeeAbsence(family, o.country(), o.at().plus(c.lagMin()), o.at().plus(c.lagMax()),
                            absenceCache)) {
                excluded++;
                continue;
            }
            antecedents.add(a);
        }
        double gaps = sources.isEmpty() ? 0 : coverage.gapFraction(sources, from, to);
        PrecursorTester.Request request = new PrecursorTester.Request(antecedents, consequents, c.lagMin(), c.lagMax(),
                spatial, c.lagMax(), from, to, gaps, seedText + ":" + from + ":" + to);
        return new EventRun(PrecursorTester.test(request, PrecursorRules.MINING_SHIFTS), excluded, gaps);
    }

    /** True when {@code a} has a consequent in its lag window (within the window end) under the spatial rule. */
    static boolean hit(PrecursorTester.Occurrence a, List<PrecursorTester.Occurrence> consequents, Duration minLag,
            Duration maxLag, PrecursorTester.Spatial spatial, Instant windowEnd) {
        Instant earliest = a.time().plus(minLag);
        Instant latest = a.time().plus(maxLag);
        for (PrecursorTester.Occurrence c : consequents) {
            if (c.time().isBefore(earliest)) {
                continue;
            }
            if (c.time().isAfter(latest) || c.time().isAfter(windowEnd)) {
                break;
            }
            if (PrecursorTester.spatially(a, c, spatial)) {
                return true;
            }
        }
        return false;
    }

    /** 116 {@code canSeeAbsence} for {@code family} in the country over the lag window, cached by day. */
    private boolean canSeeAbsence(String family, String country, Instant from, Instant to,
            Map<String, Boolean> cache) {
        String key = family + "|" + country + "|" + from.truncatedTo(ChronoUnit.DAYS) + "|"
                + Duration.between(from, to).toSeconds();
        return cache.computeIfAbsent(key, k -> {
            ObservabilityService service = observability.getIfAvailable();
            if (service == null) {
                return true;
            }
            try {
                AbsenceCheck check = service.canSeeAbsence(family, country == null ? Place.GLOBAL
                        : Place.region(country), from, to, null);
                return check.ok();
            } catch (RuntimeException e) {
                return false;
            }
        });
    }

    /** The {@code PrecursorWindowStats} of an event run. */
    static Map<String, Object> eventStats(EventRun run, Instant from, Instant to, String verdict) {
        PrecursorTester.Result r = run.result();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("window", window(from, to));
        out.put("n", r.nAntecedent());
        out.put("hits", r.hits());
        out.put("hitRate", AnalyticsSupport.round(r.hitRate()));
        out.put("hitRateInterval", interval(r.hitRateInterval()));
        out.put("baseRate", AnalyticsSupport.round(r.baseRate()));
        out.put("lift", AnalyticsSupport.round(r.lift()));
        out.put("correlation", null);
        out.put("bestLag", r.hits() == 0 ? null : PrecursorRules.iso(Duration.ofMinutes(r.lagMedian())));
        out.put("p", AnalyticsSupport.round(r.pValue()));
        out.put("adjustedP", null);
        out.put("verdict", verdict != null ? verdict : r.verdict());
        Map<String, Object> reverse = new LinkedHashMap<>();
        reverse.put("p", AnalyticsSupport.round(r.reverse().pValue()));
        reverse.put("statistic", AnalyticsSupport.round(r.reverse().lift()));
        reverse.put("coMovement", r.coOccurrence());
        out.put("reverse", reverse);
        out.put("lowDetectabilityExcluded", run.lowDetectability());
        out.put("coverageGapFraction", AnalyticsSupport.round(run.gapFraction()));
        return out;
    }

    /** The discovery test of a series candidate, or an insufficient result. */
    Tested discoverSeries(Candidate c, Windows w, Map<String, SeriesData> data, Instant now) {
        SeriesPair pair = pair(c, w, data);
        if (pair == null) {
            return insufficientResult(c, null, "series not readable");
        }
        if (pair.end() <= pair.start() || Duration.between(pair.starts()[pair.start()], pair.endInstant()).toDays()
                < MIN_RANGE_DAYS) {
            return insufficientResult(c, null, "overlap shorter than " + MIN_RANGE_DAYS + " days");
        }
        int split = pair.start() + (int) Math.round((pair.end() - pair.start()) * w.fraction());
        SeriesRun run = seriesRun(c, pair, pair.start(), split, c.key() + ":discovery");
        Map<String, Object> stats = seriesStats(run, pair, pair.start(), split, null);
        List<Map<String, Object>> caveats = new ArrayList<>();
        if (pair.market()) {
            caveats.add(FindingCaveats.dailyResolutionOnly());
        }
        Instant[] range = {pair.starts()[pair.start()], pair.starts()[split], pair.endInstant()};
        boolean insufficient = run.result().insufficient() || run.gapFraction() > GAP_LIMIT;
        return new Tested(c, range, stats, run.result().forward().pValue(), insufficient, null, run.result(), caveats);
    }

    /**
     * A series pair on one grid.
     *
     * @param antecedent the antecedent values
     * @param consequent the consequent values
     * @param starts the bucket starts
     * @param frequency the grid frequency
     * @param start the first index where both have data
     * @param end the index after the last where both have data
     * @param counts whether each side is a count series ({@code [antecedent, consequent]})
     * @param market true when either side is a market series ({@code DAILY_RESOLUTION_ONLY})
     */
    record SeriesPair(double[] antecedent, double[] consequent, Instant[] starts, SeriesFrequency frequency, int start,
            int end, boolean[] counts, boolean market) {

        /** The end of the last bucket. */
        Instant endInstant() {
            return end >= starts.length ? frequency.next(starts[starts.length - 1]) : starts[end];
        }

        /** The slot period of the grid (7 for daily series). */
        int period() {
            return frequency == SeriesFrequency.DAY ? 7 : 1;
        }

        /** The bucket width. */
        Duration width() {
            return frequency == SeriesFrequency.WEEK ? Duration.ofDays(7) : Duration.ofDays(1);
        }
    }

    /** The two series of a candidate on the coarser of their grids over the global range, or null. */
    SeriesPair pair(Candidate c, Windows w, Map<String, SeriesData> data) {
        String aRef = Candidate.seriesRef(c.antecedent());
        String bRef = Candidate.seriesRef(c.consequent());
        if (aRef == null || bRef == null) {
            return null;
        }
        SeriesData a = series(aRef, w, data);
        SeriesData b = series(bRef, w, data);
        if (a == null || b == null) {
            return null;
        }
        SeriesFrequency f = "week".equals(c.frequency()) || a.frequency() == SeriesFrequency.WEEK
                || b.frequency() == SeriesFrequency.WEEK ? SeriesFrequency.WEEK : SeriesFrequency.DAY;
        SeriesData.Grid ga = a.resample(f).grid();
        SeriesData.Grid gb = b.resample(f).grid();
        Map<Instant, Integer> index = new HashMap<>();
        for (int i = 0; i < gb.size(); i++) {
            index.put(gb.starts()[i], i);
        }
        int n = ga.size();
        double[] av = new double[n];
        double[] bv = new double[n];
        int first = -1;
        int last = -1;
        for (int i = 0; i < n; i++) {
            av[i] = ga.values()[i];
            Integer j = index.get(ga.starts()[i]);
            bv[i] = j == null ? Double.NaN : gb.values()[j];
            if (Double.isFinite(av[i]) && Double.isFinite(bv[i])) {
                first = first < 0 ? i : first;
                last = i;
            }
        }
        if (first < 0) {
            return null;
        }
        return new SeriesPair(av, bv, ga.starts(), f, first, last + 1, new boolean[] {a.counts(), b.counts()},
                AnalyticsSupport.market(a) || AnalyticsSupport.market(b));
    }

    /**
     * One run of the lead–lag test over a slice of a pair.
     *
     * @param result the test result
     * @param gapFraction the share of the slice where either side is missing
     * @param exceedance the live-threshold exceedance statistics at the best lag
     */
    record SeriesRun(LeadLagCcf.Result result, double gapFraction, Exceedance exceedance) {}

    /**
     * How often a series antecedent beyond the live threshold was followed, at the best lag, by a consequent beyond it
     * in the expected direction.
     *
     * @param n the antecedent exceedances
     * @param hits the followed exceedances
     * @param baseRate the share of consequent buckets beyond the threshold in the expected direction
     */
    record Exceedance(long n, long hits, double baseRate) {}

    /** Pre-whitens the slice {@code [from, to)} of a pair and runs the lead–lag test. */
    SeriesRun seriesRun(Candidate c, SeriesPair pair, int from, int to, String seedText) {
        double[] a = java.util.Arrays.copyOfRange(pair.antecedent(), from, to);
        double[] b = java.util.Arrays.copyOfRange(pair.consequent(), from, to);
        int missing = 0;
        for (int i = 0; i < a.length; i++) {
            if (!Double.isFinite(a[i]) || !Double.isFinite(b[i])) {
                missing++;
            }
        }
        double gaps = a.length == 0 ? 1 : (double) missing / a.length;
        double[] ra = Prewhitening.residuals(a, pair.counts()[0], pair.period());
        double[] rb = Prewhitening.residuals(b, pair.counts()[1], pair.period());
        long widthSeconds = pair.width().toSeconds();
        int maxLag = (int) Math.max(1, c.lagMax().toSeconds() / widthSeconds);
        LeadLagCcf.Result result = LeadLagCcf.test(ra, rb, maxLag, Seeds.of(seedText, pair.starts()[from].toString(),
                String.valueOf(to - from)));
        return new SeriesRun(result, gaps, exceedance(ra, rb, result));
    }

    /** The exceedance statistics of the standardized residuals at the best lag. */
    static Exceedance exceedance(double[] antecedent, double[] consequent, LeadLagCcf.Result result) {
        if (result.insufficient() || result.forward().bestLag() <= 0) {
            return new Exceedance(0, 0, Double.NaN);
        }
        double[] za = Prewhitening.standardize(antecedent);
        double[] zb = Prewhitening.standardize(consequent);
        int lag = result.forward().bestLag();
        double sign = result.forward().correlation() >= 0 ? 1 : -1;
        long usable = 0;
        long up = 0;
        long down = 0;
        for (double z : zb) {
            if (Double.isFinite(z)) {
                usable++;
                up += z >= PrecursorRules.LIVE_THRESHOLD ? 1 : 0;
                down += z <= -PrecursorRules.LIVE_THRESHOLD ? 1 : 0;
            }
        }
        long n = 0;
        long hits = 0;
        double baseSum = 0;
        for (int t = 0; t + lag < za.length; t++) {
            if (!Double.isFinite(za[t]) || Math.abs(za[t]) < PrecursorRules.LIVE_THRESHOLD
                    || !Double.isFinite(zb[t + lag])) {
                continue;
            }
            double expected = Math.signum(za[t]) * sign;
            n++;
            if (zb[t + lag] * expected >= PrecursorRules.LIVE_THRESHOLD) {
                hits++;
            }
            baseSum += usable == 0 ? 0 : (double) (expected > 0 ? up : down) / usable;
        }
        return new Exceedance(n, hits, n == 0 ? Double.NaN : baseSum / n);
    }

    /** The {@code PrecursorWindowStats} of a series run. */
    static Map<String, Object> seriesStats(SeriesRun run, SeriesPair pair, int from, int to, String verdict) {
        LeadLagCcf.Result r = run.result();
        Map<String, Object> out = new LinkedHashMap<>();
        Instant end = to >= pair.starts().length ? pair.endInstant() : pair.starts()[to];
        out.put("window", window(pair.starts()[from], end));
        out.put("n", r.overlap());
        Exceedance e = run.exceedance();
        out.put("hits", e.n() == 0 ? null : e.hits());
        out.put("hitRate", e.n() == 0 ? null : AnalyticsSupport.round((double) e.hits() / e.n()));
        out.put("hitRateInterval", e.n() == 0 ? null : interval(ai.foresignal.analytics.math.Wilson.interval(e.hits(),
                e.n())));
        out.put("baseRate", AnalyticsSupport.round(e.baseRate()));
        out.put("lift", e.n() == 0 || !(e.baseRate() > 0) ? null
                : AnalyticsSupport.round((double) e.hits() / e.n() / e.baseRate()));
        out.put("correlation", AnalyticsSupport.round(r.forward().correlation()));
        out.put("bestLag", r.forward().bestLag() <= 0 ? null
                : PrecursorRules.iso(pair.width().multipliedBy(r.forward().bestLag())));
        out.put("p", AnalyticsSupport.round(r.forward().pValue()));
        out.put("adjustedP", null);
        String v = verdict;
        if (v == null) {
            v = r.insufficient() || run.gapFraction() > GAP_LIMIT ? "insufficient"
                    : r.forward().pValue() <= PrecursorTester.NOTABLE_P ? "notable"
                    : r.forward().pValue() <= PrecursorTester.WEAK_P ? "weak" : "no-evidence-of-precedence";
        }
        out.put("verdict", v);
        Map<String, Object> reverse = new LinkedHashMap<>();
        reverse.put("p", AnalyticsSupport.round(r.reverse().pValue()));
        reverse.put("statistic", AnalyticsSupport.round(r.reverse().statistic()));
        reverse.put("coMovement", r.coMovement());
        out.put("reverse", reverse);
        out.put("lowDetectabilityExcluded", 0);
        out.put("blockLength", r.blockLength());
        out.put("coverageGapFraction", AnalyticsSupport.round(run.gapFraction()));
        return out;
    }

    /** Re-tests a survivor on its validation window and stores it in the library. */
    boolean validate(Tested t, PrecursorBatchEntity batch, Fdr.Adjusted adjusted, Map<String, SeriesData> data,
            Instant now, long asOfSeq) {
        Candidate c = t.candidate();
        Instant split = t.range()[1];
        Instant end = t.range()[2];
        Map<String, Object> validation;
        String failure;
        Headline headline;
        List<Map<String, Object>> caveats = new ArrayList<>(t.caveats());
        if (c.event()) {
            Map<String, Object> antSelector = Candidate.selector(c.antecedent());
            Map<String, Object> conSelector = Candidate.selector(c.consequent());
            List<PrecursorOccurrences.Occurrence> ant = occurrences.resolve(antSelector, split, end);
            List<PrecursorOccurrences.Occurrence> con = occurrences.resolve(conSelector, split, end);
            Set<String> sources = new LinkedHashSet<>(occurrences.sources(antSelector, ant));
            sources.addAll(occurrences.sources(conSelector, con));
            EventRun run = eventRun(c, ant, con, split, end, sources, c.key() + ":validation");
            failure = PrecursorRules.eventValidationFailure(run.result());
            validation = eventStats(run, split, end, failure == null ? "validated" : "failed");
            headline = Headline.of(run.result(), split, end);
            if (run.lowDetectability() > 0 || ((Number) t.discovery().get("lowDetectabilityExcluded")).intValue() > 0) {
                caveats.add(FindingCaveats.of(FindingCaveats.LOW_DETECTABILITY, "Antecedents whose consequent could"
                        + " not have been seen (116 detectability) were excluded rather than counted as misses."));
            }
            if (t.event().coOccurrence() || run.result().coOccurrence()) {
                caveats.add(FindingCaveats.of("CO_OCCURRENCE", "The reverse direction is as strong: co-occurrence,"
                        + " not precedence."));
            }
        } else {
            SeriesPair pair = pair(c, windows(now), data);
            if (pair == null) {
                return false;
            }
            int from = indexOf(pair.starts(), split);
            int to = Math.min(pair.end(), indexOf(pair.starts(), end));
            SeriesRun run = seriesRun(c, pair, from, Math.max(from, to), c.key() + ":validation");
            failure = run.gapFraction() > GAP_LIMIT ? "coverage gaps over 30% on the validation window"
                    : PrecursorRules.seriesValidationFailure(t.seriesResult(), run.result());
            validation = seriesStats(run, pair, from, Math.max(from, to), failure == null ? "validated" : "failed");
            headline = Headline.of(run, pair.width());
            if (t.seriesResult().coMovement() || run.result().coMovement()) {
                caveats.add(FindingCaveats.of("CO_MOVEMENT", "The reverse direction is as strong: co-movement, not"
                        + " precedence."));
            }
        }
        caveats.add(0, FindingCaveats.notGroundTruth());
        caveats.add(FindingCaveats.of(FindingCaveats.MULTIPLE_TESTS, adjusted.raw().length + " tests in the batch;"
                + " p-values adjusted by " + adjusted.method() + " at q = " + adjusted.q() + "."));
        PrecursorEntity p = precursors.findByCandidateKey(c.key()).orElse(null);
        if (p != null && !PrecursorRules.CANDIDATE.equals(p.getStatus())) {
            return false;
        }
        if (p == null) {
            p = new PrecursorEntity(Ulids.next(now), c.key(), c.kind(), now);
        }
        String before = p.getStatus();
        boolean valid = failure == null;
        p.setAntecedent(c.antecedent());
        p.setConsequent(c.consequent());
        p.setSpatial(c.spatial());
        p.setRegionCodes(c.regionCodes());
        p.setFamilies(families(c));
        p.setLagMinSeconds(c.lagMin().toSeconds());
        p.setLagMaxSeconds(c.lagMax().toSeconds());
        p.setDiscovery(t.discovery());
        p.setValidation(validation);
        headline.apply(p);
        p.setBatchId(batch.getId());
        p.setBatchSize(adjusted.raw().length);
        p.setStatus(valid ? PrecursorRules.VALIDATED : PrecursorRules.CANDIDATE);
        p.setStatusReason(valid ? "validated on " + PrecursorText.window(split, end) : failure);
        p.setStaleCount(0);
        p.setDetector(detector(c, adjusted));
        p.setDetectorVersion(c.event() ? "precursor-tester/" + PrecursorTester.VERSION
                : LeadLagCcf.NAME + "/" + LeadLagCcf.VERSION);
        p.setLastTestedAt(now);
        p.setOrigin(c.origin());
        p.setCandidateId(c.candidateId());
        p.setCaveats(caveats);
        p.setDescription(PrecursorViews.describe(p, split, end, valid));
        p.setManifestId(manifest(NAME, Map.of("candidate", definition(c), "detector", p.getDetector()), asOfSeq,
                Map.of("range", List.of(t.range()[0].toString(), split.toString(), end.toString()),
                        "batchId", batch.getId())));
        p.setUpdatedAt(now);
        if (valid && p.getFirstValidatedAt() == null) {
            p.setFirstValidatedAt(now);
        }
        if (valid) {
            p.setHistory(append(p.getHistory(), now, NAME + ":" + VERSION, before, PrecursorRules.VALIDATED,
                    p.getStatusReason()));
        }
        precursors.save(p);
        if (valid) {
            findings.announcePrecursor(p.getId(), FindingEvent.PRECURSOR_VALIDATED, NAME + ":" + VERSION,
                    p.getStatusReason());
        }
        return valid;
    }

    /**
     * The headline statistics of the latest out-of-sample window.
     *
     * @param n the antecedents (events) or exceedances (series)
     * @param hits the hits
     * @param hitRate the hit rate, or null
     * @param interval the Wilson interval, or null
     * @param baseRate the base rate, or null
     * @param lift the lift, or null
     * @param correlation the correlation (series), or null
     * @param sign the correlation sign (series), or null
     * @param bestLag the best lag, or null
     * @param lagDistribution the lag quartiles (events), or null
     */
    record Headline(long n, long hits, Double hitRate, double[] interval, Double baseRate, Double lift,
            Double correlation, String sign, Duration bestLag, Map<String, Object> lagDistribution) {

        static Headline of(PrecursorTester.Result r, Instant from, Instant to) {
            Map<String, Object> lags = null;
            if (r.hits() > 0) {
                lags = new LinkedHashMap<>();
                lags.put("p25", PrecursorRules.iso(Duration.ofMinutes(r.lagP25())));
                lags.put("median", PrecursorRules.iso(Duration.ofMinutes(r.lagMedian())));
                lags.put("p75", PrecursorRules.iso(Duration.ofMinutes(r.lagP75())));
            }
            return new Headline(r.nAntecedent(), r.hits(), AnalyticsSupport.round(r.hitRate()), r.hitRateInterval(),
                    AnalyticsSupport.round(r.baseRate()), AnalyticsSupport.round(r.lift()), null, null,
                    r.hits() == 0 ? null : Duration.ofMinutes(r.lagMedian()), lags);
        }

        static Headline of(SeriesRun run, Duration width) {
            LeadLagCcf.Result r = run.result();
            Exceedance e = run.exceedance();
            Double rate = e.n() == 0 ? null : AnalyticsSupport.round((double) e.hits() / e.n());
            Double lift = e.n() == 0 || !(e.baseRate() > 0) ? null
                    : AnalyticsSupport.round((double) e.hits() / e.n() / e.baseRate());
            return new Headline(e.n(), e.hits(), rate, e.n() == 0 ? null
                    : ai.foresignal.analytics.math.Wilson.interval(e.hits(), e.n()),
                    AnalyticsSupport.round(e.baseRate()), lift, AnalyticsSupport.round(r.forward().correlation()),
                    r.sign(), r.forward().bestLag() <= 0 ? null : width.multipliedBy(r.forward().bestLag()), null);
        }

        void apply(PrecursorEntity p) {
            p.setNAntecedent(n);
            p.setHits(hits);
            p.setHitRate(hitRate);
            p.setHitRateLower(interval == null ? null : AnalyticsSupport.round(interval[0]));
            p.setHitRateUpper(interval == null ? null : AnalyticsSupport.round(interval[1]));
            p.setBaseRate(baseRate);
            p.setLift(lift);
            p.setCorrelation(correlation);
            p.setCorrelationSign(sign);
            p.setBestLagSeconds(bestLag == null ? null : bestLag.toSeconds());
            p.setLagDistribution(lagDistribution);
        }
    }

    // ------------------------------------------------------------------------- re-test

    void retest(PrecursorBatchEntity batch, Instant now, JobMonitor monitor) {
        Windows global = windows(now);
        Instant from = split(global.start(), global.end(), global.fraction());
        Windows w = new Windows(global.start(), from, global.end(), global.fraction());
        batch.setWindows(w.view());
        batch.setMethod(Fdr.BH);
        List<PrecursorEntity> library = precursors.findByStatusInOrderByIdAsc(List.of(PrecursorRules.VALIDATED,
                PrecursorRules.STALE));
        long asOfSeq = seqClock.seqAt(jdbc, now);
        Map<String, SeriesData> data = new HashMap<>();
        int tested = 0;
        int holding = 0;
        int validated = 0;
        int insufficient = 0;
        int i = 0;
        for (PrecursorEntity p : library) {
            monitor.checkpoint((double) i++ / Math.max(1, library.size()));
            try {
                Outcome o = retestOne(p, w, data, now);
                if (o == Outcome.INSUFFICIENT) {
                    insufficient++;
                }
                if (o != Outcome.INSUFFICIENT) {
                    tested++;
                }
                if (PrecursorRules.VALIDATED.equals(p.getStatus())) {
                    validated++;
                }
                if (o == Outcome.HELD) {
                    holding++;
                }
            } catch (RuntimeException e) {
                log.warn("Precursor {} could not be re-tested: {}", p.getId(), e.toString());
            }
        }
        batch.setCandidatesGenerated(library.size());
        batch.setTested(tested);
        batch.setInsufficient(insufficient);
        batch.setSurvivors(holding);
        batch.setValidated(validated);
        batch.setManifestId(manifest(NAME, Map.of("mode", "retest", "staleLift", PrecursorRules.STALE_LIFT,
                "seriesP", PrecursorRules.SERIES_P), asOfSeq, Map.of("windows", w.view(), "library", library.size())));
        log.info("Precursor re-test {}: {} re-tested, {} holding, {} validated", batch.getId(), tested, holding,
                validated);
    }

    /** What a re-test found. */
    enum Outcome {
        /** The pattern still holds. */
        HELD,
        /** The pattern did not hold. */
        FAILED,
        /** Too little data to judge (counted as not holding). */
        INSUFFICIENT
    }

    /** Re-tests one precursor on the newest window and applies the lifecycle. */
    Outcome retestOne(PrecursorEntity p, Windows w, Map<String, SeriesData> data, Instant now) {
        Candidate c = PrecursorViews.candidate(p);
        Instant from = w.split();
        Instant to = w.end();
        String staleReason;
        boolean validates;
        Map<String, Object> stats;
        boolean insufficient;
        if (c.event()) {
            Map<String, Object> antSelector = Candidate.selector(c.antecedent());
            Map<String, Object> conSelector = Candidate.selector(c.consequent());
            List<PrecursorOccurrences.Occurrence> ant = occurrences.resolve(antSelector, from, to);
            List<PrecursorOccurrences.Occurrence> con = occurrences.resolve(conSelector, from, to);
            Set<String> sources = new LinkedHashSet<>(occurrences.sources(antSelector, ant));
            sources.addAll(occurrences.sources(conSelector, con));
            EventRun run = eventRun(c, ant, con, from, to, sources, p.getCandidateKey() + ":retest");
            staleReason = PrecursorRules.eventStaleReason(run.result());
            validates = PrecursorRules.eventValidationFailure(run.result()) == null;
            stats = eventStats(run, from, to, staleReason == null ? "validated" : "failed");
            insufficient = run.result().insufficient();
            Headline.of(run.result(), from, to).apply(p);
        } else {
            SeriesPair pair = pair(c, w, data);
            if (pair == null) {
                staleReason = "the series could not be read";
                validates = false;
                stats = null;
                insufficient = true;
            } else {
                int a = Math.max(pair.start(), indexOf(pair.starts(), from));
                int b = Math.min(pair.end(), indexOf(pair.starts(), to));
                SeriesRun run = seriesRun(c, pair, a, Math.max(a, b), p.getCandidateKey() + ":retest");
                staleReason = run.gapFraction() > GAP_LIMIT ? "coverage gaps over 30% on the re-test window"
                        : PrecursorRules.seriesStaleReason(p.getCorrelationSign(), run.result());
                validates = staleReason == null;
                stats = seriesStats(run, pair, a, Math.max(a, b), staleReason == null ? "validated" : "failed");
                insufficient = run.result().insufficient();
                String sign = p.getCorrelationSign();
                Headline.of(run, pair.width()).apply(p);
                if (sign != null) {
                    p.setCorrelationSign(sign);
                }
            }
        }
        String before = p.getStatus();
        String next = p.isOperatorValidated() ? before : PrecursorRules.afterRetest(before, staleReason, validates);
        p.setLastRetest(stats);
        p.setLastTestedAt(now);
        p.setUpdatedAt(now);
        if (!next.equals(before)) {
            String reason = switch (next) {
                case PrecursorRules.STALE -> staleReason;
                case PrecursorRules.RETIRED -> "stale twice in a row: " + staleReason;
                default -> "validated again on " + PrecursorText.window(from, to);
            };
            p.setStatus(next);
            p.setStatusReason(reason);
            p.setStaleCount(PrecursorRules.VALIDATED.equals(next) ? 0 : p.getStaleCount() + 1);
            p.setHistory(append(p.getHistory(), now, NAME + ":" + VERSION, before, next, reason));
            precursors.save(p);
            String event = switch (next) {
                case PrecursorRules.STALE -> FindingEvent.PRECURSOR_STALE;
                case PrecursorRules.RETIRED -> FindingEvent.PRECURSOR_RETIRED;
                default -> FindingEvent.PRECURSOR_VALIDATED;
            };
            findings.announcePrecursor(p.getId(), event, NAME + ":" + VERSION, reason);
        } else {
            precursors.save(p);
        }
        if (insufficient) {
            return Outcome.INSUFFICIENT;
        }
        return staleReason == null ? Outcome.HELD : Outcome.FAILED;
    }

    // ------------------------------------------------------------------------- helpers

    /** The enabled daily and weekly analysis series that are not private, read once over the global range. */
    List<CandidateGenerator.SeriesInfo> seriesInfos(Windows w, Map<String, SeriesData> data) {
        List<CandidateGenerator.SeriesInfo> out = new ArrayList<>();
        for (AnalysisSeriesEntity row : registry.findByEnabledTrueOrderByRefAsc()) {
            if (!"day".equals(row.getFrequency()) && !"week".equals(row.getFrequency())) {
                continue;
            }
            SeriesData d = series(row.getRef(), w, data);
            if (d == null) {
                continue;
            }
            long points = d.points().stream().filter(v -> v.usable()).count();
            out.add(new CandidateGenerator.SeriesInfo(row.getRef(), row.getFrequency(),
                    row.getRegionCode() != null ? row.getRegionCode() : d.regionCode(),
                    row.getFamily() != null ? row.getFamily() : d.signalFamily(), d.topicId(), points,
                    AnalyticsSupport.label(d.ref())));
        }
        return out;
    }

    /** One series over the global range as of the run (cached), or null when unreadable or private. */
    SeriesData series(String ref, Windows w, Map<String, SeriesData> data) {
        if (data.containsKey(ref)) {
            return data.get(ref);
        }
        SeriesData d;
        try {
            d = reader.read(SeriesRef.parse(ref), w.start(), w.end(), w.end());
            if (d.frequency() == SeriesFrequency.HOUR) {
                d = d.resample(SeriesFrequency.DAY);
            }
            if (support.privateSeries(d) || !d.frequency().regular() || d.frequency() == SeriesFrequency.MONTH) {
                d = null;
            }
        } catch (RuntimeException e) {
            d = null;
        }
        data.put(ref, d);
        return d;
    }

    /** The data overlap of two series: buckets where both have a usable value (on the coarser grid). */
    static long overlap(SeriesData a, SeriesData b) {
        if (a == null || b == null) {
            return 0;
        }
        SeriesFrequency f = a.frequency() == SeriesFrequency.WEEK || b.frequency() == SeriesFrequency.WEEK
                ? SeriesFrequency.WEEK : SeriesFrequency.DAY;
        Set<Instant> left = new java.util.HashSet<>();
        for (var v : a.resample(f).points()) {
            if (v.usable()) {
                left.add(f.floor(v.periodStart()));
            }
        }
        long n = 0;
        for (var v : b.resample(f).points()) {
            if (v.usable() && left.contains(f.floor(v.periodStart()))) {
                n++;
            }
        }
        return n;
    }

    /** The frequency of an operator series candidate ({@code week} when either series is weekly), or null. */
    String frequencyOf(PrecursorCandidateEntity c, Map<String, SeriesData> data, Windows w) {
        if (!Candidate.SERIES.equals(c.getKind())) {
            return null;
        }
        for (Map<String, Object> side : List.of(c.getAntecedent(), c.getConsequent())) {
            String ref = Candidate.seriesRef(side);
            SeriesData d = ref == null ? null : series(ref, w, data);
            if (d != null && d.frequency() == SeriesFrequency.WEEK) {
                return "week";
            }
        }
        return "day";
    }

    private Tested insufficientResult(Candidate c, Instant[] range, String reason) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("verdict", "insufficient");
        stats.put("reason", reason);
        return new Tested(c, range, stats, 1.0, true, null, null, List.of());
    }

    static int indexOf(Instant[] starts, Instant at) {
        for (int i = 0; i < starts.length; i++) {
            if (!starts[i].isBefore(at)) {
                return i;
            }
        }
        return starts.length;
    }

    static List<String> families(Candidate c) {
        List<String> out = new ArrayList<>();
        for (Map<String, Object> side : List.of(c.antecedent(), c.consequent())) {
            Object family = side.get("family");
            if (family != null && !out.contains(family.toString())) {
                out.add(family.toString());
            }
        }
        return out;
    }

    static Map<String, Object> definition(Candidate c) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("kind", c.kind());
        out.put("antecedent", c.antecedent());
        out.put("consequent", c.consequent());
        out.put("spatial", c.spatial());
        out.put("lagMin", PrecursorRules.iso(c.lagMin()));
        out.put("lagMax", PrecursorRules.iso(c.lagMax()));
        out.put("origin", c.origin());
        return out;
    }

    static Map<String, Object> detector(Candidate c, Fdr.Adjusted adjusted) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("lagMin", PrecursorRules.iso(c.lagMin()));
        params.put("lagMax", PrecursorRules.iso(c.lagMax()));
        params.put("spatial", c.spatial());
        if (c.event()) {
            params.put("shifts", PrecursorRules.MINING_SHIFTS);
            params.put("validationLift", PrecursorRules.VALIDATION_LIFT);
        } else {
            params.put("surrogates", LeadLagCcf.SURROGATES);
            params.put("seriesP", PrecursorRules.SERIES_P);
            params.put("frequency", c.frequency());
        }
        params.put("q", adjusted.q());
        params.put("method", adjusted.method());
        params.put("miner", NAME + " " + VERSION);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("name", c.event() ? "precursor-tester" : LeadLagCcf.NAME);
        out.put("version", c.event() ? PrecursorTester.VERSION : LeadLagCcf.VERSION);
        out.put("params", params);
        return out;
    }

    static List<Map<String, Object>> append(List<Map<String, Object>> history, Instant at, String by, String from,
            String to, String reason) {
        List<Map<String, Object>> out = new ArrayList<>(history == null ? List.of() : history);
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("at", at.toString());
        entry.put("by", by);
        entry.put("from", from);
        entry.put("to", to);
        entry.put("reason", reason);
        out.add(entry);
        return out;
    }

    static Map<String, Object> window(Instant from, Instant to) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("start", from.toString());
        out.put("end", to.toString());
        return out;
    }

    static Map<String, Object> interval(double[] interval) {
        if (interval == null) {
            return null;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("lower", AnalyticsSupport.round(interval[0]));
        out.put("upper", AnalyticsSupport.round(interval[1]));
        out.put("level", 0.95);
        out.put("method", "wilson");
        return out;
    }

    static PrecursorTester.Spatial spatial(Map<String, Object> spatial) {
        Object rule = spatial == null ? null : spatial.get("rule");
        if (rule == null || "none".equals(rule)) {
            return PrecursorTester.Spatial.none();
        }
        return switch (rule.toString()) {
            case "sameEntity" -> PrecursorTester.Spatial.sameEntity();
            case "withinKm" -> spatial.get("withinKm") instanceof Number km ? PrecursorTester.Spatial.withinKm(
                    km.doubleValue()) : PrecursorTester.Spatial.none();
            case "sameRegion" -> PrecursorTester.Spatial.sameRegion(spatial.get("regionLevel") == null ? "country"
                    : spatial.get("regionLevel").toString());
            default -> PrecursorTester.Spatial.none();
        };
    }

    private static Instant max(Instant a, Instant b) {
        return a.isAfter(b) ? a : b;
    }

    private String manifest(String component, Map<String, Object> config, long asOfSeq, Map<String, Object> query) {
        ManifestService service = manifests.getIfAvailable();
        if (service == null) {
            return null;
        }
        try {
            return service.record(ManifestRequest.of(component, VERSION).config(config)
                    .registries("signal-families.json").asOf(asOfSeq).query(query));
        } catch (RuntimeException e) {
            log.warn("Could not record a precursor manifest: {}", e.toString());
            return null;
        }
    }
}
