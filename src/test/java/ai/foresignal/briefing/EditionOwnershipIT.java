package ai.foresignal.briefing;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.user.BuiltInRole;
import ai.foresignal.security.user.PasswordHasher;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.briefing.BriefingHttp;
import ai.foresignal.testsupport.briefing.BriefingItSupport;
import ai.foresignal.testsupport.briefing.BriefingTestSources;
import ai.foresignal.testsupport.security.TestUsers;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;

/**
 * Per-user editions over HTTP (prompt NP-003): a briefing a signed-in user creates is that user's edition, listed by
 * {@code listMyEditions}, seen, run, changed and deleted only by its owner (and instance tokens), {@code 404} for any
 * other user; it follows the owner's source view; news subscriptions are per user and validated.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(BriefingTestSources.class)
class EditionOwnershipIT extends BriefingItSupport {

    @Autowired MockMvc mvc;
    @Autowired PrincipalService principals;
    @Autowired UserRepository users;
    @Autowired UserRoleRepository userRoles;
    @Autowired PasswordHasher hasher;

    BriefingHttp http;
    UserEntity alice;
    String aliceSession;
    String bobSession;

    @BeforeEach
    void setUpUsers() throws Exception {
        http = new BriefingHttp(mvc);
        alice = TestUsers.active(principals, users, userRoles, hasher, "alice-edition@example.com",
                BuiltInRole.EDITOR);
        TestUsers.active(principals, users, userRoles, hasher, "bob-edition@example.com", BuiltInRole.EDITOR);
        aliceSession = login("alice-edition@example.com");
        bobSession = login("bob-edition@example.com");
    }

    private String login(String email) throws Exception {
        return "Bearer " + http.call("POST", "/v1/auth/login", null, TestUsers.loginBody(email), 200).get("token")
                .asString();
    }

    private String createEdition(String bearer, String name, String extra) throws Exception {
        return http.call("POST", "/v1/briefings", bearer, digestBody(name, "0 7 * * *", extra), 201).get("id")
                .asString();
    }

    private static List<String> ids(JsonNode items, String field) {
        List<String> out = new ArrayList<>();
        items.forEach(i -> out.add(i.get(field).asString()));
        return out;
    }

    @Test
    @DisplayName("a session's briefing is its edition: ownerUserId set, listed only for its owner")
    void ownedAndListed() throws Exception {
        String mine = createEdition(aliceSession, "Alice daily", "\"outputLanguage\":\"es\"");
        String bobs = createEdition(bobSession, "Bob daily", null);
        String instance = createEdition(agent, "Instance briefing", null);

        JsonNode created = http.call("GET", "/v1/briefings/" + mine, aliceSession, null, 200);
        assertThat(created.get("ownerUserId").asString()).isEqualTo(alice.getId().toString());
        assertThat(created.get("outputLanguage").asString()).isEqualTo("es");
        assertThat(http.call("GET", "/v1/briefings/" + instance, agent, null, 200).get("ownerUserId").isNull())
                .isTrue();

        JsonNode page = http.call("GET", "/v1/me/editions", aliceSession, null, 200);
        assertThat(ids(page.get("items"), "briefingId")).containsExactly(mine);
        JsonNode summary = page.get("items").get(0);
        assertThat(summary.get("name").asString()).isEqualTo("Alice daily");
        assertThat(summary.get("outputLanguage").asString()).isEqualTo("es");
        assertThat(summary.get("latestRunId").isNull()).isTrue();
        assertThat(ids(http.call("GET", "/v1/me/editions", bobSession, null, 200).get("items"), "briefingId"))
                .containsExactly(bobs);

        assertThat(ids(http.call("GET", "/v1/briefings", aliceSession, null, 200), "id")).contains(mine, instance)
                .doesNotContain(bobs);
        assertThat(ids(http.call("GET", "/v1/briefings", agent, null, 200), "id")).contains(mine, bobs, instance);
    }

    @Test
    @DisplayName("another user cannot see, run, change or delete an edition; its owner and instance tokens can")
    void isolated() throws Exception {
        String mine = createEdition(aliceSession, "Alice private", null);

        http.call("GET", "/v1/briefings/" + mine, bobSession, null, 404);
        http.call("POST", "/v1/briefings/" + mine + "/run", bobSession, null, 404);
        http.call("GET", "/v1/briefings/" + mine + "/runs", bobSession, null, 404);
        http.call("GET", "/v1/briefings/" + mine + "/runs/latest", bobSession, null, 404);
        http.call("PUT", "/v1/briefings/" + mine, bobSession, digestBody("Taken", "0 8 * * *", null), 404);
        http.call("DELETE", "/v1/briefings/" + mine, bobSession, null, 404);
        http.call("GET", "/v1/briefings/" + mine + "/runs/feed.atom", bobSession, null, 404);
        http.call("GET", "/v1/briefings/" + mine + "/podcast.rss", bobSession, null, 404);

        quake("ed-1", "Tokyo", 5.2);
        JsonNode run = http.call("POST", "/v1/briefings/" + mine + "/run", aliceSession, null, 200);
        assertThat(run.get("status").asString()).isEqualTo("succeeded");
        assertThat(http.call("GET", "/v1/me/editions", aliceSession, null, 200).get("items").get(0)
                .get("latestRunId").asString()).isEqualTo(run.get("id").asString());

        http.call("GET", "/v1/briefings/" + mine, agent, null, 200);
        http.call("DELETE", "/v1/briefings/" + mine, aliceSession, null, 204);
    }

    @Test
    @DisplayName("listMyEditions needs a session (an API token is 401) and pages newest first")
    void listing() throws Exception {
        http.call("GET", "/v1/me/editions", agent, null, 401);
        String first = createEdition(aliceSession, "First", null);
        clock.advance(java.time.Duration.ofSeconds(1));
        String second = createEdition(aliceSession, "Second", null);
        JsonNode page = http.call("GET", "/v1/me/editions?limit=1", aliceSession, null, 200);
        assertThat(ids(page.get("items"), "briefingId")).containsExactly(second);
        String cursor = page.get("nextCursor").asString();
        JsonNode next = http.call("GET", "/v1/me/editions?limit=1&cursor=" + cursor, aliceSession, null, 200);
        assertThat(ids(next.get("items"), "briefingId")).containsExactly(first);
        http.call("GET", "/v1/me/editions?limit=0", aliceSession, null, 400);
    }

    @Test
    @DisplayName("an edition follows its owner's source view: a source the owner turned off is not listed")
    void followsSourceView() throws Exception {
        String mine = createEdition(aliceSession, "Alice view", null);
        quake("ed-view", "Osaka", 6.1);
        http.call("PUT", "/v1/me/sources", aliceSession,
                "{\"sources\":[{\"sourceId\":\"" + BriefingTestSources.QUAKES + "\",\"enabled\":false}]}", 200);
        JsonNode run = http.call("POST", "/v1/briefings/" + mine + "/run", aliceSession, null, 200);
        assertThat(run.get("contentMarkdown").asString()).doesNotContain("Osaka");
        BriefingEntity entity = briefingRepository.findById(UUID.fromString(mine)).orElseThrow();
        assertThat(entity.getOwnerUserId()).isEqualTo(alice.getId());
    }

    @Test
    @DisplayName("news subscriptions are per user, canonical, validated and session-only")
    void newsSubscriptions() throws Exception {
        http.call("GET", "/v1/me/news-subscriptions", agent, null, 401);
        JsonNode empty = http.call("GET", "/v1/me/news-subscriptions", aliceSession, null, 200);
        assertThat(empty.get("subscriptions").size()).isZero();
        assertThat(empty.get("updatedAt").isNull()).isTrue();

        JsonNode saved = http.call("PUT", "/v1/me/news-subscriptions", aliceSession,
                "{\"subscriptions\":[{\"type\":\"language\",\"id\":\"pt-br\"}]}", 200);
        assertThat(saved.get("subscriptions").get(0).get("id").asString()).isEqualTo("pt-BR");
        assertThat(http.call("GET", "/v1/me/news-subscriptions", bobSession, null, 200).get("subscriptions").size())
                .isZero();

        JsonNode problem = http.call("PUT", "/v1/me/news-subscriptions", aliceSession,
                "{\"subscriptions\":[{\"type\":\"outlet\",\"id\":\"no-such-outlet.invalid\"},"
                        + "{\"type\":\"language\",\"id\":\"not a tag\"},{\"type\":\"topic\",\"id\":\"x\"}]}", 400);
        assertThat(problem.toString()).contains("subscriptions[0].id").contains("subscriptions[1].id")
                .contains("subscriptions[2].type");
        http.call("PUT", "/v1/me/news-subscriptions", aliceSession, "{\"subscriptions\":[]}", 200);
    }
}
