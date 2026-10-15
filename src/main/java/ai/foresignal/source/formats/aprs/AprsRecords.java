package ai.foresignal.source.formats.aprs;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps parsed APRS packets to records (prompt 082): the shared record shape of {@code aprs-is} and
 * {@code local-aprs-{n}}. One instance per connection; not thread-safe.
 *
 * <ul>
 *   <li><strong>Stations</strong> (kind {@value #STATION_KIND}): {@code externalId} = the source call with SSID, or the
 *       object or item name. The payload is a complete snapshot (adapter rule 7): a station sends its position and
 *       its status in separate packets, so the latest of each is merged per entity, and a status packet re-emits the
 *       last position. Keys: {@code symbol, comment, status, speedMps, courseDeg, altitudeM, type, weather}
 *       (only the values present), {@code micEMessage} for Mic-E, {@code sender} for objects and items, plus the
 *       volatile {@code path} and {@code raw} (the TNC2 line, at most {@value #MAX_RAW} characters, which prompt
 *       110's APRS-IS serve sink re-emits). A re-beacon with the same content therefore hashes equal and only
 *       touches.
 *   <li><strong>Killed objects and items</strong> become tombstones of their name.
 *   <li><strong>Messages</strong> (kind {@value #MESSAGE_KIND}) only when the caller includes them; acknowledgements
 *       and rejections never. {@code externalId} = {@code SOURCE>ADDRESSEE{id}}, or a hash of the text when there is
 *       no id. Payload {@code from, addressee, text, msgId} plus the volatile {@code path} and {@code raw}.
 *   <li><strong>Observer:</strong> the receiving station is an {@link Observer}, never a payload field: the igate
 *       from the q-construct for APRS-IS, the operator's own station for a local TNC.
 *   <li><strong>Time:</strong> the sources declare {@code observedAtMode} RECEIVED, so no record sets
 *       {@code observedAt} except tombstones, which carry the receive time.
 * </ul>
 * The station state is bounded ({@value #MAX_STATIONS} entities, least recently heard evicted).
 */
public final class AprsRecords {

    /** The station, object and item kind. */
    public static final String STATION_KIND = "aprs-station";

    /** The message kind. */
    public static final String MESSAGE_KIND = "aprs-message";

    /** The longest {@code raw} value kept. */
    public static final int MAX_RAW = 512;

    /** The most stations whose state is merged per connection. */
    public static final int MAX_STATIONS = 20_000;

    /** The volatile payload keys. */
    public static final List<String> VOLATILE = List.of("path", "raw");

    private final Map<String, Map<String, Object>> stations = new LinkedHashMap<>(256, 0.75f, true) {
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Map<String, Object>> eldest) {
            return size() > MAX_STATIONS;
        }
    };
    private final Map<String, double[]> positions = new LinkedHashMap<>(256, 0.75f, true) {
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, double[]> eldest) {
            return size() > MAX_STATIONS;
        }
    };

    /**
     * The records one packet produces.
     *
     * @param packet the parsed packet
     * @param observer the receiving station, or null
     * @param includeMessages true to emit {@value #MESSAGE_KIND} records
     * @param receivedAt when the packet arrived (used only as a tombstone's time)
     * @return the records, possibly none (unsupported packets, acknowledgements, excluded messages)
     */
    public List<NormalizedRecord> map(AprsPacket packet, Observer observer, boolean includeMessages, Instant receivedAt) {
        if (packet == null || packet.type() == AprsPacket.Type.UNSUPPORTED || packet.source() == null) {
            return List.of();
        }
        List<Observer> observers = observer == null ? List.of() : List.of(observer);
        return switch (packet.type()) {
            case MESSAGE -> includeMessages && !packet.ack() ? List.of(message(packet, observers)) : List.of();
            case STATUS -> List.of(status(packet, observers));
            case OBJECT, ITEM -> packet.killed() ? List.of(killed(packet, receivedAt)) : List.of(position(packet, observers));
            default -> packet.hasPosition() ? List.of(position(packet, observers)) : List.of();
        };
    }

    /** The number of stations whose state is held. */
    public int stations() {
        return stations.size();
    }

    private NormalizedRecord position(AprsPacket packet, List<Observer> observers) {
        String id = packet.entityId();
        Map<String, Object> state = new LinkedHashMap<>();
        Map<String, Object> previous = stations.get(id);
        if (previous != null && previous.get("status") != null) {
            state.put("status", previous.get("status"));
        }
        put(state, "symbol", packet.symbol());
        put(state, "comment", packet.comment());
        put(state, "speedMps", packet.speedMps());
        put(state, "courseDeg", packet.courseDeg());
        put(state, "altitudeM", packet.altitudeM());
        put(state, "type", packet.type().code());
        if (!packet.weather().isEmpty()) {
            state.put("weather", packet.weather());
        }
        put(state, "micEMessage", packet.micEMessage());
        if (packet.type() == AprsPacket.Type.OBJECT || packet.type() == AprsPacket.Type.ITEM) {
            put(state, "sender", packet.source());
        }
        stations.put(id, state);
        if (packet.hasPosition()) {
            positions.put(id, new double[] {packet.longitude(), packet.latitude()});
        }
        return build(id, state, packet, observers);
    }

    private NormalizedRecord status(AprsPacket packet, List<Observer> observers) {
        String id = packet.source();
        Map<String, Object> previous = stations.get(id);
        Map<String, Object> state = previous == null ? new LinkedHashMap<>() : new LinkedHashMap<>(previous);
        if (previous == null) {
            state.put("type", packet.type().code());
        }
        if (packet.comment() == null) {
            state.remove("status");
        } else {
            state.put("status", packet.comment());
        }
        stations.put(id, state);
        return build(id, state, packet, observers);
    }

    private NormalizedRecord killed(AprsPacket packet, Instant receivedAt) {
        stations.remove(packet.objectName());
        positions.remove(packet.objectName());
        return NormalizedRecord.tombstone(packet.objectName(), STATION_KIND, receivedAt);
    }

    private NormalizedRecord build(String id, Map<String, Object> state, AprsPacket packet, List<Observer> observers) {
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, STATION_KIND).putAll(state)
                .put("callsign", packet.source())
                .put("path", packet.path().isEmpty() ? null : String.join(",", packet.path()))
                .put("raw", raw(packet.raw()))
                .observers(observers);
        double[] point = positions.get(id);
        if (point != null) {
            b.point(point[0], point[1]);
        }
        return b.build();
    }

    private static NormalizedRecord message(AprsPacket packet, List<Observer> observers) {
        String id = packet.source() + ">" + packet.addressee() + "{"
                + (packet.messageId() != null ? packet.messageId() : sha256(String.valueOf(packet.messageText())).substring(0, 16));
        return NormalizedRecord.builder(id, MESSAGE_KIND)
                .put("from", packet.source())
                .put("addressee", packet.addressee())
                .put("text", packet.messageText())
                .put("msgId", packet.messageId())
                .put("path", packet.path().isEmpty() ? null : String.join(",", packet.path()))
                .put("raw", raw(packet.raw()))
                .observers(observers)
                .build();
    }

    /** {@code raw} cut to {@value #MAX_RAW} characters. */
    static String raw(String raw) {
        if (raw == null) {
            return null;
        }
        return raw.length() <= MAX_RAW ? raw : raw.substring(0, MAX_RAW);
    }

    private static void put(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    /** The lower-case hex SHA-256 of {@code text} in UTF-8. */
    static String sha256(String text) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
