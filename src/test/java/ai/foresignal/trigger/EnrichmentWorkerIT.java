package ai.foresignal.trigger;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.api.mapper.AlertMapper;
import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.llm.client.Effort;
import ai.foresignal.llm.client.LlmProviderException;
import ai.foresignal.llm.client.Purpose;
import ai.foresignal.search.DocInput;
import ai.foresignal.search.EmbeddingWorker;
import ai.foresignal.search.SearchStatusService;
import ai.foresignal.store.DeferredReason;
import ai.foresignal.store.DeliveryKind;
import ai.foresignal.store.DeliveryStatus;
import ai.foresignal.store.NotificationDeliveryEntity;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.enrichment.EnrichmentItSupport;
import ai.foresignal.testsupport.enrichment.EnrichmentTestSources;
import ai.foresignal.testsupport.llm.FakeLlmClient;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * The enrichment worker and service (prompt 030) with the deterministic {@link FakeLlmClient} and
 * WireMock channel stand-ins: the summary stored with verified citations, {@code alert.enriched}
 * published, waiting Slack deliveries brought forward and rendered with the summary, an
 * already-delivered Discord message edited in place, every skip condition, the failure paths
 * (provider error, refusal, budget, timeout) leaving deliveries on schedule, the queue overflow,
 * embedding, and the acceptance path through the running worker.
 */
@IntegrationTest
@Import(EnrichmentTestSources.class)
class EnrichmentWorkerIT extends EnrichmentItSupport {

