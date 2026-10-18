package ai.foresignal.media.outlet;

import ai.foresignal.source.formats.news.ArticleUrls;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import tools.jackson.databind.JsonNode;

/**
 * The Wikidata Query Service side of the {@code wikidata} provider (prompt 133, CC0): the SPARQL query that finds the
 * items whose official website (P856) is an outlet's domain, and the mapping of the answer to registry facts and
 * ratings. Verified against the live service on 2026-10-17 (property and item ids checked with
 * {@code wbgetentities}). Pure.
 *
 * <ul>
 *   <li><strong>Lookup:</strong> P856 values are written inconsistently ({@code https://www.reuters.com/},
 *       {@code https://foxnews.com}), so each domain is asked as eight IRIs: http/https, with and without
 *       {@code www.}, with and without a trailing slash.
 *   <li><strong>Choice:</strong> when several items share a website, the one that is an instance of a news class
 *       ({@link #NEWS_CLASSES}) wins, then the one with the most facts, then the lowest QID.
 *   <li><strong>Facts</strong> (registry, not ratings): the QID, the country (P17 → P297), owned by (P127) and parent
 *       organization (P749) names, and the type: news agency → {@code wire}, public broadcaster →
 *       {@code public-broadcaster}, state media → {@code state-affiliated}.
 *   <li><strong>Ratings:</strong> {@code lean} from political alignment (P1387) through {@link #ALIGNMENTS} (far-left
 *       and left-wing −2, center-left −1, centrism and radical centrism 0, center-right +1, right-wing, far-right and
 *       right-wing extremism +2; an alignment outside the table is kept as a label with no value; several alignments
 *       give their median); {@code state-affiliation} 1 for an instance of state media; {@code ownership-type}
 *       {@code public-broadcaster}, {@code state-media} or {@code news-agency} as a label.
 * </ul>
 */
public final class WikidataParser {

    /** The Wikidata entity IRI prefix. */
    public static final String ENTITY = "http://www.wikidata.org/entity/";

    /** News agency. */
    public static final String NEWS_AGENCY = "Q192283";

    /** Public broadcaster. */
    public static final String PUBLIC_BROADCASTER = "Q1126006";

    /** State media. */
    public static final String STATE_MEDIA = "Q2324556";

    /** Classes that make an item a news outlet when choosing between items sharing a website. */
    public static final Set<String> NEWS_CLASSES = Set.of(NEWS_AGENCY, "Q11032", "Q1110794", "Q1153191", "Q17232649",
            PUBLIC_BROADCASTER, STATE_MEDIA, "Q1002697");

    /** Political alignment items (P1387 values) on the −2…+2 lean scale. */
    public static final Map<String, Double> ALIGNMENTS = Map.of(
            "Q1129409", -2.0, // far-left politics
            "Q164597", -2.0, // left-wing
            "Q737014", -1.0, // center-left
            "Q6587194", 0.0, // centrism
            "Q3448005", 0.0, // radical centrism
            "Q844072", 1.0, // center-right
            "Q76074", 2.0, // right-wing
            "Q127869500", 2.0, // far-right
            "Q204481", 2.0); // right-wing extremism

    private WikidataParser() {}

    /**
     * What Wikidata says about one domain.
     *
     * @param domain the outlet domain
     * @param qid the chosen item
     * @param country the ISO alpha-2 country, or null
     * @param ownerOrg the owning organization's name, or null
     * @param parentOrg the parent organization's name, or null
     * @param type the outlet type the item's classes imply, or null
     * @param ratings the ratings
     */
    public record DomainFacts(String domain, String qid, String country, String ownerOrg, String parentOrg,
            String type, List<RatingRow> ratings) {}

    /** The candidate P856 IRIs of a domain. */
    public static List<String> iris(String domain) {
        List<String> out = new ArrayList<>();
        for (String scheme : List.of("https", "http")) {
            for (String www : List.of("", "www.")) {
                for (String slash : List.of("", "/")) {
                    out.add(scheme + "://" + www + domain + slash);
                }
            }
        }
        return out;
    }

    /** The SPARQL query for {@code domains}. */
    public static String query(Collection<String> domains) {
        StringBuilder values = new StringBuilder();
        for (String d : domains) {
            for (String iri : iris(d)) {
                values.append(" <").append(iri).append('>');
            }
        }
        return "SELECT ?item ?site ?align ?alignLabel ?inst ?ownerLabel ?parentLabel ?iso WHERE { VALUES ?site {"
                + values + " } ?item wdt:P856 ?site . OPTIONAL { ?item wdt:P1387 ?align . }"
                + " OPTIONAL { ?item wdt:P31 ?inst . } OPTIONAL { ?item wdt:P127 ?owner . }"
                + " OPTIONAL { ?item wdt:P749 ?parent . } OPTIONAL { ?item wdt:P17 ?c . ?c wdt:P297 ?iso . }"
                + " SERVICE wikibase:label { bd:serviceParam wikibase:language \"en\". } } LIMIT 5000";
    }

    /** Collected bindings of one item. */
    private static final class Item {
        final String qid;
        final Set<String> domains = new TreeSet<>();
        final Map<String, String> aligns = new TreeMap<>();
        final Set<String> classes = new TreeSet<>();
        final Set<String> owners = new TreeSet<>();
        final Set<String> parents = new TreeSet<>();
        final Set<String> countries = new TreeSet<>();

        Item(String qid) {
            this.qid = qid;
        }

        int facts() {
            return aligns.size() + classes.size() + owners.size() + parents.size() + countries.size();
        }

