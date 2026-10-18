package ai.foresignal.object;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** {@link ObjectRef}: the Client-Contract §2 grammar (prompt 129). */
class ObjectRefTest {

    @Test
    @DisplayName("every object type round-trips through parse and format")
    void everyTypeRoundTrips() {
        for (String type : ObjectTypes.ALL) {
            String id = switch (type) {
                case ObjectTypes.RECORD -> "adsblol-mil/ae1234";
                case ObjectTypes.SENSOR -> "satellite:25544";
                case ObjectTypes.REMOTE -> "fs-eu-1/report:01JA8Q3V7S2K9W4T6Y1B5N0M3C";
                default -> "01JA8Q3V7S2K9W4T6Y1B5N0M3C";
            };
            ObjectRef ref = ObjectRef.parse(type + ":" + id);
            assertThat(ref.type()).isEqualTo(type);
            assertThat(ref.id()).isEqualTo(id);
            assertThat(ref.format()).isEqualTo(type + ":" + id);
            assertThat(ObjectRef.parse(ref.format())).isEqualTo(ref);
        }
    }

    @Test
    @DisplayName("record refs carry source, decoded external id and an optional seq")
    void recordRefs() {
        ObjectRef current = ObjectRef.parse("record:adsblol-mil/ae1234");
        assertThat(current.recordSourceId()).isEqualTo("adsblol-mil");
        assertThat(current.recordExternalId()).isEqualTo("ae1234");
        assertThat(current.recordSeq()).isNull();
        assertThat(current.withoutSeq()).isSameAs(current);

        ObjectRef pinned = ObjectRef.parse("record:adsblol-mil/ae1234@88120391");
        assertThat(pinned.recordSeq()).isEqualTo(88120391L);
        assertThat(pinned.recordExternalId()).isEqualTo("ae1234");
        assertThat(pinned.withoutSeq()).isEqualTo(current);
    }

    @Test
    @DisplayName("external ids are percent-encoded: slash, at sign and percent survive a round trip")
    void encodedExternalIds() {
        ObjectRef ref = ObjectRef.record("nhc-cyclone-cones", "al092026:track/a@b%c", 7L);
        assertThat(ref.format()).isEqualTo("record:nhc-cyclone-cones/al092026:track%2Fa%40b%25c@7");
        ObjectRef parsed = ObjectRef.parse(ref.format());
        assertThat(parsed.recordExternalId()).isEqualTo("al092026:track/a@b%c");
        assertThat(parsed.recordSeq()).isEqualTo(7L);
        assertThat(ObjectRef.decode(ObjectRef.encode("ünïcode id"))).isEqualTo("ünïcode id");
    }

    @Test
    @DisplayName("sensor refs expose their kind and id")
    void sensorRefs() {
        ObjectRef ref = ObjectRef.parse("sensor:station:satdump-home");
        assertThat(ref.sensorKind()).isEqualTo("station");
        assertThat(ref.sensorId()).isEqualTo("satdump-home");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "situation", "situation:", ":01J", "unknown:01J", "Situation:01J", "record:src",
        "record:/ext", "record:src/", "record:src/a@b@1", "record:src/a@x", "record:src/a%zz", "sensor:robot:1",
        "sensor:satellite", "situation:01J A", "chronicle:01J"})
    @DisplayName("malformed refs and unknown types are OBJECT_REF_INVALID")
    void invalidRefsRejected(String text) {
        assertThatThrownBy(() -> ObjectRef.parse(text)).isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getCode())
                        .isEqualTo(ErrorCodes.OBJECT_REF_INVALID));
        assertThat(ObjectRef.isValid(text)).isFalse();
    }

    @Test
    @DisplayName("an over-long reference is rejected")
    void tooLong() {
        assertThat(ObjectRef.isValid("situation:" + "A".repeat(ObjectRef.MAX_LENGTH))).isFalse();
    }
}
