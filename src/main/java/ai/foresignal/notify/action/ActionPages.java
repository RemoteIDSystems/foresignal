package ai.foresignal.notify.action;

import ai.foresignal.settings.Durations;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.web.util.HtmlUtils;

/**
 * The server-rendered pages of {@code /v1/actions/{token}} (prompt 033): the confirm page (one
 * {@code <form method="post">} button), the result page and the gone page. No JavaScript and no
 * external assets, so the strict {@link #CONTENT_SECURITY_POLICY} holds; every alert text is
 * HTML-escaped.
 */
public final class ActionPages {

    /** The Content-Security-Policy of every action response. */
    public static final String CONTENT_SECURITY_POLICY = "default-src 'none'; style-src 'unsafe-inline'; form-action 'self'";

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'", Locale.US)
            .withZone(ZoneOffset.UTC);

    private static final String STYLE = "body{font-family:system-ui,sans-serif;max-width:32rem;margin:2rem auto;"
            + "padding:0 1rem;color:#111}h1{font-size:1.25rem}.sev{display:inline-block;padding:.1rem .5rem;"
            + "border-radius:.25rem;background:#eee}.critical{background:#fecaca}.warning{background:#fde68a}"
            + "button{font-size:1.1rem;padding:.6rem 1.2rem;margin-top:1rem}p.muted{color:#555}"
            + "@media (prefers-color-scheme:dark){body{background:#111;color:#eee}.sev{background:#333}}";

    private ActionPages() {}

    /** The confirm page of {@code confirm}: what will happen, and one POST button. */
    public static String confirm(ActionService.Confirm confirm) {
        String verb = verb(confirm.action(), confirm.duration());
        String title = confirm.alert().ruleName() == null ? "Alert " + confirm.alert().id() : confirm.alert().ruleName();
        StringBuilder body = new StringBuilder()
                .append("<h1>").append(escape(verb)).append("?</h1>")
                .append("<p><strong>").append(escape(title)).append("</strong></p>")
                .append("<p>").append(escape(confirm.alert().message())).append("</p>")
                .append("<p><span class=\"sev ").append(escape(confirm.alert().severity())).append("\">")
                .append(escape(confirm.alert().severity())).append("</span> fired ")
                .append(escape(time(confirm.alert().firedAt()))).append("</p>")
                .append("<form method=\"post\"><button type=\"submit\">").append(escape(verb)).append("</button></form>")
                .append("<p class=\"muted\">This link works once and expires ").append(escape(time(confirm.expiresAt())))
                .append(". Opening it did not change anything.</p>");
        return page("Foresignal: " + verb, body.toString());
    }

    /** The page after an execution. */
    public static String result(ActionService.Result result) {
        String text = switch (result.result()) {
            case "acknowledged" -> "The alert is acknowledged.";
            case "snoozed" -> "The alert is snoozed.";
            case "muted" -> "The watch is muted.";
            case "rule-deleted" -> "The watch of this alert was deleted; nothing was muted.";
            default -> "Nothing changed: the alert is " + result.result().replace("already-", "already ") + ".";
        };
        return page("Foresignal: done", "<h1>" + escape(text) + "</h1><p class=\"muted\">Alert "
                + result.alertId() + ". You can close this page.</p>");
    }

    /** The page of an unknown, expired or used link. */
    public static String gone() {
        return page("Foresignal: link expired", "<h1>This action link expired or was already used.</h1>"
                + "<p class=\"muted\">Open the alert in Foresignal to act on it.</p>");
    }

    /** The page of a refused cross-site request. */
    public static String forbidden() {
        return page("Foresignal: refused", "<h1>This request came from another site and was refused.</h1>");
    }

    /** The page of a rate-limited request. */
    public static String tooMany(long retryAfterSeconds) {
        return page("Foresignal: slow down", "<h1>Too many requests.</h1><p class=\"muted\">Try again in "
                + retryAfterSeconds + " s.</p>");
    }

    static String verb(String action, java.time.Duration duration) {
        return switch (action) {
            case "snooze" -> "Snooze for " + Durations.format(duration);
            case "mute" -> "Mute the watch for " + Durations.format(duration);
            default -> "Acknowledge";
        };
    }

    private static String page(String title, String body) {
        return "<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<meta name=\"robots\" content=\"noindex\"><title>" + escape(title) + "</title><style>" + STYLE
                + "</style></head><body>" + body + "</body></html>";
    }

    private static String time(Instant at) {
        return at == null ? "" : TIME.format(at);
    }

    /** HTML-escapes {@code text}; null becomes empty. */
    static String escape(String text) {
        return text == null ? "" : HtmlUtils.htmlEscape(text, "UTF-8");
    }
}
