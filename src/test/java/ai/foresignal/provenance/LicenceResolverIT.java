package ai.foresignal.provenance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import ai.foresignal.ingest.FetchLogEntry;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.store.FetchTrigger;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Coverage;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * {@link LicenceResolver} against the real writer (prompt 109, task 2): the batch license snapshotted on the
 * version's {@code fetch_log} row wins; a version whose fetch row is gone falls back to the descriptor
 * ({@code licenceBasis: descriptor}); an MLAT position is downgraded to {@code LOCAL_ONLY} with its license id and
 * attribution kept; a peer record's {@code _foresignal.originLicence} is its license; and a whole page resolves in
 * one SQL statement.
 */
@IntegrationTest
@Import(TestIngestSources.class)
class LicenceResolverIT {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");
    static final String SOURCE = "test-lic-resolver";
    static final String KIND = "test-licensed";

    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired SourceRegistry registry;
    @Autowired LicenceResolver resolver;
    @Autowired LicenceCatalog catalog;
    @Autowired SourceLicences sourceLicences;
    @Autowired JdbcTemplate jdbc;
    @Autowired NamedParameterJdbcTemplate named;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        if (registry.contains(SOURCE)) {
            registry.unregisterDynamic(SOURCE);
        }
        registry.registerDynamic(SourceDescriptor.builder(SOURCE).name("Resolver test").kind(KIND).mode(Mode.PUSH)
                .licence("CC BY 4.0").defaultBudget(Budget.of(60, 10_000)).build());
    }

    @AfterEach
    void tearDown() {
        if (registry.contains(SOURCE)) {
            registry.unregisterDynamic(SOURCE);
        }
    }

    /** Ingests {@code record} under a fetch row whose snapshotted license is {@code licence}. */
    void ingest(String licence, NormalizedRecord record) {
        FetchLogEntry entry = new FetchLogEntry(SOURCE, FetchTrigger.PUSH, FetchStatus.OK, T, T, 200, 0, null, null,
                null, null, "test-1", licence, "tester", Coverage.none(), null);
        TestRecords.await(records.submit(SOURCE, entry, T, List.of(record)));
    }

    RecordRow row(String externalId) {
        Map<String, Object> stored = jdbc.queryForMap(
                "SELECT seq, payload::text AS payload FROM record_version WHERE source_id = ? AND external_id = ?"
                        + " ORDER BY seq DESC LIMIT 1", SOURCE, externalId);
        long seq = ((Number) stored.get("seq")).longValue();
        return new RecordRow(SOURCE, externalId, KIND, 1, seq, null, null, null, null, null, false, null, null,
                (String) stored.get("payload"), null, null, null, 0, null, null, null, null, null, null, null, null,
                null, null);
    }

    static NormalizedRecord record(String id) {
        return NormalizedRecord.builder(id, KIND).observedAt(T).point(1, 1).put("value", 1).build();
    }

    @Test
    @DisplayName("the batch licence wins over the descriptor's, with the fetch row's provenance")
    void batchLicenceWins() {
        ingest("ODbL-1.0", record("batch"));
        LicenceResolver.Resolution resolution = resolver.resolve(row("batch"));
        assertThat(resolution.basis()).isEqualTo(LicenceResolver.BASIS_BATCH);
        assertThat(resolution.terms().id()).isEqualTo("ODbL-1.0");
        assertThat(resolution.fetchId()).isNotNull();
        assertThat(resolution.adapterVersion()).isEqualTo("test-1");
        assertThat(resolution.principal()).isEqualTo("tester");
    }

    @Test
    @DisplayName("a gone fetch row falls back to the descriptor's terms, marked licenceBasis descriptor")
    void descriptorFallback() {
        ingest("ODbL-1.0", record("orphan"));
        RecordRow row = row("orphan");
        jdbc.update("DELETE FROM fetch_log WHERE id = (SELECT batch_id FROM record_version WHERE seq = ?)", row.seq());
        LicenceResolver.Resolution resolution = resolver.resolve(row);
        assertThat(resolution.basis()).isEqualTo(LicenceResolver.BASIS_DESCRIPTOR);
        assertThat(resolution.terms().id()).isEqualTo("CC-BY-4.0");
        assertThat(resolution.fetchId()).isNull();
    }

    @Test
    @DisplayName("MLAT downgrades to LOCAL_ONLY and keeps the ODbL id and its attribution requirement")
    void mlatDowngrade() {
        ingest("ODbL-1.0", NormalizedRecord.builder("mlat", KIND).observedAt(T).point(1, 1)
                .put("posSource", "mlat").build());
        LicenceResolver.Resolution resolution = resolver.resolve(row("mlat"));
        assertThat(resolution.basis()).isEqualTo(LicenceResolver.BASIS_MLAT);
        assertThat(resolution.terms().id()).isEqualTo("ODbL-1.0");
        assertThat(resolution.terms().attributionRequired()).isTrue();
        assertThat(resolution.terms().effectiveRedistribution()).isEqualTo(LicenceTerms.Redistribution.LOCAL_ONLY);
    }

    @Test
    @DisplayName("a peer record's _foresignal.originLicence is its licence")
    void peerOriginLicence() {
        ingest("CC-BY-4.0", NormalizedRecord.builder("peer", KIND).observedAt(T).point(1, 1)
                .put("_foresignal", Map.of("originLicence", "CC0-1.0", "originSeq", 7)).build());
        LicenceResolver.Resolution resolution = resolver.resolve(row("peer"));
        assertThat(resolution.basis()).isEqualTo(LicenceResolver.BASIS_PEER);
        assertThat(resolution.terms().id()).isEqualTo("CC0-1.0");
    }

    @Test
    @DisplayName("a page of versions resolves in exactly one SQL statement")
    void oneStatementPerResolve() {
        ingest("ODbL-1.0", record("p1"));
        ingest("CC-BY-4.0", record("p2"));
        ingest("CC0-1.0", record("p3"));
        List<RecordRow> rows = List.of(row("p1"), row("p2"), row("p3"));
        NamedParameterJdbcTemplate counted = spy(named);
        LicenceResolver resolving = new LicenceResolver(counted, catalog, registry, sourceLicences);

        Map<Long, LicenceResolver.Resolution> resolved = resolving.resolveAll(rows);

        verify(counted, times(1)).query(anyString(), any(SqlParameterSource.class), any(RowCallbackHandler.class));
        assertThat(resolved.values()).extracting(r -> r.terms().id())
                .containsExactly("ODbL-1.0", "CC-BY-4.0", "CC0-1.0");
    }
}
