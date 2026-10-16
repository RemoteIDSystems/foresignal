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
            Map.entry("GoesWestAbiAdapter", new Entry(new ai.foresignal.source.adapters.goes.GoesWestAbiAdapter(null, "",
                    "small", null).descriptor(), "raster")),
            // Prompt 107: the local GRB GLM station feeds the shared ring and emits lightning-cluster.
            Map.entry("GlmLocalAdapter", new Entry(new ai.foresignal.source.adapters.goes.GlmLocalAdapter("home", null,
                    null, null).descriptor(), "weather")),
            Map.entry("GfsAdapter", new Entry(new ai.foresignal.source.adapters.gfs.GfsAdapter(
                    new ai.foresignal.raster.model.ModelRunIngest(null, null), "").descriptor(), "raster")),
            Map.entry("HrrrAdapter", new Entry(new ai.foresignal.source.adapters.hrrr.HrrrAdapter(
                    new ai.foresignal.raster.model.ModelRunIngest(null, null), "small", "").descriptor(), "raster")),
            // Prompt 105: ECMWF IFS, the third forecast model.
            Map.entry("EcmwfAdapter", new Entry(new ai.foresignal.source.adapters.ecmwf.EcmwfAdapter(
                    new ai.foresignal.raster.model.EcmwfRunIngest(null, null), "").descriptor(), "raster")),
            // Prompt 106: GFS-Wave and the ECMWF AIFS machine-learning forecast.
            Map.entry("GfsWaveAdapter", new Entry(new ai.foresignal.source.adapters.gfswave.GfsWaveAdapter(
                    new ai.foresignal.raster.model.ModelRunIngest(null, null), "").descriptor(), "raster")),
            Map.entry("AifsAdapter", new Entry(new ai.foresignal.source.adapters.ecmwf.AifsAdapter(
                    new ai.foresignal.raster.model.EcmwfRunIngest(null, null), "small", "").descriptor(), "raster")),
            Map.entry("SstAdapter", new Entry(new ai.foresignal.source.adapters.sst.SstAdapter(null, "", "small", null)
                    .descriptor(), "raster")),
            Map.entry("HeatRiskAdapter", new Entry(new ai.foresignal.source.adapters.heatrisk.HeatRiskAdapter("", "small")
                    .descriptor(), "raster")),
            Map.entry("NexradL3Adapter", new Entry(new ai.foresignal.source.adapters.nexrad.NexradL3Adapter("", null)
                    .descriptor(), "raster")),
            Map.entry("EpaUvAdapter", new Entry(new ai.foresignal.source.adapters.env.epauv.EpaUvAdapter(
                    (java.util.function.Supplier<ai.foresignal.source.spi.PlacePoints>) null, "").descriptor(),
                    "weather")),
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
            // Prompt 105: forecast threshold areas and TITAN-lite storm tracks.
            Map.entry("WxForecastAreasGenerator", new Entry(
                    new ai.foresignal.source.adapters.wx.WxForecastAreasGenerator().descriptor(), "weather")),
            Map.entry("WxStormTracksGenerator", new Entry(
                    new ai.foresignal.source.adapters.wx.WxStormTracksGenerator("small").descriptor(), "weather")),
            // Checkpoint 057: the SatDump station source (049), one per station, was missing. Its three kinds keep
            // their own §2.4 windows (ground-station 30 d, satellite-pass 365 d, raster-frame 7 d).
            // Prompt 080: the rail and supplemental aircraft sources are TRACK (trackWindow, never a default).
            Map.entry("AirplanesLiveMilAdapter", new Entry(
                    new ai.foresignal.source.adapters.airplaneslive.AirplanesLiveMilAdapter("").descriptor(), "track")),
            Map.entry("AdsbFiMilAdapter", new Entry(
                    new ai.foresignal.source.adapters.adsbfi.AdsbFiMilAdapter("").descriptor(), "track")),
            Map.entry("AdsbFiRegionAdapter", new Entry(new ai.foresignal.source.adapters.adsbfi.AdsbFiRegionFamily("")
                    .adapters().getFirst().descriptor(), "track")),
            Map.entry("DigitrafficRailAdapter", new Entry(new ai.foresignal.source.adapters.digitraffic.rail
                    .DigitrafficRailAdapter("", () -> "dev").descriptor(), "track")),
            Map.entry("AmtrakerAdapter", new Entry(
                    new ai.foresignal.source.adapters.amtraker.AmtrakerAdapter("").descriptor(), "track")),
            // Prompt 081: conflict events and civil alerts are the event class (1825 d), the DeepState map too.
            Map.entry("DeepStateAdapter", new Entry(
                    new ai.foresignal.source.adapters.deepstate.DeepStateAdapter("").descriptor(), "event")),
            Map.entry("AlertsInUaAdapter", new Entry(new ai.foresignal.source.adapters.alertsinua.AlertsInUaAdapter("",
                    new ai.foresignal.reference.ukr.UaRegionIndex()).descriptor(), "event")),
            Map.entry("UaSirenAdapter", new Entry(new ai.foresignal.source.adapters.uasiren.UaSirenAdapter("",
                    new ai.foresignal.reference.ukr.UaRegionIndex()).descriptor(), "event")),
            Map.entry("AcledAdapter", new Entry(new ai.foresignal.source.adapters.acled.AcledAdapter("",
                    new ai.foresignal.reference.CountryCentroids()).descriptor(), "event")),
            Map.entry("UcdpGedAdapter", new Entry(new ai.foresignal.source.adapters.ucdp.UcdpGedAdapter("",
                    new ai.foresignal.reference.CountryCentroids()).descriptor(), "event")),
            // Prompt 082: PSK Reporter spots and APRS beacons are bulk-event (90 d); the KiwiSDR and OpenMHz lists
            // are reference data (90 d of history).
            Map.entry("PskReporterAdapter", new Entry(new ai.foresignal.source.adapters.radio.PskReporterAdapter("",
                    (java.util.function.Supplier<io.micrometer.core.instrument.MeterRegistry>) null).descriptor(),
                    "bulk-event")),
            Map.entry("KiwiSdrAdapter", new Entry(new ai.foresignal.source.adapters.radio.KiwiSdrAdapter("",
                    (java.util.function.Supplier<io.micrometer.core.instrument.MeterRegistry>) null).descriptor(),
                    "reference")),
            Map.entry("OpenMhzAdapter", new Entry(
                    new ai.foresignal.source.adapters.radio.OpenMhzAdapter("").descriptor(), "reference")),
            Map.entry("AprsIsAdapter", new Entry(new ai.foresignal.source.adapters.radio.AprsIsAdapter(() -> "abcd1234",
                    () -> "dev", new ai.foresignal.source.formats.aprs.AprsPacketDedup(), null).descriptor(),
                    "bulk-event")),
            Map.entry("LocalAprsAdapter", new Entry(new ai.foresignal.source.adapters.radio.LocalAprsAdapter(1,
                    new ai.foresignal.source.codec.KissAx25Codec(), new ai.foresignal.source.formats.aprs.AprsPacketDedup(),
                    null).descriptor(), "bulk-event")),
            // Prompt 083: mesh nodes are station-like state (365 d), messages with them.
            Map.entry("MeshtasticMqttAdapter", new Entry(new ai.foresignal.source.adapters.mesh.MeshtasticMqttAdapter(null,
                    () -> "abcd1234", new ai.foresignal.source.adapters.mesh.MeshMessageBus(), null, "").descriptor(),
                    "state")),
            Map.entry("MeshtasticLocalAdapter", new Entry(new ai.foresignal.source.adapters.mesh.MeshtasticLocalAdapter(
                    null, () -> "abcd1234", new ai.foresignal.source.adapters.mesh.MeshMessageBus(), null).descriptor(),
                    "state")),
            Map.entry("MeshtasticMapAdapter", new Entry(
                    new ai.foresignal.source.adapters.mesh.MeshtasticMapAdapter("").descriptor(), "state")),
            Map.entry("MeshCoreLocalAdapter", new Entry(new ai.foresignal.source.adapters.mesh.MeshCoreLocalAdapter(null,
                    new ai.foresignal.source.adapters.mesh.MeshMessageBus()).descriptor(), "state")),
            Map.entry("MeshCoreBridgeAdapter", new Entry(
                    new ai.foresignal.source.adapters.mesh.MeshCoreBridgeAdapter().descriptor(), "state")),
            Map.entry("MeshReportsAdapter", new Entry(
                    new ai.foresignal.source.adapters.mesh.MeshReportsAdapter().descriptor(), "event")),
            // Prompt 085: the keyless vessel sources are TRACK (trackWindow, never a default).
            Map.entry("KystverketAdapter", new Entry(new ai.foresignal.source.adapters.ais.KystverketAdapter(
                    new ai.foresignal.source.codec.AivdmSocketCodec(() -> null)).descriptor(), "track")),
            Map.entry("DigitrafficAisAdapter", new Entry(new ai.foresignal.source.adapters.ais.DigitrafficAisAdapter(
                    null, () -> "abcd1234", () -> "dev", "", "").descriptor(), "track")),
            Map.entry("LocalAisAdapter", new Entry(new ai.foresignal.source.adapters.ais.LocalAisAdapter(1,
                    new ai.foresignal.source.codec.AivdmSocketCodec(() -> null)).descriptor(), "track")),
            // Prompt 086: aviation datalink metadata is private and kept 7 days by the prompt (the "privacy" class);
            // Remote ID drones are TRACK.
            Map.entry("LocalAcarsAdapter", new Entry(new ai.foresignal.source.adapters.acars.LocalAcarsAdapter(1,
                    new ai.foresignal.source.codec.AcarsJsonCodec()).descriptor(), "privacy")),
            Map.entry("LocalRemoteIdAdapter", new Entry(new ai.foresignal.source.adapters.remoteid.LocalRemoteIdAdapter(
                    null, () -> "abcd1234", null).descriptor(), "track")),
            Map.entry("SatDumpAdapter", new Entry(new ai.foresignal.source.adapters.satdump.SatDumpAdapter("itest", null,
                    null).descriptor(), "station")),
            // Prompt 087: TFRs and NOTAM-like restrictions, airport delay programs and SIGMET/AIRMET/CWA hazards are
            // the event class (1825 d); PIREPs are high-volume reports, bulk-event (90 d); the UAS Facility Map grid is
            // reference data (90 d of history); TAFs keep the 7 days of METARs (the "weather" class; §2.4 names no
            // TAF window, see the 087 report).
            Map.entry("FaaTfrAdapter", new Entry(
                    new ai.foresignal.source.adapters.faa.FaaTfrAdapter("").descriptor(), "event")),
            Map.entry("FaaNasStatusAdapter", new Entry(
                    new ai.foresignal.source.adapters.faa.FaaNasStatusAdapter("", () -> null).descriptor(), "event")),
            Map.entry("FaaUasfmAdapter", new Entry(
                    new ai.foresignal.source.adapters.faa.FaaUasfmAdapter("").descriptor(), "reference")),
            Map.entry("AwcSigmetsAdapter", new Entry(
                    new ai.foresignal.source.adapters.awc.AwcSigmetsAdapter("").descriptor(), "event")),
            Map.entry("AwcGairmetsAdapter", new Entry(
                    new ai.foresignal.source.adapters.awc.AwcGairmetsAdapter("").descriptor(), "event")),
            Map.entry("AwcCwasAdapter", new Entry(
                    new ai.foresignal.source.adapters.awc.AwcCwasAdapter("").descriptor(), "event")),
            Map.entry("AwcPirepsAdapter", new Entry(
                    new ai.foresignal.source.adapters.awc.AwcPirepsAdapter("").descriptor(), "bulk-event")),
            Map.entry("AwcTafsAdapter", new Entry(new ai.foresignal.source.adapters.awc.AwcTafsAdapter(
                    (AreaOfInterest) null, "").descriptor(), "weather")),
            // Prompt 088: navigational warnings and vessel events are the event class (1825 d); daily chokepoint
            // transits are a measurement series (3650 d); imf-portwatch-ports is checked as reference, the class of
            // its port catalog (90 d), and its port-activity kind keeps the series window (3650 d), which is longer.
            Map.entry("NgaMsiAdapter", new Entry(
                    new ai.foresignal.source.adapters.maritime.NgaMsiAdapter("").descriptor(), "event")),
            Map.entry("PortWatchChokepointsAdapter", new Entry(
                    new ai.foresignal.source.adapters.maritime.PortWatchChokepointsAdapter("").descriptor(), "series")),
            Map.entry("PortWatchPortsAdapter", new Entry(
                    new ai.foresignal.source.adapters.maritime.PortWatchPortsAdapter("").descriptor(), "reference")),
            Map.entry("GfwEventsAdapter", new Entry(
                    new ai.foresignal.source.adapters.maritime.GfwEventsAdapter("").descriptor(), "event")),
            // Prompt 089: CAP alerts (cap-*), MeteoAlarm, the FDSN event services (fdsn-*), emsc-seismic and
            // usgs-shakemap are the event class (1825 d).
            Map.entry("CapFeedAdapter", new Entry(new ai.foresignal.source.adapters.cap.CapFeedAdapter(
                    new ai.foresignal.source.adapters.cap.CapFeed("cap-test-xx", "XX", "Test",
                            java.net.URI.create("https://example.org/cap.xml"), "atom-index", java.util.List.of("en"),
                            "test", java.net.URI.create("https://example.org/terms"), "2026-10-14", "test"))
                    .descriptor(), "event")),
            Map.entry("MeteoAlarmAdapter", new Entry(new ai.foresignal.source.adapters.meteoalarm.MeteoAlarmAdapter(
                    new ai.foresignal.source.adapters.meteoalarm.MeteoAlarmCountries.Country("test", "XX", "Test"),
                    null).descriptor(), "event")),
            Map.entry("FdsnEventAdapter", new Entry(new ai.foresignal.source.adapters.seismic.FdsnEventAdapter(
                    new ai.foresignal.source.adapters.seismic.FdsnService("test", "Test",
                            java.net.URI.create("https://example.org/fdsnws/event/1/query"), true, "test",
                            java.net.URI.create("https://example.org/terms"), "Test",
                            java.net.URI.create("https://example.org"), null)).descriptor(), "event")),
            Map.entry("EmscSeismicAdapter", new Entry(
                    new ai.foresignal.source.adapters.seismic.EmscSeismicAdapter(null, null).descriptor(), "event")),
            Map.entry("UsgsShakeMapAdapter", new Entry(
                    new ai.foresignal.source.adapters.usgs.UsgsShakeMapAdapter(null).descriptor(), "event")),
            // Prompt 090: the humanitarian sources. Disasters, outbreaks, activations and declarations are the
            // event class; volcano alert levels and travel advisories are slowly-changing state (365 d); UNHCR
            // population is a yearly measurement series (3650 d).
            Map.entry("EonetAdapter", new Entry(
                    new ai.foresignal.source.adapters.humanitarian.EonetAdapter(null).descriptor(), "event")),
            Map.entry("ReliefWebDisastersAdapter", new Entry(new ai.foresignal.source.adapters.humanitarian
                    .ReliefWebDisastersAdapter(null).descriptor(), "event")),
            Map.entry("WhoDonAdapter", new Entry(
                    new ai.foresignal.source.adapters.humanitarian.WhoDonAdapter(null,
                            new ai.foresignal.reference.CountryCentroids()).descriptor(), "event")),
            Map.entry("CopernicusEmsAdapter", new Entry(
                    new ai.foresignal.source.adapters.humanitarian.CopernicusEmsAdapter(null).descriptor(), "event")),
            Map.entry("OpenFemaDeclarationsAdapter", new Entry(new ai.foresignal.source.adapters.humanitarian
                    .OpenFemaDeclarationsAdapter(null,
                            ai.foresignal.source.spi.CountyGeometries.none()).descriptor(), "event")),
            Map.entry("UnhcrPopulationAdapter", new Entry(
                    new ai.foresignal.source.adapters.humanitarian.UnhcrPopulationAdapter(null,
                            new ai.foresignal.reference.CountryCentroids()).descriptor(), "series")),
            Map.entry("UsgsHansVolcanoAdapter", new Entry(new ai.foresignal.source.adapters.humanitarian
                    .UsgsHansVolcanoAdapter(null).descriptor(), "state")),
            Map.entry("GeoNetVolcanoAdapter", new Entry(new ai.foresignal.source.adapters.humanitarian
                    .GeoNetVolcanoAdapter(null).descriptor(), "state")),
            Map.entry("UsTravelAdvisoryAdapter", new Entry(new ai.foresignal.source.adapters.humanitarian
                    .UsTravelAdvisoryAdapter(null, new ai.foresignal.reference.CountryCentroids(),
                            new ai.foresignal.reference.BundledCountryPolygons()).descriptor(), "state")),
            // Prompt 091: sanctions entries and SEC filings are the event class (1825 d).
            Map.entry("OfacSdnAdapter", new Entry(
                    new ai.foresignal.source.adapters.sanctions.OfacSdnAdapter(null).descriptor(), "event")),
            Map.entry("UkSanctionsAdapter", new Entry(
                    new ai.foresignal.source.adapters.sanctions.UkSanctionsAdapter(null).descriptor(), "event")),
            Map.entry("UnSanctionsAdapter", new Entry(
                    new ai.foresignal.source.adapters.sanctions.UnSanctionsAdapter(null).descriptor(), "event")),
            Map.entry("Sec8kAdapter", new Entry(
                    new ai.foresignal.source.adapters.sec.Sec8kAdapter(null, null).descriptor(), "event")),
            // Prompt 092: internet measurement. Outages, BGP events, jamming cells and service incidents are the
            // event class; RIPE Atlas probe status is slowly-changing state (365 d).
            Map.entry("CloudflareOutageAdapter", new Entry(new ai.foresignal.source.adapters.internet
                    .CloudflareOutageAdapter(null, new ai.foresignal.reference.CountryCentroids()).descriptor(),
                    "event")),
            Map.entry("CloudflareBgpAdapter", new Entry(new ai.foresignal.source.adapters.internet
                    .CloudflareBgpAdapter(null, new ai.foresignal.reference.CountryCentroids()).descriptor(),
                    "event")),
            Map.entry("GpsjamAdapter", new Entry(
                    new ai.foresignal.source.adapters.internet.GpsjamAdapter(null).descriptor(), "event")),
            Map.entry("StatusPageAdapter", new Entry(new ai.foresignal.source.adapters.internet.StatusPageAdapter(
                    new ai.foresignal.source.adapters.internet.StatusPageFamily.Page("status-test", "example.com",
                            "Test"), null).descriptor(), "event")),
            Map.entry("GoogleStatusAdapter", new Entry(new ai.foresignal.source.adapters.internet.GoogleStatusAdapter(
                    new ai.foresignal.source.adapters.internet.GoogleStatusFamily.Page("status-test", "Test",
                            "https://example.com/incidents.json", "example.com"), null).descriptor(), "event")),
            Map.entry("AtlasProbesAdapter", new Entry(
                    new ai.foresignal.source.adapters.internet.AtlasProbesAdapter(null).descriptor(), "state")),
            Map.entry("RisLiveAdapter", new Entry(
                    new ai.foresignal.source.adapters.internet.RisLiveAdapter(null).descriptor(), "event")),
            // Prompt 093: the energy and markets series (series class, 3650 d) and the two grid-frequency sources
            // (their excursions are events, 1825 d).
            Map.entry("EiaPetroleumAdapter", new Entry(
                    new ai.foresignal.source.adapters.eia.EiaPetroleumAdapter("").descriptor(), "series")),
            Map.entry("Eia930Adapter", new Entry(
                    new ai.foresignal.source.adapters.eia.Eia930Adapter("").descriptor(), "series")),
            Map.entry("AgsiAdapter", new Entry(
                    new ai.foresignal.source.adapters.gie.AgsiAdapter("").descriptor(), "series")),
            Map.entry("EmberAdapter", new Entry(
                    new ai.foresignal.source.adapters.ember.EmberAdapter("").descriptor(), "series")),
            Map.entry("CarbonIntensityAdapter", new Entry(
                    new ai.foresignal.source.adapters.carbon.CarbonIntensityAdapter("").descriptor(), "series")),
            Map.entry("ErcotAdapter", new Entry(
                    new ai.foresignal.source.adapters.ercot.ErcotAdapter("", "").descriptor(), "series")),
            Map.entry("GridFrequencyGbAdapter", new Entry(
                    new ai.foresignal.source.adapters.elexon.GridFrequencyGbAdapter("").descriptor(), "event")),
            Map.entry("GridFrequencyFingridAdapter", new Entry(
                    new ai.foresignal.source.adapters.fingrid.GridFrequencyFingridAdapter("").descriptor(),
                    "event")),
            // Prompt 094: the slow-changing reference layers (power plants, airports, cables, volcanoes, ports,
            // park boundaries, launch sites, nuclear facilities) are the reference class, 730 d.
            Map.entry("WriPowerPlantsAdapter", new Entry(
                    new ai.foresignal.source.adapters.wri.WriPowerPlantsAdapter("").descriptor(), "reference")),
            Map.entry("OurAirportsAdapter", new Entry(
                    new ai.foresignal.source.adapters.ourairports.OurAirportsAdapter("").descriptor(), "reference")),
            Map.entry("SubmarineCablesAdapter", new Entry(
                    new ai.foresignal.source.adapters.submarinecables.SubmarineCablesAdapter("").descriptor(),
                    "reference")),
            Map.entry("GvpVolcanoesAdapter", new Entry(
                    new ai.foresignal.source.adapters.gvp.GvpVolcanoesAdapter("").descriptor(), "reference")),
            Map.entry("WorldPortIndexAdapter", new Entry(
                    new ai.foresignal.source.adapters.worldportindex.WorldPortIndexAdapter("").descriptor(),
                    "reference")),
            Map.entry("NpsBoundariesAdapter", new Entry(
                    new ai.foresignal.source.adapters.nps.NpsBoundariesAdapter("").descriptor(), "reference")),
            Map.entry("SpaceportsAdapter", new Entry(
                    new ai.foresignal.source.adapters.spaceports.SpaceportsAdapter("").descriptor(), "reference")),
            Map.entry("NuclearReactorsUsAdapter", new Entry(
                    new ai.foresignal.source.adapters.fema.NuclearReactorsUsAdapter("").descriptor(), "reference")),
            Map.entry("NuclearPlantsWikidataAdapter", new Entry(
                    new ai.foresignal.source.adapters.wikidata.NuclearPlantsWikidataAdapter("").descriptor(),
                    "reference")),
            // Prompt 095: citizen science and signals. Observations and bird sightings and wastewater detections
            // are the event class (1825 d); wastewater levels are sensor-like state (365 d); Wikipedia edit surges
            // are the bulk-event class (90 d, named by the Conventions table).
            Map.entry("InatObservationsAdapter", new Entry(
                    new ai.foresignal.source.adapters.inat.InatObservationsAdapter("",
                            ai.foresignal.source.spi.PlaceTimeZones.none(),
                            (java.util.function.Supplier<ai.foresignal.source.spi.PlacePoints>) () -> null)
                            .descriptor(), "event")),
            Map.entry("EbirdNotableAdapter", new Entry(
                    new ai.foresignal.source.adapters.ebird.EbirdNotableAdapter("",
                            (java.util.function.Supplier<ai.foresignal.source.spi.PlacePoints>) () -> null,
                            ai.foresignal.source.spi.PlaceTimeZones.none()).descriptor(), "event")),
            Map.entry("NwssWvalAdapter", new Entry(
                    new ai.foresignal.source.adapters.cdc.NwssWvalAdapter("",
                            ai.foresignal.source.spi.CountyGeometries.none()).descriptor(), "state")),
            Map.entry("NwssDetectionAdapter", new Entry(
                    new ai.foresignal.source.adapters.cdc.NwssDetectionAdapter("",
                            ai.foresignal.source.spi.CountyGeometries.none()).descriptor(), "event")),
            Map.entry("WikiStreamAdapter", new Entry(
                    new ai.foresignal.source.adapters.wikistream.WikiStreamAdapter("").descriptor(), "bulk-event")),
            // Prompt 096: satellite scenes are high-volume event-like observations; the prompt's 90 d matches
            // the bulk-event class (RetentionResolverTest's table). Sentinel-2 and Landsat are on, Sentinel-1 off.
            Map.entry("EarthSearchAdapter", new Entry(new ai.foresignal.source.adapters.stac.EarthSearchAdapter(
                    ai.foresignal.source.adapters.stac.StacSources.reviewed("stac-sentinel2"), "",
                    (java.util.function.Supplier<ai.foresignal.source.spi.PlacePoints>) () -> null).descriptor(),
                    "bulk-event")),
            // Checkpoint 097: the prompt 062/063/064 spaceship sources were never classified here. The
            // aircraft (adsb.lol, OpenSky) and AIS WebSocket sources are TRACK (trackWindow, never a
            // default); celestrak-gp is a reference catalogue (90 d); SOCRATES conjunctions, launches and
            // Space-Track re-entry predictions are the event class (1825 d).
            Map.entry("AdsbLolAreaAdapter", new Entry(new ai.foresignal.source.adapters.adsblol.AdsbLolAreaFamily(
                    "").adapters().getFirst().descriptor(), "track")),
            Map.entry("AdsbLolMilAdapter", new Entry(
                    new ai.foresignal.source.adapters.adsblol.AdsbLolMilAdapter("").descriptor(), "track")),
            Map.entry("OpenSkyStatesAdapter", new Entry(
                    new ai.foresignal.source.adapters.opensky.OpenSkyStatesAdapter("", "").descriptor(), "track")),
            Map.entry("AisStreamAdapter", new Entry(new ai.foresignal.source.adapters.aisstream.AisStreamAdapter("",
                    new io.micrometer.core.instrument.simple.SimpleMeterRegistry(), java.time.Clock.systemUTC())
                    .descriptor(), "track")),
            Map.entry("CelestrakGpAdapter", new Entry(
                    new ai.foresignal.source.adapters.celestrak.CelestrakGpAdapter("").descriptor(), "reference")),
            Map.entry("CelestrakSocratesAdapter", new Entry(
                    new ai.foresignal.source.adapters.celestrak.CelestrakSocratesAdapter("").descriptor(), "event")),
            Map.entry("LaunchLibraryAdapter", new Entry(
                    new ai.foresignal.source.adapters.launchlibrary.LaunchLibraryAdapter("").descriptor(), "event")),
            Map.entry("SpaceTrackTipAdapter", new Entry(
                    new ai.foresignal.source.adapters.spacetrack.SpaceTrackTipAdapter("").descriptor(), "event")),
            Map.entry("SatellitePassesGenerator", new Entry(
                    new ai.foresignal.source.adapters.passes.SatellitePassesGenerator().descriptor(), "derived")));

    /**
     * The {@code privacy} class (prompt 086): private sources whose prompt sets a short raw window on purpose, because
     * the data is sensitive rather than analytic (aviation datalink metadata, 7 days). The window must be exactly the
     * prompt's and the source must be private.
     */
    static final Map<String, Duration> PRIVACY_RETENTION = Map.of("local-acars-1", Duration.ofDays(7));

    /**
     * Kinds whose prompt names a shorter window than their source's class, for classes the Conventions table does not
     * override (only {@code event} and {@code series} windows are raised): prompt 086's {@code gnss-integrity}, 30 days
     * on the {@code state}-class {@code local-gpsd-{n}}.
     */
    static final Map<String, Map<String, Duration>> PROMPT_SHORTER_KINDS = Map.of(
            "local-gpsd-1", Map.of("gnss-integrity", Duration.ofDays(30)),
            // Prompt 106: swpc-aurora gains a raster-frame raster (24 h), shorter than its 365 d record state.
            "swpc-aurora", Map.of(ProductSpec.RECORD_KIND, Duration.ofHours(24)));

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
            // Prompt 105: forecast areas and storm tracks/swaths (owner-chosen windows, §7.1).
            Map.entry("forecast-area", Duration.ofDays(7)),
            Map.entry("storm-track", Duration.ofDays(3)),
            Map.entry("storm-swath", Duration.ofDays(3)),
            Map.entry("storm-report", Duration.ofDays(30)),
            Map.entry("nws-forecast", Duration.ofDays(7)),
            // Prompt 106: EPA UV hourly forecasts.
            Map.entry("uv-forecast", Duration.ofDays(7)),
            // Prompt 087: TAFs, kept as long as METARs; not in §2.4 (owner question in the 087 report).
            Map.entry("taf", Duration.ofDays(7)));

    /** Weather-Imagery-Design §2.4's per-kind retention of a SatDump station's kinds, for the {@code station} class. */
    static final Map<String, Duration> STATION_KIND_RETENTION = Map.of(
            "ground-station", Duration.ofDays(30),
            "satellite-pass", Duration.ofDays(365),
            ProductSpec.RECORD_KIND, Duration.ofDays(7),
            // Prompt 107: DCS and EMWIN kinds from the station (3 d).
            "dcp-observation", Duration.ofDays(3),
            "emwin-product", Duration.ofDays(3),
            "emwin-warning", Duration.ofDays(3));

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
            if (analyticClass.equals("privacy")) {
                assertThat(descriptor.privateSource()).as("%s is private", descriptor.id()).isTrue();
                assertThat(descriptor.defaultRetention()).as("%s defaultRetention", descriptor.id())
                        .isEqualTo(PRIVACY_RETENTION.get(descriptor.id()));
                continue;
            }
            if (analyticClass.equals("derived")) {
                // Derived sources (prompt 052/064) set retention per kind; the registry refuses a
                // defaultRetention and a recordTtl on a DERIVED source.
                assertThat(descriptor.mode()).as("%s is DERIVED", descriptor.id())
                        .isEqualTo(ai.foresignal.source.spi.Mode.DERIVED);
                assertThat(descriptor.defaultRetention()).as("%s defaultRetention", descriptor.id()).isNull();
                assertThat(descriptor.recordTtl()).as("%s recordTtl", descriptor.id()).isNull();
                assertThat(descriptor.kindRetention()).as("%s kindRetention", descriptor.id()).isNotEmpty();
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
            Map<String, Duration> shorter = PROMPT_SHORTER_KINDS.getOrDefault(descriptor.id(), Map.of());
            descriptor.kindRetention().forEach((kind, window) -> {
                if (shorter.containsKey(kind)) {
                    assertThat(analyticClass).as("only event and series windows are raised").isNotIn("event", "series");
                    assertThat(window).as("%s kindRetention %s", descriptor.id(), kind).isEqualTo(shorter.get(kind));
                } else {
                    assertThat(window).as("%s kindRetention %s", descriptor.id(), kind).isGreaterThanOrEqualTo(minimum);
                }
            });
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
