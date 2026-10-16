package ai.foresignal.provenance;

import ai.foresignal.source.spi.SourceDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The built-in licence catalogue (prompt 109; Weather-Imagery-Design §2.3), loaded from
 * {@code src/main/resources/provenance/licences.json} so new entries need no code. Each entry has an
 * {@code id} (an SPDX id or slug) and optional {@code aliases} that match a descriptor's free-text
 * {@code licence}. An unresolved id resolves to the restrictive {@code unknown} terms.
 */
@Component
public class LicenceCatalog {

    /** The classpath resource. */
    public static final String RESOURCE = "/provenance/licences.json";

    /** The id of the fallback terms. */
    public static final String UNKNOWN = "unknown";

    private final Map<String, LicenceTerms> byId = new LinkedHashMap<>();
    private final Map<String, String> byAlias = new LinkedHashMap<>();

    /** Loads the catalogue from the bundled resource. */
    public LicenceCatalog() {
        this(LicenceCatalog.class.getResourceAsStream(RESOURCE));
    }

    /** Loads the catalogue from a stream (tests). */
    public LicenceCatalog(InputStream stream) {
        if (stream == null) {
            throw new IllegalStateException("Licence catalogue resource is missing: " + RESOURCE);
        }
        try (stream) {
            JsonNode root = JsonMapper.builder().build().readTree(stream);
            JsonNode list = root.get("licences");
            if (list == null || !list.isArray()) {
                throw new IllegalStateException("Licence catalogue has no licences array");
            }
            for (JsonNode node : list) {
                LicenceTerms terms = new LicenceTerms(
                        text(node, "id"), text(node, "name"),
                        LicenceTerms.commercial(text(node, "commercialUse")),
                        flag(node, "shareAlike"), flag(node, "attributionRequired"),
                        LicenceTerms.redistribution(text(node, "redistribution")),
                        text(node, "attributionText"), text(node, "termsUrl"));
                byId.put(normalize(terms.id()), terms);
                JsonNode aliases = node.get("aliases");
                if (aliases != null && aliases.isArray()) {
                    for (JsonNode alias : aliases) {
                        byAlias.put(normalize(alias.asString()), terms.id());
                    }
                }
            }
            if (!byId.containsKey(UNKNOWN)) {
                byId.put(UNKNOWN, LicenceTerms.unknown());
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the licence catalogue", e);
        }
    }

    /** The terms with {@code id} (case-insensitive), or empty. */
    public Optional<LicenceTerms> find(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(byId.get(normalize(id)));
    }

    /**
     * The terms for an id or free-text licence: the catalogue entry when the id resolves, an alias
     * match when the text matches one, else the {@code unknown} terms.
     */
    public LicenceTerms terms(String idOrLicence) {
        if (idOrLicence == null || idOrLicence.isBlank()) {
            return byId.get(UNKNOWN);
        }
        String key = normalize(idOrLicence);
        LicenceTerms direct = byId.get(key);
        if (direct != null) {
            return direct;
        }
        String alias = byAlias.get(key);
        if (alias != null) {
            return byId.get(normalize(alias));
        }
        return byId.get(UNKNOWN);
    }

    /** True when {@code idOrLicence} resolves to a catalogue entry (by id or alias). */
    public boolean resolves(String idOrLicence) {
        if (idOrLicence == null || idOrLicence.isBlank()) {
            return false;
        }
        String key = normalize(idOrLicence);
        return byId.containsKey(key) || byAlias.containsKey(key);
    }

    /**
     * The terms of a descriptor: its explicit {@code licenceTerms} when set, else the catalogue entry
     * matching its free-text {@code licence}, else {@code unknown}.
     */
    public LicenceTerms forDescriptor(SourceDescriptor descriptor) {
        if (descriptor == null) {
            return byId.get(UNKNOWN);
        }
        if (descriptor.licenceTerms() != null) {
            return descriptor.licenceTerms();
        }
        return terms(descriptor.licence());
    }

    /**
     * The most restrictive of {@code inputs} (prompt 109, {@code derivedFrom}): the strictest single terms by
     * {@link LicenceTerms#restrictiveness()} when they dominate every flag of the others; otherwise merged
     * terms with id {@code derived}, the strictest value of each flag, the joined attribution texts and no
     * terms URL. An empty collection gives the restrictive {@code unknown} terms.
     */
    public LicenceTerms mostRestrictive(java.util.Collection<LicenceTerms> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return byId.get(UNKNOWN);
        }
        LicenceTerms strictest = null;
        for (LicenceTerms terms : inputs) {
            if (terms != null && (strictest == null || terms.restrictiveness() > strictest.restrictiveness())) {
                strictest = terms;
            }
        }
        if (strictest == null) {
            return byId.get(UNKNOWN);
        }
        boolean dominates = true;
        boolean shareAlike = false;
        boolean attribution = false;
        boolean commercialForbidden = false;
        java.util.Set<String> texts = new java.util.LinkedHashSet<>();
        for (LicenceTerms terms : inputs) {
            if (terms == null) {
                continue;
            }
            shareAlike |= terms.shareAlike();
            attribution |= terms.attributionRequired();
            commercialForbidden |= terms.effectiveCommercial() != LicenceTerms.CommercialUse.ALLOWED;
            if (terms.attributionText() != null && !terms.attributionText().isBlank()) {
                texts.add(terms.attributionText());
            }
        }
        dominates = (!shareAlike || strictest.shareAlike()) && (!attribution || strictest.attributionRequired())
                && (!commercialForbidden || strictest.effectiveCommercial() != LicenceTerms.CommercialUse.ALLOWED);
        if (dominates) {
            return strictest;
        }
        return new LicenceTerms("derived", "Most restrictive terms of the inputs",
                commercialForbidden ? LicenceTerms.CommercialUse.FORBIDDEN : LicenceTerms.CommercialUse.ALLOWED,
                shareAlike, attribution, strictest.effectiveRedistribution(), String.join("; ", texts), null);
    }

    /** Every catalogue entry. */
    public List<LicenceTerms> all() {
        return List.copyOf(byId.values());
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }

    private static boolean flag(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && value.asBoolean();
    }

    private static String normalize(String value) {
        return value.strip().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }
}
