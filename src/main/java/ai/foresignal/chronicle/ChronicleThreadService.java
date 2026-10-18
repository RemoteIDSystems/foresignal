package ai.foresignal.chronicle;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectService;
import ai.foresignal.object.ResolveContext;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * A chronicle's thread (prompt 131): appending items (published on the {@code chronicle} SSE topic after commit),
 * reading pages with each item's envelope, and the per-principal read cursors and unread counts of the 033 pattern.
 * Items about private data are shown only to callers with private access (EGR-1).
 */
@Service
public class ChronicleThreadService {

    private static final Logger log = LoggerFactory.getLogger(ChronicleThreadService.class);

    private final ChronicleEventRepository events;
    private final ChronicleTopic topic;
    private final ObjectService objects;
    private final JdbcTemplate jdbc;
    private final Clock clock;

    /** Creates the service. */
    public ChronicleThreadService(ChronicleEventRepository events, ChronicleTopic topic, ObjectService objects,
            JdbcTemplate jdbc, Clock clock) {
        this.events = events;
        this.topic = topic;
        this.objects = objects;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /**
     * Builds a thread item (not yet saved).
     *
     * @param chronicleId the chronicle
     * @param type one of {@link ChronicleVocabulary#ITEM_TYPES}
     * @param at when it happened
     * @param title the title
     */
    public ChronicleEventEntity item(String chronicleId, String type, Instant at, String title) {
        return new ChronicleEventEntity(chronicleId, type, at == null ? clock.instant() : at, title, clock.instant());
    }

    /**
     * Saves a thread item and publishes it on the {@code chronicle} topic once the current transaction commits (at
     * once without one).
     *
     * @return the saved item
     */
    public ChronicleEventEntity append(ChronicleEventEntity item) {
        ChronicleEventEntity saved = events.saveAndFlush(item);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish(saved);
                }
            });
        } else {
            publish(saved);
        }
        return saved;
    }

    private void publish(ChronicleEventEntity item) {
        try {
            topic.publish(item);
        } catch (RuntimeException e) {
            log.warn("Could not publish chronicle item {}: {}", item.getId(), e.toString());
        }
    }

    /**
     * Writes the {@code status-change} item of a lifecycle transition, whose snapshot is the
     * {@code ChronicleStatusEvent} (SSE {@code chronicle.status}).
     *
     * @param c the chronicle after the transition
     * @param previous the status before, or null
     * @param reason why, in a few words
     * @param bytesUsed the bytes the chronicle holds
     * @return the saved item
     */
    public ChronicleEventEntity statusChange(ChronicleEntity c, String previous, String reason, long bytesUsed) {
        Instant now = clock.instant();
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("chronicleId", c.getId());
        event.put("status", c.getStatus());
        event.put("previousStatus", previous);
        event.put("capState", c.getCapState());
        event.put("bytesUsed", bytesUsed);
        event.put("reason", reason);
        event.put("at", now.toString());
        ChronicleEventEntity item = item(c.getId(), ChronicleVocabulary.ITEM_STATUS_CHANGE, now, "Chronicle " + reason);
        item.setSummary((previous == null ? "" : previous + " → ") + c.getStatus());
        item.setSnapshot(event);
        return append(item);
    }

    /**
     * The {@code ChronicleThreadItem} view of an item.
     *
     * @param row the item
     * @param envelope its object's envelope, or null
     * @return the view
     */
    public static Map<String, Object> item(ChronicleEventEntity row, ObjectEnvelope envelope) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", row.getId());
        out.put("chronicleId", row.getChronicleId());
        out.put("type", row.getType());
        out.put("at", row.getAt().toString());
        out.put("seq", row.getSeq());
        out.put("layerId", row.getLayerId());
        out.put("title", row.getTitle());
        out.put("summary", row.getSummary());
        out.put("ref", row.getRef());
        out.put("envelope", envelope);
        out.put("snapshot", row.getSnapshot());
        out.put("gap", row.getGap());
        out.put("backfilled", row.isBackfilled());
        return out;
    }

    /**
     * One page of a chronicle's thread, oldest first.
     *
     * @param chronicleId the chronicle (canonical id)
     * @param caller the caller (private items need private access)
     * @param after only items that happened after this time, or null
     * @param types only these item types, or empty for all
     * @param cursor the opaque cursor of the previous page, or null
     * @param limit the page size
     * @return the {@code ChronicleThreadPage}
     */
    public Map<String, Object> page(String chronicleId, ObjectCaller caller, Instant after, Collection<String> types,
            String cursor, int limit) {
        long afterId = cursorId(cursor);
        StringBuilder sql = new StringBuilder("SELECT id FROM chronicle_event WHERE chronicle_id = ? AND id > ?");
        List<Object> args = new ArrayList<>(List.of(chronicleId, afterId));
        if (after != null) {
            sql.append(" AND at > ?");
            args.add(Timestamp.from(after));
        }
        if (types != null && !types.isEmpty()) {
            sql.append(" AND type = ANY(?)");
            args.add(types.toArray(String[]::new));
        }
        if (!caller.seesPrivate()) {
            sql.append(" AND NOT private");
        }
        sql.append(" ORDER BY id LIMIT ?");
        args.add(limit + 1);
        List<Long> ids = jdbc.queryForList(sql.toString(), Long.class, args.toArray());
        boolean more = ids.size() > limit;
        List<Long> pageIds = more ? ids.subList(0, limit) : ids;
        List<ChronicleEventEntity> rows = new ArrayList<>(events.findAllById(pageIds));
        rows.sort(java.util.Comparator.comparing(ChronicleEventEntity::getId));
        Map<ObjectRef, ObjectEnvelope> envelopes = envelopes(rows, caller);
        List<Map<String, Object>> items = new ArrayList<>();
        for (ChronicleEventEntity row : rows) {
            ObjectEnvelope env = null;
            if (row.getRef() != null && ObjectRef.isValid(row.getRef())) {
                env = envelopes.get(ObjectRef.parse(row.getRef()));
            }
            items.add(item(row, env));
        }
        Long readUpTo = caller.principal() == null ? null : readUpTo(chronicleId, caller.principal());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", more && !rows.isEmpty() ? cursor(rows.getLast().getId()) : null);
        out.put("unreadCount", unread(chronicleId, caller));
        out.put("readUpTo", readUpTo);
        return out;
    }

    private Map<ObjectRef, ObjectEnvelope> envelopes(List<ChronicleEventEntity> rows, ObjectCaller caller) {
        List<ObjectRef> refs = new ArrayList<>();
        for (ChronicleEventEntity row : rows) {
            if (row.getRef() != null && ObjectRef.isValid(row.getRef())) {
                refs.add(ObjectRef.parse(row.getRef()));
            }
        }
        if (refs.isEmpty()) {
            return Map.of();
        }
        try {
            return objects.resolveRefs(refs, new ResolveContext(caller, null));
        } catch (RuntimeException e) {
            log.debug("Could not resolve thread envelopes: {}", e.toString());
            return Map.of();
        }
    }

    /** The caller's read cursor, or null when it never read. */
    public Long readUpTo(String chronicleId, String principalId) {
        List<Long> rows = jdbc.queryForList("SELECT up_to_id FROM chronicle_read_cursor WHERE chronicle_id = ?"
                + " AND principal_id = ?", Long.class, chronicleId, principalId);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    /** The caller's unread items in a chronicle (0 for a caller without a principal). */
    public int unread(String chronicleId, ObjectCaller caller) {
        if (caller == null || caller.principal() == null) {
            return 0;
        }
        Long upTo = readUpTo(chronicleId, caller.principal());
        Integer n = jdbc.queryForObject("SELECT count(*) FROM chronicle_event WHERE chronicle_id = ? AND id > ?"
                + (caller.seesPrivate() ? "" : " AND NOT private"), Integer.class, chronicleId,
                upTo == null ? 0L : upTo);
        return n == null ? 0 : n;
    }

    /**
     * Moves the caller's read cursor forward to {@code upToId} (never back).
     *
     * @throws ValidationException when {@code upToId} is negative
     */
    public void markRead(String chronicleId, String principalId, long upToId) {
        if (upToId < 0) {
            throw ValidationException.forField("upToId", "must not be negative");
        }
        jdbc.update("INSERT INTO chronicle_read_cursor (chronicle_id, principal_id, up_to_id, updated_at)"
                + " VALUES (?, ?, ?, ?) ON CONFLICT (chronicle_id, principal_id) DO UPDATE SET"
                + " up_to_id = GREATEST(chronicle_read_cursor.up_to_id, EXCLUDED.up_to_id),"
                + " updated_at = EXCLUDED.updated_at", chronicleId, principalId, upToId,
                Timestamp.from(clock.instant()));
    }

    /** Deletes a chronicle's thread and read cursors. */
    public void deleteThread(String chronicleId) {
        jdbc.update("DELETE FROM chronicle_read_cursor WHERE chronicle_id = ?", chronicleId);
        jdbc.update("DELETE FROM chronicle_event WHERE chronicle_id = ?", chronicleId);
    }

    /** An opaque cursor after item {@code id}. */
    static String cursor(long id) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(("k:" + id).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * The item id of an opaque cursor (0 for none).
     *
     * @throws ValidationException {@code INVALID_CURSOR} for a malformed one
     */
    static long cursorId(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0;
        }
        try {
            String s = new String(Base64.getUrlDecoder().decode(cursor.strip()), StandardCharsets.UTF_8);
            if (!s.startsWith("k:")) {
                throw new IllegalArgumentException(s);
            }
            return Long.parseLong(s.substring(2));
        } catch (IllegalArgumentException e) {
            throw new ValidationException(ErrorCodes.INVALID_CURSOR, "The cursor is not valid.",
                    List.of(new ai.foresignal.api.error.FieldError("cursor", "is not a cursor this endpoint issued")));
        }
    }
}
