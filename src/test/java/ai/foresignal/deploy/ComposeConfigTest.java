package ai.foresignal.deploy;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * The {@code web} compose profile of prompt 050, parsed rather than run: Caddy only under that profile,
 * pinned, with HTTP/3 on UDP 443, its data and config volumes and the read-only Caddyfile mount; and
 * the Caddyfile itself: {@code tls internal}, SSE without buffering, the API routes and the same-origin
 * web client.
 */
class ComposeConfigTest {

    private static final Path COMPOSE = Path.of("docker-compose.yml");
    private static final Path CADDYFILE = Path.of("deploy", "caddy", "Caddyfile");

    @SuppressWarnings("unchecked")
    private static Map<String, Object> compose() throws IOException {
        try (InputStream in = Files.newInputStream(COMPOSE)) {
            return (Map<String, Object>) new Yaml(new SafeConstructor(new LoaderOptions())).load(in);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> caddy() throws IOException {
        Map<String, Object> services = (Map<String, Object>) compose().get("services");
        assertThat(services).containsKey("caddy");
        return (Map<String, Object>) services.get("caddy");
    }

    @Test
    @DisplayName("caddy runs only under the web profile, from the pinned alpine image")
    @SuppressWarnings("unchecked")
    void profileAndImage() throws IOException {
        Map<String, Object> caddy = caddy();
        assertThat((List<Object>) caddy.get("profiles")).containsExactly("web");
        assertThat(String.valueOf(caddy.get("image"))).isEqualTo("caddy:2.11.7-alpine");
        assertThat(caddy.get("depends_on").toString()).contains("foresignal");
    }

    @Test
    @DisplayName("caddy publishes 80, 443/tcp and 443/udp for HTTP/3")
    @SuppressWarnings("unchecked")
    void ports() throws IOException {
        List<String> ports = ((List<Object>) caddy().get("ports")).stream().map(String::valueOf).toList();
        assertThat(ports).containsExactlyInAnyOrder("80:80", "443:443/tcp", "443:443/udp");
    }

    @Test
    @DisplayName("caddy mounts its volumes, the Caddyfile read-only and the optional web client")
    @SuppressWarnings("unchecked")
    void volumes() throws IOException {
        List<String> volumes = ((List<Object>) caddy().get("volumes")).stream().map(String::valueOf).toList();
        assertThat(volumes).contains("caddy-data:/data", "caddy-config:/config",
                "./deploy/caddy/Caddyfile:/etc/caddy/Caddyfile:ro", "./deploy/web:/srv/web:ro");
        assertThat((Map<String, Object>) compose().get("volumes")).containsKeys("caddy-data", "caddy-config");
        assertThat(Files.isDirectory(Path.of("deploy", "web"))).as("the web client directory exists").isTrue();
    }

    @Test
    @DisplayName("the Caddyfile uses tls internal, proxies the API without buffering and serves the web client")
    void caddyfile() throws IOException {
        String caddyfile = Files.readString(CADDYFILE);
        assertThat(caddyfile)
                .contains("{$FORESIGNAL_HOSTNAME:foresignal.local} {")
                .contains("\ttls internal")
                .contains("xcaddy")
                .contains("@foresignal path /v1/* /mcp* /actuator/health*")
                .contains("reverse_proxy foresignal:8080")
                .contains("flush_interval -1")
                .contains("encode zstd gzip")
                .contains("root * /srv/web")
                .contains("try_files {path} /index.html")
                .contains("file_server")
                .contains("FORESIGNAL_TRUSTED_PROXIES");
        // PNG tiles are never re-encoded, and SSE is never compressed.
        assertThat(caddyfile).doesNotContain("header Content-Type image/png")
                .doesNotContain("header Content-Type text/event-stream")
                .doesNotContain("header Content-Type text/*");
        // The log filter keeps only the first eight characters of a path key.
        assertThat(caddyfile).contains("(/v1/k/[^/?]{8})[^/?]*");
    }
}
