package ai.foresignal.federation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The pure rules of federation (prompt 113): provenance chains, loop and hop limits, exercise records and the
 * {@code _foresignal} payload member. Stateless and side-effect free, so the serving side, the PULL runtime and
 * the RECEIVE endpoint apply exactly the same rules.
 *
 * <ul>
 *   <li><strong>Chain:</strong> origin first, then one hop per instance that re-served the record. A local record
 *       is served with {@code chain:[origin]}; a record that came from a peer is served with its stored chain plus
 *       a hop for this instance.
 *   <li><strong>Loop:</strong> an item whose chain names this instance is dropped ({@value #DROP_LOOP}).
 *   <li><strong>Hops:</strong> an item whose chain has {@code maxHops} or more entries is dropped
 *       ({@value #DROP_HOPS}); the default is {@value #DEFAULT_MAX_HOPS}.
 * </ul>
 */
public final class FederationRules {

    /** The volatile payload member a peer record carries its provenance in (never hashed). */
    public static final String MEMBER = "_foresignal";

    /** {@code _foresignal.originRef}: the origin's global reference. */
    public static final String ORIGIN_REF = "originRef";

    /** {@code _foresignal.originSeq}: the origin's seq of this version. */
    public static final String ORIGIN_SEQ = "originSeq";

    /** {@code _foresignal.originLicence}: the license id in force at the origin (109's per-record license). */
    public static final String ORIGIN_LICENSE = "originLicence";

    /** {@code _foresignal.chain}: the provenance chain as received. */
    public static final String CHAIN = "chain";

    /** {@code _foresignal.originExternalId}: the origin's external id when the local one had to be hashed. */
    public static final String ORIGIN_EXTERNAL_ID = "originExternalId";

    /** The payload member that marks an exercise record (prompt 115; {@code "exercise": true}). */
    public static final String EXERCISE = "exercise";

    /** The default hop limit. */
    public static final int DEFAULT_MAX_HOPS = 3;

    /** Dropped: the chain names this instance. */
    public static final String DROP_LOOP = "loop";

    /** Dropped: the chain is at or above the hop limit. */
    public static final String DROP_HOPS = "hops";

    /** Dropped: an equal or newer origin seq was already applied. */
    public static final String DROP_DEDUP = "dedup";

    /** Dropped: older than the selection's {@code maxAge}. */
    public static final String DROP_EXPIRED = "expired";

    /** Dropped: the license does not allow it here. */
    public static final String DROP_LICENSE = "licence";

    /** Dropped: a private record. */
    public static final String DROP_PRIVATE = "private";

    /** Dropped: an exercise record the caller did not ask for. */
    public static final String DROP_EXERCISE = "exercise";

    /** Dropped: outside the selection (source, kind, area) or of a kind this instance cannot store. */
    public static final String DROP_SELECTION = "selection";

    private FederationRules() {}

    /** A provenance hop. */
    public static Map<String, Object> hop(String instanceId, String sourceId, String externalId, long seq,
            Instant ingestedAt) {
        Map<String, Object> hop = new LinkedHashMap<>();
        hop.put("instanceId", instanceId);
        hop.put("sourceId", sourceId);
        hop.put("externalId", externalId);
        hop.put("seq", seq);
        hop.put("ingestedAt", ingestedAt == null ? null : ingestedAt.toString());
        return hop;
    }

    /** The {@code _foresignal} member of {@code payload}, or null. */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> member(Map<String, ?> payload) {
        return payload != null && payload.get(MEMBER) instanceof Map<?, ?> m ? (Map<String, Object>) m : null;
    }

    /** The stored chain of a peer record ({@code _foresignal.chain}), or empty for a local record. */
    public static List<Map<String, Object>> storedChain(Map<String, ?> payload) {
        return chain(member(payload) == null ? null : member(payload).get(CHAIN));
    }

    /** {@code value} as a chain: the map entries of a list, in order; empty otherwise. */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> chain(Object value) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object entry : list) {
                if (entry instanceof Map<?, ?> m) {
                    out.add((Map<String, Object>) m);
                }
            }
        }
        return out;
    }

    /**
     * The chain a record is served with: {@code [origin]} for a local record, else its stored chain plus
     * {@code self}.
     *
     * @param stored the record's stored chain (empty for a local record)
     * @param self this instance's hop for the record
     */
    public static List<Map<String, Object>> servedChain(List<Map<String, Object>> stored, Map<String, Object> self) {
        List<Map<String, Object>> out = new ArrayList<>(stored);
        out.add(self);
        return out;
    }

    /**
     * Why an arriving item must be dropped by the loop rules, or null when it may be applied.
     *
     * @param chain the item's provenance chain
     * @param localInstanceId this instance's id
     * @param maxHops the selection's hop limit (at least 1)
     * @return {@value #DROP_LOOP}, {@value #DROP_HOPS} or null
     */
    public static String loopReason(List<Map<String, Object>> chain, String localInstanceId, int maxHops) {
        for (Map<String, Object> hop : chain) {
            if (localInstanceId != null && localInstanceId.equals(String.valueOf(hop.get("instanceId")))) {
                return DROP_LOOP;
            }
        }
        if (chain.size() >= Math.max(1, maxHops)) {
            return DROP_HOPS;
        }
        return null;
    }

    /** True when {@code payload} marks an exercise record ({@code "exercise": true}, prompt 115). */
    public static boolean isExercise(Map<String, ?> payload) {
        return payload != null && Boolean.TRUE.equals(payload.get(EXERCISE));
    }

    /** {@code payload} without the {@code _foresignal} member (a served item carries provenance instead). */
    public static Map<String, Object> withoutMember(Map<String, ?> payload) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (payload != null) {
            payload.forEach((key, value) -> {
                if (!MEMBER.equals(key)) {
                    out.put(key, value);
                }
            });
        }
        return out;
    }

    /** The origin hop of a chain (its first entry), or null for an empty chain. */
    public static Map<String, Object> origin(List<Map<String, Object>> chain) {
        return chain.isEmpty() ? null : chain.getFirst();
    }

    /**
     * The origin reference of a chain's origin hop, or null when it is incomplete.
     *
     * @throws IllegalArgumentException never; a malformed hop yields null
     */
    public static String originRef(Map<String, Object> origin) {
        if (origin == null) {
            return null;
        }
        Object instance = origin.get("instanceId");
        Object source = origin.get("sourceId");
        Object external = origin.get("externalId");
        if (!(instance instanceof String i) || !(source instanceof String s) || !(external instanceof String e)) {
            return null;
        }
        try {
            return RecordRef.format(i, s, e);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /** The {@code seq} of a hop as a long, or -1 when absent. */
    public static long seqOf(Map<String, Object> hop) {
        return hop != null && hop.get("seq") instanceof Number n ? n.longValue() : -1L;
    }
}
