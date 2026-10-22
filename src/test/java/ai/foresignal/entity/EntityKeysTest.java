package ai.foresignal.entity;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Every normalization and rejection rule of {@link EntityKeys} (prompt 098). */
class EntityKeysTest {

    @Test
    void icao24IsLowercaseHexAndRejectsSentinelsAndTisB() {
        assertThat(EntityKeys.normalize("icao24", "AE1234", null)).isEqualTo("ae1234");
        assertThat(EntityKeys.normalize("icao24", "000000", null)).isNull();
        assertThat(EntityKeys.normalize("icao24", "FFFFFF", null)).isNull();
        assertThat(EntityKeys.normalize("icao24", "~abc123", null)).isNull();
        assertThat(EntityKeys.normalize("icao24", "zz1234", null)).isNull();
    }

    @Test
    void registrationDropsSpacesAndHyphensAndKeepsAHyphenatedAlias() {
        assertThat(EntityKeys.normalize("registration", " n-123 ab ", null)).isEqualTo("N123AB");
        assertThat(EntityKeys.registrationAlias("N-123AB")).isEqualTo("N-123AB");
        assertThat(EntityKeys.registrationAlias("N123AB")).isNull();
    }

    @Test
    void imoNeedsAValidCheckDigit() {
        assertThat(EntityKeys.normalize("imo", "9074729", null)).isEqualTo("9074729");
        assertThat(EntityKeys.normalize("imo", "IMO 9074729", null)).isEqualTo("9074729");
        assertThat(EntityKeys.normalize("imo", "9074728", null)).isNull();
        assertThat(EntityKeys.normalize("imo", "123", null)).isNull();
    }

    @Test
    void mmsiOnlyKeepsShipStations() {
        assertThat(EntityKeys.normalize("mmsi", "366123456", null)).isEqualTo("366123456");
        assertThat(EntityKeys.normalize("mmsi", "123456789", null)).isNull();
        assertThat(EntityKeys.normalize("mmsi", "111111111", null)).isNull();
        assertThat(EntityKeys.normalize("mmsi", "000000000", null)).isNull();
        assertThat(EntityKeys.normalize("mmsi", "990000001", null)).isNull();
        assertThat(EntityKeys.normalize("mmsi", "000000001", null)).isNull();
        assertThat(EntityKeys.normalize("mmsi", "111000001", null)).isNull();
        assertThat(EntityKeys.isShipMmsi("366123456")).isTrue();
        assertThat(EntityKeys.isShipMmsi("111000001")).isFalse();
    }

    @Test
    void noradIsAPositiveCatalogNumber() {
        assertThat(EntityKeys.normalize("norad", "25544", null)).isEqualTo("25544");
        assertThat(EntityKeys.normalize("norad", "0", null)).isNull();
        assertThat(EntityKeys.normalize("norad", "-1", null)).isNull();
    }

    @Test
    void cosparUsesTheYearDesignatorForm() {
        assertThat(EntityKeys.normalize("cospar", "1998-067a", null)).isEqualTo("1998-067A");
        assertThat(EntityKeys.normalize("cospar", "1998-67A", null)).isNull();
    }

    @Test
    void airframeCombinesManufacturerCodeAndSerial() {
        assertThat(EntityKeys.normalize("airframe", "s22:1234", null)).isEqualTo("S22:1234");
    }

    @Test
    void callsignsCarryTheirDomainAndConfidence() {
        assertThat(EntityKeys.normalize("callsign", "ual123", "air")).isEqualTo("air:UAL123");
        assertThat(EntityKeys.normalize("callsign", "w1aw", "ham")).isEqualTo("ham:W1AW");
        assertThat(EntityKeys.normalize("callsign", "sea:abcd", "sea")).isEqualTo("sea:ABCD");
        assertThat(EntityKeys.confidence("callsign", "air:UAL123")).isEqualTo(0.5f);
        assertThat(EntityKeys.confidence("callsign", "sea:ABCD")).isEqualTo(0.7f);
        assertThat(EntityKeys.confidence("callsign", "ham:W1AW")).isEqualTo(0.9f);
    }

    @Test
    void hamCallsignsAreTheBaseCallWithoutTheSsid() {
        assertThat(EntityKeys.normalize("callsign", "k1abc-9", "ham")).isEqualTo("ham:K1ABC");
        assertThat(EntityKeys.normalize("callsign", "HAM:W1AW-15", "ham")).isEqualTo("ham:W1AW");
        // Only the ham domain strips a suffix; a flight id keeps its characters.
        assertThat(EntityKeys.normalize("callsign", "AIR:N12-3", "air")).isEqualTo("air:N12-3");
    }

    @Test
    void extractEmitsAnAirframeOnlyWithBothTheModelCodeAndTheSerial() {
        SourceDescriptor descriptor = descriptor().entityKey("airframe", "serial").build();
        assertThat(EntityKeys.extract(descriptor, Map.of("serial", "1234"))).isEmpty();
        assertThat(EntityKeys.extract(descriptor, Map.of("serial", "1234", "mfrMdlCode", " ")))
                .isEmpty();
        assertThat(EntityKeys.extract(descriptor, Map.of("serial", " 1234 ", "mfrMdlCode", "s22")))
                .extracting(Key::keyValue).containsExactly("S22:1234");
    }

