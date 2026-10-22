package ai.foresignal.social.narrative;

import ai.foresignal.social.TextFolding;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The neutral, de-hyped who/what/where/when restatement of a claim (prompt 153): "tell me the checkable core, not the
 * selfie". The LLM extractor may propose a restatement, but it is never the arbiter — the proposal is re-validated
 * against the lexicon, gazetteer and entity index exactly like the extracted tuples, and anything that fails falls back
 * to a deterministic restatement synthesized from the validated who/what/where/when. The stored restatement is
 * therefore always checkable prose, never hype and never an unvalidated specific.
 */
public final class ClaimRestatement {

    /** The longest restatement kept. */
    public static final int MAX_LENGTH = 240;

    private static final Set<String> STOPWORDS = Set.of("A", "An", "The", "In", "On", "At", "Of", "And", "Or", "To",
            "By", "For", "From", "With", "As", "Is", "Are", "Was", "Were", "Reported", "Reports", "Claimed", "Claims");

    private ClaimRestatement() {}

    /**
     * The validated restatement: the LLM proposal when it survives re-validation, else a synthesized one.
     *
     * @param proposal the LLM's restatement, or null
     * @param type the claim type
     * @param polarity asserted / denied / questioned
     * @param actor the validated actor, or null
     * @param placeName the validated place name, or null
     * @param when a short time phrase, or null
     * @param places the gazetteer
     * @param entities the entity index
     */
    public static String restatement(String proposal, String type, String polarity, String actor, String placeName,
            String when, PlaceIndex places, EntityIndex entities) {
        String validated = validate(proposal, actor, places, entities);
        return validated != null ? validated : synthesize(type, polarity, actor, placeName, when);
    }

    /** The LLM proposal when it is de-hyped and introduces no unvalidated place or entity, else null. */
    static String validate(String proposal, String actor, PlaceIndex places, EntityIndex entities) {
        if (proposal == null) {
            return null;
        }
        String text = proposal.strip().replaceAll("^[\"'\\s]+|[\"'\\s]+$", "");
        if (text.isEmpty() || text.length() > MAX_LENGTH) {
            return null;
        }
        String[] tokens = text.split("\\s+");
        for (String raw : tokens) {
            String word = raw.replaceAll("[^A-Za-z]", "").toLowerCase(Locale.ROOT);
            if (FramingScore.INFLAMMATORY_WORDS.contains(word) || FramingScore.PARTISAN_WORDS.contains(word)) {
                // Hype: a de-hyped restatement must not carry outrage or partisan framing.
                return null;
            }
        }
        for (String phrase : capitalizedPhrases(tokens)) {
            if (!resolvable(phrase, actor, places, entities)) {
                // A proper noun the gazetteer and entity index do not know, and not the validated actor: an
                // unchecked specific the LLM may have invented. Reject and synthesize instead.
                return null;
            }
        }
        return text;
    }

    /** A deterministic neutral restatement from the validated who/what/where/when. */
    static String synthesize(String type, String polarity, String actor, String placeName, String when) {
        String what = type == null ? "event" : type.replace('-', ' ').replace('_', ' ');
        StringBuilder sb = new StringBuilder();
        if (actor != null && !actor.isBlank()) {
            sb.append(actor.strip()).append(": ");
        }
        sb.append(what);
        if (placeName != null && !placeName.isBlank()) {
            sb.append(" in ").append(placeName.strip());
        }
        if (when != null && !when.isBlank()) {
            sb.append(" (").append(when.strip()).append(")");
        }
        if ("denied".equals(polarity)) {
            sb.append(" — denied");
        } else if ("questioned".equals(polarity)) {
            sb.append(" — questioned");
        }
        String out = sb.toString();
        return out.length() > MAX_LENGTH ? out.substring(0, MAX_LENGTH) : out;
    }

    /** The maximal runs of capitalized, non-stopword tokens (candidate proper nouns). */
    static List<String> capitalizedPhrases(String[] tokens) {
        List<String> out = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].replaceAll("[^A-Za-z0-9'-]", "");
            boolean capitalized = !token.isEmpty() && Character.isUpperCase(token.charAt(0))
                    && !(i == 0) && !STOPWORDS.contains(token);
            if (capitalized) {
                if (current.length() > 0) {
                    current.append(' ');
                }
                current.append(token);
            } else if (current.length() > 0) {
                out.add(current.toString());
                current.setLength(0);
            }
        }
        if (current.length() > 0) {
            out.add(current.toString());
        }
        return out;
    }

    /** Whether a proper-noun phrase resolves in the gazetteer or entity index, or is the validated actor. */
    static boolean resolvable(String phrase, String actor, PlaceIndex places, EntityIndex entities) {
        if (actor != null && actor.toLowerCase(Locale.ROOT).contains(phrase.toLowerCase(Locale.ROOT))) {
            return true;
        }
        String folded = TextFolding.fold(phrase);
        if (places != null && !places.find(folded).isEmpty()) {
            return true;
        }
        return entities != null && !entities.find(folded).isEmpty();
    }
}
