package ai.foresignal.setup;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import ai.foresignal.notify.ChannelService;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.WorkspaceEntity;
import ai.foresignal.object.WorkspaceService;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.user.BuiltInRole;
import ai.foresignal.security.user.PasswordHasher;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.security.TestUsers;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import ai.foresignal.trigger.TriggerRuleService;
import ai.foresignal.view.SavedViewService;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

/**
 * The v2.0 cutover move (prompt R2-027, {@link CutoverMigrator}): owner-less configuration stays shared, what the v1
 * operator owned through the {@code owner} principal moves to the first admin, a second run is a no-op, and scoped
 * machine tokens keep working.
 */
@IntegrationTest
@Import(TriggerTestSources.class)
class GlobalToSharedMigrationIT {

    private static final String RULE = "{\"name\":\"v1 rule\",\"sources\":[\"" + STANDARD + "\"],"
            + "\"conditions\":[{\"path\":\"mag\",\"op\":\">=\",\"value\":6}]}";
    private static final String WEBHOOK = "{\"name\":\"%s\",\"type\":\"webhook\","
            + "\"config\":{\"url\":\"http://127.0.0.1:9/%s\"}}";

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper json;
    @Autowired private PrincipalService principals;
    @Autowired private UserRepository users;
    @Autowired private UserRoleRepository userRoles;
    @Autowired private PasswordHasher hasher;
    @Autowired private TokenService tokens;
    @Autowired private TriggerRuleService rules;
    @Autowired private ChannelService channels;
    @Autowired private SavedViewService views;
    @Autowired private WorkspaceService workspaces;
    @Autowired private CutoverMigrator migrator;
    @Autowired private JdbcTemplate jdbc;

    private String owner(String table, String column, Object id) {
        return jdbc.queryForObject("SELECT " + column + " FROM " + table + " WHERE id = ?", String.class, id);
    }

    @Test
    @DisplayName("shared stays shared, the v1 operator's own rows move to the first admin, and a rerun is a no-op")
    void migrates() throws Exception {
        String v1Operator = principals.owner().getId();
        UUID sharedRule = rules.create(json.readTree(RULE)).rule().getId();
        UUID sharedView = views.create(json.readTree("{\"slug\":\"v1-view\",\"name\":\"V1 view\"}")).getId();
        UUID globalChannel = channels.create(json.readTree(WEBHOOK.formatted("ops", "ops"))).channel().id();
        UUID personalChannel = channels.savePersonal(null, json.readTree(WEBHOOK.formatted("mine", "mine")),
                v1Operator);
        WorkspaceEntity workspace = workspaces.create(json.readTree("{\"name\":\"Operator desk\"}"),
                new ObjectCaller(v1Operator, "token:bootstrap", Scope.ADMIN, true, Set.of()));
        String machine = TestTokens.create(tokens, "v1-reader", Scope.READ).plaintext();
        UserEntity admin = TestUsers.active(principals, users, userRoles, hasher, "admin@example.com",
                BuiltInRole.ADMIN);

        CutoverMigrator.Result result = migrator.migrate(admin.getId());

        assertThat(result.alreadyDone()).isFalse();
        assertThat(result.fromPrincipalId()).isEqualTo(v1Operator);
        assertThat(result.counts()).containsEntry("channels", 1).containsEntry("workspaces", 1)
                .containsEntry("triggerRules", 0).containsEntry("savedViews", 0);
        assertThat(owner("trigger_rule", "owner_principal_id", sharedRule)).as("shared rule").isNull();
        assertThat(owner("saved_view", "owner_principal_id", sharedView)).as("shared view").isNull();
        assertThat(owner("notification_channel", "owner_principal_id", globalChannel)).as("global channel").isNull();
        assertThat(owner("notification_channel", "owner_principal_id", personalChannel))
                .isEqualTo(admin.getPrincipalId());
        assertThat(owner("workspace", "owner", workspace.getId())).isEqualTo(admin.getPrincipalId());

        CutoverMigrator.Result again = migrator.migrate(admin.getId());
        assertThat(again.alreadyDone()).isTrue();
        assertThat(again.counts()).isEqualTo(result.counts());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM v2_cutover", Long.class)).isEqualTo(1L);

        assertThat(mvc.perform(get("/v1/sources").header(HttpHeaders.AUTHORIZATION, "Bearer " + machine))
                .andReturn().getResponse().getStatus()).as("a scoped machine token still authenticates")
                .isEqualTo(200);
    }

    @Test
    @DisplayName("a fresh instance moves nothing and records the cutover for its first admin")
    void freshInstance() {
        UserEntity admin = TestUsers.active(principals, users, userRoles, hasher, "fresh@example.com",
                BuiltInRole.ADMIN);

        CutoverMigrator.Result result = migrator.migrate(admin.getId());

        assertThat(result.counts().values()).allMatch(n -> n == 0);
        assertThat(migrator.recorded()).hasValueSatisfying(r -> assertThat(r.adminUserId()).isEqualTo(admin.getId()));
    }

    @Test
    @DisplayName("the startup run picks the earliest active Admin, not an Editor")
    void startupPicksFirstAdmin() {
        TestUsers.active(principals, users, userRoles, hasher, "editor@example.com", BuiltInRole.EDITOR);
        UserEntity first = TestUsers.active(principals, users, userRoles, hasher, "a1@example.com",
                BuiltInRole.ADMIN);
        TestUsers.active(principals, users, userRoles, hasher, "a2@example.com", BuiltInRole.ADMIN);

        assertThat(migrator.firstAdmin()).contains(first.getId());
    }
}
