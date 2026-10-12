package ai.foresignal.notify.types;

import ai.foresignal.notify.ChannelFieldSpec;
import ai.foresignal.notify.ChannelRateLimit;
import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.ChannelTypeSpec;
import ai.foresignal.notify.MarkdownRenderer;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.Truncator;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * {@code zulip}: {@code POST {site}/api/v1/messages}, form {@code type=stream}, {@code to},
 * {@code topic}, {@code content}, with Basic auth of the bot email and its secret API key.
 *
 * <p>The topic comes from {@code topicTemplate} (default {@code {{ruleName}}}; also {@code {{severity}}},
 * {@code {{event}}}, {@code {{subtype}}} and {@code {{title}}}), so each rule gets its own topic and
 * follow-ups land in the same topic. Topics are cut to Zulip's 60 characters.
 */
@Component
public class ZulipChannel extends AbstractChannelType {

    /** The type id. */
    public static final String ID = "zulip";

    /** The default topic template. */
    public static final String DEFAULT_TOPIC = "{{ruleName}}";

    /** Zulip's topic length limit. */
    static final int TOPIC_MAX = 60;

    /** The content limit used here (Zulip's default is 10,000 characters). */
    static final int CONTENT_MAX = 10_000;

    private static final ChannelTypeSpec SPEC = new ChannelTypeSpec(ID, "Zulip",
            "Zulip stream message, one topic per rule",
            List.of(ChannelFieldSpec.required("site", ChannelFieldSpec.URL, "Zulip site URL"),
                    ChannelFieldSpec.required("stream", ChannelFieldSpec.STRING, "Stream (channel) name"),
                    ChannelFieldSpec.required("botEmail", ChannelFieldSpec.STRING, "Bot email address"),
                    ChannelFieldSpec.optional("topicTemplate", ChannelFieldSpec.STRING, "Topic template", DEFAULT_TOPIC)),
            List.of(ChannelFieldSpec.required("apiKey", ChannelFieldSpec.STRING, "Bot API key")),
            null);

    @Override
    public String id() {
        return ID;
    }

    @Override
    public ChannelTypeSpec spec() {
        return SPEC;
    }

    @Override
    public ChannelRateLimit rateLimit() {
        return ChannelRateLimit.perMinute(200);
    }

    @Override
    public PreparedRequest render(NotificationMessage m, ChannelSettings s) {
        StringBuilder content = new StringBuilder("**")
                .append(MarkdownRenderer.escape(m.title(), MarkdownRenderer.Format.ZULIP)).append("**");
        String body = MarkdownRenderer.render(m.bodyMarkdown(), MarkdownRenderer.Format.ZULIP);
        if (!body.isBlank()) {
            content.append("\n\n").append(body);
        }
        for (NotificationMessage.Link link : buttons(m)) {
            content.append("\n[").append(MarkdownRenderer.escape(link.label(), MarkdownRenderer.Format.ZULIP))
                    .append("](").append(link.url()).append(')');
        }
        Map<String, String> form = new LinkedHashMap<>();
        form.put("type", "stream");
        form.put("to", s.config("stream", ""));
        form.put("topic", topic(s.config("topicTemplate", DEFAULT_TOPIC), m));
        form.put("content", Truncator.truncate(content.toString(), CONTENT_MAX, Truncator.Unit.CHARS,
                Truncator.Mode.MARKDOWN, m.url()));
        String credentials = s.config("botEmail", "") + ":" + s.secret("apiKey");
        Map<String, String> auth = Map.of("Authorization",
                "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8)));
        return form(ChannelSettings.trimSlash(s.config("site", "")) + "/api/v1/messages", form, auth, s);
    }

    @Override
    public Optional<String> extractRef(ChannelResponse response) {
        return refAt(response, "id");
    }

    /** The rendered topic: never empty, at most 60 characters. */
    static String topic(String template, NotificationMessage m) {
        String ruleName = m.data() != null && m.data().get("ruleName") instanceof String name ? name : null;
        String out = template
                .replace("{{ruleName}}", ruleName == null ? (m.event().equals("alert") ? "Alerts" : "Foresignal") : ruleName)
                .replace("{{severity}}", m.severity())
                .replace("{{event}}", m.event())
                .replace("{{subtype}}", m.subtype())
                .replace("{{title}}", m.title())
                .replaceAll("\\{\\{[^}]*}}", "")
                .replace('\n', ' ')
                .strip();
        if (out.isEmpty()) {
            out = "Foresignal";
        }
        return Truncator.truncate(out, TOPIC_MAX, Truncator.Unit.CHARS, Truncator.Mode.PLAIN, null);
    }
}
