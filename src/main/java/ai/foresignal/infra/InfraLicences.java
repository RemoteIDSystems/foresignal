package ai.foresignal.infra;

import ai.foresignal.provenance.LicenceTerms;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * The licence of a derived infrastructure edge (prompt 104): the most restrictive of its inputs.
 * Pure.
 *
 * <p>Restrictiveness, from least to most: no attribution required; attribution required;
 * share-alike (ODbL, CC BY-SA); non-commercial; local only; redistribution forbidden. When two inputs
 * are equally restrictive but differ (for example ODbL and CC BY-SA, which are both share-alike and
 * not compatible with each other), the edge carries both ids, sorted and joined with {@code " AND "},
 * because both sets of terms apply.
 */
public final class InfraLicences {

    private InfraLicences() {}

    /** How restrictive a set of terms is (higher is more restrictive). */
    public static int restrictiveness(LicenceTerms terms) {
        if (terms == null) {
            return 5;
        }
        if (terms.forbidsRedistribution()) {
            return 6;
        }
        if (terms.localOnly()) {
            return 5;
        }
        if (terms.nonCommercial()) {
            return 4;
        }
        if (terms.shareAlike()) {
            return 3;
        }
        return terms.attributionRequired() ? 2 : 1;
    }

    /** The licence id of an edge whose inputs carry {@code inputs}. */
    public static String mostRestrictive(List<LicenceTerms> inputs) {
        int worst = inputs.stream().mapToInt(InfraLicences::restrictiveness).max().orElse(5);
        TreeSet<String> ids = new TreeSet<>(Comparator.naturalOrder());
        for (LicenceTerms terms : inputs) {
            if (restrictiveness(terms) == worst) {
                ids.add(terms == null ? "unknown" : terms.id());
            }
        }
        return ids.isEmpty() ? "unknown" : String.join(" AND ", ids);
    }
}
