package ai.foresignal.notify.mesh;

import ai.foresignal.mqtt.MqttConnection;
import ai.foresignal.mqtt.MqttConnectionSpec;
import ai.foresignal.mqtt.MqttConnections;
import ai.foresignal.mqtt.MqttEndpoint;
import ai.foresignal.mqtt.MqttMessage;
import ai.foresignal.mqtt.MqttProperties;
import ai.foresignal.mqtt.MqttSessionOptions;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.LanEndpoint;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Publishes Meshtastic JSON downlinks (prompt 084) through 060's {@link MqttConnections}: one client per broker
 * (broker URL and user), client id {@code foresignal-<installId>-mesh}, MQTT 3.1.1, a clean session and no Last Will
 * or status topic, so nothing but downlinks is ever published on the node's broker.
 *
 * <ul>
 *   <li>The broker is re-checked on every connect: it must still resolve to a LAN address. {@code mqtt://} connects
 *       to the pinned address; {@code mqtts://} connects by name for the TLS check.
 *   <li>A connection is opened on the first publish and reused; a dropped one is closed and reopened by the next
 *       publish (the delivery worker's retry ladder paces that).
 *   <li>Each downlink is QoS 1 and succeeds on the broker's PUBACK.
 * </ul>
 */
@Component
public class MeshtasticDownlinks {

    /** The prefix of every connection key. */
    public static final String KEY_PREFIX = "mesh:";

    private final MqttConnections connections;
    private final MqttProperties properties;
    private final InstallIdService installId;
    private final OutboundUrlPolicy policy;

    /**
     * Creates the publisher.
     *
     * @param connections the supervised connections
     * @param properties the connect and publish timeouts
     * @param installId the install id of the client id
     * @param policy re-checks that the broker is on the LAN
     */
    @Autowired
    public MeshtasticDownlinks(MqttConnections connections, MqttProperties properties, InstallIdService installId,
            OutboundUrlPolicy policy) {
        this.connections = connections;
        this.properties = properties;
        this.installId = installId;
        this.policy = policy;
    }

    /**
     * Publishes one downlink.
     *
     * @param config the channel's config ({@code brokerUrl})
     * @param secrets the channel's secrets ({@code username}, {@code password})
     * @return status 200 with body {@code PUBACK}
     * @throws IOException when the broker is not on the LAN, cannot be reached, refuses the message or does not
     *     acknowledge it in time
     */
    public ChannelResponse publish(Map<String, Object> config, Map<String, String> secrets, PreparedRequest.Mesh m)
            throws IOException {
        String url = String.valueOf(config.get("brokerUrl"));
        MeshLanUrls.Outcome outcome = MeshLanUrls.pin(url, Set.of("mqtt", "mqtts"), policy);
        if (!(outcome instanceof MeshLanUrls.Pinned pinned)) {
            throw new IOException("the broker is refused: " + ((MeshLanUrls.Refused) outcome).reason());
        }
        String username = secrets.get("username");
        String password = secrets.get("password");
        String key = key(url, username, password);
        MqttConnection connection = connections.find(key).filter(c -> !c.closed()).orElse(null);
        if (connection == null) {
            connection = open(key, pinned.endpoint(), username, password);
        }
        try {
            connection.connectedFuture().get(properties.connectTimeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted", e);
        } catch (ExecutionException | TimeoutException e) {
            connections.close(key);
            throw new IOException("the broker could not be reached: "
                    + (e.getCause() == null ? e.getMessage() : e.getCause().getMessage()));
        }
        try {
            connection.publish(new MqttMessage(m.topic(), m.payload(), 1, false, null, null))
                    .get(properties.publishTimeout().toMillis(), TimeUnit.MILLISECONDS);
            return new ChannelResponse(200, Map.of(), "PUBACK");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new IOException("the broker refused the downlink: " + cause.getMessage(), cause);
        } catch (TimeoutException e) {
            throw new IOException("no PUBACK within " + properties.publishTimeout().toSeconds() + " s");
        }
    }

    private synchronized MqttConnection open(String key, LanEndpoint endpoint, String username, String password) {
        MqttConnection existing = connections.find(key).filter(c -> !c.closed()).orElse(null);
        if (existing != null) {
            return existing;
        }
        boolean tls = endpoint.scheme() == LanEndpoint.Scheme.MQTTS;
        String host = tls ? endpoint.host() : endpoint.address().getHostAddress();
        MqttEndpoint broker = new MqttEndpoint(tls ? "mqtts" : "mqtt", host, endpoint.port(), tls, false, "");
        MqttConnectionSpec spec = new MqttConnectionSpec(key, "mesh-" + endpoint.host(), clientId(installId.installId()),
                broker, MqttConnectionSpec.V311, username, password, null, null,
                new MqttSessionOptions(null, true, 1, false));
        MqttConnection connection = connections.create(spec);
        connection.onDisconnected(reason -> {
            // Close only this connection: a newer one may already hold the key.
            if (connections.find(key).orElse(null) == connection) {
                connections.close(key);
            }
        });
        connection.start();
        return connection;
    }

    /** The client id of every mesh downlink connection: {@code foresignal-<installId>-mesh}. */
    public static String clientId(String installId) {
        return "foresignal-" + installId + "-mesh";
    }

    /** The connection key of a broker and user: {@code mesh:} and 16 hex digits of their SHA-256. */
    static String key(String url, String username, String password) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            for (String part : new String[] {url.strip(), username, password}) {
                sha.update((part == null ? "" : part).getBytes(StandardCharsets.UTF_8));
                sha.update((byte) 0);
            }
            return KEY_PREFIX + HexFormat.of().formatHex(sha.digest(), 0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
