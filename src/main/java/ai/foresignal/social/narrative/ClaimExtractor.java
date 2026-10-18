package ai.foresignal.social.narrative;

import ai.foresignal.social.TextFolding;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The deterministic claim extractor (prompt 136, {@code ClaimExtractor} 1.0.0): what, where, when and who of one post,
 * never whether it is true.
 *
 * <ol>
 *   <li><strong>What:</strong> each event term of the {@link ClaimLexicon} is a claim of its type (one per type per
 *       post, at the first occurrence) with the type's 119 signal family.
 *   <li><strong>Polarity:</strong> {@code denied} when a denial marker occurs anywhere or a negation within three words
 *       before the term; else {@code questioned} with a question marker or a question mark in the sentence; else
 *       {@code asserted}. {@code hedged} with a hedge marker.
 *   <li><strong>Where:</strong> the place names of the {@link PlaceIndex}, longest match first (a name inside a longer
 *       one is dropped); the claim takes the name nearest its term. Among a name's candidates an operator place (036)
 *       wins, then the most populous; confidence falls with ambiguity: {@code 0.9 × share of the population} for
 *       several candidates (never under 0.2), 0.9 for a single candidate, 0.95 for an operator place.
 *   <li><strong>Who:</strong> entity names and identifiers (IMO and MMSI numbers) of the {@link EntityIndex}, and the
 *       nearest actor name of the lexicon.
 *   <li><strong>When:</strong> {@link TimeExpressions} relative to the post's creation time.
 * </ol>
 * Confidence describes the extraction only: 0.5, plus 0.2 with a resolved place, plus 0.1 with a time, minus 0.1 when
 * questioned, clamped to [0.1, 0.95]. Pure.
 */
public final class ClaimExtractor {

    /** The base extraction confidence. */
    static final double BASE_CONFIDENCE = 0.5;

    /** Words before an event term a negation may stand in. */
    static final int NEGATION_WINDOW = 3;

    private static final Pattern IMO = Pattern.compile("(?:^| )imo (\\d{7})(?: |$)");
    private static final Pattern MMSI = Pattern.compile("(?:^| )mmsi (\\d{9})(?: |$)");

    private ClaimExtractor() {}

    /**
     * A resolved place.
     *
     * @param ref the 036 place ref, or null
     * @param name the name
     * @param lon the longitude, or null
     * @param lat the latitude, or null
     * @param precision {@code point}, {@code locality}, {@code admin1} or {@code country}
     * @param confidence the resolution confidence
     * @param regionCode the 040 region code, or null
     */
    public record Place(String ref, String name, Double lon, Double lat, String precision, double confidence,
            String regionCode) {

        /** True when the place has a point. */
        public boolean located() {
            return lon != null && lat != null;
        }

        /** The {@code ClaimPlace} view. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("ref", ref);
            out.put("name", name);
            if (located()) {
                Map<String, Object> geom = new LinkedHashMap<>();
                geom.put("type", "Point");
                geom.put("coordinates", List.of(lon, lat));
                out.put("geom", geom);
            } else {
                out.put("geom", null);
            }
            out.put("precision", precision);
            out.put("confidence", confidence);
            out.put("regionCode", regionCode);
            return out;
        }

        /** The key that groups claims about one place. */
        public String key() {
            return ref != null ? ref : name.toLowerCase(Locale.ROOT);
        }
    }

    /**
     * A claim of one post.
     *
     * @param type the lexicon type
     * @param signalFamily its signal family
     * @param polarity {@code asserted}, {@code denied} or {@code questioned}
     * @param hedged true with a hedge marker
     * @param place the place, or null
     * @param entityRefs the entities mentioned
     * @param eventTime the claimed time, or null
     * @param actor the stated actor, or null
     * @param confidence the extraction confidence
     */
    public record PostClaim(String type, String signalFamily, String polarity, boolean hedged, Place place,
            List<String> entityRefs, TimeExpressions.EventTime eventTime, String actor, double confidence) {

        /** Copies the refs. */
        public PostClaim {
            entityRefs = entityRefs == null ? List.of() : List.copyOf(entityRefs);
        }

        /** The key that groups equal claims across posts: type, polarity and place. */
        public String key() {
            return type + "|" + polarity + "|" + (place == null ? "" : place.key());
        }
    }

