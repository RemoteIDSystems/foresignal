package ai.foresignal.source.adapters.transit;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PositionSource;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import ai.foresignal.testsupport.transit.GtfsRtFixtures;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

/**
 * {@link GtfsRtVehicleParser} (prompt 070): the MBTA, Entur and HSL captures, the VEHICLE and HEADER time bases, the
 * untimed vehicle, the externalId fallback, skipped positions, mode mapping and a stable static hash across a stop
 * change.
 */
class GtfsRtVehicleParserTest {

    private static FeedMessage fixture(String dir, String name) throws Exception {
        return GtfsRt.parse(AdapterFixtures.bytes("transit/" + dir, name));
    }

    private static NormalizedRecord byId(GtfsRtVehicleParser.Result result, String id) {
        return result.records().stream().filter(r -> r.externalId().equals(id)).findFirst().orElseThrow();
    }

    private static TransitFeed feed(TimeBase timeBase) {
        TransitFeed mbta = TransitFeeds.reviewed("mbta");
        return new TransitFeed(mbta.id(), mbta.name(), mbta.operator(), mbta.region(), mbta.vehiclePositionsUrl(),
                mbta.alertsUrl(), mbta.headers(), mbta.license(), mbta.licenseUrl(), mbta.attribution(), timeBase,
                mbta.defaultMode(), mbta.routeMode(), mbta.egress(), false);
    }

    @Test
    @DisplayName("MBTA: 30 vehicles with their vehicle timestamps, route modes and the full payload")
    void mbta() throws Exception {
        GtfsRtVehicleParser.Result result = GtfsRtVehicleParser.parse(fixture("mbta", "VehiclePositions.pb"),
                TransitFeeds.reviewed("mbta"));

        assertThat(result.records()).hasSize(30);
        assertThat(result.untimed()).isZero();
        assertThat(result.fullDataset()).isTrue();
        NormalizedRecord bus = byId(result, "y3202");
        assertThat(bus.kind()).isEqualTo(GtfsRtVehicleParser.KIND);
        assertThat(bus.observedAt()).isEqualTo(Instant.ofEpochSecond(1_791_908_276L));
        Point point = (Point) bus.geometry();
        assertThat(point.getY()).isEqualTo(42.337486);
        assertThat(point.getX()).isEqualTo(-71.094696);
        assertThat(bus.payload()).containsEntry("routeId", "85").containsEntry("tripId", "78174219")
                .containsEntry("directionId", 0).containsEntry("startDate", "20261013").containsEntry("label", "3202")
                .containsEntry("currentStatus", "IN_TRANSIT_TO").containsEntry("stopId", "71391")
                .containsEntry("currentStopSequence", 3).containsEntry("occupancyStatus", "MANY_SEATS_AVAILABLE")
                .containsEntry("bearing", 295.0).containsEntry("mode", "bus").containsEntry("agency", "MBTA");
        assertThat(bus.payload()).doesNotContainKeys("licensePlate", "speedMs");
        assertThat(bus.payload().keySet()).isSubsetOf(GtfsRtVehicleParser.PAYLOAD_FIELDS);
        assertThat(bus.quality().positionSource()).isEqualTo(PositionSource.REPORTED);
        assertThat(bus.validUntil()).isNull();
        assertThat(byId(result, "1821").payload()).containsEntry("routeId", "CR-Greenbush").containsEntry("mode", "rail");
    }

    @Test
    @DisplayName("Entur: codespace route ids map to bus, congestion is kept, the vehicle id is the externalId")
    void entur() throws Exception {
        GtfsRtVehicleParser.Result result = GtfsRtVehicleParser.parse(fixture("entur-norway", "vehicle-positions.pb"),
                TransitFeeds.reviewed("entur-norway"));

        assertThat(result.records()).isNotEmpty();
        NormalizedRecord first = byId(result, "3690207514");
        assertThat(first.observedAt()).isEqualTo(Instant.ofEpochSecond(1_791_895_289L));
        assertThat(first.payload()).containsEntry("routeId", "TRO:Line:1_495").containsEntry("mode", "bus")
                .containsEntry("congestionLevel", "RUNNING_SMOOTHLY").containsEntry("currentStatus", "STOPPED_AT")
                .containsEntry("stopId", "NSR:Quay:103042").containsEntry("bearing", 93.7)
                .containsEntry("agency", "Entur");
    }

