package ai.foresignal.briefing;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.notify.DeliveryWorker;
import ai.foresignal.search.DocInput;
import ai.foresignal.search.EmbeddingWorker;
import ai.foresignal.search.SearchStatusService;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.store.NotificationDeliveryEntity;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.briefing.BriefingItSupport;
import ai.foresignal.testsupport.briefing.BriefingTestSources;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Briefing delivery (prompt 029): a scheduled digest briefing, with no LLM configured, reaches a
 * WireMock Slack stand-in with a link to the run's HTML rendition; only the listed channels get it, or
 * every channel subscribed to {@code briefing} when none is listed; the run is embedded when semantic
 * search is available.
 */
@IntegrationTest
@Import(BriefingTestSources.class)
class BriefingDeliveryIT extends BriefingItSupport {

    @RegisterExtension
    static final WireMockExtension SLACK = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().gzipDisabled(true))
            .failOnUnmatchedRequests(true)
            .resetOnEachTest(true)
            .build();

    @Autowired BriefingScheduler scheduler;
    @Autowired DeliveryWorker worker;

    @BeforeEach
    void setUpDelivery() {
        settings.put(SettingKeys.SERVER, new ServerSettings("https://fs.example"));
        SLACK.stubFor(post(urlEqualTo("/slack/one")).willReturn(aResponse().withStatus(200).withBody("ok")));
        SLACK.stubFor(post(urlEqualTo("/slack/two")).willReturn(aResponse().withStatus(200).withBody("ok")));
    }

    @Test
    @DisplayName("a scheduled digest briefing is delivered to its listed Slack channel with the HTML link; no LLM")
    void scheduledDigestDelivered() throws Exception {
        UUID one = slackChannel("one", SLACK.url("/slack/one"));
        slackChannel("two", SLACK.url("/slack/two"));
        quake("q1", "Tokyo Bay", 5.5);
        UUID id = id(briefings.create(json(digestBody("Morning", "0 13 * * *",
                "\"channelIds\":[\"" + one + "\"]")), agentCaller()));
        assertThat(briefingRepository.findById(id).orElseThrow().getNextRunAt())
                .isEqualTo(Instant.parse("2026-10-12T13:00:00Z"));

        clock.setInstant(Instant.parse("2026-10-12T13:00:05Z"));
        List<Future<BriefingRunEntity>> started = scheduler.tick();
        assertThat(started).hasSize(1);
        BriefingRunEntity run = started.getFirst().get();

        assertThat(run.getStatus()).isEqualTo("succeeded");
        assertThat(run.getDeliveries()).isEqualTo(1);
        List<NotificationDeliveryEntity> rows = notify.deliveries(one);
        assertThat(rows).singleElement().satisfies(d -> assertThat(d.getBriefingRunId()).isEqualTo(run.getId()));
        assertThat(jdbc.queryForObject("SELECT event FROM notification WHERE briefing_run_id = ?", String.class,
                run.getId())).isEqualTo("briefing");

        worker.runOnce();

        String link = "https://fs.example/v1/briefings/" + id + "/runs/" + run.getId() + "?format=html";
        SLACK.verify(1, postRequestedFor(urlEqualTo("/slack/one")).withRequestBody(containing("Morning"))
                .withRequestBody(containing(link)).withRequestBody(containing("Tokyo Bay")));
        SLACK.verify(0, postRequestedFor(urlEqualTo("/slack/two")));
        assertThat(chat().requests()).as("no LLM").isEmpty();
        assertThat(briefingRepository.findById(id).orElseThrow().getNextRunAt())
                .isEqualTo(Instant.parse("2026-10-13T13:00:00Z"));
    }

    @Test
    @DisplayName("without channelIds every channel subscribed to briefing gets it, and an alerts-only channel does not")
    void everySubscribedChannel() {
        UUID one = slackChannel("one", SLACK.url("/slack/one"));
        UUID two = slackChannel("two", SLACK.url("/slack/two"));
        UUID alertsOnly = notify.channel("{'name':'alerts','type':'slack','events':['alert'],"
                + "'secrets':{'webhookUrl':'" + SLACK.url("/slack/alerts") + "'}}");
        UUID id = id(briefings.create(json(digestBody("All", "0 7 * * *", null)), agentCaller()));

        Map<String, Object> run = briefings.runNow(id);

        assertThat(run.get("deliveries")).isEqualTo(2);
        assertThat(notify.deliveries(one)).hasSize(1);
        assertThat(notify.deliveries(two)).hasSize(1);
        assertThat(notify.deliveries(alertsOnly)).isEmpty();
    }

    @Test
    @DisplayName("a completed run is embedded as a briefing document when semantic search is available, not otherwise")
    @SuppressWarnings("unchecked")
    void embeddedWhenSearchAvailable() {
        EmbeddingWorker embedder = mock(EmbeddingWorker.class);
        ObjectProvider<EmbeddingWorker> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(embedder);
        SearchStatusService status = mock(SearchStatusService.class);
        when(status.availability()).thenReturn(new SearchStatusService.Availability(true, null, null));
        BriefingRunner runner = new BriefingRunner(null, null, null, null, null, null, null, provider, status, null, null,
                null, mock(PlatformTransactionManager.class), Clock.systemUTC());
        BriefingRunEntity run = new BriefingRunEntity(UUID.randomUUID(), UUID.randomUUID(), "Morning", "digest", "UTC",
                T, T.minusSeconds(3600), T);
        run.setContentMarkdown("# Morning\n\nQuiet.");

        runner.embed(run);
        verify(embedder).embedDocument(eq(DocInput.BRIEFING), eq(run.getId().toString()),
                eq("Morning\n\n# Morning\n\nQuiet."), isNull(), eq(T), isNull(), isNull());

        EmbeddingWorker idle = mock(EmbeddingWorker.class);
        when(provider.getIfAvailable()).thenReturn(idle);
        when(status.availability()).thenReturn(new SearchStatusService.Availability(false, "no provider", null));
        runner.embed(run);
        verify(idle, never()).embedDocument(anyString(), anyString(), anyString(), any(), any(), any(), any());
    }
}
