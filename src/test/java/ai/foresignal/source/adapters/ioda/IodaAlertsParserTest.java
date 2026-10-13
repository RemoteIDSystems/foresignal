package ai.foresignal.source.adapters.ioda;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link IodaAlertsParser} (prompt 068): the captured envelope, the fold to the newest alert per key, the
 * datasource and entity-type filters, {@code ratio}, unknown levels and malformed envelopes.
 */
class IodaAlertsParserTest {

    static final String ID = IodaOutagesAdapter.ID;
    static final Set<String> ALL_TYPES = Set.copyOf(IodaAlertsParser.ENTITY_TYPES);
    static final Set<String> ALL_SOURCES = Set.copyOf(IodaAlertsParser.DATASOURCES);

    private static IodaAlertsParser.Page captured() {
        return IodaAlertsParser.parse(AdapterFixtures.bytes(ID, "alerts-12h.json"));
    }

    private static byte[] envelope(String data) {
        return ("{\"type\":\"outages.alerts\",\"error\":null,\"data\":" + data + ",\"copyright\":\"c\"}")
                .getBytes(StandardCharsets.UTF_8);
    }

    private static String alert(String type, String code, String datasource, long time, String level, String value,
            String history) {
        return "{\"datasource\":\"" + datasource + "\",\"entity\":{\"code\":\"" + code + "\",\"name\":\"N " + code
                + "\",\"type\":\"" + type + "\",\"attrs\":{}},\"time\":" + time + ",\"level\":\"" + level
                + "\",\"condition\":\"< 0.99\",\"value\":" + value + ",\"historyValue\":" + history
                + ",\"method\":\"median\"}";
    }

    @Test
    @DisplayName("the captured envelope: 40 rows, every field of the first alert")
    void envelope() {
        IodaAlertsParser.Page page = captured();

        assertThat(page.rows()).isEqualTo(40);
        assertThat(page.skipped()).isZero();
        assertThat(page.alerts()).hasSize(40);
        IodaAlertsParser.Alert first = page.alerts().get(0);
        assertThat(first.datasource()).isEqualTo("ping-slash24");
        assertThat(first.entityType()).isEqualTo("asn");
        assertThat(first.entityCode()).isEqualTo("8643");
        assertThat(first.entityName()).isEqualTo("AS8643 (ATHENAnet)");
        assertThat(first.attrs()).containsEntry("org", "NATIONAL AND KAPODISTRIAN UNIVERSITY OF ATHENS");
        assertThat(first.time()).isEqualTo(Instant.ofEpochSecond(1791861600L));
        assertThat(first.level()).isEqualTo("critical");
        assertThat(first.condition()).isEqualTo("< 0.8");
        assertThat(first.value()).isEqualTo(40L);
        assertThat(first.historyValue()).isEqualTo(56L);
        assertThat(first.method()).isEqualTo("median");
        assertThat(first.key()).isEqualTo("asn:8643:ping-slash24");
    }

    @Test
    @DisplayName("fold keeps the newest alert per key, and of equal times the later in the array")
    void foldNewest() {
        List<IodaAlertsParser.Alert> folded = IodaAlertsParser.fold(captured().alerts(), ALL_TYPES, ALL_SOURCES);

        assertThat(folded).hasSize(22);
        assertThat(folded).extracting(IodaAlertsParser.Alert::key).doesNotHaveDuplicates();
        IodaAlertsParser.Alert santo = folded.stream().filter(a -> a.key().equals("region:497:merit-nt")).findFirst()
                .orElseThrow();
        assertThat(santo.level()).isEqualTo("normal");
        assertThat(santo.time()).isEqualTo(Instant.ofEpochSecond(1791867960L));
        IodaAlertsParser.Alert haiti = folded.stream().filter(a -> a.key().equals("country:HT:bgp")).findFirst()
                .orElseThrow();
        assertThat(haiti.level()).isEqualTo("normal");
        assertThat(haiti.time()).isEqualTo(Instant.ofEpochSecond(1791879000L));

        byte[] tie = envelope("[" + alert("country", "BI", "bgp", 100, "critical", "1", "2") + ","
                + alert("country", "BI", "bgp", 100, "normal", "2", "2") + ","
                + alert("country", "BI", "bgp", 90, "critical", "1", "2") + "]");
        List<IodaAlertsParser.Alert> tied = IodaAlertsParser.fold(IodaAlertsParser.parse(tie).alerts(), ALL_TYPES,
                ALL_SOURCES);
        assertThat(tied).singleElement().satisfies(a -> assertThat(a.level()).isEqualTo("normal"));
    }

