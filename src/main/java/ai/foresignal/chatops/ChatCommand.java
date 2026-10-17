package ai.foresignal.chatops;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.http.HttpMethod;

/**
 * The chat commands (prompt 061). Each names the REST operation it mirrors ({@link #method}, {@link #path}):
 * the router allows it only when {@code AccessPolicy} would allow that operation to a token of the chat's
 * scope, so {@code read} chats cannot acknowledge, resolve, snooze, confirm or run briefings.
 */
public enum ChatCommand {
    /** Commands available at this chat's scope. */
    HELP("help", HttpMethod.GET, "/v1/system", List.of(), "Commands available here"),
    /** The 038 glance as text. */
    STATUS("status", HttpMethod.GET, "/v1/glance", List.of(), "Overall status"),
    /** Open alerts. */
    ALERTS("alerts", HttpMethod.GET, "/v1/alerts", List.of("n", "severity"), "Open alerts: /alerts [n] [critical|warning|info]"),
    /** One alert. */
    ALERT("alert", HttpMethod.GET, "/v1/alerts/1", List.of("id"), "Alert detail: /alert <id>"),
    /** Acknowledges an alert. */
    ACK("ack", HttpMethod.POST, "/v1/alerts/1/ack", List.of("id"), "Acknowledge: /ack <id>"),
    /** Resolves an alert. */
    RESOLVE("resolve", HttpMethod.POST, "/v1/alerts/1/resolve", List.of("id"), "Resolve: /resolve <id>"),
    /** Snoozes an alert. */
    SNOOZE("snooze", HttpMethod.POST, "/v1/alerts/1/snooze", List.of("id", "duration"), "Snooze: /snooze <id> <duration>"),
    /** Records an alert's quality feedback (prompt 115). */
    FEEDBACK("feedback", HttpMethod.PUT, "/v1/alerts/1/feedback", List.of("id", "value"),
            "Feedback: /feedback <id> useful|noise|late|false_positive"),
    /** Asks a question (028). */
    ASK("ask", HttpMethod.POST, "/v1/ask", List.of("question"), "Ask a question: /ask <question>, /ask new"),
    /** The latest briefing run, or runs one ({@code /brief run}, checked separately). */
    BRIEF("brief", HttpMethod.GET, "/v1/briefings/1/runs", List.of("briefing"), "Latest briefing: /brief [id|name]"),
    /** Degraded and failing sources. */
    SOURCES("sources", HttpMethod.GET, "/v1/sources/health", List.of(), "Degraded and failing sources"),
    /** Confirms a proposed Ask action by code. */
    CONFIRM("confirm", HttpMethod.POST, "/v1/ask/actions/1/confirm", List.of("code"), "Confirm a proposed action: /confirm <code>"),
    /** Rejects a proposed Ask action by code. */
    REJECT("reject", HttpMethod.POST, "/v1/ask/actions/1/reject", List.of("code"), "Reject a proposed action: /reject <code>"),
    /** Open forecast questions (prompt 122). */
    QUESTIONS("questions", HttpMethod.GET, "/v1/forecasts/questions", List.of(), "Open forecast questions"),
    /** Records the operator's binary forecast (prompt 122). */
    FORECAST("forecast", HttpMethod.POST, "/v1/forecasts/questions/1/forecasts", List.of("questionId", "probability"),
            "Forecast: /forecast <questionId> <probability>");

    /** The rule {@code /brief run} is checked against. */
    public static final String BRIEF_RUN_PATH = "/v1/briefings/1/run";

    private final String word;
    private final HttpMethod method;
    private final String path;
    private final List<String> argumentNames;
    private final String help;

    ChatCommand(String word, HttpMethod method, String path, List<String> argumentNames, String help) {
        this.word = word;
        this.method = method;
        this.path = path;
        this.argumentNames = argumentNames;
        this.help = help;
    }

    /** The command word without the slash. */
    public String word() {
        return word;
    }

    /** The method of the mirrored operation. */
    public HttpMethod method() {
        return method;
    }

    /** A path of the mirrored operation, for the {@code AccessPolicy} lookup. */
    public String path() {
        return path;
    }

    /** The names of its arguments (audited instead of their values). */
    public List<String> argumentNames() {
        return argumentNames;
    }

    /** The help line. */
    public String help() {
        return help;
    }

    /** The command named {@code word} (case-insensitive), if any. */
    public static Optional<ChatCommand> of(String word) {
        if (word == null) {
            return Optional.empty();
        }
        String lower = word.toLowerCase(Locale.ROOT);
        for (ChatCommand command : values()) {
            if (command.word.equals(lower)) {
                return Optional.of(command);
            }
        }
        return Optional.empty();
    }
}
