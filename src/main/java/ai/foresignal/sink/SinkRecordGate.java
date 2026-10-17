package ai.foresignal.sink;

import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import org.springframework.stereotype.Component;

/**
 * The egress gate every record passes before a sink type sees it (prompt 110, task 2). It holds no policy of
 * its own: it maps the sink's mode to an {@link EgressPath} ({@code SERVE} → {@link EgressPath#SINK_SERVE_LAN},
 * {@code PUSH} → {@link EgressPath#SINK_PUSH}) and asks the one {@link EgressPolicy}
 * ({@code docs/Conventions.md}, "Shared components"), with the record's resolved license and payload and the
 * sink's {@code includePrivate} and local-target facts. {@code EgressPolicy} applies, in order, the private
 * rule (a private source needs {@code includePrivate} and a local target), the descriptor's effective
 * {@code egress} including operator overrides ({@code LOCAL_ONLY} reaches only a SERVE sink on a loopback or
 * private bind), the MLAT rule, and the 109 license table ({@code opensky-states}, {@code acled},
 * {@code aisstream} are {@code FORBIDDEN} and never reach a sink).
 */
@Component
public class SinkRecordGate {

    /** The record's license forbids redistribution. */
    public static final String REASON_LICENCE_FORBIDDEN = EgressPolicy.REASON_LICENCE_FORBIDDEN;

    /** The record's license is local-only (or unknown) and the sink is not local. */
    public static final String REASON_LICENCE_LOCAL_ONLY = EgressPolicy.REASON_LICENCE_LOCAL_ONLY;

    /** The record's source is private and the sink did not opt in on a local target. */
    public static final String REASON_PRIVATE = EgressPolicy.REASON_PRIVATE;

    /** The source's effective descriptor egress is {@code LOCAL_ONLY} and the sink is not a local SERVE sink. */
    public static final String REASON_LOCAL_ONLY = EgressPolicy.REASON_LOCAL_ONLY;

    /** The record's position is MLAT and the sink is not a local SERVE sink. */
    public static final String REASON_MLAT = EgressPolicy.REASON_MLAT;

    /** The record's source is not registered. */
    public static final String REASON_UNREGISTERED = EgressPolicy.REASON_UNREGISTERED;

    private final EgressPolicy egress;

    /** Creates the gate. */
    public SinkRecordGate(EgressPolicy egress) {
        this.egress = egress;
    }

    /**
     * The gate's decision.
     *
     * @param allowed true when the record may reach the sink
     * @param reason the denial reason (one of the {@code REASON_*} constants), null when allowed
     */
    public record Decision(boolean allowed, String reason) {

        /** An allowance. */
        public static final Decision ALLOW = new Decision(true, null);
    }

    /** The egress path of a sink mode. */
    public static EgressPath path(SinkMode mode) {
        return mode == SinkMode.SERVE ? EgressPath.SINK_SERVE_LAN : EgressPath.SINK_PUSH;
    }

    /**
     * Whether {@code record} may leave through a sink.
     *
     * @param mode the sink mode
     * @param localTarget true when a SERVE sink binds loopback/private, or a PUSH target resolves private
     * @param includePrivate the filter's {@code includePrivate}
     */
    /**
     * Whether {@code record} may leave through a {@code federation-push} sink (prompt 113): the path is
     * {@link EgressPath#FEDERATION}, not {@code SINK_PUSH}, with the sink's {@code sameOperator} exception (the
     * operator's own hub may receive {@code LOCAL_ONLY} records). Private records and MLAT positions never pass.
     */
    public Decision allowFederation(boolean sameOperator, SinkRecord record) {
        EgressPolicy.Decision decision = egress.decide(EgressPath.FEDERATION, record.sourceId(), record.licence(),
                record.properties(), EgressPolicy.EgressContext.federation(sameOperator));
        return decision.allowed() ? Decision.ALLOW : new Decision(false, decision.reason());
    }

    public Decision allow(SinkMode mode, boolean localTarget, boolean includePrivate, SinkRecord record) {
        EgressPolicy.Decision decision = egress.decide(path(mode), record.sourceId(), record.licence(),
                record.properties(), EgressPolicy.EgressContext.sink(includePrivate, localTarget));
        return decision.allowed() ? Decision.ALLOW : new Decision(false, decision.reason());
    }
}
