package ai.foresignal.social;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The names a topic's places, regions and entities add as terms (prompt 135):
 *
 * <ul>
 *   <li>a 036 place: its name and the strings of {@code attributes.aliases};
 *   <li>a 040 country: the Natural Earth and GeoNames names; an admin-1 region: the Natural Earth name and the GeoNames
 *       name and ASCII name joined through {@code gn_code};
 *   <li>a 098 entity: its display name, its current aliases and its current, unretracted key values (call signs,
 *       registrations, IMO and MMSI numbers…).
 * </ul>
 *
 * <p>Reference tables that are not loaded give no names (the topic's own terms still match).
 */
@Component
public class TermExpansion {

    private static final Logger log = LoggerFactory.getLogger(TermExpansion.class);

    /** The shortest name added as a term (shorter names match too much). */
    static final int MIN_LENGTH = 3;

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    /** Creates the expansion. */
    public TermExpansion(JdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    /** The names and aliases of a place. */
    public List<String> place(String placeId) {
        Set<String> out = new LinkedHashSet<>();
        try {
            jdbc.query("SELECT name, attributes::text AS attributes FROM place WHERE id = ?", rs -> {
                add(out, rs.getString("name"));
                String attributes = rs.getString("attributes");
                if (attributes != null) {
                    Object aliases = json.readValue(attributes, MAP).get("aliases");
                    if (aliases instanceof List<?> list) {
                        for (Object alias : list) {
                            add(out, alias == null ? null : alias.toString());
                        }
                    }
                }
            }, placeId);
        } catch (RuntimeException e) {
            log.debug("No names for place {}: {}", placeId, e.toString());
        }
        return List.copyOf(out);
    }

    /** The names of a 040 region (country or admin-1). */
    public List<String> region(String code) {
        Set<String> out = new LinkedHashSet<>();
        if (code == null) {
            return List.of();
        }
        try {
            if (code.length() == 2) {
                for (String name : jdbc.queryForList("SELECT name FROM ref_country WHERE iso2 = ?", String.class,
                        code)) {
                    add(out, name);
                }
                for (String name : jdbc.queryForList("SELECT name FROM gazetteer_country WHERE iso2 = ?",
                        String.class, code)) {
                    add(out, name);
                }
            } else {
                jdbc.query("SELECT name, gn_code FROM ref_admin1 WHERE code = ?", rs -> {
                    add(out, rs.getString("name"));
                    String gn = rs.getString("gn_code");
                    if (gn != null) {
                        jdbc.query("SELECT name, ascii_name FROM gazetteer_admin1 WHERE code = ?", r -> {
                            add(out, r.getString("name"));
                            add(out, r.getString("ascii_name"));
                        }, gn);
                    }
                }, code);
            }
        } catch (RuntimeException e) {
            log.debug("No names for region {}: {}", code, e.toString());
        }
        return List.copyOf(out);
    }

    /** The display name, current aliases and current key values of an entity. */
    public List<String> entity(String entityId) {
        Set<String> out = new LinkedHashSet<>();
        try {
            for (String name : jdbc.queryForList("SELECT display_name FROM entity WHERE id = ?", String.class,
                    entityId)) {
                add(out, name);
            }
            for (String alias : jdbc.queryForList("SELECT alias FROM entity_alias WHERE entity_id = ? AND valid_to IS"
                    + " NULL", String.class, entityId)) {
                add(out, alias);
            }
            for (String key : jdbc.queryForList("SELECT key_value FROM entity_key WHERE entity_id = ? AND NOT retracted"
                    + " AND valid_to IS NULL", String.class, entityId)) {
                add(out, key);
            }
        } catch (RuntimeException e) {
            log.debug("No names for entity {}: {}", entityId, e.toString());
        }
        return List.copyOf(out);
    }

    private static void add(Set<String> out, String name) {
        if (name != null && name.strip().length() >= MIN_LENGTH) {
            out.add(name.strip());
        }
    }

    /** Copies a list (for callers that need a mutable one). */
    static List<String> copy(List<String> in) {
        return new ArrayList<>(in);
    }
}
