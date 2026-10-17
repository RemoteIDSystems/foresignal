package ai.foresignal.situation;

import java.util.List;

/** The member types of a situation (prompt 118) and how each maps to a Client Contract {@code ObjectRef}. */
public final class SituationMemberTypes {

    /** A 101 intelligence event; ref = the event id. */
    public static final String INTEL_EVENT = "intel_event";

    /** A 119 analytic finding; ref = the finding id. */
    public static final String FINDING = "finding";

    /** A 017 alert; ref = the alert id. */
    public static final String ALERT = "alert";

    /** A 100 anomaly record; ref = {@code rule-windows/externalId}. */
    public static final String ANOMALY = "anomaly";

    /** Any record; ref = {@code sourceId/externalId}. */
    public static final String RECORD = "record";

    /** A 101 investigation; ref = the investigation id. */
    public static final String INVESTIGATION = "investigation";

    /** Every member type, in the spec's order. */
    public static final List<String> ALL = List.of(INTEL_EVENT, FINDING, ALERT, ANOMALY, RECORD, INVESTIGATION);

    /** The member roles. */
    public static final List<String> ROLES = List.of("core", "context");

    private SituationMemberTypes() {}

    /** True when {@code ref} names a record ({@code sourceId/externalId}). */
    public static boolean recordRef(String memberType) {
        return RECORD.equals(memberType) || ANOMALY.equals(memberType);
    }

    /**
     * The {@code ObjectRef} of a member: {@code intel-event:…}, {@code finding:…}, {@code alert:…},
     * {@code investigation:…}, or {@code record:{sourceId}/{externalId}[@seq]} with the external id percent-encoded.
     */
    public static String objectRef(String memberType, String ref, Long seq) {
        return switch (memberType) {
            case INTEL_EVENT -> "intel-event:" + ref;
            case FINDING -> "finding:" + ref;
            case ALERT -> "alert:" + ref;
            case INVESTIGATION -> "investigation:" + ref;
            default -> {
                int slash = ref.indexOf('/');
                String source = slash < 0 ? ref : ref.substring(0, slash);
                String external = slash < 0 ? "" : ref.substring(slash + 1);
                String encoded = external.replace("%", "%25").replace("/", "%2F").replace("@", "%40");
                yield "record:" + source + "/" + encoded + (seq == null ? "" : "@" + seq);
            }
        };
    }
}
