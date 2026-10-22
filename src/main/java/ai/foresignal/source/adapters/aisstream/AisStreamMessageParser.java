package ai.foresignal.source.adapters.aisstream;

// Origin: liveness recognition, the auth classification text and the sentinel rules are ported from
// God's Eye View (MIT, Copyright (c) 2026 Bilawal Sidhu): src/data/aisStreamAdapter.js
// (AIS_RECOGNIZED_MESSAGE_TYPES, aisEnvelopeMmsi, isRecognizedAisEnvelope, parseAisEnvelope, AUTH_TEXT) and
// server/providers/vessels/ais-store.js (normalizedSpeedOverGround, normalizedCourseOverGround,
// normalizedHeading, normalizeAisTimestamp). See THIRD_PARTY_NOTICES.md.

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses one aisstream.io message (prompt 063). Pure: no clock, no state, no network.
 *
 * <p>Each text becomes exactly one {@link Result}:
 *
 * <ul>
 *   <li>{@link AisMessage}: a recognized position or static message, scrubbed of sentinels;
 *   <li>{@link Ignored}: a recognized envelope of a type the vessel merge does not use;
 *   <li>{@link Untimed}: a recognized envelope without a parseable {@code time_utc}, dropped and counted,
 *       never stamped with the receive time (Core-Data-Model §2: the time base is fixed);
 *   <li>{@link Confirmation}: the {@code SubscriptionConfirmation}, with {@code CompressionEnabled};
 *   <li>{@link ErrorEnvelope}: {@code {"error": "…"}}, with {@link ErrorEnvelope#auth()} when the text names
 *       a credential problem;
 *   <li>{@link Unrecognized}: an object that is not AIS traffic ({@code unknown_type} or {@code no_mmsi});
 *   <li>{@link Malformed}: not a JSON object.
 * </ul>
 *
 * <p><strong>Liveness</strong> ({@link Result#liveness()}) is true only for a recognized envelope: a
 * {@code MessageType} from aisstream's complete {@code AisMessageTypes} enum, a body under
 * {@code Message.<MessageType>}, and an MMSI ({@code MetaData.MMSI}, else the body's {@code UserID}). A
 * confirmation is not liveness.
 *
 * <p>{@code MetaData} keys are read case-insensitively: verified samples use both {@code latitude} and
 * {@code Latitude}, {@code time_utc} and {@code TimeUtc}.
 */
public final class AisStreamMessageParser {

    /** aisstream's complete {@code AisMessageTypes} enum (ais-message-models type-definition.yaml). */
    public static final Set<String> RECOGNIZED_TYPES = Set.of(
            "PositionReport", "UnknownMessage", "AddressedSafetyMessage", "AddressedBinaryMessage",
            "AidsToNavigationReport", "AssignedModeCommand", "BaseStationReport", "BinaryAcknowledge",
            "BinaryBroadcastMessage", "ChannelManagement", "CoordinatedUTCInquiry", "DataLinkManagementMessage",
            "DataLinkManagementMessageData", "ExtendedClassBPositionReport", "GroupAssignmentCommand",
            "GnssBroadcastBinaryMessage", "Interrogation", "LongRangeAisBroadcastMessage", "MultiSlotBinaryMessage",
            "SafetyBroadcastMessage", "ShipStaticData", "SingleSlotBinaryMessage", "StandardClassBPositionReport",
            "StandardSearchAndRescueAircraftReport", "StaticDataReport");

    /** The message type of the subscription confirmation. */
    public static final String CONFIRMATION_TYPE = "SubscriptionConfirmation";

    /** The Class A position report. */
    public static final String POSITION_REPORT = "PositionReport";
    /** The Class B position report. */
    public static final String STANDARD_CLASS_B = "StandardClassBPositionReport";
    /** The extended Class B position report (position plus name, type and dimensions). */
    public static final String EXTENDED_CLASS_B = "ExtendedClassBPositionReport";
    /** The Class A static and voyage data. */
    public static final String SHIP_STATIC_DATA = "ShipStaticData";
    /** The Class B static data report, in parts A and B. */
    public static final String STATIC_DATA_REPORT = "StaticDataReport";

    /** The five types the vessel merge uses, which are also the {@code messageTypes} parameter's values. */
    public static final java.util.List<String> MERGED_TYPES = java.util.List.of(
            POSITION_REPORT, STANDARD_CLASS_B, EXTENDED_CLASS_B, SHIP_STATIC_DATA, STATIC_DATA_REPORT);

    /** Upstream text that identifies a credential rejection (GEV {@code AUTH_TEXT}). */
    static final Pattern AUTH_TEXT = Pattern.compile(
            "(unauthoriz|unauthoris|forbidden|invalid\\s*api[\\s_-]*key|invalid\\s*key|bad\\s*api[\\s_-]*key"
                    + "|missing\\s*api[\\s_-]*key|authentic|api\\s*key\\s*(is\\s*)?(invalid|required|missing|not\\s*valid))",
            Pattern.CASE_INSENSITIVE);

    /** SOG at or above this is "not available" (1023 in 0.1 kn). */
    static final double SOG_NOT_AVAILABLE = 102.3;
    /** COG at or above this is "not available" (3600 in 0.1°). */
    static final double COG_NOT_AVAILABLE = 360.0;
    /** True heading "not available". */
    static final int HEADING_NOT_AVAILABLE = 511;
    /** Navigational status "not defined". */
    static final int NAV_STATUS_NOT_DEFINED = 15;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** {@code 2023-05-10 11:46:52.509865357 +0000 UTC}, the fraction optional, the zone name optional. */
    private static final DateTimeFormatter TIME_UTC = new DateTimeFormatterBuilder()
            .appendPattern("uuuu-MM-dd HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .optionalEnd()
            .appendLiteral(' ')
            .appendOffset("+HHMM", "+0000")
            .optionalStart()
            .appendLiteral(" UTC")
            .optionalEnd()
            .toFormatter(Locale.ROOT);

    private AisStreamMessageParser() {}

    // ---------- results ----------

    /** What one message turned out to be. */
    public sealed interface Result
            permits AisMessage, Ignored, Untimed, Confirmation, ErrorEnvelope, Unrecognized, Malformed {

        /** True only for a recognized AIS envelope: the stream's sole proof of liveness. */
        default boolean liveness() {
            return false;
        }
    }

    /** A recognized envelope of a type the merge does not use (liveness, counted, not merged). */
    public record Ignored(String messageType, String mmsi) implements Result {
        @Override
        public boolean liveness() {
            return true;
        }
    }

    /** A recognized envelope without a parseable time (liveness, dropped with {@code reason=untimed}). */
    public record Untimed(String messageType, String mmsi) implements Result {
        @Override
        public boolean liveness() {
            return true;
        }
    }

    /** The subscription confirmation; {@code compressionEnabled} is null when the field was absent. */
    public record Confirmation(Boolean compressionEnabled) implements Result {}

    /**
     * An {@code {"error": "…"}} envelope.
     *
     * @param text the upstream's text, which never contains the API key we sent
     * @param auth true when the text names a credential problem
     */
    public record ErrorEnvelope(String text, boolean auth) implements Result {}

    /**
     * A JSON object that is not AIS traffic.
     *
     * @param reason {@code unknown_type} or {@code no_mmsi}
     * @param messageType the {@code MessageType} as sent, or null
     */
    public record Unrecognized(String reason, String messageType) implements Result {}

    /** Not a JSON object. */
    public record Malformed() implements Result {}

    // ---------- parsing ----------

    /** Parses one message. Never throws. */
    public static Result parse(String json) {
        if (json == null || json.isBlank()) {
            return new Malformed();
        }
        JsonNode root;
        try {
            root = JSON.readTree(json);
        } catch (JacksonException e) {
            return new Malformed();
        }
        if (root == null || !root.isObject()) {
            return new Malformed();
        }
        JsonNode error = root.get("error");
        if (error != null && !error.isNull()) {
            String text = error.isString() ? error.stringValue() : error.toString();
            return new ErrorEnvelope(text, isAuthText(text));
        }
        JsonNode typeNode = root.get("MessageType");
        String type = typeNode != null && typeNode.isString() ? typeNode.stringValue() : null;
        if (CONFIRMATION_TYPE.equals(type)) {
            JsonNode message = root.get("Message");
            JsonNode compression = message == null ? null : message.get("CompressionEnabled");
            return new Confirmation(compression != null && compression.isBoolean() ? compression.booleanValue() : null);
        }
        if (type == null || !RECOGNIZED_TYPES.contains(type)) {
            return new Unrecognized("unknown_type", type);
        }
        JsonNode messages = root.get("Message");
        JsonNode body = messages == null ? null : messages.get(type);
        if (body == null || !body.isObject()) {
            return new Unrecognized("unknown_type", type);
        }
        Map<String, JsonNode> meta = metadata(root);
        String mmsi = mmsi(meta.get("mmsi"));
        if (mmsi == null) {
            mmsi = mmsi(firstPresent(body, "UserID", "UserId", "Mmsi"));
        }
        if (mmsi == null) {
            return new Unrecognized("no_mmsi", type);
        }
        if (!MERGED_TYPES.contains(type)) {
            return new Ignored(type, mmsi);
        }
        Instant time = parseTime(text(firstNonNull(meta.get("time_utc"), meta.get("timeutc"))));
        if (time == null) {
            return new Untimed(type, mmsi);
        }
        return switch (type) {
            case POSITION_REPORT -> position(type, mmsi, time, "A", body, meta, true);
            case STANDARD_CLASS_B -> position(type, mmsi, time, "B", body, meta, false);
            case EXTENDED_CLASS_B -> extendedClassB(mmsi, time, body, meta);
            case SHIP_STATIC_DATA -> shipStatic(mmsi, time, body);
            default -> staticReport(mmsi, time, body);
        };
    }

    /** True when {@code text} names a credential problem (GEV {@code AUTH_TEXT}). */
    public static boolean isAuthText(String text) {
        return text != null && AUTH_TEXT.matcher(text).find();
    }

    /**
     * Parses an aisstream {@code time_utc} to nanosecond precision, for example
     * {@code 2023-05-10 11:46:52.509865357 +0000 UTC}; ISO-8601 is accepted too.
     *
     * @return the instant, or null when absent or unparseable
     */
    public static Instant parseTime(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(text, TIME_UTC).toInstant();
        } catch (DateTimeParseException e) {
            // fall through to ISO-8601
        }
        try {
            return OffsetDateTime.parse(text).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // ---------- message types ----------

    private static AisMessage position(String type, String mmsi, Instant time, String aisClass, JsonNode body,
            Map<String, JsonNode> meta, boolean classA) {
        Double[] latLon = latLon(body, meta);
        return new AisMessage(type, mmsi, time, aisClass, true, latLon[0], latLon[1],
                sog(number(body.get("Sog"))), cog(number(body.get("Cog"))), heading(number(body.get("TrueHeading"))),
                classA ? navStatus(number(body.get("NavigationalStatus"))) : null,
                null, null, null, null, null, null, null, null);
    }

    private static AisMessage extendedClassB(String mmsi, Instant time, JsonNode body, Map<String, JsonNode> meta) {
        Double[] latLon = latLon(body, meta);
        return new AisMessage(EXTENDED_CLASS_B, mmsi, time, "B", true, latLon[0], latLon[1],
                sog(number(body.get("Sog"))), cog(number(body.get("Cog"))), heading(number(body.get("TrueHeading"))),
                null, cleanText(body.get("Name")), null, null, positiveInt(body.get("Type")),
                dimension(body.get("Dimension")), null, null, null);
    }

    private static AisMessage shipStatic(String mmsi, Instant time, JsonNode body) {
        Double draft = number(body.get("MaximumStaticDraught"));
        return new AisMessage(SHIP_STATIC_DATA, mmsi, time, "A", false, null, null, null, null, null, null,
                cleanText(body.get("Name")), cleanText(body.get("CallSign")), positiveInt(body.get("ImoNumber")),
                positiveInt(body.get("Type")), dimension(body.get("Dimension")), eta(body.get("Eta")),
                draft != null && draft > 0 ? draft : null, cleanText(body.get("Destination")));
    }

    private static AisMessage staticReport(String mmsi, Instant time, JsonNode body) {
        JsonNode a = body.get("ReportA");
        JsonNode b = body.get("ReportB");
        JsonNode partNumber = body.get("PartNumber");
        // PartNumber false is part A, true is part B; each part also carries its own Valid flag.
        boolean partB = partNumber != null && partNumber.isBoolean() ? partNumber.booleanValue()
                : valid(b) && !valid(a);
        String name = null;
        String callsign = null;
        Integer shipType = null;
        AisMessage.Dimension dimension = null;
        if (!partB && a != null && valid(a)) {
            name = cleanText(a.get("Name"));
        }
        if (partB && b != null && valid(b)) {
            callsign = cleanText(b.get("CallSign"));
            shipType = positiveInt(b.get("ShipType"));
            dimension = dimension(b.get("Dimension"));
        }
        return new AisMessage(STATIC_DATA_REPORT, mmsi, time, "B", false, null, null, null, null, null, null,
                name, callsign, null, shipType, dimension, null, null, null);
    }

    // ---------- fields and sentinels ----------

    private static Map<String, JsonNode> metadata(JsonNode root) {
        JsonNode meta = root.get("MetaData");
        if (meta == null) {
            meta = root.get("Metadata");
        }
        Map<String, JsonNode> lowered = new HashMap<>();
        if (meta != null && meta.isObject()) {
            for (Map.Entry<String, JsonNode> entry : meta.properties()) {
                lowered.putIfAbsent(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue());
            }
        }
        return lowered;
    }

    /** Body {@code Latitude}/{@code Longitude}, else the metadata's; null for 91/181 or out of range. */
    private static Double[] latLon(JsonNode body, Map<String, JsonNode> meta) {
        Double lat = number(body.get("Latitude"));
        Double lon = number(body.get("Longitude"));
        if (lat == null || lon == null) {
            lat = number(meta.get("latitude"));
            lon = number(meta.get("longitude"));
        }
        if (lat == null || lon == null || lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            return new Double[] {null, null};
        }
        return new Double[] {lat, lon};
    }

    /** SOG ≥ 102.3 is "not available" (GEV {@code normalizedSpeedOverGround}). */
    static Double sog(Double value) {
        return value != null && value >= 0 && value < SOG_NOT_AVAILABLE ? value : null;
    }

    /** COG ≥ 360 is "not available" (GEV {@code normalizedCourseOverGround}). */
    static Double cog(Double value) {
        return value != null && value >= 0 && value < COG_NOT_AVAILABLE ? value : null;
    }

    /** Heading 511, or anything above 359, is "not available". */
    static Integer heading(Double value) {
        if (value == null || value != Math.rint(value) || value < 0 || value > 359) {
            return null;
        }
        return value.intValue();
    }

    /** Status 15 is "not defined"; 0–14 are kept. */
    static Integer navStatus(Double value) {
        if (value == null || value != Math.rint(value) || value < 0 || value >= NAV_STATUS_NOT_DEFINED) {
            return null;
        }
        return value.intValue();
    }

    private static AisMessage.Dimension dimension(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        AisMessage.Dimension dimension = new AisMessage.Dimension(
                nonNegativeInt(node.get("A")), nonNegativeInt(node.get("B")),
                nonNegativeInt(node.get("C")), nonNegativeInt(node.get("D")));
        return dimension.isZero() ? null : dimension;
    }

    /** The ETA, or null when any part is "not available" (month 0, day 0, hour 24, minute 60) or invalid. */
    private static AisMessage.Eta eta(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        Integer month = intValue(node.get("Month"));
        Integer day = intValue(node.get("Day"));
        Integer hour = intValue(node.get("Hour"));
        Integer minute = intValue(node.get("Minute"));
        if (month == null || day == null || hour == null || minute == null
                || month < 1 || month > 12 || day < 1 || day > 31 || hour < 0 || hour > 23 || minute < 0
                || minute > 59) {
            return null;
        }
        return new AisMessage.Eta(month, day, hour, minute);
    }

    private static boolean valid(JsonNode report) {
        if (report == null || !report.isObject()) {
            return false;
        }
        JsonNode valid = report.get("Valid");
        return valid == null || !valid.isBoolean() || valid.booleanValue();
    }

    /** A nine-digit MMSI from a number or a digit string, zero-padded; null otherwise. */
    static String mmsi(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String digits;
        if (node.isIntegralNumber()) {
            long value = node.longValue();
            if (value <= 0 || value > 999_999_999L) {
                return null;
            }
            digits = Long.toString(value);
        } else if (node.isString()) {
            digits = node.stringValue().trim();
            if (!digits.matches("[0-9]{1,9}") || Long.parseLong(digits) == 0) {
                return null;
            }
        } else {
            return null;
        }
        return "0".repeat(9 - digits.length()) + digits;
    }

    /** Trims spaces and AIS {@code @} padding; null for an empty result. */
    static String cleanText(JsonNode node) {
        String value = text(node);
        if (value == null) {
            return null;
        }
        int end = value.length();
        while (end > 0 && (value.charAt(end - 1) == '@' || Character.isWhitespace(value.charAt(end - 1)))) {
            end--;
        }
        int start = 0;
        while (start < end && (value.charAt(start) == '@' || Character.isWhitespace(value.charAt(start)))) {
            start++;
        }
        String cleaned = value.substring(start, end);
        return cleaned.isEmpty() ? null : cleaned;
    }

    private static String text(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.isString() ? node.stringValue() : node.isValueNode() ? node.asString() : null;
    }

    private static Double number(JsonNode node) {
        if (node == null || !node.isNumber()) {
            return null;
        }
        double value = node.doubleValue();
        return Double.isFinite(value) ? value : null;
    }

    private static Integer intValue(JsonNode node) {
        Double value = number(node);
        if (value == null || value != Math.rint(value) || Math.abs(value) > Integer.MAX_VALUE) {
            return null;
        }
        return value.intValue();
    }

    private static Integer positiveInt(JsonNode node) {
        Integer value = intValue(node);
        return value != null && value > 0 ? value : null;
    }

    private static int nonNegativeInt(JsonNode node) {
        Integer value = intValue(node);
        return value != null && value > 0 ? value : 0;
    }

    private static JsonNode firstPresent(JsonNode body, String... names) {
        for (String name : names) {
            JsonNode node = body.get(name);
            if (node != null && !node.isNull()) {
                return node;
            }
        }
        return null;
    }

    private static JsonNode firstNonNull(JsonNode first, JsonNode second) {
        return first != null && !first.isNull() ? first : second;
    }
}
