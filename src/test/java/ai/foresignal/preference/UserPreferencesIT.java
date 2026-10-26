package ai.foresignal.preference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.collab.PrincipalNotificationRepository;
import ai.foresignal.security.PrincipalRepository;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.user.BuiltInRole;
import ai.foresignal.security.user.PasswordHasher;
import ai.foresignal.security.user.UserAdminService;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleRepository;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.security.TestUsers;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Per-user preferences through the real filter chain (prompt R2-012): defaults, validation, the {@code self.preferences}
 * gate, the notification switches writing through to the existing notification preferences (one store), the timezone
 * living on the principal, and what deleting a user does to the row.
 */
@IntegrationTest
class UserPreferencesIT {

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper json;
    @Autowired private PrincipalService principals;
    @Autowired private PrincipalRepository principalRows;
    @Autowired private UserRepository users;
    @Autowired private UserRoleRepository userRoles;
    @Autowired private PasswordHasher hasher;
    @Autowired private TokenService tokens;
    @Autowired private UserPreferencesRepository stored;
    @Autowired private PrincipalNotificationRepository notificationPrefs;
    @Autowired private NotificationChannelRepository channels;
    @Autowired private UserAdminService userAdmin;
    @Autowired private AppSettingService settings;

    private String adminToken;
    private UserEntity alice;
    private String aliceSession;
    private String auditorSession;

    @BeforeEach
    void setUp() throws Exception {
        settings.invalidateCache();
        adminToken = TestTokens.create(tokens, "prefs-it-admin", Scope.ADMIN).plaintext();
        alice = TestUsers.active(principals, users, userRoles, hasher, "alice@example.com", BuiltInRole.VIEWER);
        TestUsers.active(principals, users, userRoles, hasher, "audit@example.com", BuiltInRole.AUDITOR);
        TestUsers.active(principals, users, userRoles, hasher, "root@example.com", BuiltInRole.ADMIN);
        aliceSession = login("alice@example.com");
        auditorSession = login("audit@example.com");
    }

    // ------------------------------------------------------------------------- helpers

