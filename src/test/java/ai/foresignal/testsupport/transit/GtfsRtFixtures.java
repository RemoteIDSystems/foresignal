package ai.foresignal.testsupport.transit;

import com.google.protobuf.TextFormat;
import com.google.transit.realtime.GtfsRealtime.Alert;
import com.google.transit.realtime.GtfsRealtime.EntitySelector;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TimeRange;
import com.google.transit.realtime.GtfsRealtime.TranslatedString;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * GTFS-RT fixture tools and builders for the transit tests (prompt 070).
 *
 * <p>Run as a program (not a test) after {@code ./gradlew compileTestJava}, with the test classes and
 * gtfs-realtime-bindings and protobuf-java on the class path:
 *
 * <ul>
 *   <li>{@code trim <in.pb> <out.pb> [n]} rewrites a captured FeedMessage to its header and first {@code n} entities
 *       (default {@value #TRIM_ENTITIES}) and writes a protobuf text-format dump beside it ({@code out.pb.txt}) for
 *       review;
 *   <li>{@code synthetic <dir>} writes the hand-made fixtures: {@code untimed-vehicle.pb},
 *       {@code full-dataset-alert-removed.pb}, {@code alert-legacy-active-period.pb}, {@code alert-impact-period.pb},
 *       {@code differential-alert-deleted.pb} and {@code html-200.html}, each with its dump.
 * </ul>
 *
 * <p>The builders are also used directly by the tests.
 */
public final class GtfsRtFixtures {

    /** The default entity count of a trimmed capture. */
    public static final int TRIM_ENTITIES = 30;

    /** The header time of the synthetic fixtures, 2026-10-13T16:00:00Z. */
    public static final long T0 = 1_791_907_200L;

    private GtfsRtFixtures() {}

    /**
     * The command-line entry point.
     *
     * @param args {@code trim <in> <out> [n]} or {@code synthetic <dir>}
     * @throws IOException when a file cannot be read or written
     */
    public static void main(String[] args) throws IOException {
        if (args.length >= 3 && args[0].equals("trim")) {
            int n = args.length > 3 ? Integer.parseInt(args[3]) : TRIM_ENTITIES;
            FeedMessage trimmed = trim(FeedMessage.parseFrom(Files.readAllBytes(Path.of(args[1]))), n);
            write(Path.of(args[2]), trimmed);
        } else if (args.length == 2 && args[0].equals("synthetic")) {
            Path dir = Path.of(args[1]);
            Files.createDirectories(dir);
            write(dir.resolve("untimed-vehicle.pb"), untimedVehicle());
            write(dir.resolve("alert-impact-period.pb"), alertImpactPeriod());
            write(dir.resolve("full-dataset-alert-removed.pb"), fullDatasetAlertRemoved());
            write(dir.resolve("alert-legacy-active-period.pb"), alertLegacyActivePeriod());
            write(dir.resolve("differential-alert-deleted.pb"), differentialAlertDeleted());
            Files.writeString(dir.resolve("html-200.html"), "<!DOCTYPE html>\n<html><head><title>Service unavailable"
                    + "</title></head><body><h1>Maintenance</h1></body></html>\n", StandardCharsets.UTF_8);
        } else {
            throw new IllegalArgumentException("usage: trim <in.pb> <out.pb> [n] | synthetic <dir>");
        }
    }

    /** {@code message} with its header and only its first {@code n} entities. */
    public static FeedMessage trim(FeedMessage message, int n) {
        FeedMessage.Builder out = FeedMessage.newBuilder().setHeader(message.getHeader());
        for (int i = 0; i < Math.min(n, message.getEntityCount()); i++) {
            out.addEntity(message.getEntity(i));
        }
        return out.build();
    }

    /** A FULL_DATASET header at {@code timestamp}. */
    public static FeedHeader header(long timestamp) {
        return FeedHeader.newBuilder().setGtfsRealtimeVersion("2.0")
                .setIncrementality(FeedHeader.Incrementality.FULL_DATASET).setTimestamp(timestamp).build();
    }

    /** A vehicle entity; {@code timestamp} 0 leaves the vehicle timestamp out, a null {@code vehicleId} the descriptor id. */
    public static FeedEntity vehicle(String entityId, String vehicleId, String routeId, String tripId, double lat,
            double lon, long timestamp) {
        VehicleDescriptor.Builder descriptor = VehicleDescriptor.newBuilder().setLabel("Bus " + entityId)
                .setLicensePlate("ABC-123");
        if (vehicleId != null) {
            descriptor.setId(vehicleId);
        }
        VehiclePosition.Builder vehicle = VehiclePosition.newBuilder()
                .setTrip(TripDescriptor.newBuilder().setRouteId(routeId).setTripId(tripId).setDirectionId(1)
                        .setStartDate("20261013"))
                .setVehicle(descriptor)
                .setPosition(Position.newBuilder().setLatitude((float) lat).setLongitude((float) lon).setBearing(95f)
                        .setSpeed(8.5f))
                .setCurrentStatus(VehiclePosition.VehicleStopStatus.IN_TRANSIT_TO)
                .setStopId("stop-1")
                .setCurrentStopSequence(4)
                .setOccupancyStatus(VehiclePosition.OccupancyStatus.MANY_SEATS_AVAILABLE);
        if (timestamp > 0) {
            vehicle.setTimestamp(timestamp);
        }
        return FeedEntity.newBuilder().setId(entityId).setVehicle(vehicle).build();
    }

    /** A translated string of {@code (language, text)} pairs. */
    public static TranslatedString text(String... languageText) {
        TranslatedString.Builder b = TranslatedString.newBuilder();
        for (int i = 0; i + 1 < languageText.length; i += 2) {
            TranslatedString.Translation.Builder t = TranslatedString.Translation.newBuilder().setText(languageText[i + 1]);
            if (languageText[i] != null) {
                t.setLanguage(languageText[i]);
            }
            b.addTranslation(t);
        }
        return b.build();
    }

    /** A time range; 0 leaves a bound out. */
    public static TimeRange range(long start, long end) {
        TimeRange.Builder b = TimeRange.newBuilder();
        if (start > 0) {
            b.setStart(start);
        }
        if (end > 0) {
            b.setEnd(end);
        }
        return b.build();
    }

    /** {@code untimed-vehicle.pb}: one timed vehicle ({@code v1}) and one without a vehicle timestamp ({@code v2}). */
    public static FeedMessage untimedVehicle() {
        return FeedMessage.newBuilder().setHeader(header(T0))
                .addEntity(vehicle("e1", "v1", "Red", "t1", 42.35, -71.06, T0 - 10))
                .addEntity(vehicle("e2", "v2", "39", "t2", 42.34, -71.10, 0))
                .build();
    }

    /** Alert {@code A}: one closed impact period; alert {@code B}: one open-ended impact period. */
    public static FeedMessage alertImpactPeriod() {
        Alert a = Alert.newBuilder()
                .addCommunicationPeriod(range(T0 - 86_400, T0 + 172_800))
                .addImpactPeriod(range(T0 + 3_600, T0 + 7_200))
                .addImpactPeriod(range(T0 + 90_000, T0 + 93_600))
                .addInformedEntity(EntitySelector.newBuilder().setAgencyId("1").setRouteId("Red").setRouteType(1))
                .setCause(Alert.Cause.SPECIAL_EVENT)
                .setEffect(Alert.Effect.DETOUR)
                .setSeverityLevel(Alert.SeverityLevel.WARNING)
                .setHeaderText(text("en", "Red Line detour", "es", "Desvío de la Línea Roja"))
                .setDescriptionText(text("en", "Shuttle buses replace trains between Alewife and Harvard."))
                .setUrl(text("en", "https://www.example.org/alerts/A"))
                .setCauseDetail(text("en", "Marathon"))
                .setEffectDetail(text("en", "Shuttle buses"))
                .build();
        Alert b = Alert.newBuilder()
                .addImpactPeriod(range(T0 - 3_600, 0))
                .addInformedEntity(EntitySelector.newBuilder().setStopId("place-pktrm"))
                .setCause(Alert.Cause.MAINTENANCE)
                .setEffect(Alert.Effect.ACCESSIBILITY_ISSUE)
                .setHeaderText(text("en", "Elevator out of service at Park Street"))
                .build();
        return FeedMessage.newBuilder().setHeader(header(T0))
                .addEntity(FeedEntity.newBuilder().setId("A").setAlert(a))
                .addEntity(FeedEntity.newBuilder().setId("B").setAlert(b))
                .build();
    }

    /** The same feed a minute later without alert {@code B} (FULL_DATASET). */
    public static FeedMessage fullDatasetAlertRemoved() {
        FeedMessage before = alertImpactPeriod();
        return FeedMessage.newBuilder().setHeader(header(T0 + 60)).addEntity(before.getEntity(0)).build();
    }

    /** One alert ({@code L}) with only the deprecated {@code active_period} and English and Finnish texts. */
    @SuppressWarnings("deprecation")
    public static FeedMessage alertLegacyActivePeriod() {
        Alert alert = Alert.newBuilder()
                .addActivePeriod(range(T0 - 7_200, T0 + 7_200))
                .addActivePeriod(range(T0 + 86_400, T0 + 90_000))
                .addInformedEntity(EntitySelector.newBuilder().setRouteId("1006")
                        .setTrip(TripDescriptor.newBuilder().setTripId("1006_20261013_1")))
                .setCause(Alert.Cause.CONSTRUCTION)
                .setEffect(Alert.Effect.REDUCED_SERVICE)
                .setHeaderText(text("fi", "Raitiolinja 6: poikkeusreitti", "en", "Tram 6: diversion"))
                .setDescriptionText(text("fi", "Ratatyöt Hämeentiellä.", "en", "Track works on Hämeentie."))
                .build();
        return FeedMessage.newBuilder().setHeader(header(T0))
                .addEntity(FeedEntity.newBuilder().setId("L").setAlert(alert))
                .build();
    }

    /** A DIFFERENTIAL feed that deletes alert {@code B} and repeats nothing else. */
    public static FeedMessage differentialAlertDeleted() {
        FeedHeader header = header(T0 + 120).toBuilder()
                .setIncrementality(FeedHeader.Incrementality.DIFFERENTIAL).build();
        return FeedMessage.newBuilder().setHeader(header)
                .addEntity(FeedEntity.newBuilder().setId("B").setIsDeleted(true))
                .build();
    }

    private static void write(Path out, FeedMessage message) throws IOException {
        Files.write(out, message.toByteArray());
        Files.writeString(out.resolveSibling(out.getFileName() + ".txt"),
                TextFormat.printer().escapingNonAscii(false).printToString(message), StandardCharsets.UTF_8);
    }
}
