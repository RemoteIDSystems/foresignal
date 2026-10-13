package ai.foresignal.source.adapters.satdump;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.InboxAccess;
import ai.foresignal.source.spi.LiveImageHolder;
import ai.foresignal.source.spi.MediaSink;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PlacePoints;
import ai.foresignal.source.spi.MediaInput;
import ai.foresignal.source.spi.MediaRef;
import ai.foresignal.source.spi.MediaRole;
import ai.foresignal.source.spi.RasterSink;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.RemoteMediaReceiver;
import ai.foresignal.source.spi.RemoteMediaReceivers;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.slf4j.Logger;

/**
 * One running {@code satdump-{station}} source (prompt 049, Weather-Imagery-Design §4): two loops that
 * share the adapter state.
 *
 * <ul>
 *   <li><b>{@code /api} loop</b> (only with {@code apiUrl}), every {@code apiInterval}: the
 *       {@code ground-station} record (a version only on a state or quantized change), the RF session
 *       (sampled about every 2 s, flushed every 30 s and at LOS), the pass's {@code receiving} and
 *       {@code recorded} versions, and during an active autotrack pass the latest
 *       {@code /polarplot.jpeg} every 15 s, held in memory only. {@code /fft.jpeg} (it turns the FFT on)
 *       and {@code /schedule.jpeg} are never requested.
 *   <li><b>Inbox loop</b>, every {@code scanInterval} (one budget unit per scan): polar pass folders,
 *       xRIT frames, overdue passes ({@code failed}) and the health reasons. It runs even without an
 *       {@code inboxPath}, for the overdue check, and its liveness is the stream's: the handle is
 *       running while this loop is.
 * </ul>
 *
 * <p>Health: {@code failing} when {@code apiUrl} is set and has been unreachable for
 * {@value #API_FAILURES_TO_FAIL} polls in a row; {@code degraded} for {@code /api} shape drift, a pass
 * without {@code dataset.json}, FY-4/MSG HRV and BigTIFF. Without {@code apiUrl} the source stays healthy
 * on the inbox loop alone, with one {@code ground-station} record in state {@code unknown}.
 *
 * <p>Prompt 054 adds polar overlays ({@link PolarOverlays}: GeoTIFFs matching {@code polarGlob} in a
 * ready pass folder become {@code satdump-{station}-{composite}} frames, and the pass geometry becomes
 * the footprint, else an approximate swath), remote pass bundles (the inbox loop also scans
 * {@code rasters/satdump/{station}/incoming/}, where {@code POST /v1/ingest/{sourceId}/rasters} writes
 * them; an upload is ready at once) and pushed images ({@link #receive}, registered with
 * {@link RemoteMediaReceivers} while the stream runs).
 *
 * <p>State changes run under the station's lock; records are emitted after it is released, because
 * {@link RecordSink#accept} may block.
 */
public final class SatDumpStation implements StreamHandle, RemoteMediaReceiver {

    /** Consecutive unreachable polls after which the source is failing. */
    public static final int API_FAILURES_TO_FAIL = 3;

    /** The polar plot period. */
    public static final Duration POLARPLOT_PERIOD = Duration.ofSeconds(15);

    /** Inactive polls after which a live-mode session ends. */
    public static final int LIVE_IDLE_POLLS = 2;

    /** The largest {@code /api} body read. */
    public static final long MAX_API_BYTES = 4L * 1024 * 1024;

    private final String station;
    private final String sourceId;
    private final StreamContext ctx;
    private final RecordSink sink;
    private final PlacePoints places;
    private final LiveImageHolder liveImages;
    private final URI apiUrl;
    private final Path inboxPath;
    private final Duration apiInterval;
    private final Duration scanInterval;
    private final StationLocation location;
    private final SatNames names;
    private final PassBook book;
    private final InboxScanner scanner = new InboxScanner();
    private final InboxScanner uploadScanner = new InboxScanner(0);
    private final PolarOverlays overlays;
    private final PolarGlob glob;
    private final RemoteMediaReceivers receivers;
    private final Map<String, String> polarProducts = new LinkedHashMap<>();
    private final XritFrames xrit;
    private final PolarPassProcessor polar;
    private final StationStore store;
    private final Logger log;
    private final Object lock = new Object();
    private final Map<String, String> reasons = new LinkedHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean();
    private final List<Consumer<Throwable>> closeCallbacks = new ArrayList<>();

