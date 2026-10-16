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
 * terms, a per-record peer origin license ({@code _foresignal.originLicence}, 113) and a per-record MLAT
 * downgrade that keeps the license's attribution. One query per response resolves every seq.
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

    /** The volatile payload member a peer source (113) carries its provenance in. */
    public static final String PEER_MEMBER = "_foresignal";

    /** The member of {@link #PEER_MEMBER} naming the origin record's license id. */
    public static final String ORIGIN_LICENCE = "originLicence";

    private static final tools.jackson.databind.json.JsonMapper JSON =
            tools.jackson.databind.json.JsonMapper.builder().build();

    private final NamedParameterJdbcTemplate jdbc;
    private final LicenceCatalog catalog;
    private final SourceRegistry registry;
    private final SourceLicences sources;

    /** Creates the resolver without derived-source resolution (tests): descriptors resolve on their own. */
    public LicenceResolver(NamedParameterJdbcTemplate jdbc, LicenceCatalog catalog, SourceRegistry registry) {
        this(jdbc, catalog, registry, null);
    }

    /**
     * Creates the resolver.
     *
     * @param sources the source-terms resolver that follows {@code derivedFrom}; null to resolve descriptors alone
     */
    @org.springframework.beans.factory.annotation.Autowired
    public LicenceResolver(NamedParameterJdbcTemplate jdbc, LicenceCatalog catalog, SourceRegistry registry,
            SourceLicences sources) {
        this.jdbc = jdbc;
        this.catalog = catalog;
        this.registry = registry;
        this.sources = sources;
    }

    /**
     * A version's licence and how it was resolved.
     *
     * @param terms the terms in force
     * @param basis {@link #BASIS_BATCH}, {@link #BASIS_DESCRIPTOR}, {@link #BASIS_PEER} or {@link #BASIS_MLAT}
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
                resolution = new Resolution(batchTerms(stored.licence(), row.sourceId()), BASIS_BATCH,
                        stored.fetchId(), stored.adapterVersion(), stored.principal());
            } else {
                resolution = descriptorFallback(row);
            }
            Map<String, Object> payload = payloadOf(row);
            String origin = originLicence(payload);
            if (origin != null) {
                resolution = new Resolution(catalog.terms(origin), BASIS_PEER, resolution.fetchId(),
                        resolution.adapterVersion(), resolution.principal());
            }
            if (EgressPolicy.isMlat(payload)) {
                // The downgrade keeps the license id and its attribution (an ODbL record must still be credited).
                resolution = new Resolution(resolution.terms().downgradedToLocalOnly(), BASIS_MLAT,
                        resolution.fetchId(), resolution.adapterVersion(), resolution.principal());
            }
            out.put(seq == null ? -1L : seq, resolution);
        }
        return out;
    }

    /** The licence terms of a row's source descriptor (the fall-back), marked {@code descriptor}. */
    public Resolution descriptorFallback(RecordRow row) {
        return new Resolution(sourceTerms(row.sourceId()), BASIS_DESCRIPTOR, null, null, null);
    }

    /** The current terms of a source: {@link SourceLicences} when wired (follows {@code derivedFrom}). */
    LicenceTerms sourceTerms(String sourceId) {
        return sources != null ? sources.termsOf(sourceId) : catalog.forDescriptor(descriptor(sourceId));
    }

    /**
     * The terms of a snapshotted batch license id. An id the catalogue does not know (a custom source's own
     * {@code licenceTerms}, 024) resolves to the source's current terms when their ids match, else to
     * {@code unknown}.
     */
    LicenceTerms batchTerms(String storedId, String sourceId) {
        if (catalog.resolves(storedId)) {
            return catalog.terms(storedId);
        }
        LicenceTerms current = sourceTerms(sourceId);
        return storedId.equalsIgnoreCase(current.id()) ? current : catalog.terms(storedId);
    }

    /**
     * The per-record origin license of a peer record (113): {@code _foresignal.originLicence} as a license id
     * string, or an object whose {@code id} names one; null when absent.
     */
    static String originLicence(Map<String, Object> payload) {
        if (payload == null || !(payload.get(PEER_MEMBER) instanceof Map<?, ?> peer)) {
            return null;
        }
        Object origin = peer.get(ORIGIN_LICENCE);
        if (origin instanceof Map<?, ?> terms) {
            origin = terms.get("id");
        }
        return origin instanceof String id && !id.isBlank() ? id : null;
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
            Object parsed = JSON.readValue(payload, Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) parsed;
            return map;
        } catch (RuntimeException e) {
            return null;
        }
    }
}
