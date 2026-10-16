package ai.foresignal.sink;

import ai.foresignal.provenance.LicenceCatalog;
import ai.foresignal.provenance.LicenceEgressGuard;
import ai.foresignal.provenance.LicenceTerms;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.source.SourceRegistry;
import org.springframework.stereotype.Component;

/**
 * The egress gate every record passes before a sink type sees it (prompt 110; task 2). A private source
 * is refused unless {@code filter.includePrivate=true} and the sink is on a local target; the 109
 * {@link LicenceEgressGuard} refuses a {@code FORBIDDEN} or non-local {@code LOCAL_ONLY} record on the
 * sink's path. Opensky/ACLED/aisstream records are always refused.
 */
@Component
public class SinkRecordGate {

    /** The record's licence forbids redistribution. */
    public static final String REASON_LICENCE_FORBIDDEN = "licence-forbidden";

    /** The record's licence is local-only and the sink is not local. */
    public static final String REASON_LICENCE_LOCAL_ONLY = "licence-local-only";

    /** The record's source is private and the sink did not opt in on a local target. */
    public static final String REASON_PRIVATE = "private";

    private final LicenceEgressGuard guard;
    private final LicenceCatalog catalog;
    private final SourceVisibility visibility;
    private final SourceRegistry registry;

    /** Creates the gate. */
    public SinkRecordGate(LicenceEgressGuard guard, LicenceCatalog catalog, SourceVisibility visibility,
            SourceRegistry registry) {
        this.guard = guard;
        this.catalog = catalog;
        this.visibility = visibility;
        this.registry = registry;
    }

    /** The gate's decision. */
    public record Decision(boolean allowed, String reason) {

        /** An allowance. */
        public static final Decision ALLOW = new Decision(true, null);
    }

    /**
     * Whether {@code record} may leave through a sink.
     *
     * @param mode the sink mode
     * @param localTarget true when a SERVE sink binds loopback/private, or a PUSH target resolves private
     * @param includePrivate the filter's {@code includePrivate}
     */
    public Decision allow(SinkMode mode, boolean localTarget, boolean includePrivate, SinkRecord record) {
        if (record.sourceId() != null && visibility.isPrivate(record.sourceId())
                && !(includePrivate && localTarget)) {
            return new Decision(false, REASON_PRIVATE);
        }
        LicenceTerms terms = record.licence() != null ? record.licence() : catalog.forDescriptor(source(record));
        // MLAT positions are local-only per record regardless of the source's terms (069).
        if (isMlat(record)) {
            if (!localTarget) {
                return new Decision(false, REASON_LICENCE_LOCAL_ONLY);
            }
            terms = catalog.terms("local-only-operator");
        }
        EgressPath path = mode == SinkMode.SERVE ? EgressPath.SINK_SERVE_LAN : EgressPath.SINK_PUSH;
        if (!guard.allows(path, terms, false, localTarget)) {
            String reason = terms.effectiveRedistribution() == LicenceTerms.Redistribution.FORBIDDEN
                    ? REASON_LICENCE_FORBIDDEN : REASON_LICENCE_LOCAL_ONLY;
            return new Decision(false, reason);
        }
        return Decision.ALLOW;
    }

    private ai.foresignal.source.spi.SourceDescriptor source(SinkRecord record) {
        return record.sourceId() != null && registry.contains(record.sourceId())
                ? registry.descriptor(record.sourceId()) : null;
    }

    private static boolean isMlat(SinkRecord record) {
        Object pos = record.properties() == null ? null : record.properties().get("posSource");
        return "mlat".equals(String.valueOf(pos));
    }
}
