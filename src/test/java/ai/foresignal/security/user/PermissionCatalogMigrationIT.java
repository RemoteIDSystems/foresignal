package ai.foresignal.security.user;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.schema.ScratchDatabase;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * {@code V14__identity_model.sql} (prompt R2-001) on a scratch database migrated to V13: it applies cleanly and creates
 * the six identity tables. The seeded catalog and built-in role matrix are then checked against {@link Permissions}
 * and {@link BuiltInRole} across the full migration set, because the catalog grows by additive migration: NP-008's
 * {@code V33__self_editions.sql} adds {@code self.editions} and its grants. This is the guard that keeps the Java
 * catalog and the seeded rows from drifting apart.
 */
@IntegrationTest
class PermissionCatalogMigrationIT {

    static final String MIGRATION = "db/migration/V14__identity_model.sql";

    /** The additive migration that completes the catalog after V14 (NP-008). */
    static final String MIGRATION_SELF_EDITIONS = "db/migration/V33__self_editions.sql";

    @Autowired JdbcTemplate admin;

    ScratchDatabase scratch;

    @AfterEach
    void drop() {
        if (scratch != null) {
            scratch.drop(admin);
        }
    }

    private Flyway flyway(String target) {
        return Flyway.configure().dataSource(scratch.dataSource()).locations("classpath:db/migration").target(target)
                .load();
    }

    private JdbcTemplate migratedTo14() {
        scratch = ScratchDatabase.recreate(admin, "identity_model_it");
        flyway("13").migrate();
        assertThat(flyway("14").migrate().migrationsExecuted).isEqualTo(1);
        return scratch.jdbc();
    }

    /** A scratch database migrated through every migration, so the catalog includes the additive {@code self.editions}. */
    private JdbcTemplate migratedToLatest() {
        scratch = ScratchDatabase.recreate(admin, "identity_model_it");
        Flyway.configure().dataSource(scratch.dataSource()).locations("classpath:db/migration").load().migrate();
        return scratch.jdbc();
    }

    private static Map<UUID, Set<String>> grants(JdbcTemplate db) {
        return db.queryForList("select role_id, permission_key from role_permission").stream()
                .collect(Collectors.groupingBy(row -> (UUID) row.get("role_id"),
                        Collectors.mapping(row -> (String) row.get("permission_key"), Collectors.toSet())));
    }

    @Test
    @DisplayName("V14 applies on top of V13 and creates the six identity tables")
    void createsTables() {
        JdbcTemplate db = migratedTo14();
        assertThat(db.queryForList("select table_name from information_schema.tables where table_schema = 'public' "
                + "and table_name in ('user', 'role', 'permission', 'user_role', 'role_permission', 'user_token')",
                String.class)).containsExactlyInAnyOrder("user", "role", "permission", "user_role", "role_permission",
                        "user_token");
        assertThat(db.queryForObject("select count(*) from \"user\"", Long.class)).isZero();
    }

    @Test
    @DisplayName("the seeded catalog equals Permissions: same keys, groups and descriptions")
    void catalogMatchesJava() {
        JdbcTemplate db = migratedToLatest();
        Set<String> seeded = new HashSet<>(db.queryForList(
                "select key || '|' || \"group\" || '|' || description from permission", String.class));
        Set<String> java = Arrays.stream(Permissions.values())
                .map(p -> p.key() + "|" + p.group().wireValue() + "|" + p.description()).collect(Collectors.toSet());
        assertThat(seeded).isEqualTo(java);
    }

    @Test
    @DisplayName("the five built-in roles are seeded with their stable ids and exactly BuiltInRole's permissions")
    void rolesMatchJava() {
        JdbcTemplate db = migratedToLatest();
        assertThat(db.queryForList("select id || '|' || name || '|' || built_in from role", String.class))
                .containsExactlyInAnyOrderElementsOf(Arrays.stream(BuiltInRole.values())
                        .map(r -> r.id() + "|" + r.roleName() + "|true").toList());
        Map<UUID, Set<String>> grants = grants(db);
        for (BuiltInRole role : BuiltInRole.values()) {
            assertThat(grants.get(role.id())).as(role.roleName())
                    .isEqualTo(role.permissions().stream().map(Permissions::key).collect(Collectors.toSet()));
        }
        assertThat(grants.get(BuiltInRole.ADMIN.id())).hasSize(Permissions.values().length);
        assertThat(grants.get(BuiltInRole.OPERATOR.id())).doesNotContain("users.manage", "roles.manage");
        assertThat(grants.get(BuiltInRole.VIEWER.id())).containsExactlyInAnyOrder("data.read", "self.views",
                "self.preferences");
        assertThat(grants.get(BuiltInRole.AUDITOR.id())).containsExactlyInAnyOrder("audit.read", "data.read");
    }

