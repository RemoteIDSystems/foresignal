package ai.foresignal.source.adapters.satdump;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.FrameInput;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.InboxAccess;
import ai.foresignal.source.spi.MediaSink;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import ai.foresignal.source.spi.RasterSink;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Turns SatDump xRIT timestamp folders into geostationary frames (prompt 049, Weather-Imagery-Design
 * §4.3 and §4.5 item 1): GOES-19/18 HRIT and Elektro-L (GK-2A only with legitimately obtained KMA keys).
 *
 * <p>A folder {@code IMAGES/{satellite}/[{region}/]{YYYY-MM-DD_HH-MM-SS}/} produces a {@code partial}
 * frame for each channel as soon as that channel's image validates, and the frames become
 * {@code complete} after {@value #QUIET_SECONDS} s without changes, or as soon as a newer timestamp
 * folder appears for the same satellite and region; each completion emits the frame's
 * {@code raster-frame} record. SatDump rewrites {@code product.cbor} as channels arrive, so a parse
 * error is retried on the next scan. Each channel is a dynamic product
 * ({@link XritProducts#productId}) whose cadence is the observed median interval.
 *
 * <p>Not thread-safe; the station synchronizes.
 */
public final class XritFrames {

    private static final Logger log = LoggerFactory.getLogger(XritFrames.class);

    /** Quiet time after which a folder's frames are complete. */
    public static final int QUIET_SECONDS = 120;

    /** The largest channel image read. */
    public static final long MAX_IMAGE_BYTES = 64L * 1024 * 1024;

    /** The largest {@code product.cbor} read. */
    public static final long MAX_CBOR_BYTES = 16L * 1024 * 1024;

    /** How long finished folders are remembered. */
    public static final Duration KEEP = XritProducts.LADDER;

    /** Timestamps kept per satellite and region for the cadence. */
    public static final int CADENCE_SAMPLES = 12;

    static final DateTimeFormatter FOLDER_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /** One folder's progress, persisted. */
    public static final class FolderState {

        /** The folder relative to the inbox path. */
        public String relative;

        /** The satellite name from the path. */
        public String satellite;

        /** The satellite key. */
        public String satKey;

        /** The region from the path, or null. */
        public String region;

        /** The folder time (epoch milliseconds). */
        public long time;

        /** {@code pending}, {@code partial} or {@code complete}. */
        public String status = "pending";

        /** Channel name to the {@code size|mtime} of the image committed for it. */
        public Map<String, String> channels = new LinkedHashMap<>();

        /** Channel name to its product id. */
        public Map<String, String> products = new LinkedHashMap<>();

        /** Why a channel of this folder was refused (FY-4, MSG HRV, no projection), or null. */
        public String reason;
    }

    /** One registered product, persisted so it is registered again when the stream restarts. */
    public static final class ProductState {

        /** The product id. */
        public String id;

        /** The satellite's display name. */
        public String satellite;

        /** The region, or null. */
        public String region;

        /** The channel. */
        public String channel;

        /** The sample type's wire value. */
        public String dtype;

        /** The bit depth, or null. */
        public Integer bitDepth;

        /** The cadence in seconds, or null. */
        public Long cadenceSeconds;
    }

    private final String station;
    private final SatNames names;
    private final Map<String, FolderState> folders = new LinkedHashMap<>();
    private final Map<String, ProductState> products = new LinkedHashMap<>();
    private final Map<String, List<Long>> times = new LinkedHashMap<>();
    private final Set<String> reasons = new LinkedHashSet<>();

    /**
     * Creates the tracker.
     *
     * @param station the station name
     * @param names the satellite name table
     */
    public XritFrames(String station, SatNames names) {
        this.station = station;
        this.names = names;
    }

    /** The folder states, for persistence. */
    public Map<String, FolderState> folders() {
        return new LinkedHashMap<>(folders);
    }

    /** The registered products, for persistence. */
    public Map<String, ProductState> products() {
        return new LinkedHashMap<>(products);
    }

    /** Restores the persisted state. */
    public void restore(Map<String, FolderState> savedFolders, Map<String, ProductState> savedProducts) {
        folders.clear();
        products.clear();
        times.clear();
        if (savedFolders != null) {
            folders.putAll(savedFolders);
            for (FolderState f : savedFolders.values()) {
                times.computeIfAbsent(key(f.satKey, f.region), k -> new ArrayList<>()).add(f.time / 1000);
            }
        }
        if (savedProducts != null) {
            products.putAll(savedProducts);
        }
    }

    /**
     * Registers every remembered product again (the catalog does not reload dynamic products on
     * restart).
     */
    public void reregister(RasterSink rasters) {
        for (ProductState p : products.values()) {
            try {
                rasters.registerProduct(spec(p));
            } catch (AdapterException | RuntimeException e) {
                log.warn("{}: could not register product {} again: {}", station, p.id, e.getMessage());
            }
        }
    }

    /** The health reasons found by the last {@link #process} (FY-4 and MSG HRV, missing projections). */
    public Set<String> reasons() {
        return java.util.Collections.unmodifiableSet(new LinkedHashSet<>(reasons));
    }

    /**
     * Processes the xRIT folders of one scan.
     *
     * @param scanned every xRIT folder present
     * @param inbox the inbox
     * @param rasters the raster pipeline
     * @param media the media sink, for guarded decoding
     * @param now the scan time
     * @return the {@code raster-frame} records of frames completed by this scan
     */
    public List<NormalizedRecord> process(List<InboxScanner.Folder> scanned, InboxAccess inbox, RasterSink rasters,
            MediaSink media, Instant now) {
        reasons.clear();
        List<NormalizedRecord> out = new ArrayList<>();
        Map<String, Long> newest = new LinkedHashMap<>();
        List<InboxScanner.Folder> ordered = new ArrayList<>();
        for (InboxScanner.Folder folder : scanned) {
            Parsed parsed = parse(folder.relative());
            if (parsed == null) {
                continue;
            }
            ordered.add(folder);
            newest.merge(key(names.key(parsed.satellite), parsed.region), parsed.time.toEpochMilli(), Math::max);
            FolderState state = folders.computeIfAbsent(folder.relative(), r -> {
                FolderState created = new FolderState();
                created.relative = r;
                created.satellite = parsed.satellite;
                created.satKey = names.key(parsed.satellite);
                created.region = parsed.region;
                created.time = parsed.time.toEpochMilli();
                List<Long> list = times.computeIfAbsent(key(created.satKey, created.region), k -> new ArrayList<>());
                list.add(created.time / 1000);
                while (list.size() > CADENCE_SAMPLES) {
                    list.remove(0);
                }
                return created;
            });
            if (!"complete".equals(state.status)) {
                partials(folder, state, inbox, rasters, media);
            }
            if (state.reason != null) {
                // Kept while the folder is there, so the health reason does not flap between scans.
                reasons.add(state.reason);
            }
        }
        ordered.sort(Comparator.comparing(InboxScanner.Folder::relative));
        for (InboxScanner.Folder folder : ordered) {
            FolderState state = folders.get(folder.relative());
            if (state == null || !"partial".equals(state.status)) {
                continue;
            }
            boolean quiet = Duration.between(folder.lastChange(), now).toSeconds() >= QUIET_SECONDS;
            boolean superseded = newest.getOrDefault(key(state.satKey, state.region), state.time) > state.time;
            if (quiet || superseded) {
                out.addAll(complete(folder, state, inbox, rasters, media));
            }
        }
        long cutoff = now.minus(KEEP).toEpochMilli();
        folders.values().removeIf(f -> f.time < cutoff);
        return out;
    }

    /** Commits a partial frame for every channel that is new or changed. */
    private void partials(InboxScanner.Folder folder, FolderState state, InboxAccess inbox, RasterSink rasters,
            MediaSink media) {
        SatDumpFiles.Product product = readProduct(folder, inbox);
        if (product == null) {
            return;
        }
        for (SatDumpFiles.Image image : product.images()) {
            InboxAccess.FileStat stat = stat(folder, image.file());
            if (stat == null) {
                continue;
            }
            String signature = stat.size() + "|" + stat.modified().toEpochMilli();
            if (signature.equals(state.channels.get(image.name()))) {
                continue;
            }
            try {
                commit(folder, state, product, image, stat.path(), inbox, rasters, media, true);
                state.channels.put(image.name(), signature);
                state.status = "partial";
            } catch (XritGeometry.NoProjectionException e) {
                state.reason = e.getMessage();
                // Remember it so the refused channel is not decoded again every scan.
                state.channels.put(image.name(), signature);
            } catch (AdapterException | IOException | RuntimeException e) {
                log.debug("{}: channel {} of {} not committed yet: {}", station, image.name(), folder.relative(),
                        e.getMessage());
            }
        }
        if ("pending".equals(state.status) && !state.channels.isEmpty() && state.products.isEmpty()) {
            // Every channel so far was refused: nothing will ever complete.
            state.status = "complete";
        }
    }

    /** Commits every validated channel as complete and returns their records. */
    private List<NormalizedRecord> complete(InboxScanner.Folder folder, FolderState state, InboxAccess inbox,
            RasterSink rasters, MediaSink media) {
        List<NormalizedRecord> out = new ArrayList<>();
        SatDumpFiles.Product product = readProduct(folder, inbox);
        if (product == null) {
            return out;
        }
        for (SatDumpFiles.Image image : product.images()) {
            if (!state.products.containsKey(image.name())) {
                continue;
            }
            InboxAccess.FileStat stat = stat(folder, image.file());
            if (stat == null) {
                continue;
            }
            try {
                NormalizedRecord record = commit(folder, state, product, image, stat.path(), inbox, rasters, media, false);
                if (record != null) {
                    out.add(record);
                }
            } catch (XritGeometry.NoProjectionException e) {
                state.reason = e.getMessage();
            } catch (AdapterException | IOException | RuntimeException e) {
                log.warn("{}: could not complete channel {} of {}: {}", station, image.name(), folder.relative(),
                        e.getMessage());
            }
        }
        state.status = "complete";
        return out;
    }

    private NormalizedRecord commit(InboxScanner.Folder folder, FolderState state, SatDumpFiles.Product product,
            SatDumpFiles.Image image, Path file, InboxAccess inbox, RasterSink rasters, MediaSink media, boolean partial)
            throws AdapterException, IOException, XritGeometry.NoProjectionException {
        byte[] bytes = inbox.readAll(file, MAX_IMAGE_BYTES);
        BufferedImage decoded = media.decode(bytes, contentType(image.file()));
        GridDefinition grid = XritGeometry.grid(state.satellite, image.name(), product.projection(), decoded.getWidth(),
                decoded.getHeight());
        RasterDtype dtype = XritProducts.dtype(decoded);
        DecodedBands bands = XritProducts.bands(decoded, dtype, image.bitDepth());
        String productId = XritProducts.productId(station, state.satKey, state.region, image.name());
        ProductState registered = products.get(productId);
        ProductState wanted = productState(productId, state, image, dtype);
        if (registered == null || !same(registered, wanted)) {
            rasters.registerProduct(spec(wanted));
            products.put(productId, wanted);
        }
        state.products.put(image.name(), productId);
        if (rasters.probing()) {
            return null;
        }
        FrameInput frame = FrameInput.builder(productId)
                .validTime(Instant.ofEpochMilli(state.time))
                .grid(grid)
                .bands(bands)
                .partial(partial)
                .upstreamKey(folder.relative() + "/" + image.file())
                .variable(image.name())
                .footprint(XritGeometry.footprint(grid))
                .build();
        return rasters.commit(frame);
    }

    private ProductState productState(String id, FolderState state, SatDumpFiles.Image image, RasterDtype dtype) {
        ProductState p = new ProductState();
        p.id = id;
        p.satellite = names.displayName(state.satKey, state.satellite);
        p.region = state.region;
        p.channel = image.name();
        p.dtype = dtype.wireValue();
        p.bitDepth = image.bitDepth();
        Duration cadence = XritProducts.medianInterval(times.getOrDefault(key(state.satKey, state.region), List.of()));
        p.cadenceSeconds = cadence == null ? null : cadence.toSeconds();
        return p;
    }

    private static boolean same(ProductState a, ProductState b) {
        return java.util.Objects.equals(a.dtype, b.dtype) && java.util.Objects.equals(a.bitDepth, b.bitDepth)
                && java.util.Objects.equals(a.cadenceSeconds, b.cadenceSeconds)
                && java.util.Objects.equals(a.satellite, b.satellite);
    }

    /** The spec of a remembered product. */
    static ProductSpec spec(ProductState p) {
        String region = p.region == null ? "full disk" : p.region;
        String title = p.satellite + " " + region + " " + p.channel + " (SatDump)";
        return XritProducts.spec(p.id, title, RasterDtype.fromWire(p.dtype), p.bitDepth,
                p.cadenceSeconds == null ? null : Duration.ofSeconds(p.cadenceSeconds), p.satellite);
    }

    private SatDumpFiles.Product readProduct(InboxScanner.Folder folder, InboxAccess inbox) {
        Path cbor = folder.path().resolve(InboxScanner.PRODUCT);
        try {
            return SatDumpFiles.product(inbox.readAll(cbor, MAX_CBOR_BYTES));
        } catch (SatDumpFiles.FormatException | IOException | RuntimeException e) {
            // SatDump rewrites product.cbor as channels arrive: retry on the next scan.
            log.debug("{}: {} not readable yet: {}", station, folder.relative(), e.getMessage());
            return null;
        }
    }

    private static InboxAccess.FileStat stat(InboxScanner.Folder folder, String file) {
        if (file.contains("..") || file.startsWith("/")) {
            return null;
        }
        Path wanted = folder.path().resolve(file).normalize();
        for (InboxAccess.FileStat f : folder.files()) {
            if (f.path().normalize().equals(wanted)) {
                return f;
            }
        }
        return null;
    }

    static String contentType(String file) {
        String lower = file.toLowerCase(java.util.Locale.ROOT);
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".tif") || lower.endsWith(".tiff")) {
            return "image/tiff";
        }
        return "image/png";
    }

    private static String key(String satKey, String region) {
        return satKey + "/" + (region == null ? "" : region);
    }

    /** The parts of an xRIT folder path. */
    record Parsed(String satellite, String region, Instant time) {}

    /**
     * Parses {@code .../IMAGES/{satellite}/[{region}/]{timestamp}}; null when the path does not have
     * that shape.
     */
    static Parsed parse(String relative) {
        String[] parts = relative.split("/");
        int images = -1;
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals(InboxScanner.IMAGES)) {
                images = i;
            }
        }
        if (images < 0) {
            return null;
        }
        int rest = parts.length - images - 1;
        if (rest != 2 && rest != 3) {
            return null;
        }
        Instant time = folderTime(parts[parts.length - 1]);
        if (time == null) {
            return null;
        }
        String satellite = parts[images + 1];
        String region = rest == 3 ? parts[images + 2] : null;
        return new Parsed(satellite, region, time);
    }

    /** {@code 2026-10-10_14-30-00} as UTC, or null. */
    static Instant folderTime(String name) {
        try {
            return LocalDateTime.parse(name, FOLDER_TIME).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