    private Map<String, Object> lastGroundStation;
    private RfSession session;
    private int apiFailures;
    private int idlePolls;
    private Instant lastPolarplot;
    private Thread apiThread;
    private Thread inboxThread;

    /**
     * Creates the station.
     *
     * @param station the station name
     * @param ctx the stream context
     * @param sink where records go
     * @param places resolves a place id {@code location}, or null when places are unavailable
     * @param liveImages holds the polar plot, or null to skip it
     */
    public SatDumpStation(String station, StreamContext ctx, RecordSink sink, PlacePoints places,
            LiveImageHolder liveImages) {
        this(station, ctx, sink, places, liveImages, null);
    }

    /**
     * Creates the station (prompt 054).
     *
     * @param station the station name
     * @param ctx the stream context
     * @param sink where records go
     * @param places resolves a place id {@code location}, or null when places are unavailable
     * @param liveImages holds the polar plot, or null to skip it
     * @param receivers where the station registers for pushed images while it runs, or null
     */
    public SatDumpStation(String station, StreamContext ctx, RecordSink sink, PlacePoints places,
            LiveImageHolder liveImages, RemoteMediaReceivers receivers) {
        this.station = station;
        this.receivers = receivers;
        this.sourceId = ctx.sourceId();
        this.ctx = ctx;
        this.sink = sink;
        this.places = places;
        this.liveImages = liveImages;
        ValidatedParams params = ctx.params();
        this.apiUrl = params.lanUrl(SatDumpDescriptor.API_URL);
        this.inboxPath = params.inboxPath(SatDumpDescriptor.INBOX_PATH);
        this.apiInterval = Objects.requireNonNullElse(params.duration(SatDumpDescriptor.API_INTERVAL),
                Duration.ofSeconds(5));
        this.scanInterval = Objects.requireNonNullElse(params.duration(SatDumpDescriptor.SCAN_INTERVAL),
                Duration.ofSeconds(30));
        this.location = StationLocation.parse(params.string(SatDumpDescriptor.LOCATION));
        this.names = SatNames.bundled().withOverrides(SatDumpDescriptor.satNames(params.string(SatDumpDescriptor.SAT_NAMES)));
        this.book = new PassBook(station, names);
        this.xrit = new XritFrames(station, names);
        this.polar = new PolarPassProcessor(station, sourceId);
        this.overlays = new PolarOverlays(station);
        this.glob = PolarGlob.orDefault(params.string(SatDumpDescriptor.POLAR_GLOB));
        this.store = new StationStore(ctx.state());
        this.log = ctx.log();
        restore();
    }

    // ------------------------------------------------------------------------- lifecycle

