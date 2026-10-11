package ai.foresignal.store;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Persists and reloads each entity, exercising the mappings most likely to be wrong: jsonb,
 * geometry and composite keys.
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
    @Autowired private TriggerRuleRepository triggerRules;
    @Autowired private AlertRepository alerts;
    @Autowired private WebhookRepository webhooks;
    @Autowired private WebhookDeliveryRepository deliveries;
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

    // ---------- record_version (immutable) ----------

    @Test
    @DisplayName("a record_version inserted through JdbcTemplate reads back through the repository")
    void recordVersionReadsBack() {
        long seq = insertRecordVersion("usgs-earthquakes", "us7000abcd", 1, point(-122.4, 37.8));

        RecordVersionEntity loaded = recordVersions.findById(seq).orElseThrow();

        assertThat(loaded.getSeq()).isEqualTo(seq);
        assertThat(loaded.getSourceId()).isEqualTo("usgs-earthquakes");
        assertThat(loaded.getExternalId()).isEqualTo("us7000abcd");
        assertThat(loaded.getVersion()).isEqualTo(1);
        assertThat(loaded.getKind()).isEqualTo("earthquake");
        assertThat(loaded.getObservedAt()).isEqualTo(NOW);
        assertThat(loaded.getContentHash()).hasSize(64);
        assertThat(loaded.isTombstone()).isFalse();
        assertThat(loaded.getPayload()).containsEntry("mag", 6.1);
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
    @DisplayName("versions of one record are returned newest first")
    void recordVersionHistory() {
        insertRecordVersion("usgs-earthquakes", "us-rev", 1, null);
        insertRecordVersion("usgs-earthquakes", "us-rev", 2, null);
        insertRecordVersion("usgs-earthquakes", "us-rev", 3, null);

        assertThat(
                        recordVersions.findBySourceIdAndExternalIdOrderByVersionDesc(
                                "usgs-earthquakes", "us-rev"))
                .extracting(RecordVersionEntity::getVersion)
                .containsExactly(3, 2, 1);
    }

    @Test
    void findsOneVersionByCoordinates() {
        insertRecordVersion("usgs-earthquakes", "us-rev", 2, null);

        assertThat(
                        recordVersions.findBySourceIdAndExternalIdAndVersion(
                                "usgs-earthquakes", "us-rev", 2))
                .isPresent();
        assertThat(
                        recordVersions.findBySourceIdAndExternalIdAndVersion(
                                "usgs-earthquakes", "us-rev", 99))
                .isEmpty();
    }

    // ---------- record_head (composite key) ----------

    @Test
    @DisplayName("a head round trips, including its composite key and search text")
    void recordHeadRoundTrip() {
        long seq = insertRecordVersion("usgs-earthquakes", "us7000abcd", 1, point(-122.4, 37.8));
        var id = new RecordHeadId("usgs-earthquakes", "us7000abcd");
        var head = new RecordHeadEntity(id);
        head.setSeq(seq);
        head.setVersion(1);
        head.setKind("earthquake");
        head.setObservedAt(NOW);
        head.setValidUntil(NOW.plusSeconds(3600));
        head.setGeom(point(-122.4, 37.8));
        head.setContentHash("a".repeat(64));
        head.setTombstone(false);
        head.setSearchText("M 6.1 - 10km NE of Example City");

        recordHeads.saveAndFlush(head);

        RecordHeadEntity loaded = reload(() -> recordHeads.findById(id).orElseThrow());
        assertThat(loaded.getId().getSourceId()).isEqualTo("usgs-earthquakes");
        assertThat(loaded.getId().getExternalId()).isEqualTo("us7000abcd");
        assertThat(loaded.getSeq()).isEqualTo(seq);
        assertThat(loaded.getKind()).isEqualTo("earthquake");
        assertThat(loaded.getValidUntil()).isEqualTo(NOW.plusSeconds(3600));
        assertThat(loaded.getSearchText()).isEqualTo("M 6.1 - 10km NE of Example City");
        assertThat(loaded.getGeom().getSRID()).isEqualTo(WGS84);
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

    // ---------- trigger_rule ----------

    @Test
    @DisplayName("a rule round trips, including conditions jsonb and a multipolygon geofence")
    void triggerRuleRoundTrip() {
        UUID id = UUID.randomUUID();
        var rule = new TriggerRuleEntity(id, "Big quakes near the bay");
        rule.setEnabled(true);
        rule.setSourceId("usgs-earthquakes");
        rule.setKind("earthquake");
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

    // ---------- webhook and delivery ----------

    @Test
    @DisplayName("a webhook round trips, including its encrypted secret and rule_ids jsonb")
    void webhookRoundTrip() {
        UUID id = UUID.randomUUID();
        UUID ruleId = UUID.randomUUID();
        var webhook = new WebhookEntity(id, "ops-slack", "https://hooks.example/abc");
        webhook.setSecretCiphertext(new byte[] {1, 2, 3});
        webhook.setSecretNonce(new byte[] {4, 5, 6});
        webhook.setRuleIds(new java.util.ArrayList<>(List.of(ruleId)));
        webhook.setCreatedAt(NOW);

        webhooks.saveAndFlush(webhook);

        WebhookEntity loaded = reload(() -> webhooks.findById(id).orElseThrow());
        assertThat(loaded.getName()).isEqualTo("ops-slack");
        assertThat(loaded.getUrl()).isEqualTo("https://hooks.example/abc");
        assertThat(loaded.getSecretCiphertext()).containsExactly(1, 2, 3);
        assertThat(loaded.getSecretNonce()).containsExactly(4, 5, 6);
        assertThat(loaded.getRuleIds()).containsExactly(ruleId);
    }

    @Test
    @DisplayName("a null rule_ids means 'every rule'")
    void webhookAllowsNullRuleIds() {
        UUID id = UUID.randomUUID();
        var webhook = new WebhookEntity(id, "all-rules", "https://hooks.example/all");
        webhook.setCreatedAt(NOW);
        webhooks.saveAndFlush(webhook);

        assertThat(webhooks.findById(id).orElseThrow().getRuleIds()).isNull();
        assertThat(webhooks.findByEnabledTrue()).hasSize(1);
    }

    @Test
    void webhookDeliveryRoundTrip() {
        UUID webhookId = UUID.randomUUID();
        var delivery = new WebhookDeliveryEntity(webhookId, 7L, NOW);

        WebhookDeliveryEntity saved = deliveries.saveAndFlush(delivery);

        assertThat(saved.getId()).isNotNull();
        WebhookDeliveryEntity loaded = deliveries.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getStatus()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(loaded.getWebhookId()).isEqualTo(webhookId);
        assertThat(loaded.getAlertId()).isEqualTo(7L);
        assertThat(loaded.getAttempts()).isZero();
        assertThat(loaded.getNextAttemptAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("delivery status is stored as its enum name")
    void deliveryStatusIsStoredAsString() {
        var delivery = new WebhookDeliveryEntity(UUID.randomUUID(), 1L, NOW);
        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredAt(NOW);
        Long id = deliveries.saveAndFlush(delivery).getId();

        assertThat(jdbc.queryForObject(
                        "select status from webhook_delivery where id = ?", String.class, id))
                .isEqualTo("DELIVERED");
    }

    @Test
    @DisplayName("the retry worker's due query returns pending deliveries oldest first")
    void findsDueDeliveries() {
        deliveries.saveAndFlush(new WebhookDeliveryEntity(UUID.randomUUID(), 1L, NOW.minusSeconds(60)));
        deliveries.saveAndFlush(new WebhookDeliveryEntity(UUID.randomUUID(), 2L, NOW.minusSeconds(30)));
        var future = new WebhookDeliveryEntity(UUID.randomUUID(), 3L, NOW.plusSeconds(600));
        deliveries.saveAndFlush(future);

        var due =
                deliveries.findByStatusAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(
                        DeliveryStatus.PENDING, NOW, org.springframework.data.domain.Limit.of(10));

        assertThat(due).extracting(WebhookDeliveryEntity::getAlertId).containsExactly(1L, 2L);
    }

    // ---------- app_setting and system_secret ----------

    @Test
    @DisplayName("a setting's raw JSON is stored verbatim, not re-encoded as a string")
    void appSettingStoresRawJson() {
        appSettings.saveAndFlush(
                new AppSettingEntity("retention", "{\"default\":\"7d\"}", NOW));

        assertThat(appSettings.findById("retention").orElseThrow().getValue())
                .isEqualTo("{\"default\":\"7d\"}");
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
        head.setObservedAt(NOW);
        head.setContentHash("b".repeat(64));
        recordHeads.saveAndFlush(head);
    }

    /**
     * Inserts a {@code record_version} the way the ingest writer will: straight through JDBC,
     * because the entity is {@code @Immutable} and Hibernate will never insert it.
     */
    private long insertRecordVersion(String sourceId, String externalId, int version, Point geom) {
        Long seq = jdbc.queryForObject("select nextval('record_seq')", Long.class);
        jdbc.update(
                """
                insert into record_version
                    (seq, source_id, external_id, version, kind, observed_at, ingested_at,
                     valid_until, geom, payload, content_hash, tombstone)
                values (?, ?, ?, ?, 'earthquake', ?, ?, null,
                        ST_SetSRID(ST_GeomFromText(cast(? as text)), 4326), ?::jsonb, ?, false)
                """,
                seq,
                sourceId,
                externalId,
                version,
                java.sql.Timestamp.from(NOW),
                java.sql.Timestamp.from(NOW),
                geom == null ? null : geom.toText(),
                "{\"mag\":6.1,\"place\":\"Example City\"}",
                "c".repeat(64));
        return seq;
    }

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
