package ai.foresignal.testsupport.weather;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.collect.FetchExecutor;
import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.adapters.awc.AwcMetarsAdapter;
import ai.foresignal.source.adapters.goes.GoesAbiAdapter;
import ai.foresignal.source.adapters.goes.GoesFdcAdapter;
import ai.foresignal.source.adapters.goes.GoesGlmAdapter;
import ai.foresignal.source.adapters.hms.HmsFireAdapter;
import ai.foresignal.source.adapters.hms.HmsSmokeAdapter;
import ai.foresignal.source.adapters.mrms.ProbSevereAdapter;
import ai.foresignal.source.adapters.nws.NwsAlertsAdapter;
import ai.foresignal.source.adapters.spc.SpcOutlooksAdapter;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.store.FetchTrigger;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * The shared context of the prompt 047 and 048 ingest ITs: the weather adapters (047's four, 048's
 * {@code goes-glm}, {@code goes-fdc}, {@code hms-smoke} and {@code hms-fire}, and 053's {@code nws-gridpoints} and
 * {@code spc-storm-reports}, which share the NWS and SPC base-URL properties, and 064's {@code celestrak-gp} and
 * {@code celestrak-socrates}, 065's {@code nasa-firms}, 066's {@code gdelt-events} and 067's {@code nhc-cyclones}, {@code gdacs} and
 * {@code tsunami-warnings}) point at one stub server
 * through their test base-URL properties (and S3 listings through {@code foresignal.test.s3-endpoint}),
 * and each test drives real fetches with {@link FetchExecutor#execute(String, FetchTrigger)
 * execute(id, MANUAL)}, so records reach the store through the real fetch path and ingest writer
 * (Core-Data-Model §17).
 *
 * <p>The stub server is static and started once, so every subclass shares one cached Spring context:
 * subclasses must not add configuration of their own. Stubs are reset before each test.
 */
@IntegrationTest
@Import(MutableClock.TestConfig.class)
public abstract class WeatherIngestSupport {

    /** The stub upstream for all four sources. */
    protected static final WireMockServer UPSTREAM = start();

    private static WireMockServer start() {
        WireMockServer server = new WireMockServer(wireMockConfig().dynamicPort().gzipDisabled(true));
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        return server;
    }

    @DynamicPropertySource
    static void upstream(DynamicPropertyRegistry registry) {
        registry.add(NwsAlertsAdapter.BASE_URL_PROPERTY, UPSTREAM::baseUrl);
        registry.add(ProbSevereAdapter.BASE_URL_PROPERTY, UPSTREAM::baseUrl);
        registry.add(SpcOutlooksAdapter.BASE_URL_PROPERTY, UPSTREAM::baseUrl);
        registry.add(AwcMetarsAdapter.BASE_URL_PROPERTY, UPSTREAM::baseUrl);
        registry.add(GoesAbiAdapter.BASE_URL_PROPERTY, UPSTREAM::baseUrl);
        registry.add(HmsSmokeAdapter.BASE_URL_PROPERTY, UPSTREAM::baseUrl);
        registry.add(UpstreamClient.S3_ENDPOINT_PROPERTY, UPSTREAM::baseUrl);
        // Prompt 064: the CelesTrak sources join this context rather than adding one.
        registry.add(ai.foresignal.source.adapters.celestrak.CelestrakGpAdapter.BASE_URL_PROPERTY, UPSTREAM::baseUrl);
        // Prompt 065: nasa-firms joins this context too.
        registry.add(ai.foresignal.source.adapters.firms.FirmsAdapter.BASE_URL_PROPERTY, UPSTREAM::baseUrl);
        // Prompt 066: gdelt-events joins this context too.
        registry.add(ai.foresignal.source.adapters.gdelt.GdeltEventsAdapter.BASE_URL_PROPERTY, UPSTREAM::baseUrl);
        // Prompt 067: the hazard sources join this context too.
        registry.add(ai.foresignal.source.adapters.nhc.NhcCyclonesAdapter.BASE_URL_PROPERTY, UPSTREAM::baseUrl);
        registry.add(ai.foresignal.source.adapters.gdacs.GdacsAdapter.BASE_URL_PROPERTY, UPSTREAM::baseUrl);
        registry.add(ai.foresignal.source.adapters.tsunami.TsunamiWarningsAdapter.BASE_URL_PROPERTY, UPSTREAM::baseUrl);
    }

