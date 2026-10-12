package ai.foresignal.inbound.device;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ready-to-use tracker app settings for a device key (OpenAPI {@code IngestDeviceSetup}, prompt 039),
 * all based on {@code publicBaseUrl}.
 *
 * <ul>
 *   <li>{@code owntracks}: an {@code .otrc} configuration ({@code _type=configuration}, {@code mode} 3 =
 *       HTTP, {@code url}, {@code auth}, {@code username}, {@code password}, {@code deviceId},
 *       {@code tid}; key names from https://owntracks.org/booklet/tech/json/). The key is the Basic
 *       password; OwnTracks' {@code X-Limit-U}/{@code X-Limit-D} are ignored for a device key, which
 *       forces its bound externalId.
 *   <li>{@code overland}: the endpoint and the key as its bearer token.
 *   <li>{@code traccar}: the OsmAnd server URL with the key in the path (Traccar Client and OsmAnd
 *       cannot send credentials).
 *   <li>{@code gpslogger}: the custom-URL template with the key in the path, sent with GET.
 * </ul>
 */
public final class IngestDeviceSetup {

    /** The GPSLogger custom-URL query template (the app's documented placeholders). */
    public static final String GPSLOGGER_QUERY =
            "lat=%LAT&lon=%LON&time=%TIME&acc=%ACC&alt=%ALT&spd=%SPD&dir=%DIR&batt=%BATT&dev=%SER";

    private IngestDeviceSetup() {}

    /**
     * The setup object of a device.
     *
     * @param base the public base URL, without a trailing slash
     * @param device the device
     * @param key its plaintext key
     * @return the {@code IngestDeviceSetup} JSON object
     */
    public static Map<String, Object> of(String base, IngestDeviceEntity device, String key) {
        String root = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String source = root + "/v1/ingest/" + device.getSourceId();
        String keyed = source + "/k/" + key;

        Map<String, Object> owntracks = new LinkedHashMap<>();
        owntracks.put("_type", "configuration");
        owntracks.put("mode", 3);
        owntracks.put("url", source + "/owntracks");
        owntracks.put("auth", true);
        owntracks.put("username", "device");
        owntracks.put("password", key);
        owntracks.put("deviceId", deviceId(device.getExternalId()));
        owntracks.put("tid", tid(device.getName()));

        Map<String, Object> overland = new LinkedHashMap<>();
        overland.put("endpoint", source + "/overland");
        overland.put("token", key);

        Map<String, Object> traccar = new LinkedHashMap<>();
        traccar.put("serverUrl", keyed + "/osmand");

        Map<String, Object> gpslogger = new LinkedHashMap<>();
        gpslogger.put("urlTemplate", keyed + "/gpslogger?" + GPSLOGGER_QUERY);
        gpslogger.put("method", "GET");

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("owntracks", owntracks);
        out.put("overland", overland);
        out.put("traccar", traccar);
        out.put("gpslogger", gpslogger);
        return out;
    }

    /** An OwnTracks {@code deviceId}: the bound id, URL-safe (it becomes {@code X-Limit-D}). */
    static String deviceId(String externalId) {
        return URLEncoder.encode(externalId, StandardCharsets.UTF_8);
    }

    /** A two-character tracker id from the device name (letters and digits only), {@code FS} when none. */
    static String tid(String name) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < name.length() && out.length() < 2; i++) {
            char c = name.charAt(i);
            if (c < 128 && Character.isLetterOrDigit(c)) {
                out.append(Character.toUpperCase(c));
            }
        }
        return out.length() == 2 ? out.toString() : out.length() == 1 ? out + "X" : "FS";
    }
}
