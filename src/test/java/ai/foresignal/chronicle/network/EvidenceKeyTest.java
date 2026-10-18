package ai.foresignal.chronicle.network;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Global evidence keys (prompt 134): each family and entity type gives the same key on any instance for the same
 * upstream content, and keyed hashing agrees exactly when the salt does.
 */
class EvidenceKeyTest {

    private static final Instant T = Instant.parse("2026-10-12T12:34:56Z");

    @Test
    @DisplayName("ev: keys come from family, originator and upstream id, and are the same on every instance")
    void eventKeys() {
        String here = EvidenceKeys.event("Seismic", "usgs", "us7000abcd");
        String there = EvidenceKeys.event("seismic ", "usgs", " us7000abcd");
        assertThat(here).isEqualTo("ev:seismic:usgs:us7000abcd").isEqualTo(there);
        assertThat(EvidenceKeys.event(null, "usgs", "x")).isNull();
        assertThat(EvidenceKeys.event("seismic", " ", "x")).isNull();
        assertThat(EvidenceKeys.event("seismic", "usgs", null)).isNull();
        assertThat(EvidenceKeys.keyType(here)).isEqualTo(NetworkVocabulary.EVIDENCE);
    }

    @Test
    @DisplayName("cell: keys use the family, the H3 resolution-5 cell and the UTC hour")
    void cellKeys() {
        String a = EvidenceKeys.cell("fire", 15.0, 44.0, T);
        String b = EvidenceKeys.cell("fire", 15.0, 44.0, Instant.parse("2026-10-12T12:00:01Z"));
        String nextHour = EvidenceKeys.cell("fire", 15.0, 44.0, Instant.parse("2026-10-12T13:00:00Z"));
        String farAway = EvidenceKeys.cell("fire", 15.0, 46.0, T);
        assertThat(a).startsWith("cell:fire:").endsWith(":2026-10-12T12").isEqualTo(b);
        assertThat(nextHour).isNotEqualTo(a);
        assertThat(farAway).isNotEqualTo(a);
        assertThat(EvidenceKeys.cell("fire", null, 44.0, T)).isNull();
        assertThat(EvidenceKeys.cell("fire", 95.0, 44.0, T)).isNull();
        assertThat(EvidenceKeys.keyType(a)).isEqualTo(NetworkVocabulary.EVIDENCE);
    }

    @Test
    @DisplayName("ent: keys exist for ICAO hex, MMSI, IMO and NORAD per UTC day, and for nothing else")
    void entityKeys() {
        assertThat(EvidenceKeys.entity("icao24", "4ca123", T)).isEqualTo("ent:icao24:4ca123:2026-10-12");
        assertThat(EvidenceKeys.entity("mmsi", "211234560", T)).isEqualTo("ent:mmsi:211234560:2026-10-12");
        assertThat(EvidenceKeys.entity("imo", "9074729", T)).isEqualTo("ent:imo:9074729:2026-10-12");
        assertThat(EvidenceKeys.entity("norad", "25544", T)).isEqualTo("ent:norad:25544:2026-10-12");
        assertThat(EvidenceKeys.entity("callsign", "air:BAW123", T)).isNull();
        assertThat(EvidenceKeys.entity("registration", "GABCD", T)).isNull();
        assertThat(EvidenceKeys.entity("icao24", "4ca123", Instant.parse("2026-10-12T23:59:59Z")))
                .isNotEqualTo(EvidenceKeys.entity("icao24", "4ca123", Instant.parse("2026-10-13T00:00:00Z")));
        assertThat(EvidenceKeys.keyType("ent:icao24:4ca123:2026-10-12")).isEqualTo(NetworkVocabulary.ENTITIES);
    }

    @Test
    @DisplayName("at: keys use the H3 resolution-4 cell and the UTC day")
    void areaTimeKeys() {
        String a = EvidenceKeys.areaTime(15.0, 44.0, T);
        assertThat(a).startsWith("at:").endsWith(":2026-10-12");
        assertThat(EvidenceKeys.areaTime(15.0, 44.0, Instant.parse("2026-10-12T01:00:00Z"))).isEqualTo(a);
        assertThat(EvidenceKeys.areaTime(15.0, 44.0, Instant.parse("2026-10-13T01:00:00Z"))).isNotEqualTo(a);
        assertThat(EvidenceKeys.areaTime(null, 44.0, T)).isNull();
        assertThat(EvidenceKeys.keyType(a)).isEqualTo(NetworkVocabulary.AREA_TIME);
    }

    @Test
    @DisplayName("topic: keys ignore keyword order, case, repeats and extra whitespace")
    void topicKeys() {
        String a = EvidenceKeys.topic(List.of("Red Sea", "Houthi", "shipping"));
        String b = EvidenceKeys.topic(List.of("shipping", "  houthi ", "RED   SEA", "shipping"));
        assertThat(a).startsWith("topic:").hasSize("topic:".length() + 32).isEqualTo(b);
        assertThat(EvidenceKeys.topic(List.of("Red Sea", "Houthi"))).isNotEqualTo(a);
        assertThat(EvidenceKeys.topic(List.of(" ", ""))).isNull();
        assertThat(EvidenceKeys.topic(null)).isNull();
        assertThat(EvidenceKeys.keyType(a)).isEqualTo(NetworkVocabulary.TOPICS);
        assertThat(EvidenceKeys.keyType("other:1")).isNull();
    }

    @Test
    @DisplayName("keyed hashes agree exactly when the salt is the same, and short salts are refused")
    void keyedHashing() {
        byte[] salt = "network-salt-0123456789".getBytes(StandardCharsets.UTF_8);
        KeyedHasher here = new KeyedHasher(salt);
        KeyedHasher there = new KeyedHasher(salt.clone());
        KeyedHasher other = new KeyedHasher("another-salt-0123456789".getBytes(StandardCharsets.UTF_8));
        String key = EvidenceKeys.event("seismic", "usgs", "us7000abcd");
        assertThat(here.hash(key)).isEqualTo(there.hash(key));
        assertThat(other.hash(key)).isNotEqualTo(here.hash(key));
        assertThat(here.hash(key)).isNotEqualTo(here.hash(EvidenceKeys.event("seismic", "usgs", "us7000abce")));
        assertThatThrownBy(() -> new KeyedHasher(new byte[8])).isInstanceOf(IllegalArgumentException.class);
    }
}
