package ai.foresignal.security.quota;

import ai.foresignal.api.error.ValidationException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tools.jackson.databind.JsonNode;

/**
 * The {@code Quota} schema (prompt R2-026): a user's limits, or the instance default. A null member is unset: in a
 * user's quota it falls through to the instance default ({@link #over}), and unset in the default too it is unlimited.
 * Every set member is non-negative; {@code 0} allows nothing (no requests, no further rules, channels or views, no
 * stored bytes).
 *
 * @param apiRateLimitPerMinute requests a minute across the user's sessions and own API tokens
 * @param maxStorageBytes bytes of attachments and workspaces the user may own
 * @param maxAlerts trigger rules the user may own
 * @param maxChannels notification channels the user may own
 * @param maxSavedViews saved views the user may own
 */
public record Quota(Integer apiRateLimitPerMinute, Long maxStorageBytes, Integer maxAlerts, Integer maxChannels,
        Integer maxSavedViews) {

    /** Every member unset: no limit at all. */
    public static final Quota UNSET = new Quota(null, null, null, null, null);

    /** The members of the wire form, in order. */
    public static final List<String> FIELDS = List.of("apiRateLimitPerMinute", "maxStorageBytes", "maxAlerts",
            "maxChannels", "maxSavedViews");

    /**
     * This quota with each unset member taken from {@code fallback}.
     *
     * @param fallback the instance default
     * @return the merged quota
     */
    public Quota over(Quota fallback) {
        Quota f = fallback == null ? UNSET : fallback;
        return new Quota(
                apiRateLimitPerMinute != null ? apiRateLimitPerMinute : f.apiRateLimitPerMinute,
                maxStorageBytes != null ? maxStorageBytes : f.maxStorageBytes,
                maxAlerts != null ? maxAlerts : f.maxAlerts,
                maxChannels != null ? maxChannels : f.maxChannels,
                maxSavedViews != null ? maxSavedViews : f.maxSavedViews);
    }

    /**
     * The limit of one count quota.
     *
     * @param limit the quota
     * @return its value, or null when unset
     */
    public Long limitOf(QuotaLimit limit) {
        return switch (limit) {
            case ALERTS -> maxAlerts == null ? null : maxAlerts.longValue();
            case CHANNELS -> maxChannels == null ? null : maxChannels.longValue();
            case SAVED_VIEWS -> maxSavedViews == null ? null : maxSavedViews.longValue();
            case STORAGE -> maxStorageBytes;
        };
    }

    /**
     * The wire form, every member present (null when unset).
     *
     * @return the {@code Quota} object
     */
    public Map<String, Object> view() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("apiRateLimitPerMinute", apiRateLimitPerMinute);
        out.put("maxStorageBytes", maxStorageBytes);
        out.put("maxAlerts", maxAlerts);
        out.put("maxChannels", maxChannels);
        out.put("maxSavedViews", maxSavedViews);
        return out;
    }

    /**
     * Parses a {@code Quota} body. An absent or null member is unset; a set one must be a non-negative integer. An
     * unknown member is refused, so a misspelled limit is not silently ignored.
     *
     * @param body the body
     * @return the quota
     * @throws ValidationException {@code 400 VALIDATION_FAILED}
     */
    public static Quota parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The body must be a Quota object.");
        }
        Set<String> known = Set.copyOf(FIELDS);
        for (String member : body.propertyNames()) {
            if (!known.contains(member)) {
                throw ValidationException.forField(member, "is not a Quota member");
            }
        }
        Long storage = nonNegative(body, "maxStorageBytes", Long.MAX_VALUE);
        return new Quota(
                intValue(nonNegative(body, "apiRateLimitPerMinute", Integer.MAX_VALUE)),
                storage,
                intValue(nonNegative(body, "maxAlerts", Integer.MAX_VALUE)),
                intValue(nonNegative(body, "maxChannels", Integer.MAX_VALUE)),
                intValue(nonNegative(body, "maxSavedViews", Integer.MAX_VALUE)));
    }

    private static Long nonNegative(JsonNode body, String field, long max) {
        JsonNode value = body.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isIntegralNumber() || !value.canConvertToLong()) {
            throw ValidationException.forField(field, "must be an integer or null");
        }
        long v = value.asLong();
        if (v < 0) {
            throw ValidationException.forField(field, "must not be negative");
        }
        if (v > max) {
            throw ValidationException.forField(field, "must be at most " + max);
        }
        return v;
    }

    private static Integer intValue(Long value) {
        return value == null ? null : Math.toIntExact(value);
    }
}
