package ai.foresignal.security.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link UserSessionEntity}: liveness, sliding idle expiry capped at the absolute one, revocation (R2-003). */
class UserSessionEntityTest {

    private static final Instant T0 = Instant.parse("2026-10-26T12:00:00Z");

    private static UserSessionEntity session() {
        return new UserSessionEntity(UUID.randomUUID(), UUID.randomUUID(), "a".repeat(64), "fss_abcd", T0,
                T0.plus(SessionService.ABSOLUTE_LIFETIME), T0.plus(SessionService.IDLE_TIMEOUT), "x".repeat(600),
                "192.0.2.1");
    }

    @Test
    @DisplayName("live until idle expiry; touching slides it; never past the absolute expiry")
    void idleAndAbsolute() {
        UserSessionEntity s = session();
        assertThat(s.isLive(T0)).isTrue();
        assertThat(s.isLive(T0.plus(Duration.ofHours(2)))).isFalse();
        s.touch(T0.plus(Duration.ofHours(1)), T0.plus(Duration.ofHours(3)));
        assertThat(s.isLive(T0.plus(Duration.ofHours(2)))).isTrue();
        assertThat(s.getLastSeenAt()).isEqualTo(T0.plus(Duration.ofHours(1)));
        s.touch(T0.plus(Duration.ofHours(11)), T0.plus(Duration.ofHours(13)));
        assertThat(s.getIdleExpiresAt()).isEqualTo(s.getExpiresAt());
        assertThat(s.isLive(T0.plus(Duration.ofHours(12)))).isFalse();
    }

    @Test
    @DisplayName("revocation ends the session and keeps the first revocation time; user agents are cut")
    void revoke() {
        UserSessionEntity s = session();
        s.revoke(T0.plusSeconds(5));
        s.revoke(T0.plusSeconds(9));
        assertThat(s.getRevokedAt()).isEqualTo(T0.plusSeconds(5));
        assertThat(s.isLive(T0.plusSeconds(6))).isFalse();
        assertThat(s.getUserAgent()).hasSize(UserSessionEntity.USER_AGENT_LENGTH);
        assertThat(SessionService.view(s, s.getId())).containsEntry("current", true).containsEntry("tokenPrefix",
                "fss_abcd").doesNotContainKey("tokenHash");
        assertThat(SessionService.view(s, UUID.randomUUID())).containsEntry("current", false);
    }
}
