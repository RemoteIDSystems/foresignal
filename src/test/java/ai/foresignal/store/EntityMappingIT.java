package ai.foresignal.store;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.test.IntegrationTest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Persists and reloads each entity, exercising the mappings most likely to be wrong: jsonb,
 * geometry, smallints and composite keys. {@code record_version} rows are inserted with native SQL,
 * the only way they are ever written.
 */
@IntegrationTest
class EntityMappingIT {

    private static final int WGS84 = 4326;
    private static final GeometryFactory GEOMETRY =
            new GeometryFactory(new PrecisionModel(), WGS84);

    private static final Instant NOW = Instant.parse("2026-03-01T12:00:00Z");

    @Autowired private SourceSettingsRepository sourceSettings;
    @Autowired private SourceCredentialRepository sourceCredentials;
    @Autowired private SourceHealthRepository sourceHealth;
    @Autowired private SourceUsageRepository sourceUsage;
    @Autowired private RecordHeadRepository recordHeads;
    @Autowired private RecordVersionRepository recordVersions;
    @Autowired private RecordLiveRepository recordLives;
    @Autowired private FetchLogRepository fetchLogs;
    @Autowired private SeqClockRepository seqClock;
    @Autowired private TriggerRuleRepository triggerRules;
    @Autowired private AlertRepository alerts;
    @Autowired private NotificationChannelRepository channels;
    @Autowired private NotificationRepository notifications;
    @Autowired private NotificationDeliveryRepository deliveries;
    @Autowired private AppSettingRepository appSettings;
    @Autowired private SystemSecretRepository systemSecrets;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private TransactionTemplate transactions;

    // ---------- source_settings ----------

    @Test
    @DisplayName("source settings round trip, including the params jsonb")
    void sourceSettingsRoundTrip() {
        var entity = new SourceSettingsEntity("usgs-earthquakes");
        entity.setEnabled(true);
        entity.setIntervalSeconds(60);
        entity.setBudgetPerMinute(10);
        entity.setBudgetPerDay(1000);
        entity.setRetention("3650d");
        entity.setUpdatedAt(NOW);
        entity.setParams(
                Map.of(
                        "minMagnitude", 2.5,
                        "window", "P1D",
                        "includeDeleted", false));

        sourceSettings.saveAndFlush(entity);
        sourceSettings.flush();

        SourceSettingsEntity loaded = reload(() -> sourceSettings.findById("usgs-earthquakes").orElseThrow());
        assertThat(loaded.isEnabled()).isTrue();
        assertThat(loaded.getIntervalSeconds()).isEqualTo(60);
        assertThat(loaded.getBudgetPerMinute()).isEqualTo(10);
        assertThat(loaded.getBudgetPerDay()).isEqualTo(1000);
        assertThat(loaded.getRetention()).isEqualTo("3650d");
        assertThat(loaded.getUpdatedAt()).isEqualTo(NOW);
        assertThat(loaded.getParams())
                .containsEntry("minMagnitude", 2.5)
                .containsEntry("window", "P1D")
                .containsEntry("includeDeleted", false);
    }

    @Test
    @DisplayName("a null retention means 'use the global default'")
    void sourceSettingsAllowsNullRetention() {
        var entity = new SourceSettingsEntity("adsblol-mil");
        entity.setUpdatedAt(NOW);
        sourceSettings.saveAndFlush(entity);

        assertThat(sourceSettings.findById("adsblol-mil")).get().extracting(
                        SourceSettingsEntity::getRetention)
                .isNull();
    }

    @Test
    void findsOnlyEnabledSources() {
        var enabled = new SourceSettingsEntity("enabled-source");
        enabled.setEnabled(true);
        enabled.setUpdatedAt(NOW);
        var disabled = new SourceSettingsEntity("disabled-source");
        disabled.setEnabled(false);
        disabled.setUpdatedAt(NOW);
        sourceSettings.saveAllAndFlush(List.of(enabled, disabled));

        assertThat(sourceSettings.findByEnabledTrue())
                .extracting(SourceSettingsEntity::getSourceId)
                .containsExactly("enabled-source");
    }

    @Test
    @DisplayName("the optimistic lock column is managed by Hibernate")
    void sourceSettingsHasVersionColumn() {
        var entity = new SourceSettingsEntity("versioned");
        entity.setUpdatedAt(NOW);
        sourceSettings.saveAndFlush(entity);
        long initial = sourceSettings.findById("versioned").orElseThrow().getLockVersion();

        transactions.executeWithoutResult(
                status -> {
                    SourceSettingsEntity loaded = sourceSettings.findById("versioned").orElseThrow();
                    loaded.setIntervalSeconds(99);
                    sourceSettings.saveAndFlush(loaded);
                });

        assertThat(sourceSettings.findById("versioned").orElseThrow().getLockVersion())
                .isGreaterThan(initial);
    }

    // ---------- source_credential ----------

    @Test
    @DisplayName("credential bytea columns round trip unchanged")
    void sourceCredentialRoundTrip() {
        byte[] ciphertext = {1, 2, 3, (byte) 0xFF, 0, (byte) 0x80};
        byte[] nonce = {9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 11, 12};

        sourceCredentials.saveAndFlush(
                new SourceCredentialEntity("nasa-firms", ciphertext, nonce, NOW));

        SourceCredentialEntity loaded = sourceCredentials.findById("nasa-firms").orElseThrow();
        assertThat(loaded.getCiphertext()).isEqualTo(ciphertext);
        assertThat(loaded.getNonce()).isEqualTo(nonce);
        assertThat(loaded.getUpdatedAt()).isEqualTo(NOW);
    }

    // ---------- source_health ----------

