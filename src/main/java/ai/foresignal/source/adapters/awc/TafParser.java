package ai.foresignal.source.adapters.awc;

import ai.foresignal.source.formats.cap.CapParser;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import tools.jackson.databind.JsonNode;

/**
 * Maps AWC TAFs to {@code taf} records (prompt 087): the JSON of {@code /api/data/taf?ids=…&format=json} and the XML
 * cache file {@code /data/cache/tafs.cache.xml.gz} (listed under "Cache Files" on aviationweather.gov/data/api and
 * fetched on 2026-10-14: 2,924 TAFs, about 340 KB gzipped, 5 MB inflated, {@code ETag} and {@code Last-Modified},
 * updated every 10 minutes). Pure.
 *
 * <p>One record per station, the newest issue kept: externalId = the station id, {@code observedAt} = the issue
 * time, {@code validFrom}/{@code validUntil} = the TAF validity (asserted by the forecaster), a point at the station.
 * Payload {@code stationId, raw, elevationM, name} (the name only in JSON); {@code raw} is upstream text, stored as
 * data only. Change groups are not decoded: the raw TAF carries them.
 *
 * <p>JSON fields (AWC OpenAPI {@code TAFJSON}, verified live): {@code icaoId, issueTime} ({@code 2026-10-13T22:56:00Z}
 * or with a space), {@code validTimeFrom, validTimeTo} (epoch seconds), {@code rawTAF, lat, lon, elev, name}. XML
 * elements: {@code TAF/raw_text, station_id, issue_time, valid_time_from, valid_time_to, latitude, longitude,
 * elevation_m}; the XML reader is the hardened StAX factory and refuses a DOCTYPE.
 */
public final class TafParser {

    /** The record kind. */
    public static final String KIND = "taf";

    private static final Set<String> TAF_FIELDS = Set.of("raw_text", "station_id", "issue_time", "valid_time_from",
            "valid_time_to", "latitude", "longitude", "elevation_m");

    private TafParser() {}

    /**
     * Which stations to keep.
     *
     * @param envelope {@code [minLon, minLat, maxLon, maxLat]}, or null for every station
     * @param stations an allowlist that overrides the envelope; empty for none
     */
    public record Filter(double[] envelope, Set<String> stations) {

        /** Copies the allowlist. */
        public Filter {
            stations = stations == null ? Set.of() : Set.copyOf(stations);
        }

        boolean keeps(String station, double lon, double lat) {
            return AwcSupport.keeps(envelope, stations, station, lon, lat);
        }
    }

    /**
     * What one document gave.
     *
     * @param records one per kept station
     * @param tafs TAFs read
     * @param skipped TAFs without a station, position, issue time or validity
     * @param filtered TAFs outside the filter
     */
    public record Parsed(List<NormalizedRecord> records, int tafs, int skipped, int filtered) {

        /** Copies the list. */
        public Parsed {
            records = List.copyOf(records);
        }
    }

    /** The JSON array of {@code /api/data/taf}; an empty body (a 204) has none. */
    public static Parsed json(byte[] body, Filter filter) {
        if (body == null || body.length == 0) {
            return new Parsed(List.of(), 0, 0, 0);
        }
        JsonNode root = AwcSupport.json(body, "AWC taf");
        if (!root.isArray()) {
            throw new UpstreamFormatException("AWC taf is not a JSON array", "");
        }
        Builder out = new Builder(filter);
        for (JsonNode taf : root) {
            out.add(AwcSupport.text(taf.get("icaoId")), AwcSupport.number(taf.get("lon")),
                    AwcSupport.number(taf.get("lat")), AwcSupport.instant(taf.get("issueTime")),
                    AwcSupport.instant(taf.get("validTimeFrom")), AwcSupport.instant(taf.get("validTimeTo")),
                    AwcSupport.text(taf.get("rawTAF")), AwcSupport.number(taf.get("elev")),
                    AwcSupport.text(taf.get("name")));
        }
        return out.parsed();
    }

