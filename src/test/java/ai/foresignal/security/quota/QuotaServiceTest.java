package ai.foresignal.security.quota;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/** {@link QuotaService} and {@link Quota} (prompt R2-026): the merge over the instance default and the checks. */
class QuotaServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-26T12:00:00Z");

    private final UserQuotaRepository quotas = mock(UserQuotaRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final AppSettingService settings = mock(AppSettingService.class);
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final JsonMapper json = JsonMapper.builder().build();
    private QuotaService service;
    private UUID userId;

    @BeforeEach
    void setUp() {
        service = new QuotaService(quotas, users, settings, jdbc, Clock.fixed(NOW, ZoneOffset.UTC));
        userId = UUID.randomUUID();
        UserEntity user = new UserEntity(userId, "q@example.com", "Q", "01PRINCIPAL", null, NOW);
        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(users.findByPrincipalId("01PRINCIPAL")).thenReturn(Optional.of(user));
        when(settings.get(SettingKeys.QUOTA_DEFAULT, Quota.class)).thenReturn(Optional.empty());
        when(quotas.findById(userId)).thenReturn(Optional.empty());
    }

    private void own(Quota quota) {
        when(quotas.findById(userId)).thenReturn(Optional.of(new UserQuotaEntity(userId, quota, NOW)));
        service.resetState();
    }

    private void instanceDefault(Quota quota) {
        when(settings.get(SettingKeys.QUOTA_DEFAULT, Quota.class)).thenReturn(Optional.of(quota));
    }

    @Test
    @DisplayName("a user's own limits merge over the instance default; a null member falls through to it")
    void mergesOverDefault() {
        instanceDefault(new Quota(600, 1_000_000L, 10, 5, 20));
        own(new Quota(60, null, 3, null, null));

        assertThat(service.effective(userId)).isEqualTo(new Quota(60, 1_000_000L, 3, 5, 20));
    }

    @Test
    @DisplayName("unset in both the user's quota and the default, a limit is unlimited")
    void unsetIsUnlimited() {
        assertThat(service.effective(userId)).isEqualTo(Quota.UNSET);
        assertThat(service.effective(userId).limitOf(QuotaLimit.ALERTS)).isNull();
    }

    @Test
    @DisplayName("a user with no row follows the default entirely")
    void noRowFollowsDefault() {
        instanceDefault(new Quota(null, null, 2, null, null));

        assertThat(service.effective(userId).maxAlerts()).isEqualTo(2);
    }

    @Test
    @DisplayName("a create at the limit is 409 QUOTA_EXCEEDED naming the limit; under it passes")
    void countCheck() {
        own(new Quota(null, null, 2, null, null));
        when(jdbc.queryForObject(eq("SELECT count(*) FROM trigger_rule WHERE owner_principal_id = ?"), eq(Long.class),
                any(Object[].class))).thenReturn(1L, 2L);

        service.checkCount("01PRINCIPAL", QuotaLimit.ALERTS);
        assertThatThrownBy(() -> service.checkCount("01PRINCIPAL", QuotaLimit.ALERTS))
                .isInstanceOfSatisfying(ForesignalException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("QUOTA_EXCEEDED");
                    assertThat(e.getStatus().value()).isEqualTo(409);
                    assertThat(e.getProperties()).containsEntry("limit", "maxAlerts").containsEntry("quota", 2L)
                            .containsEntry("usage", 2L);
                });
    }

    @Test
    @DisplayName("a principal that is no user, or none, is never limited")
    void nonUserNotLimited() {
        instanceDefault(new Quota(null, 0L, 0, 0, 0));
        when(users.findByPrincipalId(anyString())).thenReturn(Optional.empty());

        service.checkCount("token:abc", QuotaLimit.CHANNELS);
        service.checkCount(null, QuotaLimit.SAVED_VIEWS);
        service.checkStorage(null, 10);
    }

    @Test
    @DisplayName("storage counts the bytes a write adds; a write that frees space is never refused")
    void storageCheck() {
        own(new Quota(null, 100L, null, null, null));
        when(jdbc.queryForObject(anyString(), eq(Long.class), any(Object[].class))).thenReturn(90L);

        service.checkStorage("01PRINCIPAL", 10);
        service.checkStorage("01PRINCIPAL", -50);
        assertThatThrownBy(() -> service.checkStorage("01PRINCIPAL", 11))
                .isInstanceOfSatisfying(ForesignalException.class,
                        e -> assertThat(e.getProperties()).containsEntry("limit", "maxStorageBytes"));
    }

    @Test
    @DisplayName("parse: null and absent are unset; negatives, fractions and unknown members are refused")
    void parse() {
        assertThat(Quota.parse(json.readTree("{\"maxAlerts\":3,\"maxChannels\":null}")))
                .isEqualTo(new Quota(null, null, 3, null, null));
        assertThat(Quota.parse(json.readTree("{\"maxStorageBytes\":5000000000}")).maxStorageBytes())
                .isEqualTo(5_000_000_000L);
        assertThatThrownBy(() -> Quota.parse(json.readTree("{\"maxAlerts\":-1}")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> Quota.parse(json.readTree("{\"maxAlerts\":1.5}")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> Quota.parse(json.readTree("{\"maxAlert\":1}")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> Quota.parse(json.readTree("{\"maxAlerts\":3000000000}")))
                .isInstanceOf(ValidationException.class);
    }
}
