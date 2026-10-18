package ai.foresignal.chronicle.network;

import ai.foresignal.attention.AttentionTopicEntity;
import ai.foresignal.attention.AttentionTopicRepository;
import ai.foresignal.chronicle.ChronicleEntity;
import ai.foresignal.entity.EntityKeys;
import ai.foresignal.entity.Key;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Derives the global evidence keys of what a chronicle captured (prompt 134), reading its own storage
 * ({@code chronicle_version}) by seq, so keys exist only for what the chronicle actually holds.
 *
 * <ul>
 *   <li><b>evidence:</b> each captured record linked to a 116 canonical event gives {@code ev:} (with the
 *       observation's upstream id and originator) or {@code cell:} (family, H3 resolution-5 cell and UTC hour of the
 *       canonical event); its reference is the canonical event (the target of a merge, when merged);</li>
 *   <li><b>entities:</b> the 098 keys of the record (ICAO hex, MMSI, IMO and NORAD only) per UTC day;</li>
 *   <li><b>area-time:</b> the H3 resolution-4 cell and UTC day of the record's geometry;</li>
 *   <li><b>topics:</b> the keyword sets of the chronicle's linked attention topics and of the topics named by
 *       captured news articles.</li>
 * </ul>
 * Records of private sources (039) give no keys: private stays private, even as fingerprints.
 */
@Component
public class ChronicleKeyExtractor {

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private static final String READ = """
            SELECT v.seq, v.source_id, v.external_id, v.observed_at, v.event_time,
                   ST_Y(ST_PointOnSurface(v.geom)) AS lat, ST_X(ST_PointOnSurface(v.geom)) AS lon, v.payload::text
              FROM chronicle_version v
             WHERE v.chronicle_id = ? AND v.seq > ?
             ORDER BY v.seq
             LIMIT ?
            """;

    private static final String LINKS = """
            SELECT l.source_id, l.external_id, l.originator_id, l.upstream_event_id, e.id, e.merged_into, e.family,
                   e.event_time, ST_Y(ST_PointOnSurface(e.geom)) AS lat, ST_X(ST_PointOnSurface(e.geom)) AS lon
              FROM observation_link l
              JOIN canonical_event e ON e.id = l.canonical_event_id
             WHERE l.external_id = ANY(?) AND l.source_id = ANY(?)
            """;

    private final JdbcTemplate jdbc;
    private final SourceRegistry registry;
    private final SourceVisibility visibility;
    private final AttentionTopicRepository topics;
    private final JsonMapper json;

    /** Creates the extractor. */
    public ChronicleKeyExtractor(JdbcTemplate jdbc, SourceRegistry registry, SourceVisibility visibility,
            AttentionTopicRepository topics, JsonMapper json) {
        this.jdbc = jdbc;
        this.registry = registry;
        this.visibility = visibility;
        this.topics = topics;
        this.json = json;
    }

    /**
     * One derived key.
     *
     * @param keyType the key type
     * @param text the key text
     * @param ref the object it came from ({@code canonical-event:{id}} or {@code {keyType}:{value}} for an entity), or
     *     null
     * @param at when the chronicle saw it (the record's event time, else its observation time)
     */
    public record RawKey(String keyType, String text, String ref, Instant at) {}

    /**
     * The keys of one read.
     *
     * @param keys the keys (may repeat)
     * @param maxSeq the highest seq read, or the starting seq when nothing was read
     * @param rows the rows read
     */
    public record Batch(List<RawKey> keys, long maxSeq, int rows) {}

    private record Row(long seq, String sourceId, String externalId, Instant observedAt, Instant eventTime, Double lat,
            Double lon, String payload) {

        Instant at() {
            return eventTime != null ? eventTime : observedAt;
        }
    }

