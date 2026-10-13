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
            // Prompt 065: nasa-firms writes fire-detection, retained 7 d by §2.4 like goes-fdc and hms-fire.
            Map.entry("FirmsAdapter", new Entry(
                    new ai.foresignal.source.adapters.firms.FirmsAdapter("").descriptor(), "weather")),
            // Prompt 066: gdelt-events writes conflict-event; Conventions puts GDELT events in bulk-event (90 d).
            Map.entry("GdeltEventsAdapter", new Entry(
                    new ai.foresignal.source.adapters.gdelt.GdeltEventsAdapter("", null, null).descriptor(),
                    "bulk-event")),
            // Prompt 067: cyclones, disasters and CAP alerts are the event class (1825 d).
            Map.entry("NhcCyclonesAdapter", new Entry(
                    new ai.foresignal.source.adapters.nhc.NhcCyclonesAdapter("").descriptor(), "event")),
            Map.entry("GdacsAdapter", new Entry(
                    new ai.foresignal.source.adapters.gdacs.GdacsAdapter("").descriptor(), "event")),
            Map.entry("TsunamiWarningsAdapter", new Entry(
                    new ai.foresignal.source.adapters.tsunami.TsunamiWarningsAdapter("").descriptor(), "event")),
            // Prompt 068: outages are the event class (1825 d).
            Map.entry("IodaOutagesAdapter", new Entry(
                    new ai.foresignal.source.adapters.ioda.IodaOutagesAdapter("", null, null, null).descriptor(),
                    "event")),
            // Prompt 069: the local receivers. Aircraft are TRACK (trackWindow, never a default); GNSS fixes,
            // receiver health and weather-station readings are SAMPLED-style status, the state class (365 d).
            Map.entry("LocalAdsbAdapter", new Entry(
                    new ai.foresignal.source.adapters.local.LocalAdsbAdapter(1).descriptor(), "track")),
            Map.entry("LocalSbsAdapter", new Entry(new ai.foresignal.source.adapters.local.LocalSbsAdapter(1,
                    new ai.foresignal.source.codec.Sbs1Codec()).descriptor(), "track")),
            Map.entry("LocalGpsdAdapter", new Entry(new ai.foresignal.source.adapters.local.LocalGpsdAdapter(1,
                    new ai.foresignal.source.codec.GpsdCodec()).descriptor(), "state")),
            Map.entry("StationReadsbAdapter", new Entry(
                    new ai.foresignal.source.adapters.local.StationReadsbAdapter(1).descriptor(), "state")),
            Map.entry("DavisWllAdapter", new Entry(
                    new ai.foresignal.source.adapters.local.DavisWllAdapter(1).descriptor(), "state")),
            Map.entry("EcowittAdapter", new Entry(
                    new ai.foresignal.source.adapters.local.EcowittAdapter(1, null).descriptor(), "state")),
            // Prompt 070: transit vehicles are TRACK (trackWindow, never a default); service alerts are the event class.
            Map.entry("TransitVehicleAdapter", new Entry(new ai.foresignal.source.adapters.transit.TransitVehicleAdapter(
                    ai.foresignal.source.adapters.transit.TransitFeeds.reviewed("mbta"), null, null).descriptor(), "track")),
            Map.entry("TransitAlertsAdapter", new Entry(new ai.foresignal.source.adapters.transit.TransitAlertsAdapter(
                    ai.foresignal.source.adapters.transit.TransitFeeds.reviewed("mbta"), null).descriptor(), "event")),
            // Prompt 071: bikeshare station status is LATEST station state, the state class (365 d).
            Map.entry("GbfsAdapter", new Entry(new ai.foresignal.source.adapters.gbfs.GbfsAdapter(
                    ai.foresignal.source.adapters.gbfs.GbfsSystems.reviewed("nyc-citibike"), null).descriptor(), "state")),
            // Prompt 072: camera catalogs are reference data (90 d of history).
            Map.entry("CctvPackAdapter", new Entry(new ai.foresignal.source.adapters.cctv.CctvPackAdapter(
                    ai.foresignal.source.adapters.cctv.CctvPacks.reviewed("cctv-tfl"), null, null).descriptor(),
                    "reference")),
            // Prompt 074: road events (incidents, closures, roadworks) are the event class (1825 d).
            Map.entry("Open511Adapter", new Entry(new ai.foresignal.source.adapters.roads.Open511Adapter(
                    ai.foresignal.source.adapters.roads.Open511Server.REVIEWED.getFirst(), null).descriptor(), "event")),
            Map.entry("WzdxAdapter", new Entry(new ai.foresignal.source.adapters.roads.WzdxAdapter(
                    ai.foresignal.source.adapters.roads.WzdxRegistry.load().getFirst(), null).descriptor(), "event")),
            Map.entry("DigitrafficRoadAdapter", new Entry(new ai.foresignal.source.adapters.roads.DigitrafficRoadAdapter(
                    "", () -> "dev", null).descriptor(), "event")),
            Map.entry("NdwSituationsAdapter", new Entry(new ai.foresignal.source.adapters.roads.NdwSituationsAdapter(
                    ai.foresignal.source.adapters.roads.NdwSituationsAdapter.FEEDS.getFirst(), null).descriptor(),
                    "event")),
            // Prompt 075: fire perimeters and incidents and cyclone geometry are the event class (1825 d); one
            // CwfisAdapter class serves three sources and is checked through cwfis-perimeters (event). cwfis-hotspots
            // keeps fire-detection's 7 d (§2.4, as goes-fdc and nasa-firms) and cwfis-fwi-stations the state class
            // (365 d); both are asserted in CwfisAdapterTest. The station directory is reference data (90 d).
            Map.entry("NifcPerimetersAdapter", new Entry(new ai.foresignal.source.adapters.fire.NifcPerimetersAdapter(
                    "", (java.util.function.Supplier<io.micrometer.core.instrument.MeterRegistry>) null).descriptor(),
                    "event")),
            Map.entry("InciwebIncidentsAdapter", new Entry(new ai.foresignal.source.adapters.fire.InciwebIncidentsAdapter(
                    "", (java.util.function.Supplier<io.micrometer.core.instrument.MeterRegistry>) null).descriptor(),
                    "event")),
            Map.entry("CwfisAdapter", new Entry(new ai.foresignal.source.adapters.fire.CwfisAdapter(
                    ai.foresignal.source.adapters.fire.CwfisLayer.reviewed("cwfis-perimeters"), "", null).descriptor(),
                    "event")),
            Map.entry("RadioBrowserAdapter", new Entry(new ai.foresignal.source.adapters.radio.RadioBrowserAdapter(
                    "", new java.util.Random(1), null).descriptor(), "reference")),
            Map.entry("NhcCycloneConesAdapter", new Entry(new ai.foresignal.source.adapters.nhc.NhcCycloneConesAdapter(
                    "", (java.util.function.Supplier<io.micrometer.core.instrument.MeterRegistry>) null).descriptor(),
                    "event")),
            // Prompt 076: KEV entries, C2 sightings, CVEs and their EPSS bands are the event class (1825 d); URLhaus
            // malware URLs and CT-log hits are bulk-event (90 d).
            Map.entry("CisaKevAdapter", new Entry(new ai.foresignal.source.adapters.cisa.CisaKevAdapter("")
                    .descriptor(), "event")),
            Map.entry("FeodoAdapter", new Entry(new ai.foresignal.source.adapters.abusech.FeodoAdapter("", null)
                    .descriptor(), "event")),
            Map.entry("UrlhausAdapter", new Entry(new ai.foresignal.source.adapters.abusech.UrlhausAdapter("")
                    .descriptor(), "bulk-event")),
            Map.entry("NvdCvesAdapter", new Entry(new ai.foresignal.source.adapters.nvd.NvdCvesAdapter("",
                    java.time.Duration.ZERO, d -> { }).descriptor(), "event")),
            Map.entry("CtDomainWatchAdapter", new Entry(new ai.foresignal.source.adapters.ct.CtDomainWatchAdapter("",
                    java.time.Duration.ZERO, d -> { }).descriptor(), "bulk-event")),
            // Prompt 077: space weather alerts, flares, DONKI events, fireballs and close approaches are the event
            // class (1825 d); Kp, solar wind, the current X-ray class and aurora at places are SAMPLED state (365 d;
            // swpc-xray keeps flares 1825 d through kindRetention); SatNOGS stations are reference data (90 d);
            // radiosonde telemetry is high-volume bulk-event data (90 d).
            Map.entry("SwpcAlertsAdapter", new Entry(new ai.foresignal.source.adapters.space.swpc.SwpcAlertsAdapter("")
                    .descriptor(), "event")),
            Map.entry("SwpcKpAdapter", new Entry(new ai.foresignal.source.adapters.space.swpc.SwpcKpAdapter("")
                    .descriptor(), "state")),
            Map.entry("SwpcRtswAdapter", new Entry(new ai.foresignal.source.adapters.space.swpc.SwpcRtswAdapter("")
                    .descriptor(), "state")),
            Map.entry("SwpcXrayAdapter", new Entry(new ai.foresignal.source.adapters.space.swpc.SwpcXrayAdapter("")
                    .descriptor(), "state")),
            Map.entry("SwpcAuroraAdapter", new Entry(new ai.foresignal.source.adapters.space.swpc.SwpcAuroraAdapter(
                    (java.util.function.Supplier<ai.foresignal.source.spi.PlacePoints>) null, "").descriptor(), "state")),
            Map.entry("NasaDonkiAdapter", new Entry(new ai.foresignal.source.adapters.space.donki.NasaDonkiAdapter("")
                    .descriptor(), "event")),
            Map.entry("JplFireballsAdapter", new Entry(new ai.foresignal.source.adapters.space.jpl.JplFireballsAdapter("")
                    .descriptor(), "event")),
            Map.entry("JplCloseApproachesAdapter", new Entry(
                    new ai.foresignal.source.adapters.space.jpl.JplCloseApproachesAdapter("").descriptor(), "event")),
            Map.entry("SatnogsStationsAdapter", new Entry(
                    new ai.foresignal.source.adapters.space.satnogs.SatnogsStationsAdapter("").descriptor(), "reference")),
            Map.entry("SondehubAdapter", new Entry(new ai.foresignal.source.adapters.space.sondehub.SondehubAdapter(
                    (java.util.function.Supplier<ai.foresignal.source.spi.PlacePoints>) null, "").descriptor(),
                    "bulk-event")),
            Map.entry("LocalSondeAdapter", new Entry(new ai.foresignal.source.adapters.space.localsonde.LocalSondeAdapter(
                    new ai.foresignal.source.codec.AutoRxPayloadCodec()).descriptor(), "bulk-event")),
            // Prompt 078: air quality, radiation, buoy and LAN monitor readings are sensor status, the state class
            // (365 d).
            Map.entry("OpenAqAdapter", new Entry(new ai.foresignal.source.adapters.env.openaq.OpenAqAdapter("",
                    () -> null).descriptor(), "state")),
            Map.entry("SafecastRadiationAdapter", new Entry(
                    new ai.foresignal.source.adapters.env.safecast.SafecastRadiationAdapter("", () -> null).descriptor(),
                    "state")),
            Map.entry("EpaRadnetAdapter", new Entry(new ai.foresignal.source.adapters.env.radnet.EpaRadnetAdapter("",
                    () -> null).descriptor(), "state")),
            Map.entry("NdbcBuoysAdapter", new Entry(new ai.foresignal.source.adapters.env.ndbc.NdbcBuoysAdapter("",
                    () -> null).descriptor(), "state")),
            Map.entry("SensorCommunityAdapter", new Entry(
                    new ai.foresignal.source.adapters.env.sensorcommunity.SensorCommunityAdapter("", () -> null)
                            .descriptor(), "state")),
            Map.entry("AirNowHourlyAdapter", new Entry(new ai.foresignal.source.adapters.env.airnow.AirNowHourlyAdapter(
                    "", () -> null).descriptor(), "state")),
            Map.entry("AirqLocalAdapter", new Entry(new ai.foresignal.source.adapters.env.airq.AirqLocalAdapter(1)
                    .descriptor(), "state")),
            // Prompt 079: gauge, snow and tide readings are sensor status, the state class (365 d); the weekly drought
            // map and the EA flood warnings are hazards, the event class (1825 d).
            Map.entry("UsgsWaterAdapter", new Entry(new ai.foresignal.source.adapters.hydro.usgswater.UsgsWaterAdapter(
                    "", () -> null).descriptor(), "state")),
            Map.entry("NwpsGaugesAdapter", new Entry(new ai.foresignal.source.adapters.hydro.nwps.NwpsGaugesAdapter("",
                    () -> null).descriptor(), "state")),
            Map.entry("SnotelAdapter", new Entry(new ai.foresignal.source.adapters.hydro.snotel.SnotelAdapter("",
                    () -> null).descriptor(), "state")),
            Map.entry("UsdmAdapter", new Entry(new ai.foresignal.source.adapters.hydro.usdm.UsdmAdapter("")
                    .descriptor(), "event")),
            Map.entry("NoaaCoopsTidesAdapter", new Entry(
                    new ai.foresignal.source.adapters.hydro.coops.NoaaCoopsTidesAdapter("").descriptor(), "state")),
            Map.entry("EaFloodWarningsAdapter", new Entry(
                    new ai.foresignal.source.adapters.hydro.ea.EaFloodWarningsAdapter("", () -> null).descriptor(),
                    "event")),
            Map.entry("EcccHydrometricAdapter", new Entry(
                    new ai.foresignal.source.adapters.hydro.eccc.EcccHydrometricAdapter("", () -> null).descriptor(),
                    "state")),
            // Prompt 052: the derived weather sources, retained per Weather-Imagery-Design §2.4.
            Map.entry("WxPlacesGenerator", new Entry(
                    new ai.foresignal.source.adapters.wx.WxPlacesGenerator().descriptor(), "weather")),
            Map.entry("WxAreasGenerator", new Entry(
                    new ai.foresignal.source.adapters.wx.WxAreasGenerator().descriptor(), "weather")),
            Map.entry("WxCellsGenerator", new Entry(
                    new ai.foresignal.source.adapters.wx.WxCellsGenerator("small").descriptor(), "weather")),
            // Checkpoint 057: the SatDump station source (049), one per station, was missing. Its three kinds keep
            // their own §2.4 windows (ground-station 30 d, satellite-pass 365 d, raster-frame 7 d).
            Map.entry("SatDumpAdapter", new Entry(new ai.foresignal.source.adapters.satdump.SatDumpAdapter("itest", null,
                    null).descriptor(), "station")));

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

    /** Weather-Imagery-Design §2.4's per-kind retention of a SatDump station's kinds, for the {@code station} class. */
    static final Map<String, Duration> STATION_KIND_RETENTION = Map.of(
            "ground-station", Duration.ofDays(30),
            "satellite-pass", Duration.ofDays(365),
            ProductSpec.RECORD_KIND, Duration.ofDays(7));

    /** The design's raster-frame record retention (Weather-Imagery-Design §2.4). */
    static final Duration RASTER_FRAME_RETENTION = Duration.ofDays(7);

    /** Tables retention may delete from: versions and operational rows, never intelligence tables. */
    // Checkpoint 057: history_downsample is 044's bookkeeping of downsampled ranges (HistoryDownsampler), an
    // operational table that retention maintains alongside record_version.
    static final Set<String> RETENTION_TABLES = Set.of("record_version", "fetch_log", "seq_clock", "alert",
            "source_usage", "notification", "history_downsample");

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
            if (analyticClass.equals("station")) {
                assertThat(descriptor.kinds()).as("%s kinds", descriptor.id())
                        .containsExactlyInAnyOrderElementsOf(STATION_KIND_RETENTION.keySet());
                for (String kind : descriptor.kinds()) {
                    assertThat(descriptor.kindRetention().get(kind)).as("%s kindRetention %s", descriptor.id(), kind)
                            .isEqualTo(STATION_KIND_RETENTION.get(kind));
                }
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
            if (analyticClass.equals("track")) {
                // Prompt 069: TRACK retention is trackWindow plus downsampling, never defaultRetention.
                assertThat(descriptor.isTrack()).as("%s is TRACK", descriptor.id()).isTrue();
                assertThat(descriptor.defaultRetention()).as("%s defaultRetention", descriptor.id()).isNull();
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
