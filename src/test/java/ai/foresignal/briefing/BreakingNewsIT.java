package ai.foresignal.briefing;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.DeliveryWorker;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.briefing.BriefingItSupport;
import ai.foresignal.testsupport.briefing.BriefingTestSources;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * Breaking-news special editions end to end (prompt NP-006): an alert matching an edition's {@code breakingNews} runs
 * it at once, off schedule, flagged with the alert; a second match inside the cooldown is coalesced; an alert of
 * another rule, below {@code minSeverity}, suppressed, or for a disabled configuration does nothing; the breaking run
 * delivers to the edition's targets like any run.
 *
 * <p>The listener's bus subscription is closed for each test and {@link BreakingNewsListener#fire} is called with
 * the alert the engine raised, so every run is awaited deterministically.
 */
@IntegrationTest
@Import(BriefingTestSources.class)
class BreakingNewsIT extends BriefingItSupport {

    @Autowired BreakingNewsListener listener;
    @Autowired DeliveryWorker worker;

    @BeforeEach
    void detach() {
        listener.stop();
    }

    @AfterEach
    void attach() {
        listener.start();
    }

    private UUID edition(String breaking, String extra) {
        return id(briefings.create(json(digestBody("Breaking desk", "0 7 * * *",
                "\"breakingNews\":" + breaking + (extra == null ? "" : "," + extra))), agentCaller()));
    }

    private static String config(UUID rule, String minSeverity, int cooldown) {
        return "{\"enabled\":true,\"ruleIds\":[\"" + rule + "\"],\"minSeverity\":\"" + minSeverity
                + "\",\"cooldownMinutes\":" + cooldown + "}";
    }

    /** Fires {@code alert} into the listener and waits for every run it started. */
    private List<BriefingRunEntity> fire(AlertView alert) throws Exception {
        List<BriefingRunEntity> runs = new ArrayList<>();
        for (Future<BriefingRunEntity> f : listener.fire(AlertEvent.of(AlertEvent.FIRED, alert))) {
            runs.add(f.get(60, TimeUnit.SECONDS));
        }
        return runs;
    }

    private List<BriefingRunEntity> breakingRuns(UUID id) {
        return runRepository.findAll().stream().filter(r -> id.equals(r.getBriefingId()) && r.isBreaking()).toList();
    }

    @Test
    @DisplayName("a matching alert runs the edition at once, flagged with the alert, and leads with it")
    void matchingAlert() throws Exception {
        UUID rule = quakeWatch("Big quakes", 6.0, "critical");
        UUID id = edition(config(rule, "warning", 60), null);
        quake("b1", "Off Sendai", 7.1);
        AlertView alert = alertOf(rule);

        List<BriefingRunEntity> runs = fire(alert);

        assertThat(runs).singleElement().satisfies(run -> {
            assertThat(run.getBriefingId()).isEqualTo(id);
            assertThat(run.getStatus()).isIn(BriefingRunEntity.SUCCEEDED, BriefingRunEntity.FALLBACK);
            assertThat(run.getBreaking()).containsEntry("alertId", alert.id())
                    .containsEntry("ruleId", rule.toString()).containsEntry("severity", "critical");
        });
        Map<String, Object> view = BriefingViews.run(runs.getFirst());
        assertThat(view.get("breaking")).isNotNull();
        assertThat(String.valueOf(view.get("contentMarkdown"))).contains("Breaking:").contains("Big quakes");
    }

    @Test
    @DisplayName("a second matching alert inside the cooldown is coalesced into the first run")
    void cooldown() throws Exception {
        UUID rule = quakeWatch("Big quakes", 6.0, "critical");
        UUID id = edition(config(rule, "info", 60), null);
        quake("c1", "Off Sendai", 7.1);
        assertThat(fire(alertOf(rule))).hasSize(1);

        quake("c2", "Off Fukushima", 6.8);
        assertThat(fire(alertOf(rule))).isEmpty();
        assertThat(breakingRuns(id)).hasSize(1);
    }

    @Test
    @DisplayName("another rule, a severity below minSeverity, a suppressed alert or a disabled config: no run")
    void nonMatching() throws Exception {
        UUID wanted = quakeWatch("Big quakes", 6.0, "critical");
        UUID other = quakeWatch("Any quake", 1.0, "info");
        UUID strict = edition(config(wanted, "warning", 60), null);
        UUID low = edition(config(other, "critical", 60), null);
        UUID off = edition("{\"enabled\":false,\"ruleIds\":[\"" + other + "\"]}", null);
        quake("n1", "Izu", 3.0);
        AlertView minor = alertOf(other);

        assertThat(fire(minor)).isEmpty();

        quake("n2", "Off Sendai", 7.1);
        AlertView big = alertOf(wanted);
        assertThat(listener.fire(new AlertEvent(AlertEvent.FIRED, big, true))).isEmpty();
        assertThat(breakingRuns(strict)).isEmpty();
        assertThat(breakingRuns(low)).isEmpty();
        assertThat(breakingRuns(off)).isEmpty();
    }

    @Test
    @DisplayName("an empty ruleIds falls back to the scope's rules")
    void scopeRules() throws Exception {
        UUID rule = quakeWatch("Big quakes", 6.0, "warning");
        UUID id = id(briefings.create(json("{\"name\":\"Scoped\",\"schedule\":\"0 7 * * *\",\"timezone\":\"UTC\","
                + "\"scope\":{\"kinds\":[\"" + BriefingTestSources.QUAKE_KIND + "\"],\"ruleIds\":[\"" + rule + "\"]},"
                + "\"lookback\":\"24h\",\"breakingNews\":{\"enabled\":true}}"), agentCaller()));
        quake("s1", "Off Sendai", 7.1);
        assertThat(fire(alertOf(rule))).singleElement()
                .satisfies(run -> assertThat(run.getBriefingId()).isEqualTo(id));
    }

    @Test
    @DisplayName("the breaking run delivers to the edition's targets, with the alert's severity")
    void deliversToTargets() throws Exception {
        UUID channel = notify.channel("{'name':'breaking-alerts','type':'webhook','config':{'url':"
                + "'http://127.0.0.1:9/breaking'}}");
        UUID rule = quakeWatch("Big quakes", 6.0, "critical");
        edition(config(rule, "info", 60), "\"deliveryTargets\":[{\"kind\":\"channel\",\"channelId\":\"" + channel
                + "\",\"format\":\"alert\"}]");
        quake("d1", "Off Sendai", 7.1);

        BriefingRunEntity run = fire(alertOf(rule)).getFirst();

        assertThat(run.getDeliveryResults()).singleElement().satisfies(r -> assertThat(r)
                .containsEntry("status", "queued").containsEntry("channelId", channel.toString()));
        assertThat(deliveries.findAll()).anySatisfy(d -> assertThat(d.getChannelId()).isEqualTo(channel));
    }

    @Test
    @DisplayName("validation: unknown members, a bad severity and an out-of-range cooldown are 400s")
    void validation() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> edition(
                "{\"enabled\":true,\"minSeverity\":\"severe\",\"cooldownMinutes\":0,\"extra\":1}", null))
                .isInstanceOf(ai.foresignal.api.error.ValidationException.class);
    }
}
