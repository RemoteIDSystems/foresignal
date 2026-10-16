package ai.foresignal.provenance;

import ai.foresignal.security.EgressPath;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * The license table of prompt 109 (Weather-Imagery-Design §11): whether a record with {@code terms} may leave
 * on {@code path}. The {@code UNKNOWN} defaults are applied first ({@link LicenceTerms#effectiveRedistribution()}).
 *
 * <p><strong>Internal delegate of {@link ai.foresignal.security.EgressPolicy}; not an entry point.</strong>
 * {@code docs/Conventions.md} allows exactly one component to decide egress. Every caller asks
 * {@link ai.foresignal.security.EgressPolicy#decide} (or an {@code allows} overload), which applies the private
 * rule, the descriptor {@code egress}, the operator overrides and the MLAT downgrade before consulting this
 * table; asking this class directly skips all of those (the defect that let 110's sinks bypass
 * {@code LOCAL_ONLY} egress). {@code LicenceEgressGuardUsageTest} fails the build when a new caller appears.
 * The class stays public only because {@code ai.foresignal.export.ExportLicenceService} (prompt 111) still
 * injects it; that caller must move to {@code EgressPolicy.decide(EgressPath.EXPORT, sourceId, null, null,
 * EgressContext.export(viewer, includePrivate, personalUse))}.
 *
 * <table>
 *   <caption>Redistribution by path</caption>
 *   <tr><th>Redistribution</th><th>API/TILE/SSE/MCP/LLM/NOTIFICATION/FEED/ARCHIVE</th>
 *       <th>SINK_SERVE_LAN</th><th>SINK_PUSH, EXPORT</th><th>PUBLIC_SHARE, FEDERATION, SHARE</th></tr>
 *   <tr><td>ALLOWED</td><td>yes</td><td>yes</td><td>yes</td><td>yes</td></tr>
 *   <tr><td>LOCAL_ONLY</td><td>yes</td><td>yes, loopback/private sink only</td>
 *       <td>no (EXPORT with personalUse only)</td><td>no</td></tr>
 *   <tr><td>FORBIDDEN</td><td>yes (own use)</td><td>no</td>
 *       <td>no (EXPORT with personalUse only)</td><td>no</td></tr>
 * </table>
 */
@Component
public class LicenceEgressGuard {

    /** The paths that never leave this instance's own serving surface. */
    private static final Set<EgressPath> INTERNAL = EnumSet.of(EgressPath.API_READ, EgressPath.TILE, EgressPath.SSE,
            EgressPath.MCP, EgressPath.LLM_LOCAL, EgressPath.LLM_REMOTE, EgressPath.NOTIFICATION, EgressPath.FEED,
            EgressPath.ARCHIVE);

    /** The re-serving paths that reach a third party. */
    private static final Set<EgressPath> PUBLIC = EnumSet.of(EgressPath.PUBLIC_SHARE, EgressPath.FEDERATION,
            EgressPath.SHARE);

    /** The off-box push/export paths. */
    private static final Set<EgressPath> OFF_BOX = EnumSet.of(EgressPath.SINK_PUSH, EgressPath.EXPORT, EgressPath.SINK);

    private final ObjectProvider<MeterRegistry> meters;

    /** Creates the guard. */
    public LicenceEgressGuard(ObjectProvider<MeterRegistry> meters) {
        this.meters = meters;
    }

    /** Creates the guard without metrics (tests). */
    public LicenceEgressGuard() {
        this.meters = null;
    }

    /** {@link #allows(EgressPath, LicenceTerms, boolean, boolean)} with no personal-use or LAN exception. */
    public boolean allows(EgressPath path, LicenceTerms terms) {
        return allows(path, terms, false, false);
    }

    /**
     * Whether a record with {@code terms} may leave on {@code path}.
     *
     * @param personalUse the operator's own use of an export of a {@code LOCAL_ONLY}/{@code FORBIDDEN} source
     * @param lanLocal true when a {@code SINK_SERVE_LAN} sink binds loopback or a private address
     */
    public boolean allows(EgressPath path, LicenceTerms terms, boolean personalUse, boolean lanLocal) {
        boolean ok = decide(path, terms, personalUse, lanLocal);
        if (!ok && meters != null) {
            MeterRegistry registry = meters.getIfAvailable();
            if (registry != null) {
                registry.counter("foresignal.licence.egress.denied", "path", path.name(),
                        "licence", terms == null ? "unknown" : terms.id()).increment();
            }
        }
        return ok;
    }

    private static boolean decide(EgressPath path, LicenceTerms rawTerms, boolean personalUse, boolean lanLocal) {
        LicenceTerms terms = rawTerms == null ? LicenceTerms.unknown() : rawTerms;
        LicenceTerms.Redistribution r = terms.effectiveRedistribution();
        if (INTERNAL.contains(path)) {
            return true;
        }
        if (r == LicenceTerms.Redistribution.ALLOWED) {
            return true;
        }
        if (PUBLIC.contains(path)) {
            return false;
        }
        if (path == EgressPath.SINK_SERVE_LAN) {
            return r == LicenceTerms.Redistribution.LOCAL_ONLY && lanLocal;
        }
        // OFF_BOX: SINK_PUSH, SINK, EXPORT.
        if (personalUse && (path == EgressPath.EXPORT)) {
            return true;
        }
        return false;
    }

    /** True when the terms require attribution whenever the records are shown. */
    public boolean attributionRequired(LicenceTerms terms) {
        return terms != null && terms.attributionRequired();
    }
}
