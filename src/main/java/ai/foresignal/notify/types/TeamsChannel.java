package ai.foresignal.notify.types;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.notify.ChannelFieldSpec;
import ai.foresignal.notify.ChannelRateLimit;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.ChannelTypeSpec;
import ai.foresignal.notify.MarkdownRenderer;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.Truncator;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * {@code teams}: a Power Automate Workflows webhook receiving an Adaptive Card 1.5
 * ({@code {"type":"message","attachments":[{"contentType":"application/vnd.microsoft.card.adaptive",
 * "content": …}]}}).
 *
 * <p>Office 365 connector webhooks were retired in Teams in May 2026 (Weather-Imagery-Design §8), so
 * a {@code *.webhook.office.com} or {@code outlook.office.com/webhook} URL is refused with a message
 * telling the operator to create a Workflows webhook. Lifecycle updates are follow-ups.
 */
@Component
public class TeamsChannel extends AbstractChannelType {

    /** The type id. */
    public static final String ID = "teams";

    /** The refusal for a retired connector URL. */
    public static final String RETIRED_CONNECTOR = "Office 365 connector webhooks were retired in Teams in May 2026; "
            + "create a Workflows webhook (Power Automate: \"Post to a channel when a webhook request is received\") "
            + "and use its URL";

    /** The card text limit used here (Teams rejects messages above about 28 KB). */
    static final int TEXT_MAX = 20_000;

    private static final ChannelTypeSpec SPEC = new ChannelTypeSpec(ID, "Microsoft Teams",
            "Power Automate Workflows webhook with an Adaptive Card (connector webhooks are refused)",
            List.of(),
            List.of(ChannelFieldSpec.required("workflowUrl", ChannelFieldSpec.URL, "Workflows webhook URL")),
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
        return ChannelRateLimit.perSecond(1);
    }

    @Override
    public List<FieldError> validate(Map<String, Object> config, Map<String, String> secrets) {
        String url = secrets.get("workflowUrl");
        if (url != null && isRetiredConnector(url)) {
            return List.of(new FieldError("secrets.workflowUrl", RETIRED_CONNECTOR));
        }
        return List.of();
    }

    /** True for an Office 365 connector URL. */
    static boolean isRetiredConnector(String url) {
        try {
            URI uri = URI.create(url.strip());
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            String path = uri.getPath() == null ? "" : uri.getPath().toLowerCase(Locale.ROOT);
            return host.equals("webhook.office.com") || host.endsWith(".webhook.office.com")
                    || (host.equals("outlook.office.com") && path.startsWith("/webhook"));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public PreparedRequest render(NotificationMessage m, ChannelSettings s) {
        List<Object> body = new ArrayList<>();
        body.add(obj("type", "TextBlock", "text", m.title(), "weight", "Bolder", "size", "Medium", "wrap", true,
                "color", switch (m.severity()) {
                    case "critical" -> "Attention";
                    case "warning" -> "Warning";
                    default -> "Default";
                }));
        String text = MarkdownRenderer.render(m.bodyMarkdown(), MarkdownRenderer.Format.ADAPTIVE_CARD);
        if (!text.isBlank()) {
            body.add(obj("type", "TextBlock", "text",
                    Truncator.truncate(text, TEXT_MAX, Truncator.Unit.CHARS, Truncator.Mode.MARKDOWN, m.url()),
                    "wrap", true));
        }
        body.add(obj("type", "TextBlock", "text", m.severity() + " · " + displayTime(m, s), "isSubtle", true,
                "size", "Small", "wrap", true));
        if (m.imageUrl() != null) {
            body.add(obj("type", "Image", "url", m.imageUrl(), "altText", m.title()));
        }
        List<Object> actions = new ArrayList<>();
        for (NotificationMessage.Link link : buttons(m)) {
            actions.add(obj("type", "Action.OpenUrl", "title", link.label(), "url", link.url()));
        }
        Map<String, Object> card = obj(
                "$schema", "http://adaptivecards.io/schemas/adaptive-card.json",
                "type", "AdaptiveCard",
                "version", "1.5",
                "body", body,
                "actions", actions.isEmpty() ? null : actions);
        Map<String, Object> payload = obj("type", "message", "attachments", List.of(obj(
                "contentType", "application/vnd.microsoft.card.adaptive",
                "content", card)));
        return json("POST", s.secret("workflowUrl"), null, payload, s);
    }
}
