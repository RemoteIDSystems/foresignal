package ai.foresignal.sink.types;

import ai.foresignal.sink.ServeGuard;
import ai.foresignal.sink.SinkConfig;
import ai.foresignal.sink.SinkInstance;
import ai.foresignal.sink.SinkKind;
import ai.foresignal.sink.SinkMode;
import ai.foresignal.sink.SinkRecord;
import ai.foresignal.sink.SinkTcpServer;
import ai.foresignal.sink.SinkType;
import ai.foresignal.sink.SinkTypeSpec;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * {@code aprs-is-serve} (prompt 110; STATE_MIRROR, SERVE TCP): a read-only APRS-IS-compatible server
 * for local clients (Xastir, YAAC, APRSdroid). Streams the raw TNC2 packets of {@code aprs-is} and
 * {@code local-aprs-*} records; client-sent packets are discarded and never gated anywhere.
 */
@Component
public class AprsIsServeSinkType implements SinkType {

    /** The type id. */
    public static final String ID = "aprs-is-serve";

    /** The server signature. */
    public static final String SERVER = "FORESIGNAL";

    private static final SinkTypeSpec SPEC = new SinkTypeSpec(ID, "APRS-IS serve",
            "Read-only APRS-IS server for local clients", SinkKind.STATE_MIRROR, Set.of(SinkMode.SERVE),
            ServeConfig.SCHEMA, List.of(), null);

    private final ServeGuard guard;

    /** Creates the type. */
    public AprsIsServeSinkType(ServeGuard guard) {
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
        Map<SinkTcpServer.Client, Filter> filters = new ConcurrentHashMap<>();
        SinkTcpServer server = new SinkTcpServer(guard, serve.bind(), serve.port(), serve.maxClients(),
                serve.allowTailnet(), new SinkTcpServer.Handler() {
                    @Override
                    public void onConnect(SinkTcpServer.Client client) {
                        client.sendLine("# foresignal aprs-is-serve 1");
                        filters.put(client, Filter.ALL);
                    }

                    @Override
                    public void onLine(SinkTcpServer.Client client, String line) {
                        // Client-sent packets are discarded; only the login line is interpreted.
                        if (line.startsWith("user ")) {
                            String call = line.split("\\s+")[1];
                            filters.put(client, Filter.parse(line));
                            client.sendLine("# logresp " + call + " unverified, server " + SERVER);
                        }
                    }

                    @Override
                    public void onClose(SinkTcpServer.Client client) {
                        filters.remove(client);
                    }
                });
        return new Instance(server, filters);
    }

    /** The running instance. */
    private static final class Instance implements SinkInstance {
        private final SinkTcpServer server;
        private final Map<SinkTcpServer.Client, Filter> filters;

        Instance(SinkTcpServer server, Map<SinkTcpServer.Client, Filter> filters) {
            this.server = server;
            this.filters = filters;
        }

        @Override
        public void upsert(SinkRecord record) {
            if (!"aprs-is".equals(record.sourceId()) && !record.sourceId().startsWith("local-aprs-")) {
                return;
            }
            String raw = record.string("raw");
            if (raw == null || raw.isBlank()) {
                return;
            }
            for (Map.Entry<SinkTcpServer.Client, Filter> entry : filters.entrySet()) {
                if (entry.getValue().matches(raw, record.lat(), record.lon())) {
                    entry.getKey().sendRaw((raw + "\r\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
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

    /** An APRS-IS filter: {@code r/lat/lon/km}, {@code b/CALL*} and {@code t/} (minimum). */
    static final class Filter {
        static final Filter ALL = new Filter(null, null, null);

        private final double[] radius;
        private final String buddy;
        private final String types;

        Filter(double[] radius, String buddy, String types) {
            this.radius = radius;
            this.buddy = buddy;
            this.types = types;
        }

        static Filter parse(String line) {
            double[] radius = null;
            String buddy = null;
            String types = null;
            int at = line.indexOf("filter ");
            if (at >= 0) {
                for (String term : line.substring(at + 7).split("\\s+")) {
                    if (term.startsWith("r/")) {
                        String[] parts = term.substring(2).split("/");
                        if (parts.length == 3) {
                            try {
                                radius = new double[] {Double.parseDouble(parts[0]), Double.parseDouble(parts[1]),
                                        Double.parseDouble(parts[2])};
                            } catch (NumberFormatException e) {
                                radius = null;
                            }
                        }
                    } else if (term.startsWith("b/")) {
                        buddy = term.substring(2).replace("*", "");
                    } else if (term.startsWith("t/")) {
                        types = term.substring(2);
                    }
                }
            }
            return new Filter(radius, buddy, types);
        }

        boolean matches(String raw, Double lat, Double lon) {
            if (buddy != null && !raw.toUpperCase(java.util.Locale.ROOT).contains(buddy.toUpperCase(java.util.Locale.ROOT))) {
                return false;
            }
            if (radius != null && lat != null && lon != null) {
                double km = haversine(radius[0], radius[1], lat, lon);
                return km <= radius[2];
            }
            return types == null || raw.contains(">");
        }

        private static double haversine(double lat1, double lon1, double lat2, double lon2) {
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lon2 - lon1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
                    * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
            return 6371.0088 * 2 * Math.asin(Math.min(1, Math.sqrt(a)));
        }
    }
}
