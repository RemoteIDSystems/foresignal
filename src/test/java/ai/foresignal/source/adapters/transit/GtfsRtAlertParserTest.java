package ai.foresignal.source.adapters.transit;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.transit.GtfsRtFixtures;
import com.google.transit.realtime.GtfsRealtime.Alert;
import com.google.transit.realtime.GtfsRealtime.EntitySelector;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link GtfsRtAlertParser} (prompt 070): impact versus legacy active periods, an open-ended period without
 * {@code validUntil}, the translation choice, the informed-entity cap, the text caps and the captured MBTA and HSL
 * alerts.
 */
class GtfsRtAlertParserTest {

    private static final long T0 = GtfsRtFixtures.T0;

    private static FeedMessage fixture(String dir, String name) throws Exception {
        return GtfsRt.parse(AdapterFixtures.bytes("transit/" + dir, name));
    }

    private static NormalizedRecord byId(GtfsRtAlertParser.Result result, String id) {
        return result.records().stream().filter(r -> r.externalId().equals(id)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("impact periods: validFrom = eventTime = earliest start, validUntil = latest end, header time observed")
    void impactPeriods() throws Exception {
        GtfsRtAlertParser.Result result = GtfsRtAlertParser.parse(fixture("synthetic", "alert-impact-period.pb"), "en");

        NormalizedRecord a = byId(result, "A");
        assertThat(a.kind()).isEqualTo(GtfsRtAlertParser.KIND);
        assertThat(a.observedAt()).isEqualTo(Instant.ofEpochSecond(T0));
        assertThat(a.validFrom()).isEqualTo(Instant.ofEpochSecond(T0 + 3_600));
        assertThat(a.eventTime()).isEqualTo(a.validFrom());
        assertThat(a.validUntil()).isEqualTo(Instant.ofEpochSecond(T0 + 93_600));
        assertThat(a.geometry()).isNull();
        assertThat(a.payload()).containsEntry("headerText", "Red Line detour")
                .containsEntry("descriptionText", "Shuttle buses replace trains between Alewife and Harvard.")
                .containsEntry("url", "https://www.example.org/alerts/A").containsEntry("cause", "SPECIAL_EVENT")
                .containsEntry("effect", "DETOUR").containsEntry("severityLevel", "WARNING")
                .containsEntry("causeDetail", "Marathon").containsEntry("effectDetail", "Shuttle buses");
        assertThat(a.payload().get("communicationPeriods")).isEqualTo(List.of(Map.of(
                "start", Instant.ofEpochSecond(T0 - 86_400).toString(), "end", Instant.ofEpochSecond(T0 + 172_800).toString())));
        assertThat((List<?>) a.payload().get("impactPeriods")).hasSize(2);
        assertThat(a.payload().get("informed")).isEqualTo(List.of(Map.of("agencyId", "1", "routeId", "Red",
                "routeType", 1)));
        assertThat(a.payload().keySet()).isSubsetOf(GtfsRtAlertParser.PAYLOAD_FIELDS);
        assertThat(result.fullDataset()).isTrue();
    }

    @Test
    @DisplayName("an open-ended period asserts no expiry: validUntil is null, validFrom is its start")
    void openEnded() throws Exception {
        NormalizedRecord b = byId(GtfsRtAlertParser.parse(fixture("synthetic", "alert-impact-period.pb"), "en"), "B");

        assertThat(b.validFrom()).isEqualTo(Instant.ofEpochSecond(T0 - 3_600));
        assertThat(b.validUntil()).isNull();
        assertThat(b.payload().get("impactPeriods")).isEqualTo(List.of(Map.of("start",
                Instant.ofEpochSecond(T0 - 3_600).toString())));
        assertThat(b.payload()).doesNotContainKey("communicationPeriods");
    }

    @Test
    @DisplayName("legacy active_period only: used for validity and as impactPeriods; Finnish chosen by language")
    void legacyActivePeriod() throws Exception {
        FeedMessage message = fixture("synthetic", "alert-legacy-active-period.pb");

        NormalizedRecord en = byId(GtfsRtAlertParser.parse(message, "en"), "L");
        assertThat(en.validFrom()).isEqualTo(Instant.ofEpochSecond(T0 - 7_200));
        assertThat(en.validUntil()).isEqualTo(Instant.ofEpochSecond(T0 + 90_000));
        assertThat(en.payload()).containsEntry("headerText", "Tram 6: diversion");
        assertThat((List<?>) en.payload().get("impactPeriods")).hasSize(2);
        assertThat(en.payload().get("informed")).isEqualTo(List.of(Map.of("routeId", "1006", "tripId",
                "1006_20261013_1")));

        NormalizedRecord fi = byId(GtfsRtAlertParser.parse(message, "fi"), "L");
        assertThat(fi.payload()).containsEntry("headerText", "Raitiolinja 6: poikkeusreitti")
                .containsEntry("descriptionText", "Ratatyöt Hämeentiellä.");
    }

    @Test
    @DisplayName("translation choice: exact tag, then primary subtag, then the first translation")
    void translations() {
        var text = GtfsRtFixtures.text("es-419", "Desvío", "pt-BR", "Desvio", "en", "Detour");
        assertThat(GtfsRtAlertParser.text(text, "en")).isEqualTo("Detour");
        assertThat(GtfsRtAlertParser.text(text, "PT-br")).isEqualTo("Desvio");
        assertThat(GtfsRtAlertParser.text(text, "es")).isEqualTo("Desvío");
        assertThat(GtfsRtAlertParser.text(text, "fi")).isEqualTo("Desvío");
        assertThat(GtfsRtAlertParser.text(GtfsRtFixtures.text(null, "Only text"), "en")).isEqualTo("Only text");
        assertThat(GtfsRtAlertParser.text(GtfsRtFixtures.text("en", "   "), "en")).isNull();
        assertThat(GtfsRtAlertParser.text(null, "en")).isNull();
    }

    @Test
    @DisplayName("caps: texts at 4 KB on a code-point boundary, 50 informed entities, 20 periods")
    void caps() {
        String longText = "é".repeat(3000);
        Alert.Builder alert = Alert.newBuilder().setDescriptionText(GtfsRtFixtures.text("en", longText))
                .setHeaderText(GtfsRtFixtures.text("en", "x".repeat(5000)));
        for (int i = 0; i < 80; i++) {
            alert.addInformedEntity(EntitySelector.newBuilder().setStopId("s" + i));
            alert.addImpactPeriod(GtfsRtFixtures.range(T0 + i, T0 + 100 + i));
        }
        alert.addInformedEntity(0, EntitySelector.newBuilder());
        FeedMessage message = FeedMessage.newBuilder().setHeader(GtfsRtFixtures.header(T0))
                .addEntity(FeedEntity.newBuilder().setId("C").setAlert(alert)).build();

        NormalizedRecord c = GtfsRtAlertParser.parse(message, "en").records().get(0);

        String description = (String) c.payload().get("descriptionText");
        assertThat(description.getBytes(StandardCharsets.UTF_8).length).isEqualTo(4096);
        assertThat(description).isEqualTo("é".repeat(2048));
        assertThat(((String) c.payload().get("headerText"))).hasSize(4096);
        assertThat((List<?>) c.payload().get("informed")).hasSize(50).first().isEqualTo(Map.of("stopId", "s0"));
        assertThat((List<?>) c.payload().get("impactPeriods")).hasSize(20);
        assertThat(c.validFrom()).isEqualTo(Instant.ofEpochSecond(T0));
        assertThat(c.validUntil()).isEqualTo(Instant.ofEpochSecond(T0 + 179));
    }

    @Test
    @DisplayName("is_deleted entities are reported, not mapped; a DIFFERENTIAL header is not a full dataset")
    void deletions() throws Exception {
        GtfsRtAlertParser.Result result = GtfsRtAlertParser.parse(fixture("synthetic", "differential-alert-deleted.pb"),
                "en");

        assertThat(result.records()).isEmpty();
        assertThat(result.deleted()).containsExactly("B");
        assertThat(result.fullDataset()).isFalse();
        assertThat(result.observedAt()).isEqualTo(Instant.ofEpochSecond(T0 + 120));
    }

    @Test
    @DisplayName("no period at all: no validFrom, eventTime or validUntil")
    void noPeriods() {
        FeedMessage message = FeedMessage.newBuilder().setHeader(GtfsRtFixtures.header(T0))
                .addEntity(FeedEntity.newBuilder().setId("N").setAlert(Alert.newBuilder()
                        .setHeaderText(GtfsRtFixtures.text("en", "General notice")))).build();

        NormalizedRecord n = GtfsRtAlertParser.parse(message, null).records().get(0);

        assertThat(n.validFrom()).isNull();
        assertThat(n.eventTime()).isNull();
        assertThat(n.validUntil()).isNull();
        assertThat(n.payload()).containsOnlyKeys("headerText");
    }

    @Test
    @DisplayName("MBTA and HSL captures: 30 alerts each, legacy periods, English chosen from many translations")
    void captures() throws Exception {
        GtfsRtAlertParser.Result mbta = GtfsRtAlertParser.parse(fixture("mbta", "Alerts.pb"), "en");
        assertThat(mbta.records()).hasSize(30);
        NormalizedRecord first = byId(mbta, "1000001");
        assertThat(first.validFrom()).isEqualTo(Instant.ofEpochSecond(1_752_760_920L));
        assertThat(first.validUntil()).isNull();
        assertThat(first.payload()).containsEntry("url", "https://www.dot.ri.gov/projects/PVDStation/index.php")
                .containsEntry("cause", "UNKNOWN_CAUSE");

        GtfsRtAlertParser.Result hsl = GtfsRtAlertParser.parse(fixture("hsl-helsinki", "service-alerts.pb"), "en");
        NormalizedRecord tapanila = byId(hsl, "315885");
        assertThat(tapanila.validFrom()).isEqualTo(Instant.ofEpochSecond(1_788_589_380L));
        assertThat(tapanila.validUntil()).isEqualTo(Instant.ofEpochSecond(1_793_397_540L));
        assertThat(tapanila.payload()).containsEntry("headerText",
                "The northern underpass at Tapanila station closed from 7 Sept-30 Oct");
        assertThat(GtfsRtAlertParser.parse(fixture("hsl-helsinki", "service-alerts.pb"), "sv").records().stream()
                .filter(r -> r.externalId().equals("315885")).findFirst().orElseThrow().payload())
                .containsEntry("headerText", "Norra gångtunneln vid Mosabacka station stängd 7.9–30.10.");
    }
}
