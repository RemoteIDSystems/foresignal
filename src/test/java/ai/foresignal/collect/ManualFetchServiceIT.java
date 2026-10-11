package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import ai.foresignal.api.error.BudgetExceededProblem;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.adapters.FakeAdapters;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.collect.ControllablePollingAdapter;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

/** Every 404, 409 and 429 path of the manual fetch, and that it works while the source is disabled. */
@IntegrationTest
@Import(CollectorTestSources.class)
class ManualFetchServiceIT {

    private static final String POLL = CollectorTestSources.POLL;
    private static final String KEYED = CollectorTestSources.KEYED;
    private static final String DERIVED = "collect-derived";
    private static final Instant T0 = Instant.parse("2026-04-01T12:00:00Z");

    @Autowired private ManualFetchService manual;
    @Autowired private ControllablePollingAdapter collectPollAdapter;
    @Autowired private ControllablePollingAdapter collectKeyedAdapter;
    @Autowired private SourceRegistry registry;
    @Autowired private SourceService sources;
    @Autowired private BackoffRegistry backoff;
    @Autowired private MutableClock clock;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        clock.setInstant(T0);
        collectPollAdapter.reset();
        collectKeyedAdapter.reset();
        backoff.onSuccess(POLL);
    }

    @AfterEach
    void tearDown() {
        backoff.onSuccess(POLL);
        if (registry.contains(DERIVED)) {
            registry.unregisterDynamic(DERIVED);
        }
    }

    private static String code(Throwable thrown) {
        return ((ForesignalException) thrown).getCode();
    }

    @Test
    @DisplayName("an unknown source is 404")
    void unknown() {
        assertThatThrownBy(() -> manual.fetchNow("nope")).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("streaming, push and derived sources are 409 NOT_SUPPORTED_FOR_MODE")
    void notSupportedForMode() {
        registry.registerDynamic(SourceDescriptor.builder(DERIVED)
                .name("Derived test source")
                .kind("derived-thing")
                .mode(Mode.DERIVED)
                .defaultBudget(Budget.of(60, 1000))
                .build());

        for (String id : List.of(FakeAdapters.STREAMING_ID, FakeAdapters.PUSH_ID, DERIVED)) {
            assertThatThrownBy(() -> manual.fetchNow(id))
                    .isInstanceOf(ConflictException.class)
                    .satisfies(e -> assertThat(code(e)).isEqualTo(ConflictException.NOT_SUPPORTED_FOR_MODE))
                    .satisfies(e -> assertThat(((ForesignalException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
        }
        assertThat(jdbc.queryForObject("select count(*) from fetch_log", Integer.class)).isZero();
    }

    @Test
    @DisplayName("a source missing a required credential is 409 CREDENTIALS_REQUIRED")
    void credentialsRequired() {
        assertThatThrownBy(() -> manual.fetchNow(KEYED))
                .satisfies(e -> assertThat(code(e)).isEqualTo(ConflictException.CREDENTIALS_REQUIRED));
        assertThat(collectKeyedAdapter.fetchCount()).isZero();

        sources.setCredentials(KEYED, Map.of("apiKey", "value"));
        assertThat(manual.fetchNow(KEYED).success()).isTrue();
    }

    @Test
    @DisplayName("a source already being fetched is 409 ALREADY_RUNNING")
    void alreadyRunning() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        collectPollAdapter.then(ctx -> {
            release.await(10, TimeUnit.SECONDS);
            return FetchResult.complete(List.of());
        });
        CompletableFuture<FetchOutcome> first = CompletableFuture.supplyAsync(() -> manual.fetchNow(POLL));
        await().atMost(Duration.ofSeconds(5)).until(() -> collectPollAdapter.fetchCount() == 1);

        try {
            assertThatThrownBy(() -> manual.fetchNow(POLL))
                    .satisfies(e -> assertThat(code(e)).isEqualTo(ConflictException.ALREADY_RUNNING));
        } finally {
            release.countDown();
        }
        assertThat(first.get(10, TimeUnit.SECONDS).success()).isTrue();
    }

    @Test
    @DisplayName("a source in backoff is 409 IN_BACKOFF")
    void inBackoff() {
        backoff.onThrottled(POLL, Optional.of(Duration.ofMinutes(5)));

        assertThatThrownBy(() -> manual.fetchNow(POLL))
                .satisfies(e -> assertThat(code(e)).isEqualTo(ConflictException.IN_BACKOFF));
        assertThat(collectPollAdapter.fetchCount()).isZero();
    }

    @Test
    @DisplayName("an adapter that hits backoff mid-fetch is 409 IN_BACKOFF")
    void backoffMidFetch() {
        collectPollAdapter.then(ctx -> {
            throw new InBackoffException(POLL, T0.plusSeconds(30));
        });

        assertThatThrownBy(() -> manual.fetchNow(POLL))
                .satisfies(e -> assertThat(code(e)).isEqualTo(ConflictException.IN_BACKOFF));
    }

    @Test
    @DisplayName("a budget refusal is 429 BUDGET_EXCEEDED, after the REFUSED row is written")
    void budgetExceeded() {
        collectPollAdapter.then(ctx -> {
            throw new BudgetExceededException(POLL, "spent");
        });

        assertThatThrownBy(() -> manual.fetchNow(POLL))
                .isInstanceOf(BudgetExceededProblem.class)
                .satisfies(e -> assertThat(((ForesignalException) e).getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS));
        assertThat(jdbc.queryForObject("select status from fetch_log where source_id = ?", String.class, POLL))
                .isEqualTo("REFUSED");
    }

    @Test
    @DisplayName("works while the source is disabled, as a MANUAL fetch that ingests")
    void worksWhileDisabled() {
        assertThat(sources.view(POLL).enabled()).isFalse();
        collectPollAdapter.then(ctx -> FetchResult.complete(List.of(
                CollectorTestSources.record("m1", T0.minusSeconds(30)))));

        FetchOutcome outcome = manual.fetchNow(POLL);

        assertThat(outcome.success()).isTrue();
        assertThat(outcome.status()).isEqualTo(FetchStatus.OK);
        assertThat(outcome.persisted()).isEqualTo(1);
        assertThat(jdbc.queryForObject("select trigger from fetch_log where source_id = ?", String.class, POLL))
                .isEqualTo("MANUAL");
        assertThat(jdbc.queryForObject("select count(*) from record_head where source_id = ?", Integer.class, POLL))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("a failed fetch is returned, not thrown")
    void failureReturned() {
        collectPollAdapter.then(ctx -> {
            throw new IllegalStateException("upstream broke");
        });

        FetchOutcome outcome = manual.fetchNow(POLL);

        assertThat(outcome.success()).isFalse();
        assertThat(outcome.status()).isEqualTo(FetchStatus.FAILED);
        assertThat(outcome.error()).contains("upstream broke");
    }
}
