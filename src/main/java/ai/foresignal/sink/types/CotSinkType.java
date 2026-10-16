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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * {@code cot} (prompt 110; STATE_MIRROR, PUSH or SERVE): Cursor-on-Target XML (protocol version 0).
 * PUSH sends UDP multicast SA (default {@code 239.2.3.1:6969}, a LAN-scoped group only, TTL 1); SERVE listens on
 * TCP for direct ATAK/iTAK input and gives each new client the current picture. Affiliation is neutral
 * ({@code u}) or unknown ({@code n}) only. Speeds are converted from Foresignal's knots to the m/s CoT expects;
 * records without a point are skipped.
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
        } else {
            multicastGroup(config == null ? null : config.get("group"));
        }
    }

    @Override
    public SinkInstance open(SinkConfig config) {
        String affiliation = config.config("affiliation", "u");
        int stale = config.intConfig("stale", 120);
        if ("SERVE".equalsIgnoreCase(config.config("mode", "PUSH"))) {
            ServeConfig serve = ServeConfig.of(guard, config);
            Map<String, SinkRecord> mirror = new ConcurrentHashMap<>();
            SinkTcpServer server = new SinkTcpServer(guard, serve.bind(), serve.port(), serve.maxClients(),
                    serve.allowTailnet(), new SinkTcpServer.Handler() {
                        @Override
                        public void onConnect(SinkTcpServer.Client client) {
                            // A new TAK client gets the current picture first (on the client's own thread).
                            for (SinkRecord record : mirror.values()) {
                                client.sendLine(event(record, affiliation, stale, instanceId));
                            }
                        }
                    });
            return new ServeInstance(server, mirror, affiliation, stale, instanceId);
        }
        return new MulticastInstance(multicastGroup(config.config("group")), config.intConfig("port", DEFAULT_PORT),
                affiliation, stale, instanceId);
    }

    /**
     * Resolves and validates the PUSH multicast group (prompt 110: "UDP multicast SA, LAN only, TTL 1"): it must
     * be a multicast address of LAN scope ({@link ServeGuard#lanMulticast}); a unicast or global-scope group is
     * refused with {@code 400 VALIDATION_FAILED}.
     */
    static InetAddress multicastGroup(Object group) {
        String text = group == null || String.valueOf(group).isBlank() ? DEFAULT_GROUP : String.valueOf(group).strip();
        InetAddress address;
        try {
            address = InetAddress.getByName(text);
        } catch (java.net.UnknownHostException e) {
            throw ValidationException.forField("config.group", "cannot resolve " + text);
        }
        if (!ServeGuard.lanMulticast(address)) {
            throw ValidationException.forField("config.group", text
                    + " is not a LAN-scoped multicast group (239.0.0.0/8, 224.0.0.0/24, or IPv6 link/site/org scope)");
        }
        return address;
    }

    /** The CoT uid of a record. */
    static String uid(String instanceId, String sourceId, String externalId) {
        return "FORESIGNAL." + instanceId.substring(0, Math.min(8, instanceId.length())) + "." + sourceId + "."
                + externalId;
    }

    /** The callsign of a record: the flight, else the name, else the vessel callsign. */
    static String callsign(SinkRecord record) {
        String flight = record.string("flight");
        if (flight != null && !flight.isBlank()) {
            return flight;
        }
        String name = record.string("name");
        return name != null && !name.isBlank() ? name : record.string("callsign");
    }

    /** Speed over ground in m/s from the payload's knots ({@code gs} for aircraft, {@code sog} for vessels). */
    static Double speedMs(SinkRecord record) {
        Double knots = record.number("gs");
        if (knots == null) {
            knots = record.number("sog");
        }
        return knots == null ? null : knots * CotEncoder.KNOTS_TO_MS;
    }

    /** Course over ground in degrees ({@code track} for aircraft, {@code cog} for vessels). */
    static Double course(SinkRecord record) {
        Double track = record.number("track");
        return track != null ? track : record.number("cog");
    }

    /**
     * The CoT event of a record. Callers skip records without a point ({@link SinkRecord#hasPoint()}): CoT has no
     * "position unknown", and placing them at 0,0 would put markers in the Gulf of Guinea.
     */
    static String event(SinkRecord record, String affiliation, int staleSeconds, String instanceId) {
        Instant time = SinkSupport.time(record);
        Instant stale = record.validUntil() != null ? record.validUntil() : time.plus(Duration.ofSeconds(staleSeconds));
        String remarks = record.string("title");
        String attribution = record.attribution();
        String body = attribution == null ? remarks : (remarks == null ? attribution : remarks + " • " + attribution);
        return CotEncoder.event(uid(instanceId, record.sourceId(), record.externalId()),
                CotEncoder.type(record.kind(), affiliation), record.lat(), record.lon(), time, stale,
                callsign(record), course(record), speedMs(record), body, null);
    }

    /** The removal event: the record's last position with {@code stale = now}. */
    static String removal(SinkRecord record, String affiliation, String instanceId) {
        Instant now = Instant.now();
        return CotEncoder.event(uid(instanceId, record.sourceId(), record.externalId()),
                CotEncoder.type(record.kind(), affiliation), record.lat(), record.lon(), now, now, callsign(record),
                null, null, "removed", null);
    }

    /** PUSH over UDP multicast (LAN only, TTL 1). */
    private static final class MulticastInstance implements SinkInstance {
        private final InetAddress address;
        private final int port;
        private final String affiliation;
        private final int stale;
        private final String instanceId;
        private final Map<String, SinkRecord> mirror = new ConcurrentHashMap<>();
        private final MulticastSocket socket;

        MulticastInstance(InetAddress group, int port, String affiliation, int stale, String instanceId) {
            this.address = group;
            this.port = port;
            this.affiliation = affiliation;
            this.stale = stale;
            this.instanceId = instanceId;
            try {
                this.socket = new MulticastSocket();
                socket.setTimeToLive(1);
            } catch (java.io.IOException e) {
                throw new java.io.UncheckedIOException("Could not open the CoT multicast socket", e);
            }
        }

        @Override
        public void upsert(SinkRecord record) {
            if (!record.hasPoint()) {
                return;
            }
            mirror.put(record.key(), record);
            send(event(record, affiliation, stale, instanceId));
        }

        @Override
        public void remove(String sourceId, String externalId) {
            SinkRecord record = mirror.remove(sourceId + "|" + externalId);
            if (record != null) {
                send(removal(record, affiliation, instanceId));
            }
        }

        @Override
        public void resync() {
            mirror.clear();
        }

        private void send(String xml) {
            byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);
            try {
                socket.send(new DatagramPacket(bytes, bytes.length, address, port));
            } catch (java.io.IOException e) {
                // Best effort: multicast SA is fire-and-forget.
            }
        }

        @Override
        public void close() {
            socket.close();
        }
    }

    /** SERVE over TCP. */
    private static final class ServeInstance implements SinkInstance {
        private final SinkTcpServer server;
        private final String affiliation;
        private final int stale;
        private final String instanceId;
        private final Map<String, SinkRecord> mirror;

        ServeInstance(SinkTcpServer server, Map<String, SinkRecord> mirror, String affiliation, int stale,
                String instanceId) {
            this.server = server;
            this.mirror = mirror;
            this.affiliation = affiliation;
            this.stale = stale;
            this.instanceId = instanceId;
        }

        @Override
        public void upsert(SinkRecord record) {
            if (!record.hasPoint()) {
                return;
            }
            mirror.put(record.key(), record);
            server.broadcast(event(record, affiliation, stale, instanceId));
        }

        @Override
        public void remove(String sourceId, String externalId) {
            SinkRecord record = mirror.remove(sourceId + "|" + externalId);
            if (record != null) {
                server.broadcast(removal(record, affiliation, instanceId));
            }
        }

        @Override
        public void resync() {
            mirror.clear();
        }

        @Override
        public int clients() {
            return server.clients().size();
        }

        @Override
        public int maxClients() {
            return server.maxClients();
        }

        @Override
        public long rejected() {
            return server.rejected();
        }

        @Override
        public List<SinkInstance.Peer> peers() {
            return SinkInstance.peers(server);
        }

        @Override
        public void close() {
            server.close();
        }
    }
}
