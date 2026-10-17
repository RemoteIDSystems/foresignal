package ai.foresignal.baserate;

import ai.foresignal.baserate.reference.ReferenceFamilies;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The caveats of a base rate (prompt 121), pure: {@code SMALL_SAMPLE} below {@value BaseRateService#SMALL_SAMPLE}
 * windows, {@code MIXED_SOURCES} when more than one source contributed, {@code LOW_REPORTING_DENSITY} when a region's
 * reporting density is in the bottom decile, and {@code LOW_DETECTABILITY} when Foresignal's own history was used where
 * 116 detectability is below {@code absenceMinDetect}.
 */
public final class BaseRateCaveats {

    private BaseRateCaveats() {}

    /**
     * The caveats.
     *
     * @param definition the definition
     * @param result the computed rate
     * @param profiles the region profiles of the reference class
     * @param undetectable the regions where own history was used with low detectability
     * @param minDetect {@code absenceMinDetect}
     * @return the caveats, in a fixed order
     */
    public static List<Map<String, Object>> of(BaseRateDefinition definition, BaseRateCalculator.Result result,
            List<RegionProfileEntity> profiles, List<String> undetectable, double minDetect) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (result.nWindows() < BaseRateService.SMALL_SAMPLE) {
            out.add(caveat("SMALL_SAMPLE", "Only " + result.nWindows() + " windows: the interval is wide and the rate"
                    + " may move as history accumulates.", Map.of("nWindows", result.nWindows())));
        }
        if (result.sourcesUsed().size() > 1) {
            out.add(caveat("MIXED_SOURCES", "Different sources cover different regions or periods ("
                    + String.join(", ", result.sourcesUsed()) + "); each region-week uses one source, so nothing is"
                    + " counted twice, but coding rules differ between sources.",
                    Map.of("sources", List.copyOf(result.sourcesUsed()))));
        }
        List<String> low = new ArrayList<>();
        for (RegionProfileEntity p : profiles) {
            if (p.getDensityDecile() <= ReportingDensity.LOW_DECILE) {
                low.add(p.getRegionCode());
            }
        }
        if (!low.isEmpty()) {
            out.add(caveat("LOW_REPORTING_DENSITY", "Reporting on " + String.join(", ", low) + " is in the bottom"
                    + " decile for countries of similar population: events are likely under-reported and the true"
                    + " rate may be higher.", Map.of("regions", low)));
        }
        if (undetectable != null && !undetectable.isEmpty()) {
            String family = ReferenceFamilies.root(definition.family());
            out.add(caveat("LOW_DETECTABILITY", "Foresignal's own history is used where its sources would detect"
                    + " fewer than " + Math.round(minDetect * 100) + "% of " + family + " events ("
                    + String.join(", ", undetectable) + "): the true rate may be higher.",
                    Map.of("regions", List.copyOf(undetectable))));
        }
        return out;
    }

    private static Map<String, Object> caveat(String code, String text, Map<String, Object> data) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("code", code);
        out.put("text", text);
        out.put("data", data);
        return out;
    }
}