    @Test
    @DisplayName("health round trips both jsonb shapes: a list and a map")
    void sourceHealthRoundTrip() {
        var entity = new SourceHealthEntity("usgs-earthquakes");
        entity.setLastAttemptAt(NOW);
        entity.setLastSuccessAt(NOW.minusSeconds(60));
        entity.setLastErrorAt(NOW.minusSeconds(600));
        entity.setLastError("upstream returned 503");
        entity.setLastHttpStatus(503);
        entity.setConsecutiveFailures(3);
        entity.setLastRecordsSeen(120);
        entity.setLastRecordsChanged(7);
        entity.setRecentRecordCounts(new java.util.ArrayList<>(List.of(10, 20, 30)));
        entity.setBackoffUntil(NOW.plusSeconds(300));
        entity.setAdapterState(new java.util.LinkedHashMap<>(Map.of("etag", "\"abc123\"")));

        sourceHealth.saveAndFlush(entity);

        SourceHealthEntity loaded = reload(() -> sourceHealth.findById("usgs-earthquakes").orElseThrow());
        assertThat(loaded.getLastError()).isEqualTo("upstream returned 503");
        assertThat(loaded.getLastHttpStatus()).isEqualTo(503);
        assertThat(loaded.getConsecutiveFailures()).isEqualTo(3);
        assertThat(loaded.getRecentRecordCounts()).containsExactly(10, 20, 30);
        assertThat(loaded.getAdapterState()).containsEntry("etag", "\"abc123\"");
        assertThat(loaded.getBackoffUntil()).isEqualTo(NOW.plusSeconds(300));
    }

    @Test
    @DisplayName("recentRecordCounts keeps only the last five runs")
    void recentRecordCountsAreCapped() {
        var entity = new SourceHealthEntity("capped");
        for (int i = 1; i <= 8; i++) {
            entity.recordRun(i);
        }

        assertThat(entity.getRecentRecordCounts()).containsExactly(4, 5, 6, 7, 8);
    }

    @Test
    void sourceHealthAllowsEmptyOptionalFields() {
        sourceHealth.saveAndFlush(new SourceHealthEntity("fresh"));

        SourceHealthEntity loaded = sourceHealth.findById("fresh").orElseThrow();
        assertThat(loaded.getLastAttemptAt()).isNull();
        assertThat(loaded.getLastHttpStatus()).isNull();
        assertThat(loaded.getRecentRecordCounts()).isEmpty();
        assertThat(loaded.getAdapterState()).isEmpty();
    }

    // ---------- source_usage (composite key) ----------

    @Test
    @DisplayName("usage rows are keyed by (source_id, minute)")
    void sourceUsageCompositeKey() {
        Instant minute = NOW.truncatedTo(ChronoUnit.MINUTES);
        var first = new SourceUsageEntity(new SourceUsageId("usgs-earthquakes", minute));
        first.setRequests(5);
        first.setBytes(2048);
        first.setErrors(1);
        first.setRefused(2);
        var second = new SourceUsageEntity(
                new SourceUsageId("usgs-earthquakes", minute.plusSeconds(60)));
        second.setRequests(3);
        second.setBytes(1024);

        sourceUsage.saveAllAndFlush(List.of(first, second));

        assertThat(sourceUsage.count()).isEqualTo(2);
        SourceUsageEntity loaded =
                sourceUsage.findById(new SourceUsageId("usgs-earthquakes", minute)).orElseThrow();
        assertThat(loaded.getRequests()).isEqualTo(5);
        assertThat(loaded.getBytes()).isEqualTo(2048);
        assertThat(loaded.getErrors()).isEqualTo(1);
        assertThat(loaded.getRefused()).isEqualTo(2);
        assertThat(loaded.getId().getMinute()).isEqualTo(minute);
    }

    @Test
    @DisplayName("usage totals sum over the half-open window [from, to)")
    void sourceUsageTotals() {
        Instant base = NOW.truncatedTo(ChronoUnit.MINUTES);
        for (int i = 0; i < 4; i++) {
            var row = new SourceUsageEntity(
                    new SourceUsageId("usgs-earthquakes", base.plusSeconds(60L * i)));
            row.setRequests(1);
            row.setBytes(100);
            row.setErrors(i % 2);
            row.setRefused(2);
            sourceUsage.save(row);
        }
        sourceUsage.flush();

        UsageTotals totals =
                sourceUsage
                        .totalsForSource("usgs-earthquakes", base, base.plusSeconds(180))
                        .orElseThrow();

        // Minutes 0, 1 and 2 are inside the window; minute 3 is not.
        assertThat(totals.requests()).isEqualTo(3);
        assertThat(totals.bytes()).isEqualTo(300);
        assertThat(totals.errors()).isEqualTo(1);
        assertThat(totals.refused()).isEqualTo(6);
    }

    @Test
    @DisplayName("an empty window yields no totals rather than a row of zeroes")
    void sourceUsageTotalsEmptyWindow() {
        assertThat(sourceUsage.totalsForSource("nobody", NOW, NOW.plusSeconds(60))).isEmpty();
    }

    // ---------- record_version (immutable, partitioned) ----------

    @Test
    @DisplayName("a record_version inserted with native SQL reads back through the repository")
    void recordVersionReadsBack() {
        long seq = insertRecordVersion("usgs-earthquakes", "us7000abcd", 1, point(-122.4, 37.8));

        RecordVersionEntity loaded = recordVersions.findById(seq).orElseThrow();

        assertThat(loaded.getSeq()).isEqualTo(seq);
        assertThat(loaded.getSourceId()).isEqualTo("usgs-earthquakes");
        assertThat(loaded.getExternalId()).isEqualTo("us7000abcd");
        assertThat(loaded.getVersion()).isEqualTo(1);
        assertThat(loaded.getKind()).isEqualTo("earthquake");
        assertThat(loaded.getIngestedAt()).isEqualTo(NOW);
        assertThat(loaded.getObservedAt()).isEqualTo(NOW.minusSeconds(30));
        assertThat(loaded.getReceivedAt()).isEqualTo(NOW.minusSeconds(5));
        assertThat(loaded.getEventTime()).isEqualTo(NOW.minusSeconds(60));
        assertThat(loaded.getValidUntil()).isEqualTo(NOW.plusSeconds(3600));
        assertThat(loaded.getValidUntilExplicit()).isNull();
        assertThat(loaded.getValidFrom()).isEqualTo(NOW.minusSeconds(120));
        assertThat(loaded.getPrevSeq()).isNull();
        assertThat(loaded.getBatchId()).isEqualTo(77L);
        assertThat(loaded.getHistoryTier()).isEqualTo(HistoryTier.STANDARD);
        assertThat(loaded.getHistoryTierCode()).isEqualTo((short) 0);
        assertThat(loaded.getPersistReason()).isEqualTo((short) 1);
        assertThat(loaded.getQaFlags()).isEqualTo((short) 4);
        assertThat(loaded.isTombstone()).isFalse();
        assertThat(loaded.getPayload()).containsEntry("mag", 6.1);
        assertThat(loaded.getQuality()).containsEntry("positionSource", "gnss");
        assertThat(loaded.getObserver()).singleElement().satisfies(o -> assertThat(o).containsEntry("id", "rx1"));
    }

