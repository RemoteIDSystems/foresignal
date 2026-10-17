package ai.foresignal.pol;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.DerivedGenerator;
import ai.foresignal.derive.DerivedInputs;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Point;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@code pattern-of-life} (prompt 114): the DERIVED, STANDARD source publishing the analysis of the public
 * opted-in subjects as records, so rules (017), SSE, tiles and search use them unchanged.
 *
 * <ul>
 *   <li>kind {@code pol-place}, externalId {@code {subjectId}:place:{placeId}}: one record per learned place
 *       (label, visits, dwell, typical hours), emitted on change;
 *   <li>kind {@code pol-deviation}, externalId {@code {subjectId}:{TYPE}:{key}}: {@code NEW_PLACE},
 *       {@code UNUSUAL_TIME} and {@code EXPECTED_ABSENT} ({@code PolDeviation} payload).
 * </ul>
 *
 * <p>The work happens in {@link PolEngine} on this source's 60-second tick. Records of a subject that was deleted
 * or became private, and of a place that was deleted (retention, purge), are tombstoned on the next tick; the private subjects are published by
 * {@link PolPrivateGenerator} on {@code pattern-of-life-private}. {@link PolScheduler} enables the source while a
 * subject exists.
 */
@Component
public class PolGenerator implements DerivedGenerator {

    /** The source id. */
    public static final String ID = "pattern-of-life";

    /** The place kind. */
    public static final String KIND_PLACE = "pol-place";

    /** The deviation kind. */
    public static final String KIND_DEVIATION = "pol-deviation";

    /** The retention of the records (as {@code pol.retention}). */
    static final Duration RETENTION = Duration.ofDays(180);

    private final PolEngine engine;
    private final PolSubjectRepository subjects;
    private final PolPlaceRepository places;
    private final SourceDescriptor descriptor;
    /** The external ids with a live head. */
    private final Set<String> known = new LinkedHashSet<>();

    /** Creates the public generator. */
    @Autowired
    public PolGenerator(@Lazy PolEngine engine, PolSubjectRepository subjects, PolPlaceRepository places) {
        this(engine, subjects, places, buildDescriptor(ID, false));
    }

    /** Creates a generator with its descriptor (the private subclass). */
    protected PolGenerator(PolEngine engine, PolSubjectRepository subjects, PolPlaceRepository places,
            SourceDescriptor descriptor) {
        this.engine = engine;
        this.subjects = subjects;
        this.places = places;
        this.descriptor = descriptor;
    }

    /** A pattern-of-life descriptor. */
    static SourceDescriptor buildDescriptor(String id, boolean privateSource) {
        return SourceDescriptor.builder(id)
                .name(privateSource ? "Pattern of life, private subjects (derived)" : "Pattern of life (derived)")
                .description("Learned places and deviations (new place, unusual time, expected absence) of opted-in"
                        + " subjects, from their stored positions. Automated, not an accusation.")
                .kinds(KIND_PLACE, KIND_DEVIATION)
                .attribution("Derived by Foresignal from stored positions")
                .licence("derived")
                .mode(Mode.DERIVED)
                .defaultBudget(Budget.of(1, 1))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .zeroResultsNormal(true)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .titleField("title")
                .privateSource(privateSource)
                .privatePrecisionDecimals(SourceDescriptor.DEFAULT_PRIVATE_PRECISION_DECIMALS)
                .build();
    }

    /** True for the private subjects' generator. */
    protected boolean privateClass() {
        return false;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public DerivedInputs inputs(ValidatedParams params) {
        return new DerivedInputs(Set.of(), Set.of(), Set.of(), Duration.ofSeconds(60), false);
    }

    /** Rebuilds the set of live heads; heads of deleted subjects are tombstoned on the next tick. */
    @Override
    public void reconcile(List<HeadSnapshot> ownHeads, DerivedContext ctx) {
        known.clear();
        for (HeadSnapshot head : ownHeads) {
            if (!head.tombstone()) {
                known.add(head.externalId());
            }
        }
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        engine.tick(privateClass(), new ContextOutput(ctx), ctx::sourceStatus);
        Set<String> live = new HashSet<>();
        for (PolSubjectEntity subject : subjects.findAll()) {
            if (subject.isPrivateSubject() == privateClass()) {
                live.add(subject.getId());
            }
        }
        Set<String> placeIds = new HashSet<>();
        for (PolPlaceEntity place : places.findAll()) {
            placeIds.add(place.getId());
        }
        for (String externalId : new ArrayList<>(known)) {
            int colon = externalId.indexOf(':');
            String subjectId = colon < 0 ? externalId : externalId.substring(0, colon);
            boolean placeGone = KIND_PLACE.equals(kindOf(externalId))
                    && !placeIds.contains(externalId.substring(externalId.lastIndexOf(':') + 1));
            if (!live.contains(subjectId) || placeGone) {
                known.remove(externalId);
                ctx.tombstone(kindOf(externalId), externalId);
            }
        }
    }

    /** The kind of one of this source's external ids. */
    static String kindOf(String externalId) {
        return externalId.contains(":place:") ? KIND_PLACE : KIND_DEVIATION;
    }

    /** True when {@code externalId} has a live head (tests). */
    boolean published(String externalId) {
        return known.contains(externalId);
    }

    /** {@link PolEngine.Output} over a derived context. */
    final class ContextOutput implements PolEngine.Output {

        private final DerivedContext ctx;

        ContextOutput(DerivedContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void place(PolSubjectEntity subject, PolPlaceEntity place) {
            String externalId = subject.getId() + ":place:" + place.getId();
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("subjectId", subject.getId());
            payload.put("subjectRef", subject.getSubjectType() + ":" + subject.getRef());
            payload.put("placeId", place.getId());
            payload.put("label", place.getLabel());
            payload.put("title", place.getLabel() == null ? "Learned place" : "Learned place: " + place.getLabel());
            payload.put("visits", place.getVisits());
            payload.put("totalDwellMinutes", place.getTotalDwellMinutes());
            payload.put("radiusM", place.getRadiusM());
            payload.put("typicalHours", PolViews.typicalHours(place.getHourOfWeek()));
            payload.put("firstSeen", place.getFirstSeen().toString());
            payload.put("lastSeen", place.getLastSeen().toString());
            payload.put("private", subject.isPrivateSubject());
            payload.put("automated", true);
            Point c = place.getCentroid();
            ctx.emit(NormalizedRecord.builder(externalId, KIND_PLACE).observedAt(place.getLastSeen())
                    .point(c.getX(), c.getY()).putAll(payload).build());
            known.add(externalId);
        }

        @Override
        public void placeRemoved(String subjectId, String placeId) {
            String externalId = subjectId + ":place:" + placeId;
            if (known.remove(externalId)) {
                ctx.tombstone(KIND_PLACE, externalId);
            }
        }

        @Override
        public void deviation(PolSubjectEntity subject, String key, Map<String, Object> payload, Point point,
                Instant observedAt) {
            String externalId = subject.getId() + ":" + key;
            NormalizedRecord.Builder b = NormalizedRecord.builder(externalId, KIND_DEVIATION).observedAt(observedAt)
                    .putAll(payload);
            if (point != null) {
                b.point(point.getX(), point.getY());
            }
            ctx.emit(b.build());
            known.add(externalId);
        }
    }
}
