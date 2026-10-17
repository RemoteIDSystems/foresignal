package ai.foresignal.pol;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.entity.EntityRepository;
import ai.foresignal.security.Ulids;
import ai.foresignal.store.purge.EntityPurgeListener;
import ai.foresignal.watch.WatchlistService;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Pattern-of-life subjects, places and stays (prompt 114, the {@code /v1/pol} API).
 *
 * <p>Opt-in only: nothing is analyzed until a subject is created. A subject whose members include a private source
 * (039) needs an admin token to create, and is visible (listed, read, labeled, recomputed, deleted) to admin tokens
 * only; to everyone else it does not exist. Deleting a subject deletes its stays and places; its records are
 * tombstoned by its generator on the next tick. The 039 per-entity purge cascades here ({@link EntityPurgeListener}):
 * a record subject of the purged record is deleted, and the stays other subjects formed from its fixes are deleted
 * and their places recomputed.
 */
@Service
public class PolService implements EntityPurgeListener {

    /** The longest label. */
    public static final int MAX_LABEL = 100;

    private final PolSubjectRepository subjects;
    private final PolStayRepository stays;
    private final PolPlaceRepository places;
    private final PolEngine engine;
    private final PolScheduler scheduler;
    private final EntityRepository entities;
    private final WatchlistService watchlists;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final TransactionTemplate tx;
    private final Clock clock;

    /** Creates the service. */
    public PolService(PolSubjectRepository subjects, PolStayRepository stays, PolPlaceRepository places,
            PolEngine engine, PolScheduler scheduler, EntityRepository entities, WatchlistService watchlists,
            JdbcTemplate jdbc, JsonMapper json, PlatformTransactionManager transactions, Clock clock) {
        this.subjects = subjects;
        this.stays = stays;
        this.places = places;
        this.engine = engine;
        this.scheduler = scheduler;
        this.entities = entities;
        this.watchlists = watchlists;
        this.jdbc = jdbc;
        this.json = json;
        this.tx = new TransactionTemplate(transactions);
        this.clock = clock;
    }

    /**
     * A page.
     *
     * @param items the items
     * @param nextCursor the next page's cursor, or null
     */
    public record Page(List<Map<String, Object>> items, String nextCursor) {}

    // ------------------------------------------------------------------------- subjects

    /** A page of the subjects the caller may see, in creation order. */
    public Page list(String cursor, int limit, boolean admin) {
        String after = decode(cursor);
        List<Map<String, Object>> items = new ArrayList<>();
        String next = null;
        for (PolSubjectEntity s : subjects.findAllByOrderByIdAsc()) {
            if ((s.isPrivateSubject() && !admin) || (after != null && s.getId().compareTo(after) <= 0)) {
                continue;
            }
            if (items.size() == limit) {
                next = encode((String) items.getLast().get("id"));
                break;
            }
            items.add(view(s));
        }
        return new Page(items, next);
    }

    /** The {@code PolSubject} view of a subject. */
    public Map<String, Object> view(PolSubjectEntity s) {
        return PolViews.subject(s, places.countBySubjectId(s.getId()), stays.countBySubjectId(s.getId()));
    }

    /**
     * A subject the caller may see.
     *
     * @throws NotFoundException when it does not exist or is private and the caller is not admin
     */
    public PolSubjectEntity get(String id, boolean admin) {
        return subjects.findById(id).filter(s -> admin || !s.isPrivateSubject())
                .orElseThrow(() -> NotFoundException.of("Pattern-of-life subject", id));
    }