    /**
     * Reads up to {@code limit} captured rows after {@code afterSeq} and derives their keys.
     *
     * @param c the chronicle
     * @param afterSeq the last seq already turned into keys
     * @param limit the most rows to read
     * @return the keys and the new position
     */
    public Batch read(ChronicleEntity c, long afterSeq, int limit) {
        List<Row> rows = jdbc.query(READ, (rs, i) -> new Row(rs.getLong(1), rs.getString(2), rs.getString(3),
                instant(rs.getTimestamp(4)), instant(rs.getTimestamp(5)), (Double) rs.getObject(6),
                (Double) rs.getObject(7), rs.getString(8)), c.getId(), afterSeq, limit);
        if (rows.isEmpty()) {
            return new Batch(List.of(), afterSeq, 0);
        }
        long maxSeq = afterSeq;
        List<Row> kept = new ArrayList<>();
        Map<String, Boolean> privacy = new HashMap<>();
        for (Row r : rows) {
            maxSeq = Math.max(maxSeq, r.seq());
            if (!privacy.computeIfAbsent(r.sourceId(), this::isPrivate)) {
                kept.add(r);
            }
        }
        List<RawKey> out = new ArrayList<>();
        out.addAll(evidence(kept));
        Map<String, AttentionTopicEntity> topicCache = new HashMap<>();
        for (Row r : kept) {
            Instant at = r.at();
            String areaTime = EvidenceKeys.areaTime(r.lat(), r.lon(), at);
            if (areaTime != null) {
                out.add(new RawKey(NetworkVocabulary.AREA_TIME, areaTime, null, at));
            }
            Map<String, Object> payload = payload(r.payload());
            if (payload == null) {
                continue;
            }
            SourceDescriptor descriptor = registry.contains(r.sourceId()) ? registry.descriptor(r.sourceId()) : null;
            if (descriptor != null) {
                for (Key k : EntityKeys.extract(descriptor, payload)) {
                    String key = EvidenceKeys.entity(k.keyType(), k.keyValue(), at);
                    if (key != null) {
                        out.add(new RawKey(NetworkVocabulary.ENTITIES, key, k.keyType() + ":" + k.keyValue(), at));
                    }
                }
            }
            if (payload.get("topicIds") instanceof List<?> ids) {
                for (Object id : ids) {
                    if (id == null) {
                        continue;
                    }
                    AttentionTopicEntity t = topicCache.computeIfAbsent(id.toString(),
                            tid -> topics.findById(tid).orElse(null));
                    String key = topicKey(t);
                    if (key != null) {
                        out.add(new RawKey(NetworkVocabulary.TOPICS, key, null, at));
                    }
                }
            }
        }
        return new Batch(out, maxSeq, rows.size());
    }

    /**
     * The topic keys of a chronicle's linked attention topics, seen when the chronicle started capturing.
     *
     * @param c the chronicle
     * @return the keys
     */
    public List<RawKey> linkedTopics(ChronicleEntity c) {
        List<RawKey> out = new ArrayList<>();
        Instant at = c.getStartedAt() == null ? c.getCreatedAt() : c.getStartedAt();
        for (String id : c.getTopicIds()) {
            String key = topicKey(topics.findById(id).orElse(null));
            if (key != null) {
                out.add(new RawKey(NetworkVocabulary.TOPICS, key, null, at));
            }
        }
        return out;
    }

    /** The topic key of an attention topic: its keywords, else its name. */
    static String topicKey(AttentionTopicEntity t) {
        if (t == null) {
            return null;
        }
        List<String> words = t.getKeywords() == null || t.getKeywords().isEmpty() ? List.of(t.getName())
                : t.getKeywords();
        return EvidenceKeys.topic(words);
    }

    private List<RawKey> evidence(List<Row> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        Set<String> externalIds = new LinkedHashSet<>();
        Set<String> sourceIds = new LinkedHashSet<>();
        Map<String, Instant> seen = new HashMap<>();
        for (Row r : rows) {
            externalIds.add(r.externalId());
            sourceIds.add(r.sourceId());
            seen.merge(r.sourceId() + "\u0000" + r.externalId(), r.at(), (x, y) -> x.isBefore(y) ? x : y);
        }
        List<RawKey> out = new ArrayList<>();
        jdbc.query(LINKS, rs -> {
            String pair = rs.getString(1) + "\u0000" + rs.getString(2);
            Instant at = seen.get(pair);
            if (at == null) {
                return;
            }
            String originator = rs.getString(3);
            String upstream = rs.getString(4);
            String eventId = rs.getString(5);
            String mergedInto = rs.getString(6);
            String family = rs.getString(7);
            Instant eventTime = instant(rs.getTimestamp(8));
            Double lat = (Double) rs.getObject(9);
            Double lon = (Double) rs.getObject(10);
            String key = EvidenceKeys.event(family, originator, upstream);
            if (key == null) {
                key = EvidenceKeys.cell(family, lat, lon, eventTime);
            }
            if (key != null) {
                out.add(new RawKey(NetworkVocabulary.EVIDENCE, key, "canonical-event:"
                        + (mergedInto == null ? eventId : mergedInto), at));
            }
        }, (Object) externalIds.toArray(String[]::new), (Object) sourceIds.toArray(String[]::new));
        return out;
    }

    private boolean isPrivate(String sourceId) {
        try {
            return visibility.isPrivate(sourceId);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private Map<String, Object> payload(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return json.readValue(text, MAP);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Instant instant(Timestamp t) {
        return t == null ? null : t.toInstant();
    }
}