    @Test
    @DisplayName("critical-then-normal: one key, the normal alert wins")
    void criticalThenNormal() {
        List<IodaAlertsParser.Alert> folded = IodaAlertsParser.fold(
                IodaAlertsParser.parse(AdapterFixtures.bytes(ID, "critical-then-normal.json")).alerts(), ALL_TYPES,
                ALL_SOURCES);
        assertThat(folded).singleElement().satisfies(a -> {
            assertThat(a.key()).isEqualTo("country:HT:bgp");
            assertThat(a.level()).isEqualTo("normal");
        });
    }

    @Test
    @DisplayName("the default filters keep country and region on bgp and ping-slash24 only")
    void filters() {
        List<IodaAlertsParser.Alert> defaults = IodaAlertsParser.fold(captured().alerts(),
                IodaOutagesAdapter.DEFAULT_ENTITY_TYPES, IodaOutagesAdapter.DEFAULT_DATASOURCES);

        assertThat(defaults).hasSize(14);
        assertThat(defaults).extracting(IodaAlertsParser.Alert::entityType).containsOnly("country", "region");
        assertThat(defaults).extracting(IodaAlertsParser.Alert::datasource).containsOnly("bgp", "ping-slash24");

        List<IodaAlertsParser.Alert> asnOnly = IodaAlertsParser.fold(captured().alerts(), Set.of("asn"), ALL_SOURCES);
        assertThat(asnOnly).extracting(IodaAlertsParser.Alert::entityType).containsOnly("asn");
        assertThat(asnOnly).hasSize(6);

        List<IodaAlertsParser.Alert> gtr = IodaAlertsParser.fold(captured().alerts(), ALL_TYPES, Set.of("gtr"));
        assertThat(gtr).singleElement().satisfies(a -> {
            assertThat(a.key()).isEqualTo("country:NF:gtr");
            assertThat(a.level()).isEqualTo("critical");
        });

        assertThat(IodaAlertsParser.fold(captured().alerts(), ALL_TYPES, ALL_SOURCES))
                .extracting(IodaAlertsParser.Alert::entityType).doesNotContain("geoasn");
    }

    @Test
    @DisplayName("ratio: three decimals half up; null on a zero or missing history")
    void ratio() {
        assertThat(IodaAlertsParser.ratio(35L, 55L)).isEqualTo(0.636);
        assertThat(IodaAlertsParser.ratio(108L, 139L)).isEqualTo(0.777);
        assertThat(IodaAlertsParser.ratio(1L, 8L)).isEqualTo(0.125);
        assertThat(IodaAlertsParser.ratio(658L, 658L)).isEqualTo(1.0);
        assertThat(IodaAlertsParser.ratio(10L, 0L)).isNull();
        assertThat(IodaAlertsParser.ratio(10L, -1L)).isNull();
        assertThat(IodaAlertsParser.ratio(null, 5L)).isNull();
        assertThat(IodaAlertsParser.ratio(5L, null)).isNull();

        IodaAlertsParser.Alert zero = IodaAlertsParser.parse(AdapterFixtures.bytes(ID, "unknown-region.json")).alerts()
                .get(0);
        NormalizedRecord r = IodaAlertsParser.toRecord(zero, null);
        assertThat(r.payload()).containsEntry("historyValue", 0L).doesNotContainKey("ratio");
    }

    @Test
    @DisplayName("the record: externalId, observedAt, no eventTime, every payload key")
    void record() {
        IodaAlertsParser.Alert dominica = IodaAlertsParser.fold(captured().alerts(), ALL_TYPES, ALL_SOURCES).stream()
                .filter(a -> a.key().equals("country:DM:ping-slash24")).findFirst().orElseThrow();

        NormalizedRecord r = IodaAlertsParser.toRecord(dominica, ai.foresignal.source.spi.Geo.point(-61.3, 15.4));

        assertThat(r.externalId()).isEqualTo("country:DM:ping-slash24");
        assertThat(r.kind()).isEqualTo("outage");
        assertThat(r.observedAt()).isEqualTo(Instant.ofEpochSecond(1791899400L));
        assertThat(r.eventTime()).isNull();
        assertThat(r.payload()).containsEntry("entityType", "country").containsEntry("entityCode", "DM")
                .containsEntry("entityName", "Dominica").containsEntry("datasource", "ping-slash24")
                .containsEntry("level", "critical").containsEntry("condition", "< 0.8").containsEntry("value", 35L)
                .containsEntry("historyValue", 55L).containsEntry("ratio", 0.636).containsEntry("method", "median")
                .containsEntry("summary", "Dominica: ping-slash24 critical").containsEntry("country", "DM")
                .containsEntry("geometryApproximate", true);

        NormalizedRecord noGeometry = IodaAlertsParser.toRecord(dominica, null);
        assertThat(noGeometry.geometry()).isNull();
        assertThat(noGeometry.payload()).doesNotContainKey("geometryApproximate");
    }

