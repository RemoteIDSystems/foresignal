/*
 * The vehicle normalization (a usable position rejects non-finite, out-of-range and 0/0 coordinates; the record id is
 * the vehicle id, else the entity id; bearings folded into 0–360; negative speeds dropped; coordinates to 6 decimals;
 * a duplicate vehicle keeps its newest timestamp; an over-long string drops the whole record) is ported from God's
 * Eye View, src/data/gtfsRealtime.js (isPlausibleVehiclePosition, normalizeVehicleEntity, decodeVehiclePositions),
 * https://github.com/bilawalsidhu/gods-eye-view, commit 591f299d11f38a612629a274463196d57ae3862e.
 * Copyright (c) 2026 Bilawal Sidhu. MIT License; the full notice is in THIRD_PARTY_NOTICES.md.
 * Changes: Java over gtfs-realtime-bindings; Foresignal's payload keys; the per-feed time base (VEHICLE drops an
 * untimed vehicle instead of keeping it without a time); license plates are never read.
 */
package ai.foresignal.source.adapters.transit;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PositionSource;
import ai.foresignal.source.spi.Quality;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps a VehiclePositions {@code FeedMessage} to {@code transit-vehicle} records (prompt 070).
 *
 * <ul>
 *   <li><strong>Identity:</strong> {@code externalId = vehicle.vehicle.id}, else {@code entity.id}.
 *   <li><strong>Time (fixed per feed):</strong> {@link TimeBase#VEHICLE} takes {@code vehicle.timestamp} and drops
 *       a vehicle without one ({@link Result#untimed()}); {@link TimeBase#HEADER} takes {@code header.timestamp} for
 *       every vehicle (none when the header has no time).
 *   <li><strong>Geometry:</strong> {@code Point(longitude, latitude)}; 0/0, non-finite and out-of-range positions are
 *       skipped ({@link Result#skipped()}).
 *   <li><strong>Payload:</strong> {@code routeId, tripId, directionId, startDate, label, currentStatus, stopId,
 *       currentStopSequence, occupancyStatus, congestionLevel, bearing, speedMs, mode, agency}; enums by their proto
 *       names. {@code licensePlate} is never read.
 *   <li><strong>Quality:</strong> {@code positionSource} REPORTED.
 * </ul>
 */
public final class GtfsRtVehicleParser {

    /** The record kind. */
    public static final String KIND = "transit-vehicle";

    /** The payload keys, in order. */
    public static final List<String> PAYLOAD_FIELDS = List.of("routeId", "tripId", "directionId", "startDate", "label",
            "currentStatus", "stopId", "currentStopSequence", "occupancyStatus", "congestionLevel", "bearing", "speedMs",
            "mode", "agency");

    /**
     * What one message produced.
     *
     * @param records one record per vehicle, newest per {@code externalId}
     * @param untimed vehicles dropped for lack of a timestamp under the feed's time base
     * @param skipped entities without a usable vehicle position, identity or bounded strings
     * @param fullDataset true unless the header says DIFFERENTIAL
     */
    public record Result(List<NormalizedRecord> records, int untimed, int skipped, boolean fullDataset) {

        /** Copies the records. */
        public Result {
            records = List.copyOf(records);
        }
    }

    private GtfsRtVehicleParser() {}

    /**
     * Maps every vehicle of {@code message}.
     *
     * @param feed the feed entry (time base, mode rule, agency name)
     */
    public static Result parse(FeedMessage message, TransitFeed feed) {
        Instant headerTime = GtfsRt.headerTime(message);
        Map<String, NormalizedRecord> byId = new LinkedHashMap<>();
        int untimed = 0;
        int skipped = 0;
        for (FeedEntity entity : message.getEntityList()) {
            if (entity.getIsDeleted() || !entity.hasVehicle()) {
                continue;
            }
            VehiclePosition vehicle = entity.getVehicle();
            Mapped mapped = map(entity, vehicle, feed, headerTime);
            if (mapped.untimed()) {
                untimed++;
                continue;
            }
            if (mapped.record() == null) {
                skipped++;
                continue;
            }
            NormalizedRecord record = mapped.record();
            NormalizedRecord existing = byId.get(record.externalId());
            if (existing == null || !record.observedAt().isBefore(existing.observedAt())) {
                byId.put(record.externalId(), record);
            }
        }
        return new Result(new ArrayList<>(byId.values()), untimed, skipped, GtfsRt.fullDataset(message));
    }

    /** One entity's outcome: a record, untimed, or skipped (record null, untimed false). */
    private record Mapped(NormalizedRecord record, boolean untimed) {

        static final Mapped SKIPPED = new Mapped(null, false);
        static final Mapped UNTIMED = new Mapped(null, true);
    }

    private static Mapped map(FeedEntity entity, VehiclePosition vehicle, TransitFeed feed, Instant headerTime) {
        if (!vehicle.hasPosition()) {
            return Mapped.SKIPPED;
        }
        Position position = vehicle.getPosition();
        double lat = position.getLatitude();
        double lon = position.getLongitude();
        if (!plausible(lat, lon)) {
            return Mapped.SKIPPED;
        }
        VehicleDescriptor descriptor = vehicle.hasVehicle() ? vehicle.getVehicle() : VehicleDescriptor.getDefaultInstance();
        TripDescriptor trip = vehicle.hasTrip() ? vehicle.getTrip() : TripDescriptor.getDefaultInstance();
        String vehicleId = GtfsRt.bounded(descriptor.hasId() ? descriptor.getId() : null);
        String entityId = GtfsRt.bounded(entity.getId());
        String routeId = GtfsRt.bounded(trip.hasRouteId() ? trip.getRouteId() : null);
        String tripId = GtfsRt.bounded(trip.hasTripId() ? trip.getTripId() : null);
        String startDate = GtfsRt.bounded(trip.hasStartDate() ? trip.getStartDate() : null);
        String label = GtfsRt.bounded(descriptor.hasLabel() ? descriptor.getLabel() : null);
        String stopId = GtfsRt.bounded(vehicle.hasStopId() ? vehicle.getStopId() : null);
        for (String value : new String[] {vehicleId, entityId, routeId, tripId, startDate, label, stopId}) {
            if (value == GtfsRt.OVERSIZE) {
                return Mapped.SKIPPED;
            }
        }
        String externalId = vehicleId != null ? vehicleId : entityId;
        if (externalId == null) {
            return Mapped.SKIPPED;
        }
        Instant observedAt;
        if (feed.timeBase() == TimeBase.VEHICLE) {
            if (!vehicle.hasTimestamp() || vehicle.getTimestamp() <= 0) {
                return Mapped.UNTIMED;
            }
            observedAt = Instant.ofEpochSecond(vehicle.getTimestamp());
        } else {
            if (headerTime == null) {
                return Mapped.UNTIMED;
            }
            observedAt = headerTime;
        }
        NormalizedRecord.Builder builder = NormalizedRecord.builder(externalId, KIND)
                .observedAt(observedAt)
                .point(round(lon, 6), round(lat, 6))
                .quality(Quality.positionSource(PositionSource.REPORTED))
                .put("routeId", routeId)
                .put("tripId", tripId)
                .put("directionId", trip.hasDirectionId() ? trip.getDirectionId() : null)
                .put("startDate", startDate)
                .put("label", label)
                .put("currentStatus", vehicle.hasCurrentStatus() ? vehicle.getCurrentStatus().name() : null)
                .put("stopId", stopId)
                .put("currentStopSequence", vehicle.hasCurrentStopSequence() ? vehicle.getCurrentStopSequence() : null)
                .put("occupancyStatus", vehicle.hasOccupancyStatus() ? vehicle.getOccupancyStatus().name() : null)
                .put("congestionLevel", vehicle.hasCongestionLevel() ? vehicle.getCongestionLevel().name() : null)
                .put("bearing", position.hasBearing() ? bearing(position.getBearing()) : null)
                .put("speedMs", position.hasSpeed() ? speed(position.getSpeed()) : null)
                .put("mode", feed.modeFor(routeId))
                .put("agency", feed.name());
        return new Mapped(builder.build(), false);
    }

    /** True for a finite, in-range position that is not the 0/0 of a cold GPS (GEV). */
    static boolean plausible(double lat, double lon) {
        if (!Double.isFinite(lat) || !Double.isFinite(lon)) {
            return false;
        }
        if (Math.abs(lat) > 90 || Math.abs(lon) > 180) {
            return false;
        }
        return !(Math.abs(lat) < 1e-6 && Math.abs(lon) < 1e-6);
    }

    /** The bearing folded into [0, 360), one decimal; null when not finite. */
    static Double bearing(float value) {
        if (!Float.isFinite(value)) {
            return null;
        }
        double folded = ((value % 360.0) + 360.0) % 360.0;
        double rounded = round(folded, 1);
        return rounded >= 360.0 ? 0.0 : rounded;
    }

    /** The speed in m/s, two decimals; null when negative or not finite. */
    static Double speed(float value) {
        if (!Float.isFinite(value) || value < 0) {
            return null;
        }
        return round(value, 2);
    }

    private static double round(double value, int places) {
        double rounded = BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
        return rounded == 0.0 ? 0.0 : rounded;
    }
}
