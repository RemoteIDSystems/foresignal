package ai.foresignal.source.adapters.mesh;

import ai.foresignal.source.spi.ReconnectPolicy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The public Meshtastic broker rules (prompt 083, "the Mesh.md rules"), enforced in code because ShadowBroker was
 * reported to the Meshtastic project for one to two orders of magnitude more broker traffic than a typical client.
 * None of these is an operator setting except the roots, within the limits below.
 *
 * <ul>
 *   <li>Roots match {@value #ROOT_REGEX}: {@code msh/} and one or two plain segments, so no wildcard above or at the
 *       root ({@code msh/#}, {@code +/…}) can ever be subscribed.
 *   <li>One subscription per distinct root, {@code <root>/2/json/#} only, QoS 0; {@code maxSubscriptions} defaults to
 *       1 and is at most {@value #MAX_SUBSCRIPTIONS}; root plus {@code extraRoots} must fit in it.
 *   <li>Client id {@code foresignal-<installId>}; keepalive 120 s; clean session.
 *   <li>Reconnect 15 s doubling to 300 s with jitter; at most 12 connects per hour and 100 budget units per day.
 *   <li>Inbound cap 100 messages per minute, checked before parsing.
 * </ul>
 */
public final class MeshtasticMqttRules {

    /** The root pattern. */
    public static final String ROOT_REGEX = "^msh/[A-Za-z0-9_]+(/[A-Za-z0-9_]+)?$";

    /** The compiled root pattern. */
    public static final Pattern ROOT = Pattern.compile(ROOT_REGEX);

    /** The hard maximum of {@code maxSubscriptions}. */
    public static final int MAX_SUBSCRIPTIONS = 3;

    /** The keepalive. */
    public static final Duration KEEPALIVE = Duration.ofSeconds(120);

    /** The reconnect ladder: 15 s doubling to 300 s, ±20% jitter. */
    public static final ReconnectPolicy RECONNECT = new ReconnectPolicy(Duration.ofSeconds(15), Duration.ofSeconds(300),
            0.2);

    /** The most connects in any hour. */
    public static final int CONNECTS_PER_HOUR = 12;

    /** The inbound cap per minute. */
    public static final int INBOUND_PER_MINUTE = 100;

    /** The subscription suffix under a root: the JSON tree only. */
    public static final String JSON_SUFFIX = "/2/json/#";

    private MeshtasticMqttRules() {}

    /** The client id: {@code foresignal-<installId>}. */
    public static String clientId(String installId) {
        return "foresignal-" + installId;
    }

    /** The problem with one root, or null. */
    public static String rootProblem(String root) {
        if (root == null || root.isBlank()) {
            return "is required";
        }
        if (root.contains("#") || root.contains("+")) {
            return "must not contain MQTT wildcards; subscribe to one region or channel root such as msh/US";
        }
        if (!ROOT.matcher(root.strip()).matches()) {
            return "must match " + ROOT_REGEX + " (for example msh/US or msh/EU_868)";
        }
        return null;
    }

    /**
     * Every rule violation of a configuration, keyed by parameter.
     *
     * @param root the main root
     * @param extraRoots further roots, possibly null
     * @param maxSubscriptions the configured maximum, or null for the default 1
     */
    public static Map<String, String> problems(String root, List<String> extraRoots, Integer maxSubscriptions) {
        Map<String, String> problems = new LinkedHashMap<>();
        String rootProblem = rootProblem(root);
        if (rootProblem != null) {
            problems.put("root", rootProblem);
        }
        List<String> extras = extraRoots == null ? List.of() : extraRoots;
        for (String extra : extras) {
            String problem = rootProblem(extra);
            if (problem != null) {
                problems.put("extraRoots", "'" + extra + "' " + problem);
                break;
            }
        }
        int max = maxSubscriptions == null ? 1 : maxSubscriptions;
        if (max < 1 || max > MAX_SUBSCRIPTIONS) {
            problems.put("maxSubscriptions", "must be 1 to " + MAX_SUBSCRIPTIONS + " (the public broker's limit), but was "
                    + max);
        } else if (rootProblem == null) {
            int needed = roots(root, extras).size();
            if (needed > max) {
                problems.put("maxSubscriptions", "root and extraRoots need " + needed + " subscriptions, more than "
                        + max);
            }
        }
        return problems;
    }

    /** The distinct roots, main root first. */
    public static List<String> roots(String root, List<String> extraRoots) {
        Set<String> roots = new LinkedHashSet<>();
        roots.add(root.strip());
        if (extraRoots != null) {
            for (String extra : extraRoots) {
                if (extra != null && !extra.isBlank()) {
                    roots.add(extra.strip());
                }
            }
        }
        return new ArrayList<>(roots);
    }

    /** The subscription filters: {@code <root>/2/json/#} for each distinct root. */
    public static List<String> subscriptions(String root, List<String> extraRoots) {
        return roots(root, extraRoots).stream().map(r -> r + JSON_SUFFIX).toList();
    }
}