    /**
     * What one post gave.
     *
     * @param claims the claims
     * @param places every resolved place
     * @param entityRefs every entity mentioned
     */
    public record Extraction(List<PostClaim> claims, List<Place> places, List<String> entityRefs) {

        /** An empty extraction. */
        public static final Extraction EMPTY = new Extraction(List.of(), List.of(), List.of());

        /** Copies the lists. */
        public Extraction {
            claims = List.copyOf(claims);
            places = List.copyOf(places);
            entityRefs = List.copyOf(entityRefs);
        }

        /** The 040 region codes of the places. */
        public List<String> regionCodes() {
            Set<String> out = new LinkedHashSet<>();
            for (Place p : places) {
                if (p.regionCode() != null) {
                    out.add(p.regionCode());
                }
            }
            return List.copyOf(out);
        }

        /** The 036 place refs. */
        public List<String> placeRefs() {
            Set<String> out = new LinkedHashSet<>();
            for (Place p : places) {
                if (p.ref() != null) {
                    out.add(p.ref());
                }
            }
            return List.copyOf(out);
        }
    }

    /**
     * Extracts the claims of one post.
     *
     * @param text the post text
     * @param createdAt the post's creation time
     * @param lexicon the lexicon
     * @param places the place index
     * @param entities the entity index
     */
    public static Extraction extract(String text, Instant createdAt, ClaimLexicon lexicon, PlaceIndex places,
            EntityIndex entities) {
        if (text == null || text.isBlank()) {
            return Extraction.EMPTY;
        }
        String folded = TextFolding.fold(text);
        List<ClaimLexicon.Hit> hits = lexicon.scan(folded);
        List<Located> located = places(folded, places == null ? PlaceIndex.EMPTY : places);
        List<String> entityRefs = entities(folded, entities == null ? EntityIndex.EMPTY : entities);
        boolean denial = false;
        boolean hedged = false;
        boolean question = false;
        for (ClaimLexicon.Hit h : hits) {
            denial |= h.kind() == ClaimLexicon.Kind.DENIAL;
            hedged |= h.kind() == ClaimLexicon.Kind.HEDGE;
            question |= h.kind() == ClaimLexicon.Kind.QUESTION;
        }
        TimeExpressions.EventTime time = TimeExpressions.resolve(text, createdAt);
        Set<String> seenTypes = new LinkedHashSet<>();
        List<PostClaim> claims = new ArrayList<>();
        for (ClaimLexicon.Hit h : hits) {
            if (h.kind() != ClaimLexicon.Kind.TYPE || !seenTypes.add(h.id())) {
                continue;
            }
            ClaimLexicon.Type type = lexicon.type(h.id());
            String polarity;
            if (denial || negated(folded, hits, h)) {
                polarity = "denied";
            } else if (question || questionMark(text, h, folded)) {
                polarity = "questioned";
            } else {
                polarity = "asserted";
            }
            Place place = nearestPlace(located, h);
            String actor = nearestActor(hits, h);
            double confidence = BASE_CONFIDENCE + (place != null ? 0.2 : 0) + (time != null ? 0.1 : 0)
                    - ("questioned".equals(polarity) ? 0.1 : 0);
            confidence = Math.round(Math.max(0.1, Math.min(0.95, confidence)) * 100) / 100.0;
            claims.add(new PostClaim(h.id(), type == null ? null : type.signalFamily(), polarity, hedged, place,
                    entityRefs, time, actor, confidence));
        }
        List<Place> all = new ArrayList<>();
        for (Located l : located) {
            all.add(l.place());
        }
        return new Extraction(claims, all, entityRefs);
    }

    /** A place with where its name stood. */
    record Located(int start, int end, Place place) {}

