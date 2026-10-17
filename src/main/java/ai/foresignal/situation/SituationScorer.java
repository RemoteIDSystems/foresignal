package ai.foresignal.situation;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Geometry;

/**
 * The situation linker's score (prompt 118 §2), a pure function of one input, one candidate situation, an optional
 * text-embedding cosine and the {@link LinkerWeights}:
 *
 * <pre>
 *   raw    = region + distance + entity + topic + textSimilarity + theme      (clamped to [0, 1])
 *   score  = raw × (0.5 + 0.5·exp(−hours since the situation's last activity / 72))
 * </pre>
 *
 * <p>Region is 0.30 for a shared first-level region, else 0.20 for a shared country; distance is
 * {@code 0.25 × max(0, 1 − d / maxDistanceKm)} with {@code maxDistanceKm} 300 (500 when the input or the
 * situation is maritime); entity 0.25 for a shared entity; topic 0.15 when the input's text matches a keyword of
 * one of the situation's topics; text similarity {@code 0.20 × max(0, (cos − 0.70) / 0.30)} when both have
 * embeddings; theme −0.20 when the themes are incompatible ({@link SituationThemes#compatible}). The score is
 * rounded to nine decimals, so a sum such as 0.30 + 0.15 compares equal to the 0.45 threshold.
 */
public final class SituationScorer {

    /** The linker name stored with every decision. */
    public static final String NAME = "situation-linker";

    /** The linker version. */
    public static final String VERSION = "1.0.0";

    private SituationScorer() {}

    /**
     * What the scorer needs of a candidate situation.
     *
     * @param id the situation id
     * @param theme the theme
     * @param status the status
     * @param regionCodes the region codes
     * @param geom the situation geometry, or null
     * @param entityIds the entities
     * @param topicKeywords the keywords of its linked topics
     * @param lastActivityAt its last activity
     */
    public record Candidate(String id, String theme, String status, Set<String> regionCodes, Geometry geom,
            Set<String> entityIds, List<String> topicKeywords, Instant lastActivityAt) {}

    /**
     * A score and its components.
     *
     * @param score the score in [0, 1]
     * @param components the components as stored in {@code join_reason.components}
     */
    public record Score(double score, Map<String, Object> components) {}

    /**
     * True when the candidate passes the cheap prefilter: a shared region code, its geometry within
     * {@code maxDistanceKm}, a shared entity or a matching topic keyword.
     */
    public static boolean prefilter(LinkerInput input, Candidate candidate, LinkerWeights weights) {
        if (!intersection(input.regionCodes(), candidate.regionCodes()).isEmpty()) {
            return true;
        }
        Double d = SituationGeometry.distanceKm(input.geom(), candidate.geom());
        if (d != null && d <= maxDistanceKm(input, candidate, weights)) {
            return true;
        }
        if (!intersection(input.entityIds(), candidate.entityIds()).isEmpty()) {
            return true;
        }
        return topicMatch(input.text(), candidate.topicKeywords());
    }

    /**
     * Scores {@code input} against {@code candidate}.
     *
     * @param cosine the cosine similarity of the two embeddings, or null when either has none
     */
    public static Score score(LinkerInput input, Candidate candidate, Double cosine, LinkerWeights weights) {
        double region = region(input.regionCodes(), candidate.regionCodes(), weights);
        Double distanceKm = SituationGeometry.distanceKm(input.geom(), candidate.geom());
        double distance = distanceKm == null ? 0
                : weights.distance() * Math.max(0, 1 - distanceKm / maxDistanceKm(input, candidate, weights));
        double entity = intersection(input.entityIds(), candidate.entityIds()).isEmpty() ? 0 : weights.entity();
        double topic = topicMatch(input.text(), candidate.topicKeywords()) ? weights.topic() : 0;
        Double text = cosine == null ? null : textTerm(cosine, weights);
        double theme = SituationThemes.compatible(input.theme(), candidate.theme()) ? 0 : weights.themePenalty();
        double hours = recencyHours(input.observedAt(), candidate.lastActivityAt());
        double raw = region + distance + entity + topic + (text == null ? 0 : text) + theme;
        raw = Math.max(0, Math.min(1, raw));
        double score = round(raw * recency(hours, weights));
        Map<String, Object> components = new LinkedHashMap<>();
        components.put("region", region);
        components.put("distanceKm", distanceKm);
        components.put("entity", entity);
        components.put("topic", topic);
        components.put("textSimilarity", text);
        components.put("theme", theme);
        components.put("recencyHours", round(hours));
        return new Score(score, components);
    }