    @Test
    @DisplayName("a TRACK version is routed to the track tier and decodes its tier")
    void trackVersionReadsBack() {
        long seq = insertRecordVersion("adsblol-mil", "ae1234", 1, null, HistoryTier.TRACK);

        RecordVersionEntity loaded = recordVersions.findById(seq).orElseThrow();

        assertThat(loaded.getHistoryTier()).isEqualTo(HistoryTier.TRACK);
        assertThat(jdbc.queryForObject(
                        "select tableoid::regclass::text from record_version where seq = ?", String.class, seq))
                .startsWith("rv_t_");
    }

    @Test
    @DisplayName("a point geometry round trips with SRID 4326")
    void recordVersionGeometryRoundTrip() {
        Point original = point(-122.4194, 37.7749);
        long seq = insertRecordVersion("usgs-earthquakes", "us-point", 1, original);

        Geometry loaded = recordVersions.findById(seq).orElseThrow().getGeom();

        assertThat(loaded).isInstanceOf(Point.class);
        assertThat(loaded.getSRID()).isEqualTo(WGS84);
        assertThat(loaded.getCoordinate().x).isCloseTo(-122.4194, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(loaded.getCoordinate().y).isCloseTo(37.7749, org.assertj.core.data.Offset.offset(1e-9));
    }

    @Test
    void recordVersionAllowsNullGeometry() {
        long seq = insertRecordVersion("gdelt-events", "no-location", 1, null);

        assertThat(recordVersions.findById(seq).orElseThrow().getGeom()).isNull();
    }

    @Test
    @DisplayName("seq comes from record_seq and increases by one")
    void recordSequenceIsMonotonic() {
        long first = insertRecordVersion("usgs-earthquakes", "a", 1, null);
        long second = insertRecordVersion("usgs-earthquakes", "b", 1, null);

        assertThat(second).isEqualTo(first + 1);
    }

    @Test
    @DisplayName("versions of one record are ordered by seq")
    void recordVersionHistory() {
        insertRecordVersion("usgs-earthquakes", "us-rev", 1, null);
        insertRecordVersion("usgs-earthquakes", "us-rev", 2, null);
        insertRecordVersion("usgs-earthquakes", "us-rev", 3, null);

        assertThat(recordVersions.findBySourceIdAndExternalIdOrderBySeqDesc("usgs-earthquakes", "us-rev"))
                .extracting(RecordVersionEntity::getVersion)
                .containsExactly(3, 2, 1);
        assertThat(recordVersions.findBySourceIdAndExternalIdOrderBySeqAsc("usgs-earthquakes", "us-rev"))
                .extracting(RecordVersionEntity::getVersion)
                .containsExactly(1, 2, 3);
    }

    @Test
    void findsOneVersionByCoordinates() {
        insertRecordVersion("usgs-earthquakes", "us-rev", 2, null);

        assertThat(recordVersions.findBySourceIdAndExternalIdAndVersion("usgs-earthquakes", "us-rev", 2))
                .isPresent();
        assertThat(recordVersions.findBySourceIdAndExternalIdAndVersion("usgs-earthquakes", "us-rev", 99))
                .isEmpty();
    }

    @Test
    @DisplayName("two versions with the same (source, external, version) are allowed: no unique constraint")
    void noUniqueConstraintOnVersion() {
        insertRecordVersion("usgs-earthquakes", "dup", 1, null);
        insertRecordVersion("usgs-earthquakes", "dup", 1, null);

        assertThat(recordVersions.findBySourceIdAndExternalIdOrderBySeqAsc("usgs-earthquakes", "dup"))
                .hasSize(2);
    }

    // ---------- record_head (composite key) ----------

    @Test
    @DisplayName("a head round trips every persisted-state column")
    void recordHeadRoundTrip() {
        long seq = insertRecordVersion("usgs-earthquakes", "us7000abcd", 1, point(-122.4, 37.8));
        var id = new RecordHeadId("usgs-earthquakes", "us7000abcd");
        var head = new RecordHeadEntity(id);
        head.setSeq(seq);
        head.setVersion(1);
        head.setKind("earthquake");
        head.setEventTime(NOW.minusSeconds(60));
        head.setValidUntilExplicit(NOW.plusSeconds(7200));
        head.setValidFrom(NOW.minusSeconds(120));
        head.setTombstone(false);
        head.setStaticHash(-1234567890123L);
        head.setHashRecipe(2);
        head.setPersistedObservedAt(NOW);
        head.setPersistedReceivedAt(NOW.plusSeconds(1));
        head.setPersistedGeom(point(-122.4, 37.8));
        head.setPayload(Map.of("mag", 6.1));
        head.setQuality(Map.of("positionSource", "gnss"));
        head.setSearchText("M 6.1 - 10km NE of Example City");
        head.setTitle("M 6.1 - Example City");

        recordHeads.saveAndFlush(head);

        RecordHeadEntity loaded = reload(() -> recordHeads.findById(id).orElseThrow());
        assertThat(loaded.getId().getSourceId()).isEqualTo("usgs-earthquakes");
        assertThat(loaded.getId().getExternalId()).isEqualTo("us7000abcd");
        assertThat(loaded.getSeq()).isEqualTo(seq);
        assertThat(loaded.getVersion()).isEqualTo(1);
        assertThat(loaded.getKind()).isEqualTo("earthquake");
        assertThat(loaded.getEventTime()).isEqualTo(NOW.minusSeconds(60));
        assertThat(loaded.getValidUntilExplicit()).isEqualTo(NOW.plusSeconds(7200));
        assertThat(loaded.getValidFrom()).isEqualTo(NOW.minusSeconds(120));
        assertThat(loaded.isTombstone()).isFalse();
        assertThat(loaded.getStaticHash()).isEqualTo(-1234567890123L);
        assertThat(loaded.getHashRecipe()).isEqualTo(2);
        assertThat(loaded.getPersistedObservedAt()).isEqualTo(NOW);
        assertThat(loaded.getPersistedReceivedAt()).isEqualTo(NOW.plusSeconds(1));
        assertThat(loaded.getPersistedGeom().getSRID()).isEqualTo(WGS84);
        assertThat(loaded.getPayload()).containsEntry("mag", 6.1);
        assertThat(loaded.getQuality()).containsEntry("positionSource", "gnss");
        assertThat(loaded.getSearchText()).isEqualTo("M 6.1 - 10km NE of Example City");
        assertThat(loaded.getTitle()).isEqualTo("M 6.1 - Example City");
    }

    @Test
    @DisplayName("record_head has only the primary key and the (kind) index")
    void recordHeadIndexes() {
        assertThat(jdbc.queryForList(
                        "select indexname from pg_indexes where tablename = 'record_head' order by indexname",
                        String.class))
                .containsExactlyInAnyOrder("record_head_pkey", "record_head_kind_idx");
    }

    @Test
    @DisplayName("countByKind backs /v1/kinds")
    void countsHeadsByKind() {
        saveHead("usgs-earthquakes", "q1", "earthquake");
        saveHead("usgs-earthquakes", "q2", "earthquake");
        saveHead("adsblol-mil", "a1", "aircraft");

        assertThat(recordHeads.countByKind())
                .containsExactly(new KindCount("aircraft", 1), new KindCount("earthquake", 2));
    }

    @Test
    void countsHeadsBySource() {
        saveHead("usgs-earthquakes", "q1", "earthquake");
        saveHead("usgs-earthquakes", "q2", "earthquake");

        assertThat(recordHeads.countByIdSourceId("usgs-earthquakes")).isEqualTo(2);
        assertThat(recordHeads.findByIdSourceId("usgs-earthquakes")).hasSize(2);
    }

    // ---------- record_live ----------

    @Test
    @DisplayName("a live row round trips, including kinematic jsonb and geometry")
    void recordLiveRoundTrip() {
        var id = new RecordHeadId("adsblol-mil", "ae1234");
        var live = new RecordLiveEntity(id);
        live.setObservedAt(NOW);
        live.setLastReceivedAt(NOW.plusSeconds(2));
        live.setValidUntil(NOW.plusSeconds(360));
        live.setGeom(point(-77.0, 38.9));
        live.setFullHash(42L);
        live.setDirty(true);
        live.setKinematic(new java.util.LinkedHashMap<>(Map.of("track", 270.5, "gs", 410)));
        live.setQuality(Map.of("positionSource", "adsb"));
        live.setQaFlags((short) 3);

        recordLives.saveAndFlush(live);

        RecordLiveEntity loaded = reload(() -> recordLives.findById(id).orElseThrow());
        assertThat(loaded.getObservedAt()).isEqualTo(NOW);
        assertThat(loaded.getLastReceivedAt()).isEqualTo(NOW.plusSeconds(2));
        assertThat(loaded.getValidUntil()).isEqualTo(NOW.plusSeconds(360));
        assertThat(loaded.getGeom().getSRID()).isEqualTo(WGS84);
        assertThat(loaded.getFullHash()).isEqualTo(42L);
        assertThat(loaded.isDirty()).isTrue();
        assertThat(loaded.getKinematic()).containsEntry("track", 270.5).containsEntry("gs", 410);
        assertThat(loaded.getQuality()).containsEntry("positionSource", "adsb");
        assertThat(loaded.getQaFlags()).isEqualTo((short) 3);
        assertThat(recordLives.countByIdSourceId("adsblol-mil")).isEqualTo(1);
        assertThat(recordLives.findByIdSourceId("adsblol-mil")).hasSize(1);
    }

    @Test
    @DisplayName("kinematic defaults to an empty object and is never null")
    void recordLiveKinematicDefaults() {
        jdbc.update(
                """
                insert into record_live (source_id, external_id, observed_at, last_received_at,
                                         full_hash, dirty)
                values ('s', 'e', now(), now(), 1, false)
                """);

        assertThat(jdbc.queryForObject(
                        "select kinematic::text from record_live where external_id = 'e'", String.class))
                .isEqualTo("{}");
        assertThat(jdbc.queryForObject(
                        "select qa_flags from record_live where external_id = 'e'", Short.class))
                .isZero();
        var live = new RecordLiveEntity(new RecordHeadId("s", "x"));
        live.setKinematic(null);
        live.setQaFlags(null);
        assertThat(live.getKinematic()).isEmpty();
        assertThat(live.getQaFlags()).isZero();
    }

    @Test
    @DisplayName("record_live has a GiST index on geom, no index on observed_at, and HOT storage parameters")
    void recordLiveIndexesAndStorage() {
        List<String> definitions = jdbc.queryForList(
                "select indexdef from pg_indexes where tablename = 'record_live'", String.class);
        assertThat(definitions).anyMatch(d -> d.contains("record_live_geom_gist") && d.toLowerCase().contains("gist"));
        assertThat(definitions).noneMatch(d -> d.contains("observed_at"));
        assertThat(definitions).hasSize(2); // primary key + GiST

        String options = jdbc.queryForObject(
                "select array_to_string(reloptions, ',') from pg_class where relname = 'record_live'",
                String.class);
        assertThat(options)
                .contains("fillfactor=70")
                .contains("autovacuum_vacuum_scale_factor=0.02")
                .contains("autovacuum_vacuum_cost_limit=2000");
    }

    // ---------- fetch_log ----------

    @Test
    @DisplayName("a fetch-log row round trips every column, with enums stored as names")
    void fetchLogRoundTrip() {
        var entry = new FetchLogEntity("usgs-earthquakes", FetchTrigger.SCHEDULED, FetchStatus.OK, NOW);
        entry.setReceivedAt(NOW.plusSeconds(1));
        entry.setIngestedAt(NOW.plusSeconds(2));
        entry.setHttpStatus(200);
        entry.setRequestCount(1);
        entry.setUrlRedacted("https://earthquake.usgs.gov/feed?key=***");
        entry.setEtag("\"abc\"");
        entry.setBytes(12345L);
        entry.setBodySha256("f".repeat(64));
        entry.setAdapterVersion("1.0.0");
        entry.setLicence("public-domain");
        entry.setPrincipal("admin");
        entry.setRecordsSeen(10);
        entry.setRejected(1);
        entry.setLate(2);
        entry.setSkipped(3);
        entry.setVersionsWritten(4);
        entry.setLiveUpdates(5);
        entry.setTouches(6);
        entry.setFromSeq(100L);
        entry.setToSeq(103L);
        entry.setCoverage(new CoverageSample(3, 10, 9, List.of(-180.0, -90.0, 180.0, 90.0)));
        entry.setError(null);

        Long id = fetchLogs.saveAndFlush(entry).getId();

        FetchLogEntity loaded = reload(() -> fetchLogs.findById(id).orElseThrow());
        assertThat(loaded.getSourceId()).isEqualTo("usgs-earthquakes");
        assertThat(loaded.getTrigger()).isEqualTo(FetchTrigger.SCHEDULED);
        assertThat(loaded.getStatus()).isEqualTo(FetchStatus.OK);
        assertThat(loaded.getStartedAt()).isEqualTo(NOW);
        assertThat(loaded.getReceivedAt()).isEqualTo(NOW.plusSeconds(1));
        assertThat(loaded.getIngestedAt()).isEqualTo(NOW.plusSeconds(2));
        assertThat(loaded.getHttpStatus()).isEqualTo(200);
        assertThat(loaded.getRequestCount()).isEqualTo(1);
        assertThat(loaded.getUrlRedacted()).endsWith("key=***");
        assertThat(loaded.getEtag()).isEqualTo("\"abc\"");
        assertThat(loaded.getBytes()).isEqualTo(12345L);
        assertThat(loaded.getBodySha256()).hasSize(64);
        assertThat(loaded.getAdapterVersion()).isEqualTo("1.0.0");
        assertThat(loaded.getLicence()).isEqualTo("public-domain");
        assertThat(loaded.getPrincipal()).isEqualTo("admin");
        assertThat(loaded.getRecordsSeen()).isEqualTo(10);
        assertThat(loaded.getRejected()).isEqualTo(1);
        assertThat(loaded.getLate()).isEqualTo(2);
        assertThat(loaded.getSkipped()).isEqualTo(3);
        assertThat(loaded.getVersionsWritten()).isEqualTo(4);
        assertThat(loaded.getLiveUpdates()).isEqualTo(5);
        assertThat(loaded.getTouches()).isEqualTo(6);
        assertThat(loaded.getFromSeq()).isEqualTo(100L);
        assertThat(loaded.getToSeq()).isEqualTo(103L);
        assertThat(loaded.getCoverage().receivers()).isEqualTo(3);
        assertThat(loaded.getCoverage().region()).containsExactly(-180.0, -90.0, 180.0, 90.0);
        assertThat(loaded.getError()).isNull();
        assertThat(jdbc.queryForObject("select trigger || '/' || status from fetch_log where id = ?", String.class, id))
                .isEqualTo("SCHEDULED/OK");
    }

    @Test
    @DisplayName("the fetch log pages newest first by keyset on id")
    void fetchLogKeysetPaging() {
        for (int i = 0; i < 5; i++) {
            fetchLogs.save(new FetchLogEntity("s", FetchTrigger.MANUAL, FetchStatus.FAILED, NOW.plusSeconds(i)));
        }
        fetchLogs.save(new FetchLogEntity("other", FetchTrigger.PUSH, FetchStatus.REFUSED, NOW));
        fetchLogs.flush();

        List<FetchLogEntity> first = fetchLogs.findBySourceIdOrderByIdDesc("s", Limit.of(2));
        assertThat(first).extracting(FetchLogEntity::getStartedAt)
                .containsExactly(NOW.plusSeconds(4), NOW.plusSeconds(3));

        List<FetchLogEntity> next = fetchLogs.findBySourceIdAndIdLessThanOrderByIdDesc(
                "s", first.get(1).getId(), Limit.of(10));
        assertThat(next).extracting(FetchLogEntity::getStartedAt)
                .containsExactly(NOW.plusSeconds(2), NOW.plusSeconds(1), NOW);
    }

    @Test
    @DisplayName("fetch_log is indexed on (source_id, started_at DESC)")
    void fetchLogIndex() {
        assertThat(jdbc.queryForList(
                        "select indexdef from pg_indexes where tablename = 'fetch_log'", String.class))
                .anyMatch(d -> d.contains("(source_id, started_at DESC)"));
    }

    // ---------- seq_clock ----------

    @Test
    @DisplayName("seq_clock maps minutes to first seqs and finds the cutoff for a time")
    void seqClockRoundTrip() {
        Instant minute = NOW.truncatedTo(ChronoUnit.MINUTES);
        seqClock.saveAndFlush(new SeqClockEntity(minute, 100));
        seqClock.saveAndFlush(new SeqClockEntity(minute.plusSeconds(60), 250));

        assertThat(seqClock.findById(minute).orElseThrow().getFirstSeq()).isEqualTo(100);
        assertThat(seqClock.findFirstByMinuteLessThanEqualOrderByMinuteDesc(minute.plusSeconds(90)))
                .get()
                .extracting(SeqClockEntity::getFirstSeq)
                .isEqualTo(250L);
        assertThat(seqClock.findFirstByMinuteLessThanEqualOrderByMinuteDesc(minute.minusSeconds(1))).isEmpty();
    }

    // ---------- trigger_rule ----------

    @Test
    @DisplayName("a rule round trips, including conditions jsonb and a multipolygon geofence")
    void triggerRuleRoundTrip() {
        UUID id = UUID.randomUUID();
        var rule = new TriggerRuleEntity(id, "Big quakes near the bay");
        rule.setEnabled(true);
        rule.setSources(List.of("usgs-earthquakes"));
        rule.setKinds(List.of("earthquake"));
        rule.setTags(List.of("quakes"));
        rule.setSeverity("critical");
        rule.setAutoResolve(Map.of("onInverseEdge", true, "onTombstone", false, "onExpiry", true));
        rule.setConditions(
                new java.util.ArrayList<>(
                        List.of(
                                new TriggerCondition("properties.mag", "gte", 6.0),
                                new TriggerCondition("properties.status", "eq", "reviewed"))));
        rule.setGeofence(multiPolygon());
        rule.setMode(TriggerMode.ENTER);
        rule.setOnlyNew(true);
        rule.setCooldownSeconds(900);
        rule.setMessageTemplate("M{{mag}} near {{place}}");
        rule.setCreatedAt(NOW);
        rule.setUpdatedAt(NOW);

        triggerRules.saveAndFlush(rule);

        TriggerRuleEntity loaded = reload(() -> triggerRules.findById(id).orElseThrow());
        assertThat(loaded.getName()).isEqualTo("Big quakes near the bay");
        assertThat(loaded.getMode()).isEqualTo(TriggerMode.ENTER);
        assertThat(loaded.isOnlyNew()).isTrue();
        assertThat(loaded.getCooldownSeconds()).isEqualTo(900);
        assertThat(loaded.getMessageTemplate()).isEqualTo("M{{mag}} near {{place}}");
        assertThat(loaded.getConditions())
                .extracting(TriggerCondition::path)
                .containsExactly("properties.mag", "properties.status");
        assertThat(loaded.getConditions().get(0).op()).isEqualTo("gte");
        assertThat(loaded.getGeofence()).isNotNull();
        assertThat(loaded.getGeofence().getSRID()).isEqualTo(WGS84);
        assertThat(loaded.getGeofence().getNumGeometries()).isEqualTo(1);
        assertThat(loaded.getSources()).containsExactly("usgs-earthquakes");
        assertThat(loaded.getKinds()).containsExactly("earthquake");
        assertThat(loaded.getTags()).containsExactly("quakes");
        assertThat(loaded.getSeverity()).isEqualTo("critical");
        assertThat(loaded.getCooldownScope()).isEqualTo("record");
        assertThat(loaded.getAutoResolve()).containsEntry("onTombstone", false);
        assertThat(loaded.getSuppressedCount()).isZero();
    }

    @Test
    @DisplayName("mode is stored as the lowercase wire value from Architecture §7.1")
    void triggerModeIsStoredLowercase() {
        UUID id = UUID.randomUUID();
        var rule = new TriggerRuleEntity(id, "lowercase mode");
        rule.setMode(TriggerMode.EXIT);
        rule.setCreatedAt(NOW);
        rule.setUpdatedAt(NOW);
        triggerRules.saveAndFlush(rule);

        String stored =
                jdbc.queryForObject(
                        "select mode from trigger_rule where id = ?", String.class, id);

        assertThat(stored).isEqualTo("exit");
    }

    @Test
    @DisplayName("reappear_as_enter round trips and defaults to false in the database")
    void triggerRuleReappearAsEnter() {
        UUID id = UUID.randomUUID();
        var rule = new TriggerRuleEntity(id, "geofence rule");
        rule.setReappearAsEnter(true);
        rule.setCreatedAt(NOW);
        rule.setUpdatedAt(NOW);
        triggerRules.saveAndFlush(rule);

        assertThat(reload(() -> triggerRules.findById(id).orElseThrow()).isReappearAsEnter()).isTrue();

        UUID raw = UUID.randomUUID();
        jdbc.update(
                "insert into trigger_rule (id, name, enabled, conditions, only_new, created_at, updated_at)"
                        + " values (?, 'raw', true, '[]'::jsonb, false, now(), now())",
                raw);
        assertThat(jdbc.queryForObject(
                        "select reappear_as_enter from trigger_rule where id = ?", Boolean.class, raw))
                .isFalse();
    }

    @Test
    void triggerRuleDefaultsToMatchMode() {
        UUID id = UUID.randomUUID();
        var rule = new TriggerRuleEntity(id, "default mode");
        rule.setCreatedAt(NOW);
        rule.setUpdatedAt(NOW);
        triggerRules.saveAndFlush(rule);

        assertThat(triggerRules.findById(id).orElseThrow().getMode()).isEqualTo(TriggerMode.MATCH);
    }

    @Test
    void findsOnlyEnabledRules() {
        var enabled = new TriggerRuleEntity(UUID.randomUUID(), "on");
        enabled.setEnabled(true);
        enabled.setCreatedAt(NOW);
        enabled.setUpdatedAt(NOW);
        var disabled = new TriggerRuleEntity(UUID.randomUUID(), "off");
        disabled.setEnabled(false);
        disabled.setCreatedAt(NOW);
        disabled.setUpdatedAt(NOW);
        triggerRules.saveAllAndFlush(List.of(enabled, disabled));

        assertThat(triggerRules.findByEnabledTrue())
                .extracting(TriggerRuleEntity::getName)
                .containsExactly("on");
    }

    // ---------- alert ----------

    @Test
    @DisplayName("an alert gets an identity id and round trips")
    void alertRoundTrip() {
        UUID ruleId = UUID.randomUUID();
        var alert = new AlertEntity(ruleId, 42L, "usgs-earthquakes", "us7000abcd", NOW);
        alert.setMessage("M6.1 near Example City");

        AlertEntity saved = alerts.saveAndFlush(alert);

        assertThat(saved.getId()).isNotNull();
        AlertEntity loaded = alerts.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getRuleId()).isEqualTo(ruleId);
        assertThat(loaded.getSeq()).isEqualTo(42L);
        assertThat(loaded.getFiredAt()).isEqualTo(NOW);
        assertThat(loaded.getMessage()).isEqualTo("M6.1 near Example City");
        assertThat(loaded.getAcknowledgedAt()).isNull();
        assertThat(loaded.getObservedAt()).isNull();
        assertThat(loaded.getRecordSnapshot()).isNull();
        assertThat(loaded.getState()).isEqualTo("firing");
        assertThat(loaded.getSeverity()).isEqualTo("warning");
        assertThat(loaded.isSuppressed()).isFalse();
        assertThat(loaded.getTags()).isEmpty();
        assertThat(loaded.getRev()).isZero();
    }

