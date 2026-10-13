package ai.foresignal.source.adapters.env.safecast;

import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import ai.foresignal.source.support.MappedRecord;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Safecast's realtime device list ({@code https://tt.safecast.org/devices}) as {@code radiation} records (prompt
 * 078). Pure.
 *
 * <p>Verified 2026-10-13: a JSON array of 2,086 devices (1.4 MB), each with {@code device_urn, device_class,
 * device, when_captured, loc_lat, loc_lon, loc_name, loc_country} and sensor fields; 522 carry at least one
 * {@code lnd_*} Geiger tube field in counts per minute ({@code lnd_7318u} 208, {@code lnd_7318c} 247, {@code
 * lnd_712u} 95, {@code lnd_7128ec} 57). Air-only devices (Airnote) carry {@code pms_*} and no {@code lnd_*}.
 *
 * <ul>
 *   <li>Only devices with at least one numeric {@code lnd_*} field are kept ({@value #NO_TUBE} otherwise);
 *       {@code externalId = device_urn}; a point from {@code loc_lat}/{@code loc_lon} ({@value
 *       EnvSources#MISSING_LOCATION} without one).
 *   <li>Payload {@code cpm}: an object of every tube field present, field name to CPM; {@code doseRateUsvH}: the
 *       approximate dose rate from {@code lnd_7318u} ÷ {@value #CPM_PER_USVH_7318U}, else {@code lnd_7128ec} ÷
 *       {@value #CPM_PER_USVH_7128EC} (Safecast's published conversion factors, rounded to 4 decimals); none for
 *       devices with only other tubes ({@code lnd_7318c}, {@code lnd_712u}), whose factor this prompt does not
 *       give; {@code locationName, country, deviceClass}.
 *   <li>{@code observedAt = eventTime = when_captured}; an unreadable time (the list holds {@code ""},
 *       {@code 2012-00-00T00:00:00Z} and {@code 2044-00-00T…}) is {@value MappedRecord#MISSING_OBSERVED_AT}.
 * </ul>
 */
public final class SafecastDevicesParser {

    /** The record kind. */
    public static final String KIND = "radiation";

    /** CPM per µSv/h of the LND 7318 (unshielded) tube, approximate. */
    public static final double CPM_PER_USVH_7318U = 334;

    /** CPM per µSv/h of the LND 7128 (energy-compensated) tube, approximate. */
    public static final double CPM_PER_USVH_7128EC = 108;

    /** The prefix of a Geiger tube field. */
    public static final String TUBE_PREFIX = "lnd_";

    /** The skip reason of a device without a tube field. */
    public static final String NO_TUBE = "no-radiation-tube";

    /** The default {@code maxAge}: older captures are skipped. */
    public static final Duration DEFAULT_MAX_AGE = Duration.ofHours(48);

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private SafecastDevicesParser() {}

    /**
     * Parses the device list.
     *
     * @param now the fetch time
     * @param maxAge the oldest capture kept
     * @throws UpstreamFormatException when the body is not a JSON array
     */
    public static EnvSources.Parsed parse(byte[] body, Instant now, Duration maxAge) {
        JsonNode root;
        try {
            root = body == null || body.length == 0 ? null : JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("Safecast devices answer is not JSON: " + e.getOriginalMessage(), "", e);
        }
        if (root == null || !root.isArray()) {
            throw new UpstreamFormatException("Safecast devices answer is not a JSON array", "");
        }
        EnvSources.Collector collector = new EnvSources.Collector(now, maxAge);
        for (JsonNode device : root) {
            collector.add(map(device));
        }
        return collector.result();
    }

    static MappedRecord map(JsonNode d) {
        Map<String, Object> cpm = new TreeMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = d.properties().iterator();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> f = fields.next();
            if (f.getKey().startsWith(TUBE_PREFIX)) {
                Double v = JsonProps.number(d, f.getKey());
                if (v != null && v >= 0) {
                    cpm.put(f.getKey(), v);
                }
            }
        }
        if (cpm.isEmpty()) {
            return MappedRecord.skipped(NO_TUBE);
        }
        String urn = JsonProps.text(d, "device_urn");
        if (urn == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        Instant at = JsonProps.iso(d, "when_captured");
        if (at == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_OBSERVED_AT);
        }
        Double lat = JsonProps.number(d, "loc_lat");
        Double lon = JsonProps.number(d, "loc_lon");
        if (!EnvSources.validPoint(lat, lon)) {
            return MappedRecord.skipped(EnvSources.MISSING_LOCATION);
        }
        return MappedRecord.of(NormalizedRecord.builder(urn, KIND)
                .observedAt(at)
                .eventTime(at)
                .point(lon, lat)
                .put("cpm", cpm)
                .put("doseRateUsvH", doseRate(cpm))
                .put("locationName", JsonProps.text(d, "loc_name"))
                .put("country", JsonProps.text(d, "loc_country"))
                .put("deviceClass", JsonProps.text(d, "device_class"))
                .build());
    }

    /** The approximate dose rate in µSv/h from the 7318u tube, else the 7128ec tube; null with neither. */
    static Double doseRate(Map<String, Object> cpm) {
        if (cpm.get("lnd_7318u") instanceof Double v) {
            return EnvSources.round(v / CPM_PER_USVH_7318U, 4);
        }
        if (cpm.get("lnd_7128ec") instanceof Double v) {
            return EnvSources.round(v / CPM_PER_USVH_7128EC, 4);
        }
        return null;
    }
}
