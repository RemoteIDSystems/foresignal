package ai.foresignal.source.adapters.radio;

import ai.foresignal.source.formats.aprs.AprsRecords;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.SourceDescriptor;

/**
 * The descriptor fields {@code aprs-is} and {@code local-aprs-{n}} share (prompt 082): the {@link AprsRecords}
 * payload's field specs, volatile fields, title, search and tile fields, and display hints. Not an adapter.
 */
final class AprsSourceFields {

    private AprsSourceFields() {}

    /** Applies the shared fields to {@code b}. */
    static void declare(SourceDescriptor.Builder b) {
        String station = AprsRecords.STATION_KIND;
        String message = AprsRecords.MESSAGE_KIND;
        b.volatileFields(AprsRecords.VOLATILE.toArray(String[]::new))
                .searchFields("comment", "status", "text")
                .tileProperties("symbol", "type", "speedMps", "courseDeg");
        RadioSources.field(b, "symbol", FieldType.STRING, null, "Symbol", "The APRS symbol: table and code (/>)",
                station);
        RadioSources.field(b, "comment", FieldType.STRING, null, "Comment", "The position comment, control characters"
                + " stripped, at most 256 characters (untrusted text)", station);
        RadioSources.field(b, "status", FieldType.STRING, null, "Status", "The latest status report (untrusted text)",
                station);
        RadioSources.field(b, "speedMps", FieldType.NUMBER, "m/s", "Speed", "Speed over ground", station);
        RadioSources.field(b, "courseDeg", FieldType.NUMBER, "deg", "Course", "Course over ground, degrees true",
                station);
        RadioSources.vertical(b, "altitudeM", "m", "Altitude", "Altitude (/A= feet converted, or Mic-E meters)",
                "MSL", station);
        RadioSources.field(b, "type", FieldType.STRING, null, "Packet type", "position, weather, mic-e, object, item"
                + " or status", station);
        RadioSources.field(b, "weather", FieldType.OBJECT, null, "Weather", "Position-weather values present in the"
                + " packet: windDirDeg, windSpeedMs, windGustMs, tempC, rainLastHourMm, rain24hMm, rainDailyMm, rh,"
                + " pressureHpa", station);
        RadioSources.field(b, "micEMessage", FieldType.STRING, null, "Mic-E message", "The Mic-E status from the"
                + " destination field (En Route, Off Duty, Emergency, Custom-N, ...)", station);
        RadioSources.field(b, "sender", FieldType.STRING, null, "Sender", "The station that placed the object or"
                + " item", station);
        RadioSources.field(b, "path", FieldType.STRING, null, "Path", "The digipeater and q-construct path"
                + " (volatile)", station, message);
        RadioSources.field(b, "raw", FieldType.STRING, null, "Raw packet", "The TNC2 line, at most 512 characters"
                + " (volatile)", station, message);
        RadioSources.field(b, "from", FieldType.STRING, null, "From", "The message's sender", message);
        RadioSources.field(b, "addressee", FieldType.STRING, null, "Addressee", "The message's addressee", message);
        RadioSources.field(b, "text", FieldType.STRING, null, "Text", "The message text (untrusted)", message);
        RadioSources.field(b, "msgId", FieldType.STRING, null, "Message id", "The message number", message);
        b.kindDisplay(station, new KindDisplay("{symbol} {comment}", "antenna", "#2B8A3E", null));
        b.kindDisplay(message, new KindDisplay("{from} to {addressee}", "information", "#495057", null));
    }
}
