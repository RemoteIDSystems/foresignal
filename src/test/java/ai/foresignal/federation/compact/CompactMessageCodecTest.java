package ai.foresignal.federation.compact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.federation.FederationIdentity;
import ai.foresignal.federation.RecordRef;
import ai.foresignal.notify.mesh.MeshShortCode;
import ai.foresignal.notify.mesh.cbor.MeshCborMessage;
import ai.foresignal.notify.mesh.cbor.MeshCborProfile;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertView;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Compact messages (prompt 113) through the 084 {@link MeshCborProfile}: the extension keys 10 (origin short id),
 * 11 (message id) and 12 (expiry minutes) round trip, the old 084 golden vectors still decode, the size stays at
 * most 160 bytes with keys 9 then 8 dropped before 10–12 (which are never dropped), titles are cut on a code-point
 * boundary, intelligence events are type 5, and only events (never records or tracks) can become a message.
 * Vectors were derived by hand from RFC 8949 as the 084 ones were.
 */
class CompactMessageCodecTest {

    static final HexFormat HEX = HexFormat.of();

    static final byte[] ORIGIN = {1, 2, 3, 4};
    static final byte[] ID = {1, 2, 3, 4, 5, 6, 7, 8};

    /** 084's full alert vector (MeshCborProfileTest.FULL_HEX). */
    static final String FULL_HEX = "aa" + "0001" + "0101" + "0264374b3251" + "0301" + "046a65617274687175616b65"
            + "051a002e3038" + "063a009523af" + "071a69f495c0" + "08665175616b6573"
            + "0970757367732d65617274687175616b6573";

    /** 084's minimal resolved vector. */
    static final String MIN_HEX = "a3" + "0001" + "0102" + "026430303037";

    /** The full alert with keys 10 (4-byte string), 11 (8-byte string) and 12 (1440 as uint16): 13 members. */
    static final String EXTENDED_HEX = "ad" + FULL_HEX.substring(2) + "0a4401020304" + "0b480102030405060708"
            + "0c1905a0";

    static MeshCborMessage full(String title, byte[] origin, byte[] id, Integer expiry) {
        return new MeshCborMessage(1, 1, "7K2Q", 1, "earthquake", 3027000, -9774000, 1777636800L, title,
                "usgs-earthquakes", origin, id, expiry);
    }

    @Test
    @DisplayName("keys 10-12 round trip through the 084 codec (golden vector)")
    void extendedRoundTrip() {
        MeshCborMessage message = full("Quakes", ORIGIN, ID, 1440);
        byte[] encoded = MeshCborProfile.encode(message);
        assertThat(HEX.formatHex(encoded)).isEqualTo(EXTENDED_HEX);
        assertThat(encoded).hasSize(87);
        MeshCborMessage decoded = MeshCborProfile.decode(encoded);
        assertThat(decoded).isEqualTo(message);
        assertThat(decoded.originShortId()).containsExactly(ORIGIN);
        assertThat(decoded.messageId()).containsExactly(ID);
        assertThat(decoded.expiryMinutes()).isEqualTo(1440);
        assertThat(decoded.extended()).isTrue();
    }

    @Test
    @DisplayName("the old 084 golden vectors still decode, without extension keys")
    void oldVectorsStillDecode() {
        MeshCborMessage old = MeshCborProfile.decode(HEX.parseHex(FULL_HEX));
        assertThat(old).isEqualTo(new MeshCborMessage(1, 1, "7K2Q", 1, "earthquake", 3027000, -9774000, 1777636800L,
                "Quakes", "usgs-earthquakes"));
        assertThat(old.extended()).isFalse();
        assertThat(HEX.formatHex(MeshCborProfile.encode(old))).isEqualTo(FULL_HEX);
        MeshCborMessage min = MeshCborProfile.decode(HEX.parseHex(MIN_HEX));
        assertThat(min.type()).isEqualTo(MeshCborProfile.TYPE_RESOLVED);
        assertThat(min.shortCode()).isEqualTo("0007");
        // 084's forward-compatibility vector: key 10 as text and key 11 as an array are skipped, not misread.
        MeshCborMessage skipped = MeshCborProfile.decode(HEX.parseHex("a5" + "0001" + "0104" + "0a6178" + "0b820102"
                + "026441424344"));
        assertThat(skipped.shortCode()).isEqualTo("ABCD");
        assertThat(skipped.originShortId()).isNull();
        assertThat(skipped.messageId()).isNull();
    }

