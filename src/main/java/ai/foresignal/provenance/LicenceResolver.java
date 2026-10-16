package ai.foresignal.provenance;

import ai.foresignal.security.EgressPolicy;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.RecordRow;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Resolves a version's licence (prompt 109; Core-Data-Model §14): the licence snapshotted on the
 * {@code fetch_log} row of its {@code batch_id}, with a fall-back to the source's current descriptor
 * terms and a per-record MLAT downgrade. One query per response resolves every seq.
 */
@Component
public class LicenceResolver {

    /** The licence came from the batch's {@code fetch_log} row. */
    public static final String BASIS_BATCH = "batch";

    /** The licence came from the source's current descriptor terms. */
    public static final String BASIS_DESCRIPTOR = "descriptor";

    /** The licence was downgraded because the position is MLAT. */
    public static final String BASIS_MLAT = "mlat-override";

    /** The licence came from a ledger origin licence (peer sources, 113). */
    public static final String BASIS_PEER = "peer-origin";

    private static final String SQL =
            "SELECT v.seq AS seq, f.licence AS licence, f.adapter_version AS adapter_version,"
                    + " f.principal AS principal, f.id AS fetch_id"
                    + " FROM record_version v LEFT JOIN fetch_log f ON f.id = v.batch_id WHERE v.seq IN (:seqs)";

    private final NamedParameterJdbcTemplate jdbc;
    private final LicenceCatalog catalog;
    private final SourceRegistry registry;

    /** Creates the resolver. */
    public LicenceResolver(NamedParameterJdbcTemplate jdbc, LicenceCatalog catalog, SourceRegistry registry) {
        this.jdbc = jdbc;
        this.catalog = catalog;
        this.registry = registry;
    }

    /**
     * A version's licence and how it was resolved.
     *
     * @param terms the terms in force
     * @param basis {@link #BASIS_BATCH}, {@link #BASIS_DESCRIPTOR} or {@link #BASIS_MLAT}
     * @param fetchId the {@code fetch_log.id} the licence came from, or null
     * @param adapterVersion the adapter version of that fetch, or null
     * @param principal the ingest principal of that fetch, or null
     */
    public record Resolution(LicenceTerms terms, String basis, Long fetchId, String adapterVersion, String principal) {}

    /** The resolution of one row (one query). */
    public Resolution resolve(RecordRow row) {
        return resolveAll(List.of(row)).getOrDefault(row.seq(), descriptorFallback(row));
    }

    /** The resolutions of {@code rows}, in one query. */
    public Map<Long, Resolution> resolveAll(Collection<RecordRow> rows) {
        Map<Long, Resolution> out = new LinkedHashMap<>();
        if (rows == null || rows.isEmpty()) {
            return out;
        }
        List<Long> seqs = rows.stream().map(RecordRow::seq).filter(java.util.Objects::nonNull).distinct().toList();
        Map<Long, FetchRow> fetched = seqs.isEmpty() ? Map.of() : fetch(seqs);
        for (RecordRow row : rows) {
            Long seq = row.seq();
            FetchRow stored = seq == null ? null : fetched.get(seq);
            Resolution resolution;
            if (stored != null && stored.licence() != null && !stored.licence().isBlank()) {
                resolution = new Resolution(catalog.terms(stored.licence()), BASIS_BATCH, stored.fetchId(),
                        stored.adapterVersion(), stored.principal());
            } else {
                resolution = descriptorFallback(row);
            }
            if (EgressPolicy.isMlat(payloadOf(row))) {
                resolution = new Resolution(catalog.terms("local-only-operator"), BASIS_MLAT, resolution.fetchId(),
                        resolution.adapterVersion(), resolution.principal());
            }
            out.put(seq == null ? -1L : seq, resolution);
        }
        return out;
    }

    /** The licence terms of a row's source descriptor (the fall-back), marked {@code descriptor}. */
    public Resolution descriptorFallback(RecordRow row) {
        return new Resolution(catalog.forDescriptor(descriptor(row.sourceId())), BASIS_DESCRIPTOR, null, null, null);
    }

    /** The descriptor of a source id, or null. */
    SourceDescriptor descriptor(String sourceId) {
        if (sourceId == null || !registry.contains(sourceId)) {
            return null;
        }
        return registry.descriptor(sourceId);
    }

    private Map<Long, FetchRow> fetch(List<Long> seqs) {
        Map<Long, FetchRow> out = new LinkedHashMap<>();
        jdbc.query(SQL, new MapSqlParameterSource("seqs", seqs), rs -> {
            long seq = rs.getLong("seq");
            String licence = rs.getString("licence");
            Long fetchId = rs.getObject("fetch_id") == null ? null : rs.getLong("fetch_id");
            out.put(seq, new FetchRow(licence, fetchId, rs.getString("adapter_version"), rs.getString("principal")));
        });
        return out;
    }

    private record FetchRow(String licence, Long fetchId, String adapterVersion, String principal) {}

    private static Map<String, Object> payloadOf(RecordRow row) {
        String payload = row.payload();
        if (payload == null || payload.isBlank()) {
            return null;
        }
        try {
            Object parsed = tools.jackson.databind.json.JsonMapper.builder().build().readValue(payload, Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) parsed;
            return map;
        } catch (RuntimeException e) {
            return null;
        }
    }
}
