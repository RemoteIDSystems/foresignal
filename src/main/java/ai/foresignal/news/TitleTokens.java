package ai.foresignal.news;

import java.text.Normalizer;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Normalized title tokens and their Jaccard similarity (prompt 133): the story-clustering fallback without embeddings
 * (Jaccard ≥ 0.6) and the wire-reprint test (Jaccard ≥ 0.9). A title is lower-cased, stripped of accents and split on
 * anything that is not a letter or digit; tokens shorter than two characters and a small set of English function words
 * are dropped. Pure.
 */
public final class TitleTokens {

    /** Function words that carry no topic. */
    static final Set<String> STOPWORDS = Set.of("a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "has",
            "have", "in", "is", "it", "its", "of", "on", "or", "that", "the", "to", "was", "were", "will", "with",
            "after", "over", "into", "says", "say", "said");

    private TitleTokens() {}

    /** The tokens of a title, in first-seen order. */
    public static List<String> tokens(String title) {
        Set<String> out = new LinkedHashSet<>();
        if (title == null) {
            return List.of();
        }
        String folded = Normalizer.normalize(title, Normalizer.Form.NFKD).replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
        for (String t : folded.split("[^\\p{L}\\p{N}]+")) {
            if (t.length() >= 2 && !STOPWORDS.contains(t)) {
                out.add(t);
            }
        }
        return List.copyOf(out);
    }

    /** The Jaccard similarity of two token sets: |A ∩ B| ÷ |A ∪ B|; 0 when both are empty. */
    public static double jaccard(Collection<String> a, Collection<String> b) {
        Set<String> sa = new LinkedHashSet<>(a == null ? List.of() : a);
        Set<String> sb = new LinkedHashSet<>(b == null ? List.of() : b);
        if (sa.isEmpty() && sb.isEmpty()) {
            return 0;
        }
        int intersection = 0;
        for (String t : sa) {
            if (sb.contains(t)) {
                intersection++;
            }
        }
        int union = sa.size() + sb.size() - intersection;
        return union == 0 ? 0 : Math.round((double) intersection / union * 10_000) / 10_000.0;
    }
}
