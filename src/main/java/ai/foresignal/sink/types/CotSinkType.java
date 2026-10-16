package ai.foresignal.sink.types;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.sink.CotEncoder;
import ai.foresignal.sink.ServeGuard;
import ai.foresignal.sink.SinkConfig;
import ai.foresignal.sink.SinkInstance;
import ai.foresignal.sink.SinkKind;
import ai.foresignal.sink.SinkMode;
import ai.foresignal.sink.SinkRecord;
import ai.foresignal.sink.SinkSupport;
import ai.foresignal.sink.SinkTcpServer;
import ai.foresignal.sink.SinkType;
import ai.foresignal.sink.SinkTypeSpec;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * {@code cot} (prompt 110; STATE_MIRROR, PUSH or SERVE): Cursor-on-Target XML (protocol version 0).
 * PUSH sends UDP multicast SA (default {@code 239.2.3.1:6969}, LAN only); SERVE listens on TCP for
 * direct ATAK/iTAK input. Affiliation is neutral ({@code u}) or unknown ({@code n}) only.
 */
@Component
public class CotSinkType implements SinkType {

    /** The type id. */
    public static final String ID = "cot";

    /** The default multicast group. */
    public static final String DEFAULT_GROUP = "239.2.3.1";

    /** The default multicast port. */
    public static final int DEFAULT_PORT = 6969;

    private static final SinkTypeSpec SPEC = new SinkTypeSpec(ID, "Cursor-on-Target",
            "CoT XML for TAK (ATAK/iTAK/WinTAK); PUSH UDP multicast or SERVE TCP", SinkKind.STATE_MIRROR,
            Set.of(SinkMode.PUSH, SinkMode.SERVE),
            Map.of("type", "object", "properties", Map.of(
                    "mode", Map.of("type", "string", "enum", List.of("PUSH", "SERVE")),
                    "group", Map.of("type", "string", "default", DEFAULT_GROUP),
                    "port", Map.of("type", "integer", "default", DEFAULT_PORT),
                    "affiliation", Map.of("type", "string", "enum", List.of("u", "n"), "default", "u"),
                    "stale", Map.of("type", "integer", "default", 120),
                    "includeAlerts", Map.of("type", "boolean", "default", false),
                    "bind", Map.of("type", "string", "default", "127.0.0.1"),
                    "allowAllInterfaces", Map.of("type", "boolean", "default", false),
                    "allowTailnet", Map.of("type", "boolean", "default", false),
                    "maxClients", Map.of("type", "integer", "default", 8))),
            List.of(), 50);

    private final ServeGuard guard;
    private final String instanceId;

