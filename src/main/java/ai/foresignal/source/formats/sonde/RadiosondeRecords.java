package ai.foresignal.source.formats.sonde;

import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import ai.foresignal.source.spi.SourceDescriptor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * The {@code radiosonde} record contract shared by {@code sondehub} and {@code local-sonde} (prompt 077; prompt 086
 * extends {@code local-sonde} with the same contract): one kind, one payload vocabulary, one set of field specs.
 *
 * <ul>
 *   <li><strong>Identity:</strong> the sonde serial.
 *   <li><strong>Geometry:</strong> the position as a 2D point; altitude is a payload value in meters
 *       ({@code altitudeM}, Adapter Guide altitude convention), never geometry Z. A record without a valid position
 *       has no geometry.
 *   <li><strong>Payload:</strong> {@code altitudeM, type, subtype, manufacturer, verticalSpeedMs, horizontalSpeedMs,
 *       headingDeg, tempC, humidityPct, pressureHpa, frequencyMHz}. Out-of-range sensor values (the radiosonde
 *       decoders' sentinels such as {@code -273} °C or a negative humidity) are dropped. Frame counters and
 *       battery voltage are never stored (counters and noise).
 *   <li><strong>Observer:</strong> the receiving station's id with its SNR and RSSI when known; a receiver's own
 *       position or antenna is never stored.
 * </ul>
 * Sensor values are uncalibrated telemetry.
 */
public final class RadiosondeRecords {

    /** The record kind. */
    public static final String KIND = "radiosonde";

    /** Every payload key, in contract order. */
    public static final List<String> PAYLOAD_FIELDS = List.of("altitudeM", "type", "subtype", "manufacturer",
            "verticalSpeedMs", "horizontalSpeedMs", "headingDeg", "tempC", "humidityPct", "pressureHpa",
            "frequencyMHz");

    /**
     * One radiosonde observation, before scrubbing.
     *
     * @param serial the sonde serial; required
     * @param observedAt the telemetry time
     * @param lat latitude, or null
     * @param lon longitude, or null
     * @param altitudeM altitude in meters, or null
     * @param type the sonde type ({@code RS41}, {@code M10}, {@code DFM})
     * @param subtype the subtype ({@code RS41-SGP}), or null
     * @param manufacturer the manufacturer ({@code Vaisala}), or null
     * @param verticalSpeedMs vertical speed in m/s, or null
     * @param horizontalSpeedMs horizontal speed in m/s, or null
     * @param headingDeg heading in degrees, or null
     * @param tempC temperature in °C, or null
     * @param humidityPct relative humidity in percent, or null
     * @param pressureHpa pressure in hPa, or null
     * @param frequencyMHz the transmit frequency in MHz, or null
     * @param receiverId the receiving station, or null
     * @param snr the receiver's SNR in dB, or null
     * @param rssi the receiver's RSSI, or null
     */
    public record Observation(String serial, Instant observedAt, Double lat, Double lon, Double altitudeM,
            String type, String subtype, String manufacturer, Double verticalSpeedMs, Double horizontalSpeedMs,
            Double headingDeg, Double tempC, Double humidityPct, Double pressureHpa, Double frequencyMHz,
            String receiverId, Double snr, Double rssi) {}

    private RadiosondeRecords() {}

    /**
     * The record of one observation: scrubbed, rounded (altitude and speeds to 0.1, heading to 1°, temperature and
     * humidity to 0.1, pressure to 0.01, frequency to 0.001), the observer attached.
     *
     * @throws IllegalArgumentException when the serial is blank
     */
    public static NormalizedRecord record(Observation o) {
        NormalizedRecord.Builder b = NormalizedRecord.builder(o.serial(), KIND).observedAt(o.observedAt());
        if (validPosition(o.lat(), o.lon())) {
            b.point(o.lon(), o.lat());
        }
        Double heading = o.headingDeg() == null || !Double.isFinite(o.headingDeg()) ? null
                : ((o.headingDeg() % 360) + 360) % 360;
        b.put("altitudeM", range(o.altitudeM(), -500, 60_000, 1))
                .put("type", blank(o.type()))
                .put("subtype", blank(o.subtype()))
                .put("manufacturer", blank(o.manufacturer()))
                .put("verticalSpeedMs", range(o.verticalSpeedMs(), -200, 200, 1))
                .put("horizontalSpeedMs", range(o.horizontalSpeedMs(), 0, 300, 1))
                .put("headingDeg", heading == null ? null : round(heading, 0) % 360)
                .put("tempC", range(o.tempC(), -120, 60, 1))
                .put("humidityPct", range(o.humidityPct(), 0, 100, 1))
                .put("pressureHpa", range(o.pressureHpa(), 0.001, 1100, 2))
                .put("frequencyMHz", range(o.frequencyMHz(), 1, 3000, 3));
        String receiver = blank(o.receiverId());
        if (receiver != null) {
            b.observers(List.of(new Observer(receiver, null, finite(o.rssi()), finite(o.snr()), null)));
        }
        return b.build();
    }

    /** True for a finite latitude and longitude in range. */
    public static boolean validPosition(Double lat, Double lon) {
        return lat != null && lon != null && Double.isFinite(lat) && Double.isFinite(lon) && Math.abs(lat) <= 90
                && Math.abs(lon) <= 180;
    }

    /** Declares the payload's field specs on {@code b}. */
    public static void fieldSpecs(SourceDescriptor.Builder b) {
        b.fieldSpec("altitudeM", spec(FieldType.NUMBER, "m", "Altitude", "Altitude in meters (GNSS)", "WGS84"));
        b.fieldSpec("type", spec(FieldType.STRING, null, "Type", "Sonde type (RS41, M10, DFM)", null));
        b.fieldSpec("subtype", spec(FieldType.STRING, null, "Subtype", "Sonde subtype (RS41-SGP)", null));
        b.fieldSpec("manufacturer", spec(FieldType.STRING, null, "Manufacturer", "Sonde manufacturer", null));
        b.fieldSpec("verticalSpeedMs", spec(FieldType.NUMBER, "m/s", "Vertical speed", "Ascent rate, negative when"
                + " descending", null));
        b.fieldSpec("horizontalSpeedMs", spec(FieldType.NUMBER, "m/s", "Horizontal speed", "Speed over ground", null));
        b.fieldSpec("headingDeg", spec(FieldType.NUMBER, "deg", "Heading", "Course over ground, degrees from true"
                + " north", null));
        b.fieldSpec("tempC", spec(FieldType.NUMBER, "Cel", "Temperature", "Air temperature, uncalibrated", null));
        b.fieldSpec("humidityPct", spec(FieldType.NUMBER, "%", "Humidity", "Relative humidity, uncalibrated", null));
        b.fieldSpec("pressureHpa", spec(FieldType.NUMBER, "hPa", "Pressure", "Air pressure, uncalibrated", null));
        b.fieldSpec("frequencyMHz", spec(FieldType.NUMBER, "MHz", "Frequency", "Transmit frequency", null));
    }

    private static FieldSpec spec(FieldType type, String unit, String label, String description, String datum) {
        return new FieldSpec(type, unit, label, description, datum, Set.of());
    }

    /** {@code v} rounded to {@code places}, or null outside {@code [min, max]} or not finite. */
    static Double range(Double v, double min, double max, int places) {
        if (v == null || !Double.isFinite(v) || v < min || v > max) {
            return null;
        }
        return round(v, places);
    }

    static double round(double v, int places) {
        double r = BigDecimal.valueOf(v).setScale(places, RoundingMode.HALF_UP).doubleValue();
        return r == 0.0 ? 0.0 : r;
    }

    private static Double finite(Double v) {
        return v == null || !Double.isFinite(v) ? null : v;
    }

    private static String blank(String s) {
        if (s == null) {
            return null;
        }
        String t = s.strip();
        return t.isEmpty() ? null : (t.length() > 64 ? t.substring(0, 64) : t);
    }
}
