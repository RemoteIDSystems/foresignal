package ai.foresignal.pol;

import ai.foresignal.entity.EntityLinkEntity;
import ai.foresignal.entity.EntityLinkRepository;
import ai.foresignal.kinematics.GeoMath;
import ai.foresignal.pol.StayPointExtractor.Candidate;
import ai.foresignal.pol.StayPointExtractor.Fix;
import ai.foresignal.pol.StayPointExtractor.Stay;
import ai.foresignal.security.Ulids;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.PgArrays;
import ai.foresignal.watch.Identity;
import ai.foresignal.watch.IdentityResolver;
import ai.foresignal.watch.WatchlistSnapshot;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The pattern-of-life analysis of opted-in subjects (prompt 114, task 1), driven by the {@code pattern-of-life}
 * derived sources ({@link PolGenerator}).
 *
 * <p>Per subject and tick: the subject's member records are resolved (a record, a 098 entity's open links, or a
 * 036 watchlist's matching records); new persisted positions are read from {@code record_version} after the
 * subject's {@code last_seq} cursor (both tiers, downsampled ranges included, since dwell uses observed-time gaps);
 * stays are extracted incrementally ({@link StayPointExtractor}) and stored in {@code pol_stay}; a new stay is
 * assigned to the nearest place within {@code max(radius, eps)}, which updates the place's statistics, or stays
 * unclustered; {@code NEW_PLACE} and {@code UNUSUAL_TIME} deviations are emitted. Every
 * {@link PolDeviations#ABSENT_TICK} the {@code EXPECTED_ABSENT} rule runs, health-gated on the member sources.
 *
 * <p>Nightly at 03:00 operator time ({@code server.timezone}), or when requested, the subject is reclustered with
 * {@link Dbscan} over the trailing {@code lookback} of stored stays (never raw versions). New clusters keep the id
 * and label of the existing place whose centroid is within {@code eps}; a place that dissolved while visited inside
 * the lookback is removed (its record tombstoned); an older place is kept, so places outlive both the TRACK window
 * and the lookback until {@code pol.retention} removes them.
 *
 * <p>Privacy: a subject with a member on a private source (039) is private. Its stays and places are stored at the
 * source's precision cap ({@code privatePrecisionDecimals}) and its records are published on
 * {@code pattern-of-life-private}, never on the public source.
 */
@Component
public class PolEngine {

    private static final Logger log = LoggerFactory.getLogger(PolEngine.class);

    /** The most member records a subject resolves to. */
    public static final int MAX_MEMBERS = 200;

    /** The most fixes read per member and pass. */
    static final int MAX_FIXES = 20_000;

    /** The local time of the nightly recompute. */
    static final LocalTime NIGHTLY = LocalTime.of(3, 0);

    static final GeometryFactory FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private final PolSubjectRepository subjects;
    private final PolStayRepository stays;
    private final PolPlaceRepository places;
    private final JdbcTemplate jdbc;
    private final SourceRegistry registry;
    private final SourceVisibility visibility;
    private final EntityLinkRepository links;
    private final WatchlistSnapshot watchlists;
    private final IdentityResolver identities;
    private final AppSettingService settings;
    private final TransactionTemplate tx;
    private final Clock clock;

    /** Creates the engine. */
    public PolEngine(PolSubjectRepository subjects, PolStayRepository stays, PolPlaceRepository places, JdbcTemplate jdbc,
            SourceRegistry registry, SourceVisibility visibility, EntityLinkRepository links,
            WatchlistSnapshot watchlists, IdentityResolver identities, AppSettingService settings,
            PlatformTransactionManager transactions, Clock clock) {
        this.subjects = subjects;
        this.stays = stays;
        this.places = places;
        this.jdbc = jdbc;
        this.registry = registry;
        this.visibility = visibility;
        this.links = links;
        this.watchlists = watchlists;
        this.identities = identities;
        this.settings = settings;
        this.tx = new TransactionTemplate(transactions);
        this.clock = clock;
    }

    /** Where the engine publishes its records ({@link PolGenerator} over a derived context). */
    public interface Output {

        /** A place was created or changed. */
        void place(PolSubjectEntity subject, PolPlaceEntity place);

        /** A place was removed. */
        void placeRemoved(String subjectId, String placeId);

        /**
         * A deviation.
         *
         * @param subject the subject
         * @param key the deviation's unique key within the subject
         * @param payload the {@code PolDeviation} payload
         * @param point where it happened
         * @param observedAt when it became known
         */
        void deviation(PolSubjectEntity subject, String key, Map<String, Object> payload, Point point,
                Instant observedAt);
    }

    /**
     * A member record.
     *
     * @param sourceId the source
     * @param externalId the entity
     */
    public record Member(String sourceId, String externalId) {

        /** {@code {sourceId}/{externalId}}. */
        public String ref() {
            return sourceId + "/" + externalId;
        }
    }

    // ------------------------------------------------------------------------- members and privacy

    /** The member records of {@code subject}, at most {@link #MAX_MEMBERS}. */
    public List<Member> members(PolSubjectEntity subject) {
        return members(subject.getSubjectType(), subject.getRef());
    }

    /** The member records of a subject type and reference, at most {@link #MAX_MEMBERS}. */
    public List<Member> members(String type, String ref) {
        Set<Member> out = new LinkedHashSet<>();
        switch (type) {
            case "record" -> {
                int slash = ref.indexOf('/');
                if (slash > 0 && slash < ref.length() - 1) {
                    out.add(new Member(ref.substring(0, slash), ref.substring(slash + 1)));
                }
            }
            case "entity" -> {
                for (EntityLinkEntity link : links.findOpenByEntityId(ref)) {
                    out.add(new Member(link.getSourceId(), link.getExternalId()));
                }
            }
            case "watchlist" -> out.addAll(watchlistMembers(ref));
            default -> {
                // Unknown types resolve to nothing.
            }
        }
        return out.stream().limit(MAX_MEMBERS).toList();
    }

    /** The records whose identity field holds a value of the watchlist (entity items expand to open links). */
    List<Member> watchlistMembers(String listId) {
        UUID id;
        try {
            id = UUID.fromString(listId);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
        Optional<WatchlistSnapshot.ListSnapshot> list = watchlists.list(id);
        if (list.isEmpty() || list.get().values().isEmpty()) {
            return List.of();
        }
        List<Member> out = new ArrayList<>();
        WatchlistSnapshot.ListSnapshot snapshot = list.get();
        if (snapshot.identity() == Identity.ENTITY) {
            for (String value : snapshot.values()) {
                String entityId = value.startsWith("entity:") ? value.substring("entity:".length()) : value;
                for (EntityLinkEntity link : links.findOpenByEntityId(entityId)) {
                    out.add(new Member(link.getSourceId(), link.getExternalId()));
                }
            }
            return out;
        }
        List<String> values = new ArrayList<>(snapshot.values());
        for (Map.Entry<String, String> entry : identities.paths(snapshot.identity(), List.of()).entrySet()) {
            if (out.size() >= MAX_MEMBERS) {
                break;
            }
            String steps = PgArrays.text(List.of(entry.getValue().split("\\.")));
            String sql = "SELECT external_id FROM record_head WHERE source_id = ? AND NOT tombstone AND "
                    + snapshot.normalize().sql("(payload #>> CAST(? AS text[]))") + " = ANY (CAST(? AS text[]))"
                    + " ORDER BY external_id LIMIT ?";
            for (String externalId : jdbc.queryForList(sql, String.class, entry.getKey(), steps, PgArrays.text(values),
                    MAX_MEMBERS)) {
                out.add(new Member(entry.getKey(), externalId));
            }
        }
        return out;
    }

    /**
     * The privacy of a member set.
     *
     * @param privateSubject true when a member source is private
     * @param decimals the coordinate decimals kept (the smallest cap of the private sources; 7 when public)
     */
    public record Privacy(boolean privateSubject, int decimals) {}

    /** The privacy of {@code members}. */
    public Privacy privacy(List<Member> members) {
        boolean priv = false;
        int decimals = 7;
        for (Member m : members) {
            if (visibility.isPrivate(m.sourceId())) {
                priv = true;
                int cap = registry.find(m.sourceId()).map(r -> r.descriptor().privatePrecisionDecimals())
                        .orElse(SourceDescriptor.DEFAULT_PRIVATE_PRECISION_DECIMALS);
                decimals = Math.min(decimals, cap);
            }
        }
        return new Privacy(priv, priv ? decimals : 7);
    }

    /** The operator's timezone ({@code server.timezone}, UTC by default). */
    public ZoneId zone() {
        String tz = settings.getOrDefault(SettingKeys.SERVER, ServerSettings.class, ServerSettings::defaults).timezone();
        try {
            return tz == null || tz.isBlank() ? ZoneOffset.UTC : ZoneId.of(tz);
        } catch (DateTimeException e) {
            return ZoneOffset.UTC;
        }
    }

    // ------------------------------------------------------------------------- ticks

    /**
     * One tick for the subjects of one privacy class: each enabled subject is processed in a transaction of its
     * own; a failing subject is logged and skipped.
     *
     * @param privateClass true for the private subjects
     * @param out where records go
     * @param health the health of a source
     */
    public void tick(boolean privateClass, Output out, Function<String, HealthStatus> health) {
        Instant now = clock.instant();
        ZoneId zone = zone();
        for (PolSubjectEntity subject : subjects.findByEnabledTrueAndPrivateSubject(privateClass)) {
            try {
                tx.executeWithoutResult(status -> process(subject.getId(), out, health, now, zone));
            } catch (RuntimeException e) {
                log.warn("Pattern-of-life subject {} failed and is skipped this tick: {}", subject.getId(), e.getMessage());
            }
        }
    }

    /** Processes one subject (inside a transaction). */
    void process(String subjectId, Output out, Function<String, HealthStatus> health, Instant now, ZoneId zone) {
        PolSubjectEntity subject = subjects.findById(subjectId).orElse(null);
        if (subject == null) {
            return;
        }
        List<Member> members = members(subject);
        Privacy privacy = privacy(members);
        if (privacy.privateSubject() != subject.isPrivateSubject()
                || privacy.decimals() != subject.getPrecisionDecimals()) {
            boolean moved = privacy.privateSubject() != subject.isPrivateSubject();
            subject.setPrivateSubject(privacy.privateSubject());
            subject.setPrecisionDecimals(privacy.decimals());
            subjects.save(subject);
            if (moved) {
                // The other generator publishes it from now on; this one tombstones its records.
                return;
            }
        }
        PolParams params = PolParams.of(subject.getParams());
        Map<String, Object> state = new LinkedHashMap<>(subject.getState() == null ? Map.of() : subject.getState());
        Candidate open = Candidate.fromMap(state.get("open"));
        Instant floor = subject.getLastSeq() == null ? now.minus(params.lookback()) : null;
        long after = subject.getLastSeq() == null ? 0 : subject.getLastSeq();
        long[] maxSeq = {after};
        List<Fix> fixes = readFixes(members, after, floor, maxSeq);
        StayPointExtractor.Result result = StayPointExtractor.extract(open, fixes, params);
        List<Stay> closed = new ArrayList<>(result.stays());
        StayPointExtractor.Result flushed = StayPointExtractor.flush(result.open(), now, params);
        closed.addAll(flushed.stays());
        open = flushed.open();

        List<PolPlaceEntity> known = places.findBySubjectIdOrderByIdAsc(subject.getId());
        Instant historyStart = historyStart(subject, state);
        for (Stay stay : closed) {
            historyStart = store(subject, params, zone, stay, known, historyStart, out);
        }
        if (historyStart != null) {
            state.put("historyStart", historyStart.toString());
        }
        if (open == null) {
            state.remove("open");
        } else {
            state.put("open", open.toMap());
        }
        subject.setLastSeq(maxSeq[0]);
        if (subject.isRecomputeRequested() || recomputeDue(subject.getLastComputedAt(), now, zone)) {
            recompute(subject, params, zone, now, out);
            subject.setRecomputeRequested(false);
            subject.setLastComputedAt(now);
        }
        expectedAbsent(subject, params, zone, now, members, open, state, health, out);
        subject.setState(state);
        subjects.save(subject);
    }

    /** The subject's history start: the stored one, else its earliest stay. */
    private Instant historyStart(PolSubjectEntity subject, Map<String, Object> state) {
        Object stored = state.get("historyStart");
        if (stored != null) {
            try {
                return Instant.parse(String.valueOf(stored));
            } catch (DateTimeException e) {
                // Recomputed below.
            }
        }
        return stays.findFirstBySubjectIdOrderByArriveAsc(subject.getId()).map(PolStayEntity::getArrive).orElse(null);
    }

    /** Stores a closed stay, assigns it, and emits its deviations; returns the (possibly new) history start. */
    private Instant store(PolSubjectEntity subject, PolParams params, ZoneId zone, Stay stay, List<PolPlaceEntity> known,
            Instant historyStart, Output out) {
        Point centroid = point(subject, stay.lon(), stay.lat());
        PolStayEntity entity = new PolStayEntity(Ulids.next(clock.instant()));
        entity.setSubjectId(subject.getId());
        entity.setArrive(stay.arrive());
        entity.setDepart(stay.depart());
        entity.setCentroid(centroid);
        entity.setFixes(stay.fixes());
        entity.setRecordRef(stay.recordRef());
        PolPlaceEntity place = nearest(known, stay.lon(), stay.lat(), params.eps());
        int bucket = PolStats.bucket(stay.arrive(), zone);
        if (place != null) {
            Double share = PolDeviations.unusualTimeShare(place.getHourOfWeek(), bucket);
            entity.setPlaceId(place.getId());
            entity.setClusterId(place.getClusterId());
            int[] histogram = place.getHourOfWeek().clone();
            PolStats.addDwell(histogram, stay.arrive(), stay.depart(), zone);
            place.setHourOfWeek(histogram);
            place.setVisits(place.getVisits() + 1);
            place.setTotalDwellMinutes(place.getTotalDwellMinutes() + stay.dwell().toSeconds() / 60.0);
            if (stay.depart().isAfter(place.getLastSeen())) {
                place.setLastSeen(stay.depart());
            }
            if (stay.arrive().isBefore(place.getFirstSeen())) {
                place.setFirstSeen(stay.arrive());
            }
            places.save(place);
            out.place(subject, place);
            if (share != null) {
                out.deviation(subject, PolDeviations.UNUSUAL_TIME + ":" + stay.arrive().getEpochSecond(),
                        deviation(subject, PolDeviations.UNUSUAL_TIME, stay.arrive(), place, stay, centroid, bucket,
                                share, null), centroid, stay.depart());
            }
        } else if (PolDeviations.isNewPlace(stay.arrive(), historyStart)) {
            out.deviation(subject, PolDeviations.NEW_PLACE + ":" + stay.arrive().getEpochSecond(),
                    deviation(subject, PolDeviations.NEW_PLACE, stay.arrive(), null, stay, centroid, bucket, null, null),
                    centroid, stay.depart());
        }
        stays.save(entity);
        return historyStart == null || stay.arrive().isBefore(historyStart) ? stay.arrive() : historyStart;
    }

    /** The place nearest to a position within {@code max(radius, eps)}, or null. */
    static PolPlaceEntity nearest(List<PolPlaceEntity> known, double lon, double lat, double eps) {
        PolPlaceEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (PolPlaceEntity place : known) {
            double d = GeoMath.distanceMeters(lon, lat, place.getCentroid().getX(), place.getCentroid().getY());
            if (d <= Math.max(place.getRadiusM(), eps) && d < bestDistance) {
                best = place;
                bestDistance = d;
            }
        }
        return best;
    }

    /** True when the nightly recompute is due: never computed, or not since the latest 03:00 local. */
    static boolean recomputeDue(Instant lastComputedAt, Instant now, ZoneId zone) {
        if (lastComputedAt == null) {
            return true;
        }
        ZonedDateTime local = now.atZone(zone);
        ZonedDateTime nightly = local.toLocalDate().atTime(NIGHTLY).atZone(zone);
        if (nightly.isAfter(local)) {
            nightly = nightly.minusDays(1);
        }
        return lastComputedAt.isBefore(nightly.toInstant());
    }

    /** Reads the new fixes of every member, merged in observed-time order; {@code maxSeq[0]} gets the largest seq. */
    List<Fix> readFixes(List<Member> members, long afterSeq, Instant floor, long[] maxSeq) {
        List<Fix> fixes = new ArrayList<>();
        for (Member m : members) {
            String sql = "SELECT seq, observed_at, ST_X(p) AS lon, ST_Y(p) AS lat FROM (SELECT seq, observed_at,"
                    + " ST_PointOnSurface(geom) AS p FROM record_version WHERE source_id = ? AND external_id = ?"
                    + " AND seq > ? AND NOT tombstone AND geom IS NOT NULL"
                    + (floor == null ? "" : " AND observed_at >= ?") + " ORDER BY seq LIMIT " + MAX_FIXES + ") x";
            Object[] args = floor == null ? new Object[] {m.sourceId(), m.externalId(), afterSeq}
                    : new Object[] {m.sourceId(), m.externalId(), afterSeq, OffsetDateTime.ofInstant(floor, ZoneOffset.UTC)};
            String ref = m.ref();
            jdbc.query(sql, rs -> {
                long seq = rs.getLong(1);
                maxSeq[0] = Math.max(maxSeq[0], seq);
                fixes.add(new Fix(rs.getObject(2, OffsetDateTime.class).toInstant(), rs.getDouble(3), rs.getDouble(4),
                        ref, seq));
            }, args);
        }
        fixes.sort(Comparator.comparing(Fix::at).thenComparingLong(Fix::seq));
        return fixes;
    }

    // ------------------------------------------------------------------------- recompute

    /**
     * Reclusters {@code subject} over the trailing lookback of stored stays (DBSCAN) and reconciles its places.
     * Runs inside the caller's transaction.
     */
    void recompute(PolSubjectEntity subject, PolParams params, ZoneId zone, Instant now, Output out) {
        Instant from = now.minus(params.lookback());
        List<PolStayEntity> recent = stays.findBySubjectIdAndArriveGreaterThanEqualOrderByArriveAscIdAsc(
                subject.getId(), from);
        List<PolPlaceEntity> existing = new ArrayList<>(places.findBySubjectIdOrderByIdAsc(subject.getId()));
        List<double[]> lonLat = recent.stream()
                .map(s -> new double[] {s.getCentroid().getX(), s.getCentroid().getY()}).toList();
        LocalFrame frame = LocalFrame.around(lonLat);
        double[][] xy = new double[recent.size()][];
        for (int i = 0; i < recent.size(); i++) {
            xy[i] = frame.toXy(lonLat.get(i)[0], lonLat.get(i)[1]);
        }
        int[] labels = Dbscan.cluster(xy, params.eps(), params.minPts());
        Map<Integer, List<Integer>> groups = new TreeMap<>();
        for (int i = 0; i < labels.length; i++) {
            if (labels[i] != Dbscan.NOISE) {
                groups.computeIfAbsent(labels[i], k -> new ArrayList<>()).add(i);
            }
        }
        Set<String> matched = new HashSet<>();
        for (Map.Entry<Integer, List<Integer>> group : groups.entrySet()) {
            List<Stay> members = new ArrayList<>();
            for (int i : group.getValue()) {
                members.add(toStay(recent.get(i)));
            }
            PolStats.PlaceStats stats = PolStats.place(members, frame, zone);
            PolPlaceEntity place = match(existing, matched, stats.lon(), stats.lat(), params.eps());
            if (place == null) {
                place = new PolPlaceEntity(Ulids.next(now));
                place.setSubjectId(subject.getId());
            }
            matched.add(place.getId());
            place.setClusterId(group.getKey());
            place.setCentroid(point(subject, stats.lon(), stats.lat()));
            place.setRadiusM(Math.round(stats.radiusM() * 10) / 10.0);
            place.setVisits(stats.visits());
            place.setTotalDwellMinutes(Math.round(stats.totalDwellMinutes() * 10) / 10.0);
            place.setHourOfWeek(stats.hourOfWeek());
            place.setFirstSeen(stats.firstSeen());
            place.setLastSeen(stats.lastSeen());
            places.save(place);
            out.place(subject, place);
            for (int i : group.getValue()) {
                recent.get(i).setPlaceId(place.getId());
                recent.get(i).setClusterId(group.getKey());
            }
        }
        for (int i = 0; i < labels.length; i++) {
            if (labels[i] == Dbscan.NOISE) {
                recent.get(i).setPlaceId(null);
                recent.get(i).setClusterId(null);
            }
        }
        stays.saveAll(recent);
        for (PolPlaceEntity place : existing) {
            if (!matched.contains(place.getId()) && !place.getLastSeen().isBefore(from)) {
                // The place dissolved while it was being visited: remove it.
                jdbc.update("UPDATE pol_stay SET place_id = NULL, cluster_id = NULL WHERE place_id = ?", place.getId());
                places.delete(place);
                out.placeRemoved(subject.getId(), place.getId());
            }
        }
    }

    /** The unmatched existing place nearest to {@code (lon, lat)} within {@code eps}, or null. */
    private static PolPlaceEntity match(List<PolPlaceEntity> existing, Set<String> matched, double lon, double lat,
            double eps) {
        PolPlaceEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (PolPlaceEntity place : existing) {
            if (matched.contains(place.getId())) {
                continue;
            }
            double d = GeoMath.distanceMeters(lon, lat, place.getCentroid().getX(), place.getCentroid().getY());
            if (d <= eps && d < bestDistance) {
                best = place;
                bestDistance = d;
            }
        }
        return best;
    }

    static Stay toStay(PolStayEntity e) {
        return new Stay(e.getArrive(), e.getDepart(), e.getCentroid().getX(), e.getCentroid().getY(), e.getFixes(),
                e.getRecordRef());
    }

    // ------------------------------------------------------------------------- expected absence

    /** Runs {@code EXPECTED_ABSENT} when its 10-minute tick is due and every member source is healthy. */
    private void expectedAbsent(PolSubjectEntity subject, PolParams params, ZoneId zone, Instant now,
            List<Member> members, Candidate open, Map<String, Object> state, Function<String, HealthStatus> health,
            Output out) {
        Instant checked = null;
        try {
            checked = state.get("absentCheckedAt") == null ? null : Instant.parse(String.valueOf(state.get("absentCheckedAt")));
        } catch (DateTimeException e) {
            // Run now.
        }
        if (!PolDeviations.absentCheckDue(checked, now)) {
            return;
        }
        state.put("absentCheckedAt", now.toString());
        Set<HealthStatus> statuses = new HashSet<>();
        for (Member m : members) {
            statuses.add(health.apply(m.sourceId()));
        }
        if (!PolDeviations.healthy(statuses)) {
            return;
        }
        List<PolPlaceEntity> known = places.findBySubjectIdOrderByIdAsc(subject.getId());
        if (known.isEmpty()) {
            return;
        }
        int bucket = PolStats.bucket(now, zone);
        PolDeviations.Expected expected =
                PolDeviations.expectedPlace(known.stream().map(PolPlaceEntity::getHourOfWeek).toList(), bucket);
        if (expected == null) {
            return;
        }
        PolPlaceEntity place = known.get(expected.index());
        double reach = Math.max(place.getRadiusM(), params.eps());
        Fix latest = latestFix(members);
        boolean atPlace = latest != null && GeoMath.distanceMeters(latest.lon(), latest.lat(),
                place.getCentroid().getX(), place.getCentroid().getY()) <= reach;
        Instant lastAtPlace = place.getLastSeen();
        if (open != null && GeoMath.distanceMeters(open.anchorLon(), open.anchorLat(), place.getCentroid().getX(),
                place.getCentroid().getY()) <= reach && open.lastAt().isAfter(lastAtPlace)) {
            lastAtPlace = open.lastAt();
        }
        if (atPlace && latest.at().isAfter(lastAtPlace)) {
            lastAtPlace = latest.at();
        }
        if (!PolDeviations.absent(now, atPlace, latest == null ? null : latest.at(), lastAtPlace)) {
            return;
        }
        String key = PolDeviations.EXPECTED_ABSENT + ":" + place.getId() + ":"
                + PolStats.bucketStart(now, zone).getEpochSecond();
        if (key.equals(state.get("lastAbsentKey"))) {
            return;
        }
        state.put("lastAbsentKey", key);
        Map<String, Object> payload = deviation(subject, PolDeviations.EXPECTED_ABSENT, now, place, null, null, bucket,
                expected.share(), latest == null ? null : latest.at());
        out.deviation(subject, key, payload, place.getCentroid(), now);
    }

    /** The latest live position of any member, or null. */
    Fix latestFix(List<Member> members) {
        Fix best = null;
        for (Member m : members) {
            List<Fix> rows = jdbc.query("""
                    SELECT l.observed_at, ST_X(ST_PointOnSurface(l.geom)), ST_Y(ST_PointOnSurface(l.geom))
                      FROM record_live l JOIN record_head h USING (source_id, external_id)
                     WHERE l.source_id = ? AND l.external_id = ? AND NOT h.tombstone AND l.geom IS NOT NULL""",
                    (rs, n) -> new Fix(rs.getObject(1, OffsetDateTime.class).toInstant(), rs.getDouble(2),
                            rs.getDouble(3), m.ref(), 0), m.sourceId(), m.externalId());
            for (Fix f : rows) {
                if (best == null || f.at().isAfter(best.at())) {
                    best = f;
                }
            }
        }
        return best;
    }

    // ------------------------------------------------------------------------- helpers

    /** A point at the subject's precision (rounded for a private subject). */
    Point point(PolSubjectEntity subject, double lon, double lat) {
        double x = subject.isPrivateSubject() ? PolStats.round(lon, subject.getPrecisionDecimals()) : lon;
        double y = subject.isPrivateSubject() ? PolStats.round(lat, subject.getPrecisionDecimals()) : lat;
        Point p = FACTORY.createPoint(new Coordinate(x, y));
        p.setSRID(4326);
        return p;
    }

    /** A {@code PolDeviation} payload. */
    static Map<String, Object> deviation(PolSubjectEntity subject, String type, Instant at, PolPlaceEntity place,
            Stay stay, Point centroid, int bucket, Double share, Instant lastSeenAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("subjectId", subject.getId());
        payload.put("subjectRef", subject.getSubjectType() + ":" + subject.getRef());
        payload.put("deviationType", type);
        payload.put("at", at.toString());
        payload.put("placeId", place == null ? null : place.getId());
        payload.put("placeLabel", place == null ? null : place.getLabel());
        if (stay != null && centroid != null) {
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("arrive", stay.arrive().toString());
            s.put("depart", stay.depart().toString());
            s.put("centroid", Map.of("type", "Point", "coordinates", List.of(centroid.getX(), centroid.getY())));
            payload.put("stay", s);
        }
        payload.put("hourOfWeek", bucket);
        payload.put("bucketShare", share == null ? null : Math.round(share * 10_000) / 10_000.0);
        payload.put("lastSeenAt", lastSeenAt == null ? null : lastSeenAt.toString());
        payload.put("title", title(type, place));
        payload.put("private", subject.isPrivateSubject());
        payload.put("automated", true);
        return payload;
    }

    private static String title(String type, PolPlaceEntity place) {
        String where = place == null ? "" : place.getLabel() != null ? " (" + place.getLabel() + ")" : " (learned place)";
        return switch (type) {
            case PolDeviations.NEW_PLACE -> "Stay at a new place";
            case PolDeviations.UNUSUAL_TIME -> "Stay at an unusual time" + where;
            default -> "Absent from the expected place" + where;
        };
    }
}
