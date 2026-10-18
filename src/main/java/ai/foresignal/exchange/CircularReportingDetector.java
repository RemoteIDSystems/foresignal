package ai.foresignal.exchange;

import ai.foresignal.federation.FederationIdentity;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Detects circular reporting (prompt 134): an interpretation that traveled between analysts must never count as
 * independent confirmation of itself.
 *
 * <ul>
 *   <li>A product used as support (a report, a received package's object, an evidence item citing reports) whose
 *       provenance chain names the consuming analyst's own instance and principal before its last hop (importers
 *       aside) is that analyst's own judgment coming back: it is marked {@value #CIRCULAR_REPORTING}.</li>
 *   <li>Every product with a chain gets the evidence key of its origin ({@code origin:{instanceId}/{productRef}}), so
 *       two supporting items that share an origin fall into one 116 evidence group: the 125 updater and the confidence
 *       assessor count them once, and the second one marks the row {@value #CIRCULAR_REPORTING}.</li>
 * </ul>
 */
@Component
public class CircularReportingDetector {

    /** The caveat of circular or shared-origin support. */
    public static final String CIRCULAR_REPORTING = "CIRCULAR_REPORTING";

    private final ProvenanceChains chains;
    private final FederationIdentity identity;
    private final PrincipalRepository principals;

    /** Creates the detector. */
    public CircularReportingDetector(ProvenanceChains chains, FederationIdentity identity,
            PrincipalRepository principals) {
        this.chains = chains;
        this.identity = identity;
        this.principals = principals;
    }

    /**
     * What the detector found for one piece of support.
     *
     * @param evidenceKey the origin evidence key
     * @param chain the provenance chain, origin first
     * @param circular true when the chain returns to the consuming analyst
     */
    public record Assessment(String evidenceKey, List<Map<String, Object>> chain, boolean circular) {}

    /**
     * Assesses a reference used as support.
     *
     * @param ref the supporting reference
     * @param consumerPrincipalId the consuming analyst's principal, or null when unknown (shared origins are still
     *     detected)
     * @return the assessment, or empty when the reference carries no chain
     */
    public Optional<Assessment> assess(String ref, String consumerPrincipalId) {
        if (!ProvenanceChains.chained(ref)) {
            return Optional.empty();
        }
        List<Map<String, Object>> chain = chains.of(ref);
        if (chain.isEmpty()) {
            return Optional.empty();
        }
        String handle = consumerPrincipalId == null ? null : principals.findById(consumerPrincipalId)
                .map(PrincipalEntity::getHandle).orElse(null);
        return Optional.of(new Assessment(ProvenanceChains.originKey(chain), chain,
                isCircular(chain, identity.instanceId(), handle)));
    }

    /**
     * True when the chain names {@code instanceId} and {@code handle} before its last hop, importer hops aside.
     *
     * @param chain the chain, origin first
     * @param instanceId the consuming instance
     * @param handle the consuming principal's handle, or null
     */
    public static boolean isCircular(List<Map<String, Object>> chain, String instanceId, String handle) {
        if (chain == null || handle == null || instanceId == null) {
            return false;
        }
        for (int i = 0; i < chain.size() - 1; i++) {
            Map<String, Object> e = chain.get(i);
            if ("importer".equals(e.get("role"))) {
                continue;
            }
            if (instanceId.equals(e.get("instanceId")) && handle.equals(e.get("principal"))) {
                return true;
            }
        }
        return false;
    }

    /** True when two chains share an origin. */
    public static boolean sharedOrigin(List<Map<String, Object>> a, List<Map<String, Object>> b) {
        String x = ProvenanceChains.originKey(a);
        return x != null && x.equals(ProvenanceChains.originKey(b));
    }
}
