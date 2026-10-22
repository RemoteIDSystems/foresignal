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
 * are converted back to readsb's (meters → feet, m/s → ft/min).
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
            Instance instance = new Instance(server, publishStation, lat, lon, guard, serve.allowTailnet(),
                    serve.maxClients());
            server.createContext("/data/aircraft.json", exchange -> instance.guarded(exchange, instance::aircraft));
            server.createContext("/data/receiver.json", exchange -> instance.guarded(exchange, instance::receiver));
            server.setExecutor(java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor());
            server.start();
            return instance;
        } catch (IOException e) {
            throw new java.io.UncheckedIOException("Could not bind the readsb-json HTTP server", e);
        }
    }

    /** The entity tag of a body: {@code "<tag>-<first 16 hex digits of SHA-256(body)>"}. */
    static String etag(String tag, byte[] body) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256").digest(body);
            return "\"" + tag + "-" + java.util.HexFormat.of().formatHex(digest, 0, 8) + "\"";
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    /** The running instance. */
    private static final class Instance implements SinkInstance {
        private final HttpServer server;
        private final boolean publishStation;
        private final Double lat;
        private final Double lon;
        private final Map<String, SinkRecord> byHex = new ConcurrentHashMap<>();
        private final ServeGuard guard;
        private final boolean allowTailnet;
        private final int maxClients;
        private final java.util.concurrent.Semaphore inFlight;
        private final java.util.concurrent.atomic.AtomicLong rejected = new java.util.concurrent.atomic.AtomicLong();
        private final Map<String, Instant> recentPeers = new ConcurrentHashMap<>();

        Instance(HttpServer server, boolean publishStation, Double lat, Double lon, ServeGuard guard,
                boolean allowTailnet, int maxClients) {
            this.server = server;
            this.publishStation = publishStation;
            this.lat = lat;
            this.lon = lon;
            this.guard = guard;
            this.allowTailnet = allowTailnet;
            this.maxClients = Math.max(1, maxClients);
            this.inFlight = new java.util.concurrent.Semaphore(this.maxClients);
        }

        /** An HTTP handler body that may throw {@link IOException}. */
        @FunctionalInterface
        interface Handler {
            void handle(HttpExchange exchange) throws IOException;
        }

        /**
         * The SERVE safety rules for HTTP (prompt 110, task 3), which the JDK {@code HttpServer} does not apply
         * itself: a peer that fails {@link ServeGuard#allowedPeer} is disconnected without a response (it matters
         * when {@code allowAllInterfaces} binds 0.0.0.0), and at most {@code maxClients} requests are served at once
         * ({@code 503} beyond that). Both refusals count in {@link #rejected()}.
         */
        void guarded(HttpExchange exchange, Handler handler) throws IOException {
            java.net.InetAddress peer = exchange.getRemoteAddress() == null ? null
                    : exchange.getRemoteAddress().getAddress();
            if (!guard.allowedPeer(peer, allowTailnet)) {
                rejected.incrementAndGet();
                exchange.close();
                return;
            }
            if (!inFlight.tryAcquire()) {
                rejected.incrementAndGet();
                exchange.getResponseHeaders().set("Retry-After", "1");
                exchange.sendResponseHeaders(503, -1);
                exchange.close();
                return;
            }
            try {
                recentPeers.put(peer.getHostAddress(), Instant.now());
                handler.handle(exchange);
            } finally {
                inFlight.release();
                exchange.close();
            }
        }

        @Override
        public int clients() {
            Instant cutoff = Instant.now().minusSeconds(60);
            recentPeers.values().removeIf(seen -> seen.isBefore(cutoff));
            return recentPeers.size();
        }

        @Override
        public int maxClients() {
            return maxClients;
        }

        @Override
        public long rejected() {
            return rejected.get();
        }

        @Override
        public List<SinkInstance.Peer> peers() {
            clients();
            return recentPeers.entrySet().stream().map(e -> new SinkInstance.Peer(e.getKey(), e.getValue())).toList();
        }

        @Override
        public void resync() {
            byHex.clear();
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
            out.put("seen", seen(record));
            out.put("seen_pos", record.hasPoint() ? seen(record) : null);
            out.put("type", SinkSupport.readsbType(record));
            out.put("sources", record.properties() == null ? List.of() : record.properties().getOrDefault("sources", List.of()));
            return out;
        }

        /** Seconds since the record was observed (readsb {@code seen}), rounded to tenths; 0 when unknown. */
        static double seen(SinkRecord record) {
            if (record.observedAt() == null) {
                return 0.0;
            }
            double seconds = (System.currentTimeMillis() - record.observedAt().toEpochMilli()) / 1000.0;
            return Math.max(0.0, Math.round(seconds * 10) / 10.0);
        }

        private static Object altitude(SinkRecord record) {
            Object ground = record.properties() == null ? null : record.properties().get("onGround");
            if (Boolean.TRUE.equals(ground)) {
                return "ground";
            }
            return ft(record.number("alt_baro"));
        }

        private static Integer ft(Double meters) {
            return meters == null ? null : (int) Math.round(meters * 3.28084);
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

        /**
         * Writes a JSON body with {@code Cache-Control: no-cache} and a strong {@code ETag} over the body (the
         * SHA-256 of its bytes, truncated), answering {@code 304} to a matching {@code If-None-Match}.
         */
        static void write(HttpExchange exchange, byte[] body, String tag) throws IOException {
            String etag = etag(tag, body);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Cache-Control", "no-cache");
            exchange.getResponseHeaders().set("ETag", etag);
            String ifNoneMatch = exchange.getRequestHeaders().getFirst("If-None-Match");
            if (ifNoneMatch != null && (ifNoneMatch.contains(etag) || ifNoneMatch.strip().equals("*"))) {
                exchange.sendResponseHeaders(304, -1);
                return;
            }
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
