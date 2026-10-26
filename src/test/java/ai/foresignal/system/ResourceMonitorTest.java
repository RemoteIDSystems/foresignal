package ai.foresignal.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.settings.ResourceThresholds;
import java.io.IOException;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.FileStore;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link ResourceMonitor}'s gauges and rating (prompt R2-014), from stub file stores and memory beans. */
class ResourceMonitorTest {

    static final Instant T = Instant.parse("2026-10-26T12:00:00Z");
    static final long GB = 1_000_000_000L;

    /** A file store with fixed sizes. */
    static final class StubStore extends FileStore {
        private final long total;
        private final long usable;

        StubStore(long total, long usable) {
            this.total = total;
            this.usable = usable;
        }

        @Override public String name() { return "stub"; }
        @Override public String type() { return "stub"; }
        @Override public boolean isReadOnly() { return false; }
        @Override public long getTotalSpace() { return total; }
        @Override public long getUsableSpace() { return usable; }
        @Override public long getUnallocatedSpace() { return usable; }
        @Override public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) { return false; }
        @Override public boolean supportsFileAttributeView(String name) { return false; }
        @Override public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) { return null; }
        @Override public Object getAttribute(String attribute) throws IOException { return null; }
    }

    @Test
    @DisplayName("a disk gauge is total, used, free and used percent from the file store")
    void diskGauge() {
        ResourceGauge g = ResourceMonitor.disk(new StubStore(100 * GB, 25 * GB));
        assertThat(g.totalBytes()).isEqualTo(100 * GB);
        assertThat(g.freeBytes()).isEqualTo(25 * GB);
        assertThat(g.usedBytes()).isEqualTo(75 * GB);
        assertThat(g.usedPercent()).isEqualTo(75.0);
        assertThat(ResourceGauge.freePercent(g)).isEqualTo(25.0);
        assertThat(ResourceMonitor.disk(new StubStore(0, 0))).as("an unreadable store").isNull();
    }

    @Test
    @DisplayName("percent and byte floors: either one breached reaches the level; critical beats warn")
    void levels() {
        ResourceThresholds.Threshold t = new ResourceThresholds.Threshold(15.0, 50 * GB, 5.0, 10 * GB);
        assertThat(t.level(500 * GB, 50.0)).isEqualTo("ok");
        assertThat(t.level(40 * GB, 50.0)).as("below the warn byte floor").isEqualTo("warn");
        assertThat(t.level(500 * GB, 10.0)).as("below the warn percent").isEqualTo("warn");
        assertThat(t.level(500 * GB, 4.0)).isEqualTo("critical");
        assertThat(t.level(5 * GB, 50.0)).isEqualTo("critical");
        assertThat(ResourceThresholds.Threshold.OFF.level(0L, 0.0)).isEqualTo("ok");
        assertThat(t.level(null, null)).as("unknown readings never breach").isEqualTo("ok");
    }

    @Test
    @DisplayName("the disk gauge is the worst volume; the overall status is the worst gauge")
    void worstOf() {
        ResourceThresholds t = ResourceThresholds.defaults();
        ResourceGauge roomy = ResourceGauge.of(100 * GB, 60 * GB);
        ResourceGauge tight = ResourceGauge.of(100 * GB, 10 * GB);
        ResourceStatus s = ResourceMonitor.rate(List.of(roomy, tight), ResourceGauge.of(16 * GB, 8 * GB), 123L, t, T);
        assertThat(s.disk().freeBytes()).isEqualTo(10 * GB);
        assertThat(s.disk().status()).isEqualTo("warn");
        assertThat(s.memory().status()).as("memory is off by default").isEqualTo("ok");
        assertThat(s.status()).isEqualTo("warn");
        assertThat(s.warnings()).hasSize(1);
        assertThat(s.warnings().get(0)).startsWith("Disk space is low");
        assertThat(s.database().get("sizeBytes")).isEqualTo(123L);
        assertThat(s.checkedAt()).isEqualTo(T);

        ResourceStatus critical = ResourceMonitor.rate(List.of(roomy, ResourceGauge.of(100 * GB, 2 * GB)),
                null, null, t, T);
        assertThat(critical.status()).isEqualTo("critical");
        assertThat(critical.memory()).isEqualTo(ResourceGauge.UNKNOWN);

        ResourceThresholds withMemory = new ResourceThresholds(t.disk(),
                new ResourceThresholds.Threshold(20.0, null, 5.0, null), List.of(), t.sourceStorm(),
                t.collectorBacklog());
        ResourceStatus memory = ResourceMonitor.rate(List.of(roomy), ResourceGauge.of(16 * GB, GB / 2), null,
                withMemory, T);
        assertThat(memory.disk().status()).isEqualTo("ok");
        assertThat(memory.memory().status()).isEqualTo("critical");
        assertThat(memory.status()).isEqualTo("critical");
        assertThat(ResourceMonitor.rate(List.of(), null, null, t, T).disk()).isEqualTo(ResourceGauge.UNKNOWN);
    }

    @Test
    @DisplayName("without a physical-memory bean the gauge falls back to the heap")
    void heapFallback() {
        OperatingSystemMXBean plain = mock(OperatingSystemMXBean.class);
        MemoryMXBean heap = mock(MemoryMXBean.class);
        when(heap.getHeapMemoryUsage()).thenReturn(new MemoryUsage(0, 300, 400, 1000));
        ResourceGauge g = ResourceMonitor.memory(plain, heap);
        assertThat(g.totalBytes()).isEqualTo(1000);
        assertThat(g.usedBytes()).isEqualTo(300);
        assertThat(g.freeBytes()).isEqualTo(700);
    }

    @Test
    @DisplayName("the warnings contributor maps levels to DISK_/MEMORY_ codes and severities")
    void warnings() {
        ResourceStatus s = new ResourceStatus("critical", ResourceGauge.of(100, 1).withStatus("critical"),
                ResourceGauge.of(100, 10).withStatus("warn"), null, List.of(), T);
        List<SystemWarnings.Warning> w = ResourceWarnings.of(s);
        assertThat(w).extracting(SystemWarnings.Warning::code).containsExactly("DISK_CRITICAL", "MEMORY_LOW");
        assertThat(w).extracting(SystemWarnings.Warning::severity).containsExactly("critical", "warning");
        assertThat(ResourceWarnings.of(new ResourceStatus("ok", ResourceGauge.of(100, 90), ResourceGauge.UNKNOWN,
                null, List.of(), T))).isEmpty();
    }
}
