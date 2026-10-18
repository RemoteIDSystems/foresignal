package ai.foresignal.social.narrative;

import ai.foresignal.place.PlaceChangedEvent;
import ai.foresignal.reference.ReferenceDataChangedEvent;
import ai.foresignal.social.AhoCorasick;
import ai.foresignal.social.TextFolding;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The place index of claim extraction (prompt 136), built from the database and kept in memory:
 *
 * <ul>
 *   <li>036 places: the name and every {@code attributes.aliases} string (precision {@code point}, ref
 *       {@code place:{id}});
 *   <li>040 countries ({@code ref_country}, {@code gazetteer_country}) and admin-1 regions ({@code ref_admin1},
 *       {@code gazetteer_admin1} through {@code gn_code}) at their point on surface;
 *   <li>populated places of {@code gazetteer_place} with at least {@value #MIN_POPULATION} inhabitants (name and ASCII
 *       name, precision {@code locality});
 *   <li>the lexicon's aliases in other scripts, which point at the candidates of the name they stand for.
 * </ul>
 * Names shorter than three characters are left out. The index is rebuilt lazily after a place or reference-data
 * change, and at most every six hours otherwise; tables that are not loaded give no names.
 */
@Component
public class GazetteerPlaceIndex implements PlaceIndex {

    private static final Logger log = LoggerFactory.getLogger(GazetteerPlaceIndex.class);

    /** The smallest population of a gazetteer place in the index. */
    static final long MIN_POPULATION = 15_000;

    /** The most gazetteer places read. */
    static final int MAX_GAZETTEER = 200_000;

    /** How long a built index is used before it is rebuilt. */
    static final Duration MAX_AGE = Duration.ofHours(6);

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;
    private volatile AhoCorasick<List<Candidate>> automaton;
    private volatile Instant builtAt;

    /** Creates the index. */
    public GazetteerPlaceIndex(JdbcTemplate jdbc, JsonMapper json, Clock clock) {
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
    }

    /** Drops the index after a 036 place changed. */
    @EventListener
    public void onPlaceChanged(PlaceChangedEvent event) {
        automaton = null;
    }

    /** Drops the index after reference data changed. */
    @EventListener
    public void onReferenceChanged(ReferenceDataChangedEvent event) {
        automaton = null;
    }

    @Override
    public List<NameHit> find(String folded) {
        AhoCorasick<List<Candidate>> current = current();
        List<NameHit> out = new ArrayList<>();
        for (AhoCorasick.Hit<List<Candidate>> h : current.search(folded)) {
            if (TextFolding.onBoundaries(folded, h.pattern(), h.start(), h.end())) {
                out.add(new NameHit(h.start(), h.end(), h.value()));
            }
        }
        return out;
    }

    private AhoCorasick<List<Candidate>> current() {
        AhoCorasick<List<Candidate>> current = automaton;
        Instant now = clock.instant();
        if (current == null || builtAt == null || now.isAfter(builtAt.plus(MAX_AGE))) {
            synchronized (this) {
                current = automaton;
                if (current == null || builtAt == null || now.isAfter(builtAt.plus(MAX_AGE))) {
                    current = new AhoCorasick<>(build());
                    automaton = current;
                    builtAt = now;
                }
            }
        }
        return current;
    }

