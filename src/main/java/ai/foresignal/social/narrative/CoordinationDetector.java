package ai.foresignal.social.narrative;

import ai.foresignal.analytics.math.MultipleTesting;
import ai.foresignal.analytics.math.RobustStats;
import ai.foresignal.chronicle.network.MinHashSketch;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Coordinated-amplification indicators of a narrative (prompt 136, {@code CoordinationDetector} 1.1.0): statistics on
 * the narrative against null models, never labels on accounts.
 *
 * <ul>
 *   <li><strong>Synchrony:</strong> the share of distinct authors who posted near-identical text (MinHash Jaccard
 *       ≥ {@value #NEAR_IDENTICAL}) within {@value #SYNC_SECONDS} s of another author, against the same narrative with
 *       each author's post times shifted by one random offset, circularly within the window ({@value #SHUFFLES}
 *       shuffles). The shift keeps every author's own rhythm and breaks only the alignment between authors; shuffling
 *       post times among the posts did not, because a cluster that is most of the narrative keeps its burst times
 *       under any permutation of them (1.1.0).
 *   <li><strong>Text duplication:</strong> the share of distinct authors in the largest near-duplicate cluster, against
 *       the day's other narratives (their median and scaled MAD).
 *   <li><strong>Account-age skew:</strong> the share of authors whose account was created within
 *       {@value #NEW_ACCOUNT_DAYS} days of their post, where the platform provides creation dates, against the
 *       platform's topic baseline (exact binomial tail).
 *   <li><strong>Co-repost bursts:</strong> the share of reposting authors in a pair that reposted the same
 *       ≥ {@value #CO_REPOST_MIN} posts within {@value #CO_REPOST_MINUTES} minutes of each other, against a
 *       degree-preserving null (repost targets permuted across repost events, {@value #SHUFFLES} shuffles).
 * </ul>
 * Permutation p-values use a normal approximation to the shuffled distribution (its mean and standard deviation), so
 * values smaller than one over the number of shuffles are representable. Across the day's narratives the p-values are
 * adjusted with Benjamini–Hochberg; a {@code social-coordination} finding needs at least two indicators with q below
 * {@value #ALPHA}. Seeded and deterministic. Pure.
 */
public final class CoordinationDetector {

    /** Near-identical text. */
    static final double NEAR_IDENTICAL = 0.9;

    /** The synchrony window. */
    static final int SYNC_SECONDS = 60;

    /** Shuffles of a permutation null. */
    static final int SHUFFLES = 200;

    /** An account this young at posting time is new. */
    static final int NEW_ACCOUNT_DAYS = 30;

    /** Shared reposts that make a pair. */
    static final int CO_REPOST_MIN = 5;

    /** The co-repost window. */
    static final int CO_REPOST_MINUTES = 10;

    /** The adjusted significance level. */
    public static final double ALPHA = 0.01;

    /** Indicators needed for a finding. */
    public static final int MIN_SIGNIFICANT = 2;

    /** The most posts examined per narrative (the latest). */
    static final int MAX_POSTS = 2_000;

    /** The fewest authors an indicator is computed for. */
    static final int MIN_AUTHORS = 5;

    private CoordinationDetector() {}

    /**
     * One post as the detector sees it.
     *
     * @param authorKey the author
     * @param createdAt the post time
     * @param signature the text signature, or null
     * @param accountCreatedAt when the account was created, or null when the platform does not say
     * @param repostOf the reposted post key, or null
     */
    public record Post(String authorKey, Instant createdAt, MinHashSketch signature, Instant accountCreatedAt,
            String repostOf) {}

    /**
     * One indicator.
     *
     * @param value the statistic, or null
     * @param nullExpectation its expectation under the null, or null
     * @param pValue the one-sided p-value, or null
     * @param qValue the Benjamini–Hochberg value, or null
     * @param n the authors it was computed over
     * @param available false when the data is missing (no creation dates, too few authors or reposts)
     */
    public record Indicator(Double value, Double nullExpectation, Double pValue, Double qValue, int n,
            boolean available) {

        /** An unavailable indicator. */
        public static Indicator unavailable(int n) {
            return new Indicator(null, null, null, null, n, false);
        }

        Indicator withQ(Double q) {
            return new Indicator(value, nullExpectation, pValue, q, n, available);
        }

        Indicator withNull(Double expectation, Double p) {
            return new Indicator(value, expectation, p, qValue, n, available && p != null);
        }

        boolean significant() {
            return qValue != null && qValue < ALPHA;
        }

        Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("value", round(value));
            out.put("nullExpectation", round(nullExpectation));
            out.put("pValue", pValue);
            out.put("qValue", qValue);
            out.put("n", n);
            out.put("available", available);
            return out;
        }
    }

    /**
     * The indicators of one narrative over one window.
     *
     * @param narrativeId the narrative
     * @param windowStart the window start
     * @param windowEnd the window end
     * @param synchrony synchrony
     * @param textDuplication text duplication
     * @param accountAgeSkew account-age skew
     * @param coRepostBursts co-repost bursts
     */
    public record Indicators(String narrativeId, Instant windowStart, Instant windowEnd, Indicator synchrony,
            Indicator textDuplication, Indicator accountAgeSkew, Indicator coRepostBursts) {

        /** The indicators by name. */
        public Map<String, Indicator> byName() {
            Map<String, Indicator> out = new LinkedHashMap<>();
            out.put("synchrony", synchrony);
            out.put("textDuplication", textDuplication);
            out.put("accountAgeSkew", accountAgeSkew);
            out.put("coRepostBursts", coRepostBursts);
            return out;
        }

        /** The names of the indicators with q below {@link #ALPHA}. */
        public List<String> significant() {
            List<String> out = new ArrayList<>();
            for (Map.Entry<String, Indicator> e : byName().entrySet()) {
                if (e.getValue().significant()) {
                    out.add(e.getKey());
                }
            }
            return out;
        }

        /** True when enough indicators are significant for a finding. */
        public boolean detected() {
            return significant().size() >= MIN_SIGNIFICANT;
        }

        /**
         * The {@code CoordinationIndicators} view.
         *
         * @param findingId the finding, or null
         * @param computedAt when it was computed
         */
        public Map<String, Object> toMap(String findingId, Instant computedAt) {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("narrativeId", narrativeId);
            Map<String, Object> window = new LinkedHashMap<>();
            window.put("start", windowStart.toString());
            window.put("end", windowEnd.toString());
            out.put("window", window);
            for (Map.Entry<String, Indicator> e : byName().entrySet()) {
                out.put(e.getKey(), e.getValue().toMap());
            }
            out.put("significantCount", significant().size());
            out.put("findingId", findingId);
            out.put("statement", detected() ? NarrativeVocabulary.COORDINATION_STATEMENT : null);
            Map<String, Object> detector = new LinkedHashMap<>();
            detector.put("name", NarrativeVocabulary.COORDINATION);
            detector.put("version", NarrativeVocabulary.COORDINATION_VERSION);
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("nearIdentical", NEAR_IDENTICAL);
            params.put("syncSeconds", SYNC_SECONDS);
            params.put("shuffles", SHUFFLES);
            params.put("newAccountDays", NEW_ACCOUNT_DAYS);
            params.put("coRepostMin", CO_REPOST_MIN);
            params.put("coRepostMinutes", CO_REPOST_MINUTES);
            params.put("alpha", ALPHA);
            detector.put("params", params);
            out.put("detector", detector);
            out.put("computedAt", computedAt.toString());
            return out;
        }

        Indicators with(Indicator s, Indicator t, Indicator a, Indicator c) {
            return new Indicators(narrativeId, windowStart, windowEnd, s, t, a, c);
        }
    }

    /**
     * Computes the indicators of one narrative (text duplication without its null, which needs the other narratives:
     * {@link #adjust}).
     *
     * @param narrativeId the narrative
     * @param posts its posts in the window
     * @param windowStart the window start
     * @param windowEnd the window end
     * @param newAccountBaseline the platform's topic share of new accounts, or null when unknown
     * @param seed the shuffle seed
     */
    public static Indicators compute(String narrativeId, List<Post> posts, Instant windowStart, Instant windowEnd,
            Double newAccountBaseline, long seed) {
        List<Post> sample = new ArrayList<>(posts);
        sample.sort(Comparator.comparing(Post::createdAt));
        if (sample.size() > MAX_POSTS) {
            sample = new ArrayList<>(sample.subList(sample.size() - MAX_POSTS, sample.size()));
        }
        Set<String> authors = new HashSet<>();
        for (Post p : sample) {
            authors.add(p.authorKey());
        }
        int[] cluster = clusters(sample);
        Indicator synchrony = synchrony(sample, cluster, authors.size(), windowStart, windowEnd, seed);
        Indicator duplication = duplication(sample, cluster, authors.size());
        Indicator age = accountAge(sample, newAccountBaseline);
        Indicator coRepost = coReposts(sample, seed);
        return new Indicators(narrativeId, windowStart, windowEnd, synchrony, duplication, age, coRepost);
    }

    /**
     * Completes the day's indicators: the text-duplication null from the other narratives, then Benjamini–Hochberg
     * across every available p-value.
     *
     * @param day the day's indicators
     * @return the completed indicators, in input order
     */
    public static List<Indicators> adjust(List<Indicators> day) {
        List<Indicators> withNull = new ArrayList<>();
        for (int i = 0; i < day.size(); i++) {
            Indicators current = day.get(i);
            Indicator t = current.textDuplication();
            if (t.value() == null) {
                withNull.add(current);
                continue;
            }
            List<Double> others = new ArrayList<>();
            for (int j = 0; j < day.size(); j++) {
                Indicator o = day.get(j).textDuplication();
                if (j != i && o.value() != null) {
                    others.add(o.value());
                }
            }
            if (others.size() < MIN_AUTHORS) {
                withNull.add(current.with(current.synchrony(), t.withNull(null, null), current.accountAgeSkew(),
                        current.coRepostBursts()));
                continue;
            }
            double[] values = others.stream().mapToDouble(Double::doubleValue).toArray();
            double median = RobustStats.median(values);
            double scale = Math.max(RobustStats.mad(values) * RobustStats.MAD_SCALE, 0.02);
            double p = upperTail((t.value() - median) / scale);
            withNull.add(current.with(current.synchrony(), t.withNull(median, p), current.accountAgeSkew(),
                    current.coRepostBursts()));
        }
        List<double[]> slots = new ArrayList<>();
        List<Double> ps = new ArrayList<>();
        for (int i = 0; i < withNull.size(); i++) {
            int k = 0;
            for (Indicator ind : withNull.get(i).byName().values()) {
                if (ind.available() && ind.pValue() != null) {
                    slots.add(new double[] {i, k});
                    ps.add(ind.pValue());
                }
                k++;
            }
        }
        double[] q = MultipleTesting.benjaminiHochberg(ps.stream().mapToDouble(Double::doubleValue).toArray());
        Map<Integer, Map<Integer, Double>> qOf = new HashMap<>();
        for (int s = 0; s < slots.size(); s++) {
            qOf.computeIfAbsent((int) slots.get(s)[0], x -> new HashMap<>()).put((int) slots.get(s)[1], q[s]);
        }
        List<Indicators> out = new ArrayList<>();
        for (int i = 0; i < withNull.size(); i++) {
            Indicators n = withNull.get(i);
            Map<Integer, Double> qs = qOf.getOrDefault(i, Map.of());
            out.add(n.with(n.synchrony().withQ(qs.get(0)), n.textDuplication().withQ(qs.get(1)),
                    n.accountAgeSkew().withQ(qs.get(2)), n.coRepostBursts().withQ(qs.get(3))));
        }
        return out;
    }

    /** Near-identical clusters: a cluster index per post (union-find over pairs). */
    static int[] clusters(List<Post> posts) {
        int n = posts.size();
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
        for (int i = 0; i < n; i++) {
            MinHashSketch a = posts.get(i).signature();
            if (a == null || a.isEmpty()) {
                continue;
            }
            for (int j = i + 1; j < n; j++) {
                MinHashSketch b = posts.get(j).signature();
                if (b != null && a.jaccard(b) >= NEAR_IDENTICAL) {
                    int ra = find(parent, i);
                    int rb = find(parent, j);
                    if (ra != rb) {
                        parent[Math.max(ra, rb)] = Math.min(ra, rb);
                    }
                }
            }
        }
        int[] out = new int[n];
        for (int i = 0; i < n; i++) {
            out[i] = find(parent, i);
        }
        return out;
    }

    private static int find(int[] parent, int i) {
        while (parent[i] != i) {
            parent[i] = parent[parent[i]];
            i = parent[i];
        }
        return i;
    }

    /**
     * Synchrony against its author-shift null: each shuffle moves every author's posts by one offset drawn uniformly
     * from the window, wrapping past its end, so authors keep their own spacing but no longer line up with each other.
     *
     * @param windowStart the window start, or null for the first post
     * @param windowEnd the window end, or null for the last post
     */
    static Indicator synchrony(List<Post> posts, int[] cluster, int authorCount, Instant windowStart, Instant windowEnd,
            long seed) {
        if (authorCount < MIN_AUTHORS) {
            return Indicator.unavailable(authorCount);
        }
        Instant[] times = new Instant[posts.size()];
        for (int i = 0; i < times.length; i++) {
            times[i] = posts.get(i).createdAt();
        }
        double observed = synchronyValue(posts, cluster, times, authorCount);
        Instant start = windowStart;
        Instant end = windowEnd;
        for (Instant t : times) {
            start = start == null || t.isBefore(start) ? t : start;
            end = end == null || t.isAfter(end) ? t : end;
        }
        long span = Math.max(1, Duration.between(start, end).getSeconds());
        Random random = new Random(seed);
        double[] nulls = new double[SHUFFLES];
        Instant[] shifted = new Instant[times.length];
        Map<String, Long> offsets = new HashMap<>();
        for (int r = 0; r < SHUFFLES; r++) {
            offsets.clear();
            for (int i = 0; i < times.length; i++) {
                long offset = offsets.computeIfAbsent(posts.get(i).authorKey(), a -> (long) (random.nextDouble() * span));
                long at = Math.floorMod(Duration.between(start, times[i]).getSeconds() + offset, span);
                shifted[i] = start.plusSeconds(at);
            }
            nulls[r] = synchronyValue(posts, cluster, shifted, authorCount);
        }
        return permutationIndicator(observed, nulls, authorCount);
    }

    /** The share of authors with a near-identical post by another author within the window. */
    static double synchronyValue(List<Post> posts, int[] cluster, Instant[] times, int authorCount) {
        Map<Integer, List<Integer>> byCluster = new HashMap<>();
        for (int i = 0; i < posts.size(); i++) {
            byCluster.computeIfAbsent(cluster[i], k -> new ArrayList<>()).add(i);
        }
        Set<String> synced = new HashSet<>();
        for (List<Integer> members : byCluster.values()) {
            if (members.size() < 2) {
                continue;
            }
            members.sort(Comparator.comparing(i -> times[i]));
            for (int a = 0; a < members.size(); a++) {
                int i = members.get(a);
                for (int b = a + 1; b < members.size(); b++) {
                    int j = members.get(b);
                    if (Duration.between(times[i], times[j]).getSeconds() > SYNC_SECONDS) {
                        break;
                    }
                    if (!posts.get(i).authorKey().equals(posts.get(j).authorKey())) {
                        synced.add(posts.get(i).authorKey());
                        synced.add(posts.get(j).authorKey());
                    }
                }
            }
        }
        return (double) synced.size() / authorCount;
    }

    static Indicator duplication(List<Post> posts, int[] cluster, int authorCount) {
        if (authorCount < MIN_AUTHORS) {
            return Indicator.unavailable(authorCount);
        }
        Map<Integer, Set<String>> authorsOf = new HashMap<>();
        for (int i = 0; i < posts.size(); i++) {
            authorsOf.computeIfAbsent(cluster[i], k -> new HashSet<>()).add(posts.get(i).authorKey());
        }
        int largest = 0;
        for (Set<String> s : authorsOf.values()) {
            largest = Math.max(largest, s.size());
        }
        double value = largest < 2 ? 0 : (double) largest / authorCount;
        return new Indicator(value, null, null, null, authorCount, true);
    }

    static Indicator accountAge(List<Post> posts, Double baseline) {
        Map<String, Boolean> young = new HashMap<>();
        for (Post p : posts) {
            if (p.accountCreatedAt() == null || young.containsKey(p.authorKey())) {
                continue;
            }
            young.put(p.authorKey(), Duration.between(p.accountCreatedAt(), p.createdAt()).toDays() < NEW_ACCOUNT_DAYS);
        }
        int n = young.size();
        if (n < MIN_AUTHORS || baseline == null || baseline <= 0 || baseline >= 1) {
            return Indicator.unavailable(n);
        }
        int k = 0;
        for (boolean y : young.values()) {
            if (y) {
                k++;
            }
        }
        return new Indicator((double) k / n, baseline, binomialUpperTail(k, n, baseline), null, n, true);
    }

    static Indicator coReposts(List<Post> posts, long seed) {
        List<String> authors = new ArrayList<>();
        List<String> targets = new ArrayList<>();
        List<Instant> times = new ArrayList<>();
        for (Post p : posts) {
            if (p.repostOf() != null) {
                authors.add(p.authorKey());
                targets.add(p.repostOf());
                times.add(p.createdAt());
            }
        }
        Set<String> distinct = new HashSet<>(authors);
        if (distinct.size() < MIN_AUTHORS || authors.size() < CO_REPOST_MIN * 2) {
            return Indicator.unavailable(distinct.size());
        }
        double observed = coRepostValue(authors, targets, times, distinct.size());
        Random random = new Random(seed ^ 0x5DEECE66DL);
        double[] nulls = new double[SHUFFLES];
        List<String> shuffled = new ArrayList<>(targets);
        for (int r = 0; r < SHUFFLES; r++) {
            java.util.Collections.shuffle(shuffled, random);
            nulls[r] = coRepostValue(authors, shuffled, times, distinct.size());
        }
        return permutationIndicator(observed, nulls, distinct.size());
    }

    /** The share of reposting authors in a pair with ≥ 5 shared reposts within 10 minutes. */
    static double coRepostValue(List<String> authors, List<String> targets, List<Instant> times, int authorCount) {
        Map<String, List<Integer>> byTarget = new HashMap<>();
        for (int i = 0; i < targets.size(); i++) {
            byTarget.computeIfAbsent(targets.get(i), k -> new ArrayList<>()).add(i);
        }
        Map<String, Set<String>> pairTargets = new HashMap<>();
        for (Map.Entry<String, List<Integer>> e : byTarget.entrySet()) {
            List<Integer> idx = e.getValue();
            idx.sort(Comparator.comparing(times::get));
            int limit = Math.min(idx.size(), 200);
            for (int a = 0; a < limit; a++) {
                for (int b = a + 1; b < limit; b++) {
                    int i = idx.get(a);
                    int j = idx.get(b);
                    if (Duration.between(times.get(i), times.get(j)).toMinutes() > CO_REPOST_MINUTES) {
                        break;
                    }
                    String x = authors.get(i);
                    String y = authors.get(j);
                    if (x.equals(y)) {
                        continue;
                    }
                    String pair = x.compareTo(y) < 0 ? x + "|" + y : y + "|" + x;
                    pairTargets.computeIfAbsent(pair, k -> new HashSet<>()).add(e.getKey());
                }
            }
        }
        Set<String> inPairs = new HashSet<>();
        for (Map.Entry<String, Set<String>> e : pairTargets.entrySet()) {
            if (e.getValue().size() >= CO_REPOST_MIN) {
                String[] two = e.getKey().split("\\|", 2);
                inPairs.add(two[0]);
                inPairs.add(two[1]);
            }
        }
        return (double) inPairs.size() / authorCount;
    }

    private static Indicator permutationIndicator(double observed, double[] nulls, int n) {
        double mean = RobustStats.mean(nulls);
        double sd = RobustStats.sd(nulls);
        double p;
        if (!(sd > 1e-9)) {
            p = observed > mean + 1e-9 ? upperTail(10) : 1.0;
        } else {
            p = upperTail((observed - mean) / sd);
        }
        return new Indicator(observed, mean, p, null, n, true);
    }

    /** The standard normal upper tail P(Z ≥ z). */
    static double upperTail(double z) {
        if (Double.isNaN(z)) {
            return 1;
        }
        if (z < 0) {
            return 1 - upperTail(-z);
        }
        if (z > 6) {
            double phi = Math.exp(-z * z / 2) / Math.sqrt(2 * Math.PI);
            return phi / z * (1 - 1 / (z * z) + 3 / (z * z * z * z));
        }
        double t = 1 / (1 + 0.3275911 * z / Math.sqrt(2));
        double poly = t * (0.254829592 + t * (-0.284496736 + t * (1.421413741 + t * (-1.453152027
                + t * 1.061405429))));
        return 0.5 * poly * Math.exp(-z * z / 2);
    }

    /** The exact binomial upper tail P(X ≥ k) for n trials of probability p. */
    static double binomialUpperTail(int k, int n, double p) {
        if (k <= 0) {
            return 1;
        }
        if (k > n) {
            return 0;
        }
        double logP = Math.log(p);
        double logQ = Math.log1p(-p);
        double logPmf = n * logQ;
        double[] logs = new double[n + 1];
        logs[0] = logPmf;
        for (int i = 0; i < n; i++) {
            logPmf += Math.log((double) (n - i) / (i + 1)) + logP - logQ;
            logs[i + 1] = logPmf;
        }
        double max = Double.NEGATIVE_INFINITY;
        for (int i = k; i <= n; i++) {
            max = Math.max(max, logs[i]);
        }
        double sum = 0;
        for (int i = k; i <= n; i++) {
            sum += Math.exp(logs[i] - max);
        }
        return Math.min(1, Math.exp(max + Math.log(sum)));
    }

    private static Double round(Double v) {
        return v == null ? null : Math.round(v * 10_000) / 10_000.0;
    }
}
