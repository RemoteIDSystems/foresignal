package ai.elaro.store;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Per-minute request accounting for one source.
 *
 * <p>Elaro is meant to be a good upstream citizen by construction (Executive Summary), so what it
 * spends is measured rather than estimated. {@code refused} counts requests the budget governor
 * declined, which distinguishes "we chose not to ask" from "the upstream failed".
 */
@Entity
@Table(name = "source_usage")
public class SourceUsageEntity {

    @EmbeddedId
    private SourceUsageId id;

    @Column(name = "requests", nullable = false)
    private long requests;

    @Column(name = "bytes", nullable = false)
    private long bytes;

    @Column(name = "errors", nullable = false)
    private long errors;

    @Column(name = "refused", nullable = false)
    private long refused;

    protected SourceUsageEntity() {
        // for JPA
    }

    public SourceUsageEntity(SourceUsageId id) {
        this.id = id;
    }

    public SourceUsageId getId() {
        return id;
    }

    public void setId(SourceUsageId id) {
        this.id = id;
    }

    public long getRequests() {
        return requests;
    }

    public void setRequests(long requests) {
        this.requests = requests;
    }

    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

    public long getErrors() {
        return errors;
    }

    public void setErrors(long errors) {
        this.errors = errors;
    }

    public long getRefused() {
        return refused;
    }

    public void setRefused(long refused) {
        this.refused = refused;
    }
}
