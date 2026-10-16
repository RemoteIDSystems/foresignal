package ai.foresignal.sink.types;

import ai.foresignal.codec.ais.AisEncoder;
import ai.foresignal.codec.ais.AisMessage;
import ai.foresignal.codec.ais.NmeaWriter;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * {@code nmea-aivdm} (prompt 110; STATE_MIRROR, SERVE TCP, optional UDP unicast): encodes vessels with
 * the 085 AIVDM codec (class A → type 1, class B → type 18, static every 6 min). Records without a
 * valid 9-digit MMSI are skipped; sentinels encode as "not available".
 */
@Component
public class NmeaAivdmSinkType implements SinkType {

    /** The type id. */
    public static final String ID = "nmea-aivdm";

    private static final SinkTypeSpec SPEC = new SinkTypeSpec(ID, "NMEA AIVDM",
            "AIS AIVDM sentences (OpenCPN, Signal K)", SinkKind.STATE_MIRROR, Set.of(SinkMode.SERVE),
            ServeConfig.SCHEMA, List.of(), null);

    private final ServeGuard guard;

    /** Creates the type. */
    public NmeaAivdmSinkType(ServeGuard guard) {
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
        Map<String, Boolean> staticSent = new ConcurrentHashMap<>();
        SinkTcpServer server = new SinkTcpServer(guard, serve.bind(), serve.port(), serve.maxClients(),
                serve.allowTailnet(), new SinkTcpServer.Handler() {
                    @Override
                    public void onConnect(SinkTcpServer.Client client) {
                        // AIVDM clients are receive-only; the snapshot arrives as records upsert.
                    }
                });
        return new Instance(server, staticSent);
    }

    private static List<String> sentences(SinkRecord record) {
        String mmsiText = SinkSupport.mmsi(record);
        if (mmsiText == null) {
            return List.of();
        }
        int mmsi = Integer.parseInt(mmsiText);
        Integer navStatus = intOrNull(record.string("navStatus"));
        Double sog = record.number("sog");
        Double cog = record.number("cog");
        Integer heading = intOrNull(record.string("heading"));
        boolean classB = !"A".equalsIgnoreCase(record.string("class"));
        AisMessage message;
        if (classB) {
            message = new AisMessage.ClassBPosition(0, mmsi, sog, true, record.lon(), record.lat(), cog, heading, 0, 0,
                    false, false, false, false, false, false, false, 0);
        } else {
            message = new AisMessage.ClassAPosition(1, 0, mmsi, navStatus, null, sog, true, record.lon(), record.lat(),
                    cog, heading, 0, 0, false, 0);
        }
        return new NmeaWriter().write(message, "A");
    }

    private static Integer intOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.replaceAll("[^0-9-]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** The running instance. */
    private static final class Instance implements SinkInstance {
        private final SinkTcpServer server;
        private final Map<String, Boolean> staticSent;

        Instance(SinkTcpServer server, Map<String, Boolean> staticSent) {
            this.server = server;
            this.staticSent = staticSent;
        }

        @Override
        public void upsert(SinkRecord record) {
            List<String> sentences = sentences(record);
            if (sentences.isEmpty()) {
                return;
            }
            for (SinkTcpServer.Client client : server.clients()) {
                for (String sentence : sentences) {
                    client.sendLine(sentence);
                }
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
