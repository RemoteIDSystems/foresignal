package ai.foresignal.source.adapters.satdump;

import ai.foresignal.source.spi.InboxAccess;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Walks a SatDump output directory under the inbox and decides which folders are ready (prompt 049,
 * Weather-Imagery-Design §4.3).
 *
 * <p>Every scan is a full walk through {@link InboxAccess}, so every directory and file is re-checked
 * against the inbox root and links are skipped; a {@code WatchService} event is only a hint to scan
 * sooner, because inotify is unreliable on NFS, SMB and bind mounts. Two kinds of folder are found:
 *
 * <ul>
 *   <li><b>Polar pass folders</b>: a directory holding {@code dataset.json} (outside any
 *       {@code IMAGES} tree). Its signature covers every file below it except {@code IMAGES} trees; it is
 *       <em>ready</em> once the signature (every size and modification time) has been unchanged for
 *       {@value #STABLE_SCANS} scans and differs from the one last processed.
 *   <li><b>xRIT timestamp folders</b>: {@code IMAGES/{satellite}/[{region}/]{YYYY-MM-DD_HH-MM-SS}/}
 *       holding {@code product.cbor}. They are reported on every scan with their signature and last
 *       change; {@link XritFrames} decides partial and complete.
 * </ul>
 *
 * <p>The walk is bounded ({@value #MAX_DEPTH} levels, {@value #MAX_ENTRIES} entries) so a huge or
 * looping tree cannot stall the loop. Not thread-safe; the station synchronizes.
 */
public final class InboxScanner {

    /** Unchanged scans before a pass folder is ready. */
    public static final int STABLE_SCANS = 2;

    /** The deepest directory level walked below the inbox path. */
    public static final int MAX_DEPTH = 8;

    /** The most directories and files looked at in one scan. */
    public static final int MAX_ENTRIES = 50_000;

    /** The most processed folders remembered. */
    public static final int MAX_PROCESSED = 2_000;

    /** The marker of a polar pass folder. */
    public static final String DATASET = "dataset.json";

    /** The marker of an xRIT timestamp folder. */
    public static final String PRODUCT = "product.cbor";

    /** The directory under which xRIT frames are written. */
    public static final String IMAGES = "IMAGES";

    /**
     * What the scanner remembers about one folder between scans.
     *
     * @param signature the folder signature at the last scan
     * @param stableScans how many scans in a row it has been unchanged
     * @param lastChange when it last changed (epoch milliseconds)
     */
    public record Watch(String signature, int stableScans, long lastChange) {}

    /**
     * One folder found by a scan.
     *
     * @param relative the folder relative to the inbox path, with {@code /} separators
     * @param path the folder
     * @param signature its current signature
     * @param lastChange when its signature last changed
     * @param files its files with sizes and modification times
     */
    public record Folder(String relative, Path path, String signature, Instant lastChange,
            List<InboxAccess.FileStat> files) {}

    /**
     * The result of one scan.
     *
     * @param ready polar pass folders ready to process
     * @param xrit every xRIT timestamp folder present
     * @param truncated true when the walk hit {@value #MAX_ENTRIES}
     */
    public record Scan(List<Folder> ready, List<Folder> xrit, boolean truncated) {}

    private final Map<String, Watch> watches = new LinkedHashMap<>();
    private final Map<String, String> processed = new LinkedHashMap<>();
    private final int stableScans;

    /** A scanner for SatDump's output directory: a pass folder is ready after {@value #STABLE_SCANS} unchanged scans. */
    public InboxScanner() {
        this(STABLE_SCANS);
    }

    /**
     * A scanner whose pass folders are ready after {@code stableScans} unchanged scans; 0 for the remote
     * upload area (prompt 054), where an upload appears complete in one atomic rename.
     */
    public InboxScanner(int stableScans) {
        this.stableScans = stableScans;
    }

    /** The watches, for persistence. */
    public Map<String, Watch> watches() {
        return Map.copyOf(watches);
    }

    /** The processed folder signatures, for persistence. */
    public Map<String, String> processed() {
        return new LinkedHashMap<>(processed);
    }

    /** Restores the persisted watches and processed signatures. */
    public void restore(Map<String, Watch> savedWatches, Map<String, String> savedProcessed) {
        watches.clear();
        processed.clear();
        if (savedWatches != null) {
            watches.putAll(savedWatches);
        }
        if (savedProcessed != null) {
            processed.putAll(savedProcessed);
        }
    }

    /** Records that a pass folder was processed at {@code signature}, so it is not processed again. */
    public void markProcessed(String relative, String signature) {
        processed.remove(relative);
        processed.put(relative, signature);
        while (processed.size() > MAX_PROCESSED) {
            String oldest = processed.keySet().iterator().next();
            processed.remove(oldest);
        }
    }

    /**
     * Scans {@code base}.
     *
     * @param inbox the inbox access, which re-checks every path
     * @param base the station's inbox path
     * @param now the scan time
     * @throws IOException when {@code base} itself cannot be listed
     */
    public Scan scan(InboxAccess inbox, Path base, Instant now) throws IOException {
        Path root = inbox.requireInside(base);
        Walk walk = new Walk(inbox, root);
        walk.directory(root, 0, false);
        List<Folder> ready = new ArrayList<>();
        List<Folder> xrit = new ArrayList<>();
        Set<String> seen = new java.util.HashSet<>();
        for (Found found : walk.found) {
            String relative = relative(root, found.path);
            seen.add(relative);
            List<InboxAccess.FileStat> files = found.xrit ? walk.files(found.path, false)
                    : walk.files(found.path, true);
            String signature = signature(root, files);
            Watch previous = watches.get(relative);
            Watch current;
            if (previous != null && previous.signature().equals(signature)) {
                current = new Watch(signature, previous.stableScans() + 1, previous.lastChange());
            } else {
                current = new Watch(signature, 0, now.toEpochMilli());
            }
            watches.put(relative, current);
            Folder folder = new Folder(relative, found.path, signature, Instant.ofEpochMilli(current.lastChange()), files);
            if (found.xrit) {
                xrit.add(folder);
            } else if (current.stableScans() >= stableScans && !signature.equals(processed.get(relative))) {
                ready.add(folder);
            }
        }
        watches.keySet().retainAll(seen);
        return new Scan(ready, xrit, walk.truncated);
    }

    /** The signature of a set of files: SHA-256 over sorted {@code path|size|mtime} lines, 16 hex. */
    static String signature(Path root, List<InboxAccess.FileStat> files) {
        List<String> lines = new ArrayList<>();
        for (InboxAccess.FileStat f : files) {
            lines.add(relative(root, f.path()) + "|" + f.size() + "|" + f.modified().toEpochMilli());
        }
        lines.sort(null);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (String line : lines) {
                digest.update(line.getBytes(StandardCharsets.UTF_8));
                digest.update((byte) '\n');
            }
            return HexFormat.of().formatHex(digest.digest()).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    /** {@code path} relative to {@code root} with {@code /} separators. */
    static String relative(Path root, Path path) {
        String r = root.relativize(path).toString().replace('\\', '/');
        return r.isEmpty() ? "." : r;
    }

    /** A folder of interest found by the walk. */
    private record Found(Path path, boolean xrit) {}

    /** One bounded walk. */
    private static final class Walk {

        private final InboxAccess inbox;
        private final Path root;
        private final List<Found> found = new ArrayList<>();
        private int entries;
        private boolean truncated;

        Walk(InboxAccess inbox, Path root) {
            this.inbox = inbox;
            this.root = root;
        }

        void directory(Path dir, int depth, boolean underImages) throws IOException {
            if (depth > MAX_DEPTH || truncated) {
                return;
            }
            List<Path> files;
            List<Path> dirs;
            try {
                files = inbox.listRegularFiles(dir);
                dirs = inbox.listDirectories(dir);
            } catch (IOException | RuntimeException e) {
                if (dir.equals(root)) {
                    throw e instanceof IOException io ? io : new IOException(e.getMessage(), e);
                }
                return;
            }
            entries += files.size() + dirs.size();
            if (entries > MAX_ENTRIES) {
                truncated = true;
                return;
            }
            boolean hasDataset = files.stream().anyMatch(f -> f.getFileName().toString().equals(DATASET));
            boolean hasProduct = files.stream().anyMatch(f -> f.getFileName().toString().equals(PRODUCT));
            if (underImages && hasProduct) {
                found.add(new Found(dir, true));
                return;
            }
            if (!underImages && hasDataset) {
                found.add(new Found(dir, false));
            }
            for (Path child : dirs) {
                boolean images = underImages || child.getFileName().toString().equals(IMAGES);
                directory(child, depth + 1, images);
            }
        }

        /** The files of a folder; recursively (skipping IMAGES trees) for a pass folder. */
        List<InboxAccess.FileStat> files(Path dir, boolean recursive) {
            List<InboxAccess.FileStat> out = new ArrayList<>();
            collect(dir, recursive, 0, out);
            return out;
        }

        private void collect(Path dir, boolean recursive, int depth, List<InboxAccess.FileStat> out) {
            if (depth > MAX_DEPTH) {
                return;
            }
            try {
                for (Path file : inbox.listRegularFiles(dir)) {
                    try {
                        out.add(inbox.stat(file));
                    } catch (IOException | RuntimeException e) {
                        // Vanished or replaced by a link between the listing and the stat: left out.
                    }
                }
                if (recursive) {
                    for (Path child : inbox.listDirectories(dir)) {
                        if (!child.getFileName().toString().equals(IMAGES)) {
                            collect(child, true, depth + 1, out);
                        }
                    }
                }
            } catch (IOException | RuntimeException e) {
                // A directory that vanished mid-scan: what was collected stands.
            }
        }
    }
}
