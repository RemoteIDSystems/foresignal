package ai.foresignal.social.narrative;

import java.util.List;

/**
 * Place names a claim can be resolved to (prompt 136): 036 places with their aliases, 040 countries and admin-1
 * regions, and populated places of the 040 gazetteer, with the lexicon's aliases in other scripts. Implementations
 * find every name in a folded text; {@link ClaimExtractor} keeps the longest matches and ranks the candidates.
 */
public interface PlaceIndex {

    /** An index that knows no place. */
    PlaceIndex EMPTY = folded -> List.of();

    /**
     * One place a name can stand for.
     *
     * @param ref the 036 place ref ({@code place:{id}}), or null for a gazetteer or region entry
     * @param name the canonical name
     * @param lon the longitude of its point, or null
     * @param lat the latitude of its point, or null
     * @param precision {@code point}, {@code locality}, {@code admin1} or {@code country}
     * @param population its population, or 0 when unknown
     * @param regionCode its 040 region code (country or admin-1), or null
     */
    record Candidate(String ref, String name, Double lon, Double lat, String precision, long population,
            String regionCode) {}

    /**
     * One name found in a text.
     *
     * @param start the start index in the folded text
     * @param end the end index (exclusive)
     * @param candidates the places it can stand for (at least one)
     */
    record NameHit(int start, int end, List<Candidate> candidates) {

        /** Copies the candidates. */
        public NameHit {
            candidates = List.copyOf(candidates);
        }
    }

    /**
     * Every place name in a folded text, on word boundaries (overlaps allowed).
     *
     * @param folded the folded text
     */
    List<NameHit> find(String folded);
}
