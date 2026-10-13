package ai.foresignal.retention;

import ai.foresignal.source.adapters.awc.AwcMetarsAdapter;
import ai.foresignal.source.adapters.mrms.ProbSevereAdapter;
import ai.foresignal.source.adapters.nws.NwsAlertsAdapter;
import ai.foresignal.source.adapters.spc.SpcOutlooksAdapter;
import ai.foresignal.source.spi.AreaOfInterest;
import ai.foresignal.source.spi.CountyGeometries;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.adapters.goes.GoesAbiAdapter;
import ai.foresignal.source.adapters.mrms.MrmsAdapter;
import ai.foresignal.source.adapters.system.SystemCanaryAdapter;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.adapters.usgs.UsgsEarthquakesAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Invariant RET-3 (prompt 022 added this class, which the registry names but no earlier prompt
 * wrote): built-in adapters set retention by analytic class (Conventions "Analytic retention"), and
 * {@code RetentionJob} never touches tables outside the observation and operational set, so the
 * intelligence block's typed tables are never auto-deleted.
 *
 * <p>Every adapter class under {@code ai.foresignal.source.adapters} must appear in
 * {@link #CLASSES}, so a new adapter cannot ship without its class being checked here.
 */
class FamilyDefaultsTest {

    private static final Path ADAPTERS = Path.of("src", "main", "java", "ai", "foresignal", "source", "adapters");
    private static final Path RETENTION = Path.of("src", "main", "java", "ai", "foresignal", "retention");

    /** The Conventions table: class → default raw retention (track is trackWindow, never a default). */
    static final Map<String, Duration> CLASS_DEFAULTS = Map.of(
            "event", Duration.ofDays(1825),
            "bulk-event", Duration.ofDays(90),
            "series", Duration.ofDays(3650),
            "state", Duration.ofDays(365),
            "reference", Duration.ofDays(90),
            "intel", Duration.ofDays(1825),
            "social", Duration.ofDays(30));

    /**
     * Adapter class → (descriptor, analytic class). {@code system} marks Foresignal's own health
     * sources (the watchdog canary), which carry no analytic data and keep their own short window.
     * {@code raster} (prompt 046) marks product sources: their only kind is {@code raster-frame}, kept
     * 7 days by Weather-Imagery-Design §2.4 ("versions outlive frames"). The Conventions table has no
     * class for frame records; the 042 report asked the owner which class they belong to.
     * {@code weather} (prompt 047, provisional) marks weather record sources whose kinds the Conventions
     * table does not name (ProbSevere objects, SPC outlooks, METARs): their retention is exactly the
     * per-kind table of Weather-Imagery-Design §2.4. NWS alerts are "weather and CAP alerts", so
     * {@code event}.
     */
    record Entry(SourceDescriptor descriptor, String analyticClass) {}

    static final Map<String, Entry> CLASSES = Map.of(
            "UsgsEarthquakesAdapter", new Entry(new UsgsEarthquakesAdapter("").descriptor(), "event"),
            "SystemCanaryAdapter", new Entry(new SystemCanaryAdapter().descriptor(), "system"),
            "MrmsAdapter", new Entry(new MrmsAdapter(null, "").descriptor(), "raster"),
            "GoesAbiAdapter", new Entry(new GoesAbiAdapter(null, "").descriptor(), "raster"),
            "NwsAlertsAdapter", new Entry(new NwsAlertsAdapter((CountyGeometries) null, "").descriptor(), "event"),
            "ProbSevereAdapter", new Entry(new ProbSevereAdapter((AreaOfInterest) null, "").descriptor(), "weather"),
            "SpcOutlooksAdapter", new Entry(new SpcOutlooksAdapter("").descriptor(), "weather"),
            "AwcMetarsAdapter", new Entry(new AwcMetarsAdapter((AreaOfInterest) null, "").descriptor(), "weather"));

    /** Weather-Imagery-Design §2.4's per-kind record retention, for the {@code weather} class. */
    static final Map<String, Duration> WEATHER_KIND_RETENTION = Map.of(
            "probsevere-object", Duration.ofDays(3),
            "spc-outlook", Duration.ofDays(14),
            "metar", Duration.ofDays(7));

    /** The design's raster-frame record retention (Weather-Imagery-Design §2.4). */
    static final Duration RASTER_FRAME_RETENTION = Duration.ofDays(7);

    /** Tables retention may delete from: versions and operational rows, never intelligence tables. */
    static final Set<String> RETENTION_TABLES =
            Set.of("record_version", "fetch_log", "seq_clock", "alert", "source_usage", "notification");

    @Test
    @DisplayName("every built-in adapter is classified, and its retention is its class's default or longer")
    void adaptersFollowTheirClass() throws IOException {
        assertThat(adapterClassNames()).as("adapter classes under source/adapters").isEqualTo(new TreeSet<>(CLASSES.keySet()));
        for (Map.Entry<String, Entry> entry : CLASSES.entrySet()) {
            SourceDescriptor descriptor = entry.getValue().descriptor();
            String analyticClass = entry.getValue().analyticClass();
            if (analyticClass.equals("system")) {
                assertThat(descriptor.id()).as("only Foresignal's own sources are 'system'").startsWith("system-");
                continue;
            }
            if (analyticClass.equals("raster")) {
                assertThat(descriptor.kinds()).as("%s kinds", descriptor.id()).containsExactly(ProductSpec.RECORD_KIND);
                assertThat(descriptor.products()).as("%s products", descriptor.id()).isNotEmpty();
                assertThat(descriptor.kindRetention().get(ProductSpec.RECORD_KIND)).as("%s raster-frame retention",
                        descriptor.id()).isGreaterThanOrEqualTo(RASTER_FRAME_RETENTION);
                continue;
            }
            if (analyticClass.equals("weather")) {
                assertThat(descriptor.kinds()).as("%s kinds", descriptor.id()).hasSize(1);
                String kind = descriptor.kinds().iterator().next();
                assertThat(WEATHER_KIND_RETENTION).as("%s kind %s is in the §2.4 table", descriptor.id(), kind)
                        .containsKey(kind);
                assertThat(descriptor.kindRetention().get(kind)).as("%s kindRetention", descriptor.id())
                        .isEqualTo(WEATHER_KIND_RETENTION.get(kind));
                assertThat(descriptor.defaultRetention()).as("%s defaultRetention", descriptor.id())
                        .isEqualTo(WEATHER_KIND_RETENTION.get(kind));
                continue;
            }
            Duration minimum = CLASS_DEFAULTS.get(analyticClass);
            assertThat(minimum).as("class %s of %s", analyticClass, entry.getKey()).isNotNull();
            assertThat(descriptor.defaultRetention()).as("%s defaultRetention", descriptor.id()).isNotNull()
                    .isGreaterThanOrEqualTo(minimum);
            descriptor.kindRetention().forEach((kind, window) ->
                    assertThat(window).as("%s kindRetention %s", descriptor.id(), kind).isGreaterThanOrEqualTo(minimum));
        }
    }

    @Test
    @DisplayName("the retention package deletes only from version and operational tables")
    void retentionNeverTouchesOtherTables() throws IOException {
        Set<String> targets = new LinkedHashSet<>();
        Pattern delete = Pattern.compile("(?i)DELETE\\s+FROM\\s+([a-z_][a-z0-9_]*)");
        try (Stream<Path> files = Files.walk(RETENTION)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                Matcher m = delete.matcher(Files.readString(file));
                while (m.find()) {
                    targets.add(m.group(1).toLowerCase());
                }
            }
        }
        assertThat(targets).as("tables named in DELETE statements under retention/").isNotEmpty()
                .isSubsetOf(RETENTION_TABLES);
    }

    private static Set<String> adapterClassNames() throws IOException {
        Pattern adapter = Pattern.compile("class\\s+(\\w+)\\s+implements\\s+(?:PollingAdapter|StreamingAdapter|PushAdapter|DerivedAdapter)\\b");
        Set<String> names = new TreeSet<>();
        try (Stream<Path> files = Files.walk(ADAPTERS)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                Matcher m = adapter.matcher(Files.readString(file));
                while (m.find()) {
                    names.add(m.group(1));
                }
            }
        }
        return names;
    }
}