    @Test
    @DisplayName("country: ISO2 of a country, attrs.country_code of a region, null for an ASN")
    void country() {
        List<IodaAlertsParser.Alert> folded = IodaAlertsParser.fold(captured().alerts(), ALL_TYPES, ALL_SOURCES);
        assertThat(IodaAlertsParser.country(find(folded, "region:497:merit-nt"))).isEqualTo("BR");
        assertThat(IodaAlertsParser.country(find(folded, "country:SS:bgp"))).isEqualTo("SS");
        assertThat(IodaAlertsParser.country(find(folded, "asn:8643:ping-slash24"))).isNull();
    }

    private static IodaAlertsParser.Alert find(List<IodaAlertsParser.Alert> alerts, String key) {
        return alerts.stream().filter(a -> a.key().equals(key)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("an unknown level is kept as sent (lower-cased); warning is accepted")
    void unknownLevel() {
        IodaAlertsParser.Page page = IodaAlertsParser.parse(envelope("[" + alert("country", "BI", "bgp", 100,
                "Degraded", "1", "2") + "," + alert("country", "CD", "bgp", 100, "warning", "1", "2") + "]"));

        assertThat(page.alerts()).extracting(IodaAlertsParser.Alert::level).containsExactly("degraded", "warning");
        assertThat(IodaAlertsParser.toRecord(page.alerts().get(0), null).payload())
                .containsEntry("level", "degraded").containsEntry("summary", "N BI: bgp degraded");
    }

    @Test
    @DisplayName("rows without a datasource, entity, code, time or level are skipped and counted")
    void unreadableRows() {
        IodaAlertsParser.Page page = IodaAlertsParser.parse(envelope("[" + alert("country", "BI", "bgp", 100,
                "critical", "1.5", "2") + ",{\"entity\":{\"code\":\"X\",\"type\":\"country\"},\"time\":1,"
                + "\"level\":\"normal\"},{\"datasource\":\"bgp\",\"time\":1,\"level\":\"normal\"},"
                + "{\"datasource\":\"bgp\",\"entity\":{\"code\":\"X\",\"type\":\"country\"},\"level\":\"normal\"},"
                + "{\"datasource\":\"bgp\",\"entity\":{\"code\":\"X\",\"type\":\"country\"},\"time\":\"12\","
                + "\"level\":\"normal\"},3]"));

        assertThat(page.rows()).isEqualTo(6);
        assertThat(page.skipped()).isEqualTo(4);
        assertThat(page.alerts()).hasSize(2);
        assertThat(page.alerts().get(0).value()).isEqualTo(1.5);
        assertThat(page.alerts().get(1).time()).isEqualTo(Instant.ofEpochSecond(12));
        assertThat(page.alerts().get(1).value()).isNull();
    }

    @Test
    @DisplayName("malformed envelopes are UpstreamFormatException")
    void malformed() {
        assertThatThrownBy(() -> IodaAlertsParser.parse(AdapterFixtures.bytes(ID, "not-envelope.html")))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> IodaAlertsParser.parse(new byte[0])).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> IodaAlertsParser.parse(null)).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> IodaAlertsParser.parse("[]".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> IodaAlertsParser.parse("{\"data\":{}}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> IodaAlertsParser.parse("{\"type\":\"outages.alerts\"}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> IodaAlertsParser.parse("{\"error\":\"Invalid from\",\"data\":[]}"
                .getBytes(StandardCharsets.UTF_8))).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("Invalid from");
        assertThat(IodaAlertsParser.parse("{\"error\":null,\"data\":[]}".getBytes(StandardCharsets.UTF_8)).rows())
                .isZero();
    }
}
