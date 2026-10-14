package ai.foresignal.source.formats.aprs;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * One parsed APRS packet (prompt 082), from {@link AprsParser#parse(String)}.
 *
 * <p>Units are SI: {@code speedMps} in m/s (from knots), {@code altitudeM} in meters (from feet, or meters for
 * Mic-E), {@code courseDeg} in degrees true. Absent values are null. An unparseable or unsupported packet has
 * {@link Type#UNSUPPORTED} and only the header fields, which adapters count and drop.
 *
 * @param raw the TNC2 line as received, without its line terminator
 * @param source the sender's call with SSID ({@code N0CALL-9}), or null when the header did not parse
 * @param destination the destination (tocall) field with SSID, or null
 * @param path the digipeater and q-construct path, in order
 * @param qConstruct the APRS-IS q-construct ({@code qAR}, {@code qAC}, ...), or null (RF packets have none)
 * @param igate the station after the q-construct (the igate or server that injected the packet), or null
 * @param type the packet type
 * @param info the information field (everything after the first {@code :})
 * @param latitude the latitude in degrees, or null
 * @param longitude the longitude in degrees, or null
 * @param symbolTable the symbol table identifier ({@code /}, {@code \} or an overlay character), or null
 * @param symbolCode the symbol code, or null
 * @param courseDeg the course in degrees, or null
 * @param speedMps the speed in m/s, or null
 * @param altitudeM the altitude in meters, or null
 * @param comment the comment or status text, control characters stripped, at most
 *     {@value AprsParser#MAX_COMMENT} characters, or null
 * @param objectName the object or item name, or null
 * @param killed true for a killed object or item
 * @param weather position-weather values (keys {@code windDirDeg, windSpeedMs, windGustMs, tempC, rainLastHourMm,
 *     rain24hMm, rainDailyMm, rh, pressureHpa}), only those present; empty otherwise
 * @param addressee a message's addressee, or null
 * @param messageText a message's text, or null
 * @param messageId a message's id, or null
 * @param ack true for a message acknowledgement or rejection ({@code ackNN}, {@code rejNN})
 * @param micEMessage the Mic-E message from the destination field ({@code En Route}, {@code Custom-3}, ...), or null
 * @param why for {@link Type#UNSUPPORTED}, why; otherwise null
 */
public record AprsPacket(
        String raw,
        String source,
        String destination,
        List<String> path,
        String qConstruct,
        String igate,
        Type type,
        String info,
        Double latitude,
        Double longitude,
        Character symbolTable,
        Character symbolCode,
        Double courseDeg,
        Double speedMps,
        Double altitudeM,
        String comment,
        String objectName,
        boolean killed,
        Map<String, Object> weather,
        String addressee,
        String messageText,
        String messageId,
        boolean ack,
        String micEMessage,
        String why) {

    /** The packet types the parser recognizes. */
    public enum Type {
        /** An uncompressed or compressed position report ({@code ! = / @}). */
        POSITION,
        /** A position report whose symbol is the weather station ({@code _}), with weather values. */
        WEATHER,
        /** A Mic-E position ({@code `} or {@code '}). */
        MIC_E,
        /** An object ({@code ;}). */
        OBJECT,
        /** An item ({@code )}). */
        ITEM,
        /** A status report ({@code >}). */
        STATUS,
        /** A message, bulletin, acknowledgement or rejection ({@code :}). */
        MESSAGE,
        /** Anything else, or a packet that did not parse. */
        UNSUPPORTED;

        /** The payload value: lower case, {@code MIC_E} as {@code mic-e}. */
        public String code() {
            return name().toLowerCase(Locale.ROOT).replace('_', '-');
        }
    }

    /** Copies the lists and maps. */
    public AprsPacket {
        path = path == null ? List.of() : List.copyOf(path);
        weather = weather == null ? Map.of() : java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(weather));
    }

    /** True when the packet carries a position. */
    public boolean hasPosition() {
        return latitude != null && longitude != null;
    }

    /** The symbol as table and code ({@code /&gt;}), or null. */
    public String symbol() {
        return symbolTable == null || symbolCode == null ? null : "" + symbolTable + symbolCode;
    }

    /** The entity this packet describes: the object or item name, else the source call. */
    public String entityId() {
        return (type == Type.OBJECT || type == Type.ITEM) && objectName != null ? objectName : source;
    }
}
