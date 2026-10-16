package ai.foresignal.sink.types;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.sink.SbsEncoder;
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
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * {@code sbs} (prompt 110; STATE_MIRROR, SERVE TCP): BaseStation {@code MSG} lines for tar1090/VRS.
 * New clients receive the current snapshot first.
 */
@Component
public class SbsSinkType implements SinkType {

    /** The type id. */
    public static final String ID = "sbs";

    private static final SinkTypeSpec SPEC = new SinkTypeSpec(ID, "SBS-1",
            "BaseStation MSG lines (tar1090, Virtual Radar Server)", SinkKind.STATE_MIRROR, Set.of(SinkMode.SERVE),
            ServeConfig.SCHEMA, List.of(), null);

    private final ServeGuard guard;

    /** Creates the type. */
    public SbsSinkType(ServeGuard guard) {
        this.guard = guard;
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
        ServeConfig.validate(guard, config);
    }

    @Override
    public SinkInstance open(SinkConfig config) {
        ServeConfig serve = ServeConfig.of(guard, config);
        Map<String, SinkRecord> mirror = new ConcurrentHashMap<>();
        SinkTcpServer server = new SinkTcpServer(guard, serve.bind(), serve.port(), serve.maxClients(),
                serve.allowTailnet(), new SinkTcpServer.Handler() {
                    @Override
                    public void onConnect(SinkTcpServer.Client client) {
                        for (SinkRecord record : mirror.values()) {
                            send(client, record);
                        }
                    }
                });
        return new Instance(server, mirror);
    }

    private static void send(SinkTcpServer.Client client, SinkRecord record) {
        for (String line : lines(record)) {
            client.sendLine(line);
        }
    }

    /**
     * The MSG,3 (position, only when the record has a point) and MSG,4 (velocity) lines of a record. A record
     * without a position never produces a 0,0 position line.
     */
    static List<String> lines(SinkRecord record) {
        String hex = SinkSupport.hex(record).toUpperCase(java.util.Locale.ROOT);
        String callsign = record.string("flight");
        List<String> out = new java.util.ArrayList<>(2);
        if (record.hasPoint()) {
            out.add(SbsEncoder.position(hex, callsign, altitudeFt(record), record.number("gs"),
                    record.number("track"), record.lat(), record.lon(), onGround(record), SinkSupport.time(record)));
        }
        out.add(SbsEncoder.velocity(hex, callsign, record.number("gs"), record.number("track"),
                verticalRateFtMin(record), SinkSupport.time(record)));
        return out;
    }

    private static Integer altitudeFt(SinkRecord record) {
        Double metres = record.number("alt_baro");
        if (metres == null) {
            metres = record.number("alt_geom");
        }
        return metres == null ? null : (int) Math.round(metres * 3.28084);
    }

    private static Integer verticalRateFtMin(SinkRecord record) {
        Double ms = record.number("baro_rate");
        return ms == null ? null : (int) Math.round(ms * 196.8504);
    }

    private static Boolean onGround(SinkRecord record) {
        Object value = record.properties() == null ? null : record.properties().get("onGround");
        return value instanceof Boolean b ? b : null;
    }

    /** The running instance. */
    private static final class Instance implements SinkInstance {
        private final SinkTcpServer server;
        private final Map<String, SinkRecord> mirror;

        Instance(SinkTcpServer server, Map<String, SinkRecord> mirror) {
            this.server = server;
            this.mirror = mirror;
        }

        @Override
        public void upsert(SinkRecord record) {
            String key = SinkSupport.hex(record);
            SinkRecord previous = mirror.get(key);
            mirror.put(key, record);
            if (previous == null || !java.util.Objects.equals(previous.string("flight"), record.string("flight"))) {
                server.broadcast(SbsEncoder.callsign(key.toUpperCase(java.util.Locale.ROOT), record.string("flight"),
                        SinkSupport.time(record)));
            }
            for (String line : lines(record)) {
                server.broadcast(line);
            }
        }

        @Override
        public void remove(String sourceId, String externalId) {
            mirror.values().removeIf(r -> r.sourceId().equals(sourceId) && r.externalId().equals(externalId));
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