    /**
     * The cache file, gzip or plain.
     *
     * @throws UpstreamFormatException when the XML is malformed or declares a DOCTYPE
     */
    public static Parsed xml(byte[] body, Filter filter) {
        byte[] plain = AwcSupport.gunzip(body, "AWC TAF cache file");
        if (plain == null || plain.length == 0) {
            throw new UpstreamFormatException("AWC TAF cache file is empty", "");
        }
        Builder out = new Builder(filter);
        XMLStreamReader reader = null;
        try {
            reader = CapParser.secureInputFactory().createXMLStreamReader(new ByteArrayInputStream(plain));
            Map<String, String> fields = null;
            int depth = 0;
            int tafDepth = -1;
            String field = null;
            StringBuilder text = new StringBuilder();
            while (reader.hasNext()) {
                int event = reader.next();
                switch (event) {
                    case XMLStreamConstants.DTD, XMLStreamConstants.ENTITY_REFERENCE ->
                            throw new UpstreamFormatException("DOCTYPE declarations and entity references are not"
                                    + " allowed in the AWC TAF cache file", "");
                    case XMLStreamConstants.START_ELEMENT -> {
                        depth++;
                        String name = reader.getLocalName();
                        if ("TAF".equals(name) && fields == null) {
                            fields = new HashMap<>();
                            tafDepth = depth;
                        } else if (fields != null && depth == tafDepth + 1 && TAF_FIELDS.contains(name)) {
                            field = name;
                            text.setLength(0);
                        }
                    }
                    case XMLStreamConstants.CHARACTERS, XMLStreamConstants.CDATA -> {
                        if (field != null) {
                            text.append(reader.getText());
                        }
                    }
                    case XMLStreamConstants.END_ELEMENT -> {
                        if (field != null && depth == tafDepth + 1) {
                            fields.put(field, text.toString().strip());
                            field = null;
                        } else if (fields != null && depth == tafDepth) {
                            out.add(fields.get("station_id"), parse(fields.get("longitude")),
                                    parse(fields.get("latitude")), AwcSupport.instant(fields.get("issue_time")),
                                    AwcSupport.instant(fields.get("valid_time_from")),
                                    AwcSupport.instant(fields.get("valid_time_to")), fields.get("raw_text"),
                                    parse(fields.get("elevation_m")), null);
                            fields = null;
                            tafDepth = -1;
                        }
                        depth--;
                    }
                    default -> {
                        // Comments, whitespace and processing instructions carry nothing.
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new UpstreamFormatException("AWC TAF cache file is not well-formed XML: " + e.getMessage(), "", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException ignored) {
                    // Nothing to release beyond the byte array.
                }
            }
        }
        return out.parsed();
    }

    private static Double parse(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            double value = Double.parseDouble(text.strip());
            return Double.isFinite(value) ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Keeps the newest TAF of each kept station. */
    private static final class Builder {
        private final Filter filter;
        private final Map<String, NormalizedRecord> newest = new LinkedHashMap<>();
        private int tafs;
        private int skipped;
        private int filtered;

        Builder(Filter filter) {
            this.filter = filter == null ? new Filter(null, Set.of()) : filter;
        }

        void add(String station, Double lon, Double lat, Instant issued, Instant from, Instant to, String raw,
                Double elevationM, String name) {
            tafs++;
            if (station == null || lon == null || lat == null || issued == null || from == null || to == null
                    || raw == null || to.isBefore(from) || Math.abs(lat) > 90 || Math.abs(lon) > 180) {
                skipped++;
                return;
            }
            String id = station.toUpperCase(java.util.Locale.ROOT);
            if (!filter.keeps(id, lon, lat)) {
                filtered++;
                return;
            }
            NormalizedRecord current = newest.get(id);
            if (current != null && !issued.isAfter(current.observedAt())) {
                return;
            }
            newest.put(id, NormalizedRecord.builder(id, KIND)
                    .observedAt(issued)
                    .validFrom(from)
                    .validUntil(to)
                    .geometry(Geo.point(lon, lat))
                    .put("stationId", id)
                    .put("raw", AwcHazardParser.raw(raw))
                    .put("rawText", AwcHazardParser.raw(raw))
                    .put("issueTime", issued.toString())
                    .put("validFrom", from.toString())
                    .put("validTo", to.toString())
                    .put("forecast", TafGroups.parse(raw))
                    .put("elevationM", elevationM)
                    .put("name", name)
                    .build());
        }

        Parsed parsed() {
            return new Parsed(new ArrayList<>(newest.values()), tafs, skipped, filtered);
        }
    }
}
