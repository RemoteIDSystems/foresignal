package ai.foresignal.testsupport.weather;

import ai.foresignal.derive.DefaultDerivedContext;
import ai.foresignal.derive.DerivedPlaces;
import ai.foresignal.derive.DerivedRecords;
import ai.foresignal.derive.EmitLedger;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.place.PlaceView;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.sample.FrameView;
import ai.foresignal.raster.sample.RasterReader;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.FlashSink;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.MutableClock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

/**
 * An in-memory DERIVED world for the prompt 052 generator unit tests: frames as {@link FrameView}s, current
 * records, places, source statuses and a flash sink, with a {@link #context} per invocation that collects the
 * emits through a real {@link EmitLedger}. No Spring, no database.
 */
public final class FakeDerived implements RasterReader, DerivedRecords, DerivedPlaces {

    /** The clock every context uses. */
    public final MutableClock clock;

    /** Frames by product id, oldest first. */
    public final Map<String, List<FrameView>> frames = new HashMap<>();

    /** Complete runs: product id → run → steps. */
    public final Map<String, TreeMap<Instant, List<FrameView>>> runs = new HashMap<>();

    /** Current records by source id. */
    public final Map<String, List<HeadSnapshot>> heads = new HashMap<>();

    /** Places by id. */
    public final Map<String, PlaceView> places = new LinkedHashMap<>();

    /** Source statuses (missing means idle). */
    public final Map<String, HealthStatus> statuses = new HashMap<>();

    /** Unavailable products and why. */
    public final Map<String, String> unavailable = new HashMap<>();

    /** The flash sink, or null for none. */
    public FlashSink flashes;

    /** Every record emitted so far, in order. */
    public final List<NormalizedRecord> emitted = new ArrayList<>();

    private final EmitLedger ledger = new EmitLedger();
    private final Map<String, String> state = new HashMap<>();

    /** A world at {@code now}. */
    public FakeDerived(Instant now) {
        this.clock = new MutableClock(now);
    }

    /** A context for one invocation of {@code descriptor}. */
    public DefaultDerivedContext context(SourceDescriptor descriptor, ValidatedParams params) {
        return new DefaultDerivedContext(descriptor, params, new AdapterStateStore() {
            @Override
            public Optional<String> get(String key) {
                return Optional.ofNullable(state.get(key));
            }

            @Override
            public void put(String key, String value) {
                state.put(key, value);
            }

            @Override
            public void remove(String key) {
                state.remove(key);
            }
        }, this, this, this, id -> statuses.getOrDefault(id, HealthStatus.IDLE), clock, ledger);
    }

    /** Seeds the emit ledger from heads, as the runner does before every reconcile. */
    public void seed(List<HeadSnapshot> heads) {
        ledger.seed(heads);
    }

    /** Collects a finished context's emits, and returns them. */
    public List<NormalizedRecord> collect(DefaultDerivedContext ctx) {
        List<NormalizedRecord> out = ctx.emitted();
        emitted.addAll(out);
        return out;
    }

    /** Adds an observation frame. */
    public void addFrame(String productId, FrameView view) {
        frames.computeIfAbsent(productId, k -> new ArrayList<>()).add(view);
    }

    /** Adds a complete run. */
    public void addRun(String productId, Instant run, List<FrameView> steps) {
        runs.computeIfAbsent(productId, k -> new TreeMap<>()).put(run, steps);
    }

    /** Adds a point place with a radius. */
    public PlaceView addPlace(String id, String name, double lon, double lat, double radiusKm, int version) {
        Polygon circle = (Polygon) Geo.point(lon, lat).buffer(radiusKm / 111.0);
        MultiPolygon area = Geo.factory().createMultiPolygon(new Polygon[] {circle});
        PlaceView place = new PlaceView(id, name, "point", Geo.point(lon, lat), radiusKm, null, area, "America/Chicago",
                "user", List.of(), List.of(), Map.of(), true, version, clock.instant(), clock.instant());
        places.put(id, place);
        return place;
    }

    // ------------------------------------------------------------------ RasterReader

