package ai.foresignal.impact.rate;

import ai.foresignal.baserate.BaseRateCalculator;
import ai.foresignal.baserate.RegionClasses;
import ai.foresignal.impact.ImpactPairs;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Conditional impact rates {@code P(impact | hazard class)} (prompt 127 §3) from the historical co-occurrence of
 * hazard records and impact records in the same admin1 region, with the 121 base-rate interval (95% Jeffreys,
 * {@link BaseRateCalculator#jeffreys}) and the sample size.
 *
 * <ul>
 *   <li><b>Region classes</b> are the 121 {@link RegionClasses} continent classes ({@code continent:NA}); an admin1
 *       region belongs to its country's class.
 *   <li><b>History</b>: the last {@value #LOOKBACK_DAYS} days, ending one lag before now so every counted occurrence has
 *       had its whole lag to be followed by the impact.
 *   <li><b>Minimum n</b>: {@value #MIN_N} hazard occurrences; fewer is {@code insufficient} (rate and interval null),
 *       and the projector skips the pair for that region class. Unknown is no projection, never a guess.
 *   <li>Rates are cached for {@link #TTL}.
 * </ul>
 */
@Service
public class ImpactRateService {

    /** The fewest hazard occurrences a rate needs. */
    public static final int MIN_N = 20;

    /** The days of history counted. */
    public static final int LOOKBACK_DAYS = 1825;

    /** How long a computed rate is reused. */
    public static final Duration TTL = Duration.ofHours(24);

    /** The status of a usable rate. */
    public static final String OK = "ok";

    /** The status of a rate with too few occurrences. */
    public static final String INSUFFICIENT = "insufficient";

    /** The source of a rate computed from Foresignal's own records. */
    public static final String OWN_HISTORY = "own-history";

    /**
     * A conditional rate, the {@code ImpactRate} schema.
     *
     * @param hazardClass the hazard class
     * @param impactFamily the impact family
     * @param regionClass the region class
     * @param lagHours the lag
     * @param rate {@code impacts / n}, or null when insufficient
     * @param lower the interval's lower bound, or null
     * @param upper the interval's upper bound, or null
     * @param n hazard occurrences
     * @param impacts occurrences followed by the impact
     * @param source {@value #OWN_HISTORY}
     * @param status {@value #OK} or {@value #INSUFFICIENT}
     * @param computedAt when it was computed
     */
    public record ImpactRate(String hazardClass, String impactFamily, String regionClass, int lagHours, Double rate,
            Double lower, Double upper, long n, long impacts, String source, String status, Instant computedAt) {

        /** True when the rate can be used. */
        public boolean usable() {
            return OK.equals(status) && rate != null;
        }

        /** The {@code ImpactRate} view. */
        public Map<String, Object> view() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("hazardClass", hazardClass);
            out.put("impactFamily", impactFamily);
            out.put("regionClass", regionClass);
            out.put("lagHours", lagHours);
            out.put("rate", rate);
            if (lower == null || upper == null) {
                out.put("interval", null);
            } else {
                Map<String, Object> interval = new LinkedHashMap<>();
                interval.put("lo", lower);
                interval.put("hi", upper);
                out.put("interval", interval);
            }
            out.put("n", n);
            out.put("nImpacts", impacts);
            out.put("source", source);
            out.put("status", status);
            out.put("baseRateId", null);
            out.put("computedAt", computedAt.toString());
            return out;
        }
    }

    private final ImpactPairs pairs;
    private final CoOccurrenceCounter counter;
    private final RegionClasses regionClasses;
    private final Clock clock;
    private final Map<String, ImpactRate> cache = new ConcurrentHashMap<>();

    /** Creates the service. */
    public ImpactRateService(ImpactPairs pairs, CoOccurrenceCounter counter, RegionClasses regionClasses,
            Clock clock) {
        this.pairs = pairs;
        this.counter = counter;
        this.regionClasses = regionClasses;
        this.clock = clock;
    }

    /** The region class of an admin1 region or country ({@code continent:XX}), or null when unknown. */
    public String regionClassOf(String regionCode) {
        RegionClasses.Country country = regionCode == null ? null : regionClasses.country(regionCode);
        return country == null || country.continent() == null ? null : "continent:" + country.continent();
    }

    /**
     * The rate of a built-in pair in a region class.
     *
     * @param hazardClass the hazard class
     * @param impactFamily the impact family
     * @param regionClass the region class
     * @return the rate, or null when no built-in pair joins the class and the family or the region class is unknown
     */
    public ImpactRate rate(String hazardClass, String impactFamily, String regionClass) {
        ImpactPairs.Pair pair = pairs.pairs().stream().filter(p -> p.hazardClass().equals(hazardClass)
                && p.impactFamily().equals(impactFamily)).findFirst().orElse(null);
        if (pair == null || !RegionClasses.valid(regionClass)) {
            return null;
        }
        String key = hazardClass + "|" + impactFamily + "|" + regionClass;
        Instant now = clock.instant();
        ImpactRate cached = cache.get(key);
        if (cached != null && cached.computedAt().plus(TTL).isAfter(now)) {
            return cached;
        }
        ImpactRate computed = compute(pair, regionClass, now);
        cache.put(key, computed);
        return computed;
    }

    /**
     * The rates of the built-in pairs matching the filters, in pair order and then region-class order.
     *
     * @param hazardClass a hazard class, or null for any
     * @param impactFamily an impact family, or null for any
     * @param regionClass a region class, or null for every continent class
     */
    public List<ImpactRate> list(String hazardClass, String impactFamily, String regionClass) {
        List<String> classes = new ArrayList<>();
        if (regionClass != null) {
            classes.add(regionClass);
        } else {
            for (String c : RegionClasses.CONTINENTS) {
                classes.add("continent:" + c);
            }
        }
        List<ImpactRate> out = new ArrayList<>();
        for (ImpactPairs.Pair pair : pairs.pairs()) {
            if ((hazardClass != null && !hazardClass.equals(pair.hazardClass()))
                    || (impactFamily != null && !impactFamily.equals(pair.impactFamily()))) {
                continue;
            }
            for (String c : classes) {
                ImpactRate r = rate(pair.hazardClass(), pair.impactFamily(), c);
                if (r != null) {
                    out.add(r);
                }
            }
        }
        return out;
    }

    /** Forgets every cached rate (tests, and after a reference import). */
    public void clearCache() {
        cache.clear();
    }

    /** Computes a rate from the counter. */
    ImpactRate compute(ImpactPairs.Pair pair, String regionClass, Instant now) {
        ImpactPairs.HazardClass hazard = pairs.hazardClass(pair.hazardClass());
        ImpactPairs.ImpactFamily family = pairs.family(pair.impactFamily());
        Instant to = now.minus(Duration.ofHours(pair.lagHours()));
        Instant from = now.minus(Duration.ofDays(LOOKBACK_DAYS));
        CoOccurrenceCounter.Counts counts = counter.count(hazard, family, pair.lagHours(),
                regionClasses.members(regionClass), from, to);
        return of(pair, regionClass, counts, now);
    }

    /**
     * The rate of a pair from its counts: {@code impacts / n} with a 95% Jeffreys interval, or {@code insufficient}
     * below {@value #MIN_N}. Pure.
     */
    public static ImpactRate of(ImpactPairs.Pair pair, String regionClass, CoOccurrenceCounter.Counts counts,
            Instant at) {
        long n = counts.n();
        long x = Math.min(counts.impacts(), n);
        if (n < MIN_N) {
            return new ImpactRate(pair.hazardClass(), pair.impactFamily(), regionClass, pair.lagHours(), null, null,
                    null, n, x, OWN_HISTORY, INSUFFICIENT, at);
        }
        double[] interval = BaseRateCalculator.jeffreys(x, n);
        return new ImpactRate(pair.hazardClass(), pair.impactFamily(), regionClass, pair.lagHours(), (double) x / n,
                interval[0], interval[1], n, x, OWN_HISTORY, OK, at);
    }
}