    /** The places of a text: longest matches, best candidate each. */
    static List<Located> places(String folded, PlaceIndex index) {
        List<PlaceIndex.NameHit> hits = new ArrayList<>(index.find(folded));
        hits.sort(Comparator.comparingInt((PlaceIndex.NameHit h) -> h.end() - h.start()).reversed()
                .thenComparingInt(PlaceIndex.NameHit::start));
        List<Located> out = new ArrayList<>();
        for (PlaceIndex.NameHit h : hits) {
            boolean inside = false;
            for (Located kept : out) {
                if (h.start() < kept.end() && h.end() > kept.start()) {
                    inside = true;
                    break;
                }
            }
            if (!inside && !h.candidates().isEmpty()) {
                out.add(new Located(h.start(), h.end(), choose(h.candidates())));
            }
        }
        out.sort(Comparator.comparingInt(Located::start));
        return out;
    }

    /** The best candidate of a name with its confidence. */
    static Place choose(List<PlaceIndex.Candidate> candidates) {
        PlaceIndex.Candidate best = null;
        long total = 0;
        for (PlaceIndex.Candidate c : candidates) {
            total += Math.max(0, c.population());
            if (best == null || better(c, best)) {
                best = c;
            }
        }
        double confidence;
        if (best.ref() != null) {
            confidence = 0.95;
        } else if (candidates.size() == 1) {
            confidence = 0.9;
        } else {
            double share = total <= 0 ? 1.0 / candidates.size() : (double) Math.max(0, best.population()) / total;
            confidence = Math.max(0.2, 0.9 * share);
        }
        return new Place(best.ref(), best.name(), best.lon(), best.lat(), best.precision(),
                Math.round(confidence * 100) / 100.0, best.regionCode());
    }

    private static boolean better(PlaceIndex.Candidate a, PlaceIndex.Candidate b) {
        if ((a.ref() != null) != (b.ref() != null)) {
            return a.ref() != null;
        }
        int rank = Integer.compare(precisionRank(a.precision()), precisionRank(b.precision()));
        if (a.population() != b.population()) {
            return a.population() > b.population();
        }
        return rank < 0;
    }

    private static int precisionRank(String precision) {
        return switch (precision == null ? "" : precision) {
            case "point" -> 0;
            case "locality" -> 1;
            case "admin1" -> 2;
            default -> 3;
        };
    }

    private static List<String> entities(String folded, EntityIndex index) {
        Set<String> out = new LinkedHashSet<>();
        for (EntityIndex.Hit h : index.find(folded)) {
            out.addAll(h.refs());
        }
        Matcher imo = IMO.matcher(folded);
        while (imo.find()) {
            out.addAll(index.byKey("imo", imo.group(1)));
        }
        Matcher mmsi = MMSI.matcher(folded);
        while (mmsi.find()) {
            out.addAll(index.byKey("mmsi", mmsi.group(1)));
        }
        return List.copyOf(out);
    }

    private static boolean negated(String folded, List<ClaimLexicon.Hit> hits, ClaimLexicon.Hit term) {
        for (ClaimLexicon.Hit h : hits) {
            if (h.kind() != ClaimLexicon.Kind.NEGATION || h.end() > term.start()) {
                continue;
            }
            String between = folded.substring(h.end(), term.start()).strip();
            int words = between.isEmpty() ? 0 : between.split(" ").length;
            if (words < NEGATION_WINDOW) {
                return true;
            }
        }
        return false;
    }

    private static boolean questionMark(String text, ClaimLexicon.Hit term, String folded) {
        String raw = text.strip();
        return raw.endsWith("?") || raw.endsWith("؟");
    }

    private static Place nearestPlace(List<Located> located, ClaimLexicon.Hit term) {
        Located best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (Located l : located) {
            int d = l.start() >= term.end() ? l.start() - term.end() : Math.max(0, term.start() - l.end());
            if (d < bestDistance) {
                bestDistance = d;
                best = l;
            }
        }
        return best == null ? null : best.place();
    }

    private static String nearestActor(List<ClaimLexicon.Hit> hits, ClaimLexicon.Hit term) {
        String best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (ClaimLexicon.Hit h : hits) {
            if (h.kind() != ClaimLexicon.Kind.ACTOR) {
                continue;
            }
            int d = h.start() >= term.end() ? h.start() - term.end() : Math.max(0, term.start() - h.end());
            if (d < bestDistance) {
                bestDistance = d;
                best = h.id();
            }
        }
        return best;
    }
}
