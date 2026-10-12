package ai.foresignal.stream.sse;

import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RestrictionClamp;

import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.NearFilter;
import ai.foresignal.stream.Bbox;
import ai.foresignal.stream.ChangeSummary;
import ai.foresignal.stream.LiveUpdate;
import ai.foresignal.stream.Touch;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.locationtech.jts.geom.Envelope;

/**
 * The filter of the {@code changes} topic (Core-Data-Model §10.2), immutable.
 *
 * <p><strong>Three-box spatial match:</strong> a change matches when its {@code bbox}, its
 * {@code prevPersistedBbox} or its {@code prevLiveBbox} intersects the {@code bbox} filter (or lies
 * within {@code radiusKm} of {@code near}). When only a previous box matches, the match is
 * {@link FilterMatch#LEFT} and the event carries {@code leftFilter: true}, so the client removes the
 * feature. A {@code live} event matches on {@code bbox} or {@code prevBbox} with the same rule. An
 * event with no box at all never matches a spatial filter. Tombstones carry the previous boxes, so
 * they are delivered as {@code LEFT} to clients whose area the entity was in.
 *
 * @param sources source ids; empty for all
 * @param kinds kinds; empty for all
 * @param externalIds external ids; empty for all
 * @param bbox the box filter (one envelope, or two across the antimeridian), or null
 * @param near the radius filter, or null
 * @param payload load payloads on {@code change} events (kinds in {@code inlinePayloadKinds} always
 *     carry theirs)
 * @param live send {@code live} events
 * @param touches send {@code touch} events
 * @param excludedSources sources the caller may not read (private sources for anonymous callers)
 * @param restriction the caller's read restriction (prompt 035), tested at publish time with
 *     {@code RestrictionClamp}; a client's PATCHed filter keeps it, so it can never be widened. An
 *     entity that moves out of the restricted region simply stops matching (it is never reported as
 *     {@code LEFT}, which would carry its new position)
 *
 * <p><b>Payload filters (prompt 036).</b> When the restriction carries a request filter with payload
 * conditions ({@code view=}, {@code inWatchlist=}), a persisted change is judged with its payload
 * ({@link #matchChange(ChangeSummary, Map)}): a version that stops matching is delivered as
 * {@code LEFT} when its entity matched before. The filter remembers which entities match, and live
 * updates and touches (which carry no payload) are delivered only for those.
 */
