package ai.foresignal.exercise;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.NotificationChannel;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotifyFixtures;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.push.NativeDeviceTarget;
import ai.foresignal.notify.push.PushMessage;
import ai.foresignal.notify.push.PushOptions;
import ai.foresignal.notify.push.PushPayloadBudget;
import ai.foresignal.notify.push.PushPayloads;
import ai.foresignal.notify.push.Urgency;
import ai.foresignal.notify.push.apns.ApnsPayloads;
import ai.foresignal.notify.push.fcm.FcmPayloads;
import ai.foresignal.notify.types.NtfyChannel;
import ai.foresignal.notify.types.SlackChannel;
import ai.foresignal.notify.types.TelegramChannel;
import ai.foresignal.notify.types.WebhookChannel;
import ai.foresignal.trigger.AlertView;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Exercise marking (prompt 115): the one helper marks the title and body; every renderer that serializes the
 * message carries the marker; webhooks get the header and payload field; push payloads carry {@code exercise}
 * with urgency forced to normal and no time-sensitive or high-priority delivery; CAP status is Exercise.
 */
class ExerciseMarkingTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant FROM = Instant.parse("2026-10-01T00:00:00Z");
    static final Instant TO = Instant.parse("2026-10-02T06:30:00Z");

    static NotificationMessage exercise(String severity) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("exercise", Map.of("runId", "01JEXERCISE0000000000000000", "from", FROM.toString(), "to", TO.toString()));
        NotificationMessage base = NotifyFixtures.message(severity, "Body text");
        return ExerciseMarking.mark(new NotificationMessage(base.id(), base.event(), base.subtype(), base.title(),
                base.shortTitle(), base.bodyMarkdown(), base.severity(), base.url(), base.links(), data, base.tags(),
                base.attachments(), base.updateOf(), base.origin(), base.createdAt(), base.alertId(), base.ruleId(),
                base.briefingRunId(), true));
    }

    static ChannelSettings settings(Map<String, Object> config, Map<String, String> secrets) {
        return NotifyFixtures.settings(config, secrets);
    }

    static String text(PreparedRequest request) {
        return ((PreparedRequest.Bytes) request.body()).text();
    }

    @Test
    @DisplayName("the title prefix and footer are added once; a real message is untouched")
    void titleAndFooter() {
        NotificationMessage marked = exercise("warning");
        assertThat(marked.title()).startsWith(ExerciseMarking.TITLE_PREFIX);
        assertThat(marked.shortTitle()).startsWith(ExerciseMarking.TITLE_PREFIX);
        assertThat(marked.bodyMarkdown()).endsWith("This is an exercise replay of data from 2026-10-01 00:00 UTC–"
                + "2026-10-02 06:30 UTC. No action required.");
        NotificationMessage twice = ExerciseMarking.mark(marked);
        assertThat(twice.title()).isEqualTo(marked.title());
        assertThat(twice.bodyMarkdown()).isEqualTo(marked.bodyMarkdown());
        NotificationMessage real = NotifyFixtures.message();
        assertThat(ExerciseMarking.mark(real)).isSameAs(real);
        assertThat(ExerciseMarking.title("x")).isEqualTo("[EXERCISE] x");
        assertThat(ExerciseMarking.footer(null, null)).contains("unknown–unknown");
        assertThat(ExerciseMarking.body(" ", FROM, TO)).isEqualTo(ExerciseMarking.footer(FROM, TO));
        // withText (a channel template) keeps the flag, so the worker marks the re-rendered text again.
        assertThat(marked.withText("templated", "body").exercise()).isTrue();
        assertThat(ExerciseMarking.mark(marked.withText("templated", "body")).title()).isEqualTo("[EXERCISE] templated");
    }

    @Test
    @DisplayName("webhook: X-Foresignal-Exercise: true and exercise: true in the payload and message")
    void webhook() {
        PreparedRequest request = new WebhookChannel().render(exercise("warning"),
                settings(Map.of("url", "https://hook.example.invalid/x"), Map.of()));
        assertThat(request.headers()).containsEntry(ExerciseMarking.WEBHOOK_HEADER, "true");
        JsonNode body = JSON.readTree(text(request));
        assertThat(body.get("exercise").asBoolean()).isTrue();
        assertThat(body.at("/message/exercise").asBoolean()).isTrue();
        assertThat(body.at("/message/title").asString()).startsWith(ExerciseMarking.TITLE_PREFIX);
        PreparedRequest real = new WebhookChannel().render(NotifyFixtures.message(),
                settings(Map.of("url", "https://hook.example.invalid/x"), Map.of()));
        assertThat(real.headers()).doesNotContainKey(ExerciseMarking.WEBHOOK_HEADER);
        assertThat(JSON.readTree(text(real)).has("exercise")).isFalse();
    }

    @Test
    @DisplayName("text channels (ntfy, Telegram, Slack) carry the [EXERCISE] title and the footer")
    void textChannels() {
        NotificationMessage marked = exercise("critical");
        PreparedRequest ntfy = new NtfyChannel().render(marked, settings(Map.of("topic", "alerts"), Map.of()));
        String ntfyText = (ntfy.body() instanceof PreparedRequest.Bytes bytes ? bytes.text() : "") + ntfy.headers();
        assertThat(ntfyText).contains("[EXERCISE]").contains("No action required.");
        String telegram = text(new TelegramChannel().render(marked, settings(Map.of("chatId", "-1"),
                Map.of("botToken", "123456:TESTTOKEN"))));
        assertThat(telegram).contains("[EXERCISE]").contains("No action required.");
        String slack = text(new SlackChannel().render(marked, settings(Map.of(),
                Map.of("webhookUrl", "https://hooks.slack.example.invalid/T/B/X"))));
        assertThat(slack).contains("[EXERCISE]").contains("No action required.");
    }

    @Test
    @DisplayName("push: exercise in every payload, urgency normal, APNs active and FCM NORMAL even for critical")
    void push() {
        PushMessage push = new PushMessage("alert", 9L, 7L, null, "critical", "[EXERCISE] Quake", "[EXERCISE] Quake",
                "body", "https://fs.example/v1/alerts/7", null, null, List.of(), "tag", null, false, 0L, null, null,
                null, null, List.of(), null, null, true);
        PushOptions options = PushOptions.of(push, false);
        assertThat(options.urgency()).isEqualTo(Urgency.NORMAL);
        assertThat(PushOptions.of(push, true).urgency()).isEqualTo(Urgency.VERY_LOW);
        JsonNode web = JSON.readTree(new PushPayloadBudget(JSON).fit(push, PushPayloads.FORESIGNAL, "en"));
        assertThat(web.get("exercise").asBoolean()).isTrue();
        Map<String, Object> aps = ApnsPayloads.aps(push, options, NativeDeviceTarget.SHORT, true);
        assertThat(aps.get("interruption-level")).isEqualTo("active");
        assertThat(ApnsPayloads.ids(push, false)).containsEntry("exercise", true);
        assertThat(FcmPayloads.priority(push, options)).isEqualTo("NORMAL");
        assertThat(FcmPayloads.data(push, NativeDeviceTarget.SHORT)).containsEntry("exercise", "true");
        assertThat(PushMessage.fromMap(push.toMap()).exercise()).isTrue();
        PushMessage real = new PushMessage("alert", 9L, 7L, null, "critical", "Quake", "Quake", "body",
                "https://fs.example/v1/alerts/7", null, null, List.of(), "tag", null, false, 0L, null, null, null, null,
                List.of(), null, null);
        assertThat(PushOptions.of(real, false).urgency()).isEqualTo(Urgency.HIGH);
        assertThat(FcmPayloads.priority(real, PushOptions.of(real, false))).isEqualTo("HIGH");
        assertThat(ApnsPayloads.aps(real, PushOptions.of(real, false), NativeDeviceTarget.SHORT, true)
                .get("interruption-level")).isEqualTo("time-sensitive");
    }

    @Test
    @DisplayName("CAP: Exercise for an exercise alert (column or 112 context flag), Actual otherwise")
    void cap() {
        assertThat(ExerciseMarking.capStatus(alert(true, null), JSON)).isEqualTo("Exercise");
        assertThat(ExerciseMarking.capStatus(alert(false, "{\"exercise\":true}"), JSON)).isEqualTo("Exercise");
        assertThat(ExerciseMarking.capStatus(alert(false, null), JSON)).isEqualTo("Actual");
    }

    @Test
    @DisplayName("routing: an exercise reaches only opted-in or named channels; the context names them")
    void routing() {
        NotificationChannel optedIn = channel(true);
        NotificationChannel other = channel(false);
        assertThat(ExerciseMarking.routes(optedIn, List.of())).isTrue();
        assertThat(ExerciseMarking.routes(other, List.of())).isFalse();
        assertThat(ExerciseMarking.routes(other, List.of(other.id().toString()))).isTrue();
        AlertView alert = alert(true, "{\"exercise\":true,\"exerciseRunId\":\"R\",\"exerciseChannelIds\":[\"c1\"],"
                + "\"exerciseEnrich\":true,\"exerciseWindow\":{\"from\":\"" + FROM + "\",\"to\":\"" + TO + "\"}}");
        assertThat(ExerciseMarking.namedChannels(alert, JSON)).containsExactly("c1");
        assertThat(ExerciseMarking.enrichAllowed(alert, JSON)).isTrue();
        assertThat(ExerciseMarking.dataMember(alert, JSON)).containsEntry("from", FROM.toString())
                .containsEntry("to", TO.toString());
    }

    @Test
    @DisplayName("speeds: wire values parse; caps are 60 s ÷ factor")
    void speeds() {
        assertThat(ExerciseSpeed.fromWire("X10")).isEqualTo(ExerciseSpeed.X10);
        assertThat(ExerciseSpeed.REALTIME.cap()).hasSeconds(60);
        assertThat(ExerciseSpeed.X60.cap()).hasSeconds(1);
        assertThat(ExerciseSpeed.MAX.cap()).isZero();
        assertThat(ExerciseSpeed.WIRE_VALUES).containsExactly("realtime", "x10", "x60", "max");
    }

    static NotificationChannel channel(boolean acceptExercise) {
        return new NotificationChannel(UUID.randomUUID(), "c", "webhook", Map.of(), List.of(), List.of("alert"), null,
                "info", null, null, 0, true, null, null, 0, 20, true, 0, Instant.EPOCH, Instant.EPOCH, acceptExercise);
    }

    static AlertView alert(boolean exercise, String context) {
        return new AlertView(1L, UUID.randomUUID(), "rule", 5L, "s", "e", "k", Instant.EPOCH, Instant.EPOCH, "m",
                "warning", "firing", 1L, null, null, null, null, null, null, null, null, null, false, null, List.of(),
                null, null, null, null, false, context, null, exercise, exercise ? "R" : null);
    }
}
