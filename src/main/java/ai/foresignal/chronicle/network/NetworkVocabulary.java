package ai.foresignal.chronicle.network;

import java.time.Duration;
import java.util.List;

/**
 * The words and limits of the chronicle network (prompt 134): key types, strengths, link and request statuses, and
 * the anti-probing and expiry limits.
 */
public final class NetworkVocabulary {

    /** Key type: canonical events ({@code ev:} with an upstream id, else {@code cell:}). */
    public static final String EVIDENCE = "evidence";

    /** Key type: entity keys per UTC day ({@code ent:}). */
    public static final String ENTITIES = "entities";

    /** Key type: H3 resolution-4 cells per UTC day ({@code at:}). */
    public static final String AREA_TIME = "area-time";

    /** Key type: normalized keyword sets ({@code topic:}). */
    public static final String TOPICS = "topics";

    /** Every key type, in display order. */
    public static final List<String> KEY_TYPES = List.of(EVIDENCE, ENTITIES, AREA_TIME, TOPICS);

    /** Strength below a Jaccard of 0.1. */
    public static final String LOW = "low";

    /** Strength from a Jaccard of 0.1 up to 0.3. */
    public static final String MEDIUM = "medium";

    /** Strength from a Jaccard of 0.3. */
    public static final String HIGH = "high";

    /** Every strength, weakest first. */
    public static final List<String> STRENGTHS = List.of(LOW, MEDIUM, HIGH);

    /** A link both owners still let stand. */
    public static final String ACTIVE = "active";

    /** A link one owner dismissed. */
    public static final String DISMISSED = "dismissed";

    /** Temporal relation: chronicle a captured the shared keys first. */
    public static final String A_PRECEDES_B = "a-precedes-b";

    /** Temporal relation: chronicle b captured the shared keys first. */
    public static final String B_PRECEDES_A = "b-precedes-a";

    /** Temporal relation: neither captured them clearly first. */
    public static final String CONCURRENT = "concurrent";

    /** Capture spans do not overlap. */
    public static final String OVERLAP_NONE = "none";

    /** Capture spans overlap in part. */
    public static final String OVERLAP_PARTIAL = "partial";

    /** One capture span contains the other. */
    public static final String OVERLAP_FULL = "full";

    /** Full disclosure: the viewer can see both chronicles. */
    public static final String FULL = "full";

    /** Opaque disclosure: the other chronicle is discoverable to the viewer but not visible. */
    public static final String OPAQUE = "opaque";

    /** An access request waiting for the owner. */
    public static final String PENDING = "pending";

    /** An access request the owner granted. */
    public static final String GRANTED = "granted";

    /** An access request the owner declined. */
    public static final String DECLINED = "declined";

    /** Every access request status. */
    public static final List<String> REQUEST_STATUSES = List.of(PENDING, GRANTED, DECLINED);

    /** The prefix of a remote link endpoint: {@code remote:{instanceId}/chronicle:{sketchId}}. */
    public static final String REMOTE_PREFIX = "remote:";

    /** The default minimum estimated shared keys of a link ({@code network.minSharedKeys}). */
    public static final int DEFAULT_MIN_SHARED_KEYS = 5;

    /** The minimum containment of the smaller chronicle for a link. */
    public static final double MIN_CONTAINMENT = 0.05;

    /** The Jaccard at which a link is medium. */
    public static final double MEDIUM_JACCARD = 0.1;

    /** The Jaccard at which a link is high. */
    public static final double HIGH_JACCARD = 0.3;

    /** The global evidence keys a chronicle must have captured before discovery runs for it. */
    public static final int MIN_KEYS_FOR_DISCOVERY = 50;

    /** The most new opaque links a principal is shown per day. */
    public static final int OPAQUE_LINKS_PER_DAY = 20;

    /** The most access requests a principal sends per day. */
    public static final int REQUESTS_PER_DAY = 10;

    /** How long repeated requests to an owner who declined are suppressed. */
    public static final Duration DECLINE_SUPPRESSION = Duration.ofDays(30);

    /** How long a chronicle created after churn waits before discovery. */
    public static final Duration CHURN_WINDOW = Duration.ofHours(24);

    /** How long a received sketch lives unless refreshed. */
    public static final Duration SKETCH_EXPIRY = Duration.ofDays(30);

    /** How long a network salt is used before its admins must agree a new one. */
    public static final Duration NETWORK_SALT_ROTATION = Duration.ofDays(90);

    /** How long the instance discovery salt is used before it is rotated. */
    public static final Duration INSTANCE_SALT_ROTATION = Duration.ofDays(365);

    /** The relative sketch change that triggers discovery between nightly runs. */
    public static final double CHANGE_TRIGGER = 0.10;

    /** The most shared canonical events or entities a full link lists. */
    public static final int MAX_SHARED_LISTED = 100;

    /** The most sketches in one batch. */
    public static final int MAX_BATCH_SKETCHES = 1000;

    /** The longest access request message. */
    public static final int MAX_MESSAGE = 1000;

    private NetworkVocabulary() {}

    /** The strength band of an estimated Jaccard: low below 0.1, medium below 0.3, high from 0.3. */
    public static String strength(double jaccard) {
        if (jaccard >= HIGH_JACCARD) {
            return HIGH;
        }
        return jaccard >= MEDIUM_JACCARD ? MEDIUM : LOW;
    }

    /** The rank of a strength (low 0, medium 1, high 2), or -1 when unknown. */
    public static int rank(String strength) {
        return STRENGTHS.indexOf(strength);
    }

    /** True when {@code node} names a remote chronicle ({@code remote:{instanceId}/chronicle:{sketchId}}). */
    public static boolean isRemote(String node) {
        return node != null && node.startsWith(REMOTE_PREFIX);
    }

    /** The link endpoint of a remote sketch. */
    public static String remoteNode(String instanceId, String sketchId) {
        return REMOTE_PREFIX + instanceId + "/chronicle:" + sketchId;
    }
}
