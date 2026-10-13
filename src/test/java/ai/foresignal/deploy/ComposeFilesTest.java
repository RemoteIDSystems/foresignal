package ai.foresignal.deploy;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * The deployment files of prompt 021, parsed rather than run: {@code docker-compose.yml} keeps its
 * localhost binds, its readiness healthcheck and the opt-in {@code tailscale} profile, the Tailscale
 * Serve file proxies to the broker, and the Avahi service file advertises no stable identifier.
 */
class ComposeFilesTest {

    private static final Path COMPOSE = Path.of("docker-compose.yml");
    private static final Path AVAHI = Path.of("docker", "avahi", "foresignal.service");
    private static final Path SERVE = Path.of("docker", "tailscale", "serve.json");
    private static final Path CADDY = Path.of("docker", "caddy", "Caddyfile.example");

    /** {@code ${VAR:-default}} at the start of a port mapping. */
    private static final Pattern BIND = Pattern.compile("^\\$\\{([A-Z_]+):-([^}]*)}:(\\d+):(\\d+)$");

    /** The install id's shape (8 characters of {@code [a-z0-9]}, {@code InstallIdService}). */
    private static final Pattern INSTALL_ID = Pattern.compile("(?<![a-z0-9])[a-z0-9]{8}(?![a-z0-9])");

