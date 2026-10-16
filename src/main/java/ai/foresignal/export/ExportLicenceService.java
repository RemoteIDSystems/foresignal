package ai.foresignal.export;

import ai.foresignal.provenance.LicenceCatalog;
import ai.foresignal.provenance.LicenceEgressGuard;
import ai.foresignal.provenance.LicenceTerms;
import ai.foresignal.security.EgressPath;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Decides which sources an export may carry and builds its licence sidecar (prompt 111, 109).
 * FORBIDDEN and LOCAL_ONLY records are excluded unless {@code personalUse} is set, in which case the
 * sidecar says "personal use only — do not redistribute".
 */
@Component
public class ExportLicenceService {

    private final LicenceCatalog catalog;
    private final LicenceEgressGuard guard;

    /** Creates the service. */
    public ExportLicenceService(LicenceCatalog catalog, LicenceEgressGuard guard) {
        this.catalog = catalog;
        this.guard = guard;
    }

    /**
     * The egress decision for one source on the EXPORT path.
     *
     * @param descriptor the source
     * @param personalUse include FORBIDDEN/LOCAL_ONLY records
     */
    public boolean allows(SourceDescriptor descriptor, boolean personalUse) {
        LicenceTerms terms = catalog.forDescriptor(descriptor);
        return guard.allows(EgressPath.EXPORT, terms, personalUse, false);
    }

    /**
     * The allowed source ids and the summary of their licences.
     *
     * @param descriptors every candidate source
     * @param personalUse true when the operator exports for personal use
     */
    public Result summarize(Collection<SourceDescriptor> descriptors, boolean personalUse) {
        List<String> allowed = new ArrayList<>();
        List<String> personalUseSources = new ArrayList<>();
        Map<String, LicenceTerms> byId = new LinkedHashMap<>();
        List<Map<String, Object>> licences = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        List<Map<String, Object>> excluded = new ArrayList<>();
        for (SourceDescriptor descriptor : descriptors) {
            LicenceTerms terms = catalog.forDescriptor(descriptor);
            if (!guard.allows(EgressPath.EXPORT, terms, personalUse, false)) {
                Map<String, Object> out = new LinkedHashMap<>();
                out.put("sourceId", descriptor.id());
                out.put("licence", terms.id());
                out.put("reason", terms.effectiveRedistribution().name().toLowerCase(java.util.Locale.ROOT));
                excluded.add(out);
                continue;
            }
            allowed.add(descriptor.id());
            byId.put(terms.id(), terms);
            if (terms.localOnly() || terms.forbidsRedistribution()) {
                personalUseSources.add(descriptor.id());
            }
            if (seen.add(terms.id())) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("id", terms.id());
                entry.put("name", terms.name());
                entry.put("attributionText", terms.attributionText());
                entry.put("termsUrl", terms.termsUrl());
                entry.put("attributionRequired", terms.attributionRequired());
                licences.add(entry);
            }
        }
        List<String> attributions = new ArrayList<>();
        for (LicenceTerms terms : byId.values()) {
            if (terms.attributionText() != null && !terms.attributionText().isBlank()) {
                attributions.add(terms.attributionText());
            }
        }
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("licences", licences);
        summary.put("attributions", attributions);
        summary.put("personalUseOnly", !personalUseSources.isEmpty());
        summary.put("personalUseSources", personalUseSources);
        summary.put("excluded", excluded);
        return new Result(List.copyOf(allowed), List.copyOf(personalUseSources), summary, List.copyOf(attributions));
    }

    /**
     * The licence summary.
     *
     * @param allowedSources the source ids the export may carry
     * @param personalUseSources the FORBIDDEN/LOCAL_ONLY sources included
     * @param summary the {@code ExportLicenceSummary} JSON
     * @param attributions the attribution lines
     */
    public record Result(List<String> allowedSources, List<String> personalUseSources, Map<String, Object> summary,
            List<String> attributions) {}
}
