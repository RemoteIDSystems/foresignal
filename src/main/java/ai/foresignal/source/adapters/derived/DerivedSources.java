package ai.foresignal.source.adapters.derived;

import java.util.Set;

/**
 * Source ids, families and kinds the derived layers consume and produce (prompt 099).
 *
 * <p>Families are prefixes because the aircraft and vessel receiver families are configured at
 * runtime ({@code local-adsb-1..16}, {@code adsbfi-region-1..4}, {@code adsblol-area-1..3}); a
 * generator resolves the prefixes against the {@link ai.foresignal.source.SourceRegistry} so it
 * subscribes to whichever slots exist.
 */
public final class DerivedSources {

    private DerivedSources() {}

    // ---------------------------------------------------------------- kinds

    /** The aircraft record kind. */
    public static final String AIRCRAFT = "aircraft";

    /** The vessel record kind. */
    public static final String VESSEL = "vessel";

    // ---------------------------------------------------------------- aircraft families

    /** Every ADS-B/MLAT aircraft source prefix, OpenSky included. */
    public static final Set<String> AIRCRAFT_PREFIXES =
            Set.of("adsblol-", "adsbfi-", "airplaneslive-", "opensky-", "local-adsb-", "local-sbs-");

    /** The readsb-format aircraft sources (everything except OpenSky, which lacks NACp). */
    public static final Set<String> READSB_PREFIXES =
            Set.of("adsblol-", "adsbfi-", "airplaneslive-", "local-adsb-", "local-sbs-");

    /** The military aircraft sources (prompt 080/062). */
    public static final Set<String> MIL_AIRCRAFT_SOURCES = Set.of("adsblol-mil", "airplaneslive-mil");

    // ---------------------------------------------------------------- vessel families

    /** Every AIS/vessel source prefix and id. */
    public static final Set<String> VESSEL_PREFIXES = Set.of("ais", "local-ais-");

    /** Naval ship type (AIS), used by the mil-naval layer. */
    public static final int NAVAL_SHIP_TYPE = 35;

    // ---------------------------------------------------------------- source ids

    /** The PortWatch chokepoint source. */
    public static final String CHOKEPOINTS = "imf-portwatch-chokepoints";

    /** The World Port Index source (prompt 094). */
    public static final String WORLD_PORT_INDEX = "world-port-index";

    /** OurAirports (prompt 094). */
    public static final String OURAIRPORTS = "ourairports";

    /** Submarine cables (prompt 094). */
    public static final String SUBMARINE_CABLES = "submarine-cables";

    /** USGS earthquakes. */
    public static final String USGS = "usgs";

    /** EMSC earthquakes. */
    public static final String EMSC = "emsc";

    /** FDSN earthquakes. */
    public static final String FDSN = "fdsn";

    /** FIRMS fire detections. */
    public static final String FIRMS = "nasa-firms";

    /** GOES fire detections. */
    public static final String GOES_FDC = "goes-fdc";

    /** GDELT conflict events. */
    public static final String GDELT = "gdelt";

    /** IODA outages. */
    public static final String IODA = "ioda";

    /** Cloudflare Radar outages. */
    public static final String CLOUDFLARE_RADAR = "cloudflare-radar";

    /** PSKReporter FT8 spots. */
    public static final String PSKREPORTER = "pskreporter-ft8";

    /** SWPC X-ray. */
    public static final String SWPC_XRAY = "swpc-xray";

    /** The GLM lightning source (kind {@code lightning-cluster}). */
    public static final String GOES_GLM = "goes-glm";

    /** The SAR vessel detections source (prompt 088, optional; not built). */
    public static final String SAR_DETECTIONS = "gfw-sar-detections";

    /** The output of the AIS-gap layer, consumed by the dark-vessel match. */
    public static final String DERIVED_AIS_GAP = "derived-ais-gap";
}
