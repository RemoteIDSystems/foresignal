package ai.foresignal.source.adapters.transit;

import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldThreshold;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.MotionSpec;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * The descriptors of the {@code transit-*} sources (prompt 070; Core-Data-Model §5.3 row "Transit and rail").
 *
 * <ul>
 *   <li><strong>Vehicles:</strong> POLLING every 30 s (minimum 15 s), budget 4/min and 3000/day; TRACK,
 *       {@code ttlAnchor} OBSERVED, TTL 5 min; SAMPLED {@code {minInterval 15s, keyframe 240s, minDeviationMeters 150,
 *       thresholds true, edgeMinInterval 8s}}, which satisfies {@code keyframe ≤ ttl − interval − 30 s}
 *       ({@code 240 ≤ 300 − 30 − 30}). <strong>An interval above 30 s needs a shorter keyframe</strong> (225 s at
 *       45 s). The policy validator checks the keyframe against the descriptor's default interval (30 s) only, not
 *       against an operator's interval, so an operator who slows the poll must shorten the keyframe too. Significant {@code routeId, tripId, directionId,
 *       label, mode}: {@code currentStatus} and {@code stopId} change at every stop and are deliberately not
 *       significant. Motion {@code {speedMs, MPS, bearing}}, threshold {@code bearing} 30° (angular).
 *   <li><strong>Alerts:</strong> POLLING every 60 s (minimum 30 s), budget 2/min and 1500/day; STANDARD, ALL, no
 *       TTL, {@code zeroResultsNormal}; retention 1825 d (the {@code event} class); the texts are embedded (untrusted).
 * </ul>
 *
 * <p>License, attribution and {@code egress} come from the feed entry.
 */
public final class TransitDescriptors {

    /** The vehicles' record TTL. */
    public static final Duration VEHICLE_TTL = Duration.ofMinutes(5);

    /** The vehicles' default poll interval. */
    public static final Duration VEHICLE_INTERVAL = Duration.ofSeconds(30);

    /** The vehicles' minimum poll interval. */
    public static final Duration VEHICLE_MIN_INTERVAL = Duration.ofSeconds(15);

    /** The vehicles' significant fields. */
    public static final List<String> VEHICLE_SIGNIFICANT = List.of("routeId", "tripId", "directionId", "label", "mode");

    /** The vehicles' tile properties. */
    public static final List<String> VEHICLE_TILES = List.of("routeId", "mode", "bearing", "occupancyStatus");

    /** The alerts' default poll interval. */
    public static final Duration ALERTS_INTERVAL = Duration.ofSeconds(60);

    /** The alerts' minimum poll interval. */
    public static final Duration ALERTS_MIN_INTERVAL = Duration.ofSeconds(30);

    /** Raw retention of the alerts: the {@code event} class (Conventions), 1825 days. */
    public static final Duration ALERTS_RETENTION = Duration.ofDays(1825);

    /** The alerts' language parameter. */
    public static final String LANGUAGE_PARAM = "language";

    private TransitDescriptors() {}

    /** The SAMPLED policy of Core-Data-Model §5.3: {@code {15s, 240s, 150 m, thresholds, edgeMin 8s}}. */
    public static IngestPolicy vehiclePolicy() {
        return new IngestPolicy.Sampled(Duration.ofSeconds(15), Duration.ofSeconds(240), 150.0, true,
                Duration.ofSeconds(8));
    }

    /** The descriptor of {@code feed}'s VehiclePositions source. */
    public static SourceDescriptor vehicles(TransitFeed feed) {
        SourceDescriptor.Builder b = SourceDescriptor.builder(feed.vehicleSourceId())
                .name(feed.name() + " vehicles")
                .description("Live vehicle positions of " + feed.operator() + " (" + feed.region() + ") from the"
                        + " GTFS-Realtime VehiclePositions feed" + (feed.catalog() ? " listed in the Mobility Database"
                        + " snapshot" : "") + ". observedAt = " + (feed.timeBase() == TimeBase.VEHICLE
                        ? "the vehicle timestamp (a vehicle without one is dropped)" : "the feed header timestamp")
                        + ". TRACK, TTL 5 min. Keep the interval at or below 30 s, or shorten the keyframe with it.")
                .kind(GtfsRtVehicleParser.KIND)
                .homepage(feed.licenseUrl())
                .attribution(feed.attribution())
                .licence(feed.license())
                .termsUrl(feed.licenseUrl())
                .egress(feed.egress())
                .mode(Mode.POLLING)
                .interval(VEHICLE_MIN_INTERVAL, VEHICLE_INTERVAL)
                .defaultBudget(4, 3000)
                .maxResponseBytes(GtfsRt.MAX_RESPONSE_BYTES)
                .historyTier(HistoryTier.TRACK)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(VEHICLE_TTL)
                .defaultIngestPolicy(vehiclePolicy())
                .significantFields(VEHICLE_SIGNIFICANT.toArray(String[]::new))
                .motion(new MotionSpec("speedMs", MotionSpec.SpeedUnit.METERS_PER_SECOND, "bearing"))
                .threshold(FieldThreshold.angular("bearing", 30.0))
                .titleField("label")
                .tileProperties(VEHICLE_TILES.toArray(String[]::new))
                .searchFields("label", "routeId", "tripId", "agency")
                .zeroResultsNormal(true)
                .kindDisplay(GtfsRtVehicleParser.KIND, new KindDisplay("{label}", "bus", "#2B8A3E", null, "bearing",
                        null, null));
        text(b, "routeId", "Route", "GTFS route_id of the vehicle's trip");
        text(b, "tripId", "Trip", "GTFS trip_id");
        integer(b, "directionId", null, "Direction", "GTFS direction_id, 0 or 1");
        text(b, "startDate", "Start date", "Service date of the trip, YYYYMMDD");
        text(b, "label", "Label", "The vehicle's user-visible label, for example a fleet or run number");
        text(b, "currentStatus", "Stop status", "INCOMING_AT, STOPPED_AT or IN_TRANSIT_TO");
        text(b, "stopId", "Stop", "GTFS stop_id of the current or next stop");
        integer(b, "currentStopSequence", null, "Stop sequence", "Index of the current stop in the trip");
        text(b, "occupancyStatus", "Occupancy", "GTFS-RT OccupancyStatus, for example MANY_SEATS_AVAILABLE");
        text(b, "congestionLevel", "Congestion", "GTFS-RT CongestionLevel, for example RUNNING_SMOOTHLY");
        b.fieldSpec("bearing", new FieldSpec(FieldType.NUMBER, "deg", "Bearing",
                "Direction of travel, degrees clockwise from true north, 0-360", null, Set.of()));
        b.fieldSpec("speedMs", new FieldSpec(FieldType.NUMBER, "m/s", "Speed", "Ground speed", null, Set.of()));
        text(b, "mode", "Mode", "bus, tram, subway, rail, ferry or unknown, from the operator's route ids");
        text(b, "agency", "Agency", "The feed's name");
        return b.build();
    }

