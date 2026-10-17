package ai.foresignal.collab;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectService;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.place.PlaceService;
import ai.foresignal.search.DocInput;
import ai.foresignal.search.SearchRequest;
import ai.foresignal.search.SearchService;
import ai.foresignal.security.Tlp;
import ai.foresignal.security.Ulids;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Saved searches (prompt 130): a named {@code SavedSearchQuery} run over object envelopes. A run is 027 hybrid search
 * over records, alerts and embedded evidence items for {@code text}, a text match over evidence items, and the
 * objects carrying every listed tag, then structured filters on the envelopes the caller can see (types, area, time
 * window, status, severity). A saved search with {@code notify} sends its owner {@code saved-search.matches} at most
 * hourly when new matches appear ({@link #notifyDue}, every 15 minutes).
 */
@Service
public class SavedSearchService {

    private static final Logger log = LoggerFactory.getLogger(SavedSearchService.class);

    /** The most matches a run considers. */
    public static final int MAX_MATCHES = 500;

    /** The shortest time between two match notifications of one saved search. */
    public static final Duration NOTIFY_INTERVAL = Duration.ofHours(1);

    private static final Set<String> INPUT = Set.of("name", "query", "visibility", "tlp", "notify");
    private static final Set<String> QUERY = Set.of("types", "text", "filters", "area", "timeWindow", "tags");

    private final SavedSearchRepository searches;
    private final AccessResolver access;
    private final ShareService shares;
    private final SearchService search;
    private final ObjectService objects;
    private final TagService tags;
    private final TagAssignmentRepository assignments;
    private final EvidenceItemRepository evidence;
    private final PlaceService places;
    private final PrincipalCallers principalCallers;
    private final PrincipalViews views;
    private final PersonalNotifier notifier;
    private final CollabProperties properties;
    private final JsonMapper json;
    private final Clock clock;
    private final GeoJsonReader geoReader = new GeoJsonReader();

    /** Creates the service. */
    public SavedSearchService(SavedSearchRepository searches, AccessResolver access, ShareService shares,
            SearchService search, ObjectService objects, TagService tags, TagAssignmentRepository assignments,
            EvidenceItemRepository evidence, PlaceService places, PrincipalCallers principalCallers,
            PrincipalViews views, PersonalNotifier notifier, CollabProperties properties, JsonMapper json,
            Clock clock) {
        this.searches = searches;
        this.access = access;
        this.shares = shares;
        this.search = search;
        this.objects = objects;
        this.tags = tags;
        this.assignments = assignments;
        this.evidence = evidence;
        this.places = places;
        this.principalCallers = principalCallers;
        this.views = views;
        this.notifier = notifier;
        this.properties = properties;
        this.json = json;
        this.clock = clock;
    }

    /** The reference of a saved search. */
    public static String ref(String id) {
        return ObjectTypes.SAVED_SEARCH + ":" + id;
    }

    // ------------------------------------------------------------------------- CRUD

    /**
     * The saved search {@code id} when the caller sees it.
     *
     * @throws NotFoundException {@code UNKNOWN_SAVED_SEARCH} otherwise
     */
    @Transactional
    public SavedSearchEntity get(String id, ObjectCaller caller) {
        SavedSearchEntity s = id == null ? null : searches.findById(id).orElse(null);
        if (s == null || access.role(caller, ref(s.getId())) == null) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_SAVED_SEARCH, "No saved search '" + id
                    + "' exists, or you may not see it.");
        }
        return s;
    }

    /** Saved searches the caller sees, by id ({@code listSavedSearches}). */
    @Transactional
    public Map<String, Object> list(ObjectCaller caller, String cursor, int limit) {
        List<SavedSearchEntity> all = searches.findAllByOrderByIdAsc();
        Map<String, AclRole> roles = access.roles(caller, all.stream().map(s -> ref(s.getId())).toList());
        int offset = CollabInputs.offset(cursor);
        List<Map<String, Object>> items = new ArrayList<>();
        int seen = 0;
        boolean more = false;
        for (SavedSearchEntity s : all) {
            if (roles.get(ref(s.getId())) == null) {
                continue;
            }
            if (seen++ < offset) {
                continue;
            }
            if (items.size() == limit) {
                more = true;
                break;
            }
            items.add(view(s));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", more ? CollabInputs.cursor(offset + limit) : null);
        return out;
    }

    /** Creates a saved search ({@code createSavedSearch}); private to its creator until shared. */
    @Transactional
    public SavedSearchEntity create(JsonNode body, ObjectCaller caller) {
        CollabInputs.object(body, "SavedSearchInput", INPUT);
        Instant now = clock.instant();
        SavedSearchEntity s = new SavedSearchEntity(Ulids.next(now), CollabInputs.required(body, "name", 1, 200)
                .strip(), caller.principal(), now);
        s.setQuery(query(body.get("query"), caller));
        s.setNotify(CollabInputs.bool(body, "notify", false));
        Tlp tlp = Tlp.parse("tlp", CollabInputs.text(body, "tlp", 1, 16), access.defaultTlp());
        Visibility visibility = Visibility.parse("visibility", CollabInputs.text(body, "visibility", 1, 16),
                Visibility.PRIVATE);
        shares.checkTlp(tlp, caller.principal(), List.of(), visibility);
        searches.save(s);
        access.register(ref(s.getId()), caller.principal(), visibility, tlp);
        return s;
    }

    /** Applies a JSON merge patch ({@code updateSavedSearch}; owner or editor). */
    @Transactional
    public SavedSearchEntity patch(String id, JsonNode body, ObjectCaller caller) {
        SavedSearchEntity s = get(id, caller);
        ShareableObjectEntity rec = access.require(caller, ref(s.getId()), AccessAction.EDIT);
        CollabInputs.object(body, "SavedSearchPatch", INPUT);
        if (body.has("name")) {
            s.setName(CollabInputs.required(body, "name", 1, 200).strip());
        }
        if (body.has("query")) {
            s.setQuery(query(body.get("query"), caller));
            s.setLastMatchRefs(List.of());
        }
        if (body.has("notify")) {
            s.setNotify(CollabInputs.bool(body, "notify", false));
        }
        Tlp tlp = body.hasNonNull("tlp") ? Tlp.parse("tlp", CollabInputs.text(body, "tlp", 1, 16), null)
                : Tlp.fromWire(rec.getTlp());
        Visibility visibility = body.hasNonNull("visibility")
                ? Visibility.parse("visibility", CollabInputs.text(body, "visibility", 1, 16), null)
                : rec.getVisibility();
        shares.checkTlp(tlp, rec.getOwner(), List.of(), visibility);
        rec.setTlp(tlp.wireValue());
        rec.setVisibility(visibility);
        access.save(rec);
        s.setUpdatedAt(clock.instant());
        return searches.save(s);
    }

    /** Deletes a saved search ({@code deleteSavedSearch}; owner only). */
    @Transactional
    public void delete(String id, ObjectCaller caller) {
        SavedSearchEntity s = get(id, caller);
        access.require(caller, ref(s.getId()), AccessAction.DELETE);
        searches.delete(s);
        access.forget(ref(s.getId()));
    }

    /** The {@code SavedSearch} view. */
    @Transactional
    public Map<String, Object> view(SavedSearchEntity s) {
        ShareableObjectEntity rec = access.record(ref(s.getId())).orElse(null);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", s.getId());
        out.put("ref", ref(s.getId()));
        out.put("name", s.getName());
        out.put("owner", views.summary(rec == null ? s.getCreatedBy() : rec.getOwner()));
        out.put("query", s.getQuery());
        out.put("visibility", rec == null ? Visibility.PRIVATE.wireValue() : rec.getVisibility().wireValue());
        out.put("tlp", rec == null ? access.defaultTlp().wireValue() : rec.getTlp());
        out.put("notify", s.isNotify());
        out.put("lastRunAt", s.getLastRunAt());
        out.put("lastNotifiedAt", s.getLastNotifiedAt());
        out.put("lastMatchCount", s.getLastMatchCount());
        out.put("createdAt", s.getCreatedAt());
        out.put("updatedAt", s.getUpdatedAt());
        return out;
    }

    // ------------------------------------------------------------------------- running

    /**
     * One match.
     *
     * @param envelope the matching object
     * @param score its search score, or null for a filter match
     * @param matchedBy {@code text}, {@code semantic}, {@code hybrid} or {@code filter}
     */
    public record Hit(ObjectEnvelope envelope, Double score, String matchedBy) {}

    /**
     * A run's matches.
     *
     * @param hits the visible matches, best first
     * @param semantic true when semantic search contributed
     */
    public record Run(List<Hit> hits, boolean semantic) {}

    /**
     * Runs a saved search now for the caller ({@code runSavedSearch}).
     *
     * @return the {@code SavedSearchResultPage}
     */
    @Transactional
    public Map<String, Object> results(String id, ObjectCaller caller, String cursor, int limit) {
        SavedSearchEntity s = get(id, caller);
        Run run = execute(s.getQuery(), caller);
        int offset = CollabInputs.offset(cursor);
        List<Map<String, Object>> items = new ArrayList<>();
        for (Hit h : run.hits().subList(Math.min(offset, run.hits().size()), Math.min(offset + limit,
                run.hits().size()))) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("envelope", h.envelope());
            item.put("score", h.score());
            item.put("matchedBy", h.matchedBy());
            items.add(item);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", offset + limit < run.hits().size() ? CollabInputs.cursor(offset + limit) : null);
        out.put("ranAt", clock.instant());
        out.put("semantic", run.semantic());
        return out;
    }

    /** Runs a stored query for {@code caller}: search and tag matches, then the envelope filters. */
    @SuppressWarnings("unchecked")
    public Run execute(Map<String, Object> query, ObjectCaller caller) {
        JsonNode q = json.valueToTree(query);
        List<String> types = CollabInputs.strings(q, "types", 50);
        String text = q.hasNonNull("text") ? q.get("text").asString() : null;
        JsonNode filters = q.get("filters");
        Instant[] window = window(q.get("timeWindow"));
        BboxFilter area = area(q.get("area"));
        Map<String, Hit> hits = new LinkedHashMap<>();
        Map<String, ObjectRef> refs = new LinkedHashMap<>();
        boolean semantic = false;
        if (text != null && !text.isBlank()) {
            Set<String> docTypes = new LinkedHashSet<>();
            if (types.isEmpty() || types.contains(ObjectTypes.RECORD)) {
                docTypes.add(DocInput.RECORD);
            }
            if (types.isEmpty() || types.contains(ObjectTypes.ALERT)) {
                docTypes.add(DocInput.ALERT);
            }
            if (types.isEmpty() || types.contains(ObjectTypes.EVIDENCE_ITEM)) {
                docTypes.add(DocInput.EVIDENCE_ITEM);
            }
            if (!docTypes.isEmpty()) {
                SearchRequest request = new SearchRequest(text.strip(), SearchRequest.Mode.HYBRID, docTypes,
                        strings(filters, "sources"), strings(filters, "kinds"), area, window[0], window[1], false,
                        SearchRequest.MAX_LIMIT);
                Map<String, Object> result = search.search(request, caller.excludedSources());
                semantic = !"text".equals(result.get("mode")) && !Boolean.TRUE.equals(result.get("degraded"));
                String matchedBy = semantic ? "hybrid" : "text";
                for (Map<String, Object> item : (List<Map<String, Object>>) result.get("items")) {
                    ObjectRef r = refOf(item);
                    if (r != null && refs.putIfAbsent(r.format(), r) == null) {
                        Object score = item.get("score");
                        hits.put(r.format(), new Hit(null, score instanceof Number n ? n.doubleValue() : null,
                                matchedBy));
                    }
                }
            }
            if (types.isEmpty() || types.contains(ObjectTypes.EVIDENCE_ITEM)) {
                String needle = text.strip().toLowerCase(Locale.ROOT);
                for (EvidenceItemEntity e : evidence.findByIdLessThanOrderByIdDesc("~",
                        org.springframework.data.domain.Limit.of(MAX_MATCHES))) {
                    if (e.getTitle().toLowerCase(Locale.ROOT).contains(needle) || (e.getDescription() != null
                            && e.getDescription().toLowerCase(Locale.ROOT).contains(needle))) {
                        ObjectRef r = ObjectRef.of(ObjectTypes.EVIDENCE_ITEM, e.getId());
                        if (refs.putIfAbsent(r.format(), r) == null) {
                            hits.put(r.format(), new Hit(null, null, "text"));
                        }
                    }
                }
            }
        }
        List<String> tagIds = CollabInputs.strings(q, "tags", 50);
        if (!tagIds.isEmpty()) {
            Set<String> carrying = null;
            for (String tagId : tagIds) {
                Set<String> withTag = new HashSet<>();
                try {
                    TagEntity t = tags.get(tagId, caller);
                    for (TagAssignmentEntity a : assignments.findByTagId(t.getId())) {
                        withTag.add(a.getObjectRef());
                    }
                } catch (NotFoundException e) {
                    // a tag the caller no longer sees matches nothing
                }
                if (carrying == null) {
                    carrying = withTag;
                } else {
                    carrying.retainAll(withTag);
                }
            }
            if (text == null || text.isBlank()) {
                for (String r : carrying) {
                    if (ObjectRef.isValid(r)) {
                        refs.put(r, ObjectRef.parse(r));
                        hits.put(r, new Hit(null, null, "filter"));
                    }
                }
            } else {
                Set<String> keep = carrying;
                refs.keySet().removeIf(r -> !keep.contains(r));
            }
        }
        Map<ObjectRef, ObjectEnvelope> envelopes = objects.resolveRefs(refs.values().stream().limit(MAX_MATCHES)
                .toList(), new ResolveContext(caller, null));
        List<Hit> out = new ArrayList<>();
        for (Map.Entry<String, ObjectRef> e : refs.entrySet()) {
            ObjectEnvelope env = envelopes.get(e.getValue());
            if (env == null || !matches(env, types, filters, area, window)) {
                continue;
            }
            Hit h = hits.get(e.getKey());
            out.add(new Hit(env, h == null ? null : h.score(), h == null ? "filter" : h.matchedBy()));
        }
        return new Run(out, semantic);
    }

    private static boolean matches(ObjectEnvelope env, List<String> types, JsonNode filters, BboxFilter area,
            Instant[] window) {
        if (!types.isEmpty() && !types.contains(env.type())) {
            return false;
        }
        if (filters != null && filters.isObject()) {
            if (filters.hasNonNull("status") && !filters.get("status").asString().equals(env.status())) {
                return false;
            }
            if (filters.hasNonNull("severity") && !filters.get("severity").asString().equals(env.severity())) {
                return false;
            }
        }
        if (area != null) {
            if (env.bbox() == null || env.bbox().size() != 4) {
                return false;
            }
            Envelope box = new Envelope(env.bbox().get(0), env.bbox().get(2), env.bbox().get(1), env.bbox().get(3));
            if (area.envelopes().stream().noneMatch(box::intersects)) {
                return false;
            }
        }
        Instant t = env.time() == null ? null : env.time().start() != null ? env.time().start()
                : env.time().observedAt();
        if (window[0] != null && (t == null || t.isBefore(window[0]))) {
            return false;
        }
        return window[1] == null || (t != null && t.isBefore(window[1]));
    }

    private static ObjectRef refOf(Map<String, Object> item) {
        String type = String.valueOf(item.get("docType"));
        String id = String.valueOf(item.get("docId"));
        try {
            return switch (type) {
                case DocInput.RECORD -> {
                    Object source = item.get("source");
                    if (source == null || !id.startsWith(source + ":")) {
                        yield null;
                    }
                    yield ObjectRef.record(String.valueOf(source), id.substring(String.valueOf(source).length() + 1),
                            null);
                }
                case DocInput.ALERT -> ObjectRef.of(ObjectTypes.ALERT, id);
                case DocInput.EVIDENCE_ITEM -> ObjectRef.of(ObjectTypes.EVIDENCE_ITEM, id);
                default -> null;
            };
        } catch (ValidationException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------------- notifications

    /**
     * Runs every saved search with {@code notify} whose last notification is at least an hour old, as its owner, and
     * sends {@code saved-search.matches} when it has matches the previous run did not.
     *
     * @return how many notifications were sent
     */
    public int notifyDue() {
        Instant now = clock.instant();
        int sent = 0;
        for (SavedSearchEntity s : searches.findByNotifyTrueOrderByIdAsc()) {
            if (s.getLastNotifiedAt() != null && now.isBefore(s.getLastNotifiedAt().plus(NOTIFY_INTERVAL))) {
                continue;
            }
            try {
                if (notifyOne(s, now)) {
                    sent++;
                }
            } catch (RuntimeException e) {
                log.warn("Saved search {} could not run: {}", s.getId(), e.toString());
            }
        }
        return sent;
    }

    /** The scheduled tick (every 15 minutes) unless {@code foresignal.collab.saved-search-scheduled} is off. */
    @Scheduled(fixedDelayString = "${foresignal.collab.saved-search-interval:PT15M}",
            initialDelayString = "${foresignal.collab.saved-search-initial-delay:PT2M}")
    public void tick() {
        if (properties.savedSearchScheduled()) {
            notifyDue();
        }
    }

    @Transactional
    boolean notifyOne(SavedSearchEntity s, Instant now) {
        ShareableObjectEntity rec = access.record(ref(s.getId())).orElse(null);
        if (rec == null) {
            return false;
        }
        ObjectCaller owner = principalCallers.of(rec.getOwner()).orElse(null);
        if (owner == null) {
            return false;
        }
        Run run = execute(s.getQuery(), owner);
        List<String> current = run.hits().stream().map(h -> h.envelope().ref()).toList();
        Set<String> previous = new HashSet<>(s.getLastMatchRefs());
        boolean firstRun = s.getLastRunAt() == null;
        List<String> fresh = current.stream().filter(r -> !previous.contains(r)).toList();
        s.setLastRunAt(now);
        s.setLastMatchCount(current.size());
        s.setLastMatchRefs(current);
        boolean notify = !firstRun && !fresh.isEmpty();
        if (notify) {
            s.setLastNotifiedAt(now);
            notifier.send(new PersonalNotifier.Notice(rec.getOwner(), MeEvents.SAVED_SEARCH_MATCHES,
                    fresh.size() + " new match" + (fresh.size() == 1 ? "" : "es") + " for " + s.getName(), null,
                    ref(s.getId()), null, rec.getTlp(), MeEvents.savedSearchMatches(s.getId(), s.getName(),
                            fresh.size(), fresh, now)));
        }
        searches.save(s);
        return notify;
    }

    // ------------------------------------------------------------------------- query parsing

    /** Validates a {@code SavedSearchQuery}; it needs text or tags. */
    Map<String, Object> query(JsonNode q, ObjectCaller caller) {
        CollabInputs.object(q, "SavedSearchQuery", QUERY);
        for (String t : CollabInputs.strings(q, "types", 50)) {
            if (!ObjectTypes.isType(t)) {
                throw ValidationException.forField("query.types", "'" + t + "' is not an object type");
            }
        }
        String text = CollabInputs.text(q, "text", 0, 1000);
        List<String> tagIds = CollabInputs.strings(q, "tags", 50);
        if ((text == null || text.isBlank()) && tagIds.isEmpty()) {
            throw ValidationException.forField("query", "needs text or tags");
        }
        for (String id : tagIds) {
            tags.get(id, caller);
        }
        JsonNode filters = q.get("filters");
        if (filters != null && !filters.isNull() && !filters.isObject()) {
            throw ValidationException.forField("query.filters", "must be an object");
        }
        window(q.get("timeWindow"));
        area(q.get("area"));
        return json.convertValue(q, new TypeReference<Map<String, Object>>() {});
    }

    private Instant[] window(JsonNode w) {
        Instant[] out = new Instant[2];
        if (w == null || w.isNull()) {
            return out;
        }
        if (!w.isObject()) {
            throw ValidationException.forField("query.timeWindow", "must be an object");
        }
        out[0] = CollabInputs.instant(w, "start");
        out[1] = CollabInputs.instant(w, "end");
        String relative = CollabInputs.text(w, "relative", 1, 32);
        if (relative != null) {
            out[0] = back(relative.strip());
        }
        return out;
    }

    /** {@code now} minus an ISO-8601 duration ({@code PT6H}) or period ({@code P7D}). */
    private Instant back(String relative) {
        try {
            return clock.instant().minus(Duration.parse(relative));
        } catch (DateTimeParseException e) {
            try {
                return clock.instant().atZone(java.time.ZoneOffset.UTC).minus(java.time.Period.parse(relative))
                        .toInstant();
            } catch (DateTimeParseException e2) {
                throw ValidationException.forField("query.timeWindow.relative", "must be an ISO-8601 duration");
            }
        }
    }

    private BboxFilter area(JsonNode a) {
        if (a == null || a.isNull()) {
            return null;
        }
        if (!a.isObject()) {
            throw ValidationException.forField("query.area", "must be an object");
        }
        if (a.hasNonNull("bbox")) {
            return BboxParser.parse(a.get("bbox").asString());
        }
        Geometry g = null;
        if (a.hasNonNull("geometry")) {
            try {
                g = geoReader.read(a.get("geometry"));
            } catch (InvalidInputException e) {
                throw ValidationException.forField("query.area.geometry", e.getMessage());
            }
        } else if (a.hasNonNull("placeId")) {
            String placeId = a.get("placeId").asString();
            g = places.find(placeId).map(p -> p.area() != null ? (Geometry) p.area() : p.geometry())
                    .orElseThrow(() -> ValidationException.forField("query.area.placeId", "names no place"));
        }
        if (g == null || g.isEmpty()) {
            return null;
        }
        return new BboxFilter(List.of(g.getEnvelopeInternal()));
    }

    private static List<String> strings(JsonNode filters, String field) {
        if (filters == null || !filters.isObject()) {
            return List.of();
        }
        return CollabInputs.strings(filters, field, 100);
    }
}
