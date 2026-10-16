package ai.foresignal.sink.types;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.sink.ServeGuard;
import ai.foresignal.sink.SinkConfig;
import ai.foresignal.sink.SinkFilter;
import ai.foresignal.sink.SinkInstance;
import ai.foresignal.sink.SinkMode;
import ai.foresignal.sink.SinkRecord;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code readsb-json} (prompt 110): {@code aircraft.json} fields and units (m to ft, m/s to ft/min, knots kept),
 * {@code ground}, merge by hex (freshest wins), a content {@code ETag} with {@code 304}, {@code receiver.json},
 * and the SERVE safety rules the JDK {@code HttpServer} lacks: a refused peer gets no response and
 * {@code maxClients} is reported.
 */
class ReadsbJsonSinkIT {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final GeometryFactory GEOMETRY = new GeometryFactory();
    static final HttpClient HTTP = HttpClient.newHttpClient();

    static int freePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            return socket.getLocalPort();
        }
    }

    static SinkRecord aircraft(String source, String hex, Instant observedAt, Map<String, Object> payload) {
        return new SinkRecord(source, hex, "aircraft", 1, 1, observedAt, null, false,
                GEOMETRY.createPoint(new Coordinate(-79.38, 43.65)), payload, null, null);
    }

    static SinkInstance open(ServeGuard guard, int port) {
        Map<String, Object> config = Map.of("bind", "127.0.0.1", "port", port, "maxClients", 3,
                "publishStation", true, "lat", "43.6", "lon", "-79.4");
        return new ReadsbJsonSinkType(guard).open(new SinkConfig("readsb", ReadsbJsonSinkType.ID, "readsb",
                SinkMode.SERVE, config, Map.of(), SinkFilter.ALL));
    }

    static HttpResponse<String> get(int port, String path, String ifNoneMatch) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path));
        if (ifNoneMatch != null) {
            request.header("If-None-Match", ifNoneMatch);
        }
        return HTTP.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("aircraft.json: fields, units, ground, merge by hex, ETag and 304")
    void aircraftJson() throws Exception {
        int port = freePort();
        Instant now = Instant.now();
        try (SinkInstance sink = open(new ServeGuard(), port)) {
            sink.upsert(aircraft("adsblol", "abc123", now.minusSeconds(10), Map.of("hex", "abc123", "flight",
                    "OLD", "alt_baro", 1000.0, "gs", 250.0, "baro_rate", 5.08, "posSource", "adsb")));
            sink.upsert(aircraft("local-adsb-1", "abc123", now, Map.of("hex", "abc123", "flight", "NEW",
                    "alt_baro", 3048.0, "alt_geom", 3100.0, "gs", 260.0, "track", 90.0, "baro_rate", 5.08,
                    "posSource", "mlat")));
            sink.upsert(aircraft("adsblol", "def456", now, Map.of("hex", "def456", "onGround", true)));

            HttpResponse<String> response = get(port, "/data/aircraft.json", null);
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().firstValue("Cache-Control")).hasValue("no-cache");
            JsonNode body = JSON.readTree(response.body());
            assertThat(body.get("aircraft").size()).isEqualTo(2);
            JsonNode first = body.get("aircraft").get(0);
            assertThat(first.get("hex").asString()).isEqualTo("abc123");
            assertThat(first.get("flight").asString()).as("freshest wins").isEqualTo("NEW");
            assertThat(first.get("alt_baro").asInt()).isEqualTo(10000);
            assertThat(first.get("alt_geom").asInt()).isEqualTo(10171);
            assertThat(first.get("gs").asDouble()).as("knots are kept").isEqualTo(260.0);
            assertThat(first.get("baro_rate").asInt()).isEqualTo(1000);
            assertThat(first.get("type").asString()).isEqualTo("mlat");
            assertThat(body.get("aircraft").get(1).get("alt_baro").asString()).isEqualTo("ground");

            HttpResponse<String> receiver = get(port, "/data/receiver.json", null);
            JsonNode station = JSON.readTree(receiver.body());
            assertThat(station.get("lat").asDouble()).isEqualTo(43.6);
            String etag = receiver.headers().firstValue("ETag").orElseThrow();
            assertThat(get(port, "/data/receiver.json", etag).statusCode()).isEqualTo(304);
            assertThat(sink.maxClients()).isEqualTo(3);
            assertThat(sink.clients()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("a peer the guard refuses gets no response and is counted (allowAllInterfaces peer check)")
    void refusedPeer() throws Exception {
        int port = freePort();
        ServeGuard refuseAll = new ServeGuard() {
            @Override
            public boolean allowedPeer(InetAddress address, boolean allowTailnet) {
                return false;
            }
        };
        try (SinkInstance sink = open(refuseAll, port)) {
            try {
                HttpResponse<String> response = get(port, "/data/aircraft.json", null);
                assertThat(response.statusCode()).as("never a data response").isNotEqualTo(200);
            } catch (java.io.IOException expected) {
                // The connection was closed without a response.
            }
            assertThat(sink.rejected()).as("the JDK client may retry an idempotent GET once").isBetween(1L, 2L);
        }
    }

    @Test
    @DisplayName("the ETag is a hash of the body")
    void etag() {
        byte[] a = "{\"a\":1}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] b = "{\"a\":2}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(ReadsbJsonSinkType.etag("x", a)).isEqualTo(ReadsbJsonSinkType.etag("x", a))
                .isNotEqualTo(ReadsbJsonSinkType.etag("x", b)).startsWith("\"x-");
    }
}