    @Test
    @DisplayName("HSL: speed in m/s, metro routes are subway")
    void hsl() throws Exception {
        GtfsRtVehicleParser.Result result = GtfsRtVehicleParser.parse(fixture("hsl-helsinki", "vehicle-positions.pb"),
                TransitFeeds.reviewed("hsl-helsinki"));

        NormalizedRecord bus = byId(result, "18/165");
        assertThat(bus.observedAt()).isEqualTo(Instant.ofEpochSecond(1_791_908_284L));
        assertThat(bus.payload()).containsEntry("speedMs", 18.82).containsEntry("bearing", 42.0)
                .containsEntry("routeId", "9785K").containsEntry("mode", "bus");
        assertThat(result.records()).filteredOn(r -> "31M1".equals(r.payload().get("routeId")))
                .isNotEmpty().allSatisfy(r -> assertThat(r.payload()).containsEntry("mode", "subway"));
    }

    @Test
    @DisplayName("VEHICLE drops an untimed vehicle (counted); HEADER gives every vehicle the header time")
    void timeBases() throws Exception {
        FeedMessage message = fixture("synthetic", "untimed-vehicle.pb");

        GtfsRtVehicleParser.Result vehicle = GtfsRtVehicleParser.parse(message, feed(TimeBase.VEHICLE));
        assertThat(vehicle.records()).extracting(NormalizedRecord::externalId).containsExactly("v1");
        assertThat(vehicle.untimed()).isEqualTo(1);
        assertThat(vehicle.records().get(0).observedAt()).isEqualTo(Instant.ofEpochSecond(GtfsRtFixtures.T0 - 10));

        GtfsRtVehicleParser.Result header = GtfsRtVehicleParser.parse(message, feed(TimeBase.HEADER));
        assertThat(header.records()).extracting(NormalizedRecord::externalId).containsExactly("v1", "v2");
        assertThat(header.untimed()).isZero();
        assertThat(header.records()).allSatisfy(r -> assertThat(r.observedAt())
                .isEqualTo(Instant.ofEpochSecond(GtfsRtFixtures.T0)));

        FeedMessage noHeaderTime = message.toBuilder().setHeader(message.getHeader().toBuilder().clearTimestamp()).build();
        GtfsRtVehicleParser.Result none = GtfsRtVehicleParser.parse(noHeaderTime, feed(TimeBase.HEADER));
        assertThat(none.records()).isEmpty();
        assertThat(none.untimed()).isEqualTo(2);
    }

    @Test
    @DisplayName("externalId falls back to the entity id; 0/0, out-of-range and missing positions are skipped")
    void identityAndPositions() {
        long t = GtfsRtFixtures.T0;
        FeedEntity noVehicleId = GtfsRtFixtures.vehicle("entity-7", null, "39", "t7", 42.3, -71.1, t);
        FeedEntity nullIsland = GtfsRtFixtures.vehicle("e8", "v8", "39", "t8", 0.0, 0.0, t);
        FeedEntity outOfRange = GtfsRtFixtures.vehicle("e9", "v9", "39", "t9", 91.0, -71.1, t);
        FeedEntity noPosition = FeedEntity.newBuilder().setId("e10")
                .setVehicle(VehiclePosition.newBuilder().setTimestamp(t)).build();
        FeedEntity deleted = GtfsRtFixtures.vehicle("e11", "v11", "39", "t11", 42.3, -71.1, t).toBuilder()
                .setIsDeleted(true).build();
        FeedMessage message = FeedMessage.newBuilder().setHeader(GtfsRtFixtures.header(t)).addEntity(noVehicleId)
                .addEntity(nullIsland).addEntity(outOfRange).addEntity(noPosition).addEntity(deleted).build();

        GtfsRtVehicleParser.Result result = GtfsRtVehicleParser.parse(message, feed(TimeBase.VEHICLE));

        assertThat(result.records()).extracting(NormalizedRecord::externalId).containsExactly("entity-7");
        assertThat(result.skipped()).isEqualTo(3);
        assertThat(result.records().get(0).payload()).doesNotContainKey("licensePlate");
    }

    @Test
    @DisplayName("an over-long string drops the record; a duplicate vehicle keeps its newest report")
    void boundsAndDuplicates() {
        long t = GtfsRtFixtures.T0;
        FeedEntity longRoute = GtfsRtFixtures.vehicle("e1", "v1", "R".repeat(300), "t1", 42.3, -71.1, t);
        FeedEntity older = GtfsRtFixtures.vehicle("e2", "v2", "39", "old-trip", 42.3, -71.1, t - 30);
        FeedEntity newer = GtfsRtFixtures.vehicle("e3", "v2", "39", "new-trip", 42.31, -71.1, t);
        FeedMessage message = FeedMessage.newBuilder().setHeader(GtfsRtFixtures.header(t)).addEntity(longRoute)
                .addEntity(newer).addEntity(older).build();

        GtfsRtVehicleParser.Result result = GtfsRtVehicleParser.parse(message, feed(TimeBase.VEHICLE));

        assertThat(result.records()).extracting(NormalizedRecord::externalId).containsExactly("v2");
        assertThat(result.records().get(0).payload()).containsEntry("tripId", "new-trip");
        assertThat(result.skipped()).isEqualTo(1);
    }