    @SuppressWarnings("unchecked")
    private static Map<String, Object> compose() throws IOException {
        try (InputStream in = Files.newInputStream(COMPOSE)) {
            return (Map<String, Object>) new Yaml(new SafeConstructor(new LoaderOptions())).load(in);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> service(Map<String, Object> compose, String name) {
        Map<String, Object> services = (Map<String, Object>) compose.get("services");
        assertThat(services).as("services").containsKey(name);
        return (Map<String, Object>) services.get(name);
    }

    @Test
    @DisplayName("every published port binds to 127.0.0.1 by default, through an overridable variable")
    @SuppressWarnings("unchecked")
    void defaultBindsAreLocalhost() throws IOException {
        Map<String, Object> compose = compose();
        Map<String, Object> services = (Map<String, Object>) compose.get("services");
        List<String> mappings = new ArrayList<>();
        services.forEach((name, s) -> {
            // The opt-in web profile's Caddy (050) exists to serve LAN map clients on 80/443; its
            // ports are asserted by ComposeConfigTest.
            if ("caddy".equals(name)) {
                return;
            }
            Object ports = ((Map<String, Object>) s).get("ports");
            if (ports != null) {
                ((List<Object>) ports).forEach(p -> mappings.add(String.valueOf(p)));
            }
        });
        // foresignal, db and (026) the optional ollama service under its local-llm profile (checkpoint 041).
        assertThat(mappings).hasSize(3);
        for (String mapping : mappings) {
            Matcher m = BIND.matcher(mapping);
            assertThat(m.matches()).as("port mapping %s carries a bind variable", mapping).isTrue();
            assertThat(m.group(2)).as("default bind of %s", mapping).isEqualTo("127.0.0.1");
        }
        assertThat(service(compose, "foresignal").get("ports").toString()).contains("FORESIGNAL_BIND").contains(":8080:8080");
        assertThat(service(compose, "db").get("ports").toString()).contains("FORESIGNAL_DB_BIND").contains(":5433:5432");
        assertThat(service(compose, "ollama").get("ports").toString()).contains("FORESIGNAL_BIND").contains(":11434:11434");
    }

    @Test
    @DisplayName("the tailscale service exists only under its profile, pinned, with state and serve config")
    @SuppressWarnings("unchecked")
    void tailscaleOnlyUnderProfile() throws IOException {
        Map<String, Object> compose = compose();
        Map<String, Object> tailscale = service(compose, "tailscale");
        assertThat((List<Object>) tailscale.get("profiles")).containsExactly("tailscale");
        assertThat(String.valueOf(tailscale.get("image"))).matches("tailscale/tailscale:v\\d+\\.\\d+\\.\\d+");
        assertThat(tailscale).doesNotContainKey("ports").doesNotContainKey("hostname");
        Map<String, Object> env = (Map<String, Object>) tailscale.get("environment");
        assertThat(env).containsEntry("TS_HOSTNAME", "foresignal")
                .containsEntry("TS_STATE_DIR", "/var/lib/tailscale")
                .containsEntry("TS_SERVE_CONFIG", "/config/serve.json");
        assertThat(String.valueOf(env.get("TS_AUTHKEY"))).startsWith("${TS_AUTHKEY");
        assertThat((Map<String, Object>) compose.get("volumes")).containsKey("tailscale-state");

        Map<String, Object> services = (Map<String, Object>) compose.get("services");
        // Only the optional services carry a profile: tailscale, (026) ollama under local-llm (checkpoint 041),
        // (049) satdump and (050) caddy under web.
        services.forEach((name, s) -> {
            Object profiles = ((Map<String, Object>) s).get("profiles");
            switch (name) {
                case "tailscale" -> { }
                case "ollama" -> assertThat((List<Object>) profiles).as("profiles of ollama").containsExactly("local-llm");
                case "satdump" -> assertThat((List<Object>) profiles).as("profiles of satdump").containsExactly("satdump");
                case "caddy" -> assertThat((List<Object>) profiles).as("profiles of caddy").containsExactly("web");
                default -> assertThat(profiles).as("profiles of %s", name).isNull();
            }
        });
    }

    @Test
    @DisplayName("foresignal has a readiness healthcheck")
    @SuppressWarnings("unchecked")
    void readinessHealthcheck() throws IOException {
        Map<String, Object> healthcheck = (Map<String, Object>) service(compose(), "foresignal").get("healthcheck");
        assertThat(healthcheck).isNotNull();
        assertThat(healthcheck.get("test").toString()).contains("/actuator/health/readiness");
    }

    @Test
    @DisplayName("Tailscale Serve proxies HTTPS 443 on the tailnet name to the broker, without Funnel")
    void serveConfig() throws IOException {
        String serve = Files.readString(SERVE);
        assertThat(serve).contains("\"443\"").contains("\"HTTPS\": true")
                .contains("${TS_CERT_DOMAIN}:443").contains("\"Proxy\": \"http://foresignal:8080\"")
                .contains("\"AllowFunnel\"");
        assertThat(serve.replaceAll("\\s", "")).contains("\"${TS_CERT_DOMAIN}:443\":false");
    }

    @Test
    @DisplayName("the Caddy example disables buffering for SSE and points at the trusted-proxy setting")
    void caddyExample() throws IOException {
        String caddy = Files.readString(CADDY);
        assertThat(caddy).contains("reverse_proxy").contains("flush_interval -1").contains("tls internal").contains("023");
    }

    @Test
    @DisplayName("the Avahi file advertises _http._tcp and _foresignal._tcp on 8080 with path and api TXT only")
    void avahiCarriesNoIdentifier() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setExpandEntityReferences(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document;
        try (InputStream in = Files.newInputStream(AVAHI)) {
            document = builder.parse(in);
        }
        NodeList services = document.getElementsByTagName("service");
        assertThat(services.getLength()).isEqualTo(2);
        List<String> types = new ArrayList<>();
        for (int i = 0; i < services.getLength(); i++) {
            Element service = (Element) services.item(i);
            types.add(text(service, "type"));
            assertThat(text(service, "port")).isEqualTo("8080");
            NodeList txt = service.getElementsByTagName("txt-record");
            List<String> records = new ArrayList<>();
            for (int j = 0; j < txt.getLength(); j++) {
                String record = txt.item(j).getTextContent().trim();
                records.add(record);
                String value = record.substring(record.indexOf('=') + 1);
                assertThat(INSTALL_ID.matcher(value).find()).as("TXT %s looks like an install id", record).isFalse();
                assertThat(record.toLowerCase()).doesNotContain("install").doesNotContain("uuid").doesNotContain("serial");
            }
            assertThat(records).containsExactlyInAnyOrder("path=/v1", "api=v1");
        }
        assertThat(types).containsExactlyInAnyOrder("_http._tcp", "_foresignal._tcp");
    }

    private static String text(Element parent, String tag) {
        return parent.getElementsByTagName(tag).item(0).getTextContent().trim();
    }
}