    private Optional<FrameView> byRow(RasterFrameRow row) {
        for (List<FrameView> list : frames.values()) {
            for (FrameView v : list) {
                if (v.frame() == row) {
                    return Optional.of(v);
                }
            }
        }
        for (TreeMap<Instant, List<FrameView>> byRun : runs.values()) {
            for (List<FrameView> list : byRun.values()) {
                for (FrameView v : list) {
                    if (v.frame() == row) {
                        return Optional.of(v);
                    }
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<ProductSpec> product(String productId) {
        if (frames.containsKey(productId) && !frames.get(productId).isEmpty()) {
            return Optional.of(frames.get(productId).get(0).spec());
        }
        if (runs.containsKey(productId) && !runs.get(productId).isEmpty()) {
            return Optional.of(runs.get(productId).firstEntry().getValue().get(0).spec());
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> unavailableReason(String productId) {
        if (unavailable.containsKey(productId)) {
            return Optional.of(unavailable.get(productId));
        }
        return product(productId).isPresent() ? Optional.empty() : Optional.of("not installed");
    }

    @Override
    public Optional<RasterFrameRow> newestFrame(String productId) {
        List<FrameView> list = frames.getOrDefault(productId, List.of());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(list.size() - 1).frame());
    }

    @Override
    public List<RasterFrameRow> frames(String productId, Instant from, Instant to) {
        List<RasterFrameRow> out = new ArrayList<>();
        for (FrameView v : frames.getOrDefault(productId, List.of())) {
            if (!v.frame().validTime().isBefore(from) && !v.frame().validTime().isAfter(to)) {
                out.add(v.frame());
            }
        }
        return out;
    }

    @Override
    public Optional<Instant> latestCompleteRun(String productId) {
        TreeMap<Instant, List<FrameView>> byRun = runs.get(productId);
        return byRun == null || byRun.isEmpty() ? Optional.empty() : Optional.of(byRun.lastKey());
    }

    @Override
    public List<RasterFrameRow> runFrames(String productId, Instant run) {
        TreeMap<Instant, List<FrameView>> byRun = runs.get(productId);
        if (byRun == null || !byRun.containsKey(run)) {
            return List.of();
        }
        return byRun.get(run).stream().map(FrameView::frame).toList();
    }

    @Override
    public Optional<FrameView> view(RasterFrameRow frame) {
        return byRow(frame);
    }

    @Override
    public FlashSink flashes() {
        if (flashes == null) {
            throw new UnsupportedOperationException("no flash sink");
        }
        return flashes;
    }

    // ------------------------------------------------------------------ DerivedRecords

    @Override
    public List<HeadSnapshot> heads(String sourceId, String kind) {
        return heads.getOrDefault(sourceId, List.of()).stream().filter(h -> kind == null || kind.equals(h.kind())).toList();
    }

    @Override
    public List<HeadSnapshot> current(Collection<String> sources, Collection<String> kinds, int limit) {
        List<HeadSnapshot> out = new ArrayList<>();
        for (String s : sources) {
            for (HeadSnapshot h : heads.getOrDefault(s, List.of())) {
                if (!h.tombstone() && (kinds.isEmpty() || kinds.contains(h.kind()))) {
                    out.add(h);
                }
            }
        }
        return out.size() > limit ? out.subList(0, limit) : out;
    }

    @Override
    public Optional<HeadSnapshot> get(String sourceId, String externalId) {
        return heads.getOrDefault(sourceId, List.of()).stream().filter(h -> h.externalId().equals(externalId)).findFirst();
    }

    // ------------------------------------------------------------------ DerivedPlaces

    @Override
    public List<PlaceView> all() {
        return new ArrayList<>(new TreeMap<>(places).values());
    }

    @Override
    public Optional<PlaceView> find(String id) {
        return Optional.ofNullable(places.get(id));
    }

    @Override
    public MultiPolygon aoi() {
        List<Polygon> parts = new ArrayList<>();
        for (PlaceView p : places.values()) {
            Geometry g = p.geometry().buffer(150 / 111.0);
            parts.add((Polygon) g);
        }
        if (parts.isEmpty()) {
            return Geo.factory().createMultiPolygon(new Polygon[] {(Polygon) Geo.factory()
                    .toGeometry(new org.locationtech.jts.geom.Envelope(-106.65, -93.51, 25.84, 36.50))});
        }
        Geometry union = Geo.factory().buildGeometry(parts).union();
        if (union instanceof MultiPolygon m) {
            return m;
        }
        return Geo.factory().createMultiPolygon(new Polygon[] {(Polygon) union});
    }

    @Override
    public double[] aoiEnvelope() {
        org.locationtech.jts.geom.Envelope e = aoi().getEnvelopeInternal();
        return new double[] {e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY()};
    }
}