    @Test
    @DisplayName("at most 160 bytes: key 9 is dropped first, then key 8, never 10-12")
    void dropOrder() {
        // A 40-character title of two-byte characters: 162 bytes whole, 144 without the source id.
        MeshCborMessage forty = full("é".repeat(40), ORIGIN, ID, 1440);
        byte[] fit = MeshCborProfile.encode(forty);
        assertThat(fit).hasSize(144);
        MeshCborMessage kept = MeshCborProfile.decode(fit);
        assertThat(kept.sourceId()).isNull();
        assertThat(kept.title()).isEqualTo("é".repeat(40));
        assertThat(kept.originShortId()).containsExactly(ORIGIN);
        assertThat(kept.messageId()).containsExactly(ID);
        assertThat(kept.expiryMinutes()).isEqualTo(1440);

        // A 60-character title: 202 bytes whole, 184 without the source id, 61 without the title too.
        MeshCborMessage sixty = full("é".repeat(60), ORIGIN, ID, 1440);
        byte[] both = MeshCborProfile.encode(sixty);
        assertThat(both).hasSize(61);
        MeshCborMessage reduced = MeshCborProfile.decode(both);
        assertThat(reduced.sourceId()).isNull();
        assertThat(reduced.title()).isNull();
        assertThat(reduced.originShortId()).containsExactly(ORIGIN);
        assertThat(reduced.messageId()).containsExactly(ID);
        assertThat(reduced.expiryMinutes()).isEqualTo(1440);

        // The 084 order is unchanged for a message without extension keys: the title goes first.
        MeshCborMessage plain = full("é".repeat(40), null, null, null);
        MeshCborMessage plainReduced = MeshCborProfile.decode(MeshCborProfile.encode(plain, 140));
        assertThat(plainReduced.title()).isNull();
        assertThat(plainReduced.sourceId()).isEqualTo("usgs-earthquakes");
        MeshCborMessage extendedReduced = MeshCborProfile.decode(MeshCborProfile.encode(forty, 150));
        assertThat(extendedReduced.sourceId()).isNull();
        assertThat(extendedReduced.title()).isNotNull();

        // Keys 10-12 are never dropped to fit: a cap below the reduced size is refused.
        assertThatThrownBy(() -> MeshCborProfile.encode(sixty, 50)).isInstanceOf(IllegalArgumentException.class);
        assertThat(MeshCborProfile.encode(sixty, 500).length).isLessThanOrEqualTo(MeshCborProfile.MAX_BYTES);
    }

    @Test
    @DisplayName("titles are cut at 60 characters without splitting a surrogate pair (valid UTF-8)")
    void titleTruncation() {
        String title = "a".repeat(59) + "😀" + "zzz";
        MeshCborMessage decoded = MeshCborProfile.decode(MeshCborProfile.encode(
                new MeshCborMessage(1, MeshCborProfile.TYPE_INTEL_EVENT, null, 0, null, null, null, 1L, title, null,
                        ORIGIN, ID, 60)));
        assertThat(decoded.title()).isEqualTo("a".repeat(59));
        MeshCborMessage accents = MeshCborProfile.decode(MeshCborProfile.encode(
                new MeshCborMessage(1, MeshCborProfile.TYPE_INTEL_EVENT, null, 0, null, null, null, 1L,
                        "é".repeat(61), null, null, null, null)));
        assertThat(accents.title()).isEqualTo("é".repeat(60));
    }

