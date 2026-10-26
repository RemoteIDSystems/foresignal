package ai.foresignal.system;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.raster.RasterPaths;
import ai.foresignal.settings.AppSettingChangedEvent;
import ai.foresignal.settings.ResourceThresholds;
import ai.foresignal.settings.ResourceThresholdsService;
import ai.foresignal.settings.SettingKeys;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Watches the instance's own host (prompt R2-014): free space on its data volumes, memory, and the database size,
 * read at most every {@link #CACHE} like {@link OperatorGauges}, and rated against the admin's
 * {@link ResourceThresholds}.
 *
 * <ul>
 *   <li><b>Disk.</b> Every on-disk root the instance writes: the data directory ({@code foresignal.data-dir}) and the
 *       raster/media store root, de-duplicated by {@link FileStore}. The gauge is the most severe of them, the one
 *       with the least free share on a tie. The Postgres data directory is on the database container's volume, which
 *       this process cannot see; the database is gauged by size instead.
 *   <li><b>Memory.</b> Physical memory from {@code com.sun.management.OperatingSystemMXBean} where the JVM offers it
 *       (inside a container, the container's limit); otherwise the heap from {@link MemoryMXBean}.
 *   <li><b>Database.</b> {@code pg_database_size(current_database())}, as {@code /v1/system} reports it.
 * </ul>
 */
@Component
public class ResourceMonitor {

    private static final Logger log = LoggerFactory.getLogger(ResourceMonitor.class);

    /** How long a reading is reused. */
    public static final Duration CACHE = Duration.ofSeconds(30);

    private final ForesignalProperties properties;
    private final ObjectProvider<RasterPaths> rasterPaths;
    private final JdbcTemplate jdbc;
    private final ResourceThresholdsService thresholds;
    private final Clock clock;
    private volatile ResourceStatus last;

    /**
     * Creates the monitor.
     *
     * @param properties the instance properties (the data directory)
     * @param rasterPaths the raster store layout, when present
     * @param jdbc the database
     * @param thresholds the admin's thresholds
     * @param clock the clock
     */
    public ResourceMonitor(ForesignalProperties properties, ObjectProvider<RasterPaths> rasterPaths, JdbcTemplate jdbc,
            ResourceThresholdsService thresholds, Clock clock) {
        this.properties = properties;
        this.rasterPaths = rasterPaths;
        this.jdbc = jdbc;
        this.thresholds = thresholds;
        this.clock = clock;
    }

    /** The current reading, taken again when the cached one is older than {@link #CACHE}. */
    public ResourceStatus snapshot() {
        ResourceStatus cached = last;
        Instant now = clock.instant();
        if (cached != null && Duration.between(cached.checkedAt(), now).compareTo(CACHE) < 0
                && !cached.checkedAt().isAfter(now)) {
            return cached;
        }
        return refresh();
    }

    /** Takes a new reading now. */
    public synchronized ResourceStatus refresh() {
        ResourceStatus status = rate(disks(), memory(), databaseBytes(), thresholds.get(), clock.instant());
        last = status;
        return status;
    }

    /** Drops the cached reading when the thresholds change, so the next read rates against the new ones. */
    @EventListener
    public void onSettingChanged(AppSettingChangedEvent event) {
        if (SettingKeys.THRESHOLDS.equals(event.key())) {
            last = null;
        }
    }

    /**
     * Rates readings against thresholds.
     *
     * @param disks one gauge per data volume
     * @param memory the memory gauge, or null when unknown
     * @param databaseBytes the database size, or null when unknown
     * @param t the thresholds
     * @param at when the readings were taken
     * @return the status
     */
    static ResourceStatus rate(List<ResourceGauge> disks, ResourceGauge memory, Long databaseBytes,
            ResourceThresholds t, Instant at) {
        List<String> warnings = new ArrayList<>();
        ResourceGauge disk = null;
        for (ResourceGauge reading : disks) {
            ResourceGauge rated = reading.withStatus(t.disk().level(reading.freeBytes(),
                    ResourceGauge.freePercent(reading)));
            if (disk == null || ResourceStatus.rank(rated.status()) > ResourceStatus.rank(disk.status())
                    || (ResourceStatus.rank(rated.status()) == ResourceStatus.rank(disk.status())
                            && lessFree(rated, disk))) {
                disk = rated;
            }
        }
        disk = disk == null ? ResourceGauge.UNKNOWN : disk;
        ResourceGauge mem = memory == null ? ResourceGauge.UNKNOWN
                : memory.withStatus(t.memory().level(memory.freeBytes(), ResourceGauge.freePercent(memory)));
        if (!"ok".equals(disk.status())) {
            warnings.add(message("Disk space", disk));
        }
        if (!"ok".equals(mem.status())) {
            warnings.add(message("Memory", mem));
        }
        Map<String, Object> database = new LinkedHashMap<>();
        database.put("sizeBytes", databaseBytes);
        return new ResourceStatus(ResourceStatus.worst(disk.status(), mem.status()), disk, mem, database,
                List.copyOf(warnings), at);
    }

    private static boolean lessFree(ResourceGauge a, ResourceGauge b) {
        Double fa = ResourceGauge.freePercent(a);
        Double fb = ResourceGauge.freePercent(b);
        return fa != null && (fb == null || fa < fb);
    }

    /**
     * The breach message of one gauge.
     *
     * @param what {@code Disk space} or {@code Memory}
     * @param gauge the gauge, not {@code ok}
     * @return the message
     */
    static String message(String what, ResourceGauge gauge) {
        Double free = ResourceGauge.freePercent(gauge);
        String level = "critical".equals(gauge.status()) ? "critically low" : "low";
        return String.format(Locale.US, "%s is %s: %s free%s.", what, level,
                gauge.freeBytes() == null ? "unknown" : bytes(gauge.freeBytes()),
                free == null ? "" : String.format(Locale.US, " (%.1f %%)", free));
    }

    /**
     * A byte count for people.
     *
     * @param bytes the count
     * @return for example {@code 12.3 GB}
     */
    static String bytes(long bytes) {
        double gb = bytes / 1e9;
        if (gb >= 1) {
            return String.format(Locale.US, "%.1f GB", gb);
        }
        return String.format(Locale.US, "%.0f MB", bytes / 1e6);
    }

    /** One gauge per distinct file store under the instance's on-disk roots. */
    List<ResourceGauge> disks() {
        Set<Path> roots = new LinkedHashSet<>();
        if (properties.dataDir() != null) {
            roots.add(properties.dataDir());
        }
        RasterPaths raster = rasterPaths.getIfAvailable();
        if (raster != null) {
            roots.add(raster.root());
        }
        Set<FileStore> stores = new LinkedHashSet<>();
        for (Path root : roots) {
            FileStore store = storeOf(root);
            if (store != null) {
                stores.add(store);
            }
        }
        List<ResourceGauge> out = new ArrayList<>();
        for (FileStore store : stores) {
            ResourceGauge gauge = disk(store);
            if (gauge != null) {
                out.add(gauge);
            }
        }
        return out;
    }

    /**
     * The file store holding {@code path}, or of its nearest existing parent.
     *
     * @param path the path
     * @return the store, or null when none can be read
     */
    static FileStore storeOf(Path path) {
        Path p = path.toAbsolutePath().normalize();
        while (p != null && !Files.exists(p)) {
            p = p.getParent();
        }
        if (p == null) {
            return null;
        }
        try {
            return Files.getFileStore(p);
        } catch (IOException | RuntimeException e) {
            log.debug("Could not read the file store of {}", p, e);
            return null;
        }
    }

    /**
     * The gauge of one file store: total and usable space.
     *
     * @param store the store
     * @return the gauge, or null when it cannot be read
     */
    static ResourceGauge disk(FileStore store) {
        try {
            long total = store.getTotalSpace();
            if (total <= 0) {
                return null;
            }
            return ResourceGauge.of(total, store.getUsableSpace());
        } catch (IOException | RuntimeException e) {
            log.debug("Could not read the space of {}", store, e);
            return null;
        }
    }

    /** The memory gauge of this JVM. */
    static ResourceGauge memory() {
        return memory(ManagementFactory.getOperatingSystemMXBean(), ManagementFactory.getMemoryMXBean());
    }

    /**
     * Physical memory when {@code os} reports it, otherwise the heap.
     *
     * @param os the operating system bean
     * @param heap the memory bean
     * @return the gauge
     */
    static ResourceGauge memory(OperatingSystemMXBean os, MemoryMXBean heap) {
        if (os instanceof com.sun.management.OperatingSystemMXBean physical) {
            long total = physical.getTotalMemorySize();
            long free = physical.getFreeMemorySize();
            if (total > 0 && free >= 0) {
                return ResourceGauge.of(total, free);
            }
        }
        if (heap != null) {
            var usage = heap.getHeapMemoryUsage();
            long max = usage.getMax() > 0 ? usage.getMax() : usage.getCommitted();
            if (max > 0) {
                return ResourceGauge.of(max, max - usage.getUsed());
            }
        }
        Runtime runtime = Runtime.getRuntime();
        long max = runtime.maxMemory();
        return ResourceGauge.of(max, max - (runtime.totalMemory() - runtime.freeMemory()));
    }

    private Long databaseBytes() {
        try {
            return jdbc.queryForObject("SELECT pg_database_size(current_database())", Long.class);
        } catch (RuntimeException e) {
            log.debug("Could not read the database size", e);
            return null;
        }
    }
}
