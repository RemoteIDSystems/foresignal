package ai.foresignal.exchange;

import ai.foresignal.collab.AccessResolver;
import ai.foresignal.collab.EvidenceItemEntity;
import ai.foresignal.collab.EvidenceItemRepository;
import ai.foresignal.collab.ShareableObjectEntity;
import ai.foresignal.federation.FederationIdentity;
import ai.foresignal.report.ReportBlocks;
import ai.foresignal.report.ReportEntity;
import ai.foresignal.report.ReportRepository;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Provenance chains of analytic products that cross analysts or instances (prompt 134): the ordered list of
 * {@code (instanceId, principal, productRef, manifestId, role, at)} from origin to now ({@code ProvenanceChainEntry}).
 *
 * <ul>
 *   <li>a received object ({@code remote:{instanceId}/{type}:{id}}): its package's chain as received (origin and
 *       authors on other instances, forwarders, this instance's importer last);</li>
 *   <li>a local report ({@code report:{id}}): the chains of the reports, received material and evidence items it
 *       cites, then its owner on this instance;</li>
 *   <li>a local evidence item ({@code evidence-item:{id}}): the chains of the reports and received material it cites,
 *       then its creator.</li>
 * </ul>
 * Other references have no chain. Chains are followed at most {@value #MAX_DEPTH} deep and never loop.
 */
@Component
public class ProvenanceChains {

    /** The deepest citation chain followed. */
    static final int MAX_DEPTH = 8;

    private final FederationIdentity identity;
    private final ReceivedObjectRepository receivedObjects;
    private final ReceivedPackageRepository receivedPackages;
    private final ReportRepository reports;
    private final EvidenceItemRepository evidenceItems;
    private final AccessResolver access;
    private final PrincipalRepository principals;

    /** Creates the component. */
    public ProvenanceChains(FederationIdentity identity, ReceivedObjectRepository receivedObjects,
            ReceivedPackageRepository receivedPackages, ReportRepository reports,
            EvidenceItemRepository evidenceItems, AccessResolver access, PrincipalRepository principals) {
        this.identity = identity;
        this.receivedObjects = receivedObjects;
        this.receivedPackages = receivedPackages;
        this.reports = reports;
        this.evidenceItems = evidenceItems;
        this.access = access;
        this.principals = principals;
    }

    /** True when {@code ref} names a product that can carry a chain. */
    public static boolean chained(String ref) {
        return ref != null && (ref.startsWith("remote:") || ref.startsWith("report:")
                || ref.startsWith("evidence-item:"));
    }

    /**
     * The chain of a product, origin first; empty when the reference carries none.
     *
     * @param ref the product reference
     * @return the chain
     */
    public List<Map<String, Object>> of(String ref) {
        List<Map<String, Object>> out = new ArrayList<>();
        collect(ref, 0, new HashSet<>(), out);
        return out;
    }

    /** The evidence key shared by every product with the same origin: {@code origin:{instanceId}/{productRef}}. */
    public static String originKey(List<Map<String, Object>> chain) {
        if (chain == null || chain.isEmpty()) {
            return null;
        }
        Map<String, Object> first = chain.get(0);
        return "origin:" + first.get("instanceId") + "/" + first.get("productRef");
    }

    private void collect(String ref, int depth, Set<String> visited, List<Map<String, Object>> out) {
        if (ref == null || depth > MAX_DEPTH) {
            return;
        }
        String clean = strip(ref);
        if (!visited.add(clean)) {
            return;
        }
        if (clean.startsWith("remote:")) {
            for (ReceivedObjectEntity o : receivedObjects.findByRemoteRef(clean)) {
                receivedPackages.findById(o.getReceivedId()).ifPresent(p -> {
                    for (Map<String, Object> e : p.getProvenanceChain()) {
                        add(out, new LinkedHashMap<>(e));
                    }
                });
                if (!out.isEmpty()) {
                    return;
                }
            }
            return;
        }
        if (clean.startsWith("report:")) {
            ReportEntity r = reports.findById(clean.substring("report:".length())).orElse(null);
            if (r == null) {
                return;
            }
            Set<String> cited = new LinkedHashSet<>();
            for (Map<String, Object> block : r.getSections()) {
                for (String c : ReportBlocks.citedRefs(block)) {
                    if (chained(c)) {
                        cited.add(c);
                    }
                }
            }
            for (String c : cited) {
                collect(c, depth + 1, visited, out);
            }
            String owner = access.record(clean).map(ShareableObjectEntity::getOwner).orElse(null);
            add(out, entry(handle(owner), clean, out.isEmpty() ? "origin" : "author", r.getCreatedAt()));
            return;
        }
        if (clean.startsWith("evidence-item:")) {
            EvidenceItemEntity e = evidenceItems.findById(clean.substring("evidence-item:".length())).orElse(null);
            if (e == null) {
                return;
            }
            for (String c : e.getCitedRefs()) {
                if (chained(c)) {
                    collect(c, depth + 1, visited, out);
                }
            }
            add(out, entry(handle(e.getCreatedBy()), clean, out.isEmpty() ? "origin" : "author", e.getCreatedAt()));
        }
    }

    private Map<String, Object> entry(String principal, String productRef, String role, Instant at) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("instanceId", identity.instanceId());
        e.put("principal", principal);
        e.put("productRef", productRef);
        e.put("manifestId", null);
        e.put("role", role);
        e.put("at", at == null ? null : at.toString());
        return e;
    }

    /** Appends an entry unless the same hop (instance, principal, product, role) is already there. */
    private static void add(List<Map<String, Object>> out, Map<String, Object> e) {
        for (Map<String, Object> x : out) {
            if (String.valueOf(x.get("instanceId")).equals(String.valueOf(e.get("instanceId")))
                    && String.valueOf(x.get("principal")).equals(String.valueOf(e.get("principal")))
                    && String.valueOf(x.get("productRef")).equals(String.valueOf(e.get("productRef")))
                    && String.valueOf(x.get("role")).equals(String.valueOf(e.get("role")))) {
                return;
            }
        }
        out.add(e);
    }

    private String handle(String principalId) {
        if (principalId == null) {
            return "unknown";
        }
        return principals.findById(principalId).map(PrincipalEntity::getHandle).orElse(principalId);
    }

    /** A reference without a block fragment or a pinned version. */
    static String strip(String ref) {
        String r = ref.strip();
        int hash = r.indexOf('#');
        if (hash > 0) {
            r = r.substring(0, hash);
        }
        if (!r.startsWith("remote:")) {
            int at = r.lastIndexOf('@');
            if (at > 0) {
                r = r.substring(0, at);
            }
        }
        return r;
    }
}
