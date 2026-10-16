package ai.foresignal.export;

import ai.foresignal.provenance.AttributionService;
import ai.foresignal.provenance.LicenceTerms;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.EgressPolicy.EgressContext;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Which sources an export, snapshot or pack may carry, and its license sidecar (prompt 111; 039, 062,
 * 109).
 *
 * <p>Every decision goes through the single egress policy,
 * {@link EgressPolicy#decide(EgressPath, String, LicenceTerms, Map, EgressContext)} on the {@code EXPORT}
 * path with {@link EgressContext#export(Viewer, boolean, boolean)} ({@code docs/Conventions.md}
 * "EgressPolicy"), and the license terms come from {@link EgressPolicy#sourceTerms(String)}, which follows
 * {@code derivedFrom} and the operator's egress override.
 *
 * <ul>
 *   <li>A private source (039) needs an admin caller with {@code includePrivate=true} and a viewer the
 *       policy lets see it.
 *   <li>A source whose terms make redistribution {@code FORBIDDEN} or {@code LOCAL_ONLY} (unknown
 *       terms count as {@code LOCAL_ONLY}) is excluded unless {@code personalUse=true}; then it is
 *       included and the sidecar says "personal use only — do not redistribute" and lists it.
 *   <li>The cold archive is the {@code ARCHIVE} path (operator-owned storage), allowed for every
 *       license; its manifests still record the license.
 * </ul>
 */
@Component
public class ExportLicenceService {

    /** The sidecar sentence of a personal-use export. */
    public static final String PERSONAL_USE_NOTICE = "Personal use only — do not redistribute.";

    private final EgressPolicy egress;
    private final SourceRegistry registry;
    private final AttributionService attributions;

    /** Creates the service. */
    public ExportLicenceService(EgressPolicy egress, SourceRegistry registry, AttributionService attributions) {
        this.egress = egress;
        this.registry = registry;
        this.attributions = attributions;
    }

    /**
     * The decision over a set of candidate sources.
     *
     * @param allowed the sources the export carries, in candidate order
     * @param personalUseSources the allowed sources included only because {@code personalUse=true}
     * @param excluded the sources left out, each {@code {sourceId, reason}}
     */
    public record Decision(List<String> allowed, List<String> personalUseSources, List<Map<String, Object>> excluded) {

        /** Copies the lists. */
        public Decision {
            allowed = List.copyOf(allowed);
            personalUseSources = List.copyOf(personalUseSources);
            excluded = List.copyOf(excluded);
        }
    }

    /**
     * Decides which of {@code candidates} may leave on {@code EXPORT}.
     *
     * @param viewer the caller as the egress policy sees it
     * @param admin true for an admin caller
     * @param includePrivate the request's {@code includePrivate} (meaningful for admin only)
     * @param personalUse the request's {@code personalUse}
     */
    public Decision decide(Collection<SourceDescriptor> candidates, Viewer viewer, boolean admin,
            boolean includePrivate, boolean personalUse) {
        List<String> allowed = new ArrayList<>();
        List<String> personal = new ArrayList<>();
        List<Map<String, Object>> excluded = new ArrayList<>();
        EgressContext context = EgressContext.export(viewer, admin && includePrivate, personalUse);
        for (SourceDescriptor descriptor : candidates) {
            String id = descriptor.id();
            EgressPolicy.Decision decision = egress.decide(EgressPath.EXPORT, id, null, null, context);
            if (!decision.allowed()) {
                // Non-admin callers are not told which private sources exist.
                if (!EgressPolicy.REASON_PRIVATE.equals(decision.reason()) || admin) {
                    excluded.add(exclusion(id, decision.reason()));
                }
                continue;
            }
            allowed.add(id);
            LicenceTerms terms = decision.terms() != null ? decision.terms() : egress.sourceTerms(id);
            if (terms.effectiveRedistribution() != LicenceTerms.Redistribution.ALLOWED) {
                personal.add(id);
            }
        }
        return new Decision(allowed, personal, excluded);
    }

    /**
     * The {@code ExportLicenceSummary} of an export: one entry per license with its terms, sources and
     * row count, the attributions of the sources carried, the personal-use flag and sources, and the
     * exclusions.
     *
     * @param rowsBySource rows written per source (sources without rows count 0)
     */
    public Map<String, Object> summary(Decision decision, Map<String, Long> rowsBySource) {
        Map<String, Map<String, Object>> byLicense = new LinkedHashMap<>();
        List<Map<String, Object>> attributionList = new ArrayList<>();
        for (String id : decision.allowed()) {
            if (!registry.contains(id)) {
                continue;
            }
            LicenceTerms terms = egress.sourceTerms(id);
            long rows = rowsBySource == null ? 0L : rowsBySource.getOrDefault(id, 0L);
            Map<String, Object> entry = byLicense.computeIfAbsent(terms.id(), key -> {
                Map<String, Object> created = new LinkedHashMap<>();
                created.put("terms", terms(terms));
                created.put("sources", new ArrayList<String>());
                created.put("rows", 0L);
                return created;
            });
            @SuppressWarnings("unchecked")
            List<String> sources = (List<String>) entry.get("sources");
            sources.add(id);
            entry.put("rows", (Long) entry.get("rows") + rows);
            for (AttributionService.Attribution attribution : attributions.scope(id, null)) {
                Map<String, Object> map = new LinkedHashMap<>(attribution.toMap());
                map.put("count", rows);
                map.put("requiredDisplay", terms.attributionRequired());
                if (map.get("text") == null) {
                    map.put("text", terms.name());
                }
                attributionList.add(map);
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("licences", new ArrayList<>(byLicense.values()));
        out.put("attributions", attributionList);
        out.put("personalUseOnly", !decision.personalUseSources().isEmpty());
        out.put("personalUseSources", decision.personalUseSources());
        out.put("excluded", decision.excluded());
        return out;
    }

    /** The license summary of one source written to the cold archive ({@code ARCHIVE} path). */
    public Map<String, Object> archiveSummary(SourceDescriptor descriptor, long rows) {
        // ARCHIVE (operator-owned storage) is allowed for every license; the decision still runs so the
        // manifest records what the single policy decided.
        egress.decide(EgressPath.ARCHIVE, descriptor.id(), null, null, EgressContext.NONE);
        Decision decision = new Decision(List.of(descriptor.id()), List.of(), List.of());
        Map<String, Object> summary = summary(decision, Map.of(descriptor.id(), rows));
        summary.put("personalUseOnly", false);
        return summary;
    }

    /** The plain-text attribution lines of a summary (one per distinct text). */
    public static List<String> attributionLines(Map<String, Object> summary) {
        List<String> out = new ArrayList<>();
        if (summary != null && summary.get("attributions") instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map && map.get("text") != null) {
                    String text = String.valueOf(map.get("text"));
                    if (!text.isBlank() && !out.contains(text)) {
                        out.add(text);
                    }
                }
            }
        }
        return out;
    }

    /** The first license's terms URL of a summary, or null. */
    public static String firstTermsUrl(Map<String, Object> summary) {
        if (summary != null && summary.get("licences") instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map && map.get("terms") instanceof Map<?, ?> terms
                        && terms.get("termsUrl") != null) {
                    return String.valueOf(terms.get("termsUrl"));
                }
            }
        }
        return null;
    }

    /** The first license's name of a summary, or null. */
    public static String firstLicenseName(Map<String, Object> summary) {
        if (summary != null && summary.get("licences") instanceof List<?> list && !list.isEmpty()
                && list.getFirst() instanceof Map<?, ?> map && map.get("terms") instanceof Map<?, ?> terms) {
            Object name = terms.get("name");
            return name == null ? null : String.valueOf(name);
        }
        return null;
    }

    private static Map<String, Object> terms(LicenceTerms terms) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", terms.id());
        out.put("name", terms.name());
        out.put("commercialUse", terms.commercialUse() == null ? "UNKNOWN" : terms.commercialUse().name());
        out.put("shareAlike", terms.shareAlike());
        out.put("attributionRequired", terms.attributionRequired());
        out.put("redistribution", terms.redistribution() == null ? "UNKNOWN" : terms.redistribution().name());
        out.put("attributionText", terms.attributionText());
        out.put("termsUrl", terms.termsUrl());
        return out;
    }

    private static Map<String, Object> exclusion(String sourceId, String reason) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sourceId", sourceId);
        out.put("reason", reason);
        return out;
    }
}
