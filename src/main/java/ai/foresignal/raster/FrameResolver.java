package ai.foresignal.raster;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.source.spi.ProductSpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Resolves frame tokens and run selectors to stored frames (Weather-Imagery-Design §6.1).
 *
 * <ul>
 *   <li>{@code latest}: the newest complete observed frame; for a forecast, the step nearest now of
 *       the selected run (by default the newest complete run).
 *   <li>{@code now}: the frame valid at or before now with the smallest gap, if that gap is within
 *       {@code maxGap}.
 *   <li>a bare {@code timeKey}: the frame valid then (a forecast needs {@code run}, else
 *       {@code 400 RUN_REQUIRED}).
 *   <li>a {@code frameKey} ({@code timeKey~sha8}): as the time key, and the {@code sha8} must be the
 *       current content's; a stale one (a reprocessed frame) is {@code 404 UNKNOWN_FRAME} with a
 *       {@code Link} header to the current key.
 *   <li>an ISO-8601 extended time: a {@link Redirect} to the canonical time key (the caller answers
 *       301).
 *   <li>{@code run=latest|previous|<runIso>} selects a forecast run.
 * </ul>
 *
 * <p>Partial frames and runs in progress are excluded unless {@code includePartial}. A frame that
 * retention removed is {@code 410 FRAME_EXPIRED}: its time is older than the oldest retained frame,
 * or falls in the thinned part of the ladder; any other missing time is {@code 404 UNKNOWN_FRAME}.
 */
@Component
public class FrameResolver {

    /** The frame-key aliases. */
    public static final String LATEST = "latest";

    /** The "valid at or before now" alias. */
    public static final String NOW = "now";

    /** The newest complete run. */
    public static final String RUN_LATEST = "latest";

    /** The complete run before the newest. */
    public static final String RUN_PREVIOUS = "previous";

    /** Read access to the frames, so the resolver can be tested without a database. */
    public interface FrameLookup {

        /** The newest frame. */
        Optional<RasterFrameRow> newest(String productId, boolean includePartial);

        /** The newest frame valid at or before {@code at}. */
        Optional<RasterFrameRow> newestAtOrBefore(String productId, Instant at, boolean includePartial);

        /** Frames valid at exactly {@code validTime}, newest run first. */
        List<RasterFrameRow> atValidTime(String productId, Instant validTime);

        /** The oldest retained valid time. */
        Optional<Instant> earliest(String productId);

        /** The runs of a forecast product, newest first. */
        List<RasterFrameQueries.RunSummary> runs(String productId);

        /** The frames of one run, by step. */
        List<RasterFrameRow> runFrames(String productId, Instant run, boolean includePartial);
    }

    /** What a token resolved to. */
    public sealed interface Resolution permits Found, Redirect {}

    /**
     * A frame.
     *
     * @param frame the frame
     * @param contentAddressed true when the token named the content ({@code ~sha8}) and, for a
     *     forecast, a pinned run, and the frame is complete: such a URL is immutable
     */
    public record Found(RasterFrameRow frame, boolean contentAddressed) implements Resolution {}

    /**
     * A non-canonical spelling: the caller answers 301 to the URL with {@code canonicalToken}.
     *
     * @param canonicalToken the time key to use instead
     */
    public record Redirect(String canonicalToken) implements Resolution {}

    private final FrameLookup lookup;

    /** Creates the resolver over the database. */
    @org.springframework.beans.factory.annotation.Autowired
    public FrameResolver(RasterFrameQueries queries) {
        this(new FrameLookup() {
            @Override
            public Optional<RasterFrameRow> newest(String productId, boolean includePartial) {
                return queries.newest(productId, includePartial);
            }

            @Override
            public Optional<RasterFrameRow> newestAtOrBefore(String productId, Instant at, boolean includePartial) {
                return queries.newestAtOrBefore(productId, at, includePartial);
            }

            @Override
            public List<RasterFrameRow> atValidTime(String productId, Instant validTime) {
                return queries.atValidTime(productId, validTime);
            }

            @Override
            public Optional<Instant> earliest(String productId) {
                return queries.earliest(productId);
            }

            @Override
            public List<RasterFrameQueries.RunSummary> runs(String productId) {
                return queries.runs(productId);
            }

            @Override
            public List<RasterFrameRow> runFrames(String productId, Instant run, boolean includePartial) {
                return queries.runFrames(productId, run, includePartial);
            }
        });
    }