    /** Creates the type. */
    public CotSinkType(ServeGuard guard,
            @org.springframework.beans.factory.annotation.Value("${foresignal.instance-id:foresignal}") String instanceId) {
        this.guard = guard;
        this.instanceId = instanceId == null || instanceId.isBlank() ? "foresignal" : instanceId;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public SinkTypeSpec spec() {
        return SPEC;
    }

    @Override
    public void validate(Map<String, Object> config, Map<String, String> secrets) {
        String affiliation = config == null ? null : String.valueOf(config.getOrDefault("affiliation", "u"));
        if (!"u".equals(affiliation) && !"n".equals(affiliation)) {
            throw ValidationException.forField("config.affiliation", "must be u (neutral) or n (unknown)");
        }
        String mode = config == null ? "PUSH" : String.valueOf(config.getOrDefault("mode", "PUSH"));
        if ("SERVE".equalsIgnoreCase(mode)) {
            ServeConfig.validate(guard, config);
        }
    }

    @Override
    public SinkInstance open(SinkConfig config) {
        String affiliation = config.config("affiliation", "u");
        int stale = config.intConfig("stale", 120);
        if ("SERVE".equalsIgnoreCase(config.config("mode", "PUSH"))) {
            ServeConfig serve = ServeConfig.of(guard, config);
            SinkTcpServer server = new SinkTcpServer(guard, serve.bind(), serve.port(), serve.maxClients(),
                    serve.allowTailnet(), new SinkTcpServer.Handler() {
                        @Override
                        public void onConnect(SinkTcpServer.Client client) {
                        }
                    });
            return new ServeInstance(server, affiliation, stale, instanceId);
        }
        return new MulticastInstance(config.config("group", DEFAULT_GROUP), config.intConfig("port", DEFAULT_PORT),
                affiliation, stale, instanceId);
    }

    static String event(SinkRecord record, String affiliation, int staleSeconds, String instanceId) {
        String uid = "FORESIGNAL." + instanceId.substring(0, Math.min(8, instanceId.length())) + "."
                + record.sourceId() + "." + record.externalId();
        Instant time = SinkSupport.time(record);
        Instant stale = record.validUntil() != null ? record.validUntil() : time.plus(Duration.ofSeconds(staleSeconds));
        String remarks = record.string("title");
        String attribution = record.attribution();
        String body = attribution == null ? remarks : (remarks == null ? attribution : remarks + " \u2022 " + attribution);
        return CotEncoder.event(uid, CotEncoder.type(record.kind(), affiliation), record.lat() == null ? 0 : record.lat(),
                record.lon() == null ? 0 : record.lon(), time, stale, record.string("flight") != null
                        ? record.string("flight") : record.string("name"),
                record.number("track"), record.number("gs"), body, null);
    }

    /** PUSH over UDP multicast (LAN only). */
    private static final class MulticastInstance implements SinkInstance {
        private final String group;
        private final int port;
        private final String affiliation;
        private final int stale;
        private final String instanceId;
        private final Map<String, SinkRecord> mirror = new ConcurrentHashMap<>();
        private MulticastSocket socket;
        private InetAddress address;

        MulticastInstance(String group, int port, String affiliation, int stale, String instanceId) {
            this.group = group;
            this.port = port;
            this.affiliation = affiliation;
            this.stale = stale;
            this.instanceId = instanceId;
            try {
                this.address = InetAddress.getByName(group);
                this.socket = new MulticastSocket();
                socket.setTimeToLive(1);
            } catch (Exception e) {
                this.socket = null;
            }
        }

        @Override
        public void upsert(SinkRecord record) {
            mirror.put(record.sourceId() + "|" + record.externalId(), record);
            send(event(record, affiliation, stale, instanceId));
        }

        @Override
        public void remove(String sourceId, String externalId) {
            SinkRecord record = mirror.remove(sourceId + "|" + externalId);
            if (record != null && socket != null) {
                // Removal: an event with stale=now.
                String uid = "FORESIGNAL." + instanceId.substring(0, Math.min(8, instanceId.length())) + "."
                        + sourceId + "." + externalId;
                Instant now = Instant.now();
                send(CotEncoder.event(uid, CotEncoder.type(record.kind(), affiliation),
                        record.lat() == null ? 0 : record.lat(), record.lon() == null ? 0 : record.lon(), now, now,
                        record.string("flight"), null, null, "removed", null));
            }
        }

        private void send(String xml) {
            if (socket == null) {
                return;
            }
            byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);
            try {
                socket.send(new DatagramPacket(bytes, bytes.length, address, port));
            } catch (Exception e) {
                // Best effort.
            }
        }

        @Override
        public void close() {
            if (socket != null) {
                socket.close();
            }
        }
    }

    /** SERVE over TCP. */
    private static final class ServeInstance implements SinkInstance {
        private final SinkTcpServer server;
        private final String affiliation;
        private final int stale;
        private final String instanceId;
        private final Map<String, SinkRecord> mirror = new ConcurrentHashMap<>();

        ServeInstance(SinkTcpServer server, String affiliation, int stale, String instanceId) {
            this.server = server;
            this.affiliation = affiliation;
            this.stale = stale;
            this.instanceId = instanceId;
        }

        @Override
        public void upsert(SinkRecord record) {
            mirror.put(record.sourceId() + "|" + record.externalId(), record);
            String xml = event(record, affiliation, stale, instanceId);
            for (SinkTcpServer.Client client : server.clients()) {
                client.sendLine(xml);
            }
        }

        @Override
        public int clients() {
            return server.clients().size();
        }

        @Override
        public void close() {
            server.close();
        }
    }
}