    @Autowired protected FetchExecutor executor;
    @Autowired protected SourceService sources;
    @Autowired protected BackoffRegistry backoff;
    @Autowired protected MutableClock clock;
    @Autowired protected JdbcTemplate jdbc;

    /** The sources whose backoff is cleared around each test. */
    protected static final List<String> SOURCES = List.of(NwsAlertsAdapter.ID, ProbSevereAdapter.ID,
            SpcOutlooksAdapter.ID, AwcMetarsAdapter.ID, GoesGlmAdapter.ID, GoesFdcAdapter.ID, HmsSmokeAdapter.ID,
            HmsFireAdapter.ID, ai.foresignal.source.adapters.nws.NwsGridpointsAdapter.ID,
            ai.foresignal.source.adapters.spc.SpcStormReportsAdapter.ID,
            ai.foresignal.source.adapters.celestrak.CelestrakGpAdapter.ID,
            ai.foresignal.source.adapters.celestrak.CelestrakSocratesAdapter.ID,
            ai.foresignal.source.adapters.firms.FirmsAdapter.ID,
            ai.foresignal.source.adapters.gdelt.GdeltEventsAdapter.ID,
            ai.foresignal.source.adapters.nhc.NhcCyclonesAdapter.ID,
            ai.foresignal.source.adapters.gdacs.GdacsAdapter.ID,
            ai.foresignal.source.adapters.tsunami.TsunamiWarningsAdapter.ID);

    /** Resets the stubs and the backoff of the weather sources. */
    @BeforeEach
    void resetUpstream() {
        UPSTREAM.resetAll();
        clearBackoff();
    }

    /** Clears any backoff a test caused. */
    @AfterEach
    void clearBackoff() {
        for (String id : SOURCES) {
            backoff.onSuccess(id);
        }
    }

    /** Sets {@code sourceId}'s parameters and a generous budget, enabled or not (no scheduler runs in tests). */
    protected void configure(String sourceId, boolean enabled, Map<String, Object> params) {
        sources.update(sourceId, new SourceSettingsUpdate(enabled, null, params, Budget.of(600, 100_000, 10_000), null,
                false, null, false));
    }

    /** One manual fetch through the real executor and ingest writer. */
    protected FetchOutcome fetch(String sourceId) {
        return executor.execute(sourceId, FetchTrigger.MANUAL);
    }

    /** Versions of {@code sourceId}, optionally of one persist reason. */
    protected int versions(String sourceId, PersistReason reason) {
        Integer count = reason == null
                ? jdbc.queryForObject("select count(*) from record_version where source_id = ?", Integer.class, sourceId)
                : jdbc.queryForObject("select count(*) from record_version where source_id = ? and persist_reason = ?",
                        Integer.class, sourceId, reason.code());
        return count == null ? 0 : count;
    }

    /** Versions of one entity. */
    protected int versions(String sourceId, String externalId) {
        Integer count = jdbc.queryForObject(
                "select count(*) from record_version where source_id = ? and external_id = ?", Integer.class, sourceId,
                externalId);
        return count == null ? 0 : count;
    }

    /** True when the entity's head is a tombstone; null when there is no head. */
    protected Boolean tombstoned(String sourceId, String externalId) {
        List<Boolean> rows = jdbc.queryForList(
                "select tombstone from record_head where source_id = ? and external_id = ?", Boolean.class, sourceId,
                externalId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** A timestamp column of {@code record_live}, or null. */
    protected Instant live(String column, String sourceId, String externalId) {
        List<Timestamp> rows = jdbc.queryForList(
                "select " + column + " from record_live where source_id = ? and external_id = ?", Timestamp.class, sourceId,
                externalId);
        return rows.isEmpty() || rows.get(0) == null ? null : rows.get(0).toInstant();
    }

    /** A timestamp column of {@code record_head}, or null. */
    protected Instant head(String column, String sourceId, String externalId) {
        List<Timestamp> rows = jdbc.queryForList(
                "select " + column + " from record_head where source_id = ? and external_id = ?", Timestamp.class, sourceId,
                externalId);
        return rows.isEmpty() || rows.get(0) == null ? null : rows.get(0).toInstant();
    }
}