    /** Creates the resolver over {@code lookup}. */
    public FrameResolver(FrameLookup lookup) {
        this.lookup = lookup;
    }

    /**
     * Resolves {@code token} of {@code spec}.
     *
     * @param token {@code latest}, {@code now}, a time key, a frame key or an ISO-8601 time
     * @param run the run selector, or null
     * @param includePartial include partial frames and runs in progress
     * @param now the current instant
     * @param currentKeyLink builds the {@code Link} target for a current frame key (stale sha8)
     * @return the frame, or a redirect
     * @throws ForesignalException {@code 404 UNKNOWN_FRAME}, {@code 410 FRAME_EXPIRED},
     *     {@code 400 RUN_REQUIRED} or {@code 400 VALIDATION_FAILED}
     */
    public Resolution resolve(ProductSpec spec, String token, String run, boolean includePartial, Instant now,
            UnaryOperator<String> currentKeyLink) {
        if (token == null || token.isBlank()) {
            throw ValidationException.forField("frameKey", "is required");
        }
        if (LATEST.equals(token)) {
            return new Found(latest(spec, run, includePartial, now), false);
        }
        if (NOW.equals(token)) {
            return new Found(atNow(spec, run, includePartial, now), false);
        }
        FrameKeys.ParsedKey key = FrameKeys.parseFrameKey(token);
        if (key == null) {
            String canonical = FrameKeys.canonicalFromIso(token);
            if (canonical != null) {
                return new Redirect(canonical);
            }
            throw ValidationException.forField("frameKey",
                    "must be latest, now, a time key (20261010T143240Z) or a frame key (20261010T143240Z~1a2b3c4d)");
        }
        Instant time = key.time();
        RasterFrameRow frame;
        boolean pinnedRun = false;
        if (spec.isForecast()) {
            if (run == null || run.isBlank()) {
                throw new ForesignalException(HttpStatus.BAD_REQUEST, ErrorCodes.RUN_REQUIRED,
                        "Product " + spec.id() + " is a forecast; a concrete time needs run=latest, previous or a run time.");
            }
            Instant selected = selectRun(spec, run, includePartial);
            pinnedRun = !RUN_LATEST.equals(run) && !RUN_PREVIOUS.equals(run);
            frame = lookup.runFrames(spec.id(), selected, includePartial).stream()
                    .filter(f -> f.validTime().equals(time)).findFirst()
                    .orElseThrow(() -> missing(spec, time));
        } else {
            frame = lookup.atValidTime(spec.id(), time).stream()
                    .filter(f -> includePartial || f.complete()).findFirst()
                    .orElseThrow(() -> missing(spec, time));
        }
        if (key.contentAddressed() && !frame.sha8().equals(key.sha8())) {
            ForesignalException stale = new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.UNKNOWN_FRAME,
                    "Frame " + token + " was reprocessed; its current key is " + frame.frameKey() + ".");
            stale.withProperty("currentFrameKey", frame.frameKey());
            if (currentKeyLink != null) {
                stale.withHeader("Link", "<" + currentKeyLink.apply(frame.frameKey()) + ">; rel=\"current\"");
            }
            throw stale;
        }
        boolean immutable = key.contentAddressed() && frame.complete() && (!spec.isForecast() || pinnedRun);
        return new Found(frame, immutable);
    }

    /** The {@code latest} alias. */
    RasterFrameRow latest(ProductSpec spec, String run, boolean includePartial, Instant now) {
        if (spec.isForecast()) {
            List<RasterFrameRow> steps = stepsOf(spec, run == null ? RUN_LATEST : run, includePartial);
            return steps.stream()
                    .min(Comparator.comparingLong((RasterFrameRow f) -> Math.abs(Duration.between(now, f.validTime()).toMillis()))
                            .thenComparing(RasterFrameRow::validTime))
                    .orElseThrow(() -> noFrame(spec));
        }
        return lookup.newest(spec.id(), includePartial).orElseThrow(() -> noFrame(spec));
    }

    /** The {@code now} alias. */
    RasterFrameRow atNow(ProductSpec spec, String run, boolean includePartial, Instant now) {
        Optional<RasterFrameRow> candidate;
        if (spec.isForecast()) {
            candidate = stepsOf(spec, run == null ? RUN_LATEST : run, includePartial).stream()
                    .filter(f -> !f.validTime().isAfter(now))
                    .max(Comparator.comparing(RasterFrameRow::validTime));
        } else {
            candidate = lookup.newestAtOrBefore(spec.id(), now, includePartial);
        }
        RasterFrameRow frame = candidate.orElseThrow(() -> noFrame(spec));
        Duration maxGap = spec.temporal() == null ? null : spec.temporal().maxGap();
        if (maxGap == null && spec.temporal() != null) {
            maxGap = spec.temporal().cadence();
        }
        if (maxGap != null && Duration.between(frame.validTime(), now).compareTo(maxGap) > 0) {
            throw new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.UNKNOWN_FRAME, "Product " + spec.id()
                    + " has no frame within " + maxGap.toSeconds() + " s of now; the newest is " + frame.timeKey() + ".");
        }
        return frame;
    }

    private List<RasterFrameRow> stepsOf(ProductSpec spec, String run, boolean includePartial) {
        return lookup.runFrames(spec.id(), selectRun(spec, run, includePartial), includePartial);
    }

    /**
     * The reference time a run selector names.
     *
     * @throws ForesignalException {@code 404 UNKNOWN_FRAME} when no such run exists;
     *     {@code 400 VALIDATION_FAILED} for an unreadable selector
     */
    public Instant selectRun(ProductSpec spec, String run, boolean includePartial) {
        List<RasterFrameQueries.RunSummary> runs = lookup.runs(spec.id()).stream()
                .filter(r -> includePartial || r.complete()).toList();
        if (run == null || RUN_LATEST.equals(run)) {
            return runs.stream().findFirst().map(RasterFrameQueries.RunSummary::run).orElseThrow(() -> noFrame(spec));
        }
        if (RUN_PREVIOUS.equals(run)) {
            if (runs.size() < 2) {
                throw new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.UNKNOWN_FRAME,
                        "Product " + spec.id() + " has no previous complete run.");
            }
            return runs.get(1).run();
        }
        Instant wanted = FrameKeys.parseRun(run);
        if (wanted == null) {
            throw ValidationException.forField("run", "must be latest, previous or an ISO-8601 run time");
        }
        return runs.stream().map(RasterFrameQueries.RunSummary::run).filter(wanted::equals).findFirst()
                .orElseThrow(() -> new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.UNKNOWN_FRAME,
                        "Product " + spec.id() + " has no " + (includePartial ? "" : "complete ") + "run "
                                + FrameKeys.runIso(wanted) + "."));
    }

    private ForesignalException missing(ProductSpec spec, Instant time) {
        Optional<Instant> earliest = lookup.earliest(spec.id());
        Optional<RasterFrameRow> newest = lookup.newest(spec.id(), true);
        boolean expired = earliest.isPresent() && time.isBefore(earliest.get());
        if (!expired && newest.isPresent() && spec.retention() != null && !spec.retention().rungs().isEmpty()
                && !spec.isForecast()) {
            Instant thinnedBefore = newest.get().validTime().minus(spec.retention().rungs().get(0).olderThan());
            expired = time.isBefore(thinnedBefore);
        }
        if (expired) {
            return new ForesignalException(HttpStatus.GONE, ErrorCodes.FRAME_EXPIRED,
                    "Frame " + FrameKeys.timeKey(time) + " of " + spec.id() + " was removed by retention.");
        }
        return new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.UNKNOWN_FRAME,
                "Product " + spec.id() + " has no frame at " + FrameKeys.timeKey(time) + ".");
    }

    private static ForesignalException noFrame(ProductSpec spec) {
        return new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.UNKNOWN_FRAME,
                "Product " + spec.id() + " has no frame yet.");
    }
}
