package ai.foresignal.source.adapters.transit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.ingest.PersistReason;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.transit.GtfsRtFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The transit sources end to end through the real fetch path and ingest writer (prompt 070): a vehicle moving 200 m
 * after 20 s is a SAMPLE, a trip change is SIGNIFICANT, an alert is FIRST and is tombstoned when it leaves the full
 * feed, {@code validFrom} is stored, and the registry holds the reviewed and catalog sources as TRACK + SAMPLED.
 */
class TransitIngestIT extends WeatherIngestSupport {

    static final String VEHICLES = "transit-mbta";
    static final String ALERTS = "transit-alerts-mbta";
    static final long T0 = GtfsRtFixtures.T0;

    @Autowired private SourceRegistry registry;

    @BeforeEach
    void setUp() {
        clock.setInstant(Instant.ofEpochSecond(T0 + 5));
        configure(VEHICLES, false, Map.of());
        configure(ALERTS, false, Map.of());
    }

    private void serve(String path, FeedMessage message) {
        UPSTREAM.stubFor(get(urlEqualTo(path)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/x-protobuf").withBody(message.toByteArray())));
    }

    private void serveVehicle(String tripId, double lat, long timestamp) {
        serve("/realtime/VehiclePositions.pb", FeedMessage.newBuilder().setHeader(GtfsRtFixtures.header(timestamp))
                .addEntity(GtfsRtFixtures.vehicle("e1", "R-5471", "Red", tripId, lat, -71.12, timestamp)).build());
    }

    private List<Integer> reasons(String sourceId, String externalId) {
        return jdbc.queryForList("select persist_reason from record_version where source_id = ? and external_id = ?"
                + " order by seq", Integer.class, sourceId, externalId);
    }

    @Test
    @DisplayName("a vehicle moving 200 m after 20 s is a SAMPLE; a trip change is SIGNIFICANT")
    void vehicleSampling() {
        int before = versions(VEHICLES, "R-5471");
        serveVehicle("trip-1", 42.3900, T0);
        assertThat(fetch(VEHICLES).success()).isTrue();

        clock.setInstant(Instant.ofEpochSecond(T0 + 25));
        serveVehicle("trip-1", 42.3918, T0 + 20);
        assertThat(fetch(VEHICLES).success()).isTrue();

        clock.setInstant(Instant.ofEpochSecond(T0 + 30));
        serveVehicle("trip-2", 42.3918, T0 + 24);
        assertThat(fetch(VEHICLES).success()).isTrue();

        List<Integer> reasons = reasons(VEHICLES, "R-5471");
        assertThat(versions(VEHICLES, "R-5471") - before).isEqualTo(3);
        assertThat(reasons.subList(reasons.size() - 3, reasons.size())).containsExactly(
                (int) PersistReason.FIRST.code(), (int) PersistReason.SAMPLE.code(),
                (int) PersistReason.SIGNIFICANT.code());
        assertThat(jdbc.queryForObject("select payload->>'tripId' from record_head where source_id = ? and"
                + " external_id = ?", String.class, VEHICLES, "R-5471")).isEqualTo("trip-2");
        assertThat(head("observed_at", VEHICLES, "R-5471")).isEqualTo(Instant.ofEpochSecond(T0 + 24));
    }

    @Test
    @DisplayName("an alert is FIRST with validFrom stored, and tombstoned when it leaves the full feed")
    void alertLifecycle() {
        serve("/realtime/Alerts.pb", GtfsRtFixtures.alertImpactPeriod());
        assertThat(fetch(ALERTS).success()).isTrue();

        assertThat(reasons(ALERTS, "B")).last().isEqualTo((int) PersistReason.FIRST.code());
        assertThat(head("valid_from", ALERTS, "A")).isEqualTo(Instant.ofEpochSecond(T0 + 3_600));
        assertThat(head("valid_from", ALERTS, "B")).isEqualTo(Instant.ofEpochSecond(T0 - 3_600));
        assertThat(tombstoned(ALERTS, "B")).isFalse();

        clock.setInstant(Instant.ofEpochSecond(T0 + 65));
        serve("/realtime/Alerts.pb", GtfsRtFixtures.fullDatasetAlertRemoved());
        assertThat(fetch(ALERTS).success()).isTrue();

        assertThat(tombstoned(ALERTS, "B")).isTrue();
        assertThat(tombstoned(ALERTS, "A")).isFalse();
        assertThat(reasons(ALERTS, "B")).last().isEqualTo((int) PersistReason.TOMBSTONE.code());
    }

    @Test
    @DisplayName("the registry holds every reviewed source and the enabled catalog rows, TRACK + SAMPLED")
    void registered() {
        assertThat(registry.find("transit-entur-norway")).isPresent();
        assertThat(registry.find("transit-alerts-capmetro-austin")).isEmpty();
        assertThat(registry.find("transit-mdb-mdb-9001")).isPresent();
        assertThat(registry.find("transit-alerts-mdb-mdb-9003")).isPresent();
        assertThat(registry.find("transit-mdb-mdb-9005")).as("a private literal fails the URL policy").isEmpty();
        assertThat(registry.find(VEHICLES).orElseThrow().descriptor().historyTier()).isEqualTo(HistoryTier.TRACK);
        assertThat(AdapterFixtures.bytes("transit/synthetic", "html-200.html")).isNotEmpty();
    }
}
