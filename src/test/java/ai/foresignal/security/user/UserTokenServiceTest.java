package ai.foresignal.security.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.test.MutableClock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link UserTokenService} over an in-memory store (prompt R2-002): hashed, single-use, single-active, expiring. */
class UserTokenServiceTest {

    private static final Instant T0 = Instant.parse("2026-10-26T12:00:00Z");

    private final List<UserTokenEntity> rows = new ArrayList<>();
    private MutableClock clock;
    private UserRepository users;
    private UserTokenService service;
    private UserEntity user;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        clock = new MutableClock(T0);
        UserTokenRepository tokens = mock(UserTokenRepository.class);
        when(tokens.save(any())).thenAnswer(i -> {
            UserTokenEntity t = i.getArgument(0);
            rows.remove(t);
            rows.add(t);
            return t;
        });
        when(tokens.findByTokenHash(anyString())).thenAnswer(i -> rows.stream()
                .filter(t -> t.getTokenHash().equals(i.getArgument(0))).findFirst());
        when(tokens.findByUserIdAndKind(any(), anyString())).thenAnswer(i -> rows.stream()
                .filter(t -> t.getUserId().equals(i.getArgument(0))
                        && t.getKind().wireValue().equals(i.getArgument(1))).toList());
        org.mockito.Mockito.doAnswer(i -> {
            rows.removeAll((Collection<UserTokenEntity>) i.getArgument(0));
            return null;
        }).when(tokens).deleteAll(any(Iterable.class));
        user = new UserEntity(UUID.randomUUID(), "u@example.com", "U", "01JA8Q3V7S2K9W4T6Y1B5N0M3C", null, T0);
        users = mock(UserRepository.class);
        when(users.findById(user.getId())).thenReturn(Optional.of(user));
        when(users.findByEmail("u@example.com")).thenReturn(Optional.of(user));
        service = new UserTokenService(tokens, users, clock);
    }

    private static void assertInvalid(Runnable r) {
        assertThatThrownBy(r::run).isInstanceOfSatisfying(ValidationException.class,
                e -> assertThat(e.getCode()).isEqualTo(ErrorCodes.USER_TOKEN_INVALID));
    }

    @Test
    @DisplayName("an activation token has its prefix and default 72h life, and only its hash and prefix are stored")
    void issueActivation() {
        UUID admin = UUID.randomUUID();
        IssuedUserToken issued = service.issueActivation(user, admin, null);
        assertThat(issued.plaintext()).startsWith(UserTokenService.ACTIVATION_PREFIX).hasSize(4 + 43);
        UserTokenEntity row = issued.token();
        assertThat(row.getTokenHash()).isEqualTo(UserTokenService.sha256(issued.plaintext())).hasSize(64)
                .isNotEqualTo(issued.plaintext());
        assertThat(row.getTokenPrefix()).isEqualTo(issued.plaintext().substring(0, 8));
        assertThat(row.getExpiresAt()).isEqualTo(T0.plus(UserTokenService.ACTIVATION_TTL));
        assertThat(row.getCreatedBy()).isEqualTo(admin);
        assertThat(issued.toString()).doesNotContain(issued.plaintext());
    }

    @Test
    @DisplayName("a reset token has its own prefix and a default 1h life")
    void issueReset() {
        IssuedUserToken issued = service.issueReset(user, null);
        assertThat(issued.plaintext()).startsWith(UserTokenService.RESET_PREFIX);
        assertThat(issued.token().getExpiresAt()).isEqualTo(T0.plus(Duration.ofHours(1)));
        assertThatThrownBy(() -> service.issueReset(user, Duration.ZERO)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("a token consumes once; the replay fails")
    void singleUse() {
        IssuedUserToken issued = service.issueActivation(user, null, null);
        assertThat(service.consume(issued.plaintext(), UserTokenKind.ACTIVATION)).isSameAs(user);
        assertThat(issued.token().getConsumedAt()).isEqualTo(T0);
        assertInvalid(() -> service.consume(issued.plaintext(), UserTokenKind.ACTIVATION));
    }

    @Test
    @DisplayName("an expired token is refused")
    void expired() {
        IssuedUserToken issued = service.issueReset(user, null);
        clock.setInstant(T0.plus(Duration.ofHours(1)));
        assertInvalid(() -> service.consume(issued.plaintext(), UserTokenKind.RESET));
    }

    @Test
    @DisplayName("a token of the other kind, an unknown token and an empty token are refused the same way")
    void wrongKindAndUnknown() {
        IssuedUserToken issued = service.issueReset(user, null);
        assertInvalid(() -> service.consume(issued.plaintext(), UserTokenKind.ACTIVATION));
        assertInvalid(() -> service.consume("fsw_nothingLikeThis", UserTokenKind.RESET));
        assertInvalid(() -> service.consume("", UserTokenKind.RESET));
        assertInvalid(() -> service.consume(null, UserTokenKind.RESET));
        assertThat(service.consume(issued.plaintext(), UserTokenKind.RESET)).isSameAs(user);
    }

    @Test
    @DisplayName("issuing a new reset invalidates the earlier one; another kind is untouched")
    void singleActive() {
        IssuedUserToken activation = service.issueActivation(user, null, null);
        IssuedUserToken first = service.issueReset(user, null);
        IssuedUserToken second = service.issueReset(user, null);
        assertInvalid(() -> service.consume(first.plaintext(), UserTokenKind.RESET));
        assertThat(service.consume(second.plaintext(), UserTokenKind.RESET)).isSameAs(user);
        assertThat(service.consume(activation.plaintext(), UserTokenKind.ACTIVATION)).isSameAs(user);
    }

    @Test
    @DisplayName("a reset request issues a token only for an active user, and is empty otherwise")
    void requestReset() {
        assertThat(service.requestReset("nobody@example.com")).isEmpty();
        assertThat(service.requestReset("U@Example.com")).isEmpty();
        user.setStatus(UserStatus.ACTIVE, T0);
        assertThat(service.requestReset(" U@Example.com ")).get().satisfies(t -> assertThat(t.plaintext())
                .startsWith(UserTokenService.RESET_PREFIX));
        user.setStatus(UserStatus.SUSPENDED, T0);
        assertThat(service.requestReset("u@example.com")).isEmpty();
    }
}
