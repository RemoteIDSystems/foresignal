package ai.foresignal.source.adapters.satdump;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.adapters.celestrak.CelestrakGpParser;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.OrbitalElements;
import ai.foresignal.space.OmmElements;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * A SatDump station's predicted passes (prompt 064, Weather-Imagery-Design §4.4): {@code predicted} versions
 * under {@code {station}:{satKey}:{aosEpochMinute}}, reused by an RF session within ±5 min so {@code receiving}
 * continues the same record, {@code missed} when nobody received a pass, tombstones for withdrawn predictions.
 * Driven on a real {@link SatDumpStation} over {@link SatDumpTestContext}, with the ISS element set of 2026-10-13
 * as {@code predictNorad}.
 */
class StationPredictedPassIT {

    private static final Instant T = Instant.parse("2026-10-13T06:00:00Z");

    @TempDir Path temp;

    private SatDumpTestContext ctx;
    private final AtomicReference<OmmElements> elements = new AtomicReference<>();
    private SatDumpStation station;

    @BeforeEach
    void setUp() throws Exception {
        ctx = new SatDumpTestContext(Files.createDirectories(temp.resolve("inbox")), T);
        ctx.params.put(SatDumpDescriptor.PREDICT_NORAD, List.of("25544"));
        elements.set(iss());
        OrbitalElements lookup = norad -> norad == 25544 ? Optional.ofNullable(elements.get()) : Optional.empty();
        station = new SatDumpAdapter("home", () -> null, ctx::holder, null, () -> lookup).newStation(ctx, ctx.sink());
    }

    private static OmmElements iss() {
        return CelestrakGpParser.parseCsv(AdapterFixtures.bytes("celestrak-gp", "stations.csv"), "stations")
                .elements().get(0);
    }

    private List<NormalizedRecord> passes() {
        return ctx.drain().stream().filter(r -> PassBook.KIND.equals(r.kind())).toList();
    }

    @Test
    @DisplayName("a predicted pass is followed by receiving on the same externalId")
    void predictedThenReceiving() {
        station.scanInboxOnce();
        List<NormalizedRecord> predicted = passes();
        assertThat(predicted).isNotEmpty().allSatisfy(r -> {
            assertThat(r.payload()).containsEntry("status", "predicted").containsKey("tca").containsKey("aosAzimuthDeg");
            assertThat(r.externalId()).startsWith("home:");
        });
        NormalizedRecord first = predicted.get(0);
        Instant aos = Instant.parse((String) first.payload().get("aos"));
        assertThat(first.eventTime()).isEqualTo(aos);
        assertThat(first.externalId()).endsWith(":" + Math.floorDiv(aos.getEpochSecond(), 60));
        String satKey = (String) first.payload().get("satKey");

        Instant start = aos.plusSeconds(40);
        String id = station.book().sessionPassId(satKey, start);
        assertThat(id).isEqualTo(first.externalId());
        NormalizedRecord receiving = station.book().open(new RfSession(id, satKey, "ISS", ApiSnapshot.AUTOTRACK, start),
                new double[] {-95.37, 29.76});
        assertThat(receiving.externalId()).isEqualTo(first.externalId());
        assertThat(receiving.payload()).containsEntry("status", "receiving");
        assertThat(station.book().sessionPassId(satKey, aos.plus(Duration.ofMinutes(30))))
                .isNotEqualTo(first.externalId());
    }

    @Test
    @DisplayName("predictions are made at most every 30 min and an unchanged prediction emits nothing")
    void unchangedNoChurn() {
        station.scanInboxOnce();
        java.util.Set<String> ids = new java.util.HashSet<>(passes().stream().map(NormalizedRecord::externalId).toList());
        assertThat(ids).isNotEmpty();
        ctx.clock.advance(Duration.ofMinutes(10));
        station.scanInboxOnce();
        assertThat(passes()).as("no prediction within 30 min").isEmpty();
        ctx.clock.advance(Duration.ofMinutes(25));
        station.scanInboxOnce();
        // The window moved 35 min, so a new pass may enter at its far end; the known passes are unchanged.
        assertThat(passes()).noneMatch(r -> ids.contains(r.externalId()));
    }

    @Test
    @DisplayName("no session by LOS + 20 min: missed while healthy; without elements nothing is withdrawn")
    void missed() {
        station.scanInboxOnce();
        NormalizedRecord first = passes().get(0);
        Instant los = Instant.parse((String) first.payload().get("los"));
        elements.set(null);
        ctx.clock.setInstant(los.plus(Duration.ofMinutes(21)));
        station.scanInboxOnce();
        List<NormalizedRecord> after = passes();
        assertThat(after).filteredOn(r -> r.externalId().equals(first.externalId())).singleElement()
                .satisfies(r -> assertThat(r.payload()).containsEntry("status", "missed"));
        assertThat(after).noneMatch(NormalizedRecord::tombstone);
    }

    @Test
    @DisplayName("an unhealthy window tombstones the predicted pass; a changed orbit withdraws the old predictions")
    void tombstones() {
        station.scanInboxOnce();
        List<NormalizedRecord> predicted = passes();
        NormalizedRecord first = predicted.get(0);
        Instant los = Instant.parse((String) first.payload().get("los"));
        List<NormalizedRecord> judged = station.book().expirePredicted(los.plus(Duration.ofMinutes(21)),
                (a, b) -> false, null);
        assertThat(judged).filteredOn(r -> r.externalId().equals(first.externalId())).singleElement()
                .satisfies(r -> assertThat(r.tombstone()).isTrue());

        OmmElements e = iss();
        elements.set(new OmmElements(e.objectName(), e.objectId(), e.epoch(), e.meanMotion(), e.eccentricity(),
                e.inclination(), e.raan() + 40, e.argOfPericenter(), e.meanAnomaly(), e.ephemerisType(),
                e.classification(), e.norad(), e.elementSetNo(), e.revAtEpoch(), e.bstar(), e.meanMotionDot(),
                e.meanMotionDdot()));
        ctx.clock.advance(Duration.ofMinutes(31));
        station.scanInboxOnce();
        assertThat(passes()).anyMatch(NormalizedRecord::tombstone);
    }
}