    @Test
    @DisplayName("an alert carries the observation time and a record snapshot")
    void alertSnapshotRoundTrip() {
        var alert = new AlertEntity(UUID.randomUUID(), 7L, "usgs-earthquakes", "us-snap", NOW);
        alert.setObservedAt(NOW.minusSeconds(30));
        alert.setRecordSnapshot(Map.of(
                "kind", "earthquake",
                "version", 2,
                "observedAt", "2026-03-01T11:59:30Z",
                "geometry", Map.of("type", "Point", "coordinates", List.of(-122.4, 37.8)),
                "payload", Map.of("mag", 6.1)));

        Long id = alerts.saveAndFlush(alert).getId();

        AlertEntity loaded = reload(() -> alerts.findById(id).orElseThrow());
        assertThat(loaded.getObservedAt()).isEqualTo(NOW.minusSeconds(30));
        assertThat(loaded.getRecordSnapshot())
                .containsEntry("kind", "earthquake")
                .containsEntry("version", 2)
                .containsKey("geometry");
    }

    @Test
    @DisplayName("the cooldown lookup finds a recent firing for the same record")
    void alertCooldownLookup() {
        UUID ruleId = UUID.randomUUID();
        alerts.saveAndFlush(new AlertEntity(ruleId, 1L, "usgs-earthquakes", "us-1", NOW));

        assertThat(
                        alerts.existsByRuleIdAndSourceIdAndExternalIdAndFiredAtAfter(
                                ruleId, "usgs-earthquakes", "us-1", NOW.minusSeconds(60)))
                .isTrue();
        assertThat(
                        alerts.existsByRuleIdAndSourceIdAndExternalIdAndFiredAtAfter(
                                ruleId, "usgs-earthquakes", "us-1", NOW.plusSeconds(60)))
                .isFalse();
        assertThat(
                        alerts.existsByRuleIdAndSourceIdAndExternalIdAndFiredAtAfter(
                                ruleId, "usgs-earthquakes", "other", NOW.minusSeconds(60)))
                .isFalse();
    }

