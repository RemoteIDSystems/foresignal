package ai.foresignal.testsupport.export;

import static org.awaitility.Awaitility.await;

import ai.foresignal.export.BulkExportJobRepository;
import ai.foresignal.export.ExportJobEntity;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.retention.RetentionItSupport;
import ai.foresignal.testsupport.security.TestTokens;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Shared fixture of the prompt 111 integration tests. It runs in the shared 019 retention context
 * ({@code RetentionTestSources}, so no new Spring context is built) and registers its own sources at
 * runtime, each with structured license terms the export rules can judge:
 *
 * <ul>
 *   <li>{@value #STD}: STANDARD, CC BY 4.0, kind {@value #STD_KIND};
 *   <li>{@value #TRK}: TRACK, CC BY 4.0, TTL 5 min, kind {@value #TRK_KIND}, an {@code alt_geom}
 *       altitude field in meters (WGS84);
 *   <li>{@value #FORBIDDEN}: STANDARD, the OpenSky terms (redistribution FORBIDDEN);
 *   <li>{@value #SECRET}: STANDARD, CC BY 4.0, private.
 * </ul>
 *
 * <p>It also creates an agent, a second agent and an admin token for each test.
 */
public abstract class ExportItSupport extends RetentionItSupport {

    /** The open STANDARD source. */
    public static final String STD = "test-exp-std";

    /** Its kind. */
    public static final String STD_KIND = "exp-record";

    /** The open TRACK source. */
    public static final String TRK = "test-exp-trk";

    /** Its kind. */
    public static final String TRK_KIND = "exp-aircraft";

    /** The source whose terms forbid redistribution. */
    public static final String FORBIDDEN = "test-exp-forbidden";

    /** The private source. */
    public static final String SECRET = "test-exp-secret";

    @Autowired protected SourceRegistry registry;
    @Autowired protected MockMvc mvc;
    @Autowired protected TokenService tokens;
    @Autowired protected BulkExportJobRepository jobs;

    /** {@code Bearer …} of an agent token. */
    protected String agent;

    /** {@code Bearer …} of another agent token. */
    protected String otherAgent;

    /** {@code Bearer …} of an admin token. */
    protected String admin;

    /** Registers the sources and creates the tokens. */
    @BeforeEach
    void registerExportSources() {
        unregisterExportSources();
        registry.registerDynamic(base(STD, STD_KIND, "CC BY 4.0").build());
        registry.registerDynamic(base(TRK, TRK_KIND, "CC BY 4.0")
                .historyTier(HistoryTier.TRACK)
                .recordTtl(Duration.ofMinutes(5))
                .fieldSpec("alt_geom", FieldSpec.create(FieldType.NUMBER, "m", "Altitude", "Geometric altitude",
                        "WGS84 ellipsoid", null, null))
                .defaultIngestPolicy(IngestPolicy.all())
                .build());
        registry.registerDynamic(base(FORBIDDEN, STD_KIND, "OpenSky Network terms (non-commercial research)").build());
        registry.registerDynamic(base(SECRET, STD_KIND, "CC BY 4.0").privateSource(true).build());
        agent = TestTokens.bearer(TestTokens.create(tokens, "exp-agent", Scope.AGENT));
        otherAgent = TestTokens.bearer(TestTokens.create(tokens, "exp-agent-2", Scope.AGENT));
        admin = TestTokens.bearer(TestTokens.create(tokens, "exp-admin", Scope.ADMIN));
    }

    /** Unregisters the sources. */
    @AfterEach
    void unregisterExportSources() {
        for (String id : List.of(STD, TRK, FORBIDDEN, SECRET)) {
            if (registry.contains(id)) {
                registry.unregisterDynamic(id);
            }
        }
    }

    private static SourceDescriptor.Builder base(String id, String kind, String license) {
        return SourceDescriptor.builder(id)
                .name("Export test source " + id)
                .kind(kind)
                .mode(Mode.PUSH)
                .historyTier(HistoryTier.STANDARD)
                .defaultBudget(Budget.of(60, 100_000))
                .licence(license)
                .attribution("Data: " + id);
    }

    /** A point record of {@code kind} with an {@code alt_geom} payload field. */
    protected static NormalizedRecord point(String externalId, String kind, Instant at, double lon, double lat,
            int value) {
        return NormalizedRecord.builder(externalId, kind).observedAt(at).point(lon, lat).put("v", value)
                .put("alt_geom", 100 + value).build();
    }

    /** Waits until job {@code id} reaches a terminal status and returns it. */
    protected ExportJobEntity awaitTerminal(String id) {
        await().atMost(Duration.ofSeconds(60)).until(() -> jobs.findById(id).map(ExportJobEntity::terminal).orElse(false));
        return jobs.findById(id).orElseThrow();
    }
}
