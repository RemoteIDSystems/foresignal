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

    /** Follows {@code derivedFrom} when wired (prompt 109); null in hand-built unit tests. */
    private SourceLicences sourceLicences;

    /** Wires the source-terms resolver. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setSourceLicences(SourceLicences sourceLicences) {
        this.sourceLicences = sourceLicences;
    }

    /** The current terms of a source. */
    private LicenceTerms termsOf(SourceDescriptor descriptor) {
        return sourceLicences != null && descriptor != null ? sourceLicences.termsOf(descriptor)
                : catalog.forDescriptor(descriptor);
    }

    /**
     * The aggregated attribution of a set of sources (prompt 109: TileJSON {@code attribution} is the aggregate
     * for the layer's sources): each source's attribution line, or its license's attribution text when it has
     * none, distinct, joined with {@code " · "}; null when there is none.
     */
    public String layerText(Collection<SourceDescriptor> sources) {
        Set<String> parts = new LinkedHashSet<>();
        for (SourceDescriptor source : sources) {
            String text = text(source.id(), termsOf(source));
            if (text != null && !text.isBlank()) {
                parts.add(text.strip());
            }
        }
        return parts.isEmpty() ? null : String.join(" · ", parts);
    }

    /** Aggregates the attributions of {@code rows}, grouped by source and licence, in row order. */
    public List<Attribution> aggregate(Collection<RecordRow> rows, Map<Long, LicenceResolver.Resolution> resolutions) {
        Map<String, Attribution> byKey = new LinkedHashMap<>();
        for (RecordRow row : rows) {
            LicenceResolver.Resolution resolution = resolutions.get(row.seq());
            LicenceTerms terms = resolution == null ? termsOf(descriptor(row.sourceId()))
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
            LicenceTerms terms = termsOf(descriptor);
            out.add(new Attribution(descriptor.id(), terms.id(), text(descriptor.id(), terms), terms.termsUrl(), 0));
        }
        return out;
    }

    /**
     * The joined {@code Foresignal-Attribution} value: at most {@value #MAX_TEXT} characters, all of them
     * printable ISO-8859-1 (an HTTP field value; RFC 9110 §5.5), truncated with {@code "..."}. Typographic
     * dashes, quotes and the ellipsis become their ASCII forms, other characters outside ISO-8859-1 become
     * {@code ?}, and control characters become spaces.
     */
    public String header(List<Attribution> attributions) {
        Set<String> texts = new LinkedHashSet<>();
        for (Attribution a : attributions) {
            if (a.text() != null && !a.text().isBlank()) {
                texts.add(a.text());
            }
        }
        String joined = latin1(String.join("; ", texts));
        return joined.length() <= MAX_TEXT ? joined : joined.substring(0, MAX_TEXT - 3) + "...";
    }

    /** {@code text} as printable ISO-8859-1, for an HTTP header value. */
    static String latin1(String text) {
        StringBuilder out = new StringBuilder(text.length());
        text.codePoints().forEach(cp -> {
            switch (cp) {
                case 0x2010, 0x2011, 0x2012, 0x2013, 0x2014, 0x2015, 0x2212 -> out.append('-');
                case 0x2018, 0x2019, 0x201A, 0x2032 -> out.append('\'');
                case 0x201C, 0x201D, 0x201E, 0x2033 -> out.append('"');
                case 0x2026 -> out.append("...");
                case 0x00A0, 0x2007, 0x202F -> out.append(' ');
                default -> {
                    if (cp < 0x20 || cp == 0x7F || (cp >= 0x80 && cp < 0xA0)) {
                        out.append(' ');
                    } else if (cp > 0xFF) {
                        out.append('?');
                    } else {
                        out.appendCodePoint(cp);
                    }
                }
            }
        });
        return out.toString();
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
