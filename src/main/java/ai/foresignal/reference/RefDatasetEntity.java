package ai.foresignal.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * The load state of one reference dataset (table {@code ref_dataset}, prompt 040): what was loaded, from
 * where, with which checksum and license, and how many rows. Written by the {@link ReferenceDataLoader}.
 */
@Entity
@Table(name = "ref_dataset")
public class RefDatasetEntity {

    @Id
    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @Column(name = "edition", length = 300)
    private String edition;

    @Column(name = "url", length = 1000)
    private String url;

    @Column(name = "sha256", length = 64)
    private String sha256;

    @Column(name = "licence", length = 100)
    private String licence;

    @Column(name = "attribution", length = 1000)
    private String attribution;

    @Column(name = "rows")
    private Long rows;

    @Column(name = "loaded_at")
    private Instant loadedAt;

    /** {@code absent}, {@code loading}, {@code loaded} or {@code failed}. */
    @Column(name = "state", length = 16)
    private String state;

    @Column(name = "error", length = 2000)
    private String error;

    /** For JPA. */
    protected RefDatasetEntity() {}

    /** The dataset name. */
    public String getName() {
        return name;
    }

    /** The edition, or null. */
    public String getEdition() {
        return edition;
    }

    /** The URL loaded from, or null. */
    public String getUrl() {
        return url;
    }

    /** The SHA-256 of the loaded file, or null. */
    public String getSha256() {
        return sha256;
    }

    /** The license, or null. */
    public String getLicence() {
        return licence;
    }

    /** The attribution, or null. */
    public String getAttribution() {
        return attribution;
    }

    /** The rows loaded, or null. */
    public Long getRows() {
        return rows;
    }

    /** When it was loaded, or null. */
    public Instant getLoadedAt() {
        return loadedAt;
    }

    /** The state. */
    public String getState() {
        return state;
    }

    /** The last error code or message, or null. */
    public String getError() {
        return error;
    }
}
