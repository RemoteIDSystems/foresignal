package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.collect.FetchExecutor;
import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.store.FetchTrigger;
import ai.foresignal.store.query.FetchLogQuery;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.collect.ControllablePollingAdapter;
import com.jayway.jsonpath.JsonPath;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * {@code listSourceFetches} and {@link FetchLogQuery} over rows written by the real
 * {@link FetchExecutor} with a scripted adapter: keyset paging, filters and the last-page cursor.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class SourceFetchesIT {

    private static final String POLL = CollectorTestSources.POLL;
    private static final Instant T0 = Instant.parse("2026-04-02T08:00:00Z");

    @Autowired private MockMvc mvc;
    @Autowired private AdminTokenService adminTokens;
    @Autowired private AppSettingService settings;
    @Autowired private FetchExecutor executor;
    @Autowired private FetchLogQuery query;
    @Autowired private ControllablePollingAdapter collectPollAdapter;
    @Autowired private BackoffRegistry backoff;
    @Autowired private MutableClock clock;

    private String token;

    /** Ids of the seeded rows, oldest first, with their status and trigger. */
    private final List<Seeded> seeded = new ArrayList<>();

    private record Seeded(long id, FetchStatus status, FetchTrigger trigger) {}

    @BeforeEach
    void seed() {
        settings.invalidateCache();
        token = adminTokens.rotate();
        clock.setInstant(T0);
        collectPollAdapter.reset();
        backoff.onSuccess(POLL);
        seeded.clear();

        // Seven rows: OK, NOT_MODIFIED and FAILED, alternating SCHEDULED and MANUAL triggers.
        for (int i = 0; i < 7; i++) {
            FetchTrigger trigger = i % 2 == 0 ? FetchTrigger.SCHEDULED : FetchTrigger.MANUAL;
            switch (i % 3) {
                case 0 -> collectPollAdapter.then(ctx -> FetchResult.complete(List.of(
                        CollectorTestSources.record("r" + System.nanoTime(), clock.instant().minusSeconds(5)))));
                case 1 -> collectPollAdapter.then(ctx -> FetchResult.unchanged());
                default -> collectPollAdapter.then(ctx -> {
                    throw new IllegalStateException("upstream broke");
                });
            }
            FetchOutcome outcome = executor.execute(POLL, trigger);
            assertThat(outcome.fetchId()).as("fetch %d wrote a fetch_log row", i).isNotNull();
            seeded.add(new Seeded(outcome.fetchId(), outcome.status(), trigger));
            backoff.onSuccess(POLL);
            clock.setInstant(clock.instant().plusSeconds(10));
        }
        assertThat(seeded).extracting(Seeded::status)
                .contains(FetchStatus.OK, FetchStatus.NOT_MODIFIED, FetchStatus.FAILED);
    }

    private MockHttpServletRequestBuilder admin(MockHttpServletRequestBuilder request) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    private List<Long> newestFirst() {
        return seeded.stream().map(Seeded::id).sorted((a, b) -> Long.compare(b, a)).toList();
    }

    @Test
    @DisplayName("pages newest first with no duplicates, and nextAfter is null on the last page")
    void paging() throws Exception {
        List<Long> all = new ArrayList<>();
        Long after = null;
        int pages = 0;
        do {
            var request = admin(get("/v1/sources/" + POLL + "/fetches")).param("limit", "3");
            if (after != null) {
                request.param("after", after.toString());
            }
            String body = mvc.perform(request).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            List<Number> ids = JsonPath.read(body, "$.items[*].id");
            ids.forEach(id -> all.add(id.longValue()));
            Number next = JsonPath.read(body, "$.nextAfter");
            after = next == null ? null : next.longValue();
            pages++;
            if (after != null) {
                assertThat(after).isEqualTo(all.get(all.size() - 1));
            }
        } while (after != null && pages < 10);

        assertThat(pages).isEqualTo(3);
        assertThat(all).isEqualTo(newestFirst());
        assertThat(new HashSet<>(all)).hasSize(all.size());
    }

    @Test
    @DisplayName("an exact final page still ends with nextAfter null")
    void exactLastPage() {
        FetchLogQuery.Page page = query.page(POLL, null, 7, null, null);
        assertThat(page.items()).hasSize(7);
        assertThat(page.nextAfter()).isNull();

        FetchLogQuery.Page first = query.page(POLL, null, 6, Set.of(), Set.of());
        assertThat(first.nextAfter()).isEqualTo(first.items().get(5).getId());
        FetchLogQuery.Page rest = query.page(POLL, first.nextAfter(), 6, null, null);
        assertThat(rest.items()).hasSize(1);
        assertThat(rest.nextAfter()).isNull();
    }

    @Test
    @DisplayName("status and trigger filters, repeatable or comma-separated")
    void filters() throws Exception {
        long failed = seeded.stream().filter(s -> s.status() == FetchStatus.FAILED).count();
        mvc.perform(admin(get("/v1/sources/" + POLL + "/fetches")).param("status", "FAILED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value((int) failed))
                .andExpect(jsonPath("$.items[*].status").value(everyItem(is("FAILED"))))
                .andExpect(jsonPath("$.items[0].error").isString());

        long okOrNotModified = seeded.stream()
                .filter(s -> s.status() == FetchStatus.OK || s.status() == FetchStatus.NOT_MODIFIED)
                .count();
        mvc.perform(admin(get("/v1/sources/" + POLL + "/fetches")).param("status", "OK,NOT_MODIFIED"))
                .andExpect(jsonPath("$.items.length()").value((int) okOrNotModified));
        mvc.perform(admin(get("/v1/sources/" + POLL + "/fetches")).param("status", "OK").param("status", "NOT_MODIFIED"))
                .andExpect(jsonPath("$.items.length()").value((int) okOrNotModified));

        long manual = seeded.stream().filter(s -> s.trigger() == FetchTrigger.MANUAL).count();
        mvc.perform(admin(get("/v1/sources/" + POLL + "/fetches")).param("trigger", "MANUAL"))
                .andExpect(jsonPath("$.items.length()").value((int) manual))
                .andExpect(jsonPath("$.items[*].trigger").value(everyItem(is("MANUAL"))));

        long scheduledOk = seeded.stream()
                .filter(s -> s.trigger() == FetchTrigger.SCHEDULED && s.status() == FetchStatus.OK)
                .count();
        mvc.perform(admin(get("/v1/sources/" + POLL + "/fetches"))
                        .param("trigger", "SCHEDULED")
                        .param("status", "OK"))
                .andExpect(jsonPath("$.items.length()").value((int) scheduledOk));

        mvc.perform(admin(get("/v1/sources/" + POLL + "/fetches")).param("trigger", "PUSH"))
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.nextAfter").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    @DisplayName("entries carry the FetchLogEntry fields and never a body")
    void entryShape() throws Exception {
        mvc.perform(admin(get("/v1/sources/" + POLL + "/fetches")).param("status", "OK").param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").isNumber())
                .andExpect(jsonPath("$.items[0].trigger").isString())
                .andExpect(jsonPath("$.items[0].status").value("OK"))
                .andExpect(jsonPath("$.items[0].startedAt").isString())
                .andExpect(jsonPath("$.items[0].licence").value(CollectorTestSources.LICENCE))
                .andExpect(jsonPath("$.items[0].recordsSeen").value(1))
                .andExpect(jsonPath("$.items[0].versionsWritten").value(1))
                .andExpect(jsonPath("$.items[0].requestCount").isNumber())
                .andExpect(jsonPath("$.items[0].fromSeq").isNumber())
                .andExpect(jsonPath("$.items[0]").value(not(org.hamcrest.Matchers.hasKey("body"))))
                .andExpect(jsonPath("$.items[0]").value(not(org.hamcrest.Matchers.hasKey("requestBody"))))
                .andExpect(jsonPath("$.items[0]").value(not(org.hamcrest.Matchers.hasKey("responseBody"))));
    }

    @Test
    @DisplayName("bad limits, cursors and filter values are 400 naming the parameter")
    void validation() throws Exception {
        mvc.perform(admin(get("/v1/sources/" + POLL + "/fetches")).param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("limit"));
        mvc.perform(admin(get("/v1/sources/" + POLL + "/fetches")).param("limit", "1001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("limit"));
        mvc.perform(admin(get("/v1/sources/" + POLL + "/fetches")).param("after", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("after"));
        mvc.perform(admin(get("/v1/sources/" + POLL + "/fetches")).param("status", "BROKEN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("status"));
        mvc.perform(admin(get("/v1/sources/" + POLL + "/fetches")).param("trigger", "CRON"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("trigger"));
    }

    @Test
    @DisplayName("requires the admin token")
    void adminOnly() throws Exception {
        mvc.perform(get("/v1/sources/" + POLL + "/fetches")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("another source's rows are not listed")
    void scopedToSource() {
        assertThat(query.page(CollectorTestSources.KEYED, null, 100, null, null).items()).isEmpty();
    }
}
