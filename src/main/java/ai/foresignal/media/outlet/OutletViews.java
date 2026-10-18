package ai.foresignal.media.outlet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The API shapes of the outlet registry (prompt 133, {@code MediaOutlet}, {@code MediaRating},
 * {@code OutletConsensus}). Every consensus carries its per-provider breakdown: disagreement is shown, never hidden.
 *
 * <p>{@code LOCAL_ONLY} ratings (AllSides without a recorded permission, MBFC, restrictive operator imports) reach only
 * callers on a path that may carry local-only data ({@code EgressPolicy.allowsLocalOnlyData}); for any other path their
 * values and labels are withheld (provider, dimension, license and date stay) and the consensus is recomputed from the
 * {@code OPEN} providers alone.
 */
public final class OutletViews {

    private OutletViews() {}

    /**
     * A {@code MediaOutlet}.
     *
     * @param o the outlet
     * @param ratings its ratings (detail view), or null for the list view
     * @param consensus its stored consensus by dimension
     * @param localAllowed true when the caller may see {@code LOCAL_ONLY} ratings
     */
    public static Map<String, Object> outlet(OutletRegistry.Outlet o, List<OutletRegistry.Rating> ratings,
            Map<String, Consensus.Result> consensus, boolean localAllowed) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", o.id());
        out.put("ref", "outlet:" + o.id());
        out.put("name", o.name());
        out.put("domains", o.domains());
        out.put("country", o.country());
        out.put("languages", o.languages());
        out.put("type", o.type());
        out.put("ownerOrg", o.ownerOrg());
        out.put("parentOrg", o.parentOrg());
        out.put("wikidataQid", o.wikidataQid());
        out.put("originatorId", o.originatorId());
        out.put("aggregates", o.aggregates());
        out.put("rated", o.rated());
        if (ratings != null) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (OutletRegistry.Rating r : ratings) {
                list.add(rating(r, localAllowed));
            }
            out.put("ratings", list);
        }
        List<Map<String, Object>> cons = new ArrayList<>();
        for (Consensus.Result c : consensus.values()) {
            Consensus.Result shown = localAllowed ? c : openOnly(c);
            if (shown.nProviders() > 0) {
                cons.add(consensus(o.id(), shown, null));
            }
        }
        out.put("consensus", cons);
        out.put("createdAt", o.createdAt() == null ? null : o.createdAt().toString());
        out.put("updatedAt", o.updatedAt() == null ? null : o.updatedAt().toString());
        return out;
    }

    /** A {@code MediaRating}; a {@code LOCAL_ONLY} one has its values withheld unless {@code localAllowed}. */
    public static Map<String, Object> rating(OutletRegistry.Rating r, boolean localAllowed) {
        boolean withheld = !localAllowed && MediaVocabulary.LOCAL_ONLY.equals(r.egress());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("outletId", r.outletId());
        out.put("provider", r.provider());
        out.put("dimension", r.dimension());
        out.put("valueNorm", withheld ? null : r.valueNorm());
        out.put("valueRaw", withheld ? null : r.valueRaw());
        out.put("label", withheld ? null : r.label());
        out.put("ratedAt", r.ratedAt() == null ? null : r.ratedAt().toString());
        out.put("licence", r.license());
        out.put("attribution", r.attribution());
        out.put("url", r.url());
        out.put("egress", r.egress());
        out.put("importedAt", r.importedAt() == null ? null : r.importedAt().toString());
        return out;
    }

    /** An {@code OutletConsensus}. */
    public static Map<String, Object> consensus(String outletId, Consensus.Result c, String computedAt) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("outletId", outletId);
        out.put("dimension", c.dimension());
        out.put("median", c.median());
        out.put("nProviders", c.nProviders());
        out.put("disagreement", c.disagreement());
        out.put("bucket", c.bucket());
        List<Map<String, Object>> providers = new ArrayList<>();
        for (Consensus.ProviderValue v : c.providers()) {
            providers.add(v.toMap());
        }
        out.put("providers", providers);
        out.put("computedAt", computedAt);
        return out;
    }

    /** The consensus of {@code c} over its {@code OPEN} providers only. */
    public static Consensus.Result openOnly(Consensus.Result c) {
        List<Consensus.ProviderValue> open = new ArrayList<>();
        for (Consensus.ProviderValue v : c.providers()) {
            if (!MediaVocabulary.LOCAL_ONLY.equals(v.egress())) {
                open.add(v);
            }
        }
        return open.size() == c.providers().size() ? c : Consensus.compute(c.dimension(), open);
    }
}
