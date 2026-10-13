package ai.foresignal.source.adapters.roads;

import ai.foresignal.source.formats.cap.CapParser;
import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

/**
 * Streams a DATEX II v3 {@code SituationPublication} (NDW, the Netherlands) into road events (prompt 074; verified on
 * NDW's {@code actueel_beeld} and SRTI files 2026-10-13).
 *
 * <p><strong>Input:</strong> the gzip file NDW serves (detected by its magic bytes; decompressed up to
 * {@value #MAX_DECOMPRESSED_BYTES} bytes) or plain XML, read with StAX on the hardened factory
 * ({@link CapParser#secureInputFactory()}: no DTD, no external entities); a DOCTYPE or entity reference is refused.
 *
 * <p><strong>One record per {@code sit:situation}:</strong> {@code id} → {@code externalId};
 * {@code situationVersionTime} → {@code observedAt}; {@code overallSeverity} → {@code severity} ({@link #SEVERITY}).
 * Per {@code sit:situationRecord}: {@code xsi:type}, {@code validity/validityTimeSpecification/overallStartTime} and
 * {@code overallEndTime}, {@code cause/causeType}, the record's own type code
 * ({@code roadOrCarriagewayOrLaneManagementType}, {@code vehicleObstructionType}, …), {@code temporarySpeedLimit},
 * {@code numberOfLanesRestricted}, {@code alertCAffectedDirection}, {@code generalPublicComment} values, and its
 * locations: {@code loc:gmlLineString/loc:posList} and {@code loc:pointByCoordinates/loc:pointCoordinates}.
 *
 * <ul>
 *   <li>The category comes from the first record ({@link #category}); {@code isClosure} when any record is a road or
 *       carriageway closure ({@link #CLOSURE_TYPES}).
 *   <li>The geometry combines every record's locations: one part as itself, lines as a MultiLineString, points as a
 *       MultiPoint, a mix as a GeometryCollection; simplified above 9,500 vertices.
 *   <li><strong>Axis order:</strong> DATEX II GML {@code posList} holds "lat lon" pairs; they are swapped to
 *       longitude, latitude here.
 *   <li>{@code startsAt}/{@code validFrom} is the earliest record start; {@code endsAt}/{@code validUntil} the
 *       latest record end, only when every record has one.
 *   <li>The headline is the first public comment's first sentence, else the record's type code in words with its
 *       cause ({@code Carriageway closures (road maintenance)}); the description joins the public comments.
 * </ul>
 */
public final class Datex3SituationParser {

    /** The largest decompressed document read. */
    public static final long MAX_DECOMPRESSED_BYTES = 64L * 1024 * 1024;

    /** The DATEX II v3 situation namespace. */
    public static final String SIT = "http://datex2.eu/schema/3/situation";

    /** The XML Schema instance namespace. */
    static final String XSI = "http://www.w3.org/2001/XMLSchema-instance";

    /** {@code overallSeverity} → severity. */
    public static final Map<String, String> SEVERITY = Map.of(
            "highest", "severe",
            "high", "major",
            "medium", "moderate",
            "low", "minor",
            "lowest", "minor",
            "none", "minor",
            "unknown", "unknown");

    /** {@code roadOrCarriagewayOrLaneManagementType} values that close the road or a carriageway. */
    public static final Set<String> CLOSURE_TYPES = Set.of("roadClosed", "carriagewayClosures",
            "closedPermanentlyForTheWinter");

    /** Causes that make a lane management record roadwork rather than a restriction. */
    static final Set<String> WORK_CAUSES = Set.of("roadMaintenance", "constructionWork", "roadworks");

    /** The record elements whose text is the record's own type code. */
    static final Set<String> DETAIL_ELEMENTS = Set.of("roadOrCarriagewayOrLaneManagementType",
            "vehicleObstructionType", "accidentType", "obstructionType", "speedManagementType",
            "reroutingManagementType", "generalNetworkManagementType", "roadMaintenanceType", "constructionWorkType",
            "abnormalTrafficType", "poorEnvironmentType", "animalPresenceType", "environmentalObstructionType",
            "infrastructureDamageType", "publicEventType", "nonWeatherRelatedRoadConditionType",
            "weatherRelatedRoadConditionType");

    private Datex3SituationParser() {}