        boolean news() {
            for (String c : classes) {
                if (NEWS_CLASSES.contains(c)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Maps a SPARQL JSON answer ({@code application/sparql-results+json}) to facts per asked domain.
     *
     * @param results the answer
     * @param domains the domains asked
     * @param link the base of rating URLs ({@code https://www.wikidata.org/wiki/})
     */
    public static Map<String, DomainFacts> parse(JsonNode results, Collection<String> domains, String link) {
        Map<String, String> domainOfIri = new LinkedHashMap<>();
        for (String d : domains) {
            for (String iri : iris(d)) {
                domainOfIri.put(iri, d);
            }
        }
        Map<String, Item> items = new LinkedHashMap<>();
        for (JsonNode b : results.path("results").path("bindings")) {
            String qid = qid(value(b, "item"));
            String site = value(b, "site");
            if (qid == null || site == null) {
                continue;
            }
            String domain = domainOfIri.get(site);
            if (domain == null) {
                domain = ArticleUrls.domain(site);
            }
            if (domain == null || !domains.contains(domain)) {
                continue;
            }
            Item item = items.computeIfAbsent(qid, Item::new);
            item.domains.add(domain);
            String align = qid(value(b, "align"));
            if (align != null) {
                item.aligns.put(align, value(b, "alignLabel"));
            }
            String inst = qid(value(b, "inst"));
            if (inst != null) {
                item.classes.add(inst);
            }
            addLabel(item.owners, value(b, "ownerLabel"));
            addLabel(item.parents, value(b, "parentLabel"));
            String iso = value(b, "iso");
            if (iso != null && iso.length() == 2) {
                item.countries.add(iso.toUpperCase(java.util.Locale.ROOT));
            }
        }
        Map<String, DomainFacts> out = new LinkedHashMap<>();
        for (String domain : domains) {
            Item best = null;
            for (Item item : items.values()) {
                if (!item.domains.contains(domain)) {
                    continue;
                }
                if (best == null || better(item, best)) {
                    best = item;
                }
            }
            if (best != null) {
                out.put(domain, facts(domain, best, link));
            }
        }
        return out;
    }

    private static boolean better(Item a, Item b) {
        if (a.news() != b.news()) {
            return a.news();
        }
        if (a.facts() != b.facts()) {
            return a.facts() > b.facts();
        }
        return number(a.qid) < number(b.qid);
    }

    private static DomainFacts facts(String domain, Item item, String link) {
        String url = link == null ? null : link + item.qid;
        List<RatingRow> ratings = new ArrayList<>();
        if (!item.aligns.isEmpty()) {
            List<Double> values = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            for (Map.Entry<String, String> e : item.aligns.entrySet()) {
                Double v = ALIGNMENTS.get(e.getKey());
                if (v != null) {
                    values.add(v);
                }
                labels.add(e.getValue() == null || e.getValue().isBlank() ? e.getKey() : e.getValue());
            }
            Double value = values.isEmpty() ? null : Consensus.median(values);
            ratings.add(new RatingRow(domain, MediaVocabulary.LEAN, value, "P1387=" + String.join(",",
                    item.aligns.keySet()), String.join(", ", labels), null, null, null, url, null));
        }
        String type = null;
        if (item.classes.contains(STATE_MEDIA)) {
            ratings.add(new RatingRow(domain, MediaVocabulary.STATE_AFFILIATION, 1.0, "P31=" + STATE_MEDIA,
                    "state media", null, null, null, url, null));
            ratings.add(new RatingRow(domain, MediaVocabulary.OWNERSHIP_TYPE, null, "P31=" + STATE_MEDIA,
                    "state-media", null, null, null, url, null));
            type = MediaVocabulary.STATE_AFFILIATED;
        } else if (item.classes.contains(PUBLIC_BROADCASTER)) {
            ratings.add(new RatingRow(domain, MediaVocabulary.OWNERSHIP_TYPE, null, "P31=" + PUBLIC_BROADCASTER,
                    "public-broadcaster", null, null, null, url, null));
            type = "public-broadcaster";
        } else if (item.classes.contains(NEWS_AGENCY)) {
            ratings.add(new RatingRow(domain, MediaVocabulary.OWNERSHIP_TYPE, null, "P31=" + NEWS_AGENCY,
                    "news-agency", null, null, null, url, null));
            type = MediaVocabulary.WIRE;
        }
        return new DomainFacts(domain, item.qid, item.countries.isEmpty() ? null : item.countries.iterator().next(),
                item.owners.isEmpty() ? null : item.owners.iterator().next(),
                item.parents.isEmpty() ? null : item.parents.iterator().next(), type, ratings);
    }

    private static void addLabel(Set<String> into, String label) {
        // An unlabeled item comes back as its QID; it is not a name.
        if (label != null && !label.isBlank() && !label.matches("^Q[0-9]+$")) {
            into.add(label.strip());
        }
    }

    private static String value(JsonNode binding, String name) {
        JsonNode v = binding.path(name).path("value");
        if (v.isMissingNode() || v.isNull()) {
            return null;
        }
        String s = v.asString();
        return s == null || s.isBlank() ? null : s;
    }

    private static String qid(String iri) {
        if (iri == null || !iri.startsWith(ENTITY)) {
            return null;
        }
        String id = iri.substring(ENTITY.length());
        return id.matches("^Q[0-9]+$") ? id : null;
    }

    private static long number(String qid) {
        try {
            return Long.parseLong(qid.substring(1));
        } catch (RuntimeException e) {
            return Long.MAX_VALUE;
        }
    }
}
