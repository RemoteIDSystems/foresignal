package ai.foresignal.egress;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.UUID;

/**
 * The egress routing policy ({@code EgressSettings}, prompt R2-018), stored under {@code app_setting['egress']}.
 *
 * @param rotation {@value #DIRECT} (the default: no egress routing at all, exactly the behavior before R2-018),
 *     {@value #ROUND_ROBIN}, {@value #RANDOM}, {@value #PER_SOURCE_STICKY} or {@value #ROTATE_ON_RATE_LIMIT}
 * @param defaultRoute the route of a source kept out of rotation without a pin, or null for the rotation pool
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EgressSettings(String rotation, UUID defaultRoute) {

    /** No egress routing: every pull goes out from the server's own address, pins included. */
    public static final String DIRECT = "direct";

    /** Each pull takes the next usable route in turn. */
    public static final String ROUND_ROBIN = "round-robin";

    /** Each pull takes a usable route at random. */
    public static final String RANDOM = "random";

    /**
     * Each source keeps one route, chosen by its id; while that route is throttled or unhealthy the source waits
     * rather than moving.
     */
    public static final String PER_SOURCE_STICKY = "per-source-sticky";

    /**
     * Each source keeps its route until that route is throttled (429, or a {@code Retry-After}) or fails, then
     * moves to the next usable one.
     */
    public static final String ROTATE_ON_RATE_LIMIT = "rotate-on-rate-limit";

    /** The policies. */
    public static final List<String> ROTATIONS = List.of(DIRECT, ROUND_ROBIN, RANDOM, PER_SOURCE_STICKY,
            ROTATE_ON_RATE_LIMIT);

    /** Fills defaults. */
    public EgressSettings {
        rotation = rotation == null || rotation.isBlank() ? DIRECT : rotation;
    }

    /** The shipped defaults: direct, no default route. */
    public static EgressSettings defaults() {
        return new EgressSettings(DIRECT, null);
    }

    /** True when egress routing is off. */
    public boolean direct() {
        return DIRECT.equals(rotation);
    }
}