    // ---------- notification channel, notification and delivery (prompt 018) ----------

    @Test
    @DisplayName("a notification channel round trips, including its encrypted secrets and jsonb columns")
    void notificationChannelRoundTrip() {
        UUID id = UUID.randomUUID();
        UUID ruleId = UUID.randomUUID();
        var channel = new NotificationChannelEntity(id, "ops-slack", "slack");
        channel.setConfig(new java.util.LinkedHashMap<>(Map.of("username", "Foresignal")));
        channel.setSecretCiphertext(new byte[] {1, 2, 3});
        channel.setSecretNonce(new byte[] {4, 5, 6});
        channel.setEvents(new java.util.ArrayList<>(List.of("alert", "system")));
        channel.setRuleIds(new java.util.ArrayList<>(List.of(ruleId)));
        channel.setTimezone("America/Chicago");
        channel.setQuietHours(new java.util.LinkedHashMap<>(Map.of("start", "22:00", "end", "07:00")));
        channel.setCreatedAt(NOW);
        channel.setUpdatedAt(NOW);

        channels.saveAndFlush(channel);

        NotificationChannelEntity loaded = reload(() -> channels.findById(id).orElseThrow());
        assertThat(loaded.getName()).isEqualTo("ops-slack");
        assertThat(loaded.getConfig()).containsEntry("username", "Foresignal");
        assertThat(loaded.getSecretCiphertext()).containsExactly(1, 2, 3);
        assertThat(loaded.getSecretNonce()).containsExactly(4, 5, 6);
        assertThat(loaded.getEvents()).containsExactly("alert", "system");
        assertThat(loaded.getRuleIds()).containsExactly(ruleId);
        assertThat(loaded.getQuietHours()).containsEntry("start", "22:00");
        assertThat(loaded.getBacklogCap()).isEqualTo(20);
        assertThat(loaded.isEditInPlace()).isTrue();
        assertThat(loaded.getGroupWindowSeconds()).isZero();
        assertThat(channels.findByEnabledTrue()).hasSize(1);
    }

