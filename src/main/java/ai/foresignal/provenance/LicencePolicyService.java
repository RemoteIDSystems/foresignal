package ai.foresignal.provenance;

import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Resolves the caller's {@link LicencePolicy} against the source catalogue (prompt 109): the sources
 * a policy excludes wholesale, so a read path can prune them in SQL before reading (the cheap case).
 * A single record of a mixed source is still filtered by {@link LicenceResolver}.
 */
@Component
public class LicencePolicyService {

    /** The request header carrying a license policy (prompt 109). */
    public static final String POLICY_HEADER = "Foresignal-Licence-Policy";

    /**
     * The response header of a stream or tile filtered by a license policy (prompt 109). Those paths prune
     * excluded sources in SQL and never read the excluded records, so the value is the number of
     * <em>sources</em> the policy pruned (0 when it pruned none); JSON pages report records in
     * {@code licenceFiltered}.
     */
    public static final String FILTERED_HEADER = "Foresignal-Licence-Filtered";

    private final SourceRegistry registry;
    private final LicenceCatalog catalog;

    /** Follows {@code derivedFrom} when wired (prompt 109); null in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private SourceLicences sourceLicences;

    /** Creates the service. */
    public LicencePolicyService(SourceRegistry registry, LicenceCatalog catalog) {
        this.registry = registry;
        this.catalog = catalog;
    }

    /** The registered sources every record of which {@code policy} excludes. */
    public Set<String> excludedSources(LicencePolicy policy) {
        if (policy == null || policy.isEmpty()) {
            return Set.of();
        }
        Set<String> out = new LinkedHashSet<>();
        for (SourceDescriptor descriptor : registry.descriptors()) {
            LicenceTerms terms = sourceLicences != null ? sourceLicences.termsOf(descriptor.id())
                    : catalog.forDescriptor(descriptor);
            if (policy.excludes(terms)) {
                out.add(descriptor.id());
            }
        }
        return out;
    }

    /** {@code excluded} plus the sources {@code policy} excludes wholesale. */
    public Set<String> withExcluded(Set<String> excluded, LicencePolicy policy) {
        Set<String> extra = excludedSources(policy);
        if (extra.isEmpty()) {
            return excluded;
        }
        Set<String> out = new LinkedHashSet<>(excluded);
        out.addAll(extra);
        return out;
    }

    /**
     * The {@link #FILTERED_HEADER} value for {@code policy}: the number of sources it prunes, or null when the
     * policy is empty (no header).
     */
    public String filteredHeader(LicencePolicy policy) {
        return policy == null || policy.isEmpty() ? null : Integer.toString(excludedSources(policy).size());
    }

    /** The policy of a request: the token floor plus the {@code Foresignal-Licence-Policy} header/query. */
    public static LicencePolicy of(jakarta.servlet.http.HttpServletRequest request) {
        LicencePolicy token = ai.foresignal.security.CurrentToken.of(request)
                .map(current -> LicencePolicy.parse("licencePolicy", String.join(",", current.token().licencePolicy())))
                .orElse(LicencePolicy.NONE);
        String header = request.getHeader("Foresignal-Licence-Policy");
        String query = request.getParameter("licencePolicy");
        String raw = header != null && !header.isBlank() ? header : query;
        return token.plus(LicencePolicy.parse("licencePolicy", raw));
    }
}