    @Test
    void extractReportsRejectedValuesButNotAbsentOnes() {
        SourceDescriptor descriptor = descriptor().entityKey("icao24", "hex").entityKey("mmsi", "mmsi").build();
        List<String> invalid = new ArrayList<>();
        List<Key> keys = EntityKeys.extract(descriptor, Map.of("hex", "~abc123", "other", "x"), invalid::add);
        assertThat(keys).isEmpty();
        assertThat(invalid).containsExactly("icao24");
    }

    @Test
    void rolesAndTypes() {
        assertThat(EntityKeys.roleOf("airframe")).isEqualTo(EntityKeyRole.ANCHOR);
        assertThat(EntityKeys.roleOf("mmsi")).isEqualTo(EntityKeyRole.WEAK);
        assertThat(EntityKeys.typeOf("imo")).isEqualTo(EntityType.VESSEL);
        assertThat(EntityKeys.typeOf("cospar")).isEqualTo(EntityType.SATELLITE);
        assertThat(EntityKeys.typeOf("callsign")).isNull();
        assertThat(EntityKeys.known("airframe")).isTrue();
        assertThat(EntityKeys.known("aircraft-tail")).isFalse();
    }

    @Test
    void leiIsTwentyUpperAlphanumeric() {
        assertThat(EntityKeys.normalize("lei", " 5493001kjtiigc8y1r12 ", null)).isEqualTo("5493001KJTIIGC8Y1R12");
        assertThat(EntityKeys.normalize("lei", "5493001KJTIIGC8Y1R1", null)).isNull(); // 19 chars
        assertThat(EntityKeys.normalize("lei", "5493001KJTIIGC8Y1R12X", null)).isNull(); // 21 chars
        assertThat(EntityKeys.normalize("lei", "5493001-JTIIGC8Y1R12", null)).isNull(); // hyphen
    }

    @Test
    void companyNumberNeedsAJurisdictionAndNumber() {
        assertThat(EntityKeys.normalize("company-number", " gb:012 345 67 ", null)).isEqualTo("GB:01234567");
        assertThat(EntityKeys.normalize("company-number", "us-de:5203989", null)).isEqualTo("US-DE:5203989");
        assertThat(EntityKeys.normalize("company-number", "01234567", null)).isNull(); // no jurisdiction
        assertThat(EntityKeys.normalize("company-number", "gb:", null)).isNull();
    }

    @Test
    void taxIdStripsSpacesAndHyphens() {
        assertThat(EntityKeys.normalize("tax-id", " 12-3456789 ", null)).isEqualTo("123456789");
        assertThat(EntityKeys.normalize("tax-id", "GB 123 4567 89", null)).isEqualTo("GB123456789");
        assertThat(EntityKeys.normalize("tax-id", "12", null)).isNull(); // too short
    }

    @Test
    void officerIdNeedsARegistryAndId() {
        assertThat(EntityKeys.normalize("officer-id", "gb:p7v9abcd", null)).isEqualTo("GB:P7V9ABCD");
        assertThat(EntityKeys.normalize("officer-id", "p7v9abcd", null)).isNull(); // no registry
    }

    @Test
    void personKeyIsLowerAndWhitespaceCollapsed() {
        assertThat(EntityKeys.normalize("person-key", "  Jane   Doe|1980-01-02|GB  ", null))
                .isEqualTo("jane doe|1980-01-02|gb");
        assertThat(EntityKeys.normalize("person-key", "   ", null)).isNull();
    }

    @Test
    void corporateAndPersonRolesTypesAndConfidence() {
        // Corporate keys are organization anchors; a person key is a weak person key.
        assertThat(EntityKeys.roleOf("lei")).isEqualTo(EntityKeyRole.ANCHOR);
        assertThat(EntityKeys.roleOf("company-number")).isEqualTo(EntityKeyRole.ANCHOR);
        assertThat(EntityKeys.roleOf("tax-id")).isEqualTo(EntityKeyRole.ANCHOR);
        assertThat(EntityKeys.roleOf("officer-id")).isEqualTo(EntityKeyRole.ANCHOR);
        assertThat(EntityKeys.roleOf("person-key")).isEqualTo(EntityKeyRole.WEAK);

        assertThat(EntityKeys.typeOf("lei")).isEqualTo(EntityType.ORGANIZATION);
        assertThat(EntityKeys.typeOf("company-number")).isEqualTo(EntityType.ORGANIZATION);
        assertThat(EntityKeys.typeOf("tax-id")).isEqualTo(EntityType.ORGANIZATION);
        assertThat(EntityKeys.typeOf("officer-id")).isEqualTo(EntityType.PERSON);
        assertThat(EntityKeys.typeOf("person-key")).isEqualTo(EntityType.PERSON);

        assertThat(EntityKeys.confidence("lei", "5493001KJTIIGC8Y1R12")).isEqualTo(1.0f);
        assertThat(EntityKeys.confidence("officer-id", "GB:P7V9ABCD")).isEqualTo(0.9f);
        assertThat(EntityKeys.confidence("person-key", "jane doe|1980-01-02|gb")).isEqualTo(0.5f);

        assertThat(EntityKeys.known("lei")).isTrue();
        assertThat(EntityKeys.known("person-key")).isTrue();
    }

    private static SourceDescriptor.Builder descriptor() {
        return SourceDescriptor.builder("entity-keys-test")
                .name("Entity keys test")
                .kind("aircraft")
                .mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 10_000));
    }
}