    @Test
    @DisplayName("intelligence events are type 5 while open and type 2 once resolved, with the same message id")
    void intelEvents() {
        Instant now = Instant.parse("2026-10-16T12:00:00Z");
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("id", "01JA8Q3V7S2K9W4T6Y1B5N0M3C");
        summary.put("type", "conflict-cluster");
        summary.put("title", "Shelling reported near the river crossing");
        summary.put("severity", "critical");
        summary.put("startTime", "2026-10-16T11:30:00Z");
        summary.put("updatedAt", "2026-10-16T11:55:00Z");
        byte[] origin = FederationIdentity.shortId("install-1");
        MeshCborMessage open = CompactMessages.intel("created", summary, null, origin, "install-1", now);
        assertThat(open.type()).isEqualTo(MeshCborProfile.TYPE_INTEL_EVENT).isEqualTo(5);
        assertThat(open.severity()).isEqualTo(2);
        assertThat(open.kind()).isEqualTo("conflict-cluster");
        assertThat(open.epochSeconds()).isEqualTo(Instant.parse("2026-10-16T11:30:00Z").getEpochSecond());
        assertThat(open.sourceId()).isEqualTo(CompactMessages.INTEL_SOURCE);
        assertThat(open.originShortId()).containsExactly(origin).hasSize(4);
        assertThat(open.messageId()).containsExactly(RecordRef.messageId(
                CompactMessages.intelRef("install-1", "01JA8Q3V7S2K9W4T6Y1B5N0M3C")));
        assertThat(open.expiryMinutes()).isEqualTo(CompactMessages.DEFAULT_EXPIRY_MINUTES);
        byte[] encoded = MeshCborProfile.encode(open);
        assertThat(encoded.length).isLessThanOrEqualTo(MeshCborProfile.MAX_BYTES);
        assertThat(MeshCborProfile.decode(encoded)).isEqualTo(open);

        MeshCborMessage resolved = CompactMessages.intel("resolved", summary, null, origin, "install-1", now);
        assertThat(resolved.type()).isEqualTo(MeshCborProfile.TYPE_RESOLVED);
        assertThat(resolved.messageId()).containsExactly(open.messageId());
        assertThat(resolved.epochSeconds()).isEqualTo(Instant.parse("2026-10-16T11:55:00Z").getEpochSecond());
        assertThat(CompactMessages.intel("created", Map.of(), null, origin, "install-1", now)).isNull();
    }

    @Test
    @DisplayName("alerts: fired is type 1 and resolved type 2 with the short code and point; other events are not sent")
    void alerts() {
        Instant fired = Instant.parse("2026-10-16T11:00:00Z");
        AlertView alert = alert(fired, null);
        MeshCborMessage message = CompactMessages.alert(AlertEvent.of(AlertEvent.FIRED, alert), ORIGIN, "install-1");
        assertThat(message.type()).isEqualTo(MeshCborProfile.TYPE_ALERT);
        assertThat(message.shortCode()).isEqualTo(MeshShortCode.of(4711));
        assertThat(message.severity()).isEqualTo(1);
        assertThat(message.latitudeE5()).isEqualTo(3027000);
        assertThat(message.longitudeE5()).isEqualTo(-9774000);
        assertThat(message.epochSeconds()).isEqualTo(fired.getEpochSecond());
        assertThat(message.title()).isEqualTo("Quake near Austin");
        assertThat(message.messageId()).containsExactly(RecordRef.messageId(CompactMessages.alertRef("install-1",
                4711)));
        MeshCborMessage resolved = CompactMessages.alert(AlertEvent.of(AlertEvent.RESOLVED,
                alert(fired, fired.plusSeconds(600))), ORIGIN, "install-1");
        assertThat(resolved.type()).isEqualTo(MeshCborProfile.TYPE_RESOLVED);
        assertThat(resolved.epochSeconds()).isEqualTo(fired.plusSeconds(600).getEpochSecond());
        assertThat(resolved.messageId()).containsExactly(message.messageId());
        assertThat(CompactMessages.alert(AlertEvent.of(AlertEvent.ACKNOWLEDGED, alert), ORIGIN, "install-1")).isNull();
        assertThat(CompactMessages.expiresAt(message)).isEqualTo(fired.plusSeconds(1440L * 60));
        assertThat(CompactMessages.text(message, 133)).startsWith("WARN Quake near Austin #");
        assertThat(CompactMessages.text(resolved, 10)).hasSize(10).startsWith("RESOLVED");
    }

    @Test
    @DisplayName("raw tracks never become messages: no builder takes a record, a version, a live update or a sink record")
    void onlyEvents() {
        Set<Class<?>> forbidden = Set.of(ai.foresignal.source.spi.NormalizedRecord.class,
                ai.foresignal.store.query.RecordRow.class, ai.foresignal.stream.LiveUpdate.class,
                ai.foresignal.sink.SinkRecord.class);
        for (Method method : CompactMessages.class.getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            for (Class<?> parameter : method.getParameterTypes()) {
                assertThat(forbidden).as(method.getName()).doesNotContain(parameter);
            }
        }
    }

    static AlertView alert(Instant fired, Instant resolved) {
        return new AlertView(4711, UUID.fromString("00000000-0000-0000-0000-000000000001"), "Quake near Austin", 10,
                "usgs-earthquakes", "us7000n7tc", "earthquake", fired, fired, "M5.1 near Austin", "warning",
                resolved == null ? "firing" : "resolved", 1, null, null, resolved, null, null, null, null, null, null,
                false, null, List.of(), "{\"geometry\":{\"type\":\"Point\",\"coordinates\":[-97.74,30.27]}}", null,
                null, null, false, null);
    }
}