    /** The region term: shared admin1 0.30, else shared country 0.20, else 0. */
    static double region(Set<String> input, Set<String> situation, LinkerWeights weights) {
        Set<String> shared = intersection(input, situation);
        if (shared.stream().anyMatch(code -> code.indexOf('-') > 0)) {
            return weights.sharedAdmin1();
        }
        if (!intersection(countries(input), countries(situation)).isEmpty()) {
            return weights.sharedCountry();
        }
        return 0;
    }

    /** The text term {@code w × max(0, (cos − floor) / (1 − floor))}. */
    static double textTerm(double cosine, LinkerWeights weights) {
        return weights.textSimilarity() * Math.max(0, (cosine - weights.cosFloor()) / (1 - weights.cosFloor()));
    }

    /** The recency multiplier {@code 0.5 + 0.5·exp(−h / 72)}. */
    static double recency(double hours, LinkerWeights weights) {
        return 0.5 + 0.5 * Math.exp(-Math.max(0, hours) / weights.recencyHalfLifeHours());
    }

    /** Hours from {@code lastActivity} to {@code at}, never negative. */
    static double recencyHours(Instant at, Instant lastActivity) {
        if (at == null || lastActivity == null) {
            return 0;
        }
        return Math.max(0, Duration.between(lastActivity, at).toMillis() / 3_600_000.0);
    }

    /** The distance at which the distance term reaches zero: 500 km when either side is maritime, else 300. */
    static double maxDistanceKm(LinkerInput input, Candidate candidate, LinkerWeights weights) {
        boolean maritime = SituationThemes.MARITIME.equals(input.theme())
                || SituationThemes.MARITIME.equals(candidate.theme());
        return maritime ? weights.maritimeMaxDistanceKm() : weights.maxDistanceKm();
    }

    /** True when {@code text} contains any keyword as a whole word or phrase, ignoring case. */
    public static boolean topicMatch(String text, Collection<String> keywords) {
        if (text == null || text.isBlank() || keywords == null || keywords.isEmpty()) {
            return false;
        }
        for (String keyword : keywords) {
            if (keyword == null || keyword.isBlank()) {
                continue;
            }
            Pattern pattern = Pattern.compile("(?<![\\p{L}\\p{N}])" + Pattern.quote(keyword.strip())
                    + "(?![\\p{L}\\p{N}])", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            if (pattern.matcher(text).find()) {
                return true;
            }
        }
        return false;
    }

    /** The countries of region codes: two-letter codes and the prefix of {@code CC-xxx} codes. */
    static Set<String> countries(Set<String> codes) {
        Set<String> out = new TreeSet<>();
        if (codes == null) {
            return out;
        }
        for (String code : codes) {
            if (code == null || code.length() < 2 || "ZZ".equals(code)) {
                continue;
            }
            out.add(code.substring(0, 2));
        }
        return out;
    }

    private static Set<String> intersection(Set<String> a, Set<String> b) {
        Set<String> out = new TreeSet<>();
        if (a == null || b == null) {
            return out;
        }
        for (String value : a) {
            if (value != null && b.contains(value) && !"ZZ".equals(value)) {
                out.add(value);
            }
        }
        return out;
    }

    /** Rounds to nine decimals. */
    static double round(double value) {
        return Math.round(value * 1e9) / 1e9;
    }
}