    @Test
    @DisplayName("a null rule_ids means 'every rule'")
    void channelAllowsNullRuleIds() {
        UUID id = UUID.randomUUID();
        var channel = new NotificationChannelEntity(id, "all-rules", "webhook");
        channel.setCreatedAt(NOW);
        channel.setUpdatedAt(NOW);
        channels.saveAndFlush(channel);

        assertThat(channels.findById(id).orElseThrow().getRuleIds()).isNull();
    }

    @Test
    @DisplayName("a notification round trips, including links, data, the text[] tags and update_of")
    void notificationRoundTrip() {
        var n = new NotificationEntity();
        n.setEvent("alert");
        n.setSubtype("alert.fired");
        n.setSeverity("warning");
        n.setTitle("[warning] Quake");
        n.setShortTitle("Quake");
        n.setBodyMarkdown("**M5**");
        n.setUrl("http://localhost:8080/v1/alerts/7");
        n.setLinks(List.of(Map.of("label", "Open alert", "url", "http://localhost:8080/v1/alerts/7")));
        n.setData(new java.util.LinkedHashMap<>(Map.of("alertId", 7)));
        n.setAlertId(7L);
        n.setTags(new String[] {"quake", "ops"});
        n.setUpdateOf(3L);
        n.setOrigin("abcd1234");
        n.setCreatedAt(NOW);

        Long id = notifications.saveAndFlush(n).getId();

        NotificationEntity loaded = reload(() -> notifications.findById(id).orElseThrow());
        assertThat(loaded.getTags()).containsExactly("quake", "ops");
        assertThat(loaded.getLinks()).singleElement().satisfies(l -> assertThat(l).containsEntry("label", "Open alert"));
        assertThat(loaded.getData()).containsEntry("alertId", 7);
        assertThat(loaded.getUpdateOf()).isEqualTo(3L);
        assertThat(jdbc.queryForObject("select pg_typeof(tags)::text from notification where id = ?", String.class, id))
                .isEqualTo("text[]");
        assertThat(notifications.findFirstByAlertIdAndSubtypeOrderByIdAsc(7L, "alert.fired")).isPresent();
    }

