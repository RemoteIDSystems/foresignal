package ai.foresignal.social.narrative;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.object.ObjectEnvelope;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectService;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.social.SocialViews;
import ai.foresignal.social.SocialVocabulary;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The API and MCP views of narratives, claims and media fingerprints (prompt 136, OpenAPI {@code NarrativeSummary},
 * {@code Narrative}, {@code NarrativePost}, {@code PropagationGraph}, {@code NarrativeTimeline}, {@code Claim},
 * {@code MediaFingerprint}, {@code MediaMatchResult}). Claims are drawn as reports: their credibility follows their
 * 137 status at read time.
 */
@Component
public class NarrativeViews {

    /** The default page size. */
    public static final int DEFAULT_LIMIT = 100;

    /** The largest page size. */
    public static final int MAX_LIMIT = 500;

    /** The latest uses shown on a fingerprint. */
    static final int USES = 100;

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final NarrativeStore store;
    private final NarrativeService service;
    private final MediaFingerprinter media;
    private final ObjectProvider<ObjectService> objects;
    private final JsonMapper json;

    /** Creates the views. */
    public NarrativeViews(NarrativeStore store, NarrativeService service, MediaFingerprinter media,
            ObjectProvider<ObjectService> objects, JsonMapper json) {
        this.store = store;
        this.service = service;
        this.media = media;
        this.objects = objects;
        this.json = json;
    }

    /**
     * A page of results.
     *
     * @param items the items
     * @param nextCursor the next cursor, or null
     */
    public record Page(List<Map<String, Object>> items, String nextCursor) {

        /** The page document. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("items", items);
            out.put("nextCursor", nextCursor);
            return out;
        }
    }

    // ------------------------------------------------------------------------- static views

    /** The {@code NarrativeSummary} of a narrative. */
    public static Map<String, Object> summary(NarrativeStore.Narrative n) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", n.id());
        out.put("ref", "narrative:" + n.id());
        out.put("title", n.title());
        out.put("canonicalEventId", n.id());
        out.put("topics", n.topicIds());
        out.put("placeRefs", n.placeRefs());
        out.put("entityRefs", n.entityRefs());
        out.put("regionCodes", n.regionCodes());
        out.put("languages", n.languages());
        out.put("platforms", n.platforms());
        out.put("firstObservedAt", n.firstObservedAt().toString());
        out.put("lastActivityAt", n.lastActivityAt().toString());
        out.put("postCount", n.postCount());
        out.put("uniqueAuthors", n.uniqueAuthors());
        out.put("evidenceGroupCount", n.evidenceGroupCount());
        out.put("originConcentration", n.originConcentration());
        out.put("velocity", n.velocity());
        out.put("mediaFingerprints", n.mediaFingerprints());
        out.put("newsStoryIds", n.newsStoryIds());
        out.put("situationIds", n.situationIds());
        out.put("status", n.status());
        out.put("state", n.state());
        out.put("coordinationIndicatorsPresent", n.coordinationFindingId() != null);
        out.put("mergedInto", n.mergedInto());
        out.put("tlp", n.tlp());
        return out;
    }

    /** The {@code Claim} view of a claim (credibility from its current status). */
    public static Map<String, Object> claim(NarrativeStore.Claim c) {
        int credibility = SocialEvidenceGrades.credibility(c.status());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", c.id());
        out.put("ref", "claim:" + c.id());
        out.put("narrativeId", c.narrativeId());
        out.put("type", c.type());
        out.put("signalFamily", c.signalFamily() == null ? "attention" : c.signalFamily());
        out.put("polarity", c.polarity());
        out.put("hedged", c.hedged());
        out.put("place", c.place());
        out.put("entityRefs", c.entityRefs());
        out.put("eventTime", c.eventTime());
        out.put("actor", c.actor());
        out.put("supportCount", c.supportCount());
        out.put("evidenceGroupCount", c.evidenceGroupCount());
        out.put("firstObservedAt", c.firstObservedAt().toString());
        out.put("extraction", c.extraction());
        out.put("manifestId", c.manifestId());
        out.put("confidence", c.confidence());
        out.put("status", c.status());
        out.put("comparisonId", c.comparisonId());
        out.put("sourceReliability", c.sourceReliability());
        out.put("informationCredibility", credibility);
        out.put("grade", SocialEvidenceGrades.grade(c.sourceReliability(), credibility));
        out.put("tlp", c.tlp());
        out.put("createdAt", c.createdAt().toString());
        return out;
    }

