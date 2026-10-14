package ai.foresignal.notify.mesh;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.notify.ChannelFieldSpec;
import ai.foresignal.settings.Durations;
import ai.foresignal.trigger.Severity;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The configuration both mesh channel types share (prompt 084, OpenAPI {@code MeshChannelCommonConfig}): the
 * regulatory region, the airtime preset, the governor's limits, the message profile, the channel's own minimum
 * severity and the field-report settings.
 *
 * <ul>
 *   <li>{@code region} (required): {@link MeshRegion}.
 *   <li>{@code airtimePreset}: {@code meshtastic-longfast} (default of {@code meshtastic}),
 *       {@code meshtastic-mediumfast} or {@code custom} (default of {@code meshcore}) with {@code airtimeCustom}
 *       {@code {spreadingFactor, bandwidthKhz, codingRate, preambleSymbols}}.
 *   <li>{@code dutyCyclePercent} (default 1.0, above 0, at most the region's ceiling and at most 10),
 *       {@code minSpacing} (default 30 s), {@code maxMessagesPerHour} (default 20).
 *   <li>{@code profile}: {@code text} (default) or {@code cbor}.
 *   <li>{@code minSeverity} (default {@code warning}): the channel takes nothing below it, whatever its top-level
 *       {@code minSeverity}.
 *   <li>{@code reportsEnabled} (default false) and {@code allowedSenders}: an object of sender to role
 *       ({@code report} or {@code ack}). A sender is a Meshtastic node id ({@code !xxxxxxxx}), a MeshCore key prefix
 *       ({@code mc:} and 12 hex digits) or, because MeshCore channel messages name their sender only by its advertised
 *       name, {@code name:} and that name.
 * </ul>
 */
public final class MeshChannels {

    /** The two mesh channel type ids. */
    public static final Set<String> TYPES = Set.of(MeshNetworks.MESHTASTIC, MeshNetworks.MESHCORE);

    /** The region key. */
    public static final String REGION = "region";
    /** The airtime preset key. */
    public static final String AIRTIME_PRESET = "airtimePreset";
    /** The custom radio settings key. */
    public static final String AIRTIME_CUSTOM = "airtimeCustom";
    /** The duty-cycle share key. */
    public static final String DUTY_CYCLE = "dutyCyclePercent";
    /** The minimum spacing key. */
    public static final String MIN_SPACING = "minSpacing";
    /** The hourly cap key. */
    public static final String MAX_PER_HOUR = "maxMessagesPerHour";
    /** The profile key. */
    public static final String PROFILE = "profile";
    /** The channel's own minimum severity key. */
    public static final String MIN_SEVERITY = "minSeverity";
    /** The field-report switch key. */
    public static final String REPORTS_ENABLED = "reportsEnabled";
    /** The allowed senders key. */
    public static final String ALLOWED_SENDERS = "allowedSenders";
    /** The channel index key. */
    public static final String CHANNEL_INDEX = "channelIndex";

    /** The text profile. */
    public static final String PROFILE_TEXT = "text";
    /** The CBOR profile. */
    public static final String PROFILE_CBOR = "cbor";

    /** The role that may send field reports. */
    public static final String ROLE_REPORT = "report";
    /** The role that may also acknowledge alerts. */
    public static final String ROLE_ACK = "ack";

    /** The default channel minimum severity. */
    public static final String DEFAULT_MIN_SEVERITY = "warning";

    /** A sender key: {@code !} + 8 hex, {@code mc:} + 12 hex, or {@code name:} + 1–32 characters. */
    static final Pattern SENDER = Pattern.compile("^(![0-9a-f]{8}|mc:[0-9a-f]{12}|name:.{1,32})$");

    private MeshChannels() {}

    /**
     * The shared settings of one channel, parsed.
     *
     * @param region the region
     * @param radio the radio settings for time on air
     * @param limits the governor's limits
     * @param profile {@code text} or {@code cbor}
     * @param minSeverity the channel's own minimum severity
     * @param channelIndex the private channel index, 1 to 7
     * @param reportsEnabled whether field reports are read
     * @param allowedSenders sender key (lower case, {@code name:} keys as written) to role
     */
    public record Common(MeshRegion region, LoRaAirtime radio, AirtimeGovernor.Limits limits, String profile,
            String minSeverity, int channelIndex, boolean reportsEnabled, Map<String, String> allowedSenders) {}

    /**
     * The shared settings of a validated config.
     *
     * @param defaultPreset the type's default {@code airtimePreset}
     * @throws IllegalArgumentException when the config does not validate
     */
    public static Common common(Map<String, Object> config, String defaultPreset) {
        MeshRegion region = MeshRegion.parse(config.get(REGION));
        if (region == null) {
            throw new IllegalArgumentException("region is required");
        }
        LoRaAirtime radio = LoRaAirtime.preset(string(config.get(AIRTIME_PRESET), defaultPreset),
                config.get(AIRTIME_CUSTOM) instanceof Map<?, ?> custom ? custom : null);
        double duty = config.get(DUTY_CYCLE) instanceof Number n ? n.doubleValue()
                : AirtimeGovernor.DEFAULT_DUTY_CYCLE_PERCENT;
        Duration spacing = config.get(MIN_SPACING) instanceof String s && !s.isBlank() ? Durations.parse(s)
                : AirtimeGovernor.DEFAULT_MIN_SPACING;
        int perHour = config.get(MAX_PER_HOUR) instanceof Number n ? n.intValue()
                : AirtimeGovernor.DEFAULT_MAX_MESSAGES_PER_HOUR;
        AirtimeGovernor.Limits limits = new AirtimeGovernor.Limits(region, duty, spacing, perHour);
        String profile = string(config.get(PROFILE), PROFILE_TEXT);
        int index = config.get(CHANNEL_INDEX) instanceof Number n ? n.intValue() : -1;
        return new Common(region, radio, limits, profile, minSeverityOf(config), index,
                Boolean.TRUE.equals(config.get(REPORTS_ENABLED)), senders(config.get(ALLOWED_SENDERS)));
    }

    /**
     * The checks of the shared settings beyond the generic field checks.
     *
     * @param defaultPreset the type's default {@code airtimePreset}
     */
    public static List<FieldError> validate(Map<String, Object> config, String defaultPreset) {
        List<FieldError> errors = new ArrayList<>();
        MeshRegion region = MeshRegion.parse(config.get(REGION));
        if (region == null) {
            errors.add(new FieldError("config." + REGION, "must be one of EU_868, EU_433, UK_868, US, ANZ, OTHER"));
        }
        try {
            LoRaAirtime.preset(string(config.get(AIRTIME_PRESET), defaultPreset),
                    config.get(AIRTIME_CUSTOM) instanceof Map<?, ?> custom ? custom : null);
        } catch (IllegalArgumentException e) {
            errors.add(new FieldError("config." + (e.getMessage().startsWith("must be") ? AIRTIME_PRESET
                    : AIRTIME_CUSTOM), e.getMessage()));
        }
        Object custom = config.get(AIRTIME_CUSTOM);
        if (custom != null && !(custom instanceof Map<?, ?>)) {
            errors.add(new FieldError("config." + AIRTIME_CUSTOM, "must be an object"));
        }
        Duration spacing = AirtimeGovernor.DEFAULT_MIN_SPACING;
        if (config.get(MIN_SPACING) instanceof String s && !s.isBlank()) {
            try {
                spacing = Durations.parse(s);
                if (spacing.isNegative() || spacing.compareTo(Duration.ofHours(1)) > 0) {
                    errors.add(new FieldError("config." + MIN_SPACING, "must be 0 to 1h"));
                }
            } catch (RuntimeException e) {
                errors.add(new FieldError("config." + MIN_SPACING, "must be a duration such as 30s or PT30S"));
            }
        }
        double duty = config.get(DUTY_CYCLE) instanceof Number n ? n.doubleValue()
                : AirtimeGovernor.DEFAULT_DUTY_CYCLE_PERCENT;
        int perHour = config.get(MAX_PER_HOUR) instanceof Number n ? n.intValue()
                : AirtimeGovernor.DEFAULT_MAX_MESSAGES_PER_HOUR;
        if (perHour < 1 || perHour > 3_600) {
            errors.add(new FieldError("config." + MAX_PER_HOUR, "must be 1 to 3600"));
        }
        if (region != null) {
            try {
                new AirtimeGovernor.Limits(region, duty, spacing.isNegative() ? Duration.ZERO : spacing,
                        Math.max(1, perHour));
            } catch (IllegalArgumentException e) {
                errors.add(new FieldError("config." + DUTY_CYCLE, e.getMessage()));
            }
        }
        Object senders = config.get(ALLOWED_SENDERS);
        if (senders != null) {
            if (!(senders instanceof Map<?, ?> map)) {
                errors.add(new FieldError("config." + ALLOWED_SENDERS, "must be an object of sender to report or ack"));
            } else {
                map.forEach((key, role) -> {
                    String k = senderKey(String.valueOf(key));
                    if (k == null) {
                        errors.add(new FieldError("config." + ALLOWED_SENDERS, "'" + key + "' is not a sender: use"
                                + " !xxxxxxxx (Meshtastic), mc:<12 hex> (MeshCore) or name:<advertised name>"));
                    }
                    if (!ROLE_REPORT.equals(role) && !ROLE_ACK.equals(role)) {
                        errors.add(new FieldError("config." + ALLOWED_SENDERS + "." + key, "must be report or ack"));
                    }
                });
            }
        }
        if (Boolean.TRUE.equals(config.get(REPORTS_ENABLED))
                && (!(senders instanceof Map<?, ?> map) || map.isEmpty())) {
            errors.add(new FieldError("config." + ALLOWED_SENDERS, "is required when reportsEnabled is true"));
        }
        return errors;
    }

    /** The shared config fields of the type catalog. */
    public static List<ChannelFieldSpec> fields(String defaultPreset) {
        return List.of(
                ChannelFieldSpec.choice(REGION, true, "Regulatory region: sets the duty-cycle ceiling (10 % for EU_868,"
                        + " UK_868 and EU_433; none for US, ANZ and OTHER)", null,
                        List.of("EU_868", "EU_433", "UK_868", "US", "ANZ", "OTHER")),
                ChannelFieldSpec.choice(AIRTIME_PRESET, false, "Radio settings for the time-on-air estimate; confirm"
                        + " they match the node. custom reads airtimeCustom {spreadingFactor, bandwidthKhz, codingRate"
                        + " (denominator 5-8), preambleSymbols}", defaultPreset,
                        List.of(LoRaAirtime.LONGFAST, LoRaAirtime.MEDIUMFAST, LoRaAirtime.CUSTOM)),
                ChannelFieldSpec.optional(DUTY_CYCLE, ChannelFieldSpec.NUMBER, "Share of each hour this channel may"
                        + " transmit, in percent: above 0, at most the region's ceiling and at most 10",
                        AirtimeGovernor.DEFAULT_DUTY_CYCLE_PERCENT),
                ChannelFieldSpec.optional(MIN_SPACING, ChannelFieldSpec.STRING, "Least time between transmissions",
                        "30s"),
                ChannelFieldSpec.optional(MAX_PER_HOUR, ChannelFieldSpec.INTEGER, "Most transmissions per hour",
                        AirtimeGovernor.DEFAULT_MAX_MESSAGES_PER_HOUR),
                ChannelFieldSpec.choice(PROFILE, false, "text: the compact text line; cbor: the machine-readable"
                        + " CBOR profile", PROFILE_TEXT, List.of(PROFILE_TEXT, PROFILE_CBOR)),
                ChannelFieldSpec.choice(MIN_SEVERITY, false, "The channel takes nothing below this severity",
                        DEFAULT_MIN_SEVERITY, Severity.WIRE_VALUES),
                ChannelFieldSpec.optional(REPORTS_ENABLED, ChannelFieldSpec.BOOLEAN, "Read #E field reports and"
                        + " commands from allowedSenders on this private channel", false),
                ChannelFieldSpec.optional(ALLOWED_SENDERS, ChannelFieldSpec.MAP, "Sender to role (report or ack):"
                        + " !xxxxxxxx (Meshtastic node), mc:<12 hex> (MeshCore key prefix) or name:<advertised name>"
                        + " (MeshCore channel messages)", null));
    }

    /**
     * The channel's own minimum severity when {@code type} is a mesh type ({@code config.minSeverity}, default
     * {@code warning}), else null.
     */
    public static String minSeverity(String type, Map<String, Object> config) {
        return TYPES.contains(type) ? minSeverityOf(config) : null;
    }

    private static String minSeverityOf(Map<String, Object> config) {
        Object value = config == null ? null : config.get(MIN_SEVERITY);
        return value instanceof String s && Severity.WIRE_VALUES.contains(s) ? s : DEFAULT_MIN_SEVERITY;
    }

    /** The allowed senders as normalized key to role; bad entries are left out. */
    static Map<String, String> senders(Object value) {
        Map<String, String> out = new LinkedHashMap<>();
        if (value instanceof Map<?, ?> map) {
            map.forEach((key, role) -> {
                String k = senderKey(String.valueOf(key));
                if (k != null && (ROLE_REPORT.equals(role) || ROLE_ACK.equals(role))) {
                    out.put(k, (String) role);
                }
            });
        }
        return out;
    }

    /**
     * A sender key normalized: node ids and key prefixes lower case ({@code !} or {@code mc:} added to a bare
     * 8- or 12-digit hex value), {@code name:} keys as written; null when it is not a sender.
     */
    public static String senderKey(String key) {
        if (key == null) {
            return null;
        }
        String k = key.strip();
        if (k.regionMatches(true, 0, "name:", 0, 5)) {
            String name = k.substring(5).strip();
            return name.isEmpty() || name.length() > 32 ? null : "name:" + name;
        }
        String lower = k.toLowerCase(Locale.ROOT);
        if (lower.matches("[0-9a-f]{8}")) {
            lower = "!" + lower;
        } else if (lower.matches("[0-9a-f]{12}")) {
            lower = "mc:" + lower;
        }
        return SENDER.matcher(lower).matches() ? lower : null;
    }

    private static String string(Object value, String def) {
        return value instanceof String s && !s.isBlank() ? s.strip() : def;
    }
}
