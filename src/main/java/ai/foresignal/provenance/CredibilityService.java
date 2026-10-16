package ai.foresignal.provenance;

import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.RecordRow;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * The Admiralty information-credibility grade {@code 1}–{@code 6} (prompt 109; Gap-Analysis §5),
 * computed at read time (never stored or hashed). Rule order:
 *
 * <ol>
 *   <li>{@code 5} when an open {@code derived-deception} record names the entity;</li>
 *   <li>{@code 1} when the record is a member of a {@code derived-corroboration} cluster with ≥ 3
 *       independent sources, at least one graded {@code A}/{@code B};</li>
 *   <li>otherwise the cluster's own {@code credibility} value (099: 2 for ≥ 2 sources, 3 for one);</li>
 *   <li>{@code 3} when the source is graded {@code A}/{@code B} and the record has no QA flags;</li>
 *   <li>otherwise {@code 6}.</li>
 * </ol>
 *
 * <p>Membership is loaded with one query per response ({@link #load()}), not per record.
 */
@Service
public class CredibilityService {

    /** The corroboration DERIVED source id (prompt 099). */
    public static final String CORROBORATION_SOURCE = "derived-corroboration";

    /** The corroboration kind (prompt 099). */
    public static final String CORROBORATION_KIND = "corroboration";

    /** The deception DERIVED source id (prompt 099). */
    public static final String DECEPTION_SOURCE = "derived-deception";

    /** The deception kind (prompt 099). */
    public static final String DECEPTION_KIND = "deception";

    private static final tools.jackson.databind.json.JsonMapper JSON =
            tools.jackson.databind.json.JsonMapper.builder().build();

    /** One corroboration cluster's grade inputs. */
    public record Cluster(int independentSources, int credibility, boolean hasHighReliability) {}

    /**
     * The per-response membership lookup.
     *
     * @param byMember the cluster of each {@code sourceId|externalId} member of a current corroboration record
     * @param deceptionEntities the {@code sourceId|externalId} members of open deception records
     * @param deceptionIds the entity ids (MMSI or ICAO hex, the deception record's {@code externalId}) of open
     *     deception records; an aircraft or vessel record with that {@code externalId} from any source is named
     */
    public record Lookup(Map<String, Cluster> byMember, Set<String> deceptionEntities, Set<String> deceptionIds) {

        /** A lookup without entity ids. */
        public Lookup(Map<String, Cluster> byMember, Set<String> deceptionEntities) {
            this(byMember, deceptionEntities, Set.of());
        }
    }

    /** The record kinds whose {@code externalId} is the entity a deception record names (099). */
    static final Set<String> ENTITY_KINDS = Set.of("aircraft", "vessel");

    private final JdbcTemplate jdbc;
    private final SourceRegistry registry;

    /** Reads an operator's reliability override (prompt 109); resolved lazily. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private org.springframework.beans.factory.ObjectProvider<ai.foresignal.source.SourceService> sources;

    /** Creates the service. */
    public CredibilityService(JdbcTemplate jdbc, SourceRegistry registry) {
        this.jdbc = jdbc;
        this.registry = registry;
    }

    /** Loads the corroboration membership and deception entities in one query each. */
    public Lookup load() {
        if (jdbc == null) {
            return new Lookup(Map.of(), Set.of());
        }
        Map<String, Cluster> byMember = new HashMap<>();
        Map<String, String> grades = new HashMap<>();
        jdbc.query("SELECT payload FROM record_head WHERE source_id = ? AND kind = ? AND tombstone = false",
                (org.springframework.jdbc.core.RowCallbackHandler) rs -> mergeCluster(byMember, rs.getString(1), grades),
                CORROBORATION_SOURCE, CORROBORATION_KIND);
        Set<String> deception = new HashSet<>();
        Set<String> deceptionIds = new HashSet<>();
        jdbc.query("SELECT external_id, payload FROM record_head WHERE source_id = ? AND kind = ? AND tombstone = false",
                (org.springframework.jdbc.core.RowCallbackHandler) rs -> {
                    if (rs.getString(1) != null) {
                        deceptionIds.add(rs.getString(1));
                    }
                    mergeDeception(deception, rs.getString(2));
                },
                DECEPTION_SOURCE, DECEPTION_KIND);
        return new Lookup(byMember, deception, deceptionIds);
    }

    /**
     * The lookup built from payloads already read (tests and callers with their own query): corroboration and
     * deception payloads in 099's shape, and the deception records' entity ids.
     */
    public Lookup lookupOf(java.util.Collection<String> corroborationPayloads,
            java.util.Collection<String> deceptionPayloads, java.util.Collection<String> deceptionEntityIds) {
        Map<String, Cluster> byMember = new HashMap<>();
        Map<String, String> grades = new HashMap<>();
        for (String payload : corroborationPayloads) {
            mergeCluster(byMember, payload, grades);
        }
        Set<String> deception = new HashSet<>();
        for (String payload : deceptionPayloads) {
            mergeDeception(deception, payload);
        }
        return new Lookup(byMember, deception, Set.copyOf(deceptionEntityIds));
    }

    /** The credibility of {@code row}, using a preloaded {@link Lookup}. */
    public int assess(RecordRow row, Lookup lookup) {
        String key = memberKey(row.sourceId(), row.externalId());
        if (lookup != null && (lookup.deceptionEntities().contains(key)
                || (ENTITY_KINDS.contains(row.kind()) && lookup.deceptionIds().contains(row.externalId())))) {
            return 5;
        }
        Cluster cluster = lookup == null ? null : lookup.byMember().get(key);
        if (cluster != null) {
            if (cluster.independentSources() >= 3 && cluster.hasHighReliability()) {
                return 1;
            }
            return cluster.credibility();
        }
        String reliability = reliability(row.sourceId());
        if (("A".equals(reliability) || "B".equals(reliability)) && hasNoQaFlags(row)) {
            return 3;
        }
        return 6;
    }

    /** The reliability grade of a source (prompt 109): the operator override, else the descriptor; default {@code F}. */
    public String reliability(String sourceId) {
        if (sourceId == null || !registry.contains(sourceId)) {
            return SourceDescriptor.DEFAULT_RELIABILITY;
        }
        ai.foresignal.source.SourceService service = sources == null ? null : sources.getIfAvailable();
        if (service != null) {
            try {
                return service.effectiveReliability(sourceId);
            } catch (RuntimeException e) {
                // Fall through to the descriptor.
            }
        }
        SourceDescriptor descriptor = registry.descriptor(sourceId);
        return descriptor == null ? SourceDescriptor.DEFAULT_RELIABILITY : descriptor.reliability();
    }

    /** The composed grade, for example {@code B2}. */
    public String grade(String reliability, int credibility) {
        return reliability + credibility;
    }

    private static boolean hasNoQaFlags(RecordRow row) {
        return row.qaFlags() == 0;
    }

    /**
     * The number of independent sources of a corroboration payload: 099 emits {@code independentSources} as the
     * list of distinct source ids, so its size; a number is taken as is; otherwise the distinct member sources.
     */
    static int independentSources(Map<String, ?> node, java.util.List<?> members) {
        Object value = node.get("independentSources");
        if (value instanceof java.util.Collection<?> ids) {
            return new HashSet<>(ids).size();
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        Set<Object> sources = new HashSet<>();
        for (Object member : members) {
            if (member instanceof Map<?, ?> m && m.get("sourceId") != null) {
                sources.add(m.get("sourceId"));
            }
        }
        return sources.size();
    }

    @SuppressWarnings("unchecked")
    private void mergeCluster(Map<String, Cluster> byMember, String payload, Map<String, String> grades) {
        try {
            Map<String, Object> node = JSON.readValue(payload, Map.class);
            Object members = node.get("members");
            if (!(members instanceof java.util.List<?> list)) {
                return;
            }
            int credibility = node.get("credibility") instanceof Number n ? n.intValue() : 3;
            int independent = independentSources(node, list);
            boolean high = false;
            for (Object member : list) {
                if (member instanceof Map<?, ?> m && m.get("sourceId") instanceof String sid) {
                    String rel = grades.computeIfAbsent(sid, this::reliability);
                    if ("A".equals(rel) || "B".equals(rel)) {
                        high = true;
                    }
                }
            }
            for (Object member : list) {
                if (member instanceof Map<?, ?> m && m.get("sourceId") instanceof String sid
                        && m.get("externalId") instanceof String eid) {
                    byMember.put(memberKey(sid, eid), new Cluster(independent, credibility, high));
                }
            }
        } catch (RuntimeException e) {
            // A malformed payload contributes nothing.
        }
    }

    @SuppressWarnings("unchecked")
    private static void mergeDeception(Set<String> out, String payload) {
        try {
            Map<String, Object> node = JSON.readValue(payload, Map.class);
            Object members = node.get("members");
            if (members instanceof java.util.List<?> list) {
                for (Object member : list) {
                    if (member instanceof Map<?, ?> m && m.get("sourceId") instanceof String sid
                            && m.get("externalId") instanceof String eid) {
                        out.add(memberKey(sid, eid));
                    }
                }
            }
        } catch (RuntimeException e) {
            // Ignore.
        }
    }

    private static String memberKey(String sourceId, String externalId) {
        return sourceId + "|" + externalId;
    }

    /** A fresh mutable map for callers building a look-up. */
    public static Map<String, Cluster> emptyMembers() {
        return new LinkedHashMap<>();
    }
}
