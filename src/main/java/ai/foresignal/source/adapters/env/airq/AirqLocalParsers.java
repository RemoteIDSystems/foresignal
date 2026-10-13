package ai.foresignal.source.adapters.env.airq;

import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import java.util.Locale;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The local JSON of the three LAN air-quality monitor types of {@code airq-local-{n}} (prompt 078). Pure. Firmware
 * varies, so every field is optional and read tolerantly; a body that is not a JSON object, or that carries none
 * of the device's known fields, is an {@link UpstreamFormatException} so the health rules notice a changed shape.
 *
 * <ul>
 *   <li><strong>PurpleAir</strong> ({@code GET /json?live=true}; fields from PurpleAir's "Sensor JSON
 *       documentation", firmware 7.04): {@code SensorId, lat, lon, place} ({@code inside|outside}),
 *       {@code pm1_0_atm, pm2_5_atm, pm10_0_atm} (channel A) and the {@code _b} fields (channel B), {@code
 *       current_temp_f} (uncorrected; converted to °C, the sensor reads warm and the raw value is kept), {@code
 *       current_humidity}, {@code pressure} (millibar = hPa), {@code hardwareversion}. With both channels the PM
 *       values are their mean and {@code channelDisagreement} is true when PM2.5 A and B differ by more than
 *       {@value #DISAGREE_UG_M3} µg/m³ and more than {@value #DISAGREE_RELATIVE} of their mean.
 *   <li><strong>airrohr</strong> (Sensor.Community firmware, {@code GET /data.json}): {@code software_version},
 *       {@code sensordatavalues[{value_type, value}]}: {@code SDS_P1/P2}, {@code PMS_P0/P1/P2}, {@code
 *       SPS30_P0/P1/P2} (PM10, PM2.5, PM1); any {@code *_temperature}/{@code temperature}, {@code
 *       *_humidity}/{@code humidity} and {@code *_pressure}/{@code pressure} (pascals) value, the first of each
 *       kind in the body's order. No location and no serial.
 *   <li><strong>AirGradient</strong> ({@code GET /measures/current}; AirGradient's "Local Server API", firmware
 *       3.0.10 and later): {@code serialno, model, pm01, pm02, pm10, rco2, atmp, rhum, tvocIndex, noxIndex}; a
 *       model starting {@code I-} is indoor, {@code O-} outdoor.
 * </ul>
 */
public final class AirqLocalParsers {

    /** The outdoor kind. */
    public static final String OUTDOOR_KIND = "air-quality";

    /** The indoor kind. */
    public static final String INDOOR_KIND = "indoor-air-quality";

    /** The PurpleAir channel disagreement floor, µg/m³. */
    public static final double DISAGREE_UG_M3 = 5;

    /** The PurpleAir channel disagreement ratio to the channels' mean. */
    public static final double DISAGREE_RELATIVE = 0.7;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * What one device answered, in Foresignal units.
     *
     * @param serial the device serial, or null when the device names none (airrohr)
     * @param model the device model for {@code deviceModel}, or null
     * @param indoorHint what the device says about its placement, or null when it says nothing
     * @param lat the device's own latitude, or null
     * @param lon the device's own longitude, or null
     * @param pm1 PM1, µg/m³
     * @param pm25 PM2.5, µg/m³
     * @param pm10 PM10, µg/m³
     * @param tempC temperature, °C
     * @param humidityPct relative humidity, %
     * @param pressureHpa pressure, hPa
     * @param co2Ppm CO₂, ppm
     * @param tvocIndex Sensirion VOC index
     * @param noxIndex Sensirion NOx index
     * @param channelDisagreement PurpleAir A/B disagreement, or null without two channels
     */
    public record Reading(String serial, String model, Boolean indoorHint, Double lat, Double lon, Double pm1,
            Double pm25, Double pm10, Double tempC, Double humidityPct, Double pressureHpa, Double co2Ppm,
            Double tvocIndex, Double noxIndex, Boolean channelDisagreement) {

        /** True when at least one measured value is present. */
        public boolean hasValues() {
            return pm1 != null || pm25 != null || pm10 != null || tempC != null || humidityPct != null
                    || pressureHpa != null || co2Ppm != null || tvocIndex != null || noxIndex != null;
        }
    }

    private AirqLocalParsers() {}

    /**
     * Parses a PurpleAir {@code /json?live=true} body.
     *
     * @throws UpstreamFormatException when it is not a PurpleAir answer
     */
    public static Reading purpleAir(byte[] body) {
        JsonNode r = object(body, "PurpleAir /json");
        Double a = JsonProps.number(r, "pm2_5_atm");
        Double b = JsonProps.number(r, "pm2_5_atm_b");
        Boolean disagreement = a != null && b != null ? disagree(a, b) : null;
        Double tempF = JsonProps.number(r, "current_temp_f");
        String place = JsonProps.text(r, "place");
        String hardware = JsonProps.text(r, "hardwareversion");
        Reading reading = new Reading(JsonProps.text(r, "SensorId"),
                hardware == null ? "PurpleAir" : "PurpleAir " + hardware,
                place == null ? null : place.equalsIgnoreCase("inside"),
                JsonProps.number(r, "lat"), JsonProps.number(r, "lon"),
                mean(JsonProps.number(r, "pm1_0_atm"), JsonProps.number(r, "pm1_0_atm_b")),
                mean(a, b),
                mean(JsonProps.number(r, "pm10_0_atm"), JsonProps.number(r, "pm10_0_atm_b")),
                tempF == null ? null : fahrenheitToCelsius(tempF),
                JsonProps.number(r, "current_humidity"),
                EnvSources.round(JsonProps.number(r, "pressure"), 1),
                null, null, null, disagreement);
        return requireValues(reading, "PurpleAir /json");
    }

    /**
     * Parses an airrohr {@code /data.json} body.
     *
     * @throws UpstreamFormatException when it is not an airrohr answer
     */
    public static Reading airrohr(byte[] body) {
        JsonNode r = object(body, "airrohr /data.json");
        JsonNode values = r.get("sensordatavalues");
        if (values == null || !values.isArray()) {
            throw new UpstreamFormatException("airrohr /data.json has no sensordatavalues", "");
        }
        Double pm1 = null;
        Double pm25 = null;
        Double pm10 = null;
        Double temp = null;
        Double humidity = null;
        Double pressure = null;
        for (JsonNode v : values) {
            String type = JsonProps.text(v, "value_type");
            Double value = JsonProps.number(v, "value");
            if (type == null || value == null) {
                continue;
            }
            switch (type) {
                case "SDS_P1", "PMS_P1", "SPS30_P1" -> pm10 = pm10 == null ? value : pm10;
                case "SDS_P2", "PMS_P2", "SPS30_P2" -> pm25 = pm25 == null ? value : pm25;
                case "PMS_P0", "SPS30_P0" -> pm1 = pm1 == null ? value : pm1;
                default -> {
                    String t = type.toLowerCase(Locale.ROOT);
                    if ((t.equals("temperature") || t.endsWith("_temperature")) && temp == null) {
                        temp = EnvSources.round(value, 1);
                    } else if ((t.equals("humidity") || t.endsWith("_humidity")) && humidity == null) {
                        humidity = value;
                    } else if ((t.equals("pressure") || t.endsWith("_pressure")) && pressure == null) {
                        pressure = EnvSources.round(value > 2000 ? value / 100.0 : value, 1);
                    }
                }
            }
        }
        String firmware = JsonProps.text(r, "software_version");
        Reading reading = new Reading(null, firmware == null ? "airrohr" : "airrohr " + firmware, null, null, null,
                pm1, pm25, pm10, temp, humidity, pressure, null, null, null, null);
        return requireValues(reading, "airrohr /data.json");
    }

    /**
     * Parses an AirGradient {@code /measures/current} body.
     *
     * @throws UpstreamFormatException when it is not an AirGradient answer
     */
    public static Reading airGradient(byte[] body) {
        JsonNode r = object(body, "AirGradient /measures/current");
        String model = JsonProps.text(r, "model");
        Boolean indoor = null;
        if (model != null && model.length() > 1 && model.charAt(1) == '-') {
            char c = Character.toUpperCase(model.charAt(0));
            indoor = c == 'I' ? Boolean.TRUE : c == 'O' ? Boolean.FALSE : null;
        }
        Reading reading = new Reading(JsonProps.text(r, "serialno"), model, indoor, null, null,
                JsonProps.number(r, "pm01"), JsonProps.number(r, "pm02"), JsonProps.number(r, "pm10"),
                EnvSources.round(JsonProps.number(r, "atmp"), 1), JsonProps.number(r, "rhum"), null,
                JsonProps.number(r, "rco2"), JsonProps.number(r, "tvocIndex"), JsonProps.number(r, "noxIndex"), null);
        return requireValues(reading, "AirGradient /measures/current");
    }

    /**
     * The record of one reading.
     *
     * @param deviceType {@code purpleair}, {@code airrohr} or {@code airgradient}
     * @param fallbackId the id used when the device names no serial (the slot's source id)
     * @param indoorParam the operator's {@code indoor} parameter, or null to use the device's hint (default
     *     outdoor)
     * @param lat the operator's latitude, or null to use the device's
     * @param lon the operator's longitude, or null to use the device's
     * @throws UpstreamFormatException when no location is known
     */
    public static NormalizedRecord record(Reading reading, String deviceType, String fallbackId, Boolean indoorParam,
            Double lat, Double lon) {
        Double useLat = lat != null && lon != null ? lat : reading.lat();
        Double useLon = lat != null && lon != null ? lon : reading.lon();
        if (!EnvSources.validPoint(useLat, useLon)) {
            throw new UpstreamFormatException(deviceType + " reports no location; set the lat and lon parameters",
                    "");
        }
        boolean indoor = indoorParam != null ? indoorParam : Boolean.TRUE.equals(reading.indoorHint());
        String serial = reading.serial() == null ? fallbackId : reading.serial();
        return NormalizedRecord.builder(deviceType + ":" + serial, indoor ? INDOOR_KIND : OUTDOOR_KIND)
                .point(useLon, useLat)
                .put("pm1", reading.pm1())
                .put("pm25", reading.pm25())
                .put("pm10", reading.pm10())
                .put("tempC", reading.tempC())
                .put("humidityPct", reading.humidityPct())
                .put("pressureHpa", reading.pressureHpa())
                .put("co2Ppm", reading.co2Ppm())
                .put("tvocIndex", reading.tvocIndex())
                .put("noxIndex", reading.noxIndex())
                .put("deviceModel", reading.model())
                .put("channelDisagreement", reading.channelDisagreement())
                .build();
    }

    /** True when PM2.5 channels A and B differ by more than 5 µg/m³ and more than 70% of their mean. */
    static boolean disagree(double a, double b) {
        double diff = Math.abs(a - b);
        double mean = (a + b) / 2.0;
        return diff > DISAGREE_UG_M3 && mean > 0 && diff / mean > DISAGREE_RELATIVE;
    }

    /** Degrees Fahrenheit to Celsius, to 0.1. */
    static double fahrenheitToCelsius(double f) {
        return EnvSources.round((f - 32.0) * 5.0 / 9.0, 1);
    }

    private static Double mean(Double a, Double b) {
        if (a == null) {
            return b;
        }
        return b == null ? a : EnvSources.round((a + b) / 2.0, 2);
    }

    private static Reading requireValues(Reading reading, String what) {
        if (!reading.hasValues()) {
            throw new UpstreamFormatException(what + " carries none of the known measurement fields", "");
        }
        return reading;
    }

    private static JsonNode object(byte[] body, String what) {
        JsonNode root;
        try {
            root = body == null || body.length == 0 ? null : JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException(what + " is not JSON: " + e.getOriginalMessage(), "", e);
        }
        if (root == null || !root.isObject()) {
            throw new UpstreamFormatException(what + " is not a JSON object", "");
        }
        return root;
    }
}