    private MockHttpServletResponse call(MockHttpServletRequestBuilder builder, String bearer) throws Exception {
        if (bearer != null) {
            builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer);
        }
        return mvc.perform(builder).andReturn().getResponse();
    }

    private static MockHttpServletRequestBuilder jsonBody(MockHttpServletRequestBuilder builder, String body) {
        return builder.contentType(MediaType.APPLICATION_JSON).content(body.replace('\'', '"'));
    }

    private JsonNode body(MockHttpServletResponse r) throws Exception {
        return json.readTree(r.getContentAsString());
    }

    private String login(String email) throws Exception {
        MockHttpServletResponse r = mvc.perform(jsonBody(post("/v1/auth/login"), TestUsers.loginBody(email)))
                .andReturn().getResponse();
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        return body(r).get("token").asString();
    }

    private JsonNode prefs(String bearer) throws Exception {
        MockHttpServletResponse r = call(get("/v1/me/preferences"), bearer);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        return body(r);
    }

    private MockHttpServletResponse update(String bearer, String body) throws Exception {
        return call(jsonBody(put("/v1/me/preferences"), body), bearer);
    }

    private void assertInvalid(String body, String field) throws Exception {
        MockHttpServletResponse r = update(aliceSession, body);
        assertThat(r.getStatus()).as(body + " → " + r.getContentAsString()).isEqualTo(400);
        assertThat(r.getContentAsString()).contains(field);
    }

    /** Alice's notification preferences: one email channel and one ntfy channel. */
    private JsonNode twoChannels() throws Exception {
        MockHttpServletResponse r = call(jsonBody(put("/v1/me/notification-preferences"), "{'items':["
                + "{'eventTypes':['mention'],'channel':{'type':'email','config':{'smtpHost':'127.0.0.1','smtpPort':2525,"
                + "'tls':'none','from':'fs@example.invalid','to':['alice@example.com']}}},"
                + "{'eventTypes':['share'],'channel':{'type':'ntfy','config':{'topic':'alice-fs'}}}]}"), aliceSession);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        return body(r);
    }

    private NotificationChannelEntity channelOf(JsonNode item) {
        return channels.findById(UUID.fromString(item.get("channel").get("channelId").asString())).orElseThrow();
    }

    // ------------------------------------------------------------------------- tests

    @Test
    @DisplayName("an unset user reads the documented defaults")
    void defaults() throws Exception {
        JsonNode p = prefs(aliceSession);
        assertThat(p.get("timezone").asString()).isEqualTo("UTC");
        assertThat(p.get("theme").asString()).isEqualTo("system");
        assertThat(p.get("locale").asString()).isEqualTo("en");
        assertThat(p.get("notifications").get("emailEnabled").asBoolean()).isTrue();
        assertThat(p.get("notifications").get("pushEnabled").asBoolean()).isTrue();
        assertThat(p.get("notifications").get("quietHours").isNull()).isTrue();
        assertThat(p.get("ui").isObject()).isTrue();
        assertThat(p.get("ui").size()).isZero();
        assertThat(stored.findById(alice.getId())).isEmpty();
    }

    @Test
    @DisplayName("an update is stored, read back, and replaces what was there")
    void storedAfterUpdate() throws Exception {
        MockHttpServletResponse r = update(aliceSession, "{'timezone':'Europe/London','locale':'en-gb','theme':'dark',"
                + "'ui':{'mapStyle':'satellite','panels':['alerts']}}");
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        JsonNode p = prefs(aliceSession);
        assertThat(p.get("timezone").asString()).isEqualTo("Europe/London");
        assertThat(p.get("locale").asString()).isEqualTo("en-GB");
        assertThat(p.get("theme").asString()).isEqualTo("dark");
        assertThat(p.get("ui").get("mapStyle").asString()).isEqualTo("satellite");
        assertThat(principalRows.findById(alice.getPrincipalId()).orElseThrow().getTimezone())
                .isEqualTo("Europe/London");

        assertThat(update(aliceSession, "{'theme':'light'}").getStatus()).isEqualTo(200);
        p = prefs(aliceSession);
        assertThat(p.get("theme").asString()).isEqualTo("light");
        assertThat(p.get("timezone").asString()).as("an absent timezone resets to the server's").isEqualTo("UTC");
        assertThat(p.get("locale").asString()).isEqualTo("en");
        assertThat(p.get("ui").size()).isZero();
        assertThat(principalRows.findById(alice.getPrincipalId()).orElseThrow().getTimezone()).isNull();
    }

    @Test
    @DisplayName("invalid timezone, locale, theme, quiet hours, ui and unknown members are 400")
    void validation() throws Exception {
        assertInvalid("{'timezone':'Mars/Olympus_Mons'}", "timezone");
        assertInvalid("{'timezone':42}", "timezone");
        assertInvalid("{'locale':'not a tag'}", "locale");
        assertInvalid("{'locale':'qq'}", "locale");
        assertInvalid("{'theme':'neon'}", "theme");
        assertInvalid("{'notifications':{'quietHours':'25:00-07:00'}}", "notifications.quietHours");
        assertInvalid("{'notifications':{'emailEnabled':'yes'}}", "notifications.emailEnabled");
        assertInvalid("{'notifications':{'smsEnabled':true}}", "notifications.smsEnabled");
        assertInvalid("{'ui':[1,2]}", "ui");
        assertInvalid("{'ui':{'blob':'" + "x".repeat(UserPreferencesService.MAX_UI_BYTES) + "'}}", "ui");
        assertInvalid("{'colour':'red'}", "colour");
        MockHttpServletResponse r = call(put("/v1/me/preferences").contentType(MediaType.APPLICATION_JSON)
                .content("[]"), aliceSession);
        assertThat(r.getStatus()).isEqualTo(400);
        assertThat(stored.findById(alice.getId())).as("nothing invalid is stored").isEmpty();
    }

    @Test
    @DisplayName("self.preferences gates both operations; an API token is 401")
    void gate() throws Exception {
        MockHttpServletResponse denied = call(get("/v1/me/preferences"), auditorSession);
        assertThat(denied.getStatus()).as(denied.getContentAsString()).isEqualTo(403);
        assertThat(body(denied).get("code").asString()).isEqualTo(ErrorCodes.PERMISSION_DENIED);
        assertThat(body(denied).get("requiredPermission").asString()).isEqualTo("self.preferences");
        assertThat(update(auditorSession, "{'theme':'dark'}").getStatus()).isEqualTo(403);
        assertThat(call(get("/v1/me/preferences"), adminToken).getStatus()).isEqualTo(401);
        assertThat(update(adminToken, "{'theme':'dark'}").getStatus()).isEqualTo(401);
        assertThat(call(get("/v1/me/preferences"), null).getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("notifications read and write the existing notification preferences: no second store")
    void notificationsAreTheSameStore() throws Exception {
        JsonNode items = twoChannels();
        JsonNode email = items.get(0);
        JsonNode ntfy = items.get(1);

        MockHttpServletResponse r = update(aliceSession, "{'notifications':{'emailEnabled':false,"
                + "'quietHours':'22:00-07:00'}}");
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        assertThat(body(r).get("notifications").get("emailEnabled").asBoolean()).isFalse();
        assertThat(body(r).get("notifications").get("pushEnabled").asBoolean()).isTrue();
        assertThat(body(r).get("notifications").get("quietHours").asString()).isEqualTo("22:00-07:00");

        // The existing surface shows the change, because it is the same rows.
        JsonNode listed = body(call(get("/v1/me/notification-preferences"), aliceSession));
        assertThat(listed.get(0).get("enabled").asBoolean()).isFalse();
        assertThat(listed.get(1).get("enabled").asBoolean()).isTrue();
        assertThat(listed.get(0).get("quietHours").get("start").asString()).isEqualTo("22:00");
        assertThat(listed.get(1).get("quietHours").get("end").asString()).isEqualTo("07:00");
        assertThat(channelOf(email).isEnabled()).isFalse();
        assertThat(channelOf(ntfy).isEnabled()).isTrue();
        assertThat(channelOf(ntfy).getQuietHours()).containsEntry("start", "22:00");

        // And a change made there is what the preferences read.
        String replaced = "{'items':[{'id':'" + email.get("id").asString() + "','eventTypes':['mention'],'channel':"
                + "{'type':'email','config':{'smtpHost':'127.0.0.1','smtpPort':2525,'tls':'none',"
                + "'from':'fs@example.invalid','to':['alice@example.com']}},'enabled':true},{'id':'"
                + ntfy.get("id").asString() + "','eventTypes':['share'],'channel':{'type':'ntfy','config':"
                + "{'topic':'alice-fs'}},'enabled':false}]}";
        assertThat(call(jsonBody(put("/v1/me/notification-preferences"), replaced), aliceSession).getStatus())
                .isEqualTo(200);
        JsonNode n = prefs(aliceSession).get("notifications");
        assertThat(n.get("emailEnabled").asBoolean()).isTrue();
        assertThat(n.get("pushEnabled").asBoolean()).isFalse();
        assertThat(n.get("quietHours").isNull()).as("the replace cleared the windows").isTrue();

        // A write that leaves notifications out changes none of them.
        assertThat(update(aliceSession, "{'theme':'dark'}").getStatus()).isEqualTo(200);
        assertThat(prefs(aliceSession).get("notifications").get("pushEnabled").asBoolean()).isFalse();

        // Clearing quiet hours clears them everywhere.
        update(aliceSession, "{'notifications':{'quietHours':'21:30-06:00'}}");
        update(aliceSession, "{'notifications':{'quietHours':null}}");
        notificationPrefs.findByPrincipalIdOrderByCreatedAtAscIdAsc(alice.getPrincipalId())
                .forEach(p -> assertThat(p.getQuietHours()).isNull());
    }

    @Test
    @DisplayName("the timezone is the principal's and moves the personal channels; stored instants do not change")
    void timezoneRendersOverUtc() throws Exception {
        JsonNode items = twoChannels();
        NotificationChannelEntity before = channelOf(items.get(1));
        Instant created = before.getCreatedAt();
        Instant prefCreated = notificationPrefs.findByPrincipalIdOrderByCreatedAtAscIdAsc(alice.getPrincipalId())
                .get(0).getCreatedAt();

        assertThat(update(aliceSession, "{'timezone':'America/Chicago'}").getStatus()).isEqualTo(200);
        NotificationChannelEntity after = channelOf(items.get(1));
        assertThat(after.getTimezone()).isEqualTo("America/Chicago");
        assertThat(after.getCreatedAt()).isEqualTo(created);
        assertThat(notificationPrefs.findByPrincipalIdOrderByCreatedAtAscIdAsc(alice.getPrincipalId()).get(0)
                .getCreatedAt()).isEqualTo(prefCreated);
    }

    @Test
    @DisplayName("deleting the user removes the stored preferences, reassigned or not")
    void deletion() throws Exception {
        assertThat(update(aliceSession, "{'theme':'dark'}").getStatus()).isEqualTo(200);
        assertThat(stored.findById(alice.getId())).isPresent();
        UserAdminService.Deletion d = userAdmin.delete(alice.getId(), true, null, "127.0.0.1");
        assertThat(d.counts()).containsEntry("preferences", 1);
        assertThat(stored.findById(alice.getId())).isEmpty();
    }
}
