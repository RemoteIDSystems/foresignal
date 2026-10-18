package ai.foresignal.stix;

import java.util.List;
import java.util.Optional;

/**
 * The read-only TAXII 2.1 collections of the {@code foresignal} API root (prompt 141). Ids are fixed UUIDv5 values
 * ({@link StixIds}) so clients can bookmark them.
 */
public enum TaxiiCollection {

    /** CVEs from the KEV catalog and NVD as STIX {@code vulnerability} objects. */
    VULNERABILITIES("vulnerabilities", "Vulnerabilities",
            "CVEs from the CISA KEV catalog and NVD (cisa-kev, nvd-cves) as STIX vulnerability objects.",
            List.of(StixMapper.VULNERABILITY_KIND)),

    /** abuse.ch malware URLs and C2 servers as STIX {@code indicator} objects. */
    INDICATORS("indicators", "Indicators",
            "abuse.ch URLhaus malware URLs and Feodo Tracker C2 servers as STIX indicator objects.",
            List.of(StixMapper.URL_KIND, StixMapper.C2_KIND));

    private final String id;
    private final String title;
    private final String description;
    private final List<String> kinds;

    TaxiiCollection(String name, String title, String description, List<String> kinds) {
        this.id = StixIds.uuid5(StixIds.NAMESPACE, "taxii-collection:" + name).toString();
        this.title = title;
        this.description = description;
        this.kinds = kinds;
    }

    /** The collection id (a UUID). */
    public String id() {
        return id;
    }

    /** The title. */
    public String title() {
        return title;
    }

    /** The description. */
    public String description() {
        return description;
    }

    /** The record kinds the collection serves. */
    public List<String> kinds() {
        return kinds;
    }

    /** The collection with {@code id}, if any. */
    public static Optional<TaxiiCollection> byId(String id) {
        for (TaxiiCollection collection : values()) {
            if (collection.id.equalsIgnoreCase(id == null ? "" : id.strip())) {
                return Optional.of(collection);
            }
        }
        return Optional.empty();
    }
}
