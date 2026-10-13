package ai.foresignal.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** Parsing of {@code ServerSettings} bodies and the deep-link templates (prompt 023). */
class ServerSettingsServiceTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static ServerSettings parse(String text) {
        return ServerSettingsService.parse(JSON.readTree(text));
    }

    @Test
    @DisplayName("a full body parses; warnings in the request are ignored")
    void parses() {
        ServerSettings value = parse("""
                {"publicBaseUrl":"https://fs.example","timezone":"Europe/London","warnings":[],
                 "clientLinks":{"alert":"https://fs.example/a/{id}","pair":"foresignal://pair?u={publicBaseUrl}&c={code}"}}
                """);

        assertThat(value.publicBaseUrl()).isEqualTo("https://fs.example");
        assertThat(value.timezone()).isEqualTo("Europe/London");
        assertThat(value.clientLinks().alert()).isEqualTo("https://fs.example/a/{id}");
        assertThat(value.effectiveClientLinks().record()).isEqualTo(ClientLinks.DEFAULT_RECORD);
        assertThat(parse("{\"publicBaseUrl\":null}")).isEqualTo(ServerSettings.defaults());
        assertThat(parse("{\"publicBaseUrl\":\"\"}").publicBaseUrl()).isNull();
    }

    @Test
    @DisplayName("bad URLs, zones and templates, a missing publicBaseUrl and bad push settings are refused")
    void refuses() {
        for (String bad : new String[] {"{}", "[]", "{\"publicBaseUrl\":\"ftp://x\"}", "{\"publicBaseUrl\":\"https://\"}",
            "{\"publicBaseUrl\":\"https://u:p@x\"}", "{\"publicBaseUrl\":\"https://x/?q=1\"}", "{\"publicBaseUrl\":5}",
            "{\"publicBaseUrl\":null,\"timezone\":\"Mars/Olympus\"}", "{\"publicBaseUrl\":null,\"timezone\":5}",
            "{\"publicBaseUrl\":null,\"clientLinks\":{\"alert\":\"http://x/{id}\"}}",
            "{\"publicBaseUrl\":null,\"clientLinks\":{\"alert\":\"javascript:alert(1)\"}}",
            "{\"publicBaseUrl\":null,\"clientLinks\":{\"other\":\"foresignal://x\"}}",
            "{\"publicBaseUrl\":null,\"clientLinks\":[]}",
            "{\"publicBaseUrl\":null,\"push\":[]}", "{\"publicBaseUrl\":null,\"push\":{\"x\":1}}",
            "{\"publicBaseUrl\":null,\"push\":{\"contact\":\"admin@x\"}}",
            "{\"publicBaseUrl\":null,\"push\":{\"contact\":\"http://x.example\"}}",
            "{\"publicBaseUrl\":null,\"push\":{\"allowedPrivateHosts\":[\"*.lan\"]}}",
            "{\"publicBaseUrl\":null,\"push\":{\"allowedPrivateHosts\":[\"10.0.0.0/7\"]}}",
            "{\"publicBaseUrl\":null,\"push\":{\"allowedPrivateHosts\":[\"fd00::/31\"]}}",
            "{\"publicBaseUrl\":null,\"push\":{\"allowedPrivateHosts\":[\"10.0.0.0/33\"]}}",
            "{\"publicBaseUrl\":null,\"push\":{\"allowedPrivateHosts\":[\"bad host\"]}}",
            "{\"publicBaseUrl\":null,\"push\":{\"defaultLang\":\"english!\"}}",
            "{\"publicBaseUrl\":null,\"colour\":1}"}) {
            assertThatThrownBy(() -> parse(bad)).as(bad).isInstanceOf(ValidationException.class);
        }
    }

    @Test
    @DisplayName("push settings (031): contact, allowlist of exact hosts and CIDRs, default language; defaults")
    void push() {
        ServerSettings value = parse("""
                {"publicBaseUrl":null,"push":{"contact":"mailto:ops@fs.example",
                 "allowedPrivateHosts":["Distributor.LAN","192.168.0.0/16","10.0.0.0/8","fd00::/32","192.168.1.5"],
                 "defaultLang":"pt-BR"}}
                """);
        assertThat(value.push().contact()).isEqualTo("mailto:ops@fs.example");
        assertThat(value.push().allowedPrivateHosts())
                .containsExactly("distributor.lan", "192.168.0.0/16", "10.0.0.0/8", "fd00::/32", "192.168.1.5");
        assertThat(value.push().defaultLang()).isEqualTo("pt-BR");
        assertThat(parse("{\"publicBaseUrl\":null,\"push\":{\"contact\":\"https://fs.example/contact\"}}")
                .push().contact()).isEqualTo("https://fs.example/contact");
        PushSettings defaults = ServerSettings.defaults().effectivePush();
        assertThat(defaults.contact()).isEqualTo("mailto:admin@localhost");
        assertThat(defaults.allowedPrivateHosts()).isEmpty();
        assertThat(defaults.defaultLang()).isEqualTo("en");
    }

    @Test
    @DisplayName("templates fill their placeholders; defaults use foresignal://")
    void clientLinks() {
        assertThat(ClientLinks.defaults().alert()).startsWith("foresignal://");
        assertThat(ClientLinks.fill(ClientLinks.DEFAULT_ALERT, Map.of("id", "42"))).isEqualTo("foresignal://alerts/42");
        assertThat(ClientLinks.fill(ClientLinks.DEFAULT_RECORD, Map.of("source", "usgs", "externalId", "us1")))
                .isEqualTo("foresignal://records/usgs/us1");
        assertThat(new ClientLinks(null, "x", null, null).withDefaults().alert()).isEqualTo(ClientLinks.DEFAULT_ALERT);
    }

    @Test
    @DisplayName("contactEmail (prompt 047): an address is kept, null clears it, anything else is refused")
    void contactEmail() {
        assertThat(parse("{\"publicBaseUrl\":null,\"contactEmail\":\" ops@example.org \"}").contactEmail())
                .isEqualTo("ops@example.org");
        assertThat(parse("{\"publicBaseUrl\":null,\"contactEmail\":null}").contactEmail()).isNull();
        assertThat(parse("{\"publicBaseUrl\":null}").normalizedContactEmail()).isNull();
        for (String bad : new String[] {"\"not-an-email\"", "5", "\"a@b\"", "\"a b@example.org\"",
            "\"ops@example.org; install=x\"", "\"(x)@example.org\""}) {
            assertThatThrownBy(() -> parse("{\"publicBaseUrl\":null,\"contactEmail\":" + bad + "}")).as(bad)
                    .isInstanceOf(ValidationException.class);
        }
    }
}
