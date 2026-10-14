package ai.foresignal.source.formats.aprs;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

/** {@link AprsRecords} (prompt 082): the station snapshot, merge, observers, tombstones and messages. */
class AprsRecordsTest {

    private static final Instant T = Instant.parse("2026-10-14T00:30:00Z");
    private static final Observer IGATE = Observer.of("N0CALL-10");

    private static List<NormalizedRecord> map(AprsRecords records, String line, boolean messages) {
        return records.map(AprsParser.parse(line), IGATE, messages, T);
    }

    @Test
    @DisplayName("a position: externalId is the call, the igate is the observer, path and raw are present")
    void position() {
        NormalizedRecord r = map(new AprsRecords(),
                "N0CALL-9>APRS,WIDE1-1,qAR,N0CALL-10:=3745.00N/12225.00W>090/036/A=001234 mobile", false).getFirst();

        assertThat(r.externalId()).isEqualTo("N0CALL-9");
        assertThat(r.kind()).isEqualTo(AprsRecords.STATION_KIND);
        assertThat(r.observedAt()).isNull();
        assertThat(r.observers()).containsExactly(IGATE);
        assertThat(r.payload()).containsEntry("symbol", "/>").containsEntry("type", "position")
                .containsEntry("speedMps", 18.52).containsEntry("courseDeg", 90.0).containsEntry("altitudeM", 376.1)
                .containsEntry("comment", "mobile").containsEntry("path", "WIDE1-1,qAR,N0CALL-10")
                .doesNotContainKeys("igate", "receiver", "observer");
        assertThat(((Point) r.geometry()).getY()).isEqualTo(37.75);
    }

    @Test
    @DisplayName("a status merges into the station's snapshot and keeps its position; a position keeps the status")
    void statusMerge() {
        AprsRecords records = new AprsRecords();
        map(records, "N0CALL-5>APRS,TCPIP*,qAC,T2TEST:!3745.00N/12225.00W-home", false);
        NormalizedRecord status = map(records, "N0CALL-5>APRS,TCPIP*,qAC,T2TEST:>On the air", false).getFirst();
        NormalizedRecord again = map(records, "N0CALL-5>APRS,TCPIP*,qAC,T2TEST:!3745.00N/12225.00W-home", false)
                .getFirst();

        assertThat(status.payload()).containsEntry("status", "On the air").containsEntry("comment", "home")
                .containsEntry("type", "position");
        assertThat(status.geometry()).isNotNull();
        assertThat(again.payload()).containsEntry("status", "On the air");
        assertThat(records.stations()).isEqualTo(1);
    }

    @Test
    @DisplayName("a status from an unknown station has no geometry and type status")
    void statusOnly() {
        NormalizedRecord r = map(new AprsRecords(), "N0CALL-5>APRS:>QRV", false).getFirst();

        assertThat(r.geometry()).isNull();
        assertThat(r.payload()).containsEntry("type", "status").containsEntry("status", "QRV");
        assertThat(r.payload()).doesNotContainKey("path");
    }

    @Test
    @DisplayName("objects use their name and carry the sender; a killed object is a tombstone at the receive time")
    void objects() {
        AprsRecords records = new AprsRecords();
        NormalizedRecord live = map(records, "N0CALL>APRS:;EVENT-1  *141230z3745.00N/12225.00W/fair", false).getFirst();
        NormalizedRecord killed = map(records, "N0CALL>APRS:;EVENT-1  _141230z3745.00N/12225.00W/", false).getFirst();

        assertThat(live.externalId()).isEqualTo("EVENT-1");
        assertThat(live.payload()).containsEntry("sender", "N0CALL").containsEntry("type", "object");
        assertThat(killed.tombstone()).isTrue();
        assertThat(killed.externalId()).isEqualTo("EVENT-1");
        assertThat(killed.observedAt()).isEqualTo(T);
        assertThat(records.stations()).isZero();
    }

    @Test
    @DisplayName("messages only when included, never acknowledgements; unsupported packets produce nothing")
    void messages() {
        String message = "N0CALL-1>APRS,TCPIP*,qAC,T2TEST::N0CALL-2 :Hello there{42";

        assertThat(map(new AprsRecords(), message, false)).isEmpty();
        NormalizedRecord r = map(new AprsRecords(), message, true).getFirst();
        assertThat(r.kind()).isEqualTo(AprsRecords.MESSAGE_KIND);
        assertThat(r.externalId()).isEqualTo("N0CALL-1>N0CALL-2{42");
        assertThat(r.payload()).containsEntry("from", "N0CALL-1").containsEntry("addressee", "N0CALL-2")
                .containsEntry("text", "Hello there").containsEntry("msgId", "42");
        assertThat(map(new AprsRecords(), "N0CALL-2>APRS::N0CALL-1 :ack42", true)).isEmpty();
        assertThat(map(new AprsRecords(), "N0CALL-1>APRS::N0CALL-2 :no id", true).getFirst().externalId())
                .startsWith("N0CALL-1>N0CALL-2{").hasSize("N0CALL-1>N0CALL-2{".length() + 16);
        assertThat(map(new AprsRecords(), "N0CALL>APRS:T#001,1,2,3", true)).isEmpty();
        assertThat(new AprsRecords().map(null, IGATE, true, T)).isEmpty();
    }

    @Test
    @DisplayName("raw is cut at 512 characters")
    void rawCap() {
        assertThat(AprsRecords.raw("y".repeat(600))).hasSize(AprsRecords.MAX_RAW);
        assertThat(AprsRecords.raw(null)).isNull();
    }

    @Test
    @DisplayName("a re-beacon through another igate differs only in volatile fields and the observer")
    void rebeacon() {
        AprsRecords records = new AprsRecords();
        NormalizedRecord first = records.map(AprsParser.parse(
                "N0CALL-9>APRS,WIDE1-1,qAR,N0CALL-10:=3745.00N/12225.00W>mobile"), IGATE, false, T).getFirst();
        NormalizedRecord second = records.map(AprsParser.parse(
                "N0CALL-9>APRS,WIDE2-1,qAR,N0CALL-11:=3745.00N/12225.00W>mobile"), Observer.of("N0CALL-11"), false, T)
                .getFirst();

        java.util.Map<String, Object> a = new java.util.HashMap<>(first.payload());
        java.util.Map<String, Object> b = new java.util.HashMap<>(second.payload());
        AprsRecords.VOLATILE.forEach(k -> {
            a.remove(k);
            b.remove(k);
        });
        assertThat(a).isEqualTo(b);
        assertThat(first.payload().get("path")).isNotEqualTo(second.payload().get("path"));
    }
}
