package ai.foresignal.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotificationService;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.store.NotificationChannelRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.notify.NotifyTestConfig;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The {@code Channels} operations and the notification settings over HTTP (prompt 018). */
@IntegrationTest
@AutoConfigureMockMvc
@Import(NotifyTestConfig.class)
class ChannelsControllerIT extends WireMockSupport {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;
    @Autowired ChannelService channels;
    @Autowired NotificationService notifications;
    @Autowired NotificationChannelRepository channelRows;
    @Autowired MutableClock clock;

    String token;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        token = adminTokens.rotate();
        clock.setInstant(T);
    }

    MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    ResultActions send(MockHttpServletRequestBuilder request, String body) throws Exception {
        return mvc.perform(auth(request).contentType(MediaType.APPLICATION_JSON).content(body.replace('\'', '"')));
    }

    String create(String body) throws Exception {
        String response = send(post("/v1/channels"), body).andExpect(status().isCreated()).andReturn().getResponse()
                .getContentAsString();
        return JSON.readTree(response).get("id").asString();
    }

    @Test
    @DisplayName("create: 201 with Location, defaults, secret names listed and secret values never returned")
    void create() throws Exception {
        send(post("/v1/channels"), "{'name':'Slack #ops','type':'slack','config':{'username':'Foresignal'},"
                + "'secrets':{'webhookUrl':'https://hooks.example.invalid/services/T/B/SECRETVALUE'},'minSeverity':'warning'}")
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, containsString("/v1/channels/")))
                .andExpect(jsonPath("$.type", is("slack")))
                .andExpect(jsonPath("$.events", hasSize(1)))
                .andExpect(jsonPath("$.ruleIds", nullValue()))
                .andExpect(jsonPath("$.secretFields", hasItem("webhookUrl")))
                .andExpect(jsonPath("$.backlogCap", is(20)))
                .andExpect(jsonPath("$.editInPlace", is(true)))
                .andExpect(jsonPath("$.failedLast24h", is(0)))
                .andExpect(jsonPath("$.secrets").doesNotExist())
                .andExpect(content -> assertThat(content.getResponse().getContentAsString()).doesNotContain("SECRETVALUE"));
        mvc.perform(auth(get("/v1/channels"))).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(content -> assertThat(content.getResponse().getContentAsString()).doesNotContain("SECRETVALUE"));
        assertThat(channelRows.findAll()).singleElement().satisfies(e -> {
            assertThat(e.getSecretCiphertext()).isNotNull();
            assertThat(new String(e.getSecretCiphertext())).doesNotContain("SECRETVALUE");
        });
    }

    @Test
    @DisplayName("validation: unknown type, later types, missing secrets, bad timezone and quiet hours, Teams connectors")
    void validation() throws Exception {
        send(post("/v1/channels"), "{'name':'x','type':'carrier-pigeon'}").andExpect(status().isBadRequest());
        send(post("/v1/channels"), "{'name':'x','type':'webpush'}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message", containsString("built-in Push channel")));
        send(post("/v1/channels"), "{'name':'x','type':'meshtastic'}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message", containsString("084")));
        // Prompt 060: mqtt is available and needs a brokerUrl.
        send(post("/v1/channels"), "{'name':'x','type':'mqtt'}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field", is("config.brokerUrl")));
        send(post("/v1/channels"), "{'name':'x','type':'slack'}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field", is("secrets.webhookUrl")));
        send(post("/v1/channels"), "{'name':'x','type':'ntfy','config':{'topic':'t'},'timezone':'Mars/Olympus',"
                + "'quietHours':{'start':'25:00','end':'07:00'},'backlogCap':0}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors", hasSize(3)));
        send(post("/v1/channels"), "{'name':'x','type':'teams','secrets':{'workflowUrl':"
                + "'https://contoso.webhook.office.com/webhookb2/abc'}}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message", containsString("Workflows")));
        assertThat(channelRows.count()).isZero();
    }

    @Test
    @DisplayName("PUT is a full replace: omitted secrets are kept, a provided object replaces them")
    void putSecrets() throws Exception {
        String id = create("{'name':'w','type':'webhook','config':{'url':'https://r.example.invalid'},"
                + "'secrets':{'hmacSecret':'one','header.X-Key':'k'},'timezone':'America/Chicago'}");
        send(put("/v1/channels/" + id), "{'name':'renamed','type':'webhook','config':{'url':'https://r.example.invalid'}}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("renamed")))
                .andExpect(jsonPath("$.timezone", nullValue()))
                .andExpect(jsonPath("$.secretFields", hasSize(2)));
        assertThat(channels.secrets(channelRows.findById(UUID.fromString(id)).orElseThrow())).containsEntry("hmacSecret", "one");
        send(put("/v1/channels/" + id), "{'name':'renamed','type':'webhook','config':{'url':'https://r.example.invalid'},"
                + "'secrets':{'hmacSecret':'two'}}")
                .andExpect(jsonPath("$.secretFields", hasSize(1)));
        assertThat(channels.secrets(channelRows.findById(UUID.fromString(id)).orElseThrow()))
                .containsExactlyEntriesOf(java.util.Map.of("hmacSecret", "two"));
        send(put("/v1/channels/" + UUID.randomUUID()), "{'name':'x','type':'webhook','config':{'url':'https://r.example.invalid'}}")
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("401 without a token; 404 for unknown and malformed ids; delete is 204")
    void authAndNotFound() throws Exception {
        mvc.perform(get("/v1/channels")).andExpect(status().isUnauthorized());
        mvc.perform(get("/v1/channels/types")).andExpect(status().isUnauthorized());
        mvc.perform(get("/v1/settings/notifications")).andExpect(status().isUnauthorized());
        mvc.perform(auth(get("/v1/channels/not-a-uuid"))).andExpect(status().isNotFound());
        mvc.perform(auth(get("/v1/channels/" + UUID.randomUUID()))).andExpect(status().isNotFound());
        String id = create("{'name':'n','type':'ntfy','config':{'topic':'t'}}");
        mvc.perform(auth(delete("/v1/channels/" + id))).andExpect(status().isNoContent());
        mvc.perform(auth(get("/v1/channels/" + id))).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("the type catalog lists all nineteen types (with 031's webpush, 060's mqtt and 084's meshtastic and"
            + " meshcore) with fields and capabilities")
    void types() throws Exception {
        String body = mvc.perform(auth(get("/v1/channels/types"))).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(19))).andReturn().getResponse().getContentAsString();
        JsonNode types = JSON.readTree(body);
        assertThat(types.findValuesAsString("id")).contains("webhook", "slack", "mattermost", "discord", "teams", "google-chat",
                "telegram", "matrix", "ntfy", "gotify", "pushover", "email", "homeassistant", "apprise", "zulip", "webpush",
                "mqtt", "meshtastic", "meshcore");
        for (JsonNode type : types) {
            if (type.get("id").asString().equals("webpush")) {
                assertThat(type.get("capabilities").get("fanOut").asBoolean()).isTrue();
                assertThat(type.get("configFields").get(0).get("name").asString()).isEqualTo("quietHoursMode");
            }
            if (type.get("id").asString().equals("discord")) {
                assertThat(type.get("capabilities").get("editInPlace").asBoolean()).isTrue();
                assertThat(type.get("secretFields").get(0).get("name").asString()).isEqualTo("webhookUrl");
            }
        }
    }

    @Test
    @DisplayName("test sends a synthetic message synchronously, reports failures redacted, and persists nothing")
    void testEndpoint() throws Exception {
        WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.post(urlEqualTo("/ok")).willReturn(ok()));
        WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.post(urlEqualTo("/bad/SECRETPATH"))
                .willReturn(aResponse().withStatus(403).withBody("denied for /bad/SECRETPATH")));
        String good = create("{'name':'w','type':'webhook','config':{'url':'" + url("/ok") + "'}}");
        String bad = create("{'name':'s','type':'slack','secrets':{'webhookUrl':'" + url("/bad/SECRETPATH") + "'}}");
        send(post("/v1/channels/" + good + "/test"), "").andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true))).andExpect(jsonPath("$.status", is(200)));
        send(post("/v1/channels/" + bad + "/test"), "").andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(false))).andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", not(containsString("SECRETPATH"))));
        mvc.perform(auth(get("/v1/channels/" + good + "/deliveries"))).andExpect(jsonPath("$.items", hasSize(0)));
        send(post("/v1/channels/" + UUID.randomUUID() + "/test"), "").andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deliveries: newest first, status filter, keyset pagination")
    void deliveries() throws Exception {
        String id = create("{'name':'w','type':'webhook','events':['system'],'config':{'url':'https://r.example.invalid'}}");
        for (int i = 0; i < 5; i++) {
            notifications.publish(NotificationMessage.builder("system", "system.test", "s" + i, "info").build(), null);
        }
        String first = mvc.perform(auth(get("/v1/channels/" + id + "/deliveries").param("limit", "2")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].kind", is("send")))
                .andExpect(jsonPath("$.items[0].status", is("pending")))
                .andExpect(jsonPath("$.items[0].edited", is(false)))
                .andReturn().getResponse().getContentAsString();
        JsonNode page = JSON.readTree(first);
        long newest = page.get("items").get(0).get("id").asLong();
        assertThat(page.get("items").get(1).get("id").asLong()).isLessThan(newest);
        String cursor = page.get("nextCursor").asString();
        mvc.perform(auth(get("/v1/channels/" + id + "/deliveries").param("limit", "2").param("cursor", cursor)))
                .andExpect(jsonPath("$.items", hasSize(2)));
        mvc.perform(auth(get("/v1/channels/" + id + "/deliveries").param("status", "failed")))
                .andExpect(jsonPath("$.items", hasSize(0))).andExpect(jsonPath("$.nextCursor", nullValue()));
        mvc.perform(auth(get("/v1/channels/" + id + "/deliveries").param("status", "bogus"))).andExpect(status().isBadRequest());
        mvc.perform(auth(get("/v1/channels/" + id + "/deliveries").param("cursor", "garbage"))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("notification settings round trip; omitted bypass is critical; explicit null bypasses nothing")
    void notificationSettings() throws Exception {
        mvc.perform(auth(get("/v1/settings/notifications"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.pausedUntil", nullValue())).andExpect(jsonPath("$.pauseBypassSeverity", is("critical")));
        send(put("/v1/settings/notifications"), "{'pausedUntil':'2026-05-01T18:00:00Z'}").andExpect(status().isOk())
                .andExpect(jsonPath("$.pausedUntil", is("2026-05-01T18:00:00Z")))
                .andExpect(jsonPath("$.pauseBypassSeverity", is("critical")));
        send(put("/v1/settings/notifications"), "{'pausedUntil':null,'pauseBypassSeverity':null,'chatops':{'enabled':true}}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.pauseBypassSeverity", nullValue()));
        mvc.perform(auth(get("/v1/settings/notifications"))).andExpect(jsonPath("$.pauseBypassSeverity", nullValue()));
        send(put("/v1/settings/notifications"), "{'pausedUntil':'tomorrow'}").andExpect(status().isBadRequest());
        send(put("/v1/settings/notifications"), "{'pauseBypassSeverity':'loud'}").andExpect(status().isBadRequest());
        send(put("/v1/settings/notifications"), "{'volume':'loud'}").andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getChannelInteractiveStatus (061): idle before start, embedded as Channel.interactive, 404 for other types,"
            + " invalid allowlists refused, the chatops kill switch stored")
    void interactiveStatus() throws Exception {
        String id = create("{'name':'tg','type':'telegram','config':{'chatId':'1','interactive':true,"
                + "'allowedChats':[{'chatId':'123','name':'me','scope':'agent'}]},'secrets':{'botToken':'T'}}");
        mvc.perform(auth(get("/v1/channels/" + id + "/interactive"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.interactive", is(true))).andExpect(jsonPath("$.chatopsEnabled", is(true)))
                .andExpect(jsonPath("$.connected", is(false))).andExpect(jsonPath("$.status", is("idle")))
                .andExpect(jsonPath("$.chats[0].chatId", is("123"))).andExpect(jsonPath("$.chats[0].scope", is("agent")))
                .andExpect(jsonPath("$.chats[0].active", is(false)));
        mvc.perform(auth(get("/v1/channels/" + id))).andExpect(status().isOk())
                .andExpect(jsonPath("$.interactive.status", is("idle")));
        String ntfy = create("{'name':'n','type':'ntfy','config':{'topic':'t'}}");
        mvc.perform(auth(get("/v1/channels/" + ntfy + "/interactive"))).andExpect(status().isNotFound());
        mvc.perform(auth(get("/v1/channels/" + ntfy))).andExpect(jsonPath("$.interactive").doesNotExist());
        mvc.perform(get("/v1/channels/" + id + "/interactive")).andExpect(status().isUnauthorized());

        send(post("/v1/channels"), "{'name':'g','type':'telegram','config':{'chatId':'1','interactive':true,"
                + "'allowedChats':[{'chatId':'-100','scope':'admin'}]},'secrets':{'botToken':'T'}}")
                .andExpect(status().isBadRequest())
                .andExpect(content -> assertThat(content.getResponse().getContentAsString())
                        .contains("allowedChats[0].scope").contains("allowedChats[0].userIds"));

        send(put("/v1/settings/notifications"), "{'chatops':{'enabled':false}}").andExpect(status().isOk())
                .andExpect(jsonPath("$.chatops.enabled", is(false)));
        mvc.perform(auth(get("/v1/channels/" + id + "/interactive"))).andExpect(jsonPath("$.chatopsEnabled", is(false)))
                .andExpect(jsonPath("$.statusReason", is("chatops.enabled is off")));
        send(put("/v1/settings/notifications"), "{'chatops':{'enabled':'no'}}").andExpect(status().isBadRequest());
    }
}