    /**
     * The category of a situation from its first record.
     *
     * @param recordType the record's {@code xsi:type} without prefix
     * @param managementType its {@code roadOrCarriagewayOrLaneManagementType}, or null
     * @param cause its {@code causeType}, or null
     */
    public static String category(String recordType, String managementType, String cause) {
        if (recordType == null) {
            return "other";
        }
        if (recordType.equals("Accident") || recordType.endsWith("Obstruction")) {
            return "incident";
        }
        return switch (recordType) {
            case "RoadOrCarriagewayOrLaneManagement" -> {
                if (managementType != null && CLOSURE_TYPES.contains(managementType)) {
                    yield "closure";
                }
                yield cause != null && WORK_CAUSES.contains(cause) ? "roadwork" : "restriction";
            }
            case "MaintenanceWorks", "ConstructionWorks", "Roadworks" -> "roadwork";
            case "SpeedManagement", "ReroutingManagement" -> "restriction";
            case "PoorEnvironmentConditions", "WeatherRelatedRoadConditions" -> "weather";
            case "PublicEvent" -> "event";
            default -> "other";
        };
    }

    /**
     * Parses one publication.
     *
     * @param body the gzip or plain XML body
     * @param now the fetch time (for {@code status})
     * @throws UpstreamFormatException when the body is not a readable DATEX II v3 document or is too large
     */
    public static ParsedEvents parse(byte[] body, Instant now) {
        byte[] xml = gunzipIfNeeded(body);
        ParsedEvents out = new ParsedEvents();
        XMLStreamReader r = null;
        boolean sawRoot = false;
        try {
            r = CapParser.secureInputFactory().createXMLStreamReader(new ByteArrayInputStream(xml));
            Situation situation = null;
            Rec rec = null;
            StringBuilder text = new StringBuilder();
            int commentDepth = 0;
            boolean inPoint = false;
            Double lat = null;
            Double lon = null;
            while (r.hasNext()) {
                int event = r.next();
                switch (event) {
                    case XMLStreamConstants.DTD, XMLStreamConstants.ENTITY_REFERENCE ->
                            throw new UpstreamFormatException("DOCTYPE declarations and entity references are not"
                                    + " allowed", "");
                    case XMLStreamConstants.START_ELEMENT -> {
                        sawRoot = true;
                        String name = r.getLocalName();
                        if (SIT.equals(r.getNamespaceURI()) && name.equals("situation")) {
                            situation = new Situation(r.getAttributeValue(null, "id"));
                        } else if (situation != null && SIT.equals(r.getNamespaceURI())
                                && name.equals("situationRecord")) {
                            rec = new Rec(stripPrefix(r.getAttributeValue(XSI, "type")));
                        } else if (rec != null && name.equals("generalPublicComment")) {
                            commentDepth++;
                        } else if (rec != null && name.equals("pointCoordinates")) {
                            inPoint = true;
                            lat = null;
                            lon = null;
                        }
                        text.setLength(0);
                    }
                    case XMLStreamConstants.CHARACTERS, XMLStreamConstants.CDATA -> text.append(r.getText());
                    case XMLStreamConstants.END_ELEMENT -> {
                        String name = r.getLocalName();
                        String value = text.toString().strip();
                        text.setLength(0);
                        if (situation == null) {
                            break;
                        }
                        if (SIT.equals(r.getNamespaceURI()) && name.equals("situation")) {
                            situation.emit(out, now);
                            situation = null;
                            break;
                        }
                        if (rec == null) {
                            if (name.equals("overallSeverity")) {
                                situation.severity = value;
                            } else if (name.equals("situationVersionTime")) {
                                situation.versionTime = RoadEvents.instant(value);
                            }
                            break;
                        }
                        if (SIT.equals(r.getNamespaceURI()) && name.equals("situationRecord")) {
                            situation.records.add(rec);
                            rec = null;
                            break;
                        }
                        switch (name) {
                            case "overallStartTime" -> rec.start = RoadEvents.instant(value);
                            case "overallEndTime" -> rec.end = RoadEvents.instant(value);
                            case "causeType" -> rec.cause = first(rec.cause, value);
                            case "temporarySpeedLimit" -> rec.speed = number(value);
                            case "numberOfLanesRestricted" -> rec.lanesRestricted = integer(value);
                            case "originalNumberOfLanes" -> rec.lanesOriginal = integer(value);
                            case "alertCAffectedDirection", "alertCDirectionCoded" ->
                                    rec.direction = first(rec.direction, value);
                            case "posList" -> rec.addLine(value);
                            case "latitude" -> lat = inPoint ? number(value) : lat;
                            case "longitude" -> lon = inPoint ? number(value) : lon;
                            case "pointCoordinates" -> {
                                rec.addPoint(lat, lon);
                                inPoint = false;
                            }
                            case "generalPublicComment" -> commentDepth = Math.max(0, commentDepth - 1);
                            case "value" -> {
                                if (commentDepth > 0 && !value.isEmpty()) {
                                    rec.comments.add(value);
                                }
                            }
                            default -> {
                                if (DETAIL_ELEMENTS.contains(name)) {
                                    rec.detail = first(rec.detail, value);
                                    if (name.equals("roadOrCarriagewayOrLaneManagementType")) {
                                        rec.management = first(rec.management, value);
                                    }
                                }
                            }
                        }
                    }
                    default -> {
                        // comments, processing instructions, whitespace
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new UpstreamFormatException("the DATEX II document could not be parsed: " + e.getMessage(), "", e);
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (XMLStreamException ignored) {
                    // nothing to release
                }
            }
        }
        if (!sawRoot) {
            throw new UpstreamFormatException("the DATEX II document has no element", "");
        }
        return out;
    }

    /**
     * {@code body} decompressed when it starts with the gzip magic bytes, else unchanged.
     *
     * @throws UpstreamFormatException when the gzip stream is corrupt or inflates beyond
     *     {@value #MAX_DECOMPRESSED_BYTES} bytes
     */
    static byte[] gunzipIfNeeded(byte[] body) {
        if (body == null || body.length < 2 || (body[0] & 0xFF) != 0x1F || (body[1] & 0xFF) != 0x8B) {
            return body == null ? new byte[0] : body;
        }
        try (InputStream in = new GZIPInputStream(new ByteArrayInputStream(body))) {
            byte[] out = in.readNBytes((int) MAX_DECOMPRESSED_BYTES + 1);
            if (out.length > MAX_DECOMPRESSED_BYTES) {
                throw new UpstreamFormatException("the DATEX II document inflates beyond "
                        + MAX_DECOMPRESSED_BYTES + " bytes", "");
            }
            return out;
        } catch (IOException e) {
            throw new UpstreamFormatException("the DATEX II gzip stream is corrupt: " + e.getMessage(), "", e);
        }
    }

    private static String stripPrefix(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        int colon = type.indexOf(':');
        return colon < 0 ? type.strip() : type.substring(colon + 1).strip();
    }

    private static String first(String current, String value) {
        return current != null || value == null || value.isEmpty() ? current : value;
    }

    private static Double number(String value) {
        try {
            double d = Double.parseDouble(value);
            return Double.isFinite(d) ? d : null;
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    private static Integer integer(String value) {
        try {
            return Integer.valueOf(value.strip());
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    /** One {@code sit:situationRecord} while it is read. */
    private static final class Rec {
        final String type;
        Instant start;
        Instant end;
        String cause;
        String detail;
        String management;
        Double speed;
        Integer lanesRestricted;
        Integer lanesOriginal;
        String direction;
        final List<String> comments = new ArrayList<>();
        final List<Geometry> parts = new ArrayList<>();

        Rec(String type) {
            this.type = type;
        }

        /** Adds a {@code posList} of "lat lon" pairs as a line (a point when it has one pair). */
        void addLine(String posList) {
            String[] tokens = posList.strip().split("\\s+");
            List<Coordinate> coords = new ArrayList<>();
            for (int i = 0; i + 1 < tokens.length; i += 2) {
                Double la = number(tokens[i]);
                Double lo = number(tokens[i + 1]);
                if (la == null || lo == null || Math.abs(la) > 90 || Math.abs(lo) > 180) {
                    return;
                }
                Coordinate c = new Coordinate(lo, la);
                if (coords.isEmpty() || !coords.getLast().equals2D(c)) {
                    coords.add(c);
                }
            }
            GeometryFactory gf = Geo.factory();
            if (coords.size() == 1) {
                parts.add(gf.createPoint(coords.getFirst()));
            } else if (coords.size() > 1) {
                parts.add(gf.createLineString(coords.toArray(Coordinate[]::new)));
            }
        }

        void addPoint(Double lat, Double lon) {
            if (lat != null && lon != null && Math.abs(lat) <= 90 && Math.abs(lon) <= 180) {
                parts.add(Geo.factory().createPoint(new Coordinate(lon, lat)));
            }
        }
    }

    /** One {@code sit:situation} while it is read. */
    private static final class Situation {
        final String id;
        String severity;
        Instant versionTime;
        final List<Rec> records = new ArrayList<>();

        Situation(String id) {
            this.id = id == null || id.isBlank() ? null : id.strip();
        }

        void emit(ParsedEvents out, Instant now) {
            if (id == null) {
                out.skip(ParsedEvents.MISSING_ID);
                return;
            }
            if (versionTime == null) {
                out.skip(ParsedEvents.MISSING_OBSERVED_AT);
                return;
            }
            Rec first = records.isEmpty() ? null : records.getFirst();
            String type = first == null ? null : first.type;
            boolean closure = records.stream().anyMatch(x -> x.management != null
                    && CLOSURE_TYPES.contains(x.management));
            Set<String> comments = new LinkedHashSet<>();
            records.forEach(x -> comments.addAll(x.comments));
            RoadEventPayload p = new RoadEventPayload("datex3")
                    .category(category(type, first == null ? null : first.management,
                            first == null ? null : first.cause))
                    .subtype(first == null ? null : code(first))
                    .severity(severity == null ? "unknown" : SEVERITY.getOrDefault(severity, "unknown"))
                    .description(RoadEventPayload.join(List.copyOf(comments)))
                    .isClosure(closure)
                    .language("nl");
            String headline = comments.isEmpty() ? null : RoadEventPayload.firstSentence(comments.iterator().next());
            if (headline == null && first != null) {
                String what = RoadEventPayload.words(code(first));
                String cause = RoadEventPayload.words(first.cause);
                headline = what == null ? null
                        : cause == null || cause.equalsIgnoreCase("other") ? what
                        : what + " (" + cause.toLowerCase(java.util.Locale.ROOT) + ")";
            }
            p.headline(headline);
            String lanes = null;
            Double speed = null;
            String direction = null;
            Instant start = null;
            Instant end = null;
            boolean allEnd = !records.isEmpty();
            for (Rec x : records) {
                if (lanes == null && x.lanesRestricted != null) {
                    lanes = x.lanesRestricted + (x.lanesOriginal == null ? "" : " of " + x.lanesOriginal)
                            + " lanes restricted";
                }
                if (x.speed != null && (speed == null || x.speed < speed)) {
                    speed = x.speed;
                }
                direction = direction == null ? x.direction : direction;
                if (x.start != null && (start == null || x.start.isBefore(start))) {
                    start = x.start;
                }
                if (x.end == null) {
                    allEnd = false;
                } else if (end == null || x.end.isAfter(end)) {
                    end = x.end;
                }
            }
            if (lanes == null) {
                for (Rec x : records) {
                    if (x.management != null && x.management.toLowerCase(java.util.Locale.ROOT).contains("lane")) {
                        lanes = RoadEventPayload.kebab(x.management);
                        break;
                    }
                }
            }
            Instant until = allEnd ? end : null;
            p.lanesAffected(lanes).speedLimitKph(speed)
                    .direction(direction == null ? null : "both".equals(direction) ? "both" : "unknown")
                    .startsAt(start).endsAt(until).statusFrom(start, now);
            FeatureGeometry.Result geometry = combine(records);
            out.add(RoadEvents.record(id, versionTime, geometry, start, until, p));
        }

        /** The record's own type code, or its record type when the code is absent or {@code other}. */
        private static String code(Rec rec) {
            return rec.detail != null && !rec.detail.equalsIgnoreCase("other") ? rec.detail : rec.type;
        }

        private static FeatureGeometry.Result combine(List<Rec> records) {
            List<Geometry> parts = new ArrayList<>();
            Set<String> seen = new LinkedHashSet<>();
            for (Rec x : records) {
                for (Geometry g : x.parts) {
                    if (seen.add(g.toText())) {
                        parts.add(g);
                    }
                }
            }
            if (parts.isEmpty()) {
                return FeatureGeometry.Result.NONE;
            }
            GeometryFactory gf = Geo.factory();
            Geometry geometry;
            if (parts.size() == 1) {
                geometry = parts.getFirst();
            } else if (parts.stream().allMatch(g -> g instanceof LineString)) {
                geometry = gf.createMultiLineString(parts.stream().map(g -> (LineString) g)
                        .toArray(LineString[]::new));
            } else if (parts.stream().allMatch(g -> g instanceof Point)) {
                geometry = gf.createMultiPoint(parts.stream().map(g -> (Point) g).toArray(Point[]::new));
            } else {
                geometry = gf.createGeometryCollection(parts.toArray(Geometry[]::new));
            }
            geometry.setSRID(Geo.SRID);
            return FeatureGeometry.fit(geometry);
        }
    }
}
