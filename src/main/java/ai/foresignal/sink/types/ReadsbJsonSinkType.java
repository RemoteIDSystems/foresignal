package ai.foresignal.sink.types;

import ai.foresignal.sink.ServeGuard;
import ai.foresignal.sink.SinkConfig;
import ai.foresignal.sink.SinkInstance;
import ai.foresignal.sink.SinkKind;
import ai.foresignal.sink.SinkMode;
import ai.foresignal.sink.SinkRecord;
import ai.foresignal.sink.SinkSupport;
import ai.foresignal.sink.SinkType;
import ai.foresignal.sink.SinkTypeSpec;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code readsb-json} (prompt 110; STATE_MIRROR, SERVE HTTP): {@code GET /data/aircraft.json} and
 * {@code /data/receiver.json} for tar1090/VRS. Merges by hex (freshest {@code observedAt} wins). Units
 * are converted back to readsb's (metres → feet, m/s → ft/min).
 */
@Component
public class ReadsbJsonSinkType implements SinkType {

    /** The type id. */
    public static final String ID = "readsb-json";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final SinkTypeSpec SPEC = new SinkTypeSpec(ID, "readsb aircraft.json",
            "readsb /data/aircraft.json for tar1090 and Virtual Radar Server", SinkKind.STATE_MIRROR,
            Set.of(SinkMode.SERVE), ServeConfig.SCHEMA, List.of(), null);

    private final ServeGuard guard;

    /** Creates the type. */
    public ReadsbJsonSinkType(ServeGuard guard) {
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
        boolean publishStation = config.boolConfig("publishStation", false);
        Double lat = config.config("lat") == null ? null : Double.parseDouble(config.config("lat"));
        Double lon = config.config("lon") == null ? null : Double.parseDouble(config.config("lon"));
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(serve.bind(), serve.port()), 0);
            Instance instance = new Instance(server, publishStation, lat, lon);
            server.createContext("/data/aircraft.json", exchange -> instance.aircraft(exchange));
            server.createContext("/data/receiver.json", exchange -> instance.receiver(exchange));
            server.start();
            return instance;
        } catch (IOException e) {
            throw new java.io.UncheckedIOException("Could not bind the readsb-json HTTP server", e);
        }
    }

    /** The running instance. */
    private static final class Instance implements SinkInstance {
        private final HttpServer server;
        private final boolean publishStation;
        private final Double lat;
        private final Double lon;
        private final Map<String, SinkRecord> byHex = new ConcurrentHashMap<>();

        Instance(HttpServer server, boolean publishStation, Double lat, Double lon) {
            this.server = server;
            this.publishStation = publishStation;
            this.lat = lat;
            this.lon = lon;
        }

        @Override
        public void upsert(SinkRecord record) {
            String hex = SinkSupport.hex(record);
            byHex.merge(hex, record, (a, b) -> freshest(a, b));
        }

        @Override
        public void remove(String sourceId, String externalId) {
            byHex.computeIfPresent(externalId, (k, v) -> v.sourceId().equals(sourceId) ? null : v);
        }

        private static SinkRecord freshest(SinkRecord a, SinkRecord b) {
            Instant ta = a.observedAt() == null ? Instant.EPOCH : a.observedAt();
            Instant tb = b.observedAt() == null ? Instant.EPOCH : b.observedAt();
            return tb.isAfter(ta) ? b : a;
        }

        private void aircraft(HttpExchange exchange) throws IOException {
            List<Map<String, Object>> aircraft = new ArrayList<>();
            for (SinkRecord record : byHex.values()) {
                aircraft.add(entry(record));
            }
            aircraft.sort(Comparator.comparing(a -> String.valueOf(a.get("hex"))));
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("now", System.currentTimeMillis() / 1000.0);
            body.put("messages", 0);
            body.put("aircraft", aircraft);
            write(exchange, JSON.writeValueAsBytes(body), "aircraft");
        }

        private Map<String, Object> entry(SinkRecord record) {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("hex", SinkSupport.hex(record));
            out.put("flight", record.string("flight"));
            out.put("alt_baro", altitude(record));
            out.put("alt_geom", ft(record.number("alt_geom")));
            out.put("gs", record.number("gs"));
            out.put("track", record.number("track"));
            out.put("baro_rate", ftPerMin(record.number("baro_rate")));
            out.put("squawk", record.string("squawk"));
            out.put("category", record.string("category"));
            out.put("lat", record.lat());
            out.put("lon", record.lon());
            out.put("seen", 0.0);
            out.put("seen_pos", 0.0);
            out.put("type", SinkSupport.readsbType(record));
            out.put("sources", record.properties() == null ? List.of() : record.properties().getOrDefault("sources", List.of()));
            return out;
        }

        private static Object altitude(SinkRecord record) {
            Object ground = record.properties() == null ? null : record.properties().get("onGround");
            if (Boolean.TRUE.equals(ground)) {
                return "ground";
            }
            return ft(record.number("alt_baro"));
        }

        private static Integer ft(Double metres) {
            return metres == null ? null : (int) Math.round(metres * 3.28084);
        }

        private static Integer ftPerMin(Double ms) {
            return ms == null ? null : (int) Math.round(ms * 196.8504);
        }

        private void receiver(HttpExchange exchange) throws IOException {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("version", "foresignal-sink-1");
            body.put("refresh", 1000);
            body.put("history", 0);
            if (publishStation && lat != null && lon != null) {
                body.put("lat", lat);
                body.put("lon", lon);
            }
            write(exchange, JSON.writeValueAsBytes(body), "receiver");
        }

        private static void write(HttpExchange exchange, byte[] body, String tag) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Cache-Control", "no-cache");
            exchange.getResponseHeaders().set("ETag", "\"foresignal-" + tag + "\"");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        }

        @Override
        public void close() {
            server.stop(0);
        }
    }
}