    @RegisterExtension
    static final WireMockExtension HOOKS = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().gzipDisabled(true))
            .failOnUnmatchedRequests(true)
            .resetOnEachTest(true)
            .build();

    @Autowired TriggerBacktester backtester;

    /** A distinctive word of every scripted summary. */
    static final String WORD = "ZETAVESSEL";

    @BeforeEach
    void stubs() {
        HOOKS.stubFor(post(urlEqualTo("/slack/wait")).willReturn(aResponse().withStatus(200).withBody("ok")));
        HOOKS.stubFor(post(urlEqualTo("/slack/now")).willReturn(aResponse().withStatus(200).withBody("ok")));
        HOOKS.stubFor(post(urlEqualTo("/discord/1/T?wait=true")).willReturn(okJson("{\"id\":\"555\"}")));
        HOOKS.stubFor(patch(urlEqualTo("/discord/1/T/messages/555")).willReturn(okJson("{\"id\":\"555\"}")));
    }

    /** An enrich rule, a vessel near the anchor and the alerted quake; returns the alert. */
    private AlertView fire(String name) {
        UUID rule = enrichRule(name, 50, "1h");
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("name", "Harbor vessel");
        recordAt(STANDARD, STANDARD_KIND, "v-near", minutesAgo(5), LON + 0.05, LAT + 0.02, fields);
        quakeAt("q-main", "Tokyo Bay", 5.5, T, LON, LAT);
        return alertFor(rule, "q-main");
    }

    private String vesselCite() {
        return "[" + STANDARD + ":v-near@" + headSeq(STANDARD, "v-near") + "]";
    }

    /** Scripts a summary citing the vessel (valid), a bad seq of it, and the alert. */
    private void scriptSummary(AlertView alert) {
        String good = vesselCite();
        chat().then(request -> FakeLlmClient.text(WORD + " harbor vessel 5 km away " + good + " and "
                + "[" + STANDARD + ":v-near@999999] near " + "[alert:" + alert.id() + "]."));
    }

    private AlertView reload(AlertView alert) {
        return alertStore.find(alert.id()).orElseThrow();
    }

    private NotificationDeliveryEntity awaitOnePending(UUID channel) {
        await().atMost(Duration.ofSeconds(10)).until(() -> notify.pending(channel).size() == 1);
        return notify.pending(channel).getFirst();
    }

    @Test
    @DisplayName("the summary is stored with verified citations (a bad seq dropped) and alert.enriched is published")
    void summaryStored() {
        AlertView alert = fire("Stored");
        scriptSummary(alert);
        double before = count(EnrichmentService.SUCCEEDED_METRIC, null);

        EnrichmentService.Outcome outcome = enrichment.enrich(alert);

        assertThat(outcome.status()).isEqualTo(EnrichmentService.Outcome.SUCCEEDED);
        AlertView stored = reload(alert);
        assertThat(stored.summary()).contains(WORD).contains(vesselCite()).contains("[alert:" + alert.id() + "]")
                .doesNotContain("@999999");
        assertThat(stored.enrichedAt()).isEqualTo(T);
        assertThat(stored.rev()).isGreaterThan(alert.rev());
        assertThat(JSON.readTree(stored.summaryCitations()).toString())
                .contains("\"seq\":" + headSeq(STANDARD, "v-near")).contains("alert:" + alert.id())
                .doesNotContain("999999");
        assertThat(new AlertMapper().alert(stored, null)).containsKeys("summary", "enrichedAt", "summaryCitations");
        assertThat(count(EnrichmentService.SUCCEEDED_METRIC, null)).isEqualTo(before + 1);

        ChatRequest request = chat().requests().getLast();
        assertThat(request.purpose()).isEqualTo(Purpose.ENRICHMENT);
        assertThat(request.effort()).isEqualTo(Effort.LOW);
        assertThat(request.maxTokens()).isEqualTo(600);
        assertThat(request.tools()).isEmpty();
        assertThat(request.system()).isEqualTo(EnrichmentPrompts.SYSTEM);

        AlertEvent event = fixture.awaitEvent(AlertEvent.ENRICHED, alert.id());
        assertThat(event.sseEvent()).isEqualTo("alert.enriched");
        assertThat(event.alert().summary()).isEqualTo(stored.summary());
    }

    @Test
    @DisplayName("deliveries waiting for the summary are brought forward and the Slack message includes it")
    void waitingDeliveriesPulledForward() {
        UUID slack = slackChannel("wait", HOOKS.url("/slack/wait"), 30);
        AlertView alert = fire("Wait");
        NotificationDeliveryEntity waiting = awaitOnePending(slack);
        assertThat(waiting.getNextAttemptAt()).isEqualTo(t(30));
        assertThat(waiting.getDeferredReason()).isEqualTo(DeferredReason.ENRICHMENT);
        scriptSummary(alert);

        enrichment.enrich(alert);

        await().atMost(Duration.ofSeconds(10)).until(() -> notify.pending(slack).getFirst().getNextAttemptAt().equals(T));
        deliveryWorker.runOnce();
        HOOKS.verify(1, postRequestedFor(urlEqualTo("/slack/wait")).withRequestBody(containing(WORD)));
        assertThat(jdbc.queryForObject("SELECT data->>'summary' FROM notification WHERE alert_id = ?"
                + " AND subtype = 'alert.fired'", String.class, alert.id())).contains(WORD);
    }

    @Test
    @DisplayName("an already-delivered Discord message is edited to add the summary; a Slack channel gets nothing extra")
    void deliveredDiscordEdited() {
        UUID discord = discordChannel("d", HOOKS.url("/discord/1/T"));
        UUID slack = slackChannel("now", HOOKS.url("/slack/now"), 0);
        AlertView alert = fire("Edit");
        awaitOnePending(discord);
        awaitOnePending(slack);
        deliveryWorker.runOnce();
        assertThat(notify.deliveries(discord).getFirst().getProviderMessageRef()).isEqualTo("555");
        clock.setInstant(t(5));
        scriptSummary(alert);

        enrichment.enrich(alert);

        await().atMost(Duration.ofSeconds(10)).until(() -> notify.deliveries(discord).size() == 2);
        deliveryWorker.runOnce();
        HOOKS.verify(1, patchRequestedFor(urlEqualTo("/discord/1/T/messages/555"))
                .withRequestBody(matchingJsonPath("$.embeds[0].description", containing(WORD))));
        NotificationDeliveryEntity edit = notify.deliveries(discord).getLast();
        assertThat(edit.getKind()).isEqualTo(DeliveryKind.UPDATE);
        assertThat(edit.isEdited()).isTrue();
        assertThat(edit.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(notify.deliveries(slack)).hasSize(1);
        HOOKS.verify(1, postRequestedFor(urlEqualTo("/slack/now")));
    }

    @Test
    @DisplayName("skips: disabled, no provider, stale and resolved, without calling the model")
    void skips() {
        AlertView alert = fire("Skips");
        double disabled = count(EnrichmentService.SKIPPED_METRIC, "disabled");

        enrichmentSettings(CHAT, false);
        assertThat(enrichment.enrich(alert).reason()).isEqualTo("disabled");
        assertThat(count(EnrichmentService.SKIPPED_METRIC, "disabled")).isEqualTo(disabled + 1);
        enrichmentSettings(null, true);
        assertThat(enrichment.enrich(alert).reason()).isEqualTo("no_provider");
        enrichmentSettings(CHAT, true);

        clock.setInstant(t(11 * 60));
        assertThat(enrichment.enrich(alert).reason()).isEqualTo("stale");
        clock.setInstant(T);

        alertService.resolve(alert.id(), AlertService.Transition.NONE, "admin");
        assertThat(enrichment.enrich(alert).reason()).isEqualTo("resolved");

        assertThat(chat().requests()).isEmpty();
        assertThat(reload(alert).summary()).isNull();
    }

    @Test
    @DisplayName("a rule without enrich=true is skipped; testTrigger reports enrich without calling the model")
    void ruleWithoutEnrich() {
        UUID plain = plainRule("Plain");
        quakeAt("q-plain", "Tokyo Bay", 5.5, T, LON, LAT);
        AlertView alert = alertFor(plain, "q-plain");

        EnrichmentService.Outcome outcome = enrichment.enrich(alert);

        assertThat(outcome.status()).isEqualTo(EnrichmentService.Outcome.SKIPPED);
        assertThat(outcome.reason()).isEqualTo("rule");
        UUID enriched = enrichRule("Backtest", 100, "24h");
        assertThat(backtester.test(enriched, new TriggerBacktester.Request(null, null, null, null, 10, false)).enrich())
                .isTrue();
        assertThat(backtester.test(plain, new TriggerBacktester.Request(null, null, null, null, 10, false)).enrich())
                .isFalse();
        assertThat(chat().requests()).isEmpty();
    }

    @Test
    @DisplayName("provider error, refusal and budget refusal fail without a summary and leave deliveries on schedule")
    void failuresLeaveDeliveriesOnSchedule() {
        UUID slack = slackChannel("wait", HOOKS.url("/slack/wait"), 30);
        AlertView alert = fire("Failures");
        awaitOnePending(slack);
        double provider = count(EnrichmentService.FAILED_METRIC, "provider");
        double refusal = count(EnrichmentService.FAILED_METRIC, "refusal");
        double budget = count(EnrichmentService.FAILED_METRIC, "budget");

        chat().then(request -> {
            throw new LlmProviderException(LlmProviderException.Kind.OVERLOADED, CHAT, "overloaded");
        });
        assertThat(enrichment.enrich(alert).reason()).isEqualTo("provider");
        chat().then(FakeLlmClient.refusal("cyber"));
        assertThat(enrichment.enrich(alert).reason()).isEqualTo("refusal");
        provider("tiny", "openai-compatible", 1L);
        assign("tiny");
        assertThat(enrichment.enrich(alert).reason()).isEqualTo("budget");

        assertThat(count(EnrichmentService.FAILED_METRIC, "provider")).isEqualTo(provider + 1);
        assertThat(count(EnrichmentService.FAILED_METRIC, "refusal")).isEqualTo(refusal + 1);
        assertThat(count(EnrichmentService.FAILED_METRIC, "budget")).isEqualTo(budget + 1);
        assertThat(reload(alert).summary()).isNull();
        assertThat(notify.pending(slack)).singleElement()
                .satisfies(d -> assertThat(d.getNextAttemptAt()).isEqualTo(t(30)));
        assertThat(fakes.client("tiny").requests()).as("a refused call is never sent").isEmpty();
    }

    @Test
    @DisplayName("an LLM call slower than the timeout fails as timeout")
    void timeout() {
        AlertView alert = fire("Timeout");
        chat().then(request -> {
            try {
                Thread.sleep(enrichmentProperties.timeout().toMillis() + 1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return FakeLlmClient.text("late " + WORD);
        });

        EnrichmentService.Outcome outcome = enrichment.enrich(alert);

        assertThat(outcome.status()).isEqualTo(EnrichmentService.Outcome.FAILED);
        assertThat(outcome.reason()).isEqualTo("timeout");
        assertThat(reload(alert).summary()).isNull();
    }

    @Test
    @DisplayName("queue overflow drops and counts; only unsuppressed fired events are admitted")
    void queueOverflow() {
        EnrichmentService service = mock(EnrichmentService.class);
        when(service.admit(any())).thenReturn(true);
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        EnrichmentProperties tiny = new EnrichmentProperties(false, 1, 1, Duration.ofSeconds(20), Duration.ofMinutes(10),
                24 * 1024, 600, Duration.ofHours(1));
        EnrichmentWorker small = new EnrichmentWorker(alertBus, service, tiny, registry);
        AlertView a = ai.foresignal.testsupport.notify.NotifyFixture.alert(1, UUID.randomUUID(), "r", "warning", T);

        assertThat(small.submit(a)).isTrue();
        assertThat(small.submit(a)).isFalse();
        small.onAlert(AlertEvent.of(AlertEvent.FIRED, a));

        assertThat(registry.find(EnrichmentWorker.DROPPED_METRIC).counter().count()).isEqualTo(2);
        assertThat(small.backlog()).isEqualTo(1);
        small.onAlert(AlertEvent.of(AlertEvent.RESOLVED, a));
        small.onAlert(new AlertEvent(AlertEvent.FIRED, a, true));
        verify(service, org.mockito.Mockito.times(3)).admit(any());
    }

    @Test
    @DisplayName("acceptance: with the worker running, the Slack message includes the cited summary within the wait")
    void acceptanceThroughTheWorker() {
        worker.start();
        UUID slack = slackChannel("wait", HOOKS.url("/slack/wait"), 30);
        UUID rule = enrichRule("Acceptance", 50, "1h");
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("name", "Harbor vessel");
        recordAt(STANDARD, STANDARD_KIND, "v-near", minutesAgo(5), LON + 0.05, LAT + 0.02, fields);
        String good = vesselCite();
        chat().otherwise(request -> FakeLlmClient.text(WORD + " harbor vessel nearby " + good + "."));

        quakeAt("q-main", "Tokyo Bay", 5.5, T, LON, LAT);
        AlertView alert = alertFor(rule, "q-main");

        await().atMost(Duration.ofSeconds(15)).until(() -> reload(alert).summary() != null);
        await().atMost(Duration.ofSeconds(10)).until(() -> notify.pending(slack).size() == 1
                && notify.pending(slack).getFirst().getNextAttemptAt().equals(T));
        deliveryWorker.runOnce();
        HOOKS.verify(1, postRequestedFor(urlEqualTo("/slack/wait")).withRequestBody(containing(WORD)));
        assertThat(reload(alert).summaryCitations()).contains("v-near");
    }

    @Test
    @DisplayName("without a provider, alerts deliver unchanged after the wait")
    void withoutProviderDeliversUnchanged() {
        enrichmentSettings(null, true);
        worker.start();
        double before = count(EnrichmentService.SKIPPED_METRIC, "no_provider");
        UUID slack = slackChannel("wait", HOOKS.url("/slack/wait"), 30);
        AlertView alert = fire("No provider");
        awaitOnePending(slack);
        await().atMost(Duration.ofSeconds(10))
                .until(() -> count(EnrichmentService.SKIPPED_METRIC, "no_provider") > before);

        clock.setInstant(t(31));
        deliveryWorker.runOnce();

        HOOKS.verify(1, postRequestedFor(urlEqualTo("/slack/wait")).withRequestBody(containing("No provider")));
        assertThat(reload(alert).summary()).isNull();
        assertThat(chat().requests()).isEmpty();
    }

    @Test
    @DisplayName("the enriched alert is embedded as an alert document when an index is available, not otherwise")
    @SuppressWarnings("unchecked")
    void embedded() {
        EmbeddingWorker embedder = mock(EmbeddingWorker.class);
        ObjectProvider<EmbeddingWorker> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(embedder);
        SearchStatusService status = mock(SearchStatusService.class);
        when(status.availability()).thenReturn(new SearchStatusService.Availability(true, null, null));
        EnrichmentService service = new EnrichmentService(null, null, null, null, null, null, null, provider, status,
                enrichmentProperties, new SimpleMeterRegistry(), JSON, Clock.systemUTC());
        AlertView base = ai.foresignal.testsupport.notify.NotifyFixture.alert(7, UUID.randomUUID(), "r", "warning", T);
        AlertView enriched = new AlertView(base.id(), base.ruleId(), base.ruleName(), base.seq(), base.sourceId(),
                base.externalId(), base.kind(), base.firedAt(), base.observedAt(), base.message(), base.severity(),
                base.state(), base.rev(), null, null, null, null, null, null, null, null, null, false, null, base.tags(),
                base.recordSnapshot(), "Quiet nearby.", T, "[]");
        EnrichmentAnchor anchor = new EnrichmentAnchor(enriched, "{\"type\":\"Point\",\"coordinates\":[139.8,35.6]}",
                T, 100, Duration.ofHours(24), remote(), null);

        service.embed(enriched, anchor);
        verify(embedder).embedDocument(eq(DocInput.ALERT), eq("7"), eq(base.message() + "\n\nQuiet nearby."),
                any(Geometry.class), eq(T), eq(base.sourceId()), eq(base.kind()));

        when(status.availability()).thenReturn(new SearchStatusService.Availability(false, "no index", null));
        EmbeddingWorker other = mock(EmbeddingWorker.class);
        when(provider.getIfAvailable()).thenReturn(other);
        service.embed(enriched, anchor);
        verify(other, never()).embedDocument(any(), any(), any(), any(), any(), any(), any());
        service.close();
    }

    @Test
    @DisplayName("citations the context cannot back are removed from the text")
    void stripUnverified() {
        ai.foresignal.ask.CitationSet set = new ai.foresignal.ask.CitationSet();
        set.addRecord("s:a", 5, "A");
        set.addAlert("9", "Alert");
        String out = EnrichmentService.stripUnverified("A [s:a@5] B [s:a@6] C [s:a] D [alert:9] E [alert:8].", set);
        assertThat(out).isEqualTo("A [s:a@5] B C [s:a] D [alert:9] E.");
        assertThat(List.of(out)).isNotEmpty();
    }
}
