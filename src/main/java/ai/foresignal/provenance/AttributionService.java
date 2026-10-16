package ai.foresignal.provenance;

import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.RecordRow;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Aggregated attributions for record-returning responses (prompt 109; Weather-Imagery-Design §6.10):
 * one {@code {sourceId, licence, text, termsUrl, count}} per distinct source and licence, plus the
 * joined {@code Foresignal-Attribution} text and the {@code Link rel=license} URLs.
 */
@org.springframework.stereotype.Component
public class AttributionService {

    /** The longest {@code Foresignal-Attribution} value. */
    public static final int MAX_TEXT = 1024;

    /** The most {@code Link rel=license} URLs. */
    public static final int MAX_LINKS = 10;

    private final SourceRegistry registry;
    private final LicenceCatalog catalog;

    /** Creates the service. */
    public AttributionService(SourceRegistry registry, LicenceCatalog catalog) {
        this.registry = registry;
        this.catalog = catalog;
    }

    /** One aggregated attribution. */
    public record Attribution(String sourceId, String licence, String text, String termsUrl, long count) {

        /** The API map. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("sourceId", sourceId);
            out.put("licence", licence);
            out.put("text", text);
            out.put("termsUrl", termsUrl);
            out.put("count", count);
            return out;
        }
    }

    /** Aggregates the attributions of {@code rows}, grouped by source and licence, in row order. */
    public List<Attribution> aggregate(Collection<RecordRow> rows, Map<Long, LicenceResolver.Resolution> resolutions) {
        Map<String, Attribution> byKey = new LinkedHashMap<>();
        for (RecordRow row : rows) {
            LicenceResolver.Resolution resolution = resolutions.get(row.seq());
            LicenceTerms terms = resolution == null ? catalog.forDescriptor(descriptor(row.sourceId()))
                    : resolution.terms();
            String key = row.sourceId() + "|" + terms.id();
            Attribution existing = byKey.get(key);
            if (existing == null) {
                byKey.put(key, new Attribution(row.sourceId(), terms.id(), text(row.sourceId(), terms),
                        terms.termsUrl(), 1));
            } else {
                byKey.put(key, new Attribution(existing.sourceId(), existing.licence(), existing.text(),
                        existing.termsUrl(), existing.count() + 1));
            }
        }
        return List.copyOf(byKey.values());
    }

    /** The attributions of a scope (optional {@code source} and {@code kind} filters), count 0. */
    public List<Attribution> scope(String sourceId, String kind) {
        List<Attribution> out = new ArrayList<>();
        for (SourceDescriptor descriptor : registry.descriptors()) {
            if (sourceId != null && !sourceId.isBlank() && !sourceId.equals(descriptor.id())) {
                continue;
            }
            if (kind != null && !kind.isBlank() && !descriptor.kinds().contains(kind)) {
                continue;
            }
            LicenceTerms terms = catalog.forDescriptor(descriptor);
            out.add(new Attribution(descriptor.id(), terms.id(), text(descriptor.id(), terms), terms.termsUrl(), 0));
        }
        return out;
    }

    /** The joined {@code Foresignal-Attribution} value (≤ {@value #MAX_TEXT}, truncated with "…"). */
    public String header(List<Attribution> attributions) {
        Set<String> texts = new LinkedHashSet<>();
        for (Attribution a : attributions) {
            if (a.text() != null && !a.text().isBlank()) {
                texts.add(a.text());
            }
        }
        String joined = String.join("; ", texts);
        return joined.length() <= MAX_TEXT ? joined : joined.substring(0, MAX_TEXT - 1) + "\u2026";
    }

    /** The distinct {@code termsUrl}s (≤ {@value #MAX_LINKS}). */
    public List<String> licenceLinks(List<Attribution> attributions) {
        Set<String> urls = new LinkedHashSet<>();
        for (Attribution a : attributions) {
            if (a.termsUrl() != null && urls.size() < MAX_LINKS) {
                urls.add(a.termsUrl());
            }
        }
        return List.copyOf(urls);
    }

    /** The {@code attributions} JSON array. */
    public static List<Map<String, Object>> asMaps(List<Attribution> attributions) {
        List<Map<String, Object>> out = new ArrayList<>(attributions.size());
        for (Attribution a : attributions) {
            out.add(a.toMap());
        }
        return out;
    }

    /** The attribution text of a source and its terms. */
    private String text(String sourceId, LicenceTerms terms) {
        SourceDescriptor descriptor = sourceId != null && registry.contains(sourceId)
                ? registry.descriptor(sourceId) : null;
        String base = descriptor != null && descriptor.attribution() != null ? descriptor.attribution() : null;
        if (base != null && !base.isBlank()) {
            return base;
        }
        if (terms.attributionText() != null) {
            return terms.attributionText();
        }
        return sourceId;
    }

    private SourceDescriptor descriptor(String sourceId) {
        return sourceId != null && registry.contains(sourceId) ? registry.descriptor(sourceId) : null;
    }
}
