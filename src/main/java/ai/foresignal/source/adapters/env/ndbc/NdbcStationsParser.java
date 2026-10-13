package ai.foresignal.source.adapters.env.ndbc;

import ai.foresignal.source.formats.cap.CapParser;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * NDBC's {@code activestations.xml} as a station table (prompt 078). Pure.
 *
 * <p>Verified 2026-10-13: {@code <stations created="…" count="1354">} with one empty {@code <station id lat lon
 * elev name owner pgm type met currents waterquality dart/>} per station (272 KB, {@code Last-Modified} but no
 * {@code ETag}). Only {@code id}, {@code name}, {@code type} and {@code owner} are kept; positions come from
 * {@code latest_obs.txt}. Read with Foresignal's hardened StAX factory (no DTDs, no external entities).
 */
public final class NdbcStationsParser {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * One station's descriptive fields.
     *
     * @param name the station name, or null when blank
     * @param type the station type ({@code buoy}, {@code fixed}, {@code dart}, {@code other}, …), or null
     * @param owner the owner, or null
     */
    public record Station(String name, String type, String owner) {}

    private NdbcStationsParser() {}

    /**
     * Parses the list.
     *
     * @return station id (upper case) to its fields
     * @throws UpstreamFormatException when the body is not XML or lists no station
     */
    public static Map<String, Station> parse(byte[] body) {
        Map<String, Station> out = new TreeMap<>();
        XMLStreamReader reader = null;
        try {
            reader = CapParser.secureInputFactory().createXMLStreamReader(new ByteArrayInputStream(body));
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT && "station".equals(reader.getLocalName())) {
                    String id = attribute(reader, "id");
                    if (id != null) {
                        out.put(id.toUpperCase(java.util.Locale.ROOT), new Station(attribute(reader, "name"),
                                attribute(reader, "type"), attribute(reader, "owner")));
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new UpstreamFormatException("NDBC activestations.xml is not XML: " + e.getMessage(), "", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException ignored) {
                    // nothing to release
                }
            }
        }
        if (out.isEmpty()) {
            throw new UpstreamFormatException("NDBC activestations.xml lists no station", "");
        }
        return Collections.unmodifiableMap(out);
    }

    /** The table as compact JSON ({@code {id: [name, type, owner]}}), for the adapter state. */
    public static String toJson(Map<String, Station> stations) {
        ObjectNode root = JSON.createObjectNode();
        stations.forEach((id, s) -> {
            ArrayNode a = root.putArray(id);
            a.add(s.name());
            a.add(s.type());
            a.add(s.owner());
        });
        return root.toString();
    }

    /** The table written by {@link #toJson}, or an empty table when {@code json} is null or unreadable. */
    public static Map<String, Station> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            JsonNode root = JSON.readTree(json);
            Map<String, Station> out = new TreeMap<>();
            Iterator<Map.Entry<String, JsonNode>> it = root.properties().iterator();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                JsonNode a = e.getValue();
                out.put(e.getKey(), new Station(text(a.get(0)), text(a.get(1)), text(a.get(2))));
            }
            return Collections.unmodifiableMap(out);
        } catch (RuntimeException e) {
            // JacksonException included: a damaged cache is read again from the upstream.
            return Map.of();
        }
    }

    private static String text(JsonNode node) {
        return node == null || node.isNull() ? null : node.asString();
    }

    private static String attribute(XMLStreamReader reader, String name) {
        String v = reader.getAttributeValue(null, name);
        if (v == null) {
            return null;
        }
        String s = v.strip();
        return s.isEmpty() ? null : s;
    }
}
