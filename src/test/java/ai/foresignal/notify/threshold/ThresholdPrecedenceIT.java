package ai.foresignal.notify.threshold;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.user.PasswordHasher;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleRepository;
import ai.foresignal.source.group.SourceGroupEntity;
import ai.foresignal.source.group.SourceGroupRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestUsers;
import ai.foresignal.trigger.Severity;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Threshold precedence (prompt SG-003): a per-source entry overrides a covering source group's {@code GroupThreshold},
 * which overrides the default ({@code warning}).
 */
@IntegrationTest
class ThresholdPrecedenceIT {

    static final String SOURCE = "collect-poll";

    @Autowired SourceThresholdService service;
    @Autowired SourceThresholdRepository thresholds;
    @Autowired SourceGroupRepository groups;
    @Autowired PrincipalService principals;
    @Autowired UserRepository users;
    @Autowired UserRoleRepository userRoles;
    @Autowired PasswordHasher hasher;

    String principal;
    UUID owner;

    @BeforeEach
    void setUp() {
        UserEntity user = TestUsers.active(principals, users, userRoles, hasher, "precedence-user@example.com");
        principal = user.getPrincipalId();
        owner = user.getId();
    }

    private void group(String threshold) {
        SourceGroupEntity group = new SourceGroupEntity("sg-" + UUID.randomUUID(), owner);
        group.setName("Group");
        group.setSourceIds(List.of(SOURCE));
        group.setNotificationThreshold(Map.of("minSeverity", threshold));
        group.setCreatedAt(Instant.now());
        group.setUpdatedAt(Instant.now());
        groups.saveAndFlush(group);
    }

    @Test
    @DisplayName("default with nothing set, group when a covering group sets one, per-source entry when present")
    void perSourceOverridesGroupOverridesDefault() {
        // No entry, no group: the default warning.
        assertThat(service.effective(principal, SOURCE).minSeverity()).isEqualTo(Severity.WARNING);

        // A covering source group raises it to critical.
        group("critical");
        assertThat(service.effective(principal, SOURCE).minSeverity()).isEqualTo(Severity.CRITICAL);

        // A per-source entry overrides the group, lowering it to info.
        SourceThresholdEntity entry = new SourceThresholdEntity(principal, SOURCE);
        entry.setMinSeverity("info");
        entry.setMute(false);
        thresholds.saveAndFlush(entry);
        assertThat(service.effective(principal, SOURCE).minSeverity()).isEqualTo(Severity.INFO);

        // A source neither entered nor grouped stays at the default.
        assertThat(service.effective(principal, "collect-keyed").minSeverity()).isEqualTo(Severity.WARNING);
    }
}
