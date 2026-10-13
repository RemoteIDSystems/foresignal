package ai.foresignal.raster.lightning;

import ai.foresignal.source.spi.FlashBatch;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The compact daily GLM archive (prompt 048, Weather-Imagery-Design §2.4):
 * {@code rasters/glm/{sat}/{yyyymmdd}.bin}, append-only, {@value #RECORD_BYTES} bytes per flash,
 * little-endian:
 *
 * <pre>
 * int32   msOfDayUtc   milliseconds since 00:00 UTC of the file's day
 * float32 lat          degrees north
 * float32 lon          degrees east
 * float32 energyFj     radiant energy, femtojoules
 * </pre>
 *
 * <p>Each append batch is written and then {@code fsync}ed ({@link FileChannel#force}). A crash in the
 * middle of a write can leave a torn trailing record shorter than 16 bytes; it is truncated when the
 * file is next opened for appending, and ignored by readers. A full GOES-19 field of view is about
 * 1.7 M flashes, about 28 MB, a day.
 *
 * <p>Not thread-safe; {@link FlashStore} serializes writers.
 */
public final class FlashArchive {

    private static final Logger log = LoggerFactory.getLogger(FlashArchive.class);

    /** Bytes per flash. */
    public static final int RECORD_BYTES = 16;

    /** Milliseconds in a UTC day. */
    static final long DAY_MS = 86_400_000L;

    /** File names: {@code yyyymmdd.bin}. */
    static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE;

    /** A satellite directory name: letters, digits and dashes only, so it cannot leave the root. */
    static final Pattern SATELLITE = Pattern.compile("^[A-Za-z0-9-]{1,16}$");

    private static final Pattern FILE = Pattern.compile("^\\d{8}\\.bin$");

    /** Records read per I/O call. */
    private static final int READ_RECORDS = 4096;

    private final Path root;

    /**
     * Creates the archive under {@code root} ({@code rasters/glm}).
     *
     * @param root the archive root; created when missing
     */
    public FlashArchive(Path root) {
        this.root = root.toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.root);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create the GLM archive " + this.root, e);
        }
    }

    /** The archive root. */
    public Path root() {
        return root;
    }

    /**
     * The file of {@code satellite} for {@code day}.
     *
     * @throws IllegalArgumentException for a satellite name that is not letters, digits and dashes
     */
    public Path file(String satellite, LocalDate day) {
        if (satellite == null || !SATELLITE.matcher(satellite).matches()) {
            throw new IllegalArgumentException("Invalid GLM satellite name: " + satellite);
        }
        return root.resolve(satellite).resolve(DAY.format(day) + ".bin");
    }

    /**
     * Appends {@code flashes}, each to the file of its own UTC day, and fsyncs every file written.
     *
     * @throws UncheckedIOException when a file cannot be written
     */
    public void append(String satellite, FlashBatch flashes) {
        if (flashes.isEmpty()) {
            return;
        }
        Map<Long, List<Integer>> byDay = new TreeMap<>();
        for (int i = 0; i < flashes.size(); i++) {
            byDay.computeIfAbsent(Math.floorDiv(flashes.timesMs()[i], DAY_MS), d -> new ArrayList<>()).add(i);
        }
        for (Map.Entry<Long, List<Integer>> entry : byDay.entrySet()) {
            long dayStart = entry.getKey() * DAY_MS;
            LocalDate day = LocalDate.ofEpochDay(entry.getKey());
            List<Integer> indices = entry.getValue();
            ByteBuffer buffer = ByteBuffer.allocate(indices.size() * RECORD_BYTES).order(ByteOrder.LITTLE_ENDIAN);
            for (int i : indices) {
                buffer.putInt((int) (flashes.timesMs()[i] - dayStart));
                buffer.putFloat(flashes.lat()[i]);
                buffer.putFloat(flashes.lon()[i]);
                buffer.putFloat(flashes.energyFj()[i]);
            }
            buffer.flip();
            Path file = file(satellite, day);
            try {
                Files.createDirectories(file.getParent());
                try (FileChannel channel = openForAppend(file)) {
                    while (buffer.hasRemaining()) {
                        channel.write(buffer);
                    }
                    channel.force(false);
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot append to " + file, e);
            }
        }
    }

    /**
     * Opens {@code file} for writing at its end, truncating a torn trailing record first.
     *
     * @throws IOException when the file cannot be opened or truncated
     */
    static FileChannel openForAppend(Path file) throws IOException {
        if (Files.isSymbolicLink(file)) {
            throw new IOException("Refusing to append to a symbolic link: " + file);
        }
        FileChannel channel = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        try {
            long size = channel.size();
            long whole = size - size % RECORD_BYTES;
            if (whole != size) {
                log.warn("GLM archive {} ends with a torn record of {} bytes; truncated", file.getFileName(),
                        size - whole);
                channel.truncate(whole);
                channel.force(false);
            }
            channel.position(whole);
            return channel;
        } catch (IOException | RuntimeException e) {
            channel.close();
            throw e;
        }
    }

    /**
     * The flashes of {@code satellite} on {@code day} with {@code fromMs <= time <= toMs} (epoch
     * milliseconds), in file order; a torn trailing record is ignored. A missing file reads as empty.
     *
     * @throws UncheckedIOException when the file cannot be read
     */
    public FlashBatch read(String satellite, LocalDate day, long fromMs, long toMs) {
        Path file = file(satellite, day);
        if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
            return FlashBatch.EMPTY;
        }
        long dayStart = day.toEpochDay() * DAY_MS;
        Growable out = new Growable(1024);
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
            long records = channel.size() / RECORD_BYTES;
            ByteBuffer buffer = ByteBuffer.allocate(READ_RECORDS * RECORD_BYTES).order(ByteOrder.LITTLE_ENDIAN);
            long done = 0;
            while (done < records) {
                buffer.clear();
                int want = (int) Math.min(READ_RECORDS, records - done) * RECORD_BYTES;
                buffer.limit(want);
                while (buffer.hasRemaining()) {
                    if (channel.read(buffer, done * RECORD_BYTES + buffer.position()) < 0) {
                        break;
                    }
                }
                buffer.flip();
                int n = buffer.remaining() / RECORD_BYTES;
                for (int k = 0; k < n; k++) {
                    long t = dayStart + buffer.getInt();
                    float lat = buffer.getFloat();
                    float lon = buffer.getFloat();
                    float energy = buffer.getFloat();
                    if (t >= fromMs && t <= toMs) {
                        out.add(t, lat, lon, energy);
                    }
                }
                if (n == 0) {
                    break;
                }
                done += n;
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file, e);
        }
        return out.toBatch();
    }

    /** The satellites with an archive directory. */
    public List<String> satellites() {
        List<String> out = new ArrayList<>();
        try (Stream<Path> dirs = Files.list(root)) {
            dirs.filter(Files::isDirectory).map(p -> p.getFileName().toString())
                    .filter(name -> SATELLITE.matcher(name).matches()).sorted().forEach(out::add);
        } catch (IOException e) {
            log.debug("Cannot list {}", root, e);
        }
        return out;
    }

    /** The bytes of every archive file. */
    public long totalBytes() {
        long total = 0;
        for (Path file : files()) {
            try {
                total += Files.size(file);
            } catch (IOException e) {
                // A file deleted meanwhile counts as zero.
            }
        }
        return total;
    }

    /**
     * Deletes the files of days before {@code cutoff}'s UTC day (Weather-Imagery-Design §2.4: the GLM
     * archive keeps 14 days).
     *
     * @param cutoff files whose whole day lies before this instant's day are deleted
     * @return the bytes deleted
     */
    public long deleteBefore(Instant cutoff) {
        LocalDate keepFrom = LocalDate.ofInstant(cutoff, ZoneOffset.UTC);
        long deleted = 0;
        for (Path file : files()) {
            String name = file.getFileName().toString();
            LocalDate day;
            try {
                day = LocalDate.parse(name.substring(0, 8), DAY);
            } catch (DateTimeParseException e) {
                continue;
            }
            if (day.isBefore(keepFrom)) {
                try {
                    long size = Files.size(file);
                    if (Files.deleteIfExists(file)) {
                        deleted += size;
                    }
                } catch (IOException e) {
                    log.warn("Cannot delete the GLM archive file {}", file, e);
                }
            }
        }
        return deleted;
    }

    /** Every {@code yyyymmdd.bin} file under a satellite directory. */
    List<Path> files() {
        List<Path> out = new ArrayList<>();
        for (String satellite : satellites()) {
            try (Stream<Path> files = Files.list(root.resolve(satellite))) {
                files.filter(f -> FILE.matcher(f.getFileName().toString()).matches())
                        .filter(f -> Files.isRegularFile(f, LinkOption.NOFOLLOW_LINKS)).sorted().forEach(out::add);
            } catch (IOException e) {
                log.debug("Cannot list {}", satellite, e);
            }
        }
        return out;
    }

    /** Parallel arrays that grow, for reading an unknown number of flashes. */
    static final class Growable {

        private long[] t;
        private float[] lat;
        private float[] lon;
        private float[] energy;
        private int size;

        Growable(int capacity) {
            int c = Math.max(16, capacity);
            t = new long[c];
            lat = new float[c];
            lon = new float[c];
            energy = new float[c];
        }

        void add(long time, float la, float lo, float e) {
            if (size == t.length) {
                int c = t.length * 2;
                t = java.util.Arrays.copyOf(t, c);
                lat = java.util.Arrays.copyOf(lat, c);
                lon = java.util.Arrays.copyOf(lon, c);
                energy = java.util.Arrays.copyOf(energy, c);
            }
            t[size] = time;
            lat[size] = la;
            lon[size] = lo;
            energy[size] = e;
            size++;
        }

        int size() {
            return size;
        }

        FlashBatch toBatch() {
            if (size == 0) {
                return FlashBatch.EMPTY;
            }
            return new FlashBatch(java.util.Arrays.copyOf(t, size), java.util.Arrays.copyOf(lat, size),
                    java.util.Arrays.copyOf(lon, size), java.util.Arrays.copyOf(energy, size));
        }
    }
}
