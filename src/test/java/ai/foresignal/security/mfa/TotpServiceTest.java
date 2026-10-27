package ai.foresignal.security.mfa;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** RFC 6238 TOTP through java-otp (prompt R2-024): the RFC's own test vectors, the window and replay protection. */
class TotpServiceTest {

    /** The RFC 6238 Appendix B SHA-1 seed. */
    private static final byte[] SEED = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);

    private final TotpService totp = new TotpService();

    @Test
    @DisplayName("a known secret and time give the RFC 6238 code (its last six digits)")
    void rfcVectors() {
        assertThat(totp.code(SEED, TotpService.step(Instant.ofEpochSecond(59)))).isEqualTo("287082");
        assertThat(totp.code(SEED, TotpService.step(Instant.ofEpochSecond(1111111109)))).isEqualTo("081804");
        assertThat(totp.code(SEED, TotpService.step(Instant.ofEpochSecond(1234567890)))).isEqualTo("005924");
        assertThat(totp.code(SEED, TotpService.step(Instant.ofEpochSecond(2000000000)))).isEqualTo("279037");
    }

    @Test
    @DisplayName("one step either side is accepted, two are not")
    void window() {
        Instant now = Instant.ofEpochSecond(1_761_480_000L);
        long step = TotpService.step(now);
        assertThat(totp.match(SEED, totp.code(SEED, step), now, null)).hasValue(step);
        assertThat(totp.match(SEED, totp.code(SEED, step - 1), now, null)).hasValue(step - 1);
        assertThat(totp.match(SEED, totp.code(SEED, step + 1), now, null)).hasValue(step + 1);
        assertThat(totp.match(SEED, totp.code(SEED, step - 2), now, null)).isEmpty();
        assertThat(totp.match(SEED, totp.code(SEED, step + 2), now, null)).isEmpty();
    }

    @Test
    @DisplayName("a code from a step already accepted, or an earlier one, is rejected (replay)")
    void replay() {
        Instant now = Instant.ofEpochSecond(1_761_480_000L);
        long step = TotpService.step(now);
        String code = totp.code(SEED, step);
        assertThat(totp.match(SEED, code, now, step)).isEmpty();
        assertThat(totp.match(SEED, totp.code(SEED, step - 1), now, step)).isEmpty();
        assertThat(totp.match(SEED, totp.code(SEED, step + 1), now, step)).hasValue(step + 1);
    }

    @Test
    @DisplayName("malformed codes never match; spaces are ignored")
    void malformed() {
        Instant now = Instant.ofEpochSecond(59);
        assertThat(totp.match(SEED, null, now, null)).isEmpty();
        assertThat(totp.match(SEED, "28708", now, null)).isEmpty();
        assertThat(totp.match(SEED, "28708x", now, null)).isEmpty();
        assertThat(totp.match(SEED, "287 082", now, null)).hasValue(1L);
    }

    @Test
    @DisplayName("secrets are 160 random bits and round-trip through unpadded Base32")
    void secrets() {
        byte[] secret = totp.newSecret();
        assertThat(secret).hasSize(TotpService.SECRET_BYTES);
        String text = TotpService.base32(secret);
        assertThat(text).hasSize(32).matches("[A-Z2-7]+");
        assertThat(TotpService.fromBase32(text.toLowerCase())).isEqualTo(secret);
        assertThat(totp.newSecret()).isNotEqualTo(secret);
    }

    @Test
    @DisplayName("the provisioning URI names the issuer and the account")
    void otpauthUri() {
        assertThat(MfaService.otpauthUri("ed+mfa@example.com", "JBSWY3DPEHPK3PXP")).isEqualTo(
                "otpauth://totp/ForeSignal:ed%2Bmfa%40example.com?secret=JBSWY3DPEHPK3PXP&issuer=ForeSignal");
        assertThat(MfaService.normalizeRecoveryCode(" AbCdE-fGhIj ")).isEqualTo("abcdefghij");
    }
}