    /** The {@code MediaFingerprint} summary view. */
    public static Map<String, Object> fingerprint(NarrativeStore.Fingerprint f) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", f.id());
        out.put("pdq", f.pdq());
        out.put("quality", f.quality());
        out.put("matchable", f.quality() >= NarrativeVocabulary.PDQ_MIN_QUALITY);
        out.put("firstSeenAt", f.firstSeenAt().toString());
        out.put("firstPostRef", f.firstPostRef());
        out.put("useCount", f.useCount());
        out.put("platforms", f.platforms());
        out.put("narrativeIds", f.narrativeIds());
        out.put("mediaId", f.mediaId());
        out.put("reuseFindingIds", f.reuseFindingIds());
        return out;
    }

    // ------------------------------------------------------------------------- narratives

    /** The narrative or 404 {@code UNKNOWN_NARRATIVE}. */
    public NarrativeStore.Narrative require(String id) {
        return store.narrative(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_NARRATIVE,
                "No narrative '" + id + "'."));
    }

    /**
     * {@code listNarratives}: newest activity first.
     *
     * @param topic an attention topic, or null
     * @param situationId a situation, or null
     * @param place a 036 place id or 040 region code, or null
     * @param statuses statuses, or empty
     * @param states states, or empty
     * @param platform a network, or null
     * @param since lastActivityAt at or after, or null
     * @param cursor the cursor, or null
     * @param limit the page size
     */
    public Page narratives(String topic, String situationId, String place, List<String> statuses, List<String> states,
            String platform, Instant since, String cursor, int limit) {
        List<String> where = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        if (topic != null) {
            where.add("topic_ids @> ARRAY[?]::text[]");
            args.add(topic);
        }
        if (situationId != null) {
            where.add("situation_ids @> ARRAY[?]::text[]");
            args.add(situationId);
        }
        if (place != null) {
            if (place.matches("^[A-Z]{2}(-[A-Z0-9]{1,3})?$")) {
                where.add("region_codes @> ARRAY[?]::text[]");
                args.add(place);
            } else {
                where.add("place_refs @> ARRAY[?]::text[]");
                args.add(place.startsWith("place:") ? place : "place:" + place);
            }
        }
        if (!statuses.isEmpty()) {
            where.add("status = ANY(?)");
            args.add(statuses.toArray(String[]::new));
        }
        if (!states.isEmpty()) {
            where.add("state = ANY(?)");
            args.add(states.toArray(String[]::new));
        }
        if (platform != null) {
            where.add("platforms @> CAST(? AS jsonb)");
            args.add(json.writeValueAsString(List.of(Map.of("platform", platform))));
        }
        if (since != null) {
            where.add("last_activity_at >= ?");
            args.add(Timestamp.from(since));
        }
        Object[] after = decode(cursor);
        List<NarrativeStore.Narrative> rows = store.page(String.join(" AND ", where), args, after, limit);
        List<Map<String, Object>> items = new ArrayList<>();
        String next = null;
        for (int i = 0; i < Math.min(limit, rows.size()); i++) {
            items.add(summary(rows.get(i)));
        }
        if (rows.size() > limit) {
            NarrativeStore.Narrative last = rows.get(limit - 1);
            next = encode(last.lastActivityAt(), last.id());
        }
        return new Page(items, next);
    }

    /**
     * {@code getNarrative}: the narrative with its envelope, claims, media, coordination, linked stories and situations
     * and history.
     *
     * @param context the caller, or null
     */
    public Map<String, Object> detail(String id, ResolveContext context) {
        NarrativeStore.Narrative n = require(id);
        Map<String, Object> out = summary(n);
        ObjectService objectService = objects.getIfAvailable();
        out.put("envelope", envelope(objectService, "narrative:" + n.id(), context).orElse(null));
        List<Map<String, Object>> claims = new ArrayList<>();
        for (NarrativeStore.Claim c : store.claims(n.id())) {
            claims.add(claim(c));
        }
        out.put("claims", claims);
        List<Map<String, Object>> fingerprints = new ArrayList<>();
        for (NarrativeStore.Fingerprint f : store.fingerprints(n.mediaFingerprints()).values()) {
            fingerprints.add(fingerprint(f));
        }
        out.put("media", fingerprints);
        out.put("coordination", n.coordination());
        List<ObjectEnvelope> stories = new ArrayList<>();
        for (String storyId : n.newsStoryIds()) {
            envelope(objectService, "news-story:" + storyId, context).ifPresent(stories::add);
        }
        out.put("newsStories", stories);
        List<ObjectEnvelope> situations = new ArrayList<>();
        for (String situationId : n.situationIds()) {
            envelope(objectService, "situation:" + situationId, context).ifPresent(situations::add);
        }
        out.put("situations", situations);
        out.put("comparisonIds", List.of());
        out.put("history", store.history(n.id()));
        out.put("clusterer", NarrativeService.clustererRef());
        out.put("manifestId", n.manifestId());
        out.put("caveats", List.of(NarrativeService.reportingCaveat()));
        return out;
    }

    private static Optional<ObjectEnvelope> envelope(ObjectService service, String ref, ResolveContext context) {
        if (service == null || context == null) {
            return Optional.empty();
        }
        try {
            return service.find(ObjectRef.parse(ref), context);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * {@code listNarrativePosts}: member posts newest first.
     *
     * @param filter the caller's source restriction
     */
    public Page posts(String id, String platform, String evidenceGroupId, String cursor, int limit,
            SocialViews.PostFilter filter) {
        require(id);
        if (filter != null && filter.none()) {
            return new Page(List.of(), null);
        }
        List<NarrativeStore.Member> rows = store.memberPage(id, platform, evidenceGroupId, decode(cursor), limit);
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, rows.size()); i++) {
            NarrativeStore.Member m = rows.get(i);
            if (filter != null && (filter.excluded().contains(m.sourceId())
                    || (filter.allowed() != null && !filter.allowed().contains(m.sourceId())))) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("post", postOf(m));
            item.put("linkReason", m.linkReason());
            item.put("evidenceGroupId", m.evidenceGroupId() == null ? "" : m.evidenceGroupId());
            item.put("joinedAt", m.joinedAt().toString());
            item.put("centrality", m.centrality());
            items.add(item);
        }
        String next = null;
        if (rows.size() > limit) {
            NarrativeStore.Member last = rows.get(limit - 1);
            next = encode(last.createdAt(), last.id());
        }
        return new Page(items, next);
    }

    /** The {@code SocialPost} of a member: its stored head, else a stub without text (expired or deleted). */
    Map<String, Object> postOf(NarrativeStore.Member m) {
        List<Map<String, Object>> rows = store.jdbc().query("SELECT seq, version, payload::text AS payload FROM"
                + " record_head WHERE source_id = ? AND external_id = ? AND NOT tombstone", (rs, i) -> {
                    Map<String, Object> view = SocialViews.postView(m.sourceId(), m.externalId(), rs.getLong("seq"),
                            rs.getInt("version"), json.readValue(rs.getString("payload"), MAP));
                    return view;
                }, m.sourceId(), m.externalId());
        Map<String, Object> view;
        if (!rows.isEmpty()) {
            view = rows.get(0);
        } else {
            view = new LinkedHashMap<>();
            view.put("platform", m.platform());
            int colon = m.postKey().indexOf(':');
            view.put("postId", colon > 0 ? m.postKey().substring(colon + 1) : m.postKey());
            view.put("url", m.url());
            Map<String, Object> author = new LinkedHashMap<>();
            author.put("handle", "");
            author.put("accountId", "");
            view.put("author", author);
            view.put("authorKey", m.authorKey() == null ? "" : m.authorKey());
            view.put("text", null);
            view.put("createdAt", m.createdAt().toString());
            view.put("observedAt", m.createdAt().toString());
            view.put("links", m.links());
            view.put("media", List.of());
            view.put("collection", SocialVocabulary.COLLECTION_STREAM);
            view.put("topicIds", m.topicIds());
            view.put("deleted", true);
            view.put("ref", m.recordRef());
            view.put("sourceId", m.sourceId());
            view.put("externalId", m.externalId());
            view.put("seq", m.seq());
        }
        view.put("narrativeIds", List.of(m.narrativeId()));
        return view;
    }

    /** {@code getNarrativePropagation}. */
    public Map<String, Object> propagation(String id) {
        NarrativeStore.Narrative n = require(id);
        return service.propagation(n);
    }

    /**
     * {@code getNarrativeTimeline}: posts and unique authors per platform per interval.
     *
     * @param interval {@code PT5M}, {@code PT15M}, {@code PT1H} or {@code P1D}
     */
    public Map<String, Object> timeline(String id, String interval) {
        NarrativeStore.Narrative n = require(id);
        long seconds = Duration.parse(interval).getSeconds();
        Map<String, Map<Instant, long[]>> series = new TreeMap<>();
        store.jdbc().query("SELECT platform, floor(extract(epoch FROM created_at) / ?) * ? AS bucket, count(*) AS posts,"
                + " count(DISTINCT author_key) AS authors FROM social_narrative_member WHERE narrative_id = ? GROUP BY"
                + " platform, bucket ORDER BY platform, bucket", rs -> {
                    Instant t = Instant.ofEpochSecond((long) rs.getDouble("bucket"));
                    series.computeIfAbsent(rs.getString("platform"), k -> new TreeMap<>()).put(t,
                            new long[] {rs.getLong("posts"), rs.getLong("authors")});
                }, seconds, seconds, n.id());
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map.Entry<String, Map<Instant, long[]>> e : series.entrySet()) {
            List<Map<String, Object>> points = new ArrayList<>();
            for (Map.Entry<Instant, long[]> p : e.getValue().entrySet()) {
                Map<String, Object> point = new LinkedHashMap<>();
                point.put("t", p.getKey().toString());
                point.put("posts", p.getValue()[0]);
                point.put("uniqueAuthors", p.getValue()[1]);
                points.add(point);
            }
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("platform", e.getKey());
            s.put("points", points);
            out.add(s);
        }
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("narrativeId", n.id());
        doc.put("interval", interval);
        doc.put("series", out);
        return doc;
    }

    // ------------------------------------------------------------------------- claims

    /**
     * {@code listClaims}: newest first.
     *
     * @param bbox {@code [minLon, minLat, maxLon, maxLat]}, or null
     */
    public Page claims(String narrativeId, String situationId, String type, List<String> statuses, double[] bbox,
            Instant since, String cursor, int limit) {
        List<String> where = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        if (narrativeId != null) {
            where.add("narrative_id = ?");
            args.add(narrativeId);
        }
        if (situationId != null) {
            where.add("narrative_id IN (SELECT id FROM social_narrative WHERE situation_ids @> ARRAY[?]::text[])");
            args.add(situationId);
        }
        if (type != null) {
            where.add("type = ?");
            args.add(type);
        }
        if (!statuses.isEmpty()) {
            where.add("status = ANY(?)");
            args.add(statuses.toArray(String[]::new));
        }
        if (bbox != null) {
            where.add("place_lon BETWEEN ? AND ? AND place_lat BETWEEN ? AND ?");
            args.add(bbox[0]);
            args.add(bbox[2]);
            args.add(bbox[1]);
            args.add(bbox[3]);
        }
        if (since != null) {
            where.add("first_observed_at >= ?");
            args.add(Timestamp.from(since));
        }
        List<NarrativeStore.Claim> rows = store.claimPage(String.join(" AND ", where), args, decode(cursor), limit);
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, rows.size()); i++) {
            items.add(claim(rows.get(i)));
        }
        String next = null;
        if (rows.size() > limit) {
            NarrativeStore.Claim last = rows.get(limit - 1);
            next = encode(last.firstObservedAt(), last.id());
        }
        return new Page(items, next);
    }

    /** {@code getClaim}: 404 {@code UNKNOWN_CLAIM}. */
    public Map<String, Object> claimDetail(String id) {
        return claim(store.claim(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_CLAIM,
                "No claim '" + id + "'.")));
    }

    // ------------------------------------------------------------------------- media

    /**
     * {@code getMediaFingerprint}: 404 {@code UNKNOWN_FINGERPRINT}.
     *
     * @param context the caller, or null
     */
    public Map<String, Object> fingerprintDetail(String id, ResolveContext context) {
        NarrativeStore.Fingerprint f = store.fingerprint(id).orElseThrow(() -> new NotFoundException(
                ErrorCodes.UNKNOWN_FINGERPRINT, "No media fingerprint '" + id + "'."));
        Map<String, Object> out = fingerprint(f);
        out.put("uses", store.uses(f.id(), USES));
        List<ObjectEnvelope> narratives = new ArrayList<>();
        ObjectService service = objects.getIfAvailable();
        for (String narrativeId : f.narrativeIds()) {
            envelope(service, "narrative:" + narrativeId, context).ifPresent(narratives::add);
        }
        out.put("narratives", narratives);
        return out;
    }

    /**
     * {@code matchMedia}: fingerprints within {@code maxDistance} of a hash, with the {@code REFERENCE_WINDOW} caveat.
     *
     * @param query the query hash
     * @param source {@code upload} or {@code pdq}
     * @param withQuality true when the query's quality is known (an upload)
     * @param maxDistance the largest Hamming distance
     * @param limit the most matches
     */
    public Map<String, Object> match(Pdq.Hash query, String source, boolean withQuality, int maxDistance, int limit) {
        List<PdqIndex.Match> matches = media.search(query, maxDistance, limit);
        Map<String, NarrativeStore.Fingerprint> rows = store.fingerprints(matches.stream().map(PdqIndex.Match::id)
                .toList());
        List<Map<String, Object>> items = new ArrayList<>();
        for (PdqIndex.Match m : matches) {
            NarrativeStore.Fingerprint f = rows.get(m.id());
            if (f == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("fingerprint", fingerprint(f));
            item.put("distance", m.distance());
            items.add(item);
        }
        Instant start = store.storeStart();
        Map<String, Object> q = new LinkedHashMap<>();
        q.put("pdq", query.hex());
        q.put("quality", withQuality ? query.quality() : null);
        q.put("source", source);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("query", q);
        out.put("maxDistance", maxDistance);
        out.put("matches", items);
        out.put("referenceWindowStart", start == null ? null : start.toString());
        out.put("caveats", List.of(MediaReuseDetector.referenceWindowCaveat(start)));
        return out;
    }

    // ------------------------------------------------------------------------- cursors

    static String encode(Instant at, String id) {
        String raw = ChronoUnit.MICROS.between(Instant.EPOCH, at) + "|" + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    static Object[] decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            int bar = raw.indexOf('|');
            if (bar <= 0) {
                throw invalidCursor();
            }
            long micros = Long.parseLong(raw.substring(0, bar));
            return new Object[] {Timestamp.from(Instant.EPOCH.plus(micros, ChronoUnit.MICROS)), raw.substring(bar + 1)};
        } catch (IllegalArgumentException e) {
            throw invalidCursor();
        }
    }

    private static ValidationException invalidCursor() {
        return new ValidationException(ErrorCodes.INVALID_CURSOR, "The cursor is not one this endpoint issued.",
                List.of(new FieldError("cursor", "is not a cursor from this endpoint")));
    }
}
