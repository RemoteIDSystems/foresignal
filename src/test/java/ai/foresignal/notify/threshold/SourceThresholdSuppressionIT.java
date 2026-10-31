package ai.foresignal.notify.threshold;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.user.PasswordHasher;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.security.TestUsers;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * The per-source notification decision (prompt SG-003): an alert below the minimum severity, from a muted source, or
 * within cooldown does not notify; one at or above severity and outside cooldown notifies and stamps the last-notified
 * time. (The decision is what the router applies before routing; the alert itself is persisted either way.)
 */
@IntegrationTest
@Import(CollectorTestSources.class)
class SourceThresholdSuppressionIT {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant T = Instant.parse("2026-10-13T12:00:00Z");

    @Autowired SourceThresholdService service;
    @Autowired PrincipalService principals;
    @Autowired UserRepository users;
    @Autowired UserRoleRepository userRoles;
    @Autowired PasswordHasher hasher;

    String principal;

    @BeforeEach
    void setUp() {
        UserEntity user = TestUsers.active(principals, users, userRoles, hasher, "suppress-user@example.com");
        principal = user.getPrincipalId();
    }

    @Test
    @DisplayName("below the minimum severity and within cooldown are suppressed; at/above and outside notify")
    void severityAndCooldown() {
        service.replace(principal, JSON.readTree("{\"entries\":[{\"sourceId\":\"" + CollectorTestSources.POLL
                + "\",\"minSeverity\":\"critical\",\"cooldown\":\"PT1H\"}]}"));

        assertThat(service.shouldNotify(principal, CollectorTestSources.POLL, "warning", T)).isFalse();
        assertThat(service.shouldNotify(principal, CollectorTestSources.POLL, "critical", T)).isTrue();

        service.recordNotified(principal, CollectorTestSources.POLL, T);
        assertThat(service.shouldNotify(principal, CollectorTestSources.POLL, "critical", T.plus(Duration.ofMinutes(30))))
                .as("within cooldown").isFalse();
        assertThat(service.shouldNotify(principal, CollectorTestSources.POLL, "critical", T.plus(Duration.ofHours(2))))
                .as("outside cooldown").isTrue();
    }

    @Test
    @DisplayName("a muted source never notifies; a source with no entry uses the default (warning)")
    void muteAndDefault() {
        service.replace(principal, JSON.readTree("{\"entries\":[{\"sourceId\":\"" + CollectorTestSources.POLL
                + "\",\"mute\":true}]}"));
        assertThat(service.shouldNotify(principal, CollectorTestSources.POLL, "critical", T)).isFalse();

        // collect-keyed has no entry, so the default warning applies.
        assertThat(service.shouldNotify(principal, CollectorTestSources.KEYED, "info", T)).isFalse();
        assertThat(service.shouldNotify(principal, CollectorTestSources.KEYED, "warning", T)).isTrue();
    }
}
