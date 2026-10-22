package ai.foresignal.social.narrative;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The deterministic, LLM-free framing / partisan score of a post (prompt 153): how much emotional, inflammatory or
 * partisan-loaded hype surrounds the checkable core the extractor pulled out. It is the companion of the neutral
 * restatement — the restatement is the fact, this is the measure of the BS stripped off it. Pure: the same text always
 * scores the same.
 *
 * <p>The score in {@code [0, 1]} is the density of framing markers per word, lightly saturated, with a small boost for
 * shouting (ALL-CAPS runs) and exclamation. Markers are inflammatory / outrage words, partisan-loaded labels and
 * intensifiers. It measures framing only; it never judges whether the claim is true — that is the deterministic
 * comparison core's job.
 */
public final class FramingScore {

    /** Below this the post is plainly stated. */
    public static final double CHARGED = 0.12;

    /** At or above this the post is inflammatory. */
    public static final double INFLAMMATORY = 0.28;

    /** Outrage / emotional-intensity words. */
    static final Set<String> INFLAMMATORY_WORDS = Set.of("outrage", "outrageous", "shocking", "shocked", "slammed",
            "slams", "destroyed", "demolished", "obliterated", "chaos", "carnage", "bloodbath", "disaster",
            "catastrophe", "nightmare", "horrific", "horror", "terrifying", "insane", "insanity", "unbelievable",
            "disgusting", "disgrace", "disgraceful", "evil", "monster", "monstrous", "betrayal", "treason",
            "traitor", "corrupt", "scandal", "explosive", "bombshell", "panic", "furious", "fury", "rage",
            "meltdown", "annihilated", "savage", "brutal", "crisis", "apocalypse", "devastating");

    /** Partisan-loaded labels and framing. */
    static final Set<String> PARTISAN_WORDS = Set.of("radical", "woke", "libtard", "rino", "marxist", "communist",
            "socialist", "fascist", "nazi", "regime", "globalist", "elites", "elite", "deep", "state", "sheeple",
            "patriots", "patriot", "leftist", "rightwing", "right-wing", "left-wing", "liberal", "conservative",
            "maga", "antifa", "snowflake", "shill", "propaganda", "agenda", "establishment", "mainstream",
            "msm", "fake", "hoax", "witchhunt", "witch-hunt", "rigged", "stolen", "coverup", "cover-up",
            "tyranny", "tyrant", "dictator", "freedom", "patriotic", "unamerican", "un-american");

    /** Intensifiers that inflate without adding fact. */
    static final Set<String> INTENSIFIERS = Set.of("absolutely", "totally", "completely", "utterly", "literally",
            "massive", "massively", "huge", "hugely", "enormous", "incredible", "incredibly", "extremely",
            "wildly", "insanely", "ridiculously", "unprecedented", "epic", "ultimate", "biggest", "worst",
            "greatest", "ever", "never", "always", "everyone", "nobody", "everything", "nothing");

    private FramingScore() {}

    /**
     * The framing assessment of a post.
     *
     * @param score the framing intensity in {@code [0, 1]}
     * @param label neutral / charged / inflammatory
     * @param markers the marker counts contributing to the score
     */
    public record Framing(double score, String label, Map<String, Object> markers) {}

    /** Scores the framing of {@code text}; empty text is neutral. */
    public static Framing score(String text) {
        if (text == null || text.isBlank()) {
            return new Framing(0, "neutral", counts(0, 0, 0, 0, 0, 0));
        }
        String[] rawTokens = text.split("\\s+");
        int words = 0;
        int inflammatory = 0;
        int partisan = 0;
        int intensifiers = 0;
        int shouting = 0;
        for (String raw : rawTokens) {
            String letters = raw.replaceAll("[^A-Za-z]", "");
            if (letters.isEmpty()) {
                continue;
            }
            words++;
            // A run of at least three capital letters is shouting, not an acronym the gazetteer would carry.
            if (letters.length() >= 3 && letters.equals(letters.toUpperCase(Locale.ROOT))) {
                shouting++;
            }
            String word = letters.toLowerCase(Locale.ROOT);
            if (INFLAMMATORY_WORDS.contains(word)) {
                inflammatory++;
            }
            if (PARTISAN_WORDS.contains(word)) {
                partisan++;
            }
            if (INTENSIFIERS.contains(word)) {
                intensifiers++;
            }
        }
        int exclamations = count(text, '!');
        if (words == 0) {
            return new Framing(0, "neutral", counts(0, 0, 0, 0, 0, 0));
        }
        // Inflammatory and partisan markers weigh most; intensifiers and shouting less; two exclamations ~ one marker.
        double weighted = 1.0 * inflammatory + 1.0 * partisan + 0.5 * intensifiers + 0.5 * shouting
                + 0.5 * Math.min(exclamations, 4);
        double density = weighted / words;
        // Saturate: a quarter of the words being markers already reads as maximum hype.
        double score = Math.max(0, Math.min(1, density / 0.25));
        score = Math.round(score * 1000) / 1000.0;
        String label = score >= INFLAMMATORY ? "inflammatory" : score >= CHARGED ? "charged" : "neutral";
        return new Framing(score, label,
                counts(words, inflammatory, partisan, intensifiers, shouting, exclamations));
    }

    private static Map<String, Object> counts(int words, int inflammatory, int partisan, int intensifiers,
            int shouting, int exclamations) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("words", words);
        out.put("inflammatory", inflammatory);
        out.put("partisan", partisan);
        out.put("intensifiers", intensifiers);
        out.put("shouting", shouting);
        out.put("exclamations", exclamations);
        return out;
    }

    private static int count(String text, char c) {
        int n = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == c) {
                n++;
            }
        }
        return n;
    }

    /** The marker word sets, for documentation and tests. */
    public static List<Set<String>> lexicons() {
        return List.of(INFLAMMATORY_WORDS, PARTISAN_WORDS, INTENSIFIERS);
    }
}