    @Test
    @DisplayName("mode mapping per operator, with the feed default when the route id does not tell")
    void modes() {
        TransitFeed mbta = TransitFeeds.reviewed("mbta");
        assertThat(mbta.modeFor("Red")).isEqualTo("subway");
        assertThat(mbta.modeFor("Green-B")).isEqualTo("tram");
        assertThat(mbta.modeFor("Mattapan")).isEqualTo("tram");
        assertThat(mbta.modeFor("CR-Fitchburg")).isEqualTo("rail");
        assertThat(mbta.modeFor("Boat-F1")).isEqualTo("ferry");
        assertThat(mbta.modeFor(null)).isEqualTo("bus");
        TransitFeed hsl = TransitFeeds.reviewed("hsl-helsinki");
        assertThat(hsl.modeFor("1007H")).isEqualTo("tram");
        assertThat(hsl.modeFor("1019")).isEqualTo("ferry");
        assertThat(hsl.modeFor("3001Z")).isEqualTo("rail");
        assertThat(hsl.modeFor("31M2")).isEqualTo("subway");
        TransitFeed entur = TransitFeeds.reviewed("entur-norway");
        assertThat(entur.modeFor("VYG:Line:R10")).isEqualTo("rail");
        assertThat(entur.modeFor("RUT:Line:31")).isEqualTo("bus");
        TransitFeed metro = TransitFeeds.reviewed("metrotransit-msp");
        assertThat(metro.modeFor("901")).isEqualTo("tram");
        assertThat(metro.modeFor("888")).isEqualTo("rail");
        assertThat(metro.modeFor("5")).isEqualTo("bus");
        assertThat(TransitFeeds.reviewed("ovapi-nl").modeFor("anything")).isEqualTo("bus");
    }

    @Test
    @DisplayName("bearing folding and speed rounding")
    void kinematics() {
        assertThat(GtfsRtVehicleParser.bearing(-90f)).isEqualTo(270.0);
        assertThat(GtfsRtVehicleParser.bearing(725f)).isEqualTo(5.0);
        assertThat(GtfsRtVehicleParser.bearing(359.97f)).isEqualTo(0.0);
        assertThat(GtfsRtVehicleParser.bearing(Float.NaN)).isNull();
        assertThat(GtfsRtVehicleParser.speed(-1f)).isNull();
        assertThat(GtfsRtVehicleParser.speed(5.3f)).isEqualTo(5.3);
        assertThat(GtfsRtVehicleParser.plausible(0.0000001, 0.0)).isFalse();
        assertThat(GtfsRtVehicleParser.plausible(10, 181)).isFalse();
        assertThat(GtfsRtVehicleParser.plausible(Double.NaN, 1)).isFalse();
    }

    @Test
    @DisplayName("a stop change (currentStatus, stopId, sequence) keeps the static hash: they are not significant")
    void stableStaticHashAcrossStops() {
        long t = GtfsRtFixtures.T0;
        TransitFeed feed = TransitFeeds.reviewed("mbta");
        java.util.List<NormalizedRecord> snapshots = new java.util.ArrayList<>();
        String[][] stops = {{"IN_TRANSIT_TO", "70061", "1"}, {"STOPPED_AT", "70061", "1"}, {"IN_TRANSIT_TO", "70063", "2"}};
        for (int i = 0; i < stops.length; i++) {
            FeedEntity entity = GtfsRtFixtures.vehicle("e1", "R-5471", "Red", "trip-1", 42.39 - i * 0.002, -71.12, t + i * 20);
            VehiclePosition.Builder v = entity.getVehicle().toBuilder()
                    .setCurrentStatus(VehiclePosition.VehicleStopStatus.valueOf(stops[i][0]))
                    .setStopId(stops[i][1]).setCurrentStopSequence(Integer.parseInt(stops[i][2]));
            FeedMessage message = FeedMessage.newBuilder().setHeader(GtfsRtFixtures.header(t + i * 20))
                    .addEntity(entity.toBuilder().setVehicle(v)).build();
            snapshots.addAll(GtfsRtVehicleParser.parse(message, feed).records());
        }
        assertThat(snapshots).hasSize(3);
        SnapshotContract.assertStableStaticHash(TransitDescriptors.vehicles(feed), snapshots);
        assertThat(List.copyOf(snapshots.get(2).payload().keySet())).contains("stopId", "currentStatus");
        assertThat(Map.copyOf(snapshots.get(0).payload())).containsEntry("mode", "subway");
    }
}
