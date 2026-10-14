package ai.foresignal.source.adapters.faa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

/**
 * {@link NasStatusParser} on the live capture (2026-10-14) and a hand-made document with every delay type (prompt
 * 087): kinds and fields, duration parsing, closure reopen times, repeated blocks, and an unknown airport without
 * geometry; and the bundled {@link UsAirports} table.
 */
class NasStatusParserTest {

    static final Instant NOW = Instant.parse("2026-10-14T04:33:00Z");

    private static NormalizedRecord byId(List<NormalizedRecord> records, String id) {
        return records.stream().filter(r -> r.externalId().equals(id)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("the live document: one departure delay and four closures over two repeated blocks, at airport points")
    void live() {
        NasStatusParser.Parsed parsed = NasStatusParser.parse(AdapterFixtures.bytes("faa-nas-status",
                "airport-status-information.xml"), UsAirports.bundled(), NOW);

        assertThat(parsed.updateTime()).isEqualTo(Instant.parse("2026-10-14T04:32:43Z"));
        assertThat(parsed.records()).extracting(NormalizedRecord::externalId).containsExactly("ORD|departure-delay",
                "SNA|closure", "LAX|closure", "SAN|closure", "PHL|closure");
        assertThat(parsed.unknownAirports()).isZero();
        NormalizedRecord ord = byId(parsed.records(), "ORD|departure-delay");
        assertThat(ord.payload()).containsEntry("minMin", 16).containsEntry("maxMin", 30)
                .containsEntry("trend", "Increasing").containsEntry("reason", "WX:Thunderstorms");
        assertThat(ord.observedAt()).isEqualTo(Instant.parse("2026-10-14T04:32:43Z"));
        assertThat(ord.geometry()).isInstanceOf(Point.class);
        assertThat(((Point) ord.geometry()).getY()).isBetween(41.9, 42.1);
        assertThat(byId(parsed.records(), "SAN|closure").payload()).as("the NOTAM validity gives the year")
                .containsEntry("reopen", "2027-01-01T08:00:00Z");
        assertThat(byId(parsed.records(), "LAX|closure").payload()).as("a year-long closure")
                .containsEntry("reopen", "2027-05-28T16:00:00Z");
        assertThat(byId(parsed.records(), "SNA|closure").payload()).containsEntry("reopen", "2026-10-17T13:15:00Z");
    }

    @Test
    @DisplayName("every delay type: ground delay, ground stop, arrival and departure delays, closures and deicing")
    void everyType() {
        NasStatusParser.Parsed parsed = NasStatusParser.parse(AdapterFixtures.bytes("faa-nas-status",
                "all-delay-types.xml"), UsAirports.bundled(), NOW);

        assertThat(parsed.updateTime()).isEqualTo(Instant.parse("2026-10-10T23:14:47Z"));
        assertThat(parsed.records()).extracting(NormalizedRecord::externalId).containsExactly("EWR|ground-delay",
                "SFO|ground-delay", "LGA|ground-stop", "ORD|departure-delay", "ATL|arrival-delay", "SNA|closure",
                "SAN|closure", "ZZZQ|closure", "MSP|deicing");
        assertThat(byId(parsed.records(), "EWR|ground-delay").payload()).containsEntry("avgMin", 66)
                .containsEntry("maxMin", 168).containsEntry("delayKind", "ground-delay");
        assertThat(byId(parsed.records(), "SFO|ground-delay").payload()).containsEntry("avgMin", 45)
                .containsEntry("maxMin", 60);
        assertThat(byId(parsed.records(), "LGA|ground-stop").payload()).containsEntry("endTime", "7:45 pm EDT")
                .containsEntry("reason", "thunderstorms");
        assertThat(byId(parsed.records(), "ATL|arrival-delay").payload()).containsEntry("minMin", 31)
                .containsEntry("trend", "Decreasing");
        assertThat(byId(parsed.records(), "MSP|deicing").payload()).containsEntry("reason", "Deicing in effect");
    }

    @Test
    @DisplayName("an airport missing from the table gives a record without geometry, counted")
    void unknownAirport() {
        NasStatusParser.Parsed parsed = NasStatusParser.parse(AdapterFixtures.bytes("faa-nas-status",
                "all-delay-types.xml"), UsAirports.bundled(), NOW);

        assertThat(parsed.unknownAirports()).isEqualTo(1);
        assertThat(byId(parsed.records(), "ZZZQ|closure").geometry()).isNull();
        assertThat(parsed.records()).filteredOn(r -> !r.externalId().startsWith("ZZZQ"))
                .allSatisfy(r -> assertThat(r.geometry()).as(r.externalId()).isNotNull());
    }

    @Test
    @DisplayName("durations: hours and minutes in any combination; text without a number is null")
    void durations() {
        assertThat(NasStatusParser.minutes("2 hours and 48 minutes")).isEqualTo(168);
        assertThat(NasStatusParser.minutes("1 hour and 6 minutes")).isEqualTo(66);
        assertThat(NasStatusParser.minutes("1 hour")).isEqualTo(60);
        assertThat(NasStatusParser.minutes("45 minutes")).isEqualTo(45);
        assertThat(NasStatusParser.minutes("1 minute")).isEqualTo(1);
        assertThat(NasStatusParser.minutes("unknown")).isNull();
        assertThat(NasStatusParser.minutes(null)).isNull();
    }

    @Test
    @DisplayName("Update_Time and Reopen formats, the closure NOTAM's validity first; unreadable text is kept")
    void times() {
        assertThat(NasStatusParser.updateTime("Sat Oct 10 23:14:47 2026 GMT"))
                .isEqualTo(Instant.parse("2026-10-10T23:14:47Z"));
        assertThat(NasStatusParser.updateTime("Thu Oct 1 01:02:03 2026 GMT"))
                .isEqualTo(Instant.parse("2026-10-01T01:02:03Z"));
        assertThat(NasStatusParser.updateTime("yesterday")).isNull();
        assertThat(NasStatusParser.reopen("Oct 17 at 13:15 UTC.", null, NOW)).isEqualTo("2026-10-17T13:15:00Z");
        assertThat(NasStatusParser.reopen("May 28 at 16:00 UTC.", null, NOW)).as("a past date is next year's")
                .isEqualTo("2027-05-28T16:00:00Z");
        assertThat(NasStatusParser.reopen("May 28 at 16:00 UTC.", "!LAX 05/277 LAX AD AP CLSD 2605271826-2705281600",
                NOW)).isEqualTo("2027-05-28T16:00:00Z");
        assertThat(NasStatusParser.reopen("until further notice", null, NOW)).isEqualTo("until further notice");
    }

    @Test
    @DisplayName("not a NAS status document, or a DOCTYPE, is a format error")
    void rejected() {
        assertThatThrownBy(() -> NasStatusParser.parse("<html/>".getBytes(StandardCharsets.UTF_8), null, NOW))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> NasStatusParser.parse(("<!DOCTYPE AIRPORT_STATUS_INFORMATION SYSTEM"
                + " \"http://www.fly.faa.gov/AirportStatus.dtd\"><AIRPORT_STATUS_INFORMATION/>")
                .getBytes(StandardCharsets.UTF_8), null, NOW)).isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("the bundled airport table: FAA identifiers, ICAO codes and quoted names")
    void airports() throws Exception {
        UsAirports bundled = UsAirports.bundled();
        assertThat(bundled.size()).isGreaterThan(10_000);
        assertThat(bundled.find("ORD").icao()).isEqualTo("KORD");
        assertThat(bundled.find("KORD").faaLid()).isEqualTo("ORD");
        assertThat(bundled.find("gum").icao()).isEqualTo("PGUM");
        assertThat(bundled.find("ZZZQ")).isNull();
        UsAirports small = UsAirports.read(new ByteArrayInputStream(("faa_lid,icao,name,lat,lon\n"
                + "AAA,KAAA,\"Town, Field \"\"One\"\"\",30.5,-97.25\n").getBytes(StandardCharsets.UTF_8)));
        assertThat(small.find("AAA").name()).isEqualTo("Town, Field \"One\"");
        assertThat(small.find("AAA").lon()).isEqualTo(-97.25);
    }
}
