package ai.foresignal.social.narrative;

import ai.foresignal.entity.EntityKeysChangedEvent;
import ai.foresignal.evidence.RecordRef;
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

/**
 * The entity index of claim extraction (prompt 136), kept in memory: active 098 entities by display name and current
 * alias, and named 104 facilities ({@code infra_node}) by name. Only names of at least {@value #MIN_LENGTH} characters
 * that contain a space or a digit are indexed, because single common words ("Hope", "Star") name many vessels and
 * would match ordinary text; vessels are found reliably by their IMO and MMSI numbers ({@link #byKey}). The index is
 * rebuilt after an entity key change and at most every six hours otherwise.
 */
@Component
public class EntityNameIndex implements EntityIndex {

    private static final Logger log = LoggerFactory.getLogger(EntityNameIndex.class);

    /** The shortest indexed name. */
    static final int MIN_LENGTH = 5;

    /** The most names read per table. */
    static final int MAX_NAMES = 300_000;

    /** How long a built index is used. */
    static final Duration MAX_AGE = Duration.ofHours(6);

    private final JdbcTemplate jdbc;
    private final Clock clock;
    private volatile AhoCorasick<List<String>> automaton;
    private volatile Instant builtAt;

    /** Creates the index. */
    public EntityNameIndex(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /** Drops the index after entity keys changed. */
    @EventListener
    public void onKeysChanged(EntityKeysChangedEvent event) {
        automaton = null;
    }

    @Override
    public List<Hit> find(String folded) {
        List<Hit> out = new ArrayList<>();
        for (AhoCorasick.Hit<List<String>> h : current().search(folded)) {
            if (TextFolding.onBoundaries(folded, h.pattern(), h.start(), h.end())) {
                out.add(new Hit(h.start(), h.end(), h.value()));
            }
        }
        return out;
    }

    @Override
    public List<String> byKey(String keyType, String value) {
        try {
            return jdbc.queryForList("SELECT DISTINCT 'entity:' || entity_id FROM entity_key WHERE key_type = ? AND"
                    + " key_value = ? AND NOT retracted AND valid_to IS NULL LIMIT 10", String.class, keyType, value);
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    private AhoCorasick<List<String>> current() {
        AhoCorasick<List<String>> current = automaton;
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

    /** Reads every indexed name; folded name to refs. */
    Map<String, List<String>> build() {
        Map<String, List<String>> names = new LinkedHashMap<>();
        try {
            jdbc.query("SELECT id, display_name FROM entity WHERE status = 'ACTIVE' AND display_name IS NOT NULL"
                    + " LIMIT ?", (org.springframework.jdbc.core.RowCallbackHandler) rs -> add(names, rs.getString("display_name"), "entity:" + rs.getString("id")),
                    MAX_NAMES);
            jdbc.query("SELECT a.entity_id, a.alias FROM entity_alias a JOIN entity e ON e.id = a.entity_id WHERE"
                    + " a.valid_to IS NULL AND e.status = 'ACTIVE' LIMIT ?",
                    (org.springframework.jdbc.core.RowCallbackHandler) rs -> add(names, rs.getString("alias"), "entity:" + rs.getString("entity_id")), MAX_NAMES);
        } catch (RuntimeException e) {
            log.debug("No entity names for claim extraction: {}", e.toString());
        }
        try {
            jdbc.query("SELECT source_id, external_id, name FROM infra_node WHERE name IS NOT NULL LIMIT ?",
                    (org.springframework.jdbc.core.RowCallbackHandler) rs -> add(names, rs.getString("name"), RecordRef.format(rs.getString("source_id"),
                            rs.getString("external_id"))), MAX_NAMES);
        } catch (RuntimeException e) {
            log.debug("No facility names for claim extraction: {}", e.toString());
        }
        return names;
    }

    /** Adds a name when it is long enough and holds a space or a digit. */
    static void add(Map<String, List<String>> names, String name, String ref) {
        if (name == null) {
            return;
        }
        String folded = TextFolding.fold(name);
        if (folded.length() < MIN_LENGTH || !(folded.indexOf(' ') >= 0 || folded.chars().anyMatch(Character::isDigit))) {
            return;
        }
        List<String> refs = names.computeIfAbsent(folded, k -> new ArrayList<>());
        if (!refs.contains(ref) && refs.size() < 5) {
            refs.add(ref);
        }
    }
}
