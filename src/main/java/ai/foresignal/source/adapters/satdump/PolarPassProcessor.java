package ai.foresignal.source.adapters.satdump;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.InboxAccess;
import ai.foresignal.source.spi.MediaInput;
import ai.foresignal.source.spi.MediaRef;
import ai.foresignal.source.spi.MediaRole;
import ai.foresignal.source.spi.MediaSink;
import ai.foresignal.source.spi.RasterSink;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads a ready polar pass folder (prompt 049, Weather-Imagery-Design §4.3 and §4.6).
 *
 * <p>Parses {@code dataset.json} and each product's {@code product.cbor}; every composite image goes
 * through {@code GuardedImages} ({@link MediaSink#decode}), then {@code ctx.media()} and the 256 and
 * 1024 px thumbnails. The original images and the provenance files ({@code dataset.json},
 * {@code product.cbor}, any {@code .tif}) are copied to {@code rasters/satdump/{station}/{passId}/};
 * only those extensions are ever copied, so {@code .cadu} and baseband files never are. Polar GeoTIFF
 * overlays and footprints belong to prompt 054.
 *
 * <p>A composite is an image in a product directory that {@code product.cbor} does not list as a
 * channel. Not thread-safe; the station synchronizes.
 */
public final class PolarPassProcessor {

    private static final Logger log = LoggerFactory.getLogger(PolarPassProcessor.class);

    /** The health reason of a BigTIFF in a pass folder (Weather-Imagery-Design §4.3). */
    public static final String BIGTIFF_REASON = "BigTIFF not supported.";

    /** The extensions copied as provenance; nothing else is ever copied. */
    public static final Set<String> COPIED = Set.of("json", "cbor", "png", "jpg", "jpeg", "tif", "tiff");

    /** The largest {@code dataset.json} read. */
    public static final long MAX_DATASET_BYTES = 1024 * 1024;

    /** The largest provenance file copied. */
    public static final long MAX_COPY_BYTES = 64L * 1024 * 1024;

    /** The most bytes copied for one pass. */
    public static final long MAX_PASS_BYTES = 512L * 1024 * 1024;

    /** The most composites stored per pass. */
    public static final int MAX_COMPOSITES = 24;

    /**
     * What a pass folder held.
     *
     * @param dataset the parsed {@code dataset.json}
     * @param products the parsed products with their directories
     */
    public record Contents(SatDumpFiles.Dataset dataset, List<ProductDir> products) {

        /** The instruments, in order, without repeats. */
        public List<String> instruments() {
            Set<String> out = new LinkedHashSet<>();
            products.forEach(p -> out.add(p.product().instrument()));
            return List.copyOf(out);
        }

        /** The first TLE any product carries, or null. */
        public Map<String, Object> tle() {
            for (ProductDir p : products) {
                Map<String, Object> tle = p.product().tle();
                if (tle != null) {
                    return tle;
                }
            }
            return null;
        }
    }

    /**
     * One product of a pass.
     *
     * @param relative the product directory relative to the pass folder
     * @param directory the product directory
     * @param product the parsed {@code product.cbor}
     */
    public record ProductDir(String relative, Path directory, SatDumpFiles.Product product) {}

    private final String station;
    private final String sourceId;
    private final Set<String> reasons = new LinkedHashSet<>();

    /**
     * Creates the processor.
     *
     * @param station the station name
     * @param sourceId the source id that owns the stored media
     */
    public PolarPassProcessor(String station, String sourceId) {
        this.station = station;
        this.sourceId = sourceId;
    }

    /** The health reasons of the last pass processed (BigTIFF). */
    public Set<String> reasons() {
        return java.util.Collections.unmodifiableSet(new LinkedHashSet<>(reasons));
    }

    /**
     * Reads the folder's {@code dataset.json} and products.
     *
     * @throws SatDumpFiles.FormatException when {@code dataset.json} is unreadable
     * @throws IOException when it cannot be read
     */
    public Contents read(InboxScanner.Folder folder, InboxAccess inbox)
            throws SatDumpFiles.FormatException, IOException {
        SatDumpFiles.Dataset dataset = SatDumpFiles.dataset(
                inbox.readAll(folder.path().resolve(InboxScanner.DATASET), MAX_DATASET_BYTES));
        List<ProductDir> products = new ArrayList<>();
        for (String relative : dataset.products()) {
            Path dir = folder.path().resolve(relative).normalize();
            if (!dir.startsWith(folder.path())) {
                log.warn("{}: product directory '{}' leaves the pass folder {}; ignored", station, relative,
                        folder.relative());
                continue;
            }
            try {
                byte[] cbor = inbox.readAll(dir.resolve(InboxScanner.PRODUCT), XritFrames.MAX_CBOR_BYTES);
                products.add(new ProductDir(relative, dir, SatDumpFiles.product(cbor)));
            } catch (SatDumpFiles.FormatException | IOException | RuntimeException e) {
                log.warn("{}: product {} of {} skipped: {}", station, relative, folder.relative(), e.getMessage());
            }
        }
        return new Contents(dataset, products);
    }

    /**
     * Stores the composites as media owned by the pass record and returns the pass's product entries
     * ({@code instrument}, {@code composite}, {@code mediaId}, {@code thumbMediaId}).
     */
    public List<Map<String, Object>> storeComposites(InboxScanner.Folder folder, Contents contents, String passId,
            InboxAccess inbox, MediaSink media) {
        List<Map<String, Object>> out = new ArrayList<>();
        int stored = 0;
        for (ProductDir product : contents.products()) {
            Set<String> channels = new LinkedHashSet<>();
            product.product().images().forEach(i -> channels.add(i.file()));
            for (InboxAccess.FileStat file : filesIn(folder, product.directory())) {
                String name = file.path().getFileName().toString();
                String ext = extension(name);
                if (!(ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg")) || channels.contains(name)) {
                    continue;
                }
                if (stored >= MAX_COMPOSITES) {
                    break;
                }
                try {
                    byte[] bytes = inbox.readAll(file.path(), MAX_COPY_BYTES);
                    String type = XritFrames.contentType(name);
                    media.decode(bytes, type);
                    MediaRef ref = media.store(MediaInput.of(bytes, type, MediaRole.IMAGE)
                            .withRecord(sourceId, passId)
                            .withLicence(SatDumpDescriptor.LICENSE));
                    MediaRef thumb = smallest(media.thumbnails(ref));
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("instrument", product.product().instrument());
                    entry.put("composite", name);
                    if (ref.id() != null) {
                        entry.put("mediaId", ref.id());
                    }
                    if (thumb != null && thumb.id() != null) {
                        entry.put("thumbMediaId", thumb.id());
                    }
                    out.add(entry);
                    stored++;
                } catch (IOException | RuntimeException e) {
                    log.warn("{}: composite {} of {} not stored: {}", station, name, folder.relative(), e.getMessage());
                }
            }
        }
        return out;
    }

    /**
     * Copies the provenance files to {@code rasters/satdump/{station}/{passId}/}: {@code dataset.json},
     * every {@code product.cbor}, the original images and any {@code .tif}. A BigTIFF is skipped with
     * {@link #BIGTIFF_REASON}.
     *
     * @return how many files were copied
     */
    public int copyProvenance(InboxScanner.Folder folder, String passId, InboxAccess inbox, RasterSink rasters) {
        reasons.clear();
        int copied = 0;
        long total = 0;
        String base = SatNames.slug(station) + "/" + passDirectory(passId) + "/";
        List<InboxAccess.FileStat> files = new ArrayList<>(folder.files());
        files.sort(Comparator.comparing(f -> f.path().toString()));
        for (InboxAccess.FileStat file : files) {
            String name = file.path().getFileName().toString();
            String ext = extension(name);
            if (!COPIED.contains(ext)) {
                continue;
            }
            if (file.size() > MAX_COPY_BYTES || total + file.size() > MAX_PASS_BYTES) {
                log.warn("{}: {} not copied: over the provenance size limit", station, name);
                continue;
            }
            try {
                byte[] bytes = inbox.readAll(file.path(), MAX_COPY_BYTES);
                if ((ext.equals("tif") || ext.equals("tiff")) && bigTiff(bytes)) {
                    reasons.add(BIGTIFF_REASON);
                    continue;
                }
                String relative = InboxScanner.relative(folder.path(), file.path());
                rasters.storeProvenance(base + relative, bytes);
                copied++;
                total += bytes.length;
            } catch (IOException | AdapterException | RuntimeException e) {
                log.warn("{}: provenance file {} not copied: {}", station, name, e.getMessage());
            }
        }
        return copied;
    }

    /** True for a BigTIFF header ({@code II+\0} or {@code MM\0+}). */
    static boolean bigTiff(byte[] head) {
        if (head.length < 4) {
            return false;
        }
        return (head[0] == 'I' && head[1] == 'I' && head[2] == 0x2B && head[3] == 0)
                || (head[0] == 'M' && head[1] == 'M' && head[2] == 0 && head[3] == 0x2B);
    }

    /** The pass id as a directory name ({@code :} is not portable). */
    static String passDirectory(String passId) {
        return passId.replace(':', '_');
    }

    private static List<InboxAccess.FileStat> filesIn(InboxScanner.Folder folder, Path directory) {
        List<InboxAccess.FileStat> out = new ArrayList<>();
        for (InboxAccess.FileStat f : folder.files()) {
            if (directory.equals(f.path().getParent())) {
                out.add(f);
            }
        }
        out.sort(Comparator.comparing(f -> f.path().getFileName().toString()));
        return out;
    }

    private static MediaRef smallest(List<MediaRef> thumbs) {
        MediaRef best = null;
        for (MediaRef t : thumbs) {
            if (best == null || Math.max(t.width(), t.height()) < Math.max(best.width(), best.height())) {
                best = t;
            }
        }
        return best;
    }

    static String extension(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