    /** Reads every name; folded name to candidates. */
    Map<String, List<Candidate>> build() {
        Map<String, List<Candidate>> names = new LinkedHashMap<>();
        try {
            jdbc.query("SELECT id, name, attributes::text AS attributes, ST_X(ST_PointOnSurface(geom)) AS lon,"
                    + " ST_Y(ST_PointOnSurface(geom)) AS lat FROM place", rs -> {
                        Candidate c = new Candidate("place:" + rs.getString("id"), rs.getString("name"),
                                (Double) rs.getObject("lon"), (Double) rs.getObject("lat"), "point", 0, null);
                        add(names, rs.getString("name"), c);
                        String attributes = rs.getString("attributes");
                        if (attributes != null && json.readValue(attributes, MAP).get("aliases") instanceof List<?> l) {
                            for (Object alias : l) {
                                add(names, alias == null ? null : alias.toString(), c);
                            }
                        }
                    });
        } catch (RuntimeException e) {
            log.debug("No 036 places for claim extraction: {}", e.toString());
        }
        try {
            jdbc.query("SELECT c.iso2, c.name, ST_X(ST_PointOnSurface(COALESCE(c.geom_simple, c.geom))) AS lon,"
                    + " ST_Y(ST_PointOnSurface(COALESCE(c.geom_simple, c.geom))) AS lat, g.name AS gn_name,"
                    + " g.population FROM ref_country c LEFT JOIN gazetteer_country g ON g.iso2 = c.iso2", rs -> {
                        long pop = rs.getLong("population");
                        Candidate c = new Candidate(null, rs.getString("name"), (Double) rs.getObject("lon"),
                                (Double) rs.getObject("lat"), "country", pop, rs.getString("iso2"));
                        add(names, rs.getString("name"), c);
                        add(names, rs.getString("gn_name"), c);
                    });
        } catch (RuntimeException e) {
            log.debug("No countries for claim extraction: {}", e.toString());
        }
        try {
            jdbc.query("SELECT a.code, a.name, ST_X(ST_PointOnSurface(COALESCE(a.geom_simple, a.geom))) AS lon,"
                    + " ST_Y(ST_PointOnSurface(COALESCE(a.geom_simple, a.geom))) AS lat, g.name AS gn_name,"
                    + " g.ascii_name FROM ref_admin1 a LEFT JOIN gazetteer_admin1 g ON g.code = a.gn_code", rs -> {
                        Candidate c = new Candidate(null, rs.getString("name"), (Double) rs.getObject("lon"),
                                (Double) rs.getObject("lat"), "admin1", 0, rs.getString("code"));
                        add(names, rs.getString("name"), c);
                        add(names, rs.getString("gn_name"), c);
                        add(names, rs.getString("ascii_name"), c);
                    });
        } catch (RuntimeException e) {
            log.debug("No admin-1 regions for claim extraction: {}", e.toString());
        }
        try {
            jdbc.query("SELECT name, ascii_name, iso2, admin1_code, population, ST_X(geog::geometry) AS lon,"
                    + " ST_Y(geog::geometry) AS lat FROM gazetteer_place WHERE population >= ? ORDER BY population DESC"
                    + " LIMIT ?", rs -> {
                        long pop = rs.getLong("population");
                        Candidate c = new Candidate(null, rs.getString("name"), (Double) rs.getObject("lon"),
                                (Double) rs.getObject("lat"), "locality", pop, rs.getString("iso2"));
                        add(names, rs.getString("name"), c);
                        add(names, rs.getString("ascii_name"), c);
                    }, MIN_POPULATION, MAX_GAZETTEER);
        } catch (RuntimeException e) {
            log.debug("No gazetteer places for claim extraction: {}", e.toString());
        }
        for (Map.Entry<String, String> alias : ClaimLexicon.bundled().placeAliases().entrySet()) {
            List<Candidate> target = names.get(TextFolding.fold(alias.getValue()));
            if (target != null) {
                for (Candidate c : List.copyOf(target)) {
                    add(names, alias.getKey(), c);
                }
            }
        }
        return names;
    }

    /** Adds a name of a candidate (folded; names under three characters are skipped). */
    static void add(Map<String, List<Candidate>> names, String name, Candidate candidate) {
        if (name == null) {
            return;
        }
        String folded = TextFolding.fold(name);
        if (folded.codePointCount(0, folded.length()) < 3) {
            return;
        }
        List<Candidate> list = names.computeIfAbsent(folded, k -> new ArrayList<>());
        if (!list.contains(candidate)) {
            list.add(candidate);
        }
    }
}
