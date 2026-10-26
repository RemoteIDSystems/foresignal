package ai.foresignal.collect;

import static ai.foresignal.testsupport.records.RecordsTestSources.RASTER;
import static ai.foresignal.testsupport.records.RecordsTestSources.SCHEMA_A;
import static ai.foresignal.testsupport.records.RecordsTestSources.SCHEMA_B;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.user.BuiltInRole;
import ai.foresignal.security.user.PasswordHasher;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleRepository;
import ai.foresignal.security.user.UserStatus;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.view.UserSourcePrefEntity;
import ai.foresignal.source.view.UserSourcePrefRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.records.RecordsTestSources;
import ai.foresignal.testsupport.security.TestUsers;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The collection union (prompt R2-010) through the exact query the collector schedules from
 * ({@link CollectorScheduler#DESIRED_SQL}): instance-enabled and wanted by some active user's view, or every
 * instance-enabled source while there is no active user, and each source once.
 */
@IntegrationTest
@Import(RecordsTestSources.class)
class CollectionUnionIT {

    static final Instant T = Instant.parse("2026-10-26T12:00:00Z");

    @Autowired JdbcTemplate jdbc;
    @Autowired SourceService sources;
    @Autowired UserSourcePrefRepository prefs;
    @Autowired PrincipalService principals;
    @Autowired UserRepository users;
    @Autowired UserRoleRepository userRoles;
    @Autowired PasswordHasher hasher;

    @BeforeEach
    void setUp() {
        sources.update(SCHEMA_A, SourceSettingsUpdate.enabled(true));
        sources.update(SCHEMA_B, SourceSettingsUpdate.enabled(true));
        sources.update(RASTER, SourceSettingsUpdate.enabled(false));
    }

    private List<String> collected() {
        return jdbc.query(CollectorScheduler.DESIRED_SQL, (rs, row) -> rs.getString(1)).stream()
                .filter(id -> id.equals(SCHEMA_A) || id.equals(SCHEMA_B) || id.equals(RASTER))
                .sorted()
                .toList();
    }

    private UserEntity user(String email) {
        return TestUsers.active(principals, users, userRoles, hasher, email, BuiltInRole.EDITOR);
    }

    private void pref(UserEntity user, String source, boolean enabled) {
        prefs.saveAndFlush(new UserSourcePrefEntity(user.getId(), source, enabled, null, null, T));
    }

    @Test
    @DisplayName("with no user at all, the instance set is collected as before v2.0")
    void noUsers() {
        assertThat(collected()).containsExactly(SCHEMA_A, SCHEMA_B);
    }

    @Test
    @DisplayName("a view is opt-out: users who never touched it want every instance-enabled source")
    void untouchedViews() {
        user("alice@example.com");
        user("bob@example.com");
        assertThat(collected()).containsExactly(SCHEMA_A, SCHEMA_B);
    }

    @Test
    @DisplayName("a source wanted by any user is pulled; one every active user turned off is not")
    void union() {
        UserEntity alice = user("alice@example.com");
        UserEntity bob = user("bob@example.com");
        pref(alice, SCHEMA_A, false);
        assertThat(collected()).as("bob still wants A").containsExactly(SCHEMA_A, SCHEMA_B);
        pref(bob, SCHEMA_A, false);
        assertThat(collected()).as("nobody wants A").containsExactly(SCHEMA_B);
    }

    @Test
    @DisplayName("a source outside the instance set is never pulled, whatever the views say")
    void instanceSetBounds() {
        UserEntity alice = user("alice@example.com");
        pref(alice, RASTER, true);
        assertThat(collected()).doesNotContain(RASTER);
    }

    @Test
    @DisplayName("a source wanted by many users is one row: scheduled once, pulled once")
    void singlePull() {
        UserEntity alice = user("alice@example.com");
        UserEntity bob = user("bob@example.com");
        pref(alice, SCHEMA_B, true);
        pref(bob, SCHEMA_B, true);
        assertThat(collected()).filteredOn(SCHEMA_B::equals).hasSize(1);
    }

    @Test
    @DisplayName("only active users count: a suspended user's untouched view keeps nothing collected")
    void activeUsersOnly() {
        UserEntity alice = user("alice@example.com");
        UserEntity carol = user("carol@example.com");
        carol.setStatus(UserStatus.SUSPENDED, T);
        users.saveAndFlush(carol);
        pref(alice, SCHEMA_A, false);
        assertThat(collected()).containsExactly(SCHEMA_B);
    }
}