public record ChangeFilter(
        List<String> sources,
        List<String> kinds,
        List<String> externalIds,
        BboxFilter bbox,
        NearFilter near,
        boolean payload,
        boolean live,
        boolean touches,
        Set<String> excludedSources,
        ReadRestriction restriction) {

    /** An unrestricted filter (the shape before prompt 035). */
    public ChangeFilter(List<String> sources, List<String> kinds, List<String> externalIds, BboxFilter bbox,
            NearFilter near, boolean payload, boolean live, boolean touches, Set<String> excludedSources) {
        this(sources, kinds, externalIds, bbox, near, payload, live, touches, excludedSources, ReadRestriction.NONE);
    }

    /** Mean Earth radius in kilometers, for the haversine distance. */
    static final double EARTH_RADIUS_KM = 6371.0088;

    /** Copies the lists. */
    public ChangeFilter {
        sources = sources == null ? List.of() : List.copyOf(sources);
        kinds = kinds == null ? List.of() : List.copyOf(kinds);
        externalIds = externalIds == null ? List.of() : List.copyOf(externalIds);
        excludedSources = excludedSources == null ? Set.of() : Set.copyOf(excludedSources);
        restriction = restriction == null ? ReadRestriction.NONE : restriction;
    }

    /** No filter: every change, {@code live=true}, {@code touches=false}, {@code payload=false}. */
    public static ChangeFilter all(Set<String> excludedSources) {
        return all(excludedSources, ReadRestriction.NONE);
    }

    /** {@link #all(Set)} under {@code restriction}. */
    public static ChangeFilter all(Set<String> excludedSources, ReadRestriction restriction) {
        return new ChangeFilter(List.of(), List.of(), List.of(), null, null, false, true, false, excludedSources,
                restriction);
    }

    /** True when a bbox or near filter is set. */
    public boolean spatial() {
        return bbox != null || near != null;
    }

    /** How a persisted change relates to this filter. */
    public FilterMatch matchChange(ChangeSummary change) {
        if (!matchesEntity(change.sourceId(), change.kind(), change.externalId())
                || !RestrictionClamp.test(restriction, change.sourceId(), change.kind(), change.bbox())) {
            return FilterMatch.NONE;
        }
        return matchBoxes(change.bbox(), change.prevPersistedBbox(), change.prevLiveBbox());
    }

    /** True when the restriction carries a request filter that needs payloads (prompt 036). */
    public boolean needsPayload() {
        return restriction.filter() != null && restriction.filter().needsPayload();
    }

    /**
     * How a persisted change relates to this filter, judging a payload filter's conditions on
     * {@code payload} (the version's payload, or null when it was not loaded) and keeping the
     * filter's membership memory (prompt 036).
     */
    public FilterMatch matchChange(ChangeSummary change, java.util.Map<String, Object> payload) {
        FilterMatch base = matchChange(change);
        if (!needsPayload()) {
            return base;
        }
        ai.foresignal.security.restrict.RecordFilter filter = restriction.filter();
        boolean was = filter.remembered(change.sourceId(), change.externalId());
        if (change.tombstone()) {
            filter.remember(change.sourceId(), change.externalId(), false);
            return was ? base : FilterMatch.NONE;
        }
        boolean holds = payload != null && filter.testPayload(change.sourceId(), payload);
        filter.remember(change.sourceId(), change.externalId(), holds && base == FilterMatch.MATCH);
        if (holds) {
            return base;
        }
        return was && base != FilterMatch.NONE ? FilterMatch.LEFT : FilterMatch.NONE;
    }

    /** Records that a replayed change of an entity matched (its SQL already applied the filter). */
    public void rememberReplayed(String sourceId, String externalId, boolean tombstone) {
        if (needsPayload()) {
            restriction.filter().remember(sourceId, externalId, !tombstone);
        }
    }

    /** How a live update relates to this filter, ignoring {@link #live} itself. */
    public FilterMatch matchLive(LiveUpdate update) {
        if (!matchesEntity(update.sourceId(), update.kind(), update.externalId())
                || !RestrictionClamp.test(restriction, update.sourceId(), update.kind(), update.bbox())) {
            return FilterMatch.NONE;
        }
        if (needsPayload() && !restriction.filter().remembered(update.sourceId(), update.externalId())) {
            return FilterMatch.NONE;
        }
        return matchBoxes(update.bbox(), update.prevBbox(), null);
    }

    /**
     * How a touch relates to this filter, ignoring {@link #touches} itself. A touch carries no kind,
     * so a kind filter matches when any of {@code possibleKinds} (the kinds its source declares) is
     * selected; a touch has no previous box, so it never yields {@code LEFT}.
     */
    public FilterMatch matchTouch(Touch touch, Collection<String> possibleKinds) {
        if (!matchesSource(touch.sourceId()) || !matchesExternalId(touch.externalId())) {
            return FilterMatch.NONE;
        }
        if (!kinds.isEmpty() && (possibleKinds == null || possibleKinds.stream().noneMatch(kinds::contains))) {
            return FilterMatch.NONE;
        }
        if (!touchAllowed(touch, possibleKinds)) {
            return FilterMatch.NONE;
        }
        if (needsPayload() && !restriction.filter().remembered(touch.sourceId(), touch.externalId())) {
            return FilterMatch.NONE;
        }
        return matchBoxes(touch.bbox(), null, null);
    }

    /**
     * True when a touch passes the restriction: its source is allowed, every kind its source may
     * emit is allowed (a touch carries no kind), and its box lies in the restricted region.
     */
    private boolean touchAllowed(Touch touch, Collection<String> possibleKinds) {
        if (!restriction.hasDataRestrictions()) {
            return true;
        }
        if (restriction.kinds() != null
                && (possibleKinds == null || possibleKinds.isEmpty() || !restriction.kinds().containsAll(possibleKinds))) {
            return false;
        }
        String anyKind = possibleKinds == null || possibleKinds.isEmpty() ? null : possibleKinds.iterator().next();
        return RestrictionClamp.test(restriction.kinds() == null ? restriction
                : new ReadRestriction(restriction.sources(), null, restriction.bbox(), restriction.area(), null, false,
                        null), touch.sourceId(), anyKind, touch.bbox());
    }

    /** True when the source, kind and external id pass (no spatial test). */
    public boolean matchesEntity(String sourceId, String kind, String externalId) {
        return matchesSource(sourceId) && (kinds.isEmpty() || kinds.contains(kind)) && matchesExternalId(externalId);
    }

    /**
     * The three-box rule: {@code MATCH} when {@code current} matches (or there is no spatial filter),
     * {@code LEFT} when only a previous box matches, otherwise {@code NONE}.
     */
    public FilterMatch matchBoxes(Bbox current, Bbox previousPersisted, Bbox previousLive) {
        if (!spatial()) {
            return FilterMatch.MATCH;
        }
        if (inArea(current)) {
            return FilterMatch.MATCH;
        }
        if (inArea(previousPersisted) || inArea(previousLive)) {
            return FilterMatch.LEFT;
        }
        return FilterMatch.NONE;
    }

    /** True when {@code box} passes every spatial filter that is set; false for a null box. */
    public boolean inArea(Bbox box) {
        if (box == null) {
            return false;
        }
        if (bbox != null && !intersects(bbox, box)) {
            return false;
        }
        return near == null || withinRadius(near, box);
    }

    private boolean matchesSource(String sourceId) {
        return !excludedSources.contains(sourceId) && (sources.isEmpty() || sources.contains(sourceId));
    }

    private boolean matchesExternalId(String externalId) {
        return externalIds.isEmpty() || externalIds.contains(externalId);
    }

    static boolean intersects(BboxFilter filter, Bbox box) {
        for (Envelope e : filter.envelopes()) {
            if (box.minX() <= e.getMaxX() && e.getMinX() <= box.maxX()
                    && box.minY() <= e.getMaxY() && e.getMinY() <= box.maxY()) {
                return true;
            }
        }
        return false;
    }

    /**
     * True when the nearest point of {@code box} to the center lies within the radius (haversine).
     * The nearest longitude is chosen across the antimeridian as well.
     */
    static boolean withinRadius(NearFilter near, Bbox box) {
        double lat = clamp(near.lat(), box.minY(), box.maxY());
        double best = Double.MAX_VALUE;
        for (double shift : new double[] {0.0, 360.0, -360.0}) {
            double lon = clamp(near.lon() + shift, box.minX(), box.maxX());
            best = Math.min(best, haversineKm(near.lat(), near.lon() + shift, lat, lon));
        }
        return best <= near.radiusKm();
    }

    static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.min(1.0, Math.sqrt(a)));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
