package ai.foresignal.forecast.language;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns probabilities into ICD 203 likelihood terms and lints generated text (prompt 122, Intelligence-Design §6.4).
 * Numbers are the truth: a probability is stored as a number and its term is derived here; generated text is checked
 * so that no term disagrees with its number, the two ICD term rows are never mixed, and no sentence carries both a
 * likelihood term and a confidence level.
 *
 * <p><b>Bands</b> (the first ICD row only). The scale is symmetric around one half: below it a band is closed at its
 * lower bound and open at its upper bound, above it the reverse, and the middle band is closed at both ends.
 *
 * <table>
 *   <caption>Bands</caption>
 *   <tr><th>Term</th><th>Probability</th></tr>
 *   <tr><td>almost no chance</td><td>[0, 0.05)</td></tr>
 *   <tr><td>very unlikely</td><td>[0.05, 0.20)</td></tr>
 *   <tr><td>unlikely</td><td>[0.20, 0.45)</td></tr>
 *   <tr><td>roughly even chance</td><td>[0.45, 0.55]</td></tr>
 *   <tr><td>likely</td><td>(0.55, 0.80]</td></tr>
 *   <tr><td>very likely</td><td>(0.80, 0.95]</td></tr>
 *   <tr><td>almost certain</td><td>(0.95, 1]</td></tr>
 * </table>
 *
 * <p>ICD 203 states the end bands as 1–5% and 95–99%; probabilities outside [0.01, 0.99] still map to the end terms,
 * so every probability has exactly one term and {@code term(p)} and {@code term(1 − p)} are mirror terms.
 *
 * <p><b>Lint</b> ({@link #lint}) reports, with character offsets:
 * <ul>
 *   <li>{@link #TERM_MISMATCH}: a likelihood term whose band disagrees with the number it states: the number of a
 *       {@link Claim} whose span contains the term, or a percentage ({@code 70%}) in the same sentence;</li>
 *   <li>{@link #MIXED_ROWS}: a term of the second ICD row (remote, highly improbable, improbable, roughly even odds,
 *       probable, highly probable, nearly certain) in text that also uses the first row (reported at each second-row
 *       term);</li>
 *   <li>{@link #TERM_WITH_CONFIDENCE}: a likelihood term and a confidence level ({@code low}, {@code moderate} or
 *       {@code high confidence}) in the same sentence.</li>
 * </ul>
 * Terms are matched case-insensitively on word boundaries, longest first, so {@code very unlikely} is never also read
 * as {@code unlikely} or {@code likely}.
 */
public final class EstimativeLanguage {

    /** A term disagrees with its number. */
    public static final String TERM_MISMATCH = "TERM_MISMATCH";

    /** The two ICD term rows are mixed. */
    public static final String MIXED_ROWS = "MIXED_ROWS";

    /** A likelihood term and a confidence level share a sentence. */
    public static final String TERM_WITH_CONFIDENCE = "TERM_WITH_CONFIDENCE";

    /** The first ICD row, lowest band first: the only terms Foresignal generates. */
    public static final List<String> TERMS = List.of("almost no chance", "very unlikely", "unlikely",
            "roughly even chance", "likely", "very likely", "almost certain");

    /** The second ICD row, in the same band order; never generated and never mixed with the first. */
    public static final List<String> SECOND_ROW = List.of("remote", "highly improbable", "improbable",
            "roughly even odds", "probable", "highly probable", "nearly certain");

    /** The band bounds between consecutive terms. */
    static final double[] BOUNDS = {0.05, 0.20, 0.45, 0.55, 0.80, 0.95};

    /** How close to a band bound a probability counts as on it, absorbing floating-point rounding. */
    static final double BOUND_TOLERANCE = 1e-12;

    private static final Pattern CONFIDENCE = Pattern.compile("\\b(low|moderate|high)[ -]confidence\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern PERCENT = Pattern.compile("(?<![\\w.])(\\d{1,3}(?:\\.\\d+)?)\\s?%");

    private static final List<TermPattern> PATTERNS = patterns();

    private EstimativeLanguage() {}

    /**
     * One band of the scale.
     *
     * @param index the band index, 0 (almost no chance) to 6 (almost certain)
     * @param term the first-row term
     * @param lower the lower bound
     * @param upper the upper bound
     * @param lowerInclusive whether {@code lower} belongs to the band
     * @param upperInclusive whether {@code upper} belongs to the band
     */
    public record Band(int index, String term, double lower, double upper, boolean lowerInclusive,
            boolean upperInclusive) {

        /** True when {@code p} falls in this band. */
        public boolean contains(double p) {
            boolean aboveLower = lowerInclusive ? p >= lower : p > lower;
            boolean belowUpper = upperInclusive ? p <= upper : p < upper;
            return aboveLower && belowUpper;
        }
    }

    /**
     * A numeric claim in a text, for {@link #lint}.
     *
     * @param probability the claim's probability
     * @param start the first character of the text the claim covers (inclusive)
     * @param end the end of the text the claim covers (exclusive); a term inside {@code [start, end)} must agree
     */
    public record Claim(double probability, int start, int end) {}

    /**
     * A lint violation.
     *
     * @param code {@link #TERM_MISMATCH}, {@link #MIXED_ROWS} or {@link #TERM_WITH_CONFIDENCE}
     * @param term the offending term as written
     * @param start its first character
     * @param end its end (exclusive)
     * @param message what is wrong
     */
    public record Violation(String code, String term, int start, int end, String message) {}

    /**
     * The ICD 203 term of a probability.
     *
     * @throws IllegalArgumentException when {@code p} is not a probability
     */
    public static String term(double p) {
        return band(p).term();
    }

    /**
     * The band of a probability.
     *
     * @throws IllegalArgumentException when {@code p} is not in [0, 1]
     */
    public static Band band(double p) {
        if (Double.isNaN(p) || p < 0 || p > 1) {
            throw new IllegalArgumentException("not a probability: " + p);
        }
        double snapped = snapToBound(p);
        for (int i = 0; i < TERMS.size(); i++) {
            Band band = bandAt(i);
            if (band.contains(snapped)) {
                return band;
            }
        }
        throw new IllegalStateException("no band for " + p);
    }

    /**
     * Returns the band bound {@code p} lies within {@value #BOUND_TOLERANCE} of, or {@code p} itself. In binary floating
     * point {@code 1 − 0.55} is {@code 0.44999999999999996}, so without this the mirror of a probability on a bound
     * would land in the neighboring band.
     */
    private static double snapToBound(double p) {
        for (double bound : BOUNDS) {
            if (Math.abs(p - bound) < BOUND_TOLERANCE) {
                return bound;
            }
        }
        return p;
    }

    /** The band at {@code index} (0–6). */
    public static Band bandAt(int index) {
        double lower = index == 0 ? 0.0 : BOUNDS[index - 1];
        double upper = index == TERMS.size() - 1 ? 1.0 : BOUNDS[index];
        boolean lowerInclusive = index <= 3;
        boolean upperInclusive = index >= 3;
        return new Band(index, TERMS.get(index), lower, upper, lowerInclusive, upperInclusive);
    }

    /** True when {@code p0} and {@code p1} fall in different bands (a band change). */
    public static boolean crossesBand(double p0, double p1) {
        return band(p0).index() != band(p1).index();
    }

    /** The term of a probability, or null for null. */
    public static String termOrNull(Double p) {
        return p == null || p.isNaN() ? null : term(Math.max(0, Math.min(1, p)));
    }

    /**
     * Lints a text.
     *
     * @param text the text
     * @param claims the numeric claims the text states (may be empty)
     * @return the violations in text order (empty when the text is clean)
     */
    public static List<Violation> lint(String text, List<Claim> claims) {
        List<Violation> out = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return out;
        }
        List<Match> matches = find(text);
        boolean firstRow = matches.stream().anyMatch(m -> !m.secondRow());
        List<int[]> sentences = sentences(text);
        for (Match m : matches) {
            if (m.secondRow() && firstRow) {
                out.add(new Violation(MIXED_ROWS, text.substring(m.start(), m.end()), m.start(), m.end(),
                        "'" + text.substring(m.start(), m.end()) + "' is a second-row ICD 203 term in text that uses"
                                + " the first row; use '" + TERMS.get(m.band()) + "'"));
            }
            for (Claim claim : claims == null ? List.<Claim>of() : claims) {
                if (m.start() >= claim.start() && m.end() <= claim.end() && !Double.isNaN(claim.probability())) {
                    int expected = band(clamp(claim.probability())).index();
                    if (expected != m.band()) {
                        out.add(mismatch(text, m, claim.probability()));
                    }
                }
            }
            int[] sentence = sentenceOf(sentences, m.start());
            String within = text.substring(sentence[0], sentence[1]);
            Matcher percent = PERCENT.matcher(within);
            while (percent.find()) {
                double p = Double.parseDouble(percent.group(1)) / 100.0;
                if (p >= 0 && p <= 1 && band(p).index() != m.band()) {
                    out.add(mismatch(text, m, p));
                    break;
                }
            }
            Matcher confidence = CONFIDENCE.matcher(within);
            if (confidence.find()) {
                out.add(new Violation(TERM_WITH_CONFIDENCE, text.substring(m.start(), m.end()), m.start(), m.end(),
                        "the likelihood term '" + text.substring(m.start(), m.end()) + "' and the confidence level '"
                                + confidence.group() + "' share a sentence; state them in separate sentences"));
            }
        }
        // Second-row terms alone are not mixed: nothing is reported for them.
        return dedupe(out);
    }

    /** True when {@link #lint} finds nothing. */
    public static boolean clean(String text, List<Claim> claims) {
        return lint(text, claims).isEmpty();
    }

    private static Violation mismatch(String text, Match m, double p) {
        String written = text.substring(m.start(), m.end());
        return new Violation(TERM_MISMATCH, written, m.start(), m.end(), "'" + written + "' disagrees with "
                + Math.round(p * 1000) / 10.0 + "%, which is '" + term(clamp(p)) + "'");
    }

    private static double clamp(double p) {
        return Math.max(0, Math.min(1, p));
    }

    private static List<Violation> dedupe(List<Violation> in) {
        List<Violation> out = new ArrayList<>();
        for (Violation v : in) {
            boolean seen = out.stream().anyMatch(o -> o.code().equals(v.code()) && o.start() == v.start());
            if (!seen) {
                out.add(v);
            }
        }
        out.sort(Comparator.comparingInt(Violation::start).thenComparing(Violation::code));
        return out;
    }

    /** A matched term. */
    private record Match(int start, int end, int band, boolean secondRow) {}

    /** A term pattern. */
    private record TermPattern(Pattern pattern, int band, boolean secondRow, int length) {}

    private static List<TermPattern> patterns() {
        List<TermPattern> out = new ArrayList<>();
        for (int i = 0; i < TERMS.size(); i++) {
            out.add(pattern(TERMS.get(i), i, false));
            out.add(pattern(SECOND_ROW.get(i), i, true));
        }
        out.sort(Comparator.comparingInt(TermPattern::length).reversed());
        return List.copyOf(out);
    }

    private static TermPattern pattern(String term, int band, boolean secondRow) {
        String regex = "\\b" + Pattern.quote(term).replace(" ", "\\E\\s+\\Q") + "\\b";
        return new TermPattern(Pattern.compile(regex, Pattern.CASE_INSENSITIVE), band, secondRow, term.length());
    }

    /** The terms in {@code text}, longest first without overlaps, then in text order. */
    private static List<Match> find(String text) {
        boolean[] used = new boolean[text.length()];
        List<Match> out = new ArrayList<>();
        for (TermPattern p : PATTERNS) {
            Matcher m = p.pattern().matcher(text);
            while (m.find()) {
                boolean free = true;
                for (int i = m.start(); i < m.end(); i++) {
                    if (used[i]) {
                        free = false;
                        break;
                    }
                }
                if (!free) {
                    continue;
                }
                for (int i = m.start(); i < m.end(); i++) {
                    used[i] = true;
                }
                out.add(new Match(m.start(), m.end(), p.band(), p.secondRow()));
            }
        }
        out.sort(Comparator.comparingInt(Match::start));
        return out;
    }

    /** Sentence spans: split after {@code .}, {@code !} or {@code ?} followed by white space, and at line breaks. */
    static List<int[]> sentences(String text) {
        List<int[]> out = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            boolean end = c == '\n'
                    || ((c == '.' || c == '!' || c == '?') && (i + 1 == text.length()
                            || Character.isWhitespace(text.charAt(i + 1))));
            if (end) {
                out.add(new int[] {start, i + 1});
                start = i + 1;
            }
        }
        if (start < text.length()) {
            out.add(new int[] {start, text.length()});
        }
        return out;
    }

    private static int[] sentenceOf(List<int[]> sentences, int offset) {
        for (int[] s : sentences) {
            if (offset >= s[0] && offset < s[1]) {
                return s;
            }
        }
        return new int[] {0, 0};
    }
}