    /** Starts both loops; the {@code /api} loop only with an {@code apiUrl}. */
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        if (apiUrl == null) {
            emit(List.of(GroundStationRecords.record(station, GroundStationRecords.payload(station, null), ctx.now(),
                    point())));
        }
        try {
            xrit.reregister(ctx.rasters());
            reregisterPolar(ctx.rasters());
        } catch (UnsupportedOperationException e) {
            log.debug("{}: no raster pipeline in this context", sourceId);
        }
        if (receivers != null) {
            receivers.register(sourceId, this);
        }
        inboxThread = Thread.ofVirtual().name("foresignal-satdump-inbox-" + station).start(this::inboxLoop);
        if (apiUrl != null) {
            apiThread = Thread.ofVirtual().name("foresignal-satdump-api-" + station).start(this::apiLoop);
        }
        ctx.connected();
    }

    @Override
    public void close() {
        if (!running.getAndSet(false)) {
            return;
        }
        if (receivers != null) {
            receivers.unregister(sourceId, this);
        }
        for (Thread t : new Thread[] {apiThread, inboxThread}) {
            if (t != null) {
                t.interrupt();
            }
        }
        for (Thread t : new Thread[] {apiThread, inboxThread}) {
            if (t != null && t != Thread.currentThread()) {
                try {
                    t.join(2_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        synchronized (lock) {
            persist();
        }
        if (liveImages != null) {
            liveImages.clear(sourceId);
        }
    }

    @Override
    public boolean isRunning() {
        return running.get() && inboxThread != null && inboxThread.isAlive();
    }

    @Override
    public void onClose(Consumer<Throwable> callback) {
        synchronized (closeCallbacks) {
            closeCallbacks.add(callback);
        }
    }

    private void inboxLoop() {
        Throwable failure = null;
        try {
            while (running.get()) {
                try {
                    scanInboxOnce();
                } catch (RuntimeException e) {
                    log.warn("{}: inbox scan failed: {}", sourceId, e.toString());
                }
                if (!sleep(scanInterval)) {
                    break;
                }
            }
        } catch (Error e) {
            failure = e;
            throw e;
        } finally {
            if (running.get()) {
                // The loop ended on its own: the supervisor reconnects.
                running.set(false);
                notifyClosed(failure);
            }
        }
    }

    private void apiLoop() {
        while (running.get()) {
            try {
                pollApiOnce();
            } catch (RuntimeException e) {
                log.warn("{}: /api poll failed: {}", sourceId, e.toString());
            }
            if (!sleep(apiInterval)) {
                return;
            }
        }
    }

    private boolean sleep(Duration d) {
        try {
            Thread.sleep(d.toMillis());
            return running.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void notifyClosed(Throwable failure) {
        List<Consumer<Throwable>> callbacks;
        synchronized (closeCallbacks) {
            callbacks = List.copyOf(closeCallbacks);
        }
        callbacks.forEach(c -> c.accept(failure));
    }

    // ------------------------------------------------------------------------- the /api loop

    /** One {@code /api} poll, with its records emitted. No-op without an {@code apiUrl}. */
    public void pollApiOnce() {
        if (apiUrl == null) {
            return;
        }
        Instant now = ctx.now();
        String body;
        try {
            UpstreamResponse response = ctx.http().get(resolve("api"));
            body = response.bodyAsString();
        } catch (AdapterException e) {
            emit(apiFailed(now, e));
            return;
        }
        ApiSnapshot snapshot = SatDumpApiParser.parse(body);
        List<NormalizedRecord> out;
        boolean fetchPlot;
        synchronized (lock) {
            apiFailures = 0;
            ctx.state().clearFailing();
            out = observe(snapshot, now);
            fetchPlot = session != null && snapshot.autotrack()
                    && (lastPolarplot == null || !Duration.between(lastPolarplot, now).minus(POLARPLOT_PERIOD).isNegative());
            if (fetchPlot) {
                lastPolarplot = now;
            }
            updateHealth();
        }
        emit(out);
        if (fetchPlot) {
            fetchPolarplot(now);
        }
    }

    /** Applies one snapshot; returns the records to emit. Called under the lock. */
    List<NormalizedRecord> observe(ApiSnapshot snapshot, Instant now) {
        List<NormalizedRecord> out = new ArrayList<>();
        if (snapshot.driftReason() != null) {
            reasons.put("drift", snapshot.driftReason());
        } else {
            reasons.remove("drift");
        }
        Map<String, Object> payload = GroundStationRecords.payload(station, snapshot);
        if (!payload.equals(lastGroundStation)) {
            out.add(GroundStationRecords.record(station, payload, now, point()));
            lastGroundStation = payload;
        }
        boolean active = snapshot.trackingAboveHorizon() || snapshot.pipelineRunning();
        String satKey = snapshot.target() == null ? null : names.key(snapshot.target());
        if (session != null) {
            boolean targetChanged = snapshot.autotrack() && satKey != null && session.satKey() != null
                    && !satKey.equals(session.satKey());
            boolean ended;
            if (snapshot.autotrack()) {
                ended = !active || targetChanged;
            } else {
                idlePolls = active ? 0 : idlePolls + 1;
                ended = idlePolls >= LIVE_IDLE_POLLS;
            }
            if (ended) {
                out.addAll(endSession(now));
            } else {
                session.sample(now, snapshot);
                if (session.flushDue(now)) {
                    flushSession(now);
                }
            }
        }
        if (session == null && active) {
            idlePolls = 0;
            String passKey = satKey;
            session = new RfSession(book.passId(passKey, now), passKey, snapshot.target(), snapshot.mode(), now);
            session.sample(now, snapshot);
            out.add(book.open(session, point()));
            flushSession(now);
        }
        persist();
        return out;
    }

    /** LOS: closes the session at the last activity (at most one poll period before now). */
    private List<NormalizedRecord> endSession(Instant now) {
        List<NormalizedRecord> out = new ArrayList<>();
        Instant lastActivity = session.lastActivity();
        Instant los = lastActivity.plus(apiInterval).isBefore(now) ? lastActivity.plus(apiInterval) : now;
        session.close(los);
        book.close(session, point()).ifPresent(out::add);
        store.appendSession(new StationStore.SessionSummary(session.passId(), session.satKey(),
                session.start().toEpochMilli(), los.toEpochMilli(), session.aggregate()));
        store.write(StationStore.ACTIVE, null);
        session = null;
        idlePolls = 0;
        lastPolarplot = null;
        if (liveImages != null) {
            liveImages.clear(sourceId);
        }
        return out;
    }

    /** Writes the open session's ring to the adapter state. */
    private void flushSession(Instant now) {
        store.write(StationStore.ACTIVE, new StationStore.ActiveSession(session.passId(), session.satKey(),
                session.satellite(), session.mode(), session.start().toEpochMilli(), session.frequencyHz(),
                session.samples()));
        session.flushed(now);
    }

    private List<NormalizedRecord> apiFailed(Instant now, AdapterException e) {
        synchronized (lock) {
            apiFailures++;
            List<NormalizedRecord> out = new ArrayList<>();
            if (apiFailures == API_FAILURES_TO_FAIL) {
                ctx.state().fail("SatDump /api unreachable for " + API_FAILURES_TO_FAIL + " consecutive polls: "
                        + e.getMessage());
            }
            if (apiFailures >= API_FAILURES_TO_FAIL) {
                if (session != null) {
                    out.addAll(endSession(now));
                }
            }
            persist();
            return out;
        }
    }

    private void fetchPolarplot(Instant now) {
        if (liveImages == null || probing()) {
            return;
        }
        try {
            UpstreamResponse response = ctx.http().get(resolve("polarplot.jpeg"));
            synchronized (lock) {
                if (session == null) {
                    return;
                }
                liveImages.hold(sourceId, response.body(), now);
            }
        } catch (AdapterException | RuntimeException e) {
            log.debug("{}: polar plot not fetched: {}", sourceId, e.getMessage());
        }
    }

    /** {@code {apiUrl}/{path}}, keeping any base path of the URL. */
    URI resolve(String path) {
        String base = apiUrl.toString();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return URI.create(base + "/" + path);
    }

    // ------------------------------------------------------------------------- the inbox loop

    /** One inbox scan, with its records emitted. */
    public void scanInboxOnce() {
        Instant now = ctx.now();
        List<NormalizedRecord> out = new ArrayList<>();
        synchronized (lock) {
            out.addAll(book.expire(now, point()));
        }
        out.addAll(scanUploads(now));
        if (inboxPath != null) {
            try {
                ctx.chargeRequest("inbox-scan");
                out.addAll(scan(now));
            } catch (AdapterException e) {
                log.info("{}: inbox scan skipped: {}", sourceId, e.getMessage());
            } catch (IOException e) {
                log.warn("{}: inbox path {} could not be scanned: {}", sourceId, inboxPath, e.getMessage());
                synchronized (lock) {
                    reasons.put("inbox", "The inbox path " + inboxPath + " could not be scanned: " + e.getMessage());
                }
            }
        }
        synchronized (lock) {
            persist();
            updateHealth();
        }
        emit(out);
    }

    private List<NormalizedRecord> scan(Instant now) throws IOException {
        InboxAccess inbox = ctx.inbox();
        synchronized (lock) {
            reasons.remove("inbox");
            InboxScanner.Scan scan = scanner.scan(inbox, inboxPath, now);
            List<NormalizedRecord> out = new ArrayList<>();
            processReady(scan.ready(), inbox, "", now, out);
            RasterSink rasters = ctx.rasters();
            MediaSink media = ctx.media();
            out.addAll(xrit.process(scan.xrit(), inbox, rasters, media, now));
            if (xrit.reasons().isEmpty()) {
                reasons.remove("xrit");
            } else {
                reasons.put("xrit", String.join(" ", xrit.reasons()));
            }
            if (scan.truncated()) {
                log.warn("{}: the inbox walk stopped at {} entries", sourceId, InboxScanner.MAX_ENTRIES);
            }
            return out;
        }
    }

    /**
     * The remote pass bundles under {@code rasters/satdump/{station}/incoming/} (prompt 054): each upload
     * folder is ready on the scan after its atomic rename and goes through the same pass pipeline. The
     * push already paid its budget unit.
     */
    private List<NormalizedRecord> scanUploads(Instant now) {
        InboxAccess uploads;
        try {
            uploads = ctx.rasters().uploads();
        } catch (UnsupportedOperationException e) {
            return List.of();
        }
        java.nio.file.Path base = uploads.root().resolve(SatNames.slug(station)).resolve(UPLOADS_DIRECTORY);
        synchronized (lock) {
            InboxScanner.Scan scan;
            try {
                scan = uploadScanner.scan(uploads, base, now);
            } catch (IOException | RuntimeException e) {
                // No upload has arrived yet, so the directory does not exist.
                log.debug("{}: no remote uploads to scan: {}", sourceId, e.getMessage());
                return List.of();
            }
            List<NormalizedRecord> out = new ArrayList<>();
            processReady(scan.ready(), uploads, UPLOAD_PREFIX, now, out);
            return out;
        }
    }

    /** The directory of remote uploads below the station's upload area. */
    public static final String UPLOADS_DIRECTORY = "incoming";

    /** The prefix of a remote upload's dataset key in the pass book. */
    static final String UPLOAD_PREFIX = "upload:";

    /** Processes ready pass folders and updates the polar health reasons. Called under the lock. */
    private void processReady(List<InboxScanner.Folder> ready, InboxAccess access, String prefix, Instant now,
            List<NormalizedRecord> out) {
        if (ready.isEmpty()) {
            return;
        }
        Set<String> polarReasons = new LinkedHashSet<>();
        for (InboxScanner.Folder folder : ready) {
            out.addAll(processPass(folder, access, prefix, now, polarReasons));
            polarReasons.addAll(polar.reasons());
        }
        if (polarReasons.isEmpty()) {
            reasons.remove("polar");
        } else {
            reasons.put("polar", String.join(" ", polarReasons));
        }
    }

    /**
     * Joins, stores and records one ready pass folder: composites and thumbnails, provenance copies, polar
     * overlays and the pass geometry (prompt 054). Called under the lock.
     */
    private List<NormalizedRecord> processPass(InboxScanner.Folder folder, InboxAccess access, String prefix,
            Instant now, Set<String> polarReasons) {
        InboxScanner marker = prefix.isEmpty() ? scanner : uploadScanner;
        PolarPassProcessor.Contents contents;
        try {
            contents = polar.read(folder, access);
        } catch (SatDumpFiles.FormatException | IOException | RuntimeException e) {
            log.warn("{}: pass folder {}{} skipped: {}", sourceId, prefix, folder.relative(), e.getMessage());
            marker.markProcessed(folder.relative(), folder.signature());
            return List.of();
        }
        SatDumpFiles.Dataset dataset = contents.dataset();
        String satKey = names.key(dataset.satellite());
        PassState pass = book.join(satKey, dataset.satellite(), dataset.timestamp(), now);
        List<Map<String, Object>> products = polar.storeComposites(folder, contents, pass.externalId, access, ctx.media());
        RasterSink rasters = ctx.rasters();
        polar.copyProvenance(folder, pass.externalId, access, rasters);
        List<NormalizedRecord> out = new ArrayList<>();
        try {
            PolarOverlays.Result result = overlays.process(folder, contents, pass, glob, access, rasters);
            out.addAll(result.records());
            polarReasons.addAll(result.reasons());
            for (PolarOverlays.Overlay overlay : result.overlays()) {
                polarProducts.put(overlay.productId(), overlay.composite());
            }
            products = PolarOverlays.merge(products, result.overlays());
            book.geometry(pass, result.geometry(), result.geometrySource());
        } catch (RuntimeException e) {
            log.warn("{}: polar overlays of {}{} failed: {}", sourceId, prefix, folder.relative(), e.toString());
        }
        out.add(book.decoded(pass, prefix + folder.relative(), contents.instruments(), products, contents.tle(), now,
                point()));
        marker.markProcessed(folder.relative(), folder.signature());
        return out;
    }

    /** Registers the polar overlay products again after a restart (dynamic products are not reloaded). */
    private void reregisterPolar(RasterSink rasters) {
        for (Map.Entry<String, String> e : Map.copyOf(polarProducts).entrySet()) {
            try {
                rasters.registerProduct(PolarOverlays.spec(e.getKey(), station, e.getValue()));
            } catch (AdapterException ex) {
                log.warn("{}: polar product {} not registered again: {}", sourceId, e.getKey(), ex.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------------- pushed images

    /**
     * Stores one image pushed to {@code POST /v1/ingest/{sourceId}/media} (prompt 054): the image guard
     * (through {@code ctx.media().decode}), the media store with 256 and 1024 px thumbnails, owned by the
     * pass it attaches to; that pass is {@code passId} when the book knows it, else the one the 049 join
     * rule finds, else a pass minted from the image. The pass's {@code decoded} version is emitted.
     *
     * @throws AdapterException when the image cannot be stored
     */
    @Override
    public Accepted receive(Upload upload) throws AdapterException {
        MediaSink media = ctx.media();
        media.decode(upload.bytes(), upload.contentType());
        Instant now = ctx.now();
        NormalizedRecord record;
        MediaRef ref;
        String passId;
        synchronized (lock) {
            PassState pass = upload.passId() == null ? null : book.find(upload.passId()).orElse(null);
            if (pass == null) {
                pass = book.join(names.key(upload.satellite()), upload.satellite(), upload.timestamp(), now);
            }
            passId = pass.externalId;
            // A refusal (ImageRejectedException) or the size cap (413) propagates to the endpoint as is.
            ref = media.store(MediaInput.of(upload.bytes(), upload.contentType(), MediaRole.IMAGE)
                    .withRecord(sourceId, passId)
                    .withLicence(SatDumpDescriptor.LICENSE));
            MediaRef thumb = null;
            for (MediaRef t : media.thumbnails(ref)) {
                if (thumb == null || Math.max(t.width(), t.height()) < Math.max(thumb.width(), thumb.height())) {
                    thumb = t;
                }
            }
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("composite", upload.composite());
            if (ref.id() != null) {
                entry.put("mediaId", ref.id());
            }
            if (thumb != null && thumb.id() != null) {
                entry.put("thumbMediaId", thumb.id());
            }
            record = book.decoded(pass, REMOTE_MEDIA_PREFIX + upload.composite(), List.of(), List.of(entry), null, now,
                    point());
            persist();
        }
        emit(List.of(record));
        return new Accepted(ref.id(), ref.sha256(), passId);
    }

    /** The prefix of a pushed image's dataset key in the pass book. */
    static final String REMOTE_MEDIA_PREFIX = "remote-media:";

    // ------------------------------------------------------------------------- shared

    private void updateHealth() {
        String missing = book.missingDatasetReason();
        if (missing == null) {
            reasons.remove("dataset");
        } else {
            reasons.put("dataset", missing);
        }
        if (reasons.isEmpty()) {
            ctx.state().clearDegraded();
        } else {
            ctx.state().degrade(String.join(" ", reasons.values()));
        }
    }

    /** The current health reasons, for tests. */
    Map<String, String> reasons() {
        synchronized (lock) {
            return Map.copyOf(reasons);
        }
    }

    /** The open RF session, for tests. */
    RfSession session() {
        synchronized (lock) {
            return session;
        }
    }

    /** The pass book, for tests. */
    PassBook book() {
        return book;
    }

    private double[] point() {
        return location.point(places);
    }

    private boolean probing() {
        try {
            return ctx.media().probing();
        } catch (UnsupportedOperationException e) {
            return false;
        }
    }

    private void emit(List<NormalizedRecord> records) {
        for (NormalizedRecord record : records) {
            sink.accept(record);
        }
    }

    private void restore() {
        book.restore(store.passes());
        scanner.restore(store.watches(), store.processed());
        uploadScanner.restore(store.uploadWatches(), store.uploadProcessed());
        polarProducts.putAll(store.polarProducts());
        xrit.restore(store.xritFolders(), store.xritProducts());
        StationStore.ActiveSession active = store.active();
        if (active != null) {
            RfSession restored = new RfSession(active.passId(), active.satKey(), active.satellite(), active.mode(),
                    Instant.ofEpochMilli(active.start()));
            if (active.samples() != null) {
                active.samples().forEach(restored::restore);
            }
            session = restored;
        }
    }

    private void persist() {
        store.write(StationStore.PASSES, book.passes());
        store.write(StationStore.WATCHES, scanner.watches());
        store.write(StationStore.PROCESSED, scanner.processed());
        store.write(StationStore.UPLOAD_WATCHES, uploadScanner.watches());
        store.write(StationStore.UPLOAD_PROCESSED, uploadScanner.processed());
        store.write(StationStore.POLAR_PRODUCTS, polarProducts);
        store.write(StationStore.XRIT_FOLDERS, xrit.folders());
        store.write(StationStore.XRIT_PRODUCTS, xrit.products());
    }
}
