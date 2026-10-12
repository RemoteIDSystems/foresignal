package ai.foresignal.api.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CursorCodecTest {

    private static final String F = CursorCodec.fingerprint("records|a");
    private static final Instant T = Instant.parse("2026-03-01T12:00:00.123456Z");

    @Test
    void roundTripsEveryVariant() {
        assertRoundTrip(new Cursor.Observed(T, "src", "id-1", null), Cursor.Observed.class);
        assertRoundTrip(new Cursor.Observed(T, "src", "id-1", 42L), Cursor.Observed.class);
        assertRoundTrip(new Cursor.ById("src", "id \"quoted\"", 7L), Cursor.ById.class);
        assertRoundTrip(new Cursor.Distance(1234.5678, "src", "id", null), Cursor.Distance.class);
        assertRoundTrip(new Cursor.Seq(99L, null), Cursor.Seq.class);
        assertRoundTrip(new Cursor.History(T, 5L, null), Cursor.History.class);
        assertRoundTrip(new Cursor.History(null, 5L, null), Cursor.History.class);
    }

    @Test
    @DisplayName("microsecond observed times survive the round trip")
    void keepsMicroseconds() {
        String token = CursorCodec.encode(new Cursor.Observed(T, "s", "e", null), F);
        assertThat(CursorCodec.decode(token, Cursor.Observed.class, F).o()).isEqualTo(T);
    }

    @Test
    void isOpaqueAndVersioned() {
        String token = CursorCodec.encode(new Cursor.ById("s", "e", null), F);
        assertThat(token).startsWith(CursorCodec.PREFIX).doesNotContain("\"s\"").doesNotContain("=");
    }

    @Test
    void absentIsNull() {
        assertThat(CursorCodec.decode(null, Cursor.ById.class, F)).isNull();
        assertThat(CursorCodec.decode(" ", Cursor.ById.class, F)).isNull();
    }

    @Test
    @DisplayName("a cursor of another ordering is INVALID_CURSOR")
    void rejectsOtherVariant() {
        String token = CursorCodec.encode(new Cursor.ById("s", "e", null), F);
        assertInvalid(() -> CursorCodec.decode(token, Cursor.Observed.class, F));
    }

    @Test
    @DisplayName("a cursor issued for another query is INVALID_CURSOR")
    void rejectsOtherQuery() {
        String token = CursorCodec.encode(new Cursor.ById("s", "e", null), F);
        assertInvalid(() -> CursorCodec.decode(token, Cursor.ById.class, CursorCodec.fingerprint("records|b")));
    }

    @Test
    @DisplayName("tampering is detected")
    void rejectsTampering() {
        String token = CursorCodec.encode(new Cursor.Seq(10, null), F);
        String body = token.substring(CursorCodec.PREFIX.length(), token.lastIndexOf('.'));
        String json = new String(Base64.getUrlDecoder().decode(body), StandardCharsets.UTF_8).replace("10", "11");
        String forged = CursorCodec.PREFIX
                + Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8))
                + token.substring(token.lastIndexOf('.'));
        assertInvalid(() -> CursorCodec.decode(forged, Cursor.Seq.class, F));
    }

    @Test
    void rejectsGarbage() {
        assertInvalid(() -> CursorCodec.decode("hello", Cursor.Seq.class, F));
        assertInvalid(() -> CursorCodec.decode("c1.", Cursor.Seq.class, F));
        assertInvalid(() -> CursorCodec.decode("c1.!!!.abc", Cursor.Seq.class, F));
        assertInvalid(() -> CursorCodec.decode("c1.e30.0000000000000000", Cursor.Seq.class, F));
        String truncated = CursorCodec.encode(new Cursor.Seq(1, null), F);
        assertInvalid(() -> CursorCodec.decode(truncated.substring(0, truncated.length() - 2), Cursor.Seq.class, F));
    }

    @Test
    void fingerprintIsStableAndShort() {
        assertThat(CursorCodec.fingerprint("x")).hasSize(12).isEqualTo(CursorCodec.fingerprint("x"));
        assertThat(CursorCodec.fingerprint("x")).isNotEqualTo(CursorCodec.fingerprint("y"));
    }

    private static <T extends Cursor> void assertRoundTrip(T cursor, Class<T> type) {
        String token = CursorCodec.encode(cursor, F);
        assertThat(CursorCodec.decode(token, type, F)).isEqualTo(cursor);
    }

    private static void assertInvalid(org.assertj.core.api.ThrowableAssert.ThrowingCallable call) {
        assertThatThrownBy(call)
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getCode()).isEqualTo(ErrorCodes.INVALID_CURSOR));
    }
}