    @Test
    @DisplayName("a delivery round trips with its kind, reference and deferral; status is stored by name")
    void deliveryRoundTrip() {
        UUID channelId = UUID.randomUUID();
        var delivery = new NotificationDeliveryEntity(channelId, 9L, "alert", NOW);
        delivery.setAlertId(7L);
        delivery.setKind(DeliveryKind.UPDATE);
        delivery.setProviderMessageRef("123456");
        delivery.setDeferredReason(DeferredReason.QUIET_HOURS);
        delivery.setNote("folded into delivery 1");
        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredAt(NOW);

        Long id = deliveries.saveAndFlush(delivery).getId();

        NotificationDeliveryEntity loaded = deliveries.findById(id).orElseThrow();
        assertThat(loaded.getChannelId()).isEqualTo(channelId);
        assertThat(loaded.getNotificationId()).isEqualTo(9L);
        assertThat(loaded.getKind()).isEqualTo("update");
        assertThat(loaded.getProviderMessageRef()).isEqualTo("123456");
        assertThat(loaded.getDeferredReason()).isEqualTo("quiet_hours");
        assertThat(jdbc.queryForObject("select status from notification_delivery where id = ?", String.class, id))
                .isEqualTo("DELIVERED");
    }

    @Test
    @DisplayName("the deliveries page is newest first, below the cursor, filtered by status")
    void deliveriesPage() {
        UUID channelId = UUID.randomUUID();
        Long first = deliveries.saveAndFlush(new NotificationDeliveryEntity(channelId, 1L, "alert", NOW)).getId();
        Long second = deliveries.saveAndFlush(new NotificationDeliveryEntity(channelId, 2L, "alert", NOW)).getId();
        deliveries.saveAndFlush(new NotificationDeliveryEntity(UUID.randomUUID(), 3L, "alert", NOW));

        assertThat(deliveries.page(channelId, Long.MAX_VALUE, List.of(DeliveryStatus.PENDING), Limit.of(10)))
                .extracting(NotificationDeliveryEntity::getId).containsExactly(second, first);
        assertThat(deliveries.page(channelId, second, List.of(DeliveryStatus.PENDING), Limit.of(10)))
                .extracting(NotificationDeliveryEntity::getId).containsExactly(first);
        assertThat(deliveries.page(channelId, Long.MAX_VALUE, List.of(DeliveryStatus.FAILED), Limit.of(10))).isEmpty();
    }

