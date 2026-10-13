package ai.foresignal.retention;

import ai.foresignal.source.adapters.awc.AwcMetarsAdapter;
import ai.foresignal.source.adapters.goes.GoesFdcAdapter;
import ai.foresignal.source.adapters.goes.GoesGlmAdapter;
import ai.foresignal.source.adapters.hms.HmsFireAdapter;
import ai.foresignal.source.adapters.hms.HmsSmokeAdapter;
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
     * {@code event}. Prompt 048 adds the lightning and fire sources to {@code weather}: the Conventions
     * table puts "GLM lightning" and "FIRMS pixels" in {@code bulk-event} (90 d), but a shorter window
     * for a {@code bulk-event} kind is not overridden (only {@code event} and {@code series} are), and
     * §2.4 names {@code lightning-cluster} 3 d and {@code fire-detection}, {@code fire-cluster},
     * {@code smoke-polygon} 7 d. Prompt 052 adds the derived weather sources ({@code DerivedGenerator}s) to
     * {@code weather}: §2.4 names {@code place-weather} 14 d, {@code wx-area} 7 d and {@code radar-cell} 3 d. Prompt 053
     * adds {@code spc-storm-reports} ({@code storm-report} 30 d, §2.4) and {@code nws-gridpoints} ({@code nws-forecast}
     * 7 d, named by the prompt; forecasts are not events) to {@code weather}; whether storm reports belong to the
     * {@code event} class (1825 d) is an open owner question in the 053 report.
     */
    record Entry(SourceDescriptor descriptor, String analyticClass) {}

    static final Map<String, Entry> CLASSES = Map.ofEntries(
            Map.entry("UsgsEarthquakesAdapter", new Entry(new UsgsEarthquakesAdapter("").descriptor(), "event")),
            Map.entry("SystemCanaryAdapter", new Entry(new SystemCanaryAdapter().descriptor(), "system")),
            Map.entry("MrmsAdapter", new Entry(new MrmsAdapter(null, "").descriptor(), "raster")),
            Map.entry("GoesAbiAdapter", new Entry(new GoesAbiAdapter(null, "").descriptor(), "raster")),
            Map.entry("GfsAdapter", new Entry(new ai.foresignal.source.adapters.gfs.GfsAdapter(
                    new ai.foresignal.raster.model.ModelRunIngest(null, null), "").descriptor(), "raster")),
            Map.entry("HrrrAdapter", new Entry(new ai.foresignal.source.adapters.hrrr.HrrrAdapter(
                    new ai.foresignal.raster.model.ModelRunIngest(null, null), "small", "").descriptor(), "raster")),
            Map.entry("NwsAlertsAdapter", new Entry(new NwsAlertsAdapter((CountyGeometries) null, "").descriptor(), "event")),
            Map.entry("ProbSevereAdapter", new Entry(new ProbSevereAdapter((AreaOfInterest) null, "").descriptor(), "weather")),
            Map.entry("SpcOutlooksAdapter", new Entry(new SpcOutlooksAdapter("").descriptor(), "weather")),
            // Prompt 053: storm reports and NWS gridpoint forecasts, retained per the prompt and §2.4.
            Map.entry("SpcStormReportsAdapter", new Entry(
                    new ai.foresignal.source.adapters.spc.SpcStormReportsAdapter("").descriptor(), "weather")),
            Map.entry("NwsGridpointsAdapter", new Entry(new ai.foresignal.source.adapters.nws.NwsGridpointsAdapter(
                    (java.util.function.Supplier<ai.foresignal.source.spi.PlacePoints>) null, null, "").descriptor(),
                    "weather")),
            Map.entry("AwcMetarsAdapter", new Entry(new AwcMetarsAdapter((AreaOfInterest) null, "").descriptor(), "weather")),
            Map.entry("GoesGlmAdapter", new Entry(new GoesGlmAdapter(null, "").descriptor(), "weather")),
            Map.entry("GoesFdcAdapter", new Entry(new GoesFdcAdapter(null, (AreaOfInterest) null, "").descriptor(), "weather")),
            Map.entry("HmsSmokeAdapter", new Entry(new HmsSmokeAdapter("").descriptor(), "weather")),
            Map.entry("HmsFireAdapter", new Entry(new HmsFireAdapter("").descriptor(), "weather")),
            // Prompt 052: the derived weather sources, retained per Weather-Imagery-Design §2.4.
            Map.entry("WxPlacesGenerator", new Entry(
                    new ai.foresignal.source.adapters.wx.WxPlacesGenerator().descriptor(), "weather")),
            Map.entry("WxAreasGenerator", new Entry(
                    new ai.foresignal.source.adapters.wx.WxAreasGenerator().descriptor(), "weather")),
            Map.entry("WxCellsGenerator", new Entry(
                    new ai.foresignal.source.adapters.wx.WxCellsGenerator("small").descriptor(), "weather")));

    /** Weather-Imagery-Design §2.4's per-kind record retention, for the {@code weather} class. */
    static final Map<String, Duration> WEATHER_KIND_RETENTION = Map.ofEntries(
            Map.entry("probsevere-object", Duration.ofDays(3)),
            Map.entry("spc-outlook", Duration.ofDays(14)),
            Map.entry("metar", Duration.ofDays(7)),
            Map.entry("lightning-cluster", Duration.ofDays(3)),
            Map.entry("fire-detection", Duration.ofDays(7)),
            Map.entry("fire-cluster", Duration.ofDays(7)),
            Map.entry("smoke-polygon", Duration.ofDays(7)),
            Map.entry("place-weather", Duration.ofDays(14)),
            Map.entry("wx-area", Duration.ofDays(7)),
            Map.entry("radar-cell", Duration.ofDays(3)),
            Map.entry("storm-report", Duration.ofDays(30)),
            Map.entry("nws-forecast", Duration.ofDays(7)));

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
                assertThat(descriptor.kinds()).as("%s kinds", descriptor.id()).isNotEmpty();
                for (String kind : descriptor.kinds()) {
                    assertThat(WEATHER_KIND_RETENTION).as("%s kind %s is in the §2.4 table", descriptor.id(), kind)
                            .containsKey(kind);
                    assertThat(descriptor.kindRetention().get(kind)).as("%s kindRetention %s", descriptor.id(), kind)
                            .isEqualTo(WEATHER_KIND_RETENTION.get(kind));
                }
                assertThat(descriptor.defaultRetention()).as("%s defaultRetention", descriptor.id())
                        .isEqualTo(WEATHER_KIND_RETENTION.get(descriptor.kinds().iterator().next()));
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
        Pattern adapter = Pattern.compile("class\\s+(\\w+)\\s+implements\\s+(?:PollingAdapter|StreamingAdapter|PushAdapter|DerivedAdapter|DerivedGenerator)\\b");
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
