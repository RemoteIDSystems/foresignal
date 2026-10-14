package ai.foresignal.api;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.notify.NotifyTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code mqtt} channel type over HTTP without a broker (prompt 060): its catalog entry with field specs,
 * the {@code MQTT_INSECURE_PUBLIC_BROKER} warning, and the refusals of {@code republishMqttChannel}.
 */
@IntegrationTest
@Import(NotifyTestConfig.class)
class ChannelTypesCatalogIT {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;

    String token;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        token = adminTokens.rotate();
    }

    MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    ResultActions send(MockHttpServletRequestBuilder request, String body) throws Exception {
        return mvc.perform(auth(request).contentType(MediaType.APPLICATION_JSON).content(body.replace('\'', '"')));
    }

    @Test
    @DisplayName("mqtt is listed with its config and secret field specs and is not a fan-out type")
    void catalogEntry() throws Exception {
        mvc.perform(auth(get("/v1/channels/types"))).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == 'mqtt')].name", hasItem("MQTT")))
                .andExpect(jsonPath("$[?(@.id == 'mqtt')].configFields[?(@.name == 'brokerUrl')].required",
                        hasItem(true)))
                .andExpect(jsonPath("$[?(@.id == 'mqtt')].configFields[?(@.name == 'topicPrefix')].default",
                        hasItem("foresignal")))
                .andExpect(jsonPath("$[?(@.id == 'mqtt')].configFields[?(@.name == 'protocolVersion')].allowed[*]",
                        containsInAnyOrder("5", "3.1.1")))
                .andExpect(jsonPath("$[?(@.id == 'mqtt')].configFields[?(@.name == 'haDiscovery')].default",
                        hasItem(false)))
                .andExpect(jsonPath("$[?(@.id == 'mqtt')].secretFields[*].name",
                        containsInAnyOrder("username", "password", "caCertPem")))
                .andExpect(jsonPath("$[?(@.id == 'mqtt')].capabilities.fanOut", hasItem(false)));
    }

    @Test
    @DisplayName("084: meshtastic and meshcore are listed with their config fields; neither edits in place")
    void meshCatalog() throws Exception {
        mvc.perform(auth(get("/v1/channels/types"))).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == 'meshtastic')].configFields[?(@.name == 'privateChannelConfirmed')]"
                        + ".required", hasItem(true)))
                .andExpect(jsonPath("$[?(@.id == 'meshtastic')].configFields[?(@.name == 'dutyCyclePercent')].type",
                        hasItem("number")))
                .andExpect(jsonPath("$[?(@.id == 'meshtastic')].configFields[?(@.name == 'airtimePreset')].default",
                        hasItem("meshtastic-longfast")))
                .andExpect(jsonPath("$[?(@.id == 'meshtastic')].secretFields[*].name",
                        containsInAnyOrder("username", "password")))
                .andExpect(jsonPath("$[?(@.id == 'meshcore')].configFields[?(@.name == 'airtimePreset')].default",
                        hasItem("custom")))
                .andExpect(jsonPath("$[?(@.id == 'meshcore')].configFields[?(@.name == 'companionUrl')].required",
                        hasItem(true)))
                .andExpect(jsonPath("$[?(@.id == 'meshcore')].capabilities.editInPlace", hasItem(false)))
                .andExpect(jsonPath("$[?(@.id == 'meshtastic')].capabilities.editInPlace", hasItem(false)));
    }

    @Test
    @DisplayName("084: a public Meshtastic channel is 400 MESH_PUBLIC_CHANNEL; the airtime window of a private one is"
            + " served, and 404 for another type")
    void meshPublicAndAirtime() throws Exception {
        send(post("/v1/channels"), "{'name':'pub','type':'meshtastic','enabled':false,'config':{'brokerUrl':"
                + "'mqtt://192.168.1.20:1883','gatewayNodeNum':'1','channelIndex':1,'channelName':'LongFast',"
                + "'privateChannelConfirmed':true,'region':'EU_868'}}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("MESH_PUBLIC_CHANNEL")));
        String created = send(post("/v1/channels"), "{'name':'mesh','type':'meshtastic','enabled':false,"
                + "'config':{'brokerUrl':'mqtt://192.168.1.20:1883','gatewayNodeNum':'1','channelIndex':1,"
                + "'channelName':'Family','privateChannelConfirmed':true,'region':'EU_868','dutyCyclePercent':2}}")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = JSON.readTree(created).get("id").asString();
        mvc.perform(auth(get("/v1/channels/" + id + "/airtime"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.windowSeconds", is(3600)))
                .andExpect(jsonPath("$.limitSeconds", is(72.0)))
                .andExpect(jsonPath("$.dutyCyclePercent", is(2.0)))
                .andExpect(jsonPath("$.queued", is(0)))
                .andExpect(jsonPath("$.usedSeconds", is(0.0)));
        String webhook = send(post("/v1/channels"), "{'name':'w','type':'webhook','enabled':false,'config':{'url':"
                + "'https://hooks.example.invalid/x'}}").andExpect(status().isCreated()).andReturn().getResponse()
                .getContentAsString();
        mvc.perform(auth(get("/v1/channels/" + JSON.readTree(webhook).get("id").asString() + "/airtime")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("a public broker without TLS is saved with the MQTT_INSECURE_PUBLIC_BROKER warning")
    void insecureWarning() throws Exception {
        // Disabled, so nothing tries to reach the public address.
        String response = send(post("/v1/channels"), "{'name':'pub','type':'mqtt','enabled':false,"
                + "'config':{'brokerUrl':'mqtt://8.8.8.8:1883'}}").andExpect(status().isCreated())
                .andExpect(jsonPath("$.warnings[0].code", is("MQTT_INSECURE_PUBLIC_BROKER")))
                .andReturn().getResponse().getContentAsString();
        JsonNode channel = JSON.readTree(response);
        mvc.perform(auth(get("/v1/channels/" + channel.get("id").asString()))).andExpect(status().isOk())
                .andExpect(jsonPath("$.warnings[0].code", is("MQTT_INSECURE_PUBLIC_BROKER")));
        send(post("/v1/channels"), "{'name':'lan','type':'mqtt','enabled':false,"
                + "'config':{'brokerUrl':'mqtt://192.168.1.20'}}").andExpect(status().isCreated())
                .andExpect(jsonPath("$.warnings").doesNotExist());
    }

    @Test
    @DisplayName("republish: 404 for an unknown or non-mqtt channel, 409 CHANNEL_DISABLED for a disabled one")
    void republishRefusals() throws Exception {
        mvc.perform(auth(post("/v1/channels/00000000-0000-0000-0000-000000000099/mqtt/republish")))
                .andExpect(status().isNotFound());
        String webhook = send(post("/v1/channels"), "{'name':'w','type':'webhook','config':{'url':"
                + "'https://hooks.example.invalid/x'}}").andExpect(status().isCreated()).andReturn().getResponse()
                .getContentAsString();
        mvc.perform(auth(post("/v1/channels/" + JSON.readTree(webhook).get("id").asString() + "/mqtt/republish")))
                .andExpect(status().isNotFound());
        String disabled = send(post("/v1/channels"), "{'name':'off','type':'mqtt','enabled':false,"
                + "'config':{'brokerUrl':'mqtt://192.168.1.20'}}").andExpect(status().isCreated()).andReturn()
                .getResponse().getContentAsString();
        mvc.perform(auth(post("/v1/channels/" + JSON.readTree(disabled).get("id").asString() + "/mqtt/republish")))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code", is("CHANNEL_DISABLED")));
        mvc.perform(post("/v1/channels/00000000-0000-0000-0000-000000000099/mqtt/republish"))
                .andExpect(status().isUnauthorized());
    }
}
