package ai.foresignal.testsupport.mqtt;

import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.toxiproxy.ToxiproxyContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * The MQTT broker of the prompt 060 integration tests: one {@code eclipse-mosquitto:2} container per JVM with
 * an anonymous listener (1883), a password listener (1884, {@code fs}/{@code s3cret}) and a TLS listener
 * (8883, the test CA of {@code src/test/resources/mqtt/certs}), plus a Toxiproxy in front of the anonymous
 * listener, so a test can cut the client's connection without touching the broker (the broker then publishes
 * the Last Will). Both are started lazily on first use and removed by Testcontainers' Ryuk when the JVM exits.
 */
public final class MosquittoSupport {

    /** The broker image the prompt names. */
    public static final DockerImageName MOSQUITTO = DockerImageName.parse("eclipse-mosquitto:2");

    /** Toxiproxy, pinned. */
    public static final DockerImageName TOXIPROXY = DockerImageName.parse("ghcr.io/shopify/toxiproxy:2.12.0");

    /** The password listener's user. */
    public static final String USER = "fs";

    /** The password listener's password. */
    public static final String PASSWORD = "s3cret";

    /** The first port Toxiproxy proxies. */
    static final int PROXY_PORT = 8666;

    private static MosquittoSupport instance;

    private final GenericContainer<?> broker;
    private final ToxiproxyContainer toxiproxy;
    private final Proxy proxy;

    @SuppressWarnings("resource")
    private MosquittoSupport() {
        Network network = Network.newNetwork();
        broker = new GenericContainer<>(MOSQUITTO)
                .withNetwork(network)
                .withNetworkAliases("mosquitto")
                .withExposedPorts(1883, 1884, 8883)
                .withCopyFileToContainer(MountableFile.forClasspathResource("mqtt/mosquitto.conf"),
                        "/mosquitto/config/mosquitto.conf")
                .withCopyFileToContainer(MountableFile.forClasspathResource("mqtt/certs/ca.crt"),
                        "/mosquitto/certs/ca.crt")
                .withCopyFileToContainer(MountableFile.forClasspathResource("mqtt/certs/server.crt"),
                        "/mosquitto/certs/server.crt")
                .withCopyFileToContainer(MountableFile.forClasspathResource("mqtt/certs/server.key"),
                        "/mosquitto/certs/server.key")
                .withCommand("sh", "-c", "mosquitto_passwd -c -b /tmp/passwd " + USER + " " + PASSWORD
                        + " && chown mosquitto:mosquitto /tmp/passwd && chmod 0600 /tmp/passwd"
                        + " && chown -R mosquitto:mosquitto /mosquitto/certs && chmod 0600 /mosquitto/certs/server.key"
                        + " && exec /usr/sbin/mosquitto -c /mosquitto/config/mosquitto.conf")
                .waitingFor(Wait.forLogMessage(".*running.*", 1));
        broker.start();
        toxiproxy = new ToxiproxyContainer(TOXIPROXY).withNetwork(network);
        toxiproxy.start();
        try {
            proxy = new ToxiproxyClient(toxiproxy.getHost(), toxiproxy.getControlPort())
                    .createProxy("mqtt", "0.0.0.0:" + PROXY_PORT, "mosquitto:1883");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** The shared broker, started on first use. */
    public static synchronized MosquittoSupport get() {
        if (instance == null) {
            instance = new MosquittoSupport();
        }
        return instance;
    }

    /** The host the test JVM reaches the containers on. */
    public String host() {
        return broker.getHost();
    }

    /** {@code mqtt://host:port} of the anonymous listener. */
    public String plainUrl() {
        return "mqtt://" + host() + ":" + broker.getMappedPort(1883);
    }

    /** {@code mqtt://host:port} of the password listener. */
    public String passwordUrl() {
        return "mqtt://" + host() + ":" + broker.getMappedPort(1884);
    }

    /** {@code mqtts://host:port} of the TLS listener. */
    public String tlsUrl() {
        return "mqtts://" + host() + ":" + broker.getMappedPort(8883);
    }

    /** {@code mqtt://host:port} through Toxiproxy to the anonymous listener. */
    public String proxiedUrl() {
        return "mqtt://" + toxiproxy.getHost() + ":" + toxiproxy.getMappedPort(PROXY_PORT);
    }

    /** The anonymous listener's mapped port. */
    public int plainPort() {
        return broker.getMappedPort(1883);
    }

    /** Cuts every proxied connection and refuses new ones (the broker sees the client vanish). */
    public void cut() {
        try {
            proxy.disable();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Lets proxied connections through again. */
    public void restore() {
        try {
            proxy.enable();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** The test CA certificate in PEM. */
    public static String caPem() {
        try (InputStream in = MosquittoSupport.class.getResourceAsStream("/mqtt/certs/ca.crt")) {
            return new String(in.readAllBytes(), StandardCharsets.US_ASCII);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
