package ai.foresignal.baserate;

import ai.foresignal.security.EgressPath;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@code BaseRate}, {@code BaseRatePage} and {@code RegionProfile} API shapes (prompt 121). The per-source counts
 * of a {@code LOCAL_ONLY} source are left out of {@code sourceMix} where {@code EgressPolicy} does not allow that
 * source on the path, with the caveat {@code LOCAL_ONLY_WITHHELD}.
 */
public final class BaseRateViews {

    private BaseRateViews() {}

    /** A stored rate. */
    public static Map<String, Object> view(BaseRateEntity e, BaseRateService service, EgressPath path) {
        BaseRateDefinition definition = BaseRateDefinition.parse(e.getDefinition());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", e.getId());
        out.put("name", e.getName());
        out.put("definition", definition.toMap());
        out.put("referenceClass", definition.referenceClass());
        out.put("ratePerWindow", e.getRatePerWindow());
        out.put("pAtLeastOne", e.getPAtLeastOne());
        out.put("interval", BaseRateService.interval(e.getIntervalLower(), e.getIntervalUpper()));
        out.put("nWindows", e.getNWindows());
        out.put("nEvents", e.getNEvents());
        out.put("periodFrom", date(e.getPeriodFrom()));
        out.put("periodTo", date(e.getPeriodTo()));
        List<Map<String, Object>> caveats = new ArrayList<>(e.getCaveats());
        out.put("sourceMix", mix(e.getSourceMix(), service, path, caveats));
        out.put("licences", e.getLicenses());
        out.put("unconditional", e.getUnconditional());
        out.put("regionProfiles", service.profilesOf(regionsOf(e)).stream().map(BaseRateViews::profile).toList());
        out.put("caveats", caveats);
        out.put("computer", e.getComputer());
        out.put("computedAt", e.getComputedAt().toString());
        out.put("origin", e.getOrigin());
        out.put("cached", false);
        out.put("manifestId", e.getManifestId());
        return out;
    }

    /** A computed (ad-hoc) rate, never stored: {@code id} is null. */
    public static Map<String, Object> view(BaseRateService.Computed c, boolean cached, BaseRateService service,
            EgressPath path) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", null);
        out.put("name", null);
        out.put("definition", c.definition().toMap());
        out.put("referenceClass", c.definition().referenceClass());
        out.put("ratePerWindow", c.result().ratePerWindow());
        out.put("pAtLeastOne", c.result().pAtLeastOne());
        out.put("interval", BaseRateService.interval(c.result().lower(), c.result().upper()));
        out.put("nWindows", c.result().nWindows());
        out.put("nEvents", c.result().nEvents());
        out.put("periodFrom", date(c.result().periodFrom()));
        out.put("periodTo", date(c.result().periodTo()));
        List<Map<String, Object>> caveats = new ArrayList<>(c.caveats());
        Map<String, Object> rawMix = new LinkedHashMap<>(c.result().sourceMix());
        out.put("sourceMix", mix(rawMix, service, path, caveats));
        out.put("licences", c.licenses());
        out.put("unconditional", c.unconditional());
        out.put("regionProfiles", c.profiles().stream().map(BaseRateViews::profile).toList());
        out.put("caveats", caveats);
        out.put("computer", c.computer());
        out.put("computedAt", c.computedAt().toString());
        out.put("origin", "auto");
        out.put("cached", cached);
        out.put("manifestId", c.manifestId());
        return out;
    }

    /** The result of an ad-hoc query. */
    public static Map<String, Object> view(BaseRateService.QueryResult result, BaseRateService service,
            EgressPath path) {
        return result.stored() != null ? view(result.stored(), service, path)
                : view(result.computed(), result.cached(), service, path);
    }

    /** A page of stored rates. */
    public static Map<String, Object> page(BaseRateService.Page page, BaseRateService service, EgressPath path) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.items().stream().map(e -> view(e, service, path)).toList());
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    /** The {@code RegionProfile} object. */
    public static Map<String, Object> profile(RegionProfileEntity p) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("regionCode", p.getRegionCode());
        out.put("reportingDensity", p.getReportingDensity());
        out.put("densityDecile", p.getDensityDecile());
        out.put("computedAt", p.getComputedAt().toString());
        return out;
    }

    private static List<String> regionsOf(BaseRateEntity e) {
        return e.getRegionCodes();
    }

    private static Map<String, Object> mix(Map<String, Object> raw, BaseRateService service, EgressPath path,
            List<Map<String, Object>> caveats) {
        Map<String, Object> out = new LinkedHashMap<>();
        List<String> withheld = new ArrayList<>();
        raw.forEach((source, n) -> {
            if (service.mixAllowed(source, path)) {
                out.put(source, n instanceof Number num ? num.longValue() : n);
            } else {
                withheld.add(source);
            }
        });
        if (!withheld.isEmpty()) {
            Map<String, Object> caveat = new LinkedHashMap<>();
            caveat.put("code", "LOCAL_ONLY_WITHHELD");
            caveat.put("text", "The per-source counts of " + String.join(", ", withheld) + " are not shown on this path"
                    + " (licensed data stays local); the rate itself includes them.");
            caveat.put("data", Map.of("sources", withheld));
            caveats.add(caveat);
        }
        return out;
    }

    private static String date(LocalDate d) {
        return d == null ? null : d.toString();
    }
}
