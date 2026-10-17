package ai.foresignal.kinematics;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.derive.DerivedRecords;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.store.query.CurrentQuery;
import ai.foresignal.store.query.GeometryEncoding;
import ai.foresignal.store.query.ListOrder;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.store.query.Slices;
import ai.foresignal.trigger.PlaceLookup;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Kinematic states, CPA and ETA (prompt 114, task 3).
 *
 * <p>A {@link KinematicState} comes from {@code record_head ⋈ record_live} (invariant HIST-2): the live
 * position and {@code observedAt}, speed and course from the payload ({@link KinematicFields}), or, when either
 * is absent, derived from the last two persisted positions at least {@value #MIN_DERIVE_SECONDS} s apart.
 * A state is {@code stale} when older than 2 × its source TTL, and refused ({@code 422 STATE_TOO_OLD}) when
 * older than {@link #MAX_AGE}.
 *
 * <p>API reads go through {@link RecordQueryService} with the caller's excluded sources and restriction, so a
 * record the caller cannot read is "not found"; internal callers (trips, the {@code approach} condition) read
 * every source.
 */
@Service
public class KinematicService {

    /** The oldest state a question is answered for. */
    public static final Duration MAX_AGE = Duration.ofHours(1);

    /** The shortest gap between the two positions speed and course are derived from, seconds. */
    public static final long MIN_DERIVE_SECONDS = 10;

    /** The most candidates a batch evaluates. */
    public static final int MAX_BATCH = 500;

    /** The default number of batch results. */
    public static final int DEFAULT_TOP = 20;

    /** The window of the recent average speed used for route ETAs. */
    public static final Duration AVERAGE_WINDOW = Duration.ofMinutes(10);

    static final Pattern RECORD_REF = Pattern.compile("^([a-z0-9-]+)/(.+)$");

    static final Pattern LON_LAT = Pattern.compile("^\\s*(-?\\d+(?:\\.\\d+)?)\\s*,\\s*(-?\\d+(?:\\.\\d+)?)\\s*$");

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final DerivedRecords records;
    private final RecordQueryService queries;
    private final SourceRegistry registry;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final ObjectProvider<PlaceLookup> places;
    private final Clock clock;

    /** Creates the service. */
    public KinematicService(DerivedRecords records, RecordQueryService queries, SourceRegistry registry,
            JdbcTemplate jdbc, JsonMapper json, ObjectProvider<PlaceLookup> places, Clock clock) {
        this.records = records;
        this.queries = queries;
        this.registry = registry;
        this.jdbc = jdbc;
        this.json = json;
        this.places = places;
        this.clock = clock;
    }

    /**
     * A {@code {sourceId}/{externalId}} reference.
     *
     * @param sourceId the source
     * @param externalId the entity
     */
    public record RecordKey(String sourceId, String externalId) {

        /**
         * Parses {@code value}.
         *
         * @param field the request field, for the error
         * @throws ValidationException when it is not {@code {sourceId}/{externalId}}
         */
        public static RecordKey parse(String value, String field) {
            if (value == null) {
                throw ValidationException.forField(field, "is required as {sourceId}/{externalId}");
            }
            var m = RECORD_REF.matcher(value.strip());
            if (!m.matches()) {
                throw ValidationException.forField(field, "must be {sourceId}/{externalId}");
            }
            return new RecordKey(m.group(1), m.group(2));
        }

        @Override
        public String toString() {
            return sourceId + "/" + externalId;
        }
    }

    /**
     * Parses a CPA horizon: an ISO-8601 duration or shorthand, more than 0 and at most 24 h; the default 2 h when
     * absent.
     *
     * @throws ValidationException when it is not valid
     */
    public static Duration parseHorizon(String value, String field) {
        if (value == null || value.isBlank()) {
            return CpaCalculator.DEFAULT_HORIZON;
        }
        Duration d;
        try {
            d = ai.foresignal.settings.Durations.parse(value.strip());
        } catch (RuntimeException e) {
            throw ValidationException.forField(field, "must be an ISO-8601 duration or shorthand such as 2h");
        }
        if (d.isZero() || d.compareTo(Duration.ofHours(24)) > 0) {
            throw ValidationException.forField(field, "must be more than 0 and at most 24h");
        }
        return d;
    }

    // ------------------------------------------------------------------------- states

    /** The current state of a record for internal callers (every source), or empty without a live position. */
    public Optional<KinematicState> state(String sourceId, String externalId) {
        Optional<HeadSnapshot> head = records.get(sourceId, externalId);
        if (head.isEmpty() || head.get().tombstone() || head.get().geometry() == null) {
            return Optional.empty();
        }
        HeadSnapshot h = head.get();
        return Optional.of(build(sourceId, externalId, h.kind(), h.geometry(), h.observedAt(), h.payload()));
    }

    /**
     * The current state of a record as {@code caller} may read it.
     *
     * @throws NotFoundException when the record does not exist, is excluded or has no position
     */
    public KinematicState apiState(RecordKey key, Set<String> excluded, ReadRestriction restriction) {
        RecordRow row = queries.get(key.sourceId(), key.externalId(), false, FieldSelection.ALL, GeometryOptions.DEFAULT,
                excluded == null ? Set.of() : excluded, restriction == null ? ReadRestriction.NONE : restriction)
                .filter(r -> !r.tombstone() && r.geometry() != null)
                .orElseThrow(() -> NotFoundException.of("Record", key.toString()));
        return fromRow(row);
    }

    /** A state built from a current-record row. */
    KinematicState fromRow(RecordRow row) {
        Geometry geometry = new GeoJsonReader().read(row.geometry());
        Map<String, Object> payload = row.payload() == null ? Map.of() : json.readValue(row.payload(), MAP);
        return build(row.sourceId(), row.externalId(), row.kind(), geometry, row.observedAt(), payload);
    }

    /**
     * A state from a position, time and payload: speed and course from the payload, else derived from
     * the persisted positions; stale when older than 2 × the source TTL.
     */
    KinematicState build(String sourceId, String externalId, String kind, Geometry geometry, Instant observedAt,
            Map<String, ?> payload) {
        Point point = representative(geometry);
        Double speed = KinematicFields.speedMps(payload);
        Double course = KinematicFields.courseDeg(payload);
        boolean derived = false;
        if (speed == null || course == null) {
            double[] d = derive(sourceId, externalId);
            if (d != null) {
                if (speed == null) {
                    speed = d[0];
                }
                if (course == null) {
                    course = d[1];
                }
                derived = true;
            }
        }
        KinematicState state = new KinematicState(sourceId, externalId, kind, point.getX(), point.getY(), observedAt,
                speed, course, KinematicFields.altitudeM(payload), derived, false);
        return state.withStale(stale(state, clock.instant()));
    }

    /** The point of {@code geometry}: itself, or its interior point. */
    public static Point representative(Geometry geometry) {
        return geometry instanceof Point p ? p : geometry.getInteriorPoint();
    }

    /** True when {@code state} is older than 2 × its source TTL at {@code now}; never without a TTL. */
    public boolean stale(KinematicState state, Instant now) {
        Duration ttl = ttl(state.sourceId());
        return ttl != null && state.age(now).compareTo(ttl.multipliedBy(2)) > 0;
    }

    /** The source's record TTL, or null. */
    public Duration ttl(String sourceId) {
        return sourceId == null ? null : registry.find(sourceId).map(r -> r.descriptor().recordTtl()).orElse(null);
    }

    /**
     * Speed (m/s) and course (degrees) from the last two persisted positions at least
     * {@value #MIN_DERIVE_SECONDS} s apart, or null when there are none.
     */
    double[] derive(String sourceId, String externalId) {
        List<Fix> fixes = recentFixes(sourceId, externalId, 20);
        if (fixes.size() < 2) {
            return null;
        }
        Fix latest = fixes.getFirst();
        for (int i = 1; i < fixes.size(); i++) {
            Fix earlier = fixes.get(i);
            long seconds = Duration.between(earlier.at(), latest.at()).toSeconds();
            if (seconds >= MIN_DERIVE_SECONDS) {
                return derive(earlier, latest);
            }
        }
        return null;
    }

    /** Speed and course from {@code a} to {@code b}. */
    static double[] derive(Fix a, Fix b) {
        double seconds = Duration.between(a.at(), b.at()).toMillis() / 1000.0;
        double distance = GeoMath.distanceMeters(a.lon(), a.lat(), b.lon(), b.lat());
        double course = GeoMath.bearingDegrees(a.lon(), a.lat(), b.lon(), b.lat());
        return new double[] {seconds <= 0 ? 0 : distance / seconds, course};
    }

    /**
     * One persisted position.
     *
     * @param at its observed time
     * @param lon the longitude
     * @param lat the latitude
     */
    public record Fix(Instant at, double lon, double lat) {}

    /** The newest persisted positions of a record, newest first. */
    List<Fix> recentFixes(String sourceId, String externalId, int limit) {
        return jdbc.query("""
                SELECT observed_at, ST_X(ST_PointOnSurface(geom)) AS lon, ST_Y(ST_PointOnSurface(geom)) AS lat
                  FROM record_version
                 WHERE source_id = ? AND external_id = ? AND NOT tombstone AND geom IS NOT NULL
                 ORDER BY seq DESC LIMIT ?""",
                (rs, n) -> new Fix(rs.getObject(1, OffsetDateTime.class).toInstant(), rs.getDouble(2), rs.getDouble(3)),
                sourceId, externalId, limit);
    }

    /**
     * The average ground speed of a record over the last {@link #AVERAGE_WINDOW} of persisted positions
     * (prompt 114: ETA along a trip route), or null when fewer than two positions span it.
     */
    public Double recentAverageSpeed(String sourceId, String externalId) {
        List<Fix> fixes = recentFixes(sourceId, externalId, 200);
        if (fixes.size() < 2) {
            return null;
        }
        Instant newest = fixes.getFirst().at();
        Instant cutoff = newest.minus(AVERAGE_WINDOW);
        double meters = 0;
        Fix last = fixes.getFirst();
        Fix oldest = last;
        for (int i = 1; i < fixes.size(); i++) {
            Fix f = fixes.get(i);
            if (f.at().isBefore(cutoff)) {
                break;
            }
            meters += GeoMath.distanceMeters(f.lon(), f.lat(), last.lon(), last.lat());
            last = f;
            oldest = f;
        }
        double seconds = Duration.between(oldest.at(), newest).toMillis() / 1000.0;
        return seconds < MIN_DERIVE_SECONDS ? null : meters / seconds;
    }

    /**
     * Refuses a state older than {@link #MAX_AGE}.
     *
     * @throws ForesignalException {@code 422 STATE_TOO_OLD}
     */
    public void requireFresh(KinematicState state, Instant now) {
        if (state.age(now).compareTo(MAX_AGE) > 0) {
            throw new ForesignalException(HttpStatus.valueOf(422), ErrorCodes.STATE_TOO_OLD,
                    "The state of " + state.sourceId() + "/" + state.externalId() + " was observed at "
                            + state.observedAt() + ", more than 1 h ago.");
        }
    }

    // ------------------------------------------------------------------------- questions

    /**
     * The CPA of two records (OpenAPI {@code CpaResult}).
     *
     * @throws ValidationException {@code 400 CPA_RANGE} when they are more than 500 km apart
     * @throws ForesignalException {@code 422 STATE_TOO_OLD}
     */
    public Map<String, Object> cpa(RecordKey a, RecordKey b, Duration horizon, Set<String> excluded,
            ReadRestriction restriction) {
        Instant now = clock.instant();
        KinematicState sa = apiState(a, excluded, restriction);
        KinematicState sb = apiState(b, excluded, restriction);
        requireFresh(sa, now);
        requireFresh(sb, now);
        CpaCalculator.Cpa cpa = CpaCalculator.cpa(sa, sb, horizon);
        if (!cpa.inRange()) {
            throw rangeError(cpa.separationMeters());
        }
        return cpaMap(sa, sb, cpa, horizon, now);
    }

    /** The {@code 400 CPA_RANGE} problem. */
    static ValidationException rangeError(double separation) {
        return new ValidationException(ErrorCodes.CPA_RANGE, "The two records are "
                + Math.round(separation / 1000) + " km apart; CPA is answered up to 500 km.",
                List.of(new FieldError("b", "is more than 500 km from a")));
    }

    /** The OpenAPI {@code CpaResult} of a computed CPA. */
    static Map<String, Object> cpaMap(KinematicState a, KinematicState b, CpaCalculator.Cpa cpa, Duration horizon,
            Instant now) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("a", a.toMap());
        out.put("b", b.toMap());
        out.put("t0", cpa.t0());
        out.put("tcpaSeconds", KinematicState.round(cpa.tcpaSeconds(), 1));
        out.put("cpaAt", cpa.cpaAt());
        out.put("dcpaMeters", KinematicState.round(cpa.dcpaMeters(), 1));
        out.put("verticalSeparationM", cpa.verticalSeparationM() == null ? null
                : KinematicState.round(cpa.verticalSeparationM(), 0));
        out.put("horizon", ai.foresignal.settings.Durations.format(horizon));
        out.put("asOf", now);
        out.put("stale", a.stale() || b.stale());
        return out;
    }

    /**
     * The ETA of a record to a place or a {@code lon,lat} point (OpenAPI {@code EtaResult}).
     *
     * @param mayReadPlaces true when the caller may see place coordinates (agent or admin)
     * @throws ForesignalException {@code 422 STATE_TOO_OLD}; 404 for an unknown place
     */
    public Map<String, Object> eta(RecordKey record, String to, boolean mayReadPlaces, Set<String> excluded,
            ReadRestriction restriction) {
        Instant now = clock.instant();
        Target target = target(to, mayReadPlaces);
        KinematicState state = apiState(record, excluded, restriction);
        requireFresh(state, now);
        CpaCalculator.Eta eta = CpaCalculator.eta(state, target.lon(), target.lat());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("record", state.toMap());
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("placeId", target.placeId());
        t.put("point", Map.of("type", "Point", "coordinates", List.of(target.lon(), target.lat())));
        out.put("target", t);
        out.put("distanceMeters", KinematicState.round(eta.distanceMeters(), 1));
        out.put("bearingDeg", KinematicState.round(eta.bearingDeg(), 1));
        out.put("bearingDiffDeg", eta.bearingDiffDeg() == null ? null : KinematicState.round(eta.bearingDiffDeg(), 1));
        out.put("approaching", eta.approaching());
        out.put("etaSeconds", eta.etaSeconds() == null ? null : KinematicState.round(eta.etaSeconds(), 0));
        out.put("etaAt", eta.etaSeconds() == null ? null : state.observedAt().plusMillis(Math.round(eta.etaSeconds() * 1000)));
        out.put("asOf", now);
        out.put("stale", state.stale());
        return out;
    }

    /**
     * An ETA target.
     *
     * @param placeId the place id, or null for a coordinate
     * @param lon the longitude
     * @param lat the latitude
     */
    public record Target(String placeId, double lon, double lat) {}

    /**
     * Parses {@code to}: {@code lon,lat}, or a place id (its point, or the interior point of a polygon or route).
     *
     * @throws ValidationException for a malformed coordinate, or a place target from a caller who may not
     *     see place coordinates
     * @throws NotFoundException for an unknown place
     */
    public Target target(String to, boolean mayReadPlaces) {
        if (to == null || to.isBlank()) {
            throw ValidationException.forField("to", "is required: a place id or lon,lat");
        }
        var m = LON_LAT.matcher(to);
        if (m.matches()) {
            double lon = Double.parseDouble(m.group(1));
            double lat = Double.parseDouble(m.group(2));
            if (lon < -180 || lon > 180 || lat < -90 || lat > 90) {
                throw ValidationException.forField("to", "must be a longitude in -180..180 and a latitude in -90..90");
            }
            return new Target(null, lon, lat);
        }
        if (!mayReadPlaces) {
            throw ValidationException.forField("to", "a place target needs an agent token (places are never shown"
                    + " as coordinates to read tokens); pass lon,lat instead");
        }
        double[] point = placePoint(to.strip()).orElseThrow(() -> NotFoundException.of("Place", to.strip()));
        return new Target(to.strip(), point[0], point[1]);
    }

    /** A place's point ({@code {lon, lat}}), or empty when there is no such place. */
    public Optional<double[]> placePoint(String placeId) {
        PlaceLookup lookup = places.getIfAvailable();
        if (lookup == null) {
            return Optional.empty();
        }
        return lookup.origin(placeId).filter(g -> !g.isEmpty()).map(g -> {
            Point p = representative(g);
            return new double[] {p.getX(), p.getY()};
        });
    }

    /**
     * One subject against up to {@value #MAX_BATCH} records of a filter (OpenAPI {@code CpaBatchResult}): the
     * top {@code top} by {@code dcpa} within {@code maxDcpaM}. Candidates out of range, older than 1 h or equal
     * to the subject are skipped.
     *
     * @throws ForesignalException {@code 422 STATE_TOO_OLD} for the subject
     */
    public Map<String, Object> batch(RecordKey subject, List<String> sources, List<String> kinds, double[] bbox,
            Duration horizon, Double maxDcpaM, int top, Set<String> excluded, ReadRestriction restriction) {
        Instant now = clock.instant();
        KinematicState s = apiState(subject, excluded, restriction);
        requireFresh(s, now);
        BboxFilter filter = bbox == null ? null
                : new BboxFilter(List.of(new Envelope(bbox[0], bbox[2], bbox[1], bbox[3])));
        CurrentQuery query = new CurrentQuery(sources, kinds, List.of(), filter, null, now.minus(MAX_AGE), null,
                ai.foresignal.api.support.TimeField.OBSERVED, null, false, ListOrder.OBSERVED, null, null, MAX_BATCH,
                FieldSelection.ALL, GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON, false,
                excluded == null ? Set.of() : excluded, restriction == null ? ReadRestriction.NONE : restriction);
        Slices.RecordSlice slice = queries.list(query);
        List<Map.Entry<Double, Map<String, Object>>> results = new ArrayList<>();
        int evaluated = 0;
        for (RecordRow row : slice.rows()) {
            if (row.tombstone() || row.geometry() == null
                    || (row.sourceId().equals(subject.sourceId()) && row.externalId().equals(subject.externalId()))) {
                continue;
            }
            KinematicState other = fromRow(row);
            if (other.age(now).compareTo(MAX_AGE) > 0) {
                continue;
            }
            evaluated++;
            CpaCalculator.Cpa cpa = CpaCalculator.cpa(s, other, horizon);
            if (!cpa.inRange() || (maxDcpaM != null && cpa.dcpaMeters() > maxDcpaM)) {
                continue;
            }
            results.add(Map.entry(cpa.dcpaMeters(), cpaMap(s, other, cpa, horizon, now)));
        }
        results.sort(Comparator.comparingDouble(Map.Entry::getKey));
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < results.size() && i < top; i++) {
            items.add(results.get(i).getValue());
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("subject", s.toMap());
        out.put("asOf", now);
        out.put("evaluated", evaluated);
        out.put("truncated", slice.nextCursor() != null);
        out.put("items", items);
        return out;
    }
}