    /**
     * Opts a subject in ({@code PolSubjectInput}).
     *
     * @throws ValidationException for invalid input or a subject already opted in
     * @throws NotFoundException when the record, entity or watchlist does not exist
     * @throws ForesignalException {@code 403 FORBIDDEN} for a private subject without an admin token
     */
    public PolSubjectEntity create(JsonNode body, String actor, boolean admin) {
        Input input = parse(body, json);
        String type = input.subjectType();
        String ref = input.ref();
        boolean enabled = input.enabled();
        Map<String, Object> params = input.params();
        checkRef(type, ref);
        if (!subjects.findBySubjectTypeAndRef(type, ref).isEmpty()) {
            throw new ValidationException("The subject is already opted in.",
                    List.of(new FieldError("ref", "is already a pattern-of-life subject")));
        }
        PolEngine.Privacy privacy = engine.privacy(engine.members(type, ref));
        if (privacy.privateSubject() && !admin) {
            throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN,
                    "The subject includes a private source; opting it in needs an admin token.");
        }
        Instant now = clock.instant();
        PolSubjectEntity s = new PolSubjectEntity(Ulids.next(now));
        s.setSubjectType(type);
        s.setRef(ref);
        s.setEnabled(enabled);
        s.setParams(params);
        s.setPrivateSubject(privacy.privateSubject());
        s.setPrecisionDecimals(privacy.decimals());
        s.setCreatedBy(actor);
        s.setCreatedAt(now);
        PolSubjectEntity saved = subjects.saveAndFlush(s);
        scheduler.kick();
        return saved;
    }

    /**
     * A parsed {@code PolSubjectInput}.
     *
     * @param subjectType {@code record}, {@code entity} or {@code watchlist}
     * @param ref the reference
     * @param enabled whether it is analyzed
     * @param params the parameters as given
     */
    public record Input(String subjectType, String ref, boolean enabled, Map<String, Object> params) {}

    /**
     * Parses a {@code PolSubjectInput} without touching the database (whether the reference exists is checked on
     * create).
     *
     * @throws ValidationException when it is not valid
     */
    @SuppressWarnings("unchecked")
    public static Input parse(JsonNode body, JsonMapper json) {
        if (body == null || !body.isObject()) {
            throw ValidationException.forField("body", "must be a PolSubjectInput object");
        }
        String type = text(body, "subjectType");
        if (type == null || !List.of("record", "entity", "watchlist").contains(type)) {
            throw ValidationException.forField("subjectType", "must be record, entity or watchlist");
        }
        String ref = text(body, "ref");
        if (ref == null || ref.isBlank() || ref.length() > 330) {
            throw ValidationException.forField("ref", "is required (at most 330 characters)");
        }
        ref = ref.strip();
        if ("record".equals(type) && !ref.matches("^[a-z0-9-]+/.+$")) {
            throw ValidationException.forField("ref", "must be {sourceId}/{externalId} for a record");
        }
        JsonNode enabledNode = body.get("enabled");
        if (enabledNode != null && !enabledNode.isNull() && !enabledNode.isBoolean()) {
            throw ValidationException.forField("enabled", "must be a boolean");
        }
        boolean enabled = enabledNode == null || enabledNode.isNull() || enabledNode.booleanValue();
        Map<String, Object> params = new LinkedHashMap<>();
        JsonNode paramsNode = body.get("params");
        if (paramsNode != null && !paramsNode.isNull()) {
            if (!paramsNode.isObject()) {
                throw ValidationException.forField("params", "must be an object");
            }
            params = json.convertValue(paramsNode, LinkedHashMap.class);
            PolParams.of(params);
        }
        return new Input(type, ref, enabled, params);
    }

    /**
     * Parses a {@code PolPlaceInput}: the label, or null to clear it.
     *
     * @throws ValidationException when it is not valid
     */
    public static String parseLabel(JsonNode body) {
        if (body == null || !body.isObject() || !body.has("label")) {
            throw ValidationException.forField("label", "is required (a string or null)");
        }
        JsonNode label = body.get("label");
        if (!label.isNull() && (!label.isString() || label.asString().length() > MAX_LABEL)) {
            throw ValidationException.forField("label", "must be a string of at most 100 characters, or null");
        }
        return label.isNull() || label.asString().isBlank() ? null : label.asString().strip();
    }

    /** 404 when the subject's reference names nothing. */
    private void checkRef(String type, String ref) {
        switch (type) {
            case "record" -> {
                int slash = ref.indexOf('/');
                Boolean exists = jdbc.queryForObject(
                        "SELECT EXISTS (SELECT 1 FROM record_head WHERE source_id = ? AND external_id = ?)", Boolean.class,
                        ref.substring(0, slash), ref.substring(slash + 1));
                if (!Boolean.TRUE.equals(exists)) {
                    throw NotFoundException.of("Record", ref);
                }
            }
            case "entity" -> {
                if (!entities.existsById(ref)) {
                    throw NotFoundException.of("Entity", ref);
                }
            }
            default -> watchlists.get(ref);
        }
    }

    /**
     * Deletes a subject, its stays and its places; its records are tombstoned by its generator.
     *
     * @throws NotFoundException when the caller may not see it
     */
    public void delete(String id, boolean admin) {
        PolSubjectEntity s = get(id, admin);
        tx.executeWithoutResult(status -> deleteSubject(s.getId()));
        scheduler.kick();
    }

    private void deleteSubject(String id) {
        stays.deleteBySubjectId(id);
        places.deleteBySubjectId(id);
        subjects.deleteById(id);
    }

    /**
     * Asks for the nightly clustering now; the subject's generator runs it on its next tick.
     *
     * @return the subject
     */
    public PolSubjectEntity recompute(String id, boolean admin) {
        PolSubjectEntity s = get(id, admin);
        s.setRecomputeRequested(true);
        PolSubjectEntity saved = subjects.saveAndFlush(s);
        scheduler.kick();
        return saved;
    }

    // ------------------------------------------------------------------------- places and stays

    /** A page of a subject's places. */
    public Page places(String subjectId, String cursor, int limit, boolean admin) {
        PolSubjectEntity s = get(subjectId, admin);
        String after = decode(cursor);
        List<Map<String, Object>> items = new ArrayList<>();
        String next = null;
        for (PolPlaceEntity p : places.findBySubjectIdOrderByIdAsc(s.getId())) {
            if (after != null && p.getId().compareTo(after) <= 0) {
                continue;
            }
            if (items.size() == limit) {
                next = encode((String) items.getLast().get("id"));
                break;
            }
            items.add(PolViews.place(p, s.isPrivateSubject()));
        }
        return new Page(items, next);
    }

    /**
     * Sets or clears a place's label ({@code PolPlaceInput}).
     *
     * @throws NotFoundException when the place or its subject is not visible to the caller
     */
    public Map<String, Object> label(String placeId, JsonNode body, boolean admin) {
        PolPlaceEntity place = places.findById(placeId).orElseThrow(() -> NotFoundException.of("Learned place", placeId));
        PolSubjectEntity s = get(place.getSubjectId(), admin);
        place.setLabel(parseLabel(body));
        PolPlaceEntity saved = places.saveAndFlush(place);
        scheduler.kick();
        return PolViews.place(saved, s.isPrivateSubject());
    }

    /** A page of a subject's stays in arrive order within {@code [from, to)}. */
    public Page stays(String subjectId, Instant from, Instant to, String cursor, int limit, boolean admin) {
        PolSubjectEntity s = get(subjectId, admin);
        String after = decode(cursor);
        List<Map<String, Object>> items = new ArrayList<>();
        String next = null;
        List<PolStayEntity> rows = from == null ? stays.findBySubjectIdOrderByArriveAscIdAsc(s.getId())
                : stays.findBySubjectIdAndArriveGreaterThanEqualOrderByArriveAscIdAsc(s.getId(), from);
        boolean skipping = after != null;
        for (PolStayEntity stay : rows) {
            if (to != null && !stay.getArrive().isBefore(to)) {
                break;
            }
            if (skipping) {
                if (stay.getId().equals(after)) {
                    skipping = false;
                }
                continue;
            }
            if (items.size() == limit) {
                next = encode((String) items.getLast().get("id"));
                break;
            }
            items.add(PolViews.stay(stay));
        }
        return new Page(items, next);
    }

    // ------------------------------------------------------------------------- purge cascade

    /**
     * The 039 purge cascade: deletes the record subjects of the purged record (their records are tombstoned by the
     * generator), and the stays other subjects formed from its fixes, whose places are then recomputed.
     */
    @Override
    public int onEntityPurged(String sourceId, String externalId) {
        String ref = sourceId + "/" + externalId;
        int removed = 0;
        for (PolSubjectEntity s : subjects.findBySubjectTypeAndRef("record", ref)) {
            removed += (int) stays.deleteBySubjectId(s.getId());
            removed += (int) places.deleteBySubjectId(s.getId());
            subjects.deleteById(s.getId());
            removed++;
        }
        for (PolSubjectEntity s : subjects.findAll()) {
            long n = stays.deleteBySubjectIdAndRecordRef(s.getId(), ref);
            if (n > 0) {
                removed += (int) n;
                s.setRecomputeRequested(true);
                subjects.save(s);
            }
        }
        scheduler.kick();
        return removed;
    }

    // ------------------------------------------------------------------------- helpers

    private static String text(JsonNode body, String field) {
        JsonNode n = body.get(field);
        return n == null || n.isNull() || !n.isString() ? null : n.asString();
    }

    static String encode(String id) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(("pol:" + id).getBytes(StandardCharsets.UTF_8));
    }

    static String decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String text = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            if (!text.startsWith("pol:")) {
                throw new IllegalArgumentException();
            }
            return text.substring(4);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(ErrorCodes.INVALID_CURSOR, "The cursor is not valid.",
                    List.of(new FieldError("cursor", "is not a cursor of this listing")));
        }
    }
}
