package ai.foresignal.security.user;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalKind;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.TokenHasher;
import ai.foresignal.test.IntegrationTest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The identity entities against the application's own migrated database (prompt R2-001): they validate under
 * {@code ddl-auto: validate} (the context starts), round-trip through their repositories, and the seeded catalog is
 * readable through JPA.
 */
@IntegrationTest
class IdentityModelIT {

    private static final Instant T0 = Instant.parse("2026-10-26T12:00:00Z");

    @Autowired private UserRepository users;
    @Autowired private RoleRepository roles;
    @Autowired private PermissionRepository permissions;
    @Autowired private UserRoleRepository userRoles;
    @Autowired private RolePermissionRepository rolePermissions;
    @Autowired private UserTokenRepository userTokens;
    @Autowired private PrincipalService principals;
    @Autowired private TokenHasher hasher;
    @Autowired private JdbcTemplate jdbc;

    private UserEntity newUser(String email) {
        PrincipalEntity principal = principals.createFor(email, PrincipalKind.PERSON);
        return users.saveAndFlush(new UserEntity(UUID.randomUUID(), email, "Ada Lovelace", principal.getId(), null,
                T0));
    }

    @Test
    @DisplayName("a user round-trips with its person principal, normalized email and defaults")
    void userRoundTrip() {
        UserEntity saved = newUser("Ada@Example.com");
        UserEntity read = users.findById(saved.getId()).orElseThrow();
        assertThat(read.getEmail()).isEqualTo("ada@example.com");
        assertThat(read.getPrincipalId()).isEqualTo(saved.getPrincipalId());
        assertThat(principals.find(read.getPrincipalId())).get().extracting(PrincipalEntity::getKind)
                .isEqualTo("person");
        assertThat(read.getStatus()).isEqualTo(UserStatus.INVITED);
        assertThat(read.getCreatedAt().truncatedTo(ChronoUnit.MICROS)).isEqualTo(T0);
        assertThat(users.findByEmail("ada@example.com")).isPresent();
        assertThat(users.existsByEmail("ada@example.com")).isTrue();
        assertThat(users.findByPrincipalId(saved.getPrincipalId())).isPresent();
    }

    @Test
    @DisplayName("the seeded catalog and built-in roles are readable through JPA")
    void seededCatalog() {
        assertThat(permissions.findAllByOrderByPermissionKeyAsc()).extracting(PermissionEntity::getKey)
                .containsExactlyInAnyOrderElementsOf(Arrays.stream(Permissions.values()).map(Permissions::key).toList())
                .isSorted();
        assertThat(permissions.findById("audit.read")).get().extracting(PermissionEntity::getGroup)
                .isEqualTo("platform");
        assertThat(roles.findAllByOrderByNameAsc()).extracting(RoleEntity::getName)
                .containsExactly("Admin", "Auditor", "Editor", "Operator", "Viewer");
        assertThat(roles.findByName("Viewer")).get().extracting(RoleEntity::isBuiltIn).isEqualTo(true);
        assertThat(rolePermissions.findByRoleId(BuiltInRole.EDITOR.id())).extracting(
                RolePermissionEntity::getPermissionKey).containsExactlyInAnyOrderElementsOf(
                        BuiltInRole.EDITOR.permissions().stream().map(Permissions::key).collect(Collectors.toSet()));
    }

    @Test
    @DisplayName("roles link to users and count their holders")
    void userRoles() {
        UserEntity user = newUser("grace@example.com");
        userRoles.saveAndFlush(new UserRoleEntity(user.getId(), BuiltInRole.EDITOR.id()));
        userRoles.saveAndFlush(new UserRoleEntity(user.getId(), BuiltInRole.AUDITOR.id()));
        assertThat(userRoles.findByUserId(user.getId())).extracting(UserRoleEntity::getRoleId)
                .containsExactlyInAnyOrder(BuiltInRole.EDITOR.id(), BuiltInRole.AUDITOR.id());
        assertThat(userRoles.countByRoleId(BuiltInRole.EDITOR.id())).isEqualTo(1L);
        assertThat(rolePermissions.findByRoleIdIn(userRoles.findByUserIdIn(java.util.List.of(user.getId())).stream()
                .map(UserRoleEntity::getRoleId).toList())).extracting(RolePermissionEntity::getPermissionKey)
                .contains("audit.read", "self.alerts");
    }

    @Test
    @DisplayName("a user token stores only the SHA-256 and an 8-character prefix, never the plaintext")
    void tokenStoresHashOnly() {
        UserEntity user = newUser("hedy@example.com");
        String plaintext = hasher.generateToken();
        String hash = hasher.hash(plaintext);
        UserTokenEntity token = userTokens.saveAndFlush(new UserTokenEntity(UUID.randomUUID(), user.getId(),
                UserTokenKind.ACTIVATION, hash, plaintext.substring(0, UserTokenEntity.PREFIX_LENGTH), null, T0,
                T0.plusSeconds(86_400)));
        assertThat(userTokens.findByTokenHash(hash)).get().extracting(UserTokenEntity::getId).isEqualTo(token.getId());
        assertThat(userTokens.findByUserIdAndKind(user.getId(), "activation")).hasSize(1);
        String stored = jdbc.queryForObject("select row_to_json(t)::text from user_token t", String.class);
        assertThat(stored).doesNotContain(plaintext).contains(hash);
        assertThat(jdbc.queryForObject("select token_prefix from user_token", String.class))
                .hasSize(UserTokenEntity.PREFIX_LENGTH);
    }
}