    @Test
    @DisplayName("re-running the seed against a partially seeded database restores it without duplicates")
    void seedIsRerunnable() throws IOException {
        JdbcTemplate db = migratedToLatest();
        Map<UUID, Set<String>> before = grants(db);
        db.update("delete from role_permission where role_id = ?", BuiltInRole.EDITOR.id());
        for (String insert : seedStatements()) {
            db.execute(insert);
        }
        assertThat(grants(db)).isEqualTo(before);
        assertThat(db.queryForObject("select count(*) from permission", Long.class))
                .isEqualTo((long) Permissions.values().length);
        assertThat(db.queryForObject("select count(*) from role", Long.class))
                .isEqualTo((long) BuiltInRole.values().length);
    }

    @Test
    @DisplayName("email is unique, and deleting a user or role cascades to its links and tokens")
    void constraintsAndCascades() {
        JdbcTemplate db = migratedTo14();
        db.update("insert into principal (id, handle, display_name, kind, disabled, created_at, updated_at) "
                + "values ('01JA8Q3V7S2K9W4T6Y1B5N0M3C', 'ada', 'Ada', 'person', false, now(), now())");
        UUID user = UUID.randomUUID();
        db.update("insert into \"user\" (id, email, full_name, principal_id, created_at, updated_at) values "
                + "(?, 'ada@example.com', 'Ada', '01JA8Q3V7S2K9W4T6Y1B5N0M3C', now(), now())", user);
        assertThat(db.queryForObject("select status || ' ' || mfa_enabled from \"user\"", String.class))
                .isEqualTo("invited false");
        assertThat(insertFails(db, "insert into \"user\" (id, email, full_name, principal_id, created_at, updated_at) "
                + "values ('" + UUID.randomUUID() + "', 'ada@example.com', 'Other', '01JA8Q3V7S2K9W4T6Y1B5N0M3C', "
                + "now(), now())")).isTrue();
        UUID custom = UUID.randomUUID();
        db.update("insert into role (id, name, built_in, created_at, updated_at) values (?, 'Analyst', false, now(), "
                + "now())", custom);
        db.update("insert into role_permission (role_id, permission_key) values (?, 'data.read')", custom);
        db.update("insert into user_role (user_id, role_id) values (?, ?)", user, custom);
        db.update("insert into user_role (user_id, role_id) values (?, ?)", user, BuiltInRole.VIEWER.id());
        db.update("insert into user_token (id, user_id, kind, token_hash, token_prefix, created_at, expires_at) "
                + "values (?, ?, 'activation', ?, 'fsu_abcd', now(), now() + interval '1 day')", UUID.randomUUID(),
                user, "b".repeat(64));
        assertThat(insertFails(db, "insert into role_permission (role_id, permission_key) values ('" + custom
                + "', 'users.delete')")).isTrue();

        db.update("delete from role where id = ?", custom);
        assertThat(db.queryForObject("select count(*) from role_permission where role_id = ?", Long.class, custom))
                .isZero();
        assertThat(db.queryForObject("select count(*) from user_role", Long.class)).isEqualTo(1L);

        db.update("delete from \"user\" where id = ?", user);
        assertThat(db.queryForObject("select count(*) from user_role", Long.class)).isZero();
        assertThat(db.queryForObject("select count(*) from user_token", Long.class)).isZero();
    }

    private static boolean insertFails(JdbcTemplate db, String sql) {
        try {
            db.execute(sql);
            return false;
        } catch (org.springframework.dao.DataAccessException expected) {
            return true;
        }
    }

    /** The {@code insert} statements of V14 and V33 (the catalog seed), each ending at its {@code on conflict do nothing;}. */
    static List<String> seedStatements() throws IOException {
        List<String> statements = new ArrayList<>();
        for (String migration : List.of(MIGRATION, MIGRATION_SELF_EDITIONS)) {
            String sql;
            try (InputStream in = new ClassPathResource(migration).getInputStream()) {
                sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            Arrays.stream(sql.split(";"))
                    .map(s -> s.lines().filter(l -> !l.startsWith("--")).collect(Collectors.joining("\n")).strip())
                    .filter(s -> s.startsWith("insert into")).forEach(statements::add);
        }
        return statements;
    }
}
