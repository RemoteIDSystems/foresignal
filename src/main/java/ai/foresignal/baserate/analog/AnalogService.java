package ai.foresignal.baserate.analog;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.baserate.BaseRateService;
import ai.foresignal.baserate.RegionClasses;
import ai.foresignal.baserate.reference.OwnHistoryFold;
import ai.foresignal.baserate.reference.ReferenceFamilies;
import ai.foresignal.baserate.reference.WeeklyCounts;
import ai.foresignal.manifest.ManifestRequest;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Historical analogs (prompt 121, Intelligence-Design §7.3).
 *
 * <ul>
 *   <li><strong>Situation analogs</strong> ({@link #forSituation}): the target's signature at its current day (stored,
 *       or computed now) against each past situation's signature at the same day (or its last), searched by
 *       {@link AnalogSearch}. Situations overlapping the target in time, the target's own merges and situations that
 *       began after the decision time (now) are excluded; private situations only reach callers with private access.
 *       Each analog carries its outcomes (computed when missing).
 *   <li><strong>Region-period analogs</strong> ({@link #forRegion}): a signature over a region's weeks in
 *       {@code [from, to]} of {@code reference_history} (one source per family and week: the largest count, so
 *       sources are not added together) plus Foresignal's not-yet-folded canonical events, searched against two-week
 *       windows of the same continent's countries over the previous {@value #REGION_YEARS} years that ended at least
 *       30 days before the target began.
 *   <li><strong>Outcome distribution</strong> for {@code critical} or {@code {family}/P{n}D}: the frequency among the
 *       analogs, shrunk toward the base rate by k ÷ (k + 10), with the analog count and a Jeffreys interval. The base
 *       rate of a family outcome is the {@code pAtLeastOne} of the matching base rate for the target's countries; of
 *       {@code critical}, the share of past situations (begun, with their outcome recorded, by now) that reached
 *       critical.
 * </ul>
 */
@Service
public class AnalogService {

    /** The default number of analogs. */
    public static final int DEFAULT_K = 10;

    /** The years of history region-period analogs search. */
    public static final int REGION_YEARS = 10;

    /** The default region-period length. */
    public static final Duration DEFAULT_PERIOD = Duration.ofDays(14);

    /** The longest region-period. */
    public static final Duration MAX_PERIOD = Duration.ofDays(90);

    /** The target outcome grammar. */
    public static final Pattern OUTCOME = Pattern.compile("^(critical|([a-z][a-z0-9:+.-]*)/P(\\d+)D)$");

    private static final Pattern REGION = Pattern.compile("^[A-Z]{2}(-[A-Z0-9]{1,3})?$");

    private final SignatureService signatures;
    private final SituationSignatureRepository signatureRows;
    private final SituationOutcomeRepository outcomes;
    private final BaseRateService baseRates;
    private final RegionClasses regionClasses;
    private final OwnHistoryFold fold;
    private final AppSettingService settings;
    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectProvider<ManifestService> manifests;
    private final Clock clock;

    /** Creates the service. */
    public AnalogService(SignatureService signatures, SituationSignatureRepository signatureRows,
            SituationOutcomeRepository outcomes, BaseRateService baseRates, RegionClasses regionClasses,
            OwnHistoryFold fold, AppSettingService settings, NamedParameterJdbcTemplate jdbc,
            ObjectProvider<ManifestService> manifests, Clock clock) {
        this.signatures = signatures;
        this.signatureRows = signatureRows;
        this.outcomes = outcomes;
        this.baseRates = baseRates;
        this.regionClasses = regionClasses;
        this.fold = fold;
        this.settings = settings;
        this.jdbc = jdbc;
        this.manifests = manifests;
        this.clock = clock;
    }

    /**
     * A parsed target outcome.
     *
     * @param text the outcome as given
     * @param family the family, or null for {@code critical}
     * @param days the days, or 0 for {@code critical}
     */
    public record Outcome(String text, String family, int days) {

        /** True for {@code critical}. */
        public boolean critical() {
            return family == null;
        }
    }

    /**
     * Parses a target outcome.
     *
     * @return the outcome, or null when {@code text} is null
     * @throws ValidationException 400 when it does not match {@code critical} or {@code {family}/P{n}D} (1–365 days)
     */
    public static Outcome outcome(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        Matcher m = OUTCOME.matcher(text.strip());
        if (!m.matches()) {
            throw ValidationException.forField("outcome", "must be critical or {family}/P{n}D, e.g."
                    + " conflict:battles/P30D");
        }
        if ("critical".equals(m.group(1))) {
            return new Outcome("critical", null, 0);
        }
        int days = Integer.parseInt(m.group(3));
        if (days < 1 || days > 365) {
            throw ValidationException.forField("outcome", "the window must be 1 to 365 days");
        }
        return new Outcome(text.strip(), m.group(2), days);
    }

    // ------------------------------------------------------------------------- situation analogs

    /**
     * The analogs of a situation ({@code AnalogResult}).
     *
     * @param situationId the situation
     * @param k how many
     * @param outcomeText the target outcome, or null
     * @param privateAccess whether the caller may see private situations
     * @return the result
     * @throws NotFoundException 404 {@code UNKNOWN_SITUATION}
     */
    public Map<String, Object> forSituation(String situationId, int k, String outcomeText, boolean privateAccess) {
        Outcome outcome = outcome(outcomeText);
        SignatureService.SituationFacts target = signatures.facts(situationId).filter(s -> privateAccess
                || !s.privateSituation()).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_SITUATION,
                        "No situation '" + situationId + "'."));
        Instant now = clock.instant();
        int day = SignatureService.dayN(target.startedAt(), now);
        SituationSignatureEntity stored = signatureRows.findById(new SituationSignatureEntity.Key(situationId, day))
                .orElse(null);
        SignatureBuilder.Signature targetSignature = stored != null ? stored.signature()
                : signatures.signature(target, day);
        Instant computedAt = stored != null ? stored.getComputedAt() : now;
        String targetGroup = group(target);
        List<AnalogSearch.Candidate> candidates = new ArrayList<>();
        Map<String, SignatureService.SituationFacts> factsById = new HashMap<>();
        // A situation that began after the decision time is not a past case (INT-2, no look-ahead).
        List<String> ids = jdbc.queryForList("SELECT DISTINCT g.situation_id FROM situation_signature g JOIN situation s"
                + " ON s.id = g.situation_id WHERE s.id <> :id AND s.merged_into IS NULL AND s.started_at <= :now"
                + (privateAccess ? "" : " AND s.private = false"), new MapSqlParameterSource("id", situationId)
                        .addValue("now", Timestamp.from(now)), String.class);
        for (String id : ids) {
            SignatureService.SituationFacts facts = signatures.facts(id).orElse(null);
            if (facts == null) {
                continue;
            }
            SituationSignatureEntity sig = signatureAt(id, day);
            if (sig == null) {
                continue;
            }
            factsById.put(id, facts);
            candidates.add(new AnalogSearch.Candidate(id, sig.signature(), facts.startedAt(), facts.end(),
                    group(facts)));
        }
        Map<String, Double> embedding = signatures.embeddingSimilarities(situationId);
        AnalogSearch.Result result = AnalogSearch.search(targetSignature, target.startedAt(), target.end() == null ? now
                : target.end(), targetGroup, candidates, embedding, k);
        List<Map<String, Object>> analogs = new ArrayList<>();
        int known = 0;
        int hits = 0;
        for (AnalogSearch.Ranked r : result.analogs()) {
            SignatureService.SituationFacts facts = factsById.get(r.id());
            SituationOutcomeEntity o = outcomes.findById(r.id()).orElseGet(() -> signatures.outcome(r.id())
                    .orElse(null));
            Boolean occurred = null;
            if (outcome != null && o != null) {
                occurred = occurred(outcome, facts.regionCodes(), o);
                if (occurred != null) {
                    known++;
                    if (occurred) {
                        hits++;
                    }
                }
            }
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("situationId", r.id());
            a.put("title", facts.title());
            a.put("theme", facts.theme());
            a.put("regionCodes", facts.regionCodes());
            a.put("period", period(facts.startedAt(), facts.end()));
            a.put("rank", r.rank());
            a.put("similarity", r.similarity());
            a.put("featureSimilarity", r.featureSimilarity());
            a.put("embeddingSimilarity", r.embeddingSimilarity());
            a.put("outcomes", outcomes(o, occurred));
            analogs.add(a);
        }
        Map<String, Object> targetView = new LinkedHashMap<>();
        targetView.put("situationId", situationId);
        targetView.put("regionCode", null);
        targetView.put("from", target.startedAt().toString());
        targetView.put("to", target.end() == null ? null : target.end().toString());
        Map<String, Object> signatureView = new LinkedHashMap<>();
        signatureView.put("dayN", day);
        signatureView.put("computedAt", computedAt.toString());
        List<String> countries = countries(target.regionCodes());
        return result(targetView, k, result, signatureView, analogs, outcome, known, hits, countries,
                Map.of("situationId", situationId, "dayN", day));
    }

    /** A candidate's signature on {@code day}, else its last signature before it, else its first. */
    private SituationSignatureEntity signatureAt(String id, int day) {
        List<SituationSignatureEntity> all = signatureRows.findBySituationIdOrderByDayNAsc(id);
        SituationSignatureEntity best = null;
        for (SituationSignatureEntity s : all) {
            if (s.getDayN() <= day) {
                best = s;
            }
        }
        return best != null ? best : all.isEmpty() ? null : all.get(0);
    }

    /** The merge group of a situation: the situation it was merged into, else itself. */
    private static String group(SignatureService.SituationFacts s) {
        return s.mergedInto() != null ? s.mergedInto() : s.id();
    }

    /** Whether the target outcome happened after a past situation, or null when unknown. */
    Boolean occurred(Outcome outcome, List<String> regions, SituationOutcomeEntity o) {
        if (outcome.critical()) {
            return o.getPeakSeverity() == null ? null : "critical".equals(o.getPeakSeverity());
        }
        if (outcome.days() == 7) {
            return o.getFollowOn7d().contains(outcome.family());
        }
        if (outcome.days() == 30) {
            return o.getFollowOn30d().contains(outcome.family());
        }
        return signatures.familiesActive(regions, o.getReferenceAt(), outcome.days()).contains(outcome.family());
    }

    private static Map<String, Object> outcomes(SituationOutcomeEntity o, Boolean occurred) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("durationDays", o == null ? null : o.getDurationDays());
        out.put("peakSeverity", o == null ? null : o.getPeakSeverity());
        out.put("followOnFamilies7d", o == null ? List.of() : o.getFollowOn7d());
        out.put("followOnFamilies30d", o == null ? List.of() : o.getFollowOn30d());
        out.put("hypothesisOutcome", o == null ? null : o.getHypothesisOutcome());
        out.put("targetOutcomeOccurred", occurred);
        return out;
    }

    // ------------------------------------------------------------------------- region-period analogs

    /**
     * Region-period analogs ({@code AnalogQuery} → {@code AnalogResult}).
     *
     * @param body the query
     * @return the result
     * @throws ValidationException 400 for an invalid query
     */
    public Map<String, Object> forRegion(Map<String, Object> body) {
        List<FieldError> errors = new ArrayList<>();
        String region = body == null || body.get("regionCode") == null ? null : body.get("regionCode").toString();
        if (region == null || !REGION.matcher(region).matches() || "ZZ".equals(region)) {
            errors.add(new FieldError("regionCode", "must be a 040 region code"));
        }
        Instant now = clock.instant();
        Instant to = instant(body, "to", errors);
        Instant from = instant(body, "from", errors);
        if (to == null && errors.stream().noneMatch(e -> e.field().equals("to"))) {
            to = now;
        }
        if (from == null && to != null && errors.stream().noneMatch(e -> e.field().equals("from"))) {
            from = to.minus(DEFAULT_PERIOD);
        }
        if (from != null && to != null && (!from.isBefore(to) || Duration.between(from, to).compareTo(MAX_PERIOD) > 0
                || to.isAfter(now.plus(Duration.ofDays(1))))) {
            errors.add(new FieldError("to", "the period must be positive, at most 90 days and not in the future"));
        }
        int k = DEFAULT_K;
        Object kValue = body == null ? null : body.get("k");
        if (kValue != null) {
            if (!(kValue instanceof Number n) || n.intValue() < 1 || n.intValue() > 50) {
                errors.add(new FieldError("k", "must be 1 to 50"));
            } else {
                k = n.intValue();
            }
        }
        Outcome outcome = null;
        try {
            outcome = outcome(body == null || body.get("outcome") == null ? null : body.get("outcome").toString());
        } catch (ValidationException e) {
            errors.add(new FieldError("outcome", "must be critical or {family}/P{n}D"));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "The analog query is not valid.", errors);
        }
        String country = region.substring(0, 2);
        LocalDate fromWeek = ReferenceFamilies.weekStart(LocalDate.ofInstant(from, ZoneOffset.UTC));
        LocalDate toWeek = ReferenceFamilies.weekStart(LocalDate.ofInstant(to.minusSeconds(1), ZoneOffset.UTC))
                .plusWeeks(1);
        int weeks = (int) Math.max(1, java.time.temporal.ChronoUnit.WEEKS.between(fromWeek, toWeek));
        RegionClasses.Country covariates = regionClasses.country(country);
        String continent = covariates == null ? null : covariates.continent();
        List<String> pool = continent == null ? List.of(country) : regionClasses.members("continent:" + continent);
        LocalDate historyFrom = fromWeek.minusYears(REGION_YEARS);
        LocalDate historyTo = fromWeek.minusDays(30);
        Map<String, Map<String, Map<LocalDate, Long>>> history = weekly(pool, historyFrom, historyTo);
        Map<String, Map<LocalDate, Long>> target = weekly(List.of(region), fromWeek, toWeek)
                .getOrDefault(region, new TreeMap<>());
        addUnfolded(region, fromWeek, toWeek, target);
        Map<String, Double> sd = familySd(history);
        SignatureBuilder.Signature targetSignature = regionSignature(target, fromWeek, weeks, sd, covariates);
        List<AnalogSearch.Candidate> candidates = new ArrayList<>();
        Map<String, Object[]> windows = new HashMap<>();
        for (String c : pool) {
            Map<String, Map<LocalDate, Long>> rows = history.getOrDefault(c, Map.of());
            if (rows.isEmpty()) {
                continue;
            }
            RegionClasses.Country cc = regionClasses.country(c);
            for (LocalDate w = ReferenceFamilies.weekStart(historyFrom); !w.plusWeeks(weeks).isAfter(historyTo);
                    w = w.plusWeeks(weeks)) {
                SignatureBuilder.Signature sig = regionSignature(rows, w, weeks, sd, cc);
                if (sig.zero() || allFamiliesZero(sig)) {
                    continue;
                }
                String id = c + "/" + w;
                Instant start = w.atStartOfDay(ZoneOffset.UTC).toInstant();
                Instant end = w.plusWeeks(weeks).plusDays(30).atStartOfDay(ZoneOffset.UTC).toInstant();
                candidates.add(new AnalogSearch.Candidate(id, sig, start, end, null));
                windows.put(id, new Object[] {c, w});
            }
        }
        AnalogSearch.Result result = AnalogSearch.search(targetSignature, from, to, null, candidates, null, k);
        List<Map<String, Object>> analogs = new ArrayList<>();
        int known = 0;
        int hits = 0;
        for (AnalogSearch.Ranked r : result.analogs()) {
            Object[] window = windows.get(r.id());
            String c = (String) window[0];
            LocalDate w = (LocalDate) window[1];
            LocalDate end = w.plusWeeks(weeks);
            Map<String, Map<LocalDate, Long>> rows = history.getOrDefault(c, Map.of());
            List<String> f7 = active(rows, end, 7);
            List<String> f30 = active(rows, end, 30);
            Boolean occurred = null;
            if (outcome != null && !outcome.critical()) {
                occurred = active(rows, end, outcome.days()).contains(outcome.family());
                known++;
                if (occurred) {
                    hits++;
                }
            }
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("durationDays", null);
            o.put("peakSeverity", null);
            o.put("followOnFamilies7d", f7);
            o.put("followOnFamilies30d", f30);
            o.put("hypothesisOutcome", null);
            o.put("targetOutcomeOccurred", occurred);
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("situationId", null);
            a.put("title", c + ", " + w + " to " + end.minusDays(1));
            a.put("theme", null);
            a.put("regionCodes", List.of(c));
            a.put("period", period(w.atStartOfDay(ZoneOffset.UTC).toInstant(),
                    end.atStartOfDay(ZoneOffset.UTC).toInstant()));
            a.put("rank", r.rank());
            a.put("similarity", r.similarity());
            a.put("featureSimilarity", r.featureSimilarity());
            a.put("embeddingSimilarity", null);
            a.put("outcomes", o);
            analogs.add(a);
        }
        Map<String, Object> targetView = new LinkedHashMap<>();
        targetView.put("situationId", null);
        targetView.put("regionCode", region);
        targetView.put("from", from.toString());
        targetView.put("to", to.toString());
        Map<String, Object> signatureView = new LinkedHashMap<>();
        signatureView.put("dayN", (int) Math.max(1, Duration.between(from, to).toDays()));
        signatureView.put("computedAt", now.toString());
        Map<String, Object> out = result(targetView, k, result, signatureView, analogs, outcome, known, hits,
                List.of(country), Map.of("regionCode", region, "from", from.toString(), "to", to.toString()));
        if (outcome != null && outcome.critical()) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> caveats = (List<Map<String, Object>>) out.get("caveats");
            caveats.add(caveat("OUTCOME_NOT_APPLICABLE", "Region-period analogs have no severity, so 'critical' cannot"
                    + " be scored for them."));
        }
        out.put("methodNote", "feature similarity only: region-period analogs have no summary embedding");
        return out;
    }

    /** True when every {@code family:} feature is zero. */
    private static boolean allFamiliesZero(SignatureBuilder.Signature sig) {
        for (int i = 0; i < sig.names().size(); i++) {
            if (sig.names().get(i).startsWith("family:") && sig.values()[i] != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Weekly counts per region and family in {@code [from, to)}: for each family and week the largest count among the
     * sources, so overlapping sources are never added together.
     */
    Map<String, Map<String, Map<LocalDate, Long>>> weekly(List<String> regions, LocalDate from, LocalDate to) {
        Map<String, Map<String, Map<LocalDate, Long>>> out = new HashMap<>();
        if (regions.isEmpty() || !from.isBefore(to)) {
            return out;
        }
        jdbc.query("SELECT region_code, family, week_start, max(count) AS n FROM reference_history WHERE region_code IN"
                + " (:regions) AND week_start >= :from AND week_start < :to GROUP BY 1, 2, 3",
                new MapSqlParameterSource().addValue("regions", regions).addValue("from", Date.valueOf(from))
                        .addValue("to", Date.valueOf(to)), rs -> {
                    out.computeIfAbsent(rs.getString("region_code"), k -> new TreeMap<>())
                            .computeIfAbsent(rs.getString("family"), k -> new TreeMap<>())
                            .put(rs.getDate("week_start").toLocalDate(), rs.getLong("n"));
                });
        return out;
    }

    /** Adds Foresignal's canonical events of weeks the nightly fold has not reached yet. */
    private void addUnfolded(String region, LocalDate from, LocalDate to, Map<String, Map<LocalDate, Long>> target) {
        LocalDate folded = settings.get(SettingKeys.BASERATE_FOLD_WEEK, String.class).map(LocalDate::parse)
                .orElse(from);
        LocalDate start = folded.isAfter(from) ? folded : from;
        if (!start.isBefore(to)) {
            return;
        }
        for (WeeklyCounts.Row row : fold.rows(start, to)) {
            if (row.regionCode().equals(region)) {
                target.computeIfAbsent(row.family(), k -> new TreeMap<>()).merge(row.weekStart(), row.count(),
                        Math::max);
            }
        }
    }

    /** Each family's weekly standard deviation across the pool's country-weeks (floored at 1 by the builder). */
    private static Map<String, Double> familySd(Map<String, Map<String, Map<LocalDate, Long>>> history) {
        Map<String, List<Double>> values = new TreeMap<>();
        for (Map<String, Map<LocalDate, Long>> byFamily : history.values()) {
            byFamily.forEach((family, weeks) -> weeks.values().forEach(n -> values.computeIfAbsent(family,
                    k -> new ArrayList<>()).add(n.doubleValue())));
        }
        Map<String, Double> out = new TreeMap<>();
        values.forEach((family, v) -> out.put(family, SignatureService.sd(v.stream().mapToDouble(Double::doubleValue)
                .toArray())));
        return out;
    }

    /** The signature of a region over {@code weeks} weeks from {@code start}: weekly totals stand in for days. */
    static SignatureBuilder.Signature regionSignature(Map<String, Map<LocalDate, Long>> rows, LocalDate start, int weeks,
            Map<String, Double> sd, RegionClasses.Country covariates) {
        Map<String, Long> counts = new TreeMap<>();
        long[] series = new long[weeks];
        for (Map.Entry<String, Map<LocalDate, Long>> e : rows.entrySet()) {
            long total = 0;
            for (int i = 0; i < weeks; i++) {
                long n = e.getValue().getOrDefault(start.plusWeeks(i), 0L);
                total += n;
                series[i] += n;
            }
            if (total > 0) {
                counts.put(e.getKey(), total);
            }
        }
        return SignatureBuilder.build(new SignatureBuilder.Inputs(counts, sd, series, null,
                covariates == null ? null : covariates.continent(), covariates == null ? null
                        : covariates.incomeBand(), Map.of(), List.of()));
    }

    /** The families with events in the {@code days} after {@code end} (weeks starting in the window). */
    private static List<String> active(Map<String, Map<LocalDate, Long>> rows, LocalDate end, int days) {
        TreeSet<String> out = new TreeSet<>();
        LocalDate until = end.plusDays(days);
        rows.forEach((family, weeks) -> weeks.forEach((week, n) -> {
            if (n > 0 && !week.isBefore(end) && week.isBefore(until)) {
                out.add(family);
            }
        }));
        return List.copyOf(out);
    }

    // ------------------------------------------------------------------------- shared

    private Map<String, Object> result(Map<String, Object> target, int k, AnalogSearch.Result result,
            Map<String, Object> signature, List<Map<String, Object>> analogs, Outcome outcome, int known, int hits,
            List<String> countries, Map<String, Object> query) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("target", target);
        out.put("k", k);
        out.put("countFound", result.countFound());
        out.put("method", result.fused() ? "fused" : "feature-only");
        out.put("methodNote", result.fused() ? null : "feature similarity only: embeddings unavailable");
        out.put("signature", signature);
        out.put("analogs", analogs);
        List<Map<String, Object>> caveats = new ArrayList<>();
        if (!result.fused()) {
            caveats.add(caveat("FEATURE_ONLY", "Summary embeddings were not available; analogs are ranked by feature"
                    + " similarity alone."));
        }
        if (result.countFound() < k) {
            caveats.add(caveat("FEW_ANALOGS", "Only " + result.countFound() + " comparable cases were found."));
        }
        Map<String, Object> distribution = null;
        if (outcome != null) {
            BaseRate base = baseRate(outcome, countries);
            AnalogSearch.Distribution d = AnalogSearch.distribution(hits, known, base.value());
            distribution = new LinkedHashMap<>();
            distribution.put("outcome", outcome.text());
            distribution.put("analogCount", d.analogCount());
            distribution.put("hits", d.hits());
            distribution.put("frequency", d.frequency());
            distribution.put("baseRate", d.baseRate());
            distribution.put("baseRateId", base.id());
            distribution.put("probability", d.probability());
            Map<String, Object> interval = new LinkedHashMap<>();
            interval.put("lower", d.lower());
            interval.put("upper", d.upper());
            interval.put("level", 0.95);
            interval.put("method", "jeffreys");
            distribution.put("interval", interval);
            if (d.analogCount() < AnalogSearch.SHRINK) {
                caveats.add(caveat("SMALL_SAMPLE", "Only " + d.analogCount() + " analogs with a known outcome; the"
                        + " probability leans on the base rate (weight " + Math.round(d.weight() * 100) + "% on the"
                        + " analogs)."));
            }
            if (base.value() == null) {
                caveats.add(caveat("NO_BASE_RATE", "No base rate exists for this outcome; the analog frequency is not"
                        + " shrunk."));
            }
        }
        out.put("outcomeDistribution", distribution);
        out.put("caveats", caveats);
        Map<String, Object> detector = new LinkedHashMap<>();
        detector.put("name", AnalogSearch.NAME);
        detector.put("version", AnalogSearch.VERSION);
        detector.put("params", Map.of("top", AnalogSearch.TOP, "rrfK", AnalogSearch.RRF_K, "shrink",
                AnalogSearch.SHRINK, "signature", SignatureBuilder.NAME + "/" + SignatureBuilder.VERSION));
        out.put("detector", detector);
        out.put("manifestId", manifest(query, k, outcome));
        return out;
    }

    /**
     * A base rate for the outcome distribution.
     *
     * @param value the rate, or null
     * @param id the stored base rate's id, or null
     */
    private record BaseRate(Double value, String id) {}

    private BaseRate baseRate(Outcome outcome, List<String> countries) {
        if (outcome.critical()) {
            // Only situations that began, and outcomes recorded, by the decision time (INT-2).
            List<Map<String, Object>> rows = jdbc.queryForList("SELECT count(*) FILTER (WHERE o.peak_severity ="
                    + " 'critical') AS hits, count(*) AS n FROM situation_outcome o JOIN situation s ON s.id ="
                    + " o.situation_id WHERE s.private = false AND s.started_at <= :now AND o.computed_at <= :now",
                    new MapSqlParameterSource("now", Timestamp.from(clock.instant())));
            long n = rows.isEmpty() ? 0 : ((Number) rows.get(0).get("n")).longValue();
            long h = rows.isEmpty() ? 0 : ((Number) rows.get(0).get("hits")).longValue();
            return new BaseRate(n == 0 ? null : (double) h / n, null);
        }
        String window = outcome.days() <= 7 ? "P7D" : outcome.days() <= 28 ? "P28D" : "P90D";
        Map<String, Object> definition = new LinkedHashMap<>();
        definition.put("family", outcome.family());
        if (!countries.isEmpty()) {
            definition.put("regionCodes", countries);
        }
        definition.put("window", window);
        try {
            BaseRateService.QueryResult q = baseRates.query(definition);
            if (q.stored() != null) {
                return q.stored().getNWindows() == 0 ? new BaseRate(null, q.stored().getId())
                        : new BaseRate(q.stored().getPAtLeastOne(), q.stored().getId());
            }
            return q.computed().result().nWindows() == 0 ? new BaseRate(null, null)
                    : new BaseRate(q.computed().result().pAtLeastOne(), null);
        } catch (RuntimeException e) {
            return new BaseRate(null, null);
        }
    }

    private String manifest(Map<String, Object> query, int k, Outcome outcome) {
        ManifestService service = manifests.getIfAvailable();
        if (service == null) {
            return null;
        }
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("k", k);
        config.put("outcome", outcome == null ? null : outcome.text());
        config.put("signature", SignatureBuilder.NAME + "/" + SignatureBuilder.VERSION);
        try {
            return service.record(ManifestRequest.of(AnalogSearch.NAME, AnalogSearch.VERSION).config(config)
                    .query(query));
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static List<String> countries(List<String> regions) {
        TreeSet<String> out = new TreeSet<>();
        for (String r : regions) {
            if (r != null && r.length() >= 2 && !"ZZ".equals(r)) {
                out.add(r.substring(0, 2));
            }
        }
        return List.copyOf(out);
    }

    private static Map<String, Object> period(Instant start, Instant end) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("start", start.toString());
        out.put("end", end == null ? null : end.toString());
        return out;
    }

    private static Map<String, Object> caveat(String code, String text) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("code", code);
        out.put("text", text);
        return out;
    }

    private static Instant instant(Map<String, Object> body, String field, List<FieldError> errors) {
        Object value = body == null ? null : body.get(field);
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value.toString());
        } catch (RuntimeException e) {
            errors.add(new FieldError(field, "must be an RFC 3339 date-time"));
            return null;
        }
    }

    /** The stored outcome of a situation, if any (tests). */
    public Optional<SituationOutcomeEntity> storedOutcome(String id) {
        return outcomes.findById(id);
    }
}