    /** The descriptor of {@code feed}'s ServiceAlerts source. */
    public static SourceDescriptor alerts(TransitFeed feed) {
        SourceDescriptor.Builder b = SourceDescriptor.builder(feed.alertsSourceId())
                .name(feed.name() + " service alerts")
                .description("Service alerts of " + feed.operator() + " (" + feed.region() + ") from the"
                        + " GTFS-Realtime ServiceAlerts feed" + (feed.catalog() ? " listed in the Mobility Database"
                        + " snapshot" : "") + ". validFrom and validUntil come from the impact periods (the"
                        + " deprecated active periods on older feeds); an alert that leaves a FULL_DATASET feed is"
                        + " tombstoned. Texts are the translation chosen by the language parameter.")
                .kind(GtfsRtAlertParser.KIND)
                .homepage(feed.licenseUrl())
                .attribution(feed.attribution())
                .licence(feed.license())
                .termsUrl(feed.licenseUrl())
                .egress(feed.egress())
                .mode(Mode.POLLING)
                .interval(ALERTS_MIN_INTERVAL, ALERTS_INTERVAL)
                .defaultBudget(2, 1500)
                .maxResponseBytes(GtfsRt.MAX_RESPONSE_BYTES)
                .historyTier(HistoryTier.STANDARD)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(ALERTS_RETENTION)
                .kindRetention(GtfsRtAlertParser.KIND, ALERTS_RETENTION)
                .zeroResultsNormal(true)
                .titleField("headerText")
                .searchFields("headerText", "descriptionText", "causeDetail", "effectDetail")
                .embedFields("headerText", "descriptionText")
                .tileProperties("effect", "cause", "severityLevel")
                .param(ParamSpec.optional(LANGUAGE_PARAM, ParamType.STRING, "Preferred translation of the alert texts,"
                        + " a BCP 47 tag such as en, fi or nb; the first translation is used when it is missing",
                        GtfsRtAlertParser.DEFAULT_LANGUAGE))
                .kindDisplay(GtfsRtAlertParser.KIND, new KindDisplay("{headerText}", "caution", "#E8590C", null));
        text(b, "headerText", "Header", "The alert's summary (untrusted upstream text, at most 4 KB)");
        text(b, "descriptionText", "Description", "The alert's full text (untrusted upstream text, at most 4 KB)");
        text(b, "url", "URL", "A link to more information");
        text(b, "cause", "Cause", "GTFS-RT Alert.Cause, for example CONSTRUCTION or SPECIAL_EVENT");
        text(b, "effect", "Effect", "GTFS-RT Alert.Effect, for example DETOUR or NO_SERVICE");
        text(b, "severityLevel", "Severity", "INFO, WARNING, SEVERE or UNKNOWN_SEVERITY");
        text(b, "causeDetail", "Cause detail", "Agency-specific description of the cause");
        text(b, "effectDetail", "Effect detail", "Agency-specific description of the effect");
        b.fieldSpec("communicationPeriods", new FieldSpec(FieldType.ARRAY, null, "Communication periods",
                "When the alert is shown to riders, as ISO {start, end} pairs (at most 20)", null, Set.of()));
        b.fieldSpec("impactPeriods", new FieldSpec(FieldType.ARRAY, null, "Impact periods",
                "When service is affected, as ISO {start, end} pairs (at most 20); the deprecated active periods on"
                        + " feeds that publish no impact period", null, Set.of()));
        b.fieldSpec("informed", new FieldSpec(FieldType.ARRAY, null, "Informed entities",
                "Up to 50 {agencyId, routeId, routeType, stopId, tripId} the alert applies to", null, Set.of()));
        return b.build();
    }

    private static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }

    private static void integer(SourceDescriptor.Builder b, String key, String unit, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.INTEGER, unit, label, description, null, Set.of()));
    }
}