    // ---------- app_setting and system_secret ----------

    @Test
    @DisplayName("a setting's raw JSON is stored verbatim, not re-encoded as a string")
    void appSettingStoresRawJson() {
        appSettings.saveAndFlush(
                new AppSettingEntity("retention", "{\"default\":\"7d\"}", NOW));

        // jsonb stores the parsed value and prints it in its canonical form, so compare ignoring
        // whitespace; the point is that it is an object, not a JSON string.
        assertThat(appSettings.findById("retention").orElseThrow().getValue())
                .isEqualToIgnoringWhitespace("{\"default\":\"7d\"}");
        assertThat(
                        jdbc.queryForObject(
                                "select jsonb_typeof(value) from app_setting where key = 'retention'",
                                String.class))
                .isEqualTo("object");
    }

    @Test
    void systemSecretRoundTrip() {
        byte[] hash = new byte[32];
        for (int i = 0; i < hash.length; i++) {
            hash[i] = (byte) i;
        }

        systemSecrets.saveAndFlush(new SystemSecretEntity("admin_token_hash", hash));

        assertThat(systemSecrets.findById("admin_token_hash").orElseThrow().getValue())
                .isEqualTo(hash);
    }

    // ---------- helpers ----------

    /** Reads the entity back in a fresh transaction, so the assertions see the database, not the
     * first-level cache. */
    private <T> T reload(java.util.function.Supplier<T> read) {
        return transactions.execute(status -> read.get());
    }

    void saveHead(String sourceId, String externalId, String kind) {
        long seq = insertRecordVersion(sourceId, externalId, 1, null);
        var head = new RecordHeadEntity(new RecordHeadId(sourceId, externalId));
        head.setSeq(seq);
        head.setVersion(1);
        head.setKind(kind);
        head.setStaticHash(1L);
        head.setHashRecipe(1);
        head.setPersistedObservedAt(NOW);
        head.setPersistedReceivedAt(NOW);
        head.setPayload(Map.of("k", 1));
        recordHeads.saveAndFlush(head);
    }

    private long insertRecordVersion(String sourceId, String externalId, int version, Point geom) {
        return insertRecordVersion(sourceId, externalId, version, geom, HistoryTier.STANDARD);
    }

    /**
     * Inserts a {@code record_version} the way the ingest writer will: native SQL, because the
     * entity is {@code @Immutable} and Hibernate never inserts it (invariant I9).
     */
    private long insertRecordVersion(
            String sourceId, String externalId, int version, Point geom, HistoryTier tier) {
        Long seq = jdbc.queryForObject("select nextval('record_seq')", Long.class);
        jdbc.update(
                INSERT_RECORD_VERSION,
                seq,
                java.sql.Timestamp.from(NOW),
                java.sql.Timestamp.from(NOW.minusSeconds(30)),
                java.sql.Timestamp.from(NOW.minusSeconds(5)),
                java.sql.Timestamp.from(NOW.minusSeconds(60)),
                java.sql.Timestamp.from(NOW.plusSeconds(3600)),
                77L,
                version,
                tier.code(),
                sourceId,
                externalId,
                geom == null ? null : geom.toText(),
                "{\"mag\":6.1,\"place\":\"Example City\"}",
                "{\"positionSource\":\"gnss\"}",
                "[{\"id\":\"rx1\"}]",
                java.sql.Timestamp.from(NOW.minusSeconds(120)));
        return seq;
    }

    /** Every {@code record_version} column, in DDL order. */
    private static final String INSERT_RECORD_VERSION =
            """
            insert into record_version
                (seq, ingested_at, observed_at, received_at, event_time, valid_until,
                 valid_until_explicit, prev_seq, batch_id, version, history_tier, persist_reason,
                 qa_flags, tombstone, source_id, external_id, kind, geom, payload, quality, observer,
                 valid_from)
            values (?, ?, ?, ?, ?, ?, null, null, ?, ?, ?, 1, 4, false, ?, ?, 'earthquake',
                    ST_SetSRID(ST_GeomFromText(cast(? as text)), 4326), ?::jsonb, ?::jsonb, ?::jsonb, ?)
            """;

    private static Point point(double lon, double lat) {
        return GEOMETRY.createPoint(new Coordinate(lon, lat));
    }

    private static MultiPolygon multiPolygon() {
        Polygon square =
                GEOMETRY.createPolygon(
                        new Coordinate[] {
                            new Coordinate(-123.0, 37.0),
                            new Coordinate(-122.0, 37.0),
                            new Coordinate(-122.0, 38.0),
                            new Coordinate(-123.0, 38.0),
                            new Coordinate(-123.0, 37.0)
                        });
        return GEOMETRY.createMultiPolygon(new Polygon[] {square});
    }
}
