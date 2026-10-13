package ai.foresignal.source.adapters.satdump;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.media.GuardedImages;
import ai.foresignal.media.ImageGuard;
import ai.foresignal.source.InboxFiles;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.FrameInput;
import ai.foresignal.source.spi.InboxAccess;
import ai.foresignal.source.spi.LiveImageHolder;
import ai.foresignal.source.spi.MediaInput;
import ai.foresignal.source.spi.MediaRef;
import ai.foresignal.source.spi.MediaSink;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterSink;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.weather.WeatherAdapterHarness;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A hand-built {@link StreamContext} for the prompt 049 station tests: a real {@link InboxFiles} over a
 * temporary inbox, an in-memory adapter state, a settable clock, recording raster and media sinks (the
 * media sink decodes through the real {@link GuardedImages}) and an optional {@link SourceHttp}.
 */
final class SatDumpTestContext implements StreamContext {

    final SourceDescriptor descriptor = SatDumpDescriptor.build("home");
    final Map<String, Object> params = new LinkedHashMap<>();
    final Map<String, InetAddress> pinned = new LinkedHashMap<>();
    final WeatherAdapterHarness.MapState state = new WeatherAdapterHarness.MapState();
    final MutableClock clock;
    final InboxFiles inbox;
    final Recording rasters = new Recording();
    final RecordingMedia media = new RecordingMedia();
    final AtomicInteger charged = new AtomicInteger();
    final List<NormalizedRecord> records = new CopyOnWriteArrayList<>();
    final List<String> held = new CopyOnWriteArrayList<>();
    SourceHttp http;
    boolean budgetSpent;

    /**
     * A context whose inbox root is {@code inboxRoot}.
     *
     * @param inboxRoot the inbox directory
     * @param now the starting time
     */
    SatDumpTestContext(Path inboxRoot, Instant now) {
        this.clock = new MutableClock(now);
        this.inbox = new InboxFiles(new ForesignalProperties(inboxRoot.getParent(), inboxRoot, false, null));
        params.put(SatDumpDescriptor.LOCATION, "29.76,-95.37");
        params.put(SatDumpDescriptor.API_INTERVAL, java.time.Duration.ofSeconds(5));
        params.put(SatDumpDescriptor.SCAN_INTERVAL, java.time.Duration.ofSeconds(30));
    }

    /** The sink the station emits into. */
    RecordSink sink() {
        return records::add;
    }

    /** A live-image holder recording what it is given. */
    LiveImageHolder holder() {
        return new LiveImageHolder() {
            @Override
            public void hold(String sourceId, byte[] jpeg, Instant fetchedAt) {
                held.add("hold " + jpeg.length);
            }

            @Override
            public void clear(String sourceId) {
                held.add("clear");
            }
        };
    }

    /** A station over this context. */
    SatDumpStation station() throws AdapterException {
        return new SatDumpAdapter("home", () -> null, this::holder).newStation(this, sink());
    }

    /** The records emitted so far, then forgets them. */
    List<NormalizedRecord> drain() {
        List<NormalizedRecord> out = new ArrayList<>(records);
        records.clear();
        return out;
    }

    @Override
    public String sourceId() {
        return descriptor.id();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public SourceHttp http() {
        if (http == null) {
            throw new UnsupportedOperationException("no HTTP in this test");
        }
        return http;
    }

    @Override
    public ValidatedParams params() {
        return new ValidatedParams(params, pinned);
    }

    @Override
    public Map<String, String> credentials() {
        return Map.of();
    }

    @Override
    public Clock clock() {
        return clock;
    }

    @Override
    public Optional<Instant> lastSuccessAt() {
        return Optional.empty();
    }

    @Override
    public AdapterStateStore state() {
        return state;
    }

    @Override
    public RasterSink rasters() {
        return rasters;
    }

    @Override
    public MediaSink media() {
        return media;
    }

    @Override
    public InboxAccess inbox() {
        return inbox;
    }

    @Override
    public void chargeRequest(String label) throws AdapterException {
        if (budgetSpent) {
            throw new AdapterException("budget spent");
        }
        charged.incrementAndGet();
    }

    @Override
    public void recordReceivedBytes(long bytes) {}

    @Override
    public void connected() {}

    @Override
    public void disconnected(Throwable error) {}

    /** A raster sink recording registrations, commits and provenance copies. */
    static final class Recording implements RasterSink {

        final List<ProductSpec> registered = new CopyOnWriteArrayList<>();
        final List<FrameInput> commits = new CopyOnWriteArrayList<>();
        final Map<String, byte[]> provenance = new LinkedHashMap<>();
        boolean probe;
        /** The upload area behind {@link #uploads()} (prompt 054), or null for none. */
        InboxAccess uploads;
        /** The GDAL warp behind {@link #warpToWgs84} (prompt 054); empty by default (no GDAL). */
        java.util.function.Function<byte[], Optional<byte[]>> warp = b -> Optional.empty();
        final List<byte[]> warped = new CopyOnWriteArrayList<>();

        @Override
        public NormalizedRecord commit(FrameInput frame) {
            commits.add(frame);
            if (frame.partial()) {
                return null;
            }
            String frameKey = ai.foresignal.raster.FrameKeys.timeKey(frame.validTime()) + "~0a1b2c3d";
            return NormalizedRecord.builder(frame.productId(), ProductSpec.RECORD_KIND).observedAt(frame.validTime())
                    .put("productId", frame.productId())
                    .put("frameKey", frameKey)
                    .put("tiles", "http://test/v1/products/" + frame.productId() + "/tiles/" + frameKey
                            + "/{z}/{x}/{y}.png?style=rgb@1")
                    .build();
        }

        @Override
        public Optional<byte[]> warpToWgs84(byte[] geotiff) {
            warped.add(geotiff);
            return warp.apply(geotiff);
        }

        @Override
        public InboxAccess uploads() {
            if (uploads == null) {
                throw new UnsupportedOperationException("no upload area in this test");
            }
            return uploads;
        }

        @Override
        public List<NormalizedRecord> completeRun(String productId, Instant referenceTime) {
            return List.of();
        }

        @Override
        public void reportFormatChange(String productId, String reason) {}

        @Override
        public boolean probing() {
            return probe;
        }

        @Override
        public void registerProduct(ProductSpec spec) {
            registered.add(spec);
        }

        @Override
        public synchronized void storeProvenance(String relativePath, byte[] content) {
            provenance.put(relativePath, content);
        }
    }

    /** A media sink decoding through the real guard and recording what it stores. */
    static final class RecordingMedia implements MediaSink {

        final GuardedImages images = new GuardedImages(new ImageGuard(25L * 1024 * 1024, new SimpleMeterRegistry()));
        final List<MediaInput> stored = new CopyOnWriteArrayList<>();
        final AtomicInteger ids = new AtomicInteger();

        @Override
        public MediaRef store(MediaInput input) {
            stored.add(input);
            String id = "01JA0Z6Q8K3V8X2N4M5P6R7S" + String.format("%02d", ids.incrementAndGet());
            return new MediaRef(id, "sha", input.contentType(), 96, 64, input.knownSize());
        }

        @Override
        public List<MediaRef> thumbnails(MediaRef original) {
            return List.of(new MediaRef(original.id() + "L", "s", "image/jpeg", 1024, 683, 10),
                    new MediaRef(original.id() + "S", "s", "image/jpeg", 256, 171, 5));
        }

        @Override
        public BufferedImage decode(byte[] bytes, String contentType) {
            return images.decode(bytes, contentType);
        }
    }
}
